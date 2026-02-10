package com.example.examplemod.client;

import com.example.examplemod.ExampleMod;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ExampleMod.MODID, value = Dist.CLIENT)
public final class RocketFogHandler {

    @SubscribeEvent
    public static void onRenderFog(ViewportEvent.RenderFog event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        float renderDist = mc.gameRenderer.getRenderDistance();

        float fogStart = renderDist * 0.60f;
        float fogEnd = renderDist * 0.95f;

        event.setNearPlaneDistance(fogStart);
        event.setFarPlaneDistance(fogEnd);
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onFogColor(ViewportEvent.ComputeFogColor event) {

        event.setRed(0.005f);
        event.setGreen(0.005f);
        event.setBlue(0.01f);
    }
}
