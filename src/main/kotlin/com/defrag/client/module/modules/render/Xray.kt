package com.defrag.client.module.modules.render

import com.defrag.client.module.Category
import com.defrag.client.module.Module
import com.defrag.client.setting.settings.impl.collection.CollectionSetting
import com.defrag.client.util.threads.onMainThread
import kotlinx.coroutines.runBlocking
import net.minecraft.block.state.IBlockState

object Xray : Module(
    name = "Xray",
    description = "Lets you see through blocks",
    category = Category.RENDER
) {
    private val defaultVisibleList = linkedSetOf("minecraft:diamond_ore", "minecraft:iron_ore", "minecraft:gold_ore", "minecraft:portal", "minecraft:cobblestone")

    val visibleList = setting(CollectionSetting("Visible List", defaultVisibleList, { false }))

    @JvmStatic
    fun shouldReplace(state: IBlockState): Boolean {
        return isEnabled && !visibleList.contains(state.block.registryName.toString())
    }

    init {
        onToggle {
            runBlocking {
                onMainThread {
                    mc.renderGlobal?.loadRenderers()
                }
            }
        }

        visibleList.editListeners.add {
            runBlocking {
                onMainThread {
                    mc.renderGlobal?.loadRenderers()
                }
            }
        }
    }
}
