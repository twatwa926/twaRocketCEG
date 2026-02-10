package com.example.examplemod.rocket.debug

import com.example.examplemod.rocket.ship.RocketShipEntity
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.VertexConsumer
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.LevelRenderer
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.RenderType
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import org.joml.Matrix4f

object RocketDebugRenderer {

    private var collisionDebugEnabled = false
    private var debugStats = DebugStats()

    data class DebugStats(
        var rocketCount: Int = 0,
        var totalCollisionBoxes: Int = 0,
        var totalBoundingBoxes: Int = 0
    )

    fun setCollisionDebugEnabled(enabled: Boolean) {
        collisionDebugEnabled = enabled
    }

    fun isCollisionDebugEnabled(): Boolean = collisionDebugEnabled

    fun getDebugStats(): DebugStats = debugStats.copy()

    fun renderDebugInfo(
        entity: RocketShipEntity,
        poseStack: PoseStack,
        bufferSource: MultiBufferSource,
        partialTicks: Float
    ) {
        if (!collisionDebugEnabled) return

        if (System.getProperty("rocket.debug.render") != null) {
            println("[RocketDebug] 渲染火箭 ${entity.id} 的调试信息")
        }

        debugStats.rocketCount++

        val camera = Minecraft.getInstance().gameRenderer.mainCamera
        val cameraPos = camera.position

        val entityPos = entity.position().add(
            entity.deltaMovement.scale(partialTicks.toDouble())
        )

        poseStack.pushPose()

        poseStack.translate(
            entityPos.x - cameraPos.x,
            entityPos.y - cameraPos.y,
            entityPos.z - cameraPos.z
        )

        renderEncompassingBoundingBox(entity, poseStack, bufferSource, cameraPos)

        renderBlockCollisionBoxes(entity, poseStack, bufferSource, cameraPos)

        poseStack.popPose()
    }

    private fun renderEncompassingBoundingBox(
        entity: RocketShipEntity,
        poseStack: PoseStack,
        bufferSource: MultiBufferSource,
        @Suppress("UNUSED_PARAMETER") cameraPos: Vec3
    ) {
        val boundingBox = entity.boundingBox
        val entityPos = entity.position()

        val relativeBox = AABB(
            boundingBox.minX - entityPos.x,
            boundingBox.minY - entityPos.y,
            boundingBox.minZ - entityPos.z,
            boundingBox.maxX - entityPos.x,
            boundingBox.maxY - entityPos.y,
            boundingBox.maxZ - entityPos.z
        )

        renderBox(poseStack, bufferSource, relativeBox, 0.0f, 1.0f, 0.0f, 0.5f)

        debugStats.totalBoundingBoxes++

        if (System.getProperty("rocket.debug.render") != null) {
            println("[RocketDebug] 包围式碰撞箱: $relativeBox")
        }
    }

    private fun renderBlockCollisionBoxes(
        entity: RocketShipEntity,
        poseStack: PoseStack,
        bufferSource: MultiBufferSource,
        @Suppress("UNUSED_PARAMETER") cameraPos: Vec3
    ) {
        val collisionBoxes = entity.getBlockCollisionBoxes()
        val entityPos = entity.position()

        if (System.getProperty("rocket.debug.render") != null) {
            println("[RocketDebug] 方块碰撞箱数量: ${collisionBoxes.size}")
        }

        collisionBoxes.forEach { box ->

            val relativeBox = AABB(
                box.minX - entityPos.x,
                box.minY - entityPos.y,
                box.minZ - entityPos.z,
                box.maxX - entityPos.x,
                box.maxY - entityPos.y,
                box.maxZ - entityPos.z
            )

            renderBox(poseStack, bufferSource, relativeBox, 1.0f, 0.0f, 0.0f, 0.3f)

            debugStats.totalCollisionBoxes++
        }
    }

    private fun renderBox(
        poseStack: PoseStack,
        bufferSource: MultiBufferSource,
        box: AABB,
        red: Float,
        green: Float,
        blue: Float,
        alpha: Float
    ) {
        val matrix = poseStack.last().pose()
        val buffer = bufferSource.getBuffer(RenderType.lines())

        renderLine(buffer, matrix, box.minX, box.minY, box.minZ, box.maxX, box.minY, box.minZ, red, green, blue, alpha)
        renderLine(buffer, matrix, box.maxX, box.minY, box.minZ, box.maxX, box.minY, box.maxZ, red, green, blue, alpha)
        renderLine(buffer, matrix, box.maxX, box.minY, box.maxZ, box.minX, box.minY, box.maxZ, red, green, blue, alpha)
        renderLine(buffer, matrix, box.minX, box.minY, box.maxZ, box.minX, box.minY, box.minZ, red, green, blue, alpha)

        renderLine(buffer, matrix, box.minX, box.maxY, box.minZ, box.maxX, box.maxY, box.minZ, red, green, blue, alpha)
        renderLine(buffer, matrix, box.maxX, box.maxY, box.minZ, box.maxX, box.maxY, box.maxZ, red, green, blue, alpha)
        renderLine(buffer, matrix, box.maxX, box.maxY, box.maxZ, box.minX, box.maxY, box.maxZ, red, green, blue, alpha)
        renderLine(buffer, matrix, box.minX, box.maxY, box.maxZ, box.minX, box.maxY, box.minZ, red, green, blue, alpha)

        renderLine(buffer, matrix, box.minX, box.minY, box.minZ, box.minX, box.maxY, box.minZ, red, green, blue, alpha)
        renderLine(buffer, matrix, box.maxX, box.minY, box.minZ, box.maxX, box.maxY, box.minZ, red, green, blue, alpha)
        renderLine(buffer, matrix, box.maxX, box.minY, box.maxZ, box.maxX, box.maxY, box.maxZ, red, green, blue, alpha)
        renderLine(buffer, matrix, box.minX, box.minY, box.maxZ, box.minX, box.maxY, box.maxZ, red, green, blue, alpha)
    }

    private fun renderLine(
        buffer: VertexConsumer,
        matrix: Matrix4f,
        x1: Double, y1: Double, z1: Double,
        x2: Double, y2: Double, z2: Double,
        red: Float, green: Float, blue: Float, alpha: Float
    ) {
        buffer.vertex(matrix, x1.toFloat(), y1.toFloat(), z1.toFloat())
            .color(red, green, blue, alpha)
            .normal(0.0f, 1.0f, 0.0f)
            .endVertex()

        buffer.vertex(matrix, x2.toFloat(), y2.toFloat(), z2.toFloat())
            .color(red, green, blue, alpha)
            .normal(0.0f, 1.0f, 0.0f)
            .endVertex()
    }

    fun resetStats() {
        debugStats = DebugStats()
    }
}
