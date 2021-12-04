package com.defrag.client.plugin.api

import com.defrag.commons.interfaces.Nameable

interface IPluginClass : Nameable {
    val pluginMain: Plugin
}