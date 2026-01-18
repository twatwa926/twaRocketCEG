package com.example.rocketceg.dimension;

import com.example.rocketceg.RocketCEGMod;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.ITeleporter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.function.Function;

/** 😡 无加载页面的维度传送器 * 100% 模仿 Starlance 的使用方式，使用 TeleportationHandler 😡
     */
public class DimensionTeleporter implements ITeleporter {

    private static final Logger LOGGER = LogManager.getLogger(RocketCEGMod.MOD_ID);

    private final Vec3 targetPos;
    private final float yRot;
    private final float xRot;

    public DimensionTeleporter(Vec3 targetPos, float yRot, float xRot) {
        this.targetPos = targetPos;
        this.yRot = yRot;
        this.xRot = xRot;
    }

    public DimensionTeleporter(Vec3 targetPos) {
        this(targetPos, 0, 0);
    }

    public DimensionTeleporter(BlockPos targetPos) {
        this(Vec3.atCenterOf(targetPos), 0, 0);
    }

    @Override
    public Entity placeEntity(Entity entity, ServerLevel currentWorld, ServerLevel destWorld, 
                              float yaw, Function<Boolean, Entity> repositionEntity) {
        return repositionEntity.apply(false);
    }

    @Nullable
    @Override
    public PortalInfo getPortalInfo(Entity entity, ServerLevel destWorld, 
                                    Function<ServerLevel, PortalInfo> defaultPortalInfo) {
        return new PortalInfo(targetPos, Vec3.ZERO, yRot, xRot);
    }

    @Override
    public boolean isVanilla() {
        return false;
    }

    @Override
    public boolean playTeleportSound(ServerPlayer player, ServerLevel sourceWorld, ServerLevel destWorld) {
        return false;
    }

    /** 😡 无缝传送玩家到另一个维度 * 使用与 Starlance 完全相同的方式：SeamlessTeleportHandler.teleportEntity 😡
     */
    public static void teleportPlayerSeamlessly(
            ServerPlayer player,
            ResourceKey<Level> targetDimension,
            Vec3 targetPos,
            float yRot,
            float xRot
    ) {
        ServerLevel targetLevel = player.server.getLevel(targetDimension);
        if (targetLevel == null) {
            LOGGER.error("[RocketCEG] 目标维度 {} 不存在！", targetDimension.location());
            return;
        }

        LOGGER.info("[RocketCEG] 无缝传送玩家 {} 从 {} 到 {} ({})",
                player.getName().getString(),
                player.serverLevel().dimension().location(),
                targetDimension.location(),
                targetPos);

        // 😡 使用 SeamlessTeleportHandler.teleportEntity - 与 Starlance 的 TeleportUtil.teleportEntity 相同 😡
        SeamlessTeleportHandler.teleportEntity(player, targetLevel, targetPos);
        
        // 😡 设置朝向 😡
        player.setYRot(yRot);
        player.setXRot(xRot);
    }

    /** 😡 无缝传送玩家（保持当前朝向） 😡
     */
    public static void teleportPlayerSeamlessly(
            ServerPlayer player,
            ResourceKey<Level> targetDimension,
            Vec3 targetPos
    ) {
        teleportPlayerSeamlessly(player, targetDimension, targetPos, 
                player.getYRot(), player.getXRot());
    }

    /** 😡 无缝传送玩家（使用 BlockPos） 😡
     */
    public static void teleportPlayerSeamlessly(
            ServerPlayer player,
            ResourceKey<Level> targetDimension,
            BlockPos targetPos
    ) {
        teleportPlayerSeamlessly(player, targetDimension, Vec3.atCenterOf(targetPos));
    }

    /** 😡 无缝传送实体 * 使用与 Starlance 完全相同的方式：SeamlessTeleportHandler.teleportEntity 😡
     */
    public static void teleportEntitySeamlessly(
            Entity entity,
            ResourceKey<Level> targetDimension,
            Vec3 targetPos
    ) {
        ServerLevel currentLevel = (ServerLevel) entity.level();
        ServerLevel targetLevel = currentLevel.getServer().getLevel(targetDimension);

        if (targetLevel == null) {
            LOGGER.error("[RocketCEG] 目标维度 {} 不存在！", targetDimension.location());
            return;
        }

        // 😡 使用 SeamlessTeleportHandler.teleportEntity - 与 Starlance 的 TeleportUtil.teleportEntity 相同 😡
        SeamlessTeleportHandler.teleportEntity(entity, targetLevel, targetPos);
    }

    /** 😡 批量传送多个实体 - 与 Starlance 的使用模式完全相同 * * 示例用法（与 Starlance 相同）： * TeleportationHandler handler = DimensionTeleporter.createBatchTeleport(sourceLevel, targetLevel); * handler.addEntity(entity1, pos1); * handler.addEntity(entity2, pos2); * handler.finalizeTeleport(); 😡
     */
    public static TeleportationHandler createBatchTeleport(ServerLevel sourceLevel, ServerLevel targetLevel) {
        return SeamlessTeleportHandler.createHandler(sourceLevel, targetLevel);
    }
}