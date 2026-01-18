package com.example.rocketceg.rocket.config;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

/** 😡 行星/天体物理配置（阶段1数据结构）。 😡
     */
public class CelestialBodyConfig {

    private final ResourceLocation id;
    private final double mu;
    private final double radius;
    private final double surfaceGravity;
    private final double atmosphereTop;
    private final double atmosphereDensity0;
    private final double scaleHeight;
    private final ResourceKey<Level> surfaceDimension;
    private final ResourceKey<Level> orbitDimension;

    public CelestialBodyConfig(
        final ResourceLocation id,
        final double mu,
        final double radius,
        final double surfaceGravity,
        final double atmosphereTop,
        final double atmosphereDensity0,
        final double scaleHeight,
        final ResourceKey<Level> surfaceDimension,
        final ResourceKey<Level> orbitDimension
    ) {
        this.id = id;
        this.mu = mu;
        this.radius = radius;
        this.surfaceGravity = surfaceGravity;
        this.atmosphereTop = atmosphereTop;
        this.atmosphereDensity0 = atmosphereDensity0;
        this.scaleHeight = scaleHeight;
        this.surfaceDimension = surfaceDimension;
        this.orbitDimension = orbitDimension;
    }

    public ResourceLocation getId() {
        return id;
    }

    public double getMu() {
        return mu;
    }

    public double getRadius() {
        return radius;
    }

    public double getSurfaceGravity() {
        return surfaceGravity;
    }

    public double getAtmosphereTop() {
        return atmosphereTop;
    }

    public double getAtmosphereDensity0() {
        return atmosphereDensity0;
    }

    public double getScaleHeight() {
        return scaleHeight;
    }

    public ResourceKey<Level> getSurfaceDimension() {
        return surfaceDimension;
    }

    public ResourceKey<Level> getOrbitDimension() {
        return orbitDimension;
    }

    public double gravityAtRadius(final double r) {
        if (r <= 0.0D) {
            return surfaceGravity;
        }
        return mu / (r * r);
 馃槨
    }

    public double gravityAtAltitude(final double altitude) {
        return gravityAtRadius(radius + altitude);
    }

    public double densityAtAltitude(final double altitude) {
        if (altitude < 0.0D) {
            return atmosphereDensity0;
        }
        if (altitude >= atmosphereTop || atmosphereDensity0 <= 0.0D || scaleHeight <= 0.0D) {
            return 0.0D;
        }
        return atmosphereDensity0 * Math.exp(-altitude / scaleHeight);
 馃槨
    }
}

