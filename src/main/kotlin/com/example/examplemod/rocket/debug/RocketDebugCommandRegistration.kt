package com.example.examplemod.rocket.debug

import net.minecraftforge.event.RegisterCommandsEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import com.example.examplemod.ExampleMod

@Mod.EventBusSubscriber(modid = ExampleMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
object RocketDebugCommandRegistration {

    @JvmStatic
    @SubscribeEvent
    fun onRegisterCommands(event: RegisterCommandsEvent) {
        RocketDebugCommands.register(event.dispatcher)
        println("[RocketWA] 调试命令已注册")
    }
}
