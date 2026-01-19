ackage com.example.rocketceg.command;

import com.example.rocketceg.RocketCEGMod;
import com.example.rocketceg.seamless.SeamlessCore;
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
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

/** 😡 高级无缝传送命令系统 - 100% 按照 ImmersivePortalsMod 实现 * * 参考 ImmersivePortalsMod 的命令设计，提供： * 1. 基础无缝传送 * 2. 空间变换传送（平移、旋转、缩放、镜像） * 3. 预设的太空-行星传送 * 4. 完全无加载屏幕的传送体验 * * 这是对原有命令系统的增强版本 😡
     */
public class AdvancedSeamlessTeleportCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        // 😡 高级空间变换传送命令 😡
        dispatcher.register(Commands.literal("rocketceg_transform")
            .requires(source -> source.hasPermission(2))
            .then(Commands.argument("dimension", DimensionArgument.dimension())
                .then(Commands.argument("pos", Vec3Argument.vec3())
                    .executes(AdvancedSeamlessTeleportCommand::executeBasicTransform)
                    .then(Commands.argument("translation", Vec3Argument.vec3())
                        .executes(AdvancedSeamlessTeleportCommand::executeTransformWithTranslation)
                        .then(Commands.argument("yaw", FloatArgumentType.floatArg())
                            .then(Commands.argument("pitch", FloatArgumentType.floatArg())
                                .executes(AdvancedSeamlessTeleportCommand::executeTransformWithRotation)
                                .then(Commands.argument("roll", FloatArgumentType.floatArg())
                                    .executes(AdvancedSeamlessTeleportCommand::executeFullTransform)
                                    .then(Commands.argument("scale", FloatArgumentType.floatArg(0.1f, 10.0f))
                                        .executes(AdvancedSeamlessTeleportCommand::executeTransformWithScale)
                                    )
                                )
                            )
                        )
                    )
                )
            )
        );

        // 😡 太空-行星传送预设 😡
        dispatcher.register(Commands.literal("rocketceg_space")
            .requires(source -> source.hasPermission(2))
            .then(Commands.literal("to_planet")
                .then(Commands.argument("planet", StringArgumentType.string())
                    .executes(AdvancedSeamlessTeleportCommand::executeToPlanet)
                )
            )
            .then(Commands.literal("to_orbit")
                .then(Commands.argument("planet", StringArgumentType.string())
                    .executes(AdvancedSeamlessTeleportCommand::executeToOrbit)
                )
            )
            .then(Commands.literal("list")
                .executes(AdvancedSeamlessTeleportCommand::executeListPlanets)
            )
        );

        // 😡 镜像传送命令 😡
        dispatcher.register(Commands.literal("rocketceg_mirror")
            .requires(source -> source.hasPermission(2))
            .then(Commands.argument("dimension", DimensionArgument.dimension())
                .then(Commands.argument("pos", Vec3Argument.vec3())
                    .then(Commands.literal("x")
                        .executes(context -> executeMirrorTeleport(context, true, false, false))
                    )
                    .then(Commands.literal("y")
                        .executes(context -> executeMirrorTeleport(context, false, true, false))
                    )
                    .then(Commands.literal("z")
                        .executes(context -> executeMirrorTeleport(context, false, false, true))
                    )
                    .then(Commands.literal("xy")
                        .executes(context -> executeMirrorTeleport(context, true, true, false))
                    )
                    .then(Commands.literal("all")
                        .executes(context -> executeMirrorTeleport(context, true, true, true))
                    )
                )
            )
        );

        // 😡 缩放传送命令 😡
        dispatcher.register(Commands.literal("rocketceg_scale")
            .requires(source -> source.hasPermission(2))
            .then(Commands.argument("dimension", DimensionArgument.dimension())
                .then(Commands.argument("pos", Vec3Argument.vec3())
                    .then(Commands.argument("scale", FloatArgumentType.floatArg(0.1f, 10.0f))
                        .executes(AdvancedSeamlessTeleportCommand::executeScaleTeleport)
                    )
                )
            )
        );

        // 😡 测试相机旋转命令 😡
        dispatcher.register(Commands.literal("rocketceg_test_rotation")
            .requires(source -> source.hasPermission(2))
            .then(Commands.argument("yaw", FloatArgumentType.floatArg())
                .then(Commands.argument("pitch", FloatArgumentType.floatArg())
                    .executes(AdvancedSeamlessTeleportCommand::executeTestRotation)
                )
            )
        );
    }

    /** 😡 执行基础空间变换传送 😡
     */
    private static int executeBasicTransform(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayerOrException();
        
        ResourceKey<Level> dimension = DimensionArgument.getDimension(context, "dimension").dimension();
        Vec3 position = Vec3Argument.getVec3(context, "pos");
        
        // 😡 执行基础传送（无变换） 😡
        SeamlessCore.getInstance().startSeamlessTeleport(player, dimension, position);
        
        source.sendSuccess(() -> Component.literal(
            String.format("§a[RocketCEG] 开始基础空间变换传送到 %s", dimension.location())
        ), false);
        
        return 1;
    }

    /** 😡 执行带平移的空间变换传送 😡
     */
    private static int executeTransformWithTranslation(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayerOrException();
        
        ResourceKey<Level> dimension = DimensionArgument.getDimension(context, "dimension").dimension();
        Vec3 position = Vec3Argument.getVec3(context, "pos");
        Vec3 translation = Vec3Argument.getVec3(context, "translation");
        
        // 😡 执行带平移的传送 😡
        SeamlessCore.getInstance().startEyeBasedSeamlessTeleport(
            player, dimension, position, translation, new Quaternionf(), 1.0f, false
        );
        
        source.sendSuccess(() -> Component.literal(
            String.format("§a[RocketCEG] 开始带平移的空间变换传送 (平移: %.1f, %.1f, %.1f)", 
                         translation.x, translation.y, translation.z)
        ), false);
        
        return 1;
    }

    /** 😡 执行带旋转的空间变换传送 😡
     */
    private static int executeTransformWithRotation(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayerOrException();
        
        ResourceKey<Level> dimension = DimensionArgument.getDimension(context, "dimension").dimension();
        Vec3 position = Vec3Argument.getVec3(context, "pos");
        Vec3 translation = Vec3Argument.getVec3(context, "translation");
        float yaw = FloatArgumentType.getFloat(context, "yaw");
        float pitch = FloatArgumentType.getFloat(context, "pitch");
        
        // 😡 创建旋转四元数 😡
        Quaternionf rotation = new Quaternionf()
            .rotateY((float)Math.toRadians(yaw))
            .rotateX((float)Math.toRadians(pitch));
        
        // 😡 执行带旋转的传送 😡
        SeamlessCore.getInstance().startEyeBasedSeamlessTeleport(
            player, dimension, position, translation, rotation, 1.0f, false
        );
        
        source.sendSuccess(() -> Component.literal(
            String.format("§a[RocketCEG] 开始带旋转的空间变换传送 (yaw: %.1f°, pitch: %.1f°)", yaw, pitch)
        ), false);
        
        return 1;
    }

    /** 😡 执行完整空间变换传送 😡
     */
    private static int executeFullTransform(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayerOrException();
        
        ResourceKey<Level> dimension = DimensionArgument.getDimension(context, "dimension").dimension();
        Vec3 position = Vec3Argument.getVec3(context, "pos");
        Vec3 translation = Vec3Argument.getVec3(context, "translation");
        float yaw = FloatArgumentType.getFloat(context, "yaw");
        float pitch = FloatArgumentType.getFloat(context, "pitch");
        float roll = FloatArgumentType.getFloat(context, "roll");
        
        // 😡 创建完整旋转四元数 😡
        Quaternionf rotation = new Quaternionf()
            .rotateY((float)Math.toRadians(yaw))
            .rotateX((float)Math.toRadians(pitch))
            .rotateZ((float)Math.toRadians(roll));
        
        // 😡 执行完整空间变换传送 😡
        SeamlessCore.getInstance().startEyeBasedSeamlessTeleport(
            player, dimension, position, translation, rotation, 1.0f, false
        );
        
        source.sendSuccess(() -> Component.literal(
            String.format("§a[RocketCEG] 开始完整空间变换传送 (yaw: %.1f°, pitch: %.1f°, roll: %.1f°)", 
                         yaw, pitch, roll)
        ), false);
        
        return 1;
    }

    /** 😡 执行带缩放的空间变换传送 😡
     */
    private static int executeTransformWithScale(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayerOrException();
        
        ResourceKey<Level> dimension = DimensionArgument.getDimension(context, "dimension").dimension();
        Vec3 position = Vec3Argument.getVec3(context, "pos");
        Vec3 translation = Vec3Argument.getVec3(context, "translation");
        float yaw = FloatArgumentType.getFloat(context, "yaw");
        float pitch = FloatArgumentType.getFloat(context, "pitch");
        float roll = FloatArgumentType.getFloat(context, "roll");
        float scale = FloatArgumentType.getFloat(context, "scale");
        
        // 😡 创建完整旋转四元数 😡
        Quaternionf rotation = new Quaternionf()
            .rotateY((float)Math.toRadians(yaw))
            .rotateX((float)Math.toRadians(pitch))
            .rotateZ((float)Math.toRadians(roll));
        
        // 😡 执行带缩放的完整空间变换传送 😡
        SeamlessCore.getInstance().startEyeBasedSeamlessTeleport(
            player, dimension, position, translation, rotation, scale, false
        );
        
        source.sendSuccess(() -> Component.literal(
            String.format("§a[RocketCEG] 开始带缩放的完整空间变换传送 (缩放: %.2fx)", scale)
        ), false);
        
        return 1;
    }

    /** 😡 执行镜像传送 😡
     */
    private static int executeMirrorTeleport(CommandContext<CommandSourceStack> context, 
                                           boolean mirrorX, boolean mirrorY, boolean mirrorZ) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayerOrException();
        
        ResourceKey<Level> dimension = DimensionArgument.getDimension(context, "dimension").dimension();
        Vec3 position = Vec3Argument.getVec3(context, "pos");
        
        // 😡 执行镜像传送 😡
        SeamlessCore.getInstance().startEyeBasedSeamlessTeleport(
            player, dimension, position, Vec3.ZERO, new Quaternionf(), 1.0f, mirrorX || mirrorY || mirrorZ
        );
        
        String mirrorAxes = (mirrorX ? "X" : "") + (mirrorY ? "Y" : "") + (mirrorZ ? "Z" : "");
        source.sendSuccess(() -> Component.literal(
            String.format("§a[RocketCEG] 开始镜像传送 (镜像轴: %s)", mirrorAxes)
        ), false);
        
        return 1;
    }

    /** 😡 执行缩放传送 😡
     */
    private static int executeScaleTeleport(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayerOrException();
        
        ResourceKey<Level> dimension = DimensionArgument.getDimension(context, "dimension").dimension();
        Vec3 position = Vec3Argument.getVec3(context, "pos");
        float scale = FloatArgumentType.getFloat(context, "scale");
        
        // 😡 执行缩放传送 😡
        SeamlessCore.getInstance().startEyeBasedSeamlessTeleport(
            player, dimension, position, Vec3.ZERO, new Quaternionf(), scale, false
        );
        
        source.sendSuccess(() -> Component.literal(
            String.format("§a[RocketCEG] 开始缩放传送 (缩放: %.2fx)", scale)
        ), false);
        
        return 1;
    }

    /** 😡 传送到行星表面 😡
     */
    private static int executeToPlanet(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayerOrException();
        
        String planetName = StringArgumentType.getString(context, "planet");
        
        // 😡 根据行星名称确定维度和位置 😡
        ResourceKey<Level> planetDimension = getPlanetDimension(planetName);
        Vec3 planetCenter = getPlanetCenter(planetName);
        
        if (planetDimension == null) {
            source.sendFailure(Component.literal("§c[RocketCEG] 未知的行星: " + planetName));
            return 0;
        }
        
        // 😡 创建太空到行星的变换 😡
        Vec3 surfacePosition = planetCenter.add(0, getPlanetRadius(planetName) + 10, 0);
        
        // 😡 执行传送 😡
        SeamlessCore.getInstance().startSeamlessTeleport(player, planetDimension, surfacePosition);
        
        source.sendSuccess(() -> Component.literal(
            String.format("§a[RocketCEG] 无缝传送到 %s 行星表面", planetName)
        ), false);
        
        return 1;
    }

    /** 😡 传送到行星轨道 😡
     */
    private static int executeToOrbit(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayerOrException();
        
        String planetName = StringArgumentType.getString(context, "planet");
        
        // 😡 根据行星名称确定轨道维度和位置 😡
        ResourceKey<Level> orbitDimension = getOrbitDimension(planetName);
        Vec3 planetCenter = getPlanetCenter(planetName);
        
        if (orbitDimension == null) {
            source.sendFailure(Component.literal("§c[RocketCEG] 未知的行星: " + planetName));
            return 0;
        }
        
        // 😡 创建行星到太空的变换 😡
        Vec3 orbitPosition = planetCenter.add(0, getOrbitRadius(planetName), 0);
        
        // 😡 执行传送 😡
        SeamlessCore.getInstance().startSeamlessTeleport(player, orbitDimension, orbitPosition);
        
        source.sendSuccess(() -> Component.literal(
            String.format("§a[RocketCEG] 无缝传送到 %s 轨道", planetName)
        ), false);
        
        return 1;
    }

    /** 😡 测试相机旋转功能 😡
     */
    private static int executeTestRotation(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayerOrException();
        
        float yaw = FloatArgumentType.getFloat(context, "yaw");
        float pitch = FloatArgumentType.getFloat(context, "pitch");
        
        // 😡 创建旋转四元数 😡
        Quaternionf rotation = new Quaternionf()
            .rotateY((float)Math.toRadians(yaw))
            .rotateX((float)Math.toRadians(pitch));
        
        // 😡 在当前位置执行带旋转的传送（测试相机旋转） 😡
        SeamlessCore.getInstance().startEyeBasedSeamlessTeleport(
            player, player.level().dimension(), player.position(), 
            Vec3.ZERO, rotation, 1.0f, false
        );
        
        source.sendSuccess(() -> Component.literal(
            String.format("§a[RocketCEG] 测试相机旋转 - Yaw: %.1f°, Pitch: %.1f°", yaw, pitch)
        ), false);
        
        return 1;
    }

    /** 😡 列出可用的行星 😡
     */
    private static int executeListPlanets(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        
        source.sendSuccess(() -> Component.literal(
            "§a[RocketCEG] 可用的行星:\n" +
            "§7- moon (月球)\n" +
            "§7- mars (火星)\n" +
            "§7- venus (金星)\n" +
            "§7- mercury (水星)\n" +
            "§7- jupiter (木星)\n" +
            "§e使用 /rocketceg_space to_planet <行星名> 传送到行星表面\n" +
            "§e使用 /rocketceg_space to_orbit <行星名> 传送到行星轨道"
        ), false);
        
        return 1;
    }

    // 😡 === 辅助方法 === 😡

    private static ResourceKey<Level> getPlanetDimension(String planetName) {
        return switch (planetName.toLowerCase()) {
            case "moon" -> ResourceKey.create(Registries.DIMENSION, new ResourceLocation("rocketceg", "moon_surface"));
            case "mars" -> ResourceKey.create(Registries.DIMENSION, new ResourceLocation("rocketceg", "mars_surface"));
            case "venus" -> ResourceKey.create(Registries.DIMENSION, new ResourceLocation("rocketceg", "venus_surface"));
            case "mercury" -> ResourceKey.create(Registries.DIMENSION, new ResourceLocation("rocketceg", "mercury_surface"));
            case "jupiter" -> ResourceKey.create(Registries.DIMENSION, new ResourceLocation("rocketceg", "jupiter_surface"));
            default -> null;
        };
    }

    private static ResourceKey<Level> getOrbitDimension(String planetName) {
        return switch (planetName.toLowerCase()) {
            case "moon" -> ResourceKey.create(Registries.DIMENSION, new ResourceLocation("rocketceg", "moon_orbit"));
            case "mars" -> ResourceKey.create(Registries.DIMENSION, new ResourceLocation("rocketceg", "mars_orbit"));
            case "venus" -> ResourceKey.create(Registries.DIMENSION, new ResourceLocation("rocketceg", "venus_orbit"));
            case "mercury" -> ResourceKey.create(Registries.DIMENSION, new ResourceLocation("rocketceg", "mercury_orbit"));
            case "jupiter" -> ResourceKey.create(Registries.DIMENSION, new ResourceLocation("rocketceg", "jupiter_orbit"));
            default -> null;
        };
    }

    private static Vec3 getPlanetCenter(String planetName) {
        // 😡 所有行星的中心都在 (0, 0, 0) 😡
        return Vec3.ZERO;
    }

    private static float getPlanetRadius(String planetName) {
        return switch (planetName.toLowerCase()) {
            case "moon" -> 50.0f;
            case "mars" -> 80.0f;
            case "venus" -> 75.0f;
            case "mercury" -> 40.0f;
            case "jupiter" -> 200.0f;
            default -> 64.0f;
        };
    }

    private static float getOrbitRadius(String planetName) {
        return switch (planetName.toLowerCase()) {
            case "moon" -> 150.0f;
            case "mars" -> 250.0f;
            case "venus" -> 200.0f;
            case "mercury" -> 120.0f;
            case "jupiter" -> 500.0f;
            default -> 200.0f;
        };
    }
}