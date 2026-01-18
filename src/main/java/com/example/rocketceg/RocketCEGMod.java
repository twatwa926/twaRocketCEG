package com.example.rocketceg;

import com.example.rocketceg.blockentities.RocketCEGBlockEntities;
import com.example.rocketceg.command.SeamlessTeleportCommand;
import com.example.rocketceg.command.AdvancedSeamlessTeleportCommand;
import com.example.rocketceg.command.PortalCommand;
import com.example.rocketceg.commands.RocketTestCommands;
import com.example.rocketceg.dimension.seamless.AdvancedPortalRenderer;
import com.example.rocketceg.dimension.seamless.IntelligentChunkLoader;
import com.example.rocketceg.dimension.seamless.MultiDimensionRenderer;
import com.example.rocketceg.items.RocketCEGItems;
import com.example.rocketceg.items.RocketCEGTab;
import com.example.rocketceg.registry.RocketCEGBlocks;
import com.example.rocketceg.registry.RocketCEGEntities;
import com.example.rocketceg.rocket.config.RocketPartConfig;
import com.example.rocketceg.rocket.registry.RocketConfigRegistry;
import com.example.rocketceg.seamless.SeamlessCore;
import com.example.rocketceg.worldgen.RocketCEGBiomes;
import com.example.rocketceg.worldgen.RocketCEGWorldGenProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.data.DatapackBuiltinEntriesProvider;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

/** 😡 RocketCEG 模组主类 - Create × GTM 火箭物理模组 * * 增强版功能： * - 集成 ImmersivePortalsMod 技术的无缝维度传送 * - 多维度同时渲染和跨维度区块加载 * - 高级传送门渲染和空间变换系统 * - 智能性能管理和内存优化 😡
     */
@Mod(RocketCEGMod.MOD_ID)
public final class RocketCEGMod {

    public static final String MOD_ID = "rocketceg";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public RocketCEGMod() {
        final IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        // 😡 注册方块、物品、BlockEntity、实体 😡
        RocketCEGBlocks.register(modBus);
        RocketCEGItems.register(modBus);
        RocketCEGBlockEntities.register(modBus);
        RocketCEGEntities.register(modBus);
        
        // 😡 注册创造模式标签页 😡
        RocketCEGTab.register(modBus);

        // 😡 在 FMLCommonSetupEvent 中初始化配置（此时方块已注册） 😡
        modBus.addListener(this::onCommonSetup);
        
        // 😡 在客户端设置中初始化渲染系统 😡
        modBus.addListener(this::onClientSetup);

        // 😡 注册世界生成数据 😡
        modBus.addListener(this::onGatherData);

        // 😡 注册测试命令 😡
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(RocketTestCommands.class);
        
        // 😡 注册无缝传送命令 😡
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(this::onRegisterCommands);
        
        // 😡 注册维度加载事件（确保维度在服务器启动时被创建） 😡
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(this::onServerStarting);
        
        // 😡 注册服务器关闭事件 😡
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(this::onServerStopping);
        
        LOGGER.info("[RocketCEG] 初始化增强版无缝传送系统 - 参考 ImmersivePortalsMod 技术");
    }
    
