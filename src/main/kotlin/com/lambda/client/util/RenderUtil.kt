package com.lambda.client.util

import com.lambda.client.module.modules.client.ClickGUI.radius
import com.lambda.client.util.GLUProjection.Companion.instance
import com.lambda.client.util.ColorUtil.Companion.toRGBA
import com.lambda.client.util.RenderUtil.camera
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.culling.ICamera
import net.minecraft.client.renderer.culling.Frustum
import org.lwjgl.opengl.GL11
import java.nio.FloatBuffer
import org.lwjgl.BufferUtils
import java.nio.IntBuffer
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.gui.Gui
import net.minecraft.util.math.BlockPos
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.BufferBuilder
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import net.minecraft.client.renderer.GlStateManager.SourceFactor
import net.minecraft.client.renderer.GlStateManager.DestFactor
import net.minecraft.client.renderer.RenderGlobal
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.world.IBlockAccess
import org.lwjgl.util.glu.Sphere
import net.minecraft.util.ResourceLocation
import org.lwjgl.util.glu.Disk
import net.minecraft.client.model.ModelBiped
import org.lwjgl.util.glu.GLU
import net.minecraft.client.renderer.OpenGlHelper
import net.minecraft.client.shader.Framebuffer
import org.lwjgl.opengl.EXTFramebufferObject
import net.minecraft.block.material.Material
import net.minecraft.entity.Entity
import net.minecraft.util.math.Vec2f
import org.lwjgl.opengl.Display
import java.awt.Color
import java.util.*
import kotlin.math.roundToInt

object RenderUtil {
    private val mc: Minecraft? = null
    var itemRender = mc!!.renderItem
    var camera: ICamera = Frustum()
    private val frustrum = Frustum()
    private var depth = GL11.glIsEnabled(2896)
    private var texture = GL11.glIsEnabled(3042)
    private var clean = GL11.glIsEnabled(3553)
    private var bind = GL11.glIsEnabled(2929)
    private var override = GL11.glIsEnabled(2848)
    private val screenCoords = BufferUtils.createFloatBuffer(3)
    private val viewport = BufferUtils.createIntBuffer(16)
    private val modelView = BufferUtils.createFloatBuffer(16)
    private val projection = BufferUtils.createFloatBuffer(16)
    fun updateModelViewProjectionMatrix() {
        GL11.glGetFloat(2982, modelView as FloatBuffer)
        GL11.glGetFloat(2983, projection as FloatBuffer)
        GL11.glGetInteger(2978, viewport as IntBuffer)
        val res = ScaledResolution(Minecraft.getMinecraft())
        instance!!.updateMatrices(
            viewport,
            modelView,
            projection,
            (res.scaledWidth.toFloat() / Minecraft.getMinecraft().displayWidth.toFloat()).toDouble(),
            (res.scaledHeight.toFloat() / Minecraft.getMinecraft().displayHeight.toFloat()).toDouble()
        )
    }

    fun drawRectangleCorrectly(x: Int, y: Int, w: Int, h: Int, color: Int) {
        GL11.glLineWidth(1.0f)
        Gui.drawRect(x, y, (x + w), (y + h), color)
    }

    fun drawWaypointImage(
        pos: BlockPos,
        projection: GLUProjection.Projection,
        color: Color?,
        name: String,
        rectangle: Boolean,
        rectangleColor: Color?
    ) {
        GlStateManager.pushMatrix()
        GlStateManager.translate(projection.x, projection.y, 0.0)
        val text = "$name: " + mc!!.player.getDistance(
            pos.x.toDouble(),
            pos.y.toDouble(),
            pos.z.toDouble()
        ).roundToInt() + "M"
        GlStateManager.translate(-projection.x, -projection.y, 0.0)
        GlStateManager.popMatrix()
    }

    fun interpolateAxis(bb: AxisAlignedBB): AxisAlignedBB {
        return AxisAlignedBB(
            bb.minX - mc!!.renderManager.viewerPosX,
            bb.minY - mc.renderManager.viewerPosY,
            bb.minZ - mc.renderManager.viewerPosZ,
            bb.maxX - mc.renderManager.viewerPosX,
            bb.maxY - mc.renderManager.viewerPosY,
            bb.maxZ - mc.renderManager.viewerPosZ
        )
    }

    fun drawTexturedRect(x: Int, y: Int, textureX: Int, textureY: Int, width: Int, height: Int, zLevel: Int) {
        val tessellator = Tessellator.getInstance()
        val BufferBuilder2 = tessellator.buffer
        BufferBuilder2.begin(7, DefaultVertexFormats.POSITION_TEX)
        BufferBuilder2.pos((x + 0).toDouble(), (y + height).toDouble(), zLevel.toDouble()).tex(
            ((textureX + 0).toFloat() * 0.00390625f).toDouble(),
            ((textureY + height).toFloat() * 0.00390625f).toDouble()
        ).endVertex()
        BufferBuilder2.pos((x + width).toDouble(), (y + height).toDouble(), zLevel.toDouble()).tex(
            ((textureX + width).toFloat() * 0.00390625f).toDouble(),
            ((textureY + height).toFloat() * 0.00390625f).toDouble()
        ).endVertex()
        BufferBuilder2.pos((x + width).toDouble(), (y + 0).toDouble(), zLevel.toDouble()).tex(
            ((textureX + width).toFloat() * 0.00390625f).toDouble(),
            ((textureY + 0).toFloat() * 0.00390625f).toDouble()
        ).endVertex()
        BufferBuilder2.pos((x + 0).toDouble(), (y + 0).toDouble(), zLevel.toDouble()).tex(
            ((textureX + 0).toFloat() * 0.00390625f).toDouble(),
            ((textureY + 0).toFloat() * 0.00390625f).toDouble()
        ).endVertex()
        tessellator.draw()
    }

    fun drawOpenGradientBox(pos: BlockPos?, startColor: Color, endColor: Color, height: Double) {
        for (face in EnumFacing.values()) {
            if (face == EnumFacing.UP) continue
            drawGradientPlane(pos, face, startColor, endColor, height)
        }
    }

    fun drawClosedGradientBox(pos: BlockPos?, startColor: Color, endColor: Color, height: Double) {
        for (face in EnumFacing.values()) {
            drawGradientPlane(pos, face, startColor, endColor, height)
        }
    }

    fun drawTricolorGradientBox(pos: BlockPos?, startColor: Color, midColor: Color, endColor: Color) {
        for (face in EnumFacing.values()) {
            if (face == EnumFacing.UP) continue
            drawGradientPlane(pos, face, startColor, midColor, true, false)
        }
        for (face in EnumFacing.values()) {
            if (face == EnumFacing.DOWN) continue
            drawGradientPlane(pos, face, midColor, endColor, true, true)
        }
    }

    fun drawGradientPlane(
        pos: BlockPos?,
        face: EnumFacing,
        startColor: Color,
        endColor: Color,
        half: Boolean,
        top: Boolean
    ) {
        val tessellator = Tessellator.getInstance()
        val builder = tessellator.buffer
        val iblockstate = mc!!.world.getBlockState(pos)
        val bb = iblockstate.getSelectedBoundingBox(mc.world as World, pos).grow(0.002)
        val red = startColor.red.toFloat() / 255.0f
        val green = startColor.green.toFloat() / 255.0f
        val blue = startColor.blue.toFloat() / 255.0f
        val alpha = startColor.alpha.toFloat() / 255.0f
        val red1 = endColor.red.toFloat() / 255.0f
        val green1 = endColor.green.toFloat() / 255.0f
        val blue1 = endColor.blue.toFloat() / 255.0f
        val alpha1 = endColor.alpha.toFloat() / 255.0f
        var x1 = 0.0
        var y1 = 0.0
        var z1 = 0.0
        var x2 = 0.0
        var y2 = 0.0
        var z2 = 0.0
        if (face == EnumFacing.DOWN) {
            x1 = bb.minX
            x2 = bb.maxX
            y1 = bb.minY + if (top) 0.5 else 0.0
            y2 = bb.minY + if (top) 0.5 else 0.0
            z1 = bb.minZ
            z2 = bb.maxZ
        } else if (face == EnumFacing.UP) {
            x1 = bb.minX
            x2 = bb.maxX
            y1 = bb.maxY / (if (half) 2 else 1).toDouble()
            y2 = bb.maxY / (if (half) 2 else 1).toDouble()
            z1 = bb.minZ
            z2 = bb.maxZ
        } else if (face == EnumFacing.EAST) {
            x1 = bb.maxX
            x2 = bb.maxX
            y1 = bb.minY + if (top) 0.5 else 0.0
            y2 = bb.maxY / (if (half) 2 else 1).toDouble()
            z1 = bb.minZ
            z2 = bb.maxZ
        } else if (face == EnumFacing.WEST) {
            x1 = bb.minX
            x2 = bb.minX
            y1 = bb.minY + if (top) 0.5 else 0.0
            y2 = bb.maxY / (if (half) 2 else 1).toDouble()
            z1 = bb.minZ
            z2 = bb.maxZ
        } else if (face == EnumFacing.SOUTH) {
            x1 = bb.minX
            x2 = bb.maxX
            y1 = bb.minY + if (top) 0.5 else 0.0
            y2 = bb.maxY / (if (half) 2 else 1).toDouble()
            z1 = bb.maxZ
            z2 = bb.maxZ
        } else if (face == EnumFacing.NORTH) {
            x1 = bb.minX
            x2 = bb.maxX
            y1 = bb.minY + if (top) 0.5 else 0.0
            y2 = bb.maxY / (if (half) 2 else 1).toDouble()
            z1 = bb.minZ
            z2 = bb.minZ
        }
        GlStateManager.pushMatrix()
        GlStateManager.disableDepth()
        GlStateManager.disableTexture2D()
        GlStateManager.enableBlend()
        GlStateManager.disableAlpha()
        GlStateManager.depthMask(false)
        builder.begin(5, DefaultVertexFormats.POSITION_COLOR)
        if (face == EnumFacing.EAST || face == EnumFacing.WEST || face == EnumFacing.NORTH || face == EnumFacing.SOUTH) {
            builder.pos(x1, y1, z1).color(red, green, blue, alpha).endVertex()
            builder.pos(x1, y1, z1).color(red, green, blue, alpha).endVertex()
            builder.pos(x1, y1, z1).color(red, green, blue, alpha).endVertex()
            builder.pos(x1, y1, z2).color(red, green, blue, alpha).endVertex()
            builder.pos(x1, y2, z1).color(red1, green1, blue1, alpha1).endVertex()
            builder.pos(x1, y2, z2).color(red1, green1, blue1, alpha1).endVertex()
            builder.pos(x1, y2, z2).color(red1, green1, blue1, alpha1).endVertex()
            builder.pos(x1, y1, z2).color(red, green, blue, alpha).endVertex()
            builder.pos(x2, y2, z2).color(red1, green1, blue1, alpha1).endVertex()
            builder.pos(x2, y1, z2).color(red, green, blue, alpha).endVertex()
            builder.pos(x2, y1, z2).color(red, green, blue, alpha).endVertex()
            builder.pos(x2, y1, z1).color(red, green, blue, alpha).endVertex()
            builder.pos(x2, y2, z2).color(red1, green1, blue1, alpha1).endVertex()
            builder.pos(x2, y2, z1).color(red1, green1, blue1, alpha1).endVertex()
            builder.pos(x2, y2, z1).color(red1, green1, blue1, alpha1).endVertex()
            builder.pos(x2, y1, z1).color(red, green, blue, alpha).endVertex()
            builder.pos(x1, y2, z1).color(red1, green1, blue1, alpha1).endVertex()
            builder.pos(x1, y1, z1).color(red, green, blue, alpha).endVertex()
            builder.pos(x1, y1, z1).color(red, green, blue, alpha).endVertex()
            builder.pos(x2, y1, z1).color(red, green, blue, alpha).endVertex()
            builder.pos(x1, y1, z2).color(red, green, blue, alpha).endVertex()
            builder.pos(x2, y1, z2).color(red, green, blue, alpha).endVertex()
            builder.pos(x2, y1, z2).color(red, green, blue, alpha).endVertex()
            builder.pos(x1, y2, z1).color(red1, green1, blue1, alpha1).endVertex()
            builder.pos(x1, y2, z1).color(red1, green1, blue1, alpha1).endVertex()
            builder.pos(x1, y2, z2).color(red1, green1, blue1, alpha1).endVertex()
            builder.pos(x2, y2, z1).color(red1, green1, blue1, alpha1).endVertex()
            builder.pos(x2, y2, z2).color(red1, green1, blue1, alpha1).endVertex()
            builder.pos(x2, y2, z2).color(red1, green1, blue1, alpha1).endVertex()
            builder.pos(x2, y2, z2).color(red1, green1, blue1, alpha1).endVertex()
        } else if (face == EnumFacing.UP) {
            builder.pos(x1, y1, z1).color(red1, green1, blue1, alpha1).endVertex()
            builder.pos(x1, y1, z1).color(red1, green1, blue1, alpha1).endVertex()
            builder.pos(x1, y1, z1).color(red1, green1, blue1, alpha1).endVertex()
            builder.pos(x1, y1, z2).color(red1, green1, blue1, alpha1).endVertex()
            builder.pos(x1, y2, z1).color(red1, green1, blue1, alpha1).endVertex()
            builder.pos(x1, y2, z2).color(red1, green1, blue1, alpha1).endVertex()
            builder.pos(x1, y2, z2).color(red1, green1, blue1, alpha1).endVertex()
            builder.pos(x1, y1, z2).color(red1, green1, blue1, alpha1).endVertex()
            builder.pos(x2, y2, z2).color(red1, green1, blue1, alpha1).endVertex()
            builder.pos(x2, y1, z2).color(red1, green1, blue1, alpha1).endVertex()
            builder.pos(x2, y1, z2).color(red1, green1, blue1, alpha1).endVertex()
            builder.pos(x2, y1, z1).color(red1, green1, blue1, alpha1).endVertex()
            builder.pos(x2, y2, z2).color(red1, green1, blue1, alpha1).endVertex()
            builder.pos(x2, y2, z1).color(red1, green1, blue1, alpha1).endVertex()
            builder.pos(x2, y2, z1).color(red1, green1, blue1, alpha1).endVertex()
            builder.pos(x2, y1, z1).color(red1, green1, blue1, alpha1).endVertex()
            builder.pos(x1, y2, z1).color(red1, green1, blue1, alpha1).endVertex()
            builder.pos(x1, y1, z1).color(red1, green1, blue1, alpha1).endVertex()
            builder.pos(x1, y1, z1).color(red1, green1, blue1, alpha1).endVertex()
            builder.pos(x2, y1, z1).color(red1, green1, blue1, alpha1).endVertex()
            builder.pos(x1, y1, z2).color(red1, green1, blue1, alpha1).endVertex()
            builder.pos(x2, y1, z2).color(red1, green1, blue1, alpha1).endVertex()
            builder.pos(x2, y1, z2).color(red1, green1, blue1, alpha1).endVertex()
            builder.pos(x1, y2, z1).color(red1, green1, blue1, alpha1).endVertex()
            builder.pos(x1, y2, z1).color(red1, green1, blue1, alpha1).endVertex()
            builder.pos(x1, y2, z2).color(red1, green1, blue1, alpha1).endVertex()
            builder.pos(x2, y2, z1).color(red1, green1, blue1, alpha1).endVertex()
            builder.pos(x2, y2, z2).color(red1, green1, blue1, alpha1).endVertex()
            builder.pos(x2, y2, z2).color(red1, green1, blue1, alpha1).endVertex()
            builder.pos(x2, y2, z2).color(red1, green1, blue1, alpha1).endVertex()
        } else if (face == EnumFacing.DOWN) {
            builder.pos(x1, y1, z1).color(red, green, blue, alpha).endVertex()
            builder.pos(x1, y1, z1).color(red, green, blue, alpha).endVertex()
            builder.pos(x1, y1, z1).color(red, green, blue, alpha).endVertex()
            builder.pos(x1, y1, z2).color(red, green, blue, alpha).endVertex()
            builder.pos(x1, y2, z1).color(red, green, blue, alpha).endVertex()
            builder.pos(x1, y2, z2).color(red, green, blue, alpha).endVertex()
            builder.pos(x1, y2, z2).color(red, green, blue, alpha).endVertex()
            builder.pos(x1, y1, z2).color(red, green, blue, alpha).endVertex()
            builder.pos(x2, y2, z2).color(red, green, blue, alpha).endVertex()
            builder.pos(x2, y1, z2).color(red, green, blue, alpha).endVertex()
            builder.pos(x2, y1, z2).color(red, green, blue, alpha).endVertex()
            builder.pos(x2, y1, z1).color(red, green, blue, alpha).endVertex()
            builder.pos(x2, y2, z2).color(red, green, blue, alpha).endVertex()
            builder.pos(x2, y2, z1).color(red, green, blue, alpha).endVertex()
            builder.pos(x2, y2, z1).color(red, green, blue, alpha).endVertex()
            builder.pos(x2, y1, z1).color(red, green, blue, alpha).endVertex()
            builder.pos(x1, y2, z1).color(red, green, blue, alpha).endVertex()
            builder.pos(x1, y1, z1).color(red, green, blue, alpha).endVertex()
            builder.pos(x1, y1, z1).color(red, green, blue, alpha).endVertex()
            builder.pos(x2, y1, z1).color(red, green, blue, alpha).endVertex()
            builder.pos(x1, y1, z2).color(red, green, blue, alpha).endVertex()
            builder.pos(x2, y1, z2).color(red, green, blue, alpha).endVertex()
            builder.pos(x2, y1, z2).color(red, green, blue, alpha).endVertex()
            builder.pos(x1, y2, z1).color(red, green, blue, alpha).endVertex()
            builder.pos(x1, y2, z1).color(red, green, blue, alpha).endVertex()
            builder.pos(x1, y2, z2).color(red, green, blue, alpha).endVertex()
            builder.pos(x2, y2, z1).color(red, green, blue, alpha).endVertex()
            builder.pos(x2, y2, z2).color(red, green, blue, alpha).endVertex()
            builder.pos(x2, y2, z2).color(red, green, blue, alpha).endVertex()
            builder.pos(x2, y2, z2).color(red, green, blue, alpha).endVertex()
        }
        tessellator.draw()
        GlStateManager.depthMask(true)
        GlStateManager.disableBlend()
        GlStateManager.enableAlpha()
        GlStateManager.enableTexture2D()
        GlStateManager.enableDepth()
        GlStateManager.popMatrix()
    }

