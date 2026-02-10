package com.example.examplemod.rocket.ship

import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.NbtUtils
import net.minecraft.core.registries.BuiltInRegistries
import com.example.examplemod.ExampleMod
import com.example.examplemod.network.RocketNetwork
import com.example.examplemod.network.RocketShipStatePacket
import com.example.examplemod.network.RocketShipStructurePacket
import com.example.examplemod.rocket.RocketDimensions
import com.example.examplemod.rocket.RocketDimensionTransition
import com.example.examplemod.rocket.core.RocketWABridge
import com.example.examplemod.rocket.core.RocketWACore
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.MoverType
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.BlockItem
import net.minecraft.world.level.Level
import net.minecraft.world.level.ClipContext
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.HitResult
import net.minecraft.world.phys.Vec3
import net.minecraft.tags.FluidTags
import com.simibubi.create.content.contraptions.AbstractContraptionEntity
import com.simibubi.create.content.contraptions.ContraptionCollider
import com.simibubi.create.content.contraptions.StructureTransform
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Axis
import net.minecraft.util.Mth
import org.joml.Matrix4d
import org.joml.Vector3d
import net.minecraftforge.network.PacketDistributor
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.piston.PistonBaseBlock
import net.minecraft.world.level.block.piston.PistonHeadBlock
import net.minecraft.core.Direction
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import kotlin.math.max
import kotlin.math.sqrt
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.abs
import org.apache.logging.log4j.LogManager

class RocketShipEntity(type: EntityType<out RocketShipEntity>, level: Level) : AbstractContraptionEntity(type, level) {
    companion object {
        private val LOGGER = LogManager.getLogger("RocketShip")

        private const val DT = 1.0 / 20.0
        private const val GRAVITY = 7.0
        private const val BUOYANCY_BASE = 9.0
        private const val BUOYANCY_PER_FLOATER = 3.0
        private const val AIR_DRAG = 0.02
        private const val WATER_DRAG = 0.12
        private const val MAX_FALL_SPEED = -20.0
        private const val MAX_THRUST_ACCEL = 15.0
        private const val FUEL_FLOW = 0.15

        private const val COM_TORQUE_FACTOR = 3.0
        private const val ANGULAR_DRAG = 0.08
        private const val MAX_COM_TILT = 180.0

        private const val PISTON_FORCE = 8.0
        private const val PISTON_TORQUE_FACTOR = 5.0
        private const val PISTON_SCAN_RANGE = 2

        private const val BOOSTER_SEPARATION_ALTITUDE = 60.0
        private const val BOOSTER_EJECT_SPEED = 0.6
        private const val BOOSTER_EJECT_UP = 0.15

        private const val STAGE_SEPARATION_ALTITUDE = 120.0
    }

    init {

        noCulling = true

        noPhysics = false
    }

    var shipId: Long = -1L
        private set
    private var anchor: Vec3 = Vec3.ZERO
    private var storage: RocketShipStorage = RocketShipStorage.empty()
    private var shipWorld: RocketShipWorld? = null
    private var buildOrigin: BlockPos = BlockPos.ZERO
    private var roll: Float = 0.0f
    private var prevRoll: Float = 0.0f
    private var velocity: Vec3 = Vec3.ZERO
    private var prevVelocity: Vec3 = Vec3.ZERO
    private val physicsServer = RocketShipPhysicsServer()

    private var angularVelPitch: Float = 0f
    private var angularVelRoll: Float = 0f
    private var angularVelYaw: Float = 0f

    private var renderPosX = 0.0; private var renderPosY = 0.0; private var renderPosZ = 0.0
    private var renderVelX = 0.0; private var renderVelY = 0.0; private var renderVelZ = 0.0
    private var lastFrameNano = 0L
    private var renderInWater = false
    private var renderFloaterCount = 0
    private var physicsBridge: RocketWABridge? = null
    private var clientLerpSteps = 0
    private var clientTargetPos: Vec3 = Vec3.ZERO
    private var clientTargetVel: Vec3 = Vec3.ZERO
    private var clientTargetYaw: Float = 0f
    private var clientTargetPitch: Float = 0f
    private var clientTargetRoll: Float = 0f
    private var structureSyncCooldown = 0
    private val createContraption = RocketShipContraption()
    private var lastPivotShift: Vec3 = Vec3.ZERO
    private var chunkManager: RocketShipChunkManager? = null

    private var lastStructureSignature: Int = 0
    private var hasReceivedNonEmptyStructure: Boolean = false
    private var hasSentNonEmptyStructure: Boolean = false

    private var collisionBoxCache: CollisionBoxCache? = null

    fun getCreateContraption(): RocketShipContraption = createContraption

    @Volatile
    var destinationPlanet: String = ""

    @Volatile
    private var desiredThrust: Vec3 = Vec3.ZERO

    @Volatile
    private var throttle: Double = 0.0

    @Volatile
    private var fuelMass: Double = 0.0

    private var boostersSeparated: Boolean = false
    private var launchOriginY: Double = Double.MIN_VALUE

    private var stageSeparated: Boolean = false

    fun getVelocityVec(): Vec3 = velocity

    fun launch(thrustPower: Double = 1.0, fuel: Double = 100.0) {

        this.desiredThrust = getLocalUp()
        this.throttle = thrustPower.coerceIn(0.0, 1.0)
        if (this.fuelMass <= 0.0) this.fuelMass = fuel

        if (launchOriginY == Double.MIN_VALUE) {
            launchOriginY = y
        }

        boostersSeparated = false
        stageSeparated = false

        LOGGER.info("[ROCKET-LAUNCH] shipId={} pos=({}, {}, {}) thrust={} throttle={} fuel={}",
            shipId, String.format("%.2f", x), String.format("%.2f", y), String.format("%.2f", z),
            desiredThrust, String.format("%.2f", throttle), String.format("%.2f", fuelMass))
    }

    private fun getLocalUp(): Vec3 {
        val pitchRad = Math.toRadians(xRot.toDouble())
        val yawRad = Math.toRadians(yRot.toDouble())
        val rollRad = Math.toRadians(roll.toDouble())

        var ux = -sin(rollRad)
        var uy = cos(rollRad)
        var uz = 0.0

        val uy2 = uy * cos(pitchRad) - uz * sin(pitchRad)
        val uz2 = uy * sin(pitchRad) + uz * cos(pitchRad)
        uy = uy2; uz = uz2

        val ux3 = ux * cos(yawRad) + uz * sin(yawRad)
        val uz3 = -ux * sin(yawRad) + uz * cos(yawRad)
        ux = ux3; uz = uz3
        return Vec3(ux, uy, uz).normalize()
    }

    private data class PistonForce(
        val linearForce: Vec3,
        val torquePitch: Float,
        val torqueYaw: Float,
        val torqueRoll: Float
    )

