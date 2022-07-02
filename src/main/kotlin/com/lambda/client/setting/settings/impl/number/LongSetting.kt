package com.lambda.client.setting.settings.impl.number

import com.google.gson.JsonElement

class LongSetting(
    name: String,
    value: Long,
    range: LongRange,
    step: Long,
    visibility: () -> Boolean = { true },
    consumer: (prev: Long, input: Long) -> Long = { _, input -> input },
    description: String = "",
    fineStep: Long = step
) : NumberSetting<Long>(name, value, range, step, visibility, consumer, description, fineStep) {

    init {
        consumers.add(0) { _, it ->
            it.coerceIn(range)
        }
    }

    override fun read(jsonElement: JsonElement?) {
        jsonElement?.asJsonPrimitive?.asLong?.let { value = it }
    }

    override fun setValue(valueIn: Double) {
        value = valueIn.toFloat().toLong()
    }

}