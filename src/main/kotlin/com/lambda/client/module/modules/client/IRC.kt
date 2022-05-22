package com.lambda.client.module.modules.client

import com.lambda.client.module.Category
import com.lambda.client.module.Module
import com.lambda.client.module.modules.client.IRC.containsWhitelist
import com.lambda.client.util.text.MessageSendHelper
import net.minecraft.client.Minecraft
import org.apache.commons.codec.digest.DigestUtils
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.lang.Thread.sleep
import java.net.Socket
import java.util.*


object IRC : Module(
    name = "IRC Chata",
    description = "Talk with strangers",
    category = Category.CLIENT
) {
    lateinit var buffer: BufferedWriter
    val channel by setting("Channel", "#owo")
    val random = (16..65535).shuffled().last().toString()
    val server by setting("Server addess", "irc.anthrochat.net")
    val port = 6667
    val nickname = Minecraft.getMinecraft().session.username.toString()

    val message = "joined the game"
    lateinit var socket: Socket
    lateinit var outputStreamWriter: OutputStreamWriter
    lateinit var bwriter: BufferedWriter
    val arrayOfWhiteList = listOf("JOIN", "PRIVMSG")
    init {

        onEnable {
            socket = Socket(server, port)
            outputStreamWriter = OutputStreamWriter(socket.getOutputStream())
            bwriter = BufferedWriter(outputStreamWriter)

            val UwU = Thread {

                try {
                    sleep(4000)
                    sendString(bwriter, "NICK ${nickname+random}\r\n")
                    sendString(bwriter, "USER ${nickname+random} * * :${DigestUtils.sha256(nickname+random)}\r\n")

                    val inputStreamReader = InputStreamReader(socket.getInputStream());
                    var breader = BufferedReader(inputStreamReader);
                    var line: String? = null
                    while (breader.readLine().also { line = it } != null) {
                        println(">>> $line");
                        if (line?.startsWith("PING") == true) {
                            sendString(bwriter, line!!.replace("PING", "PONG"))
                        }
                        var firstSpace = line?.indexOf(" ")
                        var secondSpace = firstSpace?.let { it1 -> line?.indexOf(" ", it1) }
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
                                    val message = string[2]
                                    MessageSendHelper.sendChatMessage("IRC <$username>: $message")
                                }
                            }
                        }
                    }
                    sendString(bwriter, "JOIN ${channel}\r\n")
                    sendString(bwriter, "PRIVMSG $channel :$message")
                    var line1: String?
                    while (true) {

                        line1 = breader.readLine()
                        if (line1?.startsWith("PING") == true) {
                            sendString(bwriter, line1!!.replace("PING", "PONG"))
                        }
                        if(Minecraft.getMinecraft().currentServerData != null && line1.containsWhitelist(arrayOfWhiteList)){
                            val string = line1.split(":")
                            val username = string[1].split("!")[0]
                            val message = string[2]
                            MessageSendHelper.sendChatMessage("IRC <$username>: $message")
                        }
                        if (line1.get(0) == '/') {
                            bwriter.write(line1.substring(1) + "\r\n")
                            bwriter.flush()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            UwU.start()
        }
        onDisable {
            try {
                sendString(bwriter, "left the game")
                MessageSendHelper.sendChatMessage("Leaving the IRC...")
                bwriter.close()
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
