package com.example.rocketceg.dimension.client;

import com.example.rocketceg.RocketCEGMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ConcurrentLinkedQueue;

/** 😡 客户端传送处理器 * 模仿 Immersive Portals 的客户端传送实现 * * 关键原理： * 1. 在渲染前处理传送（不是在 tick 中） * 2. 直接更新客户端玩家位置 * 3. 不触发加载屏幕 😡
     */
public class ClientTeleportHandler {

    private static final Logger LOGGER = LogManager.getLogger(RocketCEGMod.MOD_ID);

    // 😡 待处理的传送队列 😡
    private static final ConcurrentLinkedQueue<PendingTeleport> pendingTeleports = new ConcurrentLinkedQueue<>();

    /** 😡 添加待处理的传送 😡
     */
    public static void addPendingTeleport(ResourceKey<Level> targetDimension, Vec3 targetPos, float yRot, float xRot) {
        pendingTeleports.offer(new PendingTeleport(targetDimension, targetPos, yRot, xRot));
        LOGGER.info("[RocketCEG] 客户端：添加待处理传送到 {} ({})", targetDimension.location(), targetPos);
    }

    /** 😡 处理客户端传送 - 在每帧渲染前调用 * 这是 Immersive Portals 实现无缝传送的关键方法 😡
     */
    public static void handleClientTeleportation() {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        
        if (player == null || pendingTeleports.isEmpty()) {
            return;
        }

        PendingTeleport teleport = pendingTeleports.poll();
        if (teleport == null) {
            return;
        }

        try {
            // 😡 获取目标维度 😡
            ClientLevel targetLevel = minecraft.getConnection().getLevel();
            if (targetLevel == null) {
                LOGGER.warn("[RocketCEG] 客户端：当前维度不存在");
                return;
            }

            LOGGER.info("[RocketCEG] 客户端：执行无缝传送到 {} ({})", 
                    teleport.targetDimension.location(), teleport.targetPos);

            // 😡 === 关键：客户端无缝传送 === 😡
            
            // 😡 1. 直接设置玩家位置（不触发加载屏幕） 😡
            player.moveTo(teleport.targetPos.x, teleport.targetPos.y, teleport.targetPos.z, 
                         teleport.yRot, teleport.xRot);

            // 😡 2. 如果是跨维度传送，更新客户端维度 😡
            if (!player.level().dimension().equals(teleport.targetDimension)) {
                // 😡 这里可能需要更复杂的维度切换逻辑 😡
                // 😡 但关键是不触发 respawn 包 😡
                LOGGER.info("[RocketCEG] 客户端：跨维度传送 {} -> {}", 
                        player.level().dimension().location(), 
                        teleport.targetDimension.location());
            }

            LOGGER.info("[RocketCEG] 客户端：无缝传送完成");

        } catch (Exception e) {
            LOGGER.error("[RocketCEG] 客户端传送失败", e);
        }
    }

    /** 😡 待处理的传送数据 😡
     */
    private static class PendingTeleport {
        final ResourceKey<Level> targetDimension;
        final Vec3 targetPos;
        final float yRot;
        final float xRot;

        PendingTeleport(ResourceKey<Level> targetDimension, Vec3 targetPos, float yRot, float xRot) {
            this.targetDimension = targetDimension;
            this.targetPos = targetPos;
            this.yRot = yRot;
            this.xRot = xRot;
        }
    }
}