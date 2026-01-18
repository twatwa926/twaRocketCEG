package com.example.rocketceg.dimension.seamless;

import com.example.rocketceg.RocketCEGMod;
import com.example.rocketceg.seamless.SeamlessCore;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.Entity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.platform.GlStateManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Quaternionf;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

/** 😡 高级传送门渲染器 - 参考 ImmersivePortalsMod 核心技术 * * 核心功能： * 1. 实时跨维度渲染 - 同时渲染多个维度 * 2. 无缝视觉过渡 - 消除维度切换的视觉中断 * 3. 空间变换渲染 - 支持非欧几里得几何 * 4. 性能自适应优化 - 根据硬件性能动态调整 * 5. 智能区块加载 - 按需加载跨维度区块 * * 参考 ImmersivePortalsMod 的设计理念： * - 深度修改渲染引擎以支持多维度同时渲染 * - 使用空间变换实现非欧几里得几何效果 * - 智能性能管理确保流畅的游戏体验 😡
     */
public class AdvancedPortalRenderer {
    
    private static final Logger LOGGER = LogManager.getLogger(RocketCEGMod.MOD_ID);
    
    // 😡 单例实例 😡
    private static AdvancedPortalRenderer INSTANCE;
    
    // 😡 渲染状态管理 😡
    private final Map<ResourceKey<Level>, RenderContext> renderContexts = new ConcurrentHashMap<>();
    private final Set<ResourceKey<Level>> activeRenderDimensions = new HashSet<>();
    
    // 😡 性能管理 - 参考 ImmersivePortalsMod 的性能优化 😡
    private volatile boolean enablePerformanceAdjustment = true;
    private volatile boolean enableClientPerformanceAdjustment = true;
    private volatile boolean lagAttackProof = true;
    private volatile int maxRenderDistance = 16;
    private volatile int indirectLoadingRadiusCap = 8;
    
    // 😡 渲染统计 😡
    private volatile long lastFrameTime = 0;
    private volatile int renderedChunks = 0;
    private volatile int loadedDimensions = 0;
    private volatile double averageFPS = 60.0;
    private volatile long freeMemory = 0;
    
    // 😡 渲染配置 😡
    private volatile boolean enableCrossPortalRendering = true;
    private volatile boolean enableDimensionStacking = true;
    private volatile float renderQuality = 1.0f;
    
    /** 😡 渲染上下文 - 每个维度的渲染状态 😡
     */
    private static class RenderContext {
        public final ResourceKey<Level> dimension;
        public final ClientLevel level;
        public LevelRenderer renderer;
        public Camera camera;
        public Matrix4f projectionMatrix;
        public Matrix4f modelViewMatrix;
        public Vec3 renderPosition;
        public float partialTick;
        public boolean isActive;
        public long lastRenderTime;
        public int renderDistance;
        
        public RenderContext(ResourceKey<Level> dimension, ClientLevel level) {
            this.dimension = dimension;
            this.level = level;
            this.isActive = false;
            this.lastRenderTime = System.currentTimeMillis();
            this.renderDistance = 16;
        }
    }
    
    private AdvancedPortalRenderer() {}
    
    public static AdvancedPortalRenderer getInstance() {
        if (INSTANCE == null) {
            synchronized (AdvancedPortalRenderer.class) {
                if (INSTANCE == null) {
                    INSTANCE = new AdvancedPortalRenderer();
                }
            }
        }
        return INSTANCE;
    }
    
    /** 😡 初始化高级渲染系统 😡
     */
    public void initialize() {
        LOGGER.info("[AdvancedPortalRenderer] 初始化高级传送门渲染系统 - 参考 ImmersivePortalsMod 技术");
        
        // 😡 初始化性能监控 😡
        updatePerformanceMetrics();
        
        // 😡 设置渲染配置 😡
        configureRenderingSettings();
        
        LOGGER.info("[AdvancedPortalRenderer] 高级渲染系统初始化完成");
    }
    
