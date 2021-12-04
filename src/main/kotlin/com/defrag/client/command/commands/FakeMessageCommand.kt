package com.defrag.client.command.commands

import com.defrag.client.command.ClientCommand
import com.defrag.client.module.modules.chat.ChatTimestamp
import com.defrag.client.util.text.MessageSendHelper

object FakeMessageCommand : ClientCommand(
    name = "fakemsg",
    alias = arrayOf("fm", "fakemsg"),
    description = "Send a client side fake message, use & with formatting codes."
) {
    init {
        greedy("message") { messageArg ->
            execute("Use & for color formatting") {
                MessageSendHelper.sendRawChatMessage(getTime() + messageArg.value.replace('&', '§'))
            }
        }
    }

    private fun getTime() = if (ChatTimestamp.isEnabled) ChatTimestamp.formattedTime else ""
}