    fun drawGradientPlane(pos: BlockPos?, face: EnumFacing, startColor: Color, endColor: Color, height: Double) {
        val tessellator = Tessellator.getInstance()
        val builder = tessellator.buffer
        val iblockstate = mc!!.world.getBlockState(pos)
        val bb = iblockstate.getSelectedBoundingBox(mc.world as World, pos).grow(0.002)
        val red = startColor.red.toFloat() / 255.0f
        val green = startColor.green.toFloat() / 255.0f
        val blue = startColor.blue.toFloat() / 255.0f
        val alpha = startColor.alpha.toFloat() / 255.0f
        val red1 = endColor.red.toFloat() / 255.0f
        val green1 = endColor.green.toFloat() / 255.0f
        val blue1 = endColor.blue.toFloat() / 255.0f
        val alpha1 = endColor.alpha.toFloat() / 255.0f
        var x1 = 0.0
        var y1 = 0.0
        var z1 = 0.0
        var x2 = 0.0
        var y2 = 0.0
        var z2 = 0.0
        if (face == EnumFacing.DOWN) {
            x1 = bb.minX
            x2 = bb.maxX
            y1 = bb.minY
            y2 = bb.minY
            z1 = bb.minZ
            z2 = bb.maxZ
        } else if (face == EnumFacing.UP) {
            x1 = bb.minX
            x2 = bb.maxX
            y1 = bb.maxY
            y2 = bb.maxY
            z1 = bb.minZ
            z2 = bb.maxZ
        } else if (face == EnumFacing.EAST) {
            x1 = bb.maxX
            x2 = bb.maxX
            y1 = bb.minY
            y2 = bb.maxY
            z1 = bb.minZ
            z2 = bb.maxZ
        } else if (face == EnumFacing.WEST) {
            x1 = bb.minX
            x2 = bb.minX
            y1 = bb.minY
            y2 = bb.maxY
            z1 = bb.minZ
            z2 = bb.maxZ
        } else if (face == EnumFacing.SOUTH) {
            x1 = bb.minX
            x2 = bb.maxX
            y1 = bb.minY
            y2 = bb.maxY
            z1 = bb.maxZ
            z2 = bb.maxZ
        } else if (face == EnumFacing.NORTH) {
            x1 = bb.minX
            x2 = bb.maxX
            y1 = bb.minY
            y2 = bb.maxY
            z1 = bb.minZ
            z2 = bb.minZ
        }
        GlStateManager.pushMatrix()
        GlStateManager.disableDepth()
        GlStateManager.disableTexture2D()
        GlStateManager.enableBlend()
        GlStateManager.disableAlpha()
        GlStateManager.depthMask(false)
        builder.begin(5, DefaultVertexFormats.POSITION_COLOR)
        if (face == EnumFacing.EAST || face == EnumFacing.WEST || face == EnumFacing.NORTH || face == EnumFacing.SOUTH) {
            builder.pos(x1, y1, z1).color(red, green, blue, alpha).endVertex()
            builder.pos(x1, y1, z1).color(red, green, blue, alpha).endVertex()
            builder.pos(x1, y1, z1).color(red, green, blue, alpha).endVertex()
            builder.pos(x1, y1, z2).color(red, green, blue, alpha).endVertex()
            builder.pos(x1, y2, z1).color(red1, green1, blue1, alpha1).endVertex()
            builder.pos(x1, y2, z2).color(red1, green1, blue1, alpha1).endVertex()
            builder.pos(x1, y2, z2).color(red1, green1, blue1, alpha1).endVertex()
            builder.pos(x1, y1, z2).color(red, green, blue, alpha).endVertex()
            builder.pos(x2, y2, z2).color(red1, green1, blue1, alpha1).endVertex()
            builder.pos(x2, y1, z2).color(red, green, blue, alpha).endVertex()
            builder.pos(x2, y1, z2).color(red, green, blue, alpha).endVertex()
            builder.pos(x2, y1, z1).color(red, green, blue, alpha).endVertex()
            builder.pos(x2, y2, z2).color(red1, green1, blue1, alpha1).endVertex()
            builder.pos(x2, y2, z1).color(red1, green1, blue1, alpha1).endVertex()
            builder.pos(x2, y2, z1).color(red1, green1, blue1, alpha1).endVertex()
            builder.pos(x2, y1, z1).color(red, green, blue, alpha).endVertex()
            builder.pos(x1, y2, z1).color(red1, green1, blue1, alpha1).endVertex()
            builder.pos(x1, y1, z1).color(red, green, blue, alpha).endVertex()
            builder.pos(x1, y1, z1).color(red, green, blue, alpha).endVertex()
            builder.pos(x2, y1, z1).color(red, green, blue, alpha).endVertex()
            builder.pos(x1, y1, z2).color(red, green, blue, alpha).endVertex()
            builder.pos(x2, y1, z2).color(red, green, blue, alpha).endVertex()
            builder.pos(x2, y1, z2).color(red, green, blue, alpha).endVertex()
            builder.pos(x1, y2, z1).color(red1, green1, blue1, alpha1).endVertex()
            builder.pos(x1, y2, z1).color(red1, green1, blue1, alpha1).endVertex()
            builder.pos(x1, y2, z2).color(red1, green1, blue1, alpha1).endVertex()
            builder.pos(x2, y2, z1).color(red1, green1, blue1, alpha1).endVertex()
            builder.pos(x2, y2, z2).color(red1, green1, blue1, alpha1).endVertex()
            builder.pos(x2, y2, z2).color(red1, green1, blue1, alpha1).endVertex()
            builder.pos(x2, y2, z2).color(red1, green1, blue1, alpha1).endVertex()
        } else if (face == EnumFacing.UP) {
            builder.pos(x1, y1, z1).color(red1, green1, blue1, alpha1).endVertex()
            builder.pos(x1, y1, z1).color(red1, green1, blue1, alpha1).endVertex()
            builder.pos(x1, y1, z1).color(red1, green1, blue1, alpha1).endVertex()
            builder.pos(x1, y1, z2).color(red1, green1, blue1, alpha1).endVertex()
            builder.pos(x1, y2, z1).color(red1, green1, blue1, alpha1).endVertex()
            builder.pos(x1, y2, z2).color(red1, green1, blue1, alpha1).endVertex()
            builder.pos(x1, y2, z2).color(red1, green1, blue1, alpha1).endVertex()
            builder.pos(x1, y1, z2).color(red1, green1, blue1, alpha1).endVertex()
            builder.pos(x2, y2, z2).color(red1, green1, blue1, alpha1).endVertex()
            builder.pos(x2, y1, z2).color(red1, green1, blue1, alpha1).endVertex()
            builder.pos(x2, y1, z2).color(red1, green1, blue1, alpha1).endVertex()
            builder.pos(x2, y1, z1).color(red1, green1, blue1, alpha1).endVertex()
            builder.pos(x2, y2, z2).color(red1, green1, blue1, alpha1).endVertex()
            builder.pos(x2, y2, z1).color(red1, green1, blue1, alpha1).endVertex()
            builder.pos(x2, y2, z1).color(red1, green1, blue1, alpha1).endVertex()
            builder.pos(x2, y1, z1).color(red1, green1, blue1, alpha1).endVertex()
            builder.pos(x1, y2, z1).color(red1, green1, blue1, alpha1).endVertex()
            builder.pos(x1, y1, z1).color(red1, green1, blue1, alpha1).endVertex()
            builder.pos(x1, y1, z1).color(red1, green1, blue1, alpha1).endVertex()
            builder.pos(x2, y1, z1).color(red1, green1, blue1, alpha1).endVertex()
            builder.pos(x1, y1, z2).color(red1, green1, blue1, alpha1).endVertex()
            builder.pos(x2, y1, z2).color(red1, green1, blue1, alpha1).endVertex()
            builder.pos(x2, y1, z2).color(red1, green1, blue1, alpha1).endVertex()
            builder.pos(x1, y2, z1).color(red1, green1, blue1, alpha1).endVertex()
            builder.pos(x1, y2, z1).color(red1, green1, blue1, alpha1).endVertex()
            builder.pos(x1, y2, z2).color(red1, green1, blue1, alpha1).endVertex()
            builder.pos(x2, y2, z1).color(red1, green1, blue1, alpha1).endVertex()
            builder.pos(x2, y2, z2).color(red1, green1, blue1, alpha1).endVertex()
            builder.pos(x2, y2, z2).color(red1, green1, blue1, alpha1).endVertex()
            builder.pos(x2, y2, z2).color(red1, green1, blue1, alpha1).endVertex()
        } else if (face == EnumFacing.DOWN) {
            builder.pos(x1, y1, z1).color(red, green, blue, alpha).endVertex()
            builder.pos(x1, y1, z1).color(red, green, blue, alpha).endVertex()
            builder.pos(x1, y1, z1).color(red, green, blue, alpha).endVertex()
            builder.pos(x1, y1, z2).color(red, green, blue, alpha).endVertex()
            builder.pos(x1, y2, z1).color(red, green, blue, alpha).endVertex()
            builder.pos(x1, y2, z2).color(red, green, blue, alpha).endVertex()
            builder.pos(x1, y2, z2).color(red, green, blue, alpha).endVertex()
            builder.pos(x1, y1, z2).color(red, green, blue, alpha).endVertex()
            builder.pos(x2, y2, z2).color(red, green, blue, alpha).endVertex()
            builder.pos(x2, y1, z2).color(red, green, blue, alpha).endVertex()
            builder.pos(x2, y1, z2).color(red, green, blue, alpha).endVertex()
            builder.pos(x2, y1, z1).color(red, green, blue, alpha).endVertex()
            builder.pos(x2, y2, z2).color(red, green, blue, alpha).endVertex()
            builder.pos(x2, y2, z1).color(red, green, blue, alpha).endVertex()
            builder.pos(x2, y2, z1).color(red, green, blue, alpha).endVertex()
            builder.pos(x2, y1, z1).color(red, green, blue, alpha).endVertex()
            builder.pos(x1, y2, z1).color(red, green, blue, alpha).endVertex()
            builder.pos(x1, y1, z1).color(red, green, blue, alpha).endVertex()
            builder.pos(x1, y1, z1).color(red, green, blue, alpha).endVertex()
            builder.pos(x2, y1, z1).color(red, green, blue, alpha).endVertex()
            builder.pos(x1, y1, z2).color(red, green, blue, alpha).endVertex()
            builder.pos(x2, y1, z2).color(red, green, blue, alpha).endVertex()
            builder.pos(x2, y1, z2).color(red, green, blue, alpha).endVertex()
            builder.pos(x1, y2, z1).color(red, green, blue, alpha).endVertex()
            builder.pos(x1, y2, z1).color(red, green, blue, alpha).endVertex()
            builder.pos(x1, y2, z2).color(red, green, blue, alpha).endVertex()
            builder.pos(x2, y2, z1).color(red, green, blue, alpha).endVertex()
            builder.pos(x2, y2, z2).color(red, green, blue, alpha).endVertex()
            builder.pos(x2, y2, z2).color(red, green, blue, alpha).endVertex()
            builder.pos(x2, y2, z2).color(red, green, blue, alpha).endVertex()
        }
        tessellator.draw()
        GlStateManager.depthMask(true)
        GlStateManager.disableBlend()
        GlStateManager.enableAlpha()
        GlStateManager.enableTexture2D()
        GlStateManager.enableDepth()
        GlStateManager.popMatrix()
    }

