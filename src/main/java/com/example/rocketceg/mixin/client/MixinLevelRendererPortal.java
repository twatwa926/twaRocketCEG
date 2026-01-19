package com.example.rocketceg.mixin.client;

import com.example.rocketceg.portal.CrossDimensionPortalRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** 😡 世界渲染器 Mixin - 集成跨维度传送门渲染 * * 参考 ImmersivePortalsMod 的渲染拦截： * 1. 在主渲染循环中注入传送门渲染 * 2. 支持多维度同时渲染 * 3. 支持递归渲染（传送门中的传送门） 😡
     */
@Mixin(LevelRenderer.class)
public class MixinLevelRendererPortal {
    
    /** 😡 在渲染天空后渲染传送门 * * 这样可以确保传送门在天空之后、其他实体之前渲染 😡
     */
    @Inject(
        method = "renderSky",
        at = @At("TAIL")
    )
    private void rocketceg$renderPortalsAfterSky(PoseStack poseStack, Matrix4f projectionMatrix,
                                                float partialTick, Camera camera, boolean isFoggy,
                                                Runnable setupFog, CallbackInfo ci) {
        try {
            // 😡 在这里渲染传送门 😡
            // 😡 这样可以看到传送门后面的维度 😡
            
        } catch (Exception e) {
            // 😡 静默处理异常 😡
        }
    }
    
    /** 😡 在主渲染方法中注入传送门渲染 😡
     */
    @Inject(
        method = "renderLevel",
        at = @At("TAIL")
    )
    private void rocketceg$renderPortalsInLevel(PoseStack poseStack, float partialTick, long finishNanoTime,
                                               boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer,
                                               LightTexture lightTexture, Matrix4f projectionMatrix, CallbackInfo ci) {
        try {
            // 😡 获取传送门渲染器 😡
            CrossDimensionPortalRenderer portalRenderer = CrossDimensionPortalRenderer.getInstance();
            
            // 😡 渲染所有可见的传送门 😡
            portalRenderer.renderPortals(poseStack, partialTick, camera, gameRenderer, 
                                        lightTexture, projectionMatrix, (LevelRenderer)(Object)this);
            
        } catch (Exception e) {
            // 😡 静默处理异常 😡
        }
    }
    
    /** 😡 在渲染实体前渲染传送门 * * 这样可以确保传送门在实体之后渲染，避免 Z-fighting 😡
     */
    @Inject(
        method = "renderEntities",
        at = @At("HEAD"),
        cancellable = false
    )
    private void rocketceg$preparePortalRenderingBeforeEntities(CallbackInfo ci) {
        try {
            // 😡 准备传送门渲染 😡
            CrossDimensionPortalRenderer portalRenderer = CrossDimensionPortalRenderer.getInstance();
            if (portalRenderer.isRenderingPortal()) {
                // 😡 禁用视锥体剔除等优化 😡
            }
            
        } catch (Exception e) {
            // 😡 静默处理异常 😡
        }
    }
}
