package com.defrag.client.module.modules.misc

import com.defrag.client.module.Category
import com.defrag.client.module.Module
import com.defrag.client.util.threads.runSafe
import com.defrag.client.util.threads.runSafeR
import com.defrag.client.util.threads.safeListener
import net.minecraft.world.GameType
import net.minecraftforge.fml.common.gameevent.TickEvent

object FakeGameMode : Module(
    name = "FakeGameMode",
    description = "Fakes your current gamemode client side",
    category = Category.MISC
) {
    private val gamemode by setting("Mode", GameMode.CREATIVE)

    @Suppress("UNUSED")
    private enum class GameMode(val gameType: GameType) {
        SURVIVAL(GameType.SURVIVAL),
        CREATIVE(GameType.CREATIVE),
        ADVENTURE(GameType.ADVENTURE),
        SPECTATOR(GameType.SPECTATOR)
    }

    private var prevGameMode: GameType? = null

    init {
        safeListener<TickEvent.ClientTickEvent> {
            playerController.setGameType(gamemode.gameType)
        }

        onEnable {
            runSafeR {
                prevGameMode = playerController.currentGameType
            } ?: disable()
        }

        onDisable {
            runSafe {
                prevGameMode?.let { playerController.setGameType(it) }
            }
        }
    }
}