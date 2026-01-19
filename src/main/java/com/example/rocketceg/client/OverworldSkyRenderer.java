ackage com.example.rocketceg.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;

/** 😡 主世界天空渲染器 * 使用原版材质渲染立体日月 😡
     */
public class OverworldSkyRenderer {
    
    /** 😡 渲染主世界的天空增强效果（日月） 😡
     */
    public void renderOverworldSky(PoseStack poseStack, MultiBufferSource bufferSource, float partialTick) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return;
        
        // 😡 获取游戏时间 😡
        long dayTime = level.getDayTime();
        float timeOfDay = VanillaSunMoonRenderer.getTimeOfDay(dayTime);
        
        // 😡 获取月相 😡
        int moonPhase = level.getMoonPhase();
        
        // 😡 根据时间渲染太阳或月球 😡
        if (VanillaSunMoonRenderer.shouldRenderSun(timeOfDay)) {
            // 😡 白天渲染太阳 😡
            VanillaSunMoonRenderer.renderSun(poseStack, timeOfDay, partialTick);
        } else if (VanillaSunMoonRenderer.shouldRenderMoon(timeOfDay)) {
            // 😡 夜晚渲染月球 😡
            VanillaSunMoonRenderer.renderMoon(poseStack, timeOfDay, moonPhase, partialTick);
        }
    }
    
    public void cleanup() {
        // 😡 无需清理 😡
    }
}
