package com.example.examplemod.rocket;

import com.example.examplemod.ExampleMod;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.Level;

public class RocketAvionicsMenu extends AbstractContainerMenu {
    private final Level level;
    private final BlockPos origin;
    private final long shipId;

    public RocketAvionicsMenu(int id, Inventory inventory, BlockPos origin) {
        super(ExampleMod.AVIONICS_MENU.get(), id);
        this.level = inventory.player.level();
        this.origin = origin;
        this.shipId = -1L;
    }

    public RocketAvionicsMenu(int id, Inventory inventory, long shipId) {
        super(ExampleMod.AVIONICS_MENU.get(), id);
        this.level = inventory.player.level();
        this.origin = null;
        this.shipId = shipId;
    }

    public static RocketAvionicsMenu fromNetwork(int id, Inventory inventory, FriendlyByteBuf buf) {
        boolean isEntity = buf.readBoolean();
        if (isEntity) {
            long shipId = buf.readLong();
            return new RocketAvionicsMenu(id, inventory, shipId);
        }
        BlockPos origin = buf.readBlockPos();
        return new RocketAvionicsMenu(id, inventory, origin);
    }

    public static RocketAvionicsMenu forEntity(int id, Inventory inventory, long shipId) {
        return new RocketAvionicsMenu(id, inventory, shipId);
    }

    public BlockPos getOrigin() {
        return origin;
    }

    public long getShipId() {
        return shipId;
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return false;
        }

        if (id == 0) {
            if (origin == null) {
                return false;
            }
            RocketAssemblyHandler.assembleAt(level, origin, serverPlayer);
            return true;
        }

        return false;
    }

    @Override
    public boolean stillValid(Player player) {
        if (shipId >= 0L) {
            return true;
        }
        if (origin == null) {
            return false;
        }
        return player.distanceToSqr(origin.getX() + 0.5, origin.getY() + 0.5, origin.getZ() + 0.5) <= 64.0;
    }

    @Override
    public net.minecraft.world.item.ItemStack quickMoveStack(Player player, int index) {

        return net.minecraft.world.item.ItemStack.EMPTY;
    }
}
