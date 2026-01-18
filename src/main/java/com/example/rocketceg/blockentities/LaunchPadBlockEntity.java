package com.example.rocketceg.blockentities;

import com.example.rocketceg.blocks.LaunchPadBlock;
import com.example.rocketceg.registry.RocketCEGBlocks;
import com.example.rocketceg.rocket.blueprint.MultiStageRocketBlueprint;
import com.example.rocketceg.rocket.blueprint.RocketBlueprint;
import com.example.rocketceg.rocket.config.RocketEngineDefinition;
import com.example.rocketceg.rocket.config.RocketPartConfig;
import com.example.rocketceg.rocket.contraption.RocketContraptionHelper;
import com.example.rocketceg.rocket.entity.RocketEntity;
import com.example.rocketceg.rocket.registry.RocketConfigRegistry;
import com.example.rocketceg.rocket.stage.RocketStage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** 😡 发射台 BlockEntity - 管理火箭的竖立、燃料加注和发射流程。 * * 阶段 1：实现一个简单的结构扫描： * - 上方一定高度范围内统计玩家用 RocketCEG 方块搭建的“火箭”结构 * - 根据方块数量粗略估算干重、燃料质量与总推力 * - 生成 {@link RocketEntity} 并清空结构区域 😡
     */
public class LaunchPadBlockEntity extends BlockEntity {

    /** 😡 扫描火箭的最大高度（从发射台上方开始，以方块为单位） 😡
     */
 馃槨 馃槨
    private static final int SCAN_HEIGHT = 64;

    /** 😡 水平扫描半径（方块），1 = 3x3，2 = 5x5 😡
     */
 馃槨 馃槨
    private static final int SCAN_RADIUS = 1;

    public LaunchPadBlockEntity(BlockPos pos, BlockState state) {
        super(RocketCEGBlockEntities.LAUNCH_PAD_BE.get(), pos, state);
    }

    public void tick() {
        if (level == null || level.isClientSide) {
            return;
        }
        // 😡 暂无自动逻辑，一切由玩家交互触发 😡
    }

    /** 😡 玩家右键发射台时调用，尝试扫描并发射火箭。 😡
     */
    public void tryLaunch(final ServerPlayer player) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        final ScanResult result = scanRocketStructure(serverLevel);
        if (!result.valid) {
            player.displayClientMessage(Component.literal("[RocketCEG] 未检测到有效火箭结构（需要至少一个 cockpit、一个 engine、一个 fuel tank）"), false);
            return;
        }

        // 😡 使用配置数据计算干重、燃料质量和推力 😡
        double dryMass = 0.0;
        double fuelMass = 0.0;
        double totalThrust = 0.0;

        // 😡 遍历所有扫描到的方块，从配置中获取物理参数 😡
        for (final BlockPos pos : result.scannedBlocks) {
            final BlockState state = serverLevel.getBlockState(pos);
            final Block block = state.getBlock();
            final ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(block);
            final RocketPartConfig partConfig = RocketConfigRegistry.getPartConfig(blockId);

            if (partConfig != null) {
                dryMass += partConfig.getDryMass();
                fuelMass += partConfig.getFuelCapacity();

                // 😡 如果是发动机，从关联的发动机定义获取推力 😡
                if (partConfig.isEngine() && partConfig.getEngineDefinitionId() != null) {
                    final RocketEngineDefinition engineDef = RocketConfigRegistry.getEngine(partConfig.getEngineDefinitionId());
                    if (engineDef != null) {
                        // 😡 使用海平面推力（后续可以根据高度插值） 😡
                        totalThrust += engineDef.getThrustSeaLevel();
                    }
                }
            }
        }

        final RocketBlueprint blueprint = new RocketBlueprint("player_rocket", dryMass, fuelMass);

        if (totalThrust <= 0.0) {
            player.displayClientMessage(Component.literal("[RocketCEG] 火箭缺少发动机，无法发射"), false);
            return;
        }

        // 😡 在结构顶部附近生成火箭实体 😡
        final double spawnX = worldPosition.getX() + 0.5;
        final double spawnZ = worldPosition.getZ() + 0.5;
        final double spawnY = result.topY + 1.0;

        final RocketEntity rocket = RocketEntity.createFromBlueprint(serverLevel, blueprint, totalThrust);
        rocket.moveTo(spawnX, spawnY, spawnZ, player.getYRot(), player.getXRot());

        // 😡 清空火箭结构方块 😡
        clearRocketStructure(serverLevel, result);

