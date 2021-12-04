package com.defrag.client.gui.hudgui.elements.misc

import com.defrag.client.event.SafeClientEvent
import com.defrag.client.gui.hudgui.LabelHud
import com.defrag.client.util.InfoCalculator

internal object Ping : LabelHud(
    name = "Ping",
    category = Category.MISC,
    description = "Delay between client and server"
) {

    override fun SafeClientEvent.updateText() {
        displayText.add(InfoCalculator.ping().toString(), primaryColor)
        displayText.add("ms", secondaryColor)
    }

}