package com.example.examplemod.network;

import com.example.examplemod.client.RocketClientControlState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class RocketControlLinkPacket {
    private final long shipId;

    public RocketControlLinkPacket(long shipId) {
        this.shipId = shipId;
    }

    public static void encode(RocketControlLinkPacket packet, FriendlyByteBuf buf) {
        buf.writeLong(packet.shipId);
    }

    public static RocketControlLinkPacket decode(FriendlyByteBuf buf) {
        return new RocketControlLinkPacket(buf.readLong());
    }

    public static void handle(RocketControlLinkPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> RocketClientControlState.setControlledShipId(packet.shipId));
        context.get().setPacketHandled(true);
    }
}
