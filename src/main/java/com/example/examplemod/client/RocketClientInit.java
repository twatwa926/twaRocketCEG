package com.example.examplemod.client;

import com.example.examplemod.ExampleMod;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod.EventBusSubscriber(modid = ExampleMod.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class RocketClientInit {
    private static final Logger LOGGER = LoggerFactory.getLogger(RocketClientInit.class);

    private RocketClientInit() {}

    public static void init(IEventBus modEventBus) {
        System.out.println("[渲染器] ========== RocketClientInit.init 被调用(客户端显式注册) ==========");
        System.err.println("[渲染器] ========== RocketClientInit.init 被调用(客户端显式注册) ==========");
        LOGGER.info("[渲染器] ========== RocketClientInit.init 被调用(客户端显式注册) ==========");

        modEventBus.addListener(RocketClientRenderers::registerRenderers);
        modEventBus.addListener(RocketClientInit::onClientSetup);

        System.out.println("[渲染器] ========== 已显式 addListener(registerRenderers) ==========");
        System.err.println("[渲染器] ========== 已显式 addListener(registerRenderers) ==========");
        LOGGER.info("[渲染器] ========== 已显式 addListener(registerRenderers) ==========");
    }

    private static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(ExampleMod.AVIONICS_MENU.get(), RocketAvionicsScreen::new);
            LOGGER.info("[渲染器] 已注册航电界面 rocketwa:avionics_menu");
        });
    }
}
