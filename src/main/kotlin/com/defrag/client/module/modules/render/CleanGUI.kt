package com.defrag.client.module.modules.render

import com.defrag.client.module.Category
import com.defrag.client.module.Module

object CleanGUI : Module(
    name = "CleanGUI",
    category = Category.RENDER,
    showOnArray = false,
    description = "Modifies parts of the GUI to be transparent"
) {
    val inventoryGlobal by setting("Inventory", true)
    val chatGlobal by setting("Chat", false)
}