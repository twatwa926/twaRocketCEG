ackage com.example.rocketceg.commands;

import com.example.rocketceg.RocketCEGMod;
import com.example.rocketceg.dimension.DimensionTeleporter;
import com.example.rocketceg.dimension.seamless.SeamlessDimensionManager;
import com.example.rocketceg.rocket.config.CelestialBodyConfig;
import com.example.rocketceg.rocket.entity.RocketEntity;
import com.example.rocketceg.rocket.registry.RocketConfigRegistry;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** 😡 RocketCEG 测试命令 * - /rocketceg spawn_test - 生成测试火箭 * - /rocketceg dimension - 显示当前维度信息 * - /rocketceg teleport <dimension> - 无缝传送到指定维度 😡
     */
@Mod.EventBusSubscriber(modid = "rocketceg", bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class RocketTestCommands {

    private RocketTestCommands() {
    }

    @SubscribeEvent
    public static void onRegisterCommands(final RegisterCommandsEvent event) {
        register(event.getDispatcher());
    }

    public static void register(final CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("rocketceg")
                .then(
                    Commands.literal("teleport")
                        .requires(src -> src.hasPermission(2))
                        .then(
                            Commands.argument("dimension", StringArgumentType.string())
                                .suggests((ctx, builder) -> {
                                    // 😡 提供维度建议 😡
                                    String[] planets = {"earth", "moon", "mars", "venus", "mercury", "jupiter", "saturn", "uranus", "neptune", "pluto"};
                                    for (String planet : planets) {
                                        builder.suggest(RocketCEGMod.MOD_ID + ":" + planet + "_surface");
                                        builder.suggest(RocketCEGMod.MOD_ID + ":" + planet + "_orbit");
                                    }
                                    builder.suggest("minecraft:overworld");
                                    builder.suggest("minecraft:the_nether");
                                    builder.suggest("minecraft:the_end");
                                    return builder.buildFuture();
                                })
                                .executes(ctx -> {
                                    final ServerPlayer player = ctx.getSource().getPlayerOrException();
                                    final String dimensionId = StringArgumentType.getString(ctx, "dimension");
                                    
                                    // 😡 解析维度 ID 😡
                                    final ResourceLocation dimLoc = new ResourceLocation(dimensionId);
                                    final ResourceKey<Level> targetDim = ResourceKey.create(Registries.DIMENSION, dimLoc);
                                    
                                    // 😡 获取目标维度 😡
                                    final ServerLevel targetLevel = player.server.getLevel(targetDim);
                                    if (targetLevel == null) {
                                        ctx.getSource().sendFailure(Component.literal(
                                            "§c[RocketCEG] 维度 " + dimensionId + " 不存在！"
                                        ));
                                        return 0;
                                    }
                                    
                                    // 😡 使用新的无缝传送系统 😡
                                    final Vec3 targetPos = new Vec3(player.getX(), 100, player.getZ());
                                    
                                    // 😡 预加载目标维度 😡
                                    SeamlessDimensionManager.getInstance().preloadAdjacentDimensions(targetDim);
                                    
                                    // 😡 执行无缝传送 😡
                                    DimensionTeleporter.teleportPlayerSeamlessly(player, targetDim, targetPos);
                                    
                                    ctx.getSource().sendSuccess(() -> Component.literal(
                                        "§a[RocketCEG] 正在进行无缝传送到 " + dimensionId + "..."
                                    ), true);
                                    return 1;
                                })
                        )
                )
                .then(
                    Commands.literal("dimension")
                        .executes(ctx -> {
                            final CommandSourceStack source = ctx.getSource();
                            final ServerLevel level = source.getLevel();
                            final ResourceKey<Level> dimension = level.dimension();
                            
                            // 😡 获取维度信息 😡
                            final String dimensionId = dimension.location().toString();
                            final CelestialBodyConfig body = RocketConfigRegistry.getBodyForDimension(dimension);
                            
                            // 😡 构建消息 😡
                            final Component message;
                            if (body != null) {
                                final String bodyName = body.getId().getPath();
                                final boolean isOrbit = dimension.equals(body.getOrbitDimension());
                                final String dimensionType = isOrbit ? "轨道维度" : "地表维度";
                                
                                // 😡 如果是玩家，显示高度信息 😡
                                if (source.getEntity() instanceof ServerPlayer player) {
                                    final Vec3 pos = player.position();
                                    final double altitude = pos.y - body.getRadius();
                                    message = Component.literal(
                                        String.format(
                                            "§6[RocketCEG] 维度信息:\n" +
                                            "§7维度ID: §f%s\n" +
                                            "§7行星: §f%s\n" +
                                            "§7类型: §f%s\n" +
                                            "§7当前高度: §f%.2f 米",
                                            dimensionId, bodyName, dimensionType, altitude
                                        )
                                    );
                                } else {
                                    message = Component.literal(
                                        String.format(
                                            "§6[RocketCEG] 维度信息:\n" +
                                            "§7维度ID: §f%s\n" +
                                            "§7行星: §f%s\n" +
                                            "§7类型: §f%s",
                                            dimensionId, bodyName, dimensionType
                                        )
                                    );
                                }
                            } else {
                                message = Component.literal(
                                    String.format(
                                        "§6[RocketCEG] 维度信息:\n" +
                                        "§7维度ID: §f%s\n" +
                                        "§7行星: §c未配置（可能是主世界或其他模组的维度）",
                                        dimensionId
                                    )
                                );
                            }
                            
                            source.sendSuccess(() -> message, false);
                            return 1;
                        })
                )
                .then(
                    Commands.literal("spawn_test")
                        .requires(src -> src.hasPermission(2))
                        .executes(ctx -> {
                            final ServerPlayer player = ctx.getSource().getPlayerOrException();
                            final ServerLevel level = player.serverLevel();

                            final RocketEntity rocket = RocketEntity.createTestRocket(level);
                            rocket.moveTo(
                                player.getX(),
                                player.getY() + 2.0,
                                player.getZ(),
                                player.getYRot(),
                                player.getXRot()
                            );

                            level.addFreshEntity(rocket);
                            return 1;
                        })
                )
        );
    }
}

