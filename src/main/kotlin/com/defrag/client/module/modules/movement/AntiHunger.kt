package com.defrag.client.module.modules.movement

import com.defrag.client.event.events.PacketEvent
import com.defrag.client.mixin.extension.onGround
import com.defrag.client.module.Category
import com.defrag.client.module.Module
import com.defrag.client.util.threads.safeListener
import net.minecraft.network.play.client.CPacketEntityAction
import net.minecraft.network.play.client.CPacketPlayer

/**
 * Movement taken from Seppuku
 * https://github.com/seppukudevelopment/seppuku/blob/005e2da/src/main/java/me/rigamortis/seppuku/impl/module/player/NoHungerModule.java
 */
object AntiHunger : Module(
    name = "AntiHunger",
    category = Category.MOVEMENT,
    description = "Reduces hunger lost when moving around"
) {
    private val cancelMovementState by setting("Cancel Movement State", true)

    init {
        safeListener<PacketEvent.Send> {
            when (it.packet) {
                is CPacketEntityAction -> {
                    if (cancelMovementState &&
                        (it.packet.action == CPacketEntityAction.Action.START_SPRINTING ||
                            it.packet.action == CPacketEntityAction.Action.STOP_SPRINTING)) {
                        it.cancel()
                    }
                }
                is CPacketPlayer -> {

                    it.packet.onGround = (player.fallDistance <= 0 || mc.playerController.isHittingBlock) && player.isElytraFlying
                }
            }
        }
    }
}