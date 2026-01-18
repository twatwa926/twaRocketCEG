package com.example.rocketceg.dimension.seamless;

import com.example.rocketceg.RocketCEGMod;
import com.example.rocketceg.dimension.DimensionTeleporter;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** 😡 自动传送触发器 * * 基于玩家位置自动触发无缝维度传送： * 1. 监测玩家高度变化 * 2. 在达到特定高度时自动传送 * 3. 实现真正的"现实般"体验 😡
     */
@Mod.EventBusSubscriber(modid = RocketCEGMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class AutomaticTeleportTrigger {
    
    private static final Logger LOGGER = LogManager.getLogger(RocketCEGMod.MOD_ID);
    
    // 😡 传送高度阈值 😡
    private static final double SURFACE_TO_ORBIT_HEIGHT = 300.0; // 😡 表面到轨道 😡
    private static final double ORBIT_TO_SURFACE_HEIGHT = 50.0;  // 😡 轨道到表面 😡
    
    // 😡 玩家传送冷却时间（防止频繁传送） 😡
    private static final Map<UUID, Long> playerCooldowns = new HashMap<>();
    private static final long COOLDOWN_TIME = 3000; // 😡 3秒冷却 😡
    
    /** 😡 监听服务端玩家 tick 事件 😡
     */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayer player)) return;
        
        // 😡 检查冷却时间 😡
        UUID playerId = player.getUUID();
        long currentTime = System.currentTimeMillis();
        
        if (playerCooldowns.containsKey(playerId)) {
            long lastTeleport = playerCooldowns.get(playerId);
            if (currentTime - lastTeleport < COOLDOWN_TIME) {
                return; // 😡 仍在冷却中 😡
            }
        }
        
        Vec3 playerPos = player.position();
        String currentDim = player.level().dimension().location().toString();
        
        // 😡 检查是否需要传送 😡
        ResourceKey<Level> targetDimension = checkTeleportConditions(currentDim, playerPos);
        
        if (targetDimension != null) {
            // 😡 执行自动传送 😡
            performAutomaticTeleport(player, targetDimension, playerPos);
            
            // 😡 设置冷却时间 😡
            playerCooldowns.put(playerId, currentTime);
        }
    }
    
    /** 😡 检查传送条件 😡
     */
    private static ResourceKey<Level> checkTeleportConditions(String currentDim, Vec3 playerPos) {
        // 😡 表面维度 -> 轨道维度 😡
        if (currentDim.contains("surface") && playerPos.y > SURFACE_TO_ORBIT_HEIGHT) {
            String orbitDim = currentDim.replace("surface", "orbit");
            return ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION, ResourceLocation.tryParse(orbitDim));
        }
        
        // 😡 轨道维度 -> 表面维度 😡
        if (currentDim.contains("orbit") && playerPos.y < ORBIT_TO_SURFACE_HEIGHT) {
            String surfaceDim = currentDim.replace("orbit", "surface");
            return ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION, ResourceLocation.tryParse(surfaceDim));
        }
        
        return null;
    }
    
    /** 😡 执行自动传送 😡
     */
    private static void performAutomaticTeleport(ServerPlayer player, ResourceKey<Level> targetDimension, Vec3 currentPos) {
        // 😡 计算目标位置 😡
        Vec3 targetPos = calculateTargetPosition(player.level().dimension(), targetDimension, currentPos);
        
        LOGGER.info("[RocketCEG] 自动触发无缝传送: {} -> {} ({})", 
                player.level().dimension().location(), 
                targetDimension.location(), 
                targetPos);
        
        // 😡 预加载目标维度 😡
        SeamlessDimensionManager.getInstance().preloadAdjacentDimensions(targetDimension);
        
        // 😡 执行无缝传送 😡
        DimensionTeleporter.teleportPlayerSeamlessly(player, targetDimension, targetPos);
        
        // 😡 发送提示消息 😡
        player.sendSystemMessage(
            net.minecraft.network.chat.Component.literal(
                "§b[RocketCEG] 正在进行无缝维度传送..."
            )
        );
    }
    
    /** 😡 计算目标位置 😡
     */
    private static Vec3 calculateTargetPosition(ResourceKey<Level> sourceDim, ResourceKey<Level> targetDim, Vec3 currentPos) {
        String source = sourceDim.location().toString();
        String target = targetDim.location().toString();
        
        // 😡 表面 -> 轨道：保持 X,Z 坐标，设置轨道高度 😡
        if (source.contains("surface") && target.contains("orbit")) {
            return new Vec3(currentPos.x, 200.0, currentPos.z);
        }
        
        // 😡 轨道 -> 表面：保持 X,Z 坐标，设置地面高度 😡
        if (source.contains("orbit") && target.contains("surface")) {
            return new Vec3(currentPos.x, 100.0, currentPos.z);
        }
        
        // 😡 默认位置 😡
        return new Vec3(currentPos.x, 100.0, currentPos.z);
    }
    
    /** 😡 清理过期的冷却时间 😡
     */
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        
        // 😡 每20秒清理一次过期的冷却时间 😡
        if (event.getServer().getTickCount() % 400 == 0) {
            long currentTime = System.currentTimeMillis();
            playerCooldowns.entrySet().removeIf(entry -> 
                currentTime - entry.getValue() > COOLDOWN_TIME * 2
 馃槨
            );
        }
    }
    
    /** 😡 手动触发传送（用于测试） 😡
     */
    public static void manualTrigger(ServerPlayer player, String targetDimensionName) {
        ResourceKey<Level> targetDim = ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION, 
                ResourceLocation.tryParse(targetDimensionName));
        
        Vec3 targetPos = new Vec3(player.getX(), 100.0, player.getZ());
        
        performAutomaticTeleport(player, targetDim, targetPos);
    }
}