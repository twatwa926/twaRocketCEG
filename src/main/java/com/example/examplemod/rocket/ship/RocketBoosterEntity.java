package com.example.examplemod.rocket.ship;

import com.example.examplemod.ExampleMod;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

public class RocketBoosterEntity extends Entity {

    private static final EntityDataAccessor<Float> DATA_TUMBLE_PITCH =
            SynchedEntityData.defineId(RocketBoosterEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_TUMBLE_YAW =
            SynchedEntityData.defineId(RocketBoosterEntity.class, EntityDataSerializers.FLOAT);

    private int age = 0;
    private static final int MAX_AGE = 160;

    private Vec3 vel = Vec3.ZERO;
    private static final double GRAVITY = 0.06;
    private static final double DRAG = 0.98;
    private float tumblePitchSpeed;
    private float tumbleYawSpeed;

    private int directionIndex = 0;

    private float flameScale = 1.0f;

    public RocketBoosterEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noCulling = true;
        this.noPhysics = false;
    }

    public void init(Vec3 separationDir, Vec3 inheritedVel, int index) {
        this.directionIndex = index;

        this.vel = inheritedVel.add(separationDir);

        this.tumblePitchSpeed = (float) (6.0 + random.nextFloat() * 8.0) * (random.nextBoolean() ? 1 : -1);
        this.tumbleYawSpeed = (float) (4.0 + random.nextFloat() * 6.0) * (random.nextBoolean() ? 1 : -1);
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(DATA_TUMBLE_PITCH, 0.0f);
        entityData.define(DATA_TUMBLE_YAW, 0.0f);
    }

    @Override
    public void tick() {
        super.tick();
        age++;

        if (age > MAX_AGE) {
            discard();
            return;
        }

        vel = vel.add(0.0, -GRAVITY, 0.0);

        vel = vel.multiply(DRAG, DRAG, DRAG);

        setPos(getX() + vel.x, getY() + vel.y, getZ() + vel.z);

        float pitch = entityData.get(DATA_TUMBLE_PITCH);
        float yaw = entityData.get(DATA_TUMBLE_YAW);
        pitch += tumblePitchSpeed;
        yaw += tumbleYawSpeed;
        entityData.set(DATA_TUMBLE_PITCH, pitch % 360f);
        entityData.set(DATA_TUMBLE_YAW, yaw % 360f);

        float lifeRatio = 1.0f - (float) age / MAX_AGE;
        flameScale = lifeRatio;

        if (level().isClientSide) {
            spawnTrailParticles(lifeRatio);
        }
    }

    private void spawnTrailParticles(float intensity) {
        if (intensity < 0.05f) return;
        double px = getX();
        double py = getY();
        double pz = getZ();

        int count = (int) (3 * intensity) + 1;
        for (int i = 0; i < count; i++) {
            double ox = (random.nextDouble() - 0.5) * 0.5;
            double oy = (random.nextDouble() - 0.5) * 0.3;
            double oz = (random.nextDouble() - 0.5) * 0.5;
            level().addParticle(ParticleTypes.FLAME,
                    px + ox, py + oy - 0.5, pz + oz,
                    vel.x * 0.1, vel.y * 0.1 - 0.05, vel.z * 0.1);
        }

        if (intensity > 0.3f && random.nextFloat() < intensity) {
            level().addParticle(ParticleTypes.LARGE_SMOKE,
                    px + (random.nextDouble() - 0.5) * 0.6,
                    py - 0.3,
                    pz + (random.nextDouble() - 0.5) * 0.6,
                    0, -0.02, 0);
        }

        if (intensity < 0.5f && random.nextFloat() < 0.4f) {
            level().addParticle(ParticleTypes.SOUL_FIRE_FLAME,
                    px + (random.nextDouble() - 0.5) * 0.3,
                    py + (random.nextDouble() - 0.5) * 0.2,
                    pz + (random.nextDouble() - 0.5) * 0.3,
                    0, 0, 0);
        }
    }

    public float getTumblePitch(float partialTick) {
        return entityData.get(DATA_TUMBLE_PITCH) + tumblePitchSpeed * partialTick;
    }
    public float getTumbleYaw(float partialTick) {
        return entityData.get(DATA_TUMBLE_YAW) + tumbleYawSpeed * partialTick;
    }
    public float getFlameScale() { return flameScale; }
    public int getDirectionIndex() { return directionIndex; }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        age = tag.getInt("Age");
        directionIndex = tag.getInt("DirIdx");
        tumblePitchSpeed = tag.getFloat("TPSpeed");
        tumbleYawSpeed = tag.getFloat("TYSpeed");
        vel = new Vec3(tag.getDouble("VX"), tag.getDouble("VY"), tag.getDouble("VZ"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("Age", age);
        tag.putInt("DirIdx", directionIndex);
        tag.putFloat("TPSpeed", tumblePitchSpeed);
        tag.putFloat("TYSpeed", tumbleYawSpeed);
        tag.putDouble("VX", vel.x);
        tag.putDouble("VY", vel.y);
        tag.putDouble("VZ", vel.z);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
