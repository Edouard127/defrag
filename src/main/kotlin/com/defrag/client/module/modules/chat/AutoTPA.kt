package com.defrag.client.module.modules.chat

import com.defrag.client.event.events.PacketEvent
import com.defrag.client.manager.managers.FriendManager
import com.defrag.client.module.Category
import com.defrag.client.module.Module
import com.defrag.client.util.text.MessageDetection
import com.defrag.client.util.text.MessageSendHelper.sendServerMessage
import com.defrag.event.listener.listener
import net.minecraft.network.play.server.SPacketChat

object AutoTPA : Module(
    name = "AutoTPA",
    description = "Automatically accept or decline /TPAs",
    category = Category.CHAT
) {
    private val friends by setting("Always Accept Friends", true)
    private val mode by setting("Response", Mode.DENY)

    private enum class Mode {
        ACCEPT, DENY
    }

    init {
        listener<PacketEvent.Receive> {
            if (it.packet !is SPacketChat || MessageDetection.Other.TPA_REQUEST detectNot it.packet.chatComponent.unformattedText) return@listener

            /* I tested that getting the first word is compatible with chat timestamp, and it as, as this is Receive and chat timestamp is after Receive */
            val name = it.packet.chatComponent.unformattedText.split(" ")[0]

            when (mode) {
                Mode.ACCEPT -> sendServerMessage("/tpaccept $name")
                Mode.DENY -> {
                    if (friends && FriendManager.isFriend(name)) {
                        sendServerMessage("/tpaccept $name")
                    } else {
                        sendServerMessage("/tpdeny $name")
                    }
                }
            }
        }
    }
}