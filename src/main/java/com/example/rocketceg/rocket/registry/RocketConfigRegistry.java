package com.example.rocketceg.rocket.registry;

import com.example.rocketceg.RocketCEGMod;
import com.example.rocketceg.dimension.RocketCEGDimensions;
import com.example.rocketceg.rocket.config.CelestialBodyConfig;
import com.example.rocketceg.rocket.config.RocketEngineDefinition;
import com.example.rocketceg.rocket.config.RocketPartConfig;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;

import java.util.HashMap;
import java.util.Map;

/** 😡 火箭配置注册表 - 管理发动机定义、部件配置、行星配置 😡
     */
public class RocketConfigRegistry {
    private static final Map<ResourceLocation, RocketEngineDefinition> ENGINES = new HashMap<>();
    private static final Map<ResourceLocation, RocketPartConfig> PART_CONFIGS = new HashMap<>();
    private static final Map<ResourceLocation, CelestialBodyConfig> CELESTIAL_BODIES = new HashMap<>();
    private static final Map<ResourceKey<Level>, CelestialBodyConfig> DIMENSION_TO_BODY = new HashMap<>();

    // 😡 ========== 发动机定义 ========== 😡

    public static void registerEngine(RocketEngineDefinition definition) {
        ENGINES.put(definition.getId(), definition);
    }

    public static RocketEngineDefinition getEngine(ResourceLocation id) {
        return ENGINES.get(id);
    }

    // 😡 ========== 部件配置 ========== 😡

    public static void registerPartConfig(RocketPartConfig config) {
        PART_CONFIGS.put(config.getBlockId(), config);
    }

    public static RocketPartConfig getPartConfig(ResourceLocation blockId) {
        return PART_CONFIGS.get(blockId);
    }

    // 😡 ========== 行星配置 ========== 😡

    public static void registerCelestialBody(CelestialBodyConfig body) {
        CELESTIAL_BODIES.put(body.getId(), body);
        DIMENSION_TO_BODY.put(body.getSurfaceDimension(), body);
        DIMENSION_TO_BODY.put(body.getOrbitDimension(), body); // 😡 轨道维度也映射到同一个行星 😡
    }

    /** 😡 将主世界（minecraft:overworld）映射到地球配置 * 这样玩家在主世界也能使用维度切换功能 😡
     */
    public static void mapOverworldToEarth() {
        final CelestialBodyConfig earth = CELESTIAL_BODIES.get(new ResourceLocation(RocketCEGMod.MOD_ID, "earth"));
        if (earth != null) {
            final ResourceKey<Level> overworld = Level.OVERWORLD;
            DIMENSION_TO_BODY.put(overworld, earth);
        }
    }

    public static CelestialBodyConfig getCelestialBody(ResourceLocation id) {
        return CELESTIAL_BODIES.get(id);
    }

    public static CelestialBodyConfig getBodyForDimension(ResourceKey<Level> dimension) {
        return DIMENSION_TO_BODY.get(dimension);
    }

    // 😡 ========== 初始化默认配置 ========== 😡

    /** 😡 在模组加载时注册默认配置 😡
     */
    public static void initializeDefaults() {
        // 😡 注册默认发动机：Merlin 1D（Falcon 9 一级发动机） 😡
        registerEngine(new RocketEngineDefinition(
            new ResourceLocation(RocketCEGMod.MOD_ID, "merlin_1d"),
            845_000.0,      // 😡 海平面推力 (N) 😡
            981_000.0,      // 😡 真空推力 (N) 😡
            282.0,          // 😡 海平面 Isp (s) 😡
            311.0,          // 😡 真空 Isp (s) 😡
            0.4,            // 😡 最小节流 😡
            1.0,            // 😡 最大节流 😡
            new ResourceLocation(RocketCEGMod.MOD_ID, "rp1_lox") // 😡 燃料：RP-1/LOX 😡
        ));

        // 😡 注册太阳系所有行星 😡
        registerSolarSystemBodies();
        
        // 😡 将主世界映射到地球配置（方便测试和使用） 😡
        mapOverworldToEarth();
    }

