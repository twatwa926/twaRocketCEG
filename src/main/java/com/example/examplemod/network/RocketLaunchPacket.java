package com.example.examplemod.network;

import com.example.examplemod.rocket.RocketAvionicsBayBlockEntity;
import com.example.examplemod.rocket.RocketControlRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class RocketLaunchPacket {
    private final long shipId;
    private final BlockPos pos;

    public RocketLaunchPacket(long shipId) {
        this.shipId = shipId;
        this.pos = null;
    }

    public RocketLaunchPacket(BlockPos pos) {
        this.shipId = -1L;
        this.pos = pos;
    }

    public static void encode(RocketLaunchPacket packet, FriendlyByteBuf buf) {
        buf.writeBoolean(packet.shipId >= 0);
        if (packet.shipId >= 0) {
            buf.writeLong(packet.shipId);
        } else {
            buf.writeBlockPos(packet.pos != null ? packet.pos : BlockPos.ZERO);
        }
    }

    public static RocketLaunchPacket decode(FriendlyByteBuf buf) {
        if (buf.readBoolean()) {
            return new RocketLaunchPacket(buf.readLong());
        }
        return new RocketLaunchPacket(buf.readBlockPos());
    }

    public static void handle(RocketLaunchPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer player = context.get().getSender();
            if (player == null) return;

            RocketAvionicsBayBlockEntity avionics = null;
            if (packet.shipId >= 0) {
                avionics = RocketControlRegistry.get(packet.shipId);
            } else if (packet.pos != null && player.level().getBlockEntity(packet.pos) instanceof RocketAvionicsBayBlockEntity be) {
                avionics = be;
            }
            if (avionics == null || !avionics.isAttached()) {
                player.displayClientMessage(net.minecraft.network.chat.Component.translatable("message.rocketwa.avionics_not_linked"), true);
                return;
            }

            avionics.setLaunchOrientationUp();
            avionics.setControllingPlayer(player.getUUID());
            avionics.applyInput(player.getUUID(), new Vec3(0, 1, 0));
        });
        context.get().setPacketHandled(true);
    }
}
