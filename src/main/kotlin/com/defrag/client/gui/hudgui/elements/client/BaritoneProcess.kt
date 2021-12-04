package com.defrag.client.gui.hudgui.elements.client

import com.defrag.client.event.SafeClientEvent
import com.defrag.client.gui.hudgui.LabelHud
import com.defrag.client.module.modules.movement.AutoWalk
import com.defrag.client.process.PauseProcess
import com.defrag.client.util.BaritoneUtils

internal object BaritoneProcess : LabelHud(
    name = "BaritoneProcess",
    category = Category.CLIENT,
    description = "Shows what Baritone is doing"
) {

    override fun SafeClientEvent.updateText() {
        val process = BaritoneUtils.primary?.pathingControlManager?.mostRecentInControl()?.orElse(null) ?: return

        when {
            process == PauseProcess -> {
                displayText.addLine(process.displayName0())
            }
            AutoWalk.baritoneWalk -> {
                displayText.addLine("AutoWalk (${AutoWalk.direction.displayName})")
            }
            else -> {
                displayText.addLine("Process: ${process.displayName()}")
            }
        }
    }

}