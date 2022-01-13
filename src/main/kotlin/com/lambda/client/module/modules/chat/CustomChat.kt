package com.lambda.client.module.modules.chat

import com.lambda.client.manager.managers.MessageManager.newMessageModifier
import com.lambda.client.module.Category
import com.lambda.client.module.Module
import com.lambda.client.util.text.MessageDetection
import com.lambda.client.util.text.MessageSendHelper
import kotlin.math.min

object CustomChat : Module(
    name = "CustomChat",
    category = Category.CHAT,
    description = "Add a custom ending to your message!",
    showOnArray = false,
    modulePriority = 200
) {
    private val textMode by setting("Message", TextMode.NAME)
    private val decoMode by setting("Separator", DecoMode.NONE)
    private val commands by setting("Commands", false)
    private val spammer by setting("Spammer", false)
    private val customText by setting("Custom Text", "Default")

    private enum class DecoMode {
        SEPARATOR, CLASSIC, NONE
    }

    private enum class TextMode {
        NAME, ON_TOP, WEBSITE, CUSTOM
    }

    private val modifier = newMessageModifier(
        filter = {
            (commands || MessageDetection.Command.ANY detectNot it.packet.message)
                && (spammer || it.source !is Spammer)
        },
        modifier = {
            val message = it.packet.message + getFull()
            message.substring(0, min(256, message.length))
        }
    )

    init {
        onEnable {
            if (textMode == TextMode.CUSTOM && customText.equals("Default", ignoreCase = true)) {
                MessageSendHelper.sendWarningMessage("$chatName In order to use the Custom message, please change the CustomText setting in ClickGUI")
                disable()
            } else {
                modifier.enable()
            }
        }

        onDisable {
            modifier.disable()
        }
    }

    private fun getText() = when (textMode) {
        TextMode.NAME -> "自杀"
        TextMode.ON_TOP -> "\uD835\uDD3B\uD835\uDD56\uD835\uDD57\uD835\uDD63\uD835\uDD52\uD835\uDD58 \uD835\uDD60\uD835\uDD5F\uD835\uDD65\uD835\uDD60\uD835\uDD61"
        TextMode.WEBSITE -> "\uD835\uDD56\uD835\uDFDE\uD835\uDFDA\uD835\uDFD9.\uD835\uDD5F\uD835\uDD56\uD835\uDD65"
        TextMode.CUSTOM -> customText
    }

    private fun getFull() = when (decoMode) {
        DecoMode.NONE -> " " + getText()
        DecoMode.CLASSIC -> " \u00ab " + getText() + " \u00bb"
        DecoMode.SEPARATOR -> " | " + getText()
    }

}
