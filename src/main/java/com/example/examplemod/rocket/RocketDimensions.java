package com.example.examplemod.rocket;

import com.example.examplemod.ExampleMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public final class RocketDimensions {
    public static final ResourceKey<Level> ROCKET_BUILD_LEVEL = ResourceKey.create(
            Registries.DIMENSION,
            new ResourceLocation(ExampleMod.MODID, "rocket_build")
    );

    public static final ResourceKey<Level> SUN = dim("sun");
    public static final ResourceKey<Level> MERCURY = dim("mercury");
    public static final ResourceKey<Level> VENUS = dim("venus");
    public static final ResourceKey<Level> EARTH = dim("earth");
    public static final ResourceKey<Level> EARTH_ORBIT = dim("earth_orbit");
    public static final ResourceKey<Level> MOON = dim("moon");
    public static final ResourceKey<Level> MARS = dim("mars");
    public static final ResourceKey<Level> CERES = dim("ceres");
    public static final ResourceKey<Level> JUPITER = dim("jupiter");
    public static final ResourceKey<Level> SATURN = dim("saturn");
    public static final ResourceKey<Level> URANUS = dim("uranus");
    public static final ResourceKey<Level> NEPTUNE = dim("neptune");
    public static final ResourceKey<Level> PLUTO = dim("pluto");

    private static ResourceKey<Level> dim(String name) {
        return ResourceKey.create(Registries.DIMENSION, new ResourceLocation(ExampleMod.MODID, name));
    }

    private RocketDimensions() {
    }

    public static ServerLevel getBuildLevel(MinecraftServer server) {
        return server.getLevel(ROCKET_BUILD_LEVEL);
    }

    public static ServerLevel getLevel(MinecraftServer server, ResourceKey<Level> key) {
        return server.getLevel(key);
    }

    public static BlockPos buildOriginFor(long shipId) {
        int x = (int) (shipId % 10000L) * 128;
        return new BlockPos(x, 64, 0);
    }
}
