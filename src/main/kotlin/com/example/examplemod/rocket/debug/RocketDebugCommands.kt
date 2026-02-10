package com.example.examplemod.rocket.debug

import com.example.examplemod.rocket.RocketDimensionTransition
import com.example.examplemod.rocket.ship.RocketShipEntity
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.DoubleArgumentType
import com.mojang.brigadier.arguments.FloatArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.SharedSuggestionProvider
import net.minecraft.network.chat.Component
import net.minecraft.world.phys.Vec3

object RocketDebugCommands {

    fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
        dispatcher.register(
            Commands.literal("rocketwa")
                .requires { source -> source.hasPermission(2) }
                .then(
                    Commands.literal("debug")
                        .then(Commands.literal("collision").executes { toggleCollisionDebug(it) })
                        .then(Commands.literal("render").executes { toggleRenderDebug(it) })
                        .then(Commands.literal("info").executes { showDebugInfo(it) })
                )

                .then(
                    Commands.literal("launch")
                        .executes { launchRocket(it, 1.0, 100.0) }
                        .then(
                            Commands.argument("power", DoubleArgumentType.doubleArg(0.0, 1.0))
                                .executes { launchRocket(it, DoubleArgumentType.getDouble(it, "power"), 100.0) }
                                .then(
                                    Commands.argument("fuel", DoubleArgumentType.doubleArg(0.0, 10000.0))
                                        .executes { launchRocket(it, DoubleArgumentType.getDouble(it, "power"), DoubleArgumentType.getDouble(it, "fuel")) }
                                )
                        )
                )

                .then(
                    Commands.literal("stop")
                        .executes { stopRocket(it) }
                )

                .then(
                    Commands.literal("fuel")
                        .then(
                            Commands.argument("amount", DoubleArgumentType.doubleArg(0.0, 10000.0))
                                .executes { refuelRocket(it, DoubleArgumentType.getDouble(it, "amount")) }
                        )
                )

                .then(
                    Commands.literal("goto")
                        .then(
                            Commands.argument("planet", StringArgumentType.word())
                                .suggests { context, builder ->
                                    SharedSuggestionProvider.suggest(
                                        RocketDimensionTransition.getAvailablePlanets(),
                                        builder
                                    )
                                }
                                .executes { setDestination(it, StringArgumentType.getString(it, "planet")) }
                        )
                )

                .then(
                    Commands.literal("planets")
                        .executes { listPlanets(it) }
                )

                .then(
                    Commands.literal("threshold")

                        .executes { showThresholds(it) }

                        .then(
                            Commands.literal("orbit")
                                .then(Commands.argument("value", DoubleArgumentType.doubleArg(10.0, 10000.0))
                                    .executes { setThreshold(it, "orbit", DoubleArgumentType.getDouble(it, "value")) })
                        )

                        .then(
                            Commands.literal("space")
                                .then(Commands.argument("value", DoubleArgumentType.doubleArg(10.0, 10000.0))
                                    .executes { setThreshold(it, "space", DoubleArgumentType.getDouble(it, "value")) })
                        )

                        .then(
                            Commands.literal("land")
                                .then(Commands.argument("value", DoubleArgumentType.doubleArg(-100.0, 10000.0))
                                    .executes { setThreshold(it, "land", DoubleArgumentType.getDouble(it, "value")) })
                        )

                        .then(
                            Commands.literal("entry")
                                .then(Commands.argument("value", DoubleArgumentType.doubleArg(10.0, 10000.0))
                                    .executes { setThreshold(it, "entry", DoubleArgumentType.getDouble(it, "value")) })
                        )
                )

                .then(
                    Commands.literal("tilt")
                        .executes { showTilt(it) }
                        .then(
                            Commands.argument("pitch", FloatArgumentType.floatArg(-180f, 180f))
                                .then(
                                    Commands.argument("yaw", FloatArgumentType.floatArg(-180f, 180f))
                                        .then(
                                            Commands.argument("roll", FloatArgumentType.floatArg(-180f, 180f))
                                                .executes { setTilt(it,
                                                    FloatArgumentType.getFloat(it, "pitch"),
                                                    FloatArgumentType.getFloat(it, "yaw"),
                                                    FloatArgumentType.getFloat(it, "roll")) }
                                        )
                                )
                        )
                )

                .then(
                    Commands.literal("spin")
                        .then(
                            Commands.argument("pitchVel", FloatArgumentType.floatArg(-360f, 360f))
                                .then(
                                    Commands.argument("yawVel", FloatArgumentType.floatArg(-360f, 360f))
                                        .then(
                                            Commands.argument("rollVel", FloatArgumentType.floatArg(-360f, 360f))
                                                .executes { setSpin(it,
                                                    FloatArgumentType.getFloat(it, "pitchVel"),
                                                    FloatArgumentType.getFloat(it, "yawVel"),
                                                    FloatArgumentType.getFloat(it, "rollVel")) }
                                        )
                                )
                        )
                )
        )
    }

    private fun toggleCollisionDebug(context: CommandContext<CommandSourceStack>): Int {
        val newState = !RocketDebugRenderer.isCollisionDebugEnabled()
        RocketDebugRenderer.setCollisionDebugEnabled(newState)

        val message = if (newState) {
            "§a碰撞箱调试可视化已启用"
        } else {
            "§c碰撞箱调试可视化已禁用"
        }

        context.source.sendSuccess(
            { Component.literal(message) },
            false
        )

        println("[RocketDebug] 碰撞箱调试可视化已${if (newState) "启用" else "禁用"}")

        return 1
    }

    private fun toggleRenderDebug(context: CommandContext<CommandSourceStack>): Int {
        val currentValue = System.getProperty("rocket.debug.render")
        val newState = currentValue == null

        if (newState) {
            System.setProperty("rocket.debug.render", "true")
        } else {
            System.clearProperty("rocket.debug.render")
        }

        val message = if (newState) {
            "§a渲染调试模式已启用"
        } else {
            "§c渲染调试模式已禁用"
        }

        context.source.sendSuccess(
            { Component.literal(message) },
            false
        )

        println("[RocketDebug] 渲染调试模式已${if (newState) "启用" else "禁用"}")

        return 1
    }

    private fun showDebugInfo(context: CommandContext<CommandSourceStack>): Int {
        val enabled = RocketDebugRenderer.isCollisionDebugEnabled()
        val stats = RocketDebugRenderer.getDebugStats()

        context.source.sendSuccess(
            { Component.literal("§6=== 火箭调试信息 ===") },
            false
        )
        context.source.sendSuccess(
            { Component.literal("§7碰撞箱可视化: ${if (enabled) "§a启用" else "§c禁用"}") },
            false
        )
        context.source.sendSuccess(
            { Component.literal("§7渲染的火箭数量: §f${stats.rocketCount}") },
            false
        )
        context.source.sendSuccess(
            { Component.literal("§7总碰撞箱数量: §f${stats.totalCollisionBoxes}") },
            false
        )
        context.source.sendSuccess(
            { Component.literal("§7总边界箱数量: §f${stats.totalBoundingBoxes}") },
            false
        )

        return 1
    }

    private fun findNearestRocket(source: CommandSourceStack): RocketShipEntity? {
        val pos = source.position
        var nearest: RocketShipEntity? = null
        var minDistSq = Double.MAX_VALUE

        val level = source.level
        for (entity in level.allEntities) {
            if (entity is RocketShipEntity && !entity.isRemoved) {
                val dSq = entity.position().distanceToSqr(pos)
                if (dSq < minDistSq) {
                    minDistSq = dSq
                    nearest = entity
                }
            }
        }
        return nearest
    }

    private fun launchRocket(context: CommandContext<CommandSourceStack>, power: Double, fuel: Double): Int {
        val rocket = findNearestRocket(context.source)
        if (rocket == null) {
            context.source.sendFailure(Component.literal("§c未找到附近的火箭！请先组装一艘火箭。"))
            return 0
        }
        rocket.launch(power, fuel)
        val pct = (power * 100).toInt()
        context.source.sendSuccess(
            { Component.literal("§a§l发射！§r §7油门: §f${pct}% §7燃料: §f${fuel}") },
            true
        )
        println("[RocketLaunch] 火箭 ${rocket.shipId} 发射! 油门=$pct%, 燃料=$fuel")
        return 1
    }

    private fun stopRocket(context: CommandContext<CommandSourceStack>): Int {
        val rocket = findNearestRocket(context.source)
        if (rocket == null) {
            context.source.sendFailure(Component.literal("§c未找到附近的火箭！"))
            return 0
        }
        rocket.stopThrust()
        context.source.sendSuccess(
            { Component.literal("§e推力已关闭") },
            true
        )
        return 1
    }

    private fun refuelRocket(context: CommandContext<CommandSourceStack>, amount: Double): Int {
        val rocket = findNearestRocket(context.source)
        if (rocket == null) {
            context.source.sendFailure(Component.literal("§c未找到附近的火箭！"))
            return 0
        }
        rocket.launch(rocket.getThrottle(), rocket.getFuelMass() + amount)
        context.source.sendSuccess(
            { Component.literal("§a已补充 §f${amount} §a单位燃料，当前燃料: §f${rocket.getFuelMass()}") },
            true
        )
        return 1
    }

    private fun setDestination(context: CommandContext<CommandSourceStack>, planet: String): Int {
        val rocket = findNearestRocket(context.source)
        if (rocket == null) {
            context.source.sendFailure(Component.literal("§c未找到附近的火箭！"))
            return 0
        }
        val dim = RocketDimensionTransition.getPlanetDimension(planet)
        if (dim == null) {
            context.source.sendFailure(Component.literal("§c未知星球: §f$planet§c！使用 §f/rocket planets §c查看可用星球。"))
            return 0
        }
        rocket.destinationPlanet = planet.lowercase()
        context.source.sendSuccess(
            { Component.literal("§b§l[导航] §r§a目标已设定: §f§l${planet.uppercase()} §r§7— 发射后到达轨道高度将自动跳转") },
            true
        )
        println("[RocketNav] 火箭 ${rocket.shipId} 目标星球设为: $planet")
        return 1
    }

    private fun listPlanets(context: CommandContext<CommandSourceStack>): Int {
        val planets = RocketDimensionTransition.getAvailablePlanets().sorted()
        context.source.sendSuccess(
            { Component.literal("§6=== 可用星球 ===") },
            false
        )
        for (planet in planets) {
            context.source.sendSuccess(
                { Component.literal("  §7• §f$planet") },
                false
            )
        }
        context.source.sendSuccess(
            { Component.literal("§7使用 §f/rocket goto <星球名> §7设定目标") },
            false
        )
        return 1
    }

    private fun showThresholds(context: CommandContext<CommandSourceStack>): Int {
        context.source.sendSuccess(
            { Component.literal("§6=== 跨维度高度阈值 ===") }, false)
        context.source.sendSuccess(
            { Component.literal("§7  orbit  §f= §e${RocketDimensionTransition.getOrbitThreshold()} §7(地表→轨道)") }, false)
        context.source.sendSuccess(
            { Component.literal("§7  space  §f= §e${RocketDimensionTransition.getSpaceThreshold()} §7(轨道→目标星球)") }, false)
        context.source.sendSuccess(
            { Component.literal("§7  land   §f= §e${RocketDimensionTransition.getLandThreshold()} §7(轨道→落回地表)") }, false)
        context.source.sendSuccess(
            { Component.literal("§7  entry  §f= §e${RocketDimensionTransition.getPlanetEntryAltitude()} §7(到达星球的初始高度)") }, false)
        context.source.sendSuccess(
            { Component.literal("§7使用 §f/rocketwa threshold <名称> <值> §7修改") }, false)
        return 1
    }

    private fun setThreshold(context: CommandContext<CommandSourceStack>, name: String, value: Double): Int {
        val displayName: String
        when (name) {
            "orbit" -> {
                RocketDimensionTransition.setOrbitThreshold(value)
                displayName = "地表→轨道"
            }
            "space" -> {
                RocketDimensionTransition.setSpaceThreshold(value)
                displayName = "轨道→目标星球"
            }
            "land" -> {
                RocketDimensionTransition.setLandThreshold(value)
                displayName = "轨道→落回地表"
            }
            "entry" -> {
                RocketDimensionTransition.setPlanetEntryAltitude(value)
                displayName = "到达星球初始高度"
            }
            else -> {
                context.source.sendFailure(Component.literal("§c未知阈值名称: $name"))
                return 0
            }
        }
        context.source.sendSuccess(
            { Component.literal("§a✔ §f$name §7($displayName) §a已设为 §e$value") },
            true
        )
        return 1
    }

    private fun showTilt(context: CommandContext<CommandSourceStack>): Int {
        val rocket = findNearestRocket(context.source) ?: run {
            context.source.sendFailure(Component.literal("§c附近没有找到火箭"))
            return 0
        }
        context.source.sendSuccess(
            { Component.literal("§6=== 火箭旋转角度 ===") }, false)
        context.source.sendSuccess(
            { Component.literal("§7  Pitch (俯仰) §f= §e${String.format("%.1f", rocket.xRot)}°") }, false)
        context.source.sendSuccess(
            { Component.literal("§7  Yaw   (偏航) §f= §e${String.format("%.1f", rocket.yRot)}°") }, false)
        context.source.sendSuccess(
            { Component.literal("§7  Roll  (滚转) §f= §e${String.format("%.1f", rocket.getRoll())}°") }, false)
        return 1
    }

    private fun setTilt(context: CommandContext<CommandSourceStack>,
                        pitch: Float, yaw: Float, roll: Float): Int {
        val rocket = findNearestRocket(context.source) ?: run {
            context.source.sendFailure(Component.literal("§c附近没有找到火箭"))
            return 0
        }
        rocket.xRot = pitch
        rocket.yRot = yaw
        rocket.setRollDirect(roll)
        context.source.sendSuccess(
            { Component.literal("§a§l[旋转] §r§f已设置: pitch=${String.format("%.1f", pitch)}° yaw=${String.format("%.1f", yaw)}° roll=${String.format("%.1f", roll)}°") },
            true
        )
        return 1
    }

    private fun setSpin(context: CommandContext<CommandSourceStack>,
                        pitchVel: Float, yawVel: Float, rollVel: Float): Int {
        val rocket = findNearestRocket(context.source) ?: run {
            context.source.sendFailure(Component.literal("§c附近没有找到火箭"))
            return 0
        }
        rocket.setAngularVelocity(pitchVel, yawVel, rollVel)
        context.source.sendSuccess(
            { Component.literal("§a§l[角速度] §r§f已设置: pitch=${String.format("%.1f", pitchVel)}°/s yaw=${String.format("%.1f", yawVel)}°/s roll=${String.format("%.1f", rollVel)}°/s") },
            true
        )
        return 1
    }
}
