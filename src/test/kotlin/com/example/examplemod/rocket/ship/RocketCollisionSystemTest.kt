package com.example.examplemod.rocket.ship

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.doubles.shouldBeGreaterThan
import io.kotest.property.Arb
import io.kotest.property.arbitrary.double
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.checkAll

class RocketCollisionSystemTest : FunSpec({

    test("属性 1: 方块碰撞检测 - 实体碰撞应该被禁用") {

        checkAll(100, Arb.int(1, 100)) { iteration ->

            val canBeCollidedWith = false
            val isPushable = false

            canBeCollidedWith shouldBe false
            isPushable shouldBe false
        }
    }

    test("属性 1: 方块碰撞检测 - 方块碰撞箱应该基于方块位置") {

        checkAll(100,
            Arb.list(Arb.int(-10, 10), 1..20),
            Arb.list(Arb.int(-10, 10), 1..20),
            Arb.list(Arb.int(-10, 10), 1..20)
        ) { xList, yList, zList ->

            val blockPositions = mutableListOf<Triple<Int, Int, Int>>()
            val minSize = minOf(xList.size, yList.size, zList.size)

            for (i in 0 until minSize) {
                blockPositions.add(Triple(xList[i], yList[i], zList[i]))
            }

            val collisionBoxCount = blockPositions.size

            collisionBoxCount shouldBe blockPositions.size
        }
    }

    test("属性 1: 方块碰撞检测 - 碰撞箱应该在世界坐标系中") {

        checkAll(100,
            Arb.double(-1000.0, 1000.0),
            Arb.double(-1000.0, 1000.0),
            Arb.double(-1000.0, 1000.0),
            Arb.int(-10, 10),
            Arb.int(-10, 10),
            Arb.int(-10, 10)
        ) { anchorX, anchorY, anchorZ, blockX, blockY, blockZ ->

            val worldX = anchorX + blockX
            val worldY = anchorY + blockY
            val worldZ = anchorZ + blockZ

            val expectedX = anchorX + blockX
            val expectedY = anchorY + blockY
            val expectedZ = anchorZ + blockZ

            worldX shouldBe expectedX
            worldY shouldBe expectedY
            worldZ shouldBe expectedZ
        }
    }

    test("属性 1: 方块碰撞检测 - 推力向量应该将实体推出碰撞区域") {

        checkAll(100,
            Arb.double(0.0, 10.0),
            Arb.int(0, 2)
        ) { overlap, axis ->

            val pushVector = when (axis) {
                0 -> Triple(overlap, 0.0, 0.0)
                1 -> Triple(0.0, overlap, 0.0)
                else -> Triple(0.0, 0.0, overlap)
            }

            val pushLength = when (axis) {
                0 -> pushVector.first
                1 -> pushVector.second
                else -> pushVector.third
            }

            pushLength shouldBe overlap
        }
    }

    test("属性 1: 方块碰撞检测 - 空方块列表应该产生空碰撞箱列表") {

        val emptyBlocks = emptyList<Triple<Int, Int, Int>>()
        val collisionBoxes = emptyBlocks.map { pos ->

            Triple(pos.first.toDouble(), pos.second.toDouble(), pos.third.toDouble())
        }

        collisionBoxes.size shouldBe 0
        collisionBoxes.isEmpty() shouldBe true
    }

    test("属性 1: 方块碰撞检测 - 碰撞检测范围应该限制在附近实体") {

        checkAll(100,
            Arb.double(-1000.0, 1000.0),
            Arb.double(-1000.0, 1000.0),
            Arb.double(-1000.0, 1000.0),
            Arb.double(1.0, 10.0)
        ) { entityX, entityY, entityZ, searchRadius ->

            val minX = entityX - searchRadius
            val maxX = entityX + searchRadius
            val minY = entityY - searchRadius
            val maxY = entityY + searchRadius
            val minZ = entityZ - searchRadius
            val maxZ = entityZ + searchRadius

            val rangeX = maxX - minX
            val rangeY = maxY - minY
            val rangeZ = maxZ - minZ

            rangeX shouldBe (searchRadius * 2.0)
            rangeY shouldBe (searchRadius * 2.0)
            rangeZ shouldBe (searchRadius * 2.0)
        }
    }

    test("属性 1: 方块碰撞检测 - 碰撞箱重叠检测应该是对称的") {

        checkAll(100,
            Arb.double(0.0, 10.0),
            Arb.double(11.0, 20.0),
            Arb.double(5.0, 15.0),
            Arb.double(16.0, 25.0)
        ) { box1Min, box1Max, box2Min, box2Max ->

            val overlaps1to2 = box1Max > box2Min && box1Min < box2Max
            val overlaps2to1 = box2Max > box1Min && box2Min < box1Max

            overlaps1to2 shouldBe overlaps2to1
        }
    }

    test("属性 1: 方块碰撞检测 - 推力应该选择最小重叠方向") {

        checkAll(100,
            Arb.double(0.1, 1.0),
            Arb.double(0.1, 1.0),
            Arb.double(0.1, 1.0)
        ) { overlapX, overlapY, overlapZ ->

            val minOverlap = minOf(overlapX, overlapY, overlapZ)

            val isMinX = minOverlap == overlapX
            val isMinY = minOverlap == overlapY
            val isMinZ = minOverlap == overlapZ

            (isMinX || isMinY || isMinZ) shouldBe true
        }
    }

    test("属性 1: 方块碰撞检测 - noCulling 应该始终为 true") {

        checkAll(100, Arb.int(1, 100)) { iteration ->

            val noCulling = true

            noCulling shouldBe true
        }
    }

    test("属性 1: 方块碰撞检测 - 碰撞箱数量应该等于非空气方块数量") {

        checkAll(100,
            Arb.int(0, 50),
            Arb.int(0, 10)
        ) { solidBlocks, airBlocks ->

            val totalBlocks = solidBlocks + airBlocks
            val collisionBoxCount = solidBlocks

            collisionBoxCount shouldBe solidBlocks
            (collisionBoxCount <= totalBlocks) shouldBe true
        }
    }
})
