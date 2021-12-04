package com.defrag.client.event.events

import com.defrag.client.event.Cancellable
import com.defrag.client.event.Event
import net.minecraft.entity.Entity

class PlayerAttackEvent(val entity: Entity) : Event, Cancellable()