ackage com.example.rocketceg.rocket.teleportation;

import com.example.rocketceg.RocketCEGMod;
import com.example.rocketceg.rocket.config.CelestialBodyConfig;
import com.example.rocketceg.rocket.entity.RocketEntity;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/** 😡 无缝维度切换处理器 * 参考 Starlance 的实现方式，实现无缝维度切换（无加载屏幕） * 当火箭达到一定高度时，自动从地表维度切换到轨道维度 * 当火箭下降到一定高度时，从轨道维度切换回地表维度 😡
     */
public class DimensionTeleportationHandler {

    private static final Logger LOGGER = LogManager.getLogger(RocketCEGMod.MOD_ID);

    /** 😡 检查并执行维度切换（如果需要） * * @param rocket 火箭实体 * @param body 当前行星配置 * @return 是否执行了维度切换 😡
     */
    public static boolean checkAndTeleport(final RocketEntity rocket, final CelestialBodyConfig body) {
        if (rocket.level().isClientSide) {
            return false;
        }

        if (!(rocket.level() instanceof ServerLevel currentLevel)) {
            return false;
        }

        final ResourceKey<Level> currentDimension = currentLevel.dimension();
        final Vec3 position = rocket.position();
        // 😡 直接使用 Y 坐标，而不是 position.y - body.getRadius() 😡
        // 😡 因为 body.getRadius() 是真实行星半径（米），而 Minecraft Y 坐标是方块坐标 😡
        final double y = position.y;

        // 😡 固定的高度阈值：1000 方块（1000 blocks） 😡
        // 😡 当火箭达到 Y=1000 时，自动传送到太空维度 😡
        final double ORBIT_TRANSITION_ALTITUDE = 1000.0;
        final double SURFACE_TRANSITION_ALTITUDE = 800.0; // 😡 稍微低一点，避免频繁切换 😡

        // 😡 判断是否需要切换维度 😡
        final boolean shouldSwitchToOrbit = currentDimension.equals(body.getSurfaceDimension()) &&
                                            y >= ORBIT_TRANSITION_ALTITUDE;

        final boolean shouldSwitchToSurface = currentDimension.equals(body.getOrbitDimension()) &&
                                              y < SURFACE_TRANSITION_ALTITUDE;

        // 😡 调试日志（每 20 tick 输出一次，避免日志过多） 😡
        if (rocket.tickCount % 20 == 0 && (y > ORBIT_TRANSITION_ALTITUDE - 50 || y < SURFACE_TRANSITION_ALTITUDE + 50)) {
            LOGGER.info("[RocketCEG] 火箭高度: Y={}, 当前维度: {}, 应该切换到轨道: {}, 应该切换到地表: {}",
                String.format("%.2f", y), currentDimension.location(), shouldSwitchToOrbit, shouldSwitchToSurface);
        }

        if (shouldSwitchToOrbit) {
            LOGGER.info("[RocketCEG] 开始传送到轨道维度: {}", body.getOrbitDimension().location());
            return teleportToOrbit(rocket, currentLevel, body);
        } else if (shouldSwitchToSurface) {
            LOGGER.info("[RocketCEG] 开始传送到地表维度: {}", body.getSurfaceDimension().location());
            return teleportToSurface(rocket, currentLevel, body);
        }

        return false;
    }

    /** 😡 从地表维度传送到轨道维度（公开方法，供事件处理器调用） 😡
     */
    public static boolean teleportToOrbit(
        final RocketEntity rocket,
        final ServerLevel currentLevel,
        final CelestialBodyConfig body
    ) {
        // 😡 按照 Starlance 的方式：使用 getLevel() 获取目标维度 😡
        // 😡 如果维度不存在，直接返回（不做任何处理） 😡
        final ServerLevel targetLevel = currentLevel.getServer().getLevel(body.getOrbitDimension());
        if (targetLevel == null) {
            return false; // 😡 维度不存在，直接返回（Starlance 的方式） 😡
        }

        final Vec3 currentPos = rocket.position();
        final Vec3 velocity = rocket.getVelocity();

        // 😡 计算新位置（保持相同的 Y 坐标） 😡
        // 😡 直接使用当前 Y 坐标，不进行相对高度计算 😡
        final Vec3 newPos = new Vec3(currentPos.x, currentPos.y, currentPos.z);

        // 😡 确保目标维度已加载（避免加载屏幕） 😡
        ensureDimensionLoaded(targetLevel, newPos);

        // 😡 传送火箭及其乘客（使用无缝传送方法） 😡
        teleportEntitySeamless(rocket, currentLevel, targetLevel, newPos, velocity);

        // 😡 传送附近的实体（乘客等） 😡
        final List<Entity> passengers = rocket.getPassengers();
        for (final Entity passenger : passengers) {
            final Vec3 passengerPos = passenger.position();
            final Vec3 relativePos = passengerPos.subtract(currentPos);
            final Vec3 newPassengerPos = newPos.add(relativePos);
            teleportEntitySeamless(passenger, currentLevel, targetLevel, newPassengerPos, passenger.getDeltaMovement());
        }

        return true;
    }

