package com.defrag.client.event.events

import com.defrag.client.event.Event
import com.defrag.client.module.AbstractModule

class ModuleToggleEvent internal constructor(val module: AbstractModule) : Event