package com.defrag.client.module.modules.combat

import com.defrag.client.manager.managers.CombatManager
import com.defrag.client.module.Category
import com.defrag.client.module.Module
import com.defrag.client.util.items.swapToItem
import com.defrag.client.util.math.RotationUtils.faceEntityClosest
import com.defrag.client.util.threads.safeListener
import net.minecraft.init.Items
import net.minecraftforge.fml.common.gameevent.TickEvent

@CombatManager.CombatModule
object AimBot : Module(
    name = "AimBot",
    description = "Automatically aims at entities for you.",
    category = Category.COMBAT,
    modulePriority = 20
) {
    private val bowOnly by setting("Bow Only", true)
    private val autoSwap by setting("Auto Swap", false)

    init {
        safeListener<TickEvent.ClientTickEvent> {
            if (player.heldItemMainhand.item != Items.BOW) {
                if (autoSwap) swapToItem(Items.BOW)
                if (bowOnly) return@safeListener
            }

            CombatManager.target?.let {
                faceEntityClosest(it)
            }
        }
    }
}