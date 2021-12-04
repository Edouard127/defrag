package com.defrag.client.util

import com.defrag.client.module.modules.EventStage
import net.minecraftforge.fml.common.eventhandler.Cancelable

@Cancelable
class UpdateWalkingPlayerEvent(stage: Int) : EventStage(stage)