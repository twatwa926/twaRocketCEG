package com.example.rocketceg.rocket.stage;

import com.example.rocketceg.rocket.config.RocketEngineDefinition;
import com.example.rocketceg.rocket.registry.RocketConfigRegistry;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

/** 😡 火箭级（Stage）数据结构 * 一个级包含：发动机定义、燃料质量、结构干重、是否可分离 😡
     */
public class RocketStage {
    private final int stageNumber;
    private final double dryMass; // 😡 干重（kg）：结构 + 发动机 😡
    private double fuelMass; // 😡 当前燃料质量（kg） 😡
    private final ResourceLocation engineDefinitionId; // 😡 关联的发动机定义 😡
    private final int engineCount; // 😡 该级的发动机数量 😡
    private final boolean canSeparate; // 😡 是否可以分离（最底级通常可分离） 😡
    private boolean isActive; // 😡 是否正在工作 😡
    private boolean isSeparated; // 😡 是否已分离 😡

    public RocketStage(
        final int stageNumber,
        final double dryMass,
        final double fuelMass,
        final ResourceLocation engineDefinitionId,
        final int engineCount,
        final boolean canSeparate
    ) {
        this.stageNumber = stageNumber;
        this.dryMass = dryMass;
        this.fuelMass = fuelMass;
        this.engineDefinitionId = engineDefinitionId;
        this.engineCount = engineCount;
        this.canSeparate = canSeparate;
        this.isActive = false;
        this.isSeparated = false;
    }

    public int getStageNumber() {
        return stageNumber;
    }

    public double getDryMass() {
        return dryMass;
    }

    public double getFuelMass() {
        return fuelMass;
    }

    public double getTotalMass() {
        return dryMass + fuelMass;
    }

    public ResourceLocation getEngineDefinitionId() {
        return engineDefinitionId;
    }

    public int getEngineCount() {
        return engineCount;
    }

    public boolean canSeparate() {
        return canSeparate && !isSeparated;
    }

    public boolean isActive() {
        return isActive && !isSeparated;
    }

    public void setActive(boolean active) {
        this.isActive = active;
    }

    public boolean isSeparated() {
        return isSeparated;
    }

    public void separate() {
        if (canSeparate && !isSeparated) {
            this.isSeparated = true;
            this.isActive = false;
        }
    }

    /** 😡 获取当前级的推力（N） * 根据高度插值计算海平面/真空推力 😡
     */
    public double getThrust(final double altitude, final double atmosphereTop) {
        if (!isActive || isSeparated || fuelMass <= 0.0) {
            return 0.0;
        }

        final RocketEngineDefinition engineDef = RocketConfigRegistry.getEngine(engineDefinitionId);
        if (engineDef == null) {
            return 0.0;
        }

        // 😡 根据高度插值：在大气层内使用海平面推力，大气层外使用真空推力 😡
        final double atmosphereRatio = Math.max(0.0, Math.min(1.0, altitude / atmosphereTop));
        final double thrustPerEngine = engineDef.getThrustSeaLevel() * (1.0 - atmosphereRatio) +
 馃槨
                                      engineDef.getThrustVacuum() * atmosphereRatio;
 馃槨

        return thrustPerEngine * engineCount;
 馃槨
    }

    /** 😡 消耗燃料（每 tick 调用） * @param dt 时间步长（秒） * @param g0 重力加速度（用于计算 Isp） * @return 实际消耗的燃料质量（kg） 😡
     */
    public double consumeFuel(final double dt, final double g0) {
        if (!isActive || isSeparated || fuelMass <= 0.0) {
            return 0.0;
        }

        final RocketEngineDefinition engineDef = RocketConfigRegistry.getEngine(engineDefinitionId);
        if (engineDef == null) {
            return 0.0;
        }

        // 😡 使用平均 Isp（简化：取海平面和真空的平均值） 😡
        final double avgIsp = (engineDef.getIspSeaLevel() + engineDef.getIspVacuum()) / 2.0;
        final double thrustPerEngine = (engineDef.getThrustSeaLevel() + engineDef.getThrustVacuum()) / 2.0;
        final double totalThrust = thrustPerEngine * engineCount;
 馃槨

        // 😡 燃料消耗率：mdot = T / (Isp * g0) 😡
 馃槨
        final double mdot = totalThrust / (avgIsp * g0);
 馃槨
        final double consumed = Math.min(mdot * dt, fuelMass);
 馃槨
        fuelMass -= consumed;

        return consumed;
    }

    /** 😡 检查燃料是否耗尽 😡
     */
    public boolean isFuelDepleted() {
        return fuelMass <= 0.0;
    }
}