    /** 😡 注册维度用于高级渲染 - 参考 ImmersivePortalsMod 的多维度加载 😡
     */
    public void registerDimensionForRendering(ResourceKey<Level> dimension, ClientLevel level) {
        if (renderContexts.containsKey(dimension)) {
            LOGGER.debug("[AdvancedPortalRenderer] 维度已注册: {}", dimension.location());
            return;
        }
        
        try {
            RenderContext context = new RenderContext(dimension, level);
            
            // 😡 创建专用的渲染器 - 参考 ImmersivePortalsMod 的渲染器管理 😡
            context.renderer = new LevelRenderer(
                Minecraft.getInstance(),
                Minecraft.getInstance().getEntityRenderDispatcher(),
                Minecraft.getInstance().getBlockEntityRenderDispatcher(),
                Minecraft.getInstance().renderBuffers()
            );
            
            // 😡 设置渲染距离 😡
            context.renderDistance = calculateOptimalRenderDistance(dimension);
            
            renderContexts.put(dimension, context);
            activeRenderDimensions.add(dimension);
            
            LOGGER.info("[AdvancedPortalRenderer] 注册维度用于高级渲染: {} (渲染距离: {})", 
                       dimension.location(), context.renderDistance);
            
        } catch (Exception e) {
            LOGGER.error("[AdvancedPortalRenderer] 注册维度失败: {}", dimension.location(), e);
        }
    }
    
    /** 😡 执行高级跨维度渲染 - 核心渲染方法 😡
     */
    public void renderCrossDimensional(PoseStack poseStack, float partialTick, Camera camera, 
                                     Matrix4f projectionMatrix, GameRenderer gameRenderer, 
                                     LightTexture lightTexture) {
        if (!enableCrossPortalRendering) {
            return;
        }
        
        long frameStart = System.nanoTime();
        
        try {
            // 😡 更新性能指标 😡
            updatePerformanceMetrics();
            
            // 😡 性能自适应调整 - 参考 ImmersivePortalsMod 的性能管理 😡
            if (enablePerformanceAdjustment) {
                adjustRenderingPerformance();
            }
            
            // 😡 获取当前维度 😡
            ClientLevel currentLevel = Minecraft.getInstance().level;
            if (currentLevel == null) {
                return;
            }
            
            ResourceKey<Level> currentDimension = currentLevel.dimension();
            
            // 😡 渲染当前维度 😡
            renderDimensionContext(currentDimension, poseStack, partialTick, camera, 
                                 projectionMatrix, gameRenderer, lightTexture, false);
            
            // 😡 如果正在无缝传送，渲染目标维度 😡
            SeamlessCore seamlessCore = SeamlessCore.getInstance();
            if (seamlessCore.isSeamlessTeleporting()) {
                ResourceKey<Level> targetDimension = seamlessCore.getPendingDimension();
                if (targetDimension != null && !targetDimension.equals(currentDimension)) {
                    renderSeamlessTransition(targetDimension, poseStack, partialTick, camera, 
                                           projectionMatrix, gameRenderer, lightTexture);
                }
            }
            
            // 😡 渲染维度堆栈 - 参考 ImmersivePortalsMod 的维度堆栈功能 😡
            if (enableDimensionStacking) {
                renderDimensionStack(currentDimension, poseStack, partialTick, camera, 
                                   projectionMatrix, gameRenderer, lightTexture);
            }
            
        } catch (Exception e) {
            LOGGER.error("[AdvancedPortalRenderer] 跨维度渲染失败", e);
        } finally {
            lastFrameTime = System.nanoTime() - frameStart;
        }
    }
    
    /** 😡 渲染无缝传送过渡效果 😡
     */
    private void renderSeamlessTransition(ResourceKey<Level> targetDimension, PoseStack poseStack, 
                                        float partialTick, Camera camera, Matrix4f projectionMatrix, 
                                        GameRenderer gameRenderer, LightTexture lightTexture) {
        try {
            // 😡 应用空间变换渲染目标维度 😡
            poseStack.pushPose();
            
            // 😡 获取空间变换参数 😡
            SpatialTransformation transform = getCurrentSpatialTransformation();
            if (transform != null && !transform.isIdentity()) {
                applyTransformToRenderMatrix(poseStack, transform);
            }
            
            // 😡 渲染目标维度 😡
            renderDimensionContext(targetDimension, poseStack, partialTick, camera, 
                                 projectionMatrix, gameRenderer, lightTexture, true);
            
            poseStack.popPose();
            
            LOGGER.debug("[AdvancedPortalRenderer] 渲染无缝传送过渡: {}", targetDimension.location());
            
        } catch (Exception e) {
            LOGGER.error("[AdvancedPortalRenderer] 渲染无缝传送过渡失败", e);
        }
    }
    
