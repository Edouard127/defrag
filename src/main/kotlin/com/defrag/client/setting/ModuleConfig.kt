package com.defrag.client.setting

import com.defrag.client.LambdaMod
import com.defrag.client.module.AbstractModule
import com.defrag.client.module.modules.client.Configurations
import com.defrag.client.setting.configs.NameableConfig
import java.io.File

internal object ModuleConfig : NameableConfig<AbstractModule>(
    "modules",
    "${LambdaMod.DIRECTORY}config/modules",
) {
    override val file: File get() = File("$filePath/${Configurations.modulePreset}.json")
    override val backup get() = File("$filePath/${Configurations.modulePreset}.bak")
}