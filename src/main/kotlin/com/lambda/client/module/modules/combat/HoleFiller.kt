package com.lambda.client.module.modules.combat

import com.lambda.client.event.SafeClientEvent
import com.lambda.client.event.events.RenderWorldEvent
import com.lambda.client.gui.hudgui.elements.world.PlayerList
import com.lambda.client.manager.managers.CombatManager
import com.lambda.client.manager.managers.FriendManager
import com.lambda.client.manager.managers.HotbarManager.resetHotbar
import com.lambda.client.manager.managers.HotbarManager.serverSideItem
import com.lambda.client.manager.managers.HotbarManager.spoofHotbar
import com.lambda.client.module.Category
import com.lambda.client.module.Module
import com.lambda.client.util.TickTimer
import com.lambda.client.util.color.ColorHolder
import com.lambda.client.util.combat.SurroundUtils
import com.lambda.client.util.combat.SurroundUtils.checkHole
import com.lambda.client.util.graphics.ESPRenderer
import com.lambda.client.util.graphics.GeometryMasks
import com.lambda.client.util.items.*
import com.lambda.client.util.math.RotationUtils.getRotationTo
import com.lambda.client.util.math.VectorUtils.distanceTo
import com.lambda.client.util.math.VectorUtils.toBlockPos
import com.lambda.client.util.math.VectorUtils.toVec3d
import com.lambda.client.util.text.MessageSendHelper
import com.lambda.client.util.threads.defaultScope
import com.lambda.client.util.threads.safeListener
import com.lambda.client.util.world.*
import kotlinx.coroutines.launch
import net.minecraft.block.Block
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Blocks
import net.minecraft.item.ItemStack
import net.minecraft.network.Packet
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.math.*
import net.minecraftforge.fml.common.gameevent.TickEvent
import kotlin.math.atan2
import kotlin.math.sqrt

