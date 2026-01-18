package com.example.rocketceg.mixin.client;

import com.example.rocketceg.seamless.SeamlessCore;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** 😡 多人游戏模式 Mixin * * 确保游戏模式在无缝传送过程中保持稳定 😡
     */
@Mixin(MultiPlayerGameMode.class)
public class MixinMultiPlayerGameMode {
    
    /** 😡 阻止游戏模式重置 😡
     */
    @Inject(method = "setLocalMode", at = @At("HEAD"), cancellable = true)
    private void rocketceg$blockGameModeChange(CallbackInfo ci) {
        if (SeamlessCore.getInstance().isSeamlessTeleporting()) {
            // 😡 在无缝传送过程中保持游戏模式不变 😡
            ci.cancel();
        }
    }
}