    private fun detectPistonForces(): PistonForce {
        val lvl = level()
        val bb = boundingBox ?: return PistonForce(Vec3.ZERO, 0f, 0f, 0f)
        val rocketCenter = Vec3(
            (bb.minX + bb.maxX) * 0.5,
            (bb.minY + bb.maxY) * 0.5,
            (bb.minZ + bb.maxZ) * 0.5
        )

        var totalForceX = 0.0
        var totalForceY = 0.0
        var totalForceZ = 0.0
        var totalTorquePitch = 0.0f
        var totalTorqueYaw = 0.0f
        var totalTorqueRoll = 0.0f

        val scanMinX = Mth.floor(bb.minX) - PISTON_SCAN_RANGE
        val scanMinY = Mth.floor(bb.minY) - PISTON_SCAN_RANGE
        val scanMinZ = Mth.floor(bb.minZ) - PISTON_SCAN_RANGE
        val scanMaxX = Mth.ceil(bb.maxX) + PISTON_SCAN_RANGE
        val scanMaxY = Mth.ceil(bb.maxY) + PISTON_SCAN_RANGE
        val scanMaxZ = Mth.ceil(bb.maxZ) + PISTON_SCAN_RANGE

        val mutablePos = BlockPos.MutableBlockPos()

        for (bx in scanMinX..scanMaxX) {
            for (by in scanMinY..scanMaxY) {
                for (bz in scanMinZ..scanMaxZ) {
                    mutablePos.set(bx, by, bz)
                    val state = lvl.getBlockState(mutablePos)

                    val facing: Direction
                    if (state.block is PistonHeadBlock) {
                        facing = state.getValue(PistonHeadBlock.FACING)
                    } else if (state.block is PistonBaseBlock && state.getValue(PistonBaseBlock.EXTENDED)) {
                        facing = state.getValue(PistonBaseBlock.FACING)
                    } else {
                        continue
                    }

                    val pushDir = Vec3(
                        facing.stepX.toDouble(),
                        facing.stepY.toDouble(),
                        facing.stepZ.toDouble()
                    )

                    val pistonCenter = Vec3(bx + 0.5, by + 0.5, bz + 0.5)
                    val rayEnd = pistonCenter.add(pushDir.scale(PISTON_SCAN_RANGE.toDouble() + 1.0))

                    val hitResult = bb.clip(pistonCenter, rayEnd)
                    if (hitResult.isEmpty) continue

                    val hitPoint = hitResult.get()

                    totalForceX += pushDir.x * PISTON_FORCE
                    totalForceY += pushDir.y * PISTON_FORCE
                    totalForceZ += pushDir.z * PISTON_FORCE

                    val rx = hitPoint.x - rocketCenter.x
                    val ry = hitPoint.y - rocketCenter.y
                    val rz = hitPoint.z - rocketCenter.z

                    val fx = pushDir.x * PISTON_FORCE
                    val fy = pushDir.y * PISTON_FORCE
                    val fz = pushDir.z * PISTON_FORCE
                    totalTorquePitch += (ry * fz - rz * fy).toFloat() * PISTON_TORQUE_FACTOR.toFloat()
                    totalTorqueYaw += (rz * fx - rx * fz).toFloat() * PISTON_TORQUE_FACTOR.toFloat()
                    totalTorqueRoll += (rx * fy - ry * fx).toFloat() * PISTON_TORQUE_FACTOR.toFloat()

                    LOGGER.debug("[ROCKET-PISTON] found piston at ({},{},{}) facing={} hitPoint=({},{},{})",
                        bx, by, bz, facing,
                        String.format("%.2f", hitPoint.x), String.format("%.2f", hitPoint.y), String.format("%.2f", hitPoint.z))
                }
            }
        }

        return PistonForce(
            Vec3(totalForceX, totalForceY, totalForceZ),
            totalTorquePitch,
            totalTorqueYaw,
            totalTorqueRoll
        )
    }

    fun stopThrust() {
        this.desiredThrust = Vec3.ZERO
        this.throttle = 0.0
    }

    private fun checkBoosterSeparation() {
        if (boostersSeparated) return
        if (launchOriginY == Double.MIN_VALUE) return

        val thrusting = desiredThrust.lengthSqr() > 1.0e-4 && throttle > 0.0 && fuelMass > 0.0
        if (!thrusting) return

        val altitude = y - launchOriginY
        if (altitude < BOOSTER_SEPARATION_ALTITUDE) return

        boostersSeparated = true

        val lvl = level()
        if (lvl.isClientSide) return

        val directions = arrayOf(
            Vec3(0.0, 0.0, -BOOSTER_EJECT_SPEED),
            Vec3(BOOSTER_EJECT_SPEED, 0.0, 0.0),
            Vec3(0.0, 0.0, BOOSTER_EJECT_SPEED),
            Vec3(-BOOSTER_EJECT_SPEED, 0.0, 0.0)
        )

        val rocketVelPerTick = Vec3(velocity.x * (1.0 / 20.0), velocity.y * (1.0 / 20.0), velocity.z * (1.0 / 20.0))

        val inheritedVel = Vec3(rocketVelPerTick.x * 0.3, BOOSTER_EJECT_UP, rocketVelPerTick.z * 0.3)

        for (i in 0..3) {
            val booster = RocketBoosterEntity(ExampleMod.ROCKET_BOOSTER_ENTITY.get(), lvl)

            val offsetX = directions[i].x * 3.0
            val offsetZ = directions[i].z * 3.0
            booster.setPos(x + offsetX, y - 1.0, z + offsetZ)
            booster.init(directions[i], inheritedVel, i)
            lvl.addFreshEntity(booster)
        }
    }

    private fun checkStageSeparation() {
        if (stageSeparated) return
        if (launchOriginY == Double.MIN_VALUE) return
        val thrusting = desiredThrust.lengthSqr() > 1.0e-4 && throttle > 0.0 && fuelMass > 0.0
        if (!thrusting) return

        val altitude = y - launchOriginY
        if (altitude < STAGE_SEPARATION_ALTITUDE) return

        var lowestSeparatorY = Int.MAX_VALUE
        var hasSeparator = false
        for ((pos, state) in storage.blocks) {
            if (state.`is`(ExampleMod.STAGE_SEPARATOR.get())) {
                hasSeparator = true
                if (pos.y < lowestSeparatorY) {
                    lowestSeparatorY = pos.y
                }
            }
        }
        if (!hasSeparator) return

        stageSeparated = true

        val lvl = level()
        if (lvl.isClientSide) return

        val blocksToRemove = mutableListOf<BlockPos>()
        for ((pos, _) in storage.blocks) {
            if (pos.y <= lowestSeparatorY) {
                blocksToRemove.add(pos)
            }
        }

        if (blocksToRemove.isEmpty()) return

        for (pos in blocksToRemove) {
            storage.blocks.remove(pos)
        }

        val booster = RocketBoosterEntity(ExampleMod.ROCKET_BOOSTER_ENTITY.get(), lvl)
        booster.setPos(x, y - 2.0, z)

        val inheritVel = Vec3(0.0, velocity.y * 0.02, 0.0)
        booster.init(Vec3.ZERO, inheritVel, 0)
        lvl.addFreshEntity(booster)

        shipWorld = RocketShipWorld(lvl, storage, this)
        chunkManager = RocketShipChunkManager(shipId, buildOrigin)
        rebuildCreateContraption()
        updateBoundingBox()

        structureSyncCooldown = 0

        for (passenger in passengers) {
            if (passenger is Player) {
                passenger.displayClientMessage(
                    net.minecraft.network.chat.Component.literal("§e§l[级间分离] §r§a下级火箭已分离！剩余 ${storage.blocks.size} 个方块"),
                    false
                )
            }
        }
    }

    private fun spawnExhaustParticles() {
        if (!level().isClientSide) return
        val thrusting = desiredThrust.lengthSqr() > 1.0e-4 && throttle > 0.0 && fuelMass > 0.0
        if (!thrusting) return

        val lvl = level()
        val rng = random
        val intensity = throttle.toFloat().coerceIn(0f, 1f)

        var minY = 0
        for ((pos, _) in storage.blocks) {
            if (pos.y < minY) minY = pos.y
        }
        val exhaustY = y + minY - 0.5

        val flameCount = (6 * intensity).toInt() + 2
        for (i in 0 until flameCount) {
            val ox = (rng.nextDouble() - 0.5) * 1.2
            val oz = (rng.nextDouble() - 0.5) * 1.2
            val vy = -(0.3 + rng.nextDouble() * 0.5) * intensity
            val vx = (rng.nextDouble() - 0.5) * 0.08
            val vz = (rng.nextDouble() - 0.5) * 0.08
            lvl.addParticle(
                net.minecraft.core.particles.ParticleTypes.FLAME,
                x + ox, exhaustY, z + oz,
                vx, vy, vz
            )
        }

        val soulCount = (3 * intensity).toInt() + 1
        for (i in 0 until soulCount) {
            val ox = (rng.nextDouble() - 0.5) * 0.6
            val oz = (rng.nextDouble() - 0.5) * 0.6
            val vy = -(0.2 + rng.nextDouble() * 0.4) * intensity
            lvl.addParticle(
                net.minecraft.core.particles.ParticleTypes.SOUL_FIRE_FLAME,
                x + ox, exhaustY + 0.2, z + oz,
                0.0, vy, 0.0
            )
        }

        val smokeCount = (4 * intensity).toInt() + 1
        for (i in 0 until smokeCount) {
            val ox = (rng.nextDouble() - 0.5) * 1.8
            val oz = (rng.nextDouble() - 0.5) * 1.8
            val vy = -(0.05 + rng.nextDouble() * 0.15)
            lvl.addParticle(
                net.minecraft.core.particles.ParticleTypes.LARGE_SMOKE,
                x + ox, exhaustY - 0.5 - rng.nextDouble() * 2.0, z + oz,
                (rng.nextDouble() - 0.5) * 0.06, vy, (rng.nextDouble() - 0.5) * 0.06
            )
        }

        if (intensity > 0.8f && rng.nextFloat() < 0.5f) {
            val ox = (rng.nextDouble() - 0.5) * 0.3
            val oz = (rng.nextDouble() - 0.5) * 0.3
            lvl.addParticle(
                net.minecraft.core.particles.ParticleTypes.EXPLOSION,
                x + ox, exhaustY - 1.5, z + oz,
                0.0, -0.1, 0.0
            )
        }

        if (rng.nextFloat() < 0.3f * intensity) {
            lvl.addParticle(
                net.minecraft.core.particles.ParticleTypes.CAMPFIRE_COSY_SMOKE,
                x + (rng.nextDouble() - 0.5) * 0.8,
                exhaustY - 2.0 - rng.nextDouble() * 3.0,
                z + (rng.nextDouble() - 0.5) * 0.8,
                0.0, -0.02, 0.0
            )
        }
    }

