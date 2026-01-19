package com.example.rocketceg.dimension.seamless;

import com.example.rocketceg.RocketCEGMod;
import com.example.rocketceg.client.RenderUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.joml.Vector4f;

import java.util.HashMap;
import java.util.Map;

/** 😡 太空星球渲染器 - 基于 Starlance RenderUtil 实现 * * 在太空维度中渲染可见的星球，实现真实的太空体验： * 1. 远距离星球渲染 * 2. 大气层效果 * 3. 星球表面细节（占位符） * 4. 动态光照和阴影 😡
     */
public class SpacePlanetRenderer {
    
    private static final Logger LOGGER = LogManager.getLogger(RocketCEGMod.MOD_ID);
    
    // 😡 单例实例 😡
    private static SpacePlanetRenderer INSTANCE;
    
    // 😡 星球数据 😡
    private static final Map<ResourceKey<Level>, PlanetData> PLANET_DATA = new HashMap<>();
    
    // 😡 渲染距离 😡
    private static final double MAX_RENDER_DISTANCE = 10000.0;
    private static final double MIN_RENDER_DISTANCE = 100.0;
    
    // 😡 渲染配置 😡
    private volatile boolean spaceRenderingEnabled = true;
    
    private SpacePlanetRenderer() {}
    
    public static SpacePlanetRenderer getInstance() {
        if (INSTANCE == null) {
            synchronized (SpacePlanetRenderer.class) {
                if (INSTANCE == null) {
                    INSTANCE = new SpacePlanetRenderer();
                }
            }
        }
        return INSTANCE;
    }
    
    public boolean isSpaceRenderingEnabled() {
        return spaceRenderingEnabled;
    }
    
    public void setSpaceRenderingEnabled(boolean enabled) {
        this.spaceRenderingEnabled = enabled;
    }
    
    static {
        initializePlanetData();
    }
    
    /** 😡 初始化星球数据 😡
     */
    private static void initializePlanetData() {
        // 😡 地球 😡
        addPlanet("rocketceg:earth_surface", "rocketceg:earth_orbit", 
                new Vec3(0, 0, 0), 6371.0, 
                new ResourceLocation("rocketceg", "textures/planets/earth.png"),
                0x4A90E2, true);
        
        // 😡 月球 😡
        addPlanet("rocketceg:moon_surface", "rocketceg:moon_orbit",
                new Vec3(384400, 0, 0), 1737.0,
                new ResourceLocation("rocketceg", "textures/planets/moon.png"),
                0xC0C0C0, false);
        
        // 😡 火星 😡
        addPlanet("rocketceg:mars_surface", "rocketceg:mars_orbit",
                new Vec3(0, 0, 227900000), 3390.0,
                new ResourceLocation("rocketceg", "textures/planets/mars.png"),
                0xCD5C5C, true);
        
        // 😡 金星 😡
        addPlanet("rocketceg:venus_surface", "rocketceg:venus_orbit",
                new Vec3(-108200000, 0, 0), 6052.0,
                new ResourceLocation("rocketceg", "textures/planets/venus.png"),
                0xFFC649, true);
        
        // 😡 水星 😡
        addPlanet("rocketceg:mercury_surface", "rocketceg:mercury_orbit",
                new Vec3(-57900000, 0, 0), 2440.0,
                new ResourceLocation("rocketceg", "textures/planets/mercury.png"),
                0x8C7853, false);
        
        LOGGER.info("[RocketCEG] 初始化星球数据完成，共 {} 个星球", PLANET_DATA.size());
    }
    
