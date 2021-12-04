package com.defrag.client.setting

import com.defrag.client.LambdaMod
import com.defrag.client.setting.configs.NameableConfig

internal object GenericConfig : NameableConfig<GenericConfigClass>(
    "generic",
    "${LambdaMod.DIRECTORY}config/"
)