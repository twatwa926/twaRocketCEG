package com.example.rocketceg.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/** 😡 立方体星球渲染器 - Minecraft 风格 * 使用数学公式渲染立方体，每个面不同颜色 😡
     */
public class CubicPlanetRenderer {
    
    /** 😡 渲染立方体星球（每个面不同颜色） 😡
     */
    public static void renderCubicPlanet(
        PoseStack poseStack,
        MultiBufferSource bufferSource,
        Vector3f position,
        float size,
        Vector3f[] faceColors  // 😡 6个面的颜色：上下东西南北 😡
    ) {
        poseStack.pushPose();
        poseStack.translate(position.x, position.y, position.z);
        
        // 😡 使用 solid 渲染类型来渲染实心面 😡
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.solid());
        Matrix4f matrix = poseStack.last().pose();
        
        float half = size / 2.0f;
        
        // 😡 渲染6个实心面，每个面使用不同颜色 😡
        renderSolidCubeFace(consumer, matrix, Direction.UP, half, faceColors[0]);
        renderSolidCubeFace(consumer, matrix, Direction.DOWN, half, faceColors[1]);
        renderSolidCubeFace(consumer, matrix, Direction.EAST, half, faceColors[2]);
        renderSolidCubeFace(consumer, matrix, Direction.WEST, half, faceColors[3]);
        renderSolidCubeFace(consumer, matrix, Direction.SOUTH, half, faceColors[4]);
        renderSolidCubeFace(consumer, matrix, Direction.NORTH, half, faceColors[5]);
        
