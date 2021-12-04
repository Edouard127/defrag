package com.defrag.client.setting.configs

import com.defrag.client.LambdaMod
import com.defrag.client.plugin.api.IPluginClass
import com.defrag.client.plugin.api.PluginHudElement
import com.defrag.client.plugin.api.PluginModule
import com.defrag.client.setting.settings.AbstractSetting
import com.defrag.client.setting.settings.impl.number.DoubleSetting
import com.defrag.client.setting.settings.impl.number.FloatSetting
import com.defrag.client.setting.settings.impl.number.IntegerSetting
import com.defrag.client.setting.settings.impl.other.BindSetting
import com.defrag.client.setting.settings.impl.other.ColorSetting
import com.defrag.client.setting.settings.impl.primitive.BooleanSetting
import com.defrag.client.setting.settings.impl.primitive.EnumSetting
import com.defrag.client.setting.settings.impl.primitive.StringSetting
import com.defrag.client.util.Bind
import com.defrag.client.util.color.ColorHolder
import java.io.File

class PluginConfig(pluginName: String) : NameableConfig<IPluginClass>(
    pluginName, "${LambdaMod.DIRECTORY}config/plugins/$pluginName"
) {
    override val file: File get() = File("$filePath/default.json")
    override val backup: File get() = File("$filePath/default.bak")

    override fun addSettingToConfig(owner: IPluginClass, setting: AbstractSetting<*>) {
        when (owner) {
            is PluginModule -> {
                getGroupOrPut("modules").getGroupOrPut(owner.name).addSetting(setting)
            }
            is PluginHudElement -> {
                getGroupOrPut("hud").getGroupOrPut(owner.name).addSetting(setting)
            }
            else -> {
                getGroupOrPut("misc").getGroupOrPut(owner.name).addSetting(setting)
            }
        }
    }

    override fun getSettings(owner: IPluginClass): List<AbstractSetting<*>> {
        return when (owner) {
            is PluginModule -> {
                getGroup("modules")?.getGroupOrPut(owner.name)?.getSettings()
            }
            is PluginHudElement -> {
                getGroup("hud")?.getGroup(owner.name)?.getSettings()
            }
            else -> {
                getGroup("misc")?.getGroup(owner.name)?.getSettings()
            }
        } ?: emptyList()
    }

    override fun <E : Enum<E>> IPluginClass.setting(name: String, value: E, visibility: () -> Boolean, consumer: (prev: E, input: E) -> E, description: String): EnumSetting<E> {
        return setting(EnumSetting(name, value, visibility, consumer, description))
    }

    override fun IPluginClass.setting(name: String, value: Boolean, visibility: () -> Boolean, consumer: (prev: Boolean, input: Boolean) -> Boolean, description: String): BooleanSetting {
        return setting(BooleanSetting(name, value, visibility, consumer, description))
    }

    override fun IPluginClass.setting(name: String, value: ColorHolder, hasAlpha: Boolean, visibility: () -> Boolean, description: String): ColorSetting {
        return setting(ColorSetting(name, value, hasAlpha, visibility, description))
    }

    override fun IPluginClass.setting(name: String, value: String, visibility: () -> Boolean, consumer: (prev: String, input: String) -> String, description: String): StringSetting {
        return setting(StringSetting(name, value, visibility, consumer, description))
    }

    override fun IPluginClass.setting(name: String, value: Double, range: ClosedFloatingPointRange<Double>, step: Double, visibility: () -> Boolean, consumer: (prev: Double, input: Double) -> Double, description: String, fineStep: Double): DoubleSetting {
        return setting(DoubleSetting(name, value, range, step, visibility, consumer, description))
    }

    override fun IPluginClass.setting(name: String, value: Float, range: ClosedFloatingPointRange<Float>, step: Float, visibility: () -> Boolean, consumer: (prev: Float, input: Float) -> Float, description: String, fineStep: Float): FloatSetting {
        return setting(FloatSetting(name, value, range, step, visibility, consumer, description, fineStep))
    }

    override fun IPluginClass.setting(name: String, value: Int, range: IntRange, step: Int, visibility: () -> Boolean, consumer: (prev: Int, input: Int) -> Int, description: String, fineStep: Int): IntegerSetting {
        return setting(IntegerSetting(name, value, range, step, visibility, consumer, description, fineStep))
    }

    override fun IPluginClass.setting(name: String, value: Bind, visibility: () -> Boolean, description: String): BindSetting {
        return setting(BindSetting(name, value, visibility, description))
    }
}