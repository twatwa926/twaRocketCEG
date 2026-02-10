package com.example.examplemod.mixin.rocket;

import net.minecraft.network.protocol.game.ClientboundRespawnPacket;

public interface SeamlessClientInvoker {
    void rocketwa$invokeHandleRespawn(net.minecraft.network.protocol.game.ClientboundRespawnPacket packet);
}
