package com.defrag.client.module.modules.movement

import com.defrag.client.module.Category
import com.defrag.client.module.Module
import com.defrag.client.util.TickTimer
import com.defrag.client.util.TimeUnit
import com.defrag.client.util.threads.safeListener
import net.minecraftforge.fml.common.gameevent.TickEvent

object AutoJump : Module(
    name = "AutoJump",
    category = Category.MOVEMENT,
    description = "Automatically jumps if possible"
) {
    private val delay by setting("Tick Delay", 10, 0..40, 1)

    private val timer = TickTimer(TimeUnit.TICKS)

    init {
        safeListener<TickEvent.ClientTickEvent> {
            if (player.isInWater || player.isInLava) player.motionY = 0.1
            else if (player.onGround && timer.tick(delay.toLong())) player.jump()
        }
    }
}