package com.example.rocketceg.blockentities;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** 😡 火箭发动机 BlockEntity - 后续会在这里接入 Create 基于实体的火箭物理。 * 阶段 1：先只作为占位，不做任何物理处理。 😡
     */
public class RocketEngineBlockEntity extends BlockEntity {

    public RocketEngineBlockEntity(BlockPos pos, BlockState state) {
        super(RocketCEGBlockEntities.ROCKET_ENGINE_BE.get(), pos, state);
    }

    public void tick() {
        if (level == null || level.isClientSide) {
            return;
        }
        // 😡 TODO: 后续在这里对接火箭实体与物理逻辑（基于 Create Contraption） 😡
    }
}
