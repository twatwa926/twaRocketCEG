package com.example.examplemod.rocket.ship

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.types.shouldBeInstanceOf
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState

class RocketRenderingSystemTest : FunSpec({

    test("ContraptionProxy 创建 - 应该正确初始化") {

        val storage = RocketShipStorage(mutableMapOf(
            BlockPos(0, 0, 0) to Blocks.STONE.defaultBlockState(),
            BlockPos(1, 0, 0) to Blocks.DIRT.defaultBlockState()
        ))

        val contraption = RocketShipContraption()
        contraption.rebuildFromStorage(storage)

        contraption.blocks.size shouldBe 2
        contraption.blocks.keys.contains(BlockPos(0, 0, 0)) shouldBe true
        contraption.blocks.keys.contains(BlockPos(1, 0, 0)) shouldBe true
    }

    test("ContraptionProxy 创建 - 应该包含所有方块") {

        val blocks = mutableMapOf<BlockPos, BlockState>()
        for (x in 0..2) {
            for (y in 0..2) {
                for (z in 0..2) {
                    blocks[BlockPos(x, y, z)] = Blocks.STONE.defaultBlockState()
                }
            }
        }
        val storage = RocketShipStorage(blocks)

        val contraption = RocketShipContraption()
        contraption.rebuildFromStorage(storage)

        contraption.blocks.size shouldBe 27
        for (x in 0..2) {
            for (y in 0..2) {
                for (z in 0..2) {
                    contraption.blocks.keys.contains(BlockPos(x, y, z)) shouldBe true
                }
            }
        }
    }

    test("ContraptionProxy 创建 - 空结构应该正确处理") {

        val storage = RocketShipStorage(mutableMapOf())

        val contraption = RocketShipContraption()
        contraption.rebuildFromStorage(storage)

        contraption.blocks.size shouldBe 0
        contraption.bounds.shouldNotBeNull()
    }

    test("ContraptionProxy 创建 - 边界箱应该正确计算") {

        val storage = RocketShipStorage(mutableMapOf(
            BlockPos(0, 0, 0) to Blocks.STONE.defaultBlockState(),
            BlockPos(5, 3, 2) to Blocks.DIRT.defaultBlockState()
        ))

        val contraption = RocketShipContraption()
        contraption.rebuildFromStorage(storage)

        val bounds = contraption.bounds
        bounds.shouldNotBeNull()

        bounds.minX shouldBe 0.0
        bounds.minY shouldBe 0.0
        bounds.minZ shouldBe 0.0
        bounds.maxX shouldBe 6.0
        bounds.maxY shouldBe 4.0
        bounds.maxZ shouldBe 3.0
    }

    test("纹理加载 - 原版方块应该有有效的方块状态") {

        val vanillaBlocks = listOf(
            Blocks.STONE,
            Blocks.DIRT,
            Blocks.GRASS_BLOCK,
            Blocks.OAK_PLANKS,
            Blocks.GLASS,
            Blocks.IRON_BLOCK
        )

        for (block in vanillaBlocks) {
            val state = block.defaultBlockState()
            state.shouldNotBeNull()
            state.isAir shouldBe false
            state.block shouldBe block
        }
    }

    test("纹理加载 - 方块状态应该保持属性") {

        val stoneState = Blocks.STONE.defaultBlockState()
        val dirtState = Blocks.DIRT.defaultBlockState()

        (stoneState == dirtState) shouldBe false

        stoneState.block shouldBe Blocks.STONE
        dirtState.block shouldBe Blocks.DIRT
    }

    test("纹理加载 - 空气方块应该被正确识别") {

        val airState = Blocks.AIR.defaultBlockState()

        airState.shouldNotBeNull()
        airState.isAir shouldBe true
        airState.block shouldBe Blocks.AIR
    }

    test("缺失纹理警告 - 应该能够检测空气方块") {

        val storage = RocketShipStorage(mutableMapOf(
            BlockPos(0, 0, 0) to Blocks.STONE.defaultBlockState(),
            BlockPos(1, 0, 0) to Blocks.AIR.defaultBlockState(),
            BlockPos(2, 0, 0) to Blocks.DIRT.defaultBlockState()
        ))

        var airBlockCount = 0
        for ((_, state) in storage.blocks) {
            if (state.isAir) {
                airBlockCount++
            }
        }

        airBlockCount shouldBe 1
    }

    test("缺失纹理警告 - 应该能够过滤空气方块") {

        val storage = RocketShipStorage(mutableMapOf(
            BlockPos(0, 0, 0) to Blocks.STONE.defaultBlockState(),
            BlockPos(1, 0, 0) to Blocks.AIR.defaultBlockState(),
            BlockPos(2, 0, 0) to Blocks.DIRT.defaultBlockState()
        ))

        val nonAirBlocks = storage.blocks.filter { !it.value.isAir }

        nonAirBlocks.size shouldBe 2
        nonAirBlocks.keys.contains(BlockPos(0, 0, 0)) shouldBe true
        nonAirBlocks.keys.contains(BlockPos(2, 0, 0)) shouldBe true
        nonAirBlocks.keys.contains(BlockPos(1, 0, 0)) shouldBe false
    }

    test("ContraptionProxy 更新 - 应该正确更新方块数据") {

        val storage1 = RocketShipStorage(mutableMapOf(
            BlockPos(0, 0, 0) to Blocks.STONE.defaultBlockState()
        ))

        val contraption = RocketShipContraption()
        contraption.rebuildFromStorage(storage1)

        contraption.blocks.size shouldBe 1

        val storage2 = RocketShipStorage(mutableMapOf(
            BlockPos(0, 0, 0) to Blocks.STONE.defaultBlockState(),
            BlockPos(1, 0, 0) to Blocks.DIRT.defaultBlockState()
        ))

        contraption.rebuildFromStorage(storage2)

        contraption.blocks.size shouldBe 2
    }

    test("ContraptionProxy 更新 - 应该正确处理方块移除") {

        val storage1 = RocketShipStorage(mutableMapOf(
            BlockPos(0, 0, 0) to Blocks.STONE.defaultBlockState(),
            BlockPos(1, 0, 0) to Blocks.DIRT.defaultBlockState()
        ))

        val contraption = RocketShipContraption()
        contraption.rebuildFromStorage(storage1)

        contraption.blocks.size shouldBe 2

        val storage2 = RocketShipStorage(mutableMapOf(
            BlockPos(0, 0, 0) to Blocks.STONE.defaultBlockState()
        ))

        contraption.rebuildFromStorage(storage2)

        contraption.blocks.size shouldBe 1
        contraption.blocks.keys.contains(BlockPos(0, 0, 0)) shouldBe true
        contraption.blocks.keys.contains(BlockPos(1, 0, 0)) shouldBe false
    }

    test("渲染系统集成 - Contraption 应该维护正确的锚点") {

        val storage = RocketShipStorage(mutableMapOf(
            BlockPos(0, 0, 0) to Blocks.STONE.defaultBlockState()
        ))

        val contraption = RocketShipContraption()
        contraption.rebuildFromStorage(storage)

        contraption.anchor shouldBe BlockPos.ZERO
    }

    test("渲染系统集成 - Contraption 应该正确处理负坐标") {

        val storage = RocketShipStorage(mutableMapOf(
            BlockPos(-1, -1, -1) to Blocks.STONE.defaultBlockState(),
            BlockPos(1, 1, 1) to Blocks.DIRT.defaultBlockState()
        ))

        val contraption = RocketShipContraption()
        contraption.rebuildFromStorage(storage)

        contraption.blocks.size shouldBe 2
        contraption.blocks.keys.contains(BlockPos(-1, -1, -1)) shouldBe true
        contraption.blocks.keys.contains(BlockPos(1, 1, 1)) shouldBe true

        val bounds = contraption.bounds
        bounds.minX shouldBe -1.0
        bounds.minY shouldBe -1.0
        bounds.minZ shouldBe -1.0
        bounds.maxX shouldBe 2.0
        bounds.maxY shouldBe 2.0
        bounds.maxZ shouldBe 2.0
    }
})
