package com.defrag.client.module.modules.chat

import com.defrag.client.module.Category
import com.defrag.client.module.Module
import com.defrag.client.util.TickTimer
import com.defrag.client.util.TimeUnit
import com.defrag.client.util.text.MessageSendHelper
import com.defrag.client.util.text.MessageSendHelper.sendServerMessage
import com.defrag.client.util.threads.safeListener
import net.minecraft.world.EnumDifficulty
import net.minecraftforge.fml.common.gameevent.TickEvent
import java.text.SimpleDateFormat
import java.util.*

object AutoQMain : Module(
    name = "AutoQMain",
    description = "Automatically does '/queue main'",
    category = Category.CHAT,
    showOnArray = false
) {
    private val delay by setting("Delay", 30, 1..120, 5)
    private val twoBeeCheck by setting("2B Check", true)
    private val command by setting("Command", "/queue main")

    private val timer = TickTimer(TimeUnit.SECONDS)

    init {
        safeListener<TickEvent.ClientTickEvent> {
            if (!timer.tick(delay.toLong()) ||
                world.difficulty != EnumDifficulty.PEACEFUL ||
                player.dimension != 1) return@safeListener

            if (twoBeeCheck) {
                if (@Suppress("UNNECESSARY_SAFE_CALL")
                    player.serverBrand?.contains("2b2t") == true) sendQueueMain()
            } else {
                sendQueueMain()
            }
        }
    }

    private fun sendQueueMain() {
        val formatter = SimpleDateFormat("HH:mm:ss")
        val date = Date(System.currentTimeMillis())

        MessageSendHelper.sendChatMessage("$chatName &7Run &b$command&7 at " + formatter.format(date))
        sendServerMessage(command)
    }
}
