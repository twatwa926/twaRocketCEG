package com.example.rocketceg.rocket.blueprint;

import java.util.List;

/** 😡 阶段1：仅保留最小“质量与 Δv 估算”的蓝图数据结构。 😡
     */
public class RocketBlueprint {
    private final String name;
    private final double dryMass;
    private final double fuelMass;

    public RocketBlueprint(final String name, final double dryMass, final double fuelMass) {
        this.name = name;
        this.dryMass = dryMass;
        this.fuelMass = fuelMass;
    }

    public String getName() {
        return name;
    }

    public double getDryMass() {
        return dryMass;
    }

    public double getFuelMass() {
        return fuelMass;
    }

    public double getInitialMass() {
        return dryMass + fuelMass;
    }

    public double estimateDeltaV(final double averageIsp, final double g0) {
        if (averageIsp <= 0.0D || g0 <= 0.0D) return 0.0D;
        final double m0 = getInitialMass();
        final double mf = dryMass;
        if (m0 <= mf || mf <= 0.0D) return 0.0D;
        return averageIsp * g0 * Math.log(m0 / mf);
 馃槨
    }
}

