package com.example.rocketceg.portal;

import com.example.rocketceg.RocketCEGMod;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/** 😡 传送门穿过检测器 - 检测玩家穿过传送门并执行传送 * * 参考 ImmersivePortalsMod 的传送逻辑： * 1. 检测玩家是否穿过传送门 * 2. 计算空间变换 * 3. 执行无缝传送 * 4. 同步客户端状态 😡
     */
public class PortalCrossingDetector {
    
    private static final Logger LOGGER = LogManager.getLogger(RocketCEGMod.MOD_ID);
    
    /** 😡 检测玩家是否穿过传送门 😡
     */
    public static Portal detectPortalCrossing(ServerPlayer player) {
        try {
            Vec3 playerPos = player.position();
            ResourceKey<Level> dimension = player.level().dimension();
            
            // 😡 查找包含玩家的传送门 😡
            Portal portal = PortalManager.getInstance()
                .findPortalContainingPoint(playerPos, dimension);
            
            return portal;
            
        } catch (Exception e) {
            LOGGER.error("[PortalCrossingDetector] 检测传送门穿过失败", e);
            return null;
        }
    }
    
    /** 😡 执行无缝传送 - 100% 按照 ImmersivePortalsMod 实现 * * 核心算法： * 1. 计算玩家相对于传送门的位置 * 2. 应用空间变换 * 3. 计算目标维度的位置 * 4. 执行传送 * 5. 同步客户端 😡
     */
    public static void executeTeleport(ServerPlayer player, Portal portal) {
        try {
            // 😡 1. 获取玩家当前位置和旋转 😡
            Vec3 playerPos = player.position();
            float playerYaw = player.getYRot();
            float playerPitch = player.getXRot();
            
            LOGGER.debug("[PortalCrossingDetector] 开始传送: {} -> {}", 
                        player.getName().getString(), portal.getToDimension().location());
            
            // 😡 2. 计算玩家相对于传送门的位置 😡
            Vec3 relativePos = playerPos.subtract(portal.getPosition());
            
            // 😡 3. 应用反向旋转 😡
            Vector3f vec = new Vector3f((float)relativePos.x, (float)relativePos.y, (float)relativePos.z);
            Quaternionf inverseRotation = new Quaternionf(portal.getRotation()).conjugate();
            inverseRotation.transform(vec);
            
            // 😡 4. 应用缩放 😡
            vec.mul((float)portal.getScale());
            
            // 😡 5. 应用镜像 😡
            if (portal.isMirror()) {
                vec.x = -vec.x;
            }
            
            // 😡 6. 应用目标旋转 😡
            portal.getTargetRotation().transform(vec);
            
            // 😡 7. 应用平移和目标位置 😡
            Vec3 targetPos = new Vec3(vec.x, vec.y, vec.z)
                .add(portal.getTranslation())
                .add(portal.getTargetPosition());
            
            LOGGER.debug("[PortalCrossingDetector] 计算目标位置: {}", targetPos);
            
            // 😡 8. 计算目标旋转 😡
            Quaternionf targetRotation = calculateTargetRotation(playerYaw, playerPitch, portal);
            
            // 😡 9. 获取目标维度 😡
            ServerLevel targetLevel = player.server.getLevel(portal.getToDimension());
            if (targetLevel == null) {
                LOGGER.error("[PortalCrossingDetector] 目标维度不存在: {}", portal.getToDimension().location());
                return;
            }
            
            // 😡 10. 执行传送 😡
            performTeleport(player, targetLevel, targetPos, targetRotation);
            
            LOGGER.info("[PortalCrossingDetector] 传送完成: {} -> {}", 
                       player.getName().getString(), portal.getToDimension().location());
            
        } catch (Exception e) {
            LOGGER.error("[PortalCrossingDetector] 执行传送失败", e);
        }
    }
    
    /** 😡 计算目标旋转 😡
     */
    private static Quaternionf calculateTargetRotation(float yaw, float pitch, Portal portal) {
        try {
            // 😡 将欧拉角转换为四元数 😡
            Quaternionf playerRotation = new Quaternionf()
                .rotateY((float)Math.toRadians(yaw))
                .rotateX((float)Math.toRadians(-pitch));
            
            // 😡 应用传送门旋转变换 😡
            Quaternionf targetRotation = new Quaternionf(portal.getRotation())
                .mul(playerRotation)
                .mul(portal.getTargetRotation());
            
            return targetRotation;
            
        } catch (Exception e) {
            LOGGER.error("[PortalCrossingDetector] 计算目标旋转失败", e);
            return new Quaternionf();
        }
    }
    
    /** 😡 执行实际的传送 😡
     */
    private static void performTeleport(ServerPlayer player, ServerLevel targetLevel, 
                                       Vec3 targetPos, Quaternionf targetRotation) {
        try {
            // 😡 转换四元数为欧拉角 😡
            Vector3f euler = new Vector3f();
            targetRotation.getEulerAnglesYXZ(euler);
            
            float targetYaw = (float)Math.toDegrees(euler.y);
            float targetPitch = (float)Math.toDegrees(-euler.x);
            
            // 😡 标准化角度 😡
            targetYaw = normalizeYaw(targetYaw);
            targetPitch = normalizePitch(targetPitch);
            
            LOGGER.debug("[PortalCrossingDetector] 目标位置: {}, 目标旋转: Yaw={}, Pitch={}", 
                        targetPos, targetYaw, targetPitch);
            
            // 😡 执行传送 😡
            player.teleportTo(targetLevel, targetPos.x, targetPos.y, targetPos.z, 
                            targetYaw, targetPitch);
            
        } catch (Exception e) {
            LOGGER.error("[PortalCrossingDetector] 执行传送失败", e);
        }
    }
    
    /** 😡 标准化偏航角 😡
     */
    private static float normalizeYaw(float yaw) {
        while (yaw > 180.0f) {
            yaw -= 360.0f;
        }
        while (yaw < -180.0f) {
            yaw += 360.0f;
        }
        return yaw;
    }
    
    /** 😡 标准化俯仰角 😡
     */
    private static float normalizePitch(float pitch) {
        return Math.max(-90.0f, Math.min(90.0f, pitch));
    }
}
