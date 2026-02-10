package com.example.examplemod.rocket;

import com.example.examplemod.network.RocketNetwork;
import com.example.examplemod.network.SeamlessTeleportPacket;
import net.minecraftforge.network.PacketDistributor;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class SeamlessCore {

    private static final Set<ServerPlayer> PENDING_SEAMLESS = ConcurrentHashMap.newKeySet();

    private SeamlessCore() {}

    public static void requestSeamlessTeleport(ServerPlayer player, ServerLevel targetLevel) {
        if (player == null || targetLevel == null) return;
        PENDING_SEAMLESS.add(player);
        player.changeDimension(targetLevel);
    }

    public static boolean consumeSeamlessRequest(ServerPlayer player) {
        return PENDING_SEAMLESS.remove(player);
    }

    public static Entity startEyeBasedSeamlessTeleport(ServerPlayer player, ServerLevel targetLevel) {
        if (player == null || targetLevel == null) return player;

        Vec3 eyePos = player.getEyePosition(1.0f);
        double x = eyePos.x;
        double y = eyePos.y;
        double z = eyePos.z;
        float yaw = player.getYRot();
        float pitch = player.getXRot();

        Vec3 localFrom = new Vec3(x, y, z);
        Vec3 localTo = SolarSystemCoordinateManager.transformPosition(
                localFrom, player.level().dimension(), targetLevel.dimension());
        double newX = localTo.x;
        double newY = localTo.y;
        double newZ = localTo.z;

        ResourceKey<Level> dimKey = targetLevel.dimension();
        SeamlessTeleportPacket packet = new SeamlessTeleportPacket(
                dimKey.location(),
                newX, newY, newZ,
                yaw, pitch
        );
        RocketNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);

        Set<RelativeMovement> rel = Set.of(
                RelativeMovement.X, RelativeMovement.Y, RelativeMovement.Z,
                RelativeMovement.Y_ROT, RelativeMovement.X_ROT
        );
        boolean ok = player.teleportTo(targetLevel, newX, newY, newZ, rel, yaw, pitch);
        if (!ok) return player;

        return player;
    }

    private static volatile SeamlessTeleportPacket clientPending;
    private static volatile Object clientStoredRespawnPacket;

    public static void setClientPending(SeamlessTeleportPacket packet) {
        clientPending = packet;
    }

    public static boolean hasPendingSeamless() {
        return clientPending != null;
    }

    public static void storeRespawnPacket(Object packet) {
        clientStoredRespawnPacket = packet;
    }

    public static void updateBeforeFrameRendering() {
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
        if (clientStoredRespawnPacket != null && mc.getConnection() != null) {
            Object stored = clientStoredRespawnPacket;
            var listener = mc.getConnection();
            if (listener instanceof com.example.examplemod.mixin.rocket.SeamlessClientInvoker invoker
                    && stored instanceof net.minecraft.network.protocol.game.ClientboundRespawnPacket respawnPacket) {
                invoker.rocketwa$invokeHandleRespawn(respawnPacket);
            }
            clientStoredRespawnPacket = null;
        }
        if (clientPending != null) {
            if (mc.player != null && mc.level != null) {
                mc.player.setPos(clientPending.x(), clientPending.y(), clientPending.z());
                mc.player.setYRot(clientPending.yaw());
                mc.player.setXRot(clientPending.pitch());
            }
            clientPending = null;
        }
    }

    public static SeamlessTeleportPacket getClientPending() {
        return clientPending;
    }
}
