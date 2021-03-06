package com.lambda.client

import club.minnced.discord.rpc.*
import club.minnced.discord.rpc.DiscordEventHandlers.*
import com.lambda.client.event.ForgeEventProcessor
import com.lambda.client.gui.clickgui.LambdaClickGui
import com.lambda.client.util.ConfigUtils
import com.lambda.client.util.KamiCheck
import com.lambda.client.util.WebUtils
import com.lambda.client.util.threads.BackgroundScope
import net.minecraft.client.Minecraft
import net.minecraft.util.SoundEvent
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.File


@Suppress("UNUSED_PARAMETER")
@Mod(
    modid = LambdaMod.ID,
    name = LambdaMod.NAME,
    version = LambdaMod.VERSION
)
class LambdaMod {

    companion object {
        lateinit var inventoryManager: Any
        const val NAME = "Defragmentation"
        const val ID = "defrag"
        const val DIRECTORY = "defrag/"

        const val VERSION = "Defrag 1.4.4"
        const val VERSION_MAJOR = "Defrag 1.4.4"
        const val VERSION_SIMPLE = VERSION

        const val APP_ID = "953143990386065430" //discordrpc
        const val GITHUB_API = "https://api.github.com/"
        private const val MAIN_ORG = "lambda-client"
        const val PLUGIN_ORG = "lambda-plugins"
        private const val REPO_NAME = "defrag"
        const val CAPES_JSON = "https://raw.githubusercontent.com/Edouard127/monch-client-data/main/capes/uuids/users.json"
        const val RELEASES_API = "${GITHUB_API}repos/${MAIN_ORG}/${REPO_NAME}/releases"
        const val DOWNLOAD_LINK = "https://github.com/${MAIN_ORG}/${REPO_NAME}/releases"
        const val GITHUB_LINK = "https://github.com/$MAIN_ORG/"
        const val DISCORD_INVITE = "https://discord.gg/QjfBxJzE5x"

        const val LAMBDA = "???"

        val LOG: Logger = LogManager.getLogger(NAME)

        var ready: Boolean = false; private set
    }

    @Mod.EventHandler
    fun preInit(event: FMLPreInitializationEvent) {
        val directory = File(DIRECTORY)
        if (!directory.exists()) directory.mkdir()
        val data = File("$DIRECTORY/data/sounds")
        if(!data.exists()) data.mkdir()
        val popbob = File("$DIRECTORY/popbob.txt")
        if(!popbob.exists()) popbob.createNewFile()

        WebUtils.updateCheck()
        LoaderWrapper.preLoadAll()
    }

    @Mod.EventHandler
    fun init(event: FMLInitializationEvent) {

        LOG.info("Initializing $NAME $VERSION")

        LoaderWrapper.loadAll()

        MinecraftForge.EVENT_BUS.register(ForgeEventProcessor)

        ConfigUtils.moveAllLegacyConfigs()
        ConfigUtils.loadAll()

        BackgroundScope.start()
        WebUtils.updateCheck()
        LambdaClickGui.populateRemotePlugins()

        KamiCheck.runCheck()
        LOG.info("$NAME initialized!")
    }

    @Mod.EventHandler
    fun postInit(event: FMLPostInitializationEvent) {
        ready = true
    }
}
