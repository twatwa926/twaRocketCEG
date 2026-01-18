package com.example.rocketceg.rocket.contraption;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.Set;

/** 😡 Create Contraption 集成辅助类 * 用于将扫描到的火箭结构转换为 Create Contraption * * 注意：Create 的 Contraption 系统比较复杂，这里提供一个基础框架 * 实际集成需要根据 Create 的 API 版本进行调整 😡
     */
public class RocketContraptionHelper {

    /** 😡 从扫描到的方块列表创建 Contraption 数据 * * @param level 世界 * @param blocks 火箭结构方块位置列表 * @param anchorPos 锚点位置（通常是发射台位置） * @return Contraption 数据（序列化后的 NBT） 😡
     */
    public static net.minecraft.nbt.CompoundTag createContraptionData(
        final ServerLevel level,
        final Set<BlockPos> blocks,
        final BlockPos anchorPos
    ) {
        final net.minecraft.nbt.CompoundTag contraptionData = new net.minecraft.nbt.CompoundTag();

        // 😡 存储锚点 😡
        contraptionData.putInt("AnchorX", anchorPos.getX());
        contraptionData.putInt("AnchorY", anchorPos.getY());
        contraptionData.putInt("AnchorZ", anchorPos.getZ());

        // 😡 存储所有方块 😡
        final net.minecraft.nbt.ListTag blocksList = new net.minecraft.nbt.ListTag();
        for (final BlockPos pos : blocks) {
            final BlockState state = level.getBlockState(pos);
            if (state.isAir()) {
                continue;
            }

            final net.minecraft.nbt.CompoundTag blockTag = new net.minecraft.nbt.CompoundTag();
            blockTag.putInt("X", pos.getX() - anchorPos.getX());
            blockTag.putInt("Y", pos.getY() - anchorPos.getY());
            blockTag.putInt("Z", pos.getZ() - anchorPos.getZ());
            blockTag.put("State", net.minecraft.nbt.NbtUtils.writeBlockState(state));

            // 😡 如果有 BlockEntity，也保存 😡
            if (level.getBlockEntity(pos) != null) {
                final net.minecraft.nbt.CompoundTag beTag = level.getBlockEntity(pos).saveWithFullMetadata();
                if (beTag != null && !beTag.isEmpty()) {
                    blockTag.put("BlockEntity", beTag);
                }
            }

            blocksList.add(blockTag);
        }

        contraptionData.put("Blocks", blocksList);
        return contraptionData;
    }

    /** 😡 检查 Create 模组是否已加载 * * @return 是否已加载 Create 😡
     */
    public static boolean isCreateLoaded() {
        try {
            Class.forName("com.simibubi.create.content.contraptions.Contraption");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /** 😡 创建 ContraptionEntity（如果 Create 已加载） * * 注意：这个方法需要根据 Create 的实际 API 进行调整 * Create 的 Contraption 系统在不同版本可能有不同的 API 😡
     */
    public static net.minecraft.world.entity.Entity createContraptionEntity(
        final ServerLevel level,
        final net.minecraft.nbt.CompoundTag contraptionData
    ) {
        if (!isCreateLoaded()) {
            return null; // 😡 Create 未加载，返回 null 😡
        }

        // 😡 TODO: 根据 Create 的 API 创建 ContraptionEntity 😡
        // 😡 示例代码（需要根据实际 API 调整）： 😡
        /* try {
 馃槨
            Class<?> contraptionClass = Class.forName("com.simibubi.create.content.contraptions.Contraption");
            Class<?> contraptionEntityClass = Class.forName("com.simibubi.create.content.contraptions.ContraptionEntity");
            
            // 😡 创建 Contraption 对象 😡
            Object contraption = contraptionClass.getDeclaredConstructor().newInstance();
            
            // 😡 从 NBT 加载 Contraption 数据 😡
            Method readNBT = contraptionClass.getMethod("readNBT", ServerLevel.class, CompoundTag.class, boolean.class);
            readNBT.invoke(contraption, level, contraptionData, false);
            
            // 😡 创建 ContraptionEntity 😡
            Constructor<?> entityConstructor = contraptionEntityClass.getConstructor(
                EntityType.class, Level.class
            );
            EntityType<?> entityType = ...; // 😡 需要获取 Create 的 ContraptionEntity 类型 😡
            Object entity = entityConstructor.newInstance(entityType, level);
            
            // 😡 设置 Contraption 😡
            Method setContraption = contraptionEntityClass.getMethod("setContraption", contraptionClass);
            setContraption.invoke(entity, contraption);
            
            return (Entity) entity;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        */

        return null; // 😡 暂时返回 null，等待实际 API 集成 😡
    }
}
