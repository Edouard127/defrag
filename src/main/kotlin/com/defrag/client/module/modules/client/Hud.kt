package com.defrag.client.module.modules.client

import com.defrag.client.module.Category
import com.defrag.client.module.Module
import com.defrag.client.util.color.ColorHolder

object Hud : Module(
    name = "Hud",
    description = "Toggles Hud displaying and settings",
    category = Category.CLIENT,
    showOnArray = false,
    enabledByDefault = true
) {
    val hudFrame by setting("Hud Frame", false)
    val primaryColor by setting("Primary Color", ColorHolder(255, 240, 246), false)
    val secondaryColor by setting("Secondary Color", ColorHolder(108, 0, 43), false)
}