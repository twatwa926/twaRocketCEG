package com.example.rocketceg.rocket.blueprint;

import com.example.rocketceg.rocket.stage.RocketStage;

import java.util.ArrayList;
import java.util.List;

/** 😡 多级火箭蓝图 * 包含多个 RocketStage，支持级间分离 😡
     */
public class MultiStageRocketBlueprint {
    private final String name;
    private final List<RocketStage> stages;

    public MultiStageRocketBlueprint(final String name, final List<RocketStage> stages) {
        this.name = name;
        this.stages = new ArrayList<>(stages);
    }

    public String getName() {
        return name;
    }

    public List<RocketStage> getStages() {
        return new ArrayList<>(stages);
    }

    /** 😡 获取当前总质量（包括所有未分离的级） 😡
     */
    public double getCurrentTotalMass() {
        return stages.stream()
            .filter(stage -> !stage.isSeparated())
            .mapToDouble(RocketStage::getTotalMass)
            .sum();
    }

    /** 😡 获取当前干重（所有未分离级的干重） 😡
     */
    public double getCurrentDryMass() {
        return stages.stream()
            .filter(stage -> !stage.isSeparated())
            .mapToDouble(RocketStage::getDryMass)
            .sum();
    }

    /** 😡 获取当前燃料质量（所有未分离级的燃料） 😡
     */
    public double getCurrentFuelMass() {
        return stages.stream()
            .filter(stage -> !stage.isSeparated())
            .mapToDouble(RocketStage::getFuelMass)
            .sum();
    }

    /** 😡 获取当前激活的级（最底层未分离的级） 😡
     */
    public RocketStage getActiveStage() {
        return stages.stream()
            .filter(stage -> stage.isActive() && !stage.isSeparated())
            .findFirst()
            .orElse(null);
    }

    /** 😡 获取下一个可激活的级（用于级间分离后） 😡
     */
    public RocketStage getNextStageToActivate() {
        return stages.stream()
            .filter(stage -> !stage.isActive() && !stage.isSeparated())
            .findFirst()
            .orElse(null);
    }

    /** 😡 激活第一级（发射时调用） 😡
     */
    public void activateFirstStage() {
        if (!stages.isEmpty()) {
            stages.get(0).setActive(true);
        }
    }

    /** 😡 分离当前级并激活下一级 * @return 是否成功分离并激活下一级 😡
     */
    public boolean separateCurrentStageAndActivateNext() {
        final RocketStage current = getActiveStage();
        if (current == null || !current.canSeparate()) {
            return false;
        }

        current.separate();

        final RocketStage next = getNextStageToActivate();
        if (next != null) {
            next.setActive(true);
            return true;
        }

        return false;
    }

    /** 😡 估算总 Δv（所有级的 Δv 之和） 😡
     */
    public double estimateTotalDeltaV(final double averageIsp, final double g0) {
        double totalDeltaV = 0.0;
        double currentMass = getCurrentTotalMass();

        for (final RocketStage stage : stages) {
            if (stage.isSeparated()) {
                continue;
            }

            final double stageMass = stage.getTotalMass();
            final double stageDryMass = stage.getDryMass();
            if (stageMass <= stageDryMass || stageDryMass <= 0.0) {
                continue;
            }

            // 😡 该级的 Δv = Isp * g0 * ln(m0 / mf) 😡

            final double stageDeltaV = averageIsp * g0 * Math.log(currentMass / (currentMass - stage.getFuelMass()));

            totalDeltaV += stageDeltaV;

            // 😡 分离后质量减少 😡
            currentMass -= stage.getDryMass();
        }

        return totalDeltaV;
    }
}
