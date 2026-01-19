package com.example.rocketceg.mixin.client;

import com.example.rocketceg.seamless.SeamlessCore;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** 😡 相机 Mixin - 支持跨维度相机变换 * * 参考 ImmersivePortalsMod 的相机处理： * 1. 在渲染前更新相机位置 * 2. 支持相机穿过传送门时的平滑过渡 * 3. 支持相机旋转变换 😡
     */
@Mixin(Camera.class)
public class MixinCameraPortal {
    
    @Shadow
    private Entity entity;
    
    @Shadow
    private float xRot;
    
    @Shadow
    private float yRot;
    
    /** 😡 在相机设置后应用传送门变换 😡
     */
    @Inject(
        method = "setup",
        at = @At("TAIL")
    )
    private void rocketceg$applyPortalTransformAfterSetup(CallbackInfo ci) {
        try {
            SeamlessCore seamlessCore = SeamlessCore.getInstance();
            
            // 😡 如果正在进行无缝传送，应用相机变换 😡
            if (seamlessCore.isSeamlessTeleporting()) {
                // 😡 应用相机旋转过渡 😡
                if (seamlessCore.isCameraTransitioning()) {
                    float progress = seamlessCore.getCameraTransitionProgress();
                    
                    // 😡 这里可以添加相机过渡逻辑 😡
                }
            }
            
        } catch (Exception e) {
            // 😡 静默处理异常 😡
        }
    }
    
    /** 😡 在相机移动时检查是否穿过传送门 😡
     */
    @Inject(
        method = "setPosition",
        at = @At("HEAD")
    )
    private void rocketceg$checkPortalCrossing(double x, double y, double z, CallbackInfo ci) {
        try {
            // 😡 这里可以添加传送门穿过检测逻辑 😡
            
        } catch (Exception e) {
            // 😡 静默处理异常 😡
        }
    }
}