    fun getFuelMass(): Double = fuelMass
    fun getThrottle(): Double = throttle

    override fun canAddPassenger(passenger: Entity): Boolean =
        passenger is Player && passengers.isEmpty()

    fun configure(shipId: Long, storage: RocketShipStorage, anchorPos: BlockPos, initialFuel: Double) {
        this.shipId = shipId

        this.storage = if (storage.blocks is java.util.concurrent.ConcurrentHashMap) storage else RocketShipStorage.concurrentCopyOf(storage.blocks)
        this.buildOrigin = RocketDimensions.buildOriginFor(shipId)
        this.shipWorld = RocketShipWorld(level(), storage, this)
        this.fuelMass = initialFuel
        this.chunkManager = RocketShipChunkManager(shipId, buildOrigin)

        println("[RocketShip] Configuring ship $shipId with ${storage.blocks.size} blocks")

        rebuildCreateContraption()

        setContraption(createContraption)
        initialized = false
        prevPosInvalid = false
        createContraption.entity = this
        contraptionInitialize()

        if (level().isClientSide) {
            createContraption.invalidateClientContraptionStructure()
        }

        this.anchor = snapToGrid(
            Vec3(
                anchorPos.x.toDouble(),
                anchorPos.y.toDouble(),
                anchorPos.z.toDouble()
            )
        )

        physicsBridge = RocketWABridge(this, storage)
        physicsBridge?.initialize(shipId)
        physicsBridge?.syncToPhysics(anchor, Vec3.ZERO, yRot, xRot, roll)

        physicsServer.reset(anchor)
        setPos(anchor.x, anchor.y, anchor.z)

        xRot = 0f
        updateBoundingBox()

        println("[RocketShip] Ship configured at position: $anchor")
    }

    fun updateControl(forwardBody: Vec3, throttle: Double, fuelMass: Double, yaw: Float, pitch: Float, roll: Float) {
        this.desiredThrust = if (forwardBody.lengthSqr() > 1.0e-6) forwardBody.normalize() else Vec3.ZERO
        this.throttle = throttle
        this.fuelMass = fuelMass
        this.yRot = yaw
        this.xRot = pitch
        this.prevRoll = this.roll
        this.roll = roll
        physicsServer.updateOrientation(yaw, pitch, roll)
        updateBoundingBox()
    }

    fun updateControlFromPlayerInput(input: Vec3) {
        val throttle = kotlin.math.min(1.0, kotlin.math.max(0.0, input.y))
        if (throttle > 0.0) {

            this.desiredThrust = getLocalUp()
            this.throttle = throttle

            this.yRot += (input.x * 2.0f).toFloat()
            this.xRot += (input.z * 1.5f).toFloat()
            this.yRot = Mth.wrapDegrees(this.yRot)
            this.xRot = Mth.wrapDegrees(this.xRot)
        } else {
            this.throttle = 0.0
            this.desiredThrust = Vec3.ZERO
        }
        updateBoundingBox()
    }

    fun getShipWorld(): RocketShipWorld? {
        if (shipWorld != null) return shipWorld
        if (storage.blocks.isEmpty()) return null
        shipWorld = RocketShipWorld(level(), storage, this)
        return shipWorld
    }

    override fun getAnchorVec(): Vec3 = anchor

    fun getStorageBlocks(): Map<BlockPos, net.minecraft.world.level.block.state.BlockState> = storage.blocks

    fun getCenterOfMass(): Vec3 = storage.centerOfMass()

    fun getRotationPivot(): Vec3 {
        val bounds = storage.computeLocalBounds()
        return Vec3(
            (bounds.minX + bounds.maxX) * 0.5,
            (bounds.minY + bounds.maxY) * 0.5,
            (bounds.minZ + bounds.maxZ) * 0.5
        )
    }

    fun getBuildOrigin(): BlockPos = buildOrigin

    fun getRoll(): Float = roll

    fun getPrevRoll(): Float = prevRoll

    fun setRollDirect(value: Float) {
        prevRoll = roll
        roll = value
    }

    fun setAngularVelocity(pitchVel: Float, yawVel: Float, rollVel: Float) {
        angularVelPitch = pitchVel
        angularVelYaw = yawVel
        angularVelRoll = rollVel
    }

    fun getRenderPosition(partialTicks: Float): Vec3 {
        if (!level().isClientSide) return Vec3(x, y, z)

        val now = System.nanoTime()
        if (lastFrameNano == 0L) {

            renderPosX = x; renderPosY = y; renderPosZ = z
            renderVelX = velocity.x; renderVelY = velocity.y; renderVelZ = velocity.z
            lastFrameNano = now
            return Vec3(renderPosX, renderPosY, renderPosZ)
        }

        val dtNanos = now - lastFrameNano
        if (dtNanos < 500_000L) {

            return Vec3(renderPosX, renderPosY, renderPosZ)
        }
        lastFrameNano = now
        val dt = (dtNanos / 1_000_000_000.0).coerceIn(0.0001, 0.1)

        val td = desiredThrust
        val renderThrusting = td.lengthSqr() > 1.0e-4 && throttle > 0.0 && fuelMass > 0.0

        if (renderThrusting) {

            val dir = td.normalize()
            val thrustAccel = MAX_THRUST_ACCEL * throttle
            renderVelX += dir.x * thrustAccel * dt
            renderVelY += dir.y * thrustAccel * dt - GRAVITY * dt
            renderVelZ += dir.z * thrustAccel * dt

            val keep = Math.pow(1.0 - AIR_DRAG, dt * 20.0)
            renderVelX *= keep; renderVelY *= keep; renderVelZ *= keep

            if (renderVelY < MAX_FALL_SPEED) renderVelY = MAX_FALL_SPEED

            renderPosX += renderVelX * dt
            renderPosY += renderVelY * dt
            renderPosZ += renderVelZ * dt
        } else {

            val renderGrounded = (onGround() || verticalCollision) && velocity.y <= 0.01 && renderVelY <= 0.01
            if (!renderGrounded) {
                renderVelY -= GRAVITY * dt
            } else {
                renderVelY = 0.0
            }
            if (renderInWater) {
                val buoyancyAccel = BUOYANCY_BASE + renderFloaterCount * BUOYANCY_PER_FLOATER
                renderVelY += buoyancyAccel * dt
                val keep = Math.pow(1.0 - WATER_DRAG, dt * 20.0)
                renderVelX *= keep; renderVelY *= keep; renderVelZ *= keep
            } else if (!renderGrounded) {
                val keep = Math.pow(1.0 - AIR_DRAG, dt * 20.0)
                renderVelX *= keep; renderVelY *= keep; renderVelZ *= keep
            }
            if (renderVelY < MAX_FALL_SPEED) renderVelY = MAX_FALL_SPEED
            if (!renderGrounded) {
                renderPosX += renderVelX * dt
                renderPosY += renderVelY * dt
                renderPosZ += renderVelZ * dt
            }
        }

        return Vec3(renderPosX, renderPosY, renderPosZ)
    }

    fun getRenderYaw(partialTicks: Float): Float {
        return Mth.wrapDegrees(Mth.lerp(partialTicks, yRotO, yRot))
    }

    fun getRenderPitch(partialTicks: Float): Float {
        return Mth.lerp(partialTicks, xRotO, xRot)
    }

    fun getRenderRoll(partialTicks: Float): Float {
        return Mth.lerp(partialTicks, prevRoll, roll)
    }

    override fun getPrevPositionVec(): Vec3 = Vec3(xOld, yOld, zOld)

    fun getChunkManager(): RocketShipChunkManager? = chunkManager

    fun rebuildContraption() {
        rebuildCreateContraption()
    }

    fun syncStructureNow() {
        structureSyncCooldown = 0
    }

