package com.defrag.client.gui.clickgui.window

import com.defrag.client.gui.rgui.windows.SettingWindow
import com.defrag.client.module.AbstractModule
import com.defrag.client.setting.settings.AbstractSetting

class ModuleSettingWindow(
    module: AbstractModule,
    posX: Float,
    posY: Float
) : SettingWindow<AbstractModule>(module.name, module, posX, posY, SettingGroup.NONE) {

    override fun getSettingList(): List<AbstractSetting<*>> {
        return element.fullSettingList.filter { it.name != "Enabled" && it.name != "Clicks" }
    }

}