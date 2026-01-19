ackage com.example.rocketceg.dimension.seamless;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import org.joml.Matrix4f;

/** 😡 传送过渡效果 * * 提供各种视觉过渡效果，使维度切换看起来平滑自然 😡
     */
public class TransitionEffects {
    
    /** 😡 渲染传送过渡效果 😡
     */
    public static void renderTransition(float progress, float partialTick) {
        Minecraft minecraft = Minecraft.getInstance();
        int screenWidth = minecraft.getWindow().getGuiScaledWidth();
        int screenHeight = minecraft.getWindow().getGuiScaledHeight();
        
        // 😡 根据进度选择不同的过渡效果 😡
        if (progress < 0.5f) {
            // 😡 前半段：淡出效果 😡
            renderFadeOut(progress * 2.0f, screenWidth, screenHeight);
 馃槨
        } else {
            // 😡 后半段：淡入效果 😡
            renderFadeIn((progress - 0.5f) * 2.0f, screenWidth, screenHeight);
 馃槨
        }
        
        // 😡 添加粒子效果 😡
        renderParticleEffect(progress, screenWidth, screenHeight);
        
        // 😡 添加扭曲效果（模拟空间弯曲） 😡
        renderWarpEffect(progress, screenWidth, screenHeight);
    }
    
    /** 😡 渲染淡出效果 😡
     */
    private static void renderFadeOut(float progress, int screenWidth, int screenHeight) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();
        
        // 😡 创建渐变的黑色覆盖层 😡
        float alpha = progress * 0.8f; // 😡 最大透明度 80% 😡
 馃槨
        
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        Matrix4f matrix = new Matrix4f().identity();
        
        // 😡 渲染全屏黑色覆盖层 😡
        buffer.vertex(matrix, 0, screenHeight, 0).color(0, 0, 0, alpha).endVertex();
        buffer.vertex(matrix, screenWidth, screenHeight, 0).color(0, 0, 0, alpha).endVertex();
        buffer.vertex(matrix, screenWidth, 0, 0).color(0, 0, 0, alpha).endVertex();
        buffer.vertex(matrix, 0, 0, 0).color(0, 0, 0, alpha).endVertex();
        
        tesselator.end();
        RenderSystem.disableBlend();
    }
    
    /** 😡 渲染淡入效果 😡
     */
    private static void renderFadeIn(float progress, int screenWidth, int screenHeight) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();
        
        // 😡 创建渐变的蓝色覆盖层（太空色彩） 😡
        float alpha = (1.0f - progress) * 0.6f;
 馃槨
        
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        Matrix4f matrix = new Matrix4f().identity();
        
        // 😡 渲染全屏蓝色覆盖层 😡
        buffer.vertex(matrix, 0, screenHeight, 0).color(0.1f, 0.2f, 0.4f, alpha).endVertex();
        buffer.vertex(matrix, screenWidth, screenHeight, 0).color(0.1f, 0.2f, 0.4f, alpha).endVertex();
        buffer.vertex(matrix, screenWidth, 0, 0).color(0.1f, 0.2f, 0.4f, alpha).endVertex();
        buffer.vertex(matrix, 0, 0, 0).color(0.1f, 0.2f, 0.4f, alpha).endVertex();
        
        tesselator.end();
        RenderSystem.disableBlend();
    }
    
    /** 😡 渲染粒子效果 😡
     */
    private static void renderParticleEffect(float progress, int screenWidth, int screenHeight) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();
        
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        Matrix4f matrix = new Matrix4f().identity();
        
        // 😡 渲染星星粒子效果 😡
        int particleCount = (int) (progress * 50); // 😡 最多50个粒子 😡
 馃槨
        
        for (int i = 0; i < particleCount; i++) {
            // 😡 使用伪随机位置 😡
            float x = (float) ((i * 73 + progress * 100) % screenWidth);
 馃槨
            float y = (float) ((i * 37 + progress * 80) % screenHeight);
 馃槨
            float size = 2.0f + (float) Math.sin(progress * Math.PI + i) * 1.0f;
 馃槨
            
            float alpha = (float) Math.sin(progress * Math.PI) * 0.8f;
 馃槨
            
            // 😡 渲染小方块作为星星 😡
            buffer.vertex(matrix, x, y + size, 0).color(1.0f, 1.0f, 1.0f, alpha).endVertex();
            buffer.vertex(matrix, x + size, y + size, 0).color(1.0f, 1.0f, 1.0f, alpha).endVertex();
            buffer.vertex(matrix, x + size, y, 0).color(1.0f, 1.0f, 1.0f, alpha).endVertex();
            buffer.vertex(matrix, x, y, 0).color(1.0f, 1.0f, 1.0f, alpha).endVertex();
        }
        
        tesselator.end();
        RenderSystem.disableBlend();
    }
    
    /** 😡 渲染扭曲效果（模拟空间弯曲） 😡
     */
    private static void renderWarpEffect(float progress, int screenWidth, int screenHeight) {
        // 😡 在传送的中间阶段创建扭曲效果 😡
        if (progress > 0.3f && progress < 0.7f) {
            float warpIntensity = (float) Math.sin((progress - 0.3f) / 0.4f * Math.PI) * 0.1f;
 馃槨
            
            // 😡 这里可以添加屏幕扭曲效果 😡
            // 😡 由于复杂性，暂时使用简单的视觉提示 😡
            
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            
            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder buffer = tesselator.getBuilder();
            
            buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
            Matrix4f matrix = new Matrix4f().identity();
            
            // 😡 渲染径向渐变效果 😡
            int centerX = screenWidth / 2;
            int centerY = screenHeight / 2;
            int radius = Math.min(screenWidth, screenHeight) / 4;
            
            float alpha = warpIntensity * 0.5f;
 馃槨
            
            // 😡 简化的径向效果 😡
            buffer.vertex(matrix, centerX - radius, centerY + radius, 0).color(0.5f, 0.8f, 1.0f, 0).endVertex();
            buffer.vertex(matrix, centerX + radius, centerY + radius, 0).color(0.5f, 0.8f, 1.0f, 0).endVertex();
            buffer.vertex(matrix, centerX + radius, centerY - radius, 0).color(0.5f, 0.8f, 1.0f, 0).endVertex();
            buffer.vertex(matrix, centerX - radius, centerY - radius, 0).color(0.5f, 0.8f, 1.0f, 0).endVertex();
            
            tesselator.end();
            RenderSystem.disableBlend();
        }
    }
}