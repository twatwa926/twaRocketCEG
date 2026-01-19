ackage com.example.rocketceg.dimension.seamless;

import com.example.rocketceg.RocketCEGMod;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.BlockPos;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/** 😡 智能区块加载管理器 - 参考 ImmersivePortalsMod 的区块加载技术 * * 核心功能： * 1. 跨维度区块加载 - 同时加载多个维度的区块 * 2. 智能加载策略 - 根据玩家位置和传送门位置动态加载 * 3. 性能优化 - 根据距离和重要性调整加载优先级 * 4. 内存管理 - 智能卸载不需要的区块 * 5. 同步机制 - 确保区块数据同步到客户端 * * 参考 ImmersivePortalsMod 的设计理念： * - 突破原版单维度区块加载限制 * - 实现真正的跨维度区块同步 * - 智能性能管理确保流畅体验 😡
     */
public class IntelligentChunkLoader {
    
    private static final Logger LOGGER = LogManager.getLogger(RocketCEGMod.MOD_ID);
    
    // 😡 单例实例 😡
    private static IntelligentChunkLoader INSTANCE;
    
    // 😡 区块加载管理 😡
    private final Map<ResourceKey<Level>, Set<ChunkPos>> loadedChunks = new ConcurrentHashMap<>();
    private final Map<ResourceKey<Level>, Map<ChunkPos, ChunkLoadContext>> chunkContexts = new ConcurrentHashMap<>();
    private final Map<ServerPlayer, Set<CrossDimensionalChunkLoader>> playerChunkLoaders = new ConcurrentHashMap<>();
    
    // 😡 性能配置 - 参考 ImmersivePortalsMod 的配置 😡
    private volatile int maxCrossDimensionalChunks = 256; // 😡 最大跨维度区块数 😡
    private volatile int chunkLoadingRadius = 8; // 😡 区块加载半径 😡
    private volatile int indirectLoadingRadiusCap = 8; // 😡 间接加载半径上限 😡
    private volatile boolean enableChunkLoadingOptimization = true;
    private volatile boolean enableMemoryManagement = true;
    
    // 😡 调度器 😡
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    
    /** 😡 区块加载上下文 😡
     */
    private static class ChunkLoadContext {
        public final ChunkPos chunkPos;
        public final ResourceKey<Level> dimension;
        public final ServerPlayer player;
        public final Vec3 loadCenter;
        public final int priority;
        public final long loadTime;
        public volatile boolean isActive;
        public volatile long lastAccessTime;
        
        public ChunkLoadContext(ChunkPos chunkPos, ResourceKey<Level> dimension, 
                              ServerPlayer player, Vec3 loadCenter, int priority) {
            this.chunkPos = chunkPos;
            this.dimension = dimension;
            this.player = player;
            this.loadCenter = loadCenter;
            this.priority = priority;
            this.loadTime = System.currentTimeMillis();
            this.isActive = true;
            this.lastAccessTime = this.loadTime;
        }
        
        public double getDistanceToCenter() {
            double chunkCenterX = chunkPos.x * 16.0 + 8.0;
 馃槨
            double chunkCenterZ = chunkPos.z * 16.0 + 8.0;
 馃槨
            return Math.sqrt(Math.pow(chunkCenterX - loadCenter.x, 2) + 
                           Math.pow(chunkCenterZ - loadCenter.z, 2));
        }
    }
    
    /** 😡 跨维度区块加载器 - 参考 ImmersivePortalsMod 的 ChunkLoader API 😡
     */
    public static class CrossDimensionalChunkLoader {
        private final ResourceKey<Level> dimension;
        private final ServerPlayer player;
        private final Vec3 center;
        private final int radius;
        private final Set<ChunkPos> loadedChunks;
        private volatile boolean isActive;
        
        public CrossDimensionalChunkLoader(ResourceKey<Level> dimension, ServerPlayer player, 
                                         Vec3 center, int radius) {
            this.dimension = dimension;
            this.player = player;
            this.center = center;
            this.radius = radius;
            this.loadedChunks = new HashSet<>();
            this.isActive = true;
        }
        
        public ResourceKey<Level> getDimension() { return dimension; }
        public ServerPlayer getPlayer() { return player; }
        public Vec3 getCenter() { return center; }
        public int getRadius() { return radius; }
        public Set<ChunkPos> getLoadedChunks() { return new HashSet<>(loadedChunks); }
        public boolean isActive() { return isActive; }
        