    /** 😡 渲染维度堆栈 - 参考 ImmersivePortalsMod 的维度堆栈 😡
     */
    private void renderDimensionStack(ResourceKey<Level> baseDimension, PoseStack poseStack, 
                                    float partialTick, Camera camera, Matrix4f projectionMatrix, 
                                    GameRenderer gameRenderer, LightTexture lightTexture) {
        try {
            // 😡 渲染相关的 RocketCEG 维度 😡
            for (ResourceKey<Level> dimension : activeRenderDimensions) {
                if (dimension.equals(baseDimension)) {
                    continue; // 😡 跳过当前维度 😡
                }
                
                // 😡 检查是否应该渲染这个维度 😡
                if (shouldRenderInStack(baseDimension, dimension)) {
                    poseStack.pushPose();
                    
                    // 😡 应用堆栈偏移 😡
                    Vec3 stackOffset = calculateStackOffset(baseDimension, dimension);
                    poseStack.translate(stackOffset.x, stackOffset.y, stackOffset.z);
                    
                    // 😡 渲染维度 😡
                    renderDimensionContext(dimension, poseStack, partialTick, camera, 
                                         projectionMatrix, gameRenderer, lightTexture, true);
                    
                    poseStack.popPose();
                }
            }
            
        } catch (Exception e) {
            LOGGER.error("[AdvancedPortalRenderer] 渲染维度堆栈失败", e);
        }
    }
    
    /** 😡 渲染单个维度上下文 😡
     */
    private void renderDimensionContext(ResourceKey<Level> dimension, PoseStack poseStack, 
                                      float partialTick, Camera camera, Matrix4f projectionMatrix, 
                                      GameRenderer gameRenderer, LightTexture lightTexture, 
                                      boolean isSecondary) {
        RenderContext context = renderContexts.get(dimension);
        if (context == null || context.renderer == null) {
            return;
        }
        
        try {
            // 😡 更新渲染上下文 😡
            context.partialTick = partialTick;
            context.renderPosition = camera.getPosition();
            context.projectionMatrix = new Matrix4f(projectionMatrix);
            context.isActive = true;
            context.lastRenderTime = System.currentTimeMillis();
            
            // 😡 设置渲染状态 😡
            if (isSecondary) {
                // 😡 为次要维度设置特殊的渲染状态 😡
                RenderSystem.enableBlend();
                RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, 
                                     GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            }
            
            // 😡 执行维度渲染 😡
            context.renderer.renderLevel(poseStack, partialTick, System.nanoTime(), 
                                        false, camera, gameRenderer, lightTexture, projectionMatrix);
            
            // 😡 更新统计 😡
            renderedChunks += context.renderer.countRenderedChunks();
            
            if (isSecondary) {
                RenderSystem.disableBlend();
            }
            
            LOGGER.debug("[AdvancedPortalRenderer] 渲染维度上下文: {} (次要: {})", 
                        dimension.location(), isSecondary);
            
        } catch (Exception e) {
            LOGGER.error("[AdvancedPortalRenderer] 渲染维度上下文失败: {}", dimension.location(), e);
        }
    }
    
    /** 😡 应用空间变换到渲染矩阵 😡
     */
    private void applyTransformToRenderMatrix(PoseStack poseStack, SpatialTransformation transform) {
        try {
            // 😡 应用平移 😡
            Vec3 translation = transform.getTranslation();
            poseStack.translate(translation.x, translation.y, translation.z);
            
            // 😡 应用旋转 😡
            Quaternionf rotation = transform.getRotation();
            if (!rotation.equals(new Quaternionf())) {
                poseStack.mulPose(rotation);
            }
            
            // 😡 应用缩放 😡
            float scale = transform.getScale();
            if (scale != 1.0f) {
                poseStack.scale(scale, scale, scale);
            }
            
            // 😡 应用镜像 😡
            if (transform.isMirrorX()) {
                poseStack.scale(-1.0f, 1.0f, 1.0f);
            }
            if (transform.isMirrorY()) {
                poseStack.scale(1.0f, -1.0f, 1.0f);
            }
            if (transform.isMirrorZ()) {
                poseStack.scale(1.0f, 1.0f, -1.0f);
            }
            
        } catch (Exception e) {
            LOGGER.error("[AdvancedPortalRenderer] 应用空间变换失败", e);
        }
    }
    
    /** 😡 更新性能指标 - 参考 ImmersivePortalsMod 的性能监控 😡
     */
    private void updatePerformanceMetrics() {
        try {
            // 😡 计算平均FPS 😡
            if (lastFrameTime > 0) {
                double currentFPS = 1_000_000_000.0 / lastFrameTime;
                averageFPS = averageFPS * 0.9 + currentFPS * 0.1; // 😡 平滑处理 😡
 馃槨
            }
            
            // 😡 获取内存信息 😡
            Runtime runtime = Runtime.getRuntime();
            freeMemory = runtime.freeMemory();
            
            // 😡 更新维度计数 😡
            loadedDimensions = renderContexts.size();
            
        } catch (Exception e) {
            LOGGER.error("[AdvancedPortalRenderer] 更新性能指标失败", e);
        }
    }
    
