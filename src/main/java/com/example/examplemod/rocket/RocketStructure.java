package com.example.examplemod.rocket;

import net.minecraft.core.BlockPos;

import java.util.Set;

public class RocketStructure {
    private final int avionicsBays;
    private final int controlComputers;
    private final int fuelTanks;
    private final int engineModules;
    private final int stageSeparators;
    private final BlockPos origin;
    private final Set<BlockPos> blocks;

    public RocketStructure(int avionicsBays, int controlComputers, int fuelTanks, int engineModules,
                           int stageSeparators, BlockPos origin, Set<BlockPos> blocks) {
        this.avionicsBays = avionicsBays;
        this.controlComputers = controlComputers;
        this.fuelTanks = fuelTanks;
        this.engineModules = engineModules;
        this.stageSeparators = stageSeparators;
        this.origin = origin;
        this.blocks = blocks;
    }

    public int getAvionicsBays() {
        return avionicsBays;
    }

    public int getControlComputers() {
        return controlComputers;
    }

    public int getFuelTanks() {
        return fuelTanks;
    }

    public int getEngineModules() {
        return engineModules;
    }

    public int getStageSeparators() {
        return stageSeparators;
    }

    public BlockPos getOrigin() {
        return origin;
    }

    public Set<BlockPos> getBlocks() {
        return blocks;
    }

    public int getStageCount() {
        return Math.max(1, stageSeparators + 1);
    }

    public boolean isValid() {
        return controlComputers > 0 && avionicsBays > 0 && engineModules > 0 && fuelTanks > 0;
    }
}
