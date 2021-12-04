package com.lambda.client.module.modules.combat

import baritone.api.utils.Helper
import com.lambda.client.manager.managers.HotbarManager.serverSideItem
import com.lambda.client.module.Category
import com.lambda.client.module.Module
import com.lambda.client.util.PacketEvent
import com.lambda.client.util.text.MessageSendHelper
import com.lambda.client.util.threads.safeListener
import net.minecraft.init.Items.BOW
import net.minecraft.item.ItemBow
import net.minecraft.item.ItemEgg
import net.minecraft.item.ItemEnderPearl
import net.minecraft.item.ItemSnowball
import net.minecraft.network.Packet
import net.minecraft.network.play.client.CPacketEntityAction
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraft.network.play.client.CPacketPlayer.PositionRotation
import net.minecraft.network.play.client.CPacketPlayerDigging
import net.minecraft.network.play.client.CPacketPlayerTryUseItem
import net.minecraft.util.EnumHand
import net.minecraft.util.math.BlockPos
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.InputEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import java.lang.Thread.sleep


private var shooting = false
private var lastShootTime: Long = 0
object FastUse : Module(
    name = "BowBomb",
    category = Category.COMBAT,
    description = "owo"
) {
    private val force by setting("Force", 150, 150..999, 0)
    private val debug by setting("Debug", false)
    private val snowballs by setting("Debug", true)
    private val bypass by setting("Bypass", false)
    private val spoofs by setting("Spoofs", 10, 1..300, 0)
    private val Timeout by setting("Timeout", 5000, 100..10000, 0)
    private val Bows by setting("Bow", true)
    private val eggs by setting("Bow", false)
    private val pearls by setting("Bow", false)
    private var ticks: Int = 0

    /*if(mc.player.inventory.getCurrentItem().item == BOW && mc.player.itemInUseMaxCount >= 20){
                    MessageSendHelper.sendChatMessage("Test")
                    mc.player.connection.sendPacket(PositionRotation(mc.player.posX, mc.player.posY - 0.0624, mc.player.posZ, mc.player.rotationYaw, mc.player.rotationPitch, false) as Packet<*>)
                    mc.player.connection.sendPacket(PositionRotation(mc.player.posX, mc.player.posY - force, mc.player.posZ, mc.player.rotationYaw, mc.player.rotationPitch, true) as Packet<*>)
                    mc.player.connection.sendPacket(CPacketPlayerDigging(CPacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, mc.player.horizontalFacing) as Packet<*>)
                    mc.player.connection.sendPacket(CPacketPlayerTryUseItem(EnumHand.OFF_HAND) as Packet<*>)
                    mc.player.stopActiveHand()*/
    @SubscribeEvent
    fun onPacketSend(event: PacketEvent.Send) {
        if (event.getPacket() is CPacketPlayerDigging) {
            val packet = event.getPacket() as CPacketPlayerDigging
            if (packet.action == CPacketPlayerDigging.Action.RELEASE_USE_ITEM) {
                val handStack = Helper.mc.player.getHeldItem(EnumHand.MAIN_HAND)
                if (!handStack.isEmpty && handStack.item is ItemBow && Bows.getValue() as Boolean) {
                    doSpoofs()
                    if (debug.getValue() as Boolean) MessageSendHelper.sendChatMessage("trying to spoof")
                }
            }
        } else if (event.getPacket() is CPacketPlayerTryUseItem) {
            val packet2 = event.getPacket() as CPacketPlayerTryUseItem
            if (packet2.hand == EnumHand.MAIN_HAND) {
                val handStack = Helper.mc.player.getHeldItem(EnumHand.MAIN_HAND)
                if (!handStack.isEmpty) {
                    if (handStack.item is ItemEgg && eggs.getValue() as Boolean) {
                        doSpoofs()
                    } else if (handStack.item is ItemEnderPearl && pearls.getValue() as Boolean) {
                        doSpoofs()
                    } else if (handStack.item is ItemSnowball && snowballs.getValue() as Boolean) {
                        doSpoofs()
                    }
                }
            }
        }
    }


    fun doSpoofs() {
        if (System.currentTimeMillis() - lastShootTime >= Timeout.getValue()) {
            shooting = true
            lastShootTime = System.currentTimeMillis()
            Helper.mc.player.connection.sendPacket(CPacketEntityAction(Helper.mc.player, CPacketEntityAction.Action.START_SPRINTING))
            for (index in 0 until spoofs.getValue() as Int) {
                if (bypass.getValue() as Boolean) {
                    Helper.mc.player.connection.sendPacket(CPacketPlayer.Position(Helper.mc.player.posX, Helper.mc.player.posY + 1e-10, Helper.mc.player.posZ, false))
                    Helper.mc.player.connection.sendPacket(CPacketPlayer.Position(Helper.mc.player.posX, Helper.mc.player.posY - 1e-10, Helper.mc.player.posZ, true))
                } else {
                    Helper.mc.player.connection.sendPacket(CPacketPlayer.Position(Helper.mc.player.posX, Helper.mc.player.posY - 1e-10, Helper.mc.player.posZ, true))
                    Helper.mc.player.connection.sendPacket(CPacketPlayer.Position(Helper.mc.player.posX, Helper.mc.player.posY + 1e-10, Helper.mc.player.posZ, false))
                }
            }
            if (debug.getValue() as Boolean) MessageSendHelper.sendChatMessage("Spoofed")
            shooting = false
        }
    }
}



