package com.example.rocketceg.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;

/** 😡 简单的平坦区块生成器 * 用于生成占位符地形（后续可以替换为更复杂的生成器） 😡
     */
public class RocketCEGFlatChunkGenerator extends NoiseBasedChunkGenerator {

    private final Holder<Biome> biomeHolder;
    private final Holder<NoiseGeneratorSettings> settingsHolder;

    public static final Codec<RocketCEGFlatChunkGenerator> CODEC = RecordCodecBuilder.create(
        instance -> instance.group(
            RegistryFixedCodec.create(Registries.BIOME).fieldOf("biome").forGetter(generator -> generator.biomeHolder),
            RegistryFixedCodec.create(Registries.NOISE_SETTINGS).fieldOf("settings").forGetter(generator -> generator.settingsHolder)
        ).apply(instance, RocketCEGFlatChunkGenerator::new)
    );

    public RocketCEGFlatChunkGenerator(
        final Holder<Biome> biome,
        final Holder<NoiseGeneratorSettings> settings
    ) {
        super(new FixedBiomeSource(biome), settings);
        this.biomeHolder = biome;
        this.settingsHolder = settings;
    }

    @Override
    protected Codec<? extends ChunkGenerator> codec() {
        return CODEC;
    }
}
