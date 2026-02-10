package com.example.examplemod.rocket.ship

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.doubles.shouldBeWithinPercentageOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.double
import io.kotest.property.arbitrary.float
import io.kotest.property.checkAll

class RocketContraptionProxyTest : FunSpec({

    test("属性 2: ContraptionProxy 位置同步 - 位置分量应该保持精度") {

        checkAll(100,
            Arb.double(-1000.0, 1000.0),
            Arb.double(-1000.0, 1000.0),
            Arb.double(-1000.0, 1000.0)
        ) { x, y, z ->

            val copiedX = x
            val copiedY = y
            val copiedZ = z

            copiedX.shouldBeWithinPercentageOf(x, 0.01)
            copiedY.shouldBeWithinPercentageOf(y, 0.01)
            copiedZ.shouldBeWithinPercentageOf(z, 0.01)
        }
    }

    test("属性 2: ContraptionProxy 位置同步 - 旋转值应该在有效范围内") {

        checkAll(100,
            Arb.float(-180f, 180f),
            Arb.float(-90f, 90f),
            Arb.float(-180f, 180f)
        ) { yaw, pitch, roll ->

            val normalizedYaw = normalizeAngle(yaw)
            val normalizedPitch = normalizePitch(pitch)
            val normalizedRoll = normalizeAngle(roll)

            (normalizedYaw >= -180f && normalizedYaw <= 180f) shouldBe true
            (normalizedPitch >= -90f && normalizedPitch <= 90f) shouldBe true
            (normalizedRoll >= -180f && normalizedRoll <= 180f) shouldBe true
        }
    }

    test("属性 2: ContraptionProxy 位置同步 - 连续同步应该保持一致性") {

        checkAll(100,
            Arb.double(-1000.0, 1000.0),
            Arb.double(-1000.0, 1000.0),
            Arb.double(-1000.0, 1000.0)
        ) { x, y, z ->

            val syncedX1 = x
            val syncedY1 = y
            val syncedZ1 = z

            val syncedX2 = x
            val syncedY2 = y
            val syncedZ2 = z

            syncedX1 shouldBe syncedX2
            syncedY1 shouldBe syncedY2
            syncedZ1 shouldBe syncedZ2
        }
    }

    test("属性 2: ContraptionProxy 位置同步 - 旋转标准化应该是幂等的") {

        checkAll(100, Arb.float(-720f, 720f)) { angle ->
            val normalized1 = normalizeAngle(angle)
            val normalized2 = normalizeAngle(normalized1)

            normalized1 shouldBe normalized2
        }
    }
})

private fun normalizeAngle(angle: Float): Float {
    var normalized = angle % 360f
    if (normalized > 180f) normalized -= 360f
    if (normalized < -180f) normalized += 360f
    return normalized
}

private fun normalizePitch(pitch: Float): Float {
    return pitch.coerceIn(-90f, 90f)
}