    /** 😡 注册太阳系所有行星配置 😡
     */
    private static void registerSolarSystemBodies() {
        // 😡 地球 😡
        registerCelestialBody(new CelestialBodyConfig(
            new ResourceLocation(RocketCEGMod.MOD_ID, "earth"),
            3.986004418e14,  // 😡 μ (m³/s²) 😡
            6.371e6,        // 😡 半径 (m) 😡
            9.80665,        // 😡 地表重力 (m/s²) 😡
            100_000.0,      // 😡 大气顶高度 (m) 😡
            1.225,          // 😡 海平面大气密度 (kg/m³) 😡
            8_500.0,        // 😡 标高尺度 (m) 😡
            RocketCEGDimensions.EARTH_SURFACE,
            RocketCEGDimensions.EARTH_ORBIT
        ));

        // 😡 月球 😡
        registerCelestialBody(new CelestialBodyConfig(
            new ResourceLocation(RocketCEGMod.MOD_ID, "moon"),
            4.9048695e12,   // 😡 μ (m³/s²) 😡
            1.737e6,        // 😡 半径 (m) 😡
            1.62,           // 😡 地表重力 (m/s²) 😡
            0.0,            // 😡 无大气层 😡
            0.0,            // 😡 无大气密度 😡
            0.0,            // 😡 无标高 😡
            RocketCEGDimensions.MOON_SURFACE,
            RocketCEGDimensions.MOON_ORBIT
        ));

        // 😡 火星 😡
        registerCelestialBody(new CelestialBodyConfig(
            new ResourceLocation(RocketCEGMod.MOD_ID, "mars"),
            4.282837e13,    // 😡 μ (m³/s²) 😡
            3.390e6,        // 😡 半径 (m) 😡
            3.711,          // 😡 地表重力 (m/s²) 😡
            11_000.0,       // 😡 大气顶高度 (m) 😡
            0.02,           // 😡 海平面大气密度 (kg/m³) - 非常稀薄 😡
            11_000.0,       // 😡 标高尺度 (m) 😡
            RocketCEGDimensions.MARS_SURFACE,
            RocketCEGDimensions.MARS_ORBIT
        ));

        // 😡 金星 😡
        registerCelestialBody(new CelestialBodyConfig(
            new ResourceLocation(RocketCEGMod.MOD_ID, "venus"),
            3.24859e14,     // 😡 μ (m³/s²) 😡
            6.0518e6,       // 😡 半径 (m) 😡
            8.87,           // 😡 地表重力 (m/s²) 😡
            250_000.0,      // 😡 大气顶高度 (m) 😡
            65.0,           // 😡 海平面大气密度 (kg/m³) - 非常稠密 😡
            15_900.0,       // 😡 标高尺度 (m) 😡
            RocketCEGDimensions.VENUS_SURFACE,
            RocketCEGDimensions.VENUS_ORBIT
        ));

        // 😡 水星 😡
        registerCelestialBody(new CelestialBodyConfig(
            new ResourceLocation(RocketCEGMod.MOD_ID, "mercury"),
            2.2032e13,      // 😡 μ (m³/s²) 😡
            2.4397e6,       // 😡 半径 (m) 😡
            3.7,            // 😡 地表重力 (m/s²) 😡
            0.0,            // 😡 无大气层 😡
            0.0,            // 😡 无大气密度 😡
            0.0,            // 😡 无标高 😡
            RocketCEGDimensions.MERCURY_SURFACE,
            RocketCEGDimensions.MERCURY_ORBIT
        ));

        // 😡 木星（气态巨行星，表面定义为 1 bar 压力面） 😡
        registerCelestialBody(new CelestialBodyConfig(
            new ResourceLocation(RocketCEGMod.MOD_ID, "jupiter"),
            1.2668653e17,   // 😡 μ (m³/s²) 😡
            6.9911e7,      // 😡 半径 (m) - 1 bar 压力面 😡
            24.79,          // 😡 1 bar 面重力 (m/s²) 😡
            500_000.0,      // 😡 大气顶高度 (m) - 简化值 😡
            0.16,           // 😡 1 bar 面大气密度 (kg/m³) 😡
            27_000.0,       // 😡 标高尺度 (m) 😡
            RocketCEGDimensions.JUPITER_SURFACE,
            RocketCEGDimensions.JUPITER_ORBIT
        ));

        // 😡 土星 😡
        registerCelestialBody(new CelestialBodyConfig(
            new ResourceLocation(RocketCEGMod.MOD_ID, "saturn"),
            3.7931187e16,   // 😡 μ (m³/s²) 😡
            5.8232e7,       // 😡 半径 (m) - 1 bar 压力面 😡
            10.44,          // 😡 1 bar 面重力 (m/s²) 😡
            500_000.0,      // 😡 大气顶高度 (m) 😡
            0.19,           // 😡 1 bar 面大气密度 (kg/m³) 😡
            59_500.0,       // 😡 标高尺度 (m) 😡
            RocketCEGDimensions.SATURN_SURFACE,
            RocketCEGDimensions.SATURN_ORBIT
        ));

        // 😡 天王星 😡
        registerCelestialBody(new CelestialBodyConfig(
            new ResourceLocation(RocketCEGMod.MOD_ID, "uranus"),
            5.793939e15,    // 😡 μ (m³/s²) 😡
            2.5362e7,      // 😡 半径 (m) - 1 bar 压力面 😡
            8.69,           // 😡 1 bar 面重力 (m/s²) 😡
            500_000.0,      // 😡 大气顶高度 (m) 😡
            0.42,           // 😡 1 bar 面大气密度 (kg/m³) 😡
            27_700.0,       // 😡 标高尺度 (m) 😡
            RocketCEGDimensions.URANUS_SURFACE,
            RocketCEGDimensions.URANUS_ORBIT
        ));

        // 😡 海王星 😡
        registerCelestialBody(new CelestialBodyConfig(
            new ResourceLocation(RocketCEGMod.MOD_ID, "neptune"),
            6.836529e15,    // 😡 μ (m³/s²) 😡
            2.4622e7,      // 😡 半径 (m) - 1 bar 压力面 😡
            11.15,          // 😡 1 bar 面重力 (m/s²) 😡
            500_000.0,      // 😡 大气顶高度 (m) 😡
            0.45,           // 😡 1 bar 面大气密度 (kg/m³) 😡
            19_700.0,       // 😡 标高尺度 (m) 😡
            RocketCEGDimensions.NEPTUNE_SURFACE,
            RocketCEGDimensions.NEPTUNE_ORBIT
        ));

        // 😡 冥王星（矮行星） 😡
        registerCelestialBody(new CelestialBodyConfig(
            new ResourceLocation(RocketCEGMod.MOD_ID, "pluto"),
            8.71e11,        // 😡 μ (m³/s²) 😡
            1.195e6,       // 😡 半径 (m) 😡
            0.62,           // 😡 地表重力 (m/s²) 😡
            0.0,            // 😡 无大气层（或极稀薄） 😡
            0.0,            // 😡 无大气密度 😡
            0.0,            // 😡 无标高 😡
            RocketCEGDimensions.PLUTO_SURFACE,
            RocketCEGDimensions.PLUTO_ORBIT
        ));
    }
}
