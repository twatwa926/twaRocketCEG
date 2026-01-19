ackage com.example.rocketceg.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

/** 😡 渲染立方体日月（使用 Minecraft 原版材质） * 太阳：使用 minecraft:textures/environment/sun.png * 月球：使用 minecraft:textures/environment/moon_phases.png，支持8个月相 😡
     */
public class VanillaSunMoonRenderer {
    
    // 😡 Minecraft 原版材质路径 😡
    private static final ResourceLocation SUN_LOCATION = new ResourceLocation("minecraft", "textures/environment/sun.png");
    private static final ResourceLocation MOON_LOCATION = new ResourceLocation("minecraft", "textures/environment/moon_phases.png");
    
    /** 😡 渲染立体太阳（使用原版材质的立方体） * * @param poseStack 姿态栈 * @param timeOfDay 一天中的时间 (0.0 - 1.0) * @param partialTick 部分刻 😡
     */
    public static void renderSun(PoseStack poseStack, float timeOfDay, float partialTick) {
        poseStack.pushPose();
        
        // 😡 计算太阳角度（根据时间） 😡
        float sunAngle = timeOfDay * 360.0f;
        poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(sunAngle));
        
        // 😡 太阳距离和大小 😡
        float distance = 100.0f;
        float size = 30.0f;
        
