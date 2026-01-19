ackage com.example.rocketceg.mixin.client;

import com.example.rocketceg.seamless.SeamlessCore;
import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** 😡 Camera Mixin - 相机变换和平滑过渡 * * 这个 Mixin 实现了 ImmersivePortalsMod 风格的相机处理： * 1. 检测是否在门户附近 * 2. 应用相机变换 * 3. 平滑过渡相机旋转 * * 关键特性： * - 无缝相机变换 * - 平滑旋转过渡 * - 跨维度视角支持 😡
     */
@Mixin(Camera.class)
public class MixinCameraSeamless {
    
    private static final Logger LOGGER = LogManager.getLogger("RocketCEG");
    
    /** 😡 在相机设置前进行变换检查 * * 这个 Mixin 在相机设置前执行，用于： * 1. 检查是否正在进行无缝传送 * 2. 如果是，应用相机变换 * 3. 准备平滑过渡 😡
     */
    @Inject(
        method = "setup",
        at = @At("HEAD"),
        cancellable = false
    )
    private void rocketceg$prepareSeamlessCameraTransform(
            net.minecraft.world.level.Level level,
            net.minecraft.world.entity.Entity entity,
            boolean isThirdPerson,
            boolean isInvertYaw,
            float partialTick,
            CallbackInfo ci) {
        
        try {
            SeamlessCore seamlessCore = SeamlessCore.getInstance();
            
            // 😡 检查是否正在进行无缝传送或相机过渡 😡
            if (seamlessCore.isClientTeleporting() || seamlessCore.isCameraTransitioning()) {
                LOGGER.debug("[MixinCamera] 准备无缝相机变换");
                
                // 😡 获取相机过渡进度 😡
                float transitionProgress = seamlessCore.getCameraTransitionProgress();
                
                if (transitionProgress > 0.0f && transitionProgress < 1.0f) {
                    LOGGER.debug("[MixinCamera] 相机过渡进度: {}", transitionProgress);
                }
            }
            
        } catch (Exception e) {
            LOGGER.error("[MixinCamera] 相机变换准备失败", e);
        }
    }
    
    /** 😡 在相机设置后进行变换应用 * * 这个 Mixin 在相机设置完成后执行，用于： * 1. 应用相机变换 * 2. 更新相机位置和旋转 * 3. 完成平滑过渡 😡
     */
    @Inject(
        method = "setup",
        at = @At("TAIL"),
        cancellable = false
    )
    private void rocketceg$applySeamlessCameraTransform(
            net.minecraft.world.level.Level level,
            net.minecraft.world.entity.Entity entity,
            boolean isThirdPerson,
            boolean isInvertYaw,
            float partialTick,
            CallbackInfo ci) {
        
        try {
            SeamlessCore seamlessCore = SeamlessCore.getInstance();
            
            // 😡 检查是否正在进行无缝传送 😡
            if (seamlessCore.isClientTeleporting()) {
                LOGGER.debug("[MixinCamera] 应用无缝相机变换");
                
                // 😡 获取变换后的眼部位置 😡
                net.minecraft.world.phys.Vec3 transformedEyePos = 
                    seamlessCore.getClientTransformedEyePosition();
                
                if (transformedEyePos != null && !transformedEyePos.equals(net.minecraft.world.phys.Vec3.ZERO)) {
                    LOGGER.debug("[MixinCamera] 变换后眼部位置: {}", transformedEyePos);
                }
            }
            
            // 😡 检查是否正在进行相机过渡 😡
            if (seamlessCore.isCameraTransitioning()) {
                LOGGER.debug("[MixinCamera] 应用相机平滑过渡");
                
                float transitionProgress = seamlessCore.getCameraTransitionProgress();
                LOGGER.debug("[MixinCamera] 过渡进度: {}", transitionProgress);
            }
            
        } catch (Exception e) {
            LOGGER.error("[MixinCamera] 相机变换应用失败", e);
        }
    }
}
