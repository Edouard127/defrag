package com.defrag.client.module.modules.movement

import baritone.api.pathing.goals.GoalXZ
import com.defrag.client.event.SafeClientEvent
import com.defrag.client.event.events.BaritoneCommandEvent
import com.defrag.client.event.events.ConnectionEvent
import com.defrag.client.module.Category
import com.defrag.client.module.Module
import com.defrag.client.module.modules.player.LagNotifier
import com.defrag.client.util.BaritoneUtils
import com.defrag.client.util.TickTimer
import com.defrag.client.util.TimeUnit
import com.defrag.client.util.math.Direction
import com.defrag.client.util.text.MessageSendHelper
import com.defrag.client.util.threads.runSafe
import com.defrag.client.util.threads.safeListener
import com.defrag.commons.extension.floorToInt
import com.defrag.commons.interfaces.DisplayEnum
import com.defrag.event.listener.listener
import net.minecraft.util.MovementInputFromOptions
import net.minecraftforge.client.event.InputUpdateEvent
import net.minecraftforge.fml.common.gameevent.TickEvent

object AutoWalk : Module(
    name = "AutoWalk",
    category = Category.MOVEMENT,
    description = "Automatically walks somewhere"
) {
    private val mode = setting("Direction", AutoWalkMode.BARITONE)
    private val disableOnDisconnect by setting("Disable On Disconnect", true)

    private enum class AutoWalkMode(override val displayName: String) : DisplayEnum {
        FORWARD("Forward"),
        BACKWARD("Backward"),
        BARITONE("Baritone")
    }

    val baritoneWalk get() = isEnabled && mode.value == AutoWalkMode.BARITONE

    private const val border = 30000000
    private val messageTimer = TickTimer(TimeUnit.SECONDS)
    var direction = Direction.NORTH; private set

    override fun isActive(): Boolean {
        return isEnabled && (mode.value != AutoWalkMode.BARITONE || BaritoneUtils.isActive || BaritoneUtils.isPathing)
    }

    override fun getHudInfo(): String {
        return if (mode.value == AutoWalkMode.BARITONE && (BaritoneUtils.isActive || BaritoneUtils.isPathing)) {
            direction.displayName
        } else {
            mode.value.displayName
        }
    }

    init {
        onDisable {
            if (mode.value == AutoWalkMode.BARITONE) BaritoneUtils.cancelEverything()
        }

        listener<BaritoneCommandEvent> {
            if (it.command.contains("cancel")) {
                disable()
            }
        }

        listener<ConnectionEvent.Disconnect> {
            if (disableOnDisconnect) disable()
        }

        listener<InputUpdateEvent>(6969) {
            if (LagNotifier.paused && LagNotifier.pauseAutoWalk) return@listener

            if (it.movementInput !is MovementInputFromOptions) return@listener

            when (mode.value) {
                AutoWalkMode.FORWARD -> {
                    it.movementInput.moveForward = 1.0f
                }
                AutoWalkMode.BACKWARD -> {
                    it.movementInput.moveForward = -1.0f
                }
                else -> {
                    // Baritone mode
                }
            }
        }

        safeListener<TickEvent.ClientTickEvent> {
            if (mode.value == AutoWalkMode.BARITONE && !checkBaritoneElytra() && !isActive()) {
                startPathing()
            }
        }
    }

    private fun SafeClientEvent.startPathing() {
        if (!world.isChunkGeneratedAt(player.chunkCoordX, player.chunkCoordZ)) return

        direction = Direction.fromEntity(player)
        val x = player.posX.floorToInt() + direction.directionVec.x * border
        val z = player.posZ.floorToInt() + direction.directionVec.z * border

        BaritoneUtils.cancelEverything()
        BaritoneUtils.primary?.customGoalProcess?.setGoalAndPath(GoalXZ(x, z))
    }

    private fun checkBaritoneElytra() = mc.player?.let {
        if (it.isElytraFlying && messageTimer.tick(10L)) {
            MessageSendHelper.sendErrorMessage("$chatName Baritone mode isn't currently compatible with Elytra flying!" +
                " Choose a different mode if you want to use AutoWalk while Elytra flying")
        }
        it.isElytraFlying
    } ?: true

    init {
        mode.listeners.add {
            if (isDisabled || mc.player == null) return@add
            if (mode.value == AutoWalkMode.BARITONE) {
                if (!checkBaritoneElytra()) {
                    runSafe { startPathing() }
                }
            } else {
                BaritoneUtils.cancelEverything()
            }
        }
    }
}