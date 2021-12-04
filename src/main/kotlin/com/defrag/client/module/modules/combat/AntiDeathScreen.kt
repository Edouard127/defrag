package com.defrag.client.module.modules.combat

import com.defrag.client.event.events.GuiEvent
import com.defrag.client.module.Category
import com.defrag.client.module.Module
import com.defrag.client.util.threads.safeListener
import net.minecraft.client.gui.GuiGameOver

object AntiDeathScreen : Module(
    name = "AntiDeathScreen",
    description = "Fixes random death screen glitches",
    category = Category.COMBAT
) {
    init {
        safeListener<GuiEvent.Displayed> {
            if (it.screen !is GuiGameOver) return@safeListener
            if (player.health > 0) {
                player.respawnPlayer()
                mc.displayGuiScreen(null)
            }
        }
    }
}