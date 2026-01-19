package com.example.rocketceg.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

/** 😡 使用原版材质渲染立体日月 * 参考 Minecraft 原版的 LevelRenderer.renderSky() 方法 😡
     */
public class VanillaSunMoonRenderer {
    
    // 😡 Minecraft 原版材质路径 😡
    private static final ResourceLocation SUN_LOCATION = new ResourceLocation("minecraft", "textures/environment/sun.png");
    private static final ResourceLocation MOON_LOCATION = new ResourceLocation("minecraft", "textures/environment/moon_phases.png");
    
    /** 😡 渲染立体太阳（使用原版材质） * * @param poseStack 姿态栈 * @param timeOfDay 一天中的时间 (0.0 - 1.0) * @param partialTick 部分刻 😡
     */
    public static void renderSun(PoseStack poseStack, float timeOfDay, float partialTick) {
        poseStack.pushPose();
        
        // 😡 计算太阳角度（根据时间） 😡
        float sunAngle = timeOfDay * 360.0f;
 馃槨
        poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(sunAngle));
        
        // 😡 太阳距离和大小 😡
        float distance = 100.0f;
        float size = 30.0f;
        
        // 😡 绑定太阳材质 😡
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, SUN_LOCATION);
        
        // 😡 渲染太阳的6个面（立方体） 😡
        renderCubicSunMoon(poseStack, 0, distance, 0, size, true);
        
        poseStack.popPose();
    }
    
    /** 😡 渲染立体月球（使用原版材质） * * @param poseStack 姿态栈 * @param timeOfDay 一天中的时间 (0.0 - 1.0) * @param moonPhase 月相 (0-7) * @param partialTick 部分刻 😡
     */
    public static void renderMoon(PoseStack poseStack, float timeOfDay, int moonPhase, float partialTick) {
        poseStack.pushPose();
        
        // 😡 计算月球角度（与太阳相反） 😡
        float moonAngle = timeOfDay * 360.0f;
 馃槨
        poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(moonAngle));
        
        // 😡 月球距离和大小 😡
        float distance = 100.0f;
        float size = 20.0f;
        
        // 😡 绑定月球材质 😡
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, MOON_LOCATION);
        
        // 😡 渲染月球的6个面（立方体） 😡
        // 😡 月相通过 UV 坐标选择（月球材质是 4x2 的网格） 😡
        renderCubicSunMoon(poseStack, 0, distance, 0, size, false, moonPhase);
        
        poseStack.popPose();
    }
    
    /** 😡 渲染立方体日月（6个面） 😡
     */
    private static void renderCubicSunMoon(PoseStack poseStack, float x, float y, float z, float size, boolean isSun) {
        renderCubicSunMoon(poseStack, x, y, z, size, isSun, 0);
    }
    
    /** 😡 渲染立方体日月（6个面，支持月相） 😡
     */
    private static void renderCubicSunMoon(PoseStack poseStack, float x, float y, float z, float size, boolean isSun, int moonPhase) {
        poseStack.pushPose();
        poseStack.translate(x, y, z);
        
        Matrix4f matrix = poseStack.last().pose();
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        
        float half = size / 2.0f;
        
        // 😡 计算月相的 UV 坐标 😡
        float moonU = 0.0f;
        float moonV = 0.0f;
        if (!isSun) {
            // 😡 月相材质是 4x2 的网格 😡
            int moonCol = moonPhase % 4;
            int moonRow = moonPhase / 4;
            moonU = moonCol * 0.25f;
 馃槨
            moonV = moonRow * 0.5f;
 馃槨
        }
        
        // 😡 开始渲染 😡
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        
        // 😡 渲染6个面 😡
        // 😡 前面（朝向玩家） 😡
        addQuad(bufferBuilder, matrix,
            -half, -half, half,
            half, -half, half,
            half, half, half,
            -half, half, half,
            isSun ? 0.0f : moonU, isSun ? 0.0f : moonV,
            isSun ? 1.0f : (moonU + 0.25f), isSun ? 1.0f : (moonV + 0.5f)
        );
        
        // 😡 后面 😡
        addQuad(bufferBuilder, matrix,
            half, -half, -half,
            -half, -half, -half,
            -half, half, -half,
            half, half, -half,
            isSun ? 0.0f : moonU, isSun ? 0.0f : moonV,
            isSun ? 1.0f : (moonU + 0.25f), isSun ? 1.0f : (moonV + 0.5f)
        );
        
        // 😡 上面 😡
        addQuad(bufferBuilder, matrix,
            -half, half, -half,
            -half, half, half,
            half, half, half,
            half, half, -half,
            isSun ? 0.0f : moonU, isSun ? 0.0f : moonV,
            isSun ? 1.0f : (moonU + 0.25f), isSun ? 1.0f : (moonV + 0.5f)
        );
        
        // 😡 下面 😡
        addQuad(bufferBuilder, matrix,
            -half, -half, half,
            -half, -half, -half,
            half, -half, -half,
            half, -half, half,
            isSun ? 0.0f : moonU, isSun ? 0.0f : moonV,
            isSun ? 1.0f : (moonU + 0.25f), isSun ? 1.0f : (moonV + 0.5f)
        );
        
        // 😡 右面 😡
        addQuad(bufferBuilder, matrix,
            half, -half, half,
            half, -half, -half,
            half, half, -half,
            half, half, half,
            isSun ? 0.0f : moonU, isSun ? 0.0f : moonV,
            isSun ? 1.0f : (moonU + 0.25f), isSun ? 1.0f : (moonV + 0.5f)
        );
        
        // 😡 左面 😡
        addQuad(bufferBuilder, matrix,
            -half, -half, -half,
            -half, -half, half,
            -half, half, half,
            -half, half, -half,
            isSun ? 0.0f : moonU, isSun ? 0.0f : moonV,
            isSun ? 1.0f : (moonU + 0.25f), isSun ? 1.0f : (moonV + 0.5f)
        );
        
        // 😡 结束渲染 😡
        BufferUploader.drawWithShader(bufferBuilder.end());
        
        poseStack.popPose();
    }
    
    /** 😡 添加一个四边形 😡
     */
    private static void addQuad(BufferBuilder builder, Matrix4f matrix,
                                float x1, float y1, float z1,
                                float x2, float y2, float z2,
                                float x3, float y3, float z3,
                                float x4, float y4, float z4,
                                float u1, float v1, float u2, float v2) {
        builder.vertex(matrix, x1, y1, z1).uv(u1, v2).endVertex();
        builder.vertex(matrix, x2, y2, z2).uv(u2, v2).endVertex();
        builder.vertex(matrix, x3, y3, z3).uv(u2, v1).endVertex();
        builder.vertex(matrix, x4, y4, z4).uv(u1, v1).endVertex();
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
