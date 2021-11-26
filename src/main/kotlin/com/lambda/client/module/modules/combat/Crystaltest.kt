package com.lambda.client.module.modules.combat

import com.lambda.client.event.events.PacketEvent
import com.lambda.client.module.EntityUtil
import com.lambda.client.util.*
import com.lambda.client.util.PacketEvent
import com.mojang.authlib.GameProfile
import io.netty.util.internal.ConcurrentSet
import com.lambda.client.util.Render3DEvent
import com.lambda.client.util.UpdateWalkingPlayerEvent
import net.minecraft.block.Block
import net.minecraft.block.state.IBlockState
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.item.EntityEnderCrystal
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.init.SoundEvents
import net.minecraft.item.ItemEndCrystal
import net.minecraft.item.ItemPickaxe
import net.minecraft.network.Packet
import net.minecraft.network.play.client.CPacketAnimation
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraft.network.play.client.CPacketUseEntity
import net.minecraft.network.play.server.*
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.SoundCategory
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.RayTraceResult
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent
import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse
import java.awt.Color
import java.util.*
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.collections.HashMap
import kotlin.collections.HashSet
import kotlin.collections.Map
import kotlin.collections.MutableMap
import kotlin.collections.MutableSet
import kotlin.collections.contains
import kotlin.collections.set

