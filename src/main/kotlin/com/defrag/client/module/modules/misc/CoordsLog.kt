package com.defrag.client.module.modules.misc

import com.defrag.client.manager.managers.WaypointManager
import com.defrag.client.module.Category
import com.defrag.client.module.Module
import com.defrag.client.util.InfoCalculator
import com.defrag.client.util.TickTimer
import com.defrag.client.util.TimeUnit
import com.defrag.client.util.math.CoordinateConverter.asString
import com.defrag.client.util.math.VectorUtils.toBlockPos
import com.defrag.client.util.text.MessageSendHelper
import com.defrag.client.util.threads.safeListener
import net.minecraftforge.fml.common.gameevent.TickEvent

object CoordsLog : Module(
    name = "CoordsLog",
    description = "Automatically logs your coords, based on actions",
    category = Category.MISC
) {
    private val saveOnDeath by setting("Save On Death", true)
    private val autoLog by setting("Automatically Log", false)
    private val delay by setting("Delay", 15, 1..60, 1)

    private var previousCoord: String? = null
    private var savedDeath = false
    private var timer = TickTimer(TimeUnit.SECONDS)

    init {
        safeListener<TickEvent.ClientTickEvent> {
            if (autoLog && timer.tick(delay.toLong())) {
                val currentCoord = player.positionVector.toBlockPos().asString()

                if (currentCoord != previousCoord) {
                    WaypointManager.add("autoLogger")
                    previousCoord = currentCoord
                }
            }

            if (saveOnDeath) {
                savedDeath = if (player.isDead || player.health <= 0.0f) {
                    if (!savedDeath) {
                        val deathPoint = WaypointManager.add("Death - " + InfoCalculator.getServerType()).pos
                        MessageSendHelper.sendChatMessage("You died at ${deathPoint.x}, ${deathPoint.y}, ${deathPoint.z}")
                    }
                    true
                } else {
                    false
                }
            }
        }
    }

}
