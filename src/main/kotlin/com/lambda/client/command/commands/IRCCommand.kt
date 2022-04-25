package com.lambda.client.command.commands

import com.lambda.client.command.ClientCommand
import com.lambda.client.module.modules.client.IRC
import com.lambda.client.module.modules.client.IRC.sendString
import com.lambda.client.util.text.MessageSendHelper

object IRCCommand : ClientCommand(
    name = "irc",
    description = "Talk on the IRC channel"
) {
    init {
        greedy("message") { messageArg ->

            executeSafe {
                println(messageArg.value.trim())
                sendString(IRC.bwriter, "PRIVMSG ${IRC.channel} :${messageArg.value}")
                MessageSendHelper.sendRawChatMessage("IRC: <${IRC.nickname}>: ${messageArg.value}")
            }
        }
    }
}