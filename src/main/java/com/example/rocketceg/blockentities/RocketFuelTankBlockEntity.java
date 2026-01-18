package com.example.rocketceg.blockentities;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;

/** 😡 火箭燃料箱 BlockEntity - 储存 GTM 液体燃料 😡
     */
public class RocketFuelTankBlockEntity extends BlockEntity implements IFluidHandler {

    private final FluidTank fluidTank;
    private static final int CAPACITY = 16000; // 😡 16 buckets = 16000 mB 😡

    public RocketFuelTankBlockEntity(BlockPos pos, BlockState state) {
        super(RocketCEGBlockEntities.ROCKET_FUEL_TANK_BE.get(), pos, state);
        this.fluidTank = new FluidTank(CAPACITY);
    }

    public void tick() {
        if (level == null || level.isClientSide) {
            return;
        }

        // 😡 TODO: 与相邻的燃料箱或发动机进行流体交互 😡
        // 😡 TODO: 通过 Create 的管道系统接收燃料 😡
    }

    // 😡 IFluidHandler 实现 😡
    @Override
    public int getTanks() {
        return 1;
    }

    @Override
    public FluidStack getFluidInTank(int tank) {
        return fluidTank.getFluid();
    }

    @Override
    public int getTankCapacity(int tank) {
        return CAPACITY;
    }

    @Override
    public boolean isFluidValid(int tank, FluidStack stack) {
        // 😡 TODO: 检查是否为有效的火箭燃料（GTM 液体） 😡
        return true;
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        return fluidTank.fill(resource, action);
    }

    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        return fluidTank.drain(resource, action);
    }

    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        return fluidTank.drain(maxDrain, action);
    }

    public FluidTank getFluidTank() {
        return fluidTank;
    }
}
