package com.example.examplemod;

import com.example.examplemod.network.RocketNetwork;
import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.ModList;
import org.slf4j.Logger;

public final class RocketIntegration {
    private static final Logger LOGGER = LogUtils.getLogger();

    private RocketIntegration() {
    }

    public static void init() {
        boolean hasCreate = ModList.get().isLoaded("create");
        boolean hasGtm = ModList.get().isLoaded("gtceu");

        LOGGER.info("Create loaded: {}", hasCreate);
        LOGGER.info("GTM loaded: {}", hasGtm);
        RocketNetwork.init();
    }
}
