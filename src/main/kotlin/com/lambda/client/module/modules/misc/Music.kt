package com.lambda.client.module.modules.misc

import javazoom.jl.decoder.JavaLayerException
import javazoom.jl.player.Player
import java.io.BufferedInputStream
import java.net.MalformedURLException
import java.io.IOException
import java.lang.NullPointerException
import java.net.URL

class Music {
    fun play() {
        val song = "http://www.ntonyx.com/mp3files/Morning_Flower.mp3"
        var mp3player: Player? = null
        var `in`: BufferedInputStream? = null
        try {
            `in` = BufferedInputStream(URL(song).openStream())
            mp3player = Player(`in`)
            mp3player.play()
        } catch (ex: MalformedURLException) {
        } catch (e: IOException) {
        } catch (e: JavaLayerException) {
        } catch (ex: NullPointerException) {
        }
    }
}