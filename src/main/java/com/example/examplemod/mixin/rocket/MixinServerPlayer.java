package com.example.examplemod.mixin.rocket;

import com.example.examplemod.rocket.SeamlessCore;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayer.class)
public abstract class MixinServerPlayer {

    @Inject(method = "changeDimension(Lnet/minecraft/server/level/ServerLevel;)Lnet/minecraft/world/entity/Entity;", at = @At("HEAD"), cancellable = true)
    private void rocketwa$interceptChangeDimension(ServerLevel targetLevel, CallbackInfoReturnable<Entity> cir) {
        ServerPlayer player = (ServerPlayer) (Object) this;
        if (SeamlessCore.consumeSeamlessRequest(player)) {
            Entity result = SeamlessCore.startEyeBasedSeamlessTeleport(player, targetLevel);
            cir.setReturnValue(result);
            cir.cancel();
        }
    }
}
