package com.example.rocketceg.worldgen;

import com.example.rocketceg.RocketCEGMod;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;

import java.util.OptionalLong;

/** 😡 世界生成数据提供者 * 注册所有行星的维度类型、区块生成器和群系 😡
     */
public class RocketCEGWorldGenProvider {

    /** 😡 注册所有维度类型和世界生成设置 😡
     */
    public static void bootstrapDimensionTypes(final BootstapContext<DimensionType> context) {
        // 😡 地球 😡
        registerDimensionType(context, "earth_surface", true, true, true, false, 1.0, 256, "minecraft:overworld");
        registerDimensionType(context, "earth_orbit", true, false, false, false, 1.0, 256, "minecraft:the_end"); // 😡 使用末地效果，黑色天空 😡

        // 😡 月球 😡
        registerDimensionType(context, "moon_surface", false, false, false, false, 1.0, 256, "minecraft:overworld");
        registerDimensionType(context, "moon_orbit", true, false, false, false, 1.0, 256, "minecraft:the_end");

        // 😡 火星 😡
        registerDimensionType(context, "mars_surface", true, true, true, false, 1.0, 256, "minecraft:overworld");
        registerDimensionType(context, "mars_orbit", true, false, false, false, 1.0, 256, "minecraft:the_end");

        // 😡 金星 😡
        registerDimensionType(context, "venus_surface", true, true, true, true, 1.0, 256, "minecraft:overworld");
        registerDimensionType(context, "venus_orbit", true, false, false, false, 1.0, 256, "minecraft:the_end");

        // 😡 水星 😡
        registerDimensionType(context, "mercury_surface", false, false, false, false, 1.0, 256, "minecraft:overworld");
        registerDimensionType(context, "mercury_orbit", true, false, false, false, 1.0, 256, "minecraft:the_end");

        // 😡 木星 😡
        registerDimensionType(context, "jupiter_surface", true, true, false, false, 1.0, 256, "minecraft:overworld");
        registerDimensionType(context, "jupiter_orbit", true, false, false, false, 1.0, 256, "minecraft:the_end");

        // 😡 土星 😡
        registerDimensionType(context, "saturn_surface", true, true, false, false, 1.0, 256, "minecraft:overworld");
        registerDimensionType(context, "saturn_orbit", true, false, false, false, 1.0, 256, "minecraft:the_end");

        // 😡 天王星 😡
        registerDimensionType(context, "uranus_surface", true, true, false, false, 1.0, 256, "minecraft:overworld");
        registerDimensionType(context, "uranus_orbit", true, false, false, false, 1.0, 256, "minecraft:the_end");

        // 😡 海王星 😡
        registerDimensionType(context, "neptune_surface", true, true, false, false, 1.0, 256, "minecraft:overworld");
        registerDimensionType(context, "neptune_orbit", true, false, false, false, 1.0, 256, "minecraft:the_end");

        // 😡 冥王星 😡
        registerDimensionType(context, "pluto_surface", false, false, false, false, 1.0, 256, "minecraft:overworld");
        registerDimensionType(context, "pluto_orbit", true, false, false, false, 1.0, 256, "minecraft:the_end");
    }

    private static void registerDimensionType(
        final BootstapContext<DimensionType> context,
        final String name,
        final boolean hasSkyLight,
        final boolean hasCeiling,
        final boolean natural,
        final boolean ultrawarm,
        final double coordinateScale,
        final int height,
        final String effectsLocation
    ) {
        final ResourceKey<DimensionType> key = ResourceKey.create(
            Registries.DIMENSION_TYPE,
            new ResourceLocation(RocketCEGMod.MOD_ID, name)
        );

        // 😡 创建 MonsterSettings 😡
        final DimensionType.MonsterSettings monsterSettings = new DimensionType.MonsterSettings(
            false, // 😡 piglinSafe 😡
            false, // 😡 hasRaids 😡
            ConstantInt.of(0), // 😡 monsterSpawnLightLevel 😡
            0 // 😡 monsterSpawnBlockLightLimit 😡
        );

        // 😡 获取 infiniburn 标签（使用 minecraft:infiniburn_overworld） 😡
        final TagKey<Block> infiniburn = TagKey.create(
            Registries.BLOCK,
            new ResourceLocation("minecraft", "infiniburn_overworld")
        );

        // 😡 获取 effects 资源位置 😡
        final ResourceLocation effects = new ResourceLocation(effectsLocation);

        // 😡 轨道维度使用较低的 ambientLight，使天空更暗 😡
        final float ambientLight = name.contains("_orbit") ? 0.1f : 1.0f;

        final DimensionType dimensionType = new DimensionType(
            OptionalLong.empty(), // 😡 fixedTime 😡
            hasSkyLight,
            hasCeiling,
            natural,
            ultrawarm,
            coordinateScale,
            false, // 😡 createDragonFight 😡
            true, // 😡 piglinSafe 😡
            height, // 😡 minY 😡
            height, // 😡 height 😡
            256, // 😡 logicalHeight 😡
            infiniburn, // 😡 infiniburn 😡
            effects, // 😡 effects 😡
            ambientLight, // 😡 ambientLight - 轨道维度更暗 😡
            monsterSettings // 😡 monsterSettings 😡
        );

        context.register(key, dimensionType);
    }