    /** 😡 客户端设置 - 初始化渲染系统 😡
     */
    private void onClientSetup(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            try {
                // 😡 初始化多维度渲染器 😡
                MultiDimensionRenderer.getInstance().setMultiDimensionRenderingEnabled(true);
                
                // 😡 初始化高级传送门渲染器 😡
                AdvancedPortalRenderer.getInstance().initialize();
                
                LOGGER.info("[RocketCEG] 客户端渲染系统初始化完成 - ImmersivePortalsMod 增强版");
                
            } catch (Exception e) {
                LOGGER.error("[RocketCEG] 客户端渲染系统初始化失败", e);
            }
        });
    }
    
    /** 😡 注册命令 - 包含增强版命令 😡
     */
    private void onRegisterCommands(final RegisterCommandsEvent event) {
        // 😡 注册基础无缝传送命令 😡
        SeamlessTeleportCommand.register(event.getDispatcher());
        
        // 😡 注册高级空间变换传送命令 - 参考 ImmersivePortalsMod 😡
        AdvancedSeamlessTeleportCommand.register(event.getDispatcher());
        
        // 😡 注册传送门命令 - 100% 按照 ImmersivePortalsMod 实现 😡
        PortalCommand.register(event.getDispatcher());
        
        LOGGER.info("[RocketCEG] 注册增强版无缝传送命令系统 + 跨维度传送门系统");
    }
    
    /** 😡 服务器启动时，初始化增强系统 😡
     */
    private void onServerStarting(final net.minecraftforge.event.server.ServerStartingEvent event) {
        final net.minecraft.server.MinecraftServer server = event.getServer();
        
        try {
            // 😡 初始化智能区块加载器 😡
            IntelligentChunkLoader.getInstance().initialize();
            
            // 😡 确保所有维度都被创建（通过访问它们） 😡
            checkAndLoadDimensions(server);
            
            LOGGER.info("[RocketCEG] 服务端增强系统初始化完成 - ImmersivePortalsMod 技术集成");
            
        } catch (Exception e) {
            LOGGER.error("[RocketCEG] 服务端增强系统初始化失败", e);
        }
    }
    
    /** 😡 服务器关闭时，清理增强系统 😡
     */
    private void onServerStopping(final net.minecraftforge.event.server.ServerStoppingEvent event) {
        try {
            // 😡 关闭智能区块加载器 😡
            IntelligentChunkLoader.getInstance().shutdown();
            
            // 😡 清理高级传送门渲染器 😡
            AdvancedPortalRenderer.getInstance().cleanup();
            
            // 😡 清理多维度渲染器 😡
            MultiDimensionRenderer.getInstance().cleanup();
            
            LOGGER.info("[RocketCEG] 增强系统清理完成");
            
        } catch (Exception e) {
            LOGGER.error("[RocketCEG] 增强系统清理失败", e);
        }
    }
    
    /** 😡 检查并加载维度 😡
     */
    private void checkAndLoadDimensions(net.minecraft.server.MinecraftServer server) {
        // 😡 检查地球轨道维度 😡
        final net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> earthOrbit = 
            net.minecraft.resources.ResourceKey.create(
                net.minecraft.core.registries.Registries.DIMENSION,
                new net.minecraft.resources.ResourceLocation(MOD_ID, "earth_orbit")
            );
        
        final net.minecraft.server.level.ServerLevel orbitLevel = server.getLevel(earthOrbit);
        if (orbitLevel == null) {
            LOGGER.warn(
                "[RocketCEG] 维度 {} 尚未创建。这可能是因为数据生成器失败（Create 模组 Mixin 错误）。" +
                "维度需要通过数据包注册才能使用。",
                earthOrbit.location()
            );
        } else {
            LOGGER.info("[RocketCEG] 维度 {} 已成功加载", earthOrbit.location());
        }
        
        // 😡 检查其他 RocketCEG 维度 😡
        String[] planets = {"moon", "mars", "venus", "mercury", "jupiter"};
        String[] types = {"surface", "orbit"};
        
        for (String planet : planets) {
            for (String type : types) {
                final net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> dimension = 
                    net.minecraft.resources.ResourceKey.create(
                        net.minecraft.core.registries.Registries.DIMENSION,
                        new net.minecraft.resources.ResourceLocation(MOD_ID, planet + "_" + type)
                    );
                
                final net.minecraft.server.level.ServerLevel level = server.getLevel(dimension);
                if (level != null) {
                    LOGGER.debug("[RocketCEG] 维度 {} 已加载", dimension.location());
                } else {
                    LOGGER.debug("[RocketCEG] 维度 {} 未找到", dimension.location());
                }
            }
        }
    }

    private void onGatherData(final net.minecraftforge.data.event.GatherDataEvent event) {
        // 😡 注意：由于 Create 模组的 Mixin 错误，数据生成器可能会失败 😡
        // 😡 如果数据生成器失败，维度数据包将无法生成 😡
        // 😡 在这种情况下，维度需要通过其他方式创建（例如手动创建数据包） 😡
        
        try {
            final PackOutput output = event.getGenerator().getPackOutput();
            final net.minecraftforge.common.data.ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
            final CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

            // 😡 注册世界生成数据提供者 😡
            event.getGenerator().addProvider(
                event.includeServer(),
                new DatapackBuiltinEntriesProvider(
                    output,
                    lookupProvider,
                    new RegistrySetBuilder()
                        .add(Registries.BIOME, RocketCEGBiomes::bootstrap)
                        .add(Registries.DIMENSION_TYPE, RocketCEGWorldGenProvider::bootstrapDimensionTypes)
                        .add(Registries.LEVEL_STEM, (context) -> {
                            RocketCEGWorldGenProvider.bootstrapLevelStems(
                                context,
                                context.lookup(Registries.DIMENSION_TYPE),
                                context.lookup(Registries.BIOME),
                                context.lookup(Registries.NOISE_SETTINGS)
                            );
                        }),
                    Set.of(MOD_ID)
                )
            );
        } catch (Exception e) {
            // 😡 如果数据生成器失败，记录错误但不阻止模组加载 😡
            LOGGER.error(
                "[RocketCEG] 数据生成器失败，维度数据包可能无法生成。错误: {}",
                e.getMessage()
            );
        }
    }

    private void onCommonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            // 😡 初始化默认配置（发动机、行星等） 😡
            RocketConfigRegistry.initializeDefaults();

            // 😡 注册所有火箭方块的部件配置 😡
            registerPartConfigs();
            
            // 😡 初始化无缝传送核心系统 😡
            try {
                SeamlessCore seamlessCore = SeamlessCore.getInstance();
                LOGGER.info("[RocketCEG] 无缝传送核心系统初始化完成 - ImmersivePortalsMod 增强版");
            } catch (Exception e) {
                LOGGER.error("[RocketCEG] 无缝传送核心系统初始化失败", e);
            }
        });
    }

    private void registerPartConfigs() {
        final ResourceLocation merlin1dId = new ResourceLocation(MOD_ID, "merlin_1d");

        // 😡 结构段：500 kg 😡
        registerPartConfig(RocketCEGBlocks.ROCKET_FRAME, 500.0, 0.0, null, RocketPartConfig.PartType.FRAME);

        // 😡 指令舱：2000 kg 😡
        registerPartConfig(RocketCEGBlocks.ROCKET_COCKPIT, 2_000.0, 0.0, null, RocketPartConfig.PartType.COCKPIT);

        // 😡 发动机：3000 kg 干重，关联 Merlin 1D 定义 😡
        registerPartConfig(RocketCEGBlocks.ROCKET_ENGINE, 3_000.0, 0.0, merlin1dId, RocketPartConfig.PartType.ENGINE);

        // 😡 燃料箱：1000 kg 空重，20000 kg 燃料容量 😡
        registerPartConfig(RocketCEGBlocks.ROCKET_FUEL_TANK, 1_000.0, 20_000.0, null, RocketPartConfig.PartType.FUEL_TANK);

        // 😡 发动机安装架：1500 kg 😡
        registerPartConfig(RocketCEGBlocks.ENGINE_MOUNT, 1_500.0, 0.0, null, RocketPartConfig.PartType.ENGINE_MOUNT);

        // 😡 航电舱：800 kg 😡
        registerPartConfig(RocketCEGBlocks.AVIONICS_BAY, 800.0, 0.0, null, RocketPartConfig.PartType.AVIONICS);

        // 😡 级间段：1200 kg 😡
        registerPartConfig(RocketCEGBlocks.INTERSTAGE, 1_200.0, 0.0, null, RocketPartConfig.PartType.INTERSTAGE);
    }

    private void registerPartConfig(
        final net.minecraftforge.registries.RegistryObject<net.minecraft.world.level.block.Block> block,
        final double dryMass,
        final double fuelCapacity,
        final ResourceLocation engineDefinitionId,
        final RocketPartConfig.PartType partType
    ) {
        final ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(block.get());
        RocketConfigRegistry.registerPartConfig(new RocketPartConfig(
            blockId,
            dryMass,
            fuelCapacity,
            engineDefinitionId,
            partType
        ));
    }
}