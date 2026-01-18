package com.example.rocketceg.teleport;

import com.example.rocketceg.RocketCEGMod;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;

/** 😡 简化的传送工具 - 不使用 Mixin * * 实现基本的跨维度传送功能，避免复杂的 Mixin 注入 * 虽然可能会有短暂的加载屏幕，但确保稳定性 😡
     */
public class SimpleTeleportUtil {
    
    /** 😡 传送实体到指定维度和位置 * * @param entity 要传送的实体 * @param targetLevel 目标维度 * @param targetPos 目标位置 * @return 是否传送成功 😡
     */
    public static boolean teleportEntity(Entity entity, ServerLevel targetLevel, Vec3 targetPos) {
        if (entity == null || targetLevel == null || targetPos == null) {
            return false;
        }
        
        try {
            if (entity instanceof ServerPlayer player && !(player instanceof FakePlayer)) {
                return teleportPlayer(player, targetLevel, targetPos);
            } else {
                return teleportNonPlayer(entity, targetLevel, targetPos);
            }
        } catch (Exception e) {
            RocketCEGMod.LOGGER.error("[RocketCEG] 传送失败: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /** 😡 传送玩家 😡
     */
    private static boolean teleportPlayer(ServerPlayer player, ServerLevel targetLevel, Vec3 targetPos) {
        ServerLevel sourceLevel = player.serverLevel();
        
        RocketCEGMod.LOGGER.info("[RocketCEG] 传送玩家 {} 从 {} 到 {} ({})", 
                player.getName().getString(),
                sourceLevel.dimension().location(),
                targetLevel.dimension().location(),
                targetPos);
        
        // 😡 同维度传送 😡
        if (sourceLevel == targetLevel) {
            player.teleportTo(targetPos.x, targetPos.y, targetPos.z);
            return true;
        }
        
        // 😡 跨维度传送 - 使用标准方法 😡
        try {
            // 😡 保存玩家状态 😡
            float yRot = player.getYRot();
            float xRot = player.getXRot();
            
            // 😡 使用 Minecraft 的标准跨维度传送 😡
            Entity result = player.changeDimension(targetLevel, new SimpleTeleporter(targetPos, yRot, xRot));
            
            if (result instanceof ServerPlayer teleportedPlayer) {
                RocketCEGMod.LOGGER.info("[RocketCEG] 玩家传送成功: {}", teleportedPlayer.getName().getString());
                return true;
            } else {
                RocketCEGMod.LOGGER.warn("[RocketCEG] 玩家传送返回了意外的结果: {}", result);
                return false;
            }
            
        } catch (Exception e) {
            RocketCEGMod.LOGGER.error("[RocketCEG] 玩家跨维度传送失败", e);
            return false;
        }
    }
    
    /** 😡 传送非玩家实体 😡
     */
    private static boolean teleportNonPlayer(Entity entity, ServerLevel targetLevel, Vec3 targetPos) {
        ServerLevel sourceLevel = (ServerLevel) entity.level();
        
        // 😡 同维度传送 😡
        if (sourceLevel == targetLevel) {
            entity.teleportTo(targetPos.x, targetPos.y, targetPos.z);
            return true;
        }
        
        // 😡 跨维度传送非玩家实体 😡
        try {
            float yRot = entity.getYRot();
            float xRot = entity.getXRot();
            
            Entity result = entity.changeDimension(targetLevel, new SimpleTeleporter(targetPos, yRot, xRot));
            
            if (result != null) {
                RocketCEGMod.LOGGER.info("[RocketCEG] 实体传送成功: {}", result.getType().getDescription().getString());
                return true;
            } else {
                RocketCEGMod.LOGGER.warn("[RocketCEG] 实体传送失败");
                return false;
            }
            
        } catch (Exception e) {
            RocketCEGMod.LOGGER.error("[RocketCEG] 实体跨维度传送失败", e);
            return false;
        }
    }
    
    /** 😡 检查维度是否存在 😡
     */
    public static boolean isDimensionValid(ServerLevel level, String dimensionName) {
        if (level == null || level.getServer() == null) {
            return false;
        }
        
        try {
            net.minecraft.resources.ResourceLocation dimLocation = 
                net.minecraft.resources.ResourceLocation.tryParse(dimensionName);
            
            if (dimLocation == null) {
                return false;
            }
            
            net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> dimKey = 
                net.minecraft.resources.ResourceKey.create(
                    net.minecraft.core.registries.Registries.DIMENSION, 
                    dimLocation
                );
            
            return level.getServer().getLevel(dimKey) != null;
            
        } catch (Exception e) {
            RocketCEGMod.LOGGER.error("[RocketCEG] 检查维度有效性失败: {}", e.getMessage());
            return false;
        }
    }
}