    override fun tick() {

        if (level().isClientSide) {
            baseTick()
            if (contraption == null) {
                setContraption(createContraption)
                createContraption.entity = this
                initialized = false; prevPosInvalid = false
            }
            if (!initialized) contraptionInitialize()
            ensureClientContraptionReady()

            if (clientLerpSteps > 0) {
                val blend = 0.3
                val dx = clientTargetPos.x - x
                val dy = clientTargetPos.y - y
                val dz = clientTargetPos.z - z
                val distSq = dx * dx + dy * dy + dz * dz
                if (distSq > 16.0) {

                    setPos(clientTargetPos.x, clientTargetPos.y, clientTargetPos.z)
                    velocity = clientTargetVel
                } else {

                    setPos(x + dx * blend, y + dy * blend, z + dz * blend)
                    velocity = Vec3(
                        Mth.lerp(blend, velocity.x, clientTargetVel.x),
                        Mth.lerp(blend, velocity.y, clientTargetVel.y),
                        Mth.lerp(blend, velocity.z, clientTargetVel.z)
                    )
                }

                yRotO = yRot
                yRot = Mth.wrapDegrees(Mth.lerp(blend.toFloat(), yRot, clientTargetYaw))
                xRotO = xRot
                xRot = Mth.lerp(blend.toFloat(), xRot, clientTargetPitch)
                prevRoll = roll
                roll = Mth.lerp(blend.toFloat(), roll, clientTargetRoll)
                clientLerpSteps = 0
            }

            stepPhysics()

            spawnExhaustParticles()

            anchor = position()
            updateBoundingBox()
            tickContraption()
            return
        }

        val isThrusting = desiredThrust.lengthSqr() > 1.0e-4 && throttle > 0.0 && fuelMass > 0.0

        val yBefore = y
        stepPhysics()
        val yAfterPhysics = y

        checkBoosterSeparation()

        checkStageSeparation()

        updateBoundingBox()
        handleEntityCollisions()

        if (isThrusting) {

            baseTick()
        } else {

            val savedDelta = deltaMovement
            setDeltaMovement(Vec3.ZERO)
            super.tick()
            setDeltaMovement(savedDelta)
        }
        val yAfterSuper = y

        if (isThrusting && tickCount % 20 == 0) {
            LOGGER.info("[ROCKET] tick={} thrust=YES vel.y={} yBefore={} yAfterPhysics={} yAfterSuper={} throttle={} fuel={}",
                tickCount, String.format("%.4f", velocity.y), String.format("%.2f", yBefore),
                String.format("%.2f", yAfterPhysics), String.format("%.2f", yAfterSuper),
                String.format("%.2f", throttle), String.format("%.2f", fuelMass))
        }

        val lvl = level()
        if (!lvl.isClientSide && lvl is net.minecraft.server.level.ServerLevel) {
            val rider = passengers.firstOrNull { it is net.minecraft.server.level.ServerPlayer } as? net.minecraft.server.level.ServerPlayer
            if (rider != null) {
                com.example.examplemod.rocket.RocketDimensionTransition.checkAllTransitions(rider, this, lvl)
            }
        }

        RocketNetwork.CHANNEL.send(
            PacketDistributor.TRACKING_ENTITY.with { this },
            RocketShipStatePacket(id, position(), velocity, yRot, xRot, roll,
                desiredThrust, throttle, fuelMass)
        )

        if (structureSyncCooldown-- <= 0) {
            if (storage.blocks.isNotEmpty()) {
                hasSentNonEmptyStructure = true
                RocketNetwork.CHANNEL.send(
                    PacketDistributor.TRACKING_ENTITY_AND_SELF.with { this },
                    RocketShipStructurePacket(id, anchor, storage.blocks)
                )
            } else if (!hasSentNonEmptyStructure) {
                RocketNetwork.CHANNEL.send(
                    PacketDistributor.TRACKING_ENTITY_AND_SELF.with { this },
                    RocketShipStructurePacket(id, anchor, storage.blocks)
                )
            }
            structureSyncCooldown = 40
        }
    }

    private fun snapToGrid(pos: Vec3): Vec3 {
        return Vec3(
            Mth.floor(pos.x).toDouble(),
            Mth.floor(pos.y).toDouble(),
            Mth.floor(pos.z).toDouble()
        )
    }

    private fun stepPhysics() {
        prevVelocity = velocity

        val thrusting = desiredThrust.lengthSqr() > 1.0e-4 && throttle > 0.0 && fuelMass > 0.0

        if (thrusting) {
            desiredThrust = getLocalUp()
        }
        val thrustDir = desiredThrust

        val gravityMul = RocketDimensionTransition.getGravityMultiplier(level().dimension())
        val currentGravity = GRAVITY * gravityMul
        val isZeroG = gravityMul <= 0.001

        val pistonForce = detectPistonForces()
        val hasPistonForce = pistonForce.linearForce.lengthSqr() > 1.0e-8

        if (thrusting) {

            val dir = thrustDir.normalize()
            val thrustAccel = MAX_THRUST_ACCEL * throttle

            velocity = velocity.add(
                dir.x * thrustAccel * DT,
                dir.y * thrustAccel * DT - currentGravity * DT,
                dir.z * thrustAccel * DT
            )

            if (hasPistonForce) {
                velocity = velocity.add(pistonForce.linearForce.scale(DT))
            }

            val drag = if (isZeroG) 0.0 else AIR_DRAG
            val keep = 1.0 - drag
            velocity = Vec3(velocity.x * keep, velocity.y * keep, velocity.z * keep)

            if (!level().isClientSide) consumeFuel(throttle)

            if (velocity.y < MAX_FALL_SPEED) velocity = Vec3(velocity.x, MAX_FALL_SPEED, velocity.z)

            val motion = velocity.scale(DT)
            val newX = x + motion.x
            val newY = y + motion.y
            val newZ = z + motion.z
            setPosRaw(newX, newY, newZ)

            setBoundingBox(makeBoundingBox())

        } else if (isZeroG) {

            if (hasPistonForce) {
                velocity = velocity.add(pistonForce.linearForce.scale(DT))
            }

            val motion = velocity.scale(DT)
            if (motion.lengthSqr() > 1.0e-12) {
                setPosRaw(x + motion.x, y + motion.y, z + motion.z)
                setBoundingBox(makeBoundingBox())
            }

        } else {

            val grounded = (onGround() || verticalCollision) && velocity.y <= 0.01

            if (!grounded) {
                velocity = velocity.add(0.0, -currentGravity * DT, 0.0)
            } else {
                velocity = Vec3(velocity.x, 0.0, velocity.z)
            }

            if (hasPistonForce) {
                velocity = velocity.add(pistonForce.linearForce.scale(DT))
            }

            val inWater = isShipInWater()
            if (inWater) {
                var floaterCount = 0
                for ((_, state) in storage.blocks) {
                    if (state.`is`(ExampleMod.FLOATER.get())) floaterCount++
                }
                val buoyancyAccel = BUOYANCY_BASE + floaterCount * BUOYANCY_PER_FLOATER
                velocity = velocity.add(0.0, buoyancyAccel * DT, 0.0)
                val keep = 1.0 - WATER_DRAG
                velocity = Vec3(velocity.x * keep, velocity.y * keep, velocity.z * keep)
            } else if (!grounded) {
                val keep = 1.0 - AIR_DRAG
                velocity = Vec3(velocity.x * keep, velocity.y * keep, velocity.z * keep)
            }

            if (velocity.y < MAX_FALL_SPEED) velocity = Vec3(velocity.x, MAX_FALL_SPEED, velocity.z)

            val motion = velocity.scale(DT)
            if (motion.lengthSqr() > 1.0e-12) {

                if (hasPistonForce) {
                    setPosRaw(x + motion.x, y + motion.y, z + motion.z)
                    setBoundingBox(makeBoundingBox())
                } else {
                    val beforeX = x; val beforeY = y; val beforeZ = z
                    move(MoverType.SELF, motion)
                    val afterX = x; val afterY = y; val afterZ = z
                    var vx = velocity.x; var vy = velocity.y; var vz = velocity.z
                    if (motion.y < 0 && abs((afterY - beforeY) - motion.y) > 1.0e-4) vy = 0.0
                    if (abs((afterX - beforeX) - motion.x) > 1.0e-4) vx = 0.0
                    if (abs((afterZ - beforeZ) - motion.z) > 1.0e-4) vz = 0.0
                    velocity = Vec3(vx, vy, vz)
                }
            }

            if (!hasPistonForce && (onGround() || verticalCollision) && velocity.y <= 0.0) {
                velocity = Vec3(velocity.x * 0.8, 0.0, velocity.z * 0.8)
                if (velocity.horizontalDistanceSqr() < 0.0001) velocity = Vec3.ZERO
            }
        }

        anchor = position()
        setDeltaMovement(velocity)

        if (storage.blocks.size > 1) {
            val bounds = storage.computeLocalBounds()
            val geoCenter = Vec3(
                (bounds.minX + bounds.maxX) * 0.5,
                (bounds.minY + bounds.maxY) * 0.5,
                (bounds.minZ + bounds.maxZ) * 0.5
            )
            val massProps = RocketShipPhysics.computeMassProperties(storage)
            val com = massProps.centerOfMass

            val offsetX = com.x - geoCenter.x
            val offsetZ = com.z - geoCenter.z

            val gravityFactor = currentGravity / 10.0
            val torqueRoll = (offsetX * COM_TORQUE_FACTOR * gravityFactor).toFloat()
            val torquePitch = (offsetZ * COM_TORQUE_FACTOR * gravityFactor).toFloat()
            angularVelRoll += torqueRoll * DT.toFloat()
            angularVelPitch += torquePitch * DT.toFloat()
        }

        if (hasPistonForce) {
            angularVelPitch += pistonForce.torquePitch * DT.toFloat()
            angularVelYaw += pistonForce.torqueYaw * DT.toFloat()
            angularVelRoll += pistonForce.torqueRoll * DT.toFloat()
        }

        val angDamp = 1f - ANGULAR_DRAG.toFloat()
        angularVelRoll *= angDamp
        angularVelPitch *= angDamp
        angularVelYaw *= angDamp

        prevRoll = roll
        xRotO = xRot
        yRotO = yRot
        roll += angularVelRoll * DT.toFloat()
        xRot += angularVelPitch * DT.toFloat()
        yRot += angularVelYaw * DT.toFloat()

        roll = Mth.wrapDegrees(roll)
        xRot = Mth.wrapDegrees(xRot)
        yRot = Mth.wrapDegrees(yRot)

        if (level().isClientSide) {
            syncRenderState()
        }
    }

