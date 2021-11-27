package me.han.muffin.client.utils.extensions.mc.item

import net.minecraft.inventory.Slot

class HotbarSlot(slot: Slot) : Slot(slot.inventory, slot.slotIndex, slot.xPos, slot.yPos) {
    init {
        slotNumber = slot.slotNumber
    }

    val hotbarSlot = slot.slotNumber - 36
}