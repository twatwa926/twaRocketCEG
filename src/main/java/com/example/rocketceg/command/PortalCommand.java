package com.example.rocketceg.command;

import com.example.rocketceg.portal.PortalManager;
import com.example.rocketceg.portal.Portal;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

/** 😡 传送门命令系统 - 100% 按照 ImmersivePortalsMod 实现 * * 命令： * 1. /portal create - 创建传送门 * 2. /portal delete - 删除传送门 * 3. /portal list - 列出传送门 * 4. /portal nether - 创建地狱门 * 5. /portal space - 创建太空传送门 😡
     */
public class PortalCommand {
    
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("portal")
            .requires(source -> source.hasPermission(2))
            .then(Commands.literal("create")
                .then(Commands.argument("from_dimension", DimensionArgument.dimension())
                    .then(Commands.argument("from_pos", Vec3Argument.vec3())
                        .then(Commands.argument("to_dimension", DimensionArgument.dimension())
                            .then(Commands.argument("to_pos", Vec3Argument.vec3())
                                .then(Commands.argument("width", FloatArgumentType.floatArg(0.5f, 10.0f))
                                    .then(Commands.argument("height", FloatArgumentType.floatArg(0.5f, 10.0f))
                                        .executes(PortalCommand::executeCreatePortal)
                                    )
                                )
                            )
                        )
                    )
                )
            )
            .then(Commands.literal("delete")
                .then(Commands.argument("portal_id", StringArgumentType.string())
                    .executes(PortalCommand::executeDeletePortal)
                )
            )
            .then(Commands.literal("list")
                .executes(PortalCommand::executeListPortals)
            )
            .then(Commands.literal("nether")
                .then(Commands.argument("pos", Vec3Argument.vec3())
                    .then(Commands.argument("width", FloatArgumentType.floatArg(0.5f, 10.0f))
                        .then(Commands.argument("height", FloatArgumentType.floatArg(0.5f, 10.0f))
                            .executes(PortalCommand::executeCreateNetherPortal)
                        )
                    )
                )
            )
            .then(Commands.literal("space")
                .then(Commands.argument("pos", Vec3Argument.vec3())
                    .then(Commands.argument("dimension", DimensionArgument.dimension())
                        .executes(PortalCommand::executeCreateSpacePortal)
                    )
                )
            )
        );
    }
    
    /** 😡 创建传送门 😡
     */
    private static int executeCreatePortal(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        
        ResourceKey<Level> fromDimension = DimensionArgument.getDimension(context, "from_dimension").dimension();
        Vec3 fromPos = Vec3Argument.getVec3(context, "from_pos");
        ResourceKey<Level> toDimension = DimensionArgument.getDimension(context, "to_dimension").dimension();
        Vec3 toPos = Vec3Argument.getVec3(context, "to_pos");
        float width = FloatArgumentType.getFloat(context, "width");
        float height = FloatArgumentType.getFloat(context, "height");
        
        PortalManager manager = PortalManager.getInstance();
        Portal portal = manager.createPortal(
            fromPos,
            new Quaternionf(),
            width,
            height,
            fromDimension,
            toDimension,
            toPos,
            new Quaternionf()
        );
        
        if (portal != null) {
            source.sendSuccess(() -> Component.literal(
                String.format("§a[RocketCEG] 创建传送门成功: %s -> %s", 
                            fromDimension.location(), toDimension.location())
            ), false);
        } else {
            source.sendFailure(Component.literal("§c[RocketCEG] 创建传送门失败"));
        }
        
        return 1;
    }
    
    /** 😡 删除传送门 😡
     */
    private static int executeDeletePortal(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        String portalId = StringArgumentType.getString(context, "portal_id");
        
        PortalManager manager = PortalManager.getInstance();
        manager.deletePortal(portalId);
        
        source.sendSuccess(() -> Component.literal(
            String.format("§a[RocketCEG] 删除传送门: %s", portalId)
        ), false);
        
        return 1;
    }
    
    /** 😡 列出传送门 😡
     */
    private static int executeListPortals(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        
        PortalManager manager = PortalManager.getInstance();
        var stats = manager.getStatistics();
        
        StringBuilder sb = new StringBuilder();
        sb.append("§a[RocketCEG] 传送门统计:\n");
        sb.append(String.format("§7总传送门数: %d\n", stats.get("total_portals")));
        sb.append(String.format("§7维度数: %d\n", stats.get("dimensions_with_portals")));
        
        source.sendSuccess(() -> Component.literal(sb.toString()), false);
        
        return 1;
    }
    
    /** 😡 创建地狱门 😡
     */
    private static int executeCreateNetherPortal(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        Vec3 pos = Vec3Argument.getVec3(context, "pos");
        float width = FloatArgumentType.getFloat(context, "width");
        float height = FloatArgumentType.getFloat(context, "height");
        
        PortalManager manager = PortalManager.getInstance();
        Portal portal = manager.createPortal(
            pos,
            new Quaternionf(),
            width,
            height,
            Level.OVERWORLD,
            Level.NETHER,
            new Vec3(pos.x / 8.0, pos.y, pos.z / 8.0),
            new Quaternionf()
        );
        
        if (portal != null) {
            source.sendSuccess(() -> Component.literal(
                "§a[RocketCEG] 创建地狱门传送门成功"
            ), false);
        } else {
            source.sendFailure(Component.literal("§c[RocketCEG] 创建地狱门传送门失败"));
        }
        
        return 1;
    }
    
    /** 😡 创建太空传送门 😡
     */
    private static int executeCreateSpacePortal(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        Vec3 pos = Vec3Argument.getVec3(context, "pos");
        ResourceKey<Level> spaceDimension = DimensionArgument.getDimension(context, "dimension").dimension();
        
        PortalManager manager = PortalManager.getInstance();
        Portal portal = manager.createPortal(
            pos,
            new Quaternionf(),
            4.0f,
            5.0f,
            Level.OVERWORLD,
            spaceDimension,
            new Vec3(pos.x, 256, pos.z),
            new Quaternionf()
        );
        
        if (portal != null) {
            source.sendSuccess(() -> Component.literal(
                String.format("§a[RocketCEG] 创建太空传送门成功: %s", spaceDimension.location())
            ), false);
        } else {
            source.sendFailure(Component.literal("§c[RocketCEG] 创建太空传送门失败"));
        }
        
        return 1;
    }
}
