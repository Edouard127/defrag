package com.lambda.client.module.modules.combat

import com.lambda.client.event.SafeClientEvent
import com.lambda.client.event.events.PacketEvent
import com.lambda.client.manager.managers.CombatManager
import com.lambda.client.module.Category
import com.lambda.client.module.Module
import com.lambda.client.util.Bind
import com.lambda.client.util.TickTimer
import com.lambda.client.util.TimeUnit
import com.lambda.client.util.combat.CombatUtils.calcDamageFromMob
import com.lambda.client.util.combat.CombatUtils.calcDamageFromPlayer
import com.lambda.client.util.combat.CombatUtils.scaledHealth
import com.lambda.client.util.items.*
import com.lambda.client.util.text.MessageSendHelper
import com.lambda.client.util.threads.safeListener
import com.lambda.commons.extension.next
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.entity.item.EntityEnderCrystal
import net.minecraft.entity.monster.EntityMob
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.MobEffects
import net.minecraft.inventory.Slot
import net.minecraft.item.ItemAppleGold
import net.minecraft.item.ItemEndCrystal
import net.minecraft.item.ItemPotion
import net.minecraft.item.ItemStack
import net.minecraft.network.play.server.SPacketConfirmTransaction
import net.minecraft.potion.PotionUtils
import net.minecraftforge.fml.common.gameevent.InputEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.lwjgl.input.Keyboard
import java.lang.Float.max
import kotlin.math.ceil

object CrystalAuraGapple : Module(
    name = "AutoGapple",
    description = "Manages item in your offhand",
    category = Category.COMBAT
) {
    private val type by setting("Type", Type.GAPPLE)

    // Gapple
    private val offhandGapple by setting("Offhand Gapple", false, { type == Type.GAPPLE })
    private val bindGapple by setting("Bind Gapple", Bind(), { type == Type.GAPPLE && offhandGapple })
    private val checkAuraG by setting("Check Aura G", true, { type == Type.GAPPLE && offhandGapple })
    private val checkWeaponG by setting("Check Weapon G", false, { type == Type.GAPPLE && offhandGapple })

 

    // General
    private val priority by setting("Priority", Priority.HOTBAR)
    private val switchMessage by setting("Switch Message", true)
    private val delay by setting("Delay", 2, 1..20, 1,
        description = "Ticks to wait between each move")
    private val confirmTimeout by setting("Confirm Timeout", 5, 1..20, 1,
        description = "Maximum ticks to wait for confirm packets from server")

    enum class Type(val filter: (ItemStack) -> Boolean) {
        GAPPLE({ it.item is ItemAppleGold }),
    }

    @Suppress("UNUSED")
    private enum class Priority {
        HOTBAR, INVENTORY
    }

    val transactionLog = HashMap<Short, Boolean>()
    val confirmTimer = TickTimer(TimeUnit.TICKS)
    val movingTimer = TickTimer(TimeUnit.TICKS)
    private var maxDamage = 0f

    init {
        safeListener<InputEvent.KeyInputEvent> {
            val key = Keyboard.getEventKey()
            when {
                bindGapple.isDown(key) -> switchToType(Type.GAPPLE)
            }
        }

        safeListener<PacketEvent.Receive> {
            if (it.packet !is SPacketConfirmTransaction || it.packet.windowId != 0 || !transactionLog.containsKey(it.packet.actionNumber)) return@safeListener

            transactionLog[it.packet.actionNumber] = true
            if (!transactionLog.containsValue(false)) {
                confirmTimer.reset(confirmTimeout * -50L) // If all the click packets were accepted then we reset the timer for next moving
            }
        }

        safeListener<TickEvent.ClientTickEvent>(1100) {
            if (player.isDead || player.health <= 0.0f) return@safeListener

            if (!confirmTimer.tick(confirmTimeout.toLong(), false)) return@safeListener
            if (!movingTimer.tick(delay.toLong(), false)) return@safeListener // Delays `delay` ticks


            if (!player.inventory.itemStack.isEmpty) { // If player is holding an in inventory
                if (mc.currentScreen is GuiContainer) { // If inventory is open (playing moving item)
                    movingTimer.reset() // reset movingTimer as the user is currently interacting with the inventory.
                } else { // If inventory is not open (ex. inventory desync)
                    removeHoldingItem()
                }
                return@safeListener
            }

            switchToType(getType(), true)
        }
    }

    private fun SafeClientEvent.getType() = when {
        checkGapple() -> Type.GAPPLE
        else -> null
    }


    private fun SafeClientEvent.checkGapple() = offhandGapple
        && (checkAuraG && CombatManager.isActiveAndTopPriority(KillAura)
        || checkWeaponG && player.heldItemMainhand.item.isWeapon)

    private fun SafeClientEvent.switchToType(typeOriginal: Type?, alternativeType: Boolean = false) {
        // First check for whether player is holding the right item already or not
        if (typeOriginal == null || checkOffhandItem(typeOriginal)) return

        val attempts = if (alternativeType) 4 else 1

        getItemSlot(typeOriginal, attempts)?.let { (slot, typeAlt) ->
            if (slot == player.offhandSlot) return

            transactionLog.clear()
            moveToSlot(slot, player.offhandSlot).forEach {
                transactionLog[it] = false
            }

            playerController.updateController()

            confirmTimer.reset()
            movingTimer.reset()

            if (switchMessage) MessageSendHelper.sendChatMessage("$chatName Offhand now has a ${typeAlt.toString().lowercase()}")
        }
    }

    fun SafeClientEvent.checkOffhandItem(type: Type) = type.filter(player.heldItemOffhand)

    fun SafeClientEvent.getItemSlot(type: Type, attempts: Int): Pair<Slot, Type>? =
        getSlot(type)?.to(type)
            ?: if (attempts > 1) {
                getItemSlot(type.next(), attempts - 1)
            } else {
                null
            }

    private fun SafeClientEvent.getSlot(type: Type): Slot? {
        return player.offhandSlot.takeIf(filter(type))
            ?: if (priority == Priority.HOTBAR) {
                player.hotbarSlots.findItemByType(type)
                    ?: player.inventorySlots.findItemByType(type)
                    ?: player.craftingSlots.findItemByType(type)
            } else {
                player.inventorySlots.findItemByType(type)
                    ?: player.hotbarSlots.findItemByType(type)
                    ?: player.craftingSlots.findItemByType(type)
            }
    }

    private fun List<Slot>.findItemByType(type: Type) =
        find(filter(type))

    private fun filter(type: Type) = { it: Slot ->
        type.filter(it.stack)
    }


    private val SafeClientEvent.nextFallDist get() = player.fallDistance - player.motionY.toFloat()
}