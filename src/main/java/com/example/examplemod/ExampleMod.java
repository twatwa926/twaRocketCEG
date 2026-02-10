package com.example.examplemod;

import com.mojang.logging.LogUtils;
import com.example.examplemod.fluid.GasAtmosphereFluidType;
import com.example.examplemod.rocket.RocketAvionicsBayBlock;
import com.example.examplemod.rocket.RocketAvionicsBayBlockEntity;
import com.example.examplemod.rocket.RocketAvionicsMenu;
import com.example.examplemod.rocket.RocketSeatBlock;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.common.extensions.IForgeMenuType;
import org.slf4j.Logger;
import net.minecraftforge.api.distmarker.Dist;

@Mod(ExampleMod.MODID)
public class ExampleMod {

    public static final String MODID = "rocketwa";

    private static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MODID);
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MODID);

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
    public static final DeferredRegister<net.minecraftforge.fluids.FluidType> FLUID_TYPES = DeferredRegister.create(ForgeRegistries.Keys.FLUID_TYPES, MODID);
    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(ForgeRegistries.FLUIDS, MODID);

    public static final RegistryObject<net.minecraftforge.fluids.FluidType> GAS_ATMOSPHERE_TYPE = FLUID_TYPES.register("gas_atmosphere", GasAtmosphereFluidType::new);

    public static final RegistryObject<Block> AVIONICS_BAY = BLOCKS.register("avionics_bay", () ->
            new RocketAvionicsBayBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(4.0f, 6.0f)));
    public static final RegistryObject<Block> CONTROL_COMPUTER = BLOCKS.register("control_computer", () ->
            new Block(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(4.0f, 6.0f)));
    public static final RegistryObject<Block> FUEL_TANK = BLOCKS.register("fuel_tank", () ->
            new Block(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(5.0f, 6.0f)));
    public static final RegistryObject<Block> ENGINE_MODULE = BLOCKS.register("engine_module", () ->
            new Block(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(6.0f, 8.0f)));
    public static final RegistryObject<Block> ENGINE_MODULE_LV = BLOCKS.register("engine_module_lv", () ->
            new Block(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(6.0f, 8.0f)));
    public static final RegistryObject<Block> ENGINE_MODULE_MV = BLOCKS.register("engine_module_mv", () ->
            new Block(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(6.0f, 8.0f)));
    public static final RegistryObject<Block> ENGINE_MODULE_HV = BLOCKS.register("engine_module_hv", () ->
            new Block(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(6.0f, 8.0f)));
    public static final RegistryObject<Block> STAGE_SEPARATOR = BLOCKS.register("stage_separator", () ->
            new Block(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.0f, 4.0f)));

    public static final RegistryObject<Block> ROCKET_SEAT = BLOCKS.register("rocket_seat", () ->
            new RocketSeatBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(2.0f, 4.0f)));

    public static final RegistryObject<Block> FLOATER = BLOCKS.register("floater", () ->
            new Block(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_LIGHT_BLUE).strength(1.0f, 2.0f)));

    public static final RegistryObject<Block> SUN_SURFACE = BLOCKS.register("sun_surface", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.SAND).strength(2.0f, 6.0f)));
    public static final RegistryObject<Block> MERCURY_SURFACE = BLOCKS.register("mercury_surface", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(2.0f, 6.0f)));
    public static final RegistryObject<Block> VENUS_SURFACE = BLOCKS.register("venus_surface", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.SAND).strength(2.0f, 6.0f)));
    public static final RegistryObject<Block> EARTH_SURFACE = BLOCKS.register("earth_surface", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.GRASS).strength(2.0f, 6.0f)));
    public static final RegistryObject<Block> MOON_SURFACE = BLOCKS.register("moon_surface", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(2.0f, 6.0f)));
    public static final RegistryObject<Block> MARS_SURFACE = BLOCKS.register("mars_surface", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_ORANGE).strength(2.0f, 6.0f)));
    public static final RegistryObject<Block> CERES_SURFACE = BLOCKS.register("ceres_surface", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(2.0f, 6.0f)));
    public static final RegistryObject<Block> JUPITER_SURFACE = BLOCKS.register("jupiter_surface", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_ORANGE).strength(2.0f, 6.0f)));
    public static final RegistryObject<Block> SATURN_SURFACE = BLOCKS.register("saturn_surface", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.SAND).strength(2.0f, 6.0f)));
    public static final RegistryObject<Block> URANUS_SURFACE = BLOCKS.register("uranus_surface", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_LIGHT_BLUE).strength(2.0f, 6.0f)));
    public static final RegistryObject<Block> NEPTUNE_SURFACE = BLOCKS.register("neptune_surface", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLUE).strength(2.0f, 6.0f)));
    public static final RegistryObject<Block> PLUTO_SURFACE = BLOCKS.register("pluto_surface", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(2.0f, 6.0f)));

    public static final RegistryObject<Item> AVIONICS_BAY_ITEM = ITEMS.register("avionics_bay", () ->
            new BlockItem(AVIONICS_BAY.get(), new Item.Properties()));
    public static final RegistryObject<Item> CONTROL_COMPUTER_ITEM = ITEMS.register("control_computer", () ->
            new BlockItem(CONTROL_COMPUTER.get(), new Item.Properties()));
    public static final RegistryObject<Item> FUEL_TANK_ITEM = ITEMS.register("fuel_tank", () ->
            new BlockItem(FUEL_TANK.get(), new Item.Properties()));
    public static final RegistryObject<Item> ENGINE_MODULE_ITEM = ITEMS.register("engine_module", () ->
            new BlockItem(ENGINE_MODULE.get(), new Item.Properties()));
    public static final RegistryObject<Item> ENGINE_MODULE_LV_ITEM = ITEMS.register("engine_module_lv", () ->
            new BlockItem(ENGINE_MODULE_LV.get(), new Item.Properties()));
    public static final RegistryObject<Item> ENGINE_MODULE_MV_ITEM = ITEMS.register("engine_module_mv", () ->
            new BlockItem(ENGINE_MODULE_MV.get(), new Item.Properties()));
    public static final RegistryObject<Item> ENGINE_MODULE_HV_ITEM = ITEMS.register("engine_module_hv", () ->
            new BlockItem(ENGINE_MODULE_HV.get(), new Item.Properties()));
    public static final RegistryObject<Item> STAGE_SEPARATOR_ITEM = ITEMS.register("stage_separator", () ->
            new BlockItem(STAGE_SEPARATOR.get(), new Item.Properties()));
    public static final RegistryObject<Item> ROCKET_SEAT_ITEM = ITEMS.register("rocket_seat", () ->
            new BlockItem(ROCKET_SEAT.get(), new Item.Properties()));
    public static final RegistryObject<Item> FLOATER_ITEM = ITEMS.register("floater", () ->
            new BlockItem(FLOATER.get(), new Item.Properties()));
    public static final RegistryObject<Item> SUN_SURFACE_ITEM = ITEMS.register("sun_surface", () -> new BlockItem(SUN_SURFACE.get(), new Item.Properties()));
    public static final RegistryObject<Item> MERCURY_SURFACE_ITEM = ITEMS.register("mercury_surface", () -> new BlockItem(MERCURY_SURFACE.get(), new Item.Properties()));
    public static final RegistryObject<Item> VENUS_SURFACE_ITEM = ITEMS.register("venus_surface", () -> new BlockItem(VENUS_SURFACE.get(), new Item.Properties()));
    public static final RegistryObject<Item> EARTH_SURFACE_ITEM = ITEMS.register("earth_surface", () -> new BlockItem(EARTH_SURFACE.get(), new Item.Properties()));
    public static final RegistryObject<Item> MOON_SURFACE_ITEM = ITEMS.register("moon_surface", () -> new BlockItem(MOON_SURFACE.get(), new Item.Properties()));
    public static final RegistryObject<Item> MARS_SURFACE_ITEM = ITEMS.register("mars_surface", () -> new BlockItem(MARS_SURFACE.get(), new Item.Properties()));
    public static final RegistryObject<Item> CERES_SURFACE_ITEM = ITEMS.register("ceres_surface", () -> new BlockItem(CERES_SURFACE.get(), new Item.Properties()));
    public static final RegistryObject<Item> JUPITER_SURFACE_ITEM = ITEMS.register("jupiter_surface", () -> new BlockItem(JUPITER_SURFACE.get(), new Item.Properties()));
    public static final RegistryObject<Item> SATURN_SURFACE_ITEM = ITEMS.register("saturn_surface", () -> new BlockItem(SATURN_SURFACE.get(), new Item.Properties()));
    public static final RegistryObject<Item> URANUS_SURFACE_ITEM = ITEMS.register("uranus_surface", () -> new BlockItem(URANUS_SURFACE.get(), new Item.Properties()));
    public static final RegistryObject<Item> NEPTUNE_SURFACE_ITEM = ITEMS.register("neptune_surface", () -> new BlockItem(NEPTUNE_SURFACE.get(), new Item.Properties()));
    public static final RegistryObject<Item> PLUTO_SURFACE_ITEM = ITEMS.register("pluto_surface", () -> new BlockItem(PLUTO_SURFACE.get(), new Item.Properties()));
    public static final RegistryObject<Item> ROCKET_ALLOY_INGOT = ITEMS.register("rocket_alloy_ingot", () ->
            new Item(new Item.Properties()));
    public static final RegistryObject<Item> ROCKET_TEMPLATE = ITEMS.register("rocket_template", () ->
            new Item(new Item.Properties()));
    public static final RegistryObject<Item> ROCKET_CASING = ITEMS.register("rocket_casing", () ->
            new Item(new Item.Properties()));
    public static final RegistryObject<Item> ROCKET_MODULE_CORE = ITEMS.register("rocket_module_core", () ->
            new Item(new Item.Properties()));

    public static final RegistryObject<MenuType<RocketAvionicsMenu>> AVIONICS_MENU = MENUS.register("avionics_menu",
            () -> IForgeMenuType.create(RocketAvionicsMenu::fromNetwork));

    public static final RegistryObject<BlockEntityType<RocketAvionicsBayBlockEntity>> AVIONICS_BAY_BE =
            BLOCK_ENTITIES.register("avionics_bay", () -> BlockEntityType.Builder
                    .of(RocketAvionicsBayBlockEntity::new, AVIONICS_BAY.get())
                    .build(null));

    public static final RegistryObject<EntityType<com.example.examplemod.rocket.ship.RocketShipEntity>> ROCKET_SHIP_ENTITY =
            ENTITIES.register("rocket_ship", () -> EntityType.Builder
                    .<com.example.examplemod.rocket.ship.RocketShipEntity>of(com.example.examplemod.rocket.ship.RocketShipEntity::new, MobCategory.MISC)
                    .sized(1.0f, 1.0f)
                    .clientTrackingRange(200)
                    .updateInterval(1)
                    .fireImmune()
                    .build("rocket_ship"));

    public static final RegistryObject<EntityType<com.example.examplemod.rocket.ship.RocketBoosterEntity>> ROCKET_BOOSTER_ENTITY =
            ENTITIES.register("rocket_booster", () -> EntityType.Builder
                    .<com.example.examplemod.rocket.ship.RocketBoosterEntity>of(com.example.examplemod.rocket.ship.RocketBoosterEntity::new, MobCategory.MISC)
                    .sized(1.0f, 3.0f)
                    .clientTrackingRange(200)
                    .updateInterval(2)
                    .fireImmune()
                    .build("rocket_booster"));

    public static final RegistryObject<CreativeModeTab> ROCKET_TAB = CREATIVE_MODE_TABS.register("rocket_tab", () ->
            CreativeModeTab.builder()
                    .title(net.minecraft.network.chat.Component.translatable("itemGroup.rocketwa.rocket_tab"))
                    .withTabsBefore(CreativeModeTabs.COMBAT)
                    .icon(() -> AVIONICS_BAY_ITEM.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        output.accept(AVIONICS_BAY_ITEM.get());
                        output.accept(CONTROL_COMPUTER_ITEM.get());
                        output.accept(FUEL_TANK_ITEM.get());
                        output.accept(ENGINE_MODULE_ITEM.get());
                        output.accept(ENGINE_MODULE_LV_ITEM.get());
                        output.accept(ENGINE_MODULE_MV_ITEM.get());
                        output.accept(ENGINE_MODULE_HV_ITEM.get());
                        output.accept(STAGE_SEPARATOR_ITEM.get());
                        output.accept(ROCKET_SEAT_ITEM.get());
                        output.accept(FLOATER_ITEM.get());
                        output.accept(ROCKET_ALLOY_INGOT.get());
                        output.accept(ROCKET_TEMPLATE.get());
                        output.accept(ROCKET_CASING.get());
                        output.accept(ROCKET_MODULE_CORE.get());
                        output.accept(SUN_SURFACE.get());
                        output.accept(MERCURY_SURFACE.get());
                        output.accept(VENUS_SURFACE.get());
                        output.accept(EARTH_SURFACE.get());
                        output.accept(MOON_SURFACE.get());
                        output.accept(MARS_SURFACE.get());
                        output.accept(CERES_SURFACE.get());
                        output.accept(JUPITER_SURFACE.get());
                        output.accept(SATURN_SURFACE.get());
                        output.accept(URANUS_SURFACE.get());
                        output.accept(NEPTUNE_SURFACE.get());
                        output.accept(PLUTO_SURFACE.get());
                    })
                    .build());

    public ExampleMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(this::commonSetup);

        modEventBus.addListener(this::addCreativeTabEntries);

        BLOCKS.register(modEventBus);
        FLUID_TYPES.register(modEventBus);
        FLUIDS.register(modEventBus);

        ITEMS.register(modEventBus);
        MENUS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);
        ENTITIES.register(modEventBus);

        CREATIVE_MODE_TABS.register(modEventBus);

        MinecraftForge.EVENT_BUS.register(this);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> () -> {
            com.example.examplemod.client.RocketClientInit.init(modEventBus);
        });
    }

    private void addCreativeTabEntries(final BuildCreativeModeTabContentsEvent event) {

        if (event.getTabKey().equals(CreativeModeTabs.FUNCTIONAL_BLOCKS)
                || event.getTabKey().equals(CreativeModeTabs.BUILDING_BLOCKS)
                || event.getTabKey().equals(CreativeModeTabs.TOOLS_AND_UTILITIES)
                || event.getTabKey().equals(CreativeModeTabs.INGREDIENTS)
                || event.getTabKey().equals(CreativeModeTabs.SEARCH)) {
            event.accept(FLOATER_ITEM.get());
        }

        if (event.getTabKey().equals(CreativeModeTabs.SEARCH)) {
            event.accept(AVIONICS_BAY_ITEM.get());
            event.accept(FUEL_TANK_ITEM.get());
            event.accept(ENGINE_MODULE_ITEM.get());
            event.accept(ROCKET_SEAT_ITEM.get());
        }
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        RocketIntegration.init();
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("Rocket mod server starting");
    }
}
