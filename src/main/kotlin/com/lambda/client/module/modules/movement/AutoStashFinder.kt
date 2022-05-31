package com.lambda.client.module.modules.movement

import baritone.api.BaritoneAPI
import baritone.api.utils.BetterBlockPos
import com.lambda.client.event.events.RenderWorldEvent
import com.lambda.client.event.listener.listener
import com.lambda.client.manager.managers.PlayerPacketManager.sendPlayerPacket
import com.lambda.client.module.Category
import com.lambda.client.module.Module
import com.lambda.client.module.modules.movement.ElytraBotModule.setting
import com.lambda.client.util.color.ColorHolder
import com.lambda.client.util.graphics.ESPRenderer
import com.lambda.client.util.math.RotationUtils.getRotationTo
import com.lambda.client.util.math.VectorUtils
import com.lambda.client.util.math.VectorUtils.toVec3d
import com.lambda.client.util.sendChatMessage
import com.lambda.client.util.text.MessageSendHelper
import com.lambda.client.util.threads.runSafeR
import com.lambda.client.util.threads.safeListener
import net.minecraft.init.Items
import net.minecraft.item.ItemStack
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraftforge.client.event.InputUpdateEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import java.awt.Color

object AutoStashFinder : Module(
    name = "AutoStashFinder",
    description = "Find stash(es) while afking",
    category = Category.MOVEMENT
) {
    lateinit var zone: BlockPos
    val _z = ArrayList<BlockPos>()
    var foundStashes = 0
    private val renderer = ESPRenderer()
    private var path = ArrayList<BlockPos>()
    enum class RotationMode {
        OFF, SPOOF, VIEW_LOCK
    }
    private val randomDirection by setting("Random directions", true)
    private val minY by setting("Minimum Y", 128f, 80.0f..256.0f, 1.0f)
    private val renderingPath by setting("Rendering path", false)
    private val aStarRadius by setting("AStarRadius", 3f, 1f..10f, 0.5f)
    private val aStarLoops by setting("aStarLoops", 500, 1..1000, 1)
    private val interacting by setting("Rotation Mode", RotationMode.VIEW_LOCK)



    init {
        onEnable {
            runSafeR {
                if(randomDirection){
                    val positions = arrayOf(BlockPos(1, 0, 0), BlockPos(-1, 0, 0), BlockPos(0, 0, 1), BlockPos(0, 0, -1),
                        BlockPos(1, 0, 1), BlockPos(-1, 0, -1), BlockPos(-1, 0, 1), BlockPos(1, 0, -1),
                        BlockPos(0, -1, 0), BlockPos(0, 1, 0))
                    repeat(4){
                        var xPlayer = mc.player.position.x + (-30000256..30000256).random()
                        if(xPlayer !in -30000256..30000256) xPlayer /= (xPlayer / 2)
                        var zPlayer = mc.player.position.x + (-30000256..30000256).random()
                        if(zPlayer !in -30000256..30000256) zPlayer /= (zPlayer / 2)
                        val surface = xPlayer*zPlayer
                        val block = BlockPos(xPlayer.toDouble(), minY.toDouble(), zPlayer.toDouble())
                        val unt = block.x == surface/xPlayer && block.z == surface/zPlayer

                        _z.add(BlockPos(xPlayer.toDouble(), minY.toDouble(), zPlayer.toDouble()))

                    }
                    path = AStar.generatePath(mc, mc.player.position, _z.last(), positions, VectorUtils.getBlockPosInSphere(Vec3d.ZERO, aStarRadius), aStarLoops)
                }
                //todo Add support for specified positions
            }
        }
        onDisable {
            _z.clear()
            MessageSendHelper.sendChatMessage("Disabling, found $foundStashes stash(es)")
        }
        safeListener<RenderWorldEvent>(69420){
            if(path.isNotEmpty()){
                if(renderingPath){
                    renderer.aOutline = 50
                    renderer.thickness = 2F
                    path.forEach {
                        val _it = BlockPos(it.x.toDouble(), (it.y - 1.5), it.z.toDouble())
                        //println(mc.player.getDistanceSq(it))
                        renderer.add(_it, ColorHolder(Color.RED))
                    }
                    renderer.render(true)
                }

            }
        }
        listener<InputUpdateEvent>(6969) {
            if(path.isNotEmpty()){



                if(mc.player.position.y < _z.first().y) {
                    it.movementInput.jump
                }


                it.movementInput.moveForward = 1.0f
            }
        }
        safeListener<TickEvent.ClientTickEvent> {
            //Check if there is an elytra equipped if not then equip it or toggle off if no elytra in inventory
            if (player.inventory.armorInventory[2].item != Items.ELYTRA ||
                isItemBroken(player.inventory.armorInventory[2])) {
                MessageSendHelper.sendChatMessage("$chatName You need an elytra.")
                disable()
                return@safeListener
            }
        }
    }
    private fun isItemBroken(itemStack: ItemStack): Boolean { // (100 * damage / max damage) >= (100 - 70)
        return if (itemStack.maxDamage == 0) {
            false
        } else {
            itemStack.maxDamage - itemStack.itemDamage <= 3
        }
    }
    private fun rotateUpdate(blockPos: BlockPos){
        if (path.isNotEmpty()) {
            when (interacting) {
                RotationMode.SPOOF -> {
                    sendPlayerPacket {
                        rotate(getRotationTo(mc.player.position.toVec3d(), blockPos.toVec3d()))
                    }
                }
                RotationMode.VIEW_LOCK -> {
                    mc.player?.rotationYaw = getRotationTo(mc.player.position.toVec3d(), blockPos.toVec3d()).x
                    mc.player?.rotationPitch = getRotationTo(mc.player.position.toVec3d(), blockPos.toVec3d()).y
                }
                else -> {
                    // RotationMode.OFF
                }
            }
        }

    }
}