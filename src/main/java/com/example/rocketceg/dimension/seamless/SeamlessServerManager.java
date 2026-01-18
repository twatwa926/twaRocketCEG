package com.example.rocketceg.dimension.seamless;

import com.example.rocketceg.RocketCEGMod;
import com.example.rocketceg.util.TeleportUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.ITeleporter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** 😡 服务端无缝传送管理器 - 基于 Starlance 的方法 * * 实现真正的无缝传送： * 1. 使用 TeleportUtil 进行无缝传送 * 2. 避免任何加载屏幕 * 3. 完全模仿 Starlance 的方法 😡
     */
public class SeamlessServerManager {
    
    private static final Logger LOGGER = LogManager.getLogger(RocketCEGMod.MOD_ID);
    
    /** 😡 处理无缝传送 - Starlance 风格 😡
     */
    public static boolean handleSeamlessTeleport(ServerPlayer player, ServerLevel destination, ITeleporter teleporter) {
        ServerLevel sourceLevel = player.serverLevel();
        
        // 😡 同维度传送，使用标准逻辑 😡
        if (sourceLevel.dimension().equals(destination.dimension())) {
            return false;
        }
        
        try {
            // 😡 获取传送信息 😡
            PortalInfo portalInfo = teleporter.getPortalInfo(player, destination, 
                (level) -> new PortalInfo(player.position(), Vec3.ZERO, player.getYRot(), player.getXRot()));
            
            if (portalInfo == null) {
                LOGGER.warn("[RocketCEG] 无法获取传送信息");
                return false;
            }
            
            Vec3 targetPos = portalInfo.pos;
            float yRot = portalInfo.yRot;
            float xRot = portalInfo.xRot;
            
            LOGGER.info("[RocketCEG] Starlance风格无缝传送: {} -> {} ({})", 
                    sourceLevel.dimension().location(), 
                    destination.dimension().location(), 
                    targetPos);
            
            // 😡 === 使用 TeleportUtil 进行无缝传送 === 😡
            TeleportUtil.TeleportData teleportData = new TeleportUtil.TeleportData(
                destination, targetPos, yRot, xRot
            );
            
            TeleportUtil.teleportEntity(player, teleportData);
            
            // 😡 触发传送后处理 😡
            teleporter.placeEntity(player, sourceLevel, destination, yRot, 
                (usePortal) -> player);
            
            LOGGER.info("[RocketCEG] Starlance风格无缝传送完成");
            return true;
            
        } catch (Exception e) {
            LOGGER.error("[RocketCEG] Starlance风格无缝传送失败", e);
            return false;
        }
    }
    
    /** 😡 检查是否应该使用无缝传送 😡
     */
    public static boolean shouldUseSeamlessTeleport(ServerLevel source, ServerLevel destination) {
        String sourceDim = source.dimension().location().toString();
        String destDim = destination.dimension().location().toString();
        
        return sourceDim.startsWith("rocketceg:") || destDim.startsWith("rocketceg:");
    }
}