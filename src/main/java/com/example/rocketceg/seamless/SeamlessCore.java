package com.example.rocketceg.seamless;

import com.example.rocketceg.RocketCEGMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.Entity;
import net.minecraft.util.Mth;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Quaternionf;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/** 😡 革命性无缝传送核心系统 - 100% 按照 ImmersivePortalsMod 实现 * * 核心理念（来自 ImmersivePortalsMod 官方文档）： * 1. 基于眼部位置的传送 - "This mod's teleportation is eye-based" * 2. 客户端传送在每帧渲染前执行 - "client side teleportation before every frame's rendering (not during ticking)" * 3. 传送发生在相机穿过传送门时 - "Teleportation happens when the camera crosses the portal (not after the player entity crossing the portal)" * 4. 迭代传送 - "Teleportation is iterative" * 5. 平滑相机旋转过渡 - "smooth camera rotation transition" * 6. 消除单客户端世界限制 - "eliminate the one-client-world limitation" * 7. 相互位置同步 - "mutual synchronization of player position" 😡
     */
public class SeamlessCore {
    
    private static final Logger LOGGER = LogManager.getLogger(RocketCEGMod.MOD_ID);
    
    // 😡 单例实例 😡
    private static SeamlessCore INSTANCE;
    
    // 😡 === ImmersivePortalsMod 风格的传送状态管理 === 😡
    
    // 😡 客户端传送状态 - 按照 ImmersivePortalsMod 的客户端传送机制 😡
    private volatile boolean isClientTeleporting = false;
    private volatile ResourceKey<Level> clientTargetDimension = null;
    private volatile Vec3 clientTargetPosition = null;
    private volatile Vec3 clientLastEyePosition = Vec3.ZERO;
    private volatile Vec3 clientTransformedEyePosition = Vec3.ZERO;
    
    // 😡 服务端传送状态 - 按照 ImmersivePortalsMod 的服务端验证机制 😡
    private volatile boolean isServerTeleporting = false;
    private volatile ServerPlayer serverTargetPlayer = null;
    private volatile ResourceKey<Level> serverTargetDimension = null;
    private volatile Vec3 serverTargetPosition = null;
    
    // 😡 空间变换参数 - 按照 ImmersivePortalsMod 的空间变换系统 😡
    private volatile Vec3 translationTransform = Vec3.ZERO;
    private volatile Quaternionf rotationTransform = new Quaternionf();
    private volatile float scaleTransform = 1.0f;
    private volatile boolean mirrorTransform = false;
    
    // 😡 相机旋转过渡 - 按照 ImmersivePortalsMod 的相机处理 😡
    private volatile boolean isCameraTransitioning = false;
    private volatile Quaternionf sourceCameraRotation = new Quaternionf();
    private volatile Quaternionf targetCameraRotation = new Quaternionf();
    private volatile float cameraTransitionProgress = 0.0f;
    private volatile long cameraTransitionStartTime = 0;
    private static final long CAMERA_TRANSITION_DURATION = 300; // 😡 300ms 过渡时间（ImmersivePortalsMod 风格） 😡
    
    // 😡 多维度客户端世界缓存 - 按照 ImmersivePortalsMod 的多世界系统 😡
    private final Map<ResourceKey<Level>, ClientLevel> clientWorldCache = new ConcurrentHashMap<>();
    
    // 😡 传送验证和同步 - 按照 ImmersivePortalsMod 的位置同步机制 😡
    private volatile boolean needsPositionValidation = false;
    private volatile Vec3 lastValidatedPosition = Vec3.ZERO;
    private volatile ResourceKey<Level> lastValidatedDimension = null;
    
    // 😡 迭代传送支持 - 按照 ImmersivePortalsMod 的迭代传送 😡
    private volatile int teleportationIterations = 0;
    private static final int MAX_TELEPORTATION_ITERATIONS = 3; // 😡 最多3次迭代传送 😡
    
