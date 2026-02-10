package com.example.examplemod.network;

import com.example.examplemod.rocket.RocketControlRegistry;
import com.example.examplemod.rocket.ship.RocketShipEntity;
import com.example.examplemod.rocket.ship.RocketShipRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class RocketControlInputPacket {
    private final long shipId;
    private final double x;
    private final double y;
    private final double z;

    public RocketControlInputPacket(long shipId, Vec3 input) {
        this.shipId = shipId;
        this.x = input.x;
        this.y = input.y;
        this.z = input.z;
    }

    public static void encode(RocketControlInputPacket packet, FriendlyByteBuf buf) {
        buf.writeLong(packet.shipId);
        buf.writeDouble(packet.x);
        buf.writeDouble(packet.y);
        buf.writeDouble(packet.z);
    }

    public static RocketControlInputPacket decode(FriendlyByteBuf buf) {
        long id = buf.readLong();
        Vec3 input = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
        return new RocketControlInputPacket(id, input);
    }

    public static void handle(RocketControlInputPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer player = context.get().getSender();
            if (player == null) {
                return;
            }
            Vec3 input = new Vec3(packet.x, packet.y, packet.z);
            com.example.examplemod.rocket.RocketAvionicsBayBlockEntity avionics = RocketControlRegistry.get(packet.shipId);
            if (avionics != null) {
                avionics.applyInput(player.getUUID(), input);
            } else {

                RocketShipEntity ship = RocketShipRegistry.get(packet.shipId);
                if (ship != null && player.getVehicle() == ship) {
                    ship.updateControlFromPlayerInput(input);
                }
            }
        });
        context.get().setPacketHandled(true);
    }
}