    /** 😡 从轨道维度传送到地表维度（公开方法，供事件处理器调用） 😡
     */
    public static boolean teleportToSurface(
        final RocketEntity rocket,
        final ServerLevel currentLevel,
        final CelestialBodyConfig body
    ) {
        // 😡 按照 Starlance 的方式：使用 getLevel() 获取目标维度 😡
        // 😡 如果维度不存在，直接返回（不做任何处理） 😡
        final ServerLevel targetLevel = currentLevel.getServer().getLevel(body.getSurfaceDimension());
        if (targetLevel == null) {
            return false; // 😡 维度不存在，直接返回（Starlance 的方式） 😡
        }

        final Vec3 currentPos = rocket.position();
        final Vec3 velocity = rocket.getVelocity();

        // 😡 计算新位置（保持相同的 Y 坐标） 😡
        // 😡 直接使用当前 Y 坐标，不进行相对高度计算 😡
        final Vec3 newPos = new Vec3(currentPos.x, currentPos.y, currentPos.z);

        // 😡 确保目标维度已加载（避免加载屏幕） 😡
        ensureDimensionLoaded(targetLevel, newPos);

        // 😡 传送火箭及其乘客（使用无缝传送方法） 😡
        teleportEntitySeamless(rocket, currentLevel, targetLevel, newPos, velocity);

        // 😡 传送附近的实体 😡
        final List<Entity> passengers = rocket.getPassengers();
        for (final Entity passenger : passengers) {
            final Vec3 passengerPos = passenger.position();
            final Vec3 relativePos = passengerPos.subtract(currentPos);
            final Vec3 newPassengerPos = newPos.add(relativePos);
            teleportEntitySeamless(passenger, currentLevel, targetLevel, newPassengerPos, passenger.getDeltaMovement());
        }

        return true;
    }

