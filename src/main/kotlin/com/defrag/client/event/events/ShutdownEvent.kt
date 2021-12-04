package com.defrag.client.event.events

import com.defrag.client.event.Event
import com.defrag.client.event.LambdaEventBus
import com.defrag.client.event.SingletonEvent

object ShutdownEvent : Event, SingletonEvent(LambdaEventBus)