package com.defrag.client.event.events

import com.defrag.client.event.Event
import com.defrag.client.event.ProfilerEvent

class RenderOverlayEvent : Event, ProfilerEvent {
    override val profilerName: String = "kbRender2D"
}