    private fun syncRenderState() {
        val thrusting = desiredThrust.lengthSqr() > 1.0e-4 && throttle > 0.0 && fuelMass > 0.0

        if (!thrusting && (verticalCollision || onGround())) {
            renderPosY = y
            renderVelY = 0.0
        }
        if (horizontalCollision) {
            renderPosX = x
            renderPosZ = z
            renderVelX = 0.0
            renderVelZ = 0.0
        }
        val blend = 0.5
        renderPosX += (x - renderPosX) * blend
        renderPosY += (y - renderPosY) * blend
        renderPosZ += (z - renderPosZ) * blend
        renderVelX += (velocity.x - renderVelX) * blend
        renderVelY += (velocity.y - renderVelY) * blend
        renderVelZ += (velocity.z - renderVelZ) * blend
        renderInWater = isShipInWater()
        renderFloaterCount = storage.blocks.count { (_, state) -> state.`is`(ExampleMod.FLOATER.get()) }
    }

    private fun isShipInWater(): Boolean {
        val lvl = level()
        val box = boundingBox
        val by = Mth.floor(box.minY)
        val cx = Mth.floor((box.minX + box.maxX) * 0.5)
        val cz = Mth.floor((box.minZ + box.maxZ) * 0.5)
        val x0 = Mth.floor(box.minX); val x1 = Mth.floor(box.maxX)
        val z0 = Mth.floor(box.minZ); val z1 = Mth.floor(box.maxZ)
        return lvl.getFluidState(BlockPos(cx, by, cz)).`is`(FluidTags.WATER)
            || lvl.getFluidState(BlockPos(x0, by, z0)).`is`(FluidTags.WATER)
            || lvl.getFluidState(BlockPos(x1, by, z0)).`is`(FluidTags.WATER)
            || lvl.getFluidState(BlockPos(x0, by, z1)).`is`(FluidTags.WATER)
            || lvl.getFluidState(BlockPos(x1, by, z1)).`is`(FluidTags.WATER)
    }

    private fun handleEntityCollisions() {

        val searchRadius = 3.0
        val encompassingBox = computeEncompassingBoundingBox()
        val searchBox = encompassingBox.inflate(searchRadius, searchRadius, searchRadius)

        val nearbyEntities = level().getEntities(this, searchBox) { entity ->
            entity != null && entity !is RocketShipEntity
        }

        if (System.getProperty("rocket.debug.render") != null) {
            println("[RocketDebug] 碰撞检测: 找到 ${nearbyEntities.size} 个附近实体")
        }

        if (nearbyEntities.isEmpty()) return

        val blockBoxes = getBlockCollisionBoxes()

        if (System.getProperty("rocket.debug.render") != null) {
            println("[RocketDebug] 碰撞检测: 使用 ${blockBoxes.size} 个方块碰撞箱")
        }

        if (blockBoxes.isEmpty()) return

        var collisionCount = 0
        for (entity in nearbyEntities) {
            val entityBox = entity.boundingBox
            var maxPush = Vec3.ZERO
            var maxPushLength = 0.0
            var hasCollision = false

            for (blockBox in blockBoxes) {
                if (!boxesCouldIntersect(entityBox, blockBox, searchRadius)) {
                    continue
                }

                if (entityBox.intersects(blockBox)) {
                    hasCollision = true
                    val push = calculatePushVector(entityBox, blockBox)
                    val pushLength = push.lengthSqr()
                    if (pushLength > maxPushLength) {
                        maxPush = push
                        maxPushLength = pushLength
                    }
                }
            }

            if (hasCollision && maxPushLength > 0.0001) {
                collisionCount++

                if (entity is net.minecraft.world.entity.player.Player) {
                    entity.move(MoverType.SHULKER_BOX, maxPush)
                    entity.hurtMarked = true
                } else {

                    val currentVel = entity.deltaMovement
                    entity.setDeltaMovement(currentVel.add(maxPush.scale(0.3)))
                    entity.hurtMarked = true
                }
            }
        }

        if (System.getProperty("rocket.debug.render") != null && collisionCount > 0) {
            println("[RocketDebug] 碰撞检测: 处理了 $collisionCount 次碰撞")
        }
    }

    private fun boxesCouldIntersect(box1: AABB, box2: AABB, threshold: Double): Boolean {

        val dx = if (box1.maxX < box2.minX) {
            box2.minX - box1.maxX
        } else if (box2.maxX < box1.minX) {
            box1.minX - box2.maxX
        } else {
            0.0
        }

        if (dx > threshold) return false

        val dy = if (box1.maxY < box2.minY) {
            box2.minY - box1.maxY
        } else if (box2.maxY < box1.minY) {
            box1.minY - box2.maxY
        } else {
            0.0
        }

        if (dy > threshold) return false

        val dz = if (box1.maxZ < box2.minZ) {
            box2.minZ - box1.maxZ
        } else if (box2.maxZ < box1.minZ) {
            box1.minZ - box2.maxZ
        } else {
            0.0
        }

        if (dz > threshold) return false

        val distSq = dx * dx + dy * dy + dz * dz
        val thresholdSq = threshold * threshold

        return distSq <= thresholdSq
    }

    private fun calculatePushVector(entityBox: AABB, obstacleBox: AABB): Vec3 {
        val overlapX = (entityBox.maxX - obstacleBox.minX).coerceAtMost(obstacleBox.maxX - entityBox.minX)
        val overlapY = (entityBox.maxY - obstacleBox.minY).coerceAtMost(obstacleBox.maxY - entityBox.minY)
        val overlapZ = (entityBox.maxZ - obstacleBox.minZ).coerceAtMost(obstacleBox.maxZ - entityBox.minZ)

        return when {
            overlapX < overlapY && overlapX < overlapZ -> {
                val direction = if (entityBox.minX + entityBox.maxX < obstacleBox.minX + obstacleBox.maxX) -1.0 else 1.0
                Vec3(overlapX * direction, 0.0, 0.0)
            }
            overlapY < overlapZ -> {
                val direction = if (entityBox.minY + entityBox.maxY < obstacleBox.minY + obstacleBox.maxY) -1.0 else 1.0
                Vec3(0.0, overlapY * direction, 0.0)
            }
            else -> {
                val direction = if (entityBox.minZ + entityBox.maxZ < obstacleBox.minZ + obstacleBox.maxZ) -1.0 else 1.0
                Vec3(0.0, 0.0, overlapZ * direction)
            }
        }
    }

    override fun remove(reason: RemovalReason) {
        super.remove(reason)
        if (shipId != -1L) {
            physicsBridge?.destroy(shipId)
        }
        RocketShipRegistry.removeByEntity(this)
        if (!level().isClientSide) {
            val buildLevel = RocketDimensions.getBuildLevel(level().server)
            if (buildLevel != null) {
                chunkManager?.cleanup(buildLevel)
            }
        }
    }

    override fun onAddedToWorld() {
        super.onAddedToWorld()
        RocketShipRegistry.register(this)

        if (contraption == null) {
            setContraption(createContraption)
            initialized = false
            prevPosInvalid = false
            createContraption.entity = this
        } else {
            contraption?.entity = this
        }
        if (level().isClientSide) {
            RocketShipStructurePacket.applyPending(this)
        }
    }

    override fun isPickable(): Boolean = false

    override fun isPushable(): Boolean = false

    override fun canBeCollidedWith(): Boolean = false

