package com.example.rocketceg.dimension;

import com.example.rocketceg.RocketCEGMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;

import java.util.HashMap;
import java.util.Map;

/** 😡 火箭模组维度注册表 * 为太阳系每个行星注册地表维度和轨道维度 😡
     */
public class RocketCEGDimensions {
    private static final Map<String, ResourceKey<Level>> SURFACE_DIMENSIONS = new HashMap<>();
    private static final Map<String, ResourceKey<Level>> ORBIT_DIMENSIONS = new HashMap<>();

    // 😡 ========== 太阳系行星维度 ========== 😡

    // 😡 地球 😡
    public static final ResourceKey<Level> EARTH_SURFACE = createSurfaceDimension("earth");
    public static final ResourceKey<Level> EARTH_ORBIT = createOrbitDimension("earth");

    // 😡 月球 😡
    public static final ResourceKey<Level> MOON_SURFACE = createSurfaceDimension("moon");
    public static final ResourceKey<Level> MOON_ORBIT = createOrbitDimension("moon");

    // 😡 火星 😡
    public static final ResourceKey<Level> MARS_SURFACE = createSurfaceDimension("mars");
    public static final ResourceKey<Level> MARS_ORBIT = createOrbitDimension("mars");

    // 😡 金星 😡
    public static final ResourceKey<Level> VENUS_SURFACE = createSurfaceDimension("venus");
    public static final ResourceKey<Level> VENUS_ORBIT = createOrbitDimension("venus");

    // 😡 水星 😡
    public static final ResourceKey<Level> MERCURY_SURFACE = createSurfaceDimension("mercury");
    public static final ResourceKey<Level> MERCURY_ORBIT = createOrbitDimension("mercury");

    // 😡 木星 😡
    public static final ResourceKey<Level> JUPITER_SURFACE = createSurfaceDimension("jupiter");
    public static final ResourceKey<Level> JUPITER_ORBIT = createOrbitDimension("jupiter");

    // 😡 土星 😡
    public static final ResourceKey<Level> SATURN_SURFACE = createSurfaceDimension("saturn");
    public static final ResourceKey<Level> SATURN_ORBIT = createOrbitDimension("saturn");

    // 😡 天王星 😡
    public static final ResourceKey<Level> URANUS_SURFACE = createSurfaceDimension("uranus");
    public static final ResourceKey<Level> URANUS_ORBIT = createOrbitDimension("uranus");

    // 😡 海王星 😡
    public static final ResourceKey<Level> NEPTUNE_SURFACE = createSurfaceDimension("neptune");
    public static final ResourceKey<Level> NEPTUNE_ORBIT = createOrbitDimension("neptune");

    // 😡 冥王星（矮行星） 😡
    public static final ResourceKey<Level> PLUTO_SURFACE = createSurfaceDimension("pluto");
    public static final ResourceKey<Level> PLUTO_ORBIT = createOrbitDimension("pluto");

    // 😡 ========== 辅助方法 ========== 😡

    private static ResourceKey<Level> createSurfaceDimension(final String bodyName) {
        final ResourceLocation location = new ResourceLocation(RocketCEGMod.MOD_ID, bodyName + "_surface");
        final ResourceKey<Level> key = ResourceKey.create(Registries.DIMENSION, location);
        SURFACE_DIMENSIONS.put(bodyName, key);
        return key;
    }

    private static ResourceKey<Level> createOrbitDimension(final String bodyName) {
        final ResourceLocation location = new ResourceLocation(RocketCEGMod.MOD_ID, bodyName + "_orbit");
        final ResourceKey<Level> key = ResourceKey.create(Registries.DIMENSION, location);
        ORBIT_DIMENSIONS.put(bodyName, key);
        return key;
    }

    public static ResourceKey<Level> getSurfaceDimension(final String bodyName) {
        return SURFACE_DIMENSIONS.get(bodyName);
    }

    public static ResourceKey<Level> getOrbitDimension(final String bodyName) {
        return ORBIT_DIMENSIONS.get(bodyName);
    }
}