@Suppress("UNUSED")
object HoleFiller :
    Module(name = "HoleFiller", description = "Fill holes", category = Category.COMBAT, modulePriority = 90) {
    private val range by setting("Hole Distance", 4, 0..4, 1)
    private val obby by setting("FillWith", Obby.OBSIDIAN)
    private val mode by setting("Mode", Mode.CONSTANT)
    private val playerRange by setting("Player Range", 4.0, 0.0..4.0, 0.25, { mode == Mode.SURPRISE })
    private val fillPlayerRange by setting("Fill Player Range", 2.0, 0.0..4.0, 0.25, { mode == Mode.SURPRISE })
    private val disableOnFinish by setting("DisableOnFinish", true, { mode == Mode.CONSTANT }, description = "Disable when no more holes")
    private val holeType by setting("Hole Type", HoleType.BOTH)
    private val legitPlace by setting("Legit Place", false)
    private val spoofHotbar by setting("Spoof Hotbar", true)
    private val hand by setting("Hand", Hand.MAIN)
    private val debug by setting("debug", false)
    private val render by setting("Render", false, { debug })
    private val statusMessage by setting("Status Messages", false, { debug })
    private val visibleCheck by setting("VisibleCheck", false)
    private val rotate by setting("Rotate", true, { debug })

    @Suppress("UNUSED")
    private enum class Hand(val enumHand: EnumHand) {
        MAIN(EnumHand.MAIN_HAND),
        OFF_HAND(EnumHand.OFF_HAND)
    }
    private enum class Mode {
        CONSTANT,
        SURPRISE
    }
    @Suppress("UNUSED")
    private enum class Obby(val block: Block) {
        OBSIDIAN(Blocks.OBSIDIAN),
        COBWEB(Blocks.WEB)
    }

    private enum class HoleType {
        OBSIDIAN,
        BEDROCK,
        BOTH
    }

    private val renderer = ESPRenderer()
    private val timer = TickTimer()
    private val cached = ArrayList<Triple<AxisAlignedBB, ColorHolder, Int>>()
    private var placePacket: CPacketPlayerTryUseItemOnBlock? = null
    init {
        safeListener<TickEvent.ClientTickEvent> {
            if(!isOnTopPriority) return@safeListener
            defaultScope.launch {
                for (x in -range..range) for (y in -range..range) for (z in -range..range) {
                    if (x == 0 && y == 0 && z == 0) continue
                    val playerPos = player.positionVector.toBlockPos()
                    var pos = playerPos.add(x, y, z)
                    val holeType = checkHole(pos)
                    if (holeType == SurroundUtils.HoleType.NONE) continue
                    if (shouldFill()) {
                        if(render) cached.add(Triple(AxisAlignedBB(pos), ColorHolder(255, 0, 0, 125), GeometryMasks.Quad.DOWN))

                        val placeInfo = getNeighbour(pos, 1)
                        if (placeInfo != null) {
                            val slot = player.hotbarSlots.firstBlock(obby.block) ?: return@launch
                            if (!isHoldingObby) if(spoofHotbar) spoofHotbar(slot.hotbarSlot) else swapToSlot(slot.hotbarSlot)
                            val vec = (if(visibleCheck) getClosestVisibleSide(pos) else EnumFacing.UP)?.let { it -> getHitVec(pos, it) } ?: continue
                            val rotations: Any = if(legitPlace) getLegitRotations(vec) else getRotationTo(vec)
                            var rotationPacket: Any? = null
                            when(rotations){
                                is FloatArray -> {
                                    rotationPacket =
                                        CPacketPlayer.Rotation(
                                            rotations[0],
                                            rotations[1],
                                            player.onGround
                                        )
                                }
                                is Vec2f -> {
                                    rotationPacket =
                                        CPacketPlayer.PositionRotation(
                                        player.posX,
                                        player.posY,
                                        player.posZ,
                                        rotations.x,
                                        rotations.y,
                                        player.onGround
                                    )
                                }
                            }
                            placePacket =
                                CPacketPlayerTryUseItemOnBlock(
                                    placeInfo.pos,
                                    placeInfo.side,
                                    hand.enumHand,
                                    placeInfo.hitVecOffset.x.toFloat(),
                                    placeInfo.hitVecOffset.y.toFloat(),
                                    placeInfo.hitVecOffset.z.toFloat()
                                )
                            val range = AxisAlignedBB(
                                player.posX - playerRange,
                                player.posY - playerRange,
                                player.posZ - playerRange,
                                player.posX + playerRange,
                                player.posY + playerRange,
                                player.posZ + playerRange
                            )
                            val closestPlayer =
                                player.world.getEntitiesWithinAABB(EntityPlayer::class.java, range)
                                    .toList().asSequence()
                                    .filter { it != null && !it.isDead && it.health > 0.0f }
                                    .filter { it != player && it != mc.renderViewEntity }
                                    .filter { PlayerList.friend || !FriendManager.isFriend(it.name) }.firstOrNull()
                                    ?: continue
                            if(debug && statusMessage) MessageSendHelper.sendChatMessage("${player.distanceTo(closestPlayer.position.toVec3d())}")
                            if(player.distanceTo(closestPlayer.position.toVec3d()) > fillPlayerRange) continue
                            val playerIsNotInsideTheHole = closestPlayer.position != pos
                            when(mode){
                                Mode.SURPRISE -> {
                                    if(debug && statusMessage) MessageSendHelper.sendChatMessage("${closestPlayer.distanceTo(pos.toVec3d())}")
                                    if (player.distanceTo(closestPlayer.position.toVec3d()) <= fillPlayerRange &&
                                        playerIsNotInsideTheHole
                                    ) {
                                        CombatSetting.setPaused(true)
                                        player.swingArm(hand.enumHand)
                                        if (rotate) (rotationPacket as? Packet<*>)?.let { it -> connection.sendPacket(it) }
                                        placePacket?.let { it -> connection.sendPacket(it) }
                                        resetHotbar()
                                        CombatSetting.setPaused(false)
                                        placePacket = null
                                        if(render) cached.clear()
                                    }
                                }
                                Mode.CONSTANT -> {
                                    CombatSetting.setPaused(true)
                                    player.swingArm(hand.enumHand)
                                    if (rotate) (rotationPacket as? Packet<*>)?.let { it -> connection.sendPacket(it) }
                                    placePacket?.let { it -> connection.sendPacket(it) }
                                    resetHotbar()
                                    CombatSetting.setPaused(false)
                                    placePacket = null
                                    if(render) cached.clear()
                                }
                            }
                        }
                    }
                }
                if (debug && statusMessage) MessageSendHelper.sendChatMessage("Filled all holes")
                if (disableOnFinish && mode == Mode.CONSTANT) disable()
            }
        }
        safeListener<RenderWorldEvent>(69420) {
            if (!render) return@safeListener
            if (timer.tick(133L)) { // Avoid running this on a tick
                updateRenderer()
            }
            renderer.render(false)
        }
    }
    private val SafeClientEvent.isHoldingObby
        get() = isObby(player.heldItemMainhand) || isObby(player.serverSideItem)
    private val isOnTopPriority
        get() = CombatManager.isOnTopPriority(HoleFiller)

    private fun isObby(itemStack: ItemStack) =
        itemStack.item.block == Blocks.OBSIDIAN || itemStack.item.block == Blocks.WEB

    private fun shouldFill() =
        holeType == HoleType.OBSIDIAN || holeType == HoleType.BEDROCK || holeType == HoleType.BOTH

    private fun updateRenderer() {
        renderer.aFilled = 30
        renderer.aOutline = 128
        renderer.replaceAll(cached)
    }
    private fun getLegitRotations(vec: Vec3d): FloatArray {
        val eyesPos: Vec3d = mc.player.getPositionEyes(1.0f)
        val diffX = vec.x - eyesPos.x
        val diffY = vec.y - eyesPos.y
        val diffZ = vec.z - eyesPos.z
        val diffXZ = sqrt(diffX * diffX + diffZ * diffZ)
        val yaw = Math.toDegrees(atan2(diffZ, diffX)).toFloat() - 90.0f
        val pitch = (-Math.toDegrees(atan2(diffY, diffXZ))).toFloat()
        return floatArrayOf(mc.player.rotationYaw + MathHelper.wrapDegrees(yaw - mc.player.rotationYaw), mc.player.rotationPitch + MathHelper.wrapDegrees(pitch - mc.player.rotationPitch))
    }
}
