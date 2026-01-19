ackage com.example.rocketceg.worldgen;

import com.example.rocketceg.RocketCEGMod;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;

/** 😡 火箭模组群系定义 * 为每个行星创建独特的群系 😡
     */
public class RocketCEGBiomes {
    
    // 😡 ========== 地球群系 ========== 😡
    public static final ResourceKey<Biome> EARTH_PLAINS = createKey("earth_plains");
    public static final ResourceKey<Biome> EARTH_OCEAN = createKey("earth_ocean");
    public static final ResourceKey<Biome> EARTH_MOUNTAINS = createKey("earth_mountains");

    // 😡 ========== 月球群系 ========== 😡
    public static final ResourceKey<Biome> MOON_SURFACE = createKey("moon_surface");
    public static final ResourceKey<Biome> MOON_CRATER = createKey("moon_crater");

    // 😡 ========== 火星群系 ========== 😡
    public static final ResourceKey<Biome> MARS_DESERT = createKey("mars_desert");
    public static final ResourceKey<Biome> MARS_POLAR = createKey("mars_polar");
    public static final ResourceKey<Biome> MARS_VALLEY = createKey("mars_valley");

    // 😡 ========== 金星群系 ========== 😡
    public static final ResourceKey<Biome> VENUS_SURFACE = createKey("venus_surface");
    public static final ResourceKey<Biome> VENUS_VOLCANIC = createKey("venus_volcanic");

    // 😡 ========== 水星群系 ========== 😡
    public static final ResourceKey<Biome> MERCURY_DAY = createKey("mercury_day");
    public static final ResourceKey<Biome> MERCURY_NIGHT = createKey("mercury_night");

    // 😡 ========== 木星群系（气态行星）========== 😡
    public static final ResourceKey<Biome> JUPITER_CLOUDS = createKey("jupiter_clouds");
    public static final ResourceKey<Biome> JUPITER_STORM = createKey("jupiter_storm");

    // 😡 ========== 土星群系 ========== 😡
    public static final ResourceKey<Biome> SATURN_CLOUDS = createKey("saturn_clouds");
    public static final ResourceKey<Biome> SATURN_RINGS = createKey("saturn_rings");

    // 😡 ========== 天王星群系 ========== 😡
    public static final ResourceKey<Biome> URANUS_ICE = createKey("uranus_ice");
    public static final ResourceKey<Biome> URANUS_CLOUDS = createKey("uranus_clouds");

    // 😡 ========== 海王星群系 ========== 😡
    public static final ResourceKey<Biome> NEPTUNE_ICE = createKey("neptune_ice");
    public static final ResourceKey<Biome> NEPTUNE_STORM = createKey("neptune_storm");

    // 😡 ========== 冥王星群系 ========== 😡
    public static final ResourceKey<Biome> PLUTO_SURFACE = createKey("pluto_surface");
    public static final ResourceKey<Biome> PLUTO_ICE = createKey("pluto_ice");

    // 😡 ========== 轨道群系（黑色天空）========== 😡
    public static final ResourceKey<Biome> SPACE_ORBIT = createKey("space_orbit");

    // 😡 ========== 辅助方法 ========== 😡

    private static ResourceKey<Biome> createKey(final String name) {
        return ResourceKey.create(Registries.BIOME, new ResourceLocation(RocketCEGMod.MOD_ID, name));
    }

