package com.lambda.client.module.modules.combat

import com.lambda.client.manager.managers.CombatManager
import com.lambda.client.module.Category
import com.lambda.client.module.Module
import com.lambda.client.util.text.MessageSendHelper
import com.lambda.client.util.threads.safeListener
import net.minecraft.entity.EntityLivingBase
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraftforge.fml.common.gameevent.TickEvent

object TpAura : Module(
    name = "TpAura",
    description = "KillAura with infinite reach (Infinite as in to the edge of the render distance if good config)",
    category = Category.COMBAT,
    modulePriority = 120
) {
    val mode by setting("Mode", Mode.FAST)

    val delay by setting("Tick Delay", 2, 1..200, 1)
    val disableOnDeath by setting("Death Disable", true)


    val range by setting("Range", 50, 5..500, 1)

    var timer = 0

    init {
        onEnable {
            timer = 0
        }
        onDisable {
            timer = 0
        }

        safeListener<TickEvent.ClientTickEvent> {
            if (it.phase != TickEvent.Phase.START) {
                return@safeListener
            }

            timer++
            if (timer <= delay) {
                MessageSendHelper.sendChatMessage("timer'd @ $timer")
                return@safeListener
            }
            timer = 0

            if (!player.isEntityAlive) {
                if (disableOnDeath) {
                    disable()
                }
                return@safeListener
            }
            val target = CombatManager.target ?: return@safeListener

            MessageSendHelper.sendChatMessage("e")

            if (!CombatManager.isOnTopPriority(TpAura) || CombatSetting.pause) return@safeListener
            if (player.getDistance(target) >= range) return@safeListener

            doTpHit(target)
        }
    }

    private fun doTpHit(target: EntityLivingBase) {

        if (mode == Mode.FAST) {
            MessageSendHelper.sendChatMessage("Attacked ${target.name}")

            mc.player.connection.sendPacket(CPacketPlayer.Position(target.posX, target.posY, target.posZ, false))
            mc.playerController.attackEntity(mc.player, target)
            mc.player.connection.sendPacket(CPacketPlayer.Position(mc.player.posX, mc.player.posY, mc.player.posZ, mc.player.onGround))
        }
    }

    enum class Mode {
        FAST,
        GOOD
    }
}