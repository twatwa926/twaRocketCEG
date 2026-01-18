package com.example.rocketceg.dimension;

import com.example.rocketceg.RocketCEGMod;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.joml.Quaterniond;
import org.joml.Quaterniondc;
import org.joml.Vector3d;
import org.joml.Vector3dc;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/** 😡 传送处理器 - 100% 模仿 Starlance 的 TeleportationHandler * * 最终解决方案：使用反射直接调用 Minecraft 内部方法，但跳过 respawn 包 * 这应该能实现真正的无缝传送 😡
     */
public class TeleportationHandler {

    private static final Logger LOGGER = LogManager.getLogger(RocketCEGMod.MOD_ID);

    // 😡 与 Starlance 相同的常量 😡
    private static final double ENTITY_COLLECT_RANGE = 8;
    
    // 😡 与 Starlance 相同的字段 😡
    private final Map<Entity, Vec3> entityToPos = new HashMap<>();
    private ServerLevel oldLevel;
    private ServerLevel newLevel;
    private final boolean isReturning;

    // 😡 反射字段和方法缓存 😡
    private static Field isChangingDimensionField;
    private static Method setLevelMethod;

    static {
        try {
            // 😡 获取 ServerPlayer 的 isChangingDimension 字段 😡
            isChangingDimensionField = ServerPlayer.class.getDeclaredField("isChangingDimension");
            isChangingDimensionField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            LOGGER.warn("[RocketCEG] 无法找到 isChangingDimension 字段: {}", e.getMessage());
        }

        try {
            // 😡 获取 Entity 的 setLevel 方法 😡
            setLevelMethod = Entity.class.getDeclaredMethod("setLevel", net.minecraft.world.level.Level.class);
            setLevelMethod.setAccessible(true);
        } catch (NoSuchMethodException e) {
            LOGGER.warn("[RocketCEG] 无法找到 setLevel 方法: {}", e.getMessage());
        }
    }

    /** 😡 构造函数 - 与 Starlance 完全相同的签名 😡
     */
    public TeleportationHandler(final ServerLevel oldLevel, final ServerLevel newLevel, final boolean isReturning) {
        this.oldLevel = oldLevel;
        this.newLevel = newLevel;
        this.isReturning = isReturning;
    }

    /** 😡 重置处理器 - 与 Starlance 相同 😡
     */
    public void reset(final ServerLevel oldLevel, final ServerLevel newLevel) {
        this.oldLevel = oldLevel;
        this.newLevel = newLevel;
        this.entityToPos.clear();
    }

    /** 😡 收集实体 - 与 Starlance 的 collectEntity 方法相同 😡
     */
    private void collectEntity(final Entity entity, final Vector3dc origin, final Vector3dc newPos, final Quaterniondc rotation) {
        final Entity root = entity.getRootVehicle();
        if (this.entityToPos.containsKey(root)) {
            return;
        }
        Vec3 pos = root.position();
        
        // 😡 计算相对位置并应用旋转 - 与 Starlance 相同的逻辑 😡
        final Vector3d relPos = new Vector3d(pos.x, pos.y, pos.z).sub(origin);
        rotation.transform(relPos);
        relPos.add(newPos);
        pos = new Vec3(relPos.x, relPos.y, relPos.z);
        
        this.entityToPos.put(root, pos);
    }

    /** 😡 收集附近实体 - 简化版的 Starlance collectEntities 😡
     */
    public void collectNearbyEntities(final Vector3dc origin, final Vector3dc newPos, final Quaterniondc rotation, final Vec3 centerPos) {
        // 😡 创建收集范围 - 与 Starlance 相同的逻辑 😡
        final AABB inflatedBox = new AABB(
            centerPos.x - ENTITY_COLLECT_RANGE, centerPos.y - ENTITY_COLLECT_RANGE, centerPos.z - ENTITY_COLLECT_RANGE,
            centerPos.x + ENTITY_COLLECT_RANGE, centerPos.y + ENTITY_COLLECT_RANGE, centerPos.z + ENTITY_COLLECT_RANGE
        );
        
        // 😡 收集范围内的实体 - 与 Starlance 相同 😡
        for (final Entity entity : this.oldLevel.getEntities(
            ((Entity)(null)),
            inflatedBox,
            (entity) -> !this.entityToPos.containsKey(entity)
        )) {
            this.collectEntity(entity, origin, newPos, rotation);
        }
    }

    /** 😡 传送实体 - 与 Starlance 完全相同的方法名和逻辑 😡
     */
    private void teleportEntities() {
        this.entityToPos.forEach((entity, newPos) -> {
            // 😡 使用最终的无缝传送实现 😡
            teleportEntitySeamlessly(entity, this.newLevel, newPos);
        });
        this.entityToPos.clear();
    }

    /** 😡 完成传送 - 与 Starlance 完全相同的方法名 😡
     */
    public void finalizeTeleport() {
        this.teleportEntities();
    }

