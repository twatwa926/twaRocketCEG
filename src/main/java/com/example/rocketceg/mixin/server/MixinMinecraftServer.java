package com.example.rocketceg.mixin.server;

import com.example.rocketceg.seamless.SeamlessCore;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** 😡 Minecraft 服务器 Mixin * * 确保服务器在无缝传送过程中保持稳定 😡
     */
@Mixin(MinecraftServer.class)
public class MixinMinecraftServer {
    
    /** 😡 在服务器 tick 中处理无缝传送 😡
     */
    @Inject(method = "tickServer", at = @At("HEAD"))
    private void rocketceg$handleSeamlessTeleport(CallbackInfo ci) {
        // 😡 这里可以添加服务器端的无缝传送处理逻辑 😡
        if (SeamlessCore.getInstance().isSeamlessTeleporting()) {
            // 😡 确保服务器在传送过程中正常运行 😡
        }
    }
}