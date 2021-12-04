package com.defrag.client.module.modules.player

import com.defrag.client.module.Category
import com.defrag.client.module.Module

object TpsSync : Module(
    name = "TpsSync",
    description = "Synchronizes block states with the server TPS",
    category = Category.PLAYER
)
