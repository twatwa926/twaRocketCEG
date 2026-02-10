package com.example.examplemod.rocket.core

import net.minecraft.world.phys.Vec3
import org.joml.Matrix4d
import org.joml.Quaterniond

object RocketWACore {
    private val physicsWorld = PhysicsWorld()

    fun createShip(shipId: Long, mass: Double, inertia: Vec3, centerOfMass: Vec3): PhysicsShipHandle {
        return physicsWorld.createShip(shipId, mass, inertia, centerOfMass)
    }

    fun removeShip(shipId: Long) {
        physicsWorld.removeShip(shipId)
    }

    fun getShip(shipId: Long): PhysicsShipHandle? {
        return physicsWorld.getShip(shipId)
    }

    fun tick(deltaTime: Double) {
        physicsWorld.tick(deltaTime)
    }

    fun shutdown() {
    }
}

class PhysicsShipHandle internal constructor(
    val shipId: Long,
    internal val physicsBody: PhysicsRigidBody
) {
    fun getPosition(): Vec3 = physicsBody.position

    fun getVelocity(): Vec3 = physicsBody.velocity

    fun getRotation(): Quaterniond = physicsBody.rotation

    fun setPosition(pos: Vec3) {
        physicsBody.position = pos
    }

    fun setVelocity(vel: Vec3) {
        physicsBody.velocity = vel
    }

    fun setRotation(rot: Quaterniond) {
        physicsBody.rotation = rot
    }

    fun applyForce(force: Vec3) {
        physicsBody.applyForce(force)
    }

    fun applyTorque(torque: Vec3) {
        physicsBody.applyTorque(torque)
    }

    fun getTransformMatrix(): Matrix4d {
        return physicsBody.getTransformMatrix()
    }
}

internal class PhysicsWorld {
    private val ships = mutableMapOf<Long, PhysicsShipHandle>()

    fun createShip(shipId: Long, mass: Double, inertia: Vec3, centerOfMass: Vec3): PhysicsShipHandle {
        val body = PhysicsRigidBody(mass, inertia, centerOfMass)
        val handle = PhysicsShipHandle(shipId, body)
        ships[shipId] = handle
        return handle
    }

    fun removeShip(shipId: Long) {
        ships.remove(shipId)
    }

    fun getShip(shipId: Long): PhysicsShipHandle? {
        return ships[shipId]
    }

    fun tick(deltaTime: Double) {
        for (ship in ships.values) {
            ship.physicsBody.integrate(deltaTime)
        }
    }
}

internal class PhysicsRigidBody(
    private val mass: Double,
    private val inertia: Vec3,
    private val centerOfMass: Vec3
) {
    var position: Vec3 = Vec3.ZERO
    var velocity: Vec3 = Vec3.ZERO
    var rotation: Quaterniond = Quaterniond()
    var angularVelocity: Vec3 = Vec3.ZERO

    private var accumulatedForce: Vec3 = Vec3.ZERO
    private var accumulatedTorque: Vec3 = Vec3.ZERO

    fun applyForce(force: Vec3) {
        accumulatedForce = accumulatedForce.add(force)
    }

    fun applyTorque(torque: Vec3) {
        accumulatedTorque = accumulatedTorque.add(torque)
    }

    fun integrate(deltaTime: Double) {
        val subSteps = 4
        val subDt = deltaTime / subSteps

        for (i in 0 until subSteps) {
            val acceleration = accumulatedForce.scale(1.0 / mass)
            velocity = velocity.add(acceleration.scale(subDt))
            position = position.add(velocity.scale(subDt))

            val angularAcceleration = Vec3(
                accumulatedTorque.x / inertia.x,
                accumulatedTorque.y / inertia.y,
                accumulatedTorque.z / inertia.z
            )
            angularVelocity = angularVelocity.add(angularAcceleration.scale(subDt))

            val angularVelQuat = Quaterniond(
                angularVelocity.x * subDt * 0.5,
                angularVelocity.y * subDt * 0.5,
                angularVelocity.z * subDt * 0.5,
                0.0
            )
            rotation = rotation.add(angularVelQuat.mul(rotation)).normalize()
        }

        accumulatedForce = Vec3.ZERO
        accumulatedTorque = Vec3.ZERO
    }

    fun getTransformMatrix(): Matrix4d {
        val matrix = Matrix4d()
        matrix.translate(position.x, position.y, position.z)
        matrix.rotate(rotation)
        return matrix
    }
}
