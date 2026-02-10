package com.example.examplemod.rocket;

import com.example.examplemod.ExampleMod;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ExampleMod.MODID)
public class RocketAssemblyHandler {
    private static final RocketStructureDetector DETECTOR = new RocketStructureDetector();

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        if (event.getHand() != net.minecraft.world.InteractionHand.MAIN_HAND) {
            return;
        }
        if (level.isClientSide()) {
            return;
        }

        BlockHitResult hit = event.getHitVec();
        Block block = level.getBlockState(hit.getBlockPos()).getBlock();
        if (block != ExampleMod.AVIONICS_BAY.get()) {
            return;
        }

        Player player = event.getEntity();
        var be = level.getBlockEntity(hit.getBlockPos());
        if (be instanceof RocketAvionicsBayBlockEntity avionics) {
            ItemStack heldStack = player.getItemInHand(InteractionHand.MAIN_HAND);
            if (RocketFuelHelper.isRocketFuelItem(heldStack)) {
                double currentFuel = avionics.getFuelMass();
                double add = RocketFuelHelper.tryRefuel(currentFuel, heldStack);
                if (add > 0.01) {
                    avionics.addFuel(add);
                    if (!player.getAbilities().instabuild) {
                        heldStack.shrink(1);
                        if (heldStack.isEmpty()) {
                            player.setItemInHand(InteractionHand.MAIN_HAND, net.minecraft.world.item.ItemStack.EMPTY);
                        }
                    }
                    double newFuel = avionics.getFuelMass();
                    player.displayClientMessage(Component.translatable("message.rocketwa.refueled", String.format("%.1f", add)), true);
                    event.setCanceled(true);
                    event.setCancellationResult(InteractionResult.SUCCESS);
                    return;
                } else {

                    player.displayClientMessage(Component.literal("燃料已满或无法添加燃料"), true);
                }
            }
        }

        if (player.isCrouching()) {
            openAvionicsMenu(level, hit.getBlockPos(), player);
        } else {
            assembleAt(level, hit.getBlockPos(), player);
        }
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
    }

    public static void assembleAt(Level level, BlockPos origin, Player player) {
        RocketStructure structure = DETECTOR.scan(level, origin);
        if (!structure.isValid()) {
            player.displayClientMessage(Component.literal("火箭结构无效：需要航电仓/控制电脑/燃料舱/引擎模块"), false);
            return;
        }

        RocketCreateBridge.assembleContraption(level, origin, structure, player);
        player.displayClientMessage(Component.literal("火箭组装完成：阶段数=" + structure.getStageCount()), false);
    }

    private static void openAvionicsMenu(Level level, BlockPos origin, Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        NetworkHooks.openScreen(serverPlayer,
                new SimpleMenuProvider((id, inv, p) -> new RocketAvionicsMenu(id, inv, origin),
                        Component.translatable("menu.rocketwa.avionics")),
                (FriendlyByteBuf buf) -> {
                    buf.writeBoolean(false);
                    buf.writeBlockPos(origin);
                });
    }
}
