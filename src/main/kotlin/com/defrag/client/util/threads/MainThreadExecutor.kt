package com.defrag.client.util.threads

import com.defrag.client.event.DefragEventBus
import com.defrag.client.event.events.RunGameLoopEvent
import com.defrag.client.util.Wrapper
import com.defrag.event.listener.listener
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.completeWith
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

object MainThreadExecutor {
    private val jobs = ArrayList<MainThreadJob<*>>()
    private val mutex = Mutex()

    init {
        listener<RunGameLoopEvent.Start>(Int.MIN_VALUE) {
            runJobs()
        }

        DefragEventBus.subscribe(this)
    }

    private fun runJobs() {
        if (jobs.isEmpty()) return

        runBlocking {
            mutex.withLock {
                jobs.forEach {
                    it.run()
                }
                jobs.clear()
            }
        }
    }

    suspend fun <T> add(block: () -> T) =
        MainThreadJob(block).apply {
            if (Wrapper.minecraft.isCallingFromMinecraftThread) {
                run()
            } else {
                mutex.withLock {
                    jobs.add(this)
                }
            }
        }.deferred

    private class MainThreadJob<T>(private val block: () -> T) {
        val deferred = CompletableDeferred<T>()

        fun run() {
            deferred.completeWith(
                runCatching { block.invoke() }
            )
        }
    }
}