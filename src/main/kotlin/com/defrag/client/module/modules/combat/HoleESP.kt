package com.defrag.client.module.modules.combat

import com.defrag.client.event.SafeClientEvent
import com.defrag.client.event.events.RenderWorldEvent
import com.defrag.client.module.Category
import com.defrag.client.module.Module
import com.defrag.client.util.TickTimer
import com.defrag.client.util.color.ColorHolder
import com.defrag.client.util.combat.SurroundUtils
import com.defrag.client.util.combat.SurroundUtils.checkHole
import com.defrag.client.util.graphics.ESPRenderer
import com.defrag.client.util.graphics.GeometryMasks
import com.defrag.client.util.math.VectorUtils.toBlockPos
import com.defrag.client.util.threads.defaultScope
import com.defrag.client.util.threads.safeListener
import kotlinx.coroutines.launch
import net.minecraft.util.math.AxisAlignedBB

object HoleESP : Module(
    name = "HoleESP",
    category = Category.COMBAT,
    description = "Show safe holes for crystal pvp"
) {
    private val range by setting("Render Distance", 8, 4..32, 1)
    private val filled by setting("Filled", true)
    private val outline by setting("Outline", true)
    private val hideOwn by setting("Hide Own", true)
    private val colorObsidian by setting("Obby Color", ColorHolder(208, 144, 255), false, visibility = { shouldAddObsidian() })
    private val colorBedrock by setting("Bedrock Color", ColorHolder(144, 144, 255), false, visibility = { shouldAddBedrock() })
    private val aFilled by setting("Filled Alpha", 31, 0..255, 1, { filled })
    private val aOutline by setting("Outline Alpha", 127, 0..255, 1, { outline })
    private val renderMode by setting("Mode", Mode.BLOCK_HOLE)
    private val holeType by setting("Hole Type", HoleType.BOTH)

    private enum class Mode {
        BLOCK_HOLE, BLOCK_FLOOR, FLAT
    }

    private enum class HoleType {
        OBSIDIAN, BEDROCK, BOTH
    }

    private val renderer = ESPRenderer()
    private val timer = TickTimer()

    init {
        safeListener<RenderWorldEvent> {
            if (timer.tick(133L)) { // Avoid running this on a tick
                updateRenderer()
            }
            renderer.render(false)
        }
    }

    private fun SafeClientEvent.updateRenderer() {
        renderer.aFilled = if (filled) aFilled else 0
        renderer.aOutline = if (outline) aOutline else 0

        val playerPos = player.positionVector.toBlockPos()
        val side = if (renderMode != Mode.FLAT) GeometryMasks.Quad.ALL
        else GeometryMasks.Quad.DOWN

        defaultScope.launch {
            val cached = ArrayList<Triple<AxisAlignedBB, ColorHolder, Int>>()

            for (x in -range..range) for (y in -range..range) for (z in -range..range) {
                if (hideOwn && x == 0 && y == 0 && z == 0) continue
                val pos = playerPos.add(x, y, z)

                val holeType = checkHole(pos)
                if (holeType == SurroundUtils.HoleType.NONE) continue

                val bb = AxisAlignedBB(if (renderMode == Mode.BLOCK_FLOOR) pos.down() else pos)

                if (holeType == SurroundUtils.HoleType.OBBY && shouldAddObsidian()) {
                    cached.add(Triple(bb, colorObsidian, side))
                }

                if (holeType == SurroundUtils.HoleType.BEDROCK && shouldAddBedrock()) {
                    cached.add(Triple(bb, colorBedrock, side))
                }
            }

            renderer.replaceAll(cached)
        }
    }

    private fun shouldAddObsidian() = holeType == HoleType.OBSIDIAN || holeType == HoleType.BOTH

    private fun shouldAddBedrock() = holeType == HoleType.BEDROCK || holeType == HoleType.BOTH

}