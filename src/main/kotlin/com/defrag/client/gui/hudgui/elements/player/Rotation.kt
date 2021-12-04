package com.defrag.client.gui.hudgui.elements.player

import com.defrag.client.event.SafeClientEvent
import com.defrag.client.gui.hudgui.LabelHud
import com.defrag.client.util.math.RotationUtils
import com.defrag.commons.utils.MathUtils

internal object Rotation : LabelHud(
    name = "Rotation",
    category = Category.PLAYER,
    description = "Player rotation"
) {

    override fun SafeClientEvent.updateText() {
        val yaw = MathUtils.round(RotationUtils.normalizeAngle(mc.player?.rotationYaw ?: 0.0f), 1)
        val pitch = MathUtils.round(mc.player?.rotationPitch ?: 0.0f, 1)

        displayText.add("Yaw", secondaryColor)
        displayText.add(yaw.toString(), primaryColor)
        displayText.add("Pitch", secondaryColor)
        displayText.add(pitch.toString(), primaryColor)
    }

}