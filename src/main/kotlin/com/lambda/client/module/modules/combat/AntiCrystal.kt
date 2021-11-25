package com.lambda.client.module.modules.combat

import me.earth.phobos.event.events.PacketEvent
import me.earth.phobos.features.Feature.fullNullCheck
import com.lambda.client.module.modules.combat.AntiCrystal.breakDelay
import com.lambda.client.module.modules.combat.AntiCrystal.checkDelay
import com.lambda.client.module.modules.combat.AntiCrystal.minDmg
import com.lambda.client.module.modules.combat.AntiCrystal.packet
import com.lambda.client.module.modules.combat.AntiCrystal.placeDelay
import com.lambda.client.module.modules.combat.AntiCrystal.range
import com.lambda.client.module.modules.combat.AntiCrystal.rotate
import com.lambda.client.module.modules.combat.AntiCrystal.rotations
import com.lambda.client.module.modules.combat.AntiCrystal.selfDmg
import com.lambda.client.module.modules.combat.AntiCrystal.switcher
import com.lambda.client.module.modules.combat.AntiCrystal.wallsRange
import com.lambda.client.module.modules.combat.AntiCrystal.wasteAmount
import me.earth.phobos.mixin.mixins.MixinMinecraft
import me.earth.phobos.util.*
import net.minecraft.client.Minecraft
import net.minecraft.util.math.BlockPos
import net.minecraft.entity.item.EntityEnderCrystal
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraft.entity.Entity
import net.minecraft.init.Items
import net.minecraft.util.EnumHand
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.network.Packet
import net.minecraft.util.math.Vec3d
import java.util.ArrayList


object AntiCrystal : com.lambda.client.module.Module(
    name = "AntiCrystal",
    description = "Block enemies crystals",
    category = com.lambda.client.module.Category.COMBAT,
) {

    lateinit var mc: MixinMinecraft
    public val range by setting("Range", 6.0f, 0.0f..10.0f, 1f)
    public val wallsRange by setting("WallsRange", 3.5f, 0.0f..10.0f, 1f)
    public val minDmg by setting("Min Damage", 4.75f, 0.0f..10.0f, 1f)
    public val selfDmg by setting("Self Damage", 2.0f, 0.0f..10.0f, 1f)
    public val placeDelay by setting("Place Delay", 0, 0..100, 1)
    public val breakDelay by setting("Break Delay", 0, 0..500, 1)
    public val checkDelay by setting("Check Delay", 0, 0..500, 1)
    public val wasteAmount by setting("Waste Amount", 1, 1..5, 1)
    public val switcher by setting("Switch", true)
    public val rotate by setting("Rotate", true)
    public val packet by setting("Packet", true)
    public val rotations by setting("Spoofs", 1, 1..20, 20)
}

private val Int.value: Byte
    get() {
        return value
    }
private val Boolean.value: Any
    get() {
        return value
    }
private val Float.value: Any
    get() {
        return this.value
    }
private var yaw = 0.0f
private var pitch = 0.0f
private var rotating = false
private var rotationPacketsSpoofed = 0
private val targets: MutableList<BlockPos> = ArrayList()
private var breakTarget: Entity? = null
private val timer = Timer()
private val breakTimer = Timer()
private val checkTimer = Timer()
val mc: Minecraft = Minecraft.getMinecraft()
fun onToggle() {
    rotating = false
}