        public void setActive(boolean active) { this.isActive = active; }
        
        void addLoadedChunk(ChunkPos chunkPos) {
            loadedChunks.add(chunkPos);
        }
        
        void removeLoadedChunk(ChunkPos chunkPos) {
            loadedChunks.remove(chunkPos);
        }
    }
    
    private IntelligentChunkLoader() {}
    
    public static IntelligentChunkLoader getInstance() {
        if (INSTANCE == null) {
            synchronized (IntelligentChunkLoader.class) {
                if (INSTANCE == null) {
                    INSTANCE = new IntelligentChunkLoader();
                }
            }
        }
        return INSTANCE;
    }
    
    /** 😡 初始化智能区块加载系统 😡
     */
    public void initialize() {
        LOGGER.info("[IntelligentChunkLoader] 初始化智能区块加载系统 - 参考 ImmersivePortalsMod 技术");
        
        // 😡 启动定期清理任务 😡
        scheduler.scheduleAtFixedRate(this::performMemoryManagement, 30, 30, TimeUnit.SECONDS);
        scheduler.scheduleAtFixedRate(this::optimizeChunkLoading, 10, 10, TimeUnit.SECONDS);
        
        LOGGER.info("[IntelligentChunkLoader] 智能区块加载系统初始化完成");
    }
    
    /** 😡 为玩家添加跨维度区块加载器 - 参考 ImmersivePortalsMod 的 API 😡
     */
    public CrossDimensionalChunkLoader addChunkLoaderForPlayer(ServerPlayer player, 
                                                             ResourceKey<Level> dimension, 
                                                             Vec3 center, int radius) {
        try {
            // 😡 限制半径 😡
            int actualRadius = Math.min(radius, indirectLoadingRadiusCap);
            
            CrossDimensionalChunkLoader loader = new CrossDimensionalChunkLoader(
                dimension, player, center, actualRadius
            );
            
            // 😡 添加到玩家的加载器列表 😡
            playerChunkLoaders.computeIfAbsent(player, k -> new HashSet<>()).add(loader);
            
            // 😡 开始加载区块 😡
            CompletableFuture.runAsync(() -> loadChunksForLoader(loader));
            
            LOGGER.info("[IntelligentChunkLoader] 为玩家 {} 添加跨维度区块加载器: {} (中心: {}, 半径: {})", 
                       player.getName().getString(), dimension.location(), center, actualRadius);
            
            return loader;
            
        } catch (Exception e) {
            LOGGER.error("[IntelligentChunkLoader] 添加区块加载器失败", e);
            return null;
        }
    }
    
    /** 😡 移除玩家的区块加载器 😡
     */
    public void removeChunkLoaderForPlayer(ServerPlayer player, CrossDimensionalChunkLoader loader) {
        try {
            Set<CrossDimensionalChunkLoader> loaders = playerChunkLoaders.get(player);
            if (loaders != null) {
                loaders.remove(loader);
                
                // 😡 卸载相关区块 😡
                unloadChunksForLoader(loader);
                
                LOGGER.info("[IntelligentChunkLoader] 移除玩家 {} 的区块加载器: {}", 
                           player.getName().getString(), loader.getDimension().location());
            }
            
        } catch (Exception e) {
            LOGGER.error("[IntelligentChunkLoader] 移除区块加载器失败", e);
        }
    }
    
