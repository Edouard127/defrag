package com.defrag.client.module.modules.misc

import com.defrag.client.module.Category
import com.defrag.client.module.Module
import com.defrag.client.util.Wrapper.player
import com.defrag.client.util.text.MessageSendHelper
import net.minecraft.init.Items.GOLDEN_APPLE

object PacketEat : Module(
    name = "PacketEat",
    category = Category.MISC,
    description = "Send packet to server to eat"
) {
    fun packeteat(){
        if (player!!.heldItemMainhand.item == GOLDEN_APPLE) {
            MessageSendHelper.sendChatMessage("test")
        }
    }
}