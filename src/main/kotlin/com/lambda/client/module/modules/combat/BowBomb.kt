package com.lambda.client.module.modules.combat

import com.lambda.client.manager.managers.HotbarManager.serverSideItem
import com.lambda.client.module.Category
import com.lambda.client.module.Module
import com.lambda.client.util.text.MessageSendHelper
import com.lambda.client.util.threads.safeListener
import net.minecraft.init.Items.BOW
import net.minecraft.item.ItemBow
import net.minecraft.network.Packet
import net.minecraft.network.play.client.CPacketPlayer.PositionRotation
import net.minecraft.network.play.client.CPacketPlayerDigging
import net.minecraft.network.play.client.CPacketPlayerTryUseItem
import net.minecraft.util.EnumHand
import net.minecraft.util.math.BlockPos
import net.minecraftforge.fml.common.gameevent.InputEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import java.lang.Thread.sleep


object FastUse : Module(
    name = "BowBomb",
    category = Category.COMBAT,
    description = "owo"
) {
    private val force by setting("Force", 150, 150..999, 0)
    private var ticks: Int = 0

    init {
        safeListener<TickEvent.ClientTickEvent>(2000) {
            if (it.phase == TickEvent.Phase.START) {
                    if(mc.player.inventory.getCurrentItem().item == BOW && mc.player.itemInUseMaxCount >= 20){
                        MessageSendHelper.sendChatMessage("Test")
                        mc.player.connection.sendPacket(PositionRotation(mc.player.posX, mc.player.posY - 0.0624, mc.player.posZ, mc.player.rotationYaw, mc.player.rotationPitch, false) as Packet<*>)
                        mc.player.connection.sendPacket(PositionRotation(mc.player.posX, mc.player.posY - force, mc.player.posZ, mc.player.rotationYaw, mc.player.rotationPitch, true) as Packet<*>)
                        mc.player.connection.sendPacket(CPacketPlayerDigging(CPacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, mc.player.horizontalFacing) as Packet<*>)
                        mc.player.connection.sendPacket(CPacketPlayerTryUseItem(EnumHand.OFF_HAND) as Packet<*>)
                        mc.player.stopActiveHand()
                    }

                }
            }

            }



}
