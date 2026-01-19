package com.example.rocketceg.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.renderer.GameRenderer;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/** 😡 渲染立方体日月（纯色渲染，不使用材质） * 太阳：金黄色立方体 * 月球：灰白色立方体，支持月相（通过颜色深浅变化） 😡
     */
public class VanillaSunMoonRenderer {
    
    /** 😡 渲染立体太阳（金黄色立方体） * * @param poseStack 姿态栈 * @param timeOfDay 一天中的时间 (0.0 - 1.0) * @param partialTick 部分刻 😡
     */
    public static void renderSun(PoseStack poseStack, float timeOfDay, float partialTick) {
        poseStack.pushPose();
        
        // 😡 计算太阳角度（根据时间） 😡
        float sunAngle = timeOfDay * 360.0f;
        poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(sunAngle));
        
        // 😡 太阳距离和大小 😡
        float distance = 100.0f;
        float size = 30.0f;
        
        // 😡 设置渲染模式（纯色，不使用材质） 😡
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        
        // 😡 太阳颜色：金黄色，每个面略有不同 😡
        Vector3f[] sunColors = new Vector3f[] {
            new Vector3f(1.0f, 0.95f, 0.3f),  // 😡 前面 - 亮黄色 😡
            new Vector3f(1.0f, 0.9f, 0.2f),   // 😡 后面 - 金黄色 😡
            new Vector3f(1.0f, 0.92f, 0.25f), // 😡 上面 😡
            new Vector3f(1.0f, 0.88f, 0.15f), // 😡 下面 😡
            new Vector3f(1.0f, 0.93f, 0.28f), // 😡 右面 😡
            new Vector3f(1.0f, 0.91f, 0.22f)  // 😡 左面 😡
        };
        
        // 😡 渲染太阳的6个面（立方体） 😡
        renderCubicBody(poseStack, 0, distance, 0, size, sunColors);
        
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
        
