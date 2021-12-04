package com.defrag.client.gui

import com.defrag.client.LambdaMod
import com.defrag.client.event.DefragEventBus
import com.defrag.client.gui.clickgui.LambdaClickGui
import com.defrag.client.gui.hudgui.AbstractHudElement
import com.defrag.client.gui.hudgui.LambdaHudGui
import com.defrag.client.util.AsyncCachedValue
import com.defrag.client.util.StopTimer
import com.defrag.client.util.TimeUnit
import com.defrag.commons.collections.AliasSet
import com.defrag.commons.utils.ClassUtils
import com.defrag.commons.utils.ClassUtils.instance
import kotlinx.coroutines.Deferred
import java.lang.reflect.Modifier

internal object GuiManager : com.defrag.client.AsyncLoader<List<Class<out AbstractHudElement>>> {
    override var deferred: Deferred<List<Class<out AbstractHudElement>>>? = null
    private val hudElementSet = AliasSet<AbstractHudElement>()

    val hudElements by AsyncCachedValue(5L, TimeUnit.SECONDS) {
        hudElementSet.distinct().sortedBy { it.name }
    }

    override fun preLoad0(): List<Class<out AbstractHudElement>> {
        val stopTimer = StopTimer()

        val list = ClassUtils.findClasses<AbstractHudElement>("com.lambda.client.gui.hudgui.elements") {
            filter { Modifier.isFinal(it.modifiers) }
        }

        val time = stopTimer.stop()

        LambdaMod.LOG.info("${list.size} hud elements found, took ${time}ms")
        return list
    }

    override fun load0(input: List<Class<out AbstractHudElement>>) {
        val stopTimer = StopTimer()

        for (clazz in input) {
            register(clazz.instance)
        }

        val time = stopTimer.stop()
        LambdaMod.LOG.info("${input.size} hud elements loaded, took ${time}ms")

        LambdaClickGui.onGuiClosed()
        LambdaHudGui.onGuiClosed()

        DefragEventBus.subscribe(LambdaClickGui)
        DefragEventBus.subscribe(LambdaHudGui)
    }

    internal fun register(hudElement: AbstractHudElement) {
        hudElementSet.add(hudElement)
        LambdaHudGui.register(hudElement)
    }

    internal fun unregister(hudElement: AbstractHudElement) {
        hudElementSet.remove(hudElement)
        LambdaHudGui.unregister(hudElement)
    }

    fun getHudElementOrNull(name: String?) = name?.let { hudElementSet[it] }
}