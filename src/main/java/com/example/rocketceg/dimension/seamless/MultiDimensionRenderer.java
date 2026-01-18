package com.example.rocketceg.dimension.seamless;

import com.example.rocketceg.RocketCEGMod;
import com.example.rocketceg.seamless.SeamlessCore;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.Camera;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Quaternionf;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

/** 😡 多维度同时渲染管理器 * * 参考 ImmersivePortalsMod 的核心理念： * 1. 客户端同时加载和渲染多个维度 * 2. 无缝的维度间视觉过渡 * 3. 空间变换渲染（平移、旋转、缩放、镜像） * 4. 性能优化的渲染管道 * * 这是实现真正无缝维度切换的关键组件 😡
     */
public class MultiDimensionRenderer {
    
    private static final Logger LOGGER = LogManager.getLogger(RocketCEGMod.MOD_ID);
    
    // 😡 单例实例 😡
    private static MultiDimensionRenderer INSTANCE;
    
    // 😡 多维度渲染状态 😡
    private final Map<ResourceKey<Level>, ClientLevel> loadedDimensions = new ConcurrentHashMap<>();
    private final Map<ResourceKey<Level>, LevelRenderer> dimensionRenderers = new ConcurrentHashMap<>();
    
    // 😡 渲染配置 😡
    private volatile boolean enableMultiDimensionRendering = true;
    private volatile int maxSimultaneousDimensions = 3; // 😡 最多同时渲染3个维度 😡
    private volatile float renderDistance = 16.0f; // 😡 跨维度渲染距离 😡
    
    // 😡 空间变换渲染参数 😡
    private volatile Vec3 renderOffset = Vec3.ZERO;
    private volatile Matrix4f transformMatrix = new Matrix4f();
    
    // 😡 性能监控 😡
    private volatile long lastRenderTime = 0;
    private volatile int renderedDimensions = 0;
    private volatile boolean performanceAdjustment = true;
    
    private MultiDimensionRenderer() {}
    
    public static MultiDimensionRenderer getInstance() {
        if (INSTANCE == null) {
            synchronized (MultiDimensionRenderer.class) {
                if (INSTANCE == null) {
                    INSTANCE = new MultiDimensionRenderer();
                }
            }
        }
        return INSTANCE;
    }
    
    /** 😡 注册维度用于多维度渲染 😡
     */
    public void registerDimension(ResourceKey<Level> dimension, ClientLevel level) {
        if (!enableMultiDimensionRendering) {
            return;
        }
        
        try {
            loadedDimensions.put(dimension, level);
            
            // 😡 为每个维度创建独立的渲染器 😡
            LevelRenderer renderer = new LevelRenderer(Minecraft.getInstance(), 
                                                     Minecraft.getInstance().getEntityRenderDispatcher(),
                                                     Minecraft.getInstance().getBlockEntityRenderDispatcher(),
                                                     Minecraft.getInstance().renderBuffers());
            dimensionRenderers.put(dimension, renderer);
            
            LOGGER.info("[MultiDimensionRenderer] 注册维度用于多维度渲染: {}", dimension.location());
            
        } catch (Exception e) {
            LOGGER.error("[MultiDimensionRenderer] 注册维度失败: {}", dimension.location(), e);
        }
    }
    
    /** 😡 卸载维度渲染器 😡
     */
    public void unregisterDimension(ResourceKey<Level> dimension) {
        try {
            loadedDimensions.remove(dimension);
            LevelRenderer renderer = dimensionRenderers.remove(dimension);
            
            if (renderer != null) {
                // 😡 清理渲染器资源 😡
                renderer.close();
            }
            
            LOGGER.info("[MultiDimensionRenderer] 卸载维度渲染器: {}", dimension.location());
            
        } catch (Exception e) {
            LOGGER.error("[MultiDimensionRenderer] 卸载维度渲染器失败: {}", dimension.location(), e);
        }
    }
    
