package com.defrag.client.module.modules.player

import com.defrag.client.event.events.PacketEvent
import com.defrag.client.mixin.extension.windowID
import com.defrag.client.module.Category
import com.defrag.client.module.Module
import com.defrag.event.listener.listener
import net.minecraft.network.play.client.CPacketCloseWindow

object XCarry : Module(
    name = "XCarry",
    category = Category.PLAYER,
    description = "Store items in crafting slots"
) {
    init {
        listener<PacketEvent.Send> {
            if (it.packet is CPacketCloseWindow && it.packet.windowID == 0) {
                it.cancel()
            }
        }
    }
}