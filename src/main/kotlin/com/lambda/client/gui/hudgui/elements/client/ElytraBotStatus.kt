package com.lambda.client.gui.hudgui.elements.client

import com.lambda.client.event.SafeClientEvent
import com.lambda.client.gui.hudgui.LabelHud
import com.lambda.client.module.modules.client.GuiColors.setting
import com.lambda.client.module.modules.movement.ElytraBotModule
import com.lambda.client.util.color.ColorHolder

internal object ElytraBotStatus : LabelHud(
    name = "Elytra Bot Status",
    category = Category.CLIENT,
    description = "Elytra Bot Status"
) {

    private val textColor by setting("Text Color", ColorHolder(0, 255, 0, 255))

    override fun SafeClientEvent.updateText() {
        if(ElytraBotModule.isEnabled){
            displayText.addLine("Going to ${ElytraBotModule.goal?.x}, ${ElytraBotModule.goal?.y}, ${ElytraBotModule.goal?.z}", textColor)
        }
    }

}