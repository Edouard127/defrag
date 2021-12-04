package com.defrag.client.event.events

import com.defrag.client.event.Event
import com.defrag.client.event.DefragEventBus
import com.defrag.client.event.SingletonEvent

object ShutdownEvent : Event, SingletonEvent(DefragEventBus)