package com.defrag.client.module.modules.movement

import com.defrag.client.mixin.client.entity.MixinEntity
import com.defrag.client.module.Category
import com.defrag.client.module.Module
import com.defrag.client.module.modules.player.Scaffold
import com.defrag.client.util.BaritoneUtils
import com.defrag.client.util.EntityUtils.flooredPosition
import com.defrag.client.util.Wrapper
import com.defrag.client.util.math.VectorUtils.toVec3d
import com.defrag.client.util.threads.runSafeR

/**
 * @see MixinEntity.moveInvokeIsSneakingPre
 * @see MixinEntity.moveInvokeIsSneakingPost
 */
object SafeWalk : Module(
    name = "SafeWalk",
    category = Category.MOVEMENT,
    description = "Keeps you from walking off edges"
) {
    private val checkFallDist by setting("Check Fall Distance", true, description = "Check fall distance from edge")

    init {
        onToggle {
            BaritoneUtils.settings?.assumeSafeWalk?.value = it
        }
    }

    @JvmStatic
    fun shouldSafewalk(entityID: Int) =
        (Wrapper.player?.let { !it.isSneaking && it.entityId == entityID } ?: false)
            && (isEnabled || Scaffold.isEnabled && Scaffold.safeWalk)
            && (!checkFallDist && !BaritoneUtils.isPathing || !isEdgeSafe)

    @JvmStatic
    fun setSneaking(state: Boolean) {
        Wrapper.player?.movementInput?.sneak = state
    }

    private val isEdgeSafe: Boolean
        get() = runSafeR {
            val pos = player.flooredPosition.toVec3d(0.5, 0.0, 0.5)
            world.rayTraceBlocks(
                pos,
                pos.subtract(0.0, 3.1, 0.0),
                true,
                true,
                false
            ) != null
        } ?: false
}