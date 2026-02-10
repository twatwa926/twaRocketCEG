package com.example.examplemod.rocket.ship

import com.example.examplemod.rocket.RocketDimensions
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.block.state.BlockState

class RocketShipChunkManager(
    private val shipId: Long,
    private val buildOrigin: BlockPos
) {
    private val ownedChunks = mutableSetOf<ChunkPos>()

    fun placeBlock(
        buildLevel: ServerLevel,
        worldPos: BlockPos,
        state: BlockState
    ): Boolean {
        val chunkPos = ChunkPos(worldPos)

        if (!ownedChunks.contains(chunkPos)) {
            ownedChunks.add(chunkPos)
            ensureChunkLoaded(buildLevel, chunkPos)
        }

        return try {
            buildLevel.setBlockAndUpdate(worldPos, state)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun removeBlock(
        buildLevel: ServerLevel,
        worldPos: BlockPos
    ): Boolean {
        return try {
            buildLevel.removeBlock(worldPos, false)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun getBlock(
        buildLevel: ServerLevel,
        localPos: BlockPos
    ): BlockState {
        val worldPos = buildOrigin.offset(localPos)
        return buildLevel.getBlockState(worldPos)
    }

    private fun ensureChunkLoaded(level: ServerLevel, chunkPos: ChunkPos) {
        if (!level.hasChunk(chunkPos.x, chunkPos.z)) {
            level.getChunk(chunkPos.x, chunkPos.z)
        }
    }

    fun getOwnedChunks(): Set<ChunkPos> = ownedChunks.toSet()

    fun getChunkCount(): Int = ownedChunks.size

    fun cleanup(buildLevel: ServerLevel) {
        for (chunkPos in ownedChunks) {
            if (buildLevel.hasChunk(chunkPos.x, chunkPos.z)) {
                for (x in 0..15) {
                    for (z in 0..15) {
                        for (y in buildLevel.minBuildHeight until buildLevel.maxBuildHeight) {
                            val pos = BlockPos(
                                chunkPos.minBlockX + x,
                                y,
                                chunkPos.minBlockZ + z
                            )
                            buildLevel.removeBlock(pos, false)
                        }
                    }
                }
            }
        }
        ownedChunks.clear()
    }
}
