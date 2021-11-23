package com.lambda.client.module.modules.misc

import com.lambda.client.module.Category
import com.lambda.client.module.Module

object AntiDisconnect : Module(
    name = "AntiDisconnect",
    description = "Are you sure you want to disconnect?",
    category = Category.MISC
) {
    val presses by setting("Button Presses", 3, 1..20, 1)
}