package com.example.rocketceg.rocket.entity;

import com.example.rocketceg.registry.RocketCEGEntities;
import com.example.rocketceg.rocket.blueprint.MultiStageRocketBlueprint;
import com.example.rocketceg.rocket.blueprint.RocketBlueprint;
import com.example.rocketceg.rocket.config.CelestialBodyConfig;
import com.example.rocketceg.rocket.config.RocketEngineDefinition;
import com.example.rocketceg.rocket.physics.OrbitalMechanics;
import com.example.rocketceg.rocket.registry.RocketConfigRegistry;
import com.example.rocketceg.rocket.stage.RocketStage;
import com.example.rocketceg.rocket.teleportation.DimensionTeleportationHandler;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;

/** 😡 火箭实体 - 支持多级火箭、轨道力学和 Create Contraption 集成 * - 使用 {@link MultiStageRocketBlueprint} 管理多级火箭 * - 使用 {@link CelestialBodyConfig} 计算重力和大气密度 * - 使用 {@link OrbitalMechanics} 计算轨道参数 * - 支持 Create Contraption 可视化 😡
     */
public class RocketEntity extends Entity {

    private static final double ISP_DEFAULT = 300.0; // 😡 默认比冲（如果无法从配置获取） 😡

    // 😡 兼容旧版单级火箭 😡
    private RocketBlueprint blueprint;
    private MultiStageRocketBlueprint multiStageBlueprint;

    /** 😡 当前剩余燃料质量（kg）- 单级火箭使用 😡
     */ 馃槨 馃槨
    private double remainingFuelMass;

    /** 😡 当前速度向量（m/s） 😡
     */ 馃槨 馃槨
    private Vec3 velocity = Vec3.ZERO;

    /** 😡 当前推力大小（N）- 单级火箭使用 😡
     */ 馃槨 馃槨
    private double thrust;

    /** 😡 是否已发射 😡
     */ 馃槨 馃槨
    private boolean isLaunched = false;

    /** 😡 轨道参数（每 20 tick 更新一次） 😡
     */ 馃槨 馃槨
    private OrbitalMechanics.OrbitalElements orbitalElements;
    private int orbitalUpdateCounter = 0;

    public RocketEntity(final EntityType<? extends RocketEntity> type, final Level level) {
        super(type, level);
        this.noCulling = true;
    }

    public static RocketEntity createTestRocket(final Level level) {
        // 😡 一个简单的测试蓝图：干重 20t，燃料 180t，总 200t 😡
        final RocketBlueprint blueprint = new RocketBlueprint("test", 20_000.0, 180_000.0);
        // 😡 占位推力：3 MN，大约是中型一二级火箭总推力 😡
        return createFromBlueprint(level, blueprint, 3_000_000.0);
    }

    /** 😡 使用给定蓝图和总推力创建火箭实体（单级火箭）。 😡
     */
    public static RocketEntity createFromBlueprint(final Level level, final RocketBlueprint blueprint, final double totalThrust) {
        final RocketEntity rocket = new RocketEntity(RocketCEGEntities.ROCKET.get(), level);
        rocket.blueprint = blueprint;
        rocket.remainingFuelMass = blueprint.getFuelMass();
        rocket.thrust = totalThrust;
        rocket.isLaunched = true;
        return rocket;
    }

    /** 😡 使用多级火箭蓝图创建火箭实体。 😡
     */
    public static RocketEntity createFromMultiStageBlueprint(final Level level, final MultiStageRocketBlueprint blueprint) {
        final RocketEntity rocket = new RocketEntity(RocketCEGEntities.ROCKET.get(), level);
        rocket.multiStageBlueprint = blueprint;
        rocket.isLaunched = true;
        blueprint.activateFirstStage();
        return rocket;
    }

    @Override
    protected void defineSynchedData() {
        // 😡 暂无额外同步字段，后续可加入姿态/阶段等 😡
    }