    fun drawGradientRect(x: Int, y: Int, w: Int, h: Int, startColor: Int, endColor: Int) {
        val f = (startColor shr 24 and 0xFF).toFloat() / 255.0f
        val f1 = (startColor shr 16 and 0xFF).toFloat() / 255.0f
        val f2 = (startColor shr 8 and 0xFF).toFloat() / 255.0f
        val f3 = (startColor and 0xFF).toFloat() / 255.0f
        val f4 = (endColor shr 24 and 0xFF).toFloat() / 255.0f
        val f5 = (endColor shr 16 and 0xFF).toFloat() / 255.0f
        val f6 = (endColor shr 8 and 0xFF).toFloat() / 255.0f
        val f7 = (endColor and 0xFF).toFloat() / 255.0f
        GlStateManager.disableTexture2D()
        GlStateManager.enableBlend()
        GlStateManager.disableAlpha()
        GlStateManager.tryBlendFuncSeparate(
            SourceFactor.SRC_ALPHA,
            DestFactor.ONE_MINUS_SRC_ALPHA,
            SourceFactor.ONE,
            DestFactor.ZERO
        )
        GlStateManager.shadeModel(7425)
        val tessellator = Tessellator.getInstance()
        val vertexbuffer = tessellator.buffer
        vertexbuffer.begin(7, DefaultVertexFormats.POSITION_COLOR)
        vertexbuffer.pos(x.toDouble() + w.toDouble(), y.toDouble(), 0.0).color(f1, f2, f3, f).endVertex()
        vertexbuffer.pos(x.toDouble(), y.toDouble(), 0.0).color(f1, f2, f3, f).endVertex()
        vertexbuffer.pos(x.toDouble(), y.toDouble() + h.toDouble(), 0.0).color(f5, f6, f7, f4).endVertex()
        vertexbuffer.pos(x.toDouble() + w.toDouble(), y.toDouble() + h.toDouble(), 0.0).color(f5, f6, f7, f4)
            .endVertex()
        tessellator.draw()
        GlStateManager.shadeModel(7424)
        GlStateManager.disableBlend()
        GlStateManager.enableAlpha()
        GlStateManager.enableTexture2D()
    }

    fun drawGradientBlockOutline(pos: BlockPos?, startColor: Color, endColor: Color, linewidth: Float, height: Double) {
        val iblockstate = mc!!.world.getBlockState(pos)

    }

    fun drawProperGradientBlockOutline(
        pos: BlockPos?,
        startColor: Color,
        midColor: Color,
        endColor: Color,
        linewidth: Float
    ) {
        val iblockstate = mc!!.world.getBlockState(pos)

    }

