package com.defrag.client.manager.managers

import com.defrag.client.event.events.ConnectionEvent
import com.defrag.client.event.events.RenderOverlayEvent
import com.defrag.client.manager.Manager
import com.defrag.client.mixin.extension.syncCurrentPlayItem
import com.defrag.client.module.AbstractModule
import com.defrag.client.util.TaskState
import com.defrag.client.util.TickTimer
import com.defrag.client.util.TpsCalculator
import com.defrag.client.util.items.clickSlot
import com.defrag.client.util.items.removeHoldingItem
import com.defrag.client.util.threads.safeListener
import com.defrag.event.listener.listener
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.inventory.ClickType
import java.util.*

object PlayerInventoryManager : Manager {
    private val timer = TickTimer()
    private val lockObject = Any()
    private val actionQueue = TreeSet<InventoryTask>(Comparator.reverseOrder())

    private var currentId = 0
    private var currentTask: InventoryTask? = null

    init {
        safeListener<RenderOverlayEvent>(0) {
            if (!timer.tick((1000.0f / TpsCalculator.tickRate).toLong())) return@safeListener

            if (!player.inventory.itemStack.isEmpty) {
                if (mc.currentScreen is GuiContainer) timer.reset(250L) // Wait for 5 extra ticks if player is moving item
                else removeHoldingItem()
                return@safeListener
            }

            getTaskOrNext()?.nextInfo()?.let {
                clickSlot(it.windowId, it.slot, it.mouseButton, it.type)
                playerController.syncCurrentPlayItem()
            }

            if (actionQueue.isEmpty()) currentId = 0
        }

        listener<ConnectionEvent.Disconnect> {
            actionQueue.clear()
            currentId = 0
        }
    }

    private fun getTaskOrNext() =
        currentTask?.let {
            if (!it.isDone) it
            else null
        } ?: synchronized(lockObject) {
            actionQueue.removeIf { it.isDone }
            actionQueue.firstOrNull()
        }

    /**
     * Adds a new task to the inventory manager
     *
     * @param clickInfo group of the click info in this task
     *
     * @return [TaskState] representing the state of this task
     */
    fun AbstractModule.addInventoryTask(vararg clickInfo: ClickInfo) =
        InventoryTask(currentId++, modulePriority, clickInfo).let {
            actionQueue.add(it)
            it.taskState
        }

    private data class InventoryTask(
        private val id: Int,
        private val priority: Int,
        private val infoArray: Array<out ClickInfo>,
        val taskState: TaskState = TaskState(),
        private var index: Int = 0
    ) : Comparable<InventoryTask> {
        val isDone get() = taskState.done

        fun nextInfo() =
            infoArray.getOrNull(index++).also {
                if (it == null) taskState.done = true
            }


        override fun compareTo(other: InventoryTask): Int {
            val result = priority - other.priority
            return if (result != 0) result
            else other.id - id
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is InventoryTask) return false

            if (!infoArray.contentEquals(other.infoArray)) return false
            if (index != other.index) return false

            return true
        }

        override fun hashCode() = 31 * infoArray.contentHashCode() + index

    }

    data class ClickInfo(val windowId: Int = 0, val slot: Int, val mouseButton: Int = 0, val type: ClickType)
}