ackage com.example.rocketceg.rocket.config;

import net.minecraft.resources.ResourceLocation;

public class RocketEngineDefinition {
    private final ResourceLocation id;
    private final double thrustSeaLevel;
    private final double thrustVacuum;
    private final double ispSeaLevel;
    private final double ispVacuum;
    private final double throttleMin;
    private final double throttleMax;
    private final ResourceLocation fuelId;

    public RocketEngineDefinition(
        final ResourceLocation id,
        final double thrustSeaLevel,
        final double thrustVacuum,
        final double ispSeaLevel,
        final double ispVacuum,
        final double throttleMin,
        final double throttleMax,
        final ResourceLocation fuelId
    ) {
        this.id = id;
        this.thrustSeaLevel = thrustSeaLevel;
        this.thrustVacuum = thrustVacuum;
        this.ispSeaLevel = ispSeaLevel;
        this.ispVacuum = ispVacuum;
        this.throttleMin = throttleMin;
        this.throttleMax = throttleMax;
        this.fuelId = fuelId;
    }

    public ResourceLocation getId() {
        return id;
    }

    public double getThrustSeaLevel() {
        return thrustSeaLevel;
    }

    public double getThrustVacuum() {
        return thrustVacuum;
    }

    public double getIspSeaLevel() {
        return ispSeaLevel;
    }

    public double getIspVacuum() {
        return ispVacuum;
    }

    public double getThrottleMin() {
        return throttleMin;
    }

    public double getThrottleMax() {
        return throttleMax;
    }

    public ResourceLocation getFuelId() {
        return fuelId;
    }
}

