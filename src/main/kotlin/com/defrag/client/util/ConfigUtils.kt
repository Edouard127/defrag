package com.defrag.client.util

import com.defrag.client.LambdaMod
import com.defrag.client.manager.managers.FriendManager
import com.defrag.client.manager.managers.MacroManager
import com.defrag.client.manager.managers.UUIDManager
import com.defrag.client.manager.managers.WaypointManager
import com.defrag.client.setting.ConfigManager
import com.defrag.client.setting.GenericConfig
import com.defrag.client.setting.ModuleConfig
import com.defrag.client.setting.configs.IConfig
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.nio.file.Files

object ConfigUtils {

    fun loadAll(): Boolean {
        var success = ConfigManager.loadAll()
        success = MacroManager.loadMacros() && success // Macro
        success = WaypointManager.loadWaypoints() && success // Waypoint
        success = FriendManager.loadFriends() && success // Friends
        success = UUIDManager.load() && success // UUID Cache

        return success
    }

    fun saveAll(): Boolean {
        var success = ConfigManager.saveAll()
        success = MacroManager.saveMacros() && success // Macro
        success = WaypointManager.saveWaypoints() && success // Waypoint
        success = FriendManager.saveFriends() && success // Friends
        success = UUIDManager.save() && success // UUID Cache

        return success
    }

    fun isPathValid(path: String): Boolean {
        return try {
            File(path).canonicalPath
            true
        } catch (e: Throwable) {
            false
        }
    }

    fun fixEmptyJson(file: File, isArray: Boolean = false) {
        var empty = false

        if (!file.exists()) {
            file.createNewFile()
            empty = true
        } else if (file.length() <= 8) {
            val string = file.readText()
            empty = string.isBlank() || string.all {
                it == '[' || it == ']' || it == '{' || it == '}' || it == ' ' || it == '\n' || it == '\r'
            }
        }

        if (empty) {
            try {
                FileWriter(file, false).use {
                    it.write(if (isArray) "[]" else "{}")
                }
            } catch (exception: IOException) {
                LambdaMod.LOG.warn("Failed fixing empty json", exception)
            }
        }
    }

    // TODO: Introduce a version helper for LambdaMod.BUILD_NUMBER for version-specific configs. This should be theoritically fine for now
    fun moveAllLegacyConfigs() {
        moveLegacyConfig("lambda/generic.json", "lambda/generic.bak", GenericConfig)
        moveLegacyConfig("lambda/modules/default.json", "lambda/modules/default.bak", ModuleConfig)
        moveLegacyConfig("KAMIBlueCoords.json", "lambda/waypoints.json")
        moveLegacyConfig("KAMIBlueWaypoints.json", "lambda/waypoints.json")
        moveLegacyConfig("KAMIBlueMacros.json", "lambda/macros.json")
        moveLegacyConfig("KAMIBlueFriends.json", "lambda/friends.json")
        moveLegacyConfig("chat_filter.json", "lambda/chat_filter.json")
    }

    private fun moveLegacyConfig(oldConfigIn: String, oldConfigBakIn: String, newConfig: IConfig) {
        if (newConfig.file.exists() || newConfig.backup.exists()) return

        val oldConfig = File(oldConfigIn)
        val oldConfigBak = File(oldConfigBakIn)

        if (!oldConfig.exists() && !oldConfigBak.exists()) return

        try {
            newConfig.file.parentFile.mkdirs()
            Files.move(oldConfig.absoluteFile.toPath(), newConfig.file.absoluteFile.toPath())
        } catch (e: Exception) {
            LambdaMod.LOG.warn("Error moving legacy config", e)
        }

        try {
            newConfig.backup.parentFile.mkdirs()
            Files.move(oldConfigBak.absoluteFile.toPath(), newConfig.backup.absoluteFile.toPath())
        } catch (e: Exception) {
            LambdaMod.LOG.warn("Error moving legacy config", e)
        }
    }

    private fun moveLegacyConfig(oldConfigIn: String, newConfigIn: String) {
        val newConfig = File(newConfigIn)
        if (newConfig.exists()) return

        val oldConfig = File(oldConfigIn)
        if (!oldConfig.exists()) return

        try {
            newConfig.parentFile.mkdirs()
            Files.move(oldConfig.absoluteFile.toPath(), newConfig.absoluteFile.toPath())
        } catch (e: Exception) {
            LambdaMod.LOG.warn("Error moving legacy config", e)
        }
    }

}