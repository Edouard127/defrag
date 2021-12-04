package com.defrag.client.setting.configs

import com.defrag.client.setting.settings.AbstractSetting
import com.defrag.commons.interfaces.Nameable

open class NameableConfig<T : Nameable>(
    name: String,
    filePath: String
) : AbstractConfig<T>(name, filePath) {

    override fun addSettingToConfig(owner: T, setting: AbstractSetting<*>) {
        getGroupOrPut(owner.name).addSetting(setting)
    }

    override fun getSettings(owner: T) = getGroup(owner.name)?.getSettings() ?: emptyList()
}
