package com.example.rocketceg.mixin.client;

import com.example.rocketceg.dimension.seamless.SeamlessDimensionManager;
import com.example.rocketceg.dimension.seamless.SpacePlanetRenderer;
import com.example.rocketceg.dimension.seamless.MultiDimensionRenderer;
import com.example.rocketceg.dimension.seamless.AdvancedPortalRenderer;
import com.example.rocketceg.seamless.SeamlessCore;
import com.example.rocketceg.dimension.orbital.OrbitalSkyRenderer;
import com.example.rocketceg.dimension.orbital.OrbitalDimensionManager;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** 😡 增强版世界渲染器 Mixin - 集成 ImmersivePortalsMod 技术 * * 参考 ImmersivePortalsMod 的渲染理念： * 1. 多维度同时渲染 - 突破原版单维度限制 * 2. 高级传送门渲染 - 支持跨维度实时渲染 * 3. 空间变换渲染 - 支持非欧几里得几何 * 4. 无缝视觉过渡 - 消除维度切换的视觉中断 * 5. 太空星球渲染 - 专门为太空主题优化 * * 这是实现真正无缝维度切换的核心渲染组件 😡
     */
@Mixin(LevelRenderer.class)
public class MixinLevelRenderer {

    @Unique
    private OrbitalSkyRenderer orbitalSkyRenderer;
    
    @Unique
    private com.example.rocketceg.client.OverworldSkyRenderer overworldSkyRenderer;

    /** 😡 取消原版日月渲染 * 在主世界和轨道维度，我们渲染自己的立体日月 * 通过在 renderSky 方法中太阳/月亮渲染之前取消来阻止原版渲染 😡
     */
    @Inject(
        method = "renderSky",
        at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderTexture(ILnet/minecraft/resources/ResourceLocation;)V"
        ),
        cancellable = true,
        require = 0
    )
    private void rocketceg$cancelVanillaSunMoon(PoseStack poseStack, Matrix4f projectionMatrix, 
                                               float partialTick, Camera camera, boolean isFoggy, 
                                               Runnable setupFog, CallbackInfo ci) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return;
        
        // 😡 在主世界和轨道维度，取消原版日月渲染 😡
        if (level.dimension() == Level.OVERWORLD || OrbitalDimensionManager.isOrbitalDimension(level)) {
            // 😡 取消从这里开始的渲染（包括太阳和月亮） 😡
            // 😡 我们的自定义日月会在下面的 TAIL 注入中渲染 😡
            ci.cancel();
        }
    }

    /** 😡 在渲染天空后添加天体渲染 😡
     */
    @Inject(
        method = "renderSky",
        at = @At("TAIL")
    )
    private void rocketceg$renderSpacePlanets(PoseStack poseStack, Matrix4f projectionMatrix, 
                                            float partialTick, Camera camera, boolean isFoggy, 
                                            Runnable setupFog, CallbackInfo ci) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return;
        
        MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediate(
            com.mojang.blaze3d.vertex.Tesselator.getInstance().getBuilder()
        );
        
        // 😡 检查是否在轨道维度 😡
        if (OrbitalDimensionManager.isOrbitalDimension(level)) {
            if (orbitalSkyRenderer == null) {
                orbitalSkyRenderer = new OrbitalSkyRenderer();
            }
            
            orbitalSkyRenderer.renderOrbitalSky(poseStack, bufferSource, partialTick);
            bufferSource.endBatch();
        } 
        // 😡 检查是否在主世界 😡
        else if (level.dimension() == net.minecraft.world.level.Level.OVERWORLD) {
            if (overworldSkyRenderer == null) {
                overworldSkyRenderer = new com.example.rocketceg.client.OverworldSkyRenderer();
            }
            
            overworldSkyRenderer.renderOverworldSky(poseStack, bufferSource, partialTick);
            bufferSource.endBatch();
        }
    }
    
    /** 😡 禁用：在主渲染方法中注入增强的多维度渲染逻辑 * 原因：会导致 "Already building!" 错误 * * 这个 Mixin 在 renderLevel 的 TAIL 处尝试渲染多个维度， * 但这会干扰 Minecraft 的 GUI 渲染管道，导致缓冲区状态冲突。 * * 多维度渲染需要在单独的 FrameBuffer 中进行，而不是在主渲染循环中。 😡
     */
    // 😡 @Inject( 😡
    // 😡 method = "renderLevel", 😡
    // 😡 at = @At("TAIL") 😡
    // 😡 ) 😡
    // 😡 private void rocketceg$renderAdvancedMultipleDimensions(...) { 😡
    // 😡 // DISABLED - 导致缓冲区冲突 😡
    // 😡 } 😡
    
    /** 😡 禁用：在每帧渲染开始时更新无缝传送进度 * 原因：会导致 "Already building!" 错误 * * 这个 Mixin 在 renderLevel 的 HEAD 处尝试注册维度用于渲染， * 但这会干扰 Minecraft 的渲染管道。 😡
     */
    // 😡 @Inject( 😡
    // 😡 method = "renderLevel", 😡
    // 😡 at = @At("HEAD") 😡
    // 😡 ) 😡
    // 😡 private void rocketceg$updateSeamlessTransition(...) { 😡
    // 😡 // DISABLED - 导致缓冲区冲突 😡
    // 😡 } 😡
    
    /** 😡 禁用：拦截区块编译以支持跨维度区块渲染 * 原因：会导致 "Already building!" 错误 * * 这个 Mixin 在 compileChunks 处尝试注册维度， * 但这会干扰 Minecraft 的区块编译管道。 😡
     */
    // 😡 @Inject(method = "compileChunks", at = @At("HEAD")) 😡
    // 😡 private void rocketceg$onCompileChunks(Camera camera, CallbackInfo ci) { 😡
    // 😡 // DISABLED - 导致缓冲区冲突 😡
    // 😡 } 😡
    
    /** 😡 禁用：在无缝传送过程中保持渲染稳定 * 原因：会导致 "Already building!" 错误 * * 这个 Mixin 尝试阻止渲染重置，但会干扰 Minecraft 的渲染管道。 😡
     */
    // 😡 @Inject(method = "allChanged", at = @At("HEAD"), cancellable = true) 😡
    // 😡 private void rocketceg$preventRenderingChanges(CallbackInfo ci) { 😡
    // 😡 // DISABLED - 导致缓冲区冲突 😡
    // 😡 } 😡
    
    /** 😡 清理渲染器资源 😡
     */
    @Inject(method = "close", at = @At("HEAD"))
    private void rocketceg$cleanupRenderers(CallbackInfo ci) {
        if (orbitalSkyRenderer != null) {
            orbitalSkyRenderer.cleanup();
            orbitalSkyRenderer = null;
        }
        if (overworldSkyRenderer != null) {
            overworldSkyRenderer.cleanup();
            overworldSkyRenderer = null;
        }
    }
}