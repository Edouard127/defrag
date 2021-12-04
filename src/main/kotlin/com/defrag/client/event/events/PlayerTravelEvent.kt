package com.defrag.client.event.events

import com.defrag.client.event.Cancellable
import com.defrag.client.event.Event
import com.defrag.client.event.ICancellable
import com.defrag.client.event.ProfilerEvent

class PlayerTravelEvent : Event, ICancellable by Cancellable(), ProfilerEvent {
    override val profilerName: String = "kbPlayerTravel"
}