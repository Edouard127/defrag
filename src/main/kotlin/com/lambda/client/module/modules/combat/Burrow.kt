package com.lambda.client.module.modules.combat


import com.lambda.client.LambdaMod.Companion.LOG
import com.lambda.client.event.events.PacketEvent
import com.lambda.client.manager.managers.HotbarManager.resetHotbar
import com.lambda.client.manager.managers.PlayerPacketManager.sendPlayerPacket
import com.lambda.client.module.Category
import com.lambda.client.module.Module
import com.lambda.client.util.MovementUtils.centerPlayer
import com.lambda.client.util.items.item
import com.lambda.client.util.items.swapToBlock
import com.lambda.client.util.text.MessageSendHelper
import com.lambda.client.util.threads.safeListener
import net.minecraft.init.Blocks
import net.minecraft.network.play.client.CPacketConfirmTeleport
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraft.network.play.client.CPacketPlayer.PositionRotation
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock
import net.minecraft.network.play.server.SPacketPlayerPosLook
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent

object Burrow : Module(
    name = "SelfBlock",
    description = "Tell me if u get it working somehow kek",
    category = Category.COMBAT,
    modulePriority = 220
) {
    val height by setting("Height", 10, 1..100, 1)
    val center by setting("Center", true)
    val eChest by setting("EnderChest", false)

    lateinit var toPlace: BlockPos
    var finished = false

    init {
        safeListener<ClientTickEvent> {

            if (finished) {
                return@safeListener
            }

            swapToBlock(Blocks.OBSIDIAN)
            if (player.heldItemMainhand.item != Blocks.OBSIDIAN.item) {
                MessageSendHelper.sendWarningMessage("You need to have some obsidian in hotbar for this to work!")
                disable()
                return@safeListener
            }
            if (center && !player.centerPlayer()) {
                return@safeListener
            }

            LOG.debug("2")
            toPlace = player.position
            player.connection.sendPacket(CPacketPlayer.Position(mc.player.posX, mc.player.posY + 2, mc.player.posZ, true))
            player.connection.sendPacket(CPacketPlayer.Position(mc.player.posX, mc.player.posY + 2, mc.player.posZ, true))
            player.connection.sendPacket(CPacketPlayer.Position(mc.player.posX, mc.player.posY + 2, mc.player.posZ, true))
            player.connection.sendPacket(CPacketPlayer.Position(mc.player.posX, mc.player.posY + 2, mc.player.posZ, true))
            player.setPosition(mc.player.posX, mc.player.posY + 2, mc.player.posZ)



            sendPlayerPacket {
                CPacketPlayer.Rotation(mc.player.rotationYaw, 0f, true)
            }
            clicBlok(toPlace)

            mc.player.setPosition(mc.player.posX, toPlace.y.toDouble(), mc.player.posZ)
            mc.player.connection.sendPacket(CPacketPlayer.Position(mc.player.posX, mc.player.posY + height, mc.player.posZ, false))

            resetHotbar()
            finished = true
        }

        safeListener<PacketEvent.Receive> {
            if (mc.player == null) {
                disable()
                return@safeListener
            }
            if (it.packet is SPacketPlayerPosLook) {
                it.cancel()
                val packet = it.packet
                LOG.debug("1")
                mc.player.motionY = 0.0
                mc.player.connection.sendPacket(CPacketConfirmTeleport(packet.teleportId))
                mc.player.connection.sendPacket(PositionRotation(mc.player.posX, packet.y, mc.player.posZ, packet.yaw, packet.pitch, false))
                disable()
            }
        }

        onEnable {
            finished = false
        }
    }

    private fun clicBlok(pos: BlockPos) {
        val hitVec = Vec3d(pos).add(0.5, 0.5, 0.5).add(Vec3d(EnumFacing.UP.directionVec).scale(0.5))

        mc.playerController.processRightClickBlock(mc.player, mc.world, pos, EnumFacing.UP, hitVec, EnumHand.MAIN_HAND)
    }
}

