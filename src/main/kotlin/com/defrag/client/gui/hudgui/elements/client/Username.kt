package com.defrag.client.gui.hudgui.elements.client

import com.defrag.client.event.SafeClientEvent
import com.defrag.client.gui.hudgui.LabelHud

internal object Username : LabelHud(
    name = "Username",
    category = Category.CLIENT,
    description = "Player username"
) {

    private val prefix = setting("Prefix", "Welcome")
    private val suffix = setting("Suffix", "")

    override fun SafeClientEvent.updateText() {
        displayText.add(prefix.value, primaryColor)
        displayText.add(mc.session.username, secondaryColor)
        displayText.add(suffix.value, primaryColor)
    }

}