        serverLevel.addFreshEntity(rocket);
        player.displayClientMessage(Component.literal("[RocketCEG] 火箭发射！(干重: " + (int) dryMass + " kg, 燃料: " + (int) fuelMass + " kg, 推力: " + (int) totalThrust + " N)"), false);
    }

    /** 😡 扫描发射台上方的火箭结构，收集所有火箭部件方块。 😡
     */
    private ScanResult scanRocketStructure(final ServerLevel level) {
        final ScanResult result = new ScanResult();

        final BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        final int baseX = worldPosition.getX();
        final int baseZ = worldPosition.getZ();

        int topY = worldPosition.getY();

        for (int dy = 1; dy <= SCAN_HEIGHT; dy++) {
            final int y = worldPosition.getY() + dy;

            for (int dx = -SCAN_RADIUS; dx <= SCAN_RADIUS; dx++) {
                for (int dz = -SCAN_RADIUS; dz <= SCAN_RADIUS; dz++) {
                    cursor.set(baseX + dx, y, baseZ + dz);
                    final BlockState state = level.getBlockState(cursor);
                    final Block block = state.getBlock();
                    final ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(block);
                    final RocketPartConfig partConfig = RocketConfigRegistry.getPartConfig(blockId);

                    if (partConfig != null) {
                        result.scannedBlocks.add(cursor.immutable());
                        topY = Math.max(topY, y);

                        // 😡 统计各类部件数量（用于验证结构完整性） 😡
                        switch (partConfig.getPartType()) {
                            case FRAME -> result.frameCount++;
                            case COCKPIT -> result.cockpitCount++;
                            case ENGINE -> result.engineCount++;
                            case FUEL_TANK -> result.tankCount++;
                            case ENGINE_MOUNT -> result.engineMountCount++;
                            case AVIONICS -> result.avionicsCount++;
                            case INTERSTAGE -> result.interstageCount++;
                        }
                    }
                }
            }
        }

        result.topY = topY;
        result.valid = result.cockpitCount > 0 && result.engineCount > 0 && result.tankCount > 0;
        return result;
    }

    /** 😡 将扫描到的火箭结构方块清空。 😡
     */
    private void clearRocketStructure(final ServerLevel level, final ScanResult result) {
        for (final BlockPos pos : result.scannedBlocks) {
            level.removeBlock(pos, false);
        }
    }

    /** 😡 从扫描结果创建多级火箭蓝图 * 简化逻辑：根据级间段（INTERSTAGE）自动分级 😡
     */
    private MultiStageRocketBlueprint createMultiStageBlueprint(final ScanResult result, final ServerLevel level) {
        // 😡 如果没有级间段，返回 null（使用单级火箭） 😡
        if (result.interstageCount == 0) {
            return null;
        }

        // 😡 简化：将所有方块分为两级 😡
        // 😡 第一级：从底部到第一个级间段 😡
        // 😡 第二级：从第一个级间段到顶部 😡
        final java.util.List<RocketStage> stages = new java.util.ArrayList<>();

        // 😡 第一级：包含所有发动机和部分燃料箱 😡
        final double stage1DryMass = result.frameCount * 0.5 * 500.0 + // 😡 一半结构 😡
 馃槨
                                     result.engineCount * 3000.0 +
 馃槨
                                     result.engineMountCount * 1500.0;
 馃槨
        final double stage1FuelMass = result.tankCount * 0.6 * 20000.0; // 😡 60% 燃料 😡
 馃槨
        final ResourceLocation engineId = new ResourceLocation("rocketceg", "merlin_1d");
        final int engineCount = result.engineCount;

        stages.add(new RocketStage(
            1,
            stage1DryMass,
            stage1FuelMass,
            engineId,
            engineCount,
            true // 😡 可分离 😡
        ));

        // 😡 第二级：包含指令舱、航电和剩余燃料 😡
        final double stage2DryMass = result.frameCount * 0.5 * 500.0 + // 😡 另一半结构 😡
 馃槨
                                     result.cockpitCount * 2000.0 +
 馃槨
                                     result.avionicsCount * 800.0;
 馃槨
        final double stage2FuelMass = result.tankCount * 0.4 * 20000.0; // 😡 40% 燃料 😡
 馃槨

        stages.add(new RocketStage(
            2,
            stage2DryMass,
            stage2FuelMass,
            engineId,
            0, // 😡 第二级暂时无发动机（可扩展） 😡
            false // 😡 不可分离 😡
        ));

        return new MultiStageRocketBlueprint("player_multi_stage_rocket", stages);
    }

    private static final class ScanResult {
        final java.util.List<BlockPos> scannedBlocks = new java.util.ArrayList<>();
        int frameCount;
        int cockpitCount;
        int engineCount;
        int tankCount;
        int engineMountCount;
        int avionicsCount;
        int interstageCount;
        int topY;
        boolean valid;
    }
}
