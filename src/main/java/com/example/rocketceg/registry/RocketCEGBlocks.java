package com.example.rocketceg.registry;

import com.example.rocketceg.RocketCEGMod;
import com.example.rocketceg.blocks.LaunchPadBlock;
import com.example.rocketceg.blocks.RocketEngineBlock;
import com.example.rocketceg.blocks.RocketFuelTankBlock;
import com.example.rocketceg.items.RocketCEGItems;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

/** 😡 RocketCEG 模组的方块注册类。 * 所有火箭相关的方块都在这里注册。 😡
     */
public class RocketCEGBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, RocketCEGMod.MOD_ID);

    /** 😡 发射台方块 - 用于竖立和发射火箭 😡
     */
    public static final RegistryObject<Block> LAUNCH_PAD = registerBlock(
        "launch_pad",
        () -> new LaunchPadBlock(
            BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)
                .mapColor(MapColor.METAL)
                .sound(SoundType.ANVIL)
                .strength(10.0f, 30.0f)
                .requiresCorrectToolForDrops()
        )
    );

    /** 😡 火箭发动机方块 - 提供推力，基于 GTM 燃料系统 😡
     */
    public static final RegistryObject<Block> ROCKET_ENGINE = registerBlock(
        "rocket_engine",
        () -> new RocketEngineBlock(
            BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)
                .mapColor(MapColor.METAL)
                .sound(SoundType.COPPER)
                .strength(8.0f, 20.0f)
                .requiresCorrectToolForDrops()
                .noOcclusion()
        )
    );

    /** 😡 火箭燃料箱方块 - 储存 GTM 液体燃料 😡
     */
    public static final RegistryObject<Block> ROCKET_FUEL_TANK = registerBlock(
        "rocket_fuel_tank",
        () -> new RocketFuelTankBlock(
            BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)
                .mapColor(MapColor.METAL)
                .sound(SoundType.COPPER)
                .strength(6.0f, 15.0f)
                .requiresCorrectToolForDrops()
                .noOcclusion()
        )
    );

    /** 😡 火箭指令舱方块 - 控制中心 😡
     */
    public static final RegistryObject<Block> ROCKET_COCKPIT = registerBlock(
        "rocket_cockpit",
        () -> new Block(
            BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)
                .mapColor(MapColor.METAL)
                .sound(SoundType.COPPER)
                .strength(8.0f, 20.0f)
                .requiresCorrectToolForDrops()
                .noOcclusion()
        )
    );

    /** 😡 火箭结构段方块 - 用于连接各个部件（气缸段） 😡
     */
    public static final RegistryObject<Block> ROCKET_FRAME = registerBlock(
        "rocket_frame",
        () -> new Block(
            BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)
                .mapColor(MapColor.METAL)
                .sound(SoundType.COPPER)
                .strength(5.0f, 10.0f)
                .requiresCorrectToolForDrops()
        )
    );

    /** 😡 发动机安装架 - 现实中用于把多个发动机固定在尾段 😡
     */
    public static final RegistryObject<Block> ENGINE_MOUNT = registerBlock(
        "engine_mount",
        () -> new Block(
            BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)
                .mapColor(MapColor.METAL)
                .sound(SoundType.ANVIL)
                .strength(6.0f, 15.0f)
                .requiresCorrectToolForDrops()
                .noOcclusion()
        )
    );

    /** 😡 航电舱（Avionics Bay） - 放置导航计算机、飞控等设备 😡
     */
    public static final RegistryObject<Block> AVIONICS_BAY = registerBlock(
        "avionics_bay",
        () -> new Block(
            BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)
                .mapColor(MapColor.COLOR_LIGHT_GRAY)
                .sound(SoundType.METAL)
                .strength(4.0f, 10.0f)
                .requiresCorrectToolForDrops()
                .noOcclusion()
        )
    );

    /** 😡 级间段（Interstage） - 用于连接不同火箭级，并提供分离结构 😡
     */
    public static final RegistryObject<Block> INTERSTAGE = registerBlock(
        "interstage",
        () -> new Block(
            BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)
                .mapColor(MapColor.STONE)
                .sound(SoundType.METAL)
                .strength(5.0f, 12.0f)
                .requiresCorrectToolForDrops()
                .noOcclusion()
        )
    );

    // 😡 ========== 行星占位符方块 ========== 😡
    // 😡 这些方块用于维度生成，后续可以替换材质 😡

    public static final RegistryObject<Block> EARTH_SURFACE_BLOCK = registerBlock(
        "earth_surface_block",
        () -> new Block(BlockBehaviour.Properties.copy(Blocks.GRASS_BLOCK)
            .mapColor(MapColor.GRASS)
            .sound(SoundType.GRASS)
            .strength(0.6f, 0.6f))
    );

    public static final RegistryObject<Block> MOON_SURFACE_BLOCK = registerBlock(
        "moon_surface_block",
        () -> new Block(BlockBehaviour.Properties.copy(Blocks.STONE)
            .mapColor(MapColor.STONE)
            .sound(SoundType.STONE)
            .strength(1.5f, 6.0f))
    );

    public static final RegistryObject<Block> MARS_SURFACE_BLOCK = registerBlock(
        "mars_surface_block",
        () -> new Block(BlockBehaviour.Properties.copy(Blocks.RED_SANDSTONE)
            .mapColor(MapColor.COLOR_RED)
            .sound(SoundType.SAND)
            .strength(0.8f, 0.8f))
    );

    public static final RegistryObject<Block> VENUS_SURFACE_BLOCK = registerBlock(
        "venus_surface_block",
        () -> new Block(BlockBehaviour.Properties.copy(Blocks.BASALT)
            .mapColor(MapColor.COLOR_ORANGE)
            .sound(SoundType.BASALT)
            .strength(1.25f, 4.2f))
    );

    public static final RegistryObject<Block> MERCURY_SURFACE_BLOCK = registerBlock(
        "mercury_surface_block",
        () -> new Block(BlockBehaviour.Properties.copy(Blocks.GRAY_CONCRETE)
            .mapColor(MapColor.COLOR_GRAY)
            .sound(SoundType.STONE)
            .strength(1.8f, 1.8f))
    );

    public static final RegistryObject<Block> JUPITER_SURFACE_BLOCK = registerBlock(
        "jupiter_surface_block",
        () -> new Block(BlockBehaviour.Properties.copy(Blocks.BROWN_CONCRETE)
            .mapColor(MapColor.COLOR_BROWN)
            .sound(SoundType.WOOL)
            .strength(0.5f, 0.5f))
    );

    public static final RegistryObject<Block> SATURN_SURFACE_BLOCK = registerBlock(
        "saturn_surface_block",
        () -> new Block(BlockBehaviour.Properties.copy(Blocks.YELLOW_CONCRETE)
            .mapColor(MapColor.COLOR_YELLOW)
            .sound(SoundType.WOOL)
            .strength(0.5f, 0.5f))
    );

    public static final RegistryObject<Block> URANUS_SURFACE_BLOCK = registerBlock(
        "uranus_surface_block",
        () -> new Block(BlockBehaviour.Properties.copy(Blocks.CYAN_CONCRETE)
            .mapColor(MapColor.COLOR_CYAN)
            .sound(SoundType.WOOL)
            .strength(0.5f, 0.5f))
    );

    public static final RegistryObject<Block> NEPTUNE_SURFACE_BLOCK = registerBlock(
        "neptune_surface_block",
        () -> new Block(BlockBehaviour.Properties.copy(Blocks.BLUE_CONCRETE)
            .mapColor(MapColor.COLOR_BLUE)
            .sound(SoundType.WOOL)
            .strength(0.5f, 0.5f))
    );

    public static final RegistryObject<Block> PLUTO_SURFACE_BLOCK = registerBlock(
        "pluto_surface_block",
        () -> new Block(BlockBehaviour.Properties.copy(Blocks.PACKED_ICE)
            .mapColor(MapColor.ICE)
            .sound(SoundType.GLASS)
            .strength(0.5f, 0.5f))
    );

    /** 😡 注册方块并自动创建对应的物品 😡
     */
    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block) {
        RegistryObject<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    /** 😡 为方块注册对应的物品 😡
     */
    private static <T extends Block> RegistryObject<Item> registerBlockItem(String name, RegistryObject<T> block) {
        return RocketCEGItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    /** 😡 在事件总线上注册所有方块 😡
     */
    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