    /** 😡 最终的无缝传送实现 * * 核心思路：完全模仿 Minecraft 的 changeDimension，但使用特殊的 respawn 包标志 😡
     */
    private void teleportEntitySeamlessly(Entity entity, ServerLevel targetLevel, Vec3 targetPos) {
        if (entity instanceof ServerPlayer player) {
            teleportPlayerSeamlessly(player, targetLevel, targetPos);
        } else {
            // 😡 非玩家实体使用标准传送 😡
            if (entity.level().dimension().equals(targetLevel.dimension())) {
                entity.teleportTo(targetPos.x, targetPos.y, targetPos.z);
            } else {
                Entity newEntity = entity.changeDimension(targetLevel);
                if (newEntity != null) {
                    newEntity.teleportTo(targetPos.x, targetPos.y, targetPos.z);
                }
            }
        }
    }

    /** 😡 最终的无缝玩家传送实现 * * 基于对 Minecraft 源代码的深入分析，使用正确的 respawn 包标志 😡
     */
    private void teleportPlayerSeamlessly(ServerPlayer player, ServerLevel destLevel, Vec3 targetPos) {
        ServerLevel sourceLevel = player.serverLevel();
        
        // 😡 同维度直接移动 😡
        if (sourceLevel.dimension().equals(destLevel.dimension())) {
            player.teleportTo(targetPos.x, targetPos.y, targetPos.z);
            return;
        }

        LOGGER.info("[RocketCEG] 最终无缝传送玩家 {} 从 {} 到 {} ({})",
                player.getName().getString(),
                sourceLevel.dimension().location(),
                destLevel.dimension().location(),
                targetPos);

        try {
            // 😡 保存当前状态 😡
            float yRot = player.getYRot();
            float xRot = player.getXRot();

            // 😡 设置 isChangingDimension 标志 😡
            if (isChangingDimensionField != null) {
                isChangingDimensionField.setBoolean(player, true);
            }

            // 😡 从源世界移除玩家 😡
            sourceLevel.removePlayerImmediately(player, Entity.RemovalReason.CHANGED_DIMENSION);

            // 😡 重置玩家状态 😡
            player.revive();

            // 😡 设置新维度（使用反射） 😡
            if (setLevelMethod != null) {
                setLevelMethod.invoke(player, destLevel);
            }

            // 😡 设置新位置 😡
            player.moveTo(targetPos.x, targetPos.y, targetPos.z, yRot, xRot);

            // 😡 === 关键：发送特殊的 respawn 包 === 😡
            // 😡 使用 KEEP_ALL_DATA | KEEP_ENTITY_DATA | KEEP_ATTRIBUTES = 0x07 😡
            // 😡 这个组合应该能最大程度避免加载屏幕 😡
            player.connection.send(new ClientboundRespawnPacket(
                    destLevel.dimensionTypeId(),
                    destLevel.dimension(),
                    BiomeManager.obfuscateSeed(destLevel.getSeed()),
                    player.gameMode.getGameModeForPlayer(),
                    player.gameMode.getPreviousGameModeForPlayer(),
                    destLevel.isDebug(),
                    destLevel.isFlat(),
                    (byte) 0x07,  // 😡 尝试所有保留标志 😡
                    player.getLastDeathLocation(),
                    player.getPortalCooldown()
            ));

            // 😡 发送玩家能力 😡
            player.connection.send(new ClientboundPlayerAbilitiesPacket(player.getAbilities()));

            // 😡 更新世界信息 😡
            PlayerList playerList = player.server.getPlayerList();
            playerList.sendLevelInfo(player, destLevel);
            playerList.sendAllPlayerInfo(player);

            // 😡 将玩家添加到目标世界 😡
            destLevel.addDuringTeleport(player);

            // 😡 发送位置同步包 😡
            player.connection.send(new ClientboundPlayerPositionPacket(
                    targetPos.x, targetPos.y, targetPos.z,
                    yRot, xRot,
                    java.util.Collections.emptySet(),
                    0
            ));

            // 😡 重置传送状态 😡
            if (isChangingDimensionField != null) {
                isChangingDimensionField.setBoolean(player, false);
            }

            // 😡 触发 Forge 事件 😡
            MinecraftForge.EVENT_BUS.post(
                    new PlayerEvent.PlayerChangedDimensionEvent(
                            player, sourceLevel.dimension(), destLevel.dimension()
                    )
            );

            LOGGER.info("[RocketCEG] 最终无缝传送完成: {} 现在位于 {} ({})",
                    player.getName().getString(),
                    destLevel.dimension().location(),
                    targetPos);

        } catch (Exception e) {
            LOGGER.error("[RocketCEG] 最终无缝传送失败", e);
            // 😡 重置状态 😡
            try {
                if (isChangingDimensionField != null) {
                    isChangingDimensionField.setBoolean(player, false);
                }
            } catch (Exception ignored) {}
            // 😡 回退到标准传送 😡
            player.teleportTo(destLevel, targetPos.x, targetPos.y, targetPos.z, 
                    player.getYRot(), player.getXRot());
        }
    }

    /** 😡 添加实体到传送列表 - 公共接口 😡
     */
    public void addEntity(Entity entity, Vec3 targetPos) {
        this.entityToPos.put(entity, targetPos);
    }

    /** 😡 添加实体并计算相对位置 - 与 Starlance 风格一致 😡
     */
    public void addEntityWithTransform(Entity entity, Vector3dc origin, Vector3dc newPos, Quaterniondc rotation) {
        this.collectEntity(entity, origin, newPos, rotation);
    }
}