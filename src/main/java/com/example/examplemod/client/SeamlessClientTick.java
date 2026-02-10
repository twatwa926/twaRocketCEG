package com.example.examplemod.client;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.rocket.SeamlessCore;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ExampleMod.MODID, value = Dist.CLIENT)
public final class SeamlessClientTick {

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            SeamlessCore.updateBeforeFrameRendering();
        }
    }
}
