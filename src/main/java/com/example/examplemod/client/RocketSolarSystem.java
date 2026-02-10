package com.example.examplemod.client;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

public class RocketSolarSystem {
    private static final ResourceLocation SOLAR_SYSTEM = new ResourceLocation("rocketwa", "solarsystem/solar_system.json");
    private static final Gson GSON = new Gson();
    private static RocketSolarSystem cached;

    public String name;
    public List<CelestialBody> bodies = Collections.emptyList();

    public static RocketSolarSystem getOrLoad() {
        if (cached != null) {
            return cached;
        }
        ResourceManager manager = Minecraft.getInstance().getResourceManager();
        try {
            Resource resource = manager.getResource(SOLAR_SYSTEM).orElseThrow();
            try (InputStreamReader reader = new InputStreamReader(resource.open(), StandardCharsets.UTF_8)) {
                cached = GSON.fromJson(reader, RocketSolarSystem.class);
                return cached;
            }
        } catch (Exception ex) {
            cached = new RocketSolarSystem();
            return cached;
        }
    }

    public static class CelestialBody {
        public String name;
        public float orbitDistance;
        public float orbitPeriodDays;
        public float radius;
        public int color;
        public float gravity;

        public double radiusMeters = 0;

        public double orbitSemiMajorAxisMeters = 0;

        public float inclinationDeg = 0;

        public double getRadiusMeters() {
            return radiusMeters > 0 ? radiusMeters : (radius * 1e6);
        }

        public double getOrbitMeters() {
            return orbitSemiMajorAxisMeters > 0 ? orbitSemiMajorAxisMeters : (orbitDistance * 1e9);
        }
    }
}
