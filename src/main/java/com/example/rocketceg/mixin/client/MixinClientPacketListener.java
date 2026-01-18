package com.example.rocketceg.mixin.client;

import com.example.rocketceg.seamless.SeamlessCore;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** 😡 客户端包监听器 Mixin - 完全阻止 respawn 包 * * 这是实现无缝传送的关键组件： * 1. 完全阻止 respawn 包的处理 * 2. 防止任何加载屏幕的触发 * 3. 维持游戏的连续性 😡
     */
@Mixin(ClientPacketListener.class)
public class MixinClientPacketListener {

    /** 😡 完全阻止 respawn 包处理 😡
     */
    @Inject(
        method = "handleRespawn(Lnet/minecraft/network/protocol/game/ClientboundRespawnPacket;)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void rocketceg$blockRespawn(ClientboundRespawnPacket packet, CallbackInfo ci) {
        // 😡 获取目标维度 😡
        String dimensionLocation = packet.getDimension().location().toString();
        
        // 😡 如果涉及 RocketCEG 维度或正在进行无缝传送，完全阻止处理 😡
        if (dimensionLocation.startsWith("rocketceg:") || 
            SeamlessCore.getInstance().shouldBlockRespawnPacket()) {
            
            com.example.rocketceg.RocketCEGMod.LOGGER.info(
                "[SeamlessCore] 阻止 respawn 包处理，维持无缝体验 - 维度: {}", dimensionLocation
            );
            
            // 😡 完全取消 respawn 包的处理 😡
            ci.cancel();
        }
    }
}