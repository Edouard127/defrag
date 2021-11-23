package com.lambda.client.module.modules.events

import net.minecraft.entity.player.EntityPlayer
import com.lambda.client.module.modules.EventStage

class DeathEvent(var player: EntityPlayer) : EventStage()