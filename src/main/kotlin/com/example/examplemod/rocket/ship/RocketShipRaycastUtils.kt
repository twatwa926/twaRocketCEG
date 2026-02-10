package com.example.examplemod.rocket.ship

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.ClipContext
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.material.FluidState
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.HitResult
import net.minecraft.world.phys.Vec3

sealed class UnifiedHit {
    data class World(val hit: BlockHitResult) : UnifiedHit()
    data class Ship(val ship: RocketShipEntity, val localHit: BlockHitResult) : UnifiedHit()
}

object RocketShipRaycastUtils {
    @JvmStatic
    fun unifiedClip(level: Level, ctx: ClipContext, serverOnly: Boolean = false): UnifiedHit {
        val vanillaHit = vanillaClip(level, ctx)
        var closestDist = vanillaHit.location.distanceToSqr(ctx.from)
        var bestShipHit: UnifiedHit.Ship? = null

        for (ship in RocketShipGameUtils.getShipsInLevel(level, serverOnly)) {
            val shipWorld = ship.getShipWorld() ?: continue
            if (shipWorld.getStorageBlocks().isEmpty()) continue
            val shipStart = ship.worldToLocal(ctx.from)
            val shipEnd = ship.worldToLocal(ctx.to)
            val shipHit = RocketShipRaycast.raycast(shipWorld, shipStart, shipEnd)
            if (shipHit.type == HitResult.Type.MISS) continue
            val worldHitPos = ship.localToWorld(shipHit.location)
            val dist = worldHitPos.distanceToSqr(ctx.from)
            if (dist < closestDist) {
                closestDist = dist
                bestShipHit = UnifiedHit.Ship(ship, shipHit)
            }
        }
        return bestShipHit ?: UnifiedHit.World(vanillaHit)
    }

    @JvmStatic
    fun clipIncludeShips(level: Level, ctx: ClipContext): BlockHitResult {
        return when (val u = unifiedClip(level, ctx, false)) {
            is UnifiedHit.World -> u.hit
            is UnifiedHit.Ship -> BlockHitResult(
                u.ship.localToWorld(u.localHit.location),
                u.localHit.direction,
                u.localHit.blockPos,
                u.localHit.isInside
            )
        }
    }

    private fun vanillaClip(level: Level, ctx: ClipContext): BlockHitResult {
        return clipInternal(level, ctx.from, ctx.to, ctx)
    }

    private fun clip(level: Level, realStart: Vec3, realEnd: Vec3, ctx: ClipContext): BlockHitResult {
        return clipInternal(level, realStart, realEnd, ctx)
    }

    private fun clipInternal(level: Level, realStart: Vec3, realEnd: Vec3, ctx: ClipContext): BlockHitResult {
        return clipTraverse(realStart, realEnd, ctx, { raycastContext, blockPos ->
            val blockState: BlockState = level.getBlockState(blockPos!!)
            val fluidState: FluidState = level.getFluidState(blockPos)
            val voxelShape = raycastContext.getBlockShape(blockState, level, blockPos)
            val blockHitResult: BlockHitResult? = voxelShape.clip(realStart, realEnd, blockPos)
            val fluidShape = raycastContext.getFluidShape(fluidState, level, blockPos)
            val fluidHitResult: BlockHitResult? = fluidShape.clip(realStart, realEnd, blockPos)
            val d = blockHitResult?.let { realStart.distanceToSqr(it.location) } ?: Double.MAX_VALUE
            val e = fluidHitResult?.let { realStart.distanceToSqr(it.location) } ?: Double.MAX_VALUE
            if (d <= e) blockHitResult else fluidHitResult
        }) { context ->
            val vec3 = realStart.subtract(realEnd)
            BlockHitResult.miss(realEnd, Direction.getNearest(vec3.x, vec3.y, vec3.z), BlockPos.containing(realEnd))
        } as BlockHitResult
    }

    private fun <T> clipTraverse(
        realStart: Vec3,
        realEnd: Vec3,
        raycastContext: ClipContext,
        context: java.util.function.BiFunction<ClipContext, BlockPos?, T>,
        blockRaycaster: java.util.function.Function<ClipContext, T>
    ): T {
        return BlockGetter.traverseBlocks(realStart, realEnd, raycastContext, context, blockRaycaster)
    }
}
