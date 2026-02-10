package com.example.examplemod.rocket.ship

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.BlockAndTintGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.lighting.LevelLightEngine
import net.minecraft.world.level.material.FluidState
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.shapes.VoxelShape
import net.minecraft.world.level.ColorResolver

class RocketShipWorld(
    private val level: Level,
    private val storage: RocketShipStorage,
    private val owner: RocketShipEntity
) : BlockAndTintGetter {

    fun getLevel(): Level = level

    fun getStorageBlocks(): Map<BlockPos, BlockState> = storage.blocks

    fun getOwner(): RocketShipEntity = owner

    override fun getBlockState(localPos: BlockPos): BlockState {
        return storage.blocks[localPos] ?: Blocks.AIR.defaultBlockState()
    }

    override fun getFluidState(localPos: BlockPos): FluidState =
        getBlockState(localPos).fluidState

    override fun getBlockEntity(pos: BlockPos) = null

    override fun getHeight() = level.height

    override fun getMinBuildHeight() = level.minBuildHeight

    override fun getLightEngine(): LevelLightEngine = level.lightEngine

    override fun getShade(direction: Direction, shade: Boolean): Float =
        level.getShade(direction, shade)

    override fun getBlockTint(pos: BlockPos, colorResolver: ColorResolver): Int =
        level.getBlockTint(pos, colorResolver)

    fun getBlockCollisions(entity: Entity?, aabb: AABB): Iterable<VoxelShape> {
        if (storage.blocks.isEmpty()) {
            return emptyList()
        }
        val shapes = ArrayList<VoxelShape>()
        val minX = kotlin.math.floor(aabb.minX).toInt()
        val minY = kotlin.math.floor(aabb.minY).toInt()
        val minZ = kotlin.math.floor(aabb.minZ).toInt()
        val maxX = kotlin.math.floor(aabb.maxX).toInt()
        val maxY = kotlin.math.floor(aabb.maxY).toInt()
        val maxZ = kotlin.math.floor(aabb.maxZ).toInt()
        for ((pos, state) in storage.blocks) {
            if (pos.x < minX || pos.y < minY || pos.z < minZ || pos.x > maxX || pos.y > maxY || pos.z > maxZ) {
                continue
            }
            val shape = state.getCollisionShape(level, BlockPos.ZERO)
            if (!shape.isEmpty) {
                shapes.add(shape.move(pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble()))
            }
        }
        return shapes
    }
}