    override fun isColliding(pPos: BlockPos, pState: net.minecraft.world.level.block.state.BlockState): Boolean = false

    override fun push(pEntity: Entity) {

    }

    override fun push(pX: Double, pY: Double, pZ: Double) {

    }

    override fun getBoundingBoxForCulling(): net.minecraft.world.phys.AABB {

        return boundingBox
    }

    override fun shouldRenderAtSqrDistance(pDistance: Double): Boolean = true

    override fun isInvisible(): Boolean = false

    override fun isInvisibleTo(player: Player): Boolean = false

    override fun pick(pHitDistance: Double, pPartialTicks: Float, pHitFluids: Boolean): net.minecraft.world.phys.HitResult {

        return super.pick(pHitDistance, pPartialTicks, pHitFluids)
    }

    fun getBlockCollisionBoxes(): List<net.minecraft.world.phys.AABB> {

        val currentRotation = Triple(yRot, xRot, roll)
        val currentPosition = anchor

        if (collisionBoxCache != null && collisionBoxCache!!.isValid(currentRotation, currentPosition)) {
            return collisionBoxCache!!.boxes
        }
        val boxes = computeBlockCollisionBoxes()
        collisionBoxCache = CollisionBoxCache(
            boxes = boxes,
            rotation = currentRotation,
            position = currentPosition,
            timestamp = System.currentTimeMillis()
        )
        return boxes
    }

    private fun computeBlockCollisionBoxes(): List<net.minecraft.world.phys.AABB> {
        val anchor = this.anchor

        val blocksCopy = LinkedHashMap(storage.blocks)
        val estimatedCapacity = blocksCopy.size
        val result = ArrayList<net.minecraft.world.phys.AABB>(estimatedCapacity)

        for ((localPos, state) in blocksCopy) {

            if (state.isAir) continue

            val shape = state.getCollisionShape(shipWorld?.getLevel() ?: level(), localPos)

            if (shape.isEmpty) continue

            val aabbs = shape.toAabbs()

            if (aabbs.isEmpty()) continue

            for (localBox in aabbs) {

                val blockLocalBox = localBox.move(
                    localPos.x.toDouble(),
                    localPos.y.toDouble(),
                    localPos.z.toDouble()
                )

                val volume = (blockLocalBox.maxX - blockLocalBox.minX) *
                            (blockLocalBox.maxY - blockLocalBox.minY) *
                            (blockLocalBox.maxZ - blockLocalBox.minZ)
                if (volume < 0.001) continue

                val worldBox = transformBoxToWorld(blockLocalBox, anchor)
                result.add(worldBox)
            }
        }

        return result
    }

    private fun invalidateCollisionCache() {
        collisionBoxCache = null
    }

    private fun transformBoxToWorld(
        localBox: net.minecraft.world.phys.AABB,
        anchor: Vec3,
    ): net.minecraft.world.phys.AABB {

        val center = getCenterOfMass()

        var minX = Double.POSITIVE_INFINITY
        var minY = Double.POSITIVE_INFINITY
        var minZ = Double.POSITIVE_INFINITY
        var maxX = Double.NEGATIVE_INFINITY
        var maxY = Double.NEGATIVE_INFINITY
        var maxZ = Double.NEGATIVE_INFINITY

        val vertices = arrayOf(
            Vec3(localBox.minX, localBox.minY, localBox.minZ),
            Vec3(localBox.maxX, localBox.minY, localBox.minZ),
            Vec3(localBox.minX, localBox.maxY, localBox.minZ),
            Vec3(localBox.maxX, localBox.maxY, localBox.minZ),
            Vec3(localBox.minX, localBox.minY, localBox.maxZ),
            Vec3(localBox.maxX, localBox.minY, localBox.maxZ),
            Vec3(localBox.minX, localBox.maxY, localBox.maxZ),
            Vec3(localBox.maxX, localBox.maxY, localBox.maxZ),
        )

        for (local in vertices) {
            val world = RocketShipPhysics.localToWorld(local, anchor, yRot, xRot, roll, center)

            minX = minX.coerceAtMost(world.x)
            minY = minY.coerceAtMost(world.y)
            minZ = minZ.coerceAtMost(world.z)
            maxX = maxX.coerceAtLeast(world.x)
            maxY = maxY.coerceAtLeast(world.y)
            maxZ = maxZ.coerceAtLeast(world.z)
        }

        return net.minecraft.world.phys.AABB(minX, minY, minZ, maxX, maxY, maxZ)
    }

    private fun rotateAndTranslate(point: Vec3): Vec3 {
        val center = getCenterOfMass()
        return RocketShipPhysics.localToWorld(point, anchor, yRot, xRot, roll, center)
    }

    override fun interactAt(player: Player, hitVec: Vec3, hand: InteractionHand): InteractionResult {
        return InteractionResult.PASS
    }

    override fun positionRider(passenger: Entity, moveFunction: Entity.MoveFunction) {
        if (storage.blocks.isEmpty()) {
            super.positionRider(passenger, moveFunction)
            return
        }

        var seatLocal: Vec3? = null
        for ((pos, state) in storage.blocks) {
            if (state.`is`(ExampleMod.ROCKET_SEAT.get())) {
                seatLocal = Vec3(pos.x + 0.5, pos.y + 0.6, pos.z + 0.5)
                break
            }
        }
        if (seatLocal == null) {
            super.positionRider(passenger, moveFunction)
            return
        }
        val worldPos = localToWorld(seatLocal)
        moveFunction.accept(passenger, worldPos.x, worldPos.y, worldPos.z)
    }

    override fun readAdditional(tag: CompoundTag, spawnData: Boolean) {
        if (tag.contains("Contraption")) {
            try {
                super.readAdditional(tag, spawnData)
            } catch (e: Exception) {
                println("[错误] 读取 Contraption NBT 失败，跳过: ${e.message}")
            }
        }
        shipId = tag.getLong("ShipId")
        RocketShipRegistry.register(this)
        fuelMass = tag.getDouble("FuelMass")
        roll = tag.getFloat("Roll")
        prevRoll = roll
        throttle = tag.getDouble("Throttle")
        destinationPlanet = tag.getString("DestinationPlanet")
        angularVelPitch = tag.getFloat("AngularVelPitch")
        angularVelRoll = tag.getFloat("AngularVelRoll")
        angularVelYaw = tag.getFloat("AngularVelYaw")
        boostersSeparated = tag.getBoolean("BoostersSeparated")
        stageSeparated = tag.getBoolean("StageSeparated")
        launchOriginY = if (tag.contains("LaunchOriginY")) tag.getDouble("LaunchOriginY") else Double.MIN_VALUE
        val velTag = tag.getCompound("Velocity")
        if (velTag.contains("X")) {
            velocity = Vec3(velTag.getDouble("X"), velTag.getDouble("Y"), velTag.getDouble("Z"))
            prevVelocity = velocity
        }
        val thrustTag = tag.getCompound("DesiredThrust")
        if (thrustTag.contains("X")) {
            desiredThrust = Vec3(thrustTag.getDouble("X"), thrustTag.getDouble("Y"), thrustTag.getDouble("Z"))
        }
        val posTag = tag.getCompound("PhysicsPosition")
        if (posTag.contains("X")) {
            val savedPos = Vec3(posTag.getDouble("X"), posTag.getDouble("Y"), posTag.getDouble("Z"))
            setPos(savedPos.x, savedPos.y, savedPos.z)
            physicsServer.setState(
                RocketShipPhysicsState(
                    position = savedPos,
                    velocity = velocity,
                    yaw = yRot,
                    pitch = xRot,
                    roll = roll
                )
            )
        }
        val anchorTag = tag.getCompound("Anchor")
        if (anchorTag.contains("X")) {
            anchor = Vec3(anchorTag.getDouble("X"), anchorTag.getDouble("Y"), anchorTag.getDouble("Z"))
        }
        val originTag = tag.getCompound("BuildOrigin")
        if (originTag.contains("X")) {
            buildOrigin = BlockPos(originTag.getInt("X"), originTag.getInt("Y"), originTag.getInt("Z"))
        }

        if (tag.contains("StorageBlocks")) {
            val blocksList = tag.getList("StorageBlocks", 10)
            val blocks = LinkedHashMap<BlockPos, net.minecraft.world.level.block.state.BlockState>()
            val blockLookup = BuiltInRegistries.BLOCK.asLookup()
            for (i in 0 until blocksList.size) {
                val entry = blocksList.getCompound(i)
                val pos = BlockPos(entry.getInt("X"), entry.getInt("Y"), entry.getInt("Z"))
                val state = NbtUtils.readBlockState(blockLookup, entry.getCompound("State"))
                if (!state.isAir) {
                    blocks[pos] = state
                }
            }
            if (blocks.isNotEmpty()) {
                this.storage = RocketShipStorage.concurrentCopyOf(blocks)
                this.shipWorld = RocketShipWorld(level(), storage, this)
                this.chunkManager = RocketShipChunkManager(shipId, buildOrigin)
                println("[RocketShip] 从 NBT 恢复了 ${blocks.size} 个方块")

                rebuildCreateContraption()
                createContraption.entity = this
                setContraption(createContraption)
                initialized = false
                prevPosInvalid = false
                contraptionInitialize()

                physicsBridge = RocketWABridge(this, storage)
                physicsBridge?.initialize(shipId)
                physicsBridge?.syncToPhysics(anchor, velocity, yRot, xRot, roll)
                physicsServer.reset(anchor)

                hasReceivedNonEmptyStructure = true
                hasSentNonEmptyStructure = true
                lastStructureSignature = blocks.hashCode() + blocks.size * 31

                updateBoundingBox()

                if (level().isClientSide) {
                    createContraption.invalidateClientContraptionStructure()
                }
                return
            }
        }

        if (contraption == null) {
            setContraption(createContraption)
            initialized = false
            prevPosInvalid = false
            createContraption.entity = this
            contraptionInitialize()
        } else {
            contraption?.entity = this
        }
    }