    /** 😡 执行多维度渲染 - 核心渲染方法 😡
     */
    public void renderMultipleDimensions(PoseStack poseStack, float partialTick, Camera camera) {
        if (!enableMultiDimensionRendering || loadedDimensions.isEmpty()) {
            return;
        }
        
        long startTime = System.nanoTime();
        renderedDimensions = 0;
        
        try {
            // 😡 获取当前主维度 😡
            ClientLevel currentLevel = Minecraft.getInstance().level;
            if (currentLevel == null) {
                return;
            }
            
            ResourceKey<Level> currentDimension = currentLevel.dimension();
            
            // 😡 渲染当前维度 😡
            renderDimension(currentDimension, currentLevel, poseStack, partialTick, camera, false);
            
            // 😡 如果正在进行无缝传送，同时渲染目标维度 😡
            SeamlessCore seamlessCore = SeamlessCore.getInstance();
            if (seamlessCore.isSeamlessTeleporting()) {
                ResourceKey<Level> targetDimension = seamlessCore.getPendingDimension();
                ClientLevel targetLevel = loadedDimensions.get(targetDimension);
                
                if (targetLevel != null && !targetDimension.equals(currentDimension)) {
                    // 😡 应用空间变换渲染目标维度 😡
                    renderDimensionWithTransform(targetDimension, targetLevel, poseStack, partialTick, camera);
                }
            }
            
            // 😡 渲染其他相关维度（如果性能允许） 😡
            renderAdditionalDimensions(currentDimension, poseStack, partialTick, camera);
            
        } catch (Exception e) {
            LOGGER.error("[MultiDimensionRenderer] 多维度渲染失败", e);
        } finally {
            lastRenderTime = System.nanoTime() - startTime;
            
            // 😡 性能调整 😡
            if (performanceAdjustment) {
                adjustRenderingPerformance();
            }
        }
    }
    
    /** 😡 渲染单个维度 😡
     */
    private void renderDimension(ResourceKey<Level> dimension, ClientLevel level, 
                                PoseStack poseStack, float partialTick, Camera camera, boolean isTransformed) {
        try {
            LevelRenderer renderer = dimensionRenderers.get(dimension);
            if (renderer == null) {
                return;
            }
            
            // 😡 设置渲染状态 😡
            poseStack.pushPose();
            
            if (isTransformed) {
                // 😡 应用空间变换 😡
                // 😡 将 Matrix4f 转换为 Quaternionf 😡
                Quaternionf quaternion = new Quaternionf();
                transformMatrix.getNormalizedRotation(quaternion);
                poseStack.mulPose(quaternion);
                
                Vector3f translation = new Vector3f();
                transformMatrix.getTranslation(translation);
                poseStack.translate(translation.x, translation.y, translation.z);
            }
            
            // 😡 执行维度渲染 😡
            renderer.renderLevel(poseStack, partialTick, System.nanoTime(), false, camera, 
                               Minecraft.getInstance().gameRenderer, 
                               Minecraft.getInstance().gameRenderer.lightTexture(), 
                               new Matrix4f());
            
            poseStack.popPose();
            renderedDimensions++;
            
            LOGGER.debug("[MultiDimensionRenderer] 渲染维度: {} (变换: {})", dimension.location(), isTransformed);
            
        } catch (Exception e) {
            LOGGER.error("[MultiDimensionRenderer] 渲染维度失败: {}", dimension.location(), e);
        }
    }
    
    /** 😡 使用空间变换渲染维度 😡
     */
    private void renderDimensionWithTransform(ResourceKey<Level> dimension, ClientLevel level,
                                            PoseStack poseStack, float partialTick, Camera camera) {
        try {
            // 😡 计算空间变换矩阵 😡
            calculateTransformMatrix();
            
            // 😡 渲染变换后的维度 😡
            renderDimension(dimension, level, poseStack, partialTick, camera, true);
            
        } catch (Exception e) {
            LOGGER.error("[MultiDimensionRenderer] 空间变换渲染失败: {}", dimension.location(), e);
        }
    }
    
