package com.lambda.client.module.modules.misc

import com.lambda.client.module.Category
import com.lambda.client.module.Module
import com.lambda.client.util.text.MessageSendHelper

object GetSelfBed : Module(
    name = "GetSelfBedLocation",
    description = "Get your bed location",
    category = Category.MISC
) {
    init {
        onEnable {
            MessageSendHelper.sendChatMessage("Your bed location: ${mc.player.world.spawnPoint.x}, ${mc.player.world.spawnPoint.y}, ${mc.player.world.spawnPoint.z}")
            disable()
        }
    }
}