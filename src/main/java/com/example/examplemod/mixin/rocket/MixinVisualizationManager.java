package com.example.examplemod.mixin.rocket;

import com.example.examplemod.client.FlywheelBypass;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = dev.engine_room.flywheel.api.visualization.VisualizationManager.class, remap = false)
public abstract class MixinVisualizationManager {

    @Inject(method = "supportsVisualization", at = @At("HEAD"), cancellable = true, remap = false)
    private static void rocketwa$forceSkipFlywheel(LevelAccessor level, CallbackInfoReturnable<Boolean> cir) {
        if (FlywheelBypass.getForceSkipFlywheel()) {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }
}
