package com.defrag.client.module.modules.movement

import com.defrag.client.module.Category
import com.defrag.client.module.Module

object Avoid : Module(
    name = "Avoid",
    category = Category.MOVEMENT,
    description = "Prevents contact with certain objects"
) {
    val fire by setting("Fire", true)
    val cactus by setting("Cactus", true)
    val unloaded by setting("Unloaded Chunks", true)
}