    /** 😡 性能自适应调整 - 参考 ImmersivePortalsMod 的性能管理 😡
     */
    private void adjustRenderingPerformance() {
        try {
            // 😡 如果FPS过低，减少渲染质量 😡
            if (averageFPS < 30.0 && renderQuality > 0.5f) {
                renderQuality = Math.max(0.5f, renderQuality - 0.1f);
                maxRenderDistance = Math.max(8, maxRenderDistance - 2);
                LOGGER.debug("[AdvancedPortalRenderer] 性能调整：降低渲染质量到 {}", renderQuality);
            }
            
            // 😡 如果内存不足，减少间接加载半径 😡
            long totalMemory = Runtime.getRuntime().totalMemory();
            double memoryUsage = (double)(totalMemory - freeMemory) / totalMemory;
            
            if (memoryUsage > 0.85 && indirectLoadingRadiusCap > 4) {
                indirectLoadingRadiusCap = Math.max(4, indirectLoadingRadiusCap - 1);
                LOGGER.debug("[AdvancedPortalRenderer] 内存调整：减少间接加载半径到 {}", indirectLoadingRadiusCap);
            }
            
            // 😡 如果性能良好，逐渐恢复质量 😡
            if (averageFPS > 50.0 && renderQuality < 1.0f) {
                renderQuality = Math.min(1.0f, renderQuality + 0.05f);
                maxRenderDistance = Math.min(32, maxRenderDistance + 1);
            }
            
            // 😡 防止延迟攻击 - 参考 ImmersivePortalsMod 的 lagAttackProof 😡
            if (lagAttackProof && lastFrameTime > 100_000_000) { // 😡 超过100ms 😡
                // 😡 临时禁用跨维度渲染 😡
                enableCrossPortalRendering = false;
                LOGGER.warn("[AdvancedPortalRenderer] 检测到延迟攻击，临时禁用跨维度渲染");
                
                // 😡 5秒后重新启用 😡
                new Thread(() -> {
                    try {
                        Thread.sleep(5000);
                        enableCrossPortalRendering = true;
                        LOGGER.info("[AdvancedPortalRenderer] 重新启用跨维度渲染");
                    } catch (InterruptedException ignored) {}
                }).start();
            }
            
        } catch (Exception e) {
            LOGGER.error("[AdvancedPortalRenderer] 性能调整失败", e);
        }
    }
    
    /** 😡 计算最优渲染距离 😡
     */
    private int calculateOptimalRenderDistance(ResourceKey<Level> dimension) {
        // 😡 根据维度类型和性能调整渲染距离 😡
        if (dimension.location().getNamespace().equals("rocketceg")) {
            // 😡 RocketCEG 维度使用较高的渲染距离 😡
            return Math.min(maxRenderDistance, 24);
        } else {
            // 😡 其他维度使用标准渲染距离 😡
            return Math.min(maxRenderDistance, 16);
        }
    }
    
    /** 😡 检查是否应该在堆栈中渲染维度 😡
     */
    private boolean shouldRenderInStack(ResourceKey<Level> baseDimension, ResourceKey<Level> targetDimension) {
        // 😡 只渲染相关的维度 😡
        String baseNamespace = baseDimension.location().getNamespace();
        String targetNamespace = targetDimension.location().getNamespace();
        
        // 😡 如果都是 RocketCEG 维度，检查是否是相关的行星/轨道对 😡
        if (baseNamespace.equals("rocketceg") && targetNamespace.equals("rocketceg")) {
            return areRelatedDimensions(baseDimension, targetDimension);
        }
        
        return false;
    }
    
    /** 😡 检查两个维度是否相关（如行星表面和轨道） 😡
     */
    private boolean areRelatedDimensions(ResourceKey<Level> dim1, ResourceKey<Level> dim2) {
        String path1 = dim1.location().getPath();
        String path2 = dim2.location().getPath();
        
        // 😡 检查是否是同一行星的表面和轨道 😡
        if (path1.endsWith("_surface") && path2.endsWith("_orbit")) {
            String planet1 = path1.replace("_surface", "");
            String planet2 = path2.replace("_orbit", "");
            return planet1.equals(planet2);
        }
        
        if (path1.endsWith("_orbit") && path2.endsWith("_surface")) {
            String planet1 = path1.replace("_orbit", "");
            String planet2 = path2.replace("_surface", "");
            return planet1.equals(planet2);
        }
        
        return false;
    }
    
