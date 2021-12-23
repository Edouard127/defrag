package com.lambda.client.module.modules.client

import com.lambda.client.module.Category
import com.lambda.client.module.Module
import com.lambda.client.util.color.ColorHolder

object GuiColors : Module(
    name = "GuiColors",
    description = "Customize gui colors",
    showOnArray = false,
    category = Category.CLIENT,
    alwaysEnabled = true
) {
    private val primarySetting by setting("Primary Color", ColorHolder(43, 113, 30, 255))
    private val outlineSetting by setting("Outline Color", ColorHolder(0, 193, 0, 200))
    private val backgroundSetting by setting("Background Color", ColorHolder(0, 53, 0, 235))
    private val textSetting by setting("Text Color", ColorHolder(0, 255, 0, 255))
    private val aHover by setting("Hover Alpha", 32, 0..255, 1)

    val primary get() = primarySetting.clone()
    val idle get() = if (primary.averageBrightness < 0.8f) ColorHolder(255, 255, 255, 0) else ColorHolder(0, 0, 0, 0)
    val hover get() = idle.apply { a = aHover }
    val click get() = idle.apply { a = aHover * 2 }
    val backGround get() = backgroundSetting.clone()
    val outline get() = outlineSetting.clone()
    val text get() = textSetting.clone()
}
