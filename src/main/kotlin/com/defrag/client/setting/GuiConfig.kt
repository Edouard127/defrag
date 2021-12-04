package com.defrag.client.setting

import com.defrag.client.LambdaMod
import com.defrag.client.gui.rgui.Component
import com.defrag.client.module.modules.client.Configurations
import com.defrag.client.plugin.api.IPluginClass
import com.defrag.client.setting.configs.AbstractConfig
import com.defrag.client.setting.configs.PluginConfig
import com.defrag.client.setting.settings.AbstractSetting
import java.io.File

internal object GuiConfig : AbstractConfig<Component>(
    "gui",
    "${LambdaMod.DIRECTORY}config/gui"
) {
    override val file: File get() = File("$filePath/${Configurations.guiPreset}.json")
    override val backup get() = File("$filePath/${Configurations.guiPreset}.bak")

    override fun addSettingToConfig(owner: Component, setting: AbstractSetting<*>) {
        if (owner is IPluginClass) {
            (owner.config as PluginConfig).addSettingToConfig(owner, setting)
        } else {
            val groupName = owner.settingGroup.groupName
            if (groupName.isNotEmpty()) {
                getGroupOrPut(groupName).getGroupOrPut(owner.name).addSetting(setting)
            }
        }
    }

    override fun getSettings(owner: Component): List<AbstractSetting<*>> {
        return if (owner is IPluginClass) {
            (owner.config as PluginConfig).getSettings(owner)
        } else {
            val groupName = owner.settingGroup.groupName
            if (groupName.isNotEmpty()) {
                getGroupOrPut(groupName).getGroupOrPut(owner.name).getSettings()
            } else {
                emptyList()
            }
        }
    }
}