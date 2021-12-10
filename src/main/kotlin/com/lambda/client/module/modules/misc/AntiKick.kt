package com.lambda.client.module.modules.misc

import com.lambda.client.event.events.ConnectionEvent
import com.lambda.client.event.events.PacketEvent
import com.lambda.client.module.Category
import com.lambda.client.module.Module
import com.lambda.client.module.modules.combat.AntiBot
import com.lambda.event.listener.listener
import net.minecraft.network.Packet
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraft.network.play.server.SPacketDisconnect
import net.minecraftforge.fml.common.gameevent.TickEvent

object AntiKick : Module(
    name = "AntiKick",
    category = Category.MISC,
    description = "Cancel kick packets",
){
    val _try by setting("Tries", 20, 10..100, 0)
    var ok = 0
    init {
        listener<PacketEvent.Receive> {
            if(it.packet is SPacketDisconnect){
                listener<TickEvent.ClientTickEvent> {
                while(ok<_try) {
                    mc.player.connection.sendPacket(CPacketPlayer.PositionRotation(mc.player.posX, mc.player.posY + 1e-7, mc.player.posZ, mc.player.rotationYaw, mc.player.rotationPitch, false) as Packet<*>)
                    mc.player.connection.sendPacket(CPacketPlayer.PositionRotation(mc.player.posX, mc.player.posY - 1e-7, mc.player.posZ, mc.player.rotationYaw, mc.player.rotationPitch, true) as Packet<*>)
                    mc.player.connection.sendPacket(CPacketPlayer.PositionRotation(mc.player.posX, mc.player.posY + 1e-7, mc.player.posZ, mc.player.rotationYaw, mc.player.rotationPitch, false) as Packet<*>)
                    mc.player.connection.sendPacket(CPacketPlayer.PositionRotation(mc.player.posX, mc.player.posY - 1e-7, mc.player.posZ, mc.player.rotationYaw, mc.player.rotationPitch, true) as Packet<*>)
                    mc.player.connection.sendPacket(CPacketPlayer.PositionRotation(mc.player.posX, mc.player.posY + 1e-7, mc.player.posZ, mc.player.rotationYaw, mc.player.rotationPitch, false) as Packet<*>)
                    mc.player.connection.sendPacket(CPacketPlayer.PositionRotation(mc.player.posX, mc.player.posY - 1e-7, mc.player.posZ, mc.player.rotationYaw, mc.player.rotationPitch, true) as Packet<*>)
                    mc.player.connection.sendPacket(CPacketPlayer.PositionRotation(mc.player.posX, mc.player.posY + 1e-7, mc.player.posZ, mc.player.rotationYaw, mc.player.rotationPitch, false) as Packet<*>)
                    mc.player.connection.sendPacket(CPacketPlayer.PositionRotation(mc.player.posX, mc.player.posY - 1e-7, mc.player.posZ, mc.player.rotationYaw, mc.player.rotationPitch, true) as Packet<*>)
                    mc.player.connection.sendPacket(CPacketPlayer.PositionRotation(mc.player.posX, mc.player.posY + 1e-7, mc.player.posZ, mc.player.rotationYaw, mc.player.rotationPitch, false) as Packet<*>)
                    mc.player.connection.sendPacket(CPacketPlayer.PositionRotation(mc.player.posX, mc.player.posY - 1e-7, mc.player.posZ, mc.player.rotationYaw, mc.player.rotationPitch, true) as Packet<*>)
                    mc.player.connection.sendPacket(CPacketPlayer.PositionRotation(mc.player.posX, mc.player.posY + 1e-7, mc.player.posZ, mc.player.rotationYaw, mc.player.rotationPitch, false) as Packet<*>)
                    mc.player.connection.sendPacket(CPacketPlayer.PositionRotation(mc.player.posX, mc.player.posY - 1e-7, mc.player.posZ, mc.player.rotationYaw, mc.player.rotationPitch, true) as Packet<*>)
                    mc.player.connection.sendPacket(CPacketPlayer.PositionRotation(mc.player.posX, mc.player.posY + 1e-7, mc.player.posZ, mc.player.rotationYaw, mc.player.rotationPitch, false) as Packet<*>)
                    mc.player.connection.sendPacket(CPacketPlayer.PositionRotation(mc.player.posX, mc.player.posY - 1e-7, mc.player.posZ, mc.player.rotationYaw, mc.player.rotationPitch, true) as Packet<*>)
                    mc.player.connection.sendPacket(CPacketPlayer.PositionRotation(mc.player.posX, mc.player.posY + 1e-7, mc.player.posZ, mc.player.rotationYaw, mc.player.rotationPitch, false) as Packet<*>)
                    mc.player.connection.sendPacket(CPacketPlayer.PositionRotation(mc.player.posX, mc.player.posY - 1e-7, mc.player.posZ, mc.player.rotationYaw, mc.player.rotationPitch, true) as Packet<*>)
                    mc.player.connection.sendPacket(CPacketPlayer.PositionRotation(mc.player.posX, mc.player.posY + 1e-7, mc.player.posZ, mc.player.rotationYaw, mc.player.rotationPitch, false) as Packet<*>)
                    mc.player.connection.sendPacket(CPacketPlayer.PositionRotation(mc.player.posX, mc.player.posY - 1e-7, mc.player.posZ, mc.player.rotationYaw, mc.player.rotationPitch, true) as Packet<*>)
                    mc.player.connection.sendPacket(CPacketPlayer.PositionRotation(mc.player.posX, mc.player.posY + 1e-7, mc.player.posZ, mc.player.rotationYaw, mc.player.rotationPitch, false) as Packet<*>)
                    mc.player.connection.sendPacket(CPacketPlayer.PositionRotation(mc.player.posX, mc.player.posY - 1e-7, mc.player.posZ, mc.player.rotationYaw, mc.player.rotationPitch, true) as Packet<*>)
                    ok++
                    if(ok> _try){
                        ok = 0
                    }
                }
                }
            }
        }
    }
}