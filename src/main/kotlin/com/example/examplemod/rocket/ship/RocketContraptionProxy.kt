package com.example.examplemod.rocket.ship

import com.example.examplemod.ExampleMod
import com.simibubi.create.content.contraptions.AbstractContraptionEntity
import com.simibubi.create.content.contraptions.Contraption
import com.simibubi.create.content.contraptions.StructureTransform
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import org.joml.Matrix4d
import org.joml.Vector3d
import net.minecraft.util.Mth
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Axis

class RocketContraptionProxy(private val rocket: RocketShipEntity) : AbstractContraptionEntity(
    ExampleMod.ROCKET_SHIP_ENTITY.get() as EntityType<*>,
    rocket.level()
) {
    private var currentContraption: RocketShipContraption? = null
    private var roll: Float = 0.0f

    init {

        noPhysics = true
        noCulling = true

        setInvulnerable(true)
    }

    fun updateContraption(contraption: RocketShipContraption) {
        currentContraption = contraption
        contraption.entity = this
        setContraption(contraption)
        initialized = true
        prevPosInvalid = false

        println("[RocketContraptionProxy] Contraption 已更新: ${contraption.blocks.size} 个方块")

    }

    fun syncFromRocket() {
        roll = rocket.getRoll()
        yRot = rocket.yRot
        xRot = rocket.xRot
        val pos = rocket.position()
        setPos(pos.x, pos.y, pos.z)
        xOld = rocket.xOld
        yOld = rocket.yOld
        zOld = rocket.zOld
        prevPosInvalid = false

        if (System.getProperty("rocket.debug.render") != null && rocket.tickCount % 100 == 0) {
            println("[RocketDebug] ContraptionProxy 同步: 位置=$pos, 旋转=($yRot, $xRot, $roll)")
        }
    }

    override fun getContraption(): Contraption? = currentContraption

    override fun getAnchorVec(): Vec3 = rocket.position()

    override fun getPrevAnchorVec(): Vec3 = rocket.getPrevPositionVec()

    override fun getRotationState(): ContraptionRotationState {
        val state = ContraptionRotationState()
        state.xRotation = rocket.xRot
        state.yRotation = rocket.yRot
        state.zRotation = roll
        state.secondYRotation = 0f
        return state
    }

    override fun applyRotation(vec: Vec3, partialTicks: Float): Vec3 {
        return rotate(vec, rocket.yRot, rocket.xRot, roll)
    }

    override fun reverseRotation(vec: Vec3, partialTicks: Float): Vec3 {
        return rotate(vec, -rocket.yRot, -rocket.xRot, -roll)
    }

    override fun applyLocalTransforms(matrixStack: PoseStack, partialTicks: Float) {
        val yaw = Mth.lerp(partialTicks, rocket.yRotO, rocket.yRot)
        val pitch = Mth.lerp(partialTicks, rocket.xRotO, rocket.xRot)
        val rollAngle = Mth.lerp(partialTicks, rocket.getPrevRoll(), rocket.getRoll())

        matrixStack.translate(0.5, 0.5, 0.5)
        matrixStack.mulPose(Axis.YP.rotationDegrees(yaw))
        matrixStack.mulPose(Axis.XP.rotationDegrees(pitch))
        matrixStack.mulPose(Axis.ZP.rotationDegrees(rollAngle))
        matrixStack.translate(-0.5, -0.5, -0.5)
    }

    override fun tickContraption() {
    }

    override fun makeStructureTransform(): StructureTransform {
        return StructureTransform(BlockPos.containing(getAnchorVec()), 0f, 0f, 0f)
    }

    override fun getStalledAngle(): Float = 0.0f

    override fun handleStallInformation(x: Double, y: Double, z: Double, angle: Float) {
    }

    override fun getYawOffset(): Float = 0.0f

    override fun supportsTerrainCollision(): Boolean = false

    override fun collisionEnabled(): Boolean = false

    override fun isPickable(): Boolean = false

    override fun isPushable(): Boolean = false

    override fun canBeCollidedWith(): Boolean = false

    override fun push(pEntity: Entity) {

    }

    override fun push(pX: Double, pY: Double, pZ: Double) {

    }

    override fun defineSynchedData() {
    }

    private fun rotate(vec: Vec3, yaw: Float, pitch: Float, roll: Float): Vec3 {
        val matrix = Matrix4d()
            .rotateY(Math.toRadians(yaw.toDouble()))
            .rotateX(Math.toRadians(pitch.toDouble()))
            .rotateZ(Math.toRadians(roll.toDouble()))
        val out = Vector3d(vec.x, vec.y, vec.z)
        matrix.transformPosition(out)
        return Vec3(out.x, out.y, out.z)
    }
}
