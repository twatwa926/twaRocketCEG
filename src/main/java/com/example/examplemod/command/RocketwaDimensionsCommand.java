package com.example.examplemod.command;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.rocket.RocketDimensions;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = ExampleMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class RocketwaDimensionsCommand {

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        var root = Commands.literal("rocketwa");
        root.then(Commands.literal("dimensions")
                .requires(s -> s.hasPermission(2))
                .executes(RocketwaDimensionsCommand::run));
        root.then(Commands.literal("dimension")
                .executes(RocketwaDimensionsCommand::runCurrentDimension));
        event.getDispatcher().register(root);
    }

    private static int runCurrentDimension(CommandContext<CommandSourceStack> ctx) {
        var source = ctx.getSource();
        var entity = source.getEntity();
        if (entity != null) {
            ResourceKey<Level> dim = entity.level().dimension();
            source.sendSuccess(() -> Component.literal("当前维度: §a" + dim.location()), false);
            return 1;
        }
        source.sendSuccess(() -> Component.literal("当前维度: §7" + source.getLevel().dimension().location()), false);
        return 1;
    }

    private static int run(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        var server = source.getServer();

        List<Component> lines = new ArrayList<>();
        lines.add(Component.literal("§6=== Rocketwa 维度注册检查 ==="));

        var registry = server.registryAccess().registryOrThrow(Registries.DIMENSION);
        var keys = registry.keySet().stream()
                .filter(k -> k.getNamespace().equals(ExampleMod.MODID))
                .toList();

        lines.add(Component.literal("§e维度注册表中 rocketwa 命名空间: §f" + keys.size() + " 个"));
        for (ResourceLocation key : keys) {
            lines.add(Component.literal("  - §a" + key));
        }

        lines.add(Component.literal("§eServerLevel 加载状态:"));
        checkDim(source, lines, "earth", RocketDimensions.EARTH);
        checkDim(source, lines, "earth_orbit", RocketDimensions.EARTH_ORBIT);
        checkDim(source, lines, "rocket_build", RocketDimensions.ROCKET_BUILD_LEVEL);
        checkDim(source, lines, "moon", RocketDimensions.MOON);
        checkDim(source, lines, "sun", RocketDimensions.SUN);

        lines.add(Component.literal("§6--- 说明 ---"));
        lines.add(Component.literal("§7若注册表为空或 getLevel 返回 null，说明世界在创建时未包含这些维度。"));
        lines.add(Component.literal("§7解决: 用已安装模组新建世界，或完全退出后重新进入世界（/reload 无效）。"));

        for (Component line : lines) {
            source.sendSuccess(() -> line, false);
        }
        return keys.size();
    }

    private static void checkDim(CommandSourceStack source, List<Component> lines, String name, ResourceKey<Level> key) {
        var server = source.getServer();
        ServerLevel level = server.getLevel(key);
        String status = level != null ? "§a已加载" : "§c未加载(null)";
        lines.add(Component.literal("  " + name + ": " + status + " §7(" + key.location() + ")"));
    }
}
