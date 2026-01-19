ackage com.example.rocketceg.mixin.server;

import com.example.rocketceg.seamless.SeamlessCore;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** 😡 玩家列表 Mixin * * 阻止发送 respawn 包 😡
     */
@Mixin(PlayerList.class)
public class MixinPlayerList {
    
    /** 😡 阻止发送 respawn 包 😡
     */
    @Inject(method = "sendLevelInfo", at = @At("HEAD"), cancellable = true)
    private void rocketceg$blockRespawnPacket(ServerPlayer player, ServerLevel level, CallbackInfo ci) {
        if (SeamlessCore.getInstance().shouldBlockRespawnPacket()) {
            // 😡 完全阻止发送 respawn 相关的包 😡
            ci.cancel();
        }
    }
}