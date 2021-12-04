package com.defrag.client.module.modules.player

import com.defrag.client.manager.managers.TimerManager.modifyTimer
import com.defrag.client.manager.managers.TimerManager.resetTimer
import com.defrag.client.module.Category
import com.defrag.client.module.Module
import com.defrag.event.listener.listener
import net.minecraftforge.fml.common.gameevent.TickEvent

object Timer : Module(
    name = "Timer",
    category = Category.PLAYER,
    description = "Changes your client tick speed",
    modulePriority = 500
) {
    private val slow by setting("Slow Mode", false)
    private val tickNormal by setting("Tick N", 2.0f, 1f..10f, 0.1f, { !slow })
    private val tickSlow by setting("Tick S", 8f, 1f..10f, 0.1f, { slow })

    init {
        onDisable {
            resetTimer()
        }

        listener<TickEvent.ClientTickEvent> {
            if (it.phase != TickEvent.Phase.END) return@listener

            val multiplier = if (!slow) tickNormal else tickSlow / 10.0f
            modifyTimer(50.0f / multiplier)
        }
    }
}