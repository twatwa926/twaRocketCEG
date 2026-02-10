package com.example.examplemod.rocket.ship

import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundSource
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.Vec3
import com.example.examplemod.rocket.RocketDimensions
import net.minecraft.world.level.block.Blocks
import java.util.*

const val MAX_SHIP_BLOCKS = 65536

class RocketBlockModificationHandler(private val entity: RocketShipEntity) {

    fun placeBlock(localPos: BlockPos, state: BlockState, player: Player): Boolean {
        if (!canPlaceBlock(localPos, state)) return false

        val storage = entity.getStorageBlocks() as? MutableMap<BlockPos, BlockState>
        if (storage == null) return false

        storage[localPos] = state
        entity.rebuildContraption()

        updatePhysicsProperties()
        playPlaceEffects(localPos, state, player)
        entity.syncStructureNow()
        updateBuildWorld(localPos, state)
        return true
    }

    fun breakBlock(localPos: BlockPos, player: Player): Boolean {
        val storage = entity.getStorageBlocks() as? MutableMap<BlockPos, BlockState>
        if (storage == null) return false

        val state = storage[localPos] ?: return false
        if (wouldDisconnectStructure(localPos, storage)) return false

        storage.remove(localPos)
        entity.rebuildContraption()
        updatePhysicsProperties()
        dropBlockItems(localPos, state, player)
        playBreakEffects(localPos, state, player)
        entity.syncStructureNow()
        return true
    }

    private fun canPlaceBlock(localPos: BlockPos, state: BlockState): Boolean {
        val storage = entity.getStorageBlocks()
        if (storage.containsKey(localPos)) return false
        if (state.isAir) return false
        if (storage.size >= MAX_SHIP_BLOCKS) return false
        if (storage.isNotEmpty() && !isAdjacentToExistingBlock(localPos, storage)) return false
        return true
    }

    private fun wouldDisconnectStructure(pos: BlockPos, storage: Map<BlockPos, BlockState>): Boolean {
        if (storage.size <= 1) return true
        val tempBlocks = storage.toMutableMap()
        tempBlocks.remove(pos)
        return !isStructureConnected(tempBlocks)
    }

    private fun isStructureConnected(blocks: Map<BlockPos, BlockState>): Boolean {
        if (blocks.isEmpty()) return true
        val start = blocks.keys.first()
        val visited = mutableSetOf<BlockPos>()
        val queue: Queue<BlockPos> = LinkedList()

        queue.add(start)
        visited.add(start)

        while (queue.isNotEmpty()) {
            val current = queue.poll()
            for (direction in DIRECTIONS) {
                val neighbor = current.offset(direction)

                if (blocks.containsKey(neighbor) && !visited.contains(neighbor)) {
                    visited.add(neighbor)
                    queue.add(neighbor)
                }
            }
        }
        return visited.size == blocks.size
    }

    private fun isAdjacentToExistingBlock(pos: BlockPos, storage: Map<BlockPos, BlockState>): Boolean {
        for (direction in DIRECTIONS) {
            val neighbor = pos.offset(direction)
            if (storage.containsKey(neighbor)) {
                return true
            }
        }
        return false
    }

    private fun updatePhysicsProperties() {
        val storage = RocketShipStorage.concurrentCopyOf(entity.getStorageBlocks())
        val massProps = RocketShipPhysics.computeMassProperties(storage)
    }

    private fun playPlaceEffects(localPos: BlockPos, state: BlockState, player: Player) {
        val level = entity.level()
        if (level.isClientSide) return
        val worldPos = localToWorld(localPos)
        val soundType = state.getSoundType(level, localPos, player)
        level.playSound(
            null,
            worldPos.x, worldPos.y, worldPos.z,
            soundType.placeSound,
            SoundSource.BLOCKS,
            (soundType.volume + 1.0f) / 2.0f,
            soundType.pitch * 0.8f
        )
    }

    private fun playBreakEffects(localPos: BlockPos, state: BlockState, player: Player) {
        val level = entity.level()
        if (level.isClientSide) return
        val worldPos = localToWorld(localPos)
        val soundType = state.getSoundType(level, localPos, player)
        level.playSound(
            null,
            worldPos.x, worldPos.y, worldPos.z,
            soundType.breakSound,
            SoundSource.BLOCKS,
            (soundType.volume + 1.0f) / 2.0f,
            soundType.pitch * 0.8f
        )
    }

    private fun dropBlockItems(localPos: BlockPos, state: BlockState, player: Player) {
        val level = entity.level()
        if (level.isClientSide || level !is ServerLevel) return
        if (player.isCreative) return
        val worldPos = localToWorld(localPos)
        val worldBlockPos = BlockPos.containing(worldPos.x, worldPos.y, worldPos.z)
        val drops = Block.getDrops(
            state,
            level,
            worldBlockPos,
            null,
            player,
            player.mainHandItem
        )
        for (drop in drops) {
            if (!player.inventory.add(drop)) player.drop(drop, false)
        }
    }

    private fun localToWorld(localPos: BlockPos): Vec3 {
        val localCenter = Vec3(localPos.x + 0.5, localPos.y + 0.5, localPos.z + 0.5)
        return entity.toGlobalVector(localCenter, 1.0f)
    }

    private fun updateBuildWorld(localPos: BlockPos, state: BlockState) {
        val level = entity.level()
        if (level.isClientSide) return
        val server = (level as? ServerLevel)?.server ?: return
        val buildLevel = RocketDimensions.getBuildLevel(server) ?: return
        val worldPos = entity.getBuildOrigin().offset(localPos)
        buildLevel.setBlockAndUpdate(worldPos, state)
    }

    companion object {
        private val DIRECTIONS = arrayOf(
            BlockPos(0, 1, 0),
            BlockPos(0, -1, 0),
            BlockPos(1, 0, 0),
            BlockPos(-1, 0, 0),
            BlockPos(0, 0, 1),
            BlockPos(0, 0, -1)
        )
    }
}
