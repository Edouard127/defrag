package com.lambda.client.module.modules.client

import com.lambda.client.module.Category
import com.lambda.client.module.Module
import com.lambda.client.util.color.ColorHolder

object Hud : Module(
    name = "Hud",
    description = "Toggles Hud displaying and settings",
    category = Category.CLIENT,
    showOnArray = false,
    enabledByDefault = true
) {
    val hudFrame by setting("Hud Frame", false)
    val primaryColor by setting("Primary Color", ColorHolder(0, 255, 0), false)
    val secondaryColor by setting("Secondary Color", ColorHolder(155, 255, 155), false)
}
