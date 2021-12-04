package com.defrag.client.module.modules.misc

import com.defrag.client.event.events.ConnectionEvent
import com.defrag.client.manager.managers.WaypointManager
import com.defrag.client.module.Category
import com.defrag.client.module.Module
import com.defrag.client.util.EntityUtils.flooredPosition
import com.defrag.client.util.EntityUtils.isFakeOrSelf
import com.defrag.client.util.TickTimer
import com.defrag.client.util.TimeUnit
import com.defrag.client.util.math.CoordinateConverter.asString
import com.defrag.client.util.text.MessageSendHelper
import com.defrag.client.util.threads.onMainThread
import com.defrag.client.util.threads.safeListener
import com.defrag.event.listener.asyncListener
import com.mojang.authlib.GameProfile
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.util.math.BlockPos
import net.minecraftforge.fml.common.gameevent.TickEvent

object LogoutLogger : Module(
    name = "LogoutLogger",
    category = Category.MISC,
    description = "Logs when a player leaves the game"
) {
    private val saveWaypoint by setting("Save Waypoint", true)
    private val print by setting("Print To Chat", true)

    private val loggedPlayers = LinkedHashMap<GameProfile, BlockPos>()
    private val timer = TickTimer(TimeUnit.SECONDS)

    init {
        asyncListener<ConnectionEvent.Disconnect> {
            onMainThread {
                loggedPlayers.clear()
            }
        }

        safeListener<TickEvent.ClientTickEvent> {
            if (it.phase != TickEvent.Phase.END) return@safeListener

            for (loadedPlayer in world.playerEntities) {
                if (loadedPlayer !is EntityOtherPlayerMP) continue
                if (loadedPlayer.isFakeOrSelf) continue

                val info = connection.getPlayerInfo(loadedPlayer.gameProfile.id) ?: continue
                loggedPlayers[info.gameProfile] = loadedPlayer.flooredPosition
            }

            if (timer.tick(1L)) {
                val toRemove = ArrayList<GameProfile>()

                loggedPlayers.entries.removeIf { (profile, pos) ->
                    @Suppress("SENSELESS_COMPARISON")
                    if (connection.getPlayerInfo(profile.id) == null) {
                        if (saveWaypoint) WaypointManager.add(pos, "${profile.name} Logout Spot")
                        if (print) MessageSendHelper.sendChatMessage("${profile.name} logged out at ${pos.asString()}")
                        true
                    } else {
                        false
                    }
                }

                loggedPlayers.keys.removeAll(toRemove)
            }
        }
    }
}