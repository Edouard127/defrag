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
                    if(IRC.isEnabled){
                        var message = messageArg.value
                        sendString(IRC.bwriter, "PRIVMSG ${IRC.channel} :${message}")
                        if(message.contains("@${IRC.nickname}")) message = message.replace("@${IRC.nickname}", "§4@${IRC.nickname}§7")
                        MessageSendHelper.sendChatMessage("IRC <${IRC.nickname}>: ${message}")
                    }
                    else MessageSendHelper.sendChatMessage("IRC Module is not enabled")

                }

        }
    }
}