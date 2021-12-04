package com.defrag.client.command.commands

import com.defrag.client.command.ClientCommand
import com.defrag.client.module.modules.client.CommandConfig
import com.defrag.client.util.text.MessageSendHelper
import com.defrag.client.util.text.formatValue

object PrefixCommand : ClientCommand(
    name = "prefix",
    description = "Change command prefix"
) {
    init {
        literal("reset") {
            execute("Reset the prefix to ;") {
                CommandConfig.prefix = ";"
                MessageSendHelper.sendChatMessage("Reset prefix to [&7;&f]!")
            }
        }

        string("new prefix") { prefixArg ->
            execute("Set a new prefix") {
                if (prefixArg.value.isEmpty() || prefixArg.value == "\\") {
                    CommandConfig.prefix = ";"
                    MessageSendHelper.sendChatMessage("Reset prefix to [&7;&f]!")
                    return@execute
                }

                CommandConfig.prefix = prefixArg.value
                MessageSendHelper.sendChatMessage("Set prefix to ${formatValue(prefixArg.value)}!")
            }
        }
    }
}