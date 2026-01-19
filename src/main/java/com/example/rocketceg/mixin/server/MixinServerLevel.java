package com.example.rocketceg.mixin.server;

import com.example.rocketceg.seamless.SeamlessCore;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** 😡 服务端级别 Mixin * * 处理服务端维度的实体管理 😡
     */
@Mixin(ServerLevel.class)
public class MixinServerLevel {
    
    /** 😡 阻止在无缝传送过程中移除实体 😡
     */
    @Inject(method = "removePlayerImmediately", at = @At("HEAD"), cancellable = true)
    private void rocketceg$blockPlayerRemoval(net.minecraft.server.level.ServerPlayer player, net.minecraft.world.entity.Entity.RemovalReason reason, CallbackInfo ci) {
        if (SeamlessCore.getInstance().isSeamlessTeleporting()) {
            // 😡 在无缝传送过程中不移除玩家 😡
            ci.cancel();
        }
    }
}