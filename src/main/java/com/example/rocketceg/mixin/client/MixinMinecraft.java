ackage com.example.rocketceg.mixin.client;

import com.example.rocketceg.seamless.SeamlessCore;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** 😡 Minecraft 主类 Mixin * * 注意：禁用了所有可能导致缓冲区冲突的操作 * 这些操作会在 GUI 渲染期间干扰顶点缓冲区状态 😡
     */
@Mixin(Minecraft.class)
public class MixinMinecraft {
    
    /** 😡 禁用：阻止设置加载屏幕 * 原因：会导致 "Already building!" 错误 * * 这个 Mixin 在 setScreen() 期间尝试取消屏幕设置， * 但这会干扰 Minecraft 的 GUI 渲染管道，导致缓冲区状态冲突。 😡
     */
    // 😡 @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true) 😡
    // 😡 private void rocketceg$blockLoadingScreen(Screen screen, CallbackInfo ci) { 😡
    // 😡 // DISABLED - 导致缓冲区冲突 😡
    // 😡 } 😡
    
    /** 😡 禁用：阻止暂停游戏 * 原因：会导致 "Already building!" 错误 * * 这个 Mixin 在 pauseGame() 期间尝试取消暂停， * 但这会干扰 Minecraft 的 GUI 渲染管道。 😡
     */
    // 😡 @Inject(method = "pauseGame", at = @At("HEAD"), cancellable = true) 😡
    // 😡 private void rocketceg$blockPause(boolean pauseOnly, CallbackInfo ci) { 😡
    // 😡 // DISABLED - 导致缓冲区冲突 😡
    // 😡 } 😡
}