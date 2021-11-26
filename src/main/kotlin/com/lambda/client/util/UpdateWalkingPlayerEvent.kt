package com.lambda.client.util

import com.lambda.client.module.modules.EventStage
import net.minecraftforge.fml.common.eventhandler.Cancelable

@Cancelable
class UpdateWalkingPlayerEvent(stage: Int) : EventStage(stage)