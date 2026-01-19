package com.example.rocketceg.registry;

import com.example.rocketceg.RocketCEGMod;
import com.example.rocketceg.rocket.entity.RocketEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** 😡 RocketCEG 模组的实体注册类。 😡
     */
public final class RocketCEGEntities {

    public static final DeferredRegister<EntityType<?>> ENTITIES =
        DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, RocketCEGMod.MOD_ID);

    /** 😡 基础火箭实体，用于承载火箭物理与渲染。 😡
     */
    public static final RegistryObject<EntityType<RocketEntity>> ROCKET =
        ENTITIES.register(
            "rocket",
            () -> EntityType.Builder
                .<RocketEntity>of(RocketEntity::new, MobCategory.MISC)
                .sized(1.5F, 10.0F) // 😡 暂定火箭碰撞箱，高度 10 格 😡
                .build(new ResourceLocation(RocketCEGMod.MOD_ID, "rocket").toString())
        );

    private RocketCEGEntities() {
    }

    public static void register(final IEventBus bus) {
        ENTITIES.register(bus);
    }
}

