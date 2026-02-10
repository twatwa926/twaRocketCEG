package com.example.examplemod.rocket.debug

import com.example.examplemod.rocket.ship.RocketShipEntity
import com.example.examplemod.rocket.ship.RocketShipStorage
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.phys.AABB

class RocketDebugCommandsTest : FunSpec({

    test("调试渲染器 - 应该能够启用碰撞调试") {

        RocketDebugRenderer.setCollisionDebugEnabled(false)
        RocketDebugRenderer.isCollisionDebugEnabled() shouldBe false

        RocketDebugRenderer.setCollisionDebugEnabled(true)
        RocketDebugRenderer.isCollisionDebugEnabled() shouldBe true
    }

    test("调试渲染器 - 应该能够禁用碰撞调试") {

        RocketDebugRenderer.setCollisionDebugEnabled(true)
        RocketDebugRenderer.isCollisionDebugEnabled() shouldBe true

        RocketDebugRenderer.setCollisionDebugEnabled(false)
        RocketDebugRenderer.isCollisionDebugEnabled() shouldBe false
    }

    test("调试渲染器 - 应该能够切换碰撞调试状态") {

        RocketDebugRenderer.setCollisionDebugEnabled(false)
        val initialState = RocketDebugRenderer.isCollisionDebugEnabled()

        RocketDebugRenderer.setCollisionDebugEnabled(!initialState)
        val newState = RocketDebugRenderer.isCollisionDebugEnabled()

        newState shouldBe !initialState
    }

    test("调试统计 - 应该正确初始化") {

        RocketDebugRenderer.resetStats()

        val stats = RocketDebugRenderer.getDebugStats()

        stats.rocketCount shouldBe 0
        stats.totalCollisionBoxes shouldBe 0
        stats.totalBoundingBoxes shouldBe 0
    }

    test("调试统计 - 应该能够重置") {

        RocketDebugRenderer.resetStats()

        val stats1 = RocketDebugRenderer.getDebugStats()
        stats1.rocketCount shouldBe 0

        RocketDebugRenderer.resetStats()
        val stats2 = RocketDebugRenderer.getDebugStats()

        stats2.rocketCount shouldBe 0
        stats2.totalCollisionBoxes shouldBe 0
        stats2.totalBoundingBoxes shouldBe 0
    }

    test("调试统计 - 应该返回独立的副本") {

        RocketDebugRenderer.resetStats()

        val stats1 = RocketDebugRenderer.getDebugStats()
        val stats2 = RocketDebugRenderer.getDebugStats()

        (stats1 === stats2) shouldBe false

        stats1.rocketCount shouldBe stats2.rocketCount
        stats1.totalCollisionBoxes shouldBe stats2.totalCollisionBoxes
        stats1.totalBoundingBoxes shouldBe stats2.totalBoundingBoxes
    }

    test("命令功能 - 启用和禁用应该正确工作") {

        RocketDebugRenderer.setCollisionDebugEnabled(false)
        RocketDebugRenderer.isCollisionDebugEnabled() shouldBe false

        RocketDebugRenderer.setCollisionDebugEnabled(true)
        RocketDebugRenderer.isCollisionDebugEnabled() shouldBe true

        RocketDebugRenderer.setCollisionDebugEnabled(false)
        RocketDebugRenderer.isCollisionDebugEnabled() shouldBe false
    }

    test("命令功能 - 多次切换应该正确工作") {

        RocketDebugRenderer.setCollisionDebugEnabled(false)

        for (i in 1..10) {
            val currentState = RocketDebugRenderer.isCollisionDebugEnabled()
            RocketDebugRenderer.setCollisionDebugEnabled(!currentState)
            val newState = RocketDebugRenderer.isCollisionDebugEnabled()

            newState shouldBe !currentState
        }
    }

    test("调试渲染器 - 状态应该持久化") {

        RocketDebugRenderer.setCollisionDebugEnabled(true)

        for (i in 1..5) {
            RocketDebugRenderer.isCollisionDebugEnabled() shouldBe true
        }

        RocketDebugRenderer.setCollisionDebugEnabled(false)

        for (i in 1..5) {
            RocketDebugRenderer.isCollisionDebugEnabled() shouldBe false
        }
    }

    test("调试统计 - 应该能够独立访问") {

        RocketDebugRenderer.resetStats()

        val statsList = mutableListOf<RocketDebugRenderer.DebugStats>()
        for (i in 1..5) {
            statsList.add(RocketDebugRenderer.getDebugStats())
        }

        for (i in 0 until statsList.size - 1) {
            (statsList[i] === statsList[i + 1]) shouldBe false
        }
    }

    test("命令注册 - RocketDebugCommands 对象应该存在") {

        RocketDebugCommands shouldNotBe null
    }

    test("命令注册 - 应该能够调用 register 方法") {

        val registerMethod = RocketDebugCommands::class.java.methods.find {
            it.name == "register"
        }

        registerMethod shouldNotBe null
    }

    test("调试功能 - 应该能够在禁用时正常工作") {

        RocketDebugRenderer.setCollisionDebugEnabled(false)

        RocketDebugRenderer.resetStats()

        val stats = RocketDebugRenderer.getDebugStats()
        stats shouldNotBe null
        stats.rocketCount shouldBe 0
    }

    test("调试功能 - 应该能够在启用时正常工作") {

        RocketDebugRenderer.setCollisionDebugEnabled(true)

        RocketDebugRenderer.resetStats()

        val stats = RocketDebugRenderer.getDebugStats()
        stats shouldNotBe null
        stats.rocketCount shouldBe 0
    }

    test("调试渲染器 - 应该能够处理快速切换") {

        for (i in 1..100) {
            RocketDebugRenderer.setCollisionDebugEnabled(i % 2 == 0)
        }

        RocketDebugRenderer.isCollisionDebugEnabled() shouldBe true
    }

    test("调试统计 - 应该能够处理多次重置") {

        for (i in 1..10) {
            RocketDebugRenderer.resetStats()
            val stats = RocketDebugRenderer.getDebugStats()

            stats.rocketCount shouldBe 0
            stats.totalCollisionBoxes shouldBe 0
            stats.totalBoundingBoxes shouldBe 0
        }
    }
})
