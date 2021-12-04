package com.defrag.client.gui.hudgui.window

import com.defrag.client.gui.hudgui.AbstractHudElement
import com.defrag.client.gui.rgui.windows.SettingWindow
import com.defrag.client.setting.settings.AbstractSetting

class HudSettingWindow(
    hudElement: AbstractHudElement,
    posX: Float,
    posY: Float
) : SettingWindow<AbstractHudElement>(hudElement.name, hudElement, posX, posY, SettingGroup.NONE) {

    override fun getSettingList(): List<AbstractSetting<*>> {
        return element.settingList
    }

}