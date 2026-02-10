package com.example.examplemod.rocket.ship

import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.AABB
import java.util.concurrent.ConcurrentHashMap

class RocketShipStorage(

    val blocks: MutableMap<BlockPos, BlockState>
) {
    companion object {
        @JvmStatic
        fun concurrentCopyOf(initial: Map<BlockPos, BlockState>): RocketShipStorage {
            return RocketShipStorage(ConcurrentHashMap(initial))
        }

        @JvmStatic
        fun empty(): RocketShipStorage = RocketShipStorage(ConcurrentHashMap())
    }

    fun computeLocalBounds(): AABB {

        val entries = blocks.entries.toList()

        if (entries.isEmpty()) {
            return AABB(0.0, 0.0, 0.0, 1.0, 1.0, 1.0)
        }
        var minX = Double.POSITIVE_INFINITY
        var minY = Double.POSITIVE_INFINITY
        var minZ = Double.POSITIVE_INFINITY
        var maxX = Double.NEGATIVE_INFINITY
        var maxY = Double.NEGATIVE_INFINITY
        var maxZ = Double.NEGATIVE_INFINITY
        for ((pos, _) in entries) {
            val x0 = pos.x.toDouble()
            val y0 = pos.y.toDouble()
            val z0 = pos.z.toDouble()
            val x1 = x0 + 1.0
            val y1 = y0 + 1.0
            val z1 = z0 + 1.0
            minX = minX.coerceAtMost(x0)
            minY = minY.coerceAtMost(y0)
            minZ = minZ.coerceAtMost(z0)
            maxX = maxX.coerceAtLeast(x1)
            maxY = maxY.coerceAtLeast(y1)
            maxZ = maxZ.coerceAtLeast(z1)
        }
        return AABB(minX, minY, minZ, maxX, maxY, maxZ)
    }

    fun centerOfMass(): net.minecraft.world.phys.Vec3 {
        return RocketShipPhysics.computeMassProperties(this).centerOfMass
    }
}