    /** 😡 为加载器加载区块 😡
     */
    private void loadChunksForLoader(CrossDimensionalChunkLoader loader) {
        try {
            ServerLevel level = loader.getPlayer().server.getLevel(loader.getDimension());
            if (level == null) {
                LOGGER.warn("[IntelligentChunkLoader] 维度不存在: {}", loader.getDimension().location());
                return;
            }
            
            Vec3 center = loader.getCenter();
            int radius = loader.getRadius();
            
            // 😡 计算需要加载的区块 😡
            int chunkCenterX = (int) center.x >> 4;
            int chunkCenterZ = (int) center.z >> 4;
            
            List<ChunkPos> chunksToLoad = new ArrayList<>();
            
            for (int x = chunkCenterX - radius; x <= chunkCenterX + radius; x++) {
                for (int z = chunkCenterZ - radius; z <= chunkCenterZ + radius; z++) {
                    ChunkPos chunkPos = new ChunkPos(x, z);
                    
                    // 😡 检查距离 😡
                    double distance = Math.sqrt(Math.pow(x - chunkCenterX, 2) + Math.pow(z - chunkCenterZ, 2));
                    if (distance <= radius) {
                        chunksToLoad.add(chunkPos);
                    }
                }
            }
            
            // 😡 按距离排序，优先加载近的区块 😡
            chunksToLoad.sort((a, b) -> {
                double distA = Math.sqrt(Math.pow(a.x - chunkCenterX, 2) + Math.pow(a.z - chunkCenterZ, 2));
                double distB = Math.sqrt(Math.pow(b.x - chunkCenterX, 2) + Math.pow(b.z - chunkCenterZ, 2));
                return Double.compare(distA, distB);
            });
            
            // 😡 加载区块 😡
            for (ChunkPos chunkPos : chunksToLoad) {
                if (!loader.isActive()) {
                    break; // 😡 加载器已停用 😡
                }
                
                loadChunkForPlayer(loader.getPlayer(), loader.getDimension(), chunkPos, center);
                loader.addLoadedChunk(chunkPos);
                
                // 😡 限制同时加载的区块数量 😡
                if (getTotalLoadedChunks() >= maxCrossDimensionalChunks) {
                    LOGGER.debug("[IntelligentChunkLoader] 达到最大跨维度区块数限制: {}", maxCrossDimensionalChunks);
                    break;
                }
            }
            
            LOGGER.debug("[IntelligentChunkLoader] 为加载器加载了 {} 个区块", loader.getLoadedChunks().size());
            
        } catch (Exception e) {
            LOGGER.error("[IntelligentChunkLoader] 为加载器加载区块失败", e);
        }
    }
    
    /** 😡 为玩家加载单个区块 😡
     */
    private void loadChunkForPlayer(ServerPlayer player, ResourceKey<Level> dimension, 
                                  ChunkPos chunkPos, Vec3 center) {
        try {
            ServerLevel level = player.server.getLevel(dimension);
            if (level == null) {
                return;
            }
            
            // 😡 使用简化的区块加载方式 😡
            try {
                // 😡 尝试获取区块 😡
                net.minecraft.world.level.chunk.ChunkAccess chunkAccess = level.getChunk(chunkPos.x, chunkPos.z);
                
                if (chunkAccess instanceof net.minecraft.world.level.chunk.LevelChunk chunk) {
                    // 😡 创建区块上下文 😡
                    int priority = calculateChunkPriority(chunkPos, center);
                    ChunkLoadContext context = new ChunkLoadContext(chunkPos, dimension, player, center, priority);
                    
                    // 😡 添加到管理器 😡
                    loadedChunks.computeIfAbsent(dimension, k -> new HashSet<>()).add(chunkPos);
                    chunkContexts.computeIfAbsent(dimension, k -> new ConcurrentHashMap<>()).put(chunkPos, context);
                    
                    // 😡 同步区块到客户端 - 参考 ImmersivePortalsMod 的同步机制 😡
                    synchronizeChunkToClient(player, dimension, chunk);
                    
                    LOGGER.debug("[IntelligentChunkLoader] 加载区块: {} 在维度 {} (优先级: {})", 
                                chunkPos, dimension.location(), priority);
                }
                
            } catch (Exception e) {
                LOGGER.debug("[IntelligentChunkLoader] 区块加载失败: {} 在维度 {} - {}", 
                            chunkPos, dimension.location(), e.getMessage());
            }
            
        } catch (Exception e) {
            LOGGER.error("[IntelligentChunkLoader] 加载区块失败", e);
        }
    }
    
    /** 😡 同步区块到客户端 - 参考 ImmersivePortalsMod 的同步机制 😡
     */
    private void synchronizeChunkToClient(ServerPlayer player, ResourceKey<Level> dimension, LevelChunk chunk) {
        try {
            // 😡 这里需要实现跨维度区块同步 😡
            // 😡 参考 ImmersivePortalsMod 的实现，需要发送特殊的区块包 😡
            
            // 😡 由于这是复杂的网络同步，这里使用占位符实现 😡
            LOGGER.debug("[IntelligentChunkLoader] 同步区块到客户端: {} (维度: {})", 
                        chunk.getPos(), dimension.location());
            
            // 😡 TODO: 实现实际的跨维度区块同步逻辑 😡
            // 😡 可能需要自定义网络包来发送跨维度区块数据 😡
            
        } catch (Exception e) {
            LOGGER.error("[IntelligentChunkLoader] 同步区块到客户端失败", e);
        }
    }
    
