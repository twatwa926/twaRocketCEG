package com.example.examplemod.client;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.rocket.ship.RocketBoosterEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;

public class RocketBoosterRenderer extends EntityRenderer<RocketBoosterEntity> {

    private static final BlockState BOOSTER_BODY = Blocks.IRON_BLOCK.defaultBlockState();
    private static final BlockState BOOSTER_TOP  = Blocks.SMOOTH_STONE.defaultBlockState();
    private static final BlockState BOOSTER_NOSE = Blocks.IRON_TRAPDOOR.defaultBlockState();

    public RocketBoosterRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(RocketBoosterEntity entity) {
        return new ResourceLocation(ExampleMod.MODID, "textures/entity/rocket_ship.png");
    }

    @Override
    public void render(RocketBoosterEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        if (entity == null || !entity.isAlive()) return;

        float tumblePitch = entity.getTumblePitch(partialTick);
        float tumbleYaw   = entity.getTumbleYaw(partialTick);

        poseStack.pushPose();

        poseStack.translate(0.0, 1.5, 0.0);
        poseStack.mulPose(Axis.YP.rotationDegrees(tumbleYaw));
        poseStack.mulPose(Axis.XP.rotationDegrees(tumblePitch));
        poseStack.translate(0.0, -1.5, 0.0);

        BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
        ModelBlockRenderer modelRenderer = dispatcher.getModelRenderer();
        RandomSource random = RandomSource.create();
        int fullBright = 0x00F000F0;

        renderBlock(poseStack, buffer, dispatcher, modelRenderer, random,
                BOOSTER_BODY, 0, 0, 0, fullBright);

        renderBlock(poseStack, buffer, dispatcher, modelRenderer, random,
                BOOSTER_BODY, 0, 1, 0, fullBright);

        renderBlock(poseStack, buffer, dispatcher, modelRenderer, random,
                BOOSTER_TOP, 0, 2, 0, fullBright);

        float flameScale = entity.getFlameScale();
        if (flameScale > 0.05f) {
            renderFlameGlow(poseStack, buffer, flameScale);
        }

        poseStack.popPose();
    }

    private static void renderBlock(PoseStack poseStack, MultiBufferSource buffer,
                                    BlockRenderDispatcher dispatcher, ModelBlockRenderer modelRenderer,
                                    RandomSource random, BlockState state,
                                    int ox, int oy, int oz, int light) {
        BakedModel model = dispatcher.getBlockModel(state);
        BlockPos pos = new BlockPos(ox, oy, oz);
        long seed = state.getSeed(pos);

        for (RenderType renderType : RenderType.chunkBufferLayers()) {
            random.setSeed(seed);
            if (!model.getRenderTypes(state, random, ModelData.EMPTY).contains(renderType)) continue;

            VertexConsumer vc = buffer.getBuffer(renderType);
            poseStack.pushPose();
            poseStack.translate(ox, oy, oz);
            random.setSeed(seed);
            try {
                modelRenderer.tesselateBlock(
                        Minecraft.getInstance().level,
                        model, state, pos, poseStack, vc,
                        false, random, seed,
                        OverlayTexture.NO_OVERLAY,
                        ModelData.EMPTY, renderType
                );
            } catch (Throwable ignored) {}
            poseStack.popPose();
        }
    }

    private static void renderFlameGlow(PoseStack poseStack, MultiBufferSource buffer, float intensity) {
        VertexConsumer lineBuffer = buffer.getBuffer(RenderType.lines());

        float r = 1.0f, g = 0.6f * intensity + 0.3f, b = 0.1f * intensity;
        float halfW = 0.3f * intensity + 0.15f;
        float len = 1.2f * intensity + 0.3f;

        poseStack.pushPose();
        poseStack.translate(0.5, -len, 0.5);
        net.minecraft.world.phys.AABB flameBox = new net.minecraft.world.phys.AABB(
                -halfW, 0, -halfW, halfW, len, halfW
        );
        LevelRenderer.renderLineBox(poseStack, lineBuffer, flameBox, r, g, b, intensity * 0.8f);

        float halfW2 = halfW * 0.5f;
        float len2 = len * 0.7f;
        net.minecraft.world.phys.AABB innerFlame = new net.minecraft.world.phys.AABB(
                -halfW2, 0, -halfW2, halfW2, len2, halfW2
        );
        LevelRenderer.renderLineBox(poseStack, lineBuffer, innerFlame, 1.0f, 1.0f, 0.7f, intensity);

        poseStack.popPose();
    }
}
