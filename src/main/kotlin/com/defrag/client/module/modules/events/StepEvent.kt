package com.defrag.client.module.modules.events

import com.defrag.client.module.modules.EventStage
import net.minecraft.entity.Entity
import net.minecraftforge.fml.common.eventhandler.Cancelable

@Cancelable
class StepEvent(stage: Int, val entity: Entity) : EventStage(stage) {
    var height: Float

    init {
        height = entity.stepHeight
    }
}