    /** 😡 注册所有 LevelStem（维度实例） 😡
     */
    public static void bootstrapLevelStems(
        final BootstapContext<LevelStem> context,
        final HolderGetter<DimensionType> dimensionTypes,
        final HolderGetter<Biome> biomes,
        final HolderGetter<NoiseGeneratorSettings> noiseSettings
    ) {
        // 😡 地球 😡
        registerLevelStem(context, dimensionTypes, biomes, noiseSettings, "earth_surface", RocketCEGBiomes.EARTH_PLAINS);
        registerLevelStem(context, dimensionTypes, biomes, noiseSettings, "earth_orbit", RocketCEGBiomes.SPACE_ORBIT); // 😡 使用黑色天空群系 😡

        // 😡 月球 😡
        registerLevelStem(context, dimensionTypes, biomes, noiseSettings, "moon_surface", RocketCEGBiomes.MOON_SURFACE);
        registerLevelStem(context, dimensionTypes, biomes, noiseSettings, "moon_orbit", RocketCEGBiomes.SPACE_ORBIT);

        // 😡 火星 😡
        registerLevelStem(context, dimensionTypes, biomes, noiseSettings, "mars_surface", RocketCEGBiomes.MARS_DESERT);
        registerLevelStem(context, dimensionTypes, biomes, noiseSettings, "mars_orbit", RocketCEGBiomes.SPACE_ORBIT);

        // 😡 金星 😡
        registerLevelStem(context, dimensionTypes, biomes, noiseSettings, "venus_surface", RocketCEGBiomes.VENUS_SURFACE);
        registerLevelStem(context, dimensionTypes, biomes, noiseSettings, "venus_orbit", RocketCEGBiomes.SPACE_ORBIT);

        // 😡 水星 😡
        registerLevelStem(context, dimensionTypes, biomes, noiseSettings, "mercury_surface", RocketCEGBiomes.MERCURY_DAY);
        registerLevelStem(context, dimensionTypes, biomes, noiseSettings, "mercury_orbit", RocketCEGBiomes.SPACE_ORBIT);

        // 😡 木星 😡
        registerLevelStem(context, dimensionTypes, biomes, noiseSettings, "jupiter_surface", RocketCEGBiomes.JUPITER_CLOUDS);
        registerLevelStem(context, dimensionTypes, biomes, noiseSettings, "jupiter_orbit", RocketCEGBiomes.SPACE_ORBIT);

        // 😡 土星 😡
        registerLevelStem(context, dimensionTypes, biomes, noiseSettings, "saturn_surface", RocketCEGBiomes.SATURN_CLOUDS);
        registerLevelStem(context, dimensionTypes, biomes, noiseSettings, "saturn_orbit", RocketCEGBiomes.SPACE_ORBIT);

        // 😡 天王星 😡
        registerLevelStem(context, dimensionTypes, biomes, noiseSettings, "uranus_surface", RocketCEGBiomes.URANUS_ICE);
        registerLevelStem(context, dimensionTypes, biomes, noiseSettings, "uranus_orbit", RocketCEGBiomes.SPACE_ORBIT);

        // 😡 海王星 😡
        registerLevelStem(context, dimensionTypes, biomes, noiseSettings, "neptune_surface", RocketCEGBiomes.NEPTUNE_ICE);
        registerLevelStem(context, dimensionTypes, biomes, noiseSettings, "neptune_orbit", RocketCEGBiomes.SPACE_ORBIT);

        // 😡 冥王星 😡
        registerLevelStem(context, dimensionTypes, biomes, noiseSettings, "pluto_surface", RocketCEGBiomes.PLUTO_SURFACE);
        registerLevelStem(context, dimensionTypes, biomes, noiseSettings, "pluto_orbit", RocketCEGBiomes.SPACE_ORBIT);
    }

    private static void registerLevelStem(
        final BootstapContext<LevelStem> context,
        final HolderGetter<DimensionType> dimensionTypes,
        final HolderGetter<Biome> biomes,
        final HolderGetter<NoiseGeneratorSettings> noiseSettings,
        final String name,
        final ResourceKey<Biome> defaultBiome
    ) {
        final ResourceKey<LevelStem> key = ResourceKey.create(
            Registries.LEVEL_STEM,
            new ResourceLocation(RocketCEGMod.MOD_ID, name)
        );

        final ResourceKey<DimensionType> dimTypeKey = ResourceKey.create(
            Registries.DIMENSION_TYPE,
            new ResourceLocation(RocketCEGMod.MOD_ID, name)
        );

        final Holder<DimensionType> dimType = dimensionTypes.getOrThrow(dimTypeKey);
        final Holder<Biome> biome = biomes.getOrThrow(defaultBiome);
        
        // 😡 使用 minecraft:overworld 噪声设置（minecraft:flat 在数据生成时不可用） 😡
        final ResourceKey<NoiseGeneratorSettings> noiseKey = ResourceKey.create(
            Registries.NOISE_SETTINGS,
            new ResourceLocation("minecraft", "overworld")
        );
        final Holder<NoiseGeneratorSettings> noise = noiseSettings.getOrThrow(noiseKey);

        // 😡 创建简单的平坦区块生成器 😡
        final NoiseBasedChunkGenerator chunkGenerator = new NoiseBasedChunkGenerator(
            new net.minecraft.world.level.biome.FixedBiomeSource(biome),
            noise
        );

        final LevelStem levelStem = new LevelStem(dimType, chunkGenerator);
        context.register(key, levelStem);
    }
}