        poseStack.popPose();
    }
    
    /** 😡 渲染立体月球（灰白色立方体，支持月相） * * @param poseStack 姿态栈 * @param timeOfDay 一天中的时间 (0.0 - 1.0) * @param moonPhase 月相 (0-7) * @param partialTick 部分刻 😡
     */
    public static void renderMoon(PoseStack poseStack, float timeOfDay, int moonPhase, float partialTick) {
        poseStack.pushPose();
        
        // 😡 计算月球角度（与太阳相反） 😡
        float moonAngle = timeOfDay * 360.0f;
        poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(moonAngle));
        
        // 😡 月球距离和大小 😡
        float distance = 100.0f;
        float size = 20.0f;
        
        // 😡 设置渲染模式（纯色，不使用材质） 😡
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        
        // 😡 根据月相调整亮度（0=满月最亮，4=新月最暗） 😡
        float brightness = 1.0f - (Math.abs(moonPhase - 4) / 8.0f);
        brightness = 0.5f + brightness * 0.5f; // 😡 范围：0.5 - 1.0 😡
        
        // 😡 月球颜色：灰白色，每个面略有不同 😡
        Vector3f[] moonColors = new Vector3f[] {
            new Vector3f(0.9f * brightness, 0.9f * brightness, 0.95f * brightness),  // 😡 前面 😡
            new Vector3f(0.85f * brightness, 0.85f * brightness, 0.9f * brightness), // 😡 后面 😡
            new Vector3f(0.88f * brightness, 0.88f * brightness, 0.93f * brightness),// 😡 上面 😡
            new Vector3f(0.82f * brightness, 0.82f * brightness, 0.87f * brightness),// 😡 下面 😡
            new Vector3f(0.87f * brightness, 0.87f * brightness, 0.92f * brightness),// 😡 右面 😡
            new Vector3f(0.86f * brightness, 0.86f * brightness, 0.91f * brightness) // 😡 左面 😡
        };
        
        // 😡 渲染月球的6个面（立方体） 😡
        renderCubicBody(poseStack, 0, distance, 0, size, moonColors);
        
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
        
        poseStack.popPose();
    }
    
    /** 😡 渲染立方体天体（6个面，纯色） * * @param poseStack 姿态栈 * @param x X坐标 * @param y Y坐标 * @param z Z坐标 * @param size 大小 * @param colors 6个面的颜色数组 😡
     */
    private static void renderCubicBody(PoseStack poseStack, float x, float y, float z, float size, Vector3f[] colors) {
        poseStack.pushPose();
        poseStack.translate(x, y, z);
        
        Matrix4f matrix = poseStack.last().pose();
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        
        float half = size / 2.0f;
        
        // 😡 开始渲染（使用颜色顶点格式） 😡
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        
        // 😡 前面（朝向玩家） 😡
        addColoredQuad(bufferBuilder, matrix,
            -half, -half, half,
            half, -half, half,
            half, half, half,
            -half, half, half,
            colors[0]
        );
        
        // 😡 后面 😡
        addColoredQuad(bufferBuilder, matrix,
            half, -half, -half,
            -half, -half, -half,
            -half, half, -half,
            half, half, -half,
            colors[1]
        );
        
        // 😡 上面 😡
        addColoredQuad(bufferBuilder, matrix,
            -half, half, -half,
            -half, half, half,
            half, half, half,
            half, half, -half,
            colors[2]
        );
        
        // 😡 下面 😡
        addColoredQuad(bufferBuilder, matrix,
            -half, -half, half,
            -half, -half, -half,
            half, -half, -half,
            half, -half, half,
            colors[3]
        );
        
        // 😡 右面 😡
        addColoredQuad(bufferBuilder, matrix,
            half, -half, half,
            half, -half, -half,
            half, half, -half,
            half, half, half,
            colors[4]
        );
        
        // 😡 左面 😡
        addColoredQuad(bufferBuilder, matrix,
            -half, -half, -half,
            -half, -half, half,
            -half, half, half,
            -half, half, -half,
            colors[5]
        );
        
        // 😡 结束渲染 😡
        BufferUploader.drawWithShader(bufferBuilder.end());
        
        poseStack.popPose();
    }
    
    /** 😡 添加一个带颜色的四边形 😡
     */
    private static void addColoredQuad(BufferBuilder builder, Matrix4f matrix,
                                      float x1, float y1, float z1,
                                      float x2, float y2, float z2,
                                      float x3, float y3, float z3,
                                      float x4, float y4, float z4,
                                      Vector3f color) {
        builder.vertex(matrix, x1, y1, z1).color(color.x, color.y, color.z, 1.0f).endVertex();
        builder.vertex(matrix, x2, y2, z2).color(color.x, color.y, color.z, 1.0f).endVertex();
        builder.vertex(matrix, x3, y3, z3).color(color.x, color.y, color.z, 1.0f).endVertex();
        builder.vertex(matrix, x4, y4, z4).color(color.x, color.y, color.z, 1.0f).endVertex();
    }
    
    /** 😡 检查是否应该渲染太阳（白天） 😡
     */
    public static boolean shouldRenderSun(float timeOfDay) {
        // 😡 太阳在 0.0 - 0.5 之间可见（白天） 😡
        return timeOfDay >= 0.0f && timeOfDay < 0.5f;
    }
    
    /** 😡 检查是否应该渲染月球（夜晚） 😡
     */
    public static boolean shouldRenderMoon(float timeOfDay) {
        // 😡 月球在 0.5 - 1.0 之间可见（夜晚） 😡
        return timeOfDay >= 0.5f && timeOfDay < 1.0f;
    }
    
    /** 😡 获取标准化的一天时间 (0.0 - 1.0) 😡
     */
    public static float getTimeOfDay(long dayTime) {
        float time = (dayTime % 24000L) / 24000.0f;
        return time;
    }
}
