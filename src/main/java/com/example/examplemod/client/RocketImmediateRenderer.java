package com.example.examplemod.client;

import com.example.examplemod.rocket.ship.RocketShipEntity;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.BufferUploader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.Map;

public final class RocketImmediateRenderer {

    private RocketImmediateRenderer() {}

    public static void drawRocketsImmediate(PoseStack poseStack, float partialTick, Vec3 cameraPos) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        double camX = cameraPos.x, camY = cameraPos.y, camZ = cameraPos.z;

        var rockets = mc.level.getEntitiesOfClass(
                RocketShipEntity.class,
                new AABB(camX - 128, camY - 128, camZ - 128, camX + 128, camY + 128, camZ + 128));

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.lineWidth(2.0f);

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buf = tesselator.getBuilder();
        buf.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR);

        poseStack.pushPose();
        poseStack.translate(-camX, -camY, -camZ);
        Matrix4f viewMat = poseStack.last().pose();
        poseStack.popPose();

        Matrix4f identity = new Matrix4f();
        for (RocketShipEntity ship : rockets) {
            if (!ship.isAlive()) continue;
            try {
                drawOneRocketLinesWorld(ship, partialTick, identity, buf);
            } catch (Throwable t) {
                drawFallbackBoxWorld(ship, partialTick, identity, buf);
            }
        }

        var mvStack = RenderSystem.getModelViewStack();
        mvStack.pushPose();
        mvStack.mulPoseMatrix(viewMat);
        RenderSystem.applyModelViewMatrix();
        BufferUploader.drawWithShader(buf.end());
        mvStack.popPose();
        RenderSystem.applyModelViewMatrix();

        RenderSystem.lineWidth(1.0f);
    }

    private static void drawOneRocketLinesWorld(RocketShipEntity ship, float partialTick, Matrix4f mat, BufferBuilder buf) {
        Vec3 renderPos = ship.getRenderPosition(partialTick);
        double x = renderPos.x;
        double y = renderPos.y;
        double z = renderPos.z;
        Vec3 pivot = ship.getRotationPivot();
        float yawDeg = ship.getRenderYaw(partialTick);
        float pitchDeg = ship.getRenderPitch(partialTick);
        float rollDeg = ship.getRenderRoll(partialTick);
        float yaw = (float) Math.toRadians(yawDeg);
        float pitch = (float) Math.toRadians(pitchDeg);
        float roll = (float) Math.toRadians(rollDeg);

        Map<BlockPos, net.minecraft.world.level.block.state.BlockState> blocks = ship.getStorageBlocks();
        float r = 0.4f, g = 0.7f, b = 1.0f, a = 1.0f;

        if (blocks != null && !blocks.isEmpty()) {
            for (Map.Entry<BlockPos, net.minecraft.world.level.block.state.BlockState> e : blocks.entrySet()) {
                if (e.getValue() == null || e.getValue().isAir()) continue;
                float px = e.getKey().getX();
                float py = e.getKey().getY();
                float pz = e.getKey().getZ();
                drawLineBoxWorld(mat, buf, x, y, z, pivot, yaw, pitch, roll, px, py, pz, 1f, r, g, b, a);
            }
        } else {
            drawLineBoxWorld(mat, buf, x, y, z, pivot, yaw, pitch, roll, -0.5f, 0f, -0.5f, 1f, 1f, 0.8f, 0.2f, 1f);
        }
    }

    private static void drawFallbackBoxWorld(RocketShipEntity ship, float partialTick, Matrix4f mat, BufferBuilder buf) {
        Vec3 renderPos = ship.getRenderPosition(partialTick);
        double x = renderPos.x;
        double y = renderPos.y;
        double z = renderPos.z;
        drawLineBoxWorld(mat, buf, x, y, z, Vec3.ZERO, 0, 0, 0, -1f, 0f, -1f, 2f, 1f, 0f, 0f, 1f);
    }

    private static void drawLineBoxWorld(Matrix4f mat, BufferBuilder buf,
            double shipX, double shipY, double shipZ, Vec3 pivot, float yaw, float pitch, float roll,
            float lx0, float ly0, float lz0, float size, float r, float g, float b, float a) {
        float lx1 = lx0 + size;
        float ly1 = ly0 + size;
        float lz1 = lz0 + size;
        float[] corners = {
            lx0 - (float) pivot.x, ly0 - (float) pivot.y, lz0 - (float) pivot.z,
            lx1 - (float) pivot.x, ly0 - (float) pivot.y, lz0 - (float) pivot.z,
            lx1 - (float) pivot.x, ly0 - (float) pivot.y, lz1 - (float) pivot.z,
            lx0 - (float) pivot.x, ly0 - (float) pivot.y, lz1 - (float) pivot.z,
            lx0 - (float) pivot.x, ly1 - (float) pivot.y, lz0 - (float) pivot.z,
            lx1 - (float) pivot.x, ly1 - (float) pivot.y, lz0 - (float) pivot.z,
            lx1 - (float) pivot.x, ly1 - (float) pivot.y, lz1 - (float) pivot.z,
            lx0 - (float) pivot.x, ly1 - (float) pivot.y, lz1 - (float) pivot.z
        };
        for (int i = 0; i < 8; i++) {
            float lx = corners[i * 3];
            float ly = corners[i * 3 + 1];
            float lz = corners[i * 3 + 2];
            float wx = (float) (shipX + rotateX(lx, ly, lz, yaw, pitch, roll));
            float wy = (float) (shipY + rotateY(lx, ly, lz, yaw, pitch, roll));
            float wz = (float) (shipZ + rotateZ(lx, ly, lz, yaw, pitch, roll));
            corners[i * 3] = wx;
            corners[i * 3 + 1] = wy;
            corners[i * 3 + 2] = wz;
        }
        int c = ((int)(a*255)<<24) | ((int)(b*255)<<16) | ((int)(g*255)<<8) | (int)(r*255);
        addLine(mat, buf, corners[0], corners[1], corners[2], corners[3], corners[4], corners[5], c);
        addLine(mat, buf, corners[3], corners[4], corners[5], corners[6], corners[7], corners[8], c);
        addLine(mat, buf, corners[6], corners[7], corners[8], corners[9], corners[10], corners[11], c);
        addLine(mat, buf, corners[9], corners[10], corners[11], corners[0], corners[1], corners[2], c);
        addLine(mat, buf, corners[12], corners[13], corners[14], corners[15], corners[16], corners[17], c);
        addLine(mat, buf, corners[15], corners[16], corners[17], corners[18], corners[19], corners[20], c);
        addLine(mat, buf, corners[18], corners[19], corners[20], corners[21], corners[22], corners[23], c);
        addLine(mat, buf, corners[21], corners[22], corners[23], corners[12], corners[13], corners[14], c);
        addLine(mat, buf, corners[0], corners[1], corners[2], corners[12], corners[13], corners[14], c);
        addLine(mat, buf, corners[3], corners[4], corners[5], corners[15], corners[16], corners[17], c);
        addLine(mat, buf, corners[6], corners[7], corners[8], corners[18], corners[19], corners[20], c);
        addLine(mat, buf, corners[9], corners[10], corners[11], corners[21], corners[22], corners[23], c);
    }

    private static double rotateX(float lx, float ly, float lz, float yaw, float pitch, float roll) {
        double cY = Math.cos(yaw), sY = Math.sin(yaw);
        double cP = Math.cos(pitch), sP = Math.sin(pitch);
        double cR = Math.cos(roll), sR = Math.sin(roll);
        double x = lx, y = ly, z = lz;
        double t = x * cY + z * sY; z = -x * sY + z * cY; x = t;
        t = y * cP - z * sP; z = y * sP + z * cP; y = t;
        t = x * cR + y * sR; y = -x * sR + y * cR; x = t;
        return x;
    }
    private static double rotateY(float lx, float ly, float lz, float yaw, float pitch, float roll) {
        double cY = Math.cos(yaw), sY = Math.sin(yaw);
        double cP = Math.cos(pitch), sP = Math.sin(pitch);
        double cR = Math.cos(roll), sR = Math.sin(roll);
        double x = lx, y = ly, z = lz;
        double t = x * cY + z * sY; z = -x * sY + z * cY; x = t;
        t = y * cP - z * sP; z = y * sP + z * cP; y = t;
        t = x * cR + y * sR; y = -x * sR + y * cR; x = t;
        return y;
    }
    private static double rotateZ(float lx, float ly, float lz, float yaw, float pitch, float roll) {
        double cY = Math.cos(yaw), sY = Math.sin(yaw);
        double cP = Math.cos(pitch), sP = Math.sin(pitch);
        double cR = Math.cos(roll), sR = Math.sin(roll);
        double x = lx, y = ly, z = lz;
        double t = x * cY + z * sY; z = -x * sY + z * cY; x = t;
        t = y * cP - z * sP; z = y * sP + z * cP; y = t;
        t = x * cR + y * sR; y = -x * sR + y * cR; x = t;
        return z;
    }

    private static void addLine(Matrix4f mat, BufferBuilder buf, float x1, float y1, float z1, float x2, float y2, float z2, int color) {
        float r = ((color) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = ((color >> 16) & 0xFF) / 255f;
        float a = ((color >> 24) & 0xFF) / 255f;
        buf.vertex(mat, x1, y1, z1).color(r, g, b, a).endVertex();
        buf.vertex(mat, x2, y2, z2).color(r, g, b, a).endVertex();
    }
}
