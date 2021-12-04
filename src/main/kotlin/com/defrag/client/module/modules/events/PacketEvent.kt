package com.defrag.client.module.modules.events

import com.defrag.client.module.modules.EventStage
import net.minecraft.network.Packet
import net.minecraftforge.fml.common.eventhandler.Cancelable

open class PacketEvent(stage: Int, val packet: Packet<*>) : EventStage(stage) {
    @JvmName("getPacket1") fun <T : Packet<*>?> getPacket(): Packet<*> {
        return packet
    }

    @Cancelable
    class Send(stage: Int, packet: Packet<*>) : PacketEvent(stage, packet)

    @Cancelable
    class Receive(stage: Int, packet: Packet<*>) : PacketEvent(stage, packet)
}