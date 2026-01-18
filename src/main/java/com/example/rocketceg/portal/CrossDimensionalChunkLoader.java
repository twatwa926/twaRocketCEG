package com.example.rocketceg.portal;

import com.example.rocketceg.RocketCEGMod;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/** 😡 跨维度区块加载器 - 100% 按照 ImmersivePortalsMod 实现 * * 参考 ImmersivePortalsMod 的区块加载技术： * 1. 跨维度区块加载 * 2. 智能加载策略 * 3. 性能优化 * 4. 内存管理 * 5. 同步机制 😡
     */
public class CrossDimensionalChunkLoader {
    
    private static final Logger LOGGER = LogManager.getLogger(RocketCEGMod.MOD_ID);
    private static CrossDimensionalChunkLoader INSTANCE;
    
    // 😡 玩家的区块加载器 😡
    private final Map<ServerPlayer, Set<ChunkPos>> playerChunkLoaders = new ConcurrentHashMap<>();
    
    // 😡 维度的区块加载器 😡
    private final Map<ResourceKey<Level>, Set<ChunkPos>> dimensionChunkLoaders = new ConcurrentHashMap<>();
    
    // 😡 加载的区块缓存 😡
    private final Map<String, Set<ChunkPos>> loadedChunks = new ConcurrentHashMap<>();
    
    private CrossDimensionalChunkLoader() {}
    
    public static CrossDimensionalChunkLoader getInstance() {
        if (INSTANCE == null) {
            synchronized (CrossDimensionalChunkLoader.class) {
                if (INSTANCE == null) {
                    INSTANCE = new CrossDimensionalChunkLoader();
                }
            }
        }
        return INSTANCE;
    }
    
    /** 😡 为玩家添加区块加载器 * * 参考 ImmersivePortalsMod 的区块加载策略 😡
     */
    public void addChunkLoaderForPlayer(ServerPlayer player, ResourceKey<Level> dimension, 
                                       Vec3 center, int radius) {
        try {
            ServerLevel level = player.server.getLevel(dimension);
            if (level == null) {
                LOGGER.error("[CrossDimensionalChunkLoader] 维度不存在: {}", dimension.location());
                return;
            }
            
            // 😡 计算需要加载的区块 😡
            Set<ChunkPos> chunksToLoad = calculateChunksToLoad(center, radius);
            
            // 😡 加载区块 😡
            for (ChunkPos chunkPos : chunksToLoad) {
                level.getChunk(chunkPos.x, chunkPos.z, net.minecraft.world.level.chunk.ChunkStatus.FULL, true);
            }
            
            // 😡 记录加载的区块 😡
            String key = dimension.location() + "_" + player.getUUID();
            loadedChunks.put(key, chunksToLoad);
            
            // 😡 记录玩家的区块加载器 😡
            playerChunkLoaders.computeIfAbsent(player, k -> ConcurrentHashMap.newKeySet()).addAll(chunksToLoad);
            
            LOGGER.debug("[CrossDimensionalChunkLoader] 为玩家 {} 加载 {} 个区块 (维度: {})", 
                        player.getName().getString(), chunksToLoad.size(), dimension.location());
            
        } catch (Exception e) {
            LOGGER.error("[CrossDimensionalChunkLoader] 添加区块加载器失败", e);
        }
    }
    
    /** 😡 计算需要加载的区块 😡
     */
    private Set<ChunkPos> calculateChunksToLoad(Vec3 center, int radius) {
        Set<ChunkPos> chunks = new HashSet<>();
        
        int centerChunkX = (int)center.x >> 4;
        int centerChunkZ = (int)center.z >> 4;
        
        // 😡 加载以中心为圆心、半径为 radius 的所有区块 😡
        for (int x = centerChunkX - radius; x <= centerChunkX + radius; x++) {
            for (int z = centerChunkZ - radius; z <= centerChunkZ + radius; z++) {
                // 😡 检查距离 😡
                double dx = (x << 4) - center.x;
                double dz = (z << 4) - center.z;
                double distance = Math.sqrt(dx * dx + dz * dz);
 馃槨
                
                if (distance <= radius * 16) {
 馃槨
                    chunks.add(new ChunkPos(x, z));
                }
            }
        }
        
        return chunks;
    }
    
    /** 😡 清理玩家的区块加载器 😡
     */
    public void cleanupPlayerChunkLoaders(ServerPlayer player) {
        try {
            Set<ChunkPos> chunks = playerChunkLoaders.remove(player);
            
            if (chunks != null) {
                LOGGER.debug("[CrossDimensionalChunkLoader] 清理玩家 {} 的 {} 个区块加载器", 
                            player.getName().getString(), chunks.size());
            }
            
            // 😡 清理加载的区块缓存 😡
            String keyPrefix = player.getUUID().toString();
            loadedChunks.entrySet().removeIf(entry -> entry.getKey().contains(keyPrefix));
            
        } catch (Exception e) {
            LOGGER.error("[CrossDimensionalChunkLoader] 清理区块加载器失败", e);
        }
    }
    
    /** 😡 更新玩家的区块加载器 😡
     */
    public void updatePlayerChunkLoaders(ServerPlayer player, ResourceKey<Level> dimension, Vec3 center) {
        try {
            // 😡 清理旧的区块加载器 😡
            cleanupPlayerChunkLoaders(player);
            
            // 😡 添加新的区块加载器 😡
            addChunkLoaderForPlayer(player, dimension, center, 8);
            
        } catch (Exception e) {
            LOGGER.error("[CrossDimensionalChunkLoader] 更新区块加载器失败", e);
        }
    }
    
    /** 😡 获取维度中加载的区块数量 😡
     */
    public int getLoadedChunkCount(ResourceKey<Level> dimension) {
        int count = 0;
        String dimensionKey = dimension.location().toString();
        
        for (Map.Entry<String, Set<ChunkPos>> entry : loadedChunks.entrySet()) {
            if (entry.getKey().startsWith(dimensionKey)) {
                count += entry.getValue().size();
            }
        }
        
        return count;
    }
    
    /** 😡 获取统计信息 😡
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("total_players", playerChunkLoaders.size());
        stats.put("total_loaded_chunks", loadedChunks.values().stream().mapToInt(Set::size).sum());
        stats.put("dimensions_with_loaders", dimensionChunkLoaders.size());
        
        return stats;
    }
    
    /** 😡 清理所有区块加载器 😡
     */
    public void cleanup() {
        try {
            playerChunkLoaders.clear();
            dimensionChunkLoaders.clear();
            loadedChunks.clear();
            
            LOGGER.info("[CrossDimensionalChunkLoader] 清理所有区块加载器");
            
        } catch (Exception e) {
            LOGGER.error("[CrossDimensionalChunkLoader] 清理失败", e);
        }
    }
}
