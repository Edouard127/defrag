package com.lambda.client.module.modules.movement

import com.lambda.client.event.events.RenderWorldEvent
import com.lambda.client.module.Category
import com.lambda.client.module.Module
import com.lambda.client.util.color.ColorHolder
import com.lambda.client.util.graphics.ESPRenderer
import com.lambda.client.util.sendChatMessage
import com.lambda.client.util.text.MessageSendHelper
import com.lambda.client.util.threads.runSafeR
import com.lambda.client.util.threads.safeListener
import com.lambda.event.listener.listener
import net.minecraft.init.Items
import net.minecraft.item.ItemStack
import net.minecraft.util.math.BlockPos
import net.minecraftforge.client.event.InputUpdateEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import java.awt.Color

object AutoStashFinder : Module(
    name = "AutoStashFinder",
    description = "Find stashs while afking",
    category = Category.MOVEMENT
) {
    lateinit var zone: BlockPos
    val _z = ArrayList<BlockPos>()
    var foundStashes = 0
    private val renderer = ESPRenderer()

    private val randomDirection by setting("Random directions", true)
    private val minY by setting("Minimum Y", 128f, 80.0f..256.0f, 1.0f)
    private val renderingPath by setting("Rendering path", false)



    init {
        onEnable {
            runSafeR {
                if(randomDirection){
                    repeat(4){
                        var xPlayer = mc.player.position.x + (-30000256..30000256).random()
                        if(xPlayer !in -30000256..30000256) xPlayer /= (xPlayer / 2)
                        var zPlayer = mc.player.position.x + (-30000256..30000256).random()
                        if(zPlayer !in -30000256..30000256) zPlayer /= (zPlayer / 2)
                        _z.add(BlockPos(xPlayer.toDouble(), minY.toDouble(), zPlayer.toDouble()))
                    }
                }
                //todo Add support for specified positions
            }
        }
        onDisable {
            _z.clear()
            MessageSendHelper.sendChatMessage("Disabling, found $foundStashes stash(es)")
        }
        safeListener<RenderWorldEvent>(69420){
            if(renderingPath){
                renderer.aOutline = 50
                renderer.thickness = 2F
                _z.forEach {
                    val _it = BlockPos(it.x.toDouble(), (it.y - 1.5), it.z.toDouble())
                    //println(mc.player.getDistanceSq(it))
                    renderer.add(_it, ColorHolder(Color.RED))
                }
                renderer.render(true)
            }
        }
        listener<InputUpdateEvent>(6969) {
            if(_z.isNotEmpty()){


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
}