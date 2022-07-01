package com.lambda.client.module.modules.movement

import com.lambda.client.module.Category
import com.lambda.client.module.Module
import com.lambda.client.util.threads.defaultScope
import com.lambda.client.util.threads.safeListener
import kotlinx.coroutines.launch
import net.minecraft.init.MobEffects
import net.minecraftforge.fml.common.gameevent.InputEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.lwjgl.input.Keyboard.*


object AntiLevitation : Module(
    name = "AntiLevitation",
    description = "Removes levitation potion effect",
    category = Category.MOVEMENT
) {
    private enum class Mode {
        SIMPLE, COMPLEX
    }

    private val mode by setting("Mode", Mode.COMPLEX)
    private val ySpeed by setting("Y Speed", 1.0, 0.1..2.0, 0.1)
    init {
        safeListener<TickEvent.ClientTickEvent> {
            if (player.isPotionActive(MobEffects.LEVITATION)) {
                if(mode == Mode.COMPLEX) player.setVelocity(0.0, 0.0, 0.0) else player.removeActivePotionEffect(MobEffects.LEVITATION)
            }
        }
        safeListener<InputEvent.KeyInputEvent>(6969){
            if(mode != Mode.COMPLEX) return@safeListener
            if(!player.isPotionActive(MobEffects.LEVITATION)) return@safeListener
            if(isKeyDown(KEY_SPACE)) player.setVelocity(0.0, ySpeed, 0.0)
            if(isKeyDown(KEY_LSHIFT)) player.setVelocity(0.0, -ySpeed, 0.0)
        }
    }
}