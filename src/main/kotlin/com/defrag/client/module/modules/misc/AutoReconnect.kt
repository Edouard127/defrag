package com.defrag.client.module.modules.misc

import com.defrag.client.event.events.GuiEvent
import com.defrag.client.mixin.extension.message
import com.defrag.client.mixin.extension.parentScreen
import com.defrag.client.mixin.extension.reason
import com.defrag.client.module.Category
import com.defrag.client.module.Module
import com.defrag.client.util.StopTimer
import com.defrag.event.listener.listener
import net.minecraft.client.gui.GuiDisconnected
import net.minecraft.client.multiplayer.GuiConnecting
import net.minecraft.client.multiplayer.ServerData
import kotlin.math.max

object AutoReconnect : Module(
    name = "AutoReconnect",
    description = "Automatically reconnects after being disconnected",
    category = Category.MISC,
    alwaysListening = true
) {
    private val delay by setting("Delay", 5.0f, 0.5f..100.0f, 0.5f)

    private var prevServerDate: ServerData? = null

    init {
        listener<GuiEvent.Closed> {
            if (it.screen is GuiConnecting) prevServerDate = mc.currentServerData
        }

        listener<GuiEvent.Displayed> {
            if (isDisabled || (prevServerDate == null && mc.currentServerData == null)) return@listener
            (it.screen as? GuiDisconnected)?.let { gui ->
                it.screen = LambdaGuiDisconnected(gui)
            }
        }
    }

    private class LambdaGuiDisconnected(disconnected: GuiDisconnected) : GuiDisconnected(disconnected.parentScreen, disconnected.reason, disconnected.message) {
        private val timer = StopTimer()

        override fun updateScreen() {
            if (timer.stop() >= (delay * 1000.0f)) {
                mc.displayGuiScreen(GuiConnecting(parentScreen, mc, mc.currentServerData ?: prevServerDate ?: return))
            }
        }

        override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
            super.drawScreen(mouseX, mouseY, partialTicks)
            val ms = max(delay * 1000.0f - timer.stop(), 0.0f).toInt()
            val text = "Reconnecting in ${ms}ms"
            fontRenderer.drawString(text, width / 2f - fontRenderer.getStringWidth(text) / 2f, height - 32f, 0xffffff, true)
        }
    }
}
