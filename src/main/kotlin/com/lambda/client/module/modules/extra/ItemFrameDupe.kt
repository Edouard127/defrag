package com.lambda.client.module.modules.extra

import com.lambda.client.event.SafeClientEvent
import com.lambda.client.event.events.RenderWorldEvent
import com.lambda.client.module.Category
import com.lambda.client.module.Module
import com.lambda.client.util.TickTimer
import com.lambda.client.util.color.ColorHolder
import com.lambda.client.util.graphics.ESPRenderer
import com.lambda.client.util.math.RotationUtils.faceEntityClosest
import com.lambda.client.util.math.VectorUtils.toVec3d
import com.lambda.client.util.text.MessageSendHelper
import com.lambda.client.util.threads.safeListener
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityItemFrame
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraftforge.fml.common.gameevent.PlayerEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import java.awt.Color
import kotlin.reflect.KProperty


object ItemFrameDupe : Module(
    name = "ItemFrameDupe",
    description = "Dupe",
    category = Category.MISC
) {
    private var i = 0
    private val timer = TickTimer()
    //private val renderer = ESPRenderer()
    private val delay by setting("Delay", 100L, 10L..1000L, 2L)



    init {
        onEnable {
            val range = AxisAlignedBB(mc.player.posX - 4, mc.player.posY - 4, mc.player.posZ - 4, mc.player.posX + 4, mc.player.posY + 4, mc.player.posZ + 4)
            val entities = mc.world.getEntitiesWithinAABB(EntityItemFrame::class.java, range)
            safeListener<TickEvent.ClientTickEvent> {
                if(player.positionVector.distanceTo(entities[0].positionVector) > 4){
                    MessageSendHelper.sendChatMessage("No item frame detected, disabling...")
                    disable()
                }
                if(timer.tick(delay)){
                    try {
                        swapToSlot(i)
                        faceEntityClosest(entities[0])
                        attack(entities[0])
                    } catch (e: Exception) {}
                    i++
                    if(i > 8) i = 0
                }
            }
            /**I need to register the event**/
/*
            safeListener<PlayerEvent.ItemPickupEvent> {
                println("${it.originalEntity.displayName} ${entities[0].displayName}")
                if(it.originalEntity.displayName == entities[0].displayName) MessageSendHelper.sendChatMessage("Duped")
            }
 */
            /*safeListener<RenderWorldEvent>(69420){
                    renderer.aOutline = 50
                    renderer.thickness = 2F
                    entities.forEach {
                        val _it = BlockPos(it.posX, (it.posY - 1.5), it.posZ)
                        renderer.add(_it, ColorHolder(Color.RED))
                    }
                    renderer.render(true)
            }*/

        }
    }
    fun SafeClientEvent.swapToSlot(slot: Int) {
        if (slot !in 0..8) return
        player.inventory.currentItem = slot
        playerController.updateController()
    }
    private fun SafeClientEvent.attack(entity: Entity) {
        playerController.attackEntity(player, entity)
        player.swingArm(EnumHand.MAIN_HAND)
    }
}

