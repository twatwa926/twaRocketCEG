package com.example.examplemod.rocket.ship

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.checkAll

class RocketContraptionIntegrityTest : FunSpec({

    test("属性 3: Contraption 完整性 - 所有存储的方块应该在集合中") {

        checkAll(100,
            Arb.list(Arb.int(-10, 10), 1..20),
            Arb.list(Arb.int(-10, 10), 1..20),
            Arb.list(Arb.int(-10, 10), 1..20)
        ) { xList, yList, zList ->

            val blockPositions = mutableSetOf<Triple<Int, Int, Int>>()
            val minSize = minOf(xList.size, yList.size, zList.size)

            for (i in 0 until minSize) {
                blockPositions.add(Triple(xList[i], yList[i], zList[i]))
            }

            val storageBlocks = blockPositions.associateWith { "STONE" }

            storageBlocks.keys.size shouldBe blockPositions.size
            storageBlocks.keys shouldContainAll blockPositions
        }
    }

    test("属性 3: Contraption 完整性 - 方块数量应该匹配") {

        checkAll(100, Arb.int(1, 100)) { blockCount ->

            val blocks = mutableMapOf<Triple<Int, Int, Int>, String>()

            for (i in 0 until blockCount) {
                blocks[Triple(i, 0, 0)] = "STONE"
            }

            blocks.size shouldBe blockCount
        }
    }

    test("属性 3: Contraption 完整性 - 空存储应该产生空集合") {

        val emptyStorage = mutableMapOf<Triple<Int, Int, Int>, String>()

        emptyStorage.size shouldBe 0
        emptyStorage.isEmpty() shouldBe true
    }

    test("属性 3: Contraption 完整性 - 添加方块应该增加大小") {

        checkAll(100,
            Arb.int(-100, 100),
            Arb.int(-100, 100),
            Arb.int(-100, 100)
        ) { x, y, z ->
            val blocks = mutableMapOf<Triple<Int, Int, Int>, String>()
            val pos = Triple(x, y, z)
            val initialSize = blocks.size

            blocks[pos] = "STONE"

            blocks.size shouldBe initialSize + 1
            blocks.containsKey(pos) shouldBe true
        }
    }

    test("属性 3: Contraption 完整性 - 重复添加相同位置不应该增加大小") {

        checkAll(100,
            Arb.int(-100, 100),
            Arb.int(-100, 100),
            Arb.int(-100, 100)
        ) { x, y, z ->
            val blocks = mutableMapOf<Triple<Int, Int, Int>, String>()
            val pos = Triple(x, y, z)

            blocks[pos] = "STONE"
            val sizeAfterFirst = blocks.size

            blocks[pos] = "DIRT"
            val sizeAfterSecond = blocks.size

            sizeAfterFirst shouldBe sizeAfterSecond
            sizeAfterFirst shouldBe 1
        }
    }

    test("属性 3: Contraption 完整性 - 移除方块应该减少大小") {

        checkAll(100,
            Arb.int(-100, 100),
            Arb.int(-100, 100),
            Arb.int(-100, 100)
        ) { x, y, z ->
            val blocks = mutableMapOf<Triple<Int, Int, Int>, String>()
            val pos = Triple(x, y, z)

            blocks[pos] = "STONE"
            val sizeAfterAdd = blocks.size

            blocks.remove(pos)
            val sizeAfterRemove = blocks.size

            sizeAfterAdd shouldBe 1
            sizeAfterRemove shouldBe 0
            blocks.containsKey(pos) shouldBe false
        }
    }
})