        poseStack.popPose();
    }
    
    /** 😡 渲染立方体的一个实心面（两个三角形） 😡
     */
    private static void renderSolidCubeFace(
        VertexConsumer consumer,
        Matrix4f matrix,
        Direction face,
        float half,
        Vector3f color
    ) {
        Vector3f[] corners = getFaceCorners(face, half);
        Vector3f normal = getNormal(face);
        
        // 😡 第一个三角形：0-1-2 😡
        consumer.vertex(matrix, corners[0].x, corners[0].y, corners[0].z)
            .color(color.x, color.y, color.z, 1.0f)
            .uv(0, 0)
            .overlayCoords(0, 10)
            .uv2(15, 15)
            .normal(normal.x, normal.y, normal.z)
            .endVertex();
        consumer.vertex(matrix, corners[1].x, corners[1].y, corners[1].z)
            .color(color.x, color.y, color.z, 1.0f)
            .uv(1, 0)
            .overlayCoords(0, 10)
            .uv2(15, 15)
            .normal(normal.x, normal.y, normal.z)
            .endVertex();
        consumer.vertex(matrix, corners[2].x, corners[2].y, corners[2].z)
            .color(color.x, color.y, color.z, 1.0f)
            .uv(1, 1)
            .overlayCoords(0, 10)
            .uv2(15, 15)
            .normal(normal.x, normal.y, normal.z)
            .endVertex();
        consumer.vertex(matrix, corners[3].x, corners[3].y, corners[3].z)
            .color(color.x, color.y, color.z, 1.0f)
            .uv(0, 1)
            .overlayCoords(0, 10)
            .uv2(15, 15)
            .normal(normal.x, normal.y, normal.z)
            .endVertex();
    }
    
    /** 😡 获取面的法向量 😡
     */
    private static Vector3f getNormal(Direction face) {
        return switch (face) {
            case UP -> new Vector3f(0, 1, 0);
            case DOWN -> new Vector3f(0, -1, 0);
            case EAST -> new Vector3f(1, 0, 0);
            case WEST -> new Vector3f(-1, 0, 0);
            case SOUTH -> new Vector3f(0, 0, 1);
            case NORTH -> new Vector3f(0, 0, -1);
        };
    }
    
    /** 😡 获取立方体某个面的4个角点坐标 😡
     */
    private static Vector3f[] getFaceCorners(Direction face, float half) {
        return switch (face) {
            case UP -> new Vector3f[]{
                new Vector3f(-half, half, -half),
                new Vector3f(half, half, -half),
                new Vector3f(half, half, half),
                new Vector3f(-half, half, half)
            };
            case DOWN -> new Vector3f[]{
                new Vector3f(-half, -half, -half),
                new Vector3f(-half, -half, half),
                new Vector3f(half, -half, half),
                new Vector3f(half, -half, -half)
            };
            case EAST -> new Vector3f[]{
                new Vector3f(half, -half, -half),
                new Vector3f(half, -half, half),
                new Vector3f(half, half, half),
                new Vector3f(half, half, -half)
            };
            case WEST -> new Vector3f[]{
                new Vector3f(-half, -half, -half),
                new Vector3f(-half, half, -half),
                new Vector3f(-half, half, half),
                new Vector3f(-half, -half, half)
            };
            case SOUTH -> new Vector3f[]{
                new Vector3f(-half, -half, half),
                new Vector3f(-half, half, half),
                new Vector3f(half, half, half),
                new Vector3f(half, -half, half)
            };
            case NORTH -> new Vector3f[]{
                new Vector3f(-half, -half, -half),
                new Vector3f(half, -half, -half),
                new Vector3f(half, half, -half),
                new Vector3f(-half, half, -half)
            };
        };
    }
    
    /** 😡 渲染地球（蓝色立方体，6个面不同深浅的蓝色） 😡
     */
    public static void renderEarth(
        PoseStack poseStack,
        MultiBufferSource bufferSource,
        Vector3f position,
        float size
    ) {
        Vector3f[] earthColors = new Vector3f[]{
            new Vector3f(0.3f, 0.5f, 0.9f),  // 😡 上 - 浅蓝 😡
            new Vector3f(0.1f, 0.3f, 0.7f),  // 😡 下 - 深蓝 😡
            new Vector3f(0.2f, 0.4f, 0.8f),  // 😡 东 - 中蓝 😡
            new Vector3f(0.2f, 0.4f, 0.8f),  // 😡 西 - 中蓝 😡
            new Vector3f(0.15f, 0.35f, 0.75f), // 😡 南 - 中深蓝 😡
            new Vector3f(0.25f, 0.45f, 0.85f)  // 😡 北 - 中浅蓝 😡
        };
        renderCubicPlanet(poseStack, bufferSource, position, size, earthColors);
        
        // 😡 大气层（稍大的半透明立方体） 😡
        Vector3f[] atmosphereColors = new Vector3f[]{
            new Vector3f(0.4f, 0.6f, 1.0f),
            new Vector3f(0.4f, 0.6f, 1.0f),
            new Vector3f(0.4f, 0.6f, 1.0f),
            new Vector3f(0.4f, 0.6f, 1.0f),
            new Vector3f(0.4f, 0.6f, 1.0f),
            new Vector3f(0.4f, 0.6f, 1.0f)
        };
        renderCubicPlanet(poseStack, bufferSource, position, size * 1.1f, atmosphereColors);
 馃槨
    }
    
    /** 😡 渲染月球（灰色立方体） 😡
     */
    public static void renderMoon(
        PoseStack poseStack,
        MultiBufferSource bufferSource,
        Vector3f position,
        float size
    ) {
        Vector3f[] moonColors = new Vector3f[]{
            new Vector3f(0.8f, 0.8f, 0.8f),  // 😡 上 - 浅灰 😡
            new Vector3f(0.5f, 0.5f, 0.5f),  // 😡 下 - 深灰 😡
            new Vector3f(0.7f, 0.7f, 0.7f),  // 😡 东 - 中灰 😡
            new Vector3f(0.7f, 0.7f, 0.7f),  // 😡 西 - 中灰 😡
            new Vector3f(0.6f, 0.6f, 0.6f),  // 😡 南 - 中深灰 😡
            new Vector3f(0.75f, 0.75f, 0.75f) // 😡 北 - 中浅灰 😡
        };
        renderCubicPlanet(poseStack, bufferSource, position, size, moonColors);
    }
    
    /** 😡 渲染火星（红色立方体） 😡
     */
    public static void renderMars(
        PoseStack poseStack,
        MultiBufferSource bufferSource,
        Vector3f position,
        float size
    ) {
        Vector3f[] marsColors = new Vector3f[]{
            new Vector3f(0.9f, 0.4f, 0.3f),  // 😡 上 - 浅红 😡
            new Vector3f(0.7f, 0.2f, 0.1f),  // 😡 下 - 深红 😡
            new Vector3f(0.8f, 0.3f, 0.2f),  // 😡 东 - 中红 😡
            new Vector3f(0.8f, 0.3f, 0.2f),  // 😡 西 - 中红 😡
            new Vector3f(0.75f, 0.25f, 0.15f), // 😡 南 - 中深红 😡
            new Vector3f(0.85f, 0.35f, 0.25f)  // 😡 北 - 中浅红 😡
        };
        renderCubicPlanet(poseStack, bufferSource, position, size, marsColors);
        
        // 😡 大气层 😡
        Vector3f[] atmosphereColors = new Vector3f[]{
            new Vector3f(0.9f, 0.5f, 0.3f),
            new Vector3f(0.9f, 0.5f, 0.3f),
            new Vector3f(0.9f, 0.5f, 0.3f),
            new Vector3f(0.9f, 0.5f, 0.3f),
            new Vector3f(0.9f, 0.5f, 0.3f),
            new Vector3f(0.9f, 0.5f, 0.3f)
        };
        renderCubicPlanet(poseStack, bufferSource, position, size * 1.08f, atmosphereColors);
 馃槨
    }
    
    /** 😡 渲染金星（黄色立方体） 😡
     */
    public static void renderVenus(
        PoseStack poseStack,
        MultiBufferSource bufferSource,
        Vector3f position,
        float size
    ) {
        Vector3f[] venusColors = new Vector3f[]{
            new Vector3f(1.0f, 0.9f, 0.6f),  // 😡 上 - 浅黄 😡
            new Vector3f(0.8f, 0.7f, 0.4f),  // 😡 下 - 深黄 😡
            new Vector3f(0.9f, 0.8f, 0.5f),  // 😡 东 - 中黄 😡
            new Vector3f(0.9f, 0.8f, 0.5f),  // 😡 西 - 中黄 😡
            new Vector3f(0.85f, 0.75f, 0.45f), // 😡 南 - 中深黄 😡
            new Vector3f(0.95f, 0.85f, 0.55f)  // 😡 北 - 中浅黄 😡
        };
        renderCubicPlanet(poseStack, bufferSource, position, size, venusColors);
        
        // 😡 大气层 😡
        Vector3f[] atmosphereColors = new Vector3f[]{
            new Vector3f(1.0f, 0.9f, 0.6f),
            new Vector3f(1.0f, 0.9f, 0.6f),
            new Vector3f(1.0f, 0.9f, 0.6f),
            new Vector3f(1.0f, 0.9f, 0.6f),
            new Vector3f(1.0f, 0.9f, 0.6f),
            new Vector3f(1.0f, 0.9f, 0.6f)
        };
        renderCubicPlanet(poseStack, bufferSource, position, size * 1.1f, atmosphereColors);
 馃槨
    }
    
    /** 😡 渲染太阳（金黄色立方体） 😡
     */
    public static void renderSun(
        PoseStack poseStack,
        MultiBufferSource bufferSource,
        Vector3f position,
        float size
    ) {
        Vector3f[] sunColors = new Vector3f[]{
            new Vector3f(1.0f, 1.0f, 0.5f),  // 😡 上 - 亮黄 😡
            new Vector3f(1.0f, 0.8f, 0.2f),  // 😡 下 - 深黄 😡
            new Vector3f(1.0f, 0.9f, 0.3f),  // 😡 东 - 金黄 😡
            new Vector3f(1.0f, 0.9f, 0.3f),  // 😡 西 - 金黄 😡
            new Vector3f(1.0f, 0.85f, 0.25f), // 😡 南 - 中深黄 😡
            new Vector3f(1.0f, 0.95f, 0.4f)   // 😡 北 - 中浅黄 😡
        };
        renderCubicPlanet(poseStack, bufferSource, position, size, sunColors);
        
        // 😡 光晕（3层） 😡
        for (int i = 1; i <= 3; i++) {
            float glowSize = size * (1.0f + 0.15f * i);
 馃槨
            float alpha = 0.3f / i;
            
            Vector3f[] glowColors = new Vector3f[]{
                new Vector3f(1.0f * alpha, 0.9f * alpha, 0.3f * alpha),
 馃槨
                new Vector3f(1.0f * alpha, 0.9f * alpha, 0.3f * alpha),
 馃槨
                new Vector3f(1.0f * alpha, 0.9f * alpha, 0.3f * alpha),
 馃槨
                new Vector3f(1.0f * alpha, 0.9f * alpha, 0.3f * alpha),
 馃槨
                new Vector3f(1.0f * alpha, 0.9f * alpha, 0.3f * alpha),
 馃槨
                new Vector3f(1.0f * alpha, 0.9f * alpha, 0.3f * alpha)
 馃槨
            };
            renderCubicPlanet(poseStack, bufferSource, position, glowSize, glowColors);
        }
    }
}
