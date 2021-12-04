package com.defrag.client.event.events

import com.defrag.client.event.Cancellable
import com.defrag.client.event.Event
import com.defrag.client.event.ICancellable
import net.minecraft.network.Packet

abstract class PacketEvent(val packet: Packet<*>) : Event, ICancellable by Cancellable() {
    class Receive(packet: Packet<*>) : PacketEvent(packet)
    class PostReceive(packet: Packet<*>) : PacketEvent(packet)
    class Send(packet: Packet<*>) : PacketEvent(packet)
    class PostSend(packet: Packet<*>) : PacketEvent(packet)
}