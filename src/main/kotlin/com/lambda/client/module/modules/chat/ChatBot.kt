package com.lambda.client.module.modules.chat

import com.lambda.client.module.Category
import com.lambda.client.module.Module
import com.lambda.client.module.modules.events.BlockBreakingEvent
import com.lambda.client.util.text.MessageSendHelper
import com.lambda.event.listener.listener
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.network.play.client.CPacketChatMessage
import net.minecraftforge.event.entity.EntityEvent
import net.minecraftforge.event.entity.EntityJoinWorldEvent

object ChatBot : Module(
    name = "ChatBot",
    description = "Automatise events in chat",
    category = Category.CHAT
) {


    init {
        onEnable {
            listener<EntityEvent>{
                MessageSendHelper.sendChatMessage(it.result.name)
            }
            listener<EntityJoinWorldEvent> {
                MessageSendHelper.sendChatMessage(it.entity.name)
            }
            listener<BlockBreakingEvent>{
                MessageSendHelper.sendChatMessage("${it.breakStage} ${it.result.name}")
            }
        }
    }
}