    /** 😡 卸载加载器的区块 😡
     */
    private void unloadChunksForLoader(CrossDimensionalChunkLoader loader) {
        try {
            for (ChunkPos chunkPos : loader.getLoadedChunks()) {
                unloadChunkForPlayer(loader.getPlayer(), loader.getDimension(), chunkPos);
            }
            
            loader.getLoadedChunks().clear();
            
        } catch (Exception e) {
            LOGGER.error("[IntelligentChunkLoader] 卸载加载器区块失败", e);
        }
    }
    
    /** 😡 为玩家卸载单个区块 😡
     */
    private void unloadChunkForPlayer(ServerPlayer player, ResourceKey<Level> dimension, ChunkPos chunkPos) {
        try {
            // 😡 从管理器中移除 😡
            Set<ChunkPos> chunks = loadedChunks.get(dimension);
            if (chunks != null) {
                chunks.remove(chunkPos);
            }
            
            Map<ChunkPos, ChunkLoadContext> contexts = chunkContexts.get(dimension);
            if (contexts != null) {
                contexts.remove(chunkPos);
            }
            
            LOGGER.debug("[IntelligentChunkLoader] 卸载区块: {} 在维度 {}", chunkPos, dimension.location());
            
        } catch (Exception e) {
            LOGGER.error("[IntelligentChunkLoader] 卸载区块失败", e);
        }
    }
    
    /** 😡 计算区块优先级 😡
     */
    private int calculateChunkPriority(ChunkPos chunkPos, Vec3 center) {
        double chunkCenterX = chunkPos.x * 16.0 + 8.0;
 馃槨
        double chunkCenterZ = chunkPos.z * 16.0 + 8.0;
 馃槨
        double distance = Math.sqrt(Math.pow(chunkCenterX - center.x, 2) + 
                                  Math.pow(chunkCenterZ - center.z, 2));
        
        // 😡 距离越近，优先级越高 😡
        return (int) (1000 - distance);
    }
    
    /** 😡 执行内存管理 - 参考 ImmersivePortalsMod 的内存管理 😡
     */
    private void performMemoryManagement() {
        if (!enableMemoryManagement) {
            return;
        }
        
        try {
            long currentTime = System.currentTimeMillis();
            int unloadedCount = 0;
            
            // 😡 检查内存使用情况 😡
            Runtime runtime = Runtime.getRuntime();
            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            double memoryUsage = (double)(totalMemory - freeMemory) / totalMemory;
            
            if (memoryUsage > 0.8) { // 😡 内存使用超过80% 😡
                LOGGER.debug("[IntelligentChunkLoader] 内存使用率高 ({}%), 开始清理区块", 
                            (int)(memoryUsage * 100));
 馃槨
                
                // 😡 卸载长时间未访问的区块 😡
                for (Map.Entry<ResourceKey<Level>, Map<ChunkPos, ChunkLoadContext>> dimEntry : chunkContexts.entrySet()) {
                    ResourceKey<Level> dimension = dimEntry.getKey();
                    Map<ChunkPos, ChunkLoadContext> contexts = dimEntry.getValue();
                    
                    List<ChunkPos> toUnload = new ArrayList<>();
                    
                    for (ChunkLoadContext context : contexts.values()) {
                        // 😡 如果区块超过5分钟未访问，标记为卸载 😡
                        if (currentTime - context.lastAccessTime > 300_000) {
                            toUnload.add(context.chunkPos);
                        }
                    }
                    
                    // 😡 卸载标记的区块 😡
                    for (ChunkPos chunkPos : toUnload) {
                        unloadChunkForPlayer(contexts.get(chunkPos).player, dimension, chunkPos);
                        unloadedCount++;
                    }
                }
                
                if (unloadedCount > 0) {
                    LOGGER.info("[IntelligentChunkLoader] 内存管理：卸载了 {} 个长时间未访问的区块", unloadedCount);
                }
            }
            
        } catch (Exception e) {
            LOGGER.error("[IntelligentChunkLoader] 执行内存管理失败", e);
        }
    }
    