    /** 😡 注册所有群系 😡
     */
    public static void bootstrap(final BootstapContext<Biome> context) {
        final HolderGetter<PlacedFeature> placedFeatures = context.lookup(Registries.PLACED_FEATURE);
        final HolderGetter<ConfiguredWorldCarver<?>> worldCarvers = context.lookup(Registries.CONFIGURED_CARVER);

        // 😡 地球群系 😡
        context.register(EARTH_PLAINS, createEarthPlains(placedFeatures, worldCarvers));
        context.register(EARTH_OCEAN, createEarthOcean(placedFeatures, worldCarvers));
        context.register(EARTH_MOUNTAINS, createEarthMountains(placedFeatures, worldCarvers));

        // 😡 月球群系 😡
        context.register(MOON_SURFACE, createMoonSurface(placedFeatures, worldCarvers));
        context.register(MOON_CRATER, createMoonCrater(placedFeatures, worldCarvers));

        // 😡 火星群系 😡
        context.register(MARS_DESERT, createMarsDesert(placedFeatures, worldCarvers));
        context.register(MARS_POLAR, createMarsPolar(placedFeatures, worldCarvers));
        context.register(MARS_VALLEY, createMarsValley(placedFeatures, worldCarvers));

        // 😡 金星群系 😡
        context.register(VENUS_SURFACE, createVenusSurface(placedFeatures, worldCarvers));
        context.register(VENUS_VOLCANIC, createVenusVolcanic(placedFeatures, worldCarvers));

        // 😡 水星群系 😡
        context.register(MERCURY_DAY, createMercuryDay(placedFeatures, worldCarvers));
        context.register(MERCURY_NIGHT, createMercuryNight(placedFeatures, worldCarvers));

        // 😡 木星群系 😡
        context.register(JUPITER_CLOUDS, createJupiterClouds(placedFeatures, worldCarvers));
        context.register(JUPITER_STORM, createJupiterStorm(placedFeatures, worldCarvers));

        // 😡 土星群系 😡
        context.register(SATURN_CLOUDS, createSaturnClouds(placedFeatures, worldCarvers));
        context.register(SATURN_RINGS, createSaturnRings(placedFeatures, worldCarvers));

        // 😡 天王星群系 😡
        context.register(URANUS_ICE, createUranusIce(placedFeatures, worldCarvers));
        context.register(URANUS_CLOUDS, createUranusClouds(placedFeatures, worldCarvers));

        // 😡 海王星群系 😡
        context.register(NEPTUNE_ICE, createNeptuneIce(placedFeatures, worldCarvers));
        context.register(NEPTUNE_STORM, createNeptuneStorm(placedFeatures, worldCarvers));

        // 😡 冥王星群系 😡
        context.register(PLUTO_SURFACE, createPlutoSurface(placedFeatures, worldCarvers));
        context.register(PLUTO_ICE, createPlutoIce(placedFeatures, worldCarvers));

        // 😡 轨道群系（黑色天空） 😡
        context.register(SPACE_ORBIT, createSpaceOrbit(placedFeatures, worldCarvers));
    }

    // 😡 ========== 群系创建方法 ========== 😡

    private static Biome createEarthPlains(
        final HolderGetter<PlacedFeature> placedFeatures,
        final HolderGetter<ConfiguredWorldCarver<?>> worldCarvers
    ) {
        return new Biome.BiomeBuilder()
            .hasPrecipitation(true)
            .temperature(0.8f)
            .downfall(0.4f)
            .specialEffects(new BiomeSpecialEffects.Builder()
                .waterColor(0x3F76E4)
                .waterFogColor(0x050533)
                .fogColor(0xC0D8FF)
                .skyColor(0x78A7FF)
                .build())
            .mobSpawnSettings(new MobSpawnSettings.Builder().build())
            .generationSettings(new BiomeGenerationSettings.Builder(placedFeatures, worldCarvers)
                .build())
            .build();
    }

    private static Biome createEarthOcean(
        final HolderGetter<PlacedFeature> placedFeatures,
        final HolderGetter<ConfiguredWorldCarver<?>> worldCarvers
    ) {
        return new Biome.BiomeBuilder()
            .hasPrecipitation(true)
            .temperature(0.5f)
            .downfall(0.5f)
            .specialEffects(new BiomeSpecialEffects.Builder()
                .waterColor(0x1787D4)
                .waterFogColor(0x050533)
                .fogColor(0xC0D8FF)
                .skyColor(0x78A7FF)
                .build())
            .mobSpawnSettings(new MobSpawnSettings.Builder().build())
            .generationSettings(new BiomeGenerationSettings.Builder(placedFeatures, worldCarvers)
                .build())
            .build();
    }

