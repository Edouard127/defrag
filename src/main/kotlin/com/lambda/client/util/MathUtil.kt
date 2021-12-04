package com.lambda.client.util

import net.minecraft.client.Minecraft
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*
import kotlin.math.sqrt

object MathUtil{
    private val random = Random()

    fun sin(value: Float): Float {
        return MathHelper.sin(value)
    }

    fun cos(value: Float): Float {
        return MathHelper.cos(value)
    }

    fun lengthSQ(vec3d: Vec3d): Double {
        return square(vec3d.x) + square(vec3d.y) + square(vec3d.z)
    }

    fun length(vec3d: Vec3d): Double {
        return sqrt(lengthSQ(vec3d))
    }

    fun dot(vec3d: Vec3d, other: Vec3d): Double {
        return vec3d.x * other.x + vec3d.y * other.y + vec3d.z * other.z
    }

    fun square(input: Double): Double {
        return input * input
    }

    @JvmStatic
    fun square(input: Float): Double {
        return (input * input).toDouble()
    }

    fun round(value: Double, places: Int): Double {
        require(places >= 0)
        var bd = BigDecimal.valueOf(value)
        bd = bd.setScale(places, RoundingMode.FLOOR)
        return bd.toDouble()
    }


    fun direction(yaw: Float): Vec3d {
        return Vec3d(Math.cos(degToRad((yaw + 90.0f).toDouble())), 0.0, Math.sin(degToRad((yaw + 90.0f).toDouble())))
    }

    fun round(value: Float, places: Int): Float {
        require(places >= 0)
        var bd = BigDecimal.valueOf(value.toDouble())
        bd = bd.setScale(places, RoundingMode.FLOOR)
        return bd.toFloat()
    }



    fun degToRad(deg: Double): Double {
        return Math.toRadians(deg)
    }

    @JvmStatic
    fun calcAngle(from: Vec3d, to: Vec3d): FloatArray {
        val difX: Double = to.x - from.x
        val difY: Double = (to.y - from.y) * -1.0
        val difZ: Double = to.z - from.z
        val dist: Double = MathHelper.sqrt((difX * difX + difZ * difZ)).toDouble()
        return floatArrayOf(
            MathHelper.wrapDegrees((Math.toDegrees(Math.atan2(difZ, difX)) - 90.0)).toFloat(), MathHelper.wrapDegrees(
                Math.toDegrees(Math.atan2(difY, dist))
            ).toFloat()
        )
    }

    @JvmStatic
    fun calcAngleNoY(from: Vec3d, to: Vec3d): FloatArray {
        val difX: Double = to.x - from.x
        val difZ: Double = to.z - from.z
        return floatArrayOf(MathHelper.wrapDegrees((Math.toDegrees(Math.atan2(difZ, difX)) - 90.0)).toFloat())
    }

    fun calculateLine(x1: Vec3d, x2: Vec3d, distance: Double): Vec3d {
        val length = Math.sqrt(multiply(x2.x - x1.x) + multiply(x2.y - x1.y) + multiply(x2.z - x1.z))
        val unitSlopeX: Double = (x2.x - x1.x) / length
        val unitSlopeY: Double = (x2.y - x1.y) / length
        val unitSlopeZ: Double = (x2.z - x1.z) / length
        val x: Double = x1.x + unitSlopeX * distance
        val y: Double = x1.y + unitSlopeY * distance
        val z: Double = x1.z + unitSlopeZ * distance
        return Vec3d(x, y, z)
    }

    fun multiply(one: Double): Double {
        return one * one
    }

    fun extrapolatePlayerPosition(player: EntityPlayer, ticks: Int): Vec3d {
        val lastPos = Vec3d(player.lastTickPosX, player.lastTickPosY, player.lastTickPosZ)
        val currentPos = Vec3d(player.posX, player.posY, player.posZ)
        val distance = multiply(player.motionX) + multiply(player.motionY) + multiply(player.motionZ)
        val tempVec: Vec3d = calculateLine(lastPos, currentPos, distance * ticks.toDouble())
        return Vec3d(tempVec.x, player.posY, tempVec.z)
    }

    fun differentDirectionSpeed(speed: Double): DoubleArray {
        val mc: Minecraft = Minecraft.getMinecraft()
        var forward: Float = mc.player.movementInput.moveForward
        var side: Float = mc.player.movementInput.moveStrafe
        var yaw: Float =
            mc.player.prevRotationYaw + (mc.player.rotationYaw - mc.player.prevRotationYaw) * mc.getRenderPartialTicks()
        if (forward != 0.0f) {
            if (side > 0.0f) {
                yaw += (if (forward > 0.0f) -45 else 45).toFloat()
            } else if (side < 0.0f) {
                yaw += (if (forward > 0.0f) 45 else -45).toFloat()
            }
            side = 0.0f
            if (forward > 0.0f) {
                forward = 1.0f
            } else if (forward < 0.0f) {
                forward = -1.0f
            }
        }
        val sin = Math.sin(Math.toRadians((yaw + 90.0f).toDouble()))
        val cos = Math.cos(Math.toRadians((yaw + 90.0f).toDouble()))
        val posX = forward.toDouble() * speed * cos + side.toDouble() * speed * sin
        val posZ = forward.toDouble() * speed * sin - side.toDouble() * speed * cos
        return doubleArrayOf(posX, posZ)
    }
}

