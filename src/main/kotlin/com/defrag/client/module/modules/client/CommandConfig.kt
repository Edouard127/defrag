package com.defrag.client.module.modules.client

import com.defrag.client.LambdaMod
import com.defrag.client.event.events.ModuleToggleEvent
import com.defrag.client.module.Category
import com.defrag.client.module.Module
import com.defrag.client.util.TickTimer
import com.defrag.client.util.text.MessageSendHelper
import com.defrag.client.util.text.format
import com.defrag.event.listener.listener
import net.minecraft.util.text.TextFormatting
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.lwjgl.opengl.Display

object CommandConfig : Module(
    name = "CommandConfig",
    category = Category.CLIENT,
    description = "Configures client chat related stuff",
    showOnArray = false,
    alwaysEnabled = true
) {
    var prefix by setting("Prefix", ";", { false })
    val toggleMessages by setting("Toggle Messages", false)
    private val customTitle = setting("Window Title", true)

    private val timer = TickTimer()
    private val prevTitle = Display.getTitle()
    private const val title = "${LambdaMod.NAME} ${LambdaMod.LAMBDA} ${LambdaMod.VERSION}"

    init {
        listener<ModuleToggleEvent> {
            if (!toggleMessages || it.module == ClickGUI) return@listener

            MessageSendHelper.sendChatMessage(it.module.name +
                if (it.module.isEnabled) TextFormatting.RED format " disabled"
                else TextFormatting.GREEN format " enabled"
            )
        }

        listener<TickEvent.ClientTickEvent> {
            if (timer.tick(10000L)) {
                if (customTitle.value) Display.setTitle("$title - ${mc.session.username}")
                else Display.setTitle(prevTitle)
            }
        }

        customTitle.listeners.add {
            timer.reset(-0xCAFEBABE)
        }
    }
}