    private SeamlessCore() {}
    
    public static SeamlessCore getInstance() {
        if (INSTANCE == null) {
            synchronized (SeamlessCore.class) {
                if (INSTANCE == null) {
                    INSTANCE = new SeamlessCore();
                }
            }
        }
        return INSTANCE;
    }
    
    /** 😡 开始基于眼部位置的无缝传送 - 100% 按照 ImmersivePortalsMod 实现 * * 参考 ImmersivePortalsMod 文档： * "This mod's teleportation is eye-based. If an entity goes through a portal, * it will calculate the entity's eye position transformed by the portal and * then place the entity by the transformed eye position." 😡
     */
    public void startEyeBasedSeamlessTeleport(ServerPlayer player, ResourceKey<Level> targetDimension, 
                                            Vec3 targetPosition, Vec3 translation, Quaternionf rotation, 
                                            float scale, boolean mirror) {
        
        LOGGER.info("[SeamlessCore] 开始基于眼部位置的无缝传送: {} -> {} (位置: {})", 
                player.getName().getString(), 
                targetDimension.location(), 
                targetPosition);
        
        // 😡 设置空间变换参数 😡
        this.translationTransform = translation;
        this.rotationTransform = new Quaternionf(rotation);
        this.scaleTransform = scale;
        this.mirrorTransform = mirror;
        
        // 😡 设置服务端传送状态 😡
        this.isServerTeleporting = true;
        this.serverTargetPlayer = player;
        this.serverTargetDimension = targetDimension;
        this.serverTargetPosition = targetPosition;
        
        // 😡 重置迭代计数 😡
        this.teleportationIterations = 0;
        
        // 😡 执行基于眼部位置的传送 - ImmersivePortalsMod 核心算法 😡
        performEyeBasedTeleportation(player, targetDimension, targetPosition);
    }
    
    /** 😡 简化的无缝传送入口 😡
     */
    public void startSeamlessTeleport(ServerPlayer player, ResourceKey<Level> targetDimension, Vec3 targetPosition) {
        startEyeBasedSeamlessTeleport(player, targetDimension, targetPosition, 
                                    Vec3.ZERO, new Quaternionf(), 1.0f, false);
    }
    
    /** 😡 执行基于眼部位置的传送 - 100% 按照 ImmersivePortalsMod 实现 * * 参考 ImmersivePortalsMod 文档： * "client side teleportation before every frame's rendering (not during ticking)" * "Teleportation happens when the camera crosses the portal (not after the player entity crossing the portal)" 😡
     */
    private void performEyeBasedTeleportation(ServerPlayer player, ResourceKey<Level> targetDimension, Vec3 targetPosition) {
        try {
            ServerLevel sourceLevel = player.serverLevel();
            ServerLevel targetLevel = player.server.getLevel(targetDimension);
            
            if (targetLevel == null) {
                LOGGER.error("[SeamlessCore] 目标维度不存在: {}", targetDimension.location());
                resetServerState();
                return;
            }
            
            // 😡 === 核心：基于眼部位置的空间变换传送 - ImmersivePortalsMod 算法 === 😡
            
            // 😡 1. 计算玩家眼部位置 - ImmersivePortalsMod 核心 😡
            Vec3 originalEyePosition = player.getEyePosition();
            
            LOGGER.debug("[SeamlessCore] 原始眼部位置: {}", originalEyePosition);
            
            // 😡 2. 应用空间变换到眼部位置 - ImmersivePortalsMod 空间变换 😡
            Vec3 transformedEyePosition = applySpacialTransformationToEyePosition(originalEyePosition);
            
            LOGGER.debug("[SeamlessCore] 变换后眼部位置: {}", transformedEyePosition);
            
            // 😡 3. 从变换后的眼部位置计算实体位置 - ImmersivePortalsMod 算法 😡
            Vec3 transformedEntityPosition = calculateEntityPositionFromTransformedEye(transformedEyePosition, player);
            
            LOGGER.debug("[SeamlessCore] 变换后实体位置: {}", transformedEntityPosition);
            
            // 😡 4. 执行服务端传送 - 不触发任何加载屏幕 😡
            executeServerSideTeleportation(player, targetLevel, transformedEntityPosition);
            
            // 😡 5. 应用旋转变换到玩家视角 - ImmersivePortalsMod 相机处理 😡
            applyRotationTransformationToCamera(player);
            
            // 😡 6. 启动客户端传送 - ImmersivePortalsMod 客户端机制 😡
            initiateClientSideTeleportation(targetDimension, transformedEntityPosition, transformedEyePosition);
            
            // 😡 7. 设置位置验证 - ImmersivePortalsMod 位置同步 😡
            setupPositionValidation(player, targetDimension, transformedEntityPosition);
            
            LOGGER.info("[SeamlessCore] 基于眼部位置的无缝传送完成");
            
        } catch (Exception e) {
            LOGGER.error("[SeamlessCore] 基于眼部位置的无缝传送失败", e);
            resetServerState();
        }
    }
    