    private static Biome createEarthMountains(
        final HolderGetter<PlacedFeature> placedFeatures,
        final HolderGetter<ConfiguredWorldCarver<?>> worldCarvers
    ) {
        return new Biome.BiomeBuilder()
            .hasPrecipitation(true)
            .temperature(0.2f)
            .downfall(0.3f)
            .specialEffects(new BiomeSpecialEffects.Builder()
                .waterColor(0x3F76E4)
                .waterFogColor(0x050533)
                .fogColor(0xC0D8FF)
                .skyColor(0x78A7FF)
                .build())
            .mobSpawnSettings(new MobSpawnSettings.Builder().build())
            .generationSettings(new BiomeGenerationSettings.Builder(placedFeatures, worldCarvers)
                .build())
            .build();
    }

    private static Biome createMoonSurface(
        final HolderGetter<PlacedFeature> placedFeatures,
        final HolderGetter<ConfiguredWorldCarver<?>> worldCarvers
    ) {
        return new Biome.BiomeBuilder()
            .hasPrecipitation(false)
            .temperature(0.0f)
            .downfall(0.0f)
            .specialEffects(new BiomeSpecialEffects.Builder()
                .waterColor(0x3F76E4)
                .waterFogColor(0x050533)
                .fogColor(0x000000)
                .skyColor(0x000000)
                .build())
            .mobSpawnSettings(new MobSpawnSettings.Builder().build())
            .generationSettings(new BiomeGenerationSettings.Builder(placedFeatures, worldCarvers)
                .build())
            .build();
    }

    private static Biome createMoonCrater(
        final HolderGetter<PlacedFeature> placedFeatures,
        final HolderGetter<ConfiguredWorldCarver<?>> worldCarvers
    ) {
        return new Biome.BiomeBuilder()
            .hasPrecipitation(false)
            .temperature(0.0f)
            .downfall(0.0f)
            .specialEffects(new BiomeSpecialEffects.Builder()
                .waterColor(0x3F76E4)
                .waterFogColor(0x050533)
                .fogColor(0x000000)
                .skyColor(0x000000)
                .build())
            .mobSpawnSettings(new MobSpawnSettings.Builder().build())
            .generationSettings(new BiomeGenerationSettings.Builder(placedFeatures, worldCarvers)
                .build())
            .build();
    }

    private static Biome createMarsDesert(
        final HolderGetter<PlacedFeature> placedFeatures,
        final HolderGetter<ConfiguredWorldCarver<?>> worldCarvers
    ) {
        return new Biome.BiomeBuilder()
            .hasPrecipitation(false)
            .temperature(2.0f)
            .downfall(0.0f)
            .specialEffects(new BiomeSpecialEffects.Builder()
                .waterColor(0x3F76E4)
                .waterFogColor(0x050533)
                .fogColor(0xFF6B35)
                .skyColor(0xFF6B35)
                .build())
            .mobSpawnSettings(new MobSpawnSettings.Builder().build())
            .generationSettings(new BiomeGenerationSettings.Builder(placedFeatures, worldCarvers)
                .build())
            .build();
    }

    private static Biome createMarsPolar(
        final HolderGetter<PlacedFeature> placedFeatures,
        final HolderGetter<ConfiguredWorldCarver<?>> worldCarvers
    ) {
        return new Biome.BiomeBuilder()
            .hasPrecipitation(false)
            .temperature(0.0f)
            .downfall(0.0f)
            .specialEffects(new BiomeSpecialEffects.Builder()
                .waterColor(0x3F76E4)
                .waterFogColor(0x050533)
                .fogColor(0xFFFFFF)
                .skyColor(0xFF6B35)
                .build())
            .mobSpawnSettings(new MobSpawnSettings.Builder().build())
            .generationSettings(new BiomeGenerationSettings.Builder(placedFeatures, worldCarvers)
                .build())
            .build();
    }