    fun drawProperGradientBlockOutline(
        bb: AxisAlignedBB,
        startColor: Color,
        midColor: Color,
        endColor: Color,
        linewidth: Float
    ) {
        val red = endColor.red.toFloat() / 255.0f
        val green = endColor.green.toFloat() / 255.0f
        val blue = endColor.blue.toFloat() / 255.0f
        val alpha = endColor.alpha.toFloat() / 255.0f
        val red1 = midColor.red.toFloat() / 255.0f
        val green1 = midColor.green.toFloat() / 255.0f
        val blue1 = midColor.blue.toFloat() / 255.0f
        val alpha1 = midColor.alpha.toFloat() / 255.0f
        val red2 = startColor.red.toFloat() / 255.0f
        val green2 = startColor.green.toFloat() / 255.0f
        val blue2 = startColor.blue.toFloat() / 255.0f
        val alpha2 = startColor.alpha.toFloat() / 255.0f
        val dif = (bb.maxY - bb.minY) / 2.0
        GlStateManager.pushMatrix()
        GlStateManager.enableBlend()
        GlStateManager.disableDepth()
        GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1)
        GlStateManager.disableTexture2D()
        GlStateManager.depthMask(false)
        GL11.glEnable(2848)
        GL11.glHint(3154, 4354)
        GL11.glLineWidth(linewidth)
        GL11.glBegin(1)
        GL11.glColor4d(red.toDouble(), green.toDouble(), blue.toDouble(), alpha.toDouble())
        GL11.glVertex3d(bb.minX, bb.minY, bb.minZ)
        GL11.glVertex3d(bb.maxX, bb.minY, bb.minZ)
        GL11.glVertex3d(bb.maxX, bb.minY, bb.minZ)
        GL11.glVertex3d(bb.maxX, bb.minY, bb.maxZ)
        GL11.glVertex3d(bb.maxX, bb.minY, bb.maxZ)
        GL11.glVertex3d(bb.minX, bb.minY, bb.maxZ)
        GL11.glVertex3d(bb.minX, bb.minY, bb.maxZ)
        GL11.glVertex3d(bb.minX, bb.minY, bb.minZ)
        GL11.glVertex3d(bb.minX, bb.minY, bb.minZ)
        GL11.glColor4d(red1.toDouble(), green1.toDouble(), blue1.toDouble(), alpha1.toDouble())
        GL11.glVertex3d(bb.minX, (bb.minY + dif), bb.minZ)
        GL11.glVertex3d(bb.minX, (bb.minY + dif), bb.minZ)
        GL11.glColor4f(red2, green2, blue2, alpha2)
        GL11.glVertex3d(bb.minX, bb.maxY, bb.minZ)
        GL11.glColor4d(red.toDouble(), green.toDouble(), blue.toDouble(), alpha.toDouble())
        GL11.glVertex3d(bb.minX, bb.minY, bb.maxZ)
        GL11.glColor4d(red1.toDouble(), green1.toDouble(), blue1.toDouble(), alpha1.toDouble())
        GL11.glVertex3d(bb.minX, (bb.minY + dif), bb.maxZ)
        GL11.glVertex3d(bb.minX, (bb.minY + dif), bb.maxZ)
        GL11.glColor4d(red2.toDouble(), green2.toDouble(), blue2.toDouble(), alpha2.toDouble())
        GL11.glVertex3d(bb.minX, bb.maxY, bb.maxZ)
        GL11.glColor4d(red.toDouble(), green.toDouble(), blue.toDouble(), alpha.toDouble())
        GL11.glVertex3d(bb.maxX, bb.minY, bb.maxZ)
        GL11.glColor4d(red1.toDouble(), green1.toDouble(), blue1.toDouble(), alpha1.toDouble())
        GL11.glVertex3d(bb.maxX, (bb.minY + dif), bb.maxZ)
        GL11.glVertex3d(bb.maxX, (bb.minY + dif), bb.maxZ)
        GL11.glColor4d(red2.toDouble(), green2.toDouble(), blue2.toDouble(), alpha2.toDouble())
        GL11.glVertex3d(bb.maxX, bb.maxY, bb.maxZ)
        GL11.glColor4d(red.toDouble(), green.toDouble(), blue.toDouble(), alpha.toDouble())
        GL11.glVertex3d(bb.maxX, bb.minY, bb.minZ)
        GL11.glColor4d(red1.toDouble(), green1.toDouble(), blue1.toDouble(), alpha1.toDouble())
        GL11.glVertex3d(bb.maxX, (bb.minY + dif), bb.minZ)
        GL11.glVertex3d(bb.maxX, (bb.minY + dif), bb.minZ)
        GL11.glColor4d(red2.toDouble(), green2.toDouble(), blue2.toDouble(), alpha2.toDouble())
        GL11.glVertex3d(bb.maxX, bb.maxY, bb.minZ)
        GL11.glColor4d(red2.toDouble(), green2.toDouble(), blue2.toDouble(), alpha2.toDouble())
        GL11.glVertex3d(bb.minX, bb.maxY, bb.minZ)
        GL11.glVertex3d(bb.maxX, bb.maxY, bb.minZ)
        GL11.glVertex3d(bb.maxX, bb.maxY, bb.minZ)
        GL11.glVertex3d(bb.maxX, bb.maxY, bb.maxZ)
        GL11.glVertex3d(bb.maxX, bb.maxY, bb.maxZ)
        GL11.glVertex3d(bb.minX, bb.maxY, bb.maxZ)
        GL11.glVertex3d(bb.minX, bb.maxY, bb.maxZ)
        GL11.glVertex3d(bb.minX, bb.maxY, bb.minZ)
        GL11.glVertex3d(bb.minX, bb.maxY, bb.minZ)
        GL11.glEnd()
        GL11.glDisable(2848)
        GlStateManager.depthMask(true)
        GlStateManager.enableDepth()
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
        GlStateManager.popMatrix()
    }

    fun drawGradientBlockOutline(bb: AxisAlignedBB, startColor: Color, endColor: Color, linewidth: Float) {
        val red = startColor.red.toFloat() / 255.0f
        val green = startColor.green.toFloat() / 255.0f
        val blue = startColor.blue.toFloat() / 255.0f
        val alpha = startColor.alpha.toFloat() / 255.0f
        val red1 = endColor.red.toFloat() / 255.0f
        val green1 = endColor.green.toFloat() / 255.0f
        val blue1 = endColor.blue.toFloat() / 255.0f
        val alpha1 = endColor.alpha.toFloat() / 255.0f
        GlStateManager.pushMatrix()
        GlStateManager.enableBlend()
        GlStateManager.disableDepth()
        GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1)
        GlStateManager.disableTexture2D()
        GlStateManager.depthMask(false)
        GL11.glEnable(2848)
        GL11.glHint(3154, 4354)
        GL11.glLineWidth(linewidth)
        val tessellator = Tessellator.getInstance()
        val bufferbuilder = tessellator.buffer
        bufferbuilder.begin(3, DefaultVertexFormats.POSITION_COLOR)
        bufferbuilder.pos(bb.minX, bb.minY, bb.minZ).color(red1, green1, blue1, alpha1).endVertex()
        bufferbuilder.pos(bb.minX, bb.minY, bb.maxZ).color(red1, green1, blue1, alpha1).endVertex()
        bufferbuilder.pos(bb.maxX, bb.minY, bb.maxZ).color(red1, green1, blue1, alpha1).endVertex()
        bufferbuilder.pos(bb.maxX, bb.minY, bb.minZ).color(red1, green1, blue1, alpha1).endVertex()
        bufferbuilder.pos(bb.minX, bb.minY, bb.minZ).color(red1, green1, blue1, alpha1).endVertex()
        bufferbuilder.pos(bb.minX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(bb.minX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(bb.minX, bb.minY, bb.maxZ).color(red1, green1, blue1, alpha1).endVertex()
        bufferbuilder.pos(bb.maxX, bb.minY, bb.maxZ).color(red1, green1, blue1, alpha1).endVertex()
        bufferbuilder.pos(bb.maxX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(bb.minX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(bb.maxX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(bb.maxX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(bb.maxX, bb.minY, bb.minZ).color(red1, green1, blue1, alpha1).endVertex()
        bufferbuilder.pos(bb.maxX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(bb.minX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex()
        tessellator.draw()
        GL11.glDisable(2848)
        GlStateManager.depthMask(true)
        GlStateManager.enableDepth()
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
        GlStateManager.popMatrix()
    }

    fun drawGradientFilledBox(pos: BlockPos?, startColor: Color, endColor: Color) {
        val iblockstate = mc!!.world.getBlockState(pos)

    }

    fun drawGradientFilledBox(bb: AxisAlignedBB, startColor: Color, endColor: Color) {
        GlStateManager.pushMatrix()
        GlStateManager.enableBlend()
        GlStateManager.disableDepth()
        GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1)
        GlStateManager.disableTexture2D()
        GlStateManager.depthMask(false)
        val alpha = endColor.alpha.toFloat() / 255.0f
        val red = endColor.red.toFloat() / 255.0f
        val green = endColor.green.toFloat() / 255.0f
        val blue = endColor.blue.toFloat() / 255.0f
        val alpha1 = startColor.alpha.toFloat() / 255.0f
        val red1 = startColor.red.toFloat() / 255.0f
        val green1 = startColor.green.toFloat() / 255.0f
        val blue1 = startColor.blue.toFloat() / 255.0f
        val tessellator = Tessellator.getInstance()
        val bufferbuilder = tessellator.buffer
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR)
        bufferbuilder.pos(bb.minX, bb.minY, bb.minZ).color(red1, green1, blue1, alpha1).endVertex()
        bufferbuilder.pos(bb.maxX, bb.minY, bb.minZ).color(red1, green1, blue1, alpha1).endVertex()
        bufferbuilder.pos(bb.maxX, bb.minY, bb.maxZ).color(red1, green1, blue1, alpha1).endVertex()
        bufferbuilder.pos(bb.minX, bb.minY, bb.maxZ).color(red1, green1, blue1, alpha1).endVertex()
        bufferbuilder.pos(bb.minX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(bb.minX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(bb.maxX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(bb.maxX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(bb.minX, bb.minY, bb.minZ).color(red1, green1, blue1, alpha1).endVertex()
        bufferbuilder.pos(bb.minX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(bb.maxX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(bb.maxX, bb.minY, bb.minZ).color(red1, green1, blue1, alpha1).endVertex()
        bufferbuilder.pos(bb.maxX, bb.minY, bb.minZ).color(red1, green1, blue1, alpha1).endVertex()
        bufferbuilder.pos(bb.maxX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(bb.maxX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(bb.maxX, bb.minY, bb.maxZ).color(red1, green1, blue1, alpha1).endVertex()
        bufferbuilder.pos(bb.minX, bb.minY, bb.maxZ).color(red1, green1, blue1, alpha1).endVertex()
        bufferbuilder.pos(bb.maxX, bb.minY, bb.maxZ).color(red1, green1, blue1, alpha1).endVertex()
        bufferbuilder.pos(bb.maxX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(bb.minX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(bb.minX, bb.minY, bb.minZ).color(red1, green1, blue1, alpha1).endVertex()
        bufferbuilder.pos(bb.minX, bb.minY, bb.maxZ).color(red1, green1, blue1, alpha1).endVertex()
        bufferbuilder.pos(bb.minX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(bb.minX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex()
        tessellator.draw()
        GlStateManager.depthMask(true)
        GlStateManager.enableDepth()
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
        GlStateManager.popMatrix()
    }

    fun drawGradientRect(x: Float, y: Float, w: Float, h: Float, startColor: Int, endColor: Int) {
        val f = (startColor shr 24 and 0xFF).toFloat() / 255.0f
        val f1 = (startColor shr 16 and 0xFF).toFloat() / 255.0f
        val f2 = (startColor shr 8 and 0xFF).toFloat() / 255.0f
        val f3 = (startColor and 0xFF).toFloat() / 255.0f
        val f4 = (endColor shr 24 and 0xFF).toFloat() / 255.0f
        val f5 = (endColor shr 16 and 0xFF).toFloat() / 255.0f
        val f6 = (endColor shr 8 and 0xFF).toFloat() / 255.0f
        val f7 = (endColor and 0xFF).toFloat() / 255.0f
        GlStateManager.disableTexture2D()
        GlStateManager.enableBlend()
        GlStateManager.disableAlpha()
        GlStateManager.tryBlendFuncSeparate(
            SourceFactor.SRC_ALPHA,
            DestFactor.ONE_MINUS_SRC_ALPHA,
            SourceFactor.ONE,
            DestFactor.ZERO
        )
        GlStateManager.shadeModel(7425)
        val tessellator = Tessellator.getInstance()
        val vertexbuffer = tessellator.buffer
        vertexbuffer.begin(7, DefaultVertexFormats.POSITION_COLOR)
        vertexbuffer.pos(x.toDouble() + w.toDouble(), y.toDouble(), 0.0).color(f1, f2, f3, f).endVertex()
        vertexbuffer.pos(x.toDouble(), y.toDouble(), 0.0).color(f1, f2, f3, f).endVertex()
        vertexbuffer.pos(x.toDouble(), y.toDouble() + h.toDouble(), 0.0).color(f5, f6, f7, f4).endVertex()
        vertexbuffer.pos(x.toDouble() + w.toDouble(), y.toDouble() + h.toDouble(), 0.0).color(f5, f6, f7, f4)
            .endVertex()
        tessellator.draw()
        GlStateManager.shadeModel(7424)
        GlStateManager.disableBlend()
        GlStateManager.enableAlpha()
        GlStateManager.enableTexture2D()
    }

    fun drawFilledCircle(x: Double, y: Double, z: Double, color: Color?, radius: Double) {
        val tessellator = Tessellator.getInstance()
        val builder = tessellator.buffer
        builder.begin(5, DefaultVertexFormats.POSITION_COLOR)
    }

    fun drawGradientBoxTest(pos: BlockPos?, startColor: Color?, endColor: Color?) {}
    fun blockESP(b: BlockPos, c: Color, length: Double, length2: Double) {
        blockEsp(b, c, length, length2)
    }

    fun drawBoxESP(
        pos: BlockPos,
        color: Color,
        secondC: Boolean,
        secondColor: Color,
        lineWidth: Float,
        outline: Boolean,
        box: Boolean,
        boxAlpha: Int,
        air: Boolean
    ) {
        if (box) {
            drawBox(pos, Color(color.red, color.green, color.blue, boxAlpha))
        }
        if (outline) {
            drawBlockOutline(pos, if (secondC) secondColor else color, lineWidth, air)
        }
    }

    fun drawBoxESP(
        pos: BlockPos,
        color: Color,
        secondC: Boolean,
        secondColor: Color,
        lineWidth: Float,
        outline: Boolean,
        box: Boolean,
        boxAlpha: Int,
        air: Boolean,
        height: Double,
        gradientBox: Boolean,
        gradientOutline: Boolean,
        invertGradientBox: Boolean,
        invertGradientOutline: Boolean,
        gradientAlpha: Int
    ) {
        if (box) {
            drawBox(
                pos,
                Color(color.red, color.green, color.blue, boxAlpha),
                height,
                gradientBox,
                invertGradientBox,
                gradientAlpha
            )
        }
        if (outline) {
            drawBlockOutline(
                pos,
                if (secondC) secondColor else color,
                lineWidth,
                air,
                height,
                gradientOutline,
                invertGradientOutline,
                gradientAlpha
            )
        }
    }

    fun glScissor(x: Float, y: Float, x1: Float, y1: Float, sr: ScaledResolution) {
        GL11.glScissor(
            (x * sr.scaleFactor.toFloat()).toInt(),
            (mc!!.displayHeight.toFloat() - y1 * sr.scaleFactor.toFloat()).toInt(),
            ((x1 - x) * sr.scaleFactor.toFloat()).toInt(),
            ((y1 - y) * sr.scaleFactor.toFloat()).toInt()
        )
    }

    fun drawLine(x: Float, y: Float, x1: Float, y1: Float, thickness: Float, hex: Int) {
        val red = (hex shr 16 and 0xFF).toFloat() / 255.0f
        val green = (hex shr 8 and 0xFF).toFloat() / 255.0f
        val blue = (hex and 0xFF).toFloat() / 255.0f
        val alpha = (hex shr 24 and 0xFF).toFloat() / 255.0f
        GlStateManager.pushMatrix()
        GlStateManager.disableTexture2D()
        GlStateManager.enableBlend()
        GlStateManager.disableAlpha()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        GlStateManager.shadeModel(7425)
        GL11.glLineWidth(thickness)
        GL11.glEnable(2848)
        GL11.glHint(3154, 4354)
        val tessellator = Tessellator.getInstance()
        val bufferbuilder = tessellator.buffer
        bufferbuilder.begin(3, DefaultVertexFormats.POSITION_COLOR)
        bufferbuilder.pos(x.toDouble(), y.toDouble(), 0.0).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(x1.toDouble(), y1.toDouble(), 0.0).color(red, green, blue, alpha).endVertex()
        tessellator.draw()
        GlStateManager.shadeModel(7424)
        GL11.glDisable(2848)
        GlStateManager.disableBlend()
        GlStateManager.enableAlpha()
        GlStateManager.enableTexture2D()
        GlStateManager.popMatrix()
    }

    fun drawBox(pos: BlockPos, color: Color) {
        val bb = AxisAlignedBB(
            pos.x.toDouble() - mc!!.renderManager.viewerPosX,
            pos.y.toDouble() - mc.renderManager.viewerPosY,
            pos.z.toDouble() - mc.renderManager.viewerPosZ,
            (pos.x + 1).toDouble() - mc.renderManager.viewerPosX,
            (pos.y + 1).toDouble() - mc.renderManager.viewerPosY,
            (pos.z + 1).toDouble() - mc.renderManager.viewerPosZ
        )
        Objects.requireNonNull(mc.renderViewEntity)?.let {
            camera.setPosition(
                it.posX,
                mc.renderViewEntity!!.posY,
                mc.renderViewEntity!!.posZ
            )
        }
        if (camera.isBoundingBoxInFrustum(
                AxisAlignedBB(
                    bb.minX + mc.renderManager.viewerPosX,
                    bb.minY + mc.renderManager.viewerPosY,
                    bb.minZ + mc.renderManager.viewerPosZ,
                    bb.maxX + mc.renderManager.viewerPosX,
                    bb.maxY + mc.renderManager.viewerPosY,
                    bb.maxZ + mc.renderManager.viewerPosZ
                )
            )
        ) {
            GlStateManager.pushMatrix()
            GlStateManager.enableBlend()
            GlStateManager.disableDepth()
            GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1)
            GlStateManager.disableTexture2D()
            GlStateManager.depthMask(false)
            GL11.glEnable(2848)
            GL11.glHint(3154, 4354)
            RenderGlobal.renderFilledBox(
                bb,
                (color.red.toFloat() / 255.0f),
                (color.green.toFloat() / 255.0f),
                (color.blue.toFloat() / 255.0f),
                (color.alpha.toFloat() / 255.0f)
            )
            GL11.glDisable(2848)
            GlStateManager.depthMask(true)
            GlStateManager.enableDepth()
            GlStateManager.enableTexture2D()
            GlStateManager.disableBlend()
            GlStateManager.popMatrix()
        }
    }

    fun drawBetterGradientBox(pos: BlockPos, startColor: Color, endColor: Color) {
        val red = startColor.red.toFloat() / 255.0f
        val green = startColor.green.toFloat() / 255.0f
        val blue = startColor.blue.toFloat() / 255.0f
        val alpha = startColor.alpha.toFloat() / 255.0f
        val red1 = endColor.red.toFloat() / 255.0f
        val green1 = endColor.green.toFloat() / 255.0f
        val blue1 = endColor.blue.toFloat() / 255.0f
        val alpha1 = endColor.alpha.toFloat() / 255.0f
        val bb = AxisAlignedBB(
            pos.x.toDouble() - mc!!.renderManager.viewerPosX,
            pos.y.toDouble() - mc.renderManager.viewerPosY,
            pos.z.toDouble() - mc.renderManager.viewerPosZ,
            (pos.x + 1).toDouble() - mc.renderManager.viewerPosX,
            (pos.y + 1).toDouble() - mc.renderManager.viewerPosY,
            (pos.z + 1).toDouble() - mc.renderManager.viewerPosZ
        )
        val offset = (bb.maxY - bb.minY) / 2.0
        val tessellator = Tessellator.getInstance()
        val builder = tessellator.buffer
        GlStateManager.pushMatrix()
        GlStateManager.enableBlend()
        GlStateManager.disableDepth()
        GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1)
        GlStateManager.disableTexture2D()
        GlStateManager.depthMask(false)
        GL11.glEnable(2848)
        GL11.glHint(3154, 4354)
        builder.begin(5, DefaultVertexFormats.POSITION_COLOR)
        builder.pos(bb.minX, bb.minY, bb.minZ).color(red1, green1, blue1, alpha1).endVertex()
        builder.pos(bb.minX, bb.minY, bb.minZ).color(red1, green1, blue1, alpha1).endVertex()
        builder.pos(bb.minX, bb.minY, bb.minZ).color(red1, green1, blue1, alpha1).endVertex()
        builder.pos(bb.minX, bb.minY, bb.maxZ).color(red1, green1, blue1, alpha1).endVertex()
        builder.pos(bb.minX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex()
        builder.pos(bb.minX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex()
        builder.pos(bb.minX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex()
        builder.pos(bb.minX, bb.minY, bb.maxZ).color(red1, green1, blue1, alpha1).endVertex()
        builder.pos(bb.maxX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex()
        builder.pos(bb.maxX, bb.minY, bb.maxZ).color(red1, green1, blue1, alpha1).endVertex()
        builder.pos(bb.maxX, bb.minY, bb.maxZ).color(red1, green1, blue1, alpha1).endVertex()
        builder.pos(bb.maxX, bb.minY, bb.minZ).color(red1, green1, blue1, alpha1).endVertex()
        builder.pos(bb.maxX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex()
        builder.pos(bb.maxX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex()
        builder.pos(bb.maxX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex()
        builder.pos(bb.maxX, bb.minY, bb.minZ).color(red1, green1, blue1, alpha1).endVertex()
        builder.pos(bb.minX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex()
        builder.pos(bb.minX, bb.minY, bb.minZ).color(red1, green1, blue1, alpha1).endVertex()
        builder.pos(bb.minX, bb.minY, bb.minZ).color(red1, green1, blue1, alpha1).endVertex()
        builder.pos(bb.maxX, bb.minY, bb.minZ).color(red1, green1, blue1, alpha1).endVertex()
        builder.pos(bb.minX, bb.minY, bb.maxZ).color(red1, green1, blue1, alpha1).endVertex()
        builder.pos(bb.maxX, bb.minY, bb.maxZ).color(red1, green1, blue1, alpha1).endVertex()
        builder.pos(bb.maxX, bb.minY, bb.maxZ).color(red1, green1, blue1, alpha1).endVertex()
        builder.pos(bb.minX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex()
        builder.pos(bb.minX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex()
        builder.pos(bb.minX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex()
        builder.pos(bb.maxX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex()
        builder.pos(bb.maxX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex()
        builder.pos(bb.maxX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex()
        builder.pos(bb.maxX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex()
    }

    fun drawBetterGradientBox(pos: BlockPos, startColor: Color, midColor: Color, endColor: Color) {
        val red = startColor.red.toFloat() / 255.0f
        val green = startColor.green.toFloat() / 255.0f
        val blue = startColor.blue.toFloat() / 255.0f
        val alpha = startColor.alpha.toFloat() / 255.0f
        val red1 = endColor.red.toFloat() / 255.0f
        val green1 = endColor.green.toFloat() / 255.0f
        val blue1 = endColor.blue.toFloat() / 255.0f
        val alpha1 = endColor.alpha.toFloat() / 255.0f
        val red2 = midColor.red.toFloat() / 255.0f
        val green2 = midColor.green.toFloat() / 255.0f
        val blue2 = midColor.blue.toFloat() / 255.0f
        val alpha2 = midColor.alpha.toFloat() / 255.0f
        val bb = AxisAlignedBB(
            pos.x.toDouble() - mc!!.renderManager.viewerPosX,
            pos.y.toDouble() - mc.renderManager.viewerPosY,
            pos.z.toDouble() - mc.renderManager.viewerPosZ,
            (pos.x + 1).toDouble() - mc.renderManager.viewerPosX,
            (pos.y + 1).toDouble() - mc.renderManager.viewerPosY,
            (pos.z + 1).toDouble() - mc.renderManager.viewerPosZ
        )
        val offset = (bb.maxY - bb.minY) / 2.0
        val tessellator = Tessellator.getInstance()
        val builder = tessellator.buffer
        GlStateManager.pushMatrix()
        GlStateManager.enableBlend()
        GlStateManager.disableDepth()
        GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1)
        GlStateManager.disableTexture2D()
        GlStateManager.depthMask(false)
        GL11.glEnable(2848)
        GL11.glHint(3154, 4354)
        builder.begin(5, DefaultVertexFormats.POSITION_COLOR)
        builder.pos(bb.minX, bb.minY, bb.minZ).color(red1, green1, blue1, alpha1).endVertex()
        builder.pos(bb.minX, bb.minY, bb.minZ).color(red1, green1, blue1, alpha1).endVertex()
        builder.pos(bb.minX, bb.minY, bb.minZ).color(red1, green1, blue1, alpha1).endVertex()
        builder.pos(bb.minX, bb.minY, bb.maxZ).color(red1, green1, blue1, alpha1).endVertex()
        builder.pos(bb.minX, bb.minY + offset, bb.minZ).color(red2, green2, blue2, alpha2).endVertex()
        builder.pos(bb.minX, bb.minY + offset, bb.maxZ).color(red2, green2, blue2, alpha2).endVertex()
        builder.pos(bb.minX, bb.minY + offset, bb.maxZ).color(red2, green2, blue2, alpha2).endVertex()
        builder.pos(bb.minX, bb.minY, bb.maxZ).color(red1, green1, blue1, alpha1).endVertex()
        builder.pos(bb.maxX, bb.minY + offset, bb.maxZ).color(red2, green2, blue2, alpha2).endVertex()
        builder.pos(bb.maxX, bb.minY, bb.maxZ).color(red1, green1, blue1, alpha1).endVertex()
        builder.pos(bb.minX, bb.minY + offset, bb.minZ).color(red2, green2, blue2, alpha2).endVertex()
        builder.pos(bb.minX, bb.minY + offset, bb.minZ).color(red2, green2, blue2, alpha2).endVertex()
        builder.pos(bb.minX, bb.minY + offset, bb.minZ).color(red2, green2, blue2, alpha2).endVertex()
        builder.pos(bb.minX, bb.minY + offset, bb.maxZ).color(red2, green2, blue2, alpha2).endVertex()
        builder.pos(bb.minX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex()
        builder.pos(bb.minX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex()
        builder.pos(bb.minX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex()
        builder.pos(bb.minX, bb.minY + offset, bb.maxZ).color(red2, green2, blue2, alpha2).endVertex()
        builder.pos(bb.maxX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex()
        builder.pos(bb.maxX, bb.minY + offset, bb.maxZ).color(red2, green2, blue2, alpha2).endVertex()
        tessellator.draw()
        GL11.glDisable(2848)
        GlStateManager.depthMask(true)
        GlStateManager.enableDepth()
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
        GlStateManager.popMatrix()
    }

    fun drawEvenBetterGradientBox(pos: BlockPos, startColor: Color, midColor: Color, endColor: Color) {
        val red = startColor.red.toFloat() / 255.0f
        val green = startColor.green.toFloat() / 255.0f
        val blue = startColor.blue.toFloat() / 255.0f
        val alpha = startColor.alpha.toFloat() / 255.0f
        val red1 = endColor.red.toFloat() / 255.0f
        val green1 = endColor.green.toFloat() / 255.0f
        val blue1 = endColor.blue.toFloat() / 255.0f
        val alpha1 = endColor.alpha.toFloat() / 255.0f
        val red2 = midColor.red.toFloat() / 255.0f
        val green2 = midColor.green.toFloat() / 255.0f
        val blue2 = midColor.blue.toFloat() / 255.0f
        val alpha2 = midColor.alpha.toFloat() / 255.0f
        val bb = AxisAlignedBB(
            pos.x.toDouble() - mc!!.renderManager.viewerPosX,
            pos.y.toDouble() - mc.renderManager.viewerPosY,
            pos.z.toDouble() - mc.renderManager.viewerPosZ,
            (pos.x + 1).toDouble() - mc.renderManager.viewerPosX,
            (pos.y + 1).toDouble() - mc.renderManager.viewerPosY,
            (pos.z + 1).toDouble() - mc.renderManager.viewerPosZ
        )
        val offset = (bb.maxY - bb.minY) / 2.0
        val tessellator = Tessellator.getInstance()
        val builder = tessellator.buffer
        GlStateManager.pushMatrix()
        GlStateManager.enableBlend()
        GlStateManager.disableDepth()
        GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1)
        GlStateManager.disableTexture2D()
        GlStateManager.depthMask(false)
        GL11.glEnable(2848)
        GL11.glHint(3154, 4354)
        builder.begin(5, DefaultVertexFormats.POSITION_COLOR)
        builder.pos(bb.minX, bb.minY, bb.minZ).color(red1, green1, blue1, alpha1).endVertex()
        builder.pos(bb.minX, bb.minY, bb.minZ).color(red1, green1, blue1, alpha1).endVertex()
        builder.pos(bb.minX, bb.minY, bb.minZ).color(red1, green1, blue1, alpha1).endVertex()
        builder.pos(bb.minX, bb.minY, bb.maxZ).color(red1, green1, blue1, alpha1).endVertex()
        builder.pos(bb.minX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex()
        builder.pos(bb.minX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex()
        builder.pos(bb.minX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex()
        builder.pos(bb.minX, bb.minY, bb.maxZ).color(red1, green1, blue1, alpha1).endVertex()
        builder.pos(bb.maxX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex()
        builder.pos(bb.maxX, bb.minY, bb.maxZ).color(red1, green1, blue1, alpha1).endVertex()
        builder.pos(bb.maxX, bb.minY, bb.maxZ).color(red1, green1, blue1, alpha1).endVertex()
        builder.pos(bb.maxX, bb.minY, bb.minZ).color(red1, green1, blue1, alpha1).endVertex()
        builder.pos(bb.maxX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex()
        builder.pos(bb.maxX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex()
        builder.pos(bb.maxX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex()
        builder.pos(bb.maxX, bb.minY, bb.minZ).color(red1, green1, blue1, alpha1).endVertex()
        builder.pos(bb.minX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex()
        builder.pos(bb.minX, bb.minY, bb.minZ).color(red1, green1, blue1, alpha1).endVertex()
        builder.pos(bb.minX, bb.minY, bb.minZ).color(red1, green1, blue1, alpha1).endVertex()
        builder.pos(bb.maxX, bb.minY, bb.minZ).color(red1, green1, blue1, alpha1).endVertex()
        builder.pos(bb.minX, bb.minY, bb.maxZ).color(red1, green1, blue1, alpha1).endVertex()
        builder.pos(bb.maxX, bb.minY, bb.maxZ).color(red1, green1, blue1, alpha1).endVertex()
        builder.pos(bb.maxX, bb.minY, bb.maxZ).color(red1, green1, blue1, alpha1).endVertex()
        builder.pos(bb.minX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex()
        builder.pos(bb.minX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex()
        builder.pos(bb.minX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex()
        builder.pos(bb.maxX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex()
        builder.pos(bb.maxX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex()
        builder.pos(bb.maxX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex()
        builder.pos(bb.maxX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex()
        tessellator.draw()
        GL11.glDisable(2848)
        GlStateManager.depthMask(true)
        GlStateManager.enableDepth()
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
        GlStateManager.popMatrix()
    }

    fun drawBox(pos: BlockPos, color: Color, height: Double, gradient: Boolean, invert: Boolean, alpha: Int) {
        if (gradient) {
            val endColor = Color(color.red, color.green, color.blue, alpha)
            drawOpenGradientBox(pos, if (invert) endColor else color, if (invert) color else endColor, height)
            return
        }
        val bb = AxisAlignedBB(
            pos.x.toDouble() - mc!!.renderManager.viewerPosX,
            pos.y.toDouble() - mc.renderManager.viewerPosY,
            pos.z.toDouble() - mc.renderManager.viewerPosZ,
            (pos.x + 1).toDouble() - mc.renderManager.viewerPosX,
            (pos.y + 1).toDouble() - mc.renderManager.viewerPosY + height,
            (pos.z + 1).toDouble() - mc.renderManager.viewerPosZ
        )
        Objects.requireNonNull(mc.renderViewEntity)?.let {
            camera.setPosition(
                it.posX,
                mc.renderViewEntity!!.posY,
                mc.renderViewEntity!!.posZ
            )
        }
        if (camera.isBoundingBoxInFrustum(
                AxisAlignedBB(
                    bb.minX + mc.renderManager.viewerPosX,
                    bb.minY + mc.renderManager.viewerPosY,
                    bb.minZ + mc.renderManager.viewerPosZ,
                    bb.maxX + mc.renderManager.viewerPosX,
                    bb.maxY + mc.renderManager.viewerPosY,
                    bb.maxZ + mc.renderManager.viewerPosZ
                )
            )
        ) {
            GlStateManager.pushMatrix()
            GlStateManager.enableBlend()
            GlStateManager.disableDepth()
            GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1)
            GlStateManager.disableTexture2D()
            GlStateManager.depthMask(false)
            GL11.glEnable(2848)
            GL11.glHint(3154, 4354)
            RenderGlobal.renderFilledBox(
                bb,
                (color.red.toFloat() / 255.0f),
                (color.green.toFloat() / 255.0f),
                (color.blue.toFloat() / 255.0f),
                (color.alpha.toFloat() / 255.0f)
            )
            GL11.glDisable(2848)
            GlStateManager.depthMask(true)
            GlStateManager.enableDepth()
            GlStateManager.enableTexture2D()
            GlStateManager.disableBlend()
            GlStateManager.popMatrix()
        }
    }

    fun drawBlockOutline(pos: BlockPos?, color: Color, linewidth: Float, air: Boolean) {
        val iblockstate = mc!!.world.getBlockState(pos)
        if ((air || iblockstate.material !== Material.AIR) && mc.world.worldBorder.contains(pos)) {


        }
    }

    fun drawBlockOutline(
        pos: BlockPos,
        color: Color,
        linewidth: Float,
        air: Boolean,
        height: Double,
        gradient: Boolean,
        invert: Boolean,
        alpha: Int
    ) {
        if (gradient) {
            val endColor = Color(color.red, color.green, color.blue, alpha)
            drawGradientBlockOutline(
                pos,
                if (invert) endColor else color,
                if (invert) color else endColor,
                linewidth,
                height
            )
            return
        }
        val iblockstate = mc!!.world.getBlockState(pos)
        if ((air || iblockstate.material !== Material.AIR) && mc.world.worldBorder.contains(pos)) {
            val blockAxis = AxisAlignedBB(
                pos.x.toDouble() - mc.renderManager.viewerPosX,
                pos.y.toDouble() - mc.renderManager.viewerPosY,
                pos.z.toDouble() - mc.renderManager.viewerPosZ,
                (pos.x + 1).toDouble() - mc.renderManager.viewerPosX,
                (pos.y + 1).toDouble() - mc.renderManager.viewerPosY + height,
                (pos.z + 1).toDouble() - mc.renderManager.viewerPosZ
            )
            drawBlockOutline(blockAxis.grow(0.002), color, linewidth)
        }
    }

    fun drawBlockOutline(bb: AxisAlignedBB, color: Color, linewidth: Float) {
        val red = color.red.toFloat() / 255.0f
        val green = color.green.toFloat() / 255.0f
        val blue = color.blue.toFloat() / 255.0f
        val alpha = color.alpha.toFloat() / 255.0f
        GlStateManager.pushMatrix()
        GlStateManager.enableBlend()
        GlStateManager.disableDepth()
        GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1)
        GlStateManager.disableTexture2D()
        GlStateManager.depthMask(false)
        GL11.glEnable(2848)
        GL11.glHint(3154, 4354)
        GL11.glLineWidth(linewidth)
        val tessellator = Tessellator.getInstance()
        val bufferbuilder = tessellator.buffer
        bufferbuilder.begin(3, DefaultVertexFormats.POSITION_COLOR)
        bufferbuilder.pos(bb.minX, bb.minY, bb.minZ).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(bb.minX, bb.minY, bb.maxZ).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(bb.maxX, bb.minY, bb.maxZ).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(bb.maxX, bb.minY, bb.minZ).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(bb.minX, bb.minY, bb.minZ).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(bb.minX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(bb.minX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(bb.minX, bb.minY, bb.maxZ).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(bb.maxX, bb.minY, bb.maxZ).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(bb.maxX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(bb.minX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(bb.maxX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(bb.maxX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(bb.maxX, bb.minY, bb.minZ).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(bb.maxX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(bb.minX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex()
        tessellator.draw()
        GL11.glDisable(2848)
        GlStateManager.depthMask(true)
        GlStateManager.enableDepth()
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
        GlStateManager.popMatrix()
    }

    fun drawBoxESP(pos: BlockPos, color: Color, lineWidth: Float, outline: Boolean, box: Boolean, boxAlpha: Int) {
        val bb = AxisAlignedBB(
            pos.x.toDouble() - mc!!.renderManager.viewerPosX,
            pos.y.toDouble() - mc.renderManager.viewerPosY,
            pos.z.toDouble() - mc.renderManager.viewerPosZ,
            (pos.x + 1).toDouble() - mc.renderManager.viewerPosX,
            (pos.y + 1).toDouble() - mc.renderManager.viewerPosY,
            (pos.z + 1).toDouble() - mc.renderManager.viewerPosZ
        )
        Objects.requireNonNull(mc.renderViewEntity)?.let {
            camera.setPosition(
                it.posX,
                mc.renderViewEntity!!.posY,
                mc.renderViewEntity!!.posZ
            )
        }
        if (camera.isBoundingBoxInFrustum(
                AxisAlignedBB(
                    bb.minX + mc.renderManager.viewerPosX,
                    bb.minY + mc.renderManager.viewerPosY,
                    bb.minZ + mc.renderManager.viewerPosZ,
                    bb.maxX + mc.renderManager.viewerPosX,
                    bb.maxY + mc.renderManager.viewerPosY,
                    bb.maxZ + mc.renderManager.viewerPosZ
                )
            )
        ) {
            GlStateManager.pushMatrix()
            GlStateManager.enableBlend()
            GlStateManager.disableDepth()
            GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1)
            GlStateManager.disableTexture2D()
            GlStateManager.depthMask(false)
            GL11.glEnable(2848)
            GL11.glHint(3154, 4354)
            GL11.glLineWidth(lineWidth)
            val dist = mc.player.getDistance(
                (pos.x.toFloat() + 0.5f).toDouble(),
                (pos.y.toFloat() + 0.5f).toDouble(),
                (pos.z.toFloat() + 0.5f).toDouble()
            ) * 0.75
            if (box) {
                RenderGlobal.renderFilledBox(
                    bb,
                    (color.red.toFloat() / 255.0f),
                    (color.green.toFloat() / 255.0f),
                    (color.blue.toFloat() / 255.0f),
                    (boxAlpha.toFloat() / 255.0f)
                )
            }
            if (outline) {
                RenderGlobal.drawBoundingBox(
                    bb.minX,
                    bb.minY,
                    bb.minZ,
                    bb.maxX,
                    bb.maxY,
                    bb.maxZ,
                    (color.red.toFloat() / 255.0f),
                    (color.green.toFloat() / 255.0f),
                    (color.blue.toFloat() / 255.0f),
                    (color.alpha.toFloat() / 255.0f)
                )
            }
            GL11.glDisable(2848)
            GlStateManager.depthMask(true)
            GlStateManager.enableDepth()
            GlStateManager.enableTexture2D()
            GlStateManager.disableBlend()
            GlStateManager.popMatrix()
        }
    }

    fun drawText(pos: BlockPos?, text: String?) {
        if (pos == null || text == null) {
            return
        }
        GlStateManager.pushMatrix()
        glBillboardDistanceScaled(
            pos.x.toFloat() + 0.5f,
            pos.y.toFloat() + 0.5f,
            pos.z.toFloat() + 0.5f,
            mc!!.player as EntityPlayer,
            1.0f
        )
        GlStateManager.disableDepth()
        GlStateManager.popMatrix()
    }

    fun drawOutlinedBlockESP(pos: BlockPos?, color: Color?, linewidth: Float) {
        val iblockstate = mc!!.world.getBlockState(pos)

    }

    fun blockEsp(blockPos: BlockPos, c: Color, length: Double, length2: Double) {
        val x = blockPos.x.toDouble()
        val y = blockPos.y.toDouble()
        val z = blockPos.z.toDouble()
        GL11.glPushMatrix()
        GL11.glBlendFunc(770, 771)
        GL11.glEnable(3042)
        GL11.glLineWidth(2.0f)
        GL11.glDisable(3553)
        GL11.glDisable(2929)
        GL11.glDepthMask(false)
        GL11.glColor4d(
            (c.red.toFloat() / 255.0f).toDouble(),
            (c.green.toFloat() / 255.0f).toDouble(),
            (c.blue.toFloat() / 255.0f).toDouble(),
            0.25
        )
        drawColorBox(AxisAlignedBB(x, y, z, x + length2, y + 1.0, z + length), 0.0f, 0.0f, 0.0f, 0.0f)
        GL11.glColor4d(0.0, 0.0, 0.0, 0.5)
        drawSelectionBoundingBox(AxisAlignedBB(x, y, z, x + length2, y + 1.0, z + length))
        GL11.glLineWidth(2.0f)
        GL11.glEnable(3553)
        GL11.glEnable(2929)
        GL11.glDepthMask(true)
        GL11.glDisable(3042)
        GL11.glPopMatrix()
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f)
    }

    fun drawRect(x: Float, y: Float, w: Float, h: Float, color: Int) {
        val alpha = (color shr 24 and 0xFF).toFloat() / 255.0f
        val red = (color shr 16 and 0xFF).toFloat() / 255.0f
        val green = (color shr 8 and 0xFF).toFloat() / 255.0f
        val blue = (color and 0xFF).toFloat() / 255.0f
        val tessellator = Tessellator.getInstance()
        val bufferbuilder = tessellator.buffer
        GlStateManager.enableBlend()
        GlStateManager.disableTexture2D()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR)
        bufferbuilder.pos(x.toDouble(), h.toDouble(), 0.0).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(w.toDouble(), h.toDouble(), 0.0).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(w.toDouble(), y.toDouble(), 0.0).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(x.toDouble(), y.toDouble(), 0.0).color(red, green, blue, alpha).endVertex()
        tessellator.draw()
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
    }

    fun drawColorBox(axisalignedbb: AxisAlignedBB, red: Float, green: Float, blue: Float, alpha: Float) {
        val ts = Tessellator.getInstance()
        val vb = ts.buffer
        vb.begin(7, DefaultVertexFormats.POSITION_TEX)
        vb.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex()
        vb.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex()
        vb.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex()
        vb.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex()
        vb.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex()
        vb.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex()
        vb.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex()
        vb.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex()
        ts.draw()
        vb.begin(7, DefaultVertexFormats.POSITION_TEX)
        vb.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex()
        vb.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex()
        vb.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex()
        vb.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex()
        vb.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex()
        vb.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex()
        vb.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex()
        vb.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex()
        ts.draw()
        vb.begin(7, DefaultVertexFormats.POSITION_TEX)
        vb.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex()
        vb.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex()
        vb.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex()
        vb.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex()
        vb.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex()
        vb.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex()
        vb.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex()
        vb.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex()
        ts.draw()
        vb.begin(7, DefaultVertexFormats.POSITION_TEX)
        vb.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex()
        vb.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex()
        vb.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex()
        vb.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex()
        vb.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex()
        vb.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex()
        vb.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex()
        vb.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex()
        ts.draw()
        vb.begin(7, DefaultVertexFormats.POSITION_TEX)
        vb.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex()
        vb.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex()
        vb.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex()
        vb.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex()
        vb.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex()
        vb.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex()
        vb.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex()
        vb.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex()
        ts.draw()
        vb.begin(7, DefaultVertexFormats.POSITION_TEX)
        vb.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex()
        vb.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex()
        vb.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex()
        vb.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex()
        vb.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex()
        vb.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex()
        vb.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex()
        vb.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex()
        ts.draw()
    }

    fun drawSelectionBoundingBox(boundingBox: AxisAlignedBB) {
        val tessellator = Tessellator.getInstance()
        val vertexbuffer = tessellator.buffer
        vertexbuffer.begin(3, DefaultVertexFormats.POSITION)
        vertexbuffer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).endVertex()
        vertexbuffer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.minZ).endVertex()
        vertexbuffer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ).endVertex()
        vertexbuffer.pos(boundingBox.minX, boundingBox.minY, boundingBox.maxZ).endVertex()
        vertexbuffer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).endVertex()
        tessellator.draw()
        vertexbuffer.begin(3, DefaultVertexFormats.POSITION)
        vertexbuffer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).endVertex()
        vertexbuffer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ).endVertex()
        vertexbuffer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ).endVertex()
        vertexbuffer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ).endVertex()
        vertexbuffer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).endVertex()
        tessellator.draw()
        vertexbuffer.begin(1, DefaultVertexFormats.POSITION)
        vertexbuffer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).endVertex()
        vertexbuffer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).endVertex()
        vertexbuffer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.minZ).endVertex()
        vertexbuffer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ).endVertex()
        vertexbuffer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ).endVertex()
        vertexbuffer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ).endVertex()
        vertexbuffer.pos(boundingBox.minX, boundingBox.minY, boundingBox.maxZ).endVertex()
        vertexbuffer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ).endVertex()
        tessellator.draw()
    }

    fun glrendermethod() {
        GL11.glEnable(3042)
        GL11.glBlendFunc(770, 771)
        GL11.glEnable(2848)
        GL11.glLineWidth(2.0f)
        GL11.glDisable(3553)
        GL11.glEnable(2884)
        GL11.glDisable(2929)
        val viewerPosX = mc!!.renderManager.viewerPosX
        val viewerPosY = mc.renderManager.viewerPosY
        val viewerPosZ = mc.renderManager.viewerPosZ
        GL11.glPushMatrix()
        GL11.glTranslated(-viewerPosX, -viewerPosY, -viewerPosZ)
    }

    fun glStart(n: Float, n2: Float, n3: Float, n4: Float) {
        glrendermethod()
        GL11.glColor4f(n, n2, n3, n4)
    }

    fun glEnd() {
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f)
        GL11.glPopMatrix()
        GL11.glEnable(2929)
        GL11.glEnable(3553)
        GL11.glDisable(3042)
        GL11.glDisable(2848)
    }

    fun getBoundingBox(blockPos: BlockPos?): AxisAlignedBB {
        return mc!!.world.getBlockState(blockPos).getBoundingBox(mc.world as IBlockAccess, blockPos).offset(blockPos)
    }

    fun drawOutlinedBox(axisAlignedBB: AxisAlignedBB) {
        GL11.glBegin(1)
        GL11.glVertex3d(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ)
        GL11.glVertex3d(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ)
        GL11.glVertex3d(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ)
        GL11.glVertex3d(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ)
        GL11.glVertex3d(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ)
        GL11.glVertex3d(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ)
        GL11.glVertex3d(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ)
        GL11.glVertex3d(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ)
        GL11.glVertex3d(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ)
        GL11.glVertex3d(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ)
        GL11.glVertex3d(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ)
        GL11.glVertex3d(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ)
        GL11.glVertex3d(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ)
        GL11.glVertex3d(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ)
        GL11.glVertex3d(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ)
        GL11.glVertex3d(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ)
        GL11.glVertex3d(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ)
        GL11.glVertex3d(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ)
        GL11.glVertex3d(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ)
        GL11.glVertex3d(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ)
        GL11.glVertex3d(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ)
        GL11.glVertex3d(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ)
        GL11.glVertex3d(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ)
        GL11.glVertex3d(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ)
        GL11.glEnd()
    }

    fun drawFilledBoxESPN(pos: BlockPos, color: Color?) {
        val bb = AxisAlignedBB(
            pos.x.toDouble() - mc!!.renderManager.viewerPosX,
            pos.y.toDouble() - mc.renderManager.viewerPosY,
            pos.z.toDouble() - mc.renderManager.viewerPosZ,
            (pos.x + 1).toDouble() - mc.renderManager.viewerPosX,
            (pos.y + 1).toDouble() - mc.renderManager.viewerPosY,
            (pos.z + 1).toDouble() - mc.renderManager.viewerPosZ
        )
        val rgba = toRGBA(color!!)
        drawFilledBox(bb, rgba)
    }

    fun drawFilledBox(bb: AxisAlignedBB, color: Int) {
        GlStateManager.pushMatrix()
        GlStateManager.enableBlend()
        GlStateManager.disableDepth()
        GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1)
        GlStateManager.disableTexture2D()
        GlStateManager.depthMask(false)
        val alpha = (color shr 24 and 0xFF).toFloat() / 255.0f
        val red = (color shr 16 and 0xFF).toFloat() / 255.0f
        val green = (color shr 8 and 0xFF).toFloat() / 255.0f
        val blue = (color and 0xFF).toFloat() / 255.0f
        val tessellator = Tessellator.getInstance()
        val bufferbuilder = tessellator.buffer
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR)
        bufferbuilder.pos(bb.minX, bb.minY, bb.minZ).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(bb.maxX, bb.minY, bb.minZ).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(bb.maxX, bb.minY, bb.maxZ).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(bb.minX, bb.minY, bb.maxZ).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(bb.minX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(bb.minX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(bb.maxX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(bb.maxX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(bb.minX, bb.minY, bb.minZ).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(bb.minX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(bb.maxX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(bb.maxX, bb.minY, bb.minZ).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(bb.maxX, bb.minY, bb.minZ).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(bb.maxX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(bb.maxX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(bb.maxX, bb.minY, bb.maxZ).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(bb.minX, bb.minY, bb.maxZ).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(bb.maxX, bb.minY, bb.maxZ).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(bb.maxX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(bb.minX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(bb.minX, bb.minY, bb.minZ).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(bb.minX, bb.minY, bb.maxZ).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(bb.minX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(bb.minX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex()
        tessellator.draw()
        GlStateManager.depthMask(true)
        GlStateManager.enableDepth()
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
        GlStateManager.popMatrix()
    }

    fun drawBoundingBox(bb: AxisAlignedBB, width: Float, color: Int) {
        GlStateManager.pushMatrix()
        GlStateManager.enableBlend()
        GlStateManager.disableDepth()
        GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1)
        GlStateManager.disableTexture2D()
        GlStateManager.depthMask(false)
        GL11.glEnable(2848)
        GL11.glHint(3154, 4354)
        GL11.glLineWidth(width)
        val alpha = (color shr 24 and 0xFF).toFloat() / 255.0f
        val red = (color shr 16 and 0xFF).toFloat() / 255.0f
        val green = (color shr 8 and 0xFF).toFloat() / 255.0f
        val blue = (color and 0xFF).toFloat() / 255.0f
        val tessellator = Tessellator.getInstance()
        val bufferbuilder = tessellator.buffer
        bufferbuilder.begin(3, DefaultVertexFormats.POSITION_COLOR)
        bufferbuilder.pos(bb.minX, bb.minY, bb.minZ).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(bb.minX, bb.minY, bb.maxZ).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(bb.maxX, bb.minY, bb.maxZ).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(bb.maxX, bb.minY, bb.minZ).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(bb.minX, bb.minY, bb.minZ).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(bb.minX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(bb.minX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(bb.minX, bb.minY, bb.maxZ).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(bb.maxX, bb.minY, bb.maxZ).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(bb.maxX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(bb.minX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(bb.maxX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(bb.maxX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(bb.maxX, bb.minY, bb.minZ).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(bb.maxX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(bb.minX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex()
        tessellator.draw()
        GL11.glDisable(2848)
        GlStateManager.depthMask(true)
        GlStateManager.enableDepth()
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
        GlStateManager.popMatrix()
    }

    fun glBillboard(x: Float, y: Float, z: Float) {
        val scale = 0.02666667f

        GlStateManager.glNormal3f(0.0f, 1.0f, 0.0f)
        GlStateManager.rotate(-mc?.player?.rotationYaw!!, 0.0f, 1.0f, 0.0f)
        GlStateManager.rotate(
            mc.player.rotationPitch,
            (if (mc.gameSettings.thirdPersonView == 2) -1.0f else 1.0f),
            0.0f,
            0.0f
        )
        GlStateManager.scale(-scale, -scale, scale)
    }

    fun glBillboardDistanceScaled(x: Float, y: Float, z: Float, player: EntityPlayer, scale: Float) {
        glBillboard(x, y, z)
        val distance = player.getDistance(x.toDouble(), y.toDouble(), z.toDouble()).toInt()
        var scaleDistance = distance.toFloat() / 2.0f / (2.0f + (2.0f - scale))
        if (scaleDistance < 1.0f) {
            scaleDistance = 1.0f
        }
        GlStateManager.scale(scaleDistance, scaleDistance, scaleDistance)
    }

    fun drawColoredBoundingBox(bb: AxisAlignedBB, width: Float, red: Float, green: Float, blue: Float, alpha: Float) {
        GlStateManager.pushMatrix()
        GlStateManager.enableBlend()
        GlStateManager.disableDepth()
        GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1)
        GlStateManager.disableTexture2D()
        GlStateManager.depthMask(false)
        GL11.glEnable(2848)
        GL11.glHint(3154, 4354)
        GL11.glLineWidth(width)
        val tessellator = Tessellator.getInstance()
        val bufferbuilder = tessellator.buffer
        bufferbuilder.begin(3, DefaultVertexFormats.POSITION_COLOR)
        bufferbuilder.pos(bb.minX, bb.minY, bb.minZ).color(red, green, blue, 0.0f).endVertex()
        bufferbuilder.pos(bb.minX, bb.minY, bb.minZ).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(bb.maxX, bb.minY, bb.minZ).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(bb.maxX, bb.minY, bb.maxZ).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(bb.minX, bb.minY, bb.maxZ).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(bb.minX, bb.minY, bb.minZ).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(bb.minX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(bb.maxX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(bb.maxX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(bb.minX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(bb.minX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(bb.minX, bb.maxY, bb.maxZ).color(red, green, blue, 0.0f).endVertex()
        bufferbuilder.pos(bb.minX, bb.minY, bb.maxZ).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(bb.maxX, bb.maxY, bb.maxZ).color(red, green, blue, 0.0f).endVertex()
        bufferbuilder.pos(bb.maxX, bb.minY, bb.maxZ).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(bb.maxX, bb.maxY, bb.minZ).color(red, green, blue, 0.0f).endVertex()
        bufferbuilder.pos(bb.maxX, bb.minY, bb.minZ).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(bb.maxX, bb.minY, bb.minZ).color(red, green, blue, 0.0f).endVertex()
        tessellator.draw()
        GL11.glDisable(2848)
        GlStateManager.depthMask(true)
        GlStateManager.enableDepth()
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
        GlStateManager.popMatrix()
    }

    fun drawSphere(x: Double, y: Double, z: Double, size: Float, slices: Int, stacks: Int) {
        val s = Sphere()
        GL11.glPushMatrix()
        GL11.glBlendFunc(770, 771)
        GL11.glEnable(3042)
        GL11.glLineWidth(1.2f)
        GL11.glDisable(3553)
        GL11.glDisable(2929)
        GL11.glDepthMask(false)
        s.drawStyle = 100013

        s.draw(size, slices, stacks)
        GL11.glLineWidth(2.0f)
        GL11.glEnable(3553)
        GL11.glEnable(2929)
        GL11.glDepthMask(true)
        GL11.glDisable(3042)
        GL11.glPopMatrix()
    }

    fun drawBar(
        projection: GLUProjection.Projection,
        width: Float,
        height: Float,
        totalWidth: Float,
        startColor: Color,
        outlineColor: Color
    ) {
        if (projection.type === GLUProjection.Projection.Type.INSIDE) {
            GlStateManager.pushMatrix()
            GlStateManager.translate(projection.x, projection.y, 0.0)
            drawOutlineRect(-(totalWidth / 2.0f), -(height / 2.0f), totalWidth, height, outlineColor.rgb)
            drawRect(-(totalWidth / 2.0f), -(height / 2.0f), width, height, startColor.rgb)
            GlStateManager.translate(-projection.x, -projection.y, 0.0)
            GlStateManager.popMatrix()
        }
    }

    fun drawProjectedText(
        projection: GLUProjection.Projection,
        addX: Float,
        addY: Float,
        text: String?,
        color: Color,
        shadow: Boolean
    ) {
        if (projection.type === GLUProjection.Projection.Type.INSIDE) {
            GlStateManager.pushMatrix()
            GlStateManager.translate(projection.x, projection.y, 0.0)
            GlStateManager.translate(-projection.x, -projection.y, 0.0)
            GlStateManager.popMatrix()
        }
    }

    fun drawChungusESP(projection: GLUProjection.Projection, width: Float, height: Float, location: ResourceLocation?) {
        if (projection.type === GLUProjection.Projection.Type.INSIDE) {
            GlStateManager.pushMatrix()
            GlStateManager.translate(projection.x, projection.y, 0.0)
            mc!!.textureManager.bindTexture(location)
            GlStateManager.enableTexture2D()
            GlStateManager.disableBlend()
            mc.textureManager.bindTexture(location)
            drawCompleteImage(0.0f, 0.0f, width, height)
            mc.textureManager.deleteTexture(location)
            GlStateManager.enableBlend()
            GlStateManager.disableTexture2D()
            GlStateManager.translate(-projection.x, -projection.y, 0.0)
            GlStateManager.popMatrix()
        }
    }

    fun drawCompleteImage(posX: Float, posY: Float, width: Float, height: Float) {
        GL11.glPushMatrix()
        GL11.glTranslatef(posX, posY, 0.0f)
        GL11.glBegin(7)
        GL11.glTexCoord2f(0.0f, 0.0f)
        GL11.glVertex3f(0.0f, 0.0f, 0.0f)
        GL11.glTexCoord2f(0.0f, 1.0f)
        GL11.glVertex3f(0.0f, height, 0.0f)
        GL11.glTexCoord2f(1.0f, 1.0f)
        GL11.glVertex3f(width, height, 0.0f)
        GL11.glTexCoord2f(1.0f, 0.0f)
        GL11.glVertex3f(width, 0.0f, 0.0f)
        GL11.glEnd()
        GL11.glPopMatrix()
    }

    fun drawOutlineRect(x: Float, y: Float, w: Float, h: Float, color: Int) {
        val alpha = (color shr 24 and 0xFF).toFloat() / 255.0f
        val red = (color shr 16 and 0xFF).toFloat() / 255.0f
        val green = (color shr 8 and 0xFF).toFloat() / 255.0f
        val blue = (color and 0xFF).toFloat() / 255.0f
        val tessellator = Tessellator.getInstance()
        val bufferbuilder = tessellator.buffer
        GlStateManager.enableBlend()
        GlStateManager.disableTexture2D()
        GlStateManager.glLineWidth(1.0f)
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        bufferbuilder.begin(2, DefaultVertexFormats.POSITION_COLOR)
        bufferbuilder.pos(x.toDouble(), h.toDouble(), 0.0).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(w.toDouble(), h.toDouble(), 0.0).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(w.toDouble(), y.toDouble(), 0.0).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(x.toDouble(), y.toDouble(), 0.0).color(red, green, blue, alpha).endVertex()
        tessellator.draw()
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
    }

    fun draw3DRect(x: Float, y: Float, w: Float, h: Float, startColor: Color, endColor: Color, lineWidth: Float) {
        val alpha = startColor.alpha.toFloat() / 255.0f
        val red = startColor.red.toFloat() / 255.0f
        val green = startColor.green.toFloat() / 255.0f
        val blue = startColor.blue.toFloat() / 255.0f
        val alpha1 = endColor.alpha.toFloat() / 255.0f
        val red1 = endColor.red.toFloat() / 255.0f
        val green1 = endColor.green.toFloat() / 255.0f
        val blue1 = endColor.blue.toFloat() / 255.0f
        val tessellator = Tessellator.getInstance()
        val bufferbuilder = tessellator.buffer
        GlStateManager.enableBlend()
        GlStateManager.disableTexture2D()
        GlStateManager.glLineWidth(lineWidth)
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR)
        bufferbuilder.pos(x.toDouble(), h.toDouble(), 0.0).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(w.toDouble(), h.toDouble(), 0.0).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(w.toDouble(), y.toDouble(), 0.0).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(x.toDouble(), y.toDouble(), 0.0).color(red, green, blue, alpha).endVertex()
        tessellator.draw()
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
    }

    fun drawClock(
        x: Float,
        y: Float,
        radius: Float,
        slices: Int,
        loops: Int,
        lineWidth: Float,
        fill: Boolean,
        color: Color
    ) {
        val disk = Disk()
        val date = Date()
        val hourAngle = 180 + -(Calendar.getInstance()[10] * 30 + Calendar.getInstance()[12] / 2)
        val minuteAngle = 180 + -(Calendar.getInstance()[12] * 6 + Calendar.getInstance()[13] / 10)
        val secondAngle = 180 + -(Calendar.getInstance()[13] * 6)
        val totalMinutesTime = Calendar.getInstance()[12]
        val totalHoursTime = Calendar.getInstance()[10]
        if (fill) {
            GL11.glPushMatrix()
            GL11.glColor4f(
                (color.red.toFloat() / 255.0f),
                (color.green.toFloat() / 255.0f),
                (color.blue.toFloat() / 255.0f),
                (color.alpha.toFloat() / 255.0f)
            )
            GL11.glBlendFunc(770, 771)
            GL11.glEnable(3042)
            GL11.glLineWidth(lineWidth)
            GL11.glDisable(3553)
            disk.orientation = 100020
            disk.drawStyle = 100012
            GL11.glTranslated(x.toDouble(), y.toDouble(), 0.0)
            disk.draw(0.0f, radius, slices, loops)
            GL11.glEnable(3553)
            GL11.glDisable(3042)
            GL11.glPopMatrix()
        } else {
            GL11.glPushMatrix()
            GL11.glColor4f(
                (color.red.toFloat() / 255.0f),
                (color.green.toFloat() / 255.0f),
                (color.blue.toFloat() / 255.0f),
                (color.alpha.toFloat() / 255.0f)
            )
            GL11.glEnable(3042)
            GL11.glLineWidth(lineWidth)
            GL11.glDisable(3553)
            GL11.glBegin(3)
            val hVectors = ArrayList<Vec2f>()
            var hue = (System.currentTimeMillis() % 7200L).toFloat() / 7200.0f
            for (i in 0..360) {
                val vec = Vec2f(
                    x + Math.sin(i.toDouble() * Math.PI / 180.0).toFloat() * radius,
                    y + Math.cos(i.toDouble() * Math.PI / 180.0).toFloat() * radius
                )
                hVectors.add(vec)
            }
            var color1 = Color(Color.HSBtoRGB(hue, 1.0f, 1.0f))
            for (j in 0 until hVectors.size - 1) {
                GL11.glColor4f(
                    (color1.red.toFloat() / 255.0f),
                    (color1.green.toFloat() / 255.0f),
                    (color1.blue.toFloat() / 255.0f),
                    (color1.alpha.toFloat() / 255.0f)
                )
                GL11.glVertex3d(hVectors[j].x.toDouble(), hVectors[j].y.toDouble(), 0.0)
                GL11.glVertex3d(hVectors[(j + 1)].x.toDouble(), hVectors[(j + 1)].y.toDouble(), 0.0)
                color1 = Color(Color.HSBtoRGB(0.0027777778f.let { hue += it; hue }, 1.0f, 1.0f))
            }
            GL11.glEnd()
            GL11.glEnable(3553)
            GL11.glDisable(3042)
            GL11.glPopMatrix()
        }
        drawLine(
            x,
            y,
            x + Math.sin(hourAngle.toDouble() * Math.PI / 180.0).toFloat() * (radius / 2.0f),
            y + Math.cos(hourAngle.toDouble() * Math.PI / 180.0).toFloat() * (radius / 2.0f),
            1.0f,
            Color.WHITE.rgb
        )
        drawLine(
            x,
            y,
            x + Math.sin(minuteAngle.toDouble() * Math.PI / 180.0).toFloat() * (radius - radius / 10.0f),
            y + Math.cos(minuteAngle.toDouble() * Math.PI / 180.0).toFloat() * (radius - radius / 10.0f),
            1.0f,
            Color.WHITE.rgb
        )
        drawLine(
            x,
            y,
            x + Math.sin(secondAngle.toDouble() * Math.PI / 180.0).toFloat() * (radius - radius / 10.0f),
            y + Math.cos(secondAngle.toDouble() * Math.PI / 180.0).toFloat() * (radius - radius / 10.0f),
            1.0f,
            Color.RED.rgb
        )
    }

    fun GLPre(lineWidth: Float) {
        depth = GL11.glIsEnabled(2896)
        texture = GL11.glIsEnabled(3042)
        clean = GL11.glIsEnabled(3553)
        bind = GL11.glIsEnabled(2929)
        override = GL11.glIsEnabled(2848)
        GLPre(depth, texture, clean, bind, override, lineWidth)
    }

    fun GlPost() {
        GLPost(depth, texture, clean, bind, override)
    }

    private fun GLPre(
        depth: Boolean,
        texture: Boolean,
        clean: Boolean,
        bind: Boolean,
        override: Boolean,
        lineWidth: Float
    ) {
        if (depth) {
            GL11.glDisable(2896)
        }
        if (!texture) {
            GL11.glEnable(3042)
        }
        GL11.glLineWidth(lineWidth)
        if (clean) {
            GL11.glDisable(3553)
        }
        if (bind) {
            GL11.glDisable(2929)
        }
        if (!override) {
            GL11.glEnable(2848)
        }
        GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA)
        GL11.glHint(3154, 4354)
        GlStateManager.depthMask(false)
    }

    fun getBipedRotations(biped: ModelBiped): Array<FloatArray?> {
        val rotations = arrayOfNulls<FloatArray>(5)
        val headRotation =
            floatArrayOf(biped.bipedHead.rotateAngleX, biped.bipedHead.rotateAngleY, biped.bipedHead.rotateAngleZ)
        rotations[0] = headRotation
        val rightArmRotation = floatArrayOf(
            biped.bipedRightArm.rotateAngleX,
            biped.bipedRightArm.rotateAngleY,
            biped.bipedRightArm.rotateAngleZ
        )
        rotations[1] = rightArmRotation
        val leftArmRotation = floatArrayOf(
            biped.bipedLeftArm.rotateAngleX,
            biped.bipedLeftArm.rotateAngleY,
            biped.bipedLeftArm.rotateAngleZ
        )
        rotations[2] = leftArmRotation
        val rightLegRotation = floatArrayOf(
            biped.bipedRightLeg.rotateAngleX,
            biped.bipedRightLeg.rotateAngleY,
            biped.bipedRightLeg.rotateAngleZ
        )
        rotations[3] = rightLegRotation
        val leftLegRotation = floatArrayOf(
            biped.bipedLeftLeg.rotateAngleX,
            biped.bipedLeftLeg.rotateAngleY,
            biped.bipedLeftLeg.rotateAngleZ
        )
        rotations[4] = leftLegRotation
        return rotations
    }

    private fun GLPost(depth: Boolean, texture: Boolean, clean: Boolean, bind: Boolean, override: Boolean) {
        GlStateManager.depthMask(true)
        if (!override) {
            GL11.glDisable(2848)
        }
        if (bind) {
            GL11.glEnable(2929)
        }
        if (clean) {
            GL11.glEnable(3553)
        }
        if (!texture) {
            GL11.glDisable(3042)
        }
        if (depth) {
            GL11.glEnable(2896)
        }
    }

    fun drawArc(cx: Float, cy: Float, r: Float, start_angle: Float, end_angle: Float, num_segments: Int) {
        GL11.glBegin(4)
        var i = (num_segments.toFloat() / (360.0f / start_angle)).toInt() + 1
        while (i.toFloat() <= num_segments.toFloat() / (360.0f / end_angle)) {
            val previousangle = Math.PI * 2 * (i - 1).toDouble() / num_segments.toDouble()
            val angle = Math.PI * 2 * i.toDouble() / num_segments.toDouble()
            GL11.glVertex2d(cx.toDouble(), cy.toDouble())
            GL11.glVertex2d(
                (cx.toDouble() + Math.cos(angle) * r.toDouble()),
                (cy.toDouble() + Math.sin(angle) * r.toDouble())
            )
            GL11.glVertex2d(
                (cx.toDouble() + Math.cos(previousangle) * r.toDouble()),
                (cy.toDouble() + Math.sin(previousangle) * r.toDouble())
            )
            ++i
        }
        glEnd()
    }

    fun drawArcOutline(cx: Float, cy: Float, r: Float, start_angle: Float, end_angle: Float, num_segments: Int) {
        GL11.glBegin(2)
        var i = (num_segments.toFloat() / (360.0f / start_angle)).toInt() + 1
        while (i.toFloat() <= num_segments.toFloat() / (360.0f / end_angle)) {
            val angle = Math.PI * 2 * i.toDouble() / num_segments.toDouble()
            GL11.glVertex2d(
                (cx.toDouble() + Math.cos(angle) * r.toDouble()),
                (cy.toDouble() + Math.sin(angle) * r.toDouble())
            )
            ++i
        }
        glEnd()
    }

    fun drawCircleOutline(x: Float, y: Float, radius: Float) {
        drawCircleOutline(x, y, radius, 0, 360, 40)
    }

    fun drawCircleOutline(x: Float, y: Float, radius: Float, start: Int, end: Int, segments: Int) {
        drawArcOutline(x, y, radius, start.toFloat(), end.toFloat(), segments)
    }

    fun drawCircle(x: Float, y: Float, radius: Float) {
        drawCircle(x, y, radius, 0, 360, 64)
    }

    fun drawCircle(x: Float, y: Float, radius: Float, start: Int, end: Int, segments: Int) {
        drawArc(x, y, radius, start.toFloat(), end.toFloat(), segments)
    }

    fun drawOutlinedRoundedRectangle(
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        radius: Float,
        dR: Float,
        dG: Float,
        dB: Float,
        dA: Float,
        outlineWidth: Float
    ) {
        drawRoundedRectangle(x.toFloat(), y.toFloat(), width.toFloat(), height.toFloat(), radius)
        GL11.glColor4f(dR, dG, dB, dA)
        drawRoundedRectangle(
            x.toFloat() + outlineWidth,
            y.toFloat() + outlineWidth,
            width.toFloat() - outlineWidth * 2.0f,
            height.toFloat() - outlineWidth * 2.0f,
            radius
        )
    }

    fun drawRectangle(x: Float, y: Float, width: Float, height: Float) {
        GL11.glEnable(3042)
        GL11.glBlendFunc(770, 771)
        GL11.glBegin(2)
        GL11.glVertex2d(width.toDouble(), 0.0)
        GL11.glVertex2d(0.0, 0.0)
        GL11.glVertex2d(0.0, height.toDouble())
        GL11.glVertex2d(width.toDouble(), height.toDouble())
        glEnd()
    }

    fun drawRectangleXY(x: Float, y: Float, width: Float, height: Float) {
        GL11.glEnable(3042)
        GL11.glBlendFunc(770, 771)
        GL11.glBegin(2)
        GL11.glVertex2d((x + width).toDouble(), y.toDouble())
        GL11.glVertex2d(x.toDouble(), y.toDouble())
        GL11.glVertex2d(x.toDouble(), (y + height).toDouble())
        GL11.glVertex2d((x + width).toDouble(), (y + height).toDouble())
        glEnd()
    }

    fun drawFilledRectangle(x: Float, y: Float, width: Float, height: Float) {
        GL11.glEnable(3042)
        GL11.glBlendFunc(770, 771)
        GL11.glBegin(7)
        GL11.glVertex2d((x + width).toDouble(), y.toDouble())
        GL11.glVertex2d(x.toDouble(), y.toDouble())
        GL11.glVertex2d(x.toDouble(), (y + height).toDouble())
        GL11.glVertex2d((x + width).toDouble(), (y + height).toDouble())
        glEnd()
    }

    fun to2D(x: Double, y: Double, z: Double): Vec3d? {
        GL11.glGetFloat(2982, modelView as FloatBuffer)
        GL11.glGetFloat(2983, projection as FloatBuffer)
        GL11.glGetInteger(2978, viewport as IntBuffer)
        val result = GLU.gluProject(
            x.toFloat(),
            y.toFloat(),
            z.toFloat(),
            modelView,
            projection,
            viewport,
            screenCoords as FloatBuffer
        )
        return if (result) {
            Vec3d(
                screenCoords[0].toDouble(),
                (Display.getHeight().toFloat() - screenCoords[1]).toDouble(),
                screenCoords[2].toDouble()
            )
        } else null
    }

    fun drawTracerPointer(
        x: Float,
        y: Float,
        size: Float,
        widthDiv: Float,
        heightDiv: Float,
        outline: Boolean,
        outlineWidth: Float,
        color: Int
    ) {
        val blend = GL11.glIsEnabled(3042)
        val alpha = (color shr 24 and 0xFF).toFloat() / 255.0f
        GL11.glEnable(3042)
        GL11.glDisable(3553)
        GL11.glBlendFunc(770, 771)
        GL11.glEnable(2848)
        GL11.glPushMatrix()
        hexColor(color)
        GL11.glBegin(7)
        GL11.glVertex2d(x.toDouble(), y.toDouble())
        GL11.glVertex2d((x - size / widthDiv).toDouble(), (y + size).toDouble())
        GL11.glVertex2d(x.toDouble(), (y + size / heightDiv).toDouble())
        GL11.glVertex2d((x + size / widthDiv).toDouble(), (y + size).toDouble())
        GL11.glVertex2d(x.toDouble(), y.toDouble())
        GL11.glEnd()
        if (outline) {
            GL11.glLineWidth(outlineWidth)
            GL11.glColor4f(0.0f, 0.0f, 0.0f, alpha)
            GL11.glBegin(2)
            GL11.glVertex2d(x.toDouble(), y.toDouble())
            GL11.glVertex2d((x - size / widthDiv).toDouble(), (y + size).toDouble())
            GL11.glVertex2d(x.toDouble(), (y + size / heightDiv).toDouble())
            GL11.glVertex2d((x + size / widthDiv).toDouble(), (y + size).toDouble())
            GL11.glVertex2d(x.toDouble(), y.toDouble())
            GL11.glEnd()
        }
        GL11.glPopMatrix()
        GL11.glEnable(3553)
        if (!blend) {
            GL11.glDisable(3042)
        }
        GL11.glDisable(2848)
    }

    fun getRainbow(speed: Int, offset: Int, s: Float, b: Float): Int {
        var hue = ((System.currentTimeMillis() + offset.toLong()) % speed.toLong()).toFloat()
        return Color.getHSBColor(speed.toFloat().let { hue /= it; hue }, s, b).rgb
    }

    fun hexColor(hexColor: Int) {
        val red = (hexColor shr 16 and 0xFF).toFloat() / 255.0f
        val green = (hexColor shr 8 and 0xFF).toFloat() / 255.0f
        val blue = (hexColor and 0xFF).toFloat() / 255.0f
        val alpha = (hexColor shr 24 and 0xFF).toFloat() / 255.0f
        GL11.glColor4f(red, green, blue, alpha)
    }

    fun isInViewFrustrum(entity: Entity): Boolean {
        return isInViewFrustrum(entity.entityBoundingBox) || entity.ignoreFrustumCheck
    }

    fun isInViewFrustrum(bb: AxisAlignedBB?): Boolean {
        val current = Minecraft.getMinecraft().renderViewEntity
        frustrum.setPosition(current!!.posX, current.posY, current.posZ)
        return frustrum.isBoundingBoxInFrustum(bb)
    }

    fun drawRoundedRectangle(x: Float, y: Float, width: Float, height: Float, radius: Float) {
        GL11.glEnable(3042)
        drawArc(x + width - radius, y + height - radius, radius, 0.0f, 90.0f, 16)
        drawArc(x + radius, y + height - radius, radius, 90.0f, 180.0f, 16)
        drawArc(x + radius, y + radius, radius, 180.0f, 270.0f, 16)
        drawArc(x + width - radius, y + radius, radius, 270.0f, 360.0f, 16)
        GL11.glBegin(4)
        GL11.glVertex2d((x + width - radius).toDouble(), y.toDouble())
        GL11.glVertex2d((x + radius).toDouble(), y.toDouble())
        GL11.glVertex2d((x + width - radius).toDouble(), (y + radius).toDouble())
        GL11.glVertex2d((x + width - radius).toDouble(), (y + radius).toDouble())
        GL11.glVertex2d((x + radius).toDouble(), y.toDouble())
        GL11.glVertex2d((x + radius).toDouble(), (y + radius).toDouble())
        GL11.glVertex2d((x + width).toDouble(), (y + radius).toDouble())
        GL11.glVertex2d(x.toDouble(), (y + radius).toDouble())
        GL11.glVertex2d(x.toDouble(), (y + height - radius).toDouble())
        GL11.glVertex2d((x + width).toDouble(), (y + radius).toDouble())
        GL11.glVertex2d(x.toDouble(), (y + height - radius).toDouble())
        GL11.glVertex2d((x + width).toDouble(), (y + height - radius).toDouble())
        GL11.glVertex2d((x + width - radius).toDouble(), (y + height - radius).toDouble())
        GL11.glVertex2d((x + radius).toDouble(), (y + height - radius).toDouble())
        GL11.glVertex2d((x + width - radius).toDouble(), (y + height).toDouble())
        GL11.glVertex2d((x + width - radius).toDouble(), (y + height).toDouble())
        GL11.glVertex2d((x + radius).toDouble(), (y + height - radius).toDouble())
        GL11.glVertex2d((x + radius).toDouble(), (y + height).toDouble())
        glEnd()
    }

    fun renderOne(lineWidth: Float) {
        checkSetupFBO()
        GL11.glPushAttrib(1048575)
        GL11.glDisable(3008)
        GL11.glDisable(3553)
        GL11.glDisable(2896)
        GL11.glEnable(3042)
        GL11.glBlendFunc(770, 771)
        GL11.glLineWidth(lineWidth)
        GL11.glEnable(2848)
        GL11.glEnable(2960)
        GL11.glClear(1024)
        GL11.glClearStencil(15)
        GL11.glStencilFunc(512, 1, 15)
        GL11.glStencilOp(7681, 7681, 7681)
        GL11.glPolygonMode(1032, 6913)
    }

    fun renderTwo() {
        GL11.glStencilFunc(512, 0, 15)
        GL11.glStencilOp(7681, 7681, 7681)
        GL11.glPolygonMode(1032, 6914)
    }

    fun renderThree() {
        GL11.glStencilFunc(514, 1, 15)
        GL11.glStencilOp(7680, 7680, 7680)
        GL11.glPolygonMode(1032, 6913)
    }

    fun renderFour(color: Color) {
        setColor(color)
        GL11.glDepthMask(false)
        GL11.glDisable(2929)
        GL11.glEnable(10754)
        GL11.glPolygonOffset(1.0f, -2000000.0f)
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0f, 240.0f)
    }

    fun renderFive() {
        GL11.glPolygonOffset(1.0f, 2000000.0f)
        GL11.glDisable(10754)
        GL11.glEnable(2929)
        GL11.glDepthMask(true)
        GL11.glDisable(2960)
        GL11.glDisable(2848)
        GL11.glHint(3154, 4352)
        GL11.glEnable(3042)
        GL11.glEnable(2896)
        GL11.glEnable(3553)
        GL11.glEnable(3008)
        GL11.glPopAttrib()
    }

    fun setColor(color: Color) {
        GL11.glColor4d(
            (color.red.toDouble() / 255.0),
            (color.green.toDouble() / 255.0),
            (color.blue.toDouble() / 255.0),
            (color.alpha.toDouble() / 255.0)
        )
    }

    fun checkSetupFBO() {
        val fbo = mc!!.framebuffer
        if (fbo != null && fbo.depthBuffer > -1) {
            setupFBO(fbo)
            fbo.depthBuffer = -1
        }
    }

    private fun setupFBO(fbo: Framebuffer) {
        EXTFramebufferObject.glDeleteRenderbuffersEXT(fbo.depthBuffer)
        val stencilDepthBufferID = EXTFramebufferObject.glGenRenderbuffersEXT()
        EXTFramebufferObject.glBindRenderbufferEXT(36161, stencilDepthBufferID)
        EXTFramebufferObject.glRenderbufferStorageEXT(36161, 34041, mc!!.displayWidth, mc.displayHeight)
        EXTFramebufferObject.glFramebufferRenderbufferEXT(36160, 36128, 36161, stencilDepthBufferID)
        EXTFramebufferObject.glFramebufferRenderbufferEXT(36160, 36096, 36161, stencilDepthBufferID)
    }


        fun prepareGL() {
            GL11.glBlendFunc(770, 771)
            GlStateManager.tryBlendFuncSeparate(
                SourceFactor.SRC_ALPHA,
                DestFactor.ONE_MINUS_SRC_ALPHA,
                SourceFactor.ONE,
                DestFactor.ZERO
            )
            GlStateManager.glLineWidth(1.5f)
            GlStateManager.disableTexture2D()
            GlStateManager.depthMask(false)
            GlStateManager.enableBlend()
            GlStateManager.disableDepth()
            GlStateManager.disableLighting()
            GlStateManager.disableCull()
            GlStateManager.enableAlpha()
            GlStateManager.color(1.0f, 1.0f, 1.0f)
        }






        fun drawBox(
            buffer: BufferBuilder,
            x: Float,
            y: Float,
            z: Float,
            w: Float,
            h: Float,
            d: Float,
            r: Int,
            g: Int,
            b: Int,
            a: Int,
            sides: Int
        ) {
            if (sides and 1 != 0) {
                buffer.pos((x + w).toDouble(), y.toDouble(), z.toDouble()).color(r, g, b, a).endVertex()
                buffer.pos((x + w).toDouble(), y.toDouble(), (z + d).toDouble()).color(r, g, b, a).endVertex()
                buffer.pos(x.toDouble(), y.toDouble(), (z + d).toDouble()).color(r, g, b, a).endVertex()
                buffer.pos(x.toDouble(), y.toDouble(), z.toDouble()).color(r, g, b, a).endVertex()
            }
            if (sides and 2 != 0) {
                buffer.pos((x + w).toDouble(), (y + h).toDouble(), z.toDouble()).color(r, g, b, a).endVertex()
                buffer.pos(x.toDouble(), (y + h).toDouble(), z.toDouble()).color(r, g, b, a).endVertex()
                buffer.pos(x.toDouble(), (y + h).toDouble(), (z + d).toDouble()).color(r, g, b, a).endVertex()
                buffer.pos((x + w).toDouble(), (y + h).toDouble(), (z + d).toDouble()).color(r, g, b, a).endVertex()
            }
            if (sides and 4 != 0) {
                buffer.pos((x + w).toDouble(), y.toDouble(), z.toDouble()).color(r, g, b, a).endVertex()
                buffer.pos(x.toDouble(), y.toDouble(), z.toDouble()).color(r, g, b, a).endVertex()
                buffer.pos(x.toDouble(), (y + h).toDouble(), z.toDouble()).color(r, g, b, a).endVertex()
                buffer.pos((x + w).toDouble(), (y + h).toDouble(), z.toDouble()).color(r, g, b, a).endVertex()
            }
            if (sides and 8 != 0) {
                buffer.pos(x.toDouble(), y.toDouble(), (z + d).toDouble()).color(r, g, b, a).endVertex()
                buffer.pos((x + w).toDouble(), y.toDouble(), (z + d).toDouble()).color(r, g, b, a).endVertex()
                buffer.pos((x + w).toDouble(), (y + h).toDouble(), (z + d).toDouble()).color(r, g, b, a).endVertex()
                buffer.pos(x.toDouble(), (y + h).toDouble(), (z + d).toDouble()).color(r, g, b, a).endVertex()
            }
            if (sides and 0x10 != 0) {
                buffer.pos(x.toDouble(), y.toDouble(), z.toDouble()).color(r, g, b, a).endVertex()
                buffer.pos(x.toDouble(), y.toDouble(), (z + d).toDouble()).color(r, g, b, a).endVertex()
                buffer.pos(x.toDouble(), (y + h).toDouble(), (z + d).toDouble()).color(r, g, b, a).endVertex()
                buffer.pos(x.toDouble(), (y + h).toDouble(), z.toDouble()).color(r, g, b, a).endVertex()
            }
            if (sides and 0x20 != 0) {
                buffer.pos((x + w).toDouble(), y.toDouble(), (z + d).toDouble()).color(r, g, b, a).endVertex()
                buffer.pos((x + w).toDouble(), y.toDouble(), z.toDouble()).color(r, g, b, a).endVertex()
                buffer.pos((x + w).toDouble(), (y + h).toDouble(), z.toDouble()).color(r, g, b, a).endVertex()
                buffer.pos((x + w).toDouble(), (y + h).toDouble(), (z + d).toDouble()).color(r, g, b, a).endVertex()
            }
        }

        fun drawLines(
            buffer: BufferBuilder,
            x: Float,
            y: Float,
            z: Float,
            w: Float,
            h: Float,
            d: Float,
            r: Int,
            g: Int,
            b: Int,
            a: Int,
            sides: Int
        ) {
            if (sides and 0x11 != 0) {
                buffer.pos(x.toDouble(), y.toDouble(), z.toDouble()).color(r, g, b, a).endVertex()
                buffer.pos(x.toDouble(), y.toDouble(), (z + d).toDouble()).color(r, g, b, a).endVertex()
            }
            if (sides and 0x12 != 0) {
                buffer.pos(x.toDouble(), (y + h).toDouble(), z.toDouble()).color(r, g, b, a).endVertex()
                buffer.pos(x.toDouble(), (y + h).toDouble(), (z + d).toDouble()).color(r, g, b, a).endVertex()
            }
            if (sides and 0x21 != 0) {
                buffer.pos((x + w).toDouble(), y.toDouble(), z.toDouble()).color(r, g, b, a).endVertex()
                buffer.pos((x + w).toDouble(), y.toDouble(), (z + d).toDouble()).color(r, g, b, a).endVertex()
            }
            if (sides and 0x22 != 0) {
                buffer.pos((x + w).toDouble(), (y + h).toDouble(), z.toDouble()).color(r, g, b, a).endVertex()
                buffer.pos((x + w).toDouble(), (y + h).toDouble(), (z + d).toDouble()).color(r, g, b, a).endVertex()
            }
            if (sides and 5 != 0) {
                buffer.pos(x.toDouble(), y.toDouble(), z.toDouble()).color(r, g, b, a).endVertex()
                buffer.pos((x + w).toDouble(), y.toDouble(), z.toDouble()).color(r, g, b, a).endVertex()
            }
            if (sides and 6 != 0) {
                buffer.pos(x.toDouble(), (y + h).toDouble(), z.toDouble()).color(r, g, b, a).endVertex()
                buffer.pos((x + w).toDouble(), (y + h).toDouble(), z.toDouble()).color(r, g, b, a).endVertex()
            }
            if (sides and 9 != 0) {
                buffer.pos(x.toDouble(), y.toDouble(), (z + d).toDouble()).color(r, g, b, a).endVertex()
                buffer.pos((x + w).toDouble(), y.toDouble(), (z + d).toDouble()).color(r, g, b, a).endVertex()
            }
            if (sides and 0xA != 0) {
                buffer.pos(x.toDouble(), (y + h).toDouble(), (z + d).toDouble()).color(r, g, b, a).endVertex()
                buffer.pos((x + w).toDouble(), (y + h).toDouble(), (z + d).toDouble()).color(r, g, b, a).endVertex()
            }
            if (sides and 0x14 != 0) {
                buffer.pos(x.toDouble(), y.toDouble(), z.toDouble()).color(r, g, b, a).endVertex()
                buffer.pos(x.toDouble(), (y + h).toDouble(), z.toDouble()).color(r, g, b, a).endVertex()
            }
            if (sides and 0x24 != 0) {
                buffer.pos((x + w).toDouble(), y.toDouble(), z.toDouble()).color(r, g, b, a).endVertex()
                buffer.pos((x + w).toDouble(), (y + h).toDouble(), z.toDouble()).color(r, g, b, a).endVertex()
            }
            if (sides and 0x18 != 0) {
                buffer.pos(x.toDouble(), y.toDouble(), (z + d).toDouble()).color(r, g, b, a).endVertex()
                buffer.pos(x.toDouble(), (y + h).toDouble(), (z + d).toDouble()).color(r, g, b, a).endVertex()
            }
            if (sides and 0x28 != 0) {
                buffer.pos((x + w).toDouble(), y.toDouble(), (z + d).toDouble()).color(r, g, b, a).endVertex()
                buffer.pos((x + w).toDouble(), (y + h).toDouble(), (z + d).toDouble()).color(r, g, b, a).endVertex()
            }




        fun drawFullBox(bb: AxisAlignedBB, blockPos: BlockPos, width: Float, argb: Int, alpha2: Int) {
            val a = argb ushr 24 and 0xFF
            val r = argb ushr 16 and 0xFF
            val g = argb ushr 8 and 0xFF
            val b = argb and 0xFF
        }








    }
}

