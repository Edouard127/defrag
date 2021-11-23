package com.lambda.client.module.modules.render

import com.lambda.client.event.events.RenderOverlayEvent
import com.lambda.client.event.events.RenderWorldEvent
import com.lambda.client.module.Category
import com.lambda.client.module.Module
import com.lambda.client.setting.settings.impl.collection.CollectionSetting
import com.lambda.client.util.color.ColorHolder
import com.lambda.client.util.graphics.ESPRenderer
import com.lambda.client.util.graphics.GlStateUtils
import com.lambda.client.util.graphics.ProjectionUtils
import com.lambda.client.util.graphics.font.FontRenderAdapter
import com.lambda.client.util.math.VectorUtils.toVec3dCenter
import com.lambda.client.util.threads.safeListener
import net.minecraft.tileentity.TileEntitySign
import net.minecraft.util.math.BlockPos
import net.minecraft.util.text.ITextComponent
import org.lwjgl.opengl.GL11
import java.util.*

object SignESP : Module(
    name = "SignESP",
    description = "Shows what signs say kinda like with nametags",
    category = Category.RENDER
) {
    private val textScale by setting("TextScale", 1f, 0.1f..10f, 0.1f)

    private val tracers by setting("Tracers", false)
    private val tracerWidth by setting("TracerWidth", 2f, 1f..10f, 0.1f, { tracers })
    private val tracerAlpha by setting("TracerAlpha", 255, 0..255, 1, { tracers })
    private val wordTracers by setting("WordTracers", true, { tracers })
    private val wordList = setting(CollectionSetting("WordList", mutableListOf("SalC1"), { false }))
    private val ignoreCase = setting("IgnoreCase", true, { wordTracers })

    private val showPosition by setting("ShowPosition", false)
    private val showDistance by setting("ShowDistance", false)
    private val yOffSet by setting("YOffset", 0.0, -5.0..5.0, 0.1)

    private val renderer = ESPRenderer()

    init {
        safeListener<RenderOverlayEvent> {
            for (tile in mc.world.loadedTileEntityList) {
                if (tile is TileEntitySign) {
                    render(tile.pos, tile.signText)
                }
            }
        }

        safeListener<RenderWorldEvent> {
            for (tile in mc.world.loadedTileEntityList) {
                if (tile is TileEntitySign) {
                    renderTracers(tile.pos, tile.signText)
                }
            }
        }
    }

    private fun render(pos: BlockPos, texts: Array<ITextComponent>) {

        // text nametag thingies
        GlStateUtils.rescaleActual()
        GL11.glPushMatrix()
        val vecCenterPosShifted = pos.toVec3dCenter().add(0.0, yOffSet, 0.0)
        val screenPos = ProjectionUtils.toScreenPos(vecCenterPosShifted)
        GL11.glTranslated(screenPos.x, screenPos.y, 0.0)
        GL11.glScalef(textScale * 2, textScale * 2, 0f)

        val color = ColorHolder(255, 255, 255, 255)

        val rowsToDraw = ArrayList<String>()

        try {
            for (text in texts) {
                rowsToDraw.add(text.unformattedText)
            }
        } catch (e: NullPointerException) { // todo: figure out why this sometimes throws a npe even though it shouldn't
            // commented cuz of log spam kek
            // e.printStackTrace()
        }

        if (showDistance) {
            rowsToDraw.add("distance: ${pos.toVec3dCenter().distanceTo(mc.player.position.toVec3dCenter())}")
        }
        if (showPosition) {
            rowsToDraw.add("coordinates: ${pos.toVec3dCenter()}")
        }

        rowsToDraw.forEachIndexed { index, text ->
            val halfWidth = FontRenderAdapter.getStringWidth(text) / -2.0f
            FontRenderAdapter.drawString(text, halfWidth, (FontRenderAdapter.getFontHeight() + 2.0f) * index, color = color)
        }
        GlStateUtils.rescaleMc()
        GL11.glPopMatrix()
    }

    private fun renderTracers(pos: BlockPos, texts: Array<ITextComponent>) {
        val rowsToDraw = ArrayList<String>()

        try {
            for (text in texts) {
                rowsToDraw.add(text.unformattedText)
            }
        } catch (e: NullPointerException) {
            // commented cuz of log spam kek
            // e.printStackTrace()
        }
        GL11.glPushMatrix()
        // tracers
        if (tracers) {
            renderer.aTracer = tracerAlpha
            renderer.thickness = tracerWidth

            if (wordTracers) {
                var flagged = false
                for (text in rowsToDraw) {
                    for (match in wordList) {

                        if ((ignoreCase.value && text.lowercase(Locale.getDefault()).contains(match.lowercase(Locale.getDefault()))) || text.contains(match)) {
                            flagged = true
                        }
                    }
                }

                if (flagged) {
                    renderer.add(pos, ColorHolder(255, 255, 255, tracerAlpha))
                }
            } else {
                renderer.add(pos, ColorHolder(255, 255, 255, tracerAlpha))
            }
            renderer.render(true)
        }

        GL11.glPopMatrix()
    }
}