class AutoCrystal : Module("AutoCrystal", "Best CA on the market", Module.Category.COMBAT, true, false, false) {
    private val setting: Setting<Settings> = this.register(Setting<Settings>("Settings", Settings.PLACE))
    var raytrace: Setting<Raytrace> = this.register(Setting<Any>("Raytrace", Raytrace.NONE as Any) { v -> setting.getValue() === Settings.MISC })
    var place: Setting<Boolean> = this.register(Setting<Any>("Place", java.lang.Boolean.valueOf(true)) { v -> setting.getValue() === Settings.PLACE })
    var placeDelay: Setting<Int> = this.register(Setting<Any>("PlaceDelay", Integer.valueOf(25), Integer.valueOf(0), Integer.valueOf(500)) { v -> setting.getValue() === Settings.PLACE && place.getValue() !== false })
    var placeRange: Setting<Float> = this.register(Setting<Any>("PlaceRange", java.lang.Float.valueOf(6.0f), java.lang.Float.valueOf(0.0f), java.lang.Float.valueOf(10.0f)) { v -> setting.getValue() === Settings.PLACE && place.getValue() !== false })
    var minDamage: Setting<Float> = this.register(Setting<Any>("MinDamage", java.lang.Float.valueOf(7.0f), java.lang.Float.valueOf(0.1f), java.lang.Float.valueOf(20.0f)) { v -> setting.getValue() === Settings.PLACE && place.getValue() !== false })
    var maxSelfPlace: Setting<Float> = this.register(Setting<Any>("MaxSelfPlace", java.lang.Float.valueOf(10.0f), java.lang.Float.valueOf(0.1f), java.lang.Float.valueOf(36.0f)) { v -> setting.getValue() === Settings.PLACE && place.getValue() !== false })
    var wasteAmount: Setting<Int> = this.register(Setting<Any>("WasteAmount", Integer.valueOf(2), Integer.valueOf(1), Integer.valueOf(5)) { v -> setting.getValue() === Settings.PLACE && place.getValue() !== false })
    var wasteMinDmgCount: Setting<Boolean> = this.register(Setting<Any>("CountMinDmg", java.lang.Boolean.valueOf(true)) { v -> setting.getValue() === Settings.PLACE && place.getValue() !== false })
    var facePlace: Setting<Float> = this.register(Setting<Any>("FacePlace", java.lang.Float.valueOf(8.0f), java.lang.Float.valueOf(0.1f), java.lang.Float.valueOf(20.0f)) { v -> setting.getValue() === Settings.PLACE && place.getValue() !== false })
    var placetrace: Setting<Float> = this.register(Setting<Any>("Placetrace", java.lang.Float.valueOf(4.5f), java.lang.Float.valueOf(0.0f), java.lang.Float.valueOf(10.0f)) { v -> setting.getValue() === Settings.PLACE && place.getValue() !== false && raytrace.getValue() !== Raytrace.NONE && raytrace.getValue() !== Raytrace.BREAK })
    var antiSurround: Setting<Boolean> = this.register(Setting<Any>("AntiSurround", java.lang.Boolean.valueOf(true)) { v -> setting.getValue() === Settings.PLACE && place.getValue() !== false })
    var limitFacePlace: Setting<Boolean> = this.register(Setting<Any>("LimitFacePlace", java.lang.Boolean.valueOf(true)) { v -> setting.getValue() === Settings.PLACE && place.getValue() !== false })
    var oneDot15: Setting<Boolean> = this.register(Setting<Any>("1.15", java.lang.Boolean.valueOf(false)) { v -> setting.getValue() === Settings.PLACE && place.getValue() !== false })
    var doublePop: Setting<Boolean> = this.register(Setting<Any>("AntiTotem", java.lang.Boolean.valueOf(false)) { v -> setting.getValue() === Settings.PLACE && place.getValue() !== false })
    var popHealth: Setting<Double> = this.register(Setting<Any>("PopHealth", java.lang.Double.valueOf(1.0), java.lang.Double.valueOf(0.0), java.lang.Double.valueOf(3.0)) { v -> setting.getValue() === Settings.PLACE && place.getValue() !== false && doublePop.getValue() !== false })
    var popDamage: Setting<Float> = this.register(Setting<Any>("PopDamage", java.lang.Float.valueOf(4.0f), java.lang.Float.valueOf(0.0f), java.lang.Float.valueOf(6.0f)) { v -> setting.getValue() === Settings.PLACE && place.getValue() !== false && doublePop.getValue() !== false })
    var popTime: Setting<Int> = this.register(Setting<Any>("PopTime", Integer.valueOf(500), Integer.valueOf(0), Integer.valueOf(1000)) { v -> setting.getValue() === Settings.PLACE && place.getValue() !== false && doublePop.getValue() !== false })
    var explode: Setting<Boolean> = this.register(Setting<Any>("Break", java.lang.Boolean.valueOf(true)) { v -> setting.getValue() === Settings.BREAK })
    var switchMode: Setting<Switch> = this.register(Setting<Any>("Attack", Switch.BREAKSLOT as Any) { v -> setting.getValue() === Settings.BREAK && explode.getValue() !== false })
    var breakDelay: Setting<Int> = this.register(Setting<Any>("BreakDelay", Integer.valueOf(50), Integer.valueOf(0), Integer.valueOf(500)) { v -> setting.getValue() === Settings.BREAK && explode.getValue() !== false })
    var breakRange: Setting<Float> = this.register(Setting<Any>("BreakRange", java.lang.Float.valueOf(6.0f), java.lang.Float.valueOf(0.0f), java.lang.Float.valueOf(10.0f)) { v -> setting.getValue() === Settings.BREAK && explode.getValue() !== false })
    var packets: Setting<Int> = this.register(Setting<Any>("Packets", Integer.valueOf(1), Integer.valueOf(1), Integer.valueOf(6)) { v -> setting.getValue() === Settings.BREAK && explode.getValue() !== false })
    var maxSelfBreak: Setting<Float> = this.register(Setting<Any>("MaxSelfBreak", java.lang.Float.valueOf(10.0f), java.lang.Float.valueOf(0.1f), java.lang.Float.valueOf(36.0f)) { v -> setting.getValue() === Settings.BREAK && explode.getValue() !== false })
    var breaktrace: Setting<Float> = this.register(Setting<Any>("Breaktrace", java.lang.Float.valueOf(4.5f), java.lang.Float.valueOf(0.0f), java.lang.Float.valueOf(10.0f)) { v -> setting.getValue() === Settings.BREAK && explode.getValue() !== false && raytrace.getValue() !== Raytrace.NONE && raytrace.getValue() !== Raytrace.PLACE })
    var manual: Setting<Boolean> = this.register(Setting<Any>("Manual", java.lang.Boolean.valueOf(true)) { v -> setting.getValue() === Settings.BREAK })
    var manualMinDmg: Setting<Boolean> = this.register(Setting<Any>("ManMinDmg", java.lang.Boolean.valueOf(true)) { v -> setting.getValue() === Settings.BREAK && manual.getValue() !== false })
    var manualBreak: Setting<Int> = this.register(Setting<Any>("ManualDelay", Integer.valueOf(500), Integer.valueOf(0), Integer.valueOf(500)) { v -> setting.getValue() === Settings.BREAK && manual.getValue() !== false })
    var sync: Setting<Boolean> = this.register(Setting<Any>("Sync", java.lang.Boolean.valueOf(true)) { v -> setting.getValue() === Settings.BREAK && (explode.getValue() !== false || manual.getValue() !== false) })
    var instant: Setting<Boolean> = this.register(Setting<Any>("Predict", java.lang.Boolean.valueOf(true)) { v -> setting.getValue() === Settings.BREAK && explode.getValue() !== false && place.getValue() !== false })
    var instantTimer: Setting<PredictTimer> = this.register(Setting<Any>("PredictTimer", PredictTimer.NONE as Any) { v -> setting.getValue() === Settings.BREAK && explode.getValue() !== false && place.getValue() !== false && instant.getValue() !== false })
    var resetBreakTimer: Setting<Boolean> = this.register(Setting<Any>("ResetBreakTimer", java.lang.Boolean.valueOf(true)) { v -> setting.getValue() === Settings.BREAK && explode.getValue() !== false && place.getValue() !== false && instant.getValue() !== false })
    var predictDelay: Setting<Int> = this.register(Setting<Any>("PredictDelay", Integer.valueOf(12), Integer.valueOf(0), Integer.valueOf(500)) { v -> setting.getValue() === Settings.BREAK && explode.getValue() !== false && place.getValue() !== false && instant.getValue() !== false && instantTimer.getValue() === PredictTimer.PREDICT })
    var predictCalc: Setting<Boolean> = this.register(Setting<Any>("PredictCalc", java.lang.Boolean.valueOf(true)) { v -> setting.getValue() === Settings.BREAK && explode.getValue() !== false && place.getValue() !== false && instant.getValue() !== false })
    var superSafe: Setting<Boolean> = this.register(Setting<Any>("SuperSafe", java.lang.Boolean.valueOf(true)) { v -> setting.getValue() === Settings.BREAK && explode.getValue() !== false && place.getValue() !== false && instant.getValue() !== false })
    var antiCommit: Setting<Boolean> = this.register(Setting<Any>("AntiOverCommit", java.lang.Boolean.valueOf(true)) { v -> setting.getValue() === Settings.BREAK && explode.getValue() !== false && place.getValue() !== false && instant.getValue() !== false })
    var render: Setting<Boolean> = this.register(Setting<Any>("Render", java.lang.Boolean.valueOf(true)) { v -> setting.getValue() === Settings.RENDER })
    var colorSync: Setting<Boolean> = this.register(Setting<Any>("CSync", java.lang.Boolean.valueOf(false)) { v -> setting.getValue() === Settings.RENDER })
    var box: Setting<Boolean> = this.register(Setting<Any>("Box", java.lang.Boolean.valueOf(true)) { v -> setting.getValue() === Settings.RENDER && render.getValue() !== false })
    var outline: Setting<Boolean> = this.register(Setting<Any>("Outline", java.lang.Boolean.valueOf(true)) { v -> setting.getValue() === Settings.RENDER && render.getValue() !== false })
    var text: Setting<Boolean> = this.register(Setting<Any>("Text", java.lang.Boolean.valueOf(false)) { v -> setting.getValue() === Settings.RENDER && render.getValue() !== false })
    private val red: Setting<Int> = this.register(Setting<Any>("Red", Integer.valueOf(255), Integer.valueOf(0), Integer.valueOf(255)) { v -> setting.getValue() === Settings.RENDER && render.getValue() !== false })
    private val green: Setting<Int> = this.register(Setting<Any>("Green", Integer.valueOf(255), Integer.valueOf(0), Integer.valueOf(255)) { v -> setting.getValue() === Settings.RENDER && render.getValue() !== false })
    private val blue: Setting<Int> = this.register(Setting<Any>("Blue", Integer.valueOf(255), Integer.valueOf(0), Integer.valueOf(255)) { v -> setting.getValue() === Settings.RENDER && render.getValue() !== false })
    private val alpha: Setting<Int> = this.register(Setting<Any>("Alpha", Integer.valueOf(255), Integer.valueOf(0), Integer.valueOf(255)) { v -> setting.getValue() === Settings.RENDER && render.getValue() !== false })
    private val boxAlpha: Setting<Int> = this.register(Setting<Any>("BoxAlpha", Integer.valueOf(125), Integer.valueOf(0), Integer.valueOf(255)) { v -> setting.getValue() === Settings.RENDER && render.getValue() !== false && box.getValue() !== false })
    private val lineWidth: Setting<Float> = this.register(Setting<Any>("LineWidth", java.lang.Float.valueOf(1.5f), java.lang.Float.valueOf(0.1f), java.lang.Float.valueOf(5.0f)) { v -> setting.getValue() === Settings.RENDER && render.getValue() !== false && outline.getValue() !== false })
    var customOutline: Setting<Boolean> = this.register(Setting<Any>("CustomLine", java.lang.Boolean.valueOf(false)) { v -> setting.getValue() === Settings.RENDER && render.getValue() !== false && outline.getValue() !== false })
    private val cRed: Setting<Int> = this.register(Setting<Any>("OL-Red", Integer.valueOf(255), Integer.valueOf(0), Integer.valueOf(255)) { v -> setting.getValue() === Settings.RENDER && render.getValue() !== false && customOutline.getValue() !== false && outline.getValue() !== false })
    private val cGreen: Setting<Int> = this.register(Setting<Any>("OL-Green", Integer.valueOf(255), Integer.valueOf(0), Integer.valueOf(255)) { v -> setting.getValue() === Settings.RENDER && render.getValue() !== false && customOutline.getValue() !== false && outline.getValue() !== false })
    private val cBlue: Setting<Int> = this.register(Setting<Any>("OL-Blue", Integer.valueOf(255), Integer.valueOf(0), Integer.valueOf(255)) { v -> setting.getValue() === Settings.RENDER && render.getValue() !== false && customOutline.getValue() !== false && outline.getValue() !== false })
    private val cAlpha: Setting<Int> = this.register(Setting<Any>("OL-Alpha", Integer.valueOf(255), Integer.valueOf(0), Integer.valueOf(255)) { v -> setting.getValue() === Settings.RENDER && render.getValue() !== false && customOutline.getValue() !== false && outline.getValue() !== false })
    var holdFacePlace: Setting<Boolean> = this.register(Setting<Any>("HoldFacePlace", java.lang.Boolean.valueOf(false)) { v -> setting.getValue() === Settings.MISC })
    var holdFaceBreak: Setting<Boolean> = this.register(Setting<Any>("HoldSlowBreak", java.lang.Boolean.valueOf(false)) { v -> setting.getValue() === Settings.MISC && holdFacePlace.getValue() !== false })
    var slowFaceBreak: Setting<Boolean> = this.register(Setting<Any>("SlowFaceBreak", java.lang.Boolean.valueOf(false)) { v -> setting.getValue() === Settings.MISC })
    var actualSlowBreak: Setting<Boolean> = this.register(Setting<Any>("ActuallySlow", java.lang.Boolean.valueOf(false)) { v -> setting.getValue() === Settings.MISC })
    var facePlaceSpeed: Setting<Int> = this.register(Setting<Any>("FaceSpeed", Integer.valueOf(500), Integer.valueOf(0), Integer.valueOf(500)) { v -> setting.getValue() === Settings.MISC })
    var antiNaked: Setting<Boolean> = this.register(Setting<Any>("AntiNaked", java.lang.Boolean.valueOf(true)) { v -> setting.getValue() === Settings.MISC })
    var range: Setting<Float> = this.register(Setting<Any>("Range", java.lang.Float.valueOf(12.0f), java.lang.Float.valueOf(0.1f), java.lang.Float.valueOf(20.0f)) { v -> setting.getValue() === Settings.MISC })
    var targetMode: Setting<Target> = this.register(Setting<Any>("Target", Target.CLOSEST as Any) { v -> setting.getValue() === Settings.MISC })
    var minArmor: Setting<Int> = this.register(Setting<Any>("MinArmor", Integer.valueOf(5), Integer.valueOf(0), Integer.valueOf(125)) { v -> setting.getValue() === Settings.MISC })
    private val switchCooldown: Setting<Int> = this.register(Setting<Any>("Cooldown", Integer.valueOf(500), Integer.valueOf(0), Integer.valueOf(1000)) { v -> setting.getValue() === Settings.MISC })
    var autoSwitch: Setting<AutoSwitch> = this.register(Setting<Any>("Switch", AutoSwitch.TOGGLE as Any) { v -> setting.getValue() === Settings.MISC })
    var switchBind: Setting<Bind> = this.register(Setting<Any>("SwitchBind", Bind(-1)) { v -> setting.getValue() === Settings.MISC && autoSwitch.getValue() === AutoSwitch.TOGGLE })
    var offhandSwitch: Setting<Boolean> = this.register(Setting<Any>("Offhand", java.lang.Boolean.valueOf(true)) { v -> setting.getValue() === Settings.MISC && autoSwitch.getValue() !== AutoSwitch.NONE })
    var switchBack: Setting<Boolean> = this.register(Setting<Any>("Switchback", java.lang.Boolean.valueOf(true)) { v -> setting.getValue() === Settings.MISC && autoSwitch.getValue() !== AutoSwitch.NONE && offhandSwitch.getValue() !== false })
    var lethalSwitch: Setting<Boolean> = this.register(Setting<Any>("LethalSwitch", java.lang.Boolean.valueOf(false)) { v -> setting.getValue() === Settings.MISC && autoSwitch.getValue() !== AutoSwitch.NONE })
    var mineSwitch: Setting<Boolean> = this.register(Setting<Any>("MineSwitch", java.lang.Boolean.valueOf(true)) { v -> setting.getValue() === Settings.MISC && autoSwitch.getValue() !== AutoSwitch.NONE })
    var rotate: Setting<Rotate> = this.register(Setting<Any>("Rotate", Rotate.OFF as Any) { v -> setting.getValue() === Settings.MISC })
    var suicide: Setting<Boolean> = this.register(Setting<Any>("Suicide", java.lang.Boolean.valueOf(false)) { v -> setting.getValue() === Settings.MISC })
    var webAttack: Setting<Boolean> = this.register(Setting<Any>("WebAttack", java.lang.Boolean.valueOf(true)) { v -> setting.getValue() === Settings.MISC && targetMode.getValue() !== Target.DAMAGE })
    var fullCalc: Setting<Boolean> = this.register(Setting<Any>("ExtraCalc", java.lang.Boolean.valueOf(false)) { v -> setting.getValue() === Settings.MISC })
    var sound: Setting<Boolean> = this.register(Setting<Any>("Sound", java.lang.Boolean.valueOf(true)) { v -> setting.getValue() === Settings.MISC })
    var soundRange: Setting<Float> = this.register(Setting<Any>("SoundRange", java.lang.Float.valueOf(12.0f), java.lang.Float.valueOf(0.0f), java.lang.Float.valueOf(12.0f)) { v -> setting.getValue() === Settings.MISC })
    var soundPlayer: Setting<Float> = this.register(Setting<Any>("SoundPlayer", java.lang.Float.valueOf(6.0f), java.lang.Float.valueOf(0.0f), java.lang.Float.valueOf(12.0f)) { v -> setting.getValue() === Settings.MISC })
    var soundConfirm: Setting<Boolean> = this.register(Setting<Any>("SoundConfirm", java.lang.Boolean.valueOf(true)) { v -> setting.getValue() === Settings.MISC })
    var extraSelfCalc: Setting<Boolean> = this.register(Setting<Any>("MinSelfDmg", java.lang.Boolean.valueOf(false)) { v -> setting.getValue() === Settings.MISC })
    var antiFriendPop: Setting<AntiFriendPop> = this.register(Setting<Any>("FriendPop", AntiFriendPop.NONE as Any) { v -> setting.getValue() === Settings.MISC })
    var noCount: Setting<Boolean> = this.register(Setting<Any>("AntiCount", java.lang.Boolean.valueOf(false)) { v -> setting.getValue() === Settings.MISC && (antiFriendPop.getValue() === AntiFriendPop.ALL || antiFriendPop.getValue() === AntiFriendPop.BREAK) })
    var calcEvenIfNoDamage: Setting<Boolean> = this.register(Setting<Any>("BigFriendCalc", java.lang.Boolean.valueOf(false)) { v -> setting.getValue() === Settings.MISC && (antiFriendPop.getValue() === AntiFriendPop.ALL || antiFriendPop.getValue() === AntiFriendPop.BREAK) && targetMode.getValue() !== Target.DAMAGE })
    var predictFriendDmg: Setting<Boolean> = this.register(Setting<Any>("PredictFriend", java.lang.Boolean.valueOf(false)) { v -> setting.getValue() === Settings.MISC && (antiFriendPop.getValue() === AntiFriendPop.ALL || antiFriendPop.getValue() === AntiFriendPop.BREAK) && instant.getValue() !== false })
    var minMinDmg: Setting<Float> = this.register(Setting<Any>("MinMinDmg", java.lang.Float.valueOf(0.0f), java.lang.Float.valueOf(0.0f), java.lang.Float.valueOf(3.0f)) { v -> setting.getValue() === Settings.DEV && place.getValue() !== false })
    val attackOppositeHand: Setting<Boolean> = this.register(Setting<Any>("OppositeHand", java.lang.Boolean.valueOf(false)) { v -> setting.getValue() === Settings.DEV })
    val removeAfterAttack: Setting<Boolean> = this.register(Setting<Any>("AttackRemove", java.lang.Boolean.valueOf(false)) { v -> setting.getValue() === Settings.DEV })
    val antiBlock: Setting<Boolean> = this.register(Setting<Any>("AntiFeetPlace", java.lang.Boolean.valueOf(false)) { v -> setting.getValue() === Settings.DEV })
    var breakSwing: Setting<Boolean> = this.register(Setting<Any>("BreakSwing", java.lang.Boolean.valueOf(true)) { v -> setting.getValue() === Settings.DEV })
    var placeSwing: Setting<Boolean> = this.register(Setting<Any>("PlaceSwing", java.lang.Boolean.valueOf(false)) { v -> setting.getValue() === Settings.DEV })
    var exactHand: Setting<Boolean> = this.register(Setting<Any>("ExactHand", java.lang.Boolean.valueOf(false)) { v -> setting.getValue() === Settings.DEV && placeSwing.getValue() !== false })
    var justRender: Setting<Boolean> = this.register(Setting<Any>("JustRender", java.lang.Boolean.valueOf(false)) { v -> setting.getValue() === Settings.DEV })
    var fakeSwing: Setting<Boolean> = this.register(Setting<Any>("FakeSwing", java.lang.Boolean.valueOf(false)) { v -> setting.getValue() === Settings.DEV && justRender.getValue() !== false })
    var logic: Setting<Logic> = this.register(Setting<Any>("Logic", Logic.BREAKPLACE as Any) { v -> setting.getValue() === Settings.DEV })
    var damageSync: Setting<DamageSync> = this.register(Setting<Any>("DamageSync", DamageSync.NONE as Any) { v -> setting.getValue() === Settings.DEV })
    var damageSyncTime: Setting<Int> = this.register(Setting<Any>("SyncDelay", Integer.valueOf(500), Integer.valueOf(0), Integer.valueOf(500)) { v -> setting.getValue() === Settings.DEV && damageSync.getValue() !== DamageSync.NONE })
    var dropOff: Setting<Float> = this.register(Setting<Any>("DropOff", java.lang.Float.valueOf(5.0f), java.lang.Float.valueOf(0.0f), java.lang.Float.valueOf(10.0f)) { v -> setting.getValue() === Settings.DEV && damageSync.getValue() === DamageSync.BREAK })
    var confirm: Setting<Int> = this.register(Setting<Any>("Confirm", Integer.valueOf(250), Integer.valueOf(0), Integer.valueOf(1000)) { v -> setting.getValue() === Settings.DEV && damageSync.getValue() !== DamageSync.NONE })
    var syncedFeetPlace: Setting<Boolean> = this.register(Setting<Any>("FeetSync", java.lang.Boolean.valueOf(false)) { v -> setting.getValue() === Settings.DEV && damageSync.getValue() !== DamageSync.NONE })
    var fullSync: Setting<Boolean> = this.register(Setting<Any>("FullSync", java.lang.Boolean.valueOf(false)) { v -> setting.getValue() === Settings.DEV && damageSync.getValue() !== DamageSync.NONE && syncedFeetPlace.getValue() !== false })
    var syncCount: Setting<Boolean> = this.register(Setting<Any>("SyncCount", java.lang.Boolean.valueOf(true)) { v -> setting.getValue() === Settings.DEV && damageSync.getValue() !== DamageSync.NONE && syncedFeetPlace.getValue() !== false })
    var hyperSync: Setting<Boolean> = this.register(Setting<Any>("HyperSync", java.lang.Boolean.valueOf(false)) { v -> setting.getValue() === Settings.DEV && damageSync.getValue() !== DamageSync.NONE && syncedFeetPlace.getValue() !== false })
    var gigaSync: Setting<Boolean> = this.register(Setting<Any>("GigaSync", java.lang.Boolean.valueOf(false)) { v -> setting.getValue() === Settings.DEV && damageSync.getValue() !== DamageSync.NONE && syncedFeetPlace.getValue() !== false })
    var syncySync: Setting<Boolean> = this.register(Setting<Any>("SyncySync", java.lang.Boolean.valueOf(false)) { v -> setting.getValue() === Settings.DEV && damageSync.getValue() !== DamageSync.NONE && syncedFeetPlace.getValue() !== false })
    var enormousSync: Setting<Boolean> = this.register(Setting<Any>("EnormousSync", java.lang.Boolean.valueOf(false)) { v -> setting.getValue() === Settings.DEV && damageSync.getValue() !== DamageSync.NONE && syncedFeetPlace.getValue() !== false })
    var holySync: Setting<Boolean> = this.register(Setting<Any>("UnbelievableSync", java.lang.Boolean.valueOf(false)) { v -> setting.getValue() === Settings.DEV && damageSync.getValue() !== DamageSync.NONE && syncedFeetPlace.getValue() !== false })
    private val eventMode: Setting<Int> = this.register(Setting<Any>("Updates", Integer.valueOf(3), Integer.valueOf(1), Integer.valueOf(3)) { v -> setting.getValue() === Settings.DEV })
    var rotateFirst: Setting<Boolean> = this.register(Setting<Any>("FirstRotation", java.lang.Boolean.valueOf(false)) { v -> setting.getValue() === Settings.DEV && rotate.getValue() !== Rotate.OFF && eventMode.getValue() === 2 })
    var threadMode: Setting<ThreadMode> = this.register(Setting<Any>("Thread", ThreadMode.NONE as Any) { v -> setting.getValue() === Settings.DEV })
    var threadDelay: Setting<Int> = this.register(Setting<Any>("ThreadDelay", Integer.valueOf(50), Integer.valueOf(1), Integer.valueOf(1000)) { v -> setting.getValue() === Settings.DEV && threadMode.getValue() !== ThreadMode.NONE })
    var syncThreadBool: Setting<Boolean> = this.register(Setting<Any>("ThreadSync", java.lang.Boolean.valueOf(true)) { v -> setting.getValue() === Settings.DEV && threadMode.getValue() !== ThreadMode.NONE })
    var syncThreads: Setting<Int> = this.register(Setting<Any>("SyncThreads", Integer.valueOf(1000), Integer.valueOf(1), Integer.valueOf(10000)) { v -> setting.getValue() === Settings.DEV && threadMode.getValue() !== ThreadMode.NONE && syncThreadBool.getValue() !== false })
    var predictPos: Setting<Boolean> = this.register(Setting<Any>("PredictPos", java.lang.Boolean.valueOf(false)) { v -> setting.getValue() === Settings.DEV })
    var renderExtrapolation: Setting<Boolean> = this.register(Setting<Any>("RenderExtrapolation", java.lang.Boolean.valueOf(false)) { v -> setting.getValue() === Settings.DEV && predictPos.getValue() !== false })
    var predictTicks: Setting<Int> = this.register(Setting<Any>("ExtrapolationTicks", Integer.valueOf(2), Integer.valueOf(1), Integer.valueOf(20)) { v -> setting.getValue() === Settings.DEV && predictPos.getValue() !== false })
    var rotations: Setting<Int> = this.register(Setting<Any>("Spoofs", Integer.valueOf(1), Integer.valueOf(1), Integer.valueOf(20)) { v -> setting.getValue() === Settings.DEV })
    var predictRotate: Setting<Boolean> = this.register(Setting<Any>("PredictRotate", java.lang.Boolean.valueOf(false)) { v -> setting.getValue() === Settings.DEV })
    var predictOffset: Setting<Float> = this.register(Setting<Any>("PredictOffset", java.lang.Float.valueOf(0.0f), java.lang.Float.valueOf(0.0f), java.lang.Float.valueOf(4.0f)) { v -> setting.getValue() === Settings.DEV })
    var brownZombie: Setting<Boolean> = this.register(Setting<Any>("BrownZombieMode", java.lang.Boolean.valueOf(false)) { v -> setting.getValue() === Settings.MISC })
    var doublePopOnDamage: Setting<Boolean> = this.register(Setting<Any>("DamagePop", java.lang.Boolean.valueOf(false)) { v -> setting.getValue() === Settings.PLACE && place.getValue() !== false && doublePop.getValue() !== false && targetMode.getValue() === Target.DAMAGE })
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
            doAutoCrystal()
    }

    @SubscribeEvent fun onUpdateWalkingPlayer(event: UpdateWalkingPlayerEvent) {
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
            processMultiThreading()
    }

    fun onUpdate() {

            doAutoCrystal()

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

            processMultiThreading()

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

    @SubscribeEvent fun onPacketSend(event: PacketEvent.Send) {
        var packet: CPacketUseEntity
        if (event.getStage() === 0 && rotate.getValue() !== Rotate.OFF && rotating && eventMode.getValue() !== 2 && event.getPacket() is CPacketPlayer) {
            val packet2 = event.getPacket() as CPacketPlayer
            packet2.yaw = yaw
            packet2.pitch = pitch
            ++rotationPacketsSpoofed
            if (rotationPacketsSpoofed >= rotations.getValue()) {
                rotating = false
                rotationPacketsSpoofed = 0
            }
        }
        var pos: BlockPos? = null
        if (event.getStage() === 0 && event.getPacket() is CPacketUseEntity && (event.getPacket() as CPacketUseEntity?. also { packet = it }).getAction() == CPacketUseEntity.Action.ATTACK && packet.getEntityFromWorld(mc.world as World) is EntityEnderCrystal) {
            pos = packet.getEntityFromWorld(mc.world as World).getPosition()
            if (removeAfterAttack.getValue().booleanValue()) {
                Objects.requireNonNull(packet.getEntityFromWorld(mc.world as World)).setDead()
                mc.world.removeEntityFromWorld(packet.entityId)
            }
        }
        if (event.getStage() === 0 && event.getPacket() is CPacketUseEntity && (event.getPacket() as CPacketUseEntity?. also { packet = it }).getAction() == CPacketUseEntity.Action.ATTACK && SPacketSpawnObject(mc.world as World) is EntityEnderCrystal) {
            val crystal = packet.getEntityFromWorld(mc.world as World) as EntityEnderCrystal?
            if (antiBlock.getValue().booleanValue() && EntityUtil.isCrystalAtFeet(crystal, range.getValue().floatValue()) && pos != null) {
                rotateToPos(pos)
                BlockUtil.placeCrystalOnBlock(placePos, if (offHand) EnumHand.OFF_HAND else EnumHand.MAIN_HAND, placeSwing.getValue(), exactHand.getValue())
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH, receiveCanceled = true) fun onPacketReceive(event: PacketEvent.Receive) {
        var packet: SPacketSoundEffect
        if (fullNullCheck()) {
            return
        }
        if (!justRender.getValue().booleanValue() && switchTimer.passedMs(switchCooldown.getValue().intValue()) && explode.getValue().booleanValue() && instant.getValue().booleanValue() && event.getPacket() is SPacketSpawnObject && (syncedCrystalPos == null || !syncedFeetPlace.getValue().booleanValue() || damageSync.getValue() === DamageSync.NONE)) {
            var pos: BlockPos
            val packet2 = event.getPacket() as SPacketSpawnObject
            if (packet2.type == 51 && mc.player.getDistanceSq(BlockPos(packet2.x, packet2.y, packet2.z).also { pos = it }) + predictOffset.getValue().floatValue() as Double <= MathUtil.square(breakRange.getValue().floatValue()) && (instantTimer.getValue() === PredictTimer.NONE || instantTimer.getValue() === PredictTimer.BREAK && breakTimer.passedMs(breakDelay.getValue().intValue()) || instantTimer.getValue() === PredictTimer.PREDICT && predictTimer.passedMs(predictDelay.getValue().intValue()))) {
                if (predictSlowBreak(pos.down())) {
                    return
                }
                if (predictFriendDmg.getValue().booleanValue() && (antiFriendPop.getValue() === AntiFriendPop.BREAK || antiFriendPop.getValue() === AntiFriendPop.ALL) && isRightThread) {
                    for (friend in mc.world.playerEntities) {
                        if (friend == null || mc.player.equals(friend as Any) || friend.getDistanceSq(pos) > MathUtil.square(range.getValue().floatValue() + placeRange.getValue().floatValue()) || !Phobos.friendManager.isFriend(friend) || DamageUtil.calculateDamage(pos, friend as Entity) as Double <= EntityUtil.getHealth(friend as Entity) as Double + 0.5) continue
                        return
                    }
                }
                if (placedPos.contains(pos.down())) {
                    var selfDamage: Float
                    if (if (isRightThread && superSafe.getValue() !== false) DamageUtil.canTakeDamage(suicide.getValue()) && (DamageUtil.calculateDamage(pos, mc.player as Entity).also { selfDamage = it }.toDouble() - 0.5 > EntityUtil.getHealth(mc.player as Entity) as Double || selfDamage > maxSelfBreak.getValue().floatValue()) else superSafe.getValue() !== false) {
                        return
                    }
                    attackCrystalPredict(packet2.entityID, pos)
                } else if (predictCalc.getValue().booleanValue() && isRightThread) {
                    var selfDamage = -1.0f
                    if (DamageUtil.canTakeDamage(suicide.getValue())) {
                        selfDamage = DamageUtil.calculateDamage(pos, mc.player as Entity)
                    }
                    if (selfDamage.toDouble() + 0.5 < EntityUtil.getHealth(mc.player as Entity) as Double && selfDamage <= maxSelfBreak.getValue().floatValue()) {
                        for (player in mc.world.playerEntities) {
                            var damage: Float
                            if (player.getDistanceSq(pos) > MathUtil.square(range.getValue().floatValue()) || !EntityUtil.isValid(player as Entity, range.getValue().floatValue() + breakRange.getValue().floatValue()) || antiNaked.getValue().booleanValue() && DamageUtil.isNaked(player) || !(DamageUtil.calculateDamage(pos, player as Entity).also { damage = it } > selfDamage || damage > minDamage.getValue().floatValue() && !DamageUtil.canTakeDamage(suicide.getValue())) && damage <= EntityUtil.getHealth(player as Entity)) continue
                            if (predictRotate.getValue().booleanValue() && eventMode.getValue() !== 2 && (rotate.getValue() === Rotate.BREAK || rotate.getValue() === Rotate.ALL)) {
                                rotateToPos(pos)
                            }
                            attackCrystalPredict(packet2.entityID, pos)
                            break
                        }
                    }
                }
            }
        } else if (!soundConfirm.getValue().booleanValue() && event.getPacket() is SPacketExplosion) {
            val packet3 = event.getPacket() as SPacketExplosion
            val pos = BlockPos(packet3.x, packet3.y, packet3.z).down()
            removePos(pos)
        } else if (event.getPacket() is SPacketDestroyEntities) {
            val packet4 = event.getPacket() as SPacketDestroyEntities
            for (id in packet4.entityIDs) {
                val entity: Entity = mc.world.getEntityByID(id) as? EntityEnderCrystal
                    ?: continue
                brokenPos.remove(BlockPos(entity.positionVector).down())
                placedPos.remove(BlockPos(entity.positionVector).down())
            }
        } else if (event.getPacket() is SPacketEntityStatus) {
            val packet5 = event.getPacket() as SPacketEntityStatus
            if (packet5.opCode.toInt() == 35 && packet5.getEntity(mc.world as World) is EntityPlayer) {
                totemPops[packet5.getEntity(mc.world as World) as EntityPlayer] = Timer().reset()
            }
        } else if (event.getPacket() is SPacketSoundEffect && (event.getPacket() as SPacketSoundEffect?. also { packet = it }).getCategory() == SoundCategory.BLOCKS && packet.sound === SoundEvents.ENTITY_GENERIC_EXPLODE) {
            val pos = BlockPos(packet.x, packet.y, packet.z)
            if (sound.getValue().booleanValue() || threadMode.getValue() === ThreadMode.SOUND) {
                NoSoundLag.removeEntities(packet, soundRange.getValue().floatValue())
            }
            if (soundConfirm.getValue().booleanValue()) {
                removePos(pos)
            }
            if (threadMode.getValue() === ThreadMode.SOUND && isRightThread && mc.player != null && mc.player.getDistanceSq(pos) < MathUtil.square(soundPlayer.getValue().floatValue())) {
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
        private get() = mc.isCallingFromMinecraftThread() || !Phobos.eventManager.ticksOngoing() && !threadOngoing.get()

    private fun attackCrystalPredict(entityID: Int, pos: BlockPos) {
        if (!(!predictRotate.getValue().booleanValue() || eventMode.getValue() === 2 && threadMode.getValue() === ThreadMode.NONE || rotate.getValue() !== Rotate.BREAK && rotate.getValue() !== Rotate.ALL)) {
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
        if ((offHand || mainHand || switchMode.getValue() === Switch.CALC) && renderPos != null && render.getValue().booleanValue() && (box.getValue().booleanValue() || text.getValue().booleanValue() || outline.getValue().booleanValue())) {
            RenderUtil.drawBoxESP(renderPos, if (colorSync.getValue() !== false) Colors.INSTANCE.getCurrentColor() else Color(red.getValue(), green.getValue(), blue.getValue(), alpha.getValue()), customOutline.getValue(), if (colorSync.getValue() !== false) Colors.INSTANCE.getCurrentColor() else Color(cRed.getValue(), cGreen.getValue(), cBlue.getValue(), cAlpha.getValue()), lineWidth.getValue().floatValue(), outline.getValue(), box.getValue(), boxAlpha.getValue(), false)
            if (text.getValue().booleanValue()) {
                RenderUtil.drawText(renderPos, (if (Math.floor(renderDamage) == renderDamage) Integer.valueOf(renderDamage.toInt()) else String.format("%.1f", renderDamage)).toString() + "")
            }
        }
    }

    @SubscribeEvent fun onKeyInput(event: KeyInputEvent?) {
        if (Keyboard.getEventKeyState() && mc.currentScreen !is PhobosGui && switchBind.getValue().getKey() === Keyboard.getEventKey()) {
            if (switchBack.getValue().booleanValue() && offhandSwitch.getValue().booleanValue() && offHand) {
                val module: Offhand = Phobos.moduleManager.getModuleByClass(Offhand::class.java)
                if (module.isOff()) {
                    Command.sendMessage("<" + this.getDisplayName().toString() + "> " + "\u00a7c" + "Switch failed. Enable the Offhand module.")
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

    @SubscribeEvent fun onSettingChange(event: ClientEvent) {
        if (event.getStage() === 2 && event.getSetting() != null && event.getSetting().getFeature() != null && event.getSetting().getFeature().equals(this) && this.isEnabled() && (event.getSetting().equals(threadDelay) || event.getSetting().equals(threadMode))) {
            if (executor != null) {
                executor!!.shutdown()
            }
            if (thread != null) {
                shouldInterrupt.set(true)
            }
        }
    }

    private fun postProcessing() {
        if (threadMode.getValue() !== ThreadMode.NONE || eventMode.getValue() !== 2 || rotate.getValue() === Rotate.OFF || !rotateFirst.getValue().booleanValue()) {
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
        if (justDoIt || executor == null || executor!!.isTerminated || executor!!.isShutdown || syncroTimer.passedMs(syncThreads.getValue().intValue()) && syncThreadBool.getValue().booleanValue()) {
            if (executor != null) {
                executor!!.shutdown()
            }
            executor = getExecutor()
            syncroTimer.reset()
        }
    }

    private fun handleWhile() {
        if (thread == null || thread!!.isInterrupted || !thread!!.isAlive || syncroTimer.passedMs(syncThreads.getValue().intValue()) && syncThreadBool.getValue().booleanValue()) {
            if (thread == null) {
                thread = Thread(RAutoCrystal.getInstance(this))
            } else if (syncroTimer.passedMs(syncThreads.getValue().intValue()) && !shouldInterrupt.get() && syncThreadBool.getValue().booleanValue()) {
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
        service.scheduleAtFixedRate(RAutoCrystal.getInstance(this), 0L, threadDelay.getValue().intValue(), TimeUnit.MILLISECONDS)
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
        mainHand = mc.player.getHeldItemMainhand().getItem() === Items.END_CRYSTAL
        offHand = mc.player.getHeldItemOffhand().getItem() === Items.END_CRYSTAL
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
        if (!((offHand || mainHand || switchMode.getValue() !== Switch.BREAKSLOT || switching) && DamageUtil.canBreakWeakness(mc.player as EntityPlayer) && switchTimer.passedMs(switchCooldown.getValue().intValue()))) {
            renderPos = null
            target = null
            rotating = false
            return false
        }
        if (mineSwitch.getValue().booleanValue() && Mouse.isButtonDown(0) && (switching || autoSwitch.getValue() === AutoSwitch.ALWAYS) && Mouse.isButtonDown(1) && mc.player.getHeldItemMainhand().getItem() is ItemPickaxe) {
            switchItem()
        }
        mapCrystals()
        if (!posConfirmed && damageSync.getValue() !== DamageSync.NONE && syncTimer.passedMs(confirm.getValue().intValue())) {
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
            if (syncedFeetPlace.getValue().booleanValue() && entity.getPosition().down() == syncedCrystalPos as Any? && damageSync.getValue() !== DamageSync.NONE) {
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
            if (DamageUtil.canTakeDamage(suicide.getValue())) {
                selfDamage = DamageUtil.calculateDamage(entity, mc.player as Entity)
            }
            if (selfDamage.toDouble() + 0.5 < EntityUtil.getHealth(mc.player as Entity) as Double && selfDamage <= maxSelfBreak.getValue().floatValue()) {
                val beforeCrystal = maxCrystal
                val beforeDamage = maxDamage
                for (player in mc.world.playerEntities) {
                    var damage: Float
                    if (player.getDistanceSq(entity) > MathUtil.square(range.getValue().floatValue())) continue
                    if (EntityUtil.isValid(player as Entity, range.getValue().floatValue() + breakRange.getValue().floatValue())) {
                        if (antiNaked.getValue().booleanValue() && DamageUtil.isNaked(player) || !(DamageUtil.calculateDamage(entity, player as Entity).also { damage = it } > selfDamage || damage > minDamage.getValue().floatValue() && !DamageUtil.canTakeDamage(suicide.getValue())) && damage <= EntityUtil.getHealth(player as Entity)) continue
                        if (damage > maxDamage) {
                            maxDamage = damage
                            maxCrystal = entity
                        }
                        if (packets.getValue() === 1) {
                            if (damage >= minDamage.getValue().floatValue() || !wasteMinDmgCount.getValue().booleanValue()) {
                                count = true
                            }
                            countMin = true
                            continue
                        }
                        if (crystalMap[entity] != null && crystalMap[entity]!!.toFloat() >= damage) continue
                        crystalMap[entity] = java.lang.Float.valueOf(damage)
                        continue
                    }
                    if (antiFriendPop.getValue() !== AntiFriendPop.BREAK && antiFriendPop.getValue() !== AntiFriendPop.ALL || !Phobos.friendManager.isFriend(player.name) || DamageUtil.calculateDamage(entity, player as Entity).also { damage = it }.toDouble() <= EntityUtil.getHealth(player as Entity) as Double + 0.5) continue
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
        if (damageSync.getValue() === DamageSync.BREAK && (maxDamage.toDouble() > lastDamage || syncTimer.passedMs(damageSyncTime.getValue().intValue()) || damageSync.getValue() === DamageSync.NONE)) {
            lastDamage = maxDamage.toDouble()
        }
        if (enormousSync.getValue().booleanValue() && syncedFeetPlace.getValue().booleanValue() && damageSync.getValue() !== DamageSync.NONE && syncedCrystalPos != null) {
            if (syncCount.getValue().booleanValue()) {
                minDmgCount = wasteAmount.getValue() + 1
                crystalCount = wasteAmount.getValue() + 1
            }
            return
        }
        if (webAttack.getValue().booleanValue() && webPos != null) {
            if (mc.player.getDistanceSq(webPos!!.up()) > MathUtil.square(breakRange.getValue().floatValue())) {
                webPos = null
            } else {
                for (entity in mc.world.getEntitiesWithinAABB(Entity::class.java, AxisAlignedBB(webPos!!.up()))) {
                    if (entity !is EntityEnderCrystal) continue
                    attackList.add(entity)
                    efficientTarget = entity
                    webPos = null
                    lastDamage = 0.5
                    return
                }
            }
        }
        if (shouldSlowBreak(true) && maxDamage < minDamage.getValue().floatValue() && (target == null || EntityUtil.getHealth(target as Entity?) > facePlace.getValue().floatValue() || !breakTimer.passedMs(facePlaceSpeed.getValue().intValue()) && slowFaceBreak.getValue().booleanValue() && Mouse.isButtonDown(0) && holdFacePlace.getValue().booleanValue() && holdFaceBreak.getValue().booleanValue())) {
            efficientTarget = null
            return
        }
        if (packets.getValue() === 1) {
            efficientTarget = maxCrystal
        } else {
            crystalMap = MathUtil.sortByValue(crystalMap, true)
            for ((key, value): Map.Entry<*, *> in crystalMap) {
                val crystal = key as Entity
                val damage = (value as Float).toFloat()
                if (damage >= minDamage.getValue().floatValue() || !wasteMinDmgCount.getValue().booleanValue()) {
                    ++crystalCount
                }
                attackList.add(crystal)
                ++minDmgCount
            }
        }
    }

    private fun shouldSlowBreak(withManual: Boolean): Boolean {
        return withManual && manual.getValue() !== false && manualMinDmg.getValue() !== false && Mouse.isButtonDown(1) && (!Mouse.isButtonDown(0) || holdFacePlace.getValue() === false) || holdFacePlace.getValue() !== false && holdFaceBreak.getValue() !== false && Mouse.isButtonDown(0) && !breakTimer.passedMs(facePlaceSpeed.getValue().intValue()) || slowFaceBreak.getValue() !== false && !breakTimer.passedMs(facePlaceSpeed.getValue().intValue())
    }

    private fun placeCrystal() {
        var crystalLimit: Int = wasteAmount.getValue()
        if (placeTimer.passedMs(placeDelay.getValue().intValue()) && place.getValue().booleanValue() && (offHand || mainHand || switchMode.getValue() === Switch.CALC || switchMode.getValue() === Switch.BREAKSLOT && switching)) {
            if (!(!offHand && !mainHand && (switchMode.getValue() === Switch.ALWAYS || switching) || crystalCount < crystalLimit || antiSurround.getValue().booleanValue() && lastPos != null && lastPos == placePos as Any?)) {
                return
            }
            calculateDamage(getTarget(targetMode.getValue() === Target.UNSAFE))
            if (target != null && placePos != null) {
                if (!offHand && !mainHand && autoSwitch.getValue() !== AutoSwitch.NONE && (currentDamage > minDamage.getValue().floatValue() as Double || lethalSwitch.getValue().booleanValue() && EntityUtil.getHealth(target as Entity?) <= facePlace.getValue().floatValue()) && !switchItem()) {
                    return
                }
                if (currentDamage < minDamage.getValue().floatValue() as Double && limitFacePlace.getValue().booleanValue()) {
                    crystalLimit = 1
                }
                if (currentDamage >= minMinDmg.getValue().floatValue() as Double && (offHand || mainHand || autoSwitch.getValue() !== AutoSwitch.NONE) && (crystalCount < crystalLimit || antiSurround.getValue().booleanValue() && lastPos != null && lastPos == placePos as Any?) && (currentDamage > minDamage.getValue().floatValue() as Double || minDmgCount < crystalLimit) && currentDamage >= 1.0 && (DamageUtil.isArmorLow(target, minArmor.getValue()) || EntityUtil.getHealth(target as Entity?) <= facePlace.getValue().floatValue() || currentDamage > minDamage.getValue().floatValue() as Double || shouldHoldFacePlace())) {
                    val damageOffset = if (damageSync.getValue() === DamageSync.BREAK) dropOff.getValue().floatValue() - 5.0f else 0.0f
                    var syncflag = false
                    if (syncedFeetPlace.getValue().booleanValue() && placePos == lastPos as Any? && isEligableForFeetSync(target, placePos) && !syncTimer.passedMs(damageSyncTime.getValue().intValue()) && target == currentSyncTarget as Any? && target!!.position == syncedPlayerPos as Any? && damageSync.getValue() !== DamageSync.NONE) {
                        syncedCrystalPos = placePos
                        lastDamage = currentDamage
                        if (fullSync.getValue().booleanValue()) {
                            lastDamage = 100.0
                        }
                        syncflag = true
                    }
                    if (syncflag || currentDamage - damageOffset.toDouble() > lastDamage || syncTimer.passedMs(damageSyncTime.getValue().intValue()) || damageSync.getValue() === DamageSync.NONE) {
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
                            if (addTolowDmg || actualSlowBreak.getValue().booleanValue() && currentDamage < minDamage.getValue().floatValue() as Double) {
                                lowDmgPos.add(placePos)
                            }
                            placedPos.add(placePos)
                            if (!justRender.getValue().booleanValue()) {
                                if (eventMode.getValue() === 2 && threadMode.getValue() === ThreadMode.NONE && rotateFirst.getValue().booleanValue() && rotate.getValue() !== Rotate.OFF) {
                                    placeInfo = PlaceInfo(placePos, offHand, placeSwing.getValue(), exactHand.getValue())
                                } else {
                                    BlockUtil.placeCrystalOnBlock(placePos, if (offHand) EnumHand.OFF_HAND else EnumHand.MAIN_HAND, placeSwing.getValue(), exactHand.getValue())
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
                Command.sendMessage("<" + this.getDisplayName().toString() + "> " + "\u00a7c" + "Switch failed. Enable the Offhand module.")
                switching = false
                return false
            }
            if (module.type.getValue() === Offhand.Type.NEW) {
                module.setSwapToTotem(false)
                module.setMode(Offhand.Mode.CRYSTALS)
                module.doOffhand()
            } else {
                Blocks.setMode(Offhand.Mode2.CRYSTALS)
                module.doSwitch()
            }
            switching = false
            return true
        }
        if (mc.player.getHeldItemOffhand().getItem() === Items.END_CRYSTAL) {
            mainHand = false
        } else {
            InventoryUtil.switchToHotbarSlot(ItemEndCrystal::class.java, false)
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
        if (webAttack.getValue().booleanValue() && targettedPlayer != null && mc.world.getBlockState(BlockPos(targettedPlayer.positionVector).also { playerPos = it }).getBlock().also { web = it } === Blocks.WEB) {
            setToAir = playerPos
            state = mc.world.getBlockState(playerPos)
            mc.world.setBlockToAir(playerPos)
        }
        block0@ for (pos in BlockUtil.possiblePlacePositions(placeRange.getValue().floatValue(), antiSurround.getValue(), oneDot15.getValue())) {
            if (!BlockUtil.rayTracePlaceCheck(pos, (raytrace.getValue() === Raytrace.PLACE || raytrace.getValue() === Raytrace.FULL) && mc.player.getDistanceSq(pos) > MathUtil.square(placetrace.getValue().floatValue()), 1.0f)) continue
            var selfDamage = -1.0f
            if (DamageUtil.canTakeDamage(suicide.getValue())) {
                selfDamage = DamageUtil.calculateDamage(pos, mc.player as Entity)
            }
            if (selfDamage.toDouble() + 0.5 >= EntityUtil.getHealth(mc.player as Entity) as Double || selfDamage > maxSelfPlace.getValue().floatValue()) continue
            if (targettedPlayer != null) {
                val playerDamage: Float = DamageUtil.calculateDamage(pos, targettedPlayer as Entity?)
                if (calcEvenIfNoDamage.getValue().booleanValue() && (antiFriendPop.getValue() === AntiFriendPop.ALL || antiFriendPop.getValue() === AntiFriendPop.PLACE)) {
                    var friendPop = false
                    for (friend in mc.world.playerEntities) {
                        var friendDamage: Float
                        if (friend == null || mc.player.equals(friend as Any) || friend.getDistanceSq(pos) > MathUtil.square(range.getValue().floatValue() + placeRange.getValue().floatValue()) || !Phobos.friendManager.isFriend(friend) || DamageUtil.calculateDamage(pos, friend as Entity).also { friendDamage = it }.toDouble() <= EntityUtil.getHealth(friend as Entity) as Double + 0.5) continue
                        friendPop = true
                        break
                    }
                    if (friendPop) continue
                }
                if (isDoublePoppable(targettedPlayer, playerDamage) && (currentPos == null || targettedPlayer.getDistanceSq(pos) < targettedPlayer.getDistanceSq(currentPos))) {
                    currentTarget = targettedPlayer
                    maxDamage = playerDamage
                    currentPos = pos
                    foundDoublePop = true
                    continue
                }
                if (foundDoublePop || playerDamage <= maxDamage && (!extraSelfCalc.getValue().booleanValue() || playerDamage < maxDamage || selfDamage >= maxSelfDamage) || !(playerDamage > selfDamage || playerDamage > minDamage.getValue().floatValue() && !DamageUtil.canTakeDamage(suicide.getValue())) && playerDamage <= EntityUtil.getHealth(targettedPlayer as Entity?)) continue
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
                if (EntityUtil.isValid(player as Entity, placeRange.getValue().floatValue() + range.getValue().floatValue())) {
                    if (antiNaked.getValue().booleanValue() && DamageUtil.isNaked(player)) continue
                    val playerDamage: Float = DamageUtil.calculateDamage(pos, player as Entity)
                    if (doublePopOnDamage.getValue().booleanValue() && isDoublePoppable(player, playerDamage) && (currentPos == null || player!!.getDistanceSq(pos) < player!!.getDistanceSq(currentPos))) {
                        currentTarget = player
                        maxDamage = playerDamage
                        currentPos = pos
                        maxSelfDamage = selfDamage
                        foundDoublePop = true
                        if (antiFriendPop.getValue() !== AntiFriendPop.BREAK && antiFriendPop.getValue() !== AntiFriendPop.PLACE) continue
                        continue@block0
                    }
                    if (foundDoublePop || playerDamage <= maxDamage && (!extraSelfCalc.getValue().booleanValue() || playerDamage < maxDamage || selfDamage >= maxSelfDamage) || !(playerDamage > selfDamage || playerDamage > minDamage.getValue().floatValue() && !DamageUtil.canTakeDamage(suicide.getValue())) && playerDamage <= EntityUtil.getHealth(player as Entity)) continue
                    maxDamage = playerDamage
                    currentTarget = player
                    currentPos = pos
                    maxSelfDamage = selfDamage
                    continue
                }
                if (antiFriendPop.getValue() !== AntiFriendPop.ALL && antiFriendPop.getValue() !== AntiFriendPop.PLACE || player == null || player.getDistanceSq(pos) > MathUtil.square(range.getValue().floatValue() + placeRange.getValue().floatValue()) || !Phobos.friendManager.isFriend(player) || DamageUtil.calculateDamage(pos, player as Entity).also { friendDamage = it }.toDouble() <= EntityUtil.getHealth(player as Entity) as Double + 0.5) continue
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
            if (EntityUtil.isntValid(player as Entity, placeRange.getValue().floatValue() + range.getValue().floatValue()) || antiNaked.getValue().booleanValue() && DamageUtil.isNaked(player) || unsafe && EntityUtil.isSafe(player as Entity)) continue
            if (minArmor.getValue() > 0 && DamageUtil.isArmorLow(player, minArmor.getValue())) {
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
            val profile = GameProfile(if (currentTarget.uniqueID == null) UUID.fromString("8af022c8-b926-41a0-8b79-2b544ff00fcf") else currentTarget.uniqueID, currentTarget.name)
            val newTarget = EntityOtherPlayerMP(mc.world as World, profile)
            val extrapolatePosition: Vec3d = MathUtil.extrapolatePlayerPosition(currentTarget, predictTicks.getValue())
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
        if (explode.getValue().booleanValue() && breakTimer.passedMs(breakDelay.getValue().intValue()) && (switchMode.getValue() === Switch.ALWAYS || mainHand || offHand)) {
            if (packets.getValue() === 1 && efficientTarget != null) {
                if (justRender.getValue().booleanValue()) {
                    doFakeSwing()
                    return
                }
                if (syncedFeetPlace.getValue().booleanValue() && gigaSync.getValue().booleanValue() && syncedCrystalPos != null && damageSync.getValue() !== DamageSync.NONE) {
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
                if (syncedFeetPlace.getValue().booleanValue() && gigaSync.getValue().booleanValue() && syncedCrystalPos != null && damageSync.getValue() !== DamageSync.NONE) {
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
            if (eventMode.getValue() === 2 && threadMode.getValue() === ThreadMode.NONE && rotateFirst.getValue().booleanValue() && rotate.getValue() !== Rotate.OFF) {
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
                mc.player.rotationPitch = (mc.player.rotationPitch as Double + 4.0E-4).toFloat()
                didRotation = false
            } else {
                mc.player.rotationPitch = (mc.player.rotationPitch as Double - 4.0E-4).toFloat()
                didRotation = true
            }
        }
        if ((offHand || mainHand) && manual.getValue().booleanValue() && manualTimer.passedMs(manualBreak.getValue().intValue()) && Mouse.isButtonDown(1) && mc.player.getHeldItemOffhand().getItem() !== Items.GOLDEN_APPLE && mc.player.inventory.getCurrentItem().getItem() !== Items.GOLDEN_APPLE && mc.player.inventory.getCurrentItem().getItem() !== Items.BOW && mc.player.inventory.getCurrentItem().getItem() !== Items.EXPERIENCE_BOTTLE && mc.objectMouseOver.also { result = it } != null) {
            when (result.typeOfHit) {
                RayTraceResult.Type.ENTITY -> {
                    val entity = result.entityHit as? EntityEnderCrystal ?: break
                    EntityUtil.attackEntity(entity, sync.getValue(), breakSwing.getValue())
                    manualTimer.reset()
                }
                RayTraceResult.Type.BLOCK -> {
                    val mousePos: BlockPos = mc.objectMouseOver.getBlockPos().up()
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
                val angle: FloatArray = MathUtil.calcAngle(mc.player.getPositionEyes(mc.getRenderPartialTicks()), entity!!.positionVector)
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
                val angle: FloatArray = MathUtil.calcAngle(mc.player.getPositionEyes(mc.getRenderPartialTicks()), Vec3d((pos!!.x.toFloat() + 0.5f).toDouble(), (pos.y.toFloat() - 0.5f).toDouble(), (pos.z.toFloat() + 0.5f).toDouble()))
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
        if (doublePop.getValue().booleanValue() && EntityUtil.getHealth(player as Entity?).also { health = it }.toDouble() <= popHealth.getValue() && damage.toDouble() > health.toDouble() + 0.5 && damage <= popDamage.getValue().floatValue()) {
            val timer = totemPops[player]
            return timer == null || timer.passedMs(popTime.getValue().intValue())
        }
        return false
    }

    private fun isValid(entity: Entity?): Boolean {
        return entity != null && mc.player.getDistanceSq(entity) <= MathUtil.square(breakRange.getValue().floatValue()) && (raytrace.getValue() === Raytrace.NONE || raytrace.getValue() === Raytrace.PLACE || mc.player.canEntityBeSeen(entity) || !mc.player.canEntityBeSeen(entity) && mc.player.getDistanceSq(entity) <= MathUtil.square(breaktrace.getValue().floatValue()))
    }

    private fun isEligableForFeetSync(player: EntityPlayer?, pos: BlockPos?): Boolean {
        if (holySync.getValue().booleanValue()) {
            val playerPos = BlockPos(player!!.positionVector)
            for (facing in EnumFacing.values()) {
                var holyPos: BlockPos?
                if (facing == EnumFacing.DOWN || facing == EnumFacing.UP || pos != playerPos.down().offset(facing).also { holyPos = it } as Any) continue
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

    class PlaceInfo(private val pos: BlockPos?, private val offhand: Boolean, private val placeSwing: Boolean, private val exactHand: Boolean) {
        fun runPlace() {
            BlockUtil.placeCrystalOnBlock(pos, if (offhand) EnumHand.OFF_HAND else EnumHand.MAIN_HAND, placeSwing, exactHand)
        }
    }

    private class RAutoCrystal private constructor() : Runnable {
        private var autoCrystal: AutoCrystal? = null
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
            fun getInstance(autoCrystal: AutoCrystal?): RAutoCrystal? {
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
        private var instance: AutoCrystal?
        fun getInstance(): AutoCrystal? {
            if (instance == null) {
                instance = AutoCrystal()
            }
            return instance
        }
    }

    init {
        instance = this
    }
}