package com.example.examplemod.rocket;

import com.example.examplemod.rocket.ship.RocketShipEntity;
import com.example.examplemod.rocket.ship.RocketShipRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class RocketAvionicsBayBlockEntity extends BlockEntity {
    private static final double PLANET_RADIUS = 6000.0;
    private static final double SURFACE_GRAVITY = 0.06;
    private static final double DRAG_COEFF = 0.35;
    private static final double REF_AREA = 2.0;
    private static final double TURN_ACCEL = 1.5;
    private static final double TURN_DAMPING = 0.85;
    private static final double MAX_ANGULAR_SPEED = 5.0;
    private static final double MAX_THRUST = 0.65;
    private static final double FUEL_FLOW = 0.08;
    private static final double DRY_MASS = 120.0;
    private static final double INITIAL_FUEL = 180.0;
    private static final int INPUT_TIMEOUT_TICKS = 20;

    private long controlledShipId = -1L;
    private Vec3 pendingInput = Vec3.ZERO;
    private int ticksSinceInput = 0;
    private boolean autopilotEnabled = false;
    private UUID controllingPlayer;
    private RocketFlightProgram flightProgram = RocketFlightProgram.defaultProgram();
    private BlockPos launchOrigin;
    private Vec3 launchHeading = new Vec3(0, 0, 1);
    private double fuelMass = INITIAL_FUEL;
    private float yaw;
    private float pitch;
    private float roll;
    private float yawVelocity;
    private float pitchVelocity;
    private float rollVelocity;

    public RocketAvionicsBayBlockEntity(BlockPos pos, BlockState state) {
        super(com.example.examplemod.ExampleMod.AVIONICS_BAY_BE.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, RocketAvionicsBayBlockEntity be) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        if (be.controlledShipId == -1L) {
            return;
        }

        RocketShipEntity ship = RocketShipRegistry.get(be.controlledShipId);
        if (ship == null) {
            be.detachShip();
            return;
        }

        Vec3 input = be.pendingInput;
        be.ticksSinceInput++;
        if (be.ticksSinceInput > INPUT_TIMEOUT_TICKS) {
            input = Vec3.ZERO;
        }

        Vec3 desired = be.autopilotEnabled && input.equals(Vec3.ZERO)
                ? be.computeAutopilotThrust(ship)
                : input;

        double throttle = Mth.clamp(desired.length(), 0.0, 1.0);
        if (!be.autopilotEnabled) {
            throttle = Mth.clamp(Math.max(0.0, input.y), 0.0, 1.0);
        }

        be.updateOrientation(desired, throttle);
        Vec3 forward = be.forwardVector();
        double effectiveThrottle = Mth.clamp(throttle * be.flightProgram.getMaxThrust(), 0.0, 1.0);
        ship.updateControl(forward, effectiveThrottle, be.fuelMass, be.yaw, be.pitch, be.roll);
        be.consumeFuel(throttle);

        if (be.controllingPlayer != null) {
            var player = serverLevel.getPlayerByUUID(be.controllingPlayer);
            if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                var shipLevel = ship.level() instanceof ServerLevel sl ? sl : null;
                if (shipLevel != null) {
                    RocketDimensionTransition.checkAndTransitionToEarthOrbit(serverPlayer, ship, shipLevel);
                }
            }
        }
    }

    private Vec3 computeAutopilotThrust(RocketShipEntity ship) {
        if (launchOrigin == null) {
            launchOrigin = worldPosition;
        }
        Vec3 pos = ship.position();
        double altitude = pos.y - launchOrigin.getY();
        double turnStart = flightProgram.getTurnStartAltitude();
        double turnEnd = Math.max(turnStart + 1.0, flightProgram.getTurnEndAltitude());
        double targetOrbit = flightProgram.getTargetOrbitAltitude();

        double pitch;
        if (altitude <= turnStart) {
            pitch = 90.0;
        } else if (altitude >= turnEnd) {
            pitch = 0.0;
        } else {
            double t = (altitude - turnStart) / (turnEnd - turnStart);
            pitch = 90.0 * (1.0 - t);
        }

        Vec3 up = new Vec3(0, 1, 0);
        Vec3 forward = launchHeading.normalize();
        double rad = Math.toRadians(pitch);
        Vec3 thrust = forward.scale(Math.cos(rad)).add(up.scale(Math.sin(rad))).normalize();

        if (altitude >= targetOrbit) {
            double r = PLANET_RADIUS + altitude;
            double mu = SURFACE_GRAVITY * PLANET_RADIUS * PLANET_RADIUS;
            double orbitalSpeed = Math.sqrt(mu / r);
            Vec3 velocity = ship.getVelocityVec();
            Vec3 horizontal = new Vec3(velocity.x, 0, velocity.z);
            if (horizontal.length() >= orbitalSpeed * 0.98) {
                autopilotEnabled = false;
                return Vec3.ZERO;
            }
        }

        return thrust;
    }

    private void updateOrientation(Vec3 desired, double throttle) {
        float yawInput = (float) desired.x;
        float pitchInput = (float) -desired.z;
        float rollInput = 0.0f;

        if (autopilotEnabled && desired.lengthSqr() > 1.0E-6) {
            yawInput = (float) ((yawFromVector(desired) - yaw) * 0.02);
            pitchInput = (float) ((pitchFromVector(desired) - pitch) * 0.02);
        }

        yawVelocity = (float) Mth.clamp(yawVelocity * TURN_DAMPING + yawInput * TURN_ACCEL * throttle, -MAX_ANGULAR_SPEED, MAX_ANGULAR_SPEED);
        pitchVelocity = (float) Mth.clamp(pitchVelocity * TURN_DAMPING + pitchInput * TURN_ACCEL * throttle, -MAX_ANGULAR_SPEED, MAX_ANGULAR_SPEED);
        rollVelocity = (float) Mth.clamp(rollVelocity * TURN_DAMPING + rollInput * TURN_ACCEL * throttle, -MAX_ANGULAR_SPEED, MAX_ANGULAR_SPEED);

        yaw = Mth.wrapDegrees(yaw + yawVelocity);
        pitch = Mth.clamp(pitch + pitchVelocity, -85.0f, 85.0f);
        roll = Mth.wrapDegrees(roll + rollVelocity);
    }

    private Vec3 forwardVector() {
        float yawRad = yaw * Mth.DEG_TO_RAD;
        float pitchRad = pitch * Mth.DEG_TO_RAD;
        double x = -Mth.sin(yawRad) * Mth.cos(pitchRad);
        double y = -Mth.sin(pitchRad);
        double z = Mth.cos(yawRad) * Mth.cos(pitchRad);
        return new Vec3(x, y, z).normalize();
    }

    private void consumeFuel(double throttle) {
        if (fuelMass <= 0.0) {
            fuelMass = 0.0;
            return;
        }
        fuelMass = Math.max(0.0, fuelMass - FUEL_FLOW * throttle);
    }

    private static Vec3 clamp(Vec3 motion, double maxSpeed) {
        double speed = motion.length();
        if (speed > maxSpeed) {
            return motion.normalize().scale(maxSpeed);
        }
        return motion;
    }

    public void attachShip(long shipId) {
        this.controlledShipId = shipId;
        RocketControlRegistry.register(shipId, this);
        if (level != null && !level.isClientSide) {
            level.setBlock(worldPosition, getBlockState().setValue(RocketAvionicsBayBlock.ATTACHED, true), 3);
        }
        setChanged();
    }

    public void detachShip() {
        if (controlledShipId != -1L) {
            RocketControlRegistry.unregister(controlledShipId);
        }
        controlledShipId = -1L;
        pendingInput = Vec3.ZERO;
        ticksSinceInput = 0;
        if (level != null && !level.isClientSide) {
            level.setBlock(worldPosition, getBlockState().setValue(RocketAvionicsBayBlock.ATTACHED, false), 3);
        }
        setChanged();
    }

    public boolean isAttached() {
        return controlledShipId != -1L;
    }

    public double getFuelMass() {
        return fuelMass;
    }

    public void addFuel(double amount) {
        if (amount <= 0) return;
        fuelMass = Math.min(RocketFuelHelper.getMaxFuelMass(), fuelMass + amount);
        setChanged();
    }

    public void applyInput(UUID playerId, Vec3 input) {
        if (controllingPlayer != null && !controllingPlayer.equals(playerId)) {
            return;
        }
        this.pendingInput = input;
        this.ticksSinceInput = 0;
        this.autopilotEnabled = false;
        setChanged();
    }

    public void enableAutopilot(Player player) {
        if (player != null) {
            this.controllingPlayer = player.getUUID();
            Vec3 look = player.getLookAngle();
            Vec3 flat = new Vec3(look.x, 0, look.z);
            if (flat.lengthSqr() > 1.0E-4) {
                this.launchHeading = flat.normalize();
            }
        }
        this.launchOrigin = worldPosition;
        this.autopilotEnabled = true;
        setChanged();
    }

    public void setProgram(String script) {
        this.flightProgram = RocketFlightProgram.parse(script);
        setChanged();
    }

    public RocketFlightProgram getProgram() {
        return flightProgram;
    }

    public boolean isAutopilotEnabled() {
        return autopilotEnabled;
    }

    public void setControllingPlayer(UUID playerId) {
        this.controllingPlayer = playerId;
        setChanged();
    }

    public void setLaunchOrientationUp() {
        this.pitch = -90.0f;
        this.yawVelocity = 0;
        this.pitchVelocity = 0;
        this.rollVelocity = 0;
        setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putLong("ControlledShipId", controlledShipId);
        tag.putBoolean("Autopilot", autopilotEnabled);
        tag.putString("Program", flightProgram.getRawScript());
        tag.putDouble("FuelMass", fuelMass);
        tag.putFloat("Yaw", yaw);
        tag.putFloat("Pitch", pitch);
        tag.putFloat("Roll", roll);
        tag.putFloat("YawVel", yawVelocity);
        tag.putFloat("PitchVel", pitchVelocity);
        tag.putFloat("RollVel", rollVelocity);
        if (controllingPlayer != null) {
            tag.putUUID("Controller", controllingPlayer);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        controlledShipId = tag.getLong("ControlledShipId");
        autopilotEnabled = tag.getBoolean("Autopilot");
        if (tag.contains("Program")) {
            flightProgram = RocketFlightProgram.parse(tag.getString("Program"));
        }
        if (tag.hasUUID("Controller")) {
            controllingPlayer = tag.getUUID("Controller");
        }
        fuelMass = tag.getDouble("FuelMass");
        yaw = tag.getFloat("Yaw");
        pitch = tag.getFloat("Pitch");
        roll = tag.getFloat("Roll");
        yawVelocity = tag.getFloat("YawVel");
        pitchVelocity = tag.getFloat("PitchVel");
        rollVelocity = tag.getFloat("RollVel");
    }

    private static double yawFromVector(Vec3 vec) {
        return Math.toDegrees(Math.atan2(-vec.x, vec.z));
    }

    private static double pitchFromVector(Vec3 vec) {
        double horizontal = Math.sqrt(vec.x * vec.x + vec.z * vec.z);
        return Math.toDegrees(Math.atan2(-vec.y, horizontal));
    }
}
