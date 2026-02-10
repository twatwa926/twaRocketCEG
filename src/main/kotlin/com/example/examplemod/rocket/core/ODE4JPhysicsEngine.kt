package com.example.examplemod.rocket.core

import net.minecraft.world.phys.Vec3
import org.joml.Quaterniond
import org.ode4j.math.DQuaternion
import org.ode4j.math.DVector3
import org.ode4j.ode.*
import org.ode4j.ode.OdeHelper.*

class ODE4JPhysicsEngine {
    private val world: DWorld = createWorld()
    private val space: DSpace = createHashSpace()
    private val contactGroup: DJointGroup = createJointGroup()

    private val rigidBodies = mutableMapOf<Long, DBody>()
    private val geometries = mutableMapOf<Long, DGeom>()

    init {
        world.setGravity(0.0, 0.0, 0.0)
        world.setCFM(1e-5)
        world.setERP(0.2)
        world.setQuickStepNumIterations(20)
        world.setContactMaxCorrectingVel(0.1)
        world.setContactSurfaceLayer(0.001)
    }

    fun createRigidBody(
        shipId: Long,
        mass: Double,
        inertia: Vec3,
        size: Vec3,
        position: Vec3,
        rotation: Quaterniond
    ): Long {
        val body = createBody(world)

        val massObj = createMass()
        massObj.setBoxTotal(
            mass,
            size.x,
            size.y,
            size.z
        )
        body.mass = massObj

        body.setPosition(position.x, position.y, position.z)
        body.setQuaternion(
            DQuaternion(
                rotation.w,
                rotation.x,
                rotation.y,
                rotation.z
            )
        )

        val geom = createBox(space, size.x, size.y, size.z)
        geom.body = body

        body.setLinearDamping(0.01)
        body.setAngularDamping(0.01)

        rigidBodies[shipId] = body
        geometries[shipId] = geom

        return shipId
    }

    fun removeRigidBody(shipId: Long) {
        geometries.remove(shipId)?.destroy()
        rigidBodies.remove(shipId)?.destroy()
    }

    fun applyForce(shipId: Long, force: Vec3, relativePos: Vec3 = Vec3.ZERO) {
        val body = rigidBodies[shipId] ?: return

        if (relativePos == Vec3.ZERO) {
            body.addForce(force.x, force.y, force.z)
        } else {
            body.addForceAtRelPos(
                force.x, force.y, force.z,
                relativePos.x, relativePos.y, relativePos.z
            )
        }
    }

    fun applyTorque(shipId: Long, torque: Vec3) {
        val body = rigidBodies[shipId] ?: return
        body.addTorque(torque.x, torque.y, torque.z)
    }

    fun getPosition(shipId: Long): Vec3? {
        val body = rigidBodies[shipId] ?: return null
        val pos = body.position
        return Vec3(pos.get0(), pos.get1(), pos.get2())
    }

    fun getVelocity(shipId: Long): Vec3? {
        val body = rigidBodies[shipId] ?: return null
        val vel = body.linearVel
        return Vec3(vel.get0(), vel.get1(), vel.get2())
    }

    fun getRotation(shipId: Long): Quaterniond? {
        val body = rigidBodies[shipId] ?: return null
        val quat = body.quaternion
        return Quaterniond(quat.get0(), quat.get1(), quat.get2(), quat.get3())
    }

    fun setPosition(shipId: Long, position: Vec3) {
        val body = rigidBodies[shipId] ?: return
        body.setPosition(position.x, position.y, position.z)
    }

    fun setVelocity(shipId: Long, velocity: Vec3) {
        val body = rigidBodies[shipId] ?: return
        body.setLinearVel(velocity.x, velocity.y, velocity.z)
    }

    fun setRotation(shipId: Long, rotation: Quaterniond) {
        val body = rigidBodies[shipId] ?: return
        body.setQuaternion(
            DQuaternion(
                rotation.w,
                rotation.x,
                rotation.y,
                rotation.z
            )
        )
    }

    fun step(deltaTime: Double) {
        space.collide(null) { _, geom1, geom2 ->
            val body1 = geom1.body
            val body2 = geom2.body

            if (body1 != null && body2 != null) {
                if (body1.isConnectedTo(body2)) {
                    return@collide
                }
            }

            val maxContacts = 10
            val contactArray = DContactBuffer(maxContacts)
            val numContacts = collide(geom1, geom2, maxContacts, contactArray.geomBuffer)

            for (i in 0 until numContacts) {
                val contact = contactArray.get(i)
                contact.surface.mode = OdeConstants.dContactBounce or OdeConstants.dContactSoftCFM
                contact.surface.mu = 0.5
                contact.surface.bounce = 0.1
                contact.surface.bounce_vel = 0.1
                contact.surface.soft_cfm = 0.01

                val c = createContactJoint(world, contactGroup, contact)
                c.attach(body1, body2)
            }
        }

        world.quickStep(deltaTime)
        contactGroup.empty()
    }

    fun clearForces(shipId: Long) {
        val body = rigidBodies[shipId] ?: return
        body.setForce(0.0, 0.0, 0.0)
        body.setTorque(0.0, 0.0, 0.0)
    }

    fun destroy() {
        contactGroup.destroy()
        space.destroy()
        world.destroy()
    }
}
