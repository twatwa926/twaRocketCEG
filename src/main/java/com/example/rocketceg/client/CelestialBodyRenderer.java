ackage com.example.rocketceg.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/** 😡 天体渲染器 - 渲染太阳、地球、月球等天体 * 使用 Starlance 渲染模式 😡
     */
public class CelestialBodyRenderer {
    
    // 😡 天体类型 😡
    public enum CelestialBody {
        SUN,        // 😡 太阳 😡
        EARTH,      // 😡 地球 😡
        MOON,       // 😡 月球 😡
        MARS,       // 😡 火星 😡
        VENUS       // 😡 金星 😡
    }
    
    // 😡 天体数据 😡
    private static class BodyData {
        Vector3f color;
        float size;
        boolean hasAtmosphere;
        Vector3f atmosphereColor;
        
        BodyData(Vector3f color, float size, boolean hasAtmosphere, Vector3f atmosphereColor) {
            this.color = color;
            this.size = size;
            this.hasAtmosphere = hasAtmosphere;
            this.atmosphereColor = atmosphereColor;
        }
    }
    
    // 😡 天体配置 😡
    private static final BodyData SUN_DATA = new BodyData(
        new Vector3f(1.0f, 0.9f, 0.3f),  // 😡 金黄色 😡
        100.0f,
        false,
        null
    );
    
    private static final BodyData EARTH_DATA = new BodyData(
        new Vector3f(0.2f, 0.4f, 0.8f),  // 😡 蓝色 😡
        80.0f,
        true,
        new Vector3f(0.4f, 0.6f, 1.0f)   // 😡 浅蓝色大气层 😡
    );
    
    private static final BodyData MOON_DATA = new BodyData(
        new Vector3f(0.7f, 0.7f, 0.7f),  // 😡 灰色 😡
        30.0f,
        false,
        null
    );
    
    private static final BodyData MARS_DATA = new BodyData(
        new Vector3f(0.8f, 0.3f, 0.2f),  // 😡 红色 😡
        50.0f,
        true,
        new Vector3f(0.9f, 0.5f, 0.3f)   // 😡 淡红色大气层 😡
    );
    
    private static final BodyData VENUS_DATA = new BodyData(
        new Vector3f(0.9f, 0.8f, 0.5f),  // 😡 淡黄色 😡
        70.0f,
        true,
        new Vector3f(1.0f, 0.9f, 0.6f)   // 😡 黄色大气层 😡
    );
    
    /** 😡 渲染天体 😡
     */
    public static void renderCelestialBody(
        PoseStack poseStack,
        MultiBufferSource bufferSource,
        CelestialBody body,
        Vector3f position,
        float partialTick
    ) {
        BodyData data = getBodyData(body);
        if (data == null) return;
        
        poseStack.pushPose();
        poseStack.translate(position.x, position.y, position.z);
        
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.solid());
        int packedLight = LightTexture.pack(15, 15);
        
        // 😡 渲染大气层（如果有） 😡
        if (data.hasAtmosphere) {
            renderAtmosphere(poseStack, consumer, data.atmosphereColor, data.size * 1.2f);
 馃槨
        }
        
        // 😡 渲染天体本体 😡
        renderSphere(poseStack, consumer, data.color, data.size, 32, 16);
        
        // 😡 太阳发光效果 😡
        if (body == CelestialBody.SUN) {
            renderSunGlow(poseStack, consumer, data.size);
        }
        
