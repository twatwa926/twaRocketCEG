package com.example.examplemod.rocket.debug

import com.example.examplemod.ExampleMod
import com.example.examplemod.client.RocketImmediateRenderer
import com.example.examplemod.rocket.ship.RocketShipEntity
import net.minecraft.client.Minecraft
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.client.event.RenderLevelStageEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod

@Mod.EventBusSubscriber(modid = ExampleMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = [Dist.CLIENT])
object RocketDebugClientEvents {

    @SubscribeEvent
    fun onRenderLevelStage(event: RenderLevelStageEvent) {
        when (event.stage) {
            RenderLevelStageEvent.Stage.AFTER_LEVEL ->
                RocketDebugRenderer.resetStats()
            RenderLevelStageEvent.Stage.AFTER_ENTITIES ->
                drawRocketsImmediate(event)
            else -> { }
        }
    }

    private fun drawRocketsImmediate(event: RenderLevelStageEvent) {
        val mc = Minecraft.getInstance()
        val camera = mc.gameRenderer.mainCamera
        RocketImmediateRenderer.drawRocketsImmediate(
            event.poseStack,
            event.partialTick,
            camera.position
        )
    }
}
