ackage com.example.rocketceg.mixin.server;

import com.example.rocketceg.seamless.SeamlessCore;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.util.ITeleporter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** 😡 服务端玩家 Mixin - 完全重构维度切换逻辑 * * 这个 Mixin 完全接管维度切换，实现真正的无缝传送 😡
     */
@Mixin(ServerPlayer.class)
public class MixinServerPlayer {

    /** 😡 完全接管维度切换逻辑 😡
     */
    @Inject(
        method = "changeDimension(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraftforge/common/util/ITeleporter;)Lnet/minecraft/world/entity/Entity;",
        at = @At("HEAD"),
        cancellable = true,
        remap = false
    )
    private void rocketceg$interceptDimensionChange(ServerLevel destination, ITeleporter teleporter, 
                                                  CallbackInfoReturnable<net.minecraft.world.entity.Entity> cir) {
        ServerPlayer player = (ServerPlayer) (Object) this;
        ServerLevel source = player.serverLevel();
        
        // 😡 检查是否应该使用无缝传送 😡
        if (SeamlessCore.shouldUseSeamlessTeleport(source.dimension(), destination.dimension())) {
            
            // 😡 获取传送位置 😡
            net.minecraft.world.level.portal.PortalInfo portalInfo = teleporter.getPortalInfo(
                player, destination, 
                (level) -> new net.minecraft.world.level.portal.PortalInfo(
                    player.position(), 
                    net.minecraft.world.phys.Vec3.ZERO, 
                    player.getYRot(), 
                    player.getXRot()
                )
            );
            
            if (portalInfo != null) {
                // 😡 使用无缝传送系统 😡
                SeamlessCore.getInstance().startSeamlessTeleport(
                    player, 
                    destination.dimension(), 
                    portalInfo.pos
                );
                
                // 😡 返回玩家自身，表示传送成功 😡
                cir.setReturnValue(player);
                
                com.example.rocketceg.RocketCEGMod.LOGGER.info(
                    "[SeamlessCore] 使用无缝传送: {} -> {}", 
                    source.dimension().location(), 
                    destination.dimension().location()
                );
            }
        }
        // 😡 对于非 RocketCEG 维度，让原始逻辑继续执行 😡
    }
}