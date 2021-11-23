package com.lambda.client.module.modules.events

import net.minecraft.util.math.BlockPos
import com.lambda.client.module.modules.EventStage
import net.minecraft.util.math.AxisAlignedBB
import net.minecraftforge.fml.common.eventhandler.Cancelable

@Cancelable
class JesusEvent(stage: Int, var pos: BlockPos) : EventStage(stage) {
    var boundingBox: AxisAlignedBB? = null

}