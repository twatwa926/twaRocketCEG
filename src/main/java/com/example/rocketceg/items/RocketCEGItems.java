package com.example.rocketceg.items;

import com.example.rocketceg.RocketCEGMod;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** 😡 RocketCEG 模组的物品注册类。 😡
     */
public class RocketCEGItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, RocketCEGMod.MOD_ID);

    /** 😡 火箭设计蓝图 - 用于保存和加载火箭设计 😡
     */
    public static final RegistryObject<Item> ROCKET_BLUEPRINT = ITEMS.register(
        "rocket_blueprint",
        () -> new Item(new Item.Properties().stacksTo(1))
    );

    /** 😡 火箭导航计算机 - 用于设定轨道和目标 😡
     */
    public static final RegistryObject<Item> ROCKET_NAVIGATION_COMPUTER = ITEMS.register(
        "rocket_navigation_computer",
        () -> new Item(new Item.Properties().stacksTo(1))
    );

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
