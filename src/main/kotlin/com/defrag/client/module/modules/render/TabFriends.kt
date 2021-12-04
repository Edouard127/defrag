package com.defrag.client.module.modules.render

import com.defrag.client.manager.managers.FriendManager
import com.defrag.client.module.Category
import com.defrag.client.module.Module
import com.defrag.client.util.color.EnumTextColor
import com.defrag.client.util.text.format
import net.minecraft.client.network.NetworkPlayerInfo
import net.minecraft.scoreboard.ScorePlayerTeam

object TabFriends : Module(
    name = "TabFriends",
    description = "Highlights friends in the tab menu",
    category = Category.RENDER,
    showOnArray = false
) {
    private val color by setting("Color", EnumTextColor.GREEN)

    @JvmStatic
    fun getPlayerName(info: NetworkPlayerInfo): String {
        val name = info.displayName?.formattedText
            ?: ScorePlayerTeam.formatPlayerName(info.playerTeam, info.gameProfile.name)

        return if (FriendManager.isFriend(name)) {
            color format name
        } else {
            name
        }
    }
}