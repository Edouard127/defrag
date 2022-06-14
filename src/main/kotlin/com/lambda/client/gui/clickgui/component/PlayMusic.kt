package com.lambda.client.gui.clickgui.component

import com.lambda.client.LambdaMod
import com.lambda.client.util.text.MessageSendHelper
import jaco.mp3.player.MP3Player
import net.minecraft.util.SoundCategory
import java.io.File

object PlayMusic {
    fun play(name: String){
        var file = File("${LambdaMod.DIRECTORY}/data/music/${name}.mp3")
        object : Thread(){
            override fun run(){
                //MP3Player().volume = mc.gameSettings.getSoundLevel(SoundCategory.MUSIC).toInt()
                MP3Player(file).play()
                MessageSendHelper.sendChatMessage("Playing $name")
                return
            }

        }.run()
        return
    }
}