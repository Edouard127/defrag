package com.lambda.client.module.modules.events

import net.minecraft.util.math.BlockPos
import net.minecraft.util.EnumFacing
import com.lambda.client.module.modules.EventStage
import net.minecraftforge.fml.common.eventhandler.Cancelable

@Cancelable
class BlockEvent(stage: Int, var pos: BlockPos, var facing: EnumFacing) : EventStage(stage)