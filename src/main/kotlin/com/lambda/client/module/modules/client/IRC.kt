package com.lambda.client.module.modules.client

import com.lambda.client.module.Category
import com.lambda.client.module.Module
import com.lambda.client.util.Initialize
import com.lambda.client.util.Responder
import com.lambda.client.util.Wrapper.player
import com.lambda.client.util.Wrapper.world
import com.lambda.client.util.text.MessageSendHelper
import net.minecraft.client.Minecraft
import net.minecraft.init.SoundEvents
import net.minecraft.util.SoundCategory
import org.apache.commons.codec.digest.DigestUtils
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.lang.Thread.sleep
import java.net.Socket


object IRC : Module(
    name = "IRC Chat",
    description = "Talk with strangers",
    category = Category.CLIENT
) {
    lateinit var buffer: BufferedWriter
    val nickname = Minecraft.getMinecraft().session.username.toString()
    val channel by setting("Channel", "#owo")
    public val server by setting("Server addess", "irc.anthrochat.net")
    val onJoinMessage by setting("Join message", "joined the game", description = "$nickname (custom on join message)")
    val port = 6667

    lateinit var outputStreamWriter: OutputStreamWriter
    lateinit var bwriter: BufferedWriter
    private val arrayOfWhiteList = listOf("JOIN", "PRIVMSG")
    init {

        onEnable {
            val socket = Socket(server, port)
            outputStreamWriter = OutputStreamWriter(socket.getOutputStream())
            bwriter = BufferedWriter(outputStreamWriter)

            Thread {

                try {
                    sleep(4000)
                    sendString(bwriter, "NICK $nickname\r\n")
                    sendString(bwriter, "USER $nickname * * :${DigestUtils.sha256(nickname)}\r\n")

                    val inputStreamReader = InputStreamReader(socket.getInputStream());
                    var breader = BufferedReader(inputStreamReader);
                    var line: String?
                    while (breader.readLine().also { line = it } != null) {
                        println(">>> $line");
                        if (line?.startsWith("PING") == true) {
                            sendString(bwriter, line!!.replace("PING", "PONG"))
                        }
                        val firstSpace = line?.indexOf(" ")
                        val secondSpace = firstSpace?.let { it1 -> line?.indexOf(" ", it1) }
                        if (secondSpace != null) {
                            if (secondSpace >= 0) {
                                if (line?.indexOf("004")!! >= 0) {
                                    break;
                                }
                                if (line!!.indexOf("433") >= 0) {
                                    println("Nickname is already in use.")
                                }
                                if (line?.startsWith("PING") == true) {
                                    sendString(bwriter, line!!.replace("PING", "PONG"))
                                }
                                line = breader.readLine()
                                if(Minecraft.getMinecraft().currentServerData != null && line!!.containsWhitelist(arrayOfWhiteList)){
                                    val string = line!!.split(":")
                                    val username = string[1].split("!")[0]
                                    var message = string[2]
                                    if(message.contains("@$nickname")) {
                                        message = message.replace("@$nickname", "§4@$nickname§7")
                                        world?.playSound(player!!.position, SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.AMBIENT, 70.0f, 18.0f, true)
                                    }
                                    MessageSendHelper.sendChatMessage("IRC <$username>: $message")
                                }
                            }
                        }
                    }
                    sendString(bwriter, "JOIN $channel\r\n")
                    sendString(bwriter, "PRIVMSG $channel :$onJoinMessage")
                    var line1: String?
                    while (true) {

                        line1 = breader.readLine()
                        if (line1.startsWith("PING")) {
                            sendString(bwriter, line1!!.replace("PING", "PONG"))
                        }
                        if(Minecraft.getMinecraft().currentServerData != null && line1.containsWhitelist(arrayOfWhiteList)){
                            val string = line1.split(":")
                            val username = string[1].split("!")[0]
                            var message = string[2]
                            if(message.contains("@$nickname")) {
                                message = message.replace("@$nickname", "§4@$nickname§7")
                                world?.playSound(player!!.position, SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.AMBIENT, 70.0f, 18.0f, true)
                            }
                            MessageSendHelper.sendChatMessage("IRC <$username>: $message")
                        }
                        /*if (line1[0] == '/') {
                            bwriter.write(line1.substring(1) + "\r\n")
                            bwriter.flush()
                        }*/
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }.start()
        }
        onDisable {
            try {
                //Initialize().destroy()
            } catch(e: Exception){
                println("Exception: $e")
            }
        }
    }
    fun sendString(bw: BufferedWriter, str: String) {
        try {
            bw.write("""
    $str
    
    """.trimIndent())
            bw.flush()
        } catch (e: Exception) {
            println("Exception: $e")
        }
    }
    fun String.containsWhitelist(keywords: List<String>): Boolean {
        for (keyword in keywords) {
            if (this.contains(keyword, true)) return true
        }
        return false
    }
}
