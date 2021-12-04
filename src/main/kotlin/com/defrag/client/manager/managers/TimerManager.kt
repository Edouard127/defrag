package com.defrag.client.manager.managers

import com.defrag.client.event.events.RunGameLoopEvent
import com.defrag.client.manager.Manager
import com.defrag.client.mixin.extension.tickLength
import com.defrag.client.mixin.extension.timer
import com.defrag.client.module.AbstractModule
import com.defrag.client.util.TickTimer
import com.defrag.client.util.TimeUnit
import com.defrag.commons.extension.synchronized
import com.defrag.event.listener.listener
import java.util.*

object TimerManager : Manager {
    private val timer = TickTimer(TimeUnit.TICKS)
    private val modifications = TreeMap<AbstractModule, Pair<Float, Long>>(compareByDescending { it.modulePriority }).synchronized() // <Module, <Tick length, Added Time>>

    private var modified = false

    var tickLength = 50.0f; private set

    init {
        listener<RunGameLoopEvent.Start> {
            if (timer.tick(5L)) {
                val removeTime = System.currentTimeMillis() - 250L
                modifications.values.removeIf { it.second < removeTime }
            }

            if (mc.player != null && modifications.isNotEmpty()) {
                modifications.firstEntry()?.let {
                    mc.timer.tickLength = it.value.first
                }
                modified = true
            } else if (modified) {
                reset()
            }

            tickLength = mc.timer.tickLength
        }
    }

    fun AbstractModule.resetTimer() {
        modifications.remove(this)
    }

    fun AbstractModule.modifyTimer(tickLength: Float) {
        if (mc.player != null) {
            modifications[this] = tickLength to System.currentTimeMillis()
        }
    }

    private fun reset() {
        mc.timer.tickLength = 50.0f
        modified = false
    }
}