    private static Biome createMarsValley(
        final HolderGetter<PlacedFeature> placedFeatures,
        final HolderGetter<ConfiguredWorldCarver<?>> worldCarvers
    ) {
        return new Biome.BiomeBuilder()
            .hasPrecipitation(false)
            .temperature(1.5f)
            .downfall(0.0f)
            .specialEffects(new BiomeSpecialEffects.Builder()
                .waterColor(0x3F76E4)
                .waterFogColor(0x050533)
                .fogColor(0xFF6B35)
                .skyColor(0xFF6B35)
                .build())
            .mobSpawnSettings(new MobSpawnSettings.Builder().build())
            .generationSettings(new BiomeGenerationSettings.Builder(placedFeatures, worldCarvers)
                .build())
            .build();
    }

    private static Biome createVenusSurface(
        final HolderGetter<PlacedFeature> placedFeatures,
        final HolderGetter<ConfiguredWorldCarver<?>> worldCarvers
    ) {
        return new Biome.BiomeBuilder()
            .hasPrecipitation(true)
            .temperature(4.0f)
            .downfall(1.0f)
            .specialEffects(new BiomeSpecialEffects.Builder()
                .waterColor(0x3F76E4)
                .waterFogColor(0x050533)
                .fogColor(0xFFA500)
                .skyColor(0xFFA500)
                .build())
            .mobSpawnSettings(new MobSpawnSettings.Builder().build())
            .generationSettings(new BiomeGenerationSettings.Builder(placedFeatures, worldCarvers)
                .build())
            .build();
    }

    private static Biome createVenusVolcanic(
        final HolderGetter<PlacedFeature> placedFeatures,
        final HolderGetter<ConfiguredWorldCarver<?>> worldCarvers
    ) {
        return new Biome.BiomeBuilder()
            .hasPrecipitation(true)
            .temperature(5.0f)
            .downfall(1.0f)
            .specialEffects(new BiomeSpecialEffects.Builder()
                .waterColor(0x3F76E4)
                .waterFogColor(0x050533)
                .fogColor(0xFF4500)
                .skyColor(0xFF4500)
                .build())
            .mobSpawnSettings(new MobSpawnSettings.Builder().build())
            .generationSettings(new BiomeGenerationSettings.Builder(placedFeatures, worldCarvers)
                .build())
            .build();
    }

    private static Biome createMercuryDay(
        final HolderGetter<PlacedFeature> placedFeatures,
        final HolderGetter<ConfiguredWorldCarver<?>> worldCarvers
    ) {
        return new Biome.BiomeBuilder()
            .hasPrecipitation(false)
            .temperature(5.0f)
            .downfall(0.0f)
            .specialEffects(new BiomeSpecialEffects.Builder()
                .waterColor(0x3F76E4)
                .waterFogColor(0x050533)
                .fogColor(0xFFFFFF)
                .skyColor(0xFFFFFF)
                .build())
            .mobSpawnSettings(new MobSpawnSettings.Builder().build())
            .generationSettings(new BiomeGenerationSettings.Builder(placedFeatures, worldCarvers)
                .build())
            .build();
    }

    private static Biome createMercuryNight(
        final HolderGetter<PlacedFeature> placedFeatures,
        final HolderGetter<ConfiguredWorldCarver<?>> worldCarvers
    ) {
        return new Biome.BiomeBuilder()
            .hasPrecipitation(false)
            .temperature(-2.0f)
            .downfall(0.0f)
            .specialEffects(new BiomeSpecialEffects.Builder()
                .waterColor(0x3F76E4)
                .waterFogColor(0x050533)
                .fogColor(0x000000)
                .skyColor(0x000000)
                .build())
            .mobSpawnSettings(new MobSpawnSettings.Builder().build())
            .generationSettings(new BiomeGenerationSettings.Builder(placedFeatures, worldCarvers)
                .build())
            .build();
    }

    private static Biome createJupiterClouds(
        final HolderGetter<PlacedFeature> placedFeatures,
        final HolderGetter<ConfiguredWorldCarver<?>> worldCarvers
    ) {
        return new Biome.BiomeBuilder()
            .hasPrecipitation(false)
            .temperature(0.5f)
            .downfall(0.0f)
            .specialEffects(new BiomeSpecialEffects.Builder()
                .waterColor(0x3F76E4)
                .waterFogColor(0x050533)
                .fogColor(0xD2691E)
                .skyColor(0xD2691E)
                .build())
            .mobSpawnSettings(new MobSpawnSettings.Builder().build())
            .generationSettings(new BiomeGenerationSettings.Builder(placedFeatures, worldCarvers)
                .build())
            .build();
    }

