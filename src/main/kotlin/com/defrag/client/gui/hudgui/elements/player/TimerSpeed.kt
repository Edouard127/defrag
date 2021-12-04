package com.defrag.client.gui.hudgui.elements.player

import com.defrag.client.event.SafeClientEvent
import com.defrag.client.gui.hudgui.LabelHud
import com.defrag.client.manager.managers.TimerManager
import com.defrag.commons.utils.MathUtils

internal object TimerSpeed : LabelHud(
    name = "TimerSpeed",
    category = Category.PLAYER,
    description = "Client side timer speed"
) {

    override fun SafeClientEvent.updateText() {
        val timerSpeed = MathUtils.round(50.0f / TimerManager.tickLength, 2)

        displayText.add("%.2f".format(timerSpeed), primaryColor)
        displayText.add("x", secondaryColor)
    }

}