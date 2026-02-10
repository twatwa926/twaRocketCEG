package com.example.examplemod.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;

public final class RocketRenderHelper {
    private RocketRenderHelper() {}

    private static volatile Boolean hasReflection = null;
    private static java.lang.reflect.Field fieldRenderBuffers;
    private static java.lang.reflect.Field fieldBufferSource;

    static {
        try {
            fieldRenderBuffers = Minecraft.class.getDeclaredField("renderBuffers");
            fieldRenderBuffers.setAccessible(true);
            Class<?> rb = fieldRenderBuffers.getType();
            fieldBufferSource = rb.getDeclaredField("bufferSource");
            fieldBufferSource.setAccessible(true);
            hasReflection = true;
        } catch (Throwable t) {
            hasReflection = false;
        }
    }

    public static MultiBufferSource getLevelBufferSource(Minecraft mc) {
        if (mc == null || !Boolean.TRUE.equals(hasReflection)) return null;
        try {
            Object renderBuffers = fieldRenderBuffers.get(mc);
            if (renderBuffers == null) return null;
            return (MultiBufferSource) fieldBufferSource.get(renderBuffers);
        } catch (Throwable t) {
            return null;
        }
    }
}
