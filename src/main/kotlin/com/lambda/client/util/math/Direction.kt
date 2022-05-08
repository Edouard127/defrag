package com.lambda.client.util.math

import com.lambda.commons.interfaces.DisplayEnum
import net.minecraft.client.Minecraft
import net.minecraft.entity.Entity
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.Vec3i
import kotlin.math.roundToInt

/**
 * [EnumFacing] but with 45° directions
 */
@Suppress("UNUSED")
enum class Direction(
    override val displayName: String,
    val displayNameXY: String,
    val directionVec: Vec3i,
    val isDiagonal: Boolean
) : DisplayEnum {
    NORTH("North", "-Z", Vec3i(0, 0, -1), false),
    NORTH_EAST("North East", "+X -Z", Vec3i(1, 0, -1), true),
    EAST("East", "+X", Vec3i(1, 0, 0), false),
    SOUTH_EAST("South East", "+X +Z", Vec3i(1, 0, 1), true),
    SOUTH("South", "+Z", Vec3i(0, 0, 1), false),
    SOUTH_WEST("South West", "-X +Z", Vec3i(-1, 0, 1), true),
    WEST("West", "-X", Vec3i(-1, 0, 0), false),
    NORTH_WEST("North West", "-X -Z", Vec3i(-1, 0, -1), true);

    fun clockwise(n: Int = 1) = values()[Math.floorMod((ordinal + n), 8)]

    fun counterClockwise(n: Int = 1) = values()[Math.floorMod((ordinal - n), 8)]

    companion object {
        fun getDirection(): Direction? {
            val facing = Minecraft.getMinecraft().player.horizontalFacing
            return if (facing == EnumFacing.NORTH) NORTH else if (facing == EnumFacing.WEST) WEST else if (facing == EnumFacing.SOUTH) SOUTH else EAST
        }

        /**
         * Gets the closest diagonal direction player is looking at
         */
        fun getDiagonalDirection(): Direction {
            val facing = Minecraft.getMinecraft().player.horizontalFacing
            return if (facing == EnumFacing.NORTH) {
                val closest = getClosest(135.0, -135.0)
                if (closest == -135.0) NORTH_EAST else NORTH_WEST
            } else if (facing == EnumFacing.WEST) {
                val closest = getClosest(135.0, 45.0)
                if (closest == 135.0) NORTH_WEST else SOUTH_WEST
            } else if (facing == EnumFacing.EAST) {
                val closest = getClosest(-45.0, -135.0)
                if (closest == -135.0) NORTH_EAST else SOUTH_EAST
            } else {
                val closest = getClosest(45.0, -45.0)
                if (closest == 45.0) SOUTH_WEST else SOUTH_EAST
            }
        }
        private fun getClosest(a: Double, b: Double): Double {
            var yaw = Minecraft.getMinecraft().player.rotationYaw.toDouble()
            yaw = if (yaw < -180) 360.let { yaw += it; yaw } else if (yaw > 180) 360.let { yaw -= it; yaw } else yaw
            return if (Math.abs(yaw - a) < Math.abs(yaw - b)) {
                a
            } else {
                b
            }
        }

        fun fromEntity(entity: Entity?) = entity?.let {
            fromYaw(it.rotationYaw)
        } ?: NORTH

        private fun fromYaw(yaw: Float): Direction {
            val normalizedYaw = (RotationUtils.normalizeAngle(yaw) + 180.0f).coerceIn(0.0f, 360.0f)
            val index = (normalizedYaw / 45.0f).roundToInt() % 8
            return values()[index]
        }

        fun EnumFacing.toDirection() = when (this) {
            EnumFacing.NORTH -> NORTH
            EnumFacing.EAST -> EAST
            EnumFacing.SOUTH -> SOUTH
            EnumFacing.WEST -> WEST
            else -> null
        }
    }
}