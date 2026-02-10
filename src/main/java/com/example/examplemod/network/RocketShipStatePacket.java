package com.example.examplemod.network;

import com.example.examplemod.rocket.ship.RocketShipEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class RocketShipStatePacket {
    private final int entityId;
    private final double x;
    private final double y;
    private final double z;
    private final double vx;
    private final double vy;
    private final double vz;
    private final float yaw;
    private final float pitch;
    private final float roll;

    private final double thrustX;
    private final double thrustY;
    private final double thrustZ;
    private final double throttle;
    private final double fuelMass;

    public RocketShipStatePacket(int entityId, Vec3 pos, Vec3 vel, float yaw, float pitch, float roll,
                                 Vec3 thrust, double throttle, double fuelMass) {
        this.entityId = entityId;
        this.x = pos.x;
        this.y = pos.y;
        this.z = pos.z;
        this.vx = vel.x;
        this.vy = vel.y;
        this.vz = vel.z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.roll = roll;
        this.thrustX = thrust.x;
        this.thrustY = thrust.y;
        this.thrustZ = thrust.z;
        this.throttle = throttle;
        this.fuelMass = fuelMass;
    }

    public static void encode(RocketShipStatePacket packet, FriendlyByteBuf buf) {
        buf.writeInt(packet.entityId);
        buf.writeDouble(packet.x);
        buf.writeDouble(packet.y);
        buf.writeDouble(packet.z);
        buf.writeDouble(packet.vx);
        buf.writeDouble(packet.vy);
        buf.writeDouble(packet.vz);
        buf.writeFloat(packet.yaw);
        buf.writeFloat(packet.pitch);
        buf.writeFloat(packet.roll);
        buf.writeDouble(packet.thrustX);
        buf.writeDouble(packet.thrustY);
        buf.writeDouble(packet.thrustZ);
        buf.writeDouble(packet.throttle);
        buf.writeDouble(packet.fuelMass);
    }

    public static RocketShipStatePacket decode(FriendlyByteBuf buf) {
        int entityId = buf.readInt();
        Vec3 pos = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
        Vec3 vel = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
        float yaw = buf.readFloat();
        float pitch = buf.readFloat();
        float roll = buf.readFloat();
        Vec3 thrust = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
        double throttle = buf.readDouble();
        double fuelMass = buf.readDouble();
        return new RocketShipStatePacket(entityId, pos, vel, yaw, pitch, roll, thrust, throttle, fuelMass);
    }

    public static void handle(RocketShipStatePacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.level == null) {
                return;
            }
            Entity entity = minecraft.level.getEntity(packet.entityId);
            if (entity instanceof RocketShipEntity ship) {
                ship.applyClientState(
                        new Vec3(packet.x, packet.y, packet.z),
                        new Vec3(packet.vx, packet.vy, packet.vz),
                        packet.yaw,
                        packet.pitch,
                        packet.roll,
                        new Vec3(packet.thrustX, packet.thrustY, packet.thrustZ),
                        packet.throttle,
                        packet.fuelMass
                );
            }
        });
        context.get().setPacketHandled(true);
    }
}
