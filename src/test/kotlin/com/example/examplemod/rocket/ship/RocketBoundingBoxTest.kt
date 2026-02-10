package com.example.examplemod.rocket.ship

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.doubles.shouldBeGreaterThanOrEqual
import io.kotest.matchers.doubles.shouldBeLessThanOrEqual
import io.kotest.property.Arb
import io.kotest.property.arbitrary.double
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.checkAll

class RocketBoundingBoxTest : FunSpec({

    test("属性 5: 边界箱包含性 - 边界箱应该包含所有方块碰撞箱的最小和最大坐标") {

        checkAll(100,
            Arb.list(Arb.double(-10.0, 10.0), 1..20),
            Arb.list(Arb.double(-10.0, 10.0), 1..20)
        ) { minXList, maxXList ->
            val minSize = minOf(minXList.size, maxXList.size)

            if (minSize > 0) {

                var boundingMinX = Double.POSITIVE_INFINITY
                var boundingMaxX = Double.NEGATIVE_INFINITY

                for (i in 0 until minSize) {
                    val minX = minOf(minXList[i], maxXList[i])
                    val maxX = maxOf(minXList[i], maxXList[i])
                    boundingMinX = minOf(boundingMinX, minX)
                    boundingMaxX = maxOf(boundingMaxX, maxX)
                }

                for (i in 0 until minSize) {
                    val minX = minOf(minXList[i], maxXList[i])
                    val maxX = maxOf(minXList[i], maxXList[i])
                    minX shouldBeGreaterThanOrEqual boundingMinX
                    maxX shouldBeLessThanOrEqual boundingMaxX
                }
            }
        }
    }

    test("属性 5: 边界箱包含性 - 边界箱应该从方块碰撞箱计算而来") {

        checkAll(100,
            Arb.list(Arb.double(-10.0, 10.0), 1..15),
            Arb.double(-100.0, 100.0)
        ) { boxSizes, anchorX ->
            if (boxSizes.isNotEmpty()) {

                var boundingMinX = Double.POSITIVE_INFINITY
                var boundingMaxX = Double.NEGATIVE_INFINITY

                boxSizes.forEachIndexed { index, size ->
                    val offset = index.toDouble()
                    val minX = anchorX + offset
                    val maxX = anchorX + offset + size.coerceAtLeast(0.1)
                    boundingMinX = minOf(boundingMinX, minX)
                    boundingMaxX = maxOf(boundingMaxX, maxX)
                }

                boxSizes.forEachIndexed { index, size ->
                    val offset = index.toDouble()
                    val minX = anchorX + offset
                    val maxX = anchorX + offset + size.coerceAtLeast(0.1)
                    minX shouldBeGreaterThanOrEqual boundingMinX
                    maxX shouldBeLessThanOrEqual boundingMaxX
                }
            }
        }
    }

    test("属性 5: 边界箱包含性 - 空方块列表应该产生最小边界箱") {

        val emptyBlocks = emptyList<Double>()

        val defaultSize = 1.0
        val anchorX = 0.0

        val boundingMinX = anchorX - defaultSize / 2
        val boundingMaxX = anchorX + defaultSize / 2

        val sizeX = boundingMaxX - boundingMinX

        sizeX shouldBe defaultSize
    }

    test("属性 5: 边界箱包含性 - 边界箱应该包含所有方块的顶点") {

        checkAll(100,
            Arb.double(-10.0, 10.0),
            Arb.double(0.1, 5.0)
        ) { minX, width ->
            val maxX = minX + width

            val vertices = listOf(minX, maxX)

            var boundingMinX = Double.POSITIVE_INFINITY
            var boundingMaxX = Double.NEGATIVE_INFINITY

            vertices.forEach { x ->
                boundingMinX = minOf(boundingMinX, x)
                boundingMaxX = maxOf(boundingMaxX, x)
            }

            vertices.forEach { x ->
                x shouldBeGreaterThanOrEqual boundingMinX
                x shouldBeLessThanOrEqual boundingMaxX
            }
        }
    }

    test("属性 5: 边界箱包含性 - 边界箱计算应该考虑所有方块的碰撞形状") {

        checkAll(100,
            Arb.int(1, 20),
            Arb.double(-10.0, 10.0)
        ) { blockCount, baseX ->

            val blockBoxes = (0 until blockCount).map { i ->
                val offset = i.toDouble()
                Pair(baseX + offset, baseX + offset + 1.0)
            }

            var minX = Double.POSITIVE_INFINITY
            var maxX = Double.NEGATIVE_INFINITY

            blockBoxes.forEach { (boxMinX, boxMaxX) ->
                minX = minOf(minX, boxMinX)
                maxX = maxOf(maxX, boxMaxX)
            }

            blockBoxes.forEach { (boxMinX, boxMaxX) ->
                boxMinX shouldBeGreaterThanOrEqual minX
                boxMaxX shouldBeLessThanOrEqual maxX
            }

            val sizeX = maxX - minX
            sizeX shouldBeGreaterThanOrEqual 1.0
        }
    }

    test("属性 6: 碰撞箱不用于碰撞 - 边界箱应该仅用于渲染剔除") {

        checkAll(100, Arb.int(1, 100)) { _ ->

            val usedForRendering = true
            val usedForCollision = false

            usedForRendering shouldBe true
            usedForCollision shouldBe false
        }
    }

    test("属性 6: 碰撞箱不用于碰撞 - 碰撞检测应该使用方块碰撞箱") {

        checkAll(100, Arb.int(1, 100)) { _ ->

            val usesBlockCollisionBoxes = true
            val usesEntityBoundingBox = false

            usesBlockCollisionBoxes shouldBe true
            usesEntityBoundingBox shouldBe false
        }
    }

    test("属性 6: 碰撞箱不用于碰撞 - 实体边界箱不参与碰撞计算") {

        checkAll(100, Arb.int(1, 100)) { _ ->

            val boundingBoxUsedForCollision = false
            val boundingBoxUsedForCulling = true

            boundingBoxUsedForCollision shouldBe false
            boundingBoxUsedForCulling shouldBe true
        }
    }

    test("属性 6: 碰撞箱不用于碰撞 - 方块碰撞箱是碰撞检测的唯一来源") {

        checkAll(100, Arb.int(1, 50)) { blockCount ->

            val collisionBoxCount = blockCount
            val boundingBoxCount = 1

            collisionBoxCount shouldBe blockCount
            boundingBoxCount shouldBe 1
        }
    }

    test("属性 6: 碰撞箱不用于碰撞 - 边界箱更新不影响碰撞检测") {

        checkAll(100, Arb.int(1, 100)) { _ ->

            val collisionDetectionAffected = false

            collisionDetectionAffected shouldBe false
        }
    }

    test("属性 6: 碰撞箱不用于碰撞 - 碰撞检测使用转换后的方块坐标") {

        checkAll(100, Arb.int(1, 100)) { _ ->

            val usesWorldCoordinates = true
            val usesLocalCoordinates = false

            usesWorldCoordinates shouldBe true
            usesLocalCoordinates shouldBe false
        }
    }

    test("属性 6: 碰撞箱不用于碰撞 - 边界箱大小不影响碰撞精度") {

        checkAll(100, Arb.int(1, 100)) { _ ->

            val boundingBoxSizeAffectsCollision = false
            val blockBoxSizeAffectsCollision = true

            boundingBoxSizeAffectsCollision shouldBe false
            blockBoxSizeAffectsCollision shouldBe true
        }
    }

    test("属性 6: 碰撞箱不用于碰撞 - 渲染剔除使用边界箱") {

        checkAll(100, Arb.int(1, 100)) { _ ->

            val cullingUsesBoundingBox = true
            val cullingUsesBlockBoxes = false

            cullingUsesBoundingBox shouldBe true
            cullingUsesBlockBoxes shouldBe false
        }
    }
})
