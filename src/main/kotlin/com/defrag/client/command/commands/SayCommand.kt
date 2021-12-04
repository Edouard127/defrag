package com.defrag.client.command.commands

import com.defrag.client.command.ClientCommand
import com.defrag.client.util.text.MessageSendHelper.sendServerMessage

object SayCommand : ClientCommand(
    name = "say",
    description = "Allows you to send any message, even with a prefix in it."
) {
    init {
        greedy("message") { messageArg ->
            executeSafe {
                sendServerMessage(messageArg.value.trim())
            }
        }
    }
}