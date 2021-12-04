package com.defrag.client.module.modules

import net.minecraft.network.Packet
import net.minecraftforge.fml.common.eventhandler.Event

open class EventStage : Event {
    open fun getStage(): Any {
        return this.stage
    }
    private val packet: Packet<*>? = null
    open var stage = 0

    constructor() {}
    constructor(stage: Int) {
        this.stage = stage
    }

    open fun EventStage() {}

    open fun EventStage(stage: Int) {
        this.stage = stage
    }

}