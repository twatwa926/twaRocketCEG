package com.example.examplemod.network;

import com.example.examplemod.rocket.ship.RocketShipEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class RocketShipStructurePacket {
    private static final Map<Integer, RocketShipStructurePacket> PENDING = new ConcurrentHashMap<>();
    private final int entityId;
    private final Vec3 anchor;
    private final Map<BlockPos, BlockState> blocks;

    public RocketShipStructurePacket(int entityId, Vec3 anchor, Map<BlockPos, BlockState> blocks) {
        this.entityId = entityId;
        this.anchor = anchor;
        this.blocks = blocks;
    }

    public static void encode(RocketShipStructurePacket packet, FriendlyByteBuf buf) {
        buf.writeInt(packet.entityId);
        buf.writeDouble(packet.anchor.x);
        buf.writeDouble(packet.anchor.y);
        buf.writeDouble(packet.anchor.z);
        buf.writeVarInt(packet.blocks.size());
        for (Map.Entry<BlockPos, BlockState> entry : packet.blocks.entrySet()) {
            buf.writeBlockPos(entry.getKey());
            buf.writeVarInt(Block.getId(entry.getValue()));
        }
    }

    public static RocketShipStructurePacket decode(FriendlyByteBuf buf) {
        int entityId = buf.readInt();
        Vec3 anchor = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
        int size = buf.readVarInt();
        Map<BlockPos, BlockState> blocks = new HashMap<>();
        for (int i = 0; i < size; i++) {
            BlockPos pos = buf.readBlockPos();
            BlockState state = Block.stateById(buf.readVarInt());
            blocks.put(pos, state);
        }
        return new RocketShipStructurePacket(entityId, anchor, blocks);
    }

    public static void handle(RocketShipStructurePacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.level == null) {
                return;
            }
            Entity entity = minecraft.level.getEntity(packet.entityId);
            if (entity instanceof RocketShipEntity ship) {
                ship.applyStructure(packet.anchor, packet.blocks);
            } else {
                PENDING.put(packet.entityId, packet);
            }
        });
        context.get().setPacketHandled(true);
    }

    public static void applyPending(RocketShipEntity ship) {
        RocketShipStructurePacket pending = PENDING.remove(ship.getId());
        if (pending != null) {
            ship.applyStructure(pending.anchor, pending.blocks);
        }
    }
}
