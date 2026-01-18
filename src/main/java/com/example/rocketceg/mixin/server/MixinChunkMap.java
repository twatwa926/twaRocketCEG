package com.example.rocketceg.mixin.server;

import com.example.rocketceg.dimension.seamless.IntelligentChunkLoader;
import com.example.rocketceg.seamless.SeamlessCore;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** 😡 区块映射 Mixin - 集成智能区块加载器 * * 参考 ImmersivePortalsMod 的区块加载技术： * 1. 跨维度区块加载 - 突破原版单维度限制 * 2. 智能加载策略 - 根据玩家位置动态加载 * 3. 性能优化 - 智能内存管理 * 4. 同步机制 - 确保区块数据同步到客户端 * * 这是实现真正跨维度区块加载的核心组件 😡
     */
@Mixin(ChunkMap.class)
public class MixinChunkMap {
    
    /** 😡 在玩家进入追踪范围时，为无缝传送设置跨维度区块加载 😡
     */
    @Inject(method = "addEntity", at = @At("HEAD"))
    private void rocketceg$onAddEntity(net.minecraft.world.entity.Entity entity, CallbackInfo ci) {
        try {
            if (entity instanceof ServerPlayer player) {
                // 😡 如果正在进行无缝传送，设置跨维度区块加载 😡
                SeamlessCore seamlessCore = SeamlessCore.getInstance();
                if (seamlessCore.isSeamlessTeleporting()) {
                    ResourceKey<Level> targetDimension = seamlessCore.getPendingDimension();
                    Vec3 targetPosition = seamlessCore.getPendingPosition();
                    
                    if (targetDimension != null && targetPosition != null) {
                        // 😡 为目标维度添加区块加载器 😡
                        IntelligentChunkLoader chunkLoader = IntelligentChunkLoader.getInstance();
                        chunkLoader.addChunkLoaderForPlayer(player, targetDimension, targetPosition, 8);
                    }
                }
            }
            
        } catch (Exception e) {
            // 😡 静默处理异常 😡
        }
    }
    
    /** 😡 在玩家离开追踪范围时，清理跨维度区块加载器 😡
     */
    @Inject(method = "removeEntity", at = @At("HEAD"))
    private void rocketceg$onRemoveEntity(net.minecraft.world.entity.Entity entity, CallbackInfo ci) {
        try {
            if (entity instanceof ServerPlayer player) {
                // 😡 清理玩家的跨维度区块加载器 😡
                IntelligentChunkLoader chunkLoader = IntelligentChunkLoader.getInstance();
                chunkLoader.cleanupPlayerChunkLoaders(player);
            }
            
        } catch (Exception e) {
            // 😡 静默处理异常 😡
        }
    }
}