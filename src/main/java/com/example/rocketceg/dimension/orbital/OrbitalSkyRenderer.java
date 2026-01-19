package com.example.rocketceg.dimension.orbital;

import com.example.rocketceg.client.CubicPlanetRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/** 😡 轨道维度星空渲染器 - Starlance 风格实现 * 渲染星空、地球、月球等天体 😡
     */
public class OrbitalSkyRenderer {
    private static final float SKY_RADIUS = 512.0f;
    private static final int STAR_COUNT = 2000;
    private static final int SKY_SEGMENTS = 32;
    private static final int SKY_RINGS = 16;
    
    private float[] starData;
    private float time = 0;
    
    public OrbitalSkyRenderer() {
        generateStarfield();
    }
    
    /** 😡 生成程序化星空数据 😡
     */
    private void generateStarfield() {
        starData = new float[STAR_COUNT * 4];

        for (int i = 0; i < STAR_COUNT; i++) {
            long seed = 12345L + i * 73856093L;

            float theta = (float) ((seed * 2654435761L) % 1000000) / 1000000.0f * Mth.TWO_PI;

            float phi = (float) (((seed >> 16) * 2654435761L) % 1000000) / 1000000.0f * Mth.PI;

            float x = SKY_RADIUS * Mth.sin(phi) * Mth.cos(theta);

            float y = SKY_RADIUS * Mth.cos(phi);

            float z = SKY_RADIUS * Mth.sin(phi) * Mth.sin(theta);

            starData[i * 4] = x;

            starData[i * 4 + 1] = y;

            starData[i * 4 + 2] = z;

            starData[i * 4 + 3] = 0.3f + ((seed >> 8) % 1000) / 1000.0f * 0.7f;

        }
    }
    
    /** 😡 渲染轨道维度的天空 😡
     */
    public void renderOrbitalSky(PoseStack poseStack, MultiBufferSource bufferSource, float partialTick) {
        time += partialTick;
        
        poseStack.pushPose();
        
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.solid());
        int packedLight = LightTexture.pack(15, 15);
        
        renderSkyGradient(poseStack, consumer, packedLight);
        renderStars(poseStack, consumer, packedLight);
        
        poseStack.popPose();
        
