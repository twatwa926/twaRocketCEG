package com.example.examplemod.client;

import com.example.examplemod.ExampleMod;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.math.Axis;
import com.example.examplemod.rocket.RocketDimensions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.Camera;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.client.event.RegisterDimensionSpecialEffectsEvent;
import org.joml.Matrix4f;

import java.lang.reflect.Field;
import java.util.Set;

@Mod.EventBusSubscriber(modid = ExampleMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class RocketSkyRenderer {
    private static final ResourceLocation ROCKET_SKY = new ResourceLocation(ExampleMod.MODID, "textures/environment/rocket_sky.png");

    @SubscribeEvent
    public static void registerEffects(RegisterDimensionSpecialEffectsEvent event) {
        RocketDimensionEffects effects = new RocketDimensionEffects(192.0f, true, DimensionSpecialEffects.SkyType.NORMAL, false, false);
        event.register(Level.OVERWORLD.location(), effects);
    }

    private static boolean isSpaceDimension(Level level) {
        if (level == null) return false;
        var dim = level.dimension();
        if (dim.equals(RocketDimensions.EARTH_ORBIT)) return true;
        if (dim.location().getNamespace().equals(ExampleMod.MODID)
                && !dim.equals(RocketDimensions.EARTH)) return true;
        return false;
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        RocketDimensionEffects.CUSTOM_SKY.add(params -> {
            ClientLevel level = (ClientLevel) params[0];
            PoseStack poseStack = (PoseStack) params[3];
            Runnable setupFog = (Runnable) params[7];

            RenderSystem.disableCull();
            RenderSystem.disableBlend();
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

            poseStack.pushPose();
            Matrix4f matrix = poseStack.last().pose();
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            BufferBuilder buffer = Tesselator.getInstance().getBuilder();
            float clearSize = 500.0f;
            buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

            buffer.vertex(matrix, -clearSize,  clearSize, -clearSize).color(0, 0, 0, 255).endVertex();
            buffer.vertex(matrix,  clearSize,  clearSize, -clearSize).color(0, 0, 0, 255).endVertex();
            buffer.vertex(matrix,  clearSize,  clearSize,  clearSize).color(0, 0, 0, 255).endVertex();
            buffer.vertex(matrix, -clearSize,  clearSize,  clearSize).color(0, 0, 0, 255).endVertex();

            buffer.vertex(matrix, -clearSize, -clearSize,  clearSize).color(0, 0, 0, 255).endVertex();
            buffer.vertex(matrix,  clearSize, -clearSize,  clearSize).color(0, 0, 0, 255).endVertex();
            buffer.vertex(matrix,  clearSize, -clearSize, -clearSize).color(0, 0, 0, 255).endVertex();
            buffer.vertex(matrix, -clearSize, -clearSize, -clearSize).color(0, 0, 0, 255).endVertex();

            buffer.vertex(matrix, -clearSize,  clearSize, -clearSize).color(0, 0, 0, 255).endVertex();
            buffer.vertex(matrix, -clearSize, -clearSize, -clearSize).color(0, 0, 0, 255).endVertex();
            buffer.vertex(matrix,  clearSize, -clearSize, -clearSize).color(0, 0, 0, 255).endVertex();
            buffer.vertex(matrix,  clearSize,  clearSize, -clearSize).color(0, 0, 0, 255).endVertex();

            buffer.vertex(matrix,  clearSize,  clearSize,  clearSize).color(0, 0, 0, 255).endVertex();
            buffer.vertex(matrix,  clearSize, -clearSize,  clearSize).color(0, 0, 0, 255).endVertex();
            buffer.vertex(matrix, -clearSize, -clearSize,  clearSize).color(0, 0, 0, 255).endVertex();
            buffer.vertex(matrix, -clearSize,  clearSize,  clearSize).color(0, 0, 0, 255).endVertex();

            buffer.vertex(matrix,  clearSize,  clearSize, -clearSize).color(0, 0, 0, 255).endVertex();
            buffer.vertex(matrix,  clearSize, -clearSize, -clearSize).color(0, 0, 0, 255).endVertex();
            buffer.vertex(matrix,  clearSize, -clearSize,  clearSize).color(0, 0, 0, 255).endVertex();
            buffer.vertex(matrix,  clearSize,  clearSize,  clearSize).color(0, 0, 0, 255).endVertex();

            buffer.vertex(matrix, -clearSize,  clearSize,  clearSize).color(0, 0, 0, 255).endVertex();
            buffer.vertex(matrix, -clearSize, -clearSize,  clearSize).color(0, 0, 0, 255).endVertex();
            buffer.vertex(matrix, -clearSize, -clearSize, -clearSize).color(0, 0, 0, 255).endVertex();
            buffer.vertex(matrix, -clearSize,  clearSize, -clearSize).color(0, 0, 0, 255).endVertex();
            BufferUploader.drawWithShader(buffer.end());

            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, ROCKET_SKY);

            float s = 300.0f;
            float top = s;
            float bot = s * 0.3f;

            buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
            buffer.vertex(matrix, -s,  top, -s).uv(0.0f, 0.0f).endVertex();
            buffer.vertex(matrix,  s,  top, -s).uv(1.0f, 0.0f).endVertex();
            buffer.vertex(matrix,  s,  top,  s).uv(1.0f, 1.0f).endVertex();
            buffer.vertex(matrix, -s,  top,  s).uv(0.0f, 1.0f).endVertex();
            BufferUploader.drawWithShader(buffer.end());

            buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
            buffer.vertex(matrix, -s, -bot,  s).uv(0.0f, 0.0f).endVertex();
            buffer.vertex(matrix,  s, -bot,  s).uv(1.0f, 0.0f).endVertex();
            buffer.vertex(matrix,  s, -bot, -s).uv(1.0f, 1.0f).endVertex();
            buffer.vertex(matrix, -s, -bot, -s).uv(0.0f, 1.0f).endVertex();
            BufferUploader.drawWithShader(buffer.end());

            buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
            buffer.vertex(matrix, -s,  top, -s).uv(0.0f, 0.0f).endVertex();
            buffer.vertex(matrix, -s, -bot, -s).uv(0.0f, 1.0f).endVertex();
            buffer.vertex(matrix,  s, -bot, -s).uv(1.0f, 1.0f).endVertex();
            buffer.vertex(matrix,  s,  top, -s).uv(1.0f, 0.0f).endVertex();
            BufferUploader.drawWithShader(buffer.end());

            buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
            buffer.vertex(matrix,  s,  top,  s).uv(0.0f, 0.0f).endVertex();
            buffer.vertex(matrix,  s, -bot,  s).uv(0.0f, 1.0f).endVertex();
            buffer.vertex(matrix, -s, -bot,  s).uv(1.0f, 1.0f).endVertex();
            buffer.vertex(matrix, -s,  top,  s).uv(1.0f, 0.0f).endVertex();
            BufferUploader.drawWithShader(buffer.end());

            buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
            buffer.vertex(matrix,  s,  top, -s).uv(0.0f, 0.0f).endVertex();
            buffer.vertex(matrix,  s, -bot, -s).uv(0.0f, 1.0f).endVertex();
            buffer.vertex(matrix,  s, -bot,  s).uv(1.0f, 1.0f).endVertex();
            buffer.vertex(matrix,  s,  top,  s).uv(1.0f, 0.0f).endVertex();
            BufferUploader.drawWithShader(buffer.end());

            buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
            buffer.vertex(matrix, -s,  top,  s).uv(0.0f, 0.0f).endVertex();
            buffer.vertex(matrix, -s, -bot,  s).uv(0.0f, 1.0f).endVertex();
            buffer.vertex(matrix, -s, -bot, -s).uv(1.0f, 1.0f).endVertex();
            buffer.vertex(matrix, -s,  top, -s).uv(1.0f, 0.0f).endVertex();
            BufferUploader.drawWithShader(buffer.end());

            poseStack.popPose();

            RenderSystem.enableCull();
            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();
            setupFog.run();
            return true;
        });
    }

    private static final float RADIUS_SCALE = 0.14f;
    private static final float ORBIT_SCALE = 0.04f;
    private static final float BASE_RADIUS = 110.0f;
    private static final float BASE_Y = 90.0f;

    private static float visualRadiusMeters(double radiusMeters) {
        if (radiusMeters <= 0) return 2.0f;
        return (float) (RADIUS_SCALE * Math.log10(1.0 + radiusMeters));
    }

    private static float visualOrbitMeters(double orbitMeters) {
        if (orbitMeters <= 0) return 0.0f;
        return (float) (ORBIT_SCALE * Math.log10(1.0 + orbitMeters));
    }

    public static void renderSolarSystemStatic(ClientLevel level, int ticks, float partialTick, PoseStack poseStack) {
        RocketSolarSystem system = RocketSolarSystem.getOrLoad();
        if (system.bodies == null || system.bodies.isEmpty()) {
            return;
        }

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        poseStack.pushPose();
        poseStack.mulPose(Axis.XP.rotationDegrees(18.0f));
        Matrix4f matrix = poseStack.last().pose();

        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        float time = ticks + partialTick;

        for (RocketSolarSystem.CelestialBody body : system.bodies) {
            double orbitM = body.getOrbitMeters();
            float orbitDisplay = orbitM > 0 ? visualOrbitMeters(orbitM) : 0.0f;
            float theta = body.orbitPeriodDays <= 0 ? 0.0f : (time / (body.orbitPeriodDays * 24000.0f)) * 360.0f * (float) Math.PI / 180.0f;
            float incRad = body.inclinationDeg * (float) Math.PI / 180.0f;
            float dist = BASE_RADIUS + orbitDisplay;
            float x = (float) Math.cos(theta) * dist;
            float zFlat = (float) Math.sin(theta) * dist;
            float z = zFlat * (float) Math.cos(incRad);
            float y = BASE_Y + zFlat * (float) Math.sin(incRad);

            double radiusM = body.getRadiusMeters();
            float size = Math.max(1.2f, visualRadiusMeters(radiusM));

            int color = body.color;
            float r = ((color >> 16) & 0xFF) / 255.0f;
            float g = ((color >> 8) & 0xFF) / 255.0f;
            float b = (color & 0xFF) / 255.0f;

            buffer.vertex(matrix, x - size, y, z - size).color(r, g, b, 1.0f).endVertex();
            buffer.vertex(matrix, x - size, y, z + size).color(r, g, b, 1.0f).endVertex();
            buffer.vertex(matrix, x + size, y, z + size).color(r, g, b, 1.0f).endVertex();
            buffer.vertex(matrix, x + size, y, z - size).color(r, g, b, 1.0f).endVertex();
        }

        BufferUploader.drawWithShader(buffer.end());
        poseStack.popPose();
    }
}
