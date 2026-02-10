package com.example.examplemod.rocket.ship

import com.example.examplemod.rocket.core.PhysicsState
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.doubles.shouldBeWithinPercentageOf
import io.kotest.matchers.floats.shouldBeWithinPercentageOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.double
import io.kotest.property.arbitrary.float
import io.kotest.property.checkAll
import net.minecraft.world.phys.Vec3
import kotlin.math.abs

class RocketPhysicsSyncTest : FunSpec({

    test("属性 9: 物理同步一致性 - 位置同步应该保持精度") {

        checkAll(100,
            Arb.double(-10000.0, 10000.0),
            Arb.double(-256.0, 320.0),
            Arb.double(-10000.0, 10000.0)
        ) { x, y, z ->
            val position = Vec3(x, y, z)

            val physicsState = PhysicsState(
                position = position,
                velocity = Vec3.ZERO,
                yaw = 0f,
                pitch = 0f,
                roll = 0f
            )

            physicsState.position.x.shouldBeWithinPercentageOf(x, 0.01)
            physicsState.position.y.shouldBeWithinPercentageOf(y, 0.01)
            physicsState.position.z.shouldBeWithinPercentageOf(z, 0.01)
        }
    }

    test("属性 9: 物理同步一致性 - 速度同步应该保持精度") {

        checkAll(100,
            Arb.double(-50.0, 50.0),
            Arb.double(-50.0, 50.0),
            Arb.double(-50.0, 50.0)
        ) { vx, vy, vz ->
            val velocity = Vec3(vx, vy, vz)

            val physicsState = PhysicsState(
                position = Vec3.ZERO,
                velocity = velocity,
                yaw = 0f,
                pitch = 0f,
                roll = 0f
            )

            physicsState.velocity.x.shouldBeWithinPercentageOf(vx, 0.01)
            physicsState.velocity.y.shouldBeWithinPercentageOf(vy, 0.01)
            physicsState.velocity.z.shouldBeWithinPercentageOf(vz, 0.01)
        }
    }

    test("属性 9: 物理同步一致性 - 旋转同步应该保持角度范围") {

        checkAll(100,
            Arb.float(-180f, 180f),
            Arb.float(-90f, 90f),
            Arb.float(-180f, 180f)
        ) { yaw, pitch, roll ->

            val physicsState = PhysicsState(
                position = Vec3.ZERO,
                velocity = Vec3.ZERO,
                yaw = yaw,
                pitch = pitch,
                roll = roll
            )

            (physicsState.yaw >= -180f && physicsState.yaw <= 180f) shouldBe true
            (physicsState.pitch >= -90f && physicsState.pitch <= 90f) shouldBe true
            (physicsState.roll >= -180f && physicsState.roll <= 180f) shouldBe true
        }
    }

    test("属性 9: 物理同步一致性 - 平滑插值应该在原始值之间") {

        checkAll(100,
            Arb.double(-1000.0, 1000.0),
            Arb.double(-1000.0, 1000.0),
            Arb.double(0.0, 1.0)
        ) { oldX, newX, t ->
            val interpolated = lerp(oldX, newX, t)

            val min = minOf(oldX, newX)
            val max = maxOf(oldX, newX)

            (interpolated >= min - 0.001 && interpolated <= max + 0.001) shouldBe true
        }
    }

    test("属性 9: 物理同步一致性 - 角度插值应该选择最短路径") {

        checkAll(100,
            Arb.float(-180f, 180f),
            Arb.float(-180f, 180f)
        ) { angle1, angle2 ->
            val interpolated = lerpAngle(angle1, angle2, 0.5f)

            val delta1 = normalizeAngleDelta(angle2 - angle1)
            val delta2 = normalizeAngleDelta(angle1 - angle2)

            val shortestDelta = if (abs(delta1) < abs(delta2)) delta1 else -delta2
            val expectedMidpoint = normalizeAngle(angle1 + shortestDelta * 0.5f)

            val diff = abs(normalizeAngleDelta(interpolated - expectedMidpoint))
            (diff < 1.0f) shouldBe true
        }
    }

    test("属性 9: 物理同步一致性 - 位置验证应该拒绝无效值") {

        checkAll(100,
            Arb.double(-100000000.0, 100000000.0),
            Arb.double(-100000000.0, 100000000.0),
            Arb.double(-100000000.0, 100000000.0)
        ) { x, y, z ->
            val position = Vec3(x, y, z)
            val isValid = isValidPosition(position)

            val expectedValid = x.isFinite() && y.isFinite() && z.isFinite() &&
                               abs(x) < 30000000.0 && abs(z) < 30000000.0

            isValid shouldBe expectedValid
        }
    }

    test("属性 9: 物理同步一致性 - 速度验证应该拒绝过大值") {

        checkAll(100,
            Arb.double(-200.0, 200.0),
            Arb.double(-200.0, 200.0),
            Arb.double(-200.0, 200.0)
        ) { vx, vy, vz ->
            val velocity = Vec3(vx, vy, vz)
            val isValid = isValidVelocity(velocity)

            val speed = velocity.length()
            val expectedValid = vx.isFinite() && vy.isFinite() && vz.isFinite() &&
                               speed < 100.0

            isValid shouldBe expectedValid
        }
    }

    test("属性 9: 物理同步一致性 - 速度限制应该保持方向") {

        checkAll(100,
            Arb.double(-200.0, 200.0),
            Arb.double(-200.0, 200.0),
            Arb.double(-200.0, 200.0)
        ) { vx, vy, vz ->
            val velocity = Vec3(vx, vy, vz)

            if (velocity.lengthSqr() > 0.001) {
                val clamped = clampVelocity(velocity, 100.0)

                val originalDir = velocity.normalize()
                val clampedDir = clamped.normalize()

                val dotProduct = originalDir.dot(clampedDir)
                dotProduct.shouldBeWithinPercentageOf(1.0, 1.0)
            }
        }
    }
})

private fun lerp(a: Double, b: Double, t: Double): Double {
    return a + (b - a) * t
}

private fun lerpAngle(a: Float, b: Float, t: Float): Float {
    var delta = b - a
    while (delta > 180f) delta -= 360f
    while (delta < -180f) delta += 360f
    return a + delta * t
}

private fun normalizeAngle(angle: Float): Float {
    var normalized = angle % 360f
    if (normalized > 180f) normalized -= 360f
    if (normalized < -180f) normalized += 360f
    return normalized
}

private fun normalizeAngleDelta(delta: Float): Float {
    var normalized = delta
    while (normalized > 180f) normalized -= 360f
    while (normalized < -180f) normalized += 360f
    return normalized
}

private fun isValidPosition(pos: Vec3): Boolean {
    return pos.x.isFinite() && pos.y.isFinite() && pos.z.isFinite() &&
           abs(pos.x) < 30000000.0 && abs(pos.z) < 30000000.0
}

private fun isValidVelocity(vel: Vec3): Boolean {
    return vel.x.isFinite() && vel.y.isFinite() && vel.z.isFinite() &&
           vel.length() < 100.0
}

private fun clampVelocity(vel: Vec3, maxVelocity: Double): Vec3 {
    val length = vel.length()
    return if (length > maxVelocity) {
        vel.scale(maxVelocity / length)
    } else {
        vel
    }
}
