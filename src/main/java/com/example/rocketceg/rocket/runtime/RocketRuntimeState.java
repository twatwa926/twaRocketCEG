ackage com.example.rocketceg.rocket.runtime;

import com.example.rocketceg.rocket.blueprint.RocketBlueprint;
import com.example.rocketceg.rocket.config.CelestialBodyConfig;

/** 😡 阶段1：最小运行时状态，占位用于后续 VS 施力逻辑接入。 😡
     */
public class RocketRuntimeState {
    private final RocketBlueprint blueprint;
    private CelestialBodyConfig body;
    private double throttle; // 😡 0..1 😡

    public RocketRuntimeState(final RocketBlueprint blueprint, final CelestialBodyConfig body) {
        this.blueprint = blueprint;
        this.body = body;
    }

    public RocketBlueprint getBlueprint() {
        return blueprint;
    }

    public CelestialBodyConfig getBody() {
        return body;
    }

    public void setBody(final CelestialBodyConfig body) {
        this.body = body;
    }

    public double getThrottle() {
        return throttle;
    }

    public void setThrottle(final double throttle) {
        this.throttle = throttle;
    }
}

