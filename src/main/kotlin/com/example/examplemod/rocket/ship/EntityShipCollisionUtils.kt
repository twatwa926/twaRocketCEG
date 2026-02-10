package com.example.examplemod.rocket.ship

import net.minecraft.core.Direction
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import net.minecraft.world.phys.shapes.Shapes
import net.minecraft.world.phys.shapes.VoxelShape
import org.joml.Matrix4d
import org.joml.Vector3d

object EntityShipCollisionUtils {

    @JvmStatic
    fun isCollidingWithUnloadedShips(entity: Entity): Boolean {

        return false
    }

    @JvmStatic
    fun adjustEntityMovementForShipCollisions(
        entity: Entity?,
        movement: Vec3,
        entityBoundingBox: AABB,
        world: Level
    ): Vec3 {
        val inflation = if (entity is Player) 0.5 else 0.1
        val stepHeight = entity?.maxUpStep()?.toDouble() ?: 0.0
        val testBox = entityBoundingBox
            .inflate(inflation, inflation + stepHeight / 2.0, inflation)
            .expandTowards(movement)

        val ships = RocketShipGameUtils.getShipsIntersecting(testBox)
        if (ships.isEmpty()) {
            return movement
        }

        var adjusted = movement
        for (ship in ships) {
            adjusted = adjustMovementForShip(ship, entity, adjusted, entityBoundingBox)
        }
        return adjusted
    }

    private fun adjustMovementForShip(
        ship: RocketShipEntity,
        entity: Entity?,
        movement: Vec3,
        entityBoundingBox: AABB
    ): Vec3 {
        val shipWorld = ship.getShipWorld() ?: return movement
        val shipToWorld = ship.shipToWorldMatrix()
        val worldToShip = Matrix4d(shipToWorld).invert()

        val localBox = transformAabb(entityBoundingBox, worldToShip)
        val localMovement = transformVector(worldToShip, movement)
        val expanded = localBox.expandTowards(localMovement)
        val shapes = shipWorld.getBlockCollisions(entity, expanded).toList()
        if (shapes.isEmpty()) {
            return movement
        }

        var box = localBox
        val x = Shapes.collide(Direction.Axis.X, box, shapes, localMovement.x)
        box = box.move(x, 0.0, 0.0)
        val y = Shapes.collide(Direction.Axis.Y, box, shapes, localMovement.y)
        box = box.move(0.0, y, 0.0)
        val z = Shapes.collide(Direction.Axis.Z, box, shapes, localMovement.z)

        val adjustedLocal = Vec3(x, y, z)
        return transformVector(shipToWorld, adjustedLocal)
    }

    private fun transformAabb(aabb: AABB, transform: Matrix4d): AABB {
        var minX = Double.POSITIVE_INFINITY
        var minY = Double.POSITIVE_INFINITY
        var minZ = Double.POSITIVE_INFINITY
        var maxX = Double.NEGATIVE_INFINITY
        var maxY = Double.NEGATIVE_INFINITY
        var maxZ = Double.NEGATIVE_INFINITY
        val xs = doubleArrayOf(aabb.minX, aabb.maxX)
        val ys = doubleArrayOf(aabb.minY, aabb.maxY)
        val zs = doubleArrayOf(aabb.minZ, aabb.maxZ)
        for (x in xs) {
            for (y in ys) {
                for (z in zs) {
                    val pos = Vector3d(x, y, z)
                    transform.transformPosition(pos)
                    minX = minX.coerceAtMost(pos.x)
                    minY = minY.coerceAtMost(pos.y)
                    minZ = minZ.coerceAtMost(pos.z)
                    maxX = maxX.coerceAtLeast(pos.x)
                    maxY = maxY.coerceAtLeast(pos.y)
                    maxZ = maxZ.coerceAtLeast(pos.z)
                }
            }
        }
        return AABB(minX, minY, minZ, maxX, maxY, maxZ)
    }

    private fun transformVector(transform: Matrix4d, vec: Vec3): Vec3 {
        val origin = Vector3d(0.0, 0.0, 0.0)
        transform.transformPosition(origin)
        val end = Vector3d(vec.x, vec.y, vec.z)
        transform.transformPosition(end)
        return Vec3(end.x - origin.x, end.y - origin.y, end.z - origin.z)
    }
}
