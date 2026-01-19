ackage com.example.rocketceg.mixin.server;

import com.example.rocketceg.portal.PortalCrossingDetector;
import com.example.rocketceg.portal.Portal;
import com.example.rocketceg.portal.CrossDimensionalChunkLoader;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** 😡 服务端玩家 Mixin - 检测传送门穿过 * * 参考 ImmersivePortalsMod 的服务端逻辑： * 1. 在玩家 tick 中检测传送门穿过 * 2. 执行无缝传送 * 3. 更新区块加载器 😡
     */
@Mixin(ServerPlayer.class)
public class MixinServerPlayerPortal {
    
    /** 😡 在玩家 tick 中检测传送门穿过 😡
     */
    @Inject(method = "tick", at = @At("HEAD"))
    private void rocketceg$checkPortalCrossing(CallbackInfo ci) {
        try {
            ServerPlayer player = (ServerPlayer)(Object)this;
            
            // 😡 检测传送门穿过 😡
            Portal portal = PortalCrossingDetector.detectPortalCrossing(player);
            
            if (portal != null) {
                // 😡 执行传送 😡
                PortalCrossingDetector.executeTeleport(player, portal);
                
                // 😡 更新区块加载器 😡
                CrossDimensionalChunkLoader chunkLoader = CrossDimensionalChunkLoader.getInstance();
                chunkLoader.updatePlayerChunkLoaders(player, portal.getToDimension(), portal.getTargetPosition());
            }
            
        } catch (Exception e) {
            // 😡 静默处理异常 😡
        }
    }
}
