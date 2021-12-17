package com.lambda.client.command.commands

import com.lambda.client.command.ClientCommand
import com.lambda.client.util.text.MessageSendHelper


object rollbackdupe : ClientCommand(
    name = "rollbackdupe",
    alias = arrayOf("rd"),
    description = "femboy dupe"
) {
    init{
        MessageSendHelper.sendChatMessage(".rdupe")



    }


}