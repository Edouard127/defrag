package com.defrag.client.util

import net.minecraftforge.fml.common.eventhandler.Event


class EventStage : Event {
    var stage = 0


    constructor() {}
    constructor(stage: Int) {
        this.stage = stage
    }

    @JvmName("getStage1") fun getStage(): Int {
        return stage
    }
}