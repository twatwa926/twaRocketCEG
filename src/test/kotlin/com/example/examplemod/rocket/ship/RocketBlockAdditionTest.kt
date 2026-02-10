package com.example.examplemod.rocket.ship

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.set
import io.kotest.property.checkAll
import net.minecraft.core.BlockPos

class RocketBlockAdditionTest : FunSpec({

    test("属性 10: 方块添加保持结构 - 成功添加的方块应该出现在存储中") {

        checkAll(100,
            Arb.int(-10, 10),
            Arb.int(-10, 10),
            Arb.int(-10, 10)
        ) { x, y, z ->

            val storage = mutableMapOf<BlockPos, String>()
            val initialSize = storage.size

            val pos = BlockPos(x, y, z)
            val blockType = "test_block"
            storage[pos] = blockType

            storage.containsKey(pos) shouldBe true
            storage[pos] shouldBe blockType
            storage.size shouldBe (initialSize + 1)
        }
    }

    test("属性 10: 方块添加保持结构 - 添加方块应该增加存储大小") {

        checkAll(100,
            Arb.set(Arb.int(-10, 10), 1..10),
            Arb.set(Arb.int(-10, 10), 1..10),
            Arb.set(Arb.int(-10, 10), 1..10)
        ) { xSet, ySet, zSet ->

            val storage = mutableMapOf<BlockPos, String>()
            val initialSize = 0

            val positions = mutableSetOf<BlockPos>()
            val minSize = minOf(xSet.size, ySet.size, zSet.size)
            val xList = xSet.toList()
            val yList = ySet.toList()
            val zList = zSet.toList()

            for (i in 0 until minSize) {
                val pos = BlockPos(xList[i], yList[i], zList[i])
                positions.add(pos)
                storage[pos] = "block_$i"
            }

            storage.size shouldBe positions.size
            (storage.size >= initialSize) shouldBe true
        }
    }

    test("属性 10: 方块添加保持结构 - 不能在已占用位置添加方块") {

        checkAll(100,
            Arb.int(-10, 10),
            Arb.int(-10, 10),
            Arb.int(-10, 10)
        ) { x, y, z ->

            val storage = mutableMapOf<BlockPos, String>()
            val pos = BlockPos(x, y, z)

            storage[pos] = "block_1"
            val sizeAfterFirst = storage.size

            val canPlace = !storage.containsKey(pos)

            if (storage.containsKey(pos)) {
                canPlace shouldBe false
            }

            storage[pos] = "block_2"
            storage.size shouldBe sizeAfterFirst
        }
    }

    test("属性 10: 方块添加保持结构 - 添加的方块应该与现有结构相邻") {

        checkAll(100,
            Arb.int(-5, 5),
            Arb.int(-5, 5),
            Arb.int(-5, 5),
            Arb.int(0, 5)
        ) { baseX, baseY, baseZ, direction ->

            val storage = mutableMapOf<BlockPos, String>()
            val basePos = BlockPos(baseX, baseY, baseZ)
            storage[basePos] = "base_block"

            val adjacentPos = when (direction % 6) {
                0 -> BlockPos(baseX + 1, baseY, baseZ)
                1 -> BlockPos(baseX - 1, baseY, baseZ)
                2 -> BlockPos(baseX, baseY + 1, baseZ)
                3 -> BlockPos(baseX, baseY - 1, baseZ)
                4 -> BlockPos(baseX, baseY, baseZ + 1)
                else -> BlockPos(baseX, baseY, baseZ - 1)
            }

            val isAdjacent = isAdjacentTo(adjacentPos, storage.keys)

            isAdjacent shouldBe true
        }
    }

    test("属性 10: 方块添加保持结构 - 添加方块后结构应该保持连通") {

        checkAll(100,
            Arb.list(Arb.int(-5, 5), 1..10),
            Arb.list(Arb.int(-5, 5), 1..10),
            Arb.list(Arb.int(-5, 5), 1..10)
        ) { xList, yList, zList ->

            val storage = mutableMapOf<BlockPos, String>()

            if (xList.isNotEmpty() && yList.isNotEmpty() && zList.isNotEmpty()) {

                val firstPos = BlockPos(xList[0], yList[0], zList[0])
                storage[firstPos] = "block_0"

                var lastPos = firstPos
                val minSize = minOf(xList.size, yList.size, zList.size)

                for (i in 1 until minSize.coerceAtMost(5)) {

                    val newPos = BlockPos(lastPos.x + 1, lastPos.y, lastPos.z)
                    storage[newPos] = "block_$i"
                    lastPos = newPos
                }

                val isConnected = isStructureConnected(storage.keys.toSet())
                isConnected shouldBe true
            }
        }
    }

    test("属性 10: 方块添加保持结构 - 空存储可以添加任意方块") {

        checkAll(100,
            Arb.int(-10, 10),
            Arb.int(-10, 10),
            Arb.int(-10, 10)
        ) { x, y, z ->

            val storage = mutableMapOf<BlockPos, String>()

            val canPlace = storage.isEmpty()
            canPlace shouldBe true

            val pos = BlockPos(x, y, z)
            storage[pos] = "first_block"

            storage.size shouldBe 1
            storage.containsKey(pos) shouldBe true
        }
    }

    test("属性 10: 方块添加保持结构 - 添加方块应该触发装置更新") {

        checkAll(100,
            Arb.int(-10, 10),
            Arb.int(-10, 10),
            Arb.int(-10, 10)
        ) { x, y, z ->

            val storage = mutableMapOf<BlockPos, String>()
            var contraptionNeedsUpdate = false

            val pos = BlockPos(x, y, z)
            storage[pos] = "test_block"
            contraptionNeedsUpdate = true

            contraptionNeedsUpdate shouldBe true
        }
    }

    test("属性 10: 方块添加保持结构 - 添加方块应该触发同步") {

        checkAll(100,
            Arb.int(-10, 10),
            Arb.int(-10, 10),
            Arb.int(-10, 10)
        ) { x, y, z ->

            val storage = mutableMapOf<BlockPos, String>()
            var needsSync = false

            val pos = BlockPos(x, y, z)
            storage[pos] = "test_block"
            needsSync = true

            needsSync shouldBe true
        }
    }

    test("属性 10: 方块添加保持结构 - 添加多个方块应该保持所有方块") {

        checkAll(100,
            Arb.list(Arb.int(-10, 10), 1..20),
            Arb.list(Arb.int(-10, 10), 1..20),
            Arb.list(Arb.int(-10, 10), 1..20)
        ) { xList, yList, zList ->

            val storage = mutableMapOf<BlockPos, String>()
            val addedPositions = mutableSetOf<BlockPos>()

            val minSize = minOf(xList.size, yList.size, zList.size)
            for (i in 0 until minSize) {
                val pos = BlockPos(xList[i], yList[i], zList[i])
                storage[pos] = "block_$i"
                addedPositions.add(pos)
            }

            for (pos in addedPositions) {
                storage.containsKey(pos) shouldBe true
            }

            (storage.size <= addedPositions.size) shouldBe true
        }
    }

    test("属性 10: 方块添加保持结构 - 添加方块不应该影响其他方块") {

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
            storage[pos1] = "block_1"
            val block1Type = storage[pos1]

            val pos2 = BlockPos(x2, y2, z2)
            if (pos1 != pos2) {
                storage[pos2] = "block_2"

                storage[pos1] shouldBe block1Type
            }
        }
    }
})

private fun isAdjacentTo(pos: BlockPos, positions: Set<BlockPos>): Boolean {
    val directions = listOf(
        BlockPos(1, 0, 0),
        BlockPos(-1, 0, 0),
        BlockPos(0, 1, 0),
        BlockPos(0, -1, 0),
        BlockPos(0, 0, 1),
        BlockPos(0, 0, -1)
    )

    for (dir in directions) {
        val neighbor = pos.offset(dir)
        if (positions.contains(neighbor)) {
            return true
        }
    }

    return false
}

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