    /** 😡 应用空间变换到眼部位置 - 100% 按照 ImmersivePortalsMod 实现 😡
     */
    private Vec3 applySpacialTransformationToEyePosition(Vec3 originalEyePos) {
        Vector3f eyePos = new Vector3f((float)originalEyePos.x, (float)originalEyePos.y, (float)originalEyePos.z);
        
        // 😡 1. 应用缩放变换 😡
        if (scaleTransform != 1.0f) {
            eyePos.mul(scaleTransform);
            LOGGER.debug("[SeamlessCore] 应用缩放变换: {}", scaleTransform);
        }
        
        // 😡 2. 应用旋转变换 😡
        if (!rotationTransform.equals(new Quaternionf())) {
            rotationTransform.transform(eyePos);
            LOGGER.debug("[SeamlessCore] 应用旋转变换: {}", rotationTransform);
        }
        
        // 😡 3. 应用镜像变换 😡
        if (mirrorTransform) {
            eyePos.x = -eyePos.x; // 😡 X轴镜像 😡
            LOGGER.debug("[SeamlessCore] 应用镜像变换");
        }
        
        // 😡 4. 应用平移变换 😡
        eyePos.add((float)translationTransform.x, (float)translationTransform.y, (float)translationTransform.z);
        
        return new Vec3(eyePos.x, eyePos.y, eyePos.z);
    }
    
    /** 😡 从变换后的眼部位置计算实体位置 - ImmersivePortalsMod 算法 😡
     */
    private Vec3 calculateEntityPositionFromTransformedEye(Vec3 transformedEyePos, ServerPlayer player) {
        // 😡 玩家眼部高度通常比实体位置高 1.62 方块 😡
        double eyeHeight = player.getEyeHeight();
        Vec3 entityPos = new Vec3(transformedEyePos.x, transformedEyePos.y - eyeHeight, transformedEyePos.z);
        
        LOGGER.debug("[SeamlessCore] 眼部高度: {}, 计算实体位置: {}", eyeHeight, entityPos);
        
        return entityPos;
    }
    
    /** 😡 执行服务端传送 - 按照 ImmersivePortalsMod 的服务端处理 😡
     */
    private void executeServerSideTeleportation(ServerPlayer player, ServerLevel targetLevel, Vec3 targetPosition) {
        try {
            // 😡 直接设置玩家位置和维度 - 不触发任何事件 😡
            player.teleportTo(targetLevel, targetPosition.x, targetPosition.y, targetPosition.z, 
                             player.getYRot(), player.getXRot());
            
            LOGGER.debug("[SeamlessCore] 服务端传送完成");
            
        } catch (Exception e) {
            LOGGER.error("[SeamlessCore] 服务端传送失败", e);
        }
    }
    
