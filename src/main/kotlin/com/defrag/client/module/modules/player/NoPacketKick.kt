package com.defrag.client.module.modules.player

import com.defrag.client.mixin.client.network.MixinNetworkManager
import com.defrag.client.module.Category
import com.defrag.client.module.Module
import com.defrag.client.util.text.MessageSendHelper.sendWarningMessage

/**
 * @see MixinNetworkManager
 */
object NoPacketKick : Module(
    name = "NoPacketKick",
    category = Category.PLAYER,
    description = "Suppress network exceptions and prevent getting kicked",
    showOnArray = false,
    enabledByDefault = true
) {
    @JvmStatic
    fun sendWarning(throwable: Throwable) {
        sendWarningMessage("$chatName Caught exception - \"$throwable\" check log for more info.")
        throwable.printStackTrace()
    }
}