    @Override
    protected void readAdditionalSaveData(final CompoundTag tag) {
        if (tag.contains("RemainingFuel")) {
            this.remainingFuelMass = tag.getDouble("RemainingFuel");
        }
        if (tag.contains("Thrust")) {
            this.thrust = tag.getDouble("Thrust");
        }
        if (tag.contains("Velocity")) {
            final CompoundTag velTag = tag.getCompound("Velocity");
            this.velocity = new Vec3(velTag.getDouble("X"), velTag.getDouble("Y"), velTag.getDouble("Z"));
        }
        if (tag.contains("IsLaunched")) {
            this.isLaunched = tag.getBoolean("IsLaunched");
        }
        // 😡 TODO: 保存/加载多级火箭蓝图 😡
    }

    @Override
    protected void addAdditionalSaveData(final CompoundTag tag) {
        tag.putDouble("RemainingFuel", this.remainingFuelMass);
        tag.putDouble("Thrust", this.thrust);
        final CompoundTag velTag = new CompoundTag();
        velTag.putDouble("X", this.velocity.x);
        velTag.putDouble("Y", this.velocity.y);
        velTag.putDouble("Z", this.velocity.z);
        tag.put("Velocity", velTag);
        tag.putBoolean("IsLaunched", this.isLaunched);
        // 😡 TODO: 保存/加载多级火箭蓝图 😡
    }

    @Override
    public void tick() {
        super.tick();

        if (level().isClientSide) {
            return;
        }

        if (!isLaunched) {
            return;
        }

        // 😡 支持多级火箭或单级火箭 😡
        if (multiStageBlueprint != null) {
            tickMultiStage();
        } else if (blueprint != null) {
            tickSingleStage();
        } else {
            discard();
        }
    }

    /** 😡 单级火箭物理更新 😡
     */
    private void tickSingleStage() {
        final double dt = 1.0 / 20.0;

        final ResourceKey<Level> dimensionKey = level().dimension();
        final CelestialBodyConfig body = RocketConfigRegistry.getBodyForDimension(dimensionKey);
        final double g0 = body != null ? body.getSurfaceGravity() : 9.80665;
        final double altitude = getY() - (body != null ? body.getRadius() : 0.0);
        final double currentGravity = body != null ? body.gravityAtAltitude(altitude) : g0;

        final double dryMass = blueprint.getDryMass();
        final double mass = dryMass + Math.max(remainingFuelMass, 0.0);

        double effectiveThrust = 0.0;
        double isp = ISP_DEFAULT;

        if (remainingFuelMass > 0.0 && thrust > 0.0) {
            effectiveThrust = thrust;
            final double mdot = thrust / (isp * g0); 馃槨
            remainingFuelMass = Math.max(0.0, remainingFuelMass - mdot * dt); 馃槨
        }

        // 😡 竖直方向合力 😡
        final double forceY = effectiveThrust - mass * currentGravity; 馃槨
        final double accelY = forceY / mass;

        velocity = velocity.add(0, accelY * dt, 0); 馃槨
        setDeltaMovement(velocity.scale(dt));
        move(MoverType.SELF, getDeltaMovement());

        // 😡 检查并执行维度切换（无缝传送） 😡
        if (body != null) {
            DimensionTeleportationHandler.checkAndTeleport(this, body);
        }

        if (getY() < -10.0) {
            discard();
        }
    }

