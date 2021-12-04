package com.lambda.client.util

import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.math.Vec3d
import com.lambda.client.util.MovementUtil
import net.minecraft.util.math.RayTraceResult
import net.minecraft.util.math.Vec2f
import kotlin.math.sqrt

class MovementUtil<`val`, mc> {
    fun extrapolatePlayerPosition(player: EntityPlayer, ticks: Int): Vec3d {
        val totalDistance = 0.0
        val extrapolatedMotionY = player.motionY
        for (i in 0 until ticks) {
        }
        val lastPos = Vec3d(player.lastTickPosX, player.lastTickPosY, player.lastTickPosZ)
        val currentPos = Vec3d(player.posX, player.posY, player.posZ)
        val distance = multiply(player.motionX) + multiply(player.motionY) + multiply(player.motionZ)
        var extrapolatedPosY = player.posY
        if (!player.hasNoGravity()) {
            extrapolatedPosY -= 0.1
        }
        val tempVec = calculateLine(lastPos, currentPos, distance * ticks.toDouble())
        val finalVec = Vec3d(tempVec.x, extrapolatedPosY, tempVec.z)
        return Vec3d(tempVec.x, player.posY, tempVec.z)
    }

    companion object {
        fun calculateLine(x1: Vec3d, x2: Vec3d, distance: Double): Vec3d {
            val length = sqrt(multiply(x2.x - x1.x) + multiply(x2.y - x1.y) + multiply(x2.z - x1.z))
            val unitSlopeX = (x2.x - x1.x) / length
            val unitSlopeY = (x2.y - x1.y) / length
            val unitSlopeZ = (x2.z - x1.z) / length
            val x = x1.x + unitSlopeX * distance
            val y = x1.y + unitSlopeY * distance
            val z = x1.z + unitSlopeZ * distance
            return Vec3d(x, y, z)
        }

        fun calculateLineNoY(x1: Vec2f, x2: Vec2f, distance: Double): Vec2f {
            val length = sqrt(multiply((x2.x - x1.x).toDouble()) + multiply((x2.y - x1.y).toDouble()))
            val unitSlopeX = (x2.x - x1.x).toDouble() / length
            val unitSlopeZ = (x2.y - x1.y).toDouble() / length
            val x = (x1.x.toDouble() + unitSlopeX * distance).toFloat()
            val z = (x1.y.toDouble() + unitSlopeZ * distance).toFloat()
            return Vec2f(x, z)
        }

        fun multiply(one: Double): Double {
            return one * one
        }

        fun extrapolatePlayerPositionWithGravity(player: EntityPlayer, ticks: Int): Vec3d {
            var totalDistance = 0.0
            var extrapolatedMotionY = player.motionY
            for (i in 0 until ticks) {
                totalDistance += multiply(player.motionX) + multiply(extrapolatedMotionY) + multiply(player.motionZ)
                extrapolatedMotionY -= 0.1
            }
            val horizontalDistance = multiply(player.motionX) + multiply(player.motionZ) * ticks.toDouble()
            val horizontalVec = calculateLineNoY(
                Vec2f(player.lastTickPosX.toFloat(), player.lastTickPosZ.toFloat()),
                Vec2f(player.posX.toFloat(), player.posZ.toFloat()),
                horizontalDistance
            )
            var addedY = player.motionY
            var finalY = player.posY
            val tempPos = Vec3d(horizontalVec.x.toDouble(), player.posY, horizontalVec.y.toDouble())
            for (i in 0 until ticks) {
                finalY += addedY
                addedY -= 0.1
            }
            val result: RayTraceResult? = null
            return if (result == null || result.typeOfHit == RayTraceResult.Type.ENTITY) {
                Vec3d(tempPos.x, finalY, tempPos.z)
            } else result.hitVec
        }
    }
}