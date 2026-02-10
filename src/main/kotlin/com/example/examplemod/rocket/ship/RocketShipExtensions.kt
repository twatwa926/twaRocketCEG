package com.example.examplemod.rocket.ship

import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.Vec3
import org.joml.Matrix4d
import org.joml.Vector3d

fun Matrix4d.transformPosition(vec: Vec3): Vec3 {
    val out = Vector3d(vec.x, vec.y, vec.z)
    transformPosition(out)
    return Vec3(out.x, out.y, out.z)
}

fun RocketShipStorage.massProperties(): RocketShipMassProperties {
    return RocketShipPhysics.computeMassProperties(this)
}

fun RocketShipStorage.centerOfMass(): Vec3 {
    return massProperties().centerOfMass
}

fun RocketShipStorage.inertiaVec(): Vec3 {
    return massProperties().inertia
}

fun RocketShipEntity.shipToWorldMatrix(): Matrix4d {
    val center = getCenterOfMass()
    return RocketShipPhysics.shipToWorldMatrix(getAnchorVec(), getYRot(), getXRot(), getRoll(), center)
}

fun RocketShipEntity.worldToLocal(vec: Vec3): Vec3 {
    val center = getCenterOfMass()
    return RocketShipPhysics.worldToLocal(vec, getAnchorVec(), getYRot(), getXRot(), getRoll(), center)
}

fun RocketShipEntity.localToWorld(vec: Vec3): Vec3 {
    val center = getCenterOfMass()
    return RocketShipPhysics.localToWorld(vec, getAnchorVec(), getYRot(), getXRot(), getRoll(), center)
}

fun RocketShipEntity.raycastLocal(eye: Vec3, look: Vec3, reach: Double): BlockHitResult? {
    val end = eye.add(look.scale(reach))
    val localStart = worldToLocal(eye)
    val localEnd = worldToLocal(end)
    return RocketShipRaycast.raycast(getShipWorld(), localStart, localEnd)
}
