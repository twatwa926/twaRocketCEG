ackage com.example.rocketceg.mixin.client;

import com.example.rocketceg.seamless.SeamlessCore;
import net.minecraft.client.gui.Gui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** 😡 GUI Mixin * * 阻止所有可能干扰无缝传送的 GUI 元素 😡
     */
@Mixin(Gui.class)
public class MixinGui {
    
    /** 😡 阻止显示传送相关的 GUI 元素 😡
     */
    @Inject(method = "renderPortalOverlay", at = @At("HEAD"), cancellable = true)
    private void rocketceg$blockPortalOverlay(CallbackInfo ci) {
        if (SeamlessCore.getInstance().isSeamlessTeleporting()) {
            // 😡 阻止传送门覆盖层 😡
            ci.cancel();
        }
    }
}