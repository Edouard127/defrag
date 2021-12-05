package com.lambda.client.util
import com.lambda.client.module.modules.EventStage
import net.minecraft.network.Packet
import net.minecraftforge.fml.common.eventhandler.Cancelable

open class PacketEvent(stage: Int, internal val packet: Packet<*>) : EventStage(stage) {
    fun getPacket(): Packet<*> {
        return packet
    }

    @Cancelable
    class Send(stage: Int, packet: Packet<*>) : PacketEvent(stage, packet)

    @Cancelable
    class Receive(stage: Int, packet: Packet<*>) : PacketEvent(stage, packet) {
        fun cancel() {
            return cancel()
        }
    }
}