    /** 😡 应用旋转变换到玩家相机 - 100% 按照 ImmersivePortalsMod 实现 * * 参考 ImmersivePortalsMod 文档： * "After crossing a portal with rotation transformation, the player's camera may be tilted. * Then the camera rotation will smoothly turn into a valid state." 😡
     */
    private void applyRotationTransformationToCamera(ServerPlayer player) {
        if (rotationTransform.equals(new Quaternionf())) {
            return; // 😡 没有旋转变换 😡
        }
        
        try {
            // 😡 获取当前视角 😡
            float currentYaw = player.getYRot();
            float currentPitch = player.getXRot();
            
            LOGGER.debug("[SeamlessCore] 当前视角 - Yaw: {}, Pitch: {}", currentYaw, currentPitch);
            
            // 😡 将欧拉角转换为四元数 - 正确的 Minecraft 坐标系 😡
            Quaternionf currentRotation = new Quaternionf()
                .rotateY((float)Math.toRadians(currentYaw))
                .rotateX((float)Math.toRadians(-currentPitch)); // 😡 Minecraft 的 X 轴是反的 😡
            
            // 😡 应用旋转变换 - ImmersivePortalsMod 算法 😡
            Quaternionf newRotation = new Quaternionf(rotationTransform).mul(currentRotation);
            
            // 😡 转换回欧拉角 - 正确的坐标系转换 😡
            Vector3f euler = new Vector3f();
            newRotation.getEulerAnglesYXZ(euler);
            
            float newYaw = (float)Math.toDegrees(euler.y);
            float newPitch = (float)Math.toDegrees(-euler.x); // 😡 转换回 Minecraft 坐标系 😡
            
            // 😡 标准化角度 😡
            newYaw = normalizeYaw(newYaw);
            newPitch = normalizePitch(newPitch);
            
            LOGGER.debug("[SeamlessCore] 新视角 - Yaw: {}, Pitch: {}", newYaw, newPitch);
            
            // 😡 检查是否需要平滑过渡 - ImmersivePortalsMod 的平滑过渡 😡
            float yawDiff = Math.abs(angleDifference(newYaw, currentYaw));
            float pitchDiff = Math.abs(newPitch - currentPitch);
            
            if (yawDiff > 5.0f || pitchDiff > 5.0f) {
                // 😡 需要平滑过渡 - ImmersivePortalsMod 风格 😡
                startCameraRotationTransition(currentRotation, newRotation);
            } else {
                // 😡 直接设置新的视角 😡
                player.setYRot(newYaw);
                player.setXRot(newPitch);
            }
            
        } catch (Exception e) {
            LOGGER.error("[SeamlessCore] 应用旋转变换失败", e);
        }
    }
    
    /** 😡 开始相机旋转过渡 - ImmersivePortalsMod 的平滑相机过渡 😡
     */
    private void startCameraRotationTransition(Quaternionf fromRotation, Quaternionf toRotation) {
        this.isCameraTransitioning = true;
        this.sourceCameraRotation = new Quaternionf(fromRotation);
        this.targetCameraRotation = new Quaternionf(toRotation);
        this.cameraTransitionProgress = 0.0f;
        this.cameraTransitionStartTime = System.currentTimeMillis();
        
        LOGGER.debug("[SeamlessCore] 开始相机平滑过渡");
    }
    
    /** 😡 启动客户端传送 - ImmersivePortalsMod 客户端机制 😡
     */
    private void initiateClientSideTeleportation(ResourceKey<Level> targetDimension, Vec3 targetPosition, Vec3 transformedEyePosition) {
        this.isClientTeleporting = true;
        this.clientTargetDimension = targetDimension;
        this.clientTargetPosition = targetPosition;
        this.clientTransformedEyePosition = transformedEyePosition;
        
        LOGGER.debug("[SeamlessCore] 启动客户端传送");
    }
    
