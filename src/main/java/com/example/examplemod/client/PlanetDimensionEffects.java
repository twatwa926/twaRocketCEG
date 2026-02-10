package com.example.examplemod.client;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.rocket.RocketDimensionTransition;
import com.example.examplemod.rocket.RocketDimensions;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.Camera;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public class PlanetDimensionEffects extends DimensionSpecialEffects {

    public static final ResourceLocation SKY_PLACEHOLDER = new ResourceLocation(ExampleMod.MODID, "textures/environment/rocket_sky.png");

    public static final String SKY_PLACEHOLDER_PREFIX = ExampleMod.MODID + ":textures/environment/sky_";

    public PlanetDimensionEffects() {
        super(192.0f, true, SkyType.NORMAL, false, false);
    }

    @Override
    public boolean renderSky(ClientLevel level, int ticks, float partialTick, PoseStack poseStack, Camera camera,
                             Matrix4f projectionMatrix, boolean isFoggy, Runnable setupFog) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, SKY_PLACEHOLDER);
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(level.getSunAngle(partialTick) * 360.0f));
        Matrix4f matrix = poseStack.last().pose();

        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        float size = 120.0f;
        buffer.vertex(matrix, -size, 100.0f, -size).uv(0.0f, 0.0f).endVertex();
        buffer.vertex(matrix, -size, 100.0f, size).uv(0.0f, 1.0f).endVertex();
        buffer.vertex(matrix, size, 100.0f, size).uv(1.0f, 1.0f).endVertex();
        buffer.vertex(matrix, size, 100.0f, -size).uv(1.0f, 0.0f).endVertex();
        BufferUploader.drawWithShader(buffer.end());

        RocketSkyRenderer.renderSolarSystemStatic(level, ticks, partialTick, poseStack);

        poseStack.popPose();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
        setupFog.run();
        return true;
    }

    public static final Vec3 FOG_SURFACE = new Vec3(0.52, 0.62, 0.92);

    public static final Vec3 FOG_SPACE = new Vec3(0.0, 0.0, 0.0);

    public static final double FOG_TRANSITION_START = 200.0;

    public static Vec3 computeFogColorForDimension(ResourceKey<Level> dimension, double cameraY, float sunHeight) {
        if (dimension == null) return null;
        float mul = sunHeight * 0.9f + 0.1f;
        float blend = 0f;
        if (dimension.equals(RocketDimensions.EARTH) || dimension.equals(Level.OVERWORLD)) {
            blend = (float) Mth.clamp((cameraY - FOG_TRANSITION_START) / (RocketDimensionTransition.getOrbitThreshold() - FOG_TRANSITION_START), 0, 1);
        } else if (dimension.equals(RocketDimensions.EARTH_ORBIT)) {
            blend = 1f;
        } else {

            if (dimension.location().getNamespace().equals(ExampleMod.MODID)) {
                blend = 1f;
            } else {
                return null;
            }
        }
        Vec3 fog = FOG_SURFACE.lerp(FOG_SPACE, blend);
        return new Vec3(fog.x * mul, fog.y * mul, fog.z * mul);
    }

    @Override
    public Vec3 getBrightnessDependentFogColor(Vec3 color, float sunHeight) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null && mc.gameRenderer != null) {
            Vec3 fog = computeFogColorForDimension(
                mc.level.dimension(),
                mc.gameRenderer.getMainCamera().getPosition().y,
                sunHeight
            );
            if (fog != null) return fog;
        }
        float mul = sunHeight * 0.9f + 0.1f;
        return color.multiply(mul, mul, sunHeight * 0.95f + 0.05f);
    }

    @Override
    public boolean isFoggyAt(int x, int z) {
        return false;
    }
}
