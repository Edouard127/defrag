package com.lambda.client.util

import com.lambda.client.util.MathUtil.calcAngle
import com.lambda.client.util.graphics.RenderUtils2D.mc
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.network.Packet
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import net.minecraft.util.math.Vec3i
import kotlin.math.atan2
import kotlin.math.sqrt

object RotationUtil {
    @JvmStatic
    val eyesPos: Vec3d
        get() = Vec3d(mc.player.posX, mc.player.posY + mc.player.getEyeHeight().toDouble(), mc.player.posZ)

    fun calculateLookAt(px: Double, py: Double, pz: Double, me: EntityPlayer): DoubleArray {
        var dirx = me.posX - px
        var diry = me.posY - py
        var dirz = me.posZ - pz
        val len = Math.sqrt(dirx * dirx + diry * diry + dirz * dirz)
        var pitch = Math.asin(len.let { diry /= it; diry })
        var yaw = Math.atan2(len.let { dirz /= it; dirz }, len.let { dirx /= it; dirx })
        pitch = pitch * 180.0 / Math.PI
        yaw = yaw * 180.0 / Math.PI
        return doubleArrayOf(90.0.let { yaw += it; yaw }, pitch)
    }

    @JvmStatic
    fun getLegitRotations(vec: Vec3d): FloatArray {
        val eyesPos = eyesPos
        val diffX = vec.x - eyesPos.x
        val diffY = vec.y - eyesPos.y
        val diffZ = vec.z - eyesPos.z
        val diffXZ = sqrt(diffX * diffX + diffZ * diffZ)
        val yaw = Math.toDegrees(atan2(diffZ, diffX)).toFloat() - 90.0f
        val pitch = (-Math.toDegrees(atan2(diffY, diffXZ))).toFloat()
        return floatArrayOf(
            mc.player.rotationYaw + MathHelper.wrapDegrees((yaw - mc.player.rotationYaw) as Float),
            mc.player.rotationPitch + MathHelper.wrapDegrees((pitch - mc.player.rotationPitch) as Float)
        )
    }

    fun simpleFacing(facing: EnumFacing?): FloatArray {
        when (facing) {
            EnumFacing.DOWN -> {
                return floatArrayOf(mc.player.rotationYaw, 90.0f)
            }
            EnumFacing.UP -> {
                return floatArrayOf(mc.player.rotationYaw, -90.0f)
            }
            EnumFacing.NORTH -> {
                return floatArrayOf(180.0f, 0.0f)
            }
            EnumFacing.SOUTH -> {
                return floatArrayOf(0.0f, 0.0f)
            }
            EnumFacing.WEST -> {
                return floatArrayOf(90.0f, 0.0f)
            }
        }
        return floatArrayOf(270.0f, 0.0f)
    }

    fun faceYawAndPitch(yaw: Float, pitch: Float) {
        mc.player.connection.sendPacket(CPacketPlayer.Rotation(yaw, pitch, mc.player.onGround) as Packet<*>)
    }

    @JvmStatic
    fun faceVector(vec: Vec3d, normalizeAngle: Boolean) {
        val rotations = getLegitRotations(vec)
        mc.player.connection.sendPacket(
            CPacketPlayer.Rotation(
                rotations[0], if (normalizeAngle) MathHelper.normalizeAngle(
                    rotations[1]
                        .toInt(), 360
                ).toFloat() else rotations[1], mc.player.onGround
            ) as Packet<*>
        )
    }

    fun faceEntity(entity: Entity) {
        val angle = calcAngle(
            mc.player.getPositionEyes(mc.getRenderPartialTicks()),
            entity.getPositionEyes(mc.getRenderPartialTicks())
        )
        faceYawAndPitch(angle[0], angle[1])
    }

    fun getAngle(entity: Entity): FloatArray {
        return calcAngle(
            mc.player.getPositionEyes(mc.getRenderPartialTicks()),
            entity.getPositionEyes(mc.getRenderPartialTicks())
        )
    }

    fun transformYaw(): Float {
        var yaw: Float = mc.player.rotationYaw % 360.0f
        if (mc.player.rotationYaw > 0.0f) {
            if (yaw > 180.0f) {
                yaw = -180.0f + (yaw - 180.0f)
            }
        } else if (yaw < -180.0f) {
            yaw = 180.0f + (yaw + 180.0f)
        }
        return if (yaw < 0.0f) {
            180.0f + yaw
        } else -180.0f + yaw
    }



    fun yawDist(pos: BlockPos?): Double {
        if (pos != null) {
            val difference = Vec3d(pos as Vec3i?).subtract(mc.player.getPositionEyes(mc.getRenderPartialTicks()))
            val d: Double = Math.abs(
                mc.player.rotationYaw.toDouble() - (Math.toDegrees(
                    Math.atan2(
                        difference.z,
                        difference.x
                    )
                ) - 90.0)
            ) % 360.0
            return if (d > 180.0) 360.0 - d else d
        }
        return 0.0
    }

    fun yawDist(e: Entity?): Double {
        if (e != null) {
            val difference = e.positionVector.add(0.0, (e.eyeHeight / 2.0f).toDouble(), 0.0)
                .subtract(mc.player.getPositionEyes(mc.getRenderPartialTicks()))
            val d: Double = Math.abs(
                mc.player.rotationYaw.toDouble() - (Math.toDegrees(
                    Math.atan2(
                        difference.z,
                        difference.x
                    )
                ) - 90.0)
            ) % 360.0
            return if (d > 180.0) 360.0 - d else d
        }
        return 0.0
    }
    fun rotate(vec: Vec3d, sendPacket: Boolean) {
        val rotations: FloatArray = getLegitRotations(vec)
        if (sendPacket) mc.player.connection.sendPacket(CPacketPlayer.Rotation(rotations[0], rotations[1], mc.player.onGround))
        mc.player.rotationYaw = rotations[0]
        mc.player.rotationPitch = rotations[1]
    }


}