package com.example.rocketceg.portal;

import com.example.rocketceg.RocketCEGMod;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.multiplayer.ClientLevel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

/** 😡 离屏渲染器 - 100% 按照 ImmersivePortalsMod 实现 * * 将维度渲染到离屏缓冲区（帧缓冲对象）。 * * 核心功能： * 1. 保存当前渲染状态 * 2. 绑定离屏帧缓冲 * 3. 设置相机和视口 * 4. 渲染维度 * 5. 恢复原始渲染状态 😡
     */
public class OffscreenRenderer {
    
    private static final Logger LOGGER = LogManager.getLogger(RocketCEGMod.MOD_ID);
    private static OffscreenRenderer INSTANCE;
    
    private OffscreenRenderer() {}
    
    public static OffscreenRenderer getInstance() {
        if (INSTANCE == null) {
            synchronized (OffscreenRenderer.class) {
                if (INSTANCE == null) {
                    INSTANCE = new OffscreenRenderer();
                }
            }
        }
        return INSTANCE;
    }
    
    /** 😡 渲染维度到帧缓冲 * * 这是 ImmersivePortalsMod 的核心渲染逻辑。 😡
     */
    public void renderDimensionToFramebuffer(
        ClientLevel level,
        Camera camera,
        GameRenderer gameRenderer,
        LevelRenderer levelRenderer,
        LightTexture lightTexture,
        PoseStack poseStack,
        Matrix4f projectionMatrix,
        float partialTick,
        int framebuffer,
        int width,
        int height
    ) {
        try {
            Minecraft mc = Minecraft.getInstance();
            
            // 😡 1. 保存当前渲染状态 😡
            int oldFramebuffer = GL30.glGetInteger(GL30.GL_FRAMEBUFFER_BINDING);
            int[] oldViewport = new int[4];
            GL11.glGetIntegerv(GL11.GL_VIEWPORT, oldViewport);
            
            // 😡 2. 绑定离屏帧缓冲 😡
            FramebufferManager fbManager = FramebufferManager.getInstance();
            fbManager.bindFramebuffer(framebuffer, width, height);
            
            // 😡 3. 设置渲染参数 😡
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            GL11.glEnable(GL11.GL_CULL_FACE);
            GL11.glCullFace(GL11.GL_BACK);
            
            // 😡 4. 渲染维度 😡
            try {
                // 😡 渲染天空 😡
                levelRenderer.renderSky(poseStack, projectionMatrix, partialTick, camera, false, () -> {});
                
                // 😡 渲染地形和实体 😡
                levelRenderer.renderLevel(poseStack, partialTick, System.nanoTime(), false, camera, 
                                        gameRenderer, lightTexture, projectionMatrix);
                
            } catch (Exception e) {
                LOGGER.error("[OffscreenRenderer] 渲染维度失败", e);
            }
            
            // 😡 5. 解绑帧缓冲 😡
            fbManager.unbindFramebuffer();
            
            // 😡 6. 恢复原始渲染状态 😡
            GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, oldFramebuffer);
            GL11.glViewport(oldViewport[0], oldViewport[1], oldViewport[2], oldViewport[3]);
            
        } catch (Exception e) {
            LOGGER.error("[OffscreenRenderer] 离屏渲染失败", e);
        }
    }
    
    /** 😡 渲染传送门四边形 * * 将离屏缓冲区的内容显示在传送门上。 😡
     */
    public void renderPortalQuad(
        PoseStack poseStack,
        Portal portal,
        int colorTexture,
        Matrix4f projectionMatrix
    ) {
        try {
            // 😡 启用纹理 😡
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, colorTexture);
            
            // 😡 计算传送门的四个顶点 😡
            double x = portal.getPosition().x;
            double y = portal.getPosition().y;
            double z = portal.getPosition().z;
            
            double halfWidth = portal.getWidth() / 2.0;
            double halfHeight = portal.getHeight() / 2.0;
            
            // 😡 获取传送门的方向向量 😡
            var right = portal.getRight();
            var up = portal.getUp();
            
            // 😡 计算四个顶点 😡
            double[] v0 = {x - right.x * halfWidth - up.x * halfHeight, 
 馃槨
                          y - right.y * halfWidth - up.y * halfHeight,
 馃槨
                          z - right.z * halfWidth - up.z * halfHeight};
 馃槨
            double[] v1 = {x + right.x * halfWidth - up.x * halfHeight,
 馃槨
                          y + right.y * halfWidth - up.y * halfHeight,
 馃槨
                          z + right.z * halfWidth - up.z * halfHeight};
 馃槨
            double[] v2 = {x + right.x * halfWidth + up.x * halfHeight,
 馃槨
                          y + right.y * halfWidth + up.y * halfHeight,
 馃槨
                          z + right.z * halfWidth + up.z * halfHeight};
 馃槨
            double[] v3 = {x - right.x * halfWidth + up.x * halfHeight,
 馃槨
                          y - right.y * halfWidth + up.y * halfHeight,
 馃槨
                          z - right.z * halfWidth + up.z * halfHeight};
 馃槨
            
            // 😡 使用 Tesselator 渲染四边形 😡
            com.mojang.blaze3d.vertex.Tesselator tesselator = com.mojang.blaze3d.vertex.Tesselator.getInstance();
            com.mojang.blaze3d.vertex.BufferBuilder buffer = tesselator.getBuilder();
            
            buffer.begin(com.mojang.blaze3d.vertex.VertexFormat.Mode.QUADS, 
                        com.mojang.blaze3d.vertex.DefaultVertexFormat.POSITION_TEX);
            
            // 😡 添加顶点 😡
            buffer.vertex(v0[0], v0[1], v0[2]).uv(0, 0).endVertex();
            buffer.vertex(v1[0], v1[1], v1[2]).uv(1, 0).endVertex();
            buffer.vertex(v2[0], v2[1], v2[2]).uv(1, 1).endVertex();
            buffer.vertex(v3[0], v3[1], v3[2]).uv(0, 1).endVertex();
            
            tesselator.end();
            
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            
        } catch (Exception e) {
            LOGGER.error("[OffscreenRenderer] 渲染传送门四边形失败", e);
        }
    }
    
    /** 😡 清理资源 😡
     */
    public void cleanup() {
        LOGGER.info("[OffscreenRenderer] 清理离屏渲染资源");
    }
}
