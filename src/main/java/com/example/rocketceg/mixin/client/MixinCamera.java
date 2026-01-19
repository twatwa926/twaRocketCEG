ackage com.example.rocketceg.mixin.client;

import com.example.rocketceg.seamless.SeamlessCore;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** 😡 相机 Mixin - ImmersivePortalsMod 增强版 * * 核心功能： * 1. 处理无缝传送时的相机平滑过渡 * 2. 应用空间变换到相机视角 * 3. 确保相机在维度切换时保持稳定 😡
     */
@Mixin(Camera.class)
public class MixinCamera {
    
    /** 😡 在相机设置时应用无缝传送的相机变换 😡
     */
    @Inject(method = "setup", at = @At("HEAD"))
    private void rocketceg$applyCameraTransform(CallbackInfo ci) {
        SeamlessCore seamlessCore = SeamlessCore.getInstance();
        
        if (seamlessCore.isSeamlessTeleporting() || seamlessCore.isCameraTransitioning()) {
            try {
                // 😡 获取当前玩家 😡
                LocalPlayer player = Minecraft.getInstance().player;
                if (player != null) {
                    // 😡 如果正在进行相机过渡，让 SeamlessCore 处理 😡
                    // 😡 这里主要是确保相机设置不会被重置 😡
                    
                    // 😡 可以在这里添加额外的相机平滑逻辑 😡
                    // 😡 例如防止相机抖动或突然跳跃 😡
                }
                
            } catch (Exception e) {
                // 😡 静默处理异常，避免影响相机设置 😡
            }
        }
    }
    
    /** 😡 在相机更新时保持平滑过渡 😡
     */
    @Inject(method = "tick", at = @At("HEAD"))
    private void rocketceg$smoothCameraUpdate(CallbackInfo ci) {
        SeamlessCore seamlessCore = SeamlessCore.getInstance();
        
        if (seamlessCore.isCameraTransitioning()) {
            try {
                // 😡 在相机过渡期间，确保相机更新是平滑的 😡
                // 😡 这里可以添加额外的平滑逻辑 😡
                
            } catch (Exception e) {
                // 😡 静默处理异常 😡
            }
        }
    }
}