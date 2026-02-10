package com.example.examplemod.rocket.ship

import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.Vec3
import org.joml.Matrix4d
import org.joml.Vector3d
import kotlin.math.max

class RocketShipMassProperties(
    val dryMass: Double,
    val inertia: Vec3,
    val centerOfMass: Vec3
)

object RocketShipPhysics {
    @JvmStatic
    fun computeMassProperties(storage: RocketShipStorage): RocketShipMassProperties {
        val blocks = storage.blocks
        if (blocks.isEmpty()) {
            return RocketShipMassProperties(1.0, Vec3(1.0, 1.0, 1.0), Vec3(0.5, 0.5, 0.5))
        }

        var totalMass = 0.0
        var sumX = 0.0
        var sumY = 0.0
        var sumZ = 0.0
        for ((pos, state) in blocks) {
            val mass = blockMass(state)
            val cx = pos.x + 0.5
            val cy = pos.y + 0.5
            val cz = pos.z + 0.5
            totalMass += mass
            sumX += mass * cx
            sumY += mass * cy
            sumZ += mass * cz
        }

        if (totalMass <= 1.0e-6) {
            return RocketShipMassProperties(1.0, Vec3(1.0, 1.0, 1.0), Vec3(0.5, 0.5, 0.5))
        }

        val centerOfMass = Vec3(sumX / totalMass, sumY / totalMass, sumZ / totalMass)
        var inertiaX = 0.0
        var inertiaY = 0.0
        var inertiaZ = 0.0
        for ((pos, state) in blocks) {
            val mass = blockMass(state)
            val dx = pos.x + 0.5 - centerOfMass.x
            val dy = pos.y + 0.5 - centerOfMass.y
            val dz = pos.z + 0.5 - centerOfMass.z
            inertiaX += mass * (dy * dy + dz * dz)
            inertiaY += mass * (dx * dx + dz * dz)
            inertiaZ += mass * (dx * dx + dy * dy)
        }

        val inertia = Vec3(max(1.0, inertiaX), max(1.0, inertiaY), max(1.0, inertiaZ))
        return RocketShipMassProperties(totalMass, inertia, centerOfMass)
    }

    @JvmStatic
    fun shipToWorldMatrix(anchor: Vec3, yaw: Float, pitch: Float, roll: Float, centerOfMass: Vec3): Matrix4d {
        return Matrix4d()
            .translate(anchor.x, anchor.y, anchor.z)
            .translate(centerOfMass.x, centerOfMass.y, centerOfMass.z)
            .rotateY(Math.toRadians(yaw.toDouble()))
            .rotateX(Math.toRadians(pitch.toDouble()))
            .rotateZ(Math.toRadians(roll.toDouble()))
            .translate(-centerOfMass.x, -centerOfMass.y, -centerOfMass.z)
    }

    @JvmStatic
    fun localToWorld(local: Vec3, anchor: Vec3, yaw: Float, pitch: Float, roll: Float, centerOfMass: Vec3): Vec3 {
        val world = Vector3d(local.x, local.y, local.z)
        shipToWorldMatrix(anchor, yaw, pitch, roll, centerOfMass).transformPosition(world)
        return Vec3(world.x, world.y, world.z)
    }

    @JvmStatic
    fun worldToLocal(world: Vec3, anchor: Vec3, yaw: Float, pitch: Float, roll: Float, centerOfMass: Vec3): Vec3 {
        val local = Vector3d(world.x, world.y, world.z)
        shipToWorldMatrix(anchor, yaw, pitch, roll, centerOfMass).invert().transformPosition(local)
        return Vec3(local.x, local.y, local.z)
    }

    private fun blockMass(state: BlockState): Double {
        val resistance = state.block.explosionResistance.toDouble()
        return max(0.25, 1.0 + resistance * 0.02)
    }
}
