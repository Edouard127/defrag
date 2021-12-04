package com.defrag.client.module.modules.extra

import com.defrag.client.module.Category
import com.defrag.client.module.Module
import com.defrag.client.util.threads.runSafeR

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

