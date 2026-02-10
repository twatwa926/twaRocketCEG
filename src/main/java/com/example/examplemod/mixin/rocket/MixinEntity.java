package com.example.examplemod.mixin.rocket;

import com.example.examplemod.rocket.ship.EntityShipCollisionUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Entity.class)
public abstract class MixinEntity {
    @Redirect(
        method = "move",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;collide(Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/world/phys/Vec3;"
        )
    )
    private Vec3 rocketwa$collideWithShips(Entity self, Vec3 movement) {
        AABB box = self.getBoundingBox();
        Vec3 adjusted = EntityShipCollisionUtils.adjustEntityMovementForShipCollisions(self, movement, box, self.level());
        return ((MixinEntityInvoker) self).rocketwa$invokeCollide(adjusted);
    }
}
