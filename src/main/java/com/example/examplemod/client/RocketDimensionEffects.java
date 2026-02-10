package com.example.examplemod.client;

import com.example.examplemod.rocket.RocketDimensionTransition;
import com.example.examplemod.rocket.RocketDimensions;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.Camera;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Consumer;

public class RocketDimensionEffects extends DimensionSpecialEffects {
    public static final Set<Predicate<Object[]>> CUSTOM_SKY = new HashSet<>();
    public static final Set<Predicate<Object[]>> CUSTOM_CLOUDS = new HashSet<>();
    public static final Set<Predicate<Object[]>> CUSTOM_WEATHER = new HashSet<>();
    public static final Set<Predicate<Object[]>> CUSTOM_EFFECTS = new HashSet<>();
    public static final Set<Consumer<Object[]>> CUSTOM_LIGHTS = new HashSet<>();

    public RocketDimensionEffects(float cloudHeight, boolean hasGround, SkyType skyType,
                                  boolean forceBrightLightmap, boolean constantAmbientLight) {
        super(cloudHeight, hasGround, skyType, forceBrightLightmap, constantAmbientLight);
    }

    @Override
    public Vec3 getBrightnessDependentFogColor(Vec3 color, float sunHeight) {

        return new Vec3(0.01, 0.01, 0.02);
    }

    @Override
    public boolean renderSky(ClientLevel level, int ticks, float partialTick, PoseStack poseStack, Camera camera,
                             Matrix4f projectionMatrix, boolean isFoggy, Runnable setupFog) {
        if (CUSTOM_SKY.isEmpty()) {
            return false;
        }
        boolean handled = false;
        Object[] args = { level, ticks, partialTick, poseStack, camera, projectionMatrix, isFoggy, setupFog };
        for (Predicate<Object[]> predicate : CUSTOM_SKY) {
            RenderSystem.depthMask(false);
            RenderSystem.enableDepthTest();
            RenderSystem.enableCull();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            handled |= predicate.test(args);
        }
        if (handled) {
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableBlend();
            RenderSystem.enableCull();
            RenderSystem.enableDepthTest();
            RenderSystem.depthMask(true);
        }
        return handled;
    }

    @Override
    public boolean renderClouds(ClientLevel level, int ticks, float partialTick, PoseStack poseStack,
                                double camX, double camY, double camZ, Matrix4f projectionMatrix) {

        if (CUSTOM_CLOUDS.isEmpty()) {
            return true;
        }
        boolean handled = false;
        Object[] args = { level, ticks, partialTick, poseStack, camX, camY, camZ, projectionMatrix };
        for (Predicate<Object[]> predicate : CUSTOM_CLOUDS) {
            handled |= predicate.test(args);
        }
        return handled;
    }

    @Override
    public boolean renderSnowAndRain(ClientLevel level, int ticks, float partialTick, LightTexture lightTexture,
                                     double camX, double camY, double camZ) {
        if (CUSTOM_WEATHER.isEmpty()) {
            return false;
        }
        boolean handled = false;
        Object[] args = { level, ticks, partialTick, lightTexture, camX, camY, camZ };
        for (Predicate<Object[]> predicate : CUSTOM_WEATHER) {
            handled |= predicate.test(args);
        }
        return handled;
    }

    @Override
    public boolean tickRain(ClientLevel level, int ticks, Camera camera) {
        if (CUSTOM_EFFECTS.isEmpty()) {
            return false;
        }
        boolean handled = false;
        Object[] args = { level, ticks, camera };
        for (Predicate<Object[]> predicate : CUSTOM_EFFECTS) {
            handled |= predicate.test(args);
        }
        return handled;
    }

    @Override
    public void adjustLightmapColors(ClientLevel level, float partialTick, float skyDarken,
                                     float blockLightRedFlicker, float skyLight, int pixelX, int pixelY,
                                     org.joml.Vector3f colors) {
        if (CUSTOM_LIGHTS.isEmpty()) {
            return;
        }
        Object[] args = { level, partialTick, skyDarken, blockLightRedFlicker, skyLight, pixelX, pixelY, colors };
        for (Consumer<Object[]> consumer : CUSTOM_LIGHTS) {
            consumer.accept(args);
        }
    }

    @Override
    public boolean isFoggyAt(int x, int z) {
        return false;
    }
}
