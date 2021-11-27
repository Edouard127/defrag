package com.lambda.client.util

import net.minecraft.client.Minecraft
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import com.lambda.client.util.DamageUtil
import net.minecraft.item.ItemArmor
import net.minecraft.item.ItemSword
import net.minecraft.item.ItemTool
import net.minecraft.item.ItemShield
import net.minecraft.potion.PotionEffect
import net.minecraft.init.MobEffects
import net.minecraft.item.ItemPickaxe
import net.minecraft.item.ItemAxe
import net.minecraft.item.ItemSpade
import net.minecraft.util.math.Vec3d
import net.minecraft.entity.EntityLivingBase
import net.minecraft.world.Explosion
import net.minecraft.world.World
import net.minecraft.util.DamageSource
import net.minecraft.util.CombatRules
import net.minecraft.entity.SharedMonsterAttributes
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.entity.Entity
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.BlockPos
import net.minecraft.init.Items
import java.lang.Exception

object DamageUtil {
    private val mc: Minecraft? = null
    fun isArmorLow(player: EntityPlayer, durability: Int): Boolean {
        for (piece in player.inventory.armorInventory) {
            if (piece == null) {
                return true
            }
            if (getItemDamage(piece) >= durability) continue
            return true
        }
        return false
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
        val effect = mc!!.player.getActivePotionEffect(MobEffects.STRENGTH)
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
            finald = getBlastReduction(
                entity, getDamageMultiplied(damage), Explosion(
                    mc!!.world as World, null, posX, posY, posZ, 6.0f, false, true
                )
            ).toDouble()
        }
        return finald.toFloat()
    }

    fun getBlastReduction(entity: EntityLivingBase, damageI: Float, explosion: Explosion?): Float {
        var damage = damageI
        if (entity is EntityPlayer) {
            val ep = entity
            val ds = DamageSource.causeExplosionDamage(explosion)
            damage = CombatRules.getDamageAfterAbsorb(
                damage,
                ep.totalArmorValue.toFloat(),
                ep.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).attributeValue.toFloat()
            )
            var k = 0

            val f = MathHelper.clamp(k.toFloat(), 0.0f, 20.0f)
            damage *= 1.0f - f / 25.0f
            if (entity.isPotionActive(MobEffects.RESISTANCE)) {
                damage -= damage / 4.0f
            }
            damage = damage.coerceAtLeast(0.0f)
            return damage
        }
        damage = CombatRules.getDamageAfterAbsorb(
            damage,
            entity.totalArmorValue.toFloat(),
            entity.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).attributeValue.toFloat()
        )
        return damage
    }

    fun getDamageMultiplied(damage: Float): Float {
        val diff = mc!!.world.difficulty.id
        return damage * if (diff == 0) 0.0f else if (diff == 2) 1.0f else if (diff == 1) 0.5f else 1.5f
    }

    fun calculateDamage(crystal: Entity, entity: Entity): Float {
        return calculateDamage(crystal.posX, crystal.posY, crystal.posZ, entity)
    }

    fun calculateDamage(pos: BlockPos, entity: Entity): Float {
        return calculateDamage(pos.x.toDouble() + 0.5, (pos.y + 1).toDouble(), pos.z.toDouble() + 0.5, entity)
    }

    fun canTakeDamage(suicide: Boolean): Boolean {
        return !mc!!.player.capabilities.isCreativeMode && !suicide
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
}