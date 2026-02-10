package com.example.examplemod.rocket.ship

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.util.Mth
import net.minecraft.world.phys.Vec3

object RocketShipMath {
    @JvmStatic
    fun toWorld(local: Vec3, position: Vec3, yaw: Float, pitch: Float, roll: Float): Vec3 {
        return rotate(local, yaw, pitch, roll).add(position)
    }

    @JvmStatic
    fun toLocal(world: Vec3, position: Vec3, yaw: Float, pitch: Float, roll: Float): Vec3 {
        val shifted = world.subtract(position)
        return inverseRotate(shifted, yaw, pitch, roll)
    }

    @JvmStatic
    fun toLocal(world: BlockPos, position: Vec3, yaw: Float, pitch: Float, roll: Float): BlockPos {
        val local = toLocal(Vec3(world.x + 0.5, world.y + 0.5, world.z + 0.5), position, yaw, pitch, roll)
        return BlockPos(Mth.floor(local.x), Mth.floor(local.y), Mth.floor(local.z))
    }

    @JvmStatic
    fun rotate(vector: Vec3, yawDeg: Float, pitchDeg: Float, rollDeg: Float): Vec3 {
        var rotated = rotateAroundAxis(vector, yawDeg, Direction.Axis.Y)
        rotated = rotateAroundAxis(rotated, pitchDeg, Direction.Axis.X)
        return rotateAroundAxis(rotated, rollDeg, Direction.Axis.Z)
    }

    @JvmStatic
    fun inverseRotate(vector: Vec3, yawDeg: Float, pitchDeg: Float, rollDeg: Float): Vec3 {
        var rotated = rotateAroundAxis(vector, -rollDeg, Direction.Axis.Z)
        rotated = rotateAroundAxis(rotated, -pitchDeg, Direction.Axis.X)
        return rotateAroundAxis(rotated, -yawDeg, Direction.Axis.Y)
    }

    @JvmStatic
    fun rotateAroundAxis(vector: Vec3, degrees: Float, axis: Direction.Axis): Vec3 {
        if (degrees == 0.0f) {
            return vector
        }
        val rad = degrees * Mth.DEG_TO_RAD
        val cos = Mth.cos(rad)
        val sin = Mth.sin(rad)
        return when (axis) {
            Direction.Axis.X -> Vec3(vector.x, vector.y * cos - vector.z * sin, vector.y * sin + vector.z * cos)
            Direction.Axis.Y -> Vec3(vector.x * cos + vector.z * sin, vector.y, -vector.x * sin + vector.z * cos)
            Direction.Axis.Z -> Vec3(vector.x * cos - vector.y * sin, vector.x * sin + vector.y * cos, vector.z)
        }
    }
}
