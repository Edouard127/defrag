package com.defrag.client.util

import net.minecraft.network.Packet
import net.minecraft.network.play.INetHandlerPlayServer
import kotlin.Throws
import java.io.IOException
import net.minecraft.network.PacketBuffer

class CPacketChangeSetting(module: String, setting: String, value: String) : Packet<INetHandlerPlayServer?> {
    var setting: String
    @Throws(IOException::class)
    override fun readPacketData(buf: PacketBuffer) {
        setting = buf.readString(256)
    }

    @Throws(IOException::class)
    override fun writePacketData(buf: PacketBuffer) {
        buf.writeString(setting)
    }

    override fun processPacket(handler: INetHandlerPlayServer?) {

    }

    init {
        this.setting = "$setting-$module-$value"
    }
}