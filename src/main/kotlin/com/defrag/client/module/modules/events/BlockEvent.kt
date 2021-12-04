package com.defrag.client.module.modules.events

import net.minecraft.util.math.BlockPos
import net.minecraft.util.EnumFacing
import com.defrag.client.module.modules.EventStage
import net.minecraftforge.fml.common.eventhandler.Cancelable

@Cancelable
class BlockEvent(stage: Int, var pos: BlockPos, var facing: EnumFacing) : EventStage(stage)