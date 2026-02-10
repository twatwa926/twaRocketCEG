package com.example.examplemod.rocket;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

public class RocketVSBridge {
    private static final Logger LOGGER = LogUtils.getLogger();

    private RocketVSBridge() {
    }

    public static void init() {
        LOGGER.info("Valkyrien Skies integration disabled; using Create contraptions instead.");
    }
}
