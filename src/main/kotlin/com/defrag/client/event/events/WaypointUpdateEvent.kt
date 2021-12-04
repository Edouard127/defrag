package com.defrag.client.event.events

import com.defrag.client.event.Event
import com.defrag.client.manager.managers.WaypointManager.Waypoint

class WaypointUpdateEvent(val type: Type, val waypoint: Waypoint?) : Event {
    enum class Type {
        GET, ADD, REMOVE, CLEAR, RELOAD
    }
}