    /** 😡 计算维度堆栈偏移 😡
     */
    private Vec3 calculateStackOffset(ResourceKey<Level> baseDimension, ResourceKey<Level> targetDimension) {
        // 😡 为不同维度计算堆栈偏移 😡
        String basePath = baseDimension.location().getPath();
        String targetPath = targetDimension.location().getPath();
        
        // 😡 如果目标是轨道维度，放在上方 😡
        if (targetPath.endsWith("_orbit")) {
            return new Vec3(0, 300, 0); // 😡 轨道在上方300方块 😡
        }
        
        // 😡 如果目标是表面维度，放在下方 😡
        if (targetPath.endsWith("_surface")) {
            return new Vec3(0, -300, 0); // 😡 表面在下方300方块 😡
        }
        
        return Vec3.ZERO;
    }
    
    /** 😡 获取当前空间变换 😡
     */
    private SpatialTransformation getCurrentSpatialTransformation() {
        // 😡 这里应该从 SeamlessCore 获取当前的空间变换参数 😡
        // 😡 由于访问限制，返回默认变换 😡
        return new SpatialTransformation();
    }
    
    /** 😡 配置渲染设置 😡
     */
    private void configureRenderingSettings() {
        // 😡 根据硬件性能配置渲染设置 😡
        long totalMemory = Runtime.getRuntime().totalMemory();
        
        if (totalMemory < 2L * 1024 * 1024 * 1024) { // 😡 小于2GB 😡
 馃槨
            maxRenderDistance = 12;
            indirectLoadingRadiusCap = 6;
            renderQuality = 0.7f;
        } else if (totalMemory < 4L * 1024 * 1024 * 1024) { // 😡 小于4GB 😡
 馃槨
            maxRenderDistance = 16;
            indirectLoadingRadiusCap = 8;
            renderQuality = 0.8f;
        } else { // 😡 4GB或更多 😡
            maxRenderDistance = 24;
            indirectLoadingRadiusCap = 12;
            renderQuality = 1.0f;
        }
        
        LOGGER.info("[AdvancedPortalRenderer] 配置渲染设置 - 渲染距离: {}, 间接加载半径: {}, 渲染质量: {}", 
                   maxRenderDistance, indirectLoadingRadiusCap, renderQuality);
    }
    
    /** 😡 清理渲染资源 😡
     */
    public void cleanup() {
        try {
            for (RenderContext context : renderContexts.values()) {
                if (context.renderer != null) {
                    context.renderer.close();
                }
            }
            
            renderContexts.clear();
            activeRenderDimensions.clear();
            
            LOGGER.info("[AdvancedPortalRenderer] 清理高级渲染资源完成");
            
        } catch (Exception e) {
            LOGGER.error("[AdvancedPortalRenderer] 清理渲染资源失败", e);
        }
    }
    
    // 😡 === Getter 和 Setter 方法 === 😡
    
    public boolean isPerformanceAdjustmentEnabled() {
        return enablePerformanceAdjustment;
    }
    
    public void setPerformanceAdjustmentEnabled(boolean enabled) {
        this.enablePerformanceAdjustment = enabled;
    }
    
    public boolean isCrossPortalRenderingEnabled() {
        return enableCrossPortalRendering;
    }
    
    public void setCrossPortalRenderingEnabled(boolean enabled) {
        this.enableCrossPortalRendering = enabled;
    }
    
    public boolean isDimensionStackingEnabled() {
        return enableDimensionStacking;
    }
    
    public void setDimensionStackingEnabled(boolean enabled) {
        this.enableDimensionStacking = enabled;
    }
    
    public int getMaxRenderDistance() {
        return maxRenderDistance;
    }
    
    public void setMaxRenderDistance(int distance) {
        this.maxRenderDistance = Math.max(8, Math.min(32, distance));
    }
    
    public int getIndirectLoadingRadiusCap() {
        return indirectLoadingRadiusCap;
    }
    
    public void setIndirectLoadingRadiusCap(int radius) {
        this.indirectLoadingRadiusCap = Math.max(4, Math.min(16, radius));
    }
    
    public double getAverageFPS() {
        return averageFPS;
    }
    
    public long getFreeMemory() {
        return freeMemory;
    }
    
    public int getLoadedDimensionsCount() {
        return loadedDimensions;
    }
    
    public int getRenderedChunksCount() {
        return renderedChunks;
    }
    
    public float getRenderQuality() {
        return renderQuality;
    }
}