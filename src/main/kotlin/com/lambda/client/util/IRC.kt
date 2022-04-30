package com.lambda.client.util

import com.lambda.client.event.Event
import com.lambda.client.module.modules.client.IRC
import com.lambda.client.util.text.MessageSendHelper
import net.minecraft.client.Minecraft
import org.apache.commons.codec.digest.DigestUtils
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.lang.Thread.sleep
import java.net.Socket

class IRCUtils(url: String, port: Int) {
    val random = (8..512).shuffled().last().toString()
    var socket: Socket = Socket(url, port)
    var inputBuffer: BufferedReader = BufferedReader(InputStreamReader(socket.getInputStream()));
    var outputBuffer: BufferedWriter? = null

    fun connect(url: String, port: Int, channel: String, username: String, password: String = DigestUtils.sha256(username + random).toString()) {
        val outputStreamWriter = OutputStreamWriter(socket.getOutputStream())
        outputBuffer = BufferedWriter(outputStreamWriter)
        auth(channel, username, password)
    }
    private fun auth(channel: String, username: String, password: String){
        sleep(3000)
        outputBuffer?.let { sendString(it, "NICK $username\r\n") }
        outputBuffer?.let { sendString(it, "USER $username * * :$password\r\n") }
        var line: String?

            while (inputBuffer.readLine().also { line = it } != null) {
                sendChatMessage("Connecting...")
                if(line?.startsWith("PING") == true){
                    outputBuffer?.let { sendString(it, line!!.replace("PING", "PONG")) }
                }
                var firstSpace = line?.indexOf(" ")
                var secondSpace = firstSpace?.let { it1 -> line?.indexOf(" ", it1) }
                if (secondSpace != null) {
                    if (secondSpace >= 0) {
                        if (line?.indexOf("004")!! >= 0) {
                            // We are now logged in.
                            break
                        }
                        line = inputBuffer.readLine()
                    }
                }
            }
        on()
            }



    }
    fun on() {
        Thread {
            while (true) {
                println("jfuyirghfriueghtirk")
                if (IRCUtils(IRC.server, IRC.port).inputBuffer.readLine().startsWith("PING")) {
                    IRCUtils(IRC.server, IRC.port).outputBuffer?.let { it1 -> IRC.sendString(it1, IRCUtils(IRC.server, IRC.port).inputBuffer.readLine().replace("PING", "PONG")) }
                }
                sendChatMessage(IRCUtils(IRC.server, IRC.port).inputBuffer.readLine().replace("[\u0002\u001f\u0016\u000f]", ""))

            }
        }.start()

    }


    fun sendString(bw: BufferedWriter, str: String) {
        try {
            bw.write("""$str""".trimIndent())
            bw.flush()
        } catch (e: Exception) {
            println("Exception: $e")
        }
    }
    fun sendChatMessage(str: String) {
        if(Minecraft.getMinecraft().currentServerData != null){
            return MessageSendHelper.sendRawChatMessage("<IRC>: $str")
        }
        return println("<IRC>: $str")
    }
