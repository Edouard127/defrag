package com.defrag.client.util
import com.defrag.client.module.modules.EventStage
import net.minecraft.network.Packet
import net.minecraftforge.fml.common.eventhandler.Cancelable

open class PacketEvent(stage: Int, private val packet: Packet<*>) : EventStage(stage) {

    @Cancelable
    class Send(stage: Int, packet: Packet<*>) : PacketEvent(stage, packet)

    @Cancelable
    class Receive(stage: Int, packet: Packet<*>) : PacketEvent(stage, packet)



    override fun getStage(): Int {
        return stage
    }
    open fun getPacket(): Packet<*> {
        return packet
    }
}
