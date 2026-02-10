package com.example.examplemod.rocket;

import com.example.examplemod.ExampleMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;

public class RocketStructureDetector {
    private static final int MAX_BLOCKS = 512;
    private static final int MAX_RADIUS = 12;
    private static final String CREATE_MODID = "create";

    public RocketStructure scan(Level level, BlockPos origin) {
        Set<BlockPos> visited = new HashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        queue.add(origin);
        visited.add(origin);

        int avionics = 0;
        int computers = 0;
        int tanks = 0;
        int engines = 0;
        int separators = 0;
        Set<BlockPos> rocketBlocks = new HashSet<>();

        while (!queue.isEmpty() && visited.size() <= MAX_BLOCKS) {
            BlockPos pos = queue.poll();
            Block block = level.getBlockState(pos).getBlock();
            if (block == net.minecraft.world.level.block.Blocks.AIR) {
                continue;
            }
            rocketBlocks.add(pos);

            String namespace = BuiltInRegistries.BLOCK.getKey(block).getNamespace();
            if (!ExampleMod.MODID.equals(namespace)) {
                continue;
            }
            if (block == ExampleMod.AVIONICS_BAY.get()) {
                avionics++;
            } else if (block == ExampleMod.CONTROL_COMPUTER.get()) {
                computers++;
            } else if (block == ExampleMod.FUEL_TANK.get()) {
                tanks++;
            } else if (block == ExampleMod.FLOATER.get()) {

            } else if (block == ExampleMod.ENGINE_MODULE.get()
                    || block == ExampleMod.ENGINE_MODULE_LV.get()
                    || block == ExampleMod.ENGINE_MODULE_MV.get()
                    || block == ExampleMod.ENGINE_MODULE_HV.get()) {
                engines++;
            } else if (block == ExampleMod.STAGE_SEPARATOR.get()) {
                separators++;
            }

            for (Direction direction : Direction.values()) {
                BlockPos next = pos.relative(direction);
                if (!visited.contains(next) && withinRadius(origin, next)) {
                    visited.add(next);
                    queue.add(next);
                }
            }
        }

        return new RocketStructure(avionics, computers, tanks, engines, separators, origin, rocketBlocks);
    }

    private boolean withinRadius(BlockPos origin, BlockPos target) {
        return Math.abs(target.getX() - origin.getX()) <= MAX_RADIUS
                && Math.abs(target.getY() - origin.getY()) <= MAX_RADIUS
                && Math.abs(target.getZ() - origin.getZ()) <= MAX_RADIUS;
    }
}
