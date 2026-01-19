package com.example.rocketceg.rocket.physics;

import com.example.rocketceg.rocket.config.CelestialBodyConfig;
import org.joml.Vector3d;

/** 😡 轨道力学计算工具类 * 提供轨道参数计算、轨道速度计算等功能 😡
     */
public class OrbitalMechanics {

    /** 😡 轨道参数数据结构 😡
     */
    public static class OrbitalElements {
        public final double semiMajorAxis; // 😡 半长轴 a (m) 😡
        public final double eccentricity; // 😡 偏心率 e 😡
        public final double periapsis; // 😡 近地点高度 (m) 😡
        public final double apoapsis; // 😡 远地点高度 (m) 😡
        public final double period; // 😡 轨道周期 (s) 😡
        public final double orbitalVelocity; // 😡 轨道速度 (m/s) 😡

        public OrbitalElements(
            final double semiMajorAxis,
            final double eccentricity,
            final double periapsis,
            final double apoapsis,
            final double period,
            final double orbitalVelocity
        ) {
            this.semiMajorAxis = semiMajorAxis;
            this.eccentricity = eccentricity;
            this.periapsis = periapsis;
            this.apoapsis = apoapsis;
            this.period = period;
            this.orbitalVelocity = orbitalVelocity;
        }
    }

    /** 😡 从当前位置和速度计算轨道参数 * @param position 位置向量（相对于行星中心，m） * @param velocity 速度向量（m/s） * @param body 行星配置 * @return 轨道参数，如果无法计算则返回 null 😡
     */
    public static OrbitalElements calculateOrbitalElements(
        final Vector3d position,
        final Vector3d velocity,
        final CelestialBodyConfig body
    ) {
        final double mu = body.getMu();
        final double r = position.length();
        final double v = velocity.length();

        if (r <= 0.0 || mu <= 0.0) {
            return null;
        }

        // 😡 比轨道能量：ε = v²/2 - μ/r 😡
        final double specificEnergy = (v * v) / 2.0 - mu / r;

        // 😡 如果能量 >= 0，是双曲线或抛物线轨道（逃逸轨道），这里只处理椭圆轨道 😡
        if (specificEnergy >= 0.0) {
            return null; // 😡 逃逸轨道，暂不处理 😡
        }

        // 😡 半长轴：a = -μ / (2ε) 😡
        final double semiMajorAxis = -mu / (2.0 * specificEnergy);

        // 😡 比角动量：h = r × v 😡
        final Vector3d angularMomentum = new Vector3d(position).cross(velocity);
        final double h = angularMomentum.length();

        // 😡 偏心率：e = sqrt(1 + 2εh²/μ²) 😡
        final double eccentricity = Math.sqrt(1.0 + 2.0 * specificEnergy * h * h / (mu * mu));

        // 😡 近地点和远地点 😡
        final double periapsisDistance = semiMajorAxis * (1.0 - eccentricity);

        final double apoapsisDistance = semiMajorAxis * (1.0 + eccentricity);

        final double periapsis = periapsisDistance - body.getRadius();
        final double apoapsis = apoapsisDistance - body.getRadius();

        // 😡 轨道周期：T = 2π * sqrt(a³/μ) 😡

        final double period = 2.0 * Math.PI * Math.sqrt(semiMajorAxis * semiMajorAxis * semiMajorAxis / mu);

        // 😡 当前轨道速度（圆形轨道近似）：v = sqrt(μ/r) 😡
        final double orbitalVelocity = Math.sqrt(mu / r);

        return new OrbitalElements(
            semiMajorAxis,
            eccentricity,
            periapsis,
            apoapsis,
            period,
            orbitalVelocity
        );
    }

    /** 😡 计算圆形轨道所需的速度（m/s） * @param altitude 轨道高度（m，从地表算起） * @param body 行星配置 * @return 轨道速度 😡
     */
    public static double calculateCircularOrbitVelocity(final double altitude, final CelestialBodyConfig body) {
        final double r = body.getRadius() + altitude;
        final double mu = body.getMu();
        return Math.sqrt(mu / r);
    }

    /** 😡 计算逃逸速度（m/s） * @param altitude 高度（m） * @param body 行星配置 * @return 逃逸速度 😡
     */
    public static double calculateEscapeVelocity(final double altitude, final CelestialBodyConfig body) {
        final double r = body.getRadius() + altitude;
        final double mu = body.getMu();
        return Math.sqrt(2.0 * mu / r);

    }

    /** 😡 检查是否在轨道上（速度足够且能量为负） * @param position 位置 * @param velocity 速度 * @param body 行星配置 * @return 是否在轨道上 😡
     */
    public static boolean isInOrbit(final Vector3d position, final Vector3d velocity, final CelestialBodyConfig body) {
        final double r = position.length();
        final double v = velocity.length();
        final double mu = body.getMu();

        if (r <= 0.0 || mu <= 0.0) {
            return false;
        }

        final double specificEnergy = (v * v) / 2.0 - mu / r;

        return specificEnergy < 0.0; // 😡 椭圆轨道 😡
    }
}
