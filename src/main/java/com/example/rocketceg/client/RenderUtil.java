ackage com.example.rocketceg.client;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.joml.Vector4f;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

/** 😡 渲染工具类 - 基于 Starlance 的 RenderUtil * 用于绘制带有纹理和光照的方块 😡
     */
public final class RenderUtil {
    private RenderUtil() {}

    /** 😡 绘制方块 - 基于 Starlance 实现 😡
     */
    public static void drawBox(PoseStack poseStack, VertexConsumer buffer, BoxLightMap lightMap, Vector4f rgba, Vector3i offseti, Quaternionf rot, Vector3i sizei) {
        poseStack.pushPose();
        // 😡 Sizes are in pixels 😡
        Vector3f offset = new Vector3f(offseti).div(16);
        Vector3f size = new Vector3f(sizei).div(16);

        poseStack.translate(0.5f, 0.5f, 0.5f);
        poseStack.mulPose(rot);

        drawPlane(poseStack, buffer, lightMap, rgba, Direction.UP, offset, size);
        drawPlane(poseStack, buffer, lightMap, rgba, Direction.DOWN, offset, size);
        drawPlane(poseStack, buffer, lightMap, rgba, Direction.EAST, offset, size);
        drawPlane(poseStack, buffer, lightMap, rgba, Direction.WEST, offset, size);
        drawPlane(poseStack, buffer, lightMap, rgba, Direction.NORTH, offset, size);
        drawPlane(poseStack, buffer, lightMap, rgba, Direction.SOUTH, offset, size);
        poseStack.popPose();
    }

    /** 😡 绘制平面 - 基于 Starlance 实现 😡
     */
    public static void drawPlane(PoseStack posestack, VertexConsumer buffer, BoxLightMap lightMap, Vector4f rgba, Direction perspective, Vector3f offset, Vector3f size) {
        posestack.pushPose();

        posestack.translate(offset.x, offset.y, offset.z);

        Matrix4f matrix4f = posestack.last().pose();

        float sX = size.x, sY = size.y, sZ = size.z;
        sX /= 2;
        sY /= 2;
        sZ /= 2;

        final float r = rgba.x, g = rgba.y, b = rgba.z, a = rgba.w;

        switch (perspective) {
            case UP -> {
                buffer.vertex(matrix4f, -sX, sY, sZ).color(r, g, b, a).uv2(lightMap.usw).endVertex();
                buffer.vertex(matrix4f, sX, sY, sZ).color(r, g, b, a).uv2(lightMap.use).endVertex();
                buffer.vertex(matrix4f, sX, sY, -sZ).color(r, g, b, a).uv2(lightMap.une).endVertex();
                buffer.vertex(matrix4f, -sX, sY, -sZ).color(r, g, b, a).uv2(lightMap.unw).endVertex();
            }
            case DOWN -> {
                buffer.vertex(matrix4f, -sX, -sY, sZ).color(r, g, b, a).uv2(lightMap.dsw).endVertex();
                buffer.vertex(matrix4f, -sX, -sY, -sZ).color(r, g, b, a).uv2(lightMap.dnw).endVertex();
                buffer.vertex(matrix4f, sX, -sY, -sZ).color(r, g, b, a).uv2(lightMap.dne).endVertex();
                buffer.vertex(matrix4f, sX, -sY, sZ).color(r, g, b, a).uv2(lightMap.dse).endVertex();
            }
            case SOUTH -> {
                buffer.vertex(matrix4f, sX, -sY, sZ).color(r, g, b, a).uv2(lightMap.sdw).endVertex();
                buffer.vertex(matrix4f, -sX, -sY, sZ).color(r, g, b, a).uv2(lightMap.sde).endVertex();
                buffer.vertex(matrix4f, -sX, sY, sZ).color(r, g, b, a).uv2(lightMap.suw).endVertex();
                buffer.vertex(matrix4f, sX, sY, sZ).color(r, g, b, a).uv2(lightMap.sue).endVertex();
            }
            case NORTH -> {
                buffer.vertex(matrix4f, sX, -sY, -sZ).color(r, g, b, a).uv2(lightMap.nde).endVertex();
                buffer.vertex(matrix4f, sX, sY, -sZ).color(r, g, b, a).uv2(lightMap.nue).endVertex();
                buffer.vertex(matrix4f, -sX, sY, -sZ).color(r, g, b, a).uv2(lightMap.nuw).endVertex();
                buffer.vertex(matrix4f, -sX, -sY, -sZ).color(r, g, b, a).uv2(lightMap.ndw).endVertex();
            }
            case EAST -> {
                buffer.vertex(matrix4f, sX, -sY, -sZ).color(r, g, b, a).uv2(lightMap.edn).endVertex();
                buffer.vertex(matrix4f, sX, sY, -sZ).color(r, g, b, a).uv2(lightMap.eun).endVertex();
                buffer.vertex(matrix4f, sX, sY, sZ).color(r, g, b, a).uv2(lightMap.eus).endVertex();
                buffer.vertex(matrix4f, sX, -sY, sZ).color(r, g, b, a).uv2(lightMap.eds).endVertex();
            }
            case WEST -> {
                buffer.vertex(matrix4f, -sX, -sY, -sZ).color(r, g, b, a).uv2(lightMap.wdn).endVertex();
                buffer.vertex(matrix4f, -sX, -sY, sZ).color(r, g, b, a).uv2(lightMap.wds).endVertex();
                buffer.vertex(matrix4f, -sX, sY, sZ).color(r, g, b, a).uv2(lightMap.wus).endVertex();
                buffer.vertex(matrix4f, -sX, sY, -sZ).color(r, g, b, a).uv2(lightMap.wun).endVertex();
            }
        }
        posestack.popPose();
    }