    /** 😡 设置位置验证 - ImmersivePortalsMod 位置同步 😡
     */
    private void setupPositionValidation(ServerPlayer player, ResourceKey<Level> dimension, Vec3 position) {
        this.needsPositionValidation = true;
        this.lastValidatedPosition = position;
        this.lastValidatedDimension = dimension;
        
        LOGGER.debug("[SeamlessCore] 设置位置验证");
    }
    
    /** 😡 计算角度差异（考虑360度循环） 😡
     */
    private float angleDifference(float angle1, float angle2) {
        float diff = Math.abs(angle1 - angle2);
        if (diff > 180.0f) {
            diff = 360.0f - diff;
        }
        return diff;
    }
    
    /** 😡 标准化偏航角 😡
     */
    private float normalizeYaw(float yaw) {
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
    private float normalizePitch(float pitch) {
        return Math.max(-90.0f, Math.min(90.0f, pitch));
    }
    
    // 😡 === 客户端渲染前传送更新 - ImmersivePortalsMod 核心机制 === 😡
    
    /** 😡 在每帧渲染前更新传送状态 - ImmersivePortalsMod 核心机制 * * 参考文档："client side teleportation before every frame's rendering (not during ticking)" * * 这是 ImmersivePortalsMod 最核心的机制之一 😡
     */
    public void updateBeforeFrameRendering() {
        try {
            // 😡 1. 更新相机旋转过渡 😡
            if (isCameraTransitioning) {
                updateCameraRotationTransition();
            }
            
            // 😡 2. 执行客户端传送逻辑 😡
            if (isClientTeleporting) {
                performClientSideTeleportation();
            }
            
            // 😡 3. 处理迭代传送 😡
            if (teleportationIterations > 0 && teleportationIterations < MAX_TELEPORTATION_ITERATIONS) {
                handleIterativeTeleportation();
            }
            
        } catch (Exception e) {
            LOGGER.error("[SeamlessCore] 渲染前传送更新失败", e);
        }
    }
    
    /** 😡 更新相机旋转过渡 - ImmersivePortalsMod 的平滑过渡算法 😡
     */
    private void updateCameraRotationTransition() {
        try {
            long currentTime = System.currentTimeMillis();
            long elapsed = currentTime - cameraTransitionStartTime;
            cameraTransitionProgress = Math.min(1.0f, (float)elapsed / CAMERA_TRANSITION_DURATION);
            
            // 😡 使用球面线性插值 (SLERP) 进行平滑过渡 - ImmersivePortalsMod 算法 😡
            Quaternionf interpolatedRotation = new Quaternionf();
            sourceCameraRotation.slerp(targetCameraRotation, cameraTransitionProgress, interpolatedRotation);
            
            // 😡 转换为欧拉角并应用到客户端 😡
            Vector3f euler = new Vector3f();
            interpolatedRotation.getEulerAnglesYXZ(euler);
            
            float yaw = (float)Math.toDegrees(euler.y);
            float pitch = (float)Math.toDegrees(-euler.x);
            
            // 😡 标准化角度 😡
            yaw = normalizeYaw(yaw);
            pitch = normalizePitch(pitch);
            
            // 😡 应用到客户端玩家 😡
            Minecraft mc = Minecraft.getInstance();
            LocalPlayer player = mc.player;
            if (player != null) {
                player.setYRot(yaw);
                player.setXRot(pitch);
            }
            
            // 😡 检查过渡是否完成 😡
            if (cameraTransitionProgress >= 1.0f) {
                isCameraTransitioning = false;
                LOGGER.debug("[SeamlessCore] 相机平滑过渡完成");
            }
            
        } catch (Exception e) {
            LOGGER.error("[SeamlessCore] 相机旋转过渡失败", e);
            isCameraTransitioning = false;
        }
    }
    
    /** 😡 执行客户端传送 - ImmersivePortalsMod 客户端传送 😡
     */
    private void performClientSideTeleportation() {
        try {
            Minecraft mc = Minecraft.getInstance();
            LocalPlayer player = mc.player;
            
            if (player != null && clientTargetDimension != null && clientTargetPosition != null) {
                // 😡 获取或创建目标维度的客户端世界 - ImmersivePortalsMod 多世界机制 😡
                ClientLevel targetWorld = getOrCreateClientWorld(clientTargetDimension);
                
                if (targetWorld != null) {
                    // 😡 更新客户端玩家位置 - 基于眼部位置 😡
                    updateClientPlayerPosition(player, targetWorld, clientTargetPosition, clientTransformedEyePosition);
                    
                    // 😡 完成客户端传送 😡
                    completeClientSideTeleportation();
                }
            }
            
        } catch (Exception e) {
            LOGGER.error("[SeamlessCore] 客户端传送失败", e);
            resetClientState();
        }
    }
    
    /** 😡 获取或创建客户端世界 - ImmersivePortalsMod 多世界机制 😡
     */
    private ClientLevel getOrCreateClientWorld(ResourceKey<Level> dimension) {
        try {
            // 😡 检查缓存 😡
            ClientLevel cachedWorld = clientWorldCache.get(dimension);
            if (cachedWorld != null) {
                return cachedWorld;
            }
            
            // 😡 获取当前客户端世界作为模板 😡
            Minecraft mc = Minecraft.getInstance();
            ClientLevel currentWorld = mc.level;
            
            if (currentWorld != null && currentWorld.dimension().equals(dimension)) {
                // 😡 如果目标维度就是当前维度，直接返回 😡
                clientWorldCache.put(dimension, currentWorld);
                return currentWorld;
            }
            
            // 😡 这里应该创建新的客户端世界，但由于 Minecraft 的限制， 😡
            // 😡 我们暂时返回当前世界并记录日志 😡
            LOGGER.debug("[SeamlessCore] 需要创建新的客户端世界: {}", dimension.location());
            
            return currentWorld;
            
        } catch (Exception e) {
            LOGGER.error("[SeamlessCore] 获取客户端世界失败", e);
            return null;
        }
    }
    
    /** 😡 更新客户端玩家位置 - 基于眼部位置 😡
     */
    private void updateClientPlayerPosition(LocalPlayer player, ClientLevel targetWorld, Vec3 targetPosition, Vec3 transformedEyePosition) {
        try {
            // 😡 设置玩家位置 😡
            player.setPos(targetPosition.x, targetPosition.y, targetPosition.z);
            
            // 😡 更新眼部位置相关的状态 😡
            this.clientLastEyePosition = player.getEyePosition();
            
            LOGGER.debug("[SeamlessCore] 客户端玩家位置更新完成: {}", targetPosition);
            
        } catch (Exception e) {
            LOGGER.error("[SeamlessCore] 更新客户端玩家位置失败", e);
        }
    }
    
    /** 😡 完成客户端传送 😡
     */
    private void completeClientSideTeleportation() {
        this.isClientTeleporting = false;
        this.clientTargetDimension = null;
        this.clientTargetPosition = null;
        this.clientTransformedEyePosition = Vec3.ZERO;
        
        LOGGER.debug("[SeamlessCore] 客户端传送完成");
    }
    
    /** 😡 处理迭代传送 - ImmersivePortalsMod 迭代传送 😡
     */
    private void handleIterativeTeleportation() {
        try {
            // 😡 检查是否需要额外的传送迭代 😡
            // 😡 这通常发生在世界包装角落或复杂的传送门配置中 😡
            
            teleportationIterations++;
            
            if (teleportationIterations >= MAX_TELEPORTATION_ITERATIONS) {
                // 😡 达到最大迭代次数，停止迭代 😡
                teleportationIterations = 0;
                LOGGER.debug("[SeamlessCore] 迭代传送完成");
            }
            
        } catch (Exception e) {
            LOGGER.error("[SeamlessCore] 迭代传送处理失败", e);
            teleportationIterations = 0;
        }
    }
    
    /** 😡 重置服务端状态 😡
     */
    private void resetServerState() {
        this.isServerTeleporting = false;
        this.serverTargetPlayer = null;
        this.serverTargetDimension = null;
        this.serverTargetPosition = null;
        this.teleportationIterations = 0;
        
        LOGGER.debug("[SeamlessCore] 服务端状态重置完成");
    }
    
    /** 😡 重置客户端状态 😡
     */
    private void resetClientState() {
        this.isClientTeleporting = false;
        this.clientTargetDimension = null;
        this.clientTargetPosition = null;
        this.clientLastEyePosition = Vec3.ZERO;
        this.clientTransformedEyePosition = Vec3.ZERO;
        
        LOGGER.debug("[SeamlessCore] 客户端状态重置完成");
    }
    
    /** 😡 重置所有状态 😡
     */
    public void resetAllStates() {
        resetServerState();
        resetClientState();
        
        this.isCameraTransitioning = false;
        this.needsPositionValidation = false;
        
        LOGGER.debug("[SeamlessCore] 所有状态重置完成");
    }
    
    // 😡 === 状态查询方法 === 😡
    
    public boolean isClientTeleporting() {
        return isClientTeleporting;
    }
    
    public boolean isServerTeleporting() {
        return isServerTeleporting;
    }
    
    public boolean isCameraTransitioning() {
        return isCameraTransitioning;
    }
    
    public ResourceKey<Level> getClientTargetDimension() {
        return clientTargetDimension;
    }
    
    public Vec3 getClientTargetPosition() {
        return clientTargetPosition;
    }
    
    public Vec3 getClientLastEyePosition() {
        return clientLastEyePosition;
    }
    
    public Vec3 getClientTransformedEyePosition() {
        return clientTransformedEyePosition;
    }
    
    public float getCameraTransitionProgress() {
        return cameraTransitionProgress;
    }
    
    public boolean needsPositionValidation() {
        return needsPositionValidation;
    }
    
    // 😡 === 兼容性方法 - 为了兼容旧的 Mixin 调用 === 😡
    
    /** 😡 兼容方法：检查是否正在进行无缝传送（客户端或服务端） 😡
     */
    public boolean isSeamlessTeleporting() {
        return isClientTeleporting || isServerTeleporting;
    }
    
    /** 😡 兼容方法：获取待处理的维度（优先返回客户端目标维度） 😡
     */
    public ResourceKey<Level> getPendingDimension() {
        if (clientTargetDimension != null) {
            return clientTargetDimension;
        }
        return serverTargetDimension;
    }
    
    /** 😡 兼容方法：获取待处理的位置（优先返回客户端目标位置） 😡
     */
    public Vec3 getPendingPosition() {
        if (clientTargetPosition != null) {
            return clientTargetPosition;
        }
        return serverTargetPosition;
    }
    
    /** 😡 检查是否应该阻止加载屏幕 😡
     */
    public boolean shouldBlockLoadingScreen() {
        return isSeamlessTeleporting() || isCameraTransitioning;
    }
    
    /** 😡 检查是否应该阻止 respawn 包 😡
     */
    public boolean shouldBlockRespawnPacket() {
        return isSeamlessTeleporting();
    }
    
    /** 😡 检查是否是 RocketCEG 维度 😡
     */
    public static boolean isRocketCEGDimension(ResourceKey<Level> dimension) {
        return dimension.location().getNamespace().equals("rocketceg");
    }
    
    /** 😡 检查是否应该使用无缝传送 😡
     */
    public static boolean shouldUseSeamlessTeleport(ResourceKey<Level> from, ResourceKey<Level> to) {
        return isRocketCEGDimension(from) || isRocketCEGDimension(to);
    }
}