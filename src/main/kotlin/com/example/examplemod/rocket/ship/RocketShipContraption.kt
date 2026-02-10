package com.example.examplemod.rocket.ship

import com.simibubi.create.AllContraptionTypes
import com.simibubi.create.api.contraption.ContraptionType
import com.simibubi.create.content.contraptions.AssemblyException
import com.simibubi.create.content.contraptions.TranslatingContraption
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.level.Level
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import kotlin.math.roundToInt

class RocketShipContraption : TranslatingContraption() {
    private var pivotShift: Vec3 = Vec3.ZERO

    override fun assemble(world: Level, pos: BlockPos): Boolean {
        throw AssemblyException.unmovableBlock(pos, world.getBlockState(pos))
    }

    override fun canBeStabilized(facing: Direction, localPos: BlockPos): Boolean = false

    override fun getType(): ContraptionType = AllContraptionTypes.PISTON.get()

    fun getPivotShift(): Vec3 = pivotShift

    fun rebuildFromStorage(storage: RocketShipStorage) {
        if (storage.blocks.isEmpty()) {
            blocks.clear()
            bounds = AABB(0.0, 0.0, 0.0, 1.0, 1.0, 1.0)
            pivotShift = Vec3.ZERO
            anchor = BlockPos.ZERO
            return
        }

        pivotShift = Vec3.ZERO

        blocks.clear()
        for ((pos, state) in storage.blocks) {
            blocks[pos] = StructureTemplate.StructureBlockInfo(pos, state, null)
        }

        var minX = Double.POSITIVE_INFINITY
        var minY = Double.POSITIVE_INFINITY
        var minZ = Double.POSITIVE_INFINITY
        var maxX = Double.NEGATIVE_INFINITY
        var maxY = Double.NEGATIVE_INFINITY
        var maxZ = Double.NEGATIVE_INFINITY

        for ((pos, _) in blocks) {
            minX = minX.coerceAtMost(pos.x.toDouble())
            minY = minY.coerceAtMost(pos.y.toDouble())
            minZ = minZ.coerceAtMost(pos.z.toDouble())
            maxX = maxX.coerceAtLeast(pos.x.toDouble() + 1.0)
            maxY = maxY.coerceAtLeast(pos.y.toDouble() + 1.0)
            maxZ = maxZ.coerceAtLeast(pos.z.toDouble() + 1.0)
        }

        bounds = AABB(minX, minY, minZ, maxX, maxY, maxZ)
        anchor = BlockPos.ZERO
    }
}
