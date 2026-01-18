package com.example.rocketceg.util;

import com.example.rocketceg.RocketCEGMod;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** 😡 传送工具类 - 基于 Starlance 的 TeleportUtil * * 提供真正的无缝传送功能，完全避免加载屏幕 😡
     */
public class TeleportUtil {
    
    private static final Logger LOGGER = LogManager.getLogger(RocketCEGMod.MOD_ID);
    
    /** 😡 无缝传送实体到指定维度和位置 * * @param entity 要传送的实体 * @param targetLevel 目标维度 * @param targetPos 目标位置 😡
     */
    public static void teleportEntity(Entity entity, ServerLevel targetLevel, Vec3 targetPos) {
        if (!(entity instanceof ServerPlayer player)) {
            // 😡 对于非玩家实体，使用简单的传送 😡
            teleportNonPlayerEntity(entity, targetLevel, targetPos);
            return;
        }
        
        ServerLevel sourceLevel = player.serverLevel();
        
        LOGGER.info("[RocketCEG] TeleportUtil: 传送玩家 {} 从 {} 到 {} ({})", 
                player.getName().getString(),
                sourceLevel.dimension().location(),
                targetLevel.dimension().location(),
                targetPos);
        
        // 😡 同维度传送 😡
        if (sourceLevel == targetLevel) {
            player.teleportTo(targetPos.x, targetPos.y, targetPos.z);
            return;
        }
        
        // 😡 跨维度无缝传送 😡
        teleportPlayerSeamlessly(player, sourceLevel, targetLevel, targetPos);
    }
    
    /** 😡 无缝传送玩家 - Starlance 风格 😡
     */
    private static void teleportPlayerSeamlessly(ServerPlayer player, ServerLevel sourceLevel, 
                                               ServerLevel targetLevel, Vec3 targetPos) {
        try {
            // 😡 1. 保存玩家状态 😡
            float yRot = player.getYRot();
            float xRot = player.getXRot();
            
            // 😡 2. 从源世界移除玩家（不触发死亡或重生） 😡
            sourceLevel.removePlayerImmediately(player, Entity.RemovalReason.CHANGED_DIMENSION);
            
            // 😡 3. 设置新的服务器世界 😡
            player.setServerLevel(targetLevel);
            
            // 😡 4. 设置新位置 😡
            player.moveTo(targetPos.x, targetPos.y, targetPos.z, yRot, xRot);
            
            // 😡 5. 添加到目标世界 😡
            targetLevel.addDuringTeleport(player);
            
            // 😡 6. 发送必要的同步包（避免 respawn 包） 😡
            sendTeleportSyncPackets(player, targetLevel, targetPos, yRot, xRot);
            
            // 😡 7. 触发维度切换事件 😡
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(
                new net.minecraftforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent(
                    player, sourceLevel.dimension(), targetLevel.dimension()
                )
            );
            
            LOGGER.info("[RocketCEG] TeleportUtil: 玩家无缝传送完成");
            
        } catch (Exception e) {
            LOGGER.error("[RocketCEG] TeleportUtil: 玩家无缝传送失败", e);
            
            // 😡 失败时的回退处理 - 使用简单的传送 😡
            try {
                player.teleportTo(targetPos.x, targetPos.y, targetPos.z);
            } catch (Exception fallbackError) {
                LOGGER.error("[RocketCEG] TeleportUtil: 回退传送也失败", fallbackError);
            }
        }
    }
    
    /** 😡 传送非玩家实体 😡
     */
    private static void teleportNonPlayerEntity(Entity entity, ServerLevel targetLevel, Vec3 targetPos) {
        ServerLevel sourceLevel = (ServerLevel) entity.level();
        
        if (sourceLevel == targetLevel) {
            entity.teleportTo(targetPos.x, targetPos.y, targetPos.z);
            return;
        }
        
        // 😡 跨维度传送非玩家实体 😡
        float yRot = entity.getYRot();
        float xRot = entity.getXRot();
        
        // 😡 从源世界移除 😡
        entity.unRide();
        entity.remove(Entity.RemovalReason.CHANGED_DIMENSION);
        
        // 😡 在目标世界创建新实体 😡
        Entity newEntity = entity.getType().create(targetLevel);
        if (newEntity != null) {
            newEntity.restoreFrom(entity);
            newEntity.moveTo(targetPos.x, targetPos.y, targetPos.z, yRot, xRot);
            targetLevel.addDuringTeleport(newEntity);
        }
        
        LOGGER.info("[RocketCEG] TeleportUtil: 非玩家实体传送完成");
    }
    
    /** 😡 发送传送同步包 - 关键是避免 respawn 包 😡
     */
    private static void sendTeleportSyncPackets(ServerPlayer player, ServerLevel targetLevel, 
                                              Vec3 targetPos, float yRot, float xRot) {
        try {
            // 😡 1. 位置同步包 - 最重要的包 😡
            player.connection.send(new ClientboundPlayerPositionPacket(
                targetPos.x, targetPos.y, targetPos.z,
                yRot, xRot,
                java.util.Collections.emptySet(),
                0
            ));
            
            // 😡 2. 世界信息包（但不是 respawn 包） 😡
            player.server.getPlayerList().sendLevelInfo(player, targetLevel);
            
            // 😡 3. 玩家能力包 😡
            player.connection.send(new ClientboundPlayerAbilitiesPacket(player.getAbilities()));
            
            // 😡 4. 游戏模式包 😡
            player.connection.send(new ClientboundGameEventPacket(
                ClientboundGameEventPacket.CHANGE_GAME_MODE, 
                player.gameMode.getGameModeForPlayer().getId()
            ));
            
            // 😡 5. 玩家信息更新 😡
            player.server.getPlayerList().sendAllPlayerInfo(player);
            
            // 😡 6. 经验同步 😡
            player.connection.send(new ClientboundSetExperiencePacket(
                player.experienceProgress, 
                player.totalExperience, 
                player.experienceLevel
            ));
            
            // 😡 7. 生命值和饥饿值同步 😡
            player.connection.send(new ClientboundSetHealthPacket(
                player.getHealth(), 
                player.getFoodData().getFoodLevel(), 
                player.getFoodData().getSaturationLevel()
            ));
            
            LOGGER.info("[RocketCEG] TeleportUtil: 同步包发送完成");
            
        } catch (Exception e) {
            LOGGER.error("[RocketCEG] TeleportUtil: 发送同步包失败", e);
        }
    }
    
    /** 😡 传送数据类 - 用于批量传送 😡
     */
    public static class TeleportData {
        public final ServerLevel targetLevel;
        public final Vec3 position;
        public final float yRot;
        public final float xRot;
        
        public TeleportData(ServerLevel targetLevel, Vec3 position, float yRot, float xRot) {
            this.targetLevel = targetLevel;
            this.position = position;
            this.yRot = yRot;
            this.xRot = xRot;
        }
    }
    
    /** 😡 使用传送数据传送实体 😡
     */
    public static void teleportEntity(Entity entity, TeleportData data) {
        teleportEntity(entity, data.targetLevel, data.position);
        if (entity instanceof ServerPlayer player) {
            player.setYRot(data.yRot);
            player.setXRot(data.xRot);
        }
    }
}