package com.defrag.client.event.events

import com.defrag.client.event.Event
import com.defrag.client.util.graphics.VertexHelper

class RenderRadarEvent(
    val vertexHelper: VertexHelper,
    val radius: Float,
    val scale: Float
) : Event