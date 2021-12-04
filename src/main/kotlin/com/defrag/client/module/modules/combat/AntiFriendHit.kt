package com.defrag.client.module.modules.combat

import com.defrag.client.event.events.PacketEvent
import com.defrag.client.manager.managers.FriendManager
import com.defrag.client.module.Category
import com.defrag.client.module.Module
import com.defrag.client.util.threads.safeListener
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.network.play.client.CPacketUseEntity

object AntiFriendHit : Module(
    name = "AntiFriendHit",
    description = "Don't hit your friends",
    category = Category.COMBAT
) {
    init {
        safeListener<PacketEvent.Send> {
            if (it.packet !is CPacketUseEntity || it.packet.action != CPacketUseEntity.Action.ATTACK) return@safeListener
            val entity = it.packet.getEntityFromWorld(world)
            if (entity is EntityPlayer && FriendManager.isFriend(entity.name)) {
                it.cancel()
            }
        }
    }
}