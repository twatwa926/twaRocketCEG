package com.example.rocketceg.rocket.config;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

/** 😡 火箭部件配置 - 定义每个方块类型的物理参数（质量、燃料容量等） 😡
     */
public class RocketPartConfig {
    private final ResourceLocation blockId;
    private final double dryMass; // 😡 干重（kg） 😡
    private final double fuelCapacity; // 😡 燃料容量（kg），如果该部件不储存燃料则为 0 😡
    private final ResourceLocation engineDefinitionId; // 😡 如果是发动机，关联的发动机定义 ID 😡
    private final PartType partType;

    public enum PartType {
        FRAME,          // 😡 结构段 😡
        COCKPIT,        // 😡 指令舱 😡
        ENGINE,         // 😡 发动机 😡
        FUEL_TANK,      // 😡 燃料箱 😡
        ENGINE_MOUNT,   // 😡 发动机安装架 😡
        AVIONICS,       // 😡 航电舱 😡
        INTERSTAGE      // 😡 级间段 😡
    }

    public RocketPartConfig(
        final ResourceLocation blockId,
        final double dryMass,
        final double fuelCapacity,
        final ResourceLocation engineDefinitionId,
        final PartType partType
    ) {
        this.blockId = blockId;
        this.dryMass = dryMass;
        this.fuelCapacity = fuelCapacity;
        this.engineDefinitionId = engineDefinitionId;
        this.partType = partType;
    }

    public ResourceLocation getBlockId() {
        return blockId;
    }

    public double getDryMass() {
        return dryMass;
    }

    public double getFuelCapacity() {
        return fuelCapacity;
    }

    public ResourceLocation getEngineDefinitionId() {
        return engineDefinitionId;
    }

    public PartType getPartType() {
        return partType;
    }

    public boolean isEngine() {
        return partType == PartType.ENGINE;
    }

    public boolean isFuelTank() {
        return partType == PartType.FUEL_TANK;
    }
}