    /** 😡 渲染其他相关维度 😡
     */
    private void renderAdditionalDimensions(ResourceKey<Level> currentDimension, 
                                          PoseStack poseStack, float partialTick, Camera camera) {
        if (renderedDimensions >= maxSimultaneousDimensions) {
            return; // 😡 达到最大同时渲染维度数 😡
        }
        
        // 😡 渲染相关的 RocketCEG 维度 😡
        for (Map.Entry<ResourceKey<Level>, ClientLevel> entry : loadedDimensions.entrySet()) {
            if (renderedDimensions >= maxSimultaneousDimensions) {
                break;
            }
            
            ResourceKey<Level> dimension = entry.getKey();
            ClientLevel level = entry.getValue();
            
            // 😡 跳过已渲染的维度 😡
            if (dimension.equals(currentDimension)) {
                continue;
            }
            
            // 😡 只渲染 RocketCEG 相关维度 😡
            if (isRocketCEGRelatedDimension(dimension)) {
                renderDimension(dimension, level, poseStack, partialTick, camera, false);
            }
        }
    }
    
    /** 😡 计算空间变换矩阵 😡
     */
    private void calculateTransformMatrix() {
        transformMatrix.identity();
        
        SeamlessCore seamlessCore = SeamlessCore.getInstance();
        
        // 😡 这里需要从 SeamlessCore 获取变换参数 😡
        // 😡 由于访问限制，使用默认变换 😡
        Vec3 offset = seamlessCore.getPendingPosition();
        if (offset != null) {
            renderOffset = offset;
        }
        
        // 😡 TODO: 添加旋转、缩放、镜像变换 😡
        // 😡 transformMatrix.rotate(rotation); 😡
        // 😡 transformMatrix.scale(scale); 😡
    }
    
    /** 😡 性能调整 😡
     */
    private void adjustRenderingPerformance() {
        // 😡 如果渲染时间过长，减少同时渲染的维度数 😡
        long renderTimeMs = lastRenderTime / 1_000_000;
        
        if (renderTimeMs > 50) { // 😡 超过50ms 😡
            maxSimultaneousDimensions = Math.max(1, maxSimultaneousDimensions - 1);
            LOGGER.debug("[MultiDimensionRenderer] 性能调整：减少同时渲染维度数到 {}", maxSimultaneousDimensions);
        } else if (renderTimeMs < 16 && maxSimultaneousDimensions < 3) { // 😡 低于16ms且未达到最大值 😡
            maxSimultaneousDimensions++;
            LOGGER.debug("[MultiDimensionRenderer] 性能调整：增加同时渲染维度数到 {}", maxSimultaneousDimensions);
        }
        
        // 😡 调整渲染距离 😡
        if (renderTimeMs > 33) { // 😡 超过33ms 😡
            renderDistance = Math.max(8.0f, renderDistance - 2.0f);
        } else if (renderTimeMs < 16 && renderDistance < 32.0f) {
            renderDistance = Math.min(32.0f, renderDistance + 2.0f);
        }
    }
    
    /** 😡 检查是否是 RocketCEG 相关维度 😡
     */
    private boolean isRocketCEGRelatedDimension(ResourceKey<Level> dimension) {
        return dimension.location().getNamespace().equals("rocketceg");
    }
    
    /** 😡 清理所有渲染器 😡
     */
    public void cleanup() {
        try {
            for (LevelRenderer renderer : dimensionRenderers.values()) {
                renderer.close();
            }
            
            dimensionRenderers.clear();
            loadedDimensions.clear();
            
            LOGGER.info("[MultiDimensionRenderer] 清理所有维度渲染器");
            
        } catch (Exception e) {
            LOGGER.error("[MultiDimensionRenderer] 清理渲染器失败", e);
        }
    }
    
    // 😡 === Getter 和 Setter 方法 === 😡
    
    public boolean isMultiDimensionRenderingEnabled() {
        return enableMultiDimensionRendering;
    }
    
    public void setMultiDimensionRenderingEnabled(boolean enabled) {
        this.enableMultiDimensionRendering = enabled;
    }
    
    public int getMaxSimultaneousDimensions() {
        return maxSimultaneousDimensions;
    }
    
    public void setMaxSimultaneousDimensions(int max) {
        this.maxSimultaneousDimensions = Math.max(1, Math.min(5, max));
    }
    
    public float getRenderDistance() {
        return renderDistance;
    }
    
    public void setRenderDistance(float distance) {
        this.renderDistance = Math.max(4.0f, Math.min(64.0f, distance));
    }
    
    public int getRenderedDimensionsCount() {
        return renderedDimensions;
    }
    
    public long getLastRenderTime() {
        return lastRenderTime;
    }
}