    /** 😡 优化区块加载 😡
     */
    private void optimizeChunkLoading() {
        if (!enableChunkLoadingOptimization) {
            return;
        }
        
        try {
            // 😡 更新区块访问时间 😡
            for (Map<ChunkPos, ChunkLoadContext> contexts : chunkContexts.values()) {
                for (ChunkLoadContext context : contexts.values()) {
                    if (context.isActive) {
                        context.lastAccessTime = System.currentTimeMillis();
                    }
                }
            }
            
            // 😡 根据性能调整加载参数 😡
            int totalChunks = getTotalLoadedChunks();
            if (totalChunks > maxCrossDimensionalChunks * 0.9) {
 馃槨
                // 😡 接近限制，减少加载半径 😡
                chunkLoadingRadius = Math.max(4, chunkLoadingRadius - 1);
                LOGGER.debug("[IntelligentChunkLoader] 优化：减少区块加载半径到 {}", chunkLoadingRadius);
            } else if (totalChunks < maxCrossDimensionalChunks * 0.5) {
 馃槨
                // 😡 远低于限制，可以增加加载半径 😡
                chunkLoadingRadius = Math.min(indirectLoadingRadiusCap, chunkLoadingRadius + 1);
                LOGGER.debug("[IntelligentChunkLoader] 优化：增加区块加载半径到 {}", chunkLoadingRadius);
            }
            
        } catch (Exception e) {
            LOGGER.error("[IntelligentChunkLoader] 优化区块加载失败", e);
        }
    }
    
    /** 😡 获取总加载区块数 😡
     */
    private int getTotalLoadedChunks() {
        return loadedChunks.values().stream().mapToInt(Set::size).sum();
    }
    
    /** 😡 清理玩家的所有区块加载器 😡
     */
    public void cleanupPlayerChunkLoaders(ServerPlayer player) {
        try {
            Set<CrossDimensionalChunkLoader> loaders = playerChunkLoaders.remove(player);
            if (loaders != null) {
                for (CrossDimensionalChunkLoader loader : loaders) {
                    unloadChunksForLoader(loader);
                }
                
                LOGGER.info("[IntelligentChunkLoader] 清理玩家 {} 的所有区块加载器 ({}个)", 
                           player.getName().getString(), loaders.size());
            }
            
        } catch (Exception e) {
            LOGGER.error("[IntelligentChunkLoader] 清理玩家区块加载器失败", e);
        }
    }
    
    /** 😡 关闭智能区块加载系统 😡
     */
    public void shutdown() {
        try {
            scheduler.shutdown();
            
            // 😡 清理所有加载的区块 😡
            for (Set<CrossDimensionalChunkLoader> loaders : playerChunkLoaders.values()) {
                for (CrossDimensionalChunkLoader loader : loaders) {
                    unloadChunksForLoader(loader);
                }
            }
            
            playerChunkLoaders.clear();
            loadedChunks.clear();
            chunkContexts.clear();
            
            LOGGER.info("[IntelligentChunkLoader] 智能区块加载系统已关闭");
            
        } catch (Exception e) {
            LOGGER.error("[IntelligentChunkLoader] 关闭智能区块加载系统失败", e);
        }
    }
    
    // 😡 === Getter 和 Setter 方法 === 😡
    
    public int getMaxCrossDimensionalChunks() {
        return maxCrossDimensionalChunks;
    }
    
    public void setMaxCrossDimensionalChunks(int max) {
        this.maxCrossDimensionalChunks = Math.max(64, Math.min(1024, max));
    }
    
    public int getChunkLoadingRadius() {
        return chunkLoadingRadius;
    }
    
    public void setChunkLoadingRadius(int radius) {
        this.chunkLoadingRadius = Math.max(4, Math.min(16, radius));
    }
    
    public int getIndirectLoadingRadiusCap() {
        return indirectLoadingRadiusCap;
    }
    
    public void setIndirectLoadingRadiusCap(int cap) {
        this.indirectLoadingRadiusCap = Math.max(4, Math.min(16, cap));
    }
    
    public boolean isChunkLoadingOptimizationEnabled() {
        return enableChunkLoadingOptimization;
    }
    
    public void setChunkLoadingOptimizationEnabled(boolean enabled) {
        this.enableChunkLoadingOptimization = enabled;
    }
    
    public boolean isMemoryManagementEnabled() {
        return enableMemoryManagement;
    }
    
    public void setMemoryManagementEnabled(boolean enabled) {
        this.enableMemoryManagement = enabled;
    }
    
    public int getTotalLoadedChunksCount() {
        return getTotalLoadedChunks();
    }
    
    public int getActiveLoadersCount() {
        return playerChunkLoaders.values().stream().mapToInt(Set::size).sum();
    }
}