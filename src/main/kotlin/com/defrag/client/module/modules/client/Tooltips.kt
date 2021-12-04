package com.defrag.client.module.modules.client

import com.defrag.client.module.Category
import com.defrag.client.module.Module

object Tooltips : Module(
    name = "Tooltips",
    description = "Displays handy module descriptions in the GUI",
    category = Category.CLIENT,
    showOnArray = false,
    enabledByDefault = true
)
