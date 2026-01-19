package com.example.rocketceg.mixin.server;

import com.example.rocketceg.seamless.SeamlessCore;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** 😡 服务器游戏包监听器 Mixin * * 阻止发送任何可能触发加载屏幕的包 😡
     */
@Mixin(ServerGamePacketListenerImpl.class)
public class MixinServerGamePacketListenerImpl {
    
    /** 😡 拦截所有发送的包，阻止 respawn 包 😡
     */
    @Inject(method = "send", at = @At("HEAD"), cancellable = true)
    private void rocketceg$interceptPackets(net.minecraft.network.protocol.Packet<?> packet, CallbackInfo ci) {
        if (SeamlessCore.getInstance().shouldBlockRespawnPacket()) {
            // 😡 检查是否是 respawn 包 😡
            if (packet instanceof ClientboundRespawnPacket) {
                // 😡 完全阻止 respawn 包的发送 😡
                ci.cancel();
                
                com.example.rocketceg.RocketCEGMod.LOGGER.info(
                    "[SeamlessCore] 阻止 respawn 包发送，维持无缝体验"
                );
            }
        }
    }
}