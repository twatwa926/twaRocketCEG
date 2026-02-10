package com.example.examplemod.client;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.network.RocketControlInputPacket;
import com.example.examplemod.network.RocketNetwork;
import com.example.examplemod.rocket.ship.RocketShipEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ExampleMod.MODID, value = Dist.CLIENT)
public final class RocketClientInputHandler {
    private RocketClientInputHandler() {
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) {
            return;
        }
        long shipId = RocketClientControlState.getControlledShipId();
        if (minecraft.player.getVehicle() instanceof RocketShipEntity ship) {
            if (ship.getShipId() >= 0L) {
                shipId = ship.getShipId();
                RocketClientControlState.setControlledShipId(shipId);
            }
        } else {
            if (shipId >= 0L) {
                RocketClientControlState.setControlledShipId(-1L);
            }
            return;
        }
        if (shipId < 0L) return;

        Vec3 input = readInput(minecraft);
        RocketNetwork.CHANNEL.sendToServer(new RocketControlInputPacket(shipId, input));
    }

    private static Vec3 readInput(Minecraft minecraft) {
        double forward = minecraft.options.keyUp.isDown() ? 1.0 : 0.0;
        double backward = minecraft.options.keyDown.isDown() ? 1.0 : 0.0;
        double left = minecraft.options.keyLeft.isDown() ? 1.0 : 0.0;
        double right = minecraft.options.keyRight.isDown() ? 1.0 : 0.0;

        double x = right - left;
        double y = Math.max(0.0, forward - backward);
        double z = 0.0;
        return new Vec3(x, y, z);
    }
}
