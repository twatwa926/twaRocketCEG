package com.example.examplemod.rocket;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class RocketControlRegistry {
    private static final Map<Long, RocketAvionicsBayBlockEntity> CONTROLLERS = new ConcurrentHashMap<>();

    private RocketControlRegistry() {
    }

    public static void register(long shipId, RocketAvionicsBayBlockEntity controller) {
        CONTROLLERS.put(shipId, controller);
    }

    public static void unregister(long shipId) {
        CONTROLLERS.remove(shipId);
    }

    public static RocketAvionicsBayBlockEntity get(long shipId) {
        return CONTROLLERS.get(shipId);
    }
}
