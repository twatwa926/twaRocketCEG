package com.example.examplemod.mixin.rocket;

import com.example.examplemod.rocket.SeamlessCore;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ClientPacketListener.class)
public abstract class MixinClientPacketListener implements SeamlessClientInvoker {

    @Inject(method = "handleRespawn", at = @At("HEAD"), cancellable = true)
    private void rocketwa$onRespawn(ClientboundRespawnPacket packet, CallbackInfo ci) {
        if (SeamlessCore.hasPendingSeamless()) {
            SeamlessCore.storeRespawnPacket(packet);
            ci.cancel();
        }
    }

    @Invoker("handleRespawn")
    @Override
    public abstract void rocketwa$invokeHandleRespawn(ClientboundRespawnPacket packet);
}
