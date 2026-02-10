package com.example.examplemod.network;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.rocket.ship.RocketBlockModificationHandler;
import com.example.examplemod.rocket.ship.RocketShipEntity;
import com.example.examplemod.rocket.ship.RocketShipInteractionHandler;
import com.example.examplemod.rocket.RocketDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class RocketShipBreakBlockPacket {
    private final int shipEntityId;
    private final BlockPos localPos;

    public RocketShipBreakBlockPacket(int shipEntityId, BlockPos localPos) {
        this.shipEntityId = shipEntityId;
        this.localPos = localPos.immutable();
    }

    public static void encode(RocketShipBreakBlockPacket packet, FriendlyByteBuf buf) {
        buf.writeVarInt(packet.shipEntityId);
        buf.writeBlockPos(packet.localPos);
    }

    public static RocketShipBreakBlockPacket decode(FriendlyByteBuf buf) {
        return new RocketShipBreakBlockPacket(buf.readVarInt(), buf.readBlockPos());
    }

    public static void handle(RocketShipBreakBlockPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer player = context.get().getSender();
            if (player == null) return;
            Level level = player.level();
            if (!(level instanceof ServerLevel serverLevel)) return;
            var entity = level.getEntity(packet.shipEntityId);
            if (!(entity instanceof RocketShipEntity ship)) return;
            if (ship.getStorageBlocks().isEmpty()) return;
            BlockState brokenState = ship.getStorageBlocks().get(packet.localPos);
            var handler = new RocketBlockModificationHandler(ship);
            if (handler.breakBlock(packet.localPos, player)) {
                var chunkManager = ship.getChunkManager();
                if (chunkManager != null) {
                    ServerLevel buildLevel = RocketDimensions.getBuildLevel(player.server);
                    if (buildLevel != null) {
                        BlockPos worldPos = ship.getBuildOrigin().offset(packet.localPos);
                        chunkManager.removeBlock(buildLevel, worldPos);
                    }
                }
                RocketShipInteractionHandler.checkDisassembleIfNoAvionicsAfterBreak(ship, player,
                        brokenState != null && brokenState.is(ExampleMod.AVIONICS_BAY.get()));
            }
        });
        context.get().setPacketHandled(true);
    }
}
