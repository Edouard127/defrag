package com.defrag.client.module.modules.client

import com.defrag.client.event.events.ShutdownEvent
import com.defrag.client.gui.hudgui.LambdaHudGui
import com.defrag.client.module.Category
import com.defrag.client.module.Module
import com.defrag.event.listener.listener

object HudEditor : Module(
    name = "HudEditor",
    description = "Edits the Hud",
    category = Category.CLIENT,
    showOnArray = false
) {
    init {
        onEnable {
            if (mc.currentScreen !is LambdaHudGui) {
                ClickGUI.disable()
                mc.displayGuiScreen(LambdaHudGui)
                LambdaHudGui.onDisplayed()
            }
        }

        onDisable {
            if (mc.currentScreen is LambdaHudGui) {
                mc.displayGuiScreen(null)
            }
        }

        listener<ShutdownEvent> {
            disable()
        }
    }
}
