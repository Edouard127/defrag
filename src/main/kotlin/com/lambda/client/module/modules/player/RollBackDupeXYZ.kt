package com.lambda.client.module.modules.player

import com.lambda.client.module.Category
import com.lambda.client.module.Module

object RollBackDupeXYZ : Module(
    name = "RollBackDupeXYZ",
    category = Category.PLAYER,
    description = "Censore xyz coordinated"
) {
    val xyz by setting("Censore", true)
}