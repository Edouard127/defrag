package me.han.muffin.client.utils.network

import me.han.muffin.client.mixin.ClientLoader
import java.awt.Desktop
import java.io.BufferedReader
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.net.InetSocketAddress
import java.net.Socket
import java.net.URI
import java.net.URL
import java.nio.channels.Channels

/**
 * @author balusc (StackOverflow ID 157882)
 *
 * https://stackoverflow.com/questions/3584210/preferred-java-way-to-ping-an-http-url-for-availability#3584332
 */
object WebUtils: Runnable {
    var isInternetDown = false

    fun isDown(host: String?, port: Int, timeout: Int): Boolean {
        try {
            Socket().use { socket ->
                socket.connect(InetSocketAddress(host, port), timeout)
                return false
            }
        } catch (e: IOException) {
            return true // Either timeout or unreachable or failed DNS lookup.
        }
    }

    override fun run() {
        isInternetDown = isDown("1.1.1.1", 80, 100)
    }

    fun openWebLink(url: URI) {
        try {
            Desktop.getDesktop().browse(url)
        } catch (e: IOException) {
            ClientLoader.LOGGER.error("Couldn't open link: $url")
        }
    }

    fun getUrlContents(_url: String): String {
        val content = StringBuilder()
        try {
            val stream = URL(_url).openConnection().also {
                it.addRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)")
            }.getInputStream()

            val bufferedReader = BufferedReader(InputStreamReader(stream))
            var line: String?

            while (bufferedReader.readLine().also { line = it } != null) {
                content.append(line).append("\n")
            }
            bufferedReader.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return content.toString()
    }

    @Throws(IOException::class)
    fun downloadUsingNIO(urlStr: String, file: String) {
        val url = URL(urlStr)
        val rbc = Channels.newChannel(url.openStream())
        val fos = FileOutputStream(file)
        fos.channel.transferFrom(rbc, 0, Long.MAX_VALUE)
        fos.close()
        rbc.close()
    }
}