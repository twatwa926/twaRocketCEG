package com.example.examplemod.mixin.rocket;

import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import net.minecraft.world.entity.Entity;

@Mixin(Entity.class)
public interface MixinEntityInvoker {
    @Invoker("collide")
    Vec3 rocketwa$invokeCollide(Vec3 movement);
}
