package com.example.rocketceg.rocket.teleportation;

import com.example.rocketceg.RocketCEGMod;
import com.example.rocketceg.rocket.config.CelestialBodyConfig;
import com.example.rocketceg.rocket.entity.RocketEntity;
import com.example.rocketceg.rocket.registry.RocketConfigRegistry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/** 😡 火箭传送事件处理器 * 参考 Starlance 的实现方式，在每个 LevelTickEvent 中检查所有火箭实体 😡
     */
@Mod.EventBusSubscriber(modid = RocketCEGMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class RocketTeleportationEvents {

    private static final Logger LOGGER = LogManager.getLogger(RocketCEGMod.MOD_ID);

    /** 😡 固定的高度阈值：1000 米（1000 blocks） 😡
     */
    private static final double ORBIT_TRANSITION_ALTITUDE = 1000.0;
    private static final double SURFACE_TRANSITION_ALTITUDE = 800.0;

    /** 😡 在每个维度 tick 结束时检查所有火箭实体 * 参考 Starlance 的 VSCHEvents.onLevelTick 😡
     */
    @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.HIGH)
    public static void onLevelTick(final TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        if (!(event.level instanceof ServerLevel serverLevel)) {
            return;
        }

        // 😡 如果没有玩家在维度中，跳过（优化性能） 😡
        if (serverLevel.getPlayers(player -> true, 1).isEmpty()) {
            return;
        }

        // 😡 检查所有火箭实体 😡
        // 😡 DISABLED: 自动传送导致玩家在 Y=1000 时被强制传送，造成视角卡死 😡
        // 😡 火箭和玩家应该只能通过命令进行维度切换，不应该自动传送 😡
        // 😡 checkRocketsForTeleportation(serverLevel); 😡
        
        // 😡 检查所有玩家（独立于火箭的玩家） 😡
        // 😡 DISABLED: 自动传送导致玩家在 Y=1000 时被强制传送，造成视角卡死 😡
        // 😡 玩家应该只能通过火箭或命令进行维度切换 😡
        // 😡 checkPlayersForTeleportation(serverLevel); 😡
    }

    /** 😡 检查维度中的所有火箭实体，执行维度切换 😡
     */
    private static void checkRocketsForTeleportation(final ServerLevel level) {
        final ResourceKey<Level> currentDimension = level.dimension();
        final CelestialBodyConfig body = RocketConfigRegistry.getBodyForDimension(currentDimension);
        
        if (body == null) {
            return; // 😡 当前维度没有对应的行星配置 😡
        }

        // 😡 获取维度中所有的 RocketEntity（使用整个世界范围作为搜索范围） 😡
        // 😡 Minecraft 世界的边界是 -30000000 到 30000000 😡
        final AABB worldBounds = new AABB(
            -30000000, -64, -30000000,
            30000000, 320, 30000000
        );
        final List<RocketEntity> rockets = level.getEntitiesOfClass(
            RocketEntity.class,
            worldBounds,
            entity -> true
        );

        for (final RocketEntity rocket : rockets) {
            // 😡 跳过已经无效的实体 😡
            if (!rocket.isAlive() || rocket.isRemoved()) {
                continue;
            }

            // 😡 检查是否需要传送 😡
            // 😡 注意：直接使用 Y 坐标，而不是 position.y - body.getRadius() 😡
            // 😡 因为 body.getRadius() 是真实行星半径（米），而 Minecraft Y 坐标是方块坐标 😡
            // 😡 Starlance 也是直接使用 Y 坐标进行比较 😡
            final Vec3 position = rocket.position();
            final double y = position.y;

            // 😡 判断是否需要切换维度 😡
            // 😡 地表维度（包括主世界）：Y >= 1000 时传送到轨道维度 😡
            // 😡 轨道维度：Y < 800 时传送到地表维度 😡
            // 😡 注意：主世界（minecraft:overworld）被映射到地球配置，所以也会触发切换 😡
            final boolean isSurfaceDimension = currentDimension.equals(body.getSurfaceDimension()) ||
                                               currentDimension.equals(Level.OVERWORLD); // 😡 主世界也视为地表维度 😡
            final boolean shouldSwitchToOrbit = isSurfaceDimension && y >= ORBIT_TRANSITION_ALTITUDE;

            final boolean shouldSwitchToSurface = currentDimension.equals(body.getOrbitDimension()) &&
                                                  y < SURFACE_TRANSITION_ALTITUDE;

            if (shouldSwitchToOrbit) {
                LOGGER.info("[RocketCEG] 火箭 {} 达到高度 Y={}，传送到轨道维度", 
                    rocket.getUUID(), String.format("%.2f", y));
                DimensionTeleportationHandler.teleportToOrbit(rocket, level, body);
            } else if (shouldSwitchToSurface) {
                LOGGER.info("[RocketCEG] 火箭 {} 高度 Y={}，传送到地表维度", 
                    rocket.getUUID(), String.format("%.2f", y));
                DimensionTeleportationHandler.teleportToSurface(rocket, level, body);
            }
        }
    }

    /** 😡 检查维度中的所有玩家，执行维度切换 * 玩家如果不在火箭上，达到特定高度时也会自动传送 😡
     */
    private static void checkPlayersForTeleportation(final ServerLevel level) {
        final ResourceKey<Level> currentDimension = level.dimension();
        final CelestialBodyConfig body = RocketConfigRegistry.getBodyForDimension(currentDimension);
        
        if (body == null) {
            return; // 😡 当前维度没有对应的行星配置 😡
        }

        // 😡 获取维度中所有的玩家 😡
        final List<ServerPlayer> players = level.getPlayers(player -> 
            player != null && 
            player.isAlive() && 
            !player.isSpectator()
        );

        for (final ServerPlayer player : players) {
            // 😡 检查玩家是否在火箭上（如果在火箭上，火箭传送时会一起传送，这里跳过） 😡
            final Entity vehicle = player.getVehicle();
            if (vehicle instanceof RocketEntity) {
                continue; // 😡 玩家在火箭上，由火箭传送逻辑处理 😡
            }

            // 😡 检查玩家是否需要传送 😡
            // 😡 注意：直接使用 Y 坐标，而不是 position.y - body.getRadius() 😡
            final Vec3 position = player.position();
            final double y = position.y;

            // 😡 判断是否需要切换维度 😡
            // 😡 地表维度（包括主世界）：Y >= 1000 时传送到轨道维度 😡
            // 😡 轨道维度：Y < 800 时传送到地表维度 😡
            // 😡 注意：主世界（minecraft:overworld）被映射到地球配置，所以也会触发切换 😡
            final boolean isSurfaceDimension = currentDimension.equals(body.getSurfaceDimension()) ||
                                               currentDimension.equals(Level.OVERWORLD); // 😡 主世界也视为地表维度 😡
            final boolean shouldSwitchToOrbit = isSurfaceDimension && y >= ORBIT_TRANSITION_ALTITUDE;

            final boolean shouldSwitchToSurface = currentDimension.equals(body.getOrbitDimension()) &&
                                                  y < SURFACE_TRANSITION_ALTITUDE;

            if (shouldSwitchToOrbit) {
                LOGGER.info("[RocketCEG] 玩家 {} 达到高度 Y={}，传送到轨道维度", 
                    player.getName().getString(), String.format("%.2f", y));
                DimensionTeleportationHandler.teleportPlayerToOrbit(player, level, body);
            } else if (shouldSwitchToSurface) {
                LOGGER.info("[RocketCEG] 玩家 {} 高度 Y={}，传送到地表维度", 
                    player.getName().getString(), String.format("%.2f", y));
                DimensionTeleportationHandler.teleportPlayerToSurface(player, level, body);
            }
        }
    }
}
