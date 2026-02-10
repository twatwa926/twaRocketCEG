package com.example.examplemod.rocket.ship

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.HitResult
import net.minecraft.world.phys.Vec3

object RocketShipRaycast {
    @JvmStatic
    fun raycast(world: RocketShipWorld?, start: Vec3, end: Vec3): BlockHitResult {
        if (world == null) {
            return miss(start, end)
        }
        var closest: BlockHitResult? = null
        var closestDist = Double.MAX_VALUE
        val blocks = world.getStorageBlocks()
        for ((pos, state) in blocks) {
            val shape = state.getShape(world, pos)
            if (shape.isEmpty) {
                continue
            }
            val hit = shape.clip(start, end, pos)
            if (hit != null && hit.type != HitResult.Type.MISS) {
                val dist = hit.location.distanceToSqr(start)
                if (dist < closestDist) {
                    closest = hit
                    closestDist = dist
                }
            }
        }
        return closest ?: miss(start, end)
    }

    private fun miss(start: Vec3, end: Vec3): BlockHitResult {
        val vec = start.subtract(end)
        return BlockHitResult.miss(end, Direction.getNearest(vec.x, vec.y, vec.z), BlockPos.containing(end))
    }
}
