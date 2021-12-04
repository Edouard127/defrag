package com.defrag.client.module.modules.misc

import com.defrag.client.event.events.GuiEvent
import com.defrag.client.manager.managers.WaypointManager
import com.defrag.client.module.Category
import com.defrag.client.module.Module
import com.defrag.client.util.InfoCalculator
import com.defrag.client.util.math.CoordinateConverter.asString
import com.defrag.client.util.text.MessageSendHelper
import com.defrag.client.util.threads.safeListener
import net.minecraft.client.gui.GuiGameOver

object AutoRespawn : Module(
    name = "AutoRespawn",
    description = "Automatically respawn after dying",
    category = Category.MISC
) {
    private val respawn = setting("Respawn", true)
    private val deathCoords = setting("Save Death Coords", true)
    private val antiGlitchScreen = setting("Anti Glitch Screen", true)

    init {
        safeListener<GuiEvent.Displayed> {
            if (it.screen !is GuiGameOver) return@safeListener

            if (deathCoords.value && player.health <= 0) {
                WaypointManager.add("Death - " + InfoCalculator.getServerType())
                MessageSendHelper.sendChatMessage("You died at ${player.position.asString()}")
            }

            if (respawn.value || antiGlitchScreen.value && player.health > 0) {
                player.respawnPlayer()
                mc.displayGuiScreen(null)
            }
        }
    }
}