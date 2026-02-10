package com.example.examplemod.rocket;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.rocket.ship.RocketShipEntity;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public final class RocketDimensionTransition {
    private static final Logger LOGGER = LogManager.getLogger("RocketDimTransition");

    private static volatile double ORBIT_ALTITUDE_THRESHOLD = 400.0;

    private static volatile double SPACE_ALTITUDE_THRESHOLD = 600.0;

    private static volatile double LAND_ALTITUDE_THRESHOLD = 380.0;

    private static volatile double PLANET_ENTRY_ALTITUDE = 350.0;

    public static double getOrbitThreshold() { return ORBIT_ALTITUDE_THRESHOLD; }
    public static void setOrbitThreshold(double v) { ORBIT_ALTITUDE_THRESHOLD = v; }

    public static double getSpaceThreshold() { return SPACE_ALTITUDE_THRESHOLD; }
    public static void setSpaceThreshold(double v) { SPACE_ALTITUDE_THRESHOLD = v; }

    public static double getLandThreshold() { return LAND_ALTITUDE_THRESHOLD; }
    public static void setLandThreshold(double v) { LAND_ALTITUDE_THRESHOLD = v; }

    public static double getPlanetEntryAltitude() { return PLANET_ENTRY_ALTITUDE; }
    public static void setPlanetEntryAltitude(double v) { PLANET_ENTRY_ALTITUDE = v; }

    private static final double ENTITY_COLLECT_RANGE = 16.0;

    private static final Map<String, ResourceKey<Level>> PLANET_DIMENSIONS = new HashMap<>();

    static {
        PLANET_DIMENSIONS.put("earth", RocketDimensions.EARTH);
        PLANET_DIMENSIONS.put("overworld", Level.OVERWORLD);
        PLANET_DIMENSIONS.put("earth_orbit", RocketDimensions.EARTH_ORBIT);
        PLANET_DIMENSIONS.put("moon", RocketDimensions.MOON);
        PLANET_DIMENSIONS.put("mars", RocketDimensions.MARS);
        PLANET_DIMENSIONS.put("venus", RocketDimensions.VENUS);
        PLANET_DIMENSIONS.put("mercury", RocketDimensions.MERCURY);
        PLANET_DIMENSIONS.put("jupiter", RocketDimensions.JUPITER);
        PLANET_DIMENSIONS.put("saturn", RocketDimensions.SATURN);
        PLANET_DIMENSIONS.put("uranus", RocketDimensions.URANUS);
        PLANET_DIMENSIONS.put("neptune", RocketDimensions.NEPTUNE);
        PLANET_DIMENSIONS.put("pluto", RocketDimensions.PLUTO);
        PLANET_DIMENSIONS.put("ceres", RocketDimensions.CERES);
        PLANET_DIMENSIONS.put("sun", RocketDimensions.SUN);
    }

    private RocketDimensionTransition() {}

    public static Set<String> getAvailablePlanets() {
        return PLANET_DIMENSIONS.keySet();
    }

    public static ResourceKey<Level> getPlanetDimension(String name) {
        return PLANET_DIMENSIONS.get(name.toLowerCase());
    }

    public static boolean isAboveOrbitThreshold(Entity entity) {
        return entity.getY() >= ORBIT_ALTITUDE_THRESHOLD;
    }

    private static void ensureChunksLoaded(ServerLevel targetLevel, Vec3 pos) {
        int chunkX = ((int) Math.floor(pos.x)) >> 4;
        int chunkZ = ((int) Math.floor(pos.z)) >> 4;

        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                ChunkPos cp = new ChunkPos(chunkX + dx, chunkZ + dz);
                targetLevel.getChunkSource().addRegionTicket(TicketType.POST_TELEPORT, cp, 1, 0);
            }
        }
    }

    private static void teleportPlayer(ServerPlayer player, ServerLevel targetLevel, Vec3 pos) {
        if (targetLevel == null || player == null) return;
        ensureChunksLoaded(targetLevel, pos);
        Vec3 vel = player.getDeltaMovement();
        float yaw = player.getYRot();
        float pitch = player.getXRot();
        player.teleportTo(targetLevel, pos.x, pos.y, pos.z,
                Set.of(), yaw, pitch);
        player.setDeltaMovement(vel);

        if (player.connection != null) {
            player.connection.teleport(pos.x, pos.y, pos.z, yaw, pitch);
        }
        LOGGER.info("[DimTransit] 玩家 {} 传送到 {} ({})",
                player.getName().getString(), targetLevel.dimension().location(), pos);
    }

    private static void teleportShip(RocketShipEntity ship, ServerLevel targetLevel, Vec3 pos) {
        if (targetLevel == null || ship == null) return;
        ensureChunksLoaded(targetLevel, pos);
        float yRot = ship.getYRot();
        float xRot = ship.getXRot();
        ship.teleportTo(targetLevel, pos.x, pos.y, pos.z,
                Set.of(RelativeMovement.X, RelativeMovement.Y, RelativeMovement.Z,
                        RelativeMovement.Y_ROT, RelativeMovement.X_ROT),
                yRot, xRot);
        LOGGER.info("[DimTransit] 火箭传送到 {} ({})",
                targetLevel.dimension().location(), pos);
    }

    private static void teleportNearbyEntities(ServerLevel sourceLevel, ServerLevel targetLevel,
                                                Vec3 shipPos, Vec3 newPos, Entity excludeRocket) {
        if (sourceLevel == null || targetLevel == null) return;
        AABB collectBox = new AABB(
                shipPos.x - ENTITY_COLLECT_RANGE, shipPos.y - ENTITY_COLLECT_RANGE, shipPos.z - ENTITY_COLLECT_RANGE,
                shipPos.x + ENTITY_COLLECT_RANGE, shipPos.y + ENTITY_COLLECT_RANGE, shipPos.z + ENTITY_COLLECT_RANGE
        );
        List<Entity> nearby = sourceLevel.getEntities((Entity) null, collectBox,
                e -> e != excludeRocket && !(e instanceof ServerPlayer) && !e.isRemoved());
        Vec3 offset = newPos.subtract(shipPos);
        for (Entity entity : nearby) {
            Vec3 relPos = entity.position().subtract(shipPos);
            Vec3 newEntityPos = newPos.add(relPos);
            Vec3 vel = entity.getDeltaMovement();
            float yRot = entity.getYRot();
            float xRot = entity.getXRot();
            Entity newEntity = entity.changeDimension(targetLevel);
            if (newEntity != null) {
                newEntity.moveTo(newEntityPos.x, newEntityPos.y, newEntityPos.z, yRot, xRot);
                newEntity.setDeltaMovement(vel);
            }
        }
    }

    public static void checkAllTransitions(ServerPlayer player, RocketShipEntity ship, ServerLevel currentLevel) {
        if (player == null || ship == null || currentLevel == null) return;

        var dim = currentLevel.dimension();

        if ((dim.equals(RocketDimensions.EARTH) || dim.equals(Level.OVERWORLD))
                && ship.getY() >= ORBIT_ALTITUDE_THRESHOLD) {
            ServerLevel orbitLevel = RocketDimensions.getLevel(currentLevel.getServer(), RocketDimensions.EARTH_ORBIT);
            if (orbitLevel != null) {
                Vec3 shipPos = ship.position();
                Vec3 entryPos = new Vec3(ship.getX(), 100.0, ship.getZ());
                teleportNearbyEntities(currentLevel, orbitLevel, shipPos, entryPos, ship);
                teleportShip(ship, orbitLevel, entryPos);
                teleportPlayer(player, orbitLevel, entryPos);
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                        "\u00a7b\u00a7l[\u5bfc\u822a] \u00a7r\u00a77\u8fdb\u5165\u5730\u7403\u8f68\u9053..."));
                return;
            }
        }

        if (dim.equals(RocketDimensions.EARTH_ORBIT) && ship.getY() >= SPACE_ALTITUDE_THRESHOLD) {
            String dest = ship.getDestinationPlanet();
            if (dest != null && !dest.isEmpty()) {
                ResourceKey<Level> targetDim = getPlanetDimension(dest);
                if (targetDim != null && !targetDim.equals(RocketDimensions.EARTH_ORBIT)) {
                    ServerLevel targetLevel = RocketDimensions.getLevel(currentLevel.getServer(), targetDim);
                    if (targetLevel != null) {
                        Vec3 shipPos = ship.position();
                        Vec3 entryPos = new Vec3(0.0, PLANET_ENTRY_ALTITUDE, 0.0);
                        teleportNearbyEntities(currentLevel, targetLevel, shipPos, entryPos, ship);
                        teleportShip(ship, targetLevel, entryPos);
                        teleportPlayer(player, targetLevel, entryPos);
                        ship.setDestinationPlanet("");
                        ship.stopThrust();
                        player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                                "\u00a7b\u00a7l[\u5bfc\u822a] \u00a7r\u00a7a\u5df2\u5230\u8fbe \u00a7f" + dest + "\u00a7a\uff01\u5f00\u59cb\u7740\u9646..."));
                        return;
                    }
                }
            }
        }

        if (dim.equals(RocketDimensions.EARTH_ORBIT) && ship.getY() < LAND_ALTITUDE_THRESHOLD) {
            ServerLevel earthLevel = RocketDimensions.getLevel(currentLevel.getServer(), RocketDimensions.EARTH);
            if (earthLevel == null) earthLevel = currentLevel.getServer().getLevel(Level.OVERWORLD);
            if (earthLevel != null) {
                Vec3 shipPos = ship.position();
                Vec3 entryPos = new Vec3(ship.getX(), ORBIT_ALTITUDE_THRESHOLD - 20, ship.getZ());
                teleportNearbyEntities(currentLevel, earthLevel, shipPos, entryPos, ship);
                teleportShip(ship, earthLevel, entryPos);
                teleportPlayer(player, earthLevel, entryPos);
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                        "\u00a7b\u00a7l[\u5bfc\u822a] \u00a7r\u00a77\u8fd4\u56de\u5730\u7403\u5927\u6c14\u5c42..."));
                return;
            }
        }

        if (!dim.equals(RocketDimensions.EARTH) && !dim.equals(Level.OVERWORLD)
                && !dim.equals(RocketDimensions.EARTH_ORBIT)
                && dim.location().getNamespace().equals(ExampleMod.MODID)
                && ship.getY() >= ORBIT_ALTITUDE_THRESHOLD) {
            ServerLevel orbitLevel = RocketDimensions.getLevel(currentLevel.getServer(), RocketDimensions.EARTH_ORBIT);
            if (orbitLevel != null) {
                Vec3 shipPos = ship.position();
                Vec3 entryPos = new Vec3(0.0, 100.0, 0.0);
                teleportNearbyEntities(currentLevel, orbitLevel, shipPos, entryPos, ship);
                teleportShip(ship, orbitLevel, entryPos);
                teleportPlayer(player, orbitLevel, entryPos);
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                        "\u00a7b\u00a7l[\u5bfc\u822a] \u00a7r\u00a77\u79bb\u5f00\u661f\u7403\uff0c\u8fdb\u5165\u8f68\u9053\u7a7a\u95f4..."));
            }
        }
    }

    private static final Map<ResourceKey<Level>, Double> GRAVITY_MULTIPLIERS = new HashMap<>();
    static {
        GRAVITY_MULTIPLIERS.put(Level.OVERWORLD, 1.0);
        GRAVITY_MULTIPLIERS.put(RocketDimensions.EARTH, 1.0);
        GRAVITY_MULTIPLIERS.put(RocketDimensions.EARTH_ORBIT, 0.0);
        GRAVITY_MULTIPLIERS.put(RocketDimensions.MOON, 0.166);
        GRAVITY_MULTIPLIERS.put(RocketDimensions.MARS, 0.38);
        GRAVITY_MULTIPLIERS.put(RocketDimensions.VENUS, 0.91);
        GRAVITY_MULTIPLIERS.put(RocketDimensions.MERCURY, 0.38);
        GRAVITY_MULTIPLIERS.put(RocketDimensions.JUPITER, 2.53);
        GRAVITY_MULTIPLIERS.put(RocketDimensions.SATURN, 1.07);
        GRAVITY_MULTIPLIERS.put(RocketDimensions.URANUS, 0.89);
        GRAVITY_MULTIPLIERS.put(RocketDimensions.NEPTUNE, 1.14);
        GRAVITY_MULTIPLIERS.put(RocketDimensions.PLUTO, 0.063);
        GRAVITY_MULTIPLIERS.put(RocketDimensions.CERES, 0.029);
        GRAVITY_MULTIPLIERS.put(RocketDimensions.SUN, 28.0);
    }

    public static double getGravityMultiplier(ResourceKey<Level> dimension) {
        return GRAVITY_MULTIPLIERS.getOrDefault(dimension, 1.0);
    }

    public static boolean isZeroGravity(ResourceKey<Level> dimension) {
        return getGravityMultiplier(dimension) <= 0.001;
    }

    public static void checkAndTransitionToEarthOrbit(ServerPlayer player, RocketShipEntity ship, ServerLevel currentLevel) {

    }

    public static void checkAndTransitionToEarthSurface(ServerPlayer player, RocketShipEntity ship, ServerLevel currentLevel) {

    }
}
