package com.example.examplemod.client;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.rocket.ship.RocketShipEntity;
import com.example.examplemod.rocket.ship.RocketShipWorld;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;

@Mod.EventBusSubscriber(modid = ExampleMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class RocketClientRenderers {
    private RocketClientRenderers() {}

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ExampleMod.ROCKET_SHIP_ENTITY.get(), RocketShipBlockRenderer::new);
        event.registerEntityRenderer(ExampleMod.ROCKET_BOOSTER_ENTITY.get(), RocketBoosterRenderer::new);
    }

    public static class RocketShipBlockRenderer extends EntityRenderer<RocketShipEntity> {

        public RocketShipBlockRenderer(EntityRendererProvider.Context context) {
            super(context);
        }

        @Override
        public boolean shouldRender(RocketShipEntity entity, Frustum frustum, double camX, double camY, double camZ) {
            if (entity == null || !entity.isAlive()) return false;
            AABB box = entity.getBoundingBox();
            if (box.getXsize() < 0.01 || box.getYsize() < 0.01 || box.getZsize() < 0.01) {
                double x = entity.getX(), y = entity.getY(), z = entity.getZ();
                box = new AABB(x - 2, y - 1, z - 2, x + 2, y + 3, z + 2);
            }
            return frustum.isVisible(box.inflate(16.0));
        }

        @Override
        public void render(RocketShipEntity entity, float entityYaw, float partialTicks, PoseStack poseStack,
                           MultiBufferSource buffer, int packedLight) {
            if (entity == null || !entity.isAlive()) return;

            try {

                if (entity.level().isClientSide()) {
                    try { entity.ensureClientContraptionReady(); } catch (Throwable ignored) {}
                }

                renderBlocksWithTextures(entity, partialTicks, poseStack, buffer, packedLight);
            } catch (Throwable t) {

                renderFallbackBox(entity, partialTicks, poseStack, buffer);
            }
        }

        @Override
        public ResourceLocation getTextureLocation(RocketShipEntity entity) {
            return new ResourceLocation(ExampleMod.MODID, "textures/entity/rocket_ship.png");
        }

        private static void renderBlocksWithTextures(RocketShipEntity entity, float partialTicks,
                                                     PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
            Map<BlockPos, BlockState> blocks = entity.getStorageBlocks();
            if (blocks == null || blocks.isEmpty()) {
                renderFallbackBox(entity, partialTicks, poseStack, buffer);
                return;
            }

            Vec3 renderPos = entity.getRenderPosition(partialTicks);
            Vec3 pivot = entity.getRotationPivot();

            poseStack.pushPose();

            double interpX = Mth.lerp(partialTicks, entity.xOld, entity.getX());
            double interpY = Mth.lerp(partialTicks, entity.yOld, entity.getY());
            double interpZ = Mth.lerp(partialTicks, entity.zOld, entity.getZ());
            poseStack.translate(renderPos.x - interpX, renderPos.y - interpY, renderPos.z - interpZ);

            float yaw = entity.getRenderYaw(partialTicks);
            float pitch = entity.getRenderPitch(partialTicks);
            float roll = entity.getRenderRoll(partialTicks);
            poseStack.translate(pivot.x, pivot.y, pivot.z);
            poseStack.mulPose(Axis.YP.rotationDegrees(yaw));
            poseStack.mulPose(Axis.XP.rotationDegrees(pitch));
            poseStack.mulPose(Axis.ZP.rotationDegrees(roll));
            poseStack.translate(-pivot.x, -pivot.y, -pivot.z);

            BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
            ModelBlockRenderer modelRenderer = dispatcher.getModelRenderer();
            RandomSource random = RandomSource.create();

            RocketShipWorld blockGetter = entity.getShipWorld();

            int fullBright = LevelRenderer.getLightColor(entity.level(),
                    BlockPos.containing(entity.getX(), entity.getY(), entity.getZ()));

            fullBright = Math.max(fullBright, 0x00F000F0);

            boolean anyRendered = false;
            for (RenderType renderType : RenderType.chunkBufferLayers()) {
                VertexConsumer vc = buffer.getBuffer(renderType);
                for (Map.Entry<BlockPos, BlockState> entry : blocks.entrySet()) {
                    BlockPos pos = entry.getKey();
                    BlockState state = entry.getValue();
                    if (state == null || state.isAir()) continue;
                    if (state.getRenderShape() != RenderShape.MODEL) continue;

                    BakedModel model = dispatcher.getBlockModel(state);
                    long seed = state.getSeed(pos);
                    random.setSeed(seed);

                    if (!model.getRenderTypes(state, random, ModelData.EMPTY).contains(renderType)) continue;

                    poseStack.pushPose();
                    poseStack.translate(pos.getX(), pos.getY(), pos.getZ());

                    random.setSeed(seed);
                    try {

                        net.minecraft.world.level.BlockAndTintGetter levelAccessor =
                                blockGetter != null ? blockGetter : entity.level();

                        modelRenderer.tesselateBlock(
                                levelAccessor,
                                model,
                                state,
                                pos,
                                poseStack,
                                vc,
                                false,
                                random,
                                seed,
                                OverlayTexture.NO_OVERLAY,
                                ModelData.EMPTY,
                                renderType
                        );
                        anyRendered = true;
                    } catch (Throwable ignored) {

                    }
                    poseStack.popPose();
                }
            }

            if (!anyRendered) {
                VertexConsumer lineBuffer = buffer.getBuffer(RenderType.lines());
                for (Map.Entry<BlockPos, BlockState> entry : blocks.entrySet()) {
                    BlockPos pos = entry.getKey();
                    AABB box = new AABB(pos.getX(), pos.getY(), pos.getZ(),
                            pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
                    LevelRenderer.renderLineBox(poseStack, lineBuffer, box, 0.9f, 0.7f, 0.2f, 1f);
                }
            }

            poseStack.popPose();
        }

        private static void renderFallbackBox(RocketShipEntity entity, float partialTicks,
                                              PoseStack poseStack, MultiBufferSource buffer) {
            Vec3 renderPos = entity.getRenderPosition(partialTicks);
            double interpX = Mth.lerp(partialTicks, entity.xOld, entity.getX());
            double interpY = Mth.lerp(partialTicks, entity.yOld, entity.getY());
            double interpZ = Mth.lerp(partialTicks, entity.zOld, entity.getZ());
            poseStack.pushPose();
            poseStack.translate(renderPos.x - interpX, renderPos.y - interpY, renderPos.z - interpZ);
            AABB box = new AABB(-1, 0, -1, 1, 2, 1);
            LevelRenderer.renderLineBox(poseStack, buffer.getBuffer(RenderType.lines()), box, 1f, 0f, 0f, 1f);
            poseStack.popPose();
        }
    }
}