    private static void addPlanet(String surfaceDim, String orbitDim, Vec3 position, double radius,
                                 ResourceLocation texture, int atmosphereColor, boolean hasAtmosphere) {
        ResourceKey<Level> surfaceKey = ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION, 
                ResourceLocation.tryParse(surfaceDim));
        ResourceKey<Level> orbitKey = ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION, 
                ResourceLocation.tryParse(orbitDim));
        
        PlanetData data = new PlanetData(surfaceKey, orbitKey, position, radius, 
                texture, atmosphereColor, hasAtmosphere);
        
        PLANET_DATA.put(surfaceKey, data);
        PLANET_DATA.put(orbitKey, data);
    }
    
    /** 😡 渲染太空中的星球 😡
     */
    public static void renderSpacePlanets(PoseStack poseStack, MultiBufferSource bufferSource, 
                                         Camera camera, float partialTick) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return;
        
        ResourceKey<Level> currentDim = level.dimension();
        
        // 😡 只在太空轨道维度中渲染星球 😡
        if (!isSpaceDimension(currentDim)) return;
        
        Vec3 cameraPos = camera.getPosition();
        
        for (Map.Entry<ResourceKey<Level>, PlanetData> entry : PLANET_DATA.entrySet()) {
            PlanetData planet = entry.getValue();
            
            // 😡 不渲染当前所在的星球 😡
            if (entry.getKey().equals(currentDim)) continue;
            
            double distance = cameraPos.distanceTo(planet.position);
            
            // 😡 距离裁剪 😡
            if (distance > MAX_RENDER_DISTANCE || distance < MIN_RENDER_DISTANCE) continue;
            
            renderPlanet(poseStack, bufferSource, planet, cameraPos, distance, partialTick);
        }
    }
    
    /** 😡 渲染单个星球 😡
     */
    private static void renderPlanet(PoseStack poseStack, MultiBufferSource bufferSource,
                                   PlanetData planet, Vec3 cameraPos, double distance, float partialTick) {
        poseStack.pushPose();
        
        // 😡 计算星球在屏幕上的位置 😡
        Vec3 relativePos = planet.position.subtract(cameraPos);
        poseStack.translate(relativePos.x, relativePos.y, relativePos.z);
        
        // 😡 根据距离计算渲染大小 😡
        float renderSize = (float) (planet.radius / distance * 1000.0);

        renderSize = Math.max(renderSize, 1.0f); // 😡 最小渲染大小 😡
        
        // 😡 渲染星球主体 😡
        renderPlanetSphere(poseStack, bufferSource, planet, renderSize);
        
        // 😡 渲染大气层（如果有） 😡
        if (planet.hasAtmosphere) {
            renderAtmosphere(poseStack, bufferSource, planet, renderSize * 1.1f);

        }
        
        poseStack.popPose();
    }
    
    /** 😡 渲染星球球体 - 使用 Starlance RenderUtil 方式 😡
     */
    private static void renderPlanetSphere(PoseStack poseStack, MultiBufferSource bufferSource,
                                         PlanetData planet, float size) {
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.solid());
        
        // 😡 获取颜色 😡
        int color = planet.atmosphereColor;
        float r = ((color >> 16) & 0xFF) / 255.0f;
        float g = ((color >> 8) & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;
        
        // 😡 创建光照图 - 最大亮度 😡
        RenderUtil.BoxLightMap lightMap = new RenderUtil.BoxLightMap();
        lightMap.setAll(LightTexture.pack(15, 15));
        
        // 😡 使用 RenderUtil 绘制方块 - 按照 Starlance 的方式 😡
        RenderUtil.drawBox(
            poseStack,
            consumer,
            lightMap,
            new Vector4f(r, g, b, 1.0f),
            new Vector3i(0, 0, 0),
            new Quaternionf(),
            new Vector3i((int)size, (int)size, (int)size)
        );
    }
    
    /** 😡 渲染大气层 - 使用 Starlance RenderUtil 方式 😡
     */
    private static void renderAtmosphere(PoseStack poseStack, MultiBufferSource bufferSource,
                                       PlanetData planet, float size) {
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.translucent());
        
        // 😡 获取颜色 😡
        int color = planet.atmosphereColor;
        float r = ((color >> 16) & 0xFF) / 255.0f;
        float g = ((color >> 8) & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;
        
        // 😡 创建光照图 😡
        RenderUtil.BoxLightMap lightMap = new RenderUtil.BoxLightMap();
        lightMap.setAll(LightTexture.pack(15, 15));
        
        // 😡 使用 RenderUtil 绘制半透明方块 - 按照 Starlance 的方式 😡
        RenderUtil.drawBox(
            poseStack,
            consumer,
            lightMap,
            new Vector4f(r, g, b, 0.3f),
            new Vector3i(0, 0, 0),
            new Quaternionf(),
            new Vector3i((int)(size * 1.1f), (int)(size * 1.1f), (int)(size * 1.1f))

        );
    }
    
    /** 😡 检查是否是太空维度 😡
     */
    private static boolean isSpaceDimension(ResourceKey<Level> dimension) {
        String dimName = dimension.location().toString();
        return dimName.contains("orbit") || dimName.contains("space");
    }
    
    /** 😡 获取星球数据 😡
     */
    public static PlanetData getPlanetData(ResourceKey<Level> dimension) {
        return PLANET_DATA.get(dimension);
    }
    
    /** 😡 星球数据类 😡
     */
    public static class PlanetData {
        public final ResourceKey<Level> surfaceDimension;
        public final ResourceKey<Level> orbitDimension;
        public final Vec3 position;
        public final double radius;
        public final ResourceLocation texture;
        public final int atmosphereColor;
        public final boolean hasAtmosphere;
        
        public PlanetData(ResourceKey<Level> surfaceDimension, ResourceKey<Level> orbitDimension,
                         Vec3 position, double radius, ResourceLocation texture,
                         int atmosphereColor, boolean hasAtmosphere) {
            this.surfaceDimension = surfaceDimension;
            this.orbitDimension = orbitDimension;
            this.position = position;
            this.radius = radius;
            this.texture = texture;
            this.atmosphereColor = atmosphereColor;
            this.hasAtmosphere = hasAtmosphere;
        }
    }
}