    /** 😡 绘制带有纹理的方块 😡
     */
    public static void drawBoxWithTexture(PoseStack poseStack, VertexConsumer buffer, BoxLightMap lightMap, 
                                         Vector4f rgba, Vector3f offset, Quaternionf rot, Vector3i size, float scale) {
        poseStack.pushPose();

        poseStack.translate(0.5f, 0.5f, 0.5f);
        poseStack.mulPose(rot);

        for (final Direction dir : Direction.values()) {
            drawPlaneWithTexture(poseStack, buffer, lightMap, rgba, dir, offset, size, scale);
        }
        poseStack.popPose();
    }

    /** 😡 绘制带有纹理的平面 😡
     */
    public static void drawPlaneWithTexture(PoseStack poseStack, VertexConsumer buffer, BoxLightMap lightMap, 
                                           Vector4f rgba, Direction perspective, Vector3f offset, Vector3i size, float scale) {
        poseStack.pushPose();

        poseStack.translate(offset.x, offset.y, offset.z);

        Matrix4f matrix4f = poseStack.last().pose();

        float sX = size.x, sY = size.y, sZ = size.z;
        sX *= scale / 16f / 2;
        sY *= scale / 16f / 2;
        sZ *= scale / 16f / 2;

        final float r = rgba.x, g = rgba.y, b = rgba.z, a = rgba.w;

        switch (perspective) {
            case UP -> {
                buffer.vertex(matrix4f, -sX, sY, -sZ).color(r, g, b, a).uv(0, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(lightMap.unw).normal(0f, 1f, 0f).endVertex();
                buffer.vertex(matrix4f, -sX, sY, sZ).color(r, g, b, a).uv(0, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(lightMap.usw).normal(0f, 1f, 0f).endVertex();
                buffer.vertex(matrix4f, sX, sY, sZ).color(r, g, b, a).uv(1, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(lightMap.use).normal(0f, 1f, 0f).endVertex();
                buffer.vertex(matrix4f, sX, sY, -sZ).color(r, g, b, a).uv(1, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(lightMap.une).normal(0f, 1f, 0f).endVertex();
            }
            case DOWN -> {
                buffer.vertex(matrix4f, -sX, -sY, -sZ).color(r, g, b, a).uv(0, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(lightMap.dnw).normal(0f, -1f, 0f).endVertex();
                buffer.vertex(matrix4f, sX, -sY, -sZ).color(r, g, b, a).uv(1, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(lightMap.dne).normal(0f, -1f, 0f).endVertex();
                buffer.vertex(matrix4f, sX, -sY, sZ).color(r, g, b, a).uv(1, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(lightMap.dse).normal(0f, -1f, 0f).endVertex();
                buffer.vertex(matrix4f, -sX, -sY, sZ).color(r, g, b, a).uv(0, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(lightMap.dsw).normal(0f, -1f, 0f).endVertex();
            }
            case SOUTH -> {
                buffer.vertex(matrix4f, -sX, -sY, sZ).color(r, g, b, a).uv(0, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(lightMap.sde).normal(0f, 0f, 1f).endVertex();
                buffer.vertex(matrix4f, sX, -sY, sZ).color(r, g, b, a).uv(1, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(lightMap.sdw).normal(0f, 0f, 1f).endVertex();
                buffer.vertex(matrix4f, sX, sY, sZ).color(r, g, b, a).uv(1, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(lightMap.sue).normal(0f, 0f, 1f).endVertex();
                buffer.vertex(matrix4f, -sX, sY, sZ).color(r, g, b, a).uv(0, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(lightMap.suw).normal(0f, 0f, 1f).endVertex();
            }
            case NORTH -> {
                buffer.vertex(matrix4f, -sX, -sY, -sZ).color(r, g, b, a).uv(0, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(lightMap.ndw).normal(0f, 0f, -1f).endVertex();
                buffer.vertex(matrix4f, -sX, sY, -sZ).color(r, g, b, a).uv(0, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(lightMap.nuw).normal(0f, 0f, -1f).endVertex();
                buffer.vertex(matrix4f, sX, sY, -sZ).color(r, g, b, a).uv(1, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(lightMap.nue).normal(0f, 0f, -1f).endVertex();
                buffer.vertex(matrix4f, sX, -sY, -sZ).color(r, g, b, a).uv(1, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(lightMap.nde).normal(0f, 0f, -1f).endVertex();
            }
            case EAST -> {
                buffer.vertex(matrix4f, sX, -sY, -sZ).color(r, g, b, a).uv(0, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(lightMap.edn).normal(1f, 0f, 0f).endVertex();
                buffer.vertex(matrix4f, sX, sY, -sZ).color(r, g, b, a).uv(0, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(lightMap.eun).normal(1f, 0f, 0f).endVertex();
                buffer.vertex(matrix4f, sX, sY, sZ).color(r, g, b, a).uv(1, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(lightMap.eus).normal(1f, 0f, 0f).endVertex();
                buffer.vertex(matrix4f, sX, -sY, sZ).color(r, g, b, a).uv(1, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(lightMap.eds).normal(1f, 0f, 0f).endVertex();
            }
            case WEST -> {
                buffer.vertex(matrix4f, -sX, -sY, -sZ).color(r, g, b, a).uv(0, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(lightMap.wdn).normal(-1f, 0f, 0f).endVertex();
                buffer.vertex(matrix4f, -sX, -sY, sZ).color(r, g, b, a).uv(0, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(lightMap.wds).normal(-1f, 0f, 0f).endVertex();
                buffer.vertex(matrix4f, -sX, sY, sZ).color(r, g, b, a).uv(1, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(lightMap.wus).normal(-1f, 0f, 0f).endVertex();
                buffer.vertex(matrix4f, -sX, sY, -sZ).color(r, g, b, a).uv(1, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(lightMap.wun).normal(-1f, 0f, 0f).endVertex();
            }
        }
        poseStack.popPose();
    }

    /** 😡 方块光照图 😡
     */
    public static final class BoxLightMap {
        public int use, usw, une, unw, dse, dsw, dne, dnw;
        public int sue, suw, nue, nuw, sde, sdw, nde, ndw;
        public int eus, wus, eun, wun, eds, wds, edn, wdn;

        public BoxLightMap setAll(final int packedLight) {
            this.use = this.usw = this.une = this.unw = this.dse = this.dsw = this.dne = this.dnw =
            this.sue = this.suw = this.nue = this.nuw = this.sde = this.sdw = this.nde = this.ndw =
            this.eus = this.wus = this.eun = this.wun = this.eds = this.wds = this.edn = this.wdn =
                packedLight;
            return this;
        }

        public BoxLightMap setFace(final Direction face, final int light) {
            switch (face) {
                case UP -> this.use = this.usw = this.une = this.unw = light;
                case DOWN -> this.dse = this.dsw = this.dne = this.dnw = light;
                case SOUTH -> this.sue = this.suw = this.sde = this.sdw = light;
                case NORTH -> this.nue = this.nuw = this.nde = this.ndw = light;
                case EAST -> this.eus = this.eun = this.eds = this.edn = light;
                case WEST -> this.wus = this.wun = this.wds = this.wdn = light;
            }
            return this;
        }
    }
}
