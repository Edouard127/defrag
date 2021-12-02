package com.lambda.client.module.modules.combat

import com.lambda.client.module.modules.PacketEvent
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
import com.lambda.client.util.BlockUtil.possiblePlacePositions
import com.lambda.client.util.BlockUtil.rayTracePlaceCheck
import com.lambda.client.util.DamageUtil
import com.lambda.client.util.MathUtil.square
import com.lambda.client.util.BlockUtil.placeCrystalOnBlock
import com.lambda.client.util.MathUtil.calcAngle
import net.minecraft.client.Minecraft
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.SharedMonsterAttributes
import net.minecraft.entity.item.EntityEnderCrystal
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Items
import net.minecraft.init.MobEffects
import net.minecraft.item.*
import net.minecraft.network.Packet
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraft.util.CombatRules
import net.minecraft.util.EnumHand
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import net.minecraft.world.Explosion
import net.minecraft.world.World
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent


object AntiCrystal : com.lambda.client.module.Module(
    name = "AntiCrystal",
    description = "Block enemies crystals",
    category = com.lambda.client.module.Category.COMBAT,
) {

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

fun fullNullCheck(): Boolean {
    return mc.player == null || mc.world == null
}
class Timer {
    private var time = -1L
    fun passedS(s: Double): Boolean {
        return passedMs(s.toLong() * 1000L)
    }

    fun passedDms(dms: Double): Boolean {
        return passedMs(dms.toLong() * 10L)
    }

    fun passedDs(ds: Double): Boolean {
        return passedMs(ds.toLong() * 100L)
    }

    fun passedMs(ms: Long): Boolean {
        return passedNS(convertToNS(ms))
    }

    fun setMs(ms: Long) {
        time = System.nanoTime() - convertToNS(ms)
    }

    fun passedNS(ns: Long): Boolean {
        return System.nanoTime() - time >= ns
    }

    val passedTimeMs: Long
        get() = getMs(System.nanoTime() - time)

    fun reset(): Timer {
        time = System.nanoTime()
        return this
    }

    fun getMs(time: Long): Long {
        return time / 1000000L
    }

    fun convertToNS(time: Long): Long {
        return time * 1000000L
    }
}

    fun isNaked(player: EntityPlayer): Boolean {
        for (piece in player.inventory.armorInventory) {
            if (piece == null || piece.isEmpty) continue
            return false
        }
        return true
    }

    fun getItemDamage(stack: ItemStack): Int {
        return stack.maxDamage - stack.itemDamage
    }

    fun getDamageInPercent(stack: ItemStack): Float {
        return getItemDamage(stack).toFloat() / stack.maxDamage.toFloat() * 100.0f
    }

    fun getRoundedDamage(stack: ItemStack): Int {
        return getDamageInPercent(stack).toInt()
    }

    fun hasDurability(stack: ItemStack): Boolean {
        val item = stack.item
        return item is ItemArmor || item is ItemSword || item is ItemTool || item is ItemShield
    }

    fun canBreakWeakness(player: EntityPlayer?): Boolean {
        var strengthAmp = 0
        val effect = mc.player.getActivePotionEffect(MobEffects.STRENGTH)
        if (effect != null) {
            strengthAmp = effect.amplifier
        }
        return !mc.player.isPotionActive(MobEffects.WEAKNESS) || strengthAmp >= 1 || mc.player.heldItemMainhand.item is ItemSword || mc.player.heldItemMainhand.item is ItemPickaxe || mc.player.heldItemMainhand.item is ItemAxe || mc.player.heldItemMainhand.item is ItemSpade
    }

    fun calculateDamage(posX: Double, posY: Double, posZ: Double, entity: Entity): Float {
        val doubleExplosionSize = 12.0f
        val distancedsize = entity.getDistance(posX, posY, posZ) / doubleExplosionSize.toDouble()
        val vec3d = Vec3d(posX, posY, posZ)
        var blockDensity = 0.0
        try {
            blockDensity = entity.world.getBlockDensity(vec3d, entity.entityBoundingBox).toDouble()
        } catch (exception: Exception) {
            // empty catch block
        }
        val v = (1.0 - distancedsize) * blockDensity
        val damage = ((v * v + v) / 2.0 * 7.0 * doubleExplosionSize.toDouble() + 1.0).toInt().toFloat()
        var finald = 1.0
        if (entity is EntityLivingBase) {
            finald = getBlastReduction(entity, getDamageMultiplied(damage), Explosion(mc.world as World, null, posX, posY, posZ, 6.0f, false, true)).toDouble()
        }
        return finald.toFloat()
    }

    fun getBlastReduction(entity: EntityLivingBase, damageI: Float, explosion: Explosion?): Float {
        var damage = damageI
        if (entity is EntityPlayer) {
            val ep = entity
            damage = CombatRules.getDamageAfterAbsorb(damage, ep.totalArmorValue.toFloat(), ep.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).attributeValue.toFloat())
            var k = 0

            val f = MathHelper.clamp(k.toFloat(), 0.0f, 20.0f)
            damage *= 1.0f - f / 25.0f
            if (entity.isPotionActive(MobEffects.RESISTANCE)) {
                damage -= damage / 4.0f
            }
            damage = Math.max(damage, 0.0f)
            return damage
        }
        damage = CombatRules.getDamageAfterAbsorb(damage, entity.totalArmorValue.toFloat(), entity.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).attributeValue.toFloat())
        return damage
    }

    fun getDamageMultiplied(damage: Float): Float {
        val diff = mc.world.difficulty.id
        return damage * if (diff == 0) 0.0f else if (diff == 2) 1.0f else if (diff == 1) 0.5f else 1.5f
    }

    fun calculateDamage(crystal: Entity, entity: Entity): Float {
        return calculateDamage(crystal.posX, crystal.posY, crystal.posZ, entity)
    }

    fun calculateDamage(pos: BlockPos, entity: Entity): Float {
        return calculateDamage(pos.x.toDouble() + 0.5, (pos.y + 1).toDouble(), pos.z.toDouble() + 0.5, entity)
    }

    fun canTakeDamage(suicide: Boolean): Boolean {
        return mc.player.capabilities.isCreativeMode && !suicide
    }

    fun getCooldownByWeapon(player: EntityPlayer): Int {
        val item = player.heldItemMainhand.item
        if (item is ItemSword) {
            return 600
        }
        if (item is ItemPickaxe) {
            return 850
        }
        if (item === Items.IRON_AXE) {
            return 1100
        }
        if (item === Items.STONE_HOE) {
            return 500
        }
        if (item === Items.IRON_HOE) {
            return 350
        }
        if (item === Items.WOODEN_AXE || item === Items.STONE_AXE) {
            return 1250
        }
        return if (item is ItemSpade || item === Items.GOLDEN_AXE || item === Items.DIAMOND_AXE || item === Items.WOODEN_HOE || item === Items.GOLDEN_HOE) {
            1000
        } else 250
    }


