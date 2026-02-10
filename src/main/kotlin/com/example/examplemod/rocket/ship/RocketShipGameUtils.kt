package com.example.examplemod.rocket.ship

import net.minecraft.core.BlockPos
import net.minecraft.world.level.Level
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3

object RocketShipGameUtils {

    private val LEVEL_BOUNDS = AABB(-3e7, -256.0, -3e7, 3e7, 256.0, 3e7)

    @JvmStatic
    fun getShipsIntersecting(aabb: AABB, serverOnly: Boolean = false): List<RocketShipEntity> {
        val ships = ArrayList<RocketShipEntity>()
        for (ship in RocketShipRegistry.getAll()) {
            if (!ship.isAlive) continue
            if (serverOnly && ship.level().isClientSide) continue
            if (ship.boundingBox.intersects(aabb)) ships.add(ship)
        }
        return ships
    }

    @JvmStatic
    fun getShipsIntersecting(level: Level, aabb: AABB, serverOnly: Boolean = false): List<RocketShipEntity> {
        val ships = ArrayList<RocketShipEntity>()
        val searchBox = aabb.inflate(32.0)
        for (ship in level.getEntitiesOfClass(RocketShipEntity::class.java, searchBox)) {
            if (!ship.isAlive) continue
            if (serverOnly && level.isClientSide) continue
            if (ship.boundingBox.intersects(aabb)) ships.add(ship)
        }
        return ships
    }

    @JvmStatic
    fun getShipsInLevel(level: Level, serverOnly: Boolean = false): List<RocketShipEntity> {
        val ships = ArrayList<RocketShipEntity>()
        for (ship in level.getEntitiesOfClass(RocketShipEntity::class.java, LEVEL_BOUNDS)) {
            if (!ship.isAlive) continue
            if (ship.level() != level) continue
            if (serverOnly && level.isClientSide) continue
            ships.add(ship)
        }
        return ships
    }

    @JvmStatic
    fun getShipManagingPos(level: Level, pos: Vec3): RocketShipEntity? {
        for (ship in level.getEntitiesOfClass(RocketShipEntity::class.java, LEVEL_BOUNDS)) {
            if (!ship.isAlive) continue
            if (ship.level() != level) continue
            if (ship.boundingBox.contains(pos)) return ship
        }
        return null
    }

    @JvmStatic
    fun getShipManagingPos(level: Level, pos: BlockPos): RocketShipEntity? {
        return getShipManagingPos(level, Vec3(pos.x + 0.5, pos.y + 0.5, pos.z + 0.5))
    }
}
