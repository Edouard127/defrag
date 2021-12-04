package com.defrag.client.gui.hudgui.elements.misc

import com.defrag.client.event.SafeClientEvent
import com.defrag.client.gui.hudgui.LabelHud
import com.defrag.client.util.CircularArray
import com.defrag.client.util.CircularArray.Companion.average
import com.defrag.client.util.TpsCalculator

internal object TPS : LabelHud(
    name = "TPS",
    category = Category.MISC,
    description = "Server TPS"
) {

    private val tickLength by setting("Tick Length", false, description = "Display tick length in millisseconds instead")

    // buffered TPS readings to add some fluidity to the TPS HUD element
    private val tpsBuffer = CircularArray(120, 20.0f)

    override fun SafeClientEvent.updateText() {
        tpsBuffer.add(TpsCalculator.tickRate)
        val avg = tpsBuffer.average()

        if (tickLength) {
            // If the Value returns Zero, it reads "Infinity mspt"
            if (avg == 0.0f) {
                displayText.add("%.2f".format(0.0f), primaryColor)
            } else {
                displayText.add("%.2f".format(1000.0f / avg), primaryColor)
            }

            displayText.add("mspt", secondaryColor)
        } else {
            displayText.add("%.2f".format(avg), primaryColor)
            displayText.add("tps", secondaryColor)
        }
    }

}
