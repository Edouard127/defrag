package com.defrag.client.module.modules.player

import com.defrag.client.event.events.PacketEvent
import com.defrag.client.mixin.extension.rotationPitch
import com.defrag.client.mixin.extension.rotationYaw
import com.defrag.client.module.Category
import com.defrag.client.module.Module
import com.defrag.client.util.threads.safeListener
import net.minecraft.network.play.server.SPacketPlayerPosLook

object AntiForceLook : Module(
    name = "AntiForceLook",
    category = Category.PLAYER,
    description = "Stops server packets from turning your head"
) {
    init {
        safeListener<PacketEvent.Receive> {
            if (it.packet !is SPacketPlayerPosLook) return@safeListener
            it.packet.rotationYaw = player.rotationYaw
            it.packet.rotationPitch = player.rotationPitch
        }
    }
}