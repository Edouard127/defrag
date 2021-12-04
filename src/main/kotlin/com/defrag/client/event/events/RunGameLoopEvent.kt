package com.defrag.client.event.events

import com.defrag.client.event.Event
import com.defrag.client.event.ProfilerEvent

sealed class RunGameLoopEvent(override val profilerName: String) : Event, ProfilerEvent {
    class Start : RunGameLoopEvent("start")
    class Tick : RunGameLoopEvent("tick")
    class Render : RunGameLoopEvent("render")
    class End : RunGameLoopEvent("end")
}