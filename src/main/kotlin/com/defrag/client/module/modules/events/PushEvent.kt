package com.defrag.client.module.modules.events

import com.defrag.client.module.modules.EventStage
import net.minecraft.entity.Entity
import net.minecraftforge.fml.common.eventhandler.Cancelable

@Cancelable
class PushEvent : EventStage {
    var entity: Entity? = null
    var x = 0.0
    var y = 0.0
    var z = 0.0
    var airbone = false

    constructor(entity: Entity?, x: Double, y: Double, z: Double, airbone: Boolean) : super(0) {
        this.entity = entity
        this.x = x
        this.y = y
        this.z = z
        this.airbone = airbone
    }

    constructor(stage: Int) : super(stage) {}
    constructor(stage: Int, entity: Entity?) : super(stage) {
        this.entity = entity
    }
}