package com.lambda.client.module.modules.misc

import com.lambda.client.LambdaMod.Companion.DIRECTORY
import com.lambda.client.module.Category
import com.lambda.client.module.Module
import com.lambda.client.util.text.MessageSendHelper
import java.io.BufferedReader
import java.io.InputStreamReader


object MusicPlayer : Module(
    name = "MusicPlayer",
    description = "Play music in stream",
    category = Category.MISC
) {
    private const val UNCHANGED = "Unchanged"
    public var url by setting("URL", UNCHANGED)
    //private val url by setting("URL", UNCHANGED, { MessageMode.CUSTOM })
    init {
        try {
            val p = Runtime.getRuntime().exec("systeminfo") /*Execute cmd command "systeminfo"*/
            val r = BufferedReader(InputStreamReader(p.inputStream))
            var line: String?
            val p2 = Runtime.getRuntime().exec("hostnamectl | grep 'Kernel'") /*Execute cmd command "systeminfo"*/
            val r2 = BufferedReader(InputStreamReader(p2.inputStream))
            var line2: String?
            while (true) {
                line = r.readLine()
                line2 = r2.readLine()
                if (line == null || line2 == null) {
                    break
                }
                if(line.contains("Windows")) /*If output contains OS Name and 2010*/ {
                    MessageSendHelper.sendChatMessage("Windows detected\nDownloading libraries...")
                }
                if(line2.contains("Kernel")){
                    MessageSendHelper.sendChatMessage("Linux kernl Detected\nDownloading libraries")
                    Runtime.getRuntime().exec("wget https://yt-dl.org/downloads/latest/youtube-dl -O ${DIRECTORY}youtube-dl")
                    Runtime.getRuntime().exec("chmod +x ${DIRECTORY}youtube-dl")
                    val p3 = Runtime.getRuntime().exec("python3 ./youtube-dl") /*Execute cmd command "systeminfo"*/
                    val r3 = BufferedReader(InputStreamReader(p.inputStream))
                    var line3: String?
                    while (true) {
                        line3 = r3.readLine()
                        if (line == null || line2 == null) {
                            break
                        }
                    }
                    if(!line3!!.contains("Command")) continue
                    else break
                    Runtime.getRuntime().exec("python3 ./youtube-dl -f 140 $url")



                }
            }
        } catch (e: Exception) {
            println("Platform Type: osWindowsCheck: exception$e")
        }

    }

}