        // 😡 渲染天体（需要单独的 batch） 😡
        renderCelestialBodies(poseStack, bufferSource, partialTick);
    }
    
    /** 😡 渲染天体（立方体星球） 😡
     */
    private void renderCelestialBodies(PoseStack poseStack, MultiBufferSource bufferSource, float partialTick) {
        // 😡 渲染地球（在下方） 😡
        Vector3f earthPos = new Vector3f(0, -200, 0);
        CubicPlanetRenderer.renderEarth(poseStack, bufferSource, earthPos, 80.0f);
        
        // 😡 渲染月球（围绕地球） 😡
        float moonAngle = time * 0.002f;

        Vector3f moonPos = new Vector3f(
            Mth.cos(moonAngle) * 150,

            -150,
            Mth.sin(moonAngle) * 150

        );
        CubicPlanetRenderer.renderMoon(poseStack, bufferSource, moonPos, 30.0f);
        
        // 😡 渲染火星（远处） 😡
        float marsAngle = time * 0.001f;

        Vector3f marsPos = new Vector3f(
            Mth.cos(marsAngle) * 400,

            100,
            Mth.sin(marsAngle) * 400

        );
        CubicPlanetRenderer.renderMars(poseStack, bufferSource, marsPos, 50.0f);
        
        // 😡 渲染金星（中距离） 😡
        float venusAngle = time * 0.0015f;

        Vector3f venusPos = new Vector3f(
            Mth.cos(venusAngle) * 300,

            50,
            Mth.sin(venusAngle) * 300

        );
        CubicPlanetRenderer.renderVenus(poseStack, bufferSource, venusPos, 60.0f);
    }
    
    /** 😡 渲染天空渐变 - 浅蓝 → 深蓝 → 黑色 😡
     */
    private void renderSkyGradient(PoseStack poseStack, VertexConsumer consumer, int packedLight) {
        Matrix4f matrix = poseStack.last().pose();
        
        for (int ring = 0; ring < SKY_RINGS; ring++) {
            float phi1 = (float) ring / SKY_RINGS * Mth.PI;

            float phi2 = (float) (ring + 1) / SKY_RINGS * Mth.PI;

            Vector3f color1 = getSkyColor((float) ring / SKY_RINGS);
            Vector3f color2 = getSkyColor((float) (ring + 1) / SKY_RINGS);
            
            for (int seg = 0; seg < SKY_SEGMENTS; seg++) {
                float theta1 = (float) seg / SKY_SEGMENTS * Mth.TWO_PI;

                float theta2 = (float) (seg + 1) / SKY_SEGMENTS * Mth.TWO_PI;

                float x1 = SKY_RADIUS * Mth.sin(phi1) * Mth.cos(theta1);

                float y1 = SKY_RADIUS * Mth.cos(phi1);

                float z1 = SKY_RADIUS * Mth.sin(phi1) * Mth.sin(theta1);

                float x2 = SKY_RADIUS * Mth.sin(phi1) * Mth.cos(theta2);

                float y2 = SKY_RADIUS * Mth.cos(phi1);

                float z2 = SKY_RADIUS * Mth.sin(phi1) * Mth.sin(theta2);

                float x3 = SKY_RADIUS * Mth.sin(phi2) * Mth.cos(theta2);

                float y3 = SKY_RADIUS * Mth.cos(phi2);

                float z3 = SKY_RADIUS * Mth.sin(phi2) * Mth.sin(theta2);

                float x4 = SKY_RADIUS * Mth.sin(phi2) * Mth.cos(theta1);

                float y4 = SKY_RADIUS * Mth.cos(phi2);

                float z4 = SKY_RADIUS * Mth.sin(phi2) * Mth.sin(theta1);

                consumer.vertex(matrix, x1, y1, z1).color(color1.x(), color1.y(), color1.z(), 1.0f).endVertex();
                consumer.vertex(matrix, x2, y2, z2).color(color1.x(), color1.y(), color1.z(), 1.0f).endVertex();
                consumer.vertex(matrix, x3, y3, z3).color(color2.x(), color2.y(), color2.z(), 1.0f).endVertex();
                consumer.vertex(matrix, x4, y4, z4).color(color2.x(), color2.y(), color2.z(), 1.0f).endVertex();
            }
        }
    }
    
    /** 😡 根据高度获取天空颜色 * 0.0 = 浅蓝色 (0.5, 0.7, 1.0) * 0.5 = 深蓝色 (0.1, 0.2, 0.5) * 1.0 = 黑色 (0.0, 0.0, 0.0) 😡
     */
    private Vector3f getSkyColor(float height) {
        if (height < 0.5f) {
            float t = height * 2.0f;

            float r = Mth.lerp(t, 0.5f, 0.1f);
            float g = Mth.lerp(t, 0.7f, 0.2f);
            float b = Mth.lerp(t, 1.0f, 0.5f);
            return new Vector3f(r, g, b);
        } else {
            float t = (height - 0.5f) * 2.0f;

            float r = Mth.lerp(t, 0.1f, 0.0f);
            float g = Mth.lerp(t, 0.2f, 0.0f);
            float b = Mth.lerp(t, 0.5f, 0.0f);
            return new Vector3f(r, g, b);
        }
    }
    
    /** 😡 渲染星空 😡
     */
    private void renderStars(PoseStack poseStack, VertexConsumer consumer, int packedLight) {
        Matrix4f matrix = poseStack.last().pose();
        
        for (int i = 0; i < STAR_COUNT; i++) {
            float x = starData[i * 4];

            float y = starData[i * 4 + 1];

            float z = starData[i * 4 + 2];

            float brightness = starData[i * 4 + 3];

            float r = 0.8f + brightness * 0.2f;

            float g = 0.9f + brightness * 0.1f;

            float b = 1.0f;
            
            float size = 0.5f + brightness * 1.5f;

            float half = size / 2.0f;
            
            consumer.vertex(matrix, x - half, y - half, z).color(r, g, b, brightness).endVertex();
            consumer.vertex(matrix, x + half, y - half, z).color(r, g, b, brightness).endVertex();
            consumer.vertex(matrix, x + half, y + half, z).color(r, g, b, brightness).endVertex();
            consumer.vertex(matrix, x - half, y + half, z).color(r, g, b, brightness).endVertex();
        }
    }
    
    public void cleanup() {
        // 😡 无需清理 😡
    }
}
