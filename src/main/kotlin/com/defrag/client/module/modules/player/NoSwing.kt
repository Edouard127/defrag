package com.defrag.client.module.modules.player

import com.defrag.client.event.events.PacketEvent
import com.defrag.client.module.Category
import com.defrag.client.module.Module
import com.defrag.client.util.threads.safeListener
import com.defrag.event.listener.listener
import net.minecraft.network.play.client.CPacketAnimation
import net.minecraftforge.fml.common.gameevent.TickEvent

object NoSwing : Module(
    name = "NoSwing",
    category = Category.PLAYER,
    description = "Cancels server or client swing animation"
) {
    private val mode by setting("Mode", Mode.CLIENT)

    private enum class Mode {
        CLIENT, SERVER
    }

    init {
        listener<PacketEvent.Send> {
            if (mode == Mode.SERVER && it.packet is CPacketAnimation) it.cancel()
        }

        safeListener<TickEvent.ClientTickEvent> {
            player.isSwingInProgress = false
            player.swingProgressInt = 0
            player.swingProgress = 0.0f
            player.prevSwingProgress = 0.0f
        }
    }
}