package com.lambda.client.module.modules

import net.minecraftforge.fml.common.eventhandler.Event

open class EventStage : Event {
    fun getStage(): Any {
        return this.stage
    }

    open var stage = 0

    constructor() {}
    constructor(stage: Int) {
        this.stage = stage
    }

}