private val deadlyCrystal: Entity?
    private get() {
        var damage: Float = 0.0f
        var bestcrystal: Entity? = null
        var highestDamage = 0.0f
        for (crystal in mc.world.loadedEntityList) {
            var damage: Float = 0.0f
            if (crystal !is EntityEnderCrystal || mc.player.getDistanceSq(crystal) > 169.0 || DamageUtil.calculateDamage(
                    crystal,
                    mc.player as Entity).also { damage = it } < minDmg.value.toFloat()
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

private operator fun Any.compareTo(toFloat: Any): Int {
    return this.compareTo(Any())

}

private fun Any.toFloat(): Any {
    return toFloat()

}

private fun getSafetyCrystals(deadlyCrystal: Entity): Int {
    var count = 0
    for (entity in mc.world.loadedEntityList) {
        var damage: Float
        if (entity is EntityEnderCrystal || DamageUtil.calculateDamage(entity, mc.player as Entity)
                .also { damage = it } > 2.0f || deadlyCrystal.getDistanceSq(entity) > 144.0
        ) continue
        ++count
    }
    return count
}

private fun getPlaceTarget(deadlyCrystal: Entity): BlockPos? {
    var closestPos: BlockPos? = null
    var smallestDamage = 10.0f
    for (pos in BlockUtil.possiblePlacePositions(range.value.toFloat() as Float)) {
        val damage = DamageUtil.calculateDamage(pos, mc.player as Entity)
        if (damage > 2.0f || deadlyCrystal.getDistanceSq(pos) > 144.0 || mc.player.getDistanceSq(pos) >= MathUtil.square(
                wallsRange.value.toFloat() as Double) && BlockUtil.rayTracePlaceCheck(pos, true, 1.0f)
        ) continue
        if (closestPos == null) {
            smallestDamage = damage
            closestPos = pos
            continue
        }
        if (damage >= smallestDamage && (damage != smallestDamage || mc.player.getDistanceSq(pos) >= mc.player.getDistanceSq(
                closestPos))
        ) continue
        smallestDamage = damage
        closestPos = pos
    }
    return closestPos
}

@SubscribeEvent
fun <packet> onPacketSend(event: PacketEvent.Send) {
    if (event.stage == 0 && rotate.value.toBoolean() && rotating) {
        if (event.getPacket<Packet<*>>() is CPacketPlayer) {
        }
        ++rotationPacketsSpoofed
        if (rotationPacketsSpoofed >= rotations.value) {
            rotating = false
            rotationPacketsSpoofed = 0
        }
    }
}

private fun Any.toBoolean(): Boolean {
    return toBoolean()

}

fun onTick() {
    if (!fullNullCheck() && checkTimer.passedMs(checkDelay.value.toInt().toLong())) {
        val deadlyCrystal = deadlyCrystal
        if (deadlyCrystal != null) {
            val placeTarget = getPlaceTarget(deadlyCrystal)
            if (placeTarget != null) {
                targets.add(placeTarget)
            }
            placeCrystal(deadlyCrystal)
            breakTarget = getBreakTarget(deadlyCrystal)
            breakCrystal()
        }
        checkTimer.reset()
    }
}

fun getBreakTarget(deadlyCrystal: Entity?): Entity? {
    var smallestCrystal: Entity? = null
    var smallestDamage = 10.0f
    for (entity in mc.world.loadedEntityList) {
        var damage: Float = 0.0f
        if (entity !is EntityEnderCrystal || DamageUtil.calculateDamage(entity, mc.player as Entity).also {
                damage = it
            } > selfDmg.value.toFloat() || entity.getDistanceSq(deadlyCrystal) > 144.0 || mc.player.getDistanceSq(
                entity) > MathUtil.square(
                wallsRange.value.toFloat() as Double) && EntityUtil.rayTraceHitCheck(entity, true)) continue
        if (smallestCrystal == null) {
            smallestCrystal = entity
            smallestDamage = damage
            continue
        }
        if (damage >= smallestDamage && (smallestDamage != damage || mc.player.getDistanceSq(entity) >= mc.player.getDistanceSq(
                smallestCrystal))
        ) continue
        smallestCrystal = entity
        smallestDamage = damage
    }
    return smallestCrystal
}

private fun placeCrystal(deadlyCrystal: Entity) {
    val offhand: Boolean
    offhand = mc.player.heldItemOffhand.item === Items.END_CRYSTAL
    val bl = offhand
    if (timer.passedMs(placeDelay.value.toInt()
            .toLong()) && (switcher.value.toBoolean() || mc.player.heldItemMainhand.item === Items.END_CRYSTAL || offhand) && !targets.isEmpty() && getSafetyCrystals(
            deadlyCrystal) <= wasteAmount.value
    ) {
        if (switcher.value.toBoolean() && mc.player.heldItemMainhand.item !== Items.END_CRYSTAL && !offhand) {
            doSwitch()
        }
        rotateToPos(targets[targets.size - 1])
        BlockUtil.placeCrystalOnBlock(targets[targets.size - 1],
            if (offhand) EnumHand.OFF_HAND else EnumHand.MAIN_HAND,
            true,
            true)
        timer.reset()
    }
}

private fun doSwitch() {
    var crystalSlot: Int
    crystalSlot = if (mc.player.heldItemMainhand.item === Items.END_CRYSTAL) mc.player.inventory.currentItem else -1
    val n = crystalSlot
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
    if (breakTimer.passedMs(breakDelay.value.toInt()
            .toLong()) && breakTarget != null && DamageUtil.canBreakWeakness(
            mc.player as EntityPlayer)
    ) {
        rotateTo(breakTarget)
        EntityUtil.attackEntity(breakTarget, packet.value as Boolean, true)
        breakTimer.reset()
        targets.clear()
    }
}

private fun rotateTo(entity: Entity?) {
    if (rotate.value.toBoolean()) {
        val angle = MathUtil.calcAngle(mc.player.getPositionEyes(mc.renderPartialTicks), entity!!.positionVector)
        yaw = angle[0]
        pitch = angle[1]
        rotating = true
    }
}

private fun rotateToPos(pos: BlockPos) {
    if (rotate.value.toBoolean()) {
        val angle = MathUtil.calcAngle(mc.player.getPositionEyes(mc.renderPartialTicks),
            Vec3d((pos.x.toFloat() + 0.5f).toDouble(),
                (pos.y.toFloat() - 0.5f).toDouble(),
                (pos.z.toFloat() + 0.5f).toDouble()))
        yaw = angle[0]
        pitch = angle[1]
        rotating = true
    }
}


private fun Boolean.toBoolean(): Boolean {
    return toBoolean()

}
