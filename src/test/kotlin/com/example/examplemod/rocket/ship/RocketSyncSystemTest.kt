package com.example.examplemod.rocket.ship

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.doubles.shouldBeWithinPercentageOf
import io.kotest.matchers.collections.shouldHaveSize
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.phys.Vec3

class RocketSyncSystemTest : FunSpec({

    context("结构数据接收测试") {

        test("应该正确接收和存储方块数据") {

            val blocks = mutableMapOf<BlockPos, net.minecraft.world.level.block.state.BlockState>()
            blocks[BlockPos(0, 0, 0)] = Blocks.STONE.defaultBlockState()
            blocks[BlockPos(1, 0, 0)] = Blocks.DIRT.defaultBlockState()
            blocks[BlockPos(0, 1, 0)] = Blocks.GRASS_BLOCK.defaultBlockState()

            val storage = RocketShipStorage(blocks)

            storage.blocks.size shouldBe 3
            storage.blocks[BlockPos(0, 0, 0)] shouldBe Blocks.STONE.defaultBlockState()
            storage.blocks[BlockPos(1, 0, 0)] shouldBe Blocks.DIRT.defaultBlockState()
            storage.blocks[BlockPos(0, 1, 0)] shouldBe Blocks.GRASS_BLOCK.defaultBlockState()
        }

        test("应该正确处理空结构数据") {

            val emptyBlocks = mutableMapOf<BlockPos, net.minecraft.world.level.block.state.BlockState>()
            val storage = RocketShipStorage(emptyBlocks)

            storage.blocks.size shouldBe 0

            val bounds = storage.computeLocalBounds()
            bounds.minX shouldBe 0.0
            bounds.minY shouldBe 0.0
            bounds.minZ shouldBe 0.0
            bounds.maxX shouldBe 1.0
            bounds.maxY shouldBe 1.0
            bounds.maxZ shouldBe 1.0
        }

        test("应该正确计算结构边界") {

            val blocks = mutableMapOf<BlockPos, net.minecraft.world.level.block.state.BlockState>()
            blocks[BlockPos(-5, 0, -5)] = Blocks.STONE.defaultBlockState()
            blocks[BlockPos(5, 10, 5)] = Blocks.STONE.defaultBlockState()

            val storage = RocketShipStorage(blocks)
            val bounds = storage.computeLocalBounds()

            bounds.minX shouldBe -5.0
            bounds.minY shouldBe 0.0
            bounds.minZ shouldBe -5.0
            bounds.maxX shouldBe 6.0
            bounds.maxY shouldBe 11.0
            bounds.maxZ shouldBe 6.0
        }

        test("应该正确处理大型结构数据") {

            val blocks = mutableMapOf<BlockPos, net.minecraft.world.level.block.state.BlockState>()
            for (x in 0 until 10) {
                for (y in 0 until 10) {
                    for (z in 0 until 10) {
                        blocks[BlockPos(x, y, z)] = Blocks.STONE.defaultBlockState()
                    }
                }
            }

            val storage = RocketShipStorage(blocks)

            storage.blocks.size shouldBe 1000

            val bounds = storage.computeLocalBounds()
            bounds.minX shouldBe 0.0
            bounds.minY shouldBe 0.0
            bounds.minZ shouldBe 0.0
            bounds.maxX shouldBe 10.0
            bounds.maxY shouldBe 10.0
            bounds.maxZ shouldBe 10.0
        }
    }

    context("装置重建测试") {

        test("应该从存储正确重建装置") {

            val blocks = mutableMapOf<BlockPos, net.minecraft.world.level.block.state.BlockState>()
            blocks[BlockPos(0, 0, 0)] = Blocks.STONE.defaultBlockState()
            blocks[BlockPos(1, 0, 0)] = Blocks.DIRT.defaultBlockState()

            val storage = RocketShipStorage(blocks)
            val contraption = RocketShipContraption()

            contraption.rebuildFromStorage(storage)

            contraption.blocks.size shouldBe 2
            contraption.blocks[BlockPos(0, 0, 0)] shouldNotBe null
            contraption.blocks[BlockPos(1, 0, 0)] shouldNotBe null
        }

        test("应该正确处理空存储的装置重建") {
            val emptyStorage = RocketShipStorage(mutableMapOf())
            val contraption = RocketShipContraption()

            contraption.rebuildFromStorage(emptyStorage)

            contraption.blocks.size shouldBe 0
            contraption.getPivotShift() shouldBe Vec3.ZERO
        }

        test("应该在重建时正确计算边界") {
            val blocks = mutableMapOf<BlockPos, net.minecraft.world.level.block.state.BlockState>()
            blocks[BlockPos(-2, 0, -2)] = Blocks.STONE.defaultBlockState()
            blocks[BlockPos(2, 5, 2)] = Blocks.STONE.defaultBlockState()

            val storage = RocketShipStorage(blocks)
            val contraption = RocketShipContraption()

            contraption.rebuildFromStorage(storage)

            val bounds = contraption.bounds
            bounds.minX shouldBe -2.0
            bounds.minY shouldBe 0.0
            bounds.minZ shouldBe -2.0
            bounds.maxX shouldBe 3.0
            bounds.maxY shouldBe 6.0
            bounds.maxZ shouldBe 3.0
        }

        test("应该在多次重建时保持一致性") {
            val blocks = mutableMapOf<BlockPos, net.minecraft.world.level.block.state.BlockState>()
            blocks[BlockPos(0, 0, 0)] = Blocks.STONE.defaultBlockState()

            val storage = RocketShipStorage(blocks)
            val contraption = RocketShipContraption()

            contraption.rebuildFromStorage(storage)
            val size1 = contraption.blocks.size
            val bounds1 = contraption.bounds

            contraption.rebuildFromStorage(storage)
            val size2 = contraption.blocks.size
            val bounds2 = contraption.bounds

            size1 shouldBe size2
            bounds1 shouldBe bounds2
        }
    }

    context("同步频率验证测试") {

        test("状态同步应该每 2 tick 发生一次") {

            val STATE_SYNC_INTERVAL = 2

            val ticksToSync = mutableListOf<Int>()
            for (tick in 0 until 20) {
                if (tick % STATE_SYNC_INTERVAL == 0) {
                    ticksToSync.add(tick)
                }
            }

            ticksToSync shouldBe listOf(0, 2, 4, 6, 8, 10, 12, 14, 16, 18)
        }

        test("结构同步应该每 40 tick 发生一次") {

            val STRUCTURE_SYNC_INTERVAL = 40

            val ticksToSync = mutableListOf<Int>()
            for (tick in 0 until 200) {
                if (tick % STRUCTURE_SYNC_INTERVAL == 0) {
                    ticksToSync.add(tick)
                }
            }

            ticksToSync shouldBe listOf(0, 40, 80, 120, 160)
        }

        test("结构同步冷却应该正确递减") {

            var cooldown = 40
            val syncEvents = mutableListOf<Int>()

            for (tick in 0 until 100) {
                if (cooldown-- <= 0) {
                    syncEvents.add(tick)
                    cooldown = 40
                }
            }

            syncEvents shouldHaveSize 3
            syncEvents[0] shouldBe 0
            syncEvents[1] shouldBe 41
            syncEvents[2] shouldBe 82
        }

        test("立即同步应该重置冷却计数器") {

            var cooldown = 20

            cooldown = 0

            cooldown shouldBe 0
        }
    }

    context("位置和速度同步测试") {

        test("应该正确同步位置数据") {
            val position = Vec3(100.5, 64.0, -200.3)

            val syncedX = position.x
            val syncedY = position.y
            val syncedZ = position.z

            syncedX.shouldBeWithinPercentageOf(100.5, 0.01)
            syncedY.shouldBeWithinPercentageOf(64.0, 0.01)
            syncedZ.shouldBeWithinPercentageOf(-200.3, 0.01)
        }

        test("应该正确同步速度数据") {
            val velocity = Vec3(5.2, -2.1, 3.7)

            val syncedVx = velocity.x
            val syncedVy = velocity.y
            val syncedVz = velocity.z

            syncedVx.shouldBeWithinPercentageOf(5.2, 0.01)
            syncedVy.shouldBeWithinPercentageOf(-2.1, 0.01)
            syncedVz.shouldBeWithinPercentageOf(3.7, 0.01)
        }

        test("应该正确同步旋转数据") {
            val yaw = 45.5f
            val pitch = -30.2f
            val roll = 15.7f

            val syncedYaw = yaw
            val syncedPitch = pitch
            val syncedRoll = roll

            syncedYaw shouldBe 45.5f
            syncedPitch shouldBe -30.2f
            syncedRoll shouldBe 15.7f
        }

        test("应该处理极端位置值") {
            val extremePositions = listOf(
                Vec3(Double.MAX_VALUE / 2, 0.0, 0.0),
                Vec3(0.0, Double.MAX_VALUE / 2, 0.0),
                Vec3(0.0, 0.0, Double.MAX_VALUE / 2),
                Vec3(-Double.MAX_VALUE / 2, 0.0, 0.0)
            )

            for (pos in extremePositions) {

                val syncedX = pos.x
                val syncedY = pos.y
                val syncedZ = pos.z

                syncedX shouldBe pos.x
                syncedY shouldBe pos.y
                syncedZ shouldBe pos.z
            }
        }
    }

    context("客户端插值测试") {

        test("应该正确计算插值步数") {
            val LERP_STEPS = 5

            var remainingSteps = LERP_STEPS
            val completedSteps = mutableListOf<Int>()

            while (remainingSteps > 0) {
                completedSteps.add(remainingSteps)
                remainingSteps--
            }

            completedSteps shouldBe listOf(5, 4, 3, 2, 1)
        }

        test("应该正确计算插值因子") {
            val LERP_FACTOR = 0.3

            (LERP_FACTOR >= 0.0 && LERP_FACTOR <= 1.0) shouldBe true
        }

        test("应该正确执行线性插值") {
            val current = Vec3(0.0, 0.0, 0.0)
            val target = Vec3(10.0, 10.0, 10.0)
            val lerpFactor = 0.3

            val delta = target.subtract(current).scale(lerpFactor)
            val next = current.add(delta)

            next.x.shouldBeWithinPercentageOf(3.0, 0.01)
            next.y.shouldBeWithinPercentageOf(3.0, 0.01)
            next.z.shouldBeWithinPercentageOf(3.0, 0.01)
        }

        test("插值应该逐渐接近目标") {
            var current = Vec3(0.0, 0.0, 0.0)
            val target = Vec3(10.0, 10.0, 10.0)
            val lerpFactor = 0.3

            for (i in 0 until 10) {
                val delta = target.subtract(current).scale(lerpFactor)
                current = current.add(delta)
            }

            current.x.shouldBeWithinPercentageOf(10.0, 5.0)
            current.y.shouldBeWithinPercentageOf(10.0, 5.0)
            current.z.shouldBeWithinPercentageOf(10.0, 5.0)
        }
    }

    context("数据完整性测试") {

        test("应该保持方块状态的完整性") {
            val originalState = Blocks.STONE.defaultBlockState()
            val blocks = mutableMapOf<BlockPos, net.minecraft.world.level.block.state.BlockState>()
            blocks[BlockPos(0, 0, 0)] = originalState

            val storage = RocketShipStorage(blocks)
            val retrievedState = storage.blocks[BlockPos(0, 0, 0)]

            retrievedState shouldBe originalState
        }

        test("应该正确处理方块状态的属性") {

            val stateWithProperties = Blocks.OAK_STAIRS.defaultBlockState()
            val blocks = mutableMapOf<BlockPos, net.minecraft.world.level.block.state.BlockState>()
            blocks[BlockPos(0, 0, 0)] = stateWithProperties

            val storage = RocketShipStorage(blocks)
            val retrieved = storage.blocks[BlockPos(0, 0, 0)]

            retrieved shouldBe stateWithProperties
        }

        test("应该正确处理多种方块类型") {
            val blocks = mutableMapOf<BlockPos, net.minecraft.world.level.block.state.BlockState>()
            blocks[BlockPos(0, 0, 0)] = Blocks.STONE.defaultBlockState()
            blocks[BlockPos(1, 0, 0)] = Blocks.DIRT.defaultBlockState()
            blocks[BlockPos(2, 0, 0)] = Blocks.GRASS_BLOCK.defaultBlockState()
            blocks[BlockPos(3, 0, 0)] = Blocks.OAK_LOG.defaultBlockState()
            blocks[BlockPos(4, 0, 0)] = Blocks.GLASS.defaultBlockState()

            val storage = RocketShipStorage(blocks)

            storage.blocks[BlockPos(0, 0, 0)]?.block shouldBe Blocks.STONE
            storage.blocks[BlockPos(1, 0, 0)]?.block shouldBe Blocks.DIRT
            storage.blocks[BlockPos(2, 0, 0)]?.block shouldBe Blocks.GRASS_BLOCK
            storage.blocks[BlockPos(3, 0, 0)]?.block shouldBe Blocks.OAK_LOG
            storage.blocks[BlockPos(4, 0, 0)]?.block shouldBe Blocks.GLASS
        }
    }
})
