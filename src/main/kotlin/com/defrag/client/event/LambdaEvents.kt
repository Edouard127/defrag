package com.defrag.client.event

import com.defrag.commons.interfaces.DisplayEnum
import com.defrag.event.eventbus.IEventBus

interface Event

interface ProfilerEvent {
    val profilerName: String
}

open class SingletonEvent(val eventBus: IEventBus) {
    fun post() {
        eventBus.post(this)
    }
}

interface IMultiPhase<T : Event> {
    val phase: Phase

    fun nextPhase(): T
}

interface ICancellable {
    var cancelled: Boolean

    fun cancel() {
        cancelled = true
    }
}

open class Cancellable : ICancellable {
    override var cancelled = false
}

enum class Phase(override val displayName: String) : DisplayEnum {
    PRE("Pre"),
    PERI("Peri"),
    POST("Post")
}