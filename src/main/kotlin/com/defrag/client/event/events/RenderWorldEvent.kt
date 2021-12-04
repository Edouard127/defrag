package com.defrag.client.event.events

import com.defrag.client.event.Event
import com.defrag.client.event.ProfilerEvent
import com.defrag.client.mixin.extension.renderPosX
import com.defrag.client.mixin.extension.renderPosY
import com.defrag.client.mixin.extension.renderPosZ
import com.defrag.client.util.Wrapper
import com.defrag.client.util.graphics.LambdaTessellator

class RenderWorldEvent : Event, ProfilerEvent {
    override val profilerName: String = "kbRender3D"

    init {
        LambdaTessellator.buffer.setTranslation(
            -Wrapper.minecraft.renderManager.renderPosX,
            -Wrapper.minecraft.renderManager.renderPosY,
            -Wrapper.minecraft.renderManager.renderPosZ
        )
    }
}