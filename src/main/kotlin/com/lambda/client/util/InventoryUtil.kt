package com.lambda.client.util

import net.minecraft.client.Minecraft
import com.lambda.client.util.InventoryUtil
import net.minecraft.block.Block
import net.minecraft.network.play.client.CPacketHeldItemChange
import net.minecraft.client.gui.inventory.GuiCrafting
import net.minecraft.init.Items
import net.minecraft.inventory.EntityEquipmentSlot
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.init.Enchantments
import net.minecraft.enchantment.Enchantment
import net.minecraft.inventory.Slot
import net.minecraft.item.*
import net.minecraft.network.Packet
import java.util.HashMap
import java.util.concurrent.atomic.AtomicInteger

object InventoryUtil {
    private val mc: Minecraft? = null
    fun switchToHotbarSlot(slot: Int, silent: Boolean) {
        if (mc!!.player.inventory.currentItem == slot || slot < 0) {
            return
        }
        if (silent) {
            mc.player.connection.sendPacket(CPacketHeldItemChange(slot) as Packet<*>)
            mc.playerController.updateController()
        } else {
            mc.player.connection.sendPacket(CPacketHeldItemChange(slot) as Packet<*>)
            mc.player.inventory.currentItem = slot
            mc.playerController.updateController()
        }
    }

    fun switchToHotbarSlot(clazz: Class<*>, silent: Boolean) {
        val slot = findHotbarBlock(clazz)
        if (slot > -1) {
            switchToHotbarSlot(slot, silent)
        }
    }

    fun isNull(stack: ItemStack?): Boolean {
        return stack == null || stack.item is ItemAir
    }

    fun findHotbarBlock(clazz: Class<*>): Int {
        for (i in 0..8) {
            var block: Block?
            val stack = mc!!.player.inventory.getStackInSlot(i)
            if (stack == ItemStack.EMPTY) continue
            if (clazz.isInstance(stack.item)) {
                return i
            }
            if (stack.item !is ItemBlock || !clazz.isInstance((stack.item as ItemBlock).block.also {
                    block = it
                })) continue
            return i
        }
        return -1
    }

    fun findHotbarBlock(blockIn: Block): Int {
        for (i in 0..8) {
            var block: Block?
            val stack = mc!!.player.inventory.getStackInSlot(i)
            if (stack == ItemStack.EMPTY || stack.item !is ItemBlock || (stack.item as ItemBlock).block.also {
                    block = it
                } !== blockIn) continue
            return i
        }
        return -1
    }

    fun getItemHotbar(input: Item?): Int {
        for (i in 0..8) {
            val item = mc!!.player.inventory.getStackInSlot(i).item
            if (Item.getIdFromItem(item as Item) != Item.getIdFromItem(
                    input
                )
            ) continue
            return i
        }
        return -1
    }

    fun findStackInventory(input: Item?): Int {
        return findStackInventory(input, false)
    }

    fun findStackInventory(input: Item?, withHotbar: Boolean): Int {
        var i: Int
        i = if (withHotbar) 0 else 9
        val n = i
        while (i < 36) {
            val item = mc!!.player.inventory.getStackInSlot(i).item
            if (Item.getIdFromItem(input) == Item.getIdFromItem(item as Item)) {
                return i + if (i < 9) 36 else 0
            }
            ++i
        }
        return -1
    }

    fun findItemInventorySlot(item: Item, offHand: Boolean): Int {
        val slot = AtomicInteger()
        slot.set(-1)
        for ((key, value) in inventoryAndHotbarSlots) {
            if (value.item !== item || key == 45 && !offHand) continue
            slot.set(key)
            return slot.get()
        }
        return slot.get()
    }

    fun findInventoryBlock(clazz: Class<*>, offHand: Boolean): Int {
        val slot = AtomicInteger()
        slot.set(-1)
        for ((key, value) in inventoryAndHotbarSlots) {
            if (!isBlock(value.item, clazz) || key == 45 && !offHand) continue
            slot.set(key)
            return slot.get()
        }
        return slot.get()
    }

    fun findEmptySlot(): Int {
        val slot = AtomicInteger()
        slot.set(-1)
        for ((key, value) in inventoryAndHotbarSlots) {
            if (!value.isEmpty) continue
            slot.set(key)
            return slot.get()
        }
        return slot.get()
    }

    fun isBlock(item: Item?, clazz: Class<*>): Boolean {
        if (item is ItemBlock) {
            val block = item.block
            return clazz.isInstance(block)
        }
        return false
    }

    fun confirmSlot(slot: Int) {
        mc!!.player.connection.sendPacket(CPacketHeldItemChange(slot) as Packet<*>)
        mc.player.inventory.currentItem = slot
        mc.playerController.updateController()
    }

