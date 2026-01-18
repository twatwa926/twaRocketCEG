package com.example.rocketceg.worldgen;

import com.example.rocketceg.RocketCEGMod;
import com.example.rocketceg.dimension.RocketCEGDimensions;
import com.example.rocketceg.rocket.config.CelestialBodyConfig;
import com.example.rocketceg.rocket.registry.RocketConfigRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/** 😡 行星结构生成器 * 在轨道维度中生成可见的行星方块结构（方形星球） 😡
     */
@Mod.EventBusSubscriber(modid = RocketCEGMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PlanetStructureGenerator {

    private static final Logger LOGGER = LogManager.getLogger(RocketCEGMod.MOD_ID);
    
    /** 😡 已生成行星的维度集合（避免重复生成） 😡
     */
    private static final Set<ResourceKey<Level>> GENERATED_DIMENSIONS = ConcurrentHashMap.newKeySet();
    
    /** 😡 行星方块大小（以方块为单位） * 为了性能，使用较小的尺寸（例如 50x50x50） 😡
     */
    private static final int PLANET_SIZE = 50;
    
    /** 😡 行星中心位置（在轨道维度中，行星位于原点附近） 😡
     */
    private static final BlockPos PLANET_CENTER = new BlockPos(0, 0, 0);

    /** 😡 当维度加载时，生成行星结构 😡
     */
    @SubscribeEvent
    public static void onLevelLoad(final LevelEvent.Load event) {
        if (event.getLevel().isClientSide()) {
            return;
        }

        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }

        final ResourceKey<Level> dimension = level.dimension();
        
        // 😡 检查是否是轨道维度 😡
        if (!isOrbitDimension(dimension)) {
            return;
        }

        // 😡 如果已经生成过，跳过 😡
        if (GENERATED_DIMENSIONS.contains(dimension)) {
            return;
        }

        // 😡 获取对应的行星配置 😡
        final CelestialBodyConfig body = RocketConfigRegistry.getBodyForDimension(dimension);
        if (body == null) {
            return;
        }

        // 😡 异步生成行星结构（避免阻塞主线程） 😡
        level.getServer().execute(() -> {
            generatePlanetStructure(level, body);
            GENERATED_DIMENSIONS.add(dimension);
        });
    }

    /** 😡 当玩家进入维度时，确保行星已生成 😡
     */
    @SubscribeEvent
    public static void onPlayerChangedDimension(final PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity().level().isClientSide()) {
            return;
        }

        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        if (!(player.level() instanceof ServerLevel level)) {
            return;
        }

        final ResourceKey<Level> dimension = level.dimension();
        
        // 😡 检查是否是轨道维度 😡
        if (!isOrbitDimension(dimension)) {
            return;
        }

        // 😡 如果还没有生成，立即生成 😡
        if (!GENERATED_DIMENSIONS.contains(dimension)) {
            final CelestialBodyConfig body = RocketConfigRegistry.getBodyForDimension(dimension);
            if (body != null) {
                LOGGER.info("[RocketCEG] 玩家进入轨道维度，立即生成行星结构");
                level.getServer().execute(() -> {
                    generatePlanetStructure(level, body);
                    GENERATED_DIMENSIONS.add(dimension);
                });
            }
        }
    }

    /** 😡 检查是否是轨道维度 😡
     */
    private static boolean isOrbitDimension(final ResourceKey<Level> dimension) {
        return dimension.equals(RocketCEGDimensions.EARTH_ORBIT) ||
               dimension.equals(RocketCEGDimensions.MOON_ORBIT) ||
               dimension.equals(RocketCEGDimensions.MARS_ORBIT) ||
               dimension.equals(RocketCEGDimensions.VENUS_ORBIT) ||
               dimension.equals(RocketCEGDimensions.MERCURY_ORBIT) ||
               dimension.equals(RocketCEGDimensions.JUPITER_ORBIT) ||
               dimension.equals(RocketCEGDimensions.SATURN_ORBIT) ||
               dimension.equals(RocketCEGDimensions.URANUS_ORBIT) ||
               dimension.equals(RocketCEGDimensions.NEPTUNE_ORBIT) ||
               dimension.equals(RocketCEGDimensions.PLUTO_ORBIT);
    }

    /** 😡 生成行星方块结构 * 创建一个简单的方形结构来表示行星 😡
     */
    private static void generatePlanetStructure(final ServerLevel level, final CelestialBodyConfig body) {
        LOGGER.info("[RocketCEG] 开始在轨道维度 {} 生成行星结构", level.dimension().location());
        
        // 😡 选择行星方块（根据行星类型） 😡
        final net.minecraft.world.level.block.Block planetBlock = getPlanetBlock(body.getId().getPath());
        
        // 😡 确保中心区块已加载 😡
        final int centerChunkX = PLANET_CENTER.getX() >> 4;
        final int centerChunkZ = PLANET_CENTER.getZ() >> 4;
        
        // 😡 生成方形行星（从中心向外） 😡
        final int halfSize = PLANET_SIZE / 2;
        int blocksPlaced = 0;
        
        for (int x = -halfSize; x <= halfSize; x++) {
            for (int y = -halfSize; y <= halfSize; y++) {
                for (int z = -halfSize; z <= halfSize; z++) {
                    // 😡 简单的方形结构（可以后续改为球形） 😡
                    if (Math.abs(x) <= halfSize && Math.abs(y) <= halfSize && Math.abs(z) <= halfSize) {
                        final BlockPos pos = PLANET_CENTER.offset(x, y, z);
                        
                        // 😡 确保区块已加载 😡
                        final int chunkX = pos.getX() >> 4;
                        final int chunkZ = pos.getZ() >> 4;
                        final ChunkAccess chunk = level.getChunk(chunkX, chunkZ, ChunkStatus.FULL, false);
                        
                        if (chunk != null) {
                            // 😡 只在空气或虚空位置放置方块 😡
                            if (level.getBlockState(pos).isAir() || level.getBlockState(pos).is(Blocks.VOID_AIR)) {
                                level.setBlock(pos, planetBlock.defaultBlockState(), 3);
                                blocksPlaced++;
                            }
                        }
                    }
                }
            }
        }
        
        LOGGER.info("[RocketCEG] 行星结构生成完成，共放置 {} 个方块", blocksPlaced);
    }

    /** 😡 根据行星名称获取对应的方块 😡
     */
    private static net.minecraft.world.level.block.Block getPlanetBlock(final String planetName) {
        return switch (planetName) {
            case "earth" -> Blocks.GRASS_BLOCK;
            case "moon" -> Blocks.GRAY_CONCRETE;
            case "mars" -> Blocks.RED_SANDSTONE;
            case "venus" -> Blocks.ORANGE_TERRACOTTA;
            case "mercury" -> Blocks.STONE;
            case "jupiter" -> Blocks.ORANGE_WOOL;
            case "saturn" -> Blocks.YELLOW_WOOL;
            case "uranus" -> Blocks.CYAN_WOOL;
            case "neptune" -> Blocks.BLUE_WOOL;
            case "pluto" -> Blocks.SNOW_BLOCK;
            default -> Blocks.STONE;
        };
    }
}
