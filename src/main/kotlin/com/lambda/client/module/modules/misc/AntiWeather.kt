package com.lambda.client.module.modules.misc

import com.lambda.client.event.events.world.WorldEvent
import com.lambda.client.mixin.client.world.MixinWorld
import com.lambda.client.module.Category
import com.lambda.client.module.Module
import com.lambda.event.listener.listener
import net.minecraft.client.multiplayer.WorldClient

/**
 * @see MixinWorld.getThunderStrengthHead
 * @see MixinWorld.getRainStrengthHead
 */
object AntiWeather : Module(
    name = "AntiWeather",
    description = "Removes rain and thunder from your world",
    category = Category.MISC
) {
    init {
        listener<WorldClient> {
            if(it.isThundering || it.isRaining){
                it.setRainStrength(0.0f)
                it.setThunderStrength(0.0f)
            }

        }
    }
}