    override fun writeAdditional(tag: CompoundTag, spawnPacket: Boolean) {
        try {
            super.writeAdditional(tag, spawnPacket)
        } catch (e: Exception) {
            println("[错误] 写入 Contraption NBT 失败，跳过: ${e.message}")
        }
        tag.putLong("ShipId", shipId)
        tag.putDouble("FuelMass", fuelMass)
        tag.putFloat("Roll", roll)
        tag.putDouble("Throttle", throttle)
        tag.putString("DestinationPlanet", destinationPlanet)
        tag.putFloat("AngularVelPitch", angularVelPitch)
        tag.putFloat("AngularVelRoll", angularVelRoll)
        tag.putFloat("AngularVelYaw", angularVelYaw)
        tag.putBoolean("BoostersSeparated", boostersSeparated)
        tag.putBoolean("StageSeparated", stageSeparated)
        tag.putDouble("LaunchOriginY", launchOriginY)
        val velTag = CompoundTag()
        velTag.putDouble("X", velocity.x)
        velTag.putDouble("Y", velocity.y)
        velTag.putDouble("Z", velocity.z)
        tag.put("Velocity", velTag)
        val thrustTag = CompoundTag()
        thrustTag.putDouble("X", desiredThrust.x)
        thrustTag.putDouble("Y", desiredThrust.y)
        thrustTag.putDouble("Z", desiredThrust.z)
        tag.put("DesiredThrust", thrustTag)
        val posTag = CompoundTag()
        posTag.putDouble("X", position().x)
        posTag.putDouble("Y", position().y)
        posTag.putDouble("Z", position().z)
        tag.put("PhysicsPosition", posTag)
        val anchorTag = CompoundTag()
        anchorTag.putDouble("X", anchor.x)
        anchorTag.putDouble("Y", anchor.y)
        anchorTag.putDouble("Z", anchor.z)
        tag.put("Anchor", anchorTag)
        val originTag = CompoundTag()
        originTag.putInt("X", buildOrigin.x)
        originTag.putInt("Y", buildOrigin.y)
        originTag.putInt("Z", buildOrigin.z)
        tag.put("BuildOrigin", originTag)

        val blocksList = ListTag()
        for ((pos, state) in storage.blocks) {
            if (state.isAir) continue
            val entry = CompoundTag()
            entry.putInt("X", pos.x)
            entry.putInt("Y", pos.y)
            entry.putInt("Z", pos.z)
            entry.put("State", NbtUtils.writeBlockState(state))
            blocksList.add(entry)
        }
        tag.put("StorageBlocks", blocksList)
        tag.putInt("StorageBlockCount", storage.blocks.size)
        println("[RocketShip] 保存了 ${blocksList.size} 个方块到 NBT")
    }

    private fun applyForcesToPhysics() {
        val bridge = physicsBridge ?: return

        val syncSuccess = bridge.syncToPhysics(position(), velocity, yRot, xRot, roll)

        if (!syncSuccess) {
            println("[警告] 同步状态到物理引擎失败")
            return
        }

        val massProps = RocketShipPhysics.computeMassProperties(storage)
        val mass = max(1.0, massProps.dryMass + fuelMass)

        val gravity = Vec3(0.0, -GRAVITY * mass, 0.0)
        bridge.applyForce(gravity)

        if (isShipInWater()) {
            var floaterCount = 0
            for ((_, state) in storage.blocks) {
                if (state.`is`(ExampleMod.FLOATER.get())) floaterCount++
            }
            val buoyancyAccel = BUOYANCY_BASE + floaterCount * BUOYANCY_PER_FLOATER
            bridge.applyForce(Vec3(0.0, buoyancyAccel * mass, 0.0))

            if (velocity.lengthSqr() > 1.0e-6) {
                bridge.applyForce(velocity.scale(-mass * WATER_DRAG * 20.0))
            }
        }

        if (velocity.lengthSqr() > 1.0e-6) {
            bridge.applyForce(velocity.scale(-mass * AIR_DRAG * 20.0))
        }

        val thrustDir = desiredThrust
        if (thrustDir.lengthSqr() > 1.0e-4 && throttle > 0.0 && fuelMass > 0.0) {
            val thrustMag = MAX_THRUST_ACCEL * throttle * mass
            bridge.applyThrust(thrustDir.normalize(), thrustMag)
            consumeFuel(throttle)
        }
    }

    private fun consumeFuel(throttle: Double) {
        if (fuelMass <= 0.0) {
            fuelMass = 0.0
            return
        }
        fuelMass = max(0.0, fuelMass - FUEL_FLOW * throttle)
    }

    fun applyClientState(position: Vec3, velocity: Vec3, yaw: Float, pitch: Float, roll: Float,
                         thrust: Vec3, throttle: Double, fuelMass: Double) {
        this.clientTargetPos = position
        this.clientTargetVel = velocity
        this.clientTargetYaw = yaw
        this.clientTargetPitch = pitch
        this.clientTargetRoll = roll

        this.desiredThrust = thrust
        this.throttle = throttle
        this.fuelMass = fuelMass

        this.clientLerpSteps = 1
    }

    fun applyStructure(anchor: Vec3, blocks: Map<BlockPos, net.minecraft.world.level.block.state.BlockState>) {
        if (level().isClientSide) {
            if (blocks.isEmpty()) {
                if (hasReceivedNonEmptyStructure) {
                    if (System.getProperty("rocket.debug.render") != null) {
                        println("[RocketShip] Ignoring empty structure packet; keeping ${storage.blocks.size} blocks")
                    }
                    return
                }
            } else {
                hasReceivedNonEmptyStructure = true
            }
        }
        val incomingAnchor = Vec3(anchor.x, anchor.y, anchor.z)
        val incomingSignature = blocks.hashCode() + blocks.size * 31
        if (incomingSignature == lastStructureSignature && storage.blocks.isNotEmpty()) {
            return
        }
        lastStructureSignature = incomingSignature
        val shouldApplyAnchor = !level().isClientSide || storage.blocks.isEmpty()
        if (shouldApplyAnchor) {
            this.anchor = incomingAnchor
            setPos(this.anchor.x, this.anchor.y, this.anchor.z)
        }
        physicsBridge?.ensureInitialized(shipId)

        this.storage = RocketShipStorage.concurrentCopyOf(blocks)
        this.shipWorld = RocketShipWorld(level(), storage, this)

        if (level().isClientSide) {
            validateBlockTextures()
        }

        rebuildCreateContraption()
        createContraption.entity = this
        setContraption(createContraption)
        initialized = false
        prevPosInvalid = false
        contraptionInitialize()
        if (level().isClientSide) {

            createContraption.invalidateClientContraptionStructure()
        }

        invalidateCollisionCache()

        lastPivotShift = createContraption.getPivotShift()
        updateBoundingBox()
    }

