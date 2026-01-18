package com.example.rocketceg.mixin.client;

import com.example.rocketceg.dimension.seamless.SeamlessDimensionManager;
import com.example.rocketceg.dimension.seamless.TransitionEffects;
import com.example.rocketceg.seamless.SeamlessCore;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** 😡 游戏渲染器 Mixin - ImmersivePortalsMod 增强版 * * 核心功能： * 1. 在每帧渲染前执行客户端传送 - ImmersivePortalsMod 核心机制 * 2. 处理相机旋转过渡 - 平滑的视角变换 * 3. 渲染传送过渡效果 - 无缝视觉体验 😡
     */
@Mixin(GameRenderer.class)
public class MixinGameRenderer {

    /** 😡 在每帧渲染前执行客户端传送 - ImmersivePortalsMod 核心机制 * * 参考 ImmersivePortalsMod 文档： * "client side teleportation before every frame's rendering (not during ticking)" 😡
     */
    @Inject(
        method = "render",
        at = @At("HEAD")
    )
    private void rocketceg$updateBeforeFrameRendering(float partialTick, long nanoTime, boolean renderLevel, CallbackInfo ci) {
        // 😡 执行 ImmersivePortalsMod 风格的客户端传送更新 😡
        SeamlessCore.getInstance().updateBeforeFrameRendering();
    }

    /** 😡 在渲染 GUI 后添加传送过渡效果 😡
     */
    @Inject(
        method = "render",
        at = @At("TAIL")
    )
    private void rocketceg$renderTransitionEffects(float partialTick, long nanoTime, boolean renderLevel, CallbackInfo ci) {
        // 😡 禁用过渡效果渲染 - 与 GUI 渲染冲突 😡
        // 😡 TODO: 修复顶点缓冲区状态管理后重新启用 😡
    }
    
    /** 😡 在传送过程中修改 FOV 以创建平滑效果 😡
     */
    @Inject(
        method = "getFov",
        at = @At("RETURN"),
        cancellable = true
    )
    private void rocketceg$modifyFovDuringTeleport(net.minecraft.client.Camera camera, float partialTick, 
                                                 boolean useFovSetting, org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<Double> cir) {
        SeamlessDimensionManager manager = SeamlessDimensionManager.getInstance();
        
        if (manager.isTeleporting()) {
            float progress = manager.getTransitionProgress();
            
            // 😡 在传送过程中创建 FOV 变化效果 😡
            // 😡 可以创建"穿越"的视觉效果 😡
            float fovMultiplier = 1.0f + (float) Math.sin(progress * Math.PI) * 0.2f;
 馃槨
            
            // 😡 修改 FOV 值 😡
            double currentFov = cir.getReturnValue();
            cir.setReturnValue(currentFov * fovMultiplier);
 馃槨
        }
    }
}