package com.defrag.client.module.modules.misc

import com.defrag.client.mixin.client.world.MixinWorld
import com.defrag.client.module.Category
import com.defrag.client.module.Module

/**
 * @see MixinWorld.getThunderStrengthHead
 * @see MixinWorld.getRainStrengthHead
 */
object AntiWeather : Module(
    name = "AntiWeather",
    description = "Removes rain and thunder from your world",
    category = Category.MISC
)
