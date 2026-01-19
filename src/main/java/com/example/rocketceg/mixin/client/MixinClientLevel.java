ackage com.example.rocketceg.mixin.client;

import com.example.rocketceg.seamless.SeamlessCore;
import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** 😡 客户端级别 Mixin * * 处理客户端维度的无缝切换 😡
     */
@Mixin(ClientLevel.class)
public class MixinClientLevel {
    
    /** 😡 阻止客户端级别的清理操作 😡
     */
    @Inject(method = "disconnect", at = @At("HEAD"), cancellable = true)
    private void rocketceg$blockDisconnect(CallbackInfo ci) {
        if (SeamlessCore.getInstance().isSeamlessTeleporting()) {
            // 😡 在无缝传送过程中不允许断开连接 😡
            ci.cancel();
        }
    }
}