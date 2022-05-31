package com.lambda.client.module.modules.movement

import com.lambda.client.event.events.PlayerMoveEvent
import com.lambda.client.event.events.RenderWorldEvent
import com.lambda.client.event.listener.listener
import com.lambda.client.manager.managers.CombatManager
import com.lambda.client.module.Category
import com.lambda.client.module.Module
import com.lambda.client.module.modules.combat.KillAura
import com.lambda.client.setting.settings.impl.other.ColorSetting
import com.lambda.client.util.color.ColorHolder
import com.lambda.client.util.graphics.GlStateUtils
import com.lambda.client.util.graphics.LambdaTessellator
import com.lambda.client.util.math.RotationUtils.getRotationToEntity
import com.lambda.client.util.threads.safeListener
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.init.MobEffects
import net.minecraft.util.math.Vec3d
import org.lwjgl.opengl.GL11
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt


object TargetStrafe : Module(
    name = "TargetStrafe",
    description = "Strafes around the target",
    category = Category.MOVEMENT
) {

    private val autoJump by setting("AutoJump", true)
    private val distanceSetting by setting("PreferredDistance", 1f, 0f..6f, 0.1f)
    private val maxDistance by setting("MaxDistance", 10f, 1f..32f, 0.5f)
    private val stopOver by setting("StopOverMaxDistance", true)
    private val turnAmount by setting("TurnAmount", 5f, 1f..90f, 0.5f)
    private val hSpeed by setting("HSpeed", 0.2873f, 0.001f..10.0f, 0.0001f)

    private val needsAura by setting("NeedsAura", true)
    private val antiStuck by setting("AntiStuck", true)

    private val renderCircle by setting("RenderCircle", true)
    private val renderThickness by setting("RenderThickness", 2f, 0.5f..8f, 0.5f, { renderCircle })
    private val distanceColor by setting(ColorSetting("SetDistanceColor", ColorHolder(0, 255, 0, 255)))
    private val playerDistanceColor by setting(ColorSetting("PlayerDistanceColor", ColorHolder(255, 255, 255, 255)))

    var direction = 1

    private var currentDistance = 0.toDouble()
    private var currentTargetVec: Vec3d? = null

    private var strafing = false

    init {

        safeListener<PlayerMoveEvent> {
            if (mc.player.collidedHorizontally && antiStuck) {
                switchDirection()
            }
            val strafe = canStrafe()

            if (strafe) {
                val rotations = CombatManager.target?.let { it1 -> getRotationToEntity(it1) }
                if (rotations != null) {
                    CombatManager.target?.let { it1 -> doStrafeAtSpeed(it, rotations.x, it1.positionVector) }
                    currentTargetVec = CombatManager.target?.positionVector!!
                }

                try {
                    strafing = true
                } catch (e: ConcurrentModificationException) {
                }
            } else {
                try {
                    strafing = false
                } catch (e: ConcurrentModificationException) {
                }
            }
        }

        listener<RenderWorldEvent> {
            try {
                if (strafing) {
                    if (currentTargetVec == null) {
                        return@listener
                    }
                    drawCircle(currentTargetVec!!, distanceSetting.toDouble(), distanceColor, 360)
                    drawCircle(currentTargetVec!!, currentDistance, playerDistanceColor, 360)
                }
            } catch (e: ConcurrentModificationException) {

            }
        }
    }

    private fun doStrafeAtSpeed(event: PlayerMoveEvent, rotation: Float, target: Vec3d): Boolean {



        var playerSpeed = hSpeed
        var jumpVel = 0.405
        var moveForward = 1f
        var moveStrafe = 0f
        var rotationYaw = rotation + (90f * direction)

        val disX = mc.player.posX - target.x
        val disZ = mc.player.posZ - target.z

        val distance = sqrt(disX * disX + disZ * disZ)

        if (distance < maxDistance) {
            if (distance > distanceSetting) {
                rotationYaw -= turnAmount * direction
            } else if (distance < distanceSetting) {
                rotationYaw += turnAmount * direction
            }
        } else {
            rotationYaw = rotation
        }

        currentDistance = distance

        // check for speed potion

        // check for speed potion
        if (mc.player.isPotionActive(MobEffects.SPEED)) {
            val amplifier = mc.player.getActivePotionEffect(MobEffects.SPEED)!!.amplifier
            playerSpeed *= 1.0f + 0.2f * (amplifier + 1)
        }

        // check for jump boost
        if (mc.player.isPotionActive(MobEffects.JUMP_BOOST)) {
            val amplifier = mc.player.getActivePotionEffect(MobEffects.JUMP_BOOST)!!.amplifier
            jumpVel *= amplifier
        }

        if (moveForward == 0.0f && moveStrafe == 0.0f) {
            event.x = 0.0
            event.z = 0.0
        } else {
            if (moveForward != 0.0f) {
                if (moveStrafe > 0.0f) {
                    rotationYaw += (if (moveForward > 0.0f) -45 else 45).toFloat()
                } else if (moveStrafe < 0.0f) {
                    rotationYaw += (if (moveForward > 0.0f) 45 else -45).toFloat()
                }
                moveStrafe = 0.0f
                if (moveForward > 0.0f) {
                    moveForward = 1.0f
                } else if (moveForward < 0.0f) {
                    moveForward = -1.0f
                }
            }
            event.x = moveForward * playerSpeed * cos(Math.toRadians((rotationYaw + 90.0f).toDouble())) + moveStrafe * playerSpeed * sin(Math.toRadians((rotationYaw + 90.0f).toDouble()))
            event.z = moveForward * playerSpeed * sin(Math.toRadians((rotationYaw + 90.0f).toDouble())) - moveStrafe * playerSpeed * cos(Math.toRadians((rotationYaw + 90.0f).toDouble()))
        }

        if (autoJump && mc.player.onGround) {
            mc.player.jump()
        }

        return false
    }

    private fun canStrafe(): Boolean {
        if ((KillAura.isEnabled || !needsAura) && CombatManager.target != null) {
            return true
        }
        return false
    }

    private fun switchDirection() {
        direction = -direction
    }

    private fun drawCircle(center: Vec3d, radius: Double, color: ColorHolder, precision: Int) {


        val linesToDraw = ArrayList<Pair<Vec3d, Vec3d>>()

        val magic = precision / 360

        var lastPos = center.add(0.0, 0.0, radius)


        for (i in 0..precision) {

            val yaw = i * magic

            val x = radius * cos(Math.toRadians((yaw + 90.0f).toDouble()))
            val z = radius * sin(Math.toRadians((yaw + 90.0f).toDouble()))

            val newPos = center.add(x, 0.0, z)
            linesToDraw.add(Pair(lastPos, newPos))
            lastPos = newPos
        }

        val buffer = LambdaTessellator.buffer

        GL11.glLineWidth(renderThickness)
        GlStateUtils.depth(false)

        for (pair in linesToDraw) {
            try {

                buffer.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION_COLOR)

                buffer.pos(pair.first.x, center.y, pair.first.z).color(color.r, color.g, color.b, color.a).endVertex()
                buffer.pos(pair.second.x, center.y, pair.second.z).color(color.r, color.g, color.b, color.a).endVertex()

                LambdaTessellator.render()

            } catch (e: NullPointerException) {
            }
        }

        GlStateUtils.depth(true)
        GL11.glLineWidth(1f)
    }
}