ackage com.example.rocketceg.mixin.client;

import com.example.rocketceg.seamless.SeamlessCore;
import net.minecraft.client.gui.screens.LoadingOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** 😡 阻止加载覆盖层显示 * * 这个 Mixin 完全阻止加载屏幕的显示， * 实现真正的无缝体验 😡
     */
@Mixin(LoadingOverlay.class)
public class MixinLoadingOverlay {
    
    /** 😡 阻止加载覆盖层渲染 😡
     */
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void rocketceg$blockLoadingOverlay(CallbackInfo ci) {
        if (SeamlessCore.getInstance().shouldBlockLoadingScreen()) {
            // 😡 完全阻止加载屏幕渲染 😡
            ci.cancel();
        }
    }
}