package com.lambda.client.module.modules.combat

import com.lambda.client.event.SafeClientEvent
import com.lambda.client.manager.managers.HotbarManager.serverSideItem
import com.lambda.client.module.Category
import com.lambda.client.module.Module
import com.lambda.client.module.modules.player.FastUse
import com.lambda.client.util.text.MessageSendHelper
import com.lambda.client.util.threads.safeListener
import net.minecraft.init.Items
import net.minecraft.init.Items.BOW
import net.minecraft.network.Packet
import net.minecraft.network.play.client.CPacketPlayer.PositionRotation
import net.minecraft.network.play.client.CPacketPlayerDigging
import net.minecraft.network.play.client.CPacketPlayerTryUseItem
import net.minecraft.util.EnumHand
import net.minecraft.util.math.BlockPos
import net.minecraftforge.fml.common.gameevent.TickEvent


object FastUse : Module(
    name = "BowBomb",
    category = Category.COMBAT,
    description = "owo"
) {
    init {
        safeListener<TickEvent.ClientTickEvent> {
            connection.sendPacket(CPacketPlayerDigging(CPacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, player.horizontalFacing))
            connection.sendPacket(CPacketPlayerTryUseItem(player.activeHand))
            player.stopActiveHand()
        }
    }
    fun SafeClientEvent.FastUse() {
        if (player.serverSideItem.item == BOW) {
            MessageSendHelper.sendChatMessage("Test")
            mc.player.connection.sendPacket(CPacketPlayerDigging(CPacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, mc.player.getHorizontalFacing()) as Packet<*>)
            mc.player.connection.sendPacket(PositionRotation(mc.player.posX, mc.player.posY - 0.0624, mc.player.posZ, mc.player.rotationYaw, mc.player.rotationPitch, false) as Packet<*>)
            mc.player.connection.sendPacket(PositionRotation(mc.player.posX, mc.player.posY - 999.0, mc.player.posZ, mc.player.rotationYaw, mc.player.rotationPitch, true) as Packet<*>)
            mc.player.connection.sendPacket(CPacketPlayerTryUseItem(EnumHand.OFF_HAND) as Packet<*>)
            mc.player.stopActiveHand()
        }
    }
}