    val inventoryAndHotbarSlots: Map<Int, ItemStack>
        get() = if (mc!!.currentScreen is GuiCrafting) {
            fuckYou3arthqu4kev2(10, 45)
        } else getInventorySlots(9, 44)

    private fun getInventorySlots(currentI: Int, last: Int): Map<Int, ItemStack> {
        val fullInventorySlots = HashMap<Int, ItemStack>()
        for (current in currentI..last) {
            fullInventorySlots[current] = mc!!.player.inventoryContainer.inventory[current]
        }
        return fullInventorySlots
    }

    private fun fuckYou3arthqu4kev2(currentI: Int, last: Int): Map<Int, ItemStack> {
        val fullInventorySlots = HashMap<Int, ItemStack>()
        for (current in currentI..last) {
            fullInventorySlots[current] = mc!!.player.openContainer.inventory[current]
        }
        return fullInventorySlots
    }

    fun switchItem(
        back: Boolean,
        lastHotbarSlot: Int,
        switchedItem: Boolean,
        mode: Switch?,
        clazz: Class<*>
    ): BooleanArray {
        val switchedItemSwitched = booleanArrayOf(switchedItem, false)
        when (mode) {
            Switch.NORMAL -> {
                if (!back && !switchedItem) {
                    switchToHotbarSlot(findHotbarBlock(clazz), false)
                    switchedItemSwitched[0] = true
                } else if (back && switchedItem) {
                    switchToHotbarSlot(lastHotbarSlot, false)
                    switchedItemSwitched[0] = false
                }
                switchedItemSwitched[1] = true
            }
            Switch.SILENT -> {
                if (!back && !switchedItem) {
                    switchToHotbarSlot(findHotbarBlock(clazz), true)
                    switchedItemSwitched[0] = true
                }
                switchedItemSwitched[1] = true
            }
            Switch.NONE -> {
                switchedItemSwitched[1] =
                    if (back) true else mc!!.player.inventory.currentItem == findHotbarBlock(clazz)
            }
        }
        return switchedItemSwitched
    }

    fun switchItemToItem(
        back: Boolean,
        lastHotbarSlot: Int,
        switchedItem: Boolean,
        mode: Switch?,
        item: Item?
    ): BooleanArray {
        val switchedItemSwitched = booleanArrayOf(switchedItem, false)
        when (mode) {
            Switch.NORMAL -> {
                if (!back && !switchedItem) {
                    switchToHotbarSlot(getItemHotbar(item), false)
                    switchedItemSwitched[0] = true
                } else if (back && switchedItem) {
                    switchToHotbarSlot(lastHotbarSlot, false)
                    switchedItemSwitched[0] = false
                }
                switchedItemSwitched[1] = true
            }
            Switch.SILENT -> {
                if (!back && !switchedItem) {
                    switchToHotbarSlot(getItemHotbar(item), true)
                    switchedItemSwitched[0] = true
                }
                switchedItemSwitched[1] = true
            }
            Switch.NONE -> {
                switchedItemSwitched[1] = if (back) true else mc!!.player.inventory.currentItem == getItemHotbar(item)
            }
        }
        return switchedItemSwitched
    }

    fun holdingItem(clazz: Class<*>): Boolean {
        var result = false
        val stack = mc!!.player.heldItemMainhand
        result = isInstanceOf(stack, clazz)
        if (!result) {
            val offhand = mc.player.heldItemOffhand
            result = isInstanceOf(stack, clazz)
        }
        return result
    }

    fun isInstanceOf(stack: ItemStack?, clazz: Class<*>): Boolean {
        if (stack == null) {
            return false
        }
        val item = stack.item
        if (clazz.isInstance(item)) {
            return true
        }
        if (item is ItemBlock) {
            val block = Block.getBlockFromItem(item as Item)
            return clazz.isInstance(block)
        }
        return false
    }

    val emptyXCarry: Int
        get() {
            for (i in 1..4) {
                val craftingSlot = mc!!.player.inventoryContainer.inventorySlots[i] as Slot
                val craftingStack = craftingSlot.stack
                if (!craftingStack.isEmpty && craftingStack.item !== Items.AIR) continue
                return i
            }
            return -1
        }

    fun isSlotEmpty(i: Int): Boolean {
        val slot = mc!!.player.inventoryContainer.inventorySlots[i] as Slot
        val stack = slot.stack
        return stack.isEmpty
    }

    fun convertHotbarToInv(input: Int): Int {
        return 36 + input
    }

