package com.lambda.client.module.modules.combat

import com.lambda.client.event.SafeClientEvent
import com.lambda.client.event.events.RenderWorldEvent
import com.lambda.client.module.Category
import com.lambda.client.module.Module
import com.lambda.client.module.modules.combat.CrystalAura.atValue
import com.lambda.client.module.modules.combat.CrystalAura.setting
import com.lambda.client.module.modules.combat.HoleESP.setting
import com.lambda.client.util.TickTimer
import com.lambda.client.util.color.ColorHolder
import com.lambda.client.util.combat.SurroundUtils
import com.lambda.client.util.combat.SurroundUtils.checkHole
import com.lambda.client.util.graphics.ESPRenderer
import com.lambda.client.util.graphics.GeometryMasks
import com.lambda.client.util.math.VectorUtils.toBlockPos
import com.lambda.client.util.threads.defaultScope
import com.lambda.client.util.threads.safeListener
import com.lambda.client.util.world.getClosestVisibleSide
import com.lambda.client.util.world.getHitVec
import com.lambda.client.util.world.getVisibleSides
import kotlinx.coroutines.launch
import net.minecraft.init.Blocks.OBSIDIAN
import net.minecraft.init.Blocks.WEB
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock
import net.minecraft.init.Items.FLINT_AND_STEEL
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d

object HoleFiller : Module(
    name = "HoleFiller",
    description = "Put the holes in fire",
    category = Category.COMBAT
) {
        private val range by setting("Hole Distance", 5, 0..5, 1)
        private val FillWith by setting("FillWith", Item.OBSIDIAN)
        private val filled by setting("Filled", true)
        private val outline by setting("Outline", true)
        private val hideOwn by setting("Hide Own", true)
        private val colorObsidian by setting("Obby Color", ColorHolder(208, 144, 255), false, visibility = { shouldAddObsidian() })
        private val colorBedrock by setting("Bedrock Color", ColorHolder(144, 144, 255), false, visibility = { shouldAddBedrock() })
        private val aFilled by setting("Filled Alpha", 31, 0..255, 1, { filled })
        private val aOutline by setting("Outline Alpha", 127, 0..255, 1, { outline })
        private val renderMode by setting("Mode", Mode.BLOCK_HOLE)
        private val holeType by setting("Hole Type", HoleType.BOTH)
        private val placeOffset by setting("Place Offset", 1.0f, 0f..1f, 0.05f)

        private enum class Mode {
            BLOCK_HOLE, BLOCK_FLOOR, FLAT
        }
    private enum class Item {
        OBSIDIAN, FLINT, COBWEB
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

            defaultScope.launch {
                val cached = ArrayList<Triple<AxisAlignedBB, ColorHolder, Int>>()

                for (x in -range..range) for (y in -range..range) for (z in -range..range) {
                    if (hideOwn && x == 0 && y == 0 && z == 0) continue
                    val pos = playerPos.add(x, y, z)

                    val holeType = checkHole(pos)
                    if (holeType == SurroundUtils.HoleType.NONE) continue


                    if (holeType == SurroundUtils.HoleType.OBBY && shouldAddObsidian() || holeType == SurroundUtils.HoleType.BEDROCK && shouldAddBedrock()) {
                        if(mc.player.heldItemMainhand.item == FLINT_AND_STEEL && FillWith == Item.FLINT ){
                            clicBlok(pos)
                        }
                    }

                    if (holeType == SurroundUtils.HoleType.OBBY && shouldAddObsidian() || holeType == SurroundUtils.HoleType.BEDROCK && shouldAddBedrock()) {
                        if(mc.player.heldItemMainhand.item == OBSIDIAN && FillWith == Item.OBSIDIAN ){
                            val hitVec = Vec3d(pos).add(0.5, 0.0, 0.5).add(Vec3d(EnumFacing.UP.directionVec).scale(0.5))
                            CPacketPlayerTryUseItemOnBlock(pos.down(), EnumFacing.UP, EnumHand.MAIN_HAND, hitVec.x.toFloat(), hitVec.y.toFloat(), hitVec.z.toFloat())
                        }
                    }
                    if (holeType == SurroundUtils.HoleType.OBBY && shouldAddObsidian() || holeType == SurroundUtils.HoleType.BEDROCK && shouldAddBedrock()) {
                        if(mc.player.heldItemMainhand.item == WEB && FillWith == Item.COBWEB ){
                            val hitVec = Vec3d(pos).add(0.5, 0.0, 0.5).add(Vec3d(EnumFacing.UP.directionVec).scale(0.5))
                            CPacketPlayerTryUseItemOnBlock(pos.down(), EnumFacing.UP, EnumHand.MAIN_HAND, hitVec.x.toFloat(), hitVec.y.toFloat(), hitVec.z.toFloat())
                        }
                    }
                }

                renderer.replaceAll(cached)
            }
        }
    private fun clicBlok(pos: BlockPos) {
    if(!mc.world.getBlockState(pos).block.isFireSource(mc.world, pos.down(), EnumFacing.UP)) {
        val hitVec = Vec3d(pos).add(0.5, 0.0, 0.5).add(Vec3d(EnumFacing.UP.directionVec).scale(0.5))

        mc.playerController.processRightClickBlock(mc.player, mc.world, pos.down(), EnumFacing.UP, hitVec, EnumHand.MAIN_HAND)
    }
    }

        private fun shouldAddObsidian() = holeType == HoleType.OBSIDIAN || holeType == HoleType.BOTH

        private fun shouldAddBedrock() = holeType == HoleType.BEDROCK || holeType == HoleType.BOTH

}