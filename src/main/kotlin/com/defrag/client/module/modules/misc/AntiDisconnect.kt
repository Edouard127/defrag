package com.defrag.client.module.modules.misc

import com.defrag.client.module.Category
import com.defrag.client.module.Module

object AntiDisconnect : Module(
    name = "AntiDisconnect",
    description = "Are you sure you want to disconnect?",
    category = Category.MISC
) {
    val presses by setting("Button Presses", 3, 1..20, 1)
}