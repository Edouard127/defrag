package com.defrag.client.gui.rgui.component

import com.defrag.client.module.modules.client.ClickGUI
import com.defrag.client.setting.settings.impl.primitive.BooleanSetting
import com.defrag.client.util.math.Vec2f

class SettingButton(val setting: BooleanSetting) : BooleanSlider(setting.name, 0.0, setting.description, setting.visibility) {

    override val isBold
        get() = setting.isModified && ClickGUI.showModifiedInBold

    init {
        if (setting.value) value = 1.0
    }

    override fun onTick() {
        super.onTick()
        value = if (setting.value) 1.0 else 0.0
    }

    override fun onClick(mousePos: Vec2f, buttonId: Int) {
        super.onClick(mousePos, buttonId)
        setting.value = !setting.value
    }
}