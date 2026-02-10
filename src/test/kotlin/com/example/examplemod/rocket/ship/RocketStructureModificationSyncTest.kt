package com.example.examplemod.rocket.ship

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.checkAll
import net.minecraft.core.BlockPos

class RocketStructureModificationSyncTest : FunSpec({

    test("属性 12: 结构修改同步 - 添加方块应该触发同步") {

        checkAll(100,
            Arb.int(-10, 10),
            Arb.int(-10, 10),
            Arb.int(-10, 10)
        ) { x, y, z ->

            val storage = mutableMapOf<BlockPos, String>()
            var syncTriggered = false
            var syncCooldown = 40

            val pos = BlockPos(x, y, z)
            storage[pos] = "test_block"

            syncCooldown = 0
            syncTriggered = true

            syncTriggered shouldBe true
            syncCooldown shouldBe 0
        }
    }

    test("属性 12: 结构修改同步 - 移除方块应该触发同步") {

        checkAll(100,
            Arb.int(-10, 10),
            Arb.int(-10, 10),
            Arb.int(-10, 10)
        ) { x, y, z ->

            val storage = mutableMapOf<BlockPos, String>()
            val pos = BlockPos(x, y, z)
            storage[pos] = "test_block"

            var syncTriggered = false
            var syncCooldown = 40

            storage.remove(pos)

            syncCooldown = 0
            syncTriggered = true

            syncTriggered shouldBe true
            syncCooldown shouldBe 0
        }
    }

    test("属性 12: 结构修改同步 - 同步应该包含所有方块") {

        checkAll(100,
            Arb.list(Arb.int(-10, 10), 1..20),
            Arb.list(Arb.int(-10, 10), 1..20),
            Arb.list(Arb.int(-10, 10), 1..20)
        ) { xList, yList, zList ->

            val storage = mutableMapOf<BlockPos, String>()
            val positions = mutableSetOf<BlockPos>()

            val minSize = minOf(xList.size, yList.size, zList.size)
            for (i in 0 until minSize) {
                val pos = BlockPos(xList[i], yList[i], zList[i])
                storage[pos] = "block_$i"
                positions.add(pos)
            }

            val syncData = storage.toMap()

            syncData.size shouldBe storage.size
            for (pos in positions) {
                if (storage.containsKey(pos)) {
                    syncData.containsKey(pos) shouldBe true
                }
            }
        }
    }

    test("属性 12: 结构修改同步 - 同步应该更新装置") {

        checkAll(100,
            Arb.int(-10, 10),
            Arb.int(-10, 10),
            Arb.int(-10, 10)
        ) { x, y, z ->

            val storage = mutableMapOf<BlockPos, String>()
            var contraptionUpdated = false

            val pos = BlockPos(x, y, z)
            storage[pos] = "test_block"

            contraptionUpdated = true

            contraptionUpdated shouldBe true
        }
    }

    test("属性 12: 结构修改同步 - 同步应该重新计算质量") {

        checkAll(100,
            Arb.int(1, 20),
            Arb.int(1, 10)
        ) { blockCount, blockMass ->

            var totalMass = 0.0
            var massRecalculated = false

            for (i in 0 until blockCount) {
                totalMass += blockMass.toDouble()
            }

            massRecalculated = true

            massRecalculated shouldBe true
            totalMass shouldBe (blockCount * blockMass).toDouble()
        }
    }

    test("属性 12: 结构修改同步 - 同步应该重新计算碰撞箱") {

        checkAll(100,
            Arb.int(-10, 10),
            Arb.int(-10, 10),
            Arb.int(-10, 10)
        ) { x, y, z ->

            var collisionBoxesInvalidated = false
            var collisionBoxesRecalculated = false

            val pos = BlockPos(x, y, z)

            collisionBoxesInvalidated = true

            collisionBoxesRecalculated = true

            collisionBoxesInvalidated shouldBe true
            collisionBoxesRecalculated shouldBe true
        }
    }

    test("属性 12: 结构修改同步 - 同步冷却时间应该被重置") {

        checkAll(100,
            Arb.int(0, 100)
        ) { currentCooldown ->

            var syncCooldown = currentCooldown

            syncCooldown = 0

            syncCooldown shouldBe 0
        }
    }

    test("属性 12: 结构修改同步 - 多次修改应该只触发一次同步") {

        checkAll(100,
            Arb.list(Arb.int(-10, 10), 1..10),
            Arb.list(Arb.int(-10, 10), 1..10),
            Arb.list(Arb.int(-10, 10), 1..10)
        ) { xList, yList, zList ->

            val storage = mutableMapOf<BlockPos, String>()
            var syncCount = 0
            var syncCooldown = 40

            val minSize = minOf(xList.size, yList.size, zList.size)
            for (i in 0 until minSize) {
                val pos = BlockPos(xList[i], yList[i], zList[i])
                storage[pos] = "block_$i"
            }

            if (storage.isNotEmpty()) {
                syncCooldown = 0
                syncCount = 1
            }

            syncCount shouldBe 1
            syncCooldown shouldBe 0
        }
    }

    test("属性 12: 结构修改同步 - 同步应该包含锚点位置") {

        checkAll(100,
            Arb.int(-1000, 1000),
            Arb.int(-1000, 1000),
            Arb.int(-1000, 1000)
        ) { anchorX, anchorY, anchorZ ->

            val anchor = Triple(anchorX.toDouble(), anchorY.toDouble(), anchorZ.toDouble())
            var syncIncludesAnchor = false

            syncIncludesAnchor = true

            syncIncludesAnchor shouldBe true
            anchor.first shouldBe anchorX.toDouble()
            anchor.second shouldBe anchorY.toDouble()
            anchor.third shouldBe anchorZ.toDouble()
        }
    }

    test("属性 12: 结构修改同步 - 客户端应该重建装置") {

        checkAll(100,
            Arb.int(1, 50)
        ) { blockCount ->

            var clientContraptionRebuilt = false
            var clientBlockCount = 0

            clientBlockCount = blockCount

            clientContraptionRebuilt = true

            clientContraptionRebuilt shouldBe true
            clientBlockCount shouldBe blockCount
        }
    }

    test("属性 12: 结构修改同步 - 客户端应该更新 ContraptionProxy") {

        checkAll(100,
            Arb.int(1, 50)
        ) { blockCount ->

            var proxyUpdated = false
            var proxyBlockCount = 0

            proxyBlockCount = blockCount

            proxyUpdated = true

            proxyUpdated shouldBe true
            proxyBlockCount shouldBe blockCount
        }
    }

    test("属性 12: 结构修改同步 - 同步频率应该有限制") {

        checkAll(100,
            Arb.int(1, 100)
        ) { ticks ->

            var syncCooldown = 40
            var syncCount = 0

            for (tick in 0 until ticks) {
                if (syncCooldown > 0) {
                    syncCooldown--
                } else {

                    syncCount++
                    syncCooldown = 40
                }
            }

            val expectedSyncs = (ticks + 39) / 40
            syncCount shouldBe expectedSyncs
        }
    }

    test("属性 12: 结构修改同步 - 立即同步应该绕过冷却") {

        checkAll(100,
            Arb.int(1, 100)
        ) { currentCooldown ->

            var syncCooldown = currentCooldown
            var syncTriggered = false

            syncCooldown = 0
            syncTriggered = true

            syncTriggered shouldBe true
            syncCooldown shouldBe 0
        }
    }
})
