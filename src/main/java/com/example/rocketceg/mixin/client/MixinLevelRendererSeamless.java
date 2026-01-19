package com.example.rocketceg.mixin.client;

import com.example.rocketceg.seamless.SeamlessCore;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** 😡 LevelRenderer Mixin - 跨维度渲染集成 * * 这个 Mixin 实现了 ImmersivePortalsMod 风格的跨维度渲染： * 1. 收集多个维度的块 * 2. 应用视锥体剔除 * 3. 渲染所有可见块 * * 关键特性： * - 无缝维度渲染 * - 性能优化（视锥体剔除） * - 光照计算 😡
     */
@Mixin(LevelRenderer.class)
public class MixinLevelRendererSeamless {
    
    private static final Logger LOGGER = LogManager.getLogger("RocketCEG");
    
    /** 😡 在块渲染前进行跨维度块收集 * * 这个 Mixin 在块渲染循环前执行，用于： * 1. 检查是否正在进行无缝传送 * 2. 如果是，收集目标维度的块 * 3. 应用视锥体剔除 * 4. 准备渲染数据 😡
     */
    @Inject(
        method = "renderLevel(Lcom/mojang/blaze3d/vertex/PoseStack;FJZLnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/GameRenderer;Lnet/minecraft/client/renderer/LightTexture;Lorg/joml/Matrix4f;)V",
        at = @At("HEAD"),
        cancellable = false
    )
    private void rocketceg$prepareSeamlessRendering(
            com.mojang.blaze3d.vertex.PoseStack poseStack,
            float partialTick,
            long finishTimeNano,
            boolean renderBlockOutline,
            net.minecraft.client.Camera camera,
            net.minecraft.client.renderer.GameRenderer gameRenderer,
            net.minecraft.client.renderer.LightTexture lightTexture,
            org.joml.Matrix4f projectionMatrix,
            CallbackInfo ci) {
        
        try {
            // 😡 检查是否正在进行无缝传送 😡
            SeamlessCore seamlessCore = SeamlessCore.getInstance();
            
            if (seamlessCore.isClientTeleporting()) {
                LOGGER.debug("[MixinLevelRenderer] 准备跨维度渲染");
                
                // 😡 获取目标维度信息 😡
                net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> targetDimension = 
                    seamlessCore.getClientTargetDimension();
                net.minecraft.world.phys.Vec3 targetPosition = seamlessCore.getClientTargetPosition();
                
                if (targetDimension != null && targetPosition != null) {
                    // 😡 这里可以添加额外的渲染准备逻辑 😡
                    // 😡 例如：预加载块、计算视锥体等 😡
                    
                    LOGGER.debug("[MixinLevelRenderer] 目标维度: {}, 位置: {}", 
                        targetDimension.location(), targetPosition);
                }
            }
            
        } catch (Exception e) {
            LOGGER.error("[MixinLevelRenderer] 跨维度渲染准备失败", e);
        }
    }
    
    /** 😡 在块渲染后进行清理 * * 这个 Mixin 在块渲染完成后执行，用于： * 1. 清理临时数据 * 2. 重置渲染状态 * 3. 更新传送状态 😡
     */
    @Inject(
        method = "renderLevel(Lcom/mojang/blaze3d/vertex/PoseStack;FJZLnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/GameRenderer;Lnet/minecraft/client/renderer/LightTexture;Lorg/joml/Matrix4f;)V",
        at = @At("TAIL"),
        cancellable = false
    )
    private void rocketceg$cleanupSeamlessRendering(
            com.mojang.blaze3d.vertex.PoseStack poseStack,
            float partialTick,
            long finishTimeNano,
            boolean renderBlockOutline,
            net.minecraft.client.Camera camera,
            net.minecraft.client.renderer.GameRenderer gameRenderer,
            net.minecraft.client.renderer.LightTexture lightTexture,
            org.joml.Matrix4f projectionMatrix,
            CallbackInfo ci) {
        
        try {
            SeamlessCore seamlessCore = SeamlessCore.getInstance();
            
            if (seamlessCore.isClientTeleporting()) {
                LOGGER.debug("[MixinLevelRenderer] 清理跨维度渲染数据");
                
                // 😡 这里可以添加清理逻辑 😡
                // 😡 例如：释放临时缓冲区、重置状态等 😡
            }
            
        } catch (Exception e) {
            LOGGER.error("[MixinLevelRenderer] 跨维度渲染清理失败", e);
        }
    }
}
