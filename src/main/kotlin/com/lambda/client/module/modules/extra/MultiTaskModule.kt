package com.lambda.client.module.modules.extra

import com.lambda.client.module.Category
import com.lambda.client.module.Module
import com.lambda.client.util.threads.runSafeR

object MultiTaskModule : Module(
    name = "MultiTask",
    description = "Perform multiple actions at once",
    category = Category.MISC,
) {
    data class AllowInteractEvent(var isUsingItem: Boolean)

    private fun allowInteract(event: AllowInteractEvent) {
        event.isUsingItem = false
    }

    init {
        onEnable {
            runSafeR {
                allowInteract(AllowInteractEvent(false))
            } ?: disable()
        }
    }
}

