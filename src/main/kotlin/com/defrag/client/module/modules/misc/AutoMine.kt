package com.defrag.client.module.modules.misc

import com.defrag.client.command.CommandManager
import com.defrag.client.event.events.BaritoneCommandEvent
import com.defrag.client.event.events.ConnectionEvent
import com.defrag.client.mixin.extension.sendClickBlockToController
import com.defrag.client.module.Category
import com.defrag.client.module.Module
import com.defrag.client.util.BaritoneUtils
import com.defrag.client.util.text.MessageSendHelper
import com.defrag.client.util.text.formatValue
import com.defrag.client.util.threads.runSafe
import com.defrag.client.util.threads.runSafeR
import com.defrag.client.util.threads.safeListener
import com.defrag.event.listener.listener
import net.minecraftforge.fml.common.gameevent.TickEvent

object AutoMine : Module(
    name = "AutoMine",
    description = "Automatically mines chosen ores",
    category = Category.MISC
) {

    private val manual by setting("Manual", false)
    private val iron = setting("Iron", false)
    private val diamond = setting("Diamond", true)
    private val gold = setting("Gold", false)
    private val coal = setting("Coal", false)
    private val log = setting("Logs", false)

    init {
        onEnable {
            runSafeR {
                run()
            } ?: disable()
        }

        onDisable {
            BaritoneUtils.cancelEverything()
        }
    }

    private fun run() {
        if (isDisabled || manual) return

        val blocks = ArrayList<String>()

        if (iron.value) blocks.add("iron_ore")
        if (diamond.value) blocks.add("diamond_ore")
        if (gold.value) blocks.add("gold_ore")
        if (coal.value) blocks.add("coal_ore")
        if (log.value) {
            blocks.add("log")
            blocks.add("log2")
        }

        if (blocks.isEmpty()) {
            MessageSendHelper.sendBaritoneMessage("Error: you have to choose at least one thing to mine. " +
                "To mine custom blocks run the ${formatValue("${CommandManager.prefix}b mine block")} command")
            BaritoneUtils.cancelEverything()
            return
        }

        MessageSendHelper.sendBaritoneCommand("mine", *blocks.toTypedArray())
    }

    init {
        safeListener<TickEvent.ClientTickEvent> {
            if (manual) {
                mc.sendClickBlockToController(true)
            }
        }

        listener<ConnectionEvent.Disconnect> {
            disable()
        }

        listener<BaritoneCommandEvent> {
            if (it.command.contains("cancel")) {
                disable()
            }
        }

        with({ runSafe { run() } }) {
            iron.listeners.add(this)
            diamond.listeners.add(this)
            gold.listeners.add(this)
            coal.listeners.add(this)
            log.listeners.add(this)
        }
    }
}