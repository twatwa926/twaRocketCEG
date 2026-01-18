package com.example.rocketceg.command;

import com.example.rocketceg.RocketCEGMod;
import com.example.rocketceg.seamless.SeamlessCore;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** 😡 真正无缝的传送命令 * * 使用全新的 SeamlessCore 系统，实现： * - 零加载屏幕 * - 零特效 * - 就像现实中走路一样的维度切换 😡
     */
public class SeamlessTeleportCommand {
    
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("rocketceg_seamless")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("dimension", StringArgumentType.string())
                    .then(Commands.argument("pos", Vec3Argument.vec3())
                        .executes(SeamlessTeleportCommand::executeSeamlessTeleport)
                    )
                )
        );
        
        // 😡 简化命令 😡
        dispatcher.register(
            Commands.literal("rocketceg_tp")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("dimension", StringArgumentType.string())
                    .executes(SeamlessTeleportCommand::executeSimpleSeamless)
                )
        );
        
        // 😡 兼容旧命令 😡
        dispatcher.register(
            Commands.literal("rocketceg_teleport")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("dimension", StringArgumentType.string())
                    .then(Commands.argument("pos", Vec3Argument.vec3())
                        .executes(SeamlessTeleportCommand::executeSeamlessTeleport)
                    )
                )
        );
    }
    
    private static int executeSeamlessTeleport(CommandContext<CommandSourceStack> context) {
        try {
            CommandSourceStack source = context.getSource();
            
            if (!(source.getEntity() instanceof ServerPlayer player)) {
                source.sendFailure(net.minecraft.network.chat.Component.literal("只有玩家可以使用此命令"));
                return 0;
            }
            
            String dimensionName = StringArgumentType.getString(context, "dimension");
            Vec3 targetPos = Vec3Argument.getVec3(context, "pos");
            
            return performSeamlessTeleport(source, player, dimensionName, targetPos);
            
        } catch (Exception e) {
            RocketCEGMod.LOGGER.error("[SeamlessCommand] 无缝传送命令失败", e);
            context.getSource().sendFailure(net.minecraft.network.chat.Component.literal("传送失败: " + e.getMessage()));
            return 0;
        }
    }
    
    private static int executeSimpleSeamless(CommandContext<CommandSourceStack> context) {
        try {
            CommandSourceStack source = context.getSource();
            
            if (!(source.getEntity() instanceof ServerPlayer player)) {
                source.sendFailure(net.minecraft.network.chat.Component.literal("只有玩家可以使用此命令"));
                return 0;
            }
            
            String dimensionName = StringArgumentType.getString(context, "dimension");
            Vec3 targetPos = new Vec3(0, 100, 0); // 😡 默认位置 😡
            
            return performSeamlessTeleport(source, player, dimensionName, targetPos);
            
        } catch (Exception e) {
            RocketCEGMod.LOGGER.error("[SeamlessCommand] 简化无缝传送命令失败", e);
            context.getSource().sendFailure(net.minecraft.network.chat.Component.literal("传送失败: " + e.getMessage()));
            return 0;
        }
    }
    
    private static int performSeamlessTeleport(CommandSourceStack source, ServerPlayer player, 
                                             String dimensionName, Vec3 targetPos) {
        // 😡 解析维度 😡
        ResourceLocation dimensionLocation = ResourceLocation.tryParse(dimensionName);
        if (dimensionLocation == null) {
            source.sendFailure(net.minecraft.network.chat.Component.literal("无效的维度名称: " + dimensionName));
            return 0;
        }
        
        ResourceKey<Level> dimensionKey = ResourceKey.create(Registries.DIMENSION, dimensionLocation);
        ServerLevel targetLevel = source.getServer().getLevel(dimensionKey);
        
        if (targetLevel == null) {
            source.sendFailure(net.minecraft.network.chat.Component.literal("维度不存在: " + dimensionName));
            return 0;
        }
        
        // 😡 使用 SeamlessCore 进行真正的无缝传送 😡
        RocketCEGMod.LOGGER.info("[SeamlessCommand] 开始真正无缝传送: {} -> {} ({})", 
                player.getName().getString(), dimensionName, targetPos);
        
        try {
            // 😡 调用无缝传送核心系统 😡
            SeamlessCore.getInstance().startSeamlessTeleport(player, dimensionKey, targetPos);
            
            source.sendSuccess(() -> net.minecraft.network.chat.Component.literal(
                "§a无缝传送完成！§r 传送到 " + dimensionName + 
                " §7(" + String.format("%.1f, %.1f, %.1f", targetPos.x, targetPos.y, targetPos.z) + ")§r"
            ), true);
            
            return 1;
            
        } catch (Exception e) {
            RocketCEGMod.LOGGER.error("[SeamlessCommand] 无缝传送执行失败", e);
            source.sendFailure(net.minecraft.network.chat.Component.literal("无缝传送失败: " + e.getMessage()));
            return 0;
        }
    }
}