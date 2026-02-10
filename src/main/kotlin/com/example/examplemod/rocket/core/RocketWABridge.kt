package com.example.examplemod.rocket.core

import com.example.examplemod.rocket.ship.RocketShipEntity
import com.example.examplemod.rocket.ship.RocketShipPhysics
import com.example.examplemod.rocket.ship.RocketShipStorage
import net.minecraft.world.phys.Vec3
import org.joml.Quaterniond
import kotlin.math.atan2
import kotlin.math.asin

class RocketWABridge(
    private val entity: RocketShipEntity,
    private val storage: RocketShipStorage
) {
    private var physicsHandle: PhysicsShipHandle? = null
    private var initialized = false
    private var lastSyncTime = System.currentTimeMillis()
    private var syncFailureCount = 0
    private var useFallbackPhysics = false

    private var lastPhysicsState: PhysicsState? = null
    private val smoothingFactor = if (entity.level().isClientSide) 0.3 else 1.0

    companion object {
        private const val MAX_SYNC_FAILURES = 5
        private const val SYNC_TIMEOUT_MS = 100
        private const val MAX_POSITION_DELTA = 10.0
        private const val MAX_VELOCITY = 100.0
    }

    fun initialize(shipId: Long): Boolean {
        if (initialized) {
            println("[警告] RocketWABridge 已经初始化，跳过重复初始化")
            return true
        }

        return try {
            val massProps = RocketShipPhysics.computeMassProperties(storage)

            if (massProps.dryMass <= 0.0) {
                println("[错误] 无效的质量属性：质量 = ${massProps.dryMass}")
                useFallbackPhysics = true
                return false
            }

            physicsHandle = RocketWACore.createShip(
                shipId,
                massProps.dryMass,
                massProps.inertia,
                massProps.centerOfMass
            )

            initialized = true
            syncFailureCount = 0
            useFallbackPhysics = false
            println("[信息] RocketWABridge 初始化成功，shipId = $shipId")
            true
        } catch (e: Exception) {
            println("[错误] 初始化物理引擎失败: ${e.message}")
            e.printStackTrace()
            useFallbackPhysics = true
            false
        }
    }

    fun ensureInitialized(shipId: Long): Boolean {
        if (!initialized) {
            return initialize(shipId)
        }
        if (useFallbackPhysics) {
            useFallbackPhysics = false
            syncFailureCount = 0
            return true
        }
        return true
    }

    fun destroy(shipId: Long) {
        if (!initialized) return

        try {
            RocketWACore.removeShip(shipId)
            println("[信息] RocketWABridge 清理成功，shipId = $shipId")
        } catch (e: Exception) {
            println("[错误] 清理物理引擎失败: ${e.message}")
            e.printStackTrace()
        } finally {
            physicsHandle = null
            initialized = false
            lastPhysicsState = null
            syncFailureCount = 0
        }
    }

    fun syncToPhysics(position: Vec3, velocity: Vec3, yaw: Float, pitch: Float, roll: Float): Boolean {
        if (useFallbackPhysics) return false

        val handle = physicsHandle
        if (handle == null) {
            handleSyncFailure("物理句柄为空")
            return false
        }

        return try {

            if (!isValidPosition(position)) {
                println("[警告] 无效的位置数据: $position")
                return false
            }

            if (!isValidVelocity(velocity)) {
                println("[警告] 无效的速度数据: $velocity，限制到最大值")
                val clampedVelocity = clampVelocity(velocity)
                handle.setVelocity(clampedVelocity)
            } else {
                handle.setVelocity(velocity)
            }

            handle.setPosition(position)
            handle.setRotation(eulerToQuaternion(yaw, pitch, roll))

            lastSyncTime = System.currentTimeMillis()
            syncFailureCount = 0
            true
        } catch (e: Exception) {
            handleSyncFailure("同步到物理引擎失败: ${e.message}")
            false
        }
    }

    fun syncFromPhysics(): PhysicsState? {
        if (useFallbackPhysics) return null

        val handle = physicsHandle
        if (handle == null) {
            handleSyncFailure("物理句柄为空")
            return null
        }

        return try {
            val rotation = handle.getRotation()
            val (yaw, pitch, roll) = quaternionToEuler(rotation)

            val rawState = PhysicsState(
                position = handle.getPosition(),
                velocity = handle.getVelocity(),
                yaw = yaw,
                pitch = pitch,
                roll = roll
            )

            if (!isValidPhysicsState(rawState)) {
                println("[警告] 物理引擎返回无效状态")
                return lastPhysicsState
            }

            val smoothedState = if (lastPhysicsState != null) {
                smoothPhysicsState(lastPhysicsState!!, rawState)
            } else {
                rawState
            }

            lastPhysicsState = smoothedState
            lastSyncTime = System.currentTimeMillis()
            syncFailureCount = 0

            smoothedState
        } catch (e: Exception) {
            handleSyncFailure("从物理引擎同步失败: ${e.message}")
            lastPhysicsState
        }
    }

    fun applyForce(force: Vec3): Boolean {
        if (useFallbackPhysics) return false

        return try {
            physicsHandle?.applyForce(force)
            true
        } catch (e: Exception) {
            println("[错误] 施加力失败: ${e.message}")
            false
        }
    }

    fun applyTorque(torque: Vec3): Boolean {
        if (useFallbackPhysics) return false

        return try {
            physicsHandle?.applyTorque(torque)
            true
        } catch (e: Exception) {
            println("[错误] 施加扭矩失败: ${e.message}")
            false
        }
    }

    fun applyThrust(direction: Vec3, magnitude: Double): Boolean {
        if (useFallbackPhysics) return false

        val handle = physicsHandle ?: return false

        return try {
            val rotation = handle.getRotation()
            val worldDirection = rotateVector(direction, rotation)
            val force = worldDirection.scale(magnitude)
            handle.applyForce(force)
            true
        } catch (e: Exception) {
            println("[错误] 施加推力失败: ${e.message}")
            false
        }
    }

    fun isUsingFallbackPhysics(): Boolean = useFallbackPhysics

    fun getSyncFailureCount(): Int = syncFailureCount

    private fun handleSyncFailure(reason: String) {
        syncFailureCount++
        println("[错误] 物理同步失败 ($syncFailureCount/$MAX_SYNC_FAILURES): $reason")

        if (syncFailureCount >= MAX_SYNC_FAILURES) {
            println("[错误] 物理同步失败次数过多，切换到后备物理")
            useFallbackPhysics = true
        }
    }

    private fun smoothPhysicsState(oldState: PhysicsState, newState: PhysicsState): PhysicsState {

        val positionDelta = oldState.position.distanceTo(newState.position)
        if (positionDelta > MAX_POSITION_DELTA) {

            return newState
        }

        val smoothedPosition = lerpVec3(oldState.position, newState.position, smoothingFactor)
        val smoothedVelocity = lerpVec3(oldState.velocity, newState.velocity, smoothingFactor)

        val smoothedYaw = lerpAngle(oldState.yaw, newState.yaw, smoothingFactor.toFloat())
        val smoothedPitch = lerpAngle(oldState.pitch, newState.pitch, smoothingFactor.toFloat())
        val smoothedRoll = lerpAngle(oldState.roll, newState.roll, smoothingFactor.toFloat())

        return PhysicsState(
            position = smoothedPosition,
            velocity = smoothedVelocity,
            yaw = smoothedYaw,
            pitch = smoothedPitch,
            roll = smoothedRoll
        )
    }

    private fun isValidPosition(pos: Vec3): Boolean {
        return pos.x.isFinite() && pos.y.isFinite() && pos.z.isFinite() &&
               kotlin.math.abs(pos.x) < 30000000.0 &&
               kotlin.math.abs(pos.z) < 30000000.0
    }

    private fun isValidVelocity(vel: Vec3): Boolean {
        return vel.x.isFinite() && vel.y.isFinite() && vel.z.isFinite() &&
               vel.length() < MAX_VELOCITY
    }

    private fun isValidPhysicsState(state: PhysicsState): Boolean {
        return isValidPosition(state.position) &&
               isValidVelocity(state.velocity) &&
               state.yaw.isFinite() && state.pitch.isFinite() && state.roll.isFinite()
    }

    private fun clampVelocity(vel: Vec3): Vec3 {
        val length = vel.length()
        return if (length > MAX_VELOCITY) {
            vel.scale(MAX_VELOCITY / length)
        } else {
            vel
        }
    }

    private fun lerpVec3(a: Vec3, b: Vec3, t: Double): Vec3 {
        return Vec3(
            a.x + (b.x - a.x) * t,
            a.y + (b.y - a.y) * t,
            a.z + (b.z - a.z) * t
        )
    }

    private fun lerpAngle(a: Float, b: Float, t: Float): Float {
        var delta = b - a
        while (delta > 180f) delta -= 360f
        while (delta < -180f) delta += 360f
        return a + delta * t
    }

    private fun eulerToQuaternion(yaw: Float, pitch: Float, roll: Float): Quaterniond {
        val quat = Quaterniond()
        quat.rotateYXZ(
            Math.toRadians(yaw.toDouble()),
            Math.toRadians(pitch.toDouble()),
            Math.toRadians(roll.toDouble())
        )
        return quat
    }

    private fun quaternionToEuler(quat: Quaterniond): Triple<Float, Float, Float> {
        val sinr_cosp = 2.0 * (quat.w * quat.x + quat.y * quat.z)
        val cosr_cosp = 1.0 - 2.0 * (quat.x * quat.x + quat.y * quat.y)
        val roll = Math.toDegrees(atan2(sinr_cosp, cosr_cosp)).toFloat()

        val sinp = 2.0 * (quat.w * quat.y - quat.z * quat.x)
        val pitch = if (kotlin.math.abs(sinp) >= 1.0) {
            Math.toDegrees(Math.copySign(Math.PI / 2.0, sinp)).toFloat()
        } else {
            Math.toDegrees(asin(sinp)).toFloat()
        }

        val siny_cosp = 2.0 * (quat.w * quat.z + quat.x * quat.y)
        val cosy_cosp = 1.0 - 2.0 * (quat.y * quat.y + quat.z * quat.z)
        val yaw = Math.toDegrees(atan2(siny_cosp, cosy_cosp)).toFloat()

        return Triple(yaw, pitch, roll)
    }

    private fun rotateVector(vec: Vec3, quat: Quaterniond): Vec3 {
        val v = org.joml.Vector3d(vec.x, vec.y, vec.z)
        quat.transform(v)
        return Vec3(v.x, v.y, v.z)
    }
}

data class PhysicsState(
    val position: Vec3,
    val velocity: Vec3,
    val yaw: Float,
    val pitch: Float,
    val roll: Float
)
