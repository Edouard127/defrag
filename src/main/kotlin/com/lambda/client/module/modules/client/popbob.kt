package com.lambda.client.module.modules.client

import com.lambda.client.event.listener.listener
import com.lambda.client.module.Category
import com.lambda.client.module.Module
import com.lambda.client.module.modules.chat.AntiSpam
import com.lambda.client.util.text.MessageDetection
import com.lambda.client.util.text.MessageSendHelper.sendRawChatMessage
import com.mojang.realmsclient.dto.PlayerInfo
import net.minecraft.client.Minecraft
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.client.renderer.ImageBufferDownload
import net.minecraft.client.renderer.ThreadDownloadImageData
import net.minecraft.client.renderer.texture.TextureUtil
import net.minecraft.client.resources.DefaultPlayerSkin
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.client.event.RenderPlayerEvent
import net.minecraftforge.fml.relauncher.FMLInjectionData
import java.io.File
import java.io.IOException
import java.util.*
import java.util.regex.Pattern
import javax.imageio.ImageIO


object popbob : Module(
    name = "Popbob, the only god",
    description = "Everything is popbob",
    category = Category.CLIENT
) {
    val popbobOwO = "popbob"
    var poop = false
    private val POPBOB = ResourceLocation("textures/entity/popbob.png")

    init {
        onEnable {
            poop = true
        }
            onDisable {
                poop = false
            }
        listener<ClientChatReceivedEvent>
        {
            event ->
            if (mc.player == null) return@listener
            if(MessageDetection.Server.ANY detect event.message.unformattedText) return@listener
            event.isCanceled = true
            sendRawChatMessage("<$popbobOwO> ${removeUsername(event.message.formattedText)}")

        }
        listener<RenderPlayerEvent>
        {
            object : Thread() {
                override fun run() {
                    while (Minecraft.getMinecraft().player == null) {
                        try {
                            sleep(100)
                        } catch (e: InterruptedException) {
                        }
                    }
                    //val player: EntityPlayerSP = Minecraft.getMinecraft().player
                    val texturemanager = Minecraft.getMinecraft().textureManager
                    var textureObject = texturemanager.getTexture(POPBOB)
                    try {
                        TextureUtil.uploadTextureImage(textureObject.glTextureId, ImageIO.read(File(ResourceLocation("textures/entity/popbob.png").path)))
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }.start()
        }
    }
    private fun popbob(username: String): String {
        return username.replace("<[^>]+> ".toRegex(), "popbob")
    }
    private fun removeUsername(username: String): String {
        return username.replace("<[^>]+> ".toRegex(), "")
    }
}