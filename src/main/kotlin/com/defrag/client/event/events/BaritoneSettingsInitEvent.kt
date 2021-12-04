package com.defrag.client.event.events

import com.defrag.client.event.Event
import com.defrag.client.event.DefragEventBus
import com.defrag.client.event.SingletonEvent

/**
 * Posted at the return of when Baritone's Settings are initialized.
 */
object BaritoneSettingsInitEvent : Event, SingletonEvent(DefragEventBus)