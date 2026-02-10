package com.example.examplemod.rocket.ship;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.rocket.RocketControlRegistry;
import com.example.examplemod.rocket.RocketDimensions;
import com.example.examplemod.rocket.RocketAvionicsBayBlockEntity;
import com.example.examplemod.rocket.RocketFuelHelper;
import com.example.examplemod.network.RocketControlLinkPacket;
import com.example.examplemod.network.RocketNetwork;
import com.example.examplemod.network.RocketShipBreakBlockPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.PacketDistributor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import com.example.examplemod.network.RocketNetwork;
import com.example.examplemod.network.RocketShipStructurePacket;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

@Mod.EventBusSubscriber(modid = ExampleMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class RocketShipInteractionHandler {

    private static final double REACH = 8.0;

    private static ClipContext makeClipContext(Player player) {
        Vec3 from = player.getEyePosition(1.0f);
        Vec3 to = from.add(player.getLookAngle().scale(REACH));
        return new ClipContext(from, to, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player);
    }

    private static boolean tryShipBlockInteract(ServerPlayer player, net.minecraft.world.InteractionHand hand, boolean allowRefuelAndGui) {
        UnifiedHit hit = RocketShipRaycastUtils.unifiedClip(player.level(), makeClipContext(player), true);
        if (!(hit instanceof UnifiedHit.Ship shipHit) || shipHit.getLocalHit().getType() != HitResult.Type.BLOCK) {
            return false;
        }
        RocketShipEntity ship = shipHit.getShip();
        BlockPos localPos = shipHit.getLocalHit().getBlockPos();
        BlockState state = ship.getStorageBlocks().get(localPos);
        if (state == null) return false;
        if (state.is(ExampleMod.ROCKET_SEAT.get())) {
            if (tryMountRocketSeat(player, ship)) return true;
        }
        if (allowRefuelAndGui) {
            if (state.is(ExampleMod.FUEL_TANK.get())) {
                if (tryRefuelShip(player, ship)) return true;
            }
            if (state.is(ExampleMod.AVIONICS_BAY.get()) && ship.getShipId() >= 0) {
                net.minecraftforge.network.NetworkHooks.openScreen(player,
                        new net.minecraft.world.SimpleMenuProvider(
                                (id, inv, p) -> com.example.examplemod.rocket.RocketAvionicsMenu.forEntity(id, inv, ship.getShipId()),
                                net.minecraft.network.chat.Component.translatable("menu.rocketwa.avionics")),
                        buf -> { buf.writeBoolean(true); buf.writeLong(ship.getShipId()); });
                return true;
            }
        }
        return false;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        UnifiedHit hit = RocketShipRaycastUtils.unifiedClip(player.level(), makeClipContext(player), true);
        if (hit instanceof UnifiedHit.Ship shipHit && shipHit.getLocalHit().getType() == HitResult.Type.BLOCK) {
            handleBlockBreak(player, shipHit.getShip(), shipHit.getLocalHit());
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLeftClickEmpty(PlayerInteractEvent.LeftClickEmpty event) {
        Player player = event.getEntity();
        if (player == null || !player.level().isClientSide()) return;
        UnifiedHit hit = RocketShipRaycastUtils.unifiedClip(player.level(), makeClipContext(player), false);
        if (hit instanceof UnifiedHit.Ship shipHit && shipHit.getLocalHit().getType() == HitResult.Type.BLOCK) {
            RocketNetwork.CHANNEL.sendToServer(new RocketShipBreakBlockPacket(
                    shipHit.getShip().getId(), shipHit.getLocalHit().getBlockPos()));
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onAttackEntity(AttackEntityEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!(event.getTarget() instanceof RocketShipEntity)) return;
        UnifiedHit hit = RocketShipRaycastUtils.unifiedClip(player.level(), makeClipContext(player), true);
        if (hit instanceof UnifiedHit.Ship shipHit && shipHit.getLocalHit().getType() == HitResult.Type.BLOCK) {
            handleBlockBreak(player, shipHit.getShip(), shipHit.getLocalHit());
            event.setCanceled(true);
        }
    }

    private static boolean tryMountRocketSeat(ServerPlayer player, RocketShipEntity ship) {
        if (player.isPassenger()) return false;
        if (ship.getStorageBlocks().isEmpty()) return false;
        for (java.util.Map.Entry<BlockPos, BlockState> e : ship.getStorageBlocks().entrySet()) {
            if (e.getValue().is(ExampleMod.ROCKET_SEAT.get())) {
                player.startRiding(ship);
                if (ship.getShipId() >= 0) {
                    var avionics = RocketControlRegistry.get(ship.getShipId());
                    if (avionics != null) {
                        avionics.setControllingPlayer(player.getUUID());
                        RocketNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new RocketControlLinkPacket(ship.getShipId()));
                    }
                }
                return true;
            }
        }
        return false;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!(event.getTarget() instanceof RocketShipEntity)) return;
        if (player.isPassenger()) return;

        if (tryShipBlockInteract(player, event.getHand(), true)) {
            event.setCanceled(true);
            event.setCancellationResult(net.minecraft.world.InteractionResult.SUCCESS);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onEntityInteract(PlayerInteractEvent.EntityInteractSpecific event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!(event.getTarget() instanceof RocketShipEntity targetShip)) return;
        UnifiedHit hit = RocketShipRaycastUtils.unifiedClip(player.level(), makeClipContext(player), true);
        if (!(hit instanceof UnifiedHit.Ship shipHit) || shipHit.getShip() != targetShip || shipHit.getLocalHit().getType() != HitResult.Type.BLOCK) {
            return;
        }
        RocketShipEntity ship = shipHit.getShip();
        BlockPos localPos = shipHit.getLocalHit().getBlockPos();
        BlockState state = ship.getStorageBlocks().get(localPos);
        if (state != null && state.is(ExampleMod.ROCKET_SEAT.get())) {
            if (tryMountRocketSeat(player, ship)) {
                event.setCanceled(true);
                event.setCancellationResult(net.minecraft.world.InteractionResult.SUCCESS);
            }
            return;
        }
        ItemStack stack = player.getItemInHand(event.getHand());
        if (stack.getItem() instanceof BlockItem) {
            handleBlockPlace(player, ship, shipHit.getLocalHit(), event.getHand());
            event.setCanceled(true);
            event.setCancellationResult(net.minecraft.world.InteractionResult.SUCCESS);
        }
    }

    private static boolean isInteractiveShipBlock(BlockState state) {
        return state != null && (state.is(ExampleMod.ROCKET_SEAT.get())
                || state.is(ExampleMod.FUEL_TANK.get())
                || state.is(ExampleMod.AVIONICS_BAY.get()));
    }

    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!(event.getTarget() instanceof RocketShipEntity ship)) return;
        if (ship.getStorageBlocks().isEmpty()) return;
        RocketNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                new RocketShipStructurePacket(ship.getId(), ship.getAnchorVec(), ship.getStorageBlocks()));
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRightClickEmpty(PlayerInteractEvent.RightClickEmpty event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (tryShipBlockInteract(player, event.getHand(), true)) {
            event.setCanceled(true);
            event.setCancellationResult(net.minecraft.world.InteractionResult.SUCCESS);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        UnifiedHit hit = RocketShipRaycastUtils.unifiedClip(player.level(), makeClipContext(player), true);
        if (!(hit instanceof UnifiedHit.Ship shipHit) || shipHit.getLocalHit().getType() != HitResult.Type.BLOCK) return;
        BlockState state = shipHit.getShip().getStorageBlocks().get(shipHit.getLocalHit().getBlockPos());
        boolean hasBlockItem = player.getItemInHand(event.getHand()).getItem() instanceof BlockItem;
        if (!isInteractiveShipBlock(state) && !hasBlockItem) return;
        RocketShipEntity ship = shipHit.getShip();
        BlockPos localPos = shipHit.getLocalHit().getBlockPos();
        if (state.is(ExampleMod.ROCKET_SEAT.get())) {
            if (tryMountRocketSeat(player, ship)) {
                event.setCanceled(true);
                event.setCancellationResult(net.minecraft.world.InteractionResult.SUCCESS);
            }
            return;
        }
        if (state.is(ExampleMod.FUEL_TANK.get())) {
            if (tryRefuelShip(player, ship)) {
                event.setCanceled(true);
                event.setCancellationResult(net.minecraft.world.InteractionResult.SUCCESS);
            }
            return;
        }
        if (state.is(ExampleMod.AVIONICS_BAY.get()) && ship.getShipId() >= 0) {
            net.minecraftforge.network.NetworkHooks.openScreen(player,
                    new net.minecraft.world.SimpleMenuProvider(
                            (id, inv, p) -> com.example.examplemod.rocket.RocketAvionicsMenu.forEntity(id, inv, ship.getShipId()),
                            net.minecraft.network.chat.Component.translatable("menu.rocketwa.avionics")),
                    buf -> { buf.writeBoolean(true); buf.writeLong(ship.getShipId()); });
            event.setCanceled(true);
            event.setCancellationResult(net.minecraft.world.InteractionResult.SUCCESS);
            return;
        }
        ItemStack stack = player.getItemInHand(event.getHand());
        if (stack.getItem() instanceof BlockItem) {
            handleBlockPlace(player, ship, shipHit.getLocalHit(), event.getHand());
            event.setCanceled(true);
        }
    }

    private static boolean tryRefuelShip(ServerPlayer player, RocketShipEntity ship) {
        ItemStack main = player.getItemInHand(InteractionHand.MAIN_HAND);
        ItemStack off = player.getItemInHand(InteractionHand.OFF_HAND);
        if (!RocketFuelHelper.isRocketFuelItem(main) && !RocketFuelHelper.isRocketFuelItem(off)) {
            player.displayClientMessage(Component.translatable("message.rocketwa.refuel_need_fuel"), true);
            return false;
        }
        if (ship.getShipId() < 0) {
            player.displayClientMessage(Component.translatable("message.rocketwa.refuel_no_avionics"), true);
            return false;
        }
        RocketAvionicsBayBlockEntity avionics = RocketControlRegistry.get(ship.getShipId());
        if (avionics == null) {
            player.displayClientMessage(Component.translatable("message.rocketwa.refuel_no_avionics"), true);
            return false;
        }
        ItemStack held;
        InteractionHand hand;
        if (RocketFuelHelper.isRocketFuelItem(main)) {
            held = main;
            hand = InteractionHand.MAIN_HAND;
        } else {
            held = off;
            hand = InteractionHand.OFF_HAND;
        }
        double add = RocketFuelHelper.tryRefuel(avionics.getFuelMass(), held);
        if (add < 0.01) {
            player.displayClientMessage(Component.translatable("message.rocketwa.refuel_full_or_invalid"), true);
            return false;
        }
        avionics.addFuel(add);
        if (!player.getAbilities().instabuild) {
            held.shrink(1);
            if (held.isEmpty()) player.setItemInHand(hand, ItemStack.EMPTY);
        }
        player.displayClientMessage(Component.translatable("message.rocketwa.refueled", String.format("%.1f", add)), true);
        return true;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (tryShipBlockInteract(player, event.getHand(), true)) {
            event.setCanceled(true);
            event.setCancellationResult(net.minecraft.world.InteractionResult.SUCCESS);
            return;
        }
        UnifiedHit hit = RocketShipRaycastUtils.unifiedClip(player.level(), makeClipContext(player), true);
        if (hit instanceof UnifiedHit.Ship shipHit && shipHit.getLocalHit().getType() == HitResult.Type.BLOCK) {
            ItemStack stack = player.getItemInHand(event.getHand());
            if (stack.getItem() instanceof BlockItem) {
                handleBlockPlace(player, shipHit.getShip(), shipHit.getLocalHit(), event.getHand());
                event.setCanceled(true);
                event.setCancellationResult(net.minecraft.world.InteractionResult.SUCCESS);
            }
        }
    }

    public static boolean handleRaycastBreak(ServerPlayer player) {
        UnifiedHit hit = RocketShipRaycastUtils.unifiedClip(player.level(), makeClipContext(player), true);
        if (!(hit instanceof UnifiedHit.Ship shipHit) || shipHit.getLocalHit().getType() != HitResult.Type.BLOCK) {
            return false;
        }
        handleBlockBreak(player, shipHit.getShip(), shipHit.getLocalHit());
        return true;
    }

    private static void handleBlockBreak(ServerPlayer player, RocketShipEntity ship, BlockHitResult hit) {
        BlockPos localPos = hit.getBlockPos();
        BlockState brokenState = ship.getStorageBlocks().get(localPos);

        RocketBlockModificationHandler handler = new RocketBlockModificationHandler(ship);
        boolean success = handler.breakBlock(localPos, player);

        if (success) {
            ServerLevel buildLevel = RocketDimensions.getBuildLevel(player.server);
            if (buildLevel != null) {
                var chunkManager = ship.getChunkManager();
                if (chunkManager != null) {
                    BlockPos worldPos = ship.getBuildOrigin().offset(localPos);
                    chunkManager.removeBlock(buildLevel, worldPos);
                }
            }
            checkDisassembleIfNoAvionicsAfterBreak(ship, player, brokenState != null && brokenState.is(ExampleMod.AVIONICS_BAY.get()));
        }
    }

    public static void checkDisassembleIfNoAvionicsAfterBreak(RocketShipEntity ship, ServerPlayer player, boolean brokenBlockWasAvionics) {
        if (!brokenBlockWasAvionics) return;
        if (countAvionicsBays(ship) != 0) return;
        disassembleRocket(ship, player);
    }

    private static int countAvionicsBays(RocketShipEntity ship) {
        int n = 0;
        for (BlockState state : ship.getStorageBlocks().values()) {
            if (state != null && state.is(ExampleMod.AVIONICS_BAY.get())) n++;
        }
        return n;
    }

    private static void disassembleRocket(RocketShipEntity ship, ServerPlayer triggeringPlayer) {
        if (!(ship.level() instanceof ServerLevel level)) return;
        net.minecraft.world.phys.Vec3 center = ship.position();
        for (java.util.Map.Entry<BlockPos, BlockState> e : ship.getStorageBlocks().entrySet()) {
            BlockPos localPos = e.getKey();
            BlockState state = e.getValue();
            if (state == null || state.isAir()) continue;
            net.minecraft.world.phys.Vec3 worldVec = ship.toGlobalVector(
                    new net.minecraft.world.phys.Vec3(localPos.getX() + 0.5, localPos.getY() + 0.5, localPos.getZ() + 0.5), 1.0f);
            BlockPos worldPos = BlockPos.containing(worldVec.x, worldVec.y, worldVec.z);
            java.util.List<ItemStack> drops = Block.getDrops(state, level, worldPos, null, triggeringPlayer, triggeringPlayer.getMainHandItem());
            for (ItemStack stack : drops) {
                if (stack.isEmpty()) continue;
                if (!triggeringPlayer.getAbilities().instabuild) {
                    ItemEntity item = new ItemEntity(level, worldVec.x, worldVec.y, worldVec.z, stack);
                    item.setDefaultPickUpDelay();
                    level.addFreshEntity(item);
                }
            }
        }
        ship.discard();
    }

    private static void handleBlockPlace(ServerPlayer player, RocketShipEntity ship, BlockHitResult hit, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!(stack.getItem() instanceof BlockItem blockItem)) {
            return;
        }

        BlockPos localPos = hit.getBlockPos().relative(hit.getDirection());

        BlockState newState = blockItem.getBlock().defaultBlockState();

        RocketBlockModificationHandler handler = new RocketBlockModificationHandler(ship);
        boolean success = handler.placeBlock(localPos, newState, player);

        if (success) {

            ServerLevel buildLevel = RocketDimensions.getBuildLevel(player.server);
            if (buildLevel != null) {
                var chunkManager = ship.getChunkManager();
                if (chunkManager != null) {
                    BlockPos worldPos = ship.getBuildOrigin().offset(localPos);
                    chunkManager.placeBlock(buildLevel, worldPos, newState);
                }
            }

            if (!player.isCreative()) {
                stack.shrink(1);
            }
        }
    }
}