        poseStack.popPose();
    }
    
    /** 😡 渲染立方体（Minecraft 风格的方块星球） 😡
     */
    private static void renderSphere(
        PoseStack poseStack,
        VertexConsumer consumer,
        Vector3f color,
        float size,
        int segments,
        int rings
    ) {
        // 😡 使用 Starlance 的 RenderUtil 渲染立方体 😡
        com.example.rocketceg.client.RenderUtil.BoxLightMap lightMap = 
            new com.example.rocketceg.client.RenderUtil.BoxLightMap()
                .setAll(LightTexture.pack(15, 15));
        
        // 😡 计算立方体的尺寸（像素单位） 😡
        org.joml.Vector3i cubeSize = new org.joml.Vector3i(
            (int)(size * 16 / 10),
 馃槨
            (int)(size * 16 / 10),
 馃槨
            (int)(size * 16 / 10)
 馃槨
        );
        
        // 😡 渲染立方体的 6 个面 😡
        com.example.rocketceg.client.RenderUtil.drawBox(
            poseStack,
            consumer,
            lightMap,
            new org.joml.Vector4f(color.x, color.y, color.z, 1.0f),
            new org.joml.Vector3i(0, 0, 0),  // 😡 无偏移 😡
            new org.joml.Quaternionf(),      // 😡 无旋转 😡
            cubeSize
        );
    }
    
    /** 😡 渲染大气层（立方体外壳） 😡
     */
    private static void renderAtmosphere(
        PoseStack poseStack,
        VertexConsumer consumer,
        Vector3f color,
        float size
    ) {
        // 😡 使用 Starlance 的 RenderUtil 渲染大气层立方体 😡
        com.example.rocketceg.client.RenderUtil.BoxLightMap lightMap = 
            new com.example.rocketceg.client.RenderUtil.BoxLightMap()
                .setAll(LightTexture.pack(15, 15));
        
        // 😡 大气层立方体稍大 😡
        org.joml.Vector3i atmosphereSize = new org.joml.Vector3i(
            (int)(size * 16 / 10),
 馃槨
            (int)(size * 16 / 10),
 馃槨
            (int)(size * 16 / 10)
 馃槨
        );
        
        // 😡 渲染半透明的大气层立方体 😡
        com.example.rocketceg.client.RenderUtil.drawBox(
            poseStack,
            consumer,
            lightMap,
            new org.joml.Vector4f(color.x, color.y, color.z, 0.3f),
            new org.joml.Vector3i(0, 0, 0),
            new org.joml.Quaternionf(),
            atmosphereSize
        );
    }
    
    /** 😡 渲染太阳光晕（简化版） 😡
     */
    private static void renderSunGlow(
        PoseStack poseStack,
        VertexConsumer consumer,
        float baseSize
    ) {
        // 😡 渲染多层光晕立方体 😡
        com.example.rocketceg.client.RenderUtil.BoxLightMap lightMap = 
            new com.example.rocketceg.client.RenderUtil.BoxLightMap()
                .setAll(LightTexture.pack(15, 15));
        
        // 😡 3 层光晕 😡
        for (int layer = 1; layer <= 3; layer++) {
            float glowSize = baseSize * (1.0f + 0.2f * layer);
 馃槨
            float alpha = 0.2f / layer;
            
            org.joml.Vector3i glowCubeSize = new org.joml.Vector3i(
                (int)(glowSize * 16 / 10),
 馃槨
                (int)(glowSize * 16 / 10),
 馃槨
                (int)(glowSize * 16 / 10)
 馃槨
            );
            
            com.example.rocketceg.client.RenderUtil.drawBox(
                poseStack,
                consumer,
                lightMap,
                new org.joml.Vector4f(1.0f, 0.9f, 0.3f, alpha),
                new org.joml.Vector3i(0, 0, 0),
                new org.joml.Quaternionf(),
                glowCubeSize
            );
        }
    }
    
    /** 😡 获取天体数据 😡
     */
    private static BodyData getBodyData(CelestialBody body) {
        switch (body) {
            case SUN: return SUN_DATA;
            case EARTH: return EARTH_DATA;
            case MOON: return MOON_DATA;
            case MARS: return MARS_DATA;
            case VENUS: return VENUS_DATA;
            default: return null;
        }
    }
    
    /** 😡 计算天体位置（基于时间的简单轨道） 😡
     */
    public static Vector3f calculateCelestialPosition(CelestialBody body, float time, float distance) {
        float angle = time * 0.001f; // 😡 慢速旋转 😡
 馃槨
        
        switch (body) {
            case SUN:
                // 😡 太阳在主世界的固定位置 😡
                return new Vector3f(0, 300, 0);
                
            case EARTH:
                // 😡 地球在轨道维度的位置 😡
                return new Vector3f(0, -200, 0);
                
            case MOON:
                // 😡 月球围绕地球 😡
                float moonAngle = angle * 2.0f;
 馃槨
                return new Vector3f(
                    Mth.cos(moonAngle) * distance,
 馃槨
                    -150,
                    Mth.sin(moonAngle) * distance
 馃槨
                );
                
            case MARS:
                // 😡 火星位置 😡
                return new Vector3f(
                    Mth.cos(angle * 0.5f) * distance * 1.5f,
 馃槨
                    100,
                    Mth.sin(angle * 0.5f) * distance * 1.5f
 馃槨
                );
                
            case VENUS:
                // 😡 金星位置 😡
                return new Vector3f(
                    Mth.cos(angle * 1.5f) * distance * 0.7f,
 馃槨
                    50,
                    Mth.sin(angle * 1.5f) * distance * 0.7f
 馃槨
                );
                
            default:
                return new Vector3f(0, 0, 0);
        }
    }
}
