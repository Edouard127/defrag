package com.lambda.client.gui.hudgui.elements.world

import com.lambda.client.event.SafeClientEvent
import com.lambda.client.event.events.RenderOverlayEvent
import com.lambda.client.event.events.RenderWorldEvent
import com.lambda.client.gui.hudgui.LabelHud
import com.lambda.client.gui.hudgui.elements.world.EntityList.setting
import com.lambda.client.module.AbstractModule
import com.lambda.client.module.modules.render.MapPreview.scale
import com.lambda.client.module.modules.render.SignESP
import com.lambda.client.module.modules.render.SignESP.setting
import com.lambda.client.setting.settings.impl.collection.CollectionSetting
import com.lambda.client.util.AsyncCachedValue
import com.lambda.client.util.EntityUtils.isHostile
import com.lambda.client.util.EntityUtils.isNeutral
import com.lambda.client.util.EntityUtils.isPassive
import com.lambda.client.util.color.ColorHolder
import com.lambda.client.util.graphics.GlStateUtils
import com.lambda.client.util.graphics.ProjectionUtils
import com.lambda.client.util.graphics.font.FontRenderAdapter
import com.lambda.client.util.graphics.font.TextComponent
import com.lambda.client.util.items.originalName
import com.lambda.client.util.math.VectorUtils.toVec3dCenter
import com.lambda.client.util.threads.runSafe
import com.lambda.client.util.threads.safeListener
import net.minecraft.entity.Entity
import net.minecraft.entity.item.*
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.projectile.EntityEgg
import net.minecraft.entity.projectile.EntitySnowball
import net.minecraft.entity.projectile.EntityWitherSkull
import net.minecraft.tileentity.TileEntitySign
import net.minecraft.util.math.BlockPos
import net.minecraft.util.text.ITextComponent
import org.lwjgl.opengl.GL11
import java.util.*

internal object ESPFagGou : LabelHud(
    name = "GouDaddy",
    category = Category.WORLD,
    description = "List of Signs because gou daddy asked for"
) {

    private val textScale by setting("TextScale", 1f, 0.1f..10f, 0.1f)

    private val tracers by setting("Tracers", false)

    private val wordTracers by setting("WordTracers", true, { tracers })
    private val maxEntries by setting("Max Entries", 8, 4..96, 1)
    private var remainingEntries = 0
    val map = TreeMap<String, Int>()
    var sex: String = ""
    private val cacheMap by AsyncCachedValue(50L) {
        fun render(texts: Array<ITextComponent>) {



            val rowsToDraw = ArrayList<String>()

            try {
                for (text in texts) {
                    rowsToDraw.add(text.unformattedText)
                }
            } catch (e: NullPointerException) { // todo: figure out why this sometimes throws a npe even though it shouldn't
                // commented cuz of log spam kek
                // e.printStackTrace()
            }

            rowsToDraw.forEachIndexed { _, text ->
                displayText.addLine(text)
            }
        }

        safeListener<RenderOverlayEvent> {
            for (tile in mc.world.loadedTileEntityList) {
                if (tile is TileEntitySign) {
                    render(tile.signText)
                }
            }
        }

    }
    fun render(texts: Array<ITextComponent>) {



        val rowsToDraw = ArrayList<String>()

        try {
            for (text in texts) {
                rowsToDraw.add(text.unformattedText)
            }
        } catch (e: NullPointerException) { // todo: figure out why this sometimes throws a npe even though it shouldn't
            // commented cuz of log spam kek
            // e.printStackTrace()
        }

        rowsToDraw.forEachIndexed { _, text ->
            displayText.addLine(text)
        }
    }
    override fun SafeClientEvent.updateText() {
        for (tile in mc.world.loadedTileEntityList) {
            if (tile is TileEntitySign) {
                render(tile.signText)
            }
        }
    }


}


