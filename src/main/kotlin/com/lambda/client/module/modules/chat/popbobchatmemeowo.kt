package com.lambda.client.module.modules.chat

import com.lambda.client.LambdaMod.Companion.DIRECTORY
import com.lambda.client.module.Category
import com.lambda.client.module.Module
import com.lambda.client.module.modules.player.Timer
import com.lambda.client.util.TickTimer
import com.lambda.client.util.text.MessageSendHelper
import com.lambda.client.util.text.MessageSendHelper.sendServerMessage
import com.lambda.client.util.threads.safeListener
import net.minecraftforge.fml.common.gameevent.TickEvent
import java.io.File
import java.io.InputStream

object popbobchatmemeowo : Module(
    name = "popbobchatmemeowo",
    description = "Send funny things about popbob",
    category = Category.CHAT
) {
    val text = ArrayList<String>()

    private val cooldown by setting("cooldown", 10.0f, 10.0f..360.0f, 1.0f)
    val timer = TickTimer()
    var timertick = 0

    init {
        onEnable {
            MessageSendHelper.sendServerMessage("> popbob is trying to use the defrag exploit on me!")
            val inputStream: InputStream = File("$DIRECTORY/popbob.txt").inputStream()
            inputStream.bufferedReader().forEachLine {
                text.add(it)
            }


        }
        onDisable {
            MessageSendHelper.sendServerMessage("> popbob griefed my client :(")
        }
        safeListener<TickEvent.ClientTickEvent> {
            timertick++
            if (text.size <= 0) {
                MessageSendHelper.sendChatMessage("popbob.txt is empty, disabling...")
                disable()
            }
            if(timertick >= cooldown){
                MessageSendHelper.sendServerMessage("> ${text.random()}")
                timertick = 0
            }
        }
    }

}