    private fun rebuildCreateContraption() {
        try {
            createContraption.rebuildFromStorage(storage)
            createContraption.entity = this
            lastPivotShift = createContraption.getPivotShift()

            if (level().isClientSide) {
                createContraption.invalidateClientContraptionStructure()
            }

            invalidateCollisionCache()

        } catch (e: Exception) {
            println("[错误] 重建 Contraption 失败: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun computeEncompassingBoundingBox(): AABB {
        if (storage.blocks.isEmpty()) {
            return AABB(
                anchor.x - 0.5, anchor.y - 0.5, anchor.z - 0.5,
                anchor.x + 0.5, anchor.y + 0.5, anchor.z + 0.5
            )
        }

        val localBounds = storage.computeLocalBounds()

        val corners = get8Corners(localBounds)

        val transformedCorners = corners.map { corner ->
            rotateAndTranslate(corner)
        }

        var minX = Double.POSITIVE_INFINITY
        var minY = Double.POSITIVE_INFINITY
        var minZ = Double.POSITIVE_INFINITY
        var maxX = Double.NEGATIVE_INFINITY
        var maxY = Double.NEGATIVE_INFINITY
        var maxZ = Double.NEGATIVE_INFINITY

        transformedCorners.forEach { corner ->
            minX = minX.coerceAtMost(corner.x)
            minY = minY.coerceAtMost(corner.y)
            minZ = minZ.coerceAtMost(corner.z)
            maxX = maxX.coerceAtLeast(corner.x)
            maxY = maxY.coerceAtLeast(corner.y)
            maxZ = maxZ.coerceAtLeast(corner.z)
        }

        return AABB(minX - 0.1, minY - 0.1, minZ - 0.1, maxX + 0.1, maxY + 0.1, maxZ + 0.1)
    }

    private fun get8Corners(box: AABB): List<Vec3> {
        return listOf(
            Vec3(box.minX, box.minY, box.minZ),
            Vec3(box.maxX, box.minY, box.minZ),
            Vec3(box.minX, box.maxY, box.minZ),
            Vec3(box.maxX, box.maxY, box.minZ),
            Vec3(box.minX, box.minY, box.maxZ),
            Vec3(box.maxX, box.minY, box.maxZ),
            Vec3(box.minX, box.maxY, box.maxZ),
            Vec3(box.maxX, box.maxY, box.maxZ)
        )
    }

    private fun updateBoundingBox() {
        if (storage.blocks.isNotEmpty()) {
            val boxes = getBlockCollisionBoxes()
            if (boxes.isNotEmpty()) {
                var minX = Double.POSITIVE_INFINITY
                var minY = Double.POSITIVE_INFINITY
                var minZ = Double.POSITIVE_INFINITY
                var maxX = Double.NEGATIVE_INFINITY
                var maxY = Double.NEGATIVE_INFINITY
                var maxZ = Double.NEGATIVE_INFINITY
                for (box in boxes) {
                    minX = minOf(minX, box.minX)
                    minY = minOf(minY, box.minY)
                    minZ = minOf(minZ, box.minZ)
                    maxX = maxOf(maxX, box.maxX)
                    maxY = maxOf(maxY, box.maxY)
                    maxZ = maxOf(maxZ, box.maxZ)
                }
                setBoundingBox(AABB(minX, minY, minZ, maxX, maxY, maxZ))
                return
            }
            setBoundingBox(computeEncompassingBoundingBox())
            return
        }
        val bounds = contraption?.bounds
        if (bounds != null && bounds.getXsize() > 0.01 && bounds.getYsize() > 0.01 && bounds.getZsize() > 0.01) {
            setBoundingBox(bounds.move(anchor.x, anchor.y, anchor.z))
            return
        }

        setBoundingBox(AABB(anchor.x - 0.5, anchor.y, anchor.z - 0.5, anchor.x + 0.5, anchor.y + 1.0, anchor.z + 0.5))
    }

    fun ensureClientContraptionReady() {
        if (!level().isClientSide) return
        if (contraption == null) {
            createContraption.entity = this
            setContraption(createContraption)
            initialized = false
            prevPosInvalid = false
            contraptionInitialize()
        }
        if (storage.blocks.isEmpty()) return
        if (createContraption.blocks.size != storage.blocks.size || !initialized) {
            rebuildCreateContraption()
            createContraption.entity = this
            setContraption(createContraption)
            initialized = false
            prevPosInvalid = false
            contraptionInitialize()

            createContraption.invalidateClientContraptionStructure()
            updateBoundingBox()
        }
    }

    override fun isReadyForRender(): Boolean {
        if (contraption == null || storage.blocks.isEmpty()) return false
        return super.isReadyForRender() || createContraption.blocks.isNotEmpty()
    }

    override fun getRotationState(): AbstractContraptionEntity.ContraptionRotationState {
        val state = AbstractContraptionEntity.ContraptionRotationState()
        state.xRotation = xRot
        state.yRotation = yRot
        state.zRotation = roll
        state.secondYRotation = 0f
        return state
    }

    override fun applyRotation(vec: Vec3, partialTicks: Float): Vec3 {
        return rotateVector(vec, yRot, xRot, roll)
    }

    override fun reverseRotation(vec: Vec3, partialTicks: Float): Vec3 {
        return rotateVector(vec, -yRot, -xRot, -roll)
    }

    override fun applyLocalTransforms(matrixStack: PoseStack, partialTicks: Float) {

        val yaw = Mth.lerp(partialTicks, yRotO, yRot)
        val pitch = Mth.lerp(partialTicks, xRotO, xRot)
        val rollAngle = Mth.lerp(partialTicks, prevRoll, roll)
        matrixStack.translate(0.5, 0.5, 0.5)
        matrixStack.mulPose(Axis.YP.rotationDegrees(yaw))
        matrixStack.mulPose(Axis.XP.rotationDegrees(pitch))
        matrixStack.mulPose(Axis.ZP.rotationDegrees(rollAngle))
        matrixStack.translate(-0.5, -0.5, -0.5)
    }

    override fun tickContraption() {
        if (contraption == null) {
            return
        }

        val thrusting = desiredThrust.lengthSqr() > 1.0e-4 && throttle > 0.0 && fuelMass > 0.0
        if (!thrusting) {
            ContraptionCollider.collideBlocks(this)
        }
        tickActors()
    }

    override fun makeStructureTransform(): StructureTransform {
        return StructureTransform(BlockPos.containing(position()), 0f, 0f, 0f)
    }

    override fun getStalledAngle(): Float = 0.0f

    override fun handleStallInformation(x: Double, y: Double, z: Double, angle: Float) {
    }

    private fun rotateVector(vec: Vec3, yaw: Float, pitch: Float, roll: Float): Vec3 {
        val matrix = Matrix4d()
            .rotateY(Math.toRadians(yaw.toDouble()))
            .rotateX(Math.toRadians(pitch.toDouble()))
            .rotateZ(Math.toRadians(roll.toDouble()))
        val out = Vector3d(vec.x, vec.y, vec.z)
        matrix.transformPosition(out)
        return Vec3(out.x, out.y, out.z)
    }

    private fun validateBlockTextures() {
        if (!level().isClientSide) return

        var missingTextureCount = 0
        var airBlockCount = 0

        val blocksCopy = LinkedHashMap(storage.blocks)

        for ((pos, state) in blocksCopy) {

            if (state.isAir) {
                airBlockCount++
                continue
            }

            try {
                val shapeGetter = shipWorld ?: level()
                val shape = state.getShape(shapeGetter, pos)
                if (shape.isEmpty && !state.isAir) {
                    println("[警告] 方块 ${state.block.descriptionId} 在位置 $pos 没有渲染形状")
                    missingTextureCount++
                }
            } catch (e: Exception) {
                println("[错误] 无法获取方块 ${state.block.descriptionId} 在位置 $pos 的渲染形状: ${e.message}")
                missingTextureCount++
            }
        }

        if (missingTextureCount > 0) {
            println("[警告] 火箭结构中有 $missingTextureCount 个方块可能缺少纹理")
        }

        if (airBlockCount > 0) {
            println("[信息] 火箭结构中有 $airBlockCount 个空气方块（已跳过渲染）")
        }

        println("[信息] 纹理验证完成：${storage.blocks.size} 个方块，$missingTextureCount 个警告，$airBlockCount 个空气方块")
    }

}

data class CollisionBoxCache(
    val boxes: List<AABB>,
    val rotation: Triple<Float, Float, Float>,
    val position: Vec3,
    val timestamp: Long
) {

    private val rotationHash: Int = rotation.hashCode()
    private val positionHash: Int = position.hashCode()

    fun isValid(currentRotation: Triple<Float, Float, Float>, currentPosition: Vec3): Boolean {

        if (rotationHash != currentRotation.hashCode()) {
            return false
        }

        if (positionHash != currentPosition.hashCode()) {
            return false
        }

        val rotationChanged = kotlin.math.abs(rotation.first - currentRotation.first) > 0.01f ||
                             kotlin.math.abs(rotation.second - currentRotation.second) > 0.01f ||
                             kotlin.math.abs(rotation.third - currentRotation.third) > 0.01f

        if (rotationChanged) {
            return false
        }

        val distSq = position.distanceToSqr(currentPosition)
        if (distSq > 0.01) {
            return false
        }

        return true
    }
}
