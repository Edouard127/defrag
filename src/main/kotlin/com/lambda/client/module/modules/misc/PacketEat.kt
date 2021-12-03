package com.lambda.client.module.modules.misc

import com.lambda.client.module.Category
import com.lambda.client.module.Module
import com.lambda.client.util.Wrapper.player
import com.lambda.client.util.text.MessageSendHelper
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