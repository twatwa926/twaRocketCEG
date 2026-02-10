package com.example.examplemod.rocket.ship

import net.minecraft.world.phys.Vec3
import kotlin.math.max
import kotlin.math.sqrt

class RocketShipPhysicsServer(
    private var state: RocketShipPhysicsState = RocketShipPhysicsState()
) {
    private val subSteps = 4

    fun reset(position: Vec3) {
        state.position = position
        state.velocity = Vec3.ZERO
        state.angularVelocity = Vec3.ZERO
    }

    fun updateOrientation(yaw: Float, pitch: Float, roll: Float) {
        state.yaw = yaw
        state.pitch = pitch
        state.roll = roll
    }

    fun tick(accel: Vec3, dt: Double) {
        val subDt = dt / subSteps
        for (i in 0 until subSteps) {
            state.velocity = state.velocity.add(accel.scale(subDt))
            state.position = state.position.add(state.velocity.scale(subDt))
        }
    }

    fun getState(): RocketShipPhysicsState = state

    fun setState(newState: RocketShipPhysicsState) {
        state = newState
    }

    private fun Vec3.length(): Double = sqrt(x * x + y * y + z * z)
}

data class RocketShipPhysicsState(
    var position: Vec3 = Vec3.ZERO,
    var velocity: Vec3 = Vec3.ZERO,
    var yaw: Float = 0.0f,
    var pitch: Float = 0.0f,
    var roll: Float = 0.0f,
    var angularVelocity: Vec3 = Vec3.ZERO
)
