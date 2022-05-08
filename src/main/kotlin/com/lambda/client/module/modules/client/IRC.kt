package com.lambda.client.module.modules.client

import com.lambda.client.module.Category
import com.lambda.client.module.Module
import com.lambda.client.util.IRCUtils
import com.lambda.client.util.text.MessageSendHelper
import com.lambda.client.util.text.MessageSendHelper.sendChatMessage
import com.lambda.event.listener.listener
import net.minecraft.client.Minecraft
import org.apache.commons.codec.digest.DigestUtils
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.lang.Thread.sleep
import java.net.Socket
import java.util.*
import javax.net.ssl.SSLSocket


object IRC : Module(
    name = "IRC Chat",
    description = "Talk with strangers",
    category = Category.CLIENT
) {

    lateinit var buffer: BufferedWriter
    val channel by setting("Channel", "#owo")
    val server by setting("Server", "irc.anthrochat.net")

    val random = (64..128).shuffled().last().toString()
    val port = 6667
    val nickname = Minecraft.getMinecraft().session.username.toString()

    val message = "joined the game"
    val socket = Socket(server, port)
    val outputStreamWriter = OutputStreamWriter(socket.getOutputStream())
    val bwriter = BufferedWriter(outputStreamWriter)
    init {

        onEnable {
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
                            sendRawChatMessage(line1!!.replace("[\u0002\u001f\u0016\u000f]", ""))
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
            sendString(bwriter, "left the game")
            sendChatMessage("Leaving the IRC...")
            bwriter.close()
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
    fun sendChatMessage(str: String) {
        if(Minecraft.getMinecraft().currentServerData != null && !str.startsWith("PING")){

            return MessageSendHelper.sendRawChatMessage("<IRC>: $str")
        }
        return println("IRC: $str")
    }
    fun sendRawChatMessage(message: String?) {
        if (message == null) return
        if (message.startsWith("PING")) return
        if (Minecraft.getMinecraft().currentServerData != null) return

        mc.player?.sendMessage(MessageSendHelper.ChatMessage("<IRC> $message"))
    }
}



