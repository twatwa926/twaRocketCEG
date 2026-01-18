package com.example.rocketceg.items;

import com.example.rocketceg.RocketCEGMod;
import com.example.rocketceg.registry.RocketCEGBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

/** 😡 RocketCEG 模组的创造模式物品栏标签页 😡
     */
public class RocketCEGTab {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = 
        DeferredRegister.create(Registries.CREATIVE_MODE_TAB, RocketCEGMod.MOD_ID);

    /** 😡 RocketCEG 主标签页 - 包含所有火箭相关的方块和物品 😡
     */
    public static final RegistryObject<CreativeModeTab> ROCKETCEG_TAB = CREATIVE_MODE_TABS.register(
        "rocketceg_tab",
        () -> CreativeModeTab.builder()
            .icon(() -> new ItemStack(RocketCEGBlocks.ROCKET_ENGINE.get()))
            .title(Component.translatable("itemGroup.rocketceg.rocketceg_tab"))
            .displayItems((parameters, output) -> {
                // 😡 核心火箭方块 😡
                output.accept(RocketCEGBlocks.LAUNCH_PAD.get());
                output.accept(RocketCEGBlocks.ROCKET_COCKPIT.get());
                output.accept(RocketCEGBlocks.ROCKET_ENGINE.get());
                output.accept(RocketCEGBlocks.ROCKET_FUEL_TANK.get());
                output.accept(RocketCEGBlocks.ROCKET_FRAME.get());
                
                // 😡 结构组件 😡
                output.accept(RocketCEGBlocks.ENGINE_MOUNT.get());
                output.accept(RocketCEGBlocks.AVIONICS_BAY.get());
                output.accept(RocketCEGBlocks.INTERSTAGE.get());
                
                // 😡 物品 😡
                output.accept(RocketCEGItems.ROCKET_BLUEPRINT.get());
                output.accept(RocketCEGItems.ROCKET_NAVIGATION_COMPUTER.get());
                
                // 😡 行星占位符方块（可选，如果不想显示可以注释掉） 😡
                output.accept(RocketCEGBlocks.EARTH_SURFACE_BLOCK.get());
                output.accept(RocketCEGBlocks.MOON_SURFACE_BLOCK.get());
                output.accept(RocketCEGBlocks.MARS_SURFACE_BLOCK.get());
                output.accept(RocketCEGBlocks.VENUS_SURFACE_BLOCK.get());
                output.accept(RocketCEGBlocks.MERCURY_SURFACE_BLOCK.get());
                output.accept(RocketCEGBlocks.JUPITER_SURFACE_BLOCK.get());
                output.accept(RocketCEGBlocks.SATURN_SURFACE_BLOCK.get());
                output.accept(RocketCEGBlocks.URANUS_SURFACE_BLOCK.get());
                output.accept(RocketCEGBlocks.NEPTUNE_SURFACE_BLOCK.get());
                output.accept(RocketCEGBlocks.PLUTO_SURFACE_BLOCK.get());
            })
            .build()
    );

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
