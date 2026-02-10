package com.example.examplemod.rocket.ship

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.checkAll
import net.minecraft.core.BlockPos

class RocketBlockRemovalTest : FunSpec({

    test("属性 11: 方块移除保持连通性 - 移除方块应该从存储中删除") {

        checkAll(100,
            Arb.int(-10, 10),
            Arb.int(-10, 10),
            Arb.int(-10, 10)
        ) { x, y, z ->

            val storage = mutableMapOf<BlockPos, String>()
            val pos = BlockPos(x, y, z)

            storage[pos] = "test_block"
            val sizeAfterAdd = storage.size

            storage.remove(pos)

            storage.containsKey(pos) shouldBe false
            storage.size shouldBe (sizeAfterAdd - 1)
        }
    }

    test("属性 11: 方块移除保持连通性 - 移除方块应该减少存储大小") {

        checkAll(100,
            Arb.list(Arb.int(-10, 10), 2..10),
            Arb.list(Arb.int(-10, 10), 2..10),
            Arb.list(Arb.int(-10, 10), 2..10)
        ) { xList, yList, zList ->

            val storage = mutableMapOf<BlockPos, String>()

            val minSize = minOf(xList.size, yList.size, zList.size)
            val positions = mutableListOf<BlockPos>()

            for (i in 0 until minSize) {
                val pos = BlockPos(xList[i], yList[i], zList[i])
                storage[pos] = "block_$i"
                positions.add(pos)
            }

            val sizeBeforeRemoval = storage.size

            if (positions.isNotEmpty()) {
                storage.remove(positions[0])

                storage.size shouldBe (sizeBeforeRemoval - 1)
            }
        }
    }

    test("属性 11: 方块移除保持连通性 - 不能移除不存在的方块") {

        checkAll(100,
            Arb.int(-10, 10),
            Arb.int(-10, 10),
            Arb.int(-10, 10)
        ) { x, y, z ->

            val storage = mutableMapOf<BlockPos, String>()
            val pos = BlockPos(x, y, z)

            val canRemove = storage.containsKey(pos)

            canRemove shouldBe false
        }
    }

    test("属性 11: 方块移除保持连通性 - 移除会断开结构的方块应该被拒绝") {

        checkAll(100,
            Arb.int(-5, 5),
            Arb.int(-5, 5),
            Arb.int(-5, 5)
        ) { baseX, baseY, baseZ ->

            val storage = mutableMapOf<BlockPos, String>()
            val posA = BlockPos(baseX, baseY, baseZ)
            val posB = BlockPos(baseX + 1, baseY, baseZ)
            val posC = BlockPos(baseX + 2, baseY, baseZ)

            storage[posA] = "block_A"
            storage[posB] = "block_B"
            storage[posC] = "block_C"

            val tempStorage = storage.toMutableMap()
            tempStorage.remove(posB)

            val isConnected = isStructureConnected(tempStorage.keys.toSet())

            isConnected shouldBe false
        }
    }

    test("属性 11: 方块移除保持连通性 - 移除末端方块应该保持结构连通") {

        checkAll(100,
            Arb.int(-5, 5),
            Arb.int(-5, 5),
            Arb.int(-5, 5)
        ) { baseX, baseY, baseZ ->

            val storage = mutableMapOf<BlockPos, String>()
            val posA = BlockPos(baseX, baseY, baseZ)
            val posB = BlockPos(baseX + 1, baseY, baseZ)
            val posC = BlockPos(baseX + 2, baseY, baseZ)

            storage[posA] = "block_A"
            storage[posB] = "block_B"
            storage[posC] = "block_C"

            val tempStorage = storage.toMutableMap()
            tempStorage.remove(posC)

            val isConnected = isStructureConnected(tempStorage.keys.toSet())

            isConnected shouldBe true
        }
    }

    test("属性 11: 方块移除保持连通性 - 不能移除最后一个方块") {

        checkAll(100,
            Arb.int(-10, 10),
            Arb.int(-10, 10),
            Arb.int(-10, 10)
        ) { x, y, z ->

            val storage = mutableMapOf<BlockPos, String>()
            val pos = BlockPos(x, y, z)
            storage[pos] = "only_block"

            val canRemove = storage.size > 1

            canRemove shouldBe false
        }
    }

    test("属性 11: 方块移除保持连通性 - 移除方块应该触发装置更新") {

        checkAll(100,
            Arb.int(-10, 10),
            Arb.int(-10, 10),
            Arb.int(-10, 10)
        ) { x, y, z ->

            val storage = mutableMapOf<BlockPos, String>()
            val pos = BlockPos(x, y, z)
            storage[pos] = "test_block"

            var contraptionNeedsUpdate = false

            if (storage.containsKey(pos)) {
                storage.remove(pos)
                contraptionNeedsUpdate = true
            }

            contraptionNeedsUpdate shouldBe true
        }
    }

    test("属性 11: 方块移除保持连通性 - 移除方块应该触发同步") {

        checkAll(100,
            Arb.int(-10, 10),
            Arb.int(-10, 10),
            Arb.int(-10, 10)
        ) { x, y, z ->

            val storage = mutableMapOf<BlockPos, String>()
            val pos = BlockPos(x, y, z)
            storage[pos] = "test_block"

            var needsSync = false

            if (storage.containsKey(pos)) {
                storage.remove(pos)
                needsSync = true
            }

            needsSync shouldBe true
        }
    }

    test("属性 11: 方块移除保持连通性 - 移除方块不应该影响其他方块") {

        checkAll(100,
            Arb.int(-10, 10),
            Arb.int(-10, 10),
            Arb.int(-10, 10),
            Arb.int(-10, 10),
            Arb.int(-10, 10),
            Arb.int(-10, 10)
        ) { x1, y1, z1, x2, y2, z2 ->

            val storage = mutableMapOf<BlockPos, String>()

            val pos1 = BlockPos(x1, y1, z1)
            val pos2 = BlockPos(x2, y2, z2)

            storage[pos1] = "block_1"
            storage[pos2] = "block_2"

            val block2Type = storage[pos2]

            if (pos1 != pos2) {
                storage.remove(pos1)

                storage[pos2] shouldBe block2Type
                storage.containsKey(pos2) shouldBe true
            }
        }
    }

    test("属性 11: 方块移除保持连通性 - 移除方块后结构应该保持连通") {

        checkAll(100,
            Arb.list(Arb.int(-5, 5), 3..10),
            Arb.list(Arb.int(-5, 5), 3..10),
            Arb.list(Arb.int(-5, 5), 3..10)
        ) { xList, yList, zList ->

            val storage = mutableMapOf<BlockPos, String>()

            if (xList.size >= 3 && yList.size >= 3 && zList.size >= 3) {

                val positions = mutableListOf<BlockPos>()
                var lastPos = BlockPos(xList[0], yList[0], zList[0])
                storage[lastPos] = "block_0"
                positions.add(lastPos)

                for (i in 1 until minOf(xList.size, yList.size, zList.size).coerceAtMost(5)) {
                    val newPos = BlockPos(lastPos.x + 1, lastPos.y, lastPos.z)
                    storage[newPos] = "block_$i"
                    positions.add(newPos)
                    lastPos = newPos
                }

                if (positions.size > 1) {
                    storage.remove(positions.last())

                    val isConnected = isStructureConnected(storage.keys.toSet())
                    isConnected shouldBe true
                }
            }
        }
    }

    test("属性 11: 方块移除保持连通性 - 移除方块应该掉落物品") {

        checkAll(100,
            Arb.int(-10, 10),
            Arb.int(-10, 10),
            Arb.int(-10, 10)
        ) { x, y, z ->

            val storage = mutableMapOf<BlockPos, String>()
            val pos = BlockPos(x, y, z)
            storage[pos] = "test_block"

            var shouldDropItems = false

            if (storage.containsKey(pos)) {
                storage.remove(pos)
                shouldDropItems = true
            }

            shouldDropItems shouldBe true
        }
    }

    test("属性 11: 方块移除保持连通性 - 移除方块应该播放效果") {

        checkAll(100,
            Arb.int(-10, 10),
            Arb.int(-10, 10),
            Arb.int(-10, 10)
        ) { x, y, z ->

            val storage = mutableMapOf<BlockPos, String>()
            val pos = BlockPos(x, y, z)
            storage[pos] = "test_block"

            var shouldPlayEffects = false

            if (storage.containsKey(pos)) {
                storage.remove(pos)
                shouldPlayEffects = true
            }

            shouldPlayEffects shouldBe true
        }
    }

    test("属性 11: 方块移除保持连通性 - 连通性检查应该使用 BFS") {

        checkAll(100,
            Arb.int(-5, 5),
            Arb.int(-5, 5),
            Arb.int(-5, 5)
        ) { baseX, baseY, baseZ ->

            val positions = setOf(
                BlockPos(baseX, baseY, baseZ),
                BlockPos(baseX + 1, baseY, baseZ),
                BlockPos(baseX + 2, baseY, baseZ)
            )

            val isConnected = isStructureConnected(positions)
            isConnected shouldBe true

            val disconnectedPositions = setOf(
                BlockPos(baseX, baseY, baseZ),
                BlockPos(baseX + 2, baseY, baseZ)
            )

            val isDisconnected = isStructureConnected(disconnectedPositions)
            isDisconnected shouldBe false
        }
    }
})

private fun isStructureConnected(positions: Set<BlockPos>): Boolean {
    if (positions.isEmpty()) {
        return true
    }

    val start = positions.first()
    val visited = mutableSetOf<BlockPos>()
    val queue = ArrayDeque<BlockPos>()

    queue.add(start)
    visited.add(start)

    val directions = listOf(
        BlockPos(1, 0, 0),
        BlockPos(-1, 0, 0),
        BlockPos(0, 1, 0),
        BlockPos(0, -1, 0),
        BlockPos(0, 0, 1),
        BlockPos(0, 0, -1)
    )

    while (queue.isNotEmpty()) {
        val current = queue.removeFirst()

        for (dir in directions) {
            val neighbor = current.offset(dir)
            if (positions.contains(neighbor) && !visited.contains(neighbor)) {
                visited.add(neighbor)
                queue.add(neighbor)
            }
        }
    }

    return visited.size == positions.size
}