    fun areStacksCompatible(stack1: ItemStack, stack2: ItemStack): Boolean {
        if (stack1.item != stack2.item) {
            return false
        }
        if (stack1.item is ItemBlock && stack2.item is ItemBlock) {
            val block1 = (stack1.item as ItemBlock).block
            val block2 = (stack2.item as ItemBlock).block
        }
        return if (stack1.displayName != stack2.displayName) {
            false
        } else stack1.itemDamage == stack2.itemDamage
    }

    fun getEquipmentFromSlot(slot: Int): EntityEquipmentSlot {
        if (slot == 5) {
            return EntityEquipmentSlot.HEAD
        }
        if (slot == 6) {
            return EntityEquipmentSlot.CHEST
        }
        return if (slot == 7) {
            EntityEquipmentSlot.LEGS
        } else EntityEquipmentSlot.FEET
    }

    fun findArmorSlot(type: EntityEquipmentSlot, binding: Boolean): Int {
        var slot = -1
        var damage = 0.0f
        for (i in 9..44) {
            var cursed: Boolean
            val s = Minecraft.getMinecraft().player.inventoryContainer.getSlot(i).stack
            if (s.item === Items.AIR || s.item !is ItemArmor) continue
            val armor = s.item as ItemArmor
            if (armor.armorType != type) continue
            val currentDamage = (armor.damageReduceAmount + EnchantmentHelper.getEnchantmentLevel(
                Enchantments.PROTECTION as Enchantment,
                s as ItemStack
            )).toFloat()
            cursed = binding && EnchantmentHelper.hasBindingCurse(s)
            val bl = cursed
            if (currentDamage <= damage || cursed) continue
            damage = currentDamage
            slot = i
        }
        return slot
    }

    fun findArmorSlot(type: EntityEquipmentSlot, binding: Boolean, withXCarry: Boolean): Int {
        var slot = findArmorSlot(type, binding)
        if (slot == -1 && withXCarry) {
            var damage = 0.0f
            for (i in 1..4) {
                var cursed: Boolean
                val craftingSlot = mc!!.player.inventoryContainer.inventorySlots[i] as Slot
                val craftingStack = craftingSlot.stack
                if (craftingStack.item === Items.AIR || craftingStack.item !is ItemArmor) continue
                val armor = craftingStack.item as ItemArmor
                if (armor.armorType != type) continue
                val currentDamage = (armor.damageReduceAmount + EnchantmentHelper.getEnchantmentLevel(
                    Enchantments.PROTECTION as Enchantment,
                    craftingStack as ItemStack
                )).toFloat()
                cursed = binding && EnchantmentHelper.hasBindingCurse(craftingStack)
                val bl = cursed
                if (currentDamage <= damage || cursed) continue
                damage = currentDamage
                slot = i
            }
        }
        return slot
    }

    fun findItemInventorySlot(item: Item, offHand: Boolean, withXCarry: Boolean): Int {
        var slot = findItemInventorySlot(item, offHand)
        if (slot == -1 && withXCarry) {
            for (i in 1..4) {
                var craftingStackItem: Item?
                val craftingSlot = mc!!.player.inventoryContainer.inventorySlots[i] as Slot
                val craftingStack = craftingSlot.stack
                if (craftingStack.item === Items.AIR || craftingStack.item.also {
                        craftingStackItem = it
                    } !== item) continue
                slot = i
            }
        }
        return slot
    }

    fun findBlockSlotInventory(clazz: Class<*>, offHand: Boolean, withXCarry: Boolean): Int {
        var slot = findInventoryBlock(clazz, offHand)
        if (slot == -1 && withXCarry) {
            for (i in 1..4) {
                var block: Block?
                val craftingSlot = mc!!.player.inventoryContainer.inventorySlots[i] as Slot
                val craftingStack = craftingSlot.stack
                if (craftingStack.item === Items.AIR) continue
                val craftingStackItem = craftingStack.item
                if (clazz.isInstance(craftingStackItem)) {
                    slot = i
                    continue
                }
                if (craftingStackItem !is ItemBlock || !clazz.isInstance(craftingStackItem.block.also {
                        block = it
                    })) continue
                slot = i
            }
        }
        return slot
    }

    class Task {
        private val slot: Int
        private val update: Boolean
        private val quickClick: Boolean

        constructor() {
            update = true
            slot = -1
            quickClick = false
        }

        constructor(slot: Int) {
            this.slot = slot
            quickClick = false
            update = false
        }

        constructor(slot: Int, quickClick: Boolean) {
            this.slot = slot
            this.quickClick = quickClick
            update = false
        }

        val isSwitching: Boolean
            get() = !update
    }

    enum class Switch {
        NORMAL, SILENT, NONE
    }
}