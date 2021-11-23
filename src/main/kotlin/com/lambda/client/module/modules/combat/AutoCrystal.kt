package com.lambda.client.module.modules.combat
/*
import baritone.by
import com.lambda.client.manager.managers.CombatManager
import com.lambda.client.module.modules.EventStage.*
import com.lambda.client.module.modules.events.PacketEvent.*
import com.lambda.client.util.BlockUtil.placeCrystalOnBlock
import com.lambda.client.util.DamageUtil.calculateDamage
import com.lambda.client.util.DamageUtil.canTakeDamage
import com.lambda.client.util.DamageUtil.isNaked
import com.lambda.client.util.Timer.*
import com.lambda.client.util.RenderUtil.drawBoxESP
import com.lambda.client.util.RenderUtil.drawText
import com.lambda.client.module.AbstractModule.*
import com.lambda.client.util.DamageUtil.canBreakWeakness
import com.lambda.client.util.MathUtil.sortByValue
import com.lambda.client.util.DamageUtil.isArmorLow
import com.lambda.client.util.InventoryUtil.switchToHotbarSlot
import com.lambda.client.util.BlockUtil.rayTracePlaceCheck
import com.lambda.client.util.MathUtil.extrapolatePlayerPosition
import com.lambda.client.util.MathUtil.calcAngle
import java.util.concurrent.ConcurrentLinkedQueue
import net.minecraft.util.math.BlockPos
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.ScheduledExecutorService
import java.lang.Thread
import net.minecraft.entity.player.EntityPlayer
import java.util.concurrent.ConcurrentHashMap
import net.minecraft.network.play.client.CPacketUseEntity
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import com.lambda.client.module.modules.events.UpdateWalkingPlayerEvent
import net.minecraft.network.play.client.CPacketPlayer
import com.lambda.client.module.AbstractModule
import com.lambda.client.module.modules.combat.CrystalAura.atValue
import net.minecraft.world.World
import net.minecraft.entity.item.EntityEnderCrystal
import net.minecraft.util.EnumHand
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraft.network.play.server.SPacketSoundEffect
import net.minecraft.network.play.server.SPacketSpawnObject
import net.minecraft.network.play.server.SPacketExplosion
import net.minecraft.network.play.server.SPacketDestroyEntities
import net.minecraft.network.play.server.SPacketEntityStatus
import net.minecraft.util.SoundCategory
import net.minecraft.init.SoundEvents
import net.minecraft.network.play.client.CPacketAnimation
import com.lambda.client.module.modules.events.Render3DEvent
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent
import com.lambda.client.module.modules.combat.CrystalAura.setting
import com.lambda.client.event.Phase
import com.lambda.client.event.SafeClientEvent
import com.lambda.client.event.events.OnUpdateWalkingPlayerEvent
import com.lambda.client.event.events.RunGameLoopEvent
import com.lambda.client.manager.managers.HotbarManager
import com.lambda.client.manager.managers.HotbarManager.resetHotbar
import com.lambda.client.manager.managers.HotbarManager.serverSideItem
import com.lambda.client.manager.managers.HotbarManager.spoofHotbar
import com.lambda.client.manager.managers.PlayerPacketManager
import com.lambda.client.manager.managers.PlayerPacketManager.sendPlayerPacket
import com.lambda.client.mixin.extension.id
import com.lambda.client.mixin.extension.packetAction
import com.lambda.client.module.Category
import com.lambda.client.module.Module
import com.lambda.client.module.modules.combat.CrystalAuraEPIC.breakTimer
import com.lambda.client.util.EntityUtils
import com.lambda.client.util.InfoCalculator
import com.lambda.client.util.TickTimer
import com.lambda.client.util.combat.CombatUtils.equipBestWeapon
import com.lambda.client.util.combat.CombatUtils.scaledHealth
import com.lambda.client.util.combat.CrystalUtils
import com.lambda.client.util.combat.CrystalUtils.getCrystalBB
import com.lambda.client.util.combat.CrystalUtils.getCrystalList
import com.lambda.client.util.items.*
import com.lambda.client.util.math.RotationUtils
import com.lambda.client.util.math.RotationUtils.getRotationTo
import com.lambda.client.util.math.RotationUtils.getRotationToEntity
import com.lambda.client.util.math.VectorUtils.distanceTo
import com.lambda.client.util.math.VectorUtils.toBlockPos
import com.lambda.client.util.math.VectorUtils.toVec3d
import com.lambda.client.util.math.VectorUtils.toVec3dCenter
import com.lambda.client.util.text.MessageSendHelper
import com.lambda.client.util.threads.runSafeR
import com.lambda.client.util.threads.safeListener
import com.lambda.client.util.world.getClosestVisibleSide
import com.lambda.commons.extension.synchronized
import com.lambda.commons.interfaces.DisplayEnum
import com.lambda.event.listener.listener
import net.minecraft.init.Items
import com.lambda.client.module.modules.events.ClientEvent
import com.lambda.client.util.*
import com.lambda.client.util.Timer
import net.minecraft.item.ItemPickaxe
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.item.ItemEndCrystal
import net.minecraft.block.state.IBlockState
import net.minecraft.init.Blocks
import com.mojang.authlib.GameProfile
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.util.math.Vec3d
import net.minecraft.entity.EntityLivingBase
import net.minecraft.util.math.RayTraceResult
import net.minecraft.util.EnumFacing
import java.lang.Runnable
import java.lang.InterruptedException
import io.netty.util.internal.ConcurrentSet
import net.minecraft.block.Block
import net.minecraft.entity.Entity
import net.minecraft.network.Packet
import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse
import java.awt.Color
import java.lang.Exception
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@CombatManager.CombatModule
object CrystalAuraEPIC : Module(
    name = "CrystalAuraEPIC",
    alias = arrayOf("CA", "AC", "AutoCrystal"),
    description = "Places End Crystals to kill enemies",
    category = Category.COMBAT,
    modulePriority = 80
) {
    /* Settings */
    private val page = setting("Page", CrystalAura.Page.GENERAL)

    public val raytrace = by setting("Raytrace", (Object) Raytrace.NONE, v - > this.setting.getValue() == Settings.MISC));
    public val place = by setting("Place", Boolean.valueOf(true), v - > this.setting.getValue() == Settings.PLACE));
    public val placeDelay = by setting("PlaceDelay", Integer.valueOf(25), Integer.valueOf(0), Integer.valueOf(500), v - > this.setting.getValue() == Settings.PLACE && this.place.getValue() != false));
    public val placeRange = by setting("PlaceRange", Float.valueOf(6.0 f), Float.valueOf(0.0 f), Float.valueOf(10.0 f), v - > this.setting.getValue() == Settings.PLACE && this.place.getValue() != false));
    public val minDamage = by setting("MinDamage", Float.valueOf(7.0 f), Float.valueOf(0.1 f), Float.valueOf(20.0 f), v - > this.setting.getValue() == Settings.PLACE && this.place.getValue() != false));
    public val maxSelfPlace = by setting("MaxSelfPlace", Float.valueOf(10.0 f), Float.valueOf(0.1 f), Float.valueOf(36.0 f), v - > this.setting.getValue() == Settings.PLACE && this.place.getValue() != false));
    public val wasteAmount = by setting("WasteAmount", Integer.valueOf(2), Integer.valueOf(1), Integer.valueOf(5), v - > this.setting.getValue() == Settings.PLACE && this.place.getValue() != false));
    public val wasteMinDmgCount = by setting("CountMinDmg", Boolean.valueOf(true), v - > this.setting.getValue() == Settings.PLACE && this.place.getValue() != false));
    public val facePlace = by setting("FacePlace", Float.valueOf(8.0 f), Float.valueOf(0.1 f), Float.valueOf(20.0 f), v - > this.setting.getValue() == Settings.PLACE && this.place.getValue() != false));
    public val placetrace = by setting("Placetrace", (4.5 f), Float.valueOf(0.0 f), Float.valueOf(10.0 f), v - > this.setting.getValue() == Settings.PLACE && this.place.getValue() != false && this.raytrace.getValue() != Raytrace.NONE && this.raytrace.getValue() != Raytrace.BREAK));
    public val antiSurround = by setting("AntiSurround", Boolean.valueOf(true), v - > this.setting.getValue() == Settings.PLACE && this.place.getValue() != false));
    public val limitFacePlace = by setting("LimitFacePlace", Boolean.valueOf(true), v - > this.setting.getValue() == Settings.PLACE && this.place.getValue() != false));
    public val oneDot15 = by setting("1.15", Boolean.valueOf(false), v - > this.setting.getValue() == Settings.PLACE && this.place.getValue() != false));
    public val doublePop = by setting("AntiTotem", Boolean.valueOf(false), v - > this.setting.getValue() == Settings.PLACE && this.place.getValue() != false));
    public val popHealth = by setting("PopHealth", Double.valueOf(1.0), Double.valueOf(0.0), Double.valueOf(3.0), v - > this.setting.getValue() == Settings.PLACE && this.place.getValue() != false && this.doublePop.getValue() != false));
    public val popDamage = by setting("PopDamage", Float.valueOf(4.0 f), Float.valueOf(0.0 f), Float.valueOf(6.0 f), v - > this.setting.getValue() == Settings.PLACE && this.place.getValue() != false && this.doublePop.getValue() != false));
    public val popTime = by setting("PopTime", Integer.valueOf(500), Integer.valueOf(0), Integer.valueOf(1000), v - > this.setting.getValue() == Settings.PLACE && this.place.getValue() != false && this.doublePop.getValue() != false));
    public val explode = by setting("Break", Boolean.valueOf(true), v - > this.setting.getValue() == Settings.BREAK));
    public val switchMode = by setting("Attack", (Object) Switch.BREAKSLOT, v - > this.setting.getValue() == Settings.BREAK && this.explode.getValue() != false));
    public val breakDelay = by setting("BreakDelay", Integer.valueOf(50), Integer.valueOf(0), Integer.valueOf(500), v - > this.setting.getValue() == Settings.BREAK && this.explode.getValue() != false));
    public val breakRange = by setting("BreakRange", Float.valueOf(6.0 f), Float.valueOf(0.0 f), Float.valueOf(10.0 f), v - > this.setting.getValue() == Settings.BREAK && this.explode.getValue() != false));
    public val packets = by setting(new Setting < Object > ("Packets", Integer.valueOf(1), Integer.valueOf(1), Integer.valueOf(6), v - > this.setting.getValue() == Settings.BREAK && this.explode.getValue() != false));
    public val maxSelfBreak = by setting("MaxSelfBreak", Float.valueOf(10.0 f), Float.valueOf(0.1 f), Float.valueOf(36.0 f), v - > this.setting.getValue() == Settings.BREAK && this.explode.getValue() != false));
    public val breaktrace = by setting("Breaktrace", Float.valueOf(4.5 f), Float.valueOf(0.0 f), Float.valueOf(10.0 f), v - > this.setting.getValue() == Settings.BREAK && this.explode.getValue() != false && this.raytrace.getValue() != Raytrace.NONE && this.raytrace.getValue() != Raytrace.PLACE));
    public val manual = by setting("Manual", Boolean.valueOf(true), v - > this.setting.getValue() == Settings.BREAK));
    public val manualMinDmg = by setting("ManMinDmg", Boolean.valueOf(true), v - > this.setting.getValue() == Settings.BREAK && this.manual.getValue() != false));
    public val manualBreak = by setting("ManualDelay", Integer.valueOf(500), Integer.valueOf(0), Integer.valueOf(500), v - > this.setting.getValue() == Settings.BREAK && this.manual.getValue() != false));
    public val sync = by setting("Sync", Boolean.valueOf(true), v - > this.setting.getValue() == Settings.BREAK && (this.explode.getValue() != false || this.manual.getValue() != false)));
    public val instant = by setting("Predict", Boolean.valueOf(true), v - > this.setting.getValue() == Settings.BREAK && this.explode.getValue() != false && this.place.getValue() != false));
    public val instantTimer = by setting("PredictTimer", (Object) PredictTimer.NONE, v - > this.setting.getValue() == Settings.BREAK && this.explode.getValue() != false && this.place.getValue() != false && this.instant.getValue() != false));
    public val resetBreakTimer = by setting("ResetBreakTimer", Boolean.valueOf(true), v - > this.setting.getValue() == Settings.BREAK && this.explode.getValue() != false && this.place.getValue() != false && this.instant.getValue() != false));
    public val predictDelay = by setting("PredictDelay", Integer.valueOf(12), Integer.valueOf(0), Integer.valueOf(500), v - > this.setting.getValue() == Settings.BREAK && this.explode.getValue() != false && this.place.getValue() != false && this.instant.getValue() != false && this.instantTimer.getValue() == PredictTimer.PREDICT));
    public val predictCalc = by setting("PredictCalc", Boolean.valueOf(true), v - > this.setting.getValue() == Settings.BREAK && this.explode.getValue() != false && this.place.getValue() != false && this.instant.getValue() != false));
    public val superSafe = by setting("SuperSafe", Boolean.valueOf(true), v - > this.setting.getValue() == Settings.BREAK && this.explode.getValue() != false && this.place.getValue() != false && this.instant.getValue() != false));
    public val antiCommit = by setting("AntiOverCommit", Boolean.valueOf(true), v - > this.setting.getValue() == Settings.BREAK && this.explode.getValue() != false && this.place.getValue() != false && this.instant.getValue() != false));
    public val render = by setting("Render", Boolean.valueOf(true), v - > this.setting.getValue() == Settings.RENDER));
    public val colorSync = by setting("CSync", Boolean.valueOf(false), v - > this.setting.getValue() == Settings.RENDER));
    public val box = by setting("Box", Boolean.valueOf(true), v - > this.setting.getValue() == Settings.RENDER && this.render.getValue() != false));
    public val outline = by setting("Outline", Boolean.valueOf(true), v - > this.setting.getValue() == Settings.RENDER && this.render.getValue() != false));
    public val text = by setting("Text", Boolean.valueOf(false), v - > this.setting.getValue() == Settings.RENDER && this.render.getValue() != false));
    public val red = by setting("Red", Integer.valueOf(255), Integer.valueOf(0), Integer.valueOf(255), v - > this.setting.getValue() == Settings.RENDER && this.render.getValue() != false));
    public val green = by setting("Green", Integer.valueOf(255), Integer.valueOf(0), Integer.valueOf(255), v - > this.setting.getValue() == Settings.RENDER && this.render.getValue() != false));
    public val blue = by setting("Blue", Integer.valueOf(255), Integer.valueOf(0), Integer.valueOf(255), v - > this.setting.getValue() == Settings.RENDER && this.render.getValue() != false));
    public val alpha = by setting("Alpha", Integer.valueOf(255), Integer.valueOf(0), Integer.valueOf(255), v - > this.setting.getValue() == Settings.RENDER && this.render.getValue() != false));
    public val boxAlpha = by setting("BoxAlpha", Integer.valueOf(125), Integer.valueOf(0), Integer.valueOf(255), v - > this.setting.getValue() == Settings.RENDER && this.render.getValue() != false && this.box.getValue() != false));
    public val lineWidth = by setting("LineWidth", Float.valueOf(1.5 f), Float.valueOf(0.1 f), Float.valueOf(5.0 f), v - > this.setting.getValue() == Settings.RENDER && this.render.getValue() != false && this.outline.getValue() != false));
    public val customOutline = by setting("CustomLine", Boolean.valueOf(false), v - > this.setting.getValue() == Settings.RENDER && this.render.getValue() != false && this.outline.getValue() != false));
    public val cRed = by setting("OL-Red", Integer.valueOf(255), Integer.valueOf(0), Integer.valueOf(255), v - > this.setting.getValue() == Settings.RENDER && this.render.getValue() != false && this.customOutline.getValue() != false && this.outline.getValue() != false));
    public val cGreen = by setting("OL-Green", Integer.valueOf(255), Integer.valueOf(0), Integer.valueOf(255), v - > this.setting.getValue() == Settings.RENDER && this.render.getValue() != false && this.customOutline.getValue() != false && this.outline.getValue() != false));
    public val cBlue = by setting("OL-Blue", Integer.valueOf(255), Integer.valueOf(0), Integer.valueOf(255), v - > this.setting.getValue() == Settings.RENDER && this.render.getValue() != false && this.customOutline.getValue() != false && this.outline.getValue() != false));
    public val cAlpha = by setting("OL-Alpha", Integer.valueOf(255), Integer.valueOf(0), Integer.valueOf(255), v - > this.setting.getValue() == Settings.RENDER && this.render.getValue() != false && this.customOutline.getValue() != false && this.outline.getValue() != false));
    public val holdFacePlace = by setting("HoldFacePlace", Boolean.valueOf(false), v - > this.setting.getValue() == Settings.MISC));
    public val holdFaceBreak = by setting("HoldSlowBreak", Boolean.valueOf(false), v - > this.setting.getValue() == Settings.MISC && this.holdFacePlace.getValue() != false));
    public val slowFaceBreak = by setting("SlowFaceBreak", Boolean.valueOf(false), v - > this.setting.getValue() == Settings.MISC));
    public val actualSlowBreak = by setting("ActuallySlow", Boolean.valueOf(false), v - > this.setting.getValue() == Settings.MISC));
    public val facePlaceSpeed = by setting("FaceSpeed", Integer.valueOf(500), Integer.valueOf(0), Integer.valueOf(500), v - > this.setting.getValue() == Settings.MISC));
    public val antiNaked = by setting("AntiNaked", Boolean.valueOf(true), v - > this.setting.getValue() == Settings.MISC));
    public val range = by setting("Range", Float.valueOf(12.0 f), Float.valueOf(0.1 f), Float.valueOf(20.0 f), v - > this.setting.getValue() == Settings.MISC));
    public val targetMode = by setting("Target", (Object) Target.CLOSEST, v - > this.setting.getValue() == Settings.MISC));
    public val minArmor = by setting("MinArmor", Integer.valueOf(5), Integer.valueOf(0), Integer.valueOf(125), v - > this.setting.getValue() == Settings.MISC));
    public val switchCooldown = by setting("Cooldown", Integer.valueOf(500), Integer.valueOf(0), Integer.valueOf(1000), v - > this.setting.getValue() == Settings.MISC));
    public val autoSwitch = by setting("Switch", (Object) AutoSwitch.TOGGLE, v - > this.setting.getValue() == Settings.MISC));
    public val switchBind = by setting("SwitchBind", new Bind(-1), v - > this.setting.getValue() == Settings.MISC && this.autoSwitch.getValue() == AutoSwitch.TOGGLE));
    public val offhandSwitch = by setting("Offhand", Boolean.valueOf(true), v - > this.setting.getValue() == Settings.MISC && this.autoSwitch.getValue() != AutoSwitch.NONE));
    public val switchBack = by setting("Switchback", Boolean.valueOf(true), v - > this.setting.getValue() == Settings.MISC && this.autoSwitch.getValue() != AutoSwitch.NONE && this.offhandSwitch.getValue() != false));
    public val lethalSwitch = by setting("LethalSwitch", Boolean.valueOf(false), v - > this.setting.getValue() == Settings.MISC && this.autoSwitch.getValue() != AutoSwitch.NONE));
    public val mineSwitch = by setting("MineSwitch", Boolean.valueOf(true), v - > this.setting.getValue() == Settings.MISC && this.autoSwitch.getValue() != AutoSwitch.NONE));
    public val rotate = by setting("Rotate", (Object) Rotate.OFF, v - > this.setting.getValue() == Settings.MISC));
    public val suicide = by setting("Suicide", Boolean.valueOf(false), v - > this.setting.getValue() == Settings.MISC));
    public val webAttack = by setting("WebAttack", Boolean.valueOf(true), v - > this.setting.getValue() == Settings.MISC && this.targetMode.getValue() != Target.DAMAGE));
    public val fullCalc = by setting("ExtraCalc", Boolean.valueOf(false), v - > this.setting.getValue() == Settings.MISC));
    public val sound = by setting("Sound", Boolean.valueOf(true), v - > this.setting.getValue() == Settings.MISC));
    public val soundRange = by setting("SoundRange", Float.valueOf(12.0 f), Float.valueOf(0.0 f), Float.valueOf(12.0 f), v - > this.setting.getValue() == Settings.MISC));
    public val soundPlayer = by setting("SoundPlayer", Float.valueOf(6.0 f), Float.valueOf(0.0 f), Float.valueOf(12.0 f), v - > this.setting.getValue() == Settings.MISC));
    public val soundConfirm = by setting("SoundConfirm", Boolean.valueOf(true), v - > this.setting.getValue() == Settings.MISC));
    public val extraSelfCalc = by setting("MinSelfDmg", Boolean.valueOf(false), v - > this.setting.getValue() == Settings.MISC));
    public val antiFriendPop = by setting("FriendPop", (Object) AntiFriendPop.NONE, v - > this.setting.getValue() == Settings.MISC));
    public val noCount = by setting("AntiCount", Boolean.valueOf(false), v - > this.setting.getValue() == Settings.MISC && (this.antiFriendPop.getValue() == AntiFriendPop.ALL || this.antiFriendPop.getValue() == AntiFriendPop.BREAK)));
    public val calcEvenIfNoDamage = by setting("BigFriendCalc", Boolean.valueOf(false), v - > this.setting.getValue() == Settings.MISC && (this.antiFriendPop.getValue() == AntiFriendPop.ALL || this.antiFriendPop.getValue() == AntiFriendPop.BREAK) && this.targetMode.getValue() != Target.DAMAGE));
    public val predictFriendDmg = by setting("PredictFriend", Boolean.valueOf(false), v - > this.setting.getValue() == Settings.MISC && (this.antiFriendPop.getValue() == AntiFriendPop.ALL || this.antiFriendPop.getValue() == AntiFriendPop.BREAK) && this.instant.getValue() != false));
    public val minMinDmg = by setting("MinMinDmg", Float.valueOf(0.0 f), Float.valueOf(0.0 f), Float.valueOf(3.0 f), v - > this.setting.getValue() == Settings.DEV && this.place.getValue() != false));
    public val attackOppositeHand = by setting("OppositeHand", Boolean.valueOf(false), v - > this.setting.getValue() == Settings.DEV));
    public val removeAfterAttack = by setting("AttackRemove", Boolean.valueOf(false), v - > this.setting.getValue() == Settings.DEV));
    public val antiBlock = by setting"AntiFeetPlace", Boolean.valueOf(false), v - > this.setting.getValue() == Settings.DEV));
    public val breakSwing = by setting("BreakSwing", Boolean.valueOf(true), v - > this.setting.getValue() == Settings.DEV));
    public val placeSwing = by setting("PlaceSwing", Boolean.valueOf(false), v - > this.setting.getValue() == Settings.DEV));
    public val exactHand = by setting("ExactHand", Boolean.valueOf(false), v - > this.setting.getValue() == Settings.DEV && this.placeSwing.getValue() != false));
    public val justRender = by setting("JustRender", Boolean.valueOf(false), v - > this.setting.getValue() == Settings.DEV));
    public val fakeSwing = by setting("FakeSwing", Boolean.valueOf(false), v - > this.setting.getValue() == Settings.DEV && this.justRender.getValue() != false));
    public val logic = by setting("Logic", (Object) Logic.BREAKPLACE, v - > this.setting.getValue() == Settings.DEV));
    public val damageSync = by setting("DamageSync", (Object) DamageSync.NONE, v - > this.setting.getValue() == Settings.DEV));
    public val damageSyncTime = by setting("SyncDelay", Integer.valueOf(500), Integer.valueOf(0), Integer.valueOf(500), v - > this.setting.getValue() == Settings.DEV && this.damageSync.getValue() != DamageSync.NONE));
    public val dropOff = by setting("DropOff", Float.valueOf(5.0 f), Float.valueOf(0.0 f), Float.valueOf(10.0 f), v - > this.setting.getValue() == Settings.DEV && this.damageSync.getValue() == DamageSync.BREAK));
    public val confirm = by setting("Confirm", Integer.valueOf(250), Integer.valueOf(0), Integer.valueOf(1000), v - > this.setting.getValue() == Settings.DEV && this.damageSync.getValue() != DamageSync.NONE));
    public val syncedFeetPlace = by setting("FeetSync", Boolean.valueOf(false), v - > this.setting.getValue() == Settings.DEV && this.damageSync.getValue() != DamageSync.NONE));
    public val fullSync = by setting("FullSync", Boolean.valueOf(false), v - > this.setting.getValue() == Settings.DEV && this.damageSync.getValue() != DamageSync.NONE && this.syncedFeetPlace.getValue() != false));
    public val syncCount = by setting("SyncCount", Boolean.valueOf(true), v - > this.setting.getValue() == Settings.DEV && this.damageSync.getValue() != DamageSync.NONE && this.syncedFeetPlace.getValue() != false));
    public val hyperSync = by setting("HyperSync", Boolean.valueOf(false), v - > this.setting.getValue() == Settings.DEV && this.damageSync.getValue() != DamageSync.NONE && this.syncedFeetPlace.getValue() != false));
    public val gigaSync = by setting("GigaSync", Boolean.valueOf(false), v - > this.setting.getValue() == Settings.DEV && this.damageSync.getValue() != DamageSync.NONE && this.syncedFeetPlace.getValue() != false));
    public val syncySync = by setting("SyncySync", Boolean.valueOf(false), v - > this.setting.getValue() == Settings.DEV && this.damageSync.getValue() != DamageSync.NONE && this.syncedFeetPlace.getValue() != false));
    public val enormousSync = by setting("EnormousSync", Boolean.valueOf(false), v - > this.setting.getValue() == Settings.DEV && this.damageSync.getValue() != DamageSync.NONE && this.syncedFeetPlace.getValue() != false));
    public val holySync by setting(new Setting < Object > ("UnbelievableSync", Boolean.valueOf(false), v - > this.setting.getValue() == Settings.DEV && this.damageSync.getValue() != DamageSync.NONE && this.syncedFeetPlace.getValue() != false));
    public val eventMode = by setting("Updates", Integer.valueOf(3), Integer.valueOf(1), Integer.valueOf(3), v - > this.setting.getValue() == Settings.DEV));
    public val  rotateFirst = by setting("FirstRotation", Boolean.valueOf(false), v - > this.setting.getValue() == Settings.DEV && this.rotate.getValue() != Rotate.OFF && this.eventMode.getValue() == 2));
    public val threadMode = by setting("Thread", (Object) ThreadMode.NONE, v - > this.setting.getValue() == Settings.DEV));
    public val threadDelay = by setting("ThreadDelay", Integer.valueOf(50), Integer.valueOf(1), Integer.valueOf(1000), v - > this.setting.getValue() == Settings.DEV && this.threadMode.getValue() != ThreadMode.NONE));
    public val syncThreadBool = by setting("ThreadSync", Boolean.valueOf(true), v - > this.setting.getValue() == Settings.DEV && this.threadMode.getValue() != ThreadMode.NONE));
    public val syncThreads = by setting("SyncThreads", Integer.valueOf(1000), Integer.valueOf(1), Integer.valueOf(10000), v - > this.setting.getValue() == Settings.DEV && this.threadMode.getValue() != ThreadMode.NONE && this.syncThreadBool.getValue() != false));
    public val predictPos = by setting("PredictPos", Boolean.valueOf(false), v - > this.setting.getValue() == Settings.DEV));
    public val renderExtrapolation = by setting("RenderExtrapolation", Boolean.valueOf(false), v - > this.setting.getValue() == Settings.DEV && this.predictPos.getValue() != false));
    public val predictTicks = by setting("ExtrapolationTicks", Integer.valueOf(2), Integer.valueOf(1), Integer.valueOf(20), v - > this.setting.getValue() == Settings.DEV && this.predictPos.getValue() != false));
    public val rotations = by setting("Spoofs", Integer.valueOf(1), Integer.valueOf(1), Integer.valueOf(20), v - > this.setting.getValue() == Settings.DEV));
    public val predictRotate = by setting("PredictRotate", Boolean.valueOf(false), v - > this.setting.getValue() == Settings.DEV));
    public val predictOffset = by setting("PredictOffset", Float.valueOf(0.0 f), Float.valueOf(0.0 f), Float.valueOf(4.0 f), v - > this.setting.getValue() == Settings.DEV));
    public val brownZombie = by setting("BrownZombieMode", Boolean.valueOf(false), v - > this.setting.getValue() == Settings.MISC));
    public val doublePopOnDamage = by setting("DamagePop", Boolean.valueOf(false), v - > this.setting.getValue() == Settings.PLACE && this.place.getValue() != false && this.doublePop.getValue() != false && this.targetMode.getValue() == Target.DAMAGE));
    private var attackList: Queue<Entity> = ConcurrentLinkedQueue()
    private var crystalMap: MutableMap<Entity, Float?> = HashMap()
    private val switchTimer = Timer()
    private val manualTimer = Timer()
    private val breakTimer = Timer()
    private val placeTimer = Timer()
    private val syncTimer = Timer()
    private val predictTimer = Timer()
    private var efficientTarget: Entity? = null
    private var currentDamage = 0.0
    private var renderDamage = 0.0
    private var lastDamage = 0.0
    private var didRotation = false
    private var switching = false
    private var placePos: BlockPos? = null
    private var renderPos: BlockPos? = null
    private var mainHand = false
    var rotating = false
    private var offHand = false
    private var crystalCount = 0
    private var minDmgCount = 0
    private var lastSlot = -1
    private var yaw = 0.0f
    private var pitch = 0.0f
    private var webPos: BlockPos? = null
    private val renderTimer = Timer()
    private var lastPos: BlockPos? = null
    private var posConfirmed = false
    private var foundDoublePop = false
    private var rotationPacketsSpoofed = 0
    private val shouldInterrupt = AtomicBoolean(false)
    private var executor: ScheduledExecutorService? = null
    private val syncroTimer = Timer()
    private var thread: Thread? = null
    private var currentSyncTarget: EntityPlayer? = null
    private var syncedPlayerPos: BlockPos? = null
    private var syncedCrystalPos: BlockPos? = null
    private val totemPops: MutableMap<EntityPlayer?, Timer> = ConcurrentHashMap()
    private val packetUseEntities: Queue<CPacketUseEntity> = LinkedList()
    private var placeInfo: PlaceInfo? = null
    private val threadOngoing = AtomicBoolean(false)
    val threadTimer = Timer()
    private var addTolowDmg = false
    fun onTick() {
        if (threadMode.getValue() === ThreadMode.NONE && eventMode.getValue() === 3) {
            doAutoCrystal()
        }
    }

    @SubscribeEvent
    fun onUpdateWalkingPlayer(event: UpdateWalkingPlayerEvent) {
        if (event.getStage() === 1) {
            postProcessing()
        }
        if (event.getStage() !== 0) {
            return
        }
        if (eventMode.getValue() === 2) {
            doAutoCrystal()
        }
    }

    fun postTick() {
        if (threadMode.getValue() !== ThreadMode.NONE) {
            processMultiThreading()
        }
    }

    fun onUpdate() {
        if (threadMode.getValue() === ThreadMode.NONE && eventMode.getValue() === 1) {
            doAutoCrystal()
        }
    }

    fun onToggle() {
        brokenPos.clear()
        placedPos.clear()
        totemPops.clear()
        rotating = false
    }

    fun onDisable() {
        if (thread != null) {
            shouldInterrupt.set(true)
        }
        if (executor != null) {
            executor!!.shutdown()
        }
    }

    fun onEnable() {
        if (threadMode.getValue() !== ThreadMode.NONE) {
            processMultiThreading()
        }
    }

    val displayInfo: String?
        get() {
            if (switching) {
                return "\u00a7aSwitch"
            }
            return if (target != null) {
                target!!.name
            } else null
        }

    @SubscribeEvent
    fun onPacketSend(event: PacketEvent.Send) {
        var packet: CPacketUseEntity
        if (event.stage == 0 && rotate.getValue() !== Rotate.OFF && rotating && eventMode.getValue() !== 2 && event.getPacket<Packet<*>>() is CPacketPlayer) {
            val packet2 = event.getPacket<Packet<*>>() as CPacketPlayer
            packet2.yaw = yaw
            packet2.pitch = pitch
            ++rotationPacketsSpoofed
            if (rotationPacketsSpoofed >= rotations.getValue()) {
                rotating = false
                rotationPacketsSpoofed = 0
            }
        }
        var pos: BlockPos? = null
        if (event.stage == 0 && event.getPacket<Packet<*>>() is CPacketUseEntity && (event.getPacket<Packet<*>>() as CPacketUseEntity?. also {
                packet = it
            }).getAction() == CPacketUseEntity.Action.ATTACK && packet.getEntityFromWorld(
                mc.world as World
            ) is EntityEnderCrystal
        ) {
            pos = packet.getEntityFromWorld(mc.world as World).getPosition()
            if (removeAfterAttack.getValue().booleanValue()) {
                Objects.requireNonNull(packet.getEntityFromWorld(mc.world as World)).setDead()
                mc.world.removeEntityFromWorld(packet.entityId)
            }
        }
        if (event.stage == 0 && event.getPacket<Packet<*>>() is CPacketUseEntity && (event.getPacket<Packet<*>>() as CPacketUseEntity?. also {
                packet = it
            }).getAction() == CPacketUseEntity.Action.ATTACK && packet.getEntityFromWorld(
                mc.world as World
            ) is EntityEnderCrystal
        ) {
            val crystal = packet.getEntityFromWorld(mc.world as World) as EntityEnderCrystal?
            if (antiBlock.getValue().booleanValue() && EntityUtil.isCrystalAtFeet(
                    crystal,
                    range.getValue().floatValue()
                ) && pos != null
            ) {
                rotateToPos(pos)
                placeCrystalOnBlock(
                    placePos!!,
                    if (offHand) EnumHand.OFF_HAND else EnumHand.MAIN_HAND,
                    placeSwing.getValue(),
                    exactHand.getValue()
                )
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH, receiveCanceled = true)
    fun onPacketReceive(event: PacketEvent.Receive) {
        var packet: SPacketSoundEffect
        if (fullNullCheck()) {
            return
        }
        if (!justRender.getValue().booleanValue() && switchTimer.passedMs(
                switchCooldown.getValue().intValue()
            ) && explode.getValue().booleanValue() && instant.getValue()
                .booleanValue() && event.getPacket<Packet<*>>() is SPacketSpawnObject && (syncedCrystalPos == null || !syncedFeetPlace.getValue()
                .booleanValue() || damageSync.getValue() === DamageSync.NONE)
        ) {
            var pos: BlockPos
            val packet2 = event.getPacket<Packet<*>>() as SPacketSpawnObject
            if (packet2.type == 51 && mc.player.getDistanceSq(BlockPos(packet2.x, packet2.y, packet2.z).also {
                    pos = it
                }) + predictOffset.getValue().floatValue() as Double <= MathUtil.square(
                    breakRange.getValue().floatValue()
                ) && (instantTimer.getValue() === PredictTimer.NONE || instantTimer.getValue() === PredictTimer.BREAK && breakTimer.passedMs(
                    breakDelay.getValue().intValue()
                ) || instantTimer.getValue() === PredictTimer.PREDICT && predictTimer.passedMs(
                    predictDelay.getValue().intValue()
                ))
            ) {
                if (predictSlowBreak(pos.down())) {
                    return
                }
                if (predictFriendDmg.getValue()
                        .booleanValue() && (antiFriendPop.getValue() === AntiFriendPop.BREAK || antiFriendPop.getValue() === AntiFriendPop.ALL) && isRightThread
                ) {
                    for (friend in mc.world.playerEntities) {
                        if (friend == null || mc.player == friend as Any || friend.getDistanceSq(pos) > MathUtil.square(
                                range.getValue().floatValue() + placeRange.getValue().floatValue()
                            ) || !Phobos.friendManager.isFriend(friend) || calculateDamage(
                                pos,
                                (friend as Entity)
                            ).toDouble() <= EntityUtil.getHealth(friend as Entity) as Double + 0.5
                        ) continue
                        return
                    }
                }
                if (placedPos.contains(pos.down())) {
                    var selfDamage: Float
                    if (if (isRightThread && superSafe.getValue() !== false) canTakeDamage(suicide.getValue()) && (calculateDamage(
                            pos,
                            (mc.player as Entity)
                        ).also { selfDamage = it }
                            .toDouble() - 0.5 > EntityUtil.getHealth(mc.player as Entity) as Double || selfDamage > maxSelfBreak.getValue()
                            .floatValue()) else superSafe.getValue() !== false
                    ) {
                        return
                    }
                    attackCrystalPredict(packet2.entityID, pos)
                } else if (predictCalc.getValue().booleanValue() && isRightThread) {
                    var selfDamage = -1.0f
                    if (canTakeDamage(suicide.getValue())) {
                        selfDamage = calculateDamage(pos, (mc.player as Entity))
                    }
                    if (selfDamage.toDouble() + 0.5 < EntityUtil.getHealth(mc.player as Entity) as Double && selfDamage <= maxSelfBreak.getValue()
                            .floatValue()
                    ) {
                        for (player in mc.world.playerEntities) {
                            var damage: Float
                            if (player.getDistanceSq(pos) > MathUtil.square(
                                    range.getValue().floatValue()
                                ) || !EntityUtil.isValid(
                                    player as Entity,
                                    range.getValue().floatValue() + breakRange.getValue().floatValue()
                                ) || antiNaked.getValue().booleanValue() && isNaked(player) || !(calculateDamage(
                                    pos,
                                    (player as Entity)
                                ).also { damage = it } > selfDamage || damage > minDamage.getValue()
                                    .floatValue() && !canTakeDamage(
                                    suicide.getValue()
                                )) && damage <= EntityUtil.getHealth(player as Entity)
                            ) continue
                            if (predictRotate.getValue()
                                    .booleanValue() && eventMode.getValue() !== 2 && (rotate.getValue() === Rotate.BREAK || rotate.getValue() === Rotate.ALL)
                            ) {
                                rotateToPos(pos)
                            }
                            attackCrystalPredict(packet2.entityID, pos)
                            break
                        }
                    }
                }
            }
        } else if (!soundConfirm.getValue().booleanValue() && event.getPacket<Packet<*>>() is SPacketExplosion) {
            val packet3 = event.getPacket<Packet<*>>() as SPacketExplosion
            val pos = BlockPos(packet3.x, packet3.y, packet3.z).down()
            removePos(pos)
        } else if (event.getPacket<Packet<*>>() is SPacketDestroyEntities) {
            val packet4 = event.getPacket<Packet<*>>() as SPacketDestroyEntities
            for (id in packet4.entityIDs) {
                val entity = mc.world.getEntityByID(id) as? EntityEnderCrystal ?: continue
                brokenPos.remove(BlockPos(entity.positionVector).down())
                placedPos.remove(BlockPos(entity.positionVector).down())
            }
        } else if (event.getPacket<Packet<*>>() is SPacketEntityStatus) {
            val packet5 = event.getPacket<Packet<*>>() as SPacketEntityStatus
            if (packet5.opCode.toInt() == 35 && packet5.getEntity(mc.world as World) is EntityPlayer) {
                totemPops[packet5.getEntity(mc.world as World) as EntityPlayer] = Timer().reset()
            }
        } else if (event.getPacket<Packet<*>>() is SPacketSoundEffect && (event.getPacket<Packet<*>>() as SPacketSoundEffect?. also {
                packet = it
            }).getCategory() == SoundCategory.BLOCKS && packet.sound === SoundEvents.ENTITY_GENERIC_EXPLODE) {
            val pos = BlockPos(packet.x, packet.y, packet.z)
            if (sound.getValue().booleanValue() || threadMode.getValue() === ThreadMode.SOUND) {
                NoSoundLag.removeEntities(packet, soundRange.getValue().floatValue())
            }
            if (soundConfirm.getValue().booleanValue()) {
                removePos(pos)
            }
            if (threadMode.getValue() === ThreadMode.SOUND && isRightThread && mc.player != null && mc.player.getDistanceSq(
                    pos
                ) < MathUtil.square(
                    soundPlayer.getValue().floatValue()
                )
            ) {
                handlePool(true)
            }
        }
    }

    private fun predictSlowBreak(pos: BlockPos): Boolean {
        return if (antiCommit.getValue().booleanValue() && lowDmgPos.remove(pos)) {
            shouldSlowBreak(false)
        } else false
    }

    private val isRightThread: Boolean
        private get() = mc.isCallingFromMinecraftThread || !Phobos.eventManager.ticksOngoing() && !threadOngoing.get()

    private fun attackCrystalPredict(entityID: Int, pos: BlockPos) {
        if (!(!predictRotate.getValue()
                .booleanValue() || eventMode.getValue() === 2 && threadMode.getValue() === ThreadMode.NONE || rotate.getValue() !== Rotate.BREAK && rotate.getValue() !== Rotate.ALL)
        ) {
            rotateToPos(pos)
        }
        val attackPacket = CPacketUseEntity()
        attackPacket.entityId = entityID
        attackPacket.action = CPacketUseEntity.Action.ATTACK
        mc.player.connection.sendPacket(attackPacket as Packet<*>)
        if (breakSwing.getValue().booleanValue()) {
            mc.player.connection.sendPacket(CPacketAnimation(EnumHand.MAIN_HAND) as Packet<*>)
        }
        if (resetBreakTimer.getValue().booleanValue()) {
            breakTimer.reset()
        }
        predictTimer.reset()
    }

    private fun removePos(pos: BlockPos) {
        if (damageSync.getValue() === DamageSync.PLACE) {
            if (placedPos.remove(pos)) {
                posConfirmed = true
            }
        } else if (damageSync.getValue() === DamageSync.BREAK && brokenPos.remove(pos)) {
            posConfirmed = true
        }
    }

    fun onRender3D(event: Render3DEvent?) {
        if ((offHand || mainHand || switchMode.getValue() === Switch.CALC) && renderPos != null && render.getValue()
                .booleanValue() && (box.getValue().booleanValue() || text.getValue()
                .booleanValue() || outline.getValue().booleanValue())
        ) {
            drawBoxESP(
                renderPos!!,
                if (colorSync.getValue() !== false) Colors.INSTANCE.getCurrentColor() else Color(
                    red.getValue(), green.getValue(), blue.getValue(), alpha.getValue()
                ),
                customOutline.getValue(),
                if (colorSync.getValue() !== false) Colors.INSTANCE.getCurrentColor() else Color(
                    cRed.getValue(), cGreen.getValue(), cBlue.getValue(), cAlpha.getValue()
                ),
                lineWidth.getValue().floatValue(),
                outline.getValue(),
                box.getValue(),
                boxAlpha.getValue(),
                false
            )
            if (text.getValue().booleanValue()) {
                drawText(
                    renderPos, (if (Math.floor(renderDamage) == renderDamage) Integer.valueOf(
                        renderDamage.toInt()
                    ) else String.format("%.1f", renderDamage)).toString() + ""
                )
            }
        }
    }

    @SubscribeEvent
    fun onKeyInput(event: KeyInputEvent?) {
        if (Keyboard.getEventKeyState() && mc.currentScreen !is PhobosGui && switchBind.getValue()
                .getKey() === Keyboard.getEventKey()
        ) {
            if (switchBack.getValue().booleanValue() && offhandSwitch.getValue().booleanValue() && offHand) {
                val module: Offhand = Phobos.moduleManager.getModuleByClass(Offhand::class.java)
                if (module.isOff()) {
                    Command.sendMessage(
                        "<" + this.getDisplayName()
                            .toString() + "> " + "\u00a7c" + "Switch failed. Enable the Offhand module."
                    )
                } else if (module.type.getValue() === Offhand.Type.NEW) {
                    module.setSwapToTotem(true)
                    module.doOffhand()
                } else {
                    module.setMode(Offhand.Mode2.TOTEMS)
                    module.doSwitch()
                }
                return
            }
            switching = !switching
        }
    }

    @SubscribeEvent
    fun onSettingChange(event: ClientEvent) {
        if (event.stage == 2 && event.getSetting() != null && event.getSetting()
                .getFeature() != null && event.getSetting().getFeature()
                .equals(this) && isEnabled && (event.getSetting().equals(
                threadDelay
            ) || event.getSetting().equals(threadMode))
        ) {
            if (executor != null) {
                executor!!.shutdown()
            }
            if (thread != null) {
                shouldInterrupt.set(true)
            }
        }
    }

    private fun postProcessing() {
        if (threadMode.getValue() !== ThreadMode.NONE || eventMode.getValue() !== 2 || rotate.getValue() === Rotate.OFF || !rotateFirst.getValue()
                .booleanValue()
        ) {
            return
        }
        when (logic.getValue()) {
            BREAKPLACE -> {
                postProcessBreak()
                postProcessPlace()
            }
            PLACEBREAK -> {
                postProcessPlace()
                postProcessBreak()
            }
        }
    }

    private fun postProcessBreak() {
        while (!packetUseEntities.isEmpty()) {
            val packet = packetUseEntities.poll()
            mc.player.connection.sendPacket(packet as Packet<*>)
            if (breakSwing.getValue().booleanValue()) {
                mc.player.swingArm(EnumHand.MAIN_HAND)
            }
            breakTimer.reset()
        }
    }

    private fun postProcessPlace() {
        if (placeInfo != null) {
            placeInfo!!.runPlace()
            placeTimer.reset()
            placeInfo = null
        }
    }

    private fun processMultiThreading() {
        if (this.isOff()) {
            return
        }
        if (threadMode.getValue() === ThreadMode.WHILE) {
            handleWhile()
        } else if (threadMode.getValue() !== ThreadMode.NONE) {
            handlePool(false)
        }
    }

    private fun handlePool(justDoIt: Boolean) {
        if (justDoIt || executor == null || executor!!.isTerminated || executor!!.isShutdown || syncroTimer.passedMs(
                syncThreads.getValue().intValue()
            ) && syncThreadBool.getValue().booleanValue()
        ) {
            if (executor != null) {
                executor!!.shutdown()
            }
            executor = getExecutor()
            syncroTimer.reset()
        }
    }

    private fun handleWhile() {
        if (thread == null || thread!!.isInterrupted || !thread!!.isAlive || syncroTimer.passedMs(
                syncThreads.getValue().intValue()
            ) && syncThreadBool.getValue().booleanValue()
        ) {
            if (thread == null) {
                thread = Thread(RAutoCrystal.getInstance(this))
            } else if (syncroTimer.passedMs(
                    syncThreads.getValue().intValue()
                ) && !shouldInterrupt.get() && syncThreadBool.getValue().booleanValue()
            ) {
                shouldInterrupt.set(true)
                syncroTimer.reset()
                return
            }
            if (thread != null && (thread!!.isInterrupted || !thread!!.isAlive)) {
                thread = Thread(RAutoCrystal.getInstance(this))
            }
            if (thread != null && thread!!.state == Thread.State.NEW) {
                try {
                    thread!!.start()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                syncroTimer.reset()
            }
        }
    }

    private fun getExecutor(): ScheduledExecutorService {
        val service = Executors.newSingleThreadScheduledExecutor()
        service.scheduleAtFixedRate(
            RAutoCrystal.getInstance(this),
            0L,
            threadDelay.getValue().intValue(),
            TimeUnit.MILLISECONDS
        )
        return service
    }

    fun doAutoCrystal() {
        if (brownZombie.getValue().booleanValue()) {
            return
        }
        if (this.check()) {
            when (logic.getValue()) {
                PLACEBREAK -> {
                    placeCrystal()
                    breakCrystal()
                }
                BREAKPLACE -> {
                    breakCrystal()
                    placeCrystal()
                }
            }
            manualBreaker()
        }
    }

    private fun check(): Boolean {
        if (fullNullCheck()) {
            return false
        }
        if (syncTimer.passedMs(damageSyncTime.getValue().intValue())) {
            currentSyncTarget = null
            syncedCrystalPos = null
            syncedPlayerPos = null
        } else if (syncySync.getValue().booleanValue() && syncedCrystalPos != null) {
            posConfirmed = true
        }
        foundDoublePop = false
        if (renderTimer.passedMs(500L)) {
            renderPos = null
            renderTimer.reset()
        }
        mainHand = mc.player.heldItemMainhand.item === Items.END_CRYSTAL
        offHand = mc.player.heldItemOffhand.item === Items.END_CRYSTAL
        currentDamage = 0.0
        placePos = null
        if (lastSlot != mc.player.inventory.currentItem || AutoTrap.isPlacing || Surround.isPlacing) {
            lastSlot = mc.player.inventory.currentItem
            switchTimer.reset()
        }
        if (!offHand && !mainHand) {
            placeInfo = null
            packetUseEntities.clear()
        }
        if (offHand || mainHand) {
            switching = false
        }
        if (!((offHand || mainHand || switchMode.getValue() !== Switch.BREAKSLOT || switching) && canBreakWeakness(mc.player as EntityPlayer) && switchTimer.passedMs(
                switchCooldown.getValue().intValue()
            ))
        ) {
            renderPos = null
            target = null
            rotating = false
            return false
        }
        if (mineSwitch.getValue()
                .booleanValue() && Mouse.isButtonDown(0) && (switching || autoSwitch.getValue() === AutoSwitch.ALWAYS) && Mouse.isButtonDown(
                1
            ) && mc.player.heldItemMainhand.item is ItemPickaxe
        ) {
            switchItem()
        }
        mapCrystals()
        if (!posConfirmed && damageSync.getValue() !== DamageSync.NONE && syncTimer.passedMs(
                confirm.getValue().intValue()
            )
        ) {
            syncTimer.setMs(damageSyncTime.getValue() + 1)
        }
        return true
    }

    private fun mapCrystals() {
        efficientTarget = null
        if (packets.getValue() !== 1) {
            attackList = ConcurrentLinkedQueue()
            crystalMap = HashMap()
        }
        crystalCount = 0
        minDmgCount = 0
        var maxCrystal: Entity? = null
        var maxDamage = 0.5f
        for (entity in mc.world.loadedEntityList) {
            if (entity.isDead || entity !is EntityEnderCrystal || !isValid(entity)) continue
            if (syncedFeetPlace.getValue().booleanValue() && entity.getPosition()
                    .down() == syncedCrystalPos as Any? && damageSync.getValue() !== DamageSync.NONE
            ) {
                ++minDmgCount
                ++crystalCount
                if (syncCount.getValue().booleanValue()) {
                    minDmgCount = wasteAmount.getValue() + 1
                    crystalCount = wasteAmount.getValue() + 1
                }
                if (!hyperSync.getValue().booleanValue()) continue
                maxCrystal = null
                break
            }
            var count = false
            var countMin = false
            var selfDamage = -1.0f
            if (canTakeDamage(suicide.getValue())) {
                selfDamage = calculateDamage(entity, (mc.player as Entity))
            }
            if (selfDamage.toDouble() + 0.5 < EntityUtil.getHealth(mc.player as Entity) as Double && selfDamage <= maxSelfBreak.getValue()
                    .floatValue()
            ) {
                val beforeCrystal = maxCrystal
                val beforeDamage = maxDamage
                for (player in mc.world.playerEntities) {
                    var damage: Float
                    if (player.getDistanceSq(entity) > MathUtil.square(range.getValue().floatValue())) continue
                    if (EntityUtil.isValid(
                            player as Entity,
                            range.getValue().floatValue() + breakRange.getValue().floatValue()
                        )
                    ) {
                        if (antiNaked.getValue().booleanValue() && isNaked(player) || !(calculateDamage(
                                entity,
                                (player as Entity)
                            ).also { damage = it } > selfDamage || damage > minDamage.getValue()
                                .floatValue() && !canTakeDamage(
                                suicide.getValue()
                            )) && damage <= EntityUtil.getHealth(player as Entity)
                        ) continue
                        if (damage > maxDamage) {
                            maxDamage = damage
                            maxCrystal = entity
                        }
                        if (packets.getValue() === 1) {
                            if (damage >= minDamage.getValue().floatValue() || !wasteMinDmgCount.getValue()
                                    .booleanValue()
                            ) {
                                count = true
                            }
                            countMin = true
                            continue
                        }
                        if (crystalMap[entity] != null && crystalMap[entity]!!.toFloat() >= damage) continue
                        crystalMap[entity] = java.lang.Float.valueOf(damage)
                        continue
                    }
                    if (antiFriendPop.getValue() !== AntiFriendPop.BREAK && antiFriendPop.getValue() !== AntiFriendPop.ALL || !Phobos.friendManager.isFriend(
                            player.name
                        ) || calculateDamage(entity, (player as Entity)).also { damage = it }
                            .toDouble() <= EntityUtil.getHealth(player as Entity) as Double + 0.5
                    ) continue
                    maxCrystal = beforeCrystal
                    maxDamage = beforeDamage
                    crystalMap.remove(entity)
                    if (!noCount.getValue().booleanValue()) break
                    count = false
                    countMin = false
                    break
                }
            }
            if (!countMin) continue
            ++minDmgCount
            if (!count) continue
            ++crystalCount
        }
        if (damageSync.getValue() === DamageSync.BREAK && (maxDamage.toDouble() > lastDamage || syncTimer.passedMs(
                damageSyncTime.getValue().intValue()
            ) || damageSync.getValue() === DamageSync.NONE)
        ) {
            lastDamage = maxDamage.toDouble()
        }
        if (enormousSync.getValue().booleanValue() && syncedFeetPlace.getValue()
                .booleanValue() && damageSync.getValue() !== DamageSync.NONE && syncedCrystalPos != null
        ) {
            if (syncCount.getValue().booleanValue()) {
                minDmgCount = wasteAmount.getValue() + 1
                crystalCount = wasteAmount.getValue() + 1
            }
            return
        }
        if (webAttack.getValue().booleanValue() && webPos != null) {
            if (mc.player.getDistanceSq(webPos!!.up()) > MathUtil.square(
                    breakRange.getValue().floatValue()
                )
            ) {
                webPos = null
            } else {
                for (entity in mc.world.getEntitiesWithinAABB(
                    Entity::class.java, AxisAlignedBB(webPos!!.up())
                )) {
                    if (entity !is EntityEnderCrystal) continue
                    attackList.add(entity)
                    efficientTarget = entity
                    webPos = null
                    lastDamage = 0.5
                    return
                }
            }
        }
        if (shouldSlowBreak(true) && maxDamage < minDamage.getValue()
                .floatValue() && (target == null || EntityUtil.getHealth(
                target as Entity?
            ) > facePlace.getValue().floatValue() || !breakTimer.passedMs(
                facePlaceSpeed.getValue().intValue()
            ) && slowFaceBreak.getValue().booleanValue() && Mouse.isButtonDown(
                0
            ) && holdFacePlace.getValue().booleanValue() && holdFaceBreak.getValue().booleanValue())
        ) {
            efficientTarget = null
            return
        }
        if (packets.getValue() === 1) {
            efficientTarget = maxCrystal
        } else {
            crystalMap = sortByValue(crystalMap, true)
            for ((key, value) in crystalMap) {
                val damage = (value as Float).toFloat()
                if (damage >= minDamage.getValue().floatValue() || !wasteMinDmgCount.getValue().booleanValue()) {
                    ++crystalCount
                }
                attackList.add(key)
                ++minDmgCount
            }
        }
    }

    private fun shouldSlowBreak(withManual: Boolean): Boolean {
        return withManual && manual.getValue() !== false && manualMinDmg.getValue() !== false && Mouse.isButtonDown(
            1
        ) && (!Mouse.isButtonDown(0) || holdFacePlace.getValue() === false) || holdFacePlace.getValue() !== false && holdFaceBreak.getValue() !== false && Mouse.isButtonDown(
            0
        ) && !breakTimer
            facePlaceSpeed.getValue().intValue()
        ) || slowFaceBreak.getValue() !== false && !breakTimer(
            facePlaceSpeed.getValue().intValue()
        )
    }

    private fun placeCrystal() {
        var crystalLimit: Int = wasteAmount.getValue()
        if (placeTimer.passedMs(placeDelay.getValue().intValue()) && place.getValue()
                .booleanValue() && (offHand || mainHand || switchMode.getValue() === Switch.CALC || switchMode.getValue() === Switch.BREAKSLOT && switching)
        ) {
            if (!(!offHand && !mainHand && (switchMode.getValue() === Switch.ALWAYS || switching) || crystalCount < crystalLimit || antiSurround.getValue()
                    .booleanValue() && lastPos != null && lastPos == placePos as Any?)
            ) {
                return
            }
            this.calculateDamage(getTarget(targetMode.getValue() === Target.UNSAFE))
            if (target != null && placePos != null) {
                if (!offHand && !mainHand && autoSwitch.getValue() !== AutoSwitch.NONE && (currentDamage > minDamage.getValue()
                        .floatValue() as Double || lethalSwitch.getValue().booleanValue() && EntityUtil.getHealth(
                        target as Entity?
                    ) <= facePlace.getValue().floatValue()) && !switchItem()
                ) {
                    return
                }
                if (currentDamage < minDamage.getValue().floatValue() as Double && limitFacePlace.getValue()
                        .booleanValue()
                ) {
                    crystalLimit = 1
                }
                if (currentDamage >= minMinDmg.getValue()
                        .floatValue() as Double && (offHand || mainHand || autoSwitch.getValue() !== AutoSwitch.NONE) && (crystalCount < crystalLimit || antiSurround.getValue()
                        .booleanValue() && lastPos != null && lastPos == placePos as Any?) && (currentDamage > minDamage.getValue()
                        .floatValue() as Double || minDmgCount < crystalLimit) && currentDamage >= 1.0 && (isArmorLow(
                        target!!, minArmor.getValue()
                    ) || EntityUtil.getHealth(target as Entity?) <= facePlace.getValue()
                        .floatValue() || currentDamage > minDamage.getValue()
                        .floatValue() as Double || shouldHoldFacePlace())
                ) {
                    val damageOffset =
                        if (damageSync.getValue() === DamageSync.BREAK) dropOff.getValue().floatValue() - 5.0f else 0.0f
                    var syncflag = false
                    if (syncedFeetPlace.getValue()
                            .booleanValue() && placePos == lastPos as Any? && isEligableForFeetSync(
                            target, placePos
                        ) && !syncTimer.passedMs(
                            damageSyncTime.getValue().intValue()
                        ) && target == currentSyncTarget as Any? && target!!.position == syncedPlayerPos as Any? && damageSync.getValue() !== DamageSync.NONE
                    ) {
                        syncedCrystalPos = placePos
                        lastDamage = currentDamage
                        if (fullSync.getValue().booleanValue()) {
                            lastDamage = 100.0
                        }
                        syncflag = true
                    }
                    if (syncflag || currentDamage - damageOffset.toDouble() > lastDamage || syncTimer.passedMs(
                            damageSyncTime.getValue().intValue()
                        ) || damageSync.getValue() === DamageSync.NONE
                    ) {
                        if (!syncflag && damageSync.getValue() !== DamageSync.BREAK) {
                            lastDamage = currentDamage
                        }
                        renderPos = placePos
                        renderDamage = currentDamage
                        if (switchItem()) {
                            currentSyncTarget = target
                            syncedPlayerPos = target!!.position
                            if (foundDoublePop) {
                                totemPops[target] = Timer().reset()
                            }
                            rotateToPos(placePos)
                            if (addTolowDmg || actualSlowBreak.getValue()
                                    .booleanValue() && currentDamage < minDamage.getValue().floatValue() as Double
                            ) {
                                lowDmgPos.add(placePos)
                            }
                            placedPos.add(placePos)
                            if (!justRender.getValue().booleanValue()) {
                                if (eventMode.getValue() === 2 && threadMode.getValue() === ThreadMode.NONE && rotateFirst.getValue()
                                        .booleanValue() && rotate.getValue() !== Rotate.OFF
                                ) {
                                    placeInfo =
                                        PlaceInfo(placePos, offHand, placeSwing.getValue(), exactHand.getValue())
                                } else {
                                    placeCrystalOnBlock(
                                        placePos!!,
                                        if (offHand) EnumHand.OFF_HAND else EnumHand.MAIN_HAND,
                                        placeSwing.getValue(),
                                        exactHand.getValue()
                                    )
                                }
                            }
                            lastPos = placePos
                            placeTimer.reset()
                            posConfirmed = false
                            if (syncTimer.passedMs(damageSyncTime.getValue().intValue())) {
                                syncedCrystalPos = null
                                syncTimer.reset()
                            }
                        }
                    }
                }
            } else {
                renderPos = null
            }
        }
    }

    private fun shouldHoldFacePlace(): Boolean {
        addTolowDmg = false
        if (holdFacePlace.getValue().booleanValue() && Mouse.isButtonDown(0)) {
            addTolowDmg = true
            return true
        }
        return false
    }

    private fun switchItem(): Boolean {
        if (offHand || mainHand) {
            return true
        }
        when (autoSwitch.getValue()) {
            NONE -> {
                return false
            }
            TOGGLE -> {
                run {
                    if (!this.switching) {
                        return false
                    }
                }
                run {
                    if (!this.doSwitch()) break
                    return true
                }
            }
            ALWAYS -> {
                if (!doSwitch()) break
                return true
            }
        }
        return false
    }

    private fun doSwitch(): Boolean {
        if (offhandSwitch.getValue().booleanValue()) {
            val module: Offhand = Phobos.moduleManager.getModuleByClass(Offhand::class.java)
            if (module.isOff()) {
                Command.sendMessage(
                    "<" + this.getDisplayName()
                        .toString() + "> " + "\u00a7c" + "Switch failed. Enable the Offhand module."
                )
                switching = false
                return false
            }
            if (module.type.getValue() === Offhand.Type.NEW) {
                module.setSwapToTotem(false)
                module.setMode(Offhand.Mode.CRYSTALS)
                module.doOffhand()
            } else {
                module.setMode(Offhand.Mode2.CRYSTALS)
                module.doSwitch()
            }
            switching = false
            return true
        }
        if (mc.player.heldItemOffhand.item === Items.END_CRYSTAL) {
            mainHand = false
        } else {
            switchToHotbarSlot(ItemEndCrystal::class.java, false)
            mainHand = true
        }
        switching = false
        return true
    }

    private fun calculateDamage(targettedPlayer: EntityPlayer?) {
        var playerPos: BlockPos?
        var web: Block?
        if (targettedPlayer == null && targetMode.getValue() !== Target.DAMAGE && !fullCalc.getValue().booleanValue()) {
            return
        }
        var maxDamage = 0.5f
        var currentTarget: EntityPlayer? = null
        var currentPos: BlockPos? = null
        var maxSelfDamage = 0.0f
        foundDoublePop = false
        var setToAir: BlockPos? = null
        var state: IBlockState? = null
        if (webAttack.getValue().booleanValue() && targettedPlayer != null && mc.world.getBlockState(
                BlockPos(
                    targettedPlayer.positionVector
                ).also { playerPos = it }).block.also { web = it } === Blocks.WEB
        ) {
            setToAir = playerPos
            state = mc.world.getBlockState(playerPos)
            mc.world.setBlockToAir(playerPos)
        }
        block0@ for (pos in BlockUtil.possiblePlacePositions(
            placeRange.getValue().floatValue(),
            antiSurround.getValue(),
            oneDot15.getValue()
        )) {
            if (!rayTracePlaceCheck(
                    pos,
                    (raytrace.getValue() === Raytrace.PLACE || raytrace.getValue() === Raytrace.FULL) && mc.player.getDistanceSq(
                        pos
                    ) > MathUtil.square(
                        placetrace.getValue().floatValue()
                    ),
                    1.0f
                )
            ) continue
            var selfDamage = -1.0f
            if (canTakeDamage(suicide.getValue())) {
                selfDamage = calculateDamage(pos, (mc.player as Entity))
            }
            if (selfDamage.toDouble() + 0.5 >= EntityUtil.getHealth(mc.player as Entity) as Double || selfDamage > maxSelfPlace.getValue()
                    .floatValue()
            ) continue
            if (targettedPlayer != null) {
                val playerDamage = calculateDamage(pos, (targettedPlayer as Entity?)!!)
                if (calcEvenIfNoDamage.getValue()
                        .booleanValue() && (antiFriendPop.getValue() === AntiFriendPop.ALL || antiFriendPop.getValue() === AntiFriendPop.PLACE)
                ) {
                    var friendPop = false
                    for (friend in mc.world.playerEntities) {
                        var friendDamage: Float
                        if (friend == null || mc.player == friend as Any || friend.getDistanceSq(pos) > MathUtil.square(
                                range.getValue().floatValue() + placeRange.getValue().floatValue()
                            ) || !Phobos.friendManager.isFriend(friend) || calculateDamage(
                                pos,
                                (friend as Entity)
                            ).also { friendDamage = it }
                                .toDouble() <= EntityUtil.getHealth(friend as Entity) as Double + 0.5
                        ) continue
                        friendPop = true
                        break
                    }
                    if (friendPop) continue
                }
                if (isDoublePoppable(
                        targettedPlayer,
                        playerDamage
                    ) && (currentPos == null || targettedPlayer.getDistanceSq(pos) < targettedPlayer.getDistanceSq(
                        currentPos
                    ))
                ) {
                    currentTarget = targettedPlayer
                    maxDamage = playerDamage
                    currentPos = pos
                    foundDoublePop = true
                    continue
                }
                if (foundDoublePop || playerDamage <= maxDamage && (!extraSelfCalc.getValue()
                        .booleanValue() || playerDamage < maxDamage || selfDamage >= maxSelfDamage) || !(playerDamage > selfDamage || playerDamage > minDamage.getValue()
                        .floatValue() && !canTakeDamage(
                        suicide.getValue()
                    )) && playerDamage <= EntityUtil.getHealth(targettedPlayer as Entity?)
                ) continue
                maxDamage = playerDamage
                currentTarget = targettedPlayer
                currentPos = pos
                maxSelfDamage = selfDamage
                continue
            }
            val maxDamageBefore = maxDamage
            val currentTargetBefore = currentTarget
            val currentPosBefore = currentPos
            val maxSelfDamageBefore = maxSelfDamage
            for (player in mc.world.playerEntities) {
                var friendDamage: Float
                if (EntityUtil.isValid(
                        player as Entity,
                        placeRange.getValue().floatValue() + range.getValue().floatValue()
                    )
                ) {
                    if (antiNaked.getValue().booleanValue() && isNaked(player)) continue
                    val playerDamage = calculateDamage(pos, (player as Entity))
                    if (doublePopOnDamage.getValue().booleanValue() && isDoublePoppable(
                            player,
                            playerDamage
                        ) && (currentPos == null || player.getDistanceSq(pos) < player.getDistanceSq(currentPos))
                    ) {
                        currentTarget = player
                        maxDamage = playerDamage
                        currentPos = pos
                        maxSelfDamage = selfDamage
                        foundDoublePop = true
                        if (antiFriendPop.getValue() !== AntiFriendPop.BREAK && antiFriendPop.getValue() !== AntiFriendPop.PLACE) continue
                        continue@block0
                    }
                    if (foundDoublePop || playerDamage <= maxDamage && (!extraSelfCalc.getValue()
                            .booleanValue() || playerDamage < maxDamage || selfDamage >= maxSelfDamage) || !(playerDamage > selfDamage || playerDamage > minDamage.getValue()
                            .floatValue() && !canTakeDamage(
                            suicide.getValue()
                        )) && playerDamage <= EntityUtil.getHealth(player as Entity)
                    ) continue
                    maxDamage = playerDamage
                    currentTarget = player
                    currentPos = pos
                    maxSelfDamage = selfDamage
                    continue
                }
                if (antiFriendPop.getValue() !== AntiFriendPop.ALL && antiFriendPop.getValue() !== AntiFriendPop.PLACE || player == null || player.getDistanceSq(
                        pos
                    ) > MathUtil.square(
                        range.getValue().floatValue() + placeRange.getValue().floatValue()
                    ) || !Phobos.friendManager.isFriend(player) || calculateDamage(
                        pos,
                        (player as Entity)
                    ).also { friendDamage = it }
                        .toDouble() <= EntityUtil.getHealth(player as Entity) as Double + 0.5
                ) continue
                maxDamage = maxDamageBefore
                currentTarget = currentTargetBefore
                currentPos = currentPosBefore
                maxSelfDamage = maxSelfDamageBefore
                continue@block0
            }
        }
        if (setToAir != null) {
            mc.world.setBlockState(setToAir, state)
            webPos = currentPos
        }
        target = currentTarget
        currentDamage = maxDamage.toDouble()
        placePos = currentPos
    }

    private fun getTarget(unsafe: Boolean): EntityPlayer? {
        if (targetMode.getValue() === Target.DAMAGE) {
            return null
        }
        var currentTarget: EntityPlayer? = null
        for (player in mc.world.playerEntities) {
            if (EntityUtil.isntValid(
                    player as Entity,
                    placeRange.getValue().floatValue() + range.getValue().floatValue()
                ) || antiNaked.getValue()
                    .booleanValue() && isNaked(player) || unsafe && EntityUtil.isSafe(player as Entity)
            ) continue
            if (minArmor.getValue() > 0 && isArmorLow(player, minArmor.getValue())) {
                currentTarget = player
                break
            }
            if (currentTarget == null) {
                currentTarget = player
                continue
            }
            if (mc.player.getDistanceSq(player as Entity) >= mc.player.getDistanceSq(currentTarget as Entity?)) continue
            currentTarget = player
        }
        if (unsafe && currentTarget == null) {
            return getTarget(false)
        }
        if (predictPos.getValue().booleanValue() && currentTarget != null) {
            val profile = GameProfile(
                if (currentTarget.uniqueID == null) UUID.fromString("8af022c8-b926-41a0-8b79-2b544ff00fcf") else currentTarget.uniqueID,
                currentTarget.name
            )
            val newTarget = EntityOtherPlayerMP(mc.world as World, profile)
            val extrapolatePosition = extrapolatePlayerPosition(currentTarget, predictTicks.getValue())
            newTarget.copyLocationAndAnglesFrom(currentTarget as Entity?)
            newTarget.posX = extrapolatePosition.x
            newTarget.posY = extrapolatePosition.y
            newTarget.posZ = extrapolatePosition.z
            newTarget.health = EntityUtil.getHealth(currentTarget as Entity?)
            newTarget.inventory.copyInventory(currentTarget.inventory)
            currentTarget = newTarget
        }
        return currentTarget
    }

    private fun breakCrystal() {
        if (explode.getValue().booleanValue() && breakTimer.passedMs(
                breakDelay.getValue().intValue()
            ) && (switchMode.getValue() === Switch.ALWAYS || mainHand || offHand)
        ) {
            if (packets.getValue() === 1 && efficientTarget != null) {
                if (justRender.getValue().booleanValue()) {
                    doFakeSwing()
                    return
                }
                if (syncedFeetPlace.getValue().booleanValue() && gigaSync.getValue()
                        .booleanValue() && syncedCrystalPos != null && damageSync.getValue() !== DamageSync.NONE
                ) {
                    return
                }
                rotateTo(efficientTarget)
                attackEntity(efficientTarget)
                breakTimer.reset()
            } else if (!attackList.isEmpty()) {
                if (justRender.getValue().booleanValue()) {
                    doFakeSwing()
                    return
                }
                if (syncedFeetPlace.getValue().booleanValue() && gigaSync.getValue()
                        .booleanValue() && syncedCrystalPos != null && damageSync.getValue() !== DamageSync.NONE
                ) {
                    return
                }
                for (i in 0 until packets.getValue()) {
                    val entity = attackList.poll() ?: continue
                    rotateTo(entity)
                    attackEntity(entity)
                }
                breakTimer.reset()
            }
        }
    }

    private fun attackEntity(entity: Entity?) {
        if (entity != null) {
            if (eventMode.getValue() === 2 && threadMode.getValue() === ThreadMode.NONE && rotateFirst.getValue()
                    .booleanValue() && rotate.getValue() !== Rotate.OFF
            ) {
                packetUseEntities.add(CPacketUseEntity(entity))
            } else {
                EntityUtil.attackEntity(entity, sync.getValue(), breakSwing.getValue())
                brokenPos.add(BlockPos(entity.positionVector).down())
            }
        }
    }

    private fun doFakeSwing() {
        if (fakeSwing.getValue().booleanValue()) {
            EntityUtil.swingArmNoPacket(EnumHand.MAIN_HAND, mc.player as EntityLivingBase)
        }
    }

    private fun manualBreaker() {
        var result: RayTraceResult
        if (rotate.getValue() !== Rotate.OFF && eventMode.getValue() !== 2 && rotating) {
            if (didRotation) {
                mc.player.rotationPitch = (mc.player.rotationPitch.toDouble() + 4.0E-4).toFloat()
                didRotation = false
            } else {
                mc.player.rotationPitch = (mc.player.rotationPitch.toDouble() - 4.0E-4).toFloat()
                didRotation = true
            }
        }
        if ((offHand || mainHand) && manual.getValue().booleanValue() && manualTimer.passedMs(
                manualBreak.getValue().intValue()
            ) && Mouse.isButtonDown(
                1
            ) && mc.player.heldItemOffhand.item !== Items.GOLDEN_APPLE && mc.player.inventory.getCurrentItem().item !== Items.GOLDEN_APPLE && mc.player.inventory.getCurrentItem().item !== Items.BOW && mc.player.inventory.getCurrentItem().item !== Items.EXPERIENCE_BOTTLE && mc.objectMouseOver.also {
                result = it
            } != null
        ) {
            when (result.typeOfHit) {
                RayTraceResult.Type.ENTITY -> {
                    val entity = result.entityHit as? EntityEnderCrystal ?: break
                    EntityUtil.attackEntity(entity, sync.getValue(), breakSwing.getValue())
                    manualTimer.reset()
                }
                RayTraceResult.Type.BLOCK -> {
                    val mousePos = mc.objectMouseOver.blockPos.up()
                    for (target in mc.world.getEntitiesWithinAABBExcludingEntity(null, AxisAlignedBB(mousePos))) {
                        if (target !is EntityEnderCrystal) continue
                        EntityUtil.attackEntity(target, sync.getValue(), breakSwing.getValue())
                        manualTimer.reset()
                    }
                }
            }
        }
    }

    private fun rotateTo(entity: Entity?) {
        when (rotate.getValue()) {
            OFF -> {
                run { this.rotating = false }
                run {}
            }
            PLACE -> {
            }
            BREAK, ALL -> {
                val angle = calcAngle(mc.player.getPositionEyes(mc.renderPartialTicks), entity!!.positionVector)
                if (eventMode.getValue() === 2 && threadMode.getValue() === ThreadMode.NONE) {
                    Phobos.rotationManager.setPlayerRotations(angle[0], angle[1])
                    break
                }
                yaw = angle[0]
                pitch = angle[1]
                rotating = true
            }
        }
    }

    private fun rotateToPos(pos: BlockPos?) {
        when (rotate.getValue()) {
            OFF -> {
                run { this.rotating = false }
                run {}
            }
            BREAK -> {
            }
            PLACE, ALL -> {
                val angle = calcAngle(
                    mc.player.getPositionEyes(mc.renderPartialTicks),
                    Vec3d(
                        (pos!!.x.toFloat() + 0.5f).toDouble(),
                        (pos.y.toFloat() - 0.5f).toDouble(),
                        (pos.z.toFloat() + 0.5f).toDouble()
                    )
                )
                if (eventMode.getValue() === 2 && threadMode.getValue() === ThreadMode.NONE) {
                    Phobos.rotationManager.setPlayerRotations(angle[0], angle[1])
                    break
                }
                yaw = angle[0]
                pitch = angle[1]
                rotating = true
            }
        }
    }

    private fun isDoublePoppable(player: EntityPlayer?, damage: Float): Boolean {
        var health: Float
        if (doublePop.getValue().booleanValue() && EntityUtil.getHealth(player as Entity?).also { health = it }
                .toDouble() <= popHealth.getValue() && damage.toDouble() > health.toDouble() + 0.5 && damage <= popDamage.getValue()
                .floatValue()) {
            val timer = totemPops[player]
            return timer == null || timer.passedMs(popTime.getValue().intValue())
        }
        return false
    }

    private fun isValid(entity: Entity?): Boolean {
        return entity != null && mc.player.getDistanceSq(entity) <= MathUtil.square(
            breakRange.getValue().floatValue()
        ) && (raytrace.getValue() === Raytrace.NONE || raytrace.getValue() === Raytrace.PLACE || mc.player.canEntityBeSeen(
            entity
        ) || !mc.player.canEntityBeSeen(entity) && mc.player.getDistanceSq(entity) <= MathUtil.square(
            breaktrace.getValue().floatValue()
        ))
    }

    private fun isEligableForFeetSync(player: EntityPlayer?, pos: BlockPos?): Boolean {
        if (holySync.getValue().booleanValue()) {
            val playerPos = BlockPos(player!!.positionVector)
            for (facing in EnumFacing.values()) {
                var holyPos: BlockPos?
                if (facing == EnumFacing.DOWN || facing == EnumFacing.UP || pos != playerPos.down().offset(facing)
                        .also { holyPos = it } as Any
                ) continue
                return true
            }
            return false
        }
        return true
    }

    enum class PredictTimer {
        NONE, BREAK, PREDICT
    }

    enum class AntiFriendPop {
        NONE, PLACE, BREAK, ALL
    }

    enum class ThreadMode {
        NONE, POOL, SOUND, WHILE
    }

    enum class AutoSwitch {
        NONE, TOGGLE, ALWAYS
    }

    enum class Raytrace {
        NONE, PLACE, BREAK, FULL
    }

    enum class Switch {
        ALWAYS, BREAKSLOT, CALC
    }

    enum class Logic {
        BREAKPLACE, PLACEBREAK
    }

    enum class Target {
        CLOSEST, UNSAFE, DAMAGE
    }

    enum class Rotate {
        OFF, PLACE, BREAK, ALL
    }

    enum class DamageSync {
        NONE, PLACE, BREAK
    }

    enum class Settings {
        PLACE, BREAK, RENDER, MISC, DEV
    }

    class PlaceInfo(
        private val pos: BlockPos?,
        private val offhand: Boolean,
        private val placeSwing: Boolean,
        private val exactHand: Boolean
    ) {
        fun runPlace() {
            placeCrystalOnBlock(pos!!, if (offhand) EnumHand.OFF_HAND else EnumHand.MAIN_HAND, placeSwing, exactHand)
        }
    }

    private class RAutoCrystal private constructor() : Runnable {
        private var autoCrystal: AutoCrystal<*>? = null
        override fun run() {
            if (autoCrystal!!.threadMode.getValue() === ThreadMode.WHILE) {
                while (autoCrystal.isOn() && autoCrystal!!.threadMode.getValue() === ThreadMode.WHILE) {
                    while (Phobos.eventManager.ticksOngoing()) {
                    }
                    if (autoCrystal!!.shouldInterrupt.get()) {
                        autoCrystal!!.shouldInterrupt.set(false)
                        autoCrystal!!.syncroTimer.reset()
                        autoCrystal!!.thread!!.interrupt()
                        break
                    }
                    autoCrystal!!.threadOngoing.set(true)
                    Phobos.safetyManager.doSafetyCheck()
                    autoCrystal!!.doAutoCrystal()
                    autoCrystal!!.threadOngoing.set(false)
                    try {
                        Thread.sleep(autoCrystal!!.threadDelay.getValue().intValue())
                    } catch (e: InterruptedException) {
                        autoCrystal!!.thread!!.interrupt()
                        e.printStackTrace()
                    }
                }
            } else if (autoCrystal!!.threadMode.getValue() !== ThreadMode.NONE && autoCrystal.isOn()) {
                while (Phobos.eventManager.ticksOngoing()) {
                }
                autoCrystal!!.threadOngoing.set(true)
                Phobos.safetyManager.doSafetyCheck()
                autoCrystal!!.doAutoCrystal()
                autoCrystal!!.threadOngoing.set(false)
            }
        }

        companion object {
            private var instance: RAutoCrystal? = null
            fun getInstance(autoCrystal: AutoCrystal<*>?): RAutoCrystal? {
                if (instance == null) {
                    instance = RAutoCrystal()
                    instance!!.autoCrystal = autoCrystal
                }
                return instance
            }
        }
    }

    companion object {
        var target: EntityPlayer? = null
        var lowDmgPos: MutableSet<BlockPos?> = ConcurrentSet<Any?>()
        var placedPos: MutableSet<BlockPos?> = HashSet()
        var brokenPos: MutableSet<BlockPos> = HashSet()
        private var instance: AutoCrystal<*>?
        fun getInstance(): AutoCrystal<*>? {
            if (instance == null) {
                instance = AutoCrystal<Any?>()
            }
            return instance
        }
    }

    init {
        instance = this
    }
}*/