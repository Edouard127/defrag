package com.defrag.client.module.modules.misc

import com.defrag.client.manager.managers.FriendManager
import com.defrag.client.module.Category
import com.defrag.client.module.Module
import com.defrag.client.util.TickTimer
import com.defrag.client.util.text.MessageSendHelper
import com.defrag.client.util.threads.defaultScope
import com.defrag.client.util.threads.safeListener
import kotlinx.coroutines.launch
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.util.math.RayTraceResult
import net.minecraftforge.fml.common.gameevent.InputEvent
import org.lwjgl.input.Mouse

object MidClickFriends : Module(
    name = "MidClickFriends",
    category = Category.MISC,
    description = "Middle click players to friend or unfriend them",
    showOnArray = false
) {
    private val timer = TickTimer()
    private var lastPlayer: EntityOtherPlayerMP? = null

    init {
        safeListener<InputEvent.MouseInputEvent> {
            mc.objectMouseOver?.let {
                // 0 is left, 1 is right, 2 is middle
                if (Mouse.getEventButton() != 2 || it.typeOfHit != RayTraceResult.Type.ENTITY) return@safeListener
                val player = it.entityHit as? EntityOtherPlayerMP ?: return@safeListener
                if (timer.tick(5000L) || player != lastPlayer && timer.tick(500L)) {
                    if (FriendManager.isFriend(player.name)) remove(player.name)
                    else add(player.name)
                    lastPlayer = player
                }
            }
        }
    }

    private fun remove(name: String) {
        if (FriendManager.removeFriend(name)) {
            MessageSendHelper.sendChatMessage("&b$name&r has been unfriended.")
        }
    }

    private fun add(name: String) {
        defaultScope.launch {
            if (FriendManager.addFriend(name)) MessageSendHelper.sendChatMessage("&b$name&r has been friended.")
            else MessageSendHelper.sendChatMessage("Failed to find UUID of $name")
        }
    }
}