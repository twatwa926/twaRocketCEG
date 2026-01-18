package com.example.rocketceg.mixin.client;

import com.example.rocketceg.dimension.orbital.OrbitalDimensionManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** 😡 取消原版日月渲染的 Mixin * 在主世界和轨道维度，我们渲染自己的立体日月，所以需要屏蔽原版的 😡
     */
@Mixin(LevelRenderer.class)
public class MixinLevelRendererSunMoon {
    
    /** 😡 拦截太阳材质绑定，在主世界和轨道维度返回空材质 * * 注意：这个方法可能需要根据实际的 Minecraft 代码调整 * 如果编译失败，可以尝试使用 @Inject + cancellable 的方式 😡
     */
    @Redirect(
        method = "renderSky",
        at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderTexture(ILnet/minecraft/resources/ResourceLocation;)V",
            ordinal = 0
        ),
        require = 0  // 😡 不强制要求，如果找不到就跳过 😡
    )
    private void rocketceg$cancelSunTexture(int texture, ResourceLocation location) {
        ClientLevel level = Minecraft.getInstance().level;
        
        // 😡 检查是否应该取消原版日月渲染 😡
        boolean shouldCancel = level != null && 
                              (level.dimension() == Level.OVERWORLD || 
                               OrbitalDimensionManager.isOrbitalDimension(level));
        
        // 😡 如果不需要取消，正常绑定材质 😡
        if (!shouldCancel) {
            RenderSystem.setShaderTexture(texture, location);
        }
        // 😡 否则不绑定材质，这样原版日月就不会渲染 😡
    }
}
