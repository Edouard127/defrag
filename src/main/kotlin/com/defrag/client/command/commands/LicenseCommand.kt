package com.defrag.client.command.commands

import com.defrag.client.command.ClientCommand
import com.defrag.client.util.text.MessageSendHelper

object LicenseCommand : ClientCommand(
    name = "license",
    description = "Information about Lambda's license"
) {
    init {
        execute {
            MessageSendHelper.sendChatMessage("You can view Lambda's &7client&f License (LGPLv3) at &9https://lambda-client.org/license")
        }
    }
}