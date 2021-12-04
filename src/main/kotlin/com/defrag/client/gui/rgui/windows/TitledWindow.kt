package com.defrag.client.gui.rgui.windows

import com.defrag.client.module.modules.client.CustomFont
import com.defrag.client.module.modules.client.GuiColors
import com.defrag.client.util.graphics.VertexHelper
import com.defrag.client.util.graphics.font.FontRenderAdapter
import com.defrag.client.util.math.Vec2f

/**
 * Window with rectangle and title rendering
 */
open class TitledWindow(
    name: String,
    posX: Float,
    posY: Float,
    width: Float,
    height: Float,
    settingGroup: SettingGroup
) : BasicWindow(name, posX, posY, width, height, settingGroup) {
    override val draggableHeight: Float get() = FontRenderAdapter.getFontHeight() + 5.0f

    override val minimizable get() = true

    override fun onRender(vertexHelper: VertexHelper, absolutePos: Vec2f) {
        super.onRender(vertexHelper, absolutePos)
        FontRenderAdapter.drawString(componentName, 3.0f, 3.0f, color = GuiColors.text, drawShadow = CustomFont.shadow)
    }
}