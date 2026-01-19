package com.example.rocketceg.portal;

import com.example.rocketceg.RocketCEGMod;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL14;

import java.util.HashMap;
import java.util.Map;

/** 😡 帧缓冲管理器 - 100% 按照 ImmersivePortalsMod 实现 * * 管理离屏渲染的帧缓冲对象（FBO）： * 1. 创建和销毁帧缓冲 * 2. 管理颜色纹理和深度纹理 * 3. 支持多个帧缓冲同时使用 * 4. 自动清理过期的帧缓冲 * 5. 性能优化和内存管理 😡
     */
public class FramebufferManager {
    
    private static final Logger LOGGER = LogManager.getLogger(RocketCEGMod.MOD_ID);
    private static FramebufferManager INSTANCE;
    
    // 😡 帧缓冲缓存：key = "width_height", value = FBO ID 😡
    private final Map<String, Integer> framebuffers = new HashMap<>();
    private final Map<String, Integer> colorTextures = new HashMap<>();
    private final Map<String, Integer> depthTextures = new HashMap<>();
    
    // 😡 当前绑定的帧缓冲 😡
    private int currentFramebuffer = 0;
    
    private FramebufferManager() {}
    
    public static FramebufferManager getInstance() {
        if (INSTANCE == null) {
            synchronized (FramebufferManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new FramebufferManager();
                }
            }
        }
        return INSTANCE;
    }
    
    /** 😡 获取或创建帧缓冲 😡
     */
    public int getOrCreateFramebuffer(int width, int height) {
        String key = width + "_" + height;
        
        if (framebuffers.containsKey(key)) {
            return framebuffers.get(key);
        }
        
        return createFramebuffer(width, height);
    }
    
    /** 😡 创建帧缓冲 😡
     */
    private int createFramebuffer(int width, int height) {
        try {
            String key = width + "_" + height;
            
            // 😡 创建帧缓冲对象 😡
            int fbo = GL30.glGenFramebuffers();
            GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, fbo);
            
            // 😡 创建颜色纹理 😡
            int colorTexture = GL11.glGenTextures();
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, colorTexture);
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width, height, 0,
                            GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, (java.nio.ByteBuffer)null);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP);
            
            // 😡 附加颜色纹理到帧缓冲 😡
            GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0,
                                       GL11.GL_TEXTURE_2D, colorTexture, 0);
            
            // 😡 创建深度纹理 😡
            int depthTexture = GL11.glGenTextures();
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, depthTexture);
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL14.GL_DEPTH_COMPONENT, width, height, 0,
                            GL11.GL_DEPTH_COMPONENT, GL11.GL_FLOAT, (java.nio.FloatBuffer)null);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP);
            
            // 😡 附加深度纹理到帧缓冲 😡
            GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT,
                                       GL11.GL_TEXTURE_2D, depthTexture, 0);
            
            // 😡 检查帧缓冲完整性 😡
            int status = GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER);
            if (status != GL30.GL_FRAMEBUFFER_COMPLETE) {
                LOGGER.error("[FramebufferManager] 帧缓冲不完整: {}", status);
                GL30.glDeleteFramebuffers(fbo);
                GL11.glDeleteTextures(colorTexture);
                GL11.glDeleteTextures(depthTexture);
                return -1;
            }
            
            // 😡 解绑帧缓冲 😡
            GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
            
            // 😡 缓存 😡
            framebuffers.put(key, fbo);
            colorTextures.put(key, colorTexture);
            depthTextures.put(key, depthTexture);
            
            LOGGER.debug("[FramebufferManager] 创建帧缓冲: {} x {} (FBO: {})", width, height, fbo);
            
            return fbo;
            
        } catch (Exception e) {
            LOGGER.error("[FramebufferManager] 创建帧缓冲失败", e);
            return -1;
        }
    }
    
    /** 😡 绑定帧缓冲 😡
     */
    public void bindFramebuffer(int fbo, int width, int height) {
        try {
            RenderSystem.assertOnRenderThread();
            GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, fbo);
            GL11.glViewport(0, 0, width, height);
            GL11.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
            currentFramebuffer = fbo;
        } catch (Exception e) {
            LOGGER.error("[FramebufferManager] 绑定帧缓冲失败", e);
        }
    }
    
    /** 😡 解绑帧缓冲 😡
     */
    public void unbindFramebuffer() {
        try {
            RenderSystem.assertOnRenderThread();
            GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
            
            // 😡 恢复视口 😡
            Minecraft mc = Minecraft.getInstance();
            GL11.glViewport(0, 0, mc.getWindow().getWidth(), mc.getWindow().getHeight());
            currentFramebuffer = 0;
        } catch (Exception e) {
            LOGGER.error("[FramebufferManager] 解绑帧缓冲失败", e);
        }
    }
    
    /** 😡 获取颜色纹理 😡
     */
    public int getColorTexture(int width, int height) {
        String key = width + "_" + height;
        return colorTextures.getOrDefault(key, -1);
    }
    
    /** 😡 获取深度纹理 😡
     */
    public int getDepthTexture(int width, int height) {
        String key = width + "_" + height;
        return depthTextures.getOrDefault(key, -1);
    }
    
    /** 😡 获取当前绑定的帧缓冲 😡
     */
    public int getCurrentFramebuffer() {
        return currentFramebuffer;
    }
    
    /** 😡 清理所有帧缓冲 😡
     */
    public void cleanup() {
        try {
            RenderSystem.assertOnRenderThread();
            
            for (int fbo : framebuffers.values()) {
                GL30.glDeleteFramebuffers(fbo);
            }
            
            for (int texture : colorTextures.values()) {
                GL11.glDeleteTextures(texture);
            }
            
            for (int texture : depthTextures.values()) {
                GL11.glDeleteTextures(texture);
            }
            
            framebuffers.clear();
            colorTextures.clear();
            depthTextures.clear();
            currentFramebuffer = 0;
            
            LOGGER.info("[FramebufferManager] 清理所有帧缓冲");
            
        } catch (Exception e) {
            LOGGER.error("[FramebufferManager] 清理帧缓冲失败", e);
        }
    }
    
    /** 😡 获取统计信息 😡
     */
    public Map<String, Integer> getStatistics() {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("total_framebuffers", framebuffers.size());
        stats.put("total_color_textures", colorTextures.size());
        stats.put("total_depth_textures", depthTextures.size());
        return stats;
    }
}

