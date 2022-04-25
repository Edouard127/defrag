package com.lambda.client.module.modules.client

import com.lambda.client.module.Category
import com.lambda.client.module.Module
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
    name = "IRC Chat",
    description = "Talk with strangers",
    category = Category.CLIENT
) {
    lateinit var buffer: BufferedWriter
    val channel = "#owo"
    val random = (64..128).shuffled().last().toString()
    val server = "irc.anthrochat.net"
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
                                    if(Minecraft.getMinecraft().currentServerData != null){
                                        MessageSendHelper.sendChatMessage(line!!)
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
                            if(Minecraft.getMinecraft().currentServerData != null){
                                MessageSendHelper.sendRawChatMessage(line1!!)
                            }
                            println(line1!!)
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
            MessageSendHelper.sendChatMessage("Leaving the IRC...")
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
}