    private static Biome createJupiterStorm(
        final HolderGetter<PlacedFeature> placedFeatures,
        final HolderGetter<ConfiguredWorldCarver<?>> worldCarvers
    ) {
        return new Biome.BiomeBuilder()
            .hasPrecipitation(false)
            .temperature(0.3f)
            .downfall(0.0f)
            .specialEffects(new BiomeSpecialEffects.Builder()
                .waterColor(0x3F76E4)
                .waterFogColor(0x050533)
                .fogColor(0x8B4513)
                .skyColor(0x8B4513)
                .build())
            .mobSpawnSettings(new MobSpawnSettings.Builder().build())
            .generationSettings(new BiomeGenerationSettings.Builder(placedFeatures, worldCarvers)
                .build())
            .build();
    }

    private static Biome createSaturnClouds(
        final HolderGetter<PlacedFeature> placedFeatures,
        final HolderGetter<ConfiguredWorldCarver<?>> worldCarvers
    ) {
        return new Biome.BiomeBuilder()
            .hasPrecipitation(false)
            .temperature(0.4f)
            .downfall(0.0f)
            .specialEffects(new BiomeSpecialEffects.Builder()
                .waterColor(0x3F76E4)
                .waterFogColor(0x050533)
                .fogColor(0xFAD5A5)
                .skyColor(0xFAD5A5)
                .build())
            .mobSpawnSettings(new MobSpawnSettings.Builder().build())
            .generationSettings(new BiomeGenerationSettings.Builder(placedFeatures, worldCarvers)
                .build())
            .build();
    }

    private static Biome createSaturnRings(
        final HolderGetter<PlacedFeature> placedFeatures,
        final HolderGetter<ConfiguredWorldCarver<?>> worldCarvers
    ) {
        return new Biome.BiomeBuilder()
            .hasPrecipitation(false)
            .temperature(0.2f)
            .downfall(0.0f)
            .specialEffects(new BiomeSpecialEffects.Builder()
                .waterColor(0x3F76E4)
                .waterFogColor(0x050533)
                .fogColor(0xE6E6FA)
                .skyColor(0xE6E6FA)
                .build())
            .mobSpawnSettings(new MobSpawnSettings.Builder().build())
            .generationSettings(new BiomeGenerationSettings.Builder(placedFeatures, worldCarvers)
                .build())
            .build();
    }

    private static Biome createUranusIce(
        final HolderGetter<PlacedFeature> placedFeatures,
        final HolderGetter<ConfiguredWorldCarver<?>> worldCarvers
    ) {
        return new Biome.BiomeBuilder()
            .hasPrecipitation(false)
            .temperature(-1.0f)
            .downfall(0.0f)
            .specialEffects(new BiomeSpecialEffects.Builder()
                .waterColor(0x3F76E4)
                .waterFogColor(0x050533)
                .fogColor(0x4FD0E7)
                .skyColor(0x4FD0E7)
                .build())
            .mobSpawnSettings(new MobSpawnSettings.Builder().build())
            .generationSettings(new BiomeGenerationSettings.Builder(placedFeatures, worldCarvers)
                .build())
            .build();
    }

    private static Biome createUranusClouds(
        final HolderGetter<PlacedFeature> placedFeatures,
        final HolderGetter<ConfiguredWorldCarver<?>> worldCarvers
    ) {
        return new Biome.BiomeBuilder()
            .hasPrecipitation(false)
            .temperature(-0.5f)
            .downfall(0.0f)
            .specialEffects(new BiomeSpecialEffects.Builder()
                .waterColor(0x3F76E4)
                .waterFogColor(0x050533)
                .fogColor(0x87CEEB)
                .skyColor(0x87CEEB)
                .build())
            .mobSpawnSettings(new MobSpawnSettings.Builder().build())
            .generationSettings(new BiomeGenerationSettings.Builder(placedFeatures, worldCarvers)
                .build())
            .build();
    }

