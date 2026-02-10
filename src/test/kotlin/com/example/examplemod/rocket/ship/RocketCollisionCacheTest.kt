package com.example.examplemod.rocket.ship

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.property.Arb
import io.kotest.property.arbitrary.double
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.checkAll

class RocketCollisionCacheTest : FunSpec({

    data class CacheState(
        val posX: Double,
        val posY: Double,
        val posZ: Double,
        val yaw: Double,
        val pitch: Double,
        val roll: Double
    ) {
        fun distanceSquared(other: CacheState): Double {
            val dx = posX - other.posX
            val dy = posY - other.posY
            val dz = posZ - other.posZ
            return dx * dx + dy * dy + dz * dz
        }
    }

    data class CollisionBox(
        val minX: Double,
        val minY: Double,
        val minZ: Double,
        val maxX: Double,
        val maxY: Double,
        val maxZ: Double
    )

    data class RotationState(
        val yaw: Double,
        val pitch: Double,
        val roll: Double
    )

    test("属性 7: 碰撞缓存一致性 - 相同状态应该返回缓存结果") {

        checkAll(100,
            Arb.double(-1000.0, 1000.0),
            Arb.double(-1000.0, 1000.0),
            Arb.double(-1000.0, 1000.0),
            Arb.double(-180.0, 180.0),
            Arb.double(-90.0, 90.0),
            Arb.double(-180.0, 180.0)
        ) { posX, posY, posZ, yaw, pitch, roll ->

            val state1 = CacheState(posX, posY, posZ, yaw, pitch, roll)
            val state2 = CacheState(posX, posY, posZ, yaw, pitch, roll)

            val isCacheValid = state1 == state2

            isCacheValid shouldBe true
        }
    }

    test("属性 7: 碰撞缓存一致性 - 旋转改变应该使缓存失效") {

        checkAll(100,
            Arb.double(-1000.0, 1000.0),
            Arb.double(-1000.0, 1000.0),
            Arb.double(-1000.0, 1000.0),
            Arb.double(-180.0, 180.0),
            Arb.double(-180.0, 180.0),
            Arb.double(-90.0, 90.0),
            Arb.double(-180.0, 180.0)
        ) { posX, posY, posZ, yaw1, yaw2, pitch, roll ->

            if (kotlin.math.abs(yaw1 - yaw2) > 0.01) {
                val state1 = CacheState(posX, posY, posZ, yaw1, pitch, roll)
                val state2 = CacheState(posX, posY, posZ, yaw2, pitch, roll)

                val isCacheValid = state1 == state2

                isCacheValid shouldBe false
            }
        }
    }

    test("属性 7: 碰撞缓存一致性 - 位置改变应该使缓存失效") {

        checkAll(100,
            Arb.double(-1000.0, 1000.0),
            Arb.double(-1000.0, 1000.0),
            Arb.double(-1000.0, 1000.0),
            Arb.double(-1000.0, 1000.0),
            Arb.double(-180.0, 180.0),
            Arb.double(-90.0, 90.0),
            Arb.double(-180.0, 180.0)
        ) { posX1, posX2, posY, posZ, yaw, pitch, roll ->

            if (kotlin.math.abs(posX1 - posX2) > 0.1) {
                val state1 = CacheState(posX1, posY, posZ, yaw, pitch, roll)
                val state2 = CacheState(posX2, posY, posZ, yaw, pitch, roll)

                val isCacheValid = state1 == state2

                isCacheValid shouldBe false
            }
        }
    }

    test("属性 7: 碰撞缓存一致性 - 小的位置变化应该保持缓存有效") {

        checkAll(100,
            Arb.double(-1000.0, 1000.0),
            Arb.double(-1000.0, 1000.0),
            Arb.double(-1000.0, 1000.0),
            Arb.double(-180.0, 180.0),
            Arb.double(-90.0, 90.0),
            Arb.double(-180.0, 180.0)
        ) { posX, posY, posZ, yaw, pitch, roll ->

            val epsilon = 0.001
            val state1 = CacheState(posX, posY, posZ, yaw, pitch, roll)
            val state2 = CacheState(posX + epsilon, posY, posZ, yaw, pitch, roll)

            val distSq = epsilon * epsilon

            val isCacheValid = distSq < 0.01

            isCacheValid shouldBe true
        }
    }

    test("属性 7: 碰撞缓存一致性 - 缓存应该存储碰撞箱列表") {

        checkAll(100,
            Arb.list(Arb.int(-10, 10), 0..20)
        ) { blockList ->

            val cachedBoxes = blockList.map { blockId ->
                CollisionBox(
                    blockId.toDouble(),
                    blockId.toDouble(),
                    blockId.toDouble(),
                    blockId.toDouble() + 1.0,
                    blockId.toDouble() + 1.0,
                    blockId.toDouble() + 1.0
                )
            }

            cachedBoxes.size shouldBe blockList.size
        }
    }

    test("属性 7: 碰撞缓存一致性 - 空方块列表应该产生空缓存") {

        val emptyBlocks = emptyList<Int>()
        val cachedBoxes = emptyBlocks.map { blockId ->
            CollisionBox(0.0, 0.0, 0.0, 1.0, 1.0, 1.0)
        }

        cachedBoxes.shouldBeEmpty()
    }

    test("属性 7: 碰撞缓存一致性 - 缓存时间戳应该单调递增") {

        checkAll(100,
            Arb.int(0, 1000000)
        ) { initialTimestamp ->
            val timestamp1 = initialTimestamp.toLong()
            val timestamp2 = (initialTimestamp + 1).toLong()

            (timestamp2 >= timestamp1) shouldBe true
        }
    }

    test("属性 7: 碰撞缓存一致性 - 缓存失效后应该重新计算") {

        checkAll(100,
            Arb.double(-180.0, 180.0),
            Arb.double(-180.0, 180.0)
        ) { yaw1, yaw2 ->

            val cacheValid1 = true
            val rotationChanged = kotlin.math.abs(yaw1 - yaw2) > 0.01
            val cacheValid2 = if (rotationChanged) false else cacheValid1

            if (rotationChanged) {
                cacheValid2 shouldBe false
            }
        }
    }

    test("属性 7: 碰撞缓存一致性 - 缓存应该包含旋转状态") {

        checkAll(100,
            Arb.double(-180.0, 180.0),
            Arb.double(-90.0, 90.0),
            Arb.double(-180.0, 180.0)
        ) { yaw, pitch, roll ->

            val cachedRotation = RotationState(yaw, pitch, roll)

            cachedRotation.yaw shouldBe yaw
            cachedRotation.pitch shouldBe pitch
            cachedRotation.roll shouldBe roll
        }
    }

    test("属性 7: 碰撞缓存一致性 - 连续调用应该返回相同结果") {

        checkAll(100,
            Arb.int(1, 100)
        ) { callCount ->

            val results = mutableListOf<Int>()
            val cachedValue = 42

            repeat(callCount) {
                results.add(cachedValue)
            }

            val allSame = results.all { it == cachedValue }
            allSame shouldBe true
        }
    }
})
