package com.defrag.client.event.events

import com.defrag.client.event.Event
import net.minecraft.util.math.BlockPos

class BlockBreakEvent(val breakerID: Int, val position: BlockPos, val progress: Int) : Event