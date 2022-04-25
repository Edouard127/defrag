package com.lambda.client.gui.mc

import com.lambda.client.LambdaMod
import com.lambda.client.LambdaMod.Companion.DIRECTORY
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.GuiButton
import com.lambda.client.util.color.ColorConverter
import com.lambda.client.util.text.MessageSendHelper
import jaco.mp3.player.MP3Player
import net.minecraft.util.SoundCategory
import java.io.File

var i  = 1044
const val base = 1044
class DefragLoadMusic : GuiScreen() {
    private var file: File? = null
    var musicLength = 0

    var posY = 0
    override fun initGui() {
        buttonList.add(GuiButton(696969, getPosX(216), getPosY(0), 150, 20, "Stop music"))
        buttonList.add(GuiButton(42069, getPosX(216), getPosY(24), 150, 20, "Repeat/Unrepeat"))
            File("$DIRECTORY/data/music").walkTopDown().forEach lit@{
                if(it.isFile){
                    print(it.name.toString())

                    buttonList.add(GuiButton(i, getPosX(0), getPosY(posY), 150, 20, it.name.toString()))
                    i++
                    posY += 24
                    musicLength++
                }
            }
        super.initGui()
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        drawDefaultBackground()
        drawCenteredString(fontRenderer, "Play some fucking music", width / 2, 40, ColorConverter.rgbToHex(155, 144, 255))
        super.drawScreen(mouseX, mouseY, partialTicks)
    }

    private fun getPosX(x: Int): Int {
        return (width - screenW) / 2 + x
    }

    private fun getPosY(y: Int): Int {
        return (height - screenH) / 2 + y
    }
    override fun actionPerformed(button: GuiButton) {
        println(button.id)
        if(button.id == 696969 && MP3Player().isPlaying) {
            MP3Player(file).stop()
            MessageSendHelper.sendChatMessage("Successfully stopped")
            return
        }
        if(button.id in base..i && !MP3Player().isPlaying){
            this.file = File("$DIRECTORY/data/music/${button.displayString}")
            object : Thread(){
                override fun run(){
                    MP3Player().volume = mc.gameSettings.getSoundLevel(SoundCategory.MUSIC).toInt()
                    MP3Player(file).play()
                    MessageSendHelper.sendChatMessage("Playing ${button.displayString}")
                    return
                }

            }.run()
            return
        }
        if(button.id == 42069 && file != null && MP3Player().isPlaying){
            MP3Player(file).setRepeat(!MP3Player().isRepeat)
            MessageSendHelper.sendChatMessage("Successfully looped")
            return

        }

    }

    companion object {
        private const val screenW = 370
        private const val screenH = 68
    }
}