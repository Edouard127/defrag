package com.lambda.client.module.modules.combat

import com.lambda.client.module.Category
import com.lambda.client.module.Module
import com.lambda.client.module.modules.combat.AntiCrystal.breakDelay
import com.lambda.client.module.modules.combat.AntiCrystal.checkDelay
import com.lambda.client.module.modules.combat.AntiCrystal.minDmg
import com.lambda.client.module.modules.combat.AntiCrystal.rotate
import com.lambda.client.module.modules.combat.AntiCrystal.selfDmg
import com.lambda.client.module.modules.combat.AntiCrystal.switcher
import com.lambda.client.module.modules.combat.AntiCrystal.wallsRange
import com.lambda.client.module.modules.combat.KillAura.range
import com.lambda.client.util.BlockUtil
import com.lambda.client.util.BlockUtil.placeCrystalOnBlock
import com.lambda.client.util.BlockUtil.rayTracePlaceCheck
import com.lambda.client.util.DamageUtil.calculateDamage
import com.lambda.client.util.DamageUtil.canBreakWeakness
import com.lambda.client.util.MathUtil
import com.lambda.client.util.MathUtil.calcAngle
import com.lambda.client.util.PlayerUtil.timer
import com.lambda.client.util.Timer
import com.lambda.client.util.graphics.RenderUtils2D.mc
import net.minecraft.client.Minecraft
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityEnderCrystal
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Items
import net.minecraft.util.EnumHand
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d

object AntiCrystal : Module(
    name = "AntiCrystal",
    description = "Block enemies crystals",
    category = Category.COMBAT,
) {

    public val range by setting("Range", 6.0f, 0.0f..10.0f, 10.0f)
    public val wallsRange by setting("WallsRange", 3.5f, 0.0f..10.0f, 10.0f)
    public val minDmg by setting("Min Damage Place", 4.75f, 0.0f..10.0f, 0.25f)
    public val selfDmg by setting("Self Damage", 2.0f, 0.0f..10.0f, 10.0f)
    public val placeDelay by setting("Place Delay", 0, 0..100, 500)
    public val breakDelay by setting("Break Delay", 0, 0..100, 500)
    public val checkDelay by setting("Check Delay", 0, 0..100, 500)
    public val switcher by setting("Switch", true)
    public val rotate by setting("Rotate", true)
    public val packet by setting("Packet", true)
    public val rotations by setting("Spoofs", 1, 1..1, 20)
}
public val mc: Minecraft = Minecraft.getMinecraft()


private var damage: Float = 0.0f
internal var yaw = 0.0f
internal var pitch = 0.0f
private var rotating = false
private val targets: MutableList<BlockPos> = ArrayList()
private var breakTarget: Entity? = null
private val breakTimer = Timer()
private val checkTimer = Timer()

fun onToggle() {
    rotating = false
}

private val deadlyCrystal: Entity?
    private get() {
        var bestcrystal: Entity? = null
        var highestDamage = 0.0f
        for (crystal in mc.world.loadedEntityList) {

            if (crystal !is EntityEnderCrystal || mc.player.getDistanceSq(crystal) > 169.0 || calculateDamage(
                    crystal,
                    (mc.player as Entity)
                ).also { damage = it } < minDmg/*.getValue().floatValue()*/
            ) continue
            if (bestcrystal == null) {
                bestcrystal = crystal
                highestDamage = damage
                continue
            }
            if (damage <= highestDamage) continue
            bestcrystal = crystal
            highestDamage = damage
        }
        return bestcrystal
    }

private fun getPlaceTarget(deadlyCrystal: Entity): BlockPos? {
    var closestPos: BlockPos? = null
    var smallestDamage = 10.0f
    for (pos in BlockUtil.possiblePlacePositions(range.getValue().floatValue())) {
        val damage = calculateDamage(pos, (mc.player as Entity))
        if (!(damage <= 2.0f && deadlyCrystal.getDistanceSq(pos.up()) <= 144.0 && !(mc.player.getDistanceSq(pos.up()) >= MathUtil.square(
                wallsRange.getValue().floatValue()
            ) && rayTracePlaceCheck(pos, true, 1.0f)))
        )
        if (closestPos == null) {
            smallestDamage = damage
            closestPos = pos
        }
        if (damage >= smallestDamage && (damage != smallestDamage || mc.player.getDistanceSq(pos.up()) >= mc.player.getDistanceSq(
                closestPos
            ))
        )
        smallestDamage = damage
        closestPos = pos
    }
    return closestPos
}






