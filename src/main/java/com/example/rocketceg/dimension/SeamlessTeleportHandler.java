package com.example.rocketceg.dimension;

import com.example.rocketceg.RocketCEGMod;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.joml.Quaterniond;
import org.joml.Vector3d;

import java.util.HashSet;
import java.util.Set;

/** 😡 无缝传送处理器 - 100% 模仿 Starlance 的使用方式 * * 使用方式与 Starlance 完全相同： * 1. 创建 TeleportationHandler * 2. 收集实体 * 3. 调用 finalizeTeleport() 😡
     */
public class SeamlessTeleportHandler {

    private static final Logger LOGGER = LogManager.getLogger(RocketCEGMod.MOD_ID);

    // 😡 存储 RocketCEG 的维度 ID 😡
    private static final Set<String> ROCKETCEG_DIMENSIONS = new HashSet<>();

    static {
        // 😡 注册所有 RocketCEG 维度 😡
        String[] planets = {"earth", "moon", "mars", "venus", "mercury", 
                           "jupiter", "saturn", "uranus", "neptune", "pluto"};
        for (String planet : planets) {
            ROCKETCEG_DIMENSIONS.add(RocketCEGMod.MOD_ID + ":" + planet + "_surface");
            ROCKETCEG_DIMENSIONS.add(RocketCEGMod.MOD_ID + ":" + planet + "_orbit");
        }
    }

    /** 😡 检查是否应该使用无缝传送 😡
     */
    public static boolean shouldUseSeamlessTeleport(Entity entity, ServerLevel destination) {
        if (!(entity instanceof ServerPlayer)) {
            return false;
        }
        String sourceDim = entity.level().dimension().location().toString();
        String destDim = destination.dimension().location().toString();
        return ROCKETCEG_DIMENSIONS.contains(sourceDim) || ROCKETCEG_DIMENSIONS.contains(destDim);
    }

    /** 😡 传送单个实体 - 与 Starlance 的 TeleportUtil.teleportEntity 相同的接口 😡
     */
    public static void teleportEntity(Entity entity, ServerLevel targetLevel, Vec3 targetPos) {
        ServerLevel sourceLevel = (ServerLevel) entity.level();
        
        // 😡 创建 TeleportationHandler - 与 Starlance 完全相同的方式 😡
        TeleportationHandler handler = new TeleportationHandler(sourceLevel, targetLevel, false);
        
        // 😡 添加实体到传送列表 😡
        handler.addEntity(entity, targetPos);
        
        // 😡 如果是玩家，收集附近实体 - 与 Starlance 相同的逻辑 😡
        if (entity instanceof ServerPlayer) {
            Vector3d origin = new Vector3d(entity.getX(), entity.getY(), entity.getZ());
            Vector3d newPos = new Vector3d(targetPos.x, targetPos.y, targetPos.z);
            Quaterniond rotation = new Quaterniond(); // 😡 无旋转 😡
            
            handler.collectNearbyEntities(origin, newPos, rotation, entity.position());
        }
        
        // 😡 执行传送 - 与 Starlance 完全相同的方法名 😡
        handler.finalizeTeleport();
        
        LOGGER.info("[RocketCEG] 使用 TeleportationHandler 传送: {} -> {} ({})",
                sourceLevel.dimension().location(),
                targetLevel.dimension().location(),
                targetPos);
    }

    /** 😡 传送玩家到指定维度 - 便捷方法 😡
     */
    public static void teleportPlayer(ServerPlayer player, ResourceKey<Level> targetDimension, Vec3 targetPos) {
        ServerLevel targetLevel = player.server.getLevel(targetDimension);
        if (targetLevel == null) {
            LOGGER.error("[RocketCEG] 目标维度 {} 不存在！", targetDimension.location());
            return;
        }
        teleportEntity(player, targetLevel, targetPos);
    }

    /** 😡 批量传送 - 与 Starlance 的使用模式相同 * * 使用方式： * 1. 创建 handler * 2. 添加多个实体 * 3. 调用 finalizeTeleport() 😡
     */
    public static TeleportationHandler createHandler(ServerLevel sourceLevel, ServerLevel targetLevel) {
        return new TeleportationHandler(sourceLevel, targetLevel, false);
    }

    /** 😡 检查维度是否是 RocketCEG 的维度 😡
     */
    public static boolean isRocketCEGDimension(ResourceKey<Level> dimension) {
        return ROCKETCEG_DIMENSIONS.contains(dimension.location().toString());
    }

    /** 😡 注册自定义维度 😡
     */
    public static void registerDimension(String dimensionId) {
        ROCKETCEG_DIMENSIONS.add(dimensionId);
    }
}