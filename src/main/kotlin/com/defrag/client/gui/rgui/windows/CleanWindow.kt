package com.defrag.client.gui.rgui.windows

import com.defrag.client.gui.rgui.WindowComponent
import com.defrag.client.setting.GuiConfig
import com.defrag.client.setting.configs.AbstractConfig
import com.defrag.commons.interfaces.Nameable

/**
 * Window with no rendering
 */
open class CleanWindow(
    name: String,
    posX: Float,
    posY: Float,
    width: Float,
    height: Float,
    settingGroup: SettingGroup,
    config: AbstractConfig<out Nameable> = GuiConfig
) : WindowComponent(name, posX, posY, width, height, settingGroup, config)