private val Int.value: Byte
    get() {
        return value
    }
val Boolean.value: Any
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
    for (pos in possiblePlacePositions((range.value.toFloat() as Int).toFloat())!!) {
        val damage = DamageUtil.calculateDamage(pos, mc.player as Entity)
        if (damage > 2.0f || deadlyCrystal.getDistanceSq(pos) > 144.0 || mc.player.getDistanceSq(pos) >= square(
                wallsRange.value.toFloat() as Double) && rayTracePlaceCheck(pos, true, 1.0f)
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

private operator fun Any.iterator(): Iterator<BlockPos> {
    return iterator()

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
                entity) > square(
                wallsRange.value.toFloat() as Double)) continue
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
        placeCrystalOnBlock(targets[targets.size - 1],
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
        breakTimer.reset()
        targets.clear()
    }
}

private fun rotateTo(entity: Entity?) {
    if (rotate.value.toBoolean()) {
        val angle = calcAngle(mc.player.getPositionEyes(mc.renderPartialTicks), entity!!.positionVector)
        yaw = angle[0]
        pitch = angle[1]
        rotating = true
    }
}

private fun rotateToPos(pos: BlockPos) {
    if (rotate.value.toBoolean()) {
        val angle = calcAngle(mc.player.getPositionEyes(mc.renderPartialTicks),
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
