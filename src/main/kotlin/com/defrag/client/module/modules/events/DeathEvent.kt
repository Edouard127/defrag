package com.defrag.client.module.modules.events

import net.minecraft.entity.player.EntityPlayer
import com.defrag.client.module.modules.EventStage

class DeathEvent(var player: EntityPlayer) : EventStage()