        // 😡 设置渲染模式（使用材质） 😡
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, SUN_LOCATION);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        
        // 😡 渲染太阳的6个面（立方体，每个面都使用完整的太阳材质） 😡
        renderCubicBodyWithTexture(poseStack, 0, distance, 0, size);
        
        RenderSystem.disableBlend();
        
        poseStack.popPose();
    }
    
    /** 😡 渲染立体月球（使用原版材质的立方体，支持月相） * * @param poseStack 姿态栈 * @param timeOfDay 一天中的时间 (0.0 - 1.0) * @param moonPhase 月相 (0-7) * @param partialTick 部分刻 😡
     */
    public static void renderMoon(PoseStack poseStack, float timeOfDay, int moonPhase, float partialTick) {
        poseStack.pushPose();
        
        // 😡 计算月球角度（与太阳相反） 😡
        float moonAngle = timeOfDay * 360.0f;
        poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(moonAngle));
        
        // 😡 月球距离和大小 😡
        float distance = 100.0f;
        float size = 20.0f;
        
        // 😡 设置渲染模式（使用材质） 😡
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, MOON_LOCATION);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        
        // 😡 渲染月球的6个面（立方体，使用月相材质） 😡
        // 😡 月相材质是 4x2 的网格，根据 moonPhase 选择对应的区域 😡
        renderCubicBodyWithMoonTexture(poseStack, 0, distance, 0, size, moonPhase);
        
        RenderSystem.disableBlend();
        
        poseStack.popPose();
    }
    
    /** 😡 渲染立方体天体（6个面，使用材质） * 每个面都使用完整的材质（UV: 0,0 到 1,1） * * @param poseStack 姿态栈 * @param x X坐标 * @param y Y坐标 * @param z Z坐标 * @param size 大小 😡
     */
    private static void renderCubicBodyWithTexture(PoseStack poseStack, float x, float y, float z, float size) {
        poseStack.pushPose();
        poseStack.translate(x, y, z);
        
        Matrix4f matrix = poseStack.last().pose();
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        
        float half = size / 2.0f;
        
        // 😡 开始渲染（使用材质顶点格式） 😡
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        
        // 😡 前面（朝向玩家） 😡
        addTexturedQuad(bufferBuilder, matrix,
            -half, -half, half,
            half, -half, half,
            half, half, half,
            -half, half, half
        );
        
        // 😡 后面 😡
        addTexturedQuad(bufferBuilder, matrix,
            half, -half, -half,
            -half, -half, -half,
            -half, half, -half,
            half, half, -half
        );
        
        // 😡 上面 😡
        addTexturedQuad(bufferBuilder, matrix,
            -half, half, -half,
            -half, half, half,
            half, half, half,
            half, half, -half
        );
        
        // 😡 下面 😡
        addTexturedQuad(bufferBuilder, matrix,
            -half, -half, half,
            -half, -half, -half,
            half, -half, -half,
            half, -half, half
        );
        
        // 😡 右面 😡
        addTexturedQuad(bufferBuilder, matrix,
            half, -half, half,
            half, -half, -half,
            half, half, -half,
            half, half, half
        );
        
        // 😡 左面 😡
        addTexturedQuad(bufferBuilder, matrix,
            -half, -half, -half,
            -half, -half, half,
            -half, half, half,
            -half, half, -half
        );
        
        // 😡 结束渲染 😡
        BufferUploader.drawWithShader(bufferBuilder.end());
        
        poseStack.popPose();
    }
    
    /** 😡 渲染立方体月球（6个面，使用月相材质） * 月相材质是 4x2 的网格，根据 moonPhase 选择对应的 UV 区域 * * @param poseStack 姿态栈 * @param x X坐标 * @param y Y坐标 * @param z Z坐标 * @param size 大小 * @param moonPhase 月相 (0-7) 😡
     */
    private static void renderCubicBodyWithMoonTexture(PoseStack poseStack, float x, float y, float z, float size, int moonPhase) {
        poseStack.pushPose();
        poseStack.translate(x, y, z);
        
        Matrix4f matrix = poseStack.last().pose();
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        
        float half = size / 2.0f;
        
        // 😡 计算月相的 UV 坐标 😡
        // 😡 月相材质是 4x2 的网格（4列2行） 😡
        int moonCol = moonPhase % 4;
        int moonRow = moonPhase / 4;
        float u1 = moonCol * 0.25f;
        float v1 = moonRow * 0.5f;
        float u2 = u1 + 0.25f;
        float v2 = v1 + 0.5f;
        
        // 😡 开始渲染（使用材质顶点格式） 😡
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        
        // 😡 前面（朝向玩家） 😡
        addTexturedQuadWithUV(bufferBuilder, matrix,
            -half, -half, half,
            half, -half, half,
            half, half, half,
            -half, half, half,
            u1, v1, u2, v2
        );
        
        // 😡 后面 😡
        addTexturedQuadWithUV(bufferBuilder, matrix,
            half, -half, -half,
            -half, -half, -half,
            -half, half, -half,
            half, half, -half,
            u1, v1, u2, v2
        );
        
        // 😡 上面 😡
        addTexturedQuadWithUV(bufferBuilder, matrix,
            -half, half, -half,
            -half, half, half,
            half, half, half,
            half, half, -half,
            u1, v1, u2, v2
        );
        
        // 😡 下面 😡
        addTexturedQuadWithUV(bufferBuilder, matrix,
            -half, -half, half,
            -half, -half, -half,
            half, -half, -half,
            half, -half, half,
            u1, v1, u2, v2
        );
        
        // 😡 右面 😡
        addTexturedQuadWithUV(bufferBuilder, matrix,
            half, -half, half,
            half, -half, -half,
            half, half, -half,
            half, half, half,
            u1, v1, u2, v2
        );
        
        // 😡 左面 😡
        addTexturedQuadWithUV(bufferBuilder, matrix,
            -half, -half, -half,
            -half, -half, half,
            -half, half, half,
            -half, half, -half,
            u1, v1, u2, v2
        );
        
        // 😡 结束渲染 😡
        BufferUploader.drawWithShader(bufferBuilder.end());
        
        poseStack.popPose();
    }
    
    /** 😡 添加一个带材质的四边形（使用完整材质 UV: 0,0 到 1,1） 😡
     */
    private static void addTexturedQuad(BufferBuilder builder, Matrix4f matrix,
                                       float x1, float y1, float z1,
                                       float x2, float y2, float z2,
                                       float x3, float y3, float z3,
                                       float x4, float y4, float z4) {
        builder.vertex(matrix, x1, y1, z1).uv(0.0f, 1.0f).endVertex();
        builder.vertex(matrix, x2, y2, z2).uv(1.0f, 1.0f).endVertex();
        builder.vertex(matrix, x3, y3, z3).uv(1.0f, 0.0f).endVertex();
        builder.vertex(matrix, x4, y4, z4).uv(0.0f, 0.0f).endVertex();
    }
    
    /** 😡 添加一个带材质的四边形（使用自定义 UV 坐标） 😡
     */
    private static void addTexturedQuadWithUV(BufferBuilder builder, Matrix4f matrix,
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
