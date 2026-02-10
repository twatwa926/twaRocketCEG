package com.example.examplemod.network;

import com.example.examplemod.rocket.SeamlessCore;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record SeamlessTeleportPacket(
        ResourceLocation dimensionId,
        double x, double y, double z,
        float yaw, float pitch
) {
    public static void encode(SeamlessTeleportPacket packet, FriendlyByteBuf buf) {
        buf.writeResourceLocation(packet.dimensionId);
        buf.writeDouble(packet.x);
        buf.writeDouble(packet.y);
        buf.writeDouble(packet.z);
        buf.writeFloat(packet.yaw);
        buf.writeFloat(packet.pitch);
    }

    public static SeamlessTeleportPacket decode(FriendlyByteBuf buf) {
        return new SeamlessTeleportPacket(
                buf.readResourceLocation(),
                buf.readDouble(), buf.readDouble(), buf.readDouble(),
                buf.readFloat(), buf.readFloat()
        );
    }

    public static void handle(SeamlessTeleportPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            if (context.get().getDirection().getReceptionSide().isClient()) {
                SeamlessCore.setClientPending(packet);
            }
        });
        context.get().setPacketHandled(true);
    }
}
