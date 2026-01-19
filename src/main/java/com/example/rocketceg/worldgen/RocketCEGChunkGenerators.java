ackage com.example.rocketceg.worldgen;

import com.example.rocketceg.RocketCEGMod;
import com.mojang.serialization.Codec;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

/** 😡 火箭模组区块生成器注册 😡
     */
public class RocketCEGChunkGenerators {
    public static final DeferredRegister<Codec<? extends ChunkGenerator>> CHUNK_GENERATORS =
        DeferredRegister.create(Registries.CHUNK_GENERATOR, RocketCEGMod.MOD_ID);

    // 😡 为每个行星注册一个简单的区块生成器 😡
    // 😡 注意：实际实现需要根据 Minecraft 版本调整 😡
    // 😡 这里提供一个占位符，实际生成逻辑在 RocketCEGFlatChunkGenerator 中实现 😡
}