    private static Biome createNeptuneIce(
        final HolderGetter<PlacedFeature> placedFeatures,
        final HolderGetter<ConfiguredWorldCarver<?>> worldCarvers
    ) {
        return new Biome.BiomeBuilder()
            .hasPrecipitation(false)
            .temperature(-1.5f)
            .downfall(0.0f)
            .specialEffects(new BiomeSpecialEffects.Builder()
                .waterColor(0x3F76E4)
                .waterFogColor(0x050533)
                .fogColor(0x0000CD)
                .skyColor(0x0000CD)
                .build())
            .mobSpawnSettings(new MobSpawnSettings.Builder().build())
            .generationSettings(new BiomeGenerationSettings.Builder(placedFeatures, worldCarvers)
                .build())
            .build();
    }

    private static Biome createNeptuneStorm(
        final HolderGetter<PlacedFeature> placedFeatures,
        final HolderGetter<ConfiguredWorldCarver<?>> worldCarvers
    ) {
        return new Biome.BiomeBuilder()
            .hasPrecipitation(false)
            .temperature(-1.0f)
            .downfall(0.0f)
            .specialEffects(new BiomeSpecialEffects.Builder()
                .waterColor(0x3F76E4)
                .waterFogColor(0x050533)
                .fogColor(0x191970)
                .skyColor(0x191970)
                .build())
            .mobSpawnSettings(new MobSpawnSettings.Builder().build())
            .generationSettings(new BiomeGenerationSettings.Builder(placedFeatures, worldCarvers)
                .build())
            .build();
    }

    private static Biome createPlutoSurface(
        final HolderGetter<PlacedFeature> placedFeatures,
        final HolderGetter<ConfiguredWorldCarver<?>> worldCarvers
    ) {
        return new Biome.BiomeBuilder()
            .hasPrecipitation(false)
            .temperature(-2.0f)
            .downfall(0.0f)
            .specialEffects(new BiomeSpecialEffects.Builder()
                .waterColor(0x3F76E4)
                .waterFogColor(0x050533)
                .fogColor(0x000000)
                .skyColor(0x000000)
                .build())
            .mobSpawnSettings(new MobSpawnSettings.Builder().build())
            .generationSettings(new BiomeGenerationSettings.Builder(placedFeatures, worldCarvers)
                .build())
            .build();
    }

    private static Biome createPlutoIce(
        final HolderGetter<PlacedFeature> placedFeatures,
        final HolderGetter<ConfiguredWorldCarver<?>> worldCarvers
    ) {
        return new Biome.BiomeBuilder()
            .hasPrecipitation(false)
            .temperature(-2.5f)
            .downfall(0.0f)
            .specialEffects(new BiomeSpecialEffects.Builder()
                .waterColor(0x3F76E4)
                .waterFogColor(0x050533)
                .fogColor(0x000000)
                .skyColor(0x000000)
                .build())
            .mobSpawnSettings(new MobSpawnSettings.Builder().build())
            .generationSettings(new BiomeGenerationSettings.Builder(placedFeatures, worldCarvers)
                .build())
            .build();
    }

    /** 😡 创建轨道群系 - 黑色天空，用于所有轨道维度 😡
     */
    private static Biome createSpaceOrbit(
        final HolderGetter<PlacedFeature> placedFeatures,
        final HolderGetter<ConfiguredWorldCarver<?>> worldCarvers
    ) {
        return new Biome.BiomeBuilder()
            .hasPrecipitation(false)
            .temperature(0.0f)
            .downfall(0.0f)
            .specialEffects(new BiomeSpecialEffects.Builder()
                .waterColor(0x3F76E4)
                .waterFogColor(0x050533)
                .fogColor(0x000000)  // 😡 黑色雾 😡
                .skyColor(0x000000)  // 😡 黑色天空 😡
                .build())
            .mobSpawnSettings(new MobSpawnSettings.Builder().build())
            .generationSettings(new BiomeGenerationSettings.Builder(placedFeatures, worldCarvers)
                .build())
            .build();
    }
}