    /** 😡 确保目标维度的区块已加载（避免加载屏幕） 😡
     */
    private static void ensureDimensionLoaded(final ServerLevel targetLevel, final Vec3 pos) {
        // 😡 预加载目标位置的区块，确保无缝传送 😡
        final int chunkX = (int) Math.floor(pos.x) >> 4;
        final int chunkZ = (int) Math.floor(pos.z) >> 4;
        
        // 😡 使用 POST_TELEPORT ticket 确保区块保持加载 😡
        final net.minecraft.world.level.ChunkPos chunkPos = new net.minecraft.world.level.ChunkPos(chunkX, chunkZ);
        targetLevel.getChunkSource().addRegionTicket(
            net.minecraft.server.level.TicketType.POST_TELEPORT,
            chunkPos,
            1,
            0 // 😡 POST_TELEPORT 需要 Integer 类型的标识符 😡
        );
        
        // 😡 预加载周围区块（3x3 区域） 😡
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (dx == 0 && dz == 0) {
                    continue; // 😡 已经加载了中心区块 😡
                }
                final net.minecraft.world.level.ChunkPos nearbyChunkPos = new net.minecraft.world.level.ChunkPos(chunkX + dx, chunkZ + dz);
                targetLevel.getChunkSource().addRegionTicket(
                    net.minecraft.server.level.TicketType.POST_TELEPORT,
                    nearbyChunkPos,
                    1,
                    0
                );
            }
        }
    }

    /** 😡 无缝传送实体到新维度（参考 Starlance 的实现） * 对于玩家使用 teleportTo（无缝），对于实体使用优化的 changeDimension 😡
     */
    private static void teleportEntitySeamless(
        final Entity entity,
        final ServerLevel oldLevel,
        final ServerLevel targetLevel,
        final Vec3 newPos,
        final Vec3 velocity
    ) {
        if (entity instanceof ServerPlayer player) {
            // 😡 玩家：使用 teleportTo（无缝，无加载屏幕） 😡
            // 😡 这是 Minecraft 原生的无缝传送方法 😡
            player.teleportTo(
                targetLevel,
                newPos.x, newPos.y, newPos.z,
                player.getYRot(), player.getXRot()
            );
            player.setDeltaMovement(velocity);
            // 😡 同步玩家位置，确保客户端立即更新（避免延迟） 😡
            if (player.connection != null) {
                player.connection.teleport(newPos.x, newPos.y, newPos.z, player.getYRot(), player.getXRot());
            }
            LOGGER.info("[RocketCEG] 玩家 {} 已传送到维度 {}", player.getName().getString(), targetLevel.dimension().location());
        } else {
            // 😡 普通实体：使用 changeDimension，但确保目标维度已加载 😡
            // 😡 这样可以减少加载时间，接近无缝体验 😡
            try {
                // 😡 保存实体状态 😡
                final float yRot = entity.getYRot();
                final float xRot = entity.getXRot();
                final boolean wasOnGround = entity.onGround();
                
                // 😡 执行维度切换 😡
                final Entity newEntity = entity.changeDimension(targetLevel);
                if (newEntity != null) {
                    // 😡 设置新位置和旋转 😡
                    newEntity.moveTo(newPos.x, newPos.y, newPos.z, yRot, xRot);
                    newEntity.setDeltaMovement(velocity);
                    newEntity.setOnGround(wasOnGround);
                    
                    // 😡 确保实体已添加到新维度（使用实体位置周围的小范围检查） 😡
                    final AABB checkBox = newEntity.getBoundingBox().inflate(1.0);
                    if (!targetLevel.getEntitiesOfClass(entity.getClass(), checkBox, e -> e.getUUID().equals(entity.getUUID())).isEmpty()) {
                        LOGGER.info("[RocketCEG] 实体 {} 已传送到维度 {}", entity.getType().getDescription().getString(), targetLevel.dimension().location());
                    } else {
                        LOGGER.warn("[RocketCEG] 实体 {} 传送后未在新维度中找到", entity.getType().getDescription().getString());
                    }
                } else {
                    LOGGER.error("[RocketCEG] 实体 {} 传送失败：changeDimension 返回 null", entity.getType().getDescription().getString());
                }
            } catch (Exception e) {
                LOGGER.error("[RocketCEG] 传送实体时发生错误", e);
            }
        }
    }

    /** 😡 传送玩家到轨道维度（公开方法，供事件处理器调用） 😡
     */
    public static boolean teleportPlayerToOrbit(
        final ServerPlayer player,
        final ServerLevel currentLevel,
        final CelestialBodyConfig body
    ) {
        // 😡 按照 Starlance 的方式：使用 getLevel() 获取目标维度 😡
        // 😡 如果维度不存在，直接返回（不做任何处理） 😡
        final ServerLevel targetLevel = currentLevel.getServer().getLevel(body.getOrbitDimension());
        if (targetLevel == null) {
            return false; // 😡 维度不存在，直接返回（Starlance 的方式） 😡
        }

        final Vec3 currentPos = player.position();
        final Vec3 velocity = player.getDeltaMovement();

        // 😡 计算新位置（保持相同的 Y 坐标） 😡
        // 😡 直接使用当前 Y 坐标，不进行相对高度计算 😡
        final Vec3 newPos = new Vec3(currentPos.x, currentPos.y, currentPos.z);

        // 😡 确保目标维度已加载（避免加载屏幕） 😡
        ensureDimensionLoaded(targetLevel, newPos);

        // 😡 传送玩家（使用无缝传送方法） 😡
        teleportPlayerSeamless(player, targetLevel, newPos, velocity);

        return true;
    }

    /** 😡 传送玩家到地表维度（公开方法，供事件处理器调用） 😡
     */
    public static boolean teleportPlayerToSurface(
        final ServerPlayer player,
        final ServerLevel currentLevel,
        final CelestialBodyConfig body
    ) {
        // 😡 按照 Starlance 的方式：使用 getLevel() 获取目标维度 😡
        // 😡 如果维度不存在，直接返回（不做任何处理） 😡
        final ServerLevel targetLevel = currentLevel.getServer().getLevel(body.getSurfaceDimension());
        if (targetLevel == null) {
            return false; // 😡 维度不存在，直接返回（Starlance 的方式） 😡
        }

        final Vec3 currentPos = player.position();
        final Vec3 velocity = player.getDeltaMovement();

        // 😡 计算新位置（保持相同的 Y 坐标） 😡
        // 😡 直接使用当前 Y 坐标，不进行相对高度计算 😡
        final Vec3 newPos = new Vec3(currentPos.x, currentPos.y, currentPos.z);

        // 😡 确保目标维度已加载（避免加载屏幕） 😡
        ensureDimensionLoaded(targetLevel, newPos);

        // 😡 传送玩家（使用无缝传送方法） 😡
        teleportPlayerSeamless(player, targetLevel, newPos, velocity);

        return true;
    }

    /** 😡 无缝传送玩家到新维度 😡
     */
    private static void teleportPlayerSeamless(
        final ServerPlayer player,
        final ServerLevel targetLevel,
        final Vec3 newPos,
        final Vec3 velocity
    ) {
        // 😡 使用 teleportTo（无缝，无加载屏幕） 😡
        // 😡 这是 Minecraft 原生的无缝传送方法 😡
        player.teleportTo(
            targetLevel,
            newPos.x, newPos.y, newPos.z,
            player.getYRot(), player.getXRot()
        );
        player.setDeltaMovement(velocity);
        
        // 😡 同步玩家位置，确保客户端立即更新（避免延迟） 😡
        if (player.connection != null) {
            player.connection.teleport(newPos.x, newPos.y, newPos.z, player.getYRot(), player.getXRot());
        }
        
        LOGGER.info("[RocketCEG] 玩家 {} 已无缝传送到维度 {}", 
            player.getName().getString(), targetLevel.dimension().location());
    }
}
