package com.defrag.client.manager

import com.defrag.client.util.Wrapper

interface Manager {
    val mc get() = Wrapper.minecraft
}