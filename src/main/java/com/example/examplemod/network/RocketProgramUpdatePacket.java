package com.example.examplemod.network;

import com.example.examplemod.rocket.RocketAvionicsBayBlockEntity;
import com.example.examplemod.rocket.RocketControlRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class RocketProgramUpdatePacket {
    private final BlockPos pos;
    private final long shipId;
    private final String script;
    private final boolean enableAutopilot;

    public RocketProgramUpdatePacket(BlockPos pos, String script, boolean enableAutopilot) {
        this.pos = pos;
        this.shipId = -1L;
        this.script = script;
        this.enableAutopilot = enableAutopilot;
    }

    public RocketProgramUpdatePacket(long shipId, String script, boolean enableAutopilot) {
        this.pos = null;
        this.shipId = shipId;
        this.script = script;
        this.enableAutopilot = enableAutopilot;
    }

    public static void encode(RocketProgramUpdatePacket packet, FriendlyByteBuf buf) {
        buf.writeBoolean(packet.shipId >= 0);
        if (packet.shipId >= 0) {
            buf.writeLong(packet.shipId);
        } else {
            buf.writeBlockPos(packet.pos);
        }
        buf.writeUtf(packet.script);
        buf.writeBoolean(packet.enableAutopilot);
    }

    public static RocketProgramUpdatePacket decode(FriendlyByteBuf buf) {
        boolean isEntity = buf.readBoolean();
        long shipId = -1L;
        BlockPos pos = null;
        if (isEntity) {
            shipId = buf.readLong();
        } else {
            pos = buf.readBlockPos();
        }
        String script = buf.readUtf(2048);
        boolean enable = buf.readBoolean();
        return isEntity ? new RocketProgramUpdatePacket(shipId, script, enable)
                : new RocketProgramUpdatePacket(pos, script, enable);
    }

    public static void handle(RocketProgramUpdatePacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer player = context.get().getSender();
            if (player == null) return;
            if (packet.shipId >= 0) {
                RocketAvionicsBayBlockEntity avionics = RocketControlRegistry.get(packet.shipId);
                if (avionics == null) {
                    player.displayClientMessage(net.minecraft.network.chat.Component.translatable("message.rocketwa.avionics_not_linked"), true);
                    return;
                }
                avionics.setProgram(packet.script);
                if (packet.enableAutopilot) {
                    avionics.enableAutopilot(player);
                }
                return;
            }
            if (packet.pos != null && player.level().getBlockEntity(packet.pos) instanceof RocketAvionicsBayBlockEntity avionics) {
                avionics.setProgram(packet.script);
                if (packet.enableAutopilot) {
                    avionics.enableAutopilot(player);
                }
            }
        });
        context.get().setPacketHandled(true);
    }
}
