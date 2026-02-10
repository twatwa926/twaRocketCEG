package com.example.examplemod.rocket;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.rocket.ship.RocketShipEntity;
import com.example.examplemod.rocket.ship.RocketShipRegistry;
import com.example.examplemod.rocket.ship.RocketShipStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Clearable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class RocketCreateBridge {

    private RocketCreateBridge() {
    }

    public static void assembleContraption(Level level, BlockPos origin, RocketStructure structure, Player player) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        if (!(level.getBlockEntity(origin) instanceof RocketAvionicsBayBlockEntity avionics)) {
            player.displayClientMessage(net.minecraft.network.chat.Component.literal("航电仓未就绪，无法组装"), false);
            return;
        }

        long shipId = RocketShipRegistry.nextId();

        avionics.attachShip(shipId);
        avionics.setControllingPlayer(player.getUUID());

        Map<BlockPos, BlockState> blocks = new HashMap<>();
        ServerLevel buildLevel = RocketDimensions.getBuildLevel(serverLevel.getServer());
        BlockPos buildOrigin = RocketDimensions.buildOriginFor(shipId);

        for (BlockPos pos : structure.getBlocks()) {
            BlockPos local = pos.subtract(origin);
            BlockState state = level.getBlockState(pos);
            if (!state.isAir()) {
                blocks.put(local, state);
                if (buildLevel != null) {
                    buildLevel.setBlockAndUpdate(buildOrigin.offset(local), state);
                }
            }
        }

        List<BlockPos> placeholderFloaters = addFloaterPlaceholders(blocks);
        if (buildLevel != null && !placeholderFloaters.isEmpty()) {
            BlockState floater = ExampleMod.FLOATER.get().defaultBlockState();
            for (BlockPos localPos : placeholderFloaters) {
                buildLevel.setBlockAndUpdate(buildOrigin.offset(localPos), floater);
            }
        }

        RocketShipStorage storage = new RocketShipStorage(blocks);
        RocketShipEntity shipEntity = new RocketShipEntity(ExampleMod.ROCKET_SHIP_ENTITY.get(), serverLevel);
        shipEntity.configure(shipId, storage, origin, Math.max(1.0, structure.getFuelTanks() * 60.0));
        serverLevel.addFreshEntity(shipEntity);

        com.example.examplemod.network.RocketNetwork.CHANNEL.send(
                net.minecraftforge.network.PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> shipEntity),
                new com.example.examplemod.network.RocketShipStructurePacket(shipEntity.getId(), shipEntity.position(), storage.getBlocks()));

        avionics.detachShip();

        serverLevel.getServer().tell(new net.minecraft.server.TickTask(serverLevel.getServer().getTickCount() + 1, () -> {
            for (BlockPos pos : structure.getBlocks()) {
                var blockEntity = level.getBlockEntity(pos);
                if (blockEntity != null) {
                    if (blockEntity instanceof Clearable clearable) {
                        clearable.clearContent();
                    }
                    blockEntity.setRemoved();
                    level.removeBlockEntity(pos);
                }
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 11);
            }
        }));

        com.example.examplemod.network.RocketNetwork.CHANNEL.send(
                net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> (net.minecraft.server.level.ServerPlayer) player),
                new com.example.examplemod.network.RocketControlLinkPacket(shipId));
    }

    private static List<BlockPos> addFloaterPlaceholders(Map<BlockPos, BlockState> blocks) {
        List<BlockPos> added = new ArrayList<>();
        if (blocks.isEmpty()) {
            return added;
        }
        boolean hasFloater = blocks.values().stream().anyMatch(state -> state.is(ExampleMod.FLOATER.get()));
        if (hasFloater) {
            return added;
        }

        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxZ = Integer.MIN_VALUE;
        for (BlockPos pos : blocks.keySet()) {
            minX = Math.min(minX, pos.getX());
            minY = Math.min(minY, pos.getY());
            minZ = Math.min(minZ, pos.getZ());
            maxX = Math.max(maxX, pos.getX());
            maxZ = Math.max(maxZ, pos.getZ());
        }

        int floaterY = minY - 1;
        BlockState floater = ExampleMod.FLOATER.get().defaultBlockState();
        BlockPos[] candidates = new BlockPos[] {
                new BlockPos(minX, floaterY, minZ),
                new BlockPos(maxX, floaterY, minZ),
                new BlockPos(minX, floaterY, maxZ),
                new BlockPos(maxX, floaterY, maxZ)
        };
        for (BlockPos candidate : candidates) {
            if (blocks.putIfAbsent(candidate, floater) == null) {
                added.add(candidate);
            }
        }
        return added;
    }
}
