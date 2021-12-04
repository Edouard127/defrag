package com.defrag.client.manager.managers

import com.defrag.capeapi.AbstractUUIDManager
import com.defrag.capeapi.PlayerProfile
import com.defrag.capeapi.UUIDUtils
import com.defrag.client.LambdaMod
import com.defrag.client.manager.Manager
import com.defrag.client.util.Wrapper

object UUIDManager : AbstractUUIDManager(LambdaMod.DIRECTORY + "uuid_cache.json", LambdaMod.LOG, maxCacheSize = 1000), Manager {

    override fun getOrRequest(nameOrUUID: String): PlayerProfile? {
        return Wrapper.minecraft.connection?.playerInfoMap?.let { playerInfoMap ->
            val infoMap = ArrayList(playerInfoMap)
            val isUUID = UUIDUtils.isUUID(nameOrUUID)
            val withOutDashes = UUIDUtils.removeDashes(nameOrUUID)

            infoMap.find {
                isUUID && UUIDUtils.removeDashes(it.gameProfile.id.toString()).equals(withOutDashes, ignoreCase = true)
                    || !isUUID && it.gameProfile.name.equals(nameOrUUID, ignoreCase = true)
            }?.gameProfile?.let {
                PlayerProfile(it.id, it.name)
            }
        } ?: super.getOrRequest(nameOrUUID)
    }
}