@OptIn(ExperimentalStdlibApi::class) fun onTick() {
    if (checkTimer(checkDelay.getValue()) as Boolean) {
        val deadlyCrystal = deadlyCrystal
        if (deadlyCrystal != null) {
            val placeTarget = getPlaceTarget(deadlyCrystal)
            if (placeTarget != null) {
                targets.add(placeTarget)
            }
            placeCrystal()
            breakTarget = getBreakTarget(deadlyCrystal)
            breakCrystal()
        }
        checkTimer.reset()
    }
}


fun checkTimer(value: Any): Any {
    return value

}

fun getBreakTarget(deadlyCrystal: Entity?): Entity? {
    var smallestCrystal: Entity? = null
    var smallestDamage = 10.0f
    for (entity in mc.world.loadedEntityList) {
        if (entity !is EntityEnderCrystal || calculateDamage(entity, (mc.player as Entity)).also {
                damage = it
            } > selfDmg || entity.getDistanceSq(deadlyCrystal) > 144.0 || mc.player.getDistanceSq(entity) > MathUtil.square(
                wallsRange
            )) continue
        if (smallestCrystal == null) {
            smallestCrystal = entity
            smallestDamage = damage
            continue
        }


        if (damage >= smallestDamage && (smallestDamage != 0.0f || mc.player.getDistanceSq(entity) >= mc.player.getDistanceSq(
                smallestCrystal
            ))
        ) {
            continue
        }
        smallestCrystal = entity
        smallestDamage = damage
    }
    return smallestCrystal
}

private fun placeCrystal() {
    val offhand: Boolean = mc.player.heldItemOffhand.item === Items.END_CRYSTAL
    run {
        if (switcher && mc.player.heldItemMainhand
                .item !== Items.END_CRYSTAL && !offhand
        ) {
            doSwitch()
        }
        rotateToPos(targets[targets.size - 1])
        placeCrystalOnBlock(targets[targets.size - 1], if (offhand) EnumHand.OFF_HAND else EnumHand.MAIN_HAND, true, true)
        timer.reset()
    }
}

private fun doSwitch() {
    var crystalSlot: Int
    crystalSlot =
        if (mc.player.heldItemMainhand.item === Items.END_CRYSTAL) mc.player.inventory.currentItem else -1
    if (crystalSlot == -1) {
        for (l in 0..8) {
            if (mc.player.inventory.getStackInSlot(l).item !== Items.END_CRYSTAL) continue
            crystalSlot = l
            break
        }
    }
    if (crystalSlot != -1) {
        mc.player.inventory.currentItem = crystalSlot
    }
}

private fun breakCrystal() {
    if (true(
            breakDelay.getValue()
        ) as Boolean && breakTarget != null && canBreakWeakness(mc.player as EntityPlayer)
    ) {
        rotateTo(breakTarget)
        breakTimer.reset()
        targets.clear()
    }
}

private fun Int.getValue(): Any {
    return getValue()

}

private operator fun Boolean.invoke(intValue: Any): Any {
    return intValue
}

private fun rotateTo(entity: Entity?) {
    if (rotate) {
        val angle = calcAngle(mc.player.getPositionEyes(mc.renderPartialTicks), entity!!.positionVector)
        yaw = angle[0]
        pitch = angle[1]
        rotating = true
    }
}

private fun rotateToPos(pos: BlockPos) {
    if (rotate) {
        val angle = calcAngle(
            mc.player.getPositionEyes(mc.renderPartialTicks),
            Vec3d(
                (pos.x.toFloat() + 0.5f).toDouble(),
                (pos.y.toFloat() - 0.5f).toDouble(),
                (pos.z.toFloat() + 0.5f).toDouble()
            )
        )
        yaw = angle[0]
        pitch = angle[1]
        rotating = true
    }
}