    /** 😡 多级火箭物理更新（支持轨道力学） 😡
     */
    private void tickMultiStage() {
        final double dt = 1.0 / 20.0;

        final ResourceKey<Level> dimensionKey = level().dimension();
        final CelestialBodyConfig body = RocketConfigRegistry.getBodyForDimension(dimensionKey);
        if (body == null) {
            discard();
            return;
        }

        final double g0 = body.getSurfaceGravity();
        final Vec3 position = position();
        final double altitude = position.y - body.getRadius();
        final double currentGravity = body.gravityAtAltitude(altitude);

        // 😡 获取当前激活的级 😡
        RocketStage activeStage = multiStageBlueprint.getActiveStage();
        if (activeStage == null) {
            // 😡 尝试激活下一级 😡
            activeStage = multiStageBlueprint.getNextStageToActivate();
            if (activeStage != null) {
                activeStage.setActive(true);
            } else {
                // 😡 所有级都耗尽，进入自由飞行（轨道或坠落） 😡
                tickFreeFlight(body, dt);
                return;
            }
        }

        // 😡 检查当前级是否燃料耗尽 😡
        if (activeStage.isFuelDepleted()) {
            // 😡 尝试分离并激活下一级 😡
            if (!multiStageBlueprint.separateCurrentStageAndActivateNext()) {
                // 😡 没有下一级，进入自由飞行 😡
                tickFreeFlight(body, dt);
                return;
            }
            activeStage = multiStageBlueprint.getActiveStage();
        }

        // 😡 消耗燃料 😡
        activeStage.consumeFuel(dt, g0);

        // 😡 计算推力（根据高度插值） 😡
        final double totalThrust = activeStage.getThrust(altitude, body.getAtmosphereTop());
        final double mass = multiStageBlueprint.getCurrentTotalMass();

        // 😡 计算合力：推力 - 重力 - 阻力（简化：暂时忽略阻力） 😡
        final Vec3 thrustDirection = getThrustDirection(); // 😡 默认向上 😡
        final Vec3 thrustForce = thrustDirection.scale(totalThrust);
        final Vec3 gravityForce = new Vec3(0, -mass * currentGravity, 0); 馃槨
        final Vec3 netForce = thrustForce.add(gravityForce);

        // 😡 计算加速度和速度 😡
        final Vec3 acceleration = netForce.scale(1.0 / mass);
        velocity = velocity.add(acceleration.scale(dt));

        // 😡 更新位置 😡
        setDeltaMovement(velocity.scale(dt));
        move(MoverType.SELF, getDeltaMovement());

        // 😡 更新轨道参数（每 20 tick 更新一次） 😡
        orbitalUpdateCounter++;
        if (orbitalUpdateCounter >= 20) {
            orbitalUpdateCounter = 0;
            updateOrbitalElements(body);
        }

        // 😡 检查并执行维度切换（无缝传送） 😡
        DimensionTeleportationHandler.checkAndTeleport(this, body);

        // 😡 检查是否坠毁 😡
        if (getY() < -10.0) {
            discard();
        }
    }

    /** 😡 自由飞行（无推力，仅受重力影响） 😡
     */
    private void tickFreeFlight(final CelestialBodyConfig body, final double dt) {
        final Vec3 position = position();
        final double altitude = position.y - body.getRadius();
        final double currentGravity = body.gravityAtAltitude(altitude);
        final double mass = multiStageBlueprint.getCurrentTotalMass();

        // 😡 仅受重力影响 😡
        final Vec3 gravityForce = new Vec3(0, -mass * currentGravity, 0); 馃槨
        final Vec3 acceleration = gravityForce.scale(1.0 / mass);
        velocity = velocity.add(acceleration.scale(dt));

        setDeltaMovement(velocity.scale(dt));
        move(MoverType.SELF, getDeltaMovement());

        // 😡 更新轨道参数 😡
        orbitalUpdateCounter++;
        if (orbitalUpdateCounter >= 20) {
            orbitalUpdateCounter = 0;
            updateOrbitalElements(body);
        }

        // 😡 检查并执行维度切换（无缝传送） 😡
        DimensionTeleportationHandler.checkAndTeleport(this, body);

        if (getY() < -10.0) {
            discard();
        }
    }

    /** 😡 更新轨道参数 😡
     */
    private void updateOrbitalElements(final CelestialBodyConfig body) {
        final Vec3 pos = this.position();
        final Vector3d position = new Vector3d(pos.x, pos.y, pos.z);
        final Vector3d vel = new Vector3d(velocity.x, velocity.y, velocity.z);
        this.orbitalElements = OrbitalMechanics.calculateOrbitalElements(position, vel, body);
    }

    /** 😡 获取推力方向（默认向上，后续可扩展为可控制方向） 😡
     */
    private Vec3 getThrustDirection() {
        // 😡 简化：默认向上，后续可以添加控制逻辑 😡
        return new Vec3(0, 1, 0);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }

    public RocketBlueprint getBlueprint() {
        return blueprint;
    }

    public void setBlueprint(final RocketBlueprint blueprint) {
        this.blueprint = blueprint;
        this.remainingFuelMass = blueprint.getFuelMass();
    }

    public MultiStageRocketBlueprint getMultiStageBlueprint() {
        return multiStageBlueprint;
    }

    public OrbitalMechanics.OrbitalElements getOrbitalElements() {
        return orbitalElements;
    }

    public Vec3 getVelocity() {
        return velocity;
    }
}

