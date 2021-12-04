package com.defrag.client.util

open class Timer {
    var time = currentTime; protected set

    protected val currentTime get() = System.currentTimeMillis()

    fun reset(offset: Long = 0L) {
        time = currentTime + offset
    }

}

class TickTimer(val timeUnit: TimeUnit = TimeUnit.MILLISECONDS) : Timer() {
    fun tick(delay: Int, resetIfTick: Boolean = true): Boolean {
        return tick(delay.toLong(), resetIfTick)
    }

    fun tick(delay: Long, resetIfTick: Boolean = true): Boolean {
        return if (currentTime - time > delay * timeUnit.multiplier) {
            if (resetIfTick) time = currentTime
            true
        } else {
            false
        }
    }
}

class StopTimer(val timeUnit: TimeUnit = TimeUnit.MILLISECONDS) : Timer() {
    fun stop(): Long {
        return (currentTime - time) / timeUnit.multiplier
    }
}

enum class TimeUnit(val multiplier: Long) {
    MILLISECONDS(1L),
    TICKS(50L),
    SECONDS(1000L),
    MINUTES(60000L);

    class Timer {
        private var time = -1L
        fun passedS(s: Double): Boolean {
            return passedMs(s.toLong() * 1000L)
        }

        fun passedDms(dms: Double): Boolean {
            return passedMs(dms.toLong() * 10L)
        }

        fun passedDs(ds: Double): Boolean {
            return passedMs(ds.toLong() * 100L)
        }

        fun passedMs(ms: Long): Boolean {
            return passedNS(convertToNS(ms))
        }

        fun setMs(ms: Long) {
            time = System.nanoTime() - convertToNS(ms)
        }

        fun passedNS(ns: Long): Boolean {
            return System.nanoTime() - time >= ns
        }

        val passedTimeMs: Long
            get() = getMs(System.nanoTime() - time)

        fun reset(): Timer {
            time = System.nanoTime()
            return this
        }

        fun getMs(time: Long): Long {
            return time / 1000000L
        }

        fun convertToNS(time: Long): Long {
            return time * 1000000L
        }
    }
}