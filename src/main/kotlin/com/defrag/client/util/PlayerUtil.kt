package com.defrag.client.util

import com.google.gson.JsonParser
import com.defrag.client.util.PlayerUtil.lookUpName
import java.lang.Thread
import java.io.BufferedInputStream
import com.defrag.client.util.PlayerUtil
import com.google.gson.JsonArray
import com.google.common.collect.Lists
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import kotlin.Throws
import net.minecraft.advancements.AdvancementManager
import java.lang.Runnable
import kotlin.jvm.Volatile
import net.minecraft.entity.player.EntityPlayer
import java.io.DataOutputStream
import java.io.InputStream
import java.lang.Exception
import java.lang.StringBuilder
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.*
import javax.net.ssl.HttpsURLConnection

object PlayerUtil {
    var timer = Timer()
    private val PARSER = JsonParser()
    fun getNameFromUUID(uuid: UUID): String? {
        return try {
            val process = lookUpName(uuid)
            val thread = Thread(process)
            thread.start()
            thread.join()
            process.name
        } catch (e: Exception) {
            null
        }
    }

    fun getNameFromUUID(uuid: String?): String? {
        return try {
            val process = lookUpName(uuid)
            val thread = Thread(process)
            thread.start()
            thread.join()
            process.name
        } catch (e: Exception) {
            null
        }
    }

    fun requestIDs(data: String): String? {
        return try {
            val query = "https://api.mojang.com/profiles/minecraft"
            val url = URL(query)
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 5000
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            conn.doOutput = true
            conn.doInput = true
            conn.requestMethod = "POST"
            val os = conn.outputStream
            os.write(data.toByteArray(StandardCharsets.UTF_8))
            os.close()
            val `in` = BufferedInputStream(conn.inputStream)
            val res = convertStreamToString(`in`)
            (`in` as InputStream).close()
            conn.disconnect()
            res
        } catch (e: Exception) {
            null
        }
    }

    fun convertStreamToString(`is`: InputStream?): String {
        val s = Scanner(`is`).useDelimiter("\\A")
        return if (s.hasNext()) s.next() else "/"
    }

    fun getIdNoHyphens(uuid: UUID): String {
        return uuid.toString().replace("-".toRegex(), "")
    }

    @Throws(Exception::class)
    private fun getResources(url: URL, request: String): JsonElement {
        return getResources(url, request, null)
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Throws(Exception::class)
    private fun getResources(url: URL, request: String, element: JsonElement?): JsonElement {
        var connection: HttpsURLConnection? = null
        return try {
            val data: JsonElement
            connection = url.openConnection() as HttpsURLConnection
            connection.doOutput = true
            connection!!.requestMethod = request
            connection.setRequestProperty("Content-Type", "application/json")
            if (element != null) {
                val output = DataOutputStream(connection.outputStream)
                output.writeBytes(AdvancementManager.GSON.toJson(element))
                output.close()
            }
            val scanner = Scanner(connection.inputStream)
            val builder = StringBuilder()
            while (scanner.hasNextLine()) {
                builder.append(scanner.nextLine())
                builder.append('\n')
            }
            scanner.close()
            val json = builder.toString()
            data = PARSER.parse(json)
            data
        } finally {
            connection?.disconnect()
        }
    }

    class lookUpName : Runnable {
        val name: String? = null
        private var uuid: String? = null
        private var uuidID: UUID? = null
        private val JSONValue: Any? = null

        constructor(input: String?) {
            this@lookUpName.uuid = input
            uuidID = UUID.fromString(input)
        }

        constructor(input: UUID) {
            uuidID = input
            uuid = input.toString()
        }

        override fun run() {
            this.lookUpName()
        }

        private fun lookUpName() {
            TODO("Not yet implemented")
        }

        constructor() {
            var player: EntityPlayer? = null
            if (Util.mc.world != null) {
                player = Util.mc.world.getPlayerEntityByUUID(uuidID)
            }
        }
    }
}