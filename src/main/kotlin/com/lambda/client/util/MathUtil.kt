package com.lambda.client.util

import com.lambda.client.util.graphics.RenderUtils2D.mc
import net.minecraft.client.Minecraft
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*

object MathUtil{
    private val random = Random()
    fun getRandom(min: Int, max: Int): Int {
        return min + random.nextInt(max - min + 1)
    }

    fun getRandom(min: Double, max: Double): Double {
        return MathHelper.clamp((min + random.nextDouble() * max), min, max)
    }

    fun getRandom(min: Float, max: Float): Float {
        return MathHelper.clamp((min + random.nextFloat() * max), min, max)
    }

    fun clamp(num: Int, min: Int, max: Int): Int {
        return if (num < min) min else Math.min(num, max)
    }

    fun clamp(num: Float, min: Float, max: Float): Float {
        return if (num < min) min else Math.min(num, max)
    }

    fun clamp(num: Double, min: Double, max: Double): Double {
        return if (num < min) min else Math.min(num, max)
    }

    fun sin(value: Float): Float {
        return MathHelper.sin(value)
    }

    fun cos(value: Float): Float {
        return MathHelper.cos(value)
    }

    fun wrapDegrees(value: Float): Float {
        return MathHelper.wrapDegrees(value)
    }

    fun wrapDegrees(value: Double): Double {
        return MathHelper.wrapDegrees(value)
    }

    fun roundVec(vec3d: Vec3d, places: Int): Vec3d {
        return Vec3d(round(vec3d.x, places), round(vec3d.y, places), round(vec3d.z, places))
    }

    fun angleBetweenVecs(vec3d: Vec3d, other: Vec3d): Double {
        var angle = Math.atan2(vec3d.x - other.x, vec3d.z - other.z)
        angle = -(angle / Math.PI) * 360.0 / 2.0 + 180.0
        return angle
    }

    fun lengthSQ(vec3d: Vec3d): Double {
        return square(vec3d.x) + square(vec3d.y) + square(vec3d.z)
    }

    fun length(vec3d: Vec3d): Double {
        return Math.sqrt(lengthSQ(vec3d))
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

    fun wrap(valI: Float): Float {
        var `val` = valI % 360.0f
        if (`val` >= 180.0f) {
            `val` -= 360.0f
        }
        if (`val` < -180.0f) {
            `val` += 360.0f
        }
        return `val`
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

    fun <K, V : Comparable<V>?> sortByValue(map: Map<K, V>, descending: Boolean): LinkedHashMap<*, *> {
        val list = LinkedList(map.entries)
        if (descending) {
            list.sortWith(java.util.Map.Entry.comparingByValue(Comparator.reverseOrder()))
        } else {
            list.sortWith(java.util.Map.Entry.comparingByValue())
        }
        val result: LinkedHashMap<*, *> = LinkedHashMap<Any?, Any?>()

        return result
    }

    val timeOfDay: String
        get() {
            val c = Calendar.getInstance()
            val timeOfDay = c[11]
            if (timeOfDay < 12) {
                return "Good Morning "
            }
            if (timeOfDay < 16) {
                return "Good Afternoon "
            }
            return if (timeOfDay < 21) {
                "Good Evening "
            } else "Good Night "
        }

    fun radToDeg(rad: Double): Double {
        return rad * 57.29578
    }

    fun degToRad(deg: Double): Double {
        return deg * 0.01745329238474369
    }

    fun getIncremental(`val`: Double, inc: Double): Double {
        val one = 1.0 / inc
        return Math.round(`val` * one).toDouble() / one
    }

    fun directionSpeed(speed: Double): DoubleArray {
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

    fun getBlockBlocks(entity: Entity): List<Vec3d> {
        val vec3ds: ArrayList<Vec3d> = ArrayList<Vec3d>()
        val bb: AxisAlignedBB = entity.entityBoundingBox
        val y = entity.posY
        val minX = round(bb.minX, 0)
        val minZ = round(bb.minZ, 0)
        val maxX = round(bb.maxX, 0)
        val maxZ = round(bb.maxZ, 0)
        if (minX != maxX) {
            val vec3d1 = Vec3d(minX, y, minZ)
            val vec3d2 = Vec3d(maxX, y, minZ)
            val pos1 = BlockPos(vec3d1)
            val pos2 = BlockPos(vec3d2)
            if (BlockUtil.isBlockUnSolid(pos1) && BlockUtil.isBlockUnSolid(pos2)) {
                vec3ds.add(vec3d1)
                vec3ds.add(vec3d2)
            }
            if (minZ != maxZ) {
                val vec3d3 = Vec3d(minX, y, maxZ)
                val vec3d4 = Vec3d(maxX, y, maxZ)
                val pos3 = BlockPos(vec3d1)
                val pos4 = BlockPos(vec3d2)
                if (BlockUtil.isBlockUnSolid(pos3) && BlockUtil.isBlockUnSolid(pos4)) {
                    vec3ds.add(vec3d3)
                    vec3ds.add(vec3d4)
                    return vec3ds
                }
            }
            if (vec3ds.isEmpty()) {
                vec3ds.add(entity.positionVector)
            }
            return vec3ds
        }
        if (minZ != maxZ) {
            val vec3d1 = Vec3d(minX, y, minZ)
            val vec3d2 = Vec3d(minX, y, maxZ)
            val pos1 = BlockPos(vec3d1)
            val pos2 = BlockPos(vec3d2)
            if (BlockUtil.isBlockUnSolid(pos1) && BlockUtil.isBlockUnSolid(pos2)) {
                vec3ds.add(vec3d1)
                vec3ds.add(vec3d2)
            }
            if (vec3ds.isEmpty()) {
                vec3ds.add(entity.positionVector)
            }
            return vec3ds
        }
        vec3ds.add(entity.positionVector)
        return vec3ds
    }

    fun areVec3dsAligned(vec3d1: Vec3d, vec3d2: Vec3d): Boolean {
        return areVec3dsAlignedRetarded(vec3d1, vec3d2)
    }

    fun areVec3dsAlignedRetarded(vec3d1: Vec3d, vec3d2: Vec3d): Boolean {
        val pos1 = BlockPos(vec3d1)
        val pos2 = BlockPos(vec3d2.x, vec3d1.y, vec3d2.z)
        return pos1 == pos2 as Any
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

private operator fun <K, V> LinkedHashMap<K, V>.set(key: Any?, value: Comparable<V>?) {

}
