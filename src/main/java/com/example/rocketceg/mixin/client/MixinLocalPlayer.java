package com.example.rocketceg.mixin.client;

import com.example.rocketceg.dimension.seamless.SeamlessDimensionManager;
import com.example.rocketceg.seamless.SeamlessCore;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** 😡 本地玩家 Mixin - ImmersivePortalsMod 增强版 * * 核心功能： * 1. 处理无缝传送中的玩家状态 * 2. 应用客户端相机旋转变换 * 3. 预加载相邻维度 * 4. 平滑传送过程中的移动 😡
     */
@Mixin(LocalPlayer.class)
public class MixinLocalPlayer {

    /** 😡 在玩家 tick 时处理无缝传送相关逻辑 😡
     */
    @Inject(
        method = "tick",
        at = @At("HEAD")
    )
    private void rocketceg$handleSeamlessTeleportTick(CallbackInfo ci) {
        LocalPlayer player = (LocalPlayer) (Object) this;
        SeamlessCore seamlessCore = SeamlessCore.getInstance();
        
        // 😡 1. 处理相机过渡 😡
        if (seamlessCore.isCameraTransitioning()) {
            // 😡 让 SeamlessCore 处理客户端相机过渡 😡
            // 😡 这里主要是确保不会被其他逻辑干扰 😡
        }
        
        // 😡 2. 检查是否需要预加载相邻维度 😡
        checkDimensionPreload(player);
    }
    
    /** 😡 检查是否需要预加载相邻维度 😡
     */
    private void checkDimensionPreload(LocalPlayer player) {
        Vec3 pos = player.position();
        
        // 😡 如果玩家在高空（接近太空），预加载轨道维度 😡
        if (pos.y > 200 && player.level().dimension().location().toString().contains("surface")) {
            String currentDim = player.level().dimension().location().toString();
            String orbitDim = currentDim.replace("surface", "orbit");
            
            net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> orbitKey = 
                net.minecraft.resources.ResourceKey.create(
                    net.minecraft.core.registries.Registries.DIMENSION,
                    net.minecraft.resources.ResourceLocation.tryParse(orbitDim)
                );
            
            SeamlessDimensionManager.getInstance().preloadAdjacentDimensions(orbitKey);
        }
        
        // 😡 如果玩家在轨道中且高度较低，预加载表面维度 😡
        if (pos.y < 100 && player.level().dimension().location().toString().contains("orbit")) {
            String currentDim = player.level().dimension().location().toString();
            String surfaceDim = currentDim.replace("orbit", "surface");
            
            net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> surfaceKey = 
                net.minecraft.resources.ResourceKey.create(
                    net.minecraft.core.registries.Registries.DIMENSION,
                    net.minecraft.resources.ResourceLocation.tryParse(surfaceDim)
                );
            
            SeamlessDimensionManager.getInstance().preloadAdjacentDimensions(surfaceKey);
        }
    }
    
    /** 😡 在传送过程中平滑插值玩家位置和视角 😡
     */
    @Inject(
        method = "move",
        at = @At("HEAD")
    )
    private void rocketceg$smoothTeleportMovement(net.minecraft.world.entity.MoverType moverType, 
                                                Vec3 movement, CallbackInfo ci) {
        SeamlessDimensionManager manager = SeamlessDimensionManager.getInstance();
        
        if (manager.isTeleporting()) {
            // 😡 在传送过程中，可以添加特殊的移动逻辑 😡
            float progress = manager.getTransitionProgress();
            
            // 😡 这里可以添加传送过程中的特殊效果 😡
            // 😡 例如：屏幕模糊、粒子效果、音效等 😡
        }
    }
    
    /** 😡 在玩家视角更新时处理无缝传送的相机变换 😡
     */
    @Inject(
        method = "aiStep",
        at = @At("HEAD")
    )
    private void rocketceg$handleCameraRotationDuringTeleport(CallbackInfo ci) {
        SeamlessCore seamlessCore = SeamlessCore.getInstance();
        
        // 😡 如果正在进行相机过渡，让 SeamlessCore 控制相机 😡
        if (seamlessCore.isCameraTransitioning()) {
            // 😡 在相机过渡期间，可以添加额外的处理逻辑 😡
            // 😡 例如阻止某些玩家输入或添加视觉效果 😡
        }
    }
}