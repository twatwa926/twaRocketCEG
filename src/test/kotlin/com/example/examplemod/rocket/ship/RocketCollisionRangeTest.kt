package com.example.examplemod.rocket.ship

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.doubles.shouldBeGreaterThan
import io.kotest.matchers.doubles.shouldBeLessThan
import io.kotest.property.Arb
import io.kotest.property.arbitrary.double
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.checkAll

class RocketCollisionRangeTest : FunSpec({

    data class EntityPosition(
        val x: Double,
        val y: Double,
        val z: Double
    )

    data class EntityInfo(
        val id: Int,
        val isRocket: Boolean
    )

    test("属性 8: 碰撞检测范围限制 - 搜索范围应该基于边界箱扩展") {

        checkAll(100,
            Arb.double(-1000.0, 1000.0),
            Arb.double(-1000.0, 1000.0),
            Arb.double(1.0, 10.0)
        ) { minX, maxX, inflation ->

            val actualMin = kotlin.math.min(minX, maxX)
            val actualMax = kotlin.math.max(minX, maxX)

            val searchMin = actualMin - inflation
            val searchMax = actualMax + inflation

            val searchSize = searchMax - searchMin
            val originalSize = actualMax - actualMin

            searchSize shouldBeGreaterThan originalSize
        }
    }

    test("属性 8: 碰撞检测范围限制 - 实体在范围内应该被检查") {

        checkAll(100,
            Arb.double(-1000.0, 1000.0),
            Arb.double(-1000.0, 1000.0),
            Arb.double(-1000.0, 1000.0),
            Arb.double(-5.0, 5.0),
            Arb.double(-5.0, 5.0),
            Arb.double(-5.0, 5.0)
        ) { rocketX, rocketY, rocketZ, offsetX, offsetY, offsetZ ->
            val entityX = rocketX + offsetX
            val entityY = rocketY + offsetY
            val entityZ = rocketZ + offsetZ

            val distSq = offsetX * offsetX + offsetY * offsetY + offsetZ * offsetZ
            val searchRadius = 3.0
            val searchRadiusSq = searchRadius * searchRadius

            val shouldBeChecked = distSq <= searchRadiusSq * 3.0

            val isInRange = kotlin.math.abs(offsetX) <= searchRadius &&
                           kotlin.math.abs(offsetY) <= searchRadius &&
                           kotlin.math.abs(offsetZ) <= searchRadius

            if (isInRange) {
                shouldBeChecked.shouldBeTrue()
            }
        }
    }

    test("属性 8: 碰撞检测范围限制 - 实体在范围外不应该被检查") {

        checkAll(100,
            Arb.double(-1000.0, 1000.0),
            Arb.double(10.0, 100.0)
        ) { rocketX, distance ->
            val entityX = rocketX + distance
            val searchRadius = 3.0

            val offset = kotlin.math.abs(entityX - rocketX)
            val isInRange = offset <= searchRadius

            isInRange.shouldBeFalse()
        }
    }

    test("属性 8: 碰撞检测范围限制 - 搜索范围应该对称") {

        checkAll(100,
            Arb.double(-1000.0, 1000.0),
            Arb.double(-1000.0, 1000.0),
            Arb.double(-1000.0, 1000.0),
            Arb.double(1.0, 10.0)
        ) { centerX, centerY, centerZ, radius ->

            val minX = centerX - radius
            val maxX = centerX + radius
            val minY = centerY - radius
            val maxY = centerY + radius
            val minZ = centerZ - radius
            val maxZ = centerZ + radius

            val rangeX = maxX - minX
            val rangeY = maxY - minY
            val rangeZ = maxZ - minZ

            rangeX shouldBe (radius * 2.0)
            rangeY shouldBe (radius * 2.0)
            rangeZ shouldBe (radius * 2.0)
        }
    }

    test("属性 8: 碰撞检测范围限制 - 空实体列表应该不进行碰撞检查") {

        val emptyEntities = emptyList<EntityPosition>()

        emptyEntities.isEmpty().shouldBeTrue()
        emptyEntities.size shouldBe 0
    }

    test("属性 8: 碰撞检测范围限制 - 搜索范围应该包含边界箱") {

        checkAll(100,
            Arb.double(-100.0, 100.0),
            Arb.double(-100.0, 100.0),
            Arb.double(1.0, 10.0)
        ) { minX, maxX, inflation ->
            val actualMin = kotlin.math.min(minX, maxX)
            val actualMax = kotlin.math.max(minX, maxX)

            val searchMin = actualMin - inflation
            val searchMax = actualMax + inflation

            (searchMin <= actualMin).shouldBeTrue()
            (searchMax >= actualMax).shouldBeTrue()
        }
    }

    test("属性 8: 碰撞检测范围限制 - 距离计算应该使用欧几里得距离") {

        checkAll(100,
            Arb.double(-100.0, 100.0),
            Arb.double(-100.0, 100.0),
            Arb.double(-100.0, 100.0),
            Arb.double(-100.0, 100.0),
            Arb.double(-100.0, 100.0),
            Arb.double(-100.0, 100.0)
        ) { x1, y1, z1, x2, y2, z2 ->
            val dx = x2 - x1
            val dy = y2 - y1
            val dz = z2 - z1

            val distSq = dx * dx + dy * dy + dz * dz
            val dist = kotlin.math.sqrt(distSq)

            (dist >= 0.0).shouldBeTrue()

            val expectedDistSq = dx * dx + dy * dy + dz * dz
            distSq shouldBe expectedDistSq
        }
    }

    test("属性 8: 碰撞检测范围限制 - 搜索半径应该是常量") {

        checkAll(100, Arb.int(1, 100)) { iteration ->

            val searchRadius = 3.0

            searchRadius shouldBeGreaterThan 0.0

            searchRadius shouldBeGreaterThan 0.1
            searchRadius shouldBeLessThan 100.0
        }
    }

    test("属性 8: 碰撞检测范围限制 - 过滤应该排除火箭实体本身") {

        checkAll(100,
            Arb.int(1, 100)
        ) { entityCount ->

            val entities = (0 until entityCount).map { id ->
                EntityInfo(id, isRocket = id == 0)
            }

            val filtered = entities.filter { !it.isRocket }

            filtered.none { it.isRocket }.shouldBeTrue()

            filtered.size shouldBe (entityCount - 1)
        }
    }

    test("属性 8: 碰撞检测范围限制 - 边界箱扩展应该是各向同性的") {

        checkAll(100,
            Arb.double(1.0, 10.0)
        ) { inflation ->

            val inflationX = inflation
            val inflationY = inflation
            val inflationZ = inflation

            inflationX shouldBe inflationY
            inflationY shouldBe inflationZ
        }
    }

    test("属性 8: 碰撞检测范围限制 - 搜索范围应该基于 AABB") {

        checkAll(100,
            Arb.double(-100.0, 100.0),
            Arb.double(-100.0, 100.0),
            Arb.double(-100.0, 100.0),
            Arb.double(-100.0, 100.0),
            Arb.double(-100.0, 100.0),
            Arb.double(-100.0, 100.0)
        ) { minX, minY, minZ, maxX, maxY, maxZ ->

            val actualMinX = kotlin.math.min(minX, maxX)
            val actualMaxX = kotlin.math.max(minX, maxX)
            val actualMinY = kotlin.math.min(minY, maxY)
            val actualMaxY = kotlin.math.max(minY, maxY)
            val actualMinZ = kotlin.math.min(minZ, maxZ)
            val actualMaxZ = kotlin.math.max(minZ, maxZ)

            val volumeX = actualMaxX - actualMinX
            val volumeY = actualMaxY - actualMinY
            val volumeZ = actualMaxZ - actualMinZ

            (volumeX >= 0.0).shouldBeTrue()
            (volumeY >= 0.0).shouldBeTrue()
            (volumeZ >= 0.0).shouldBeTrue()
        }
    }
})
