package com.defrag.client.manager.managers

import com.defrag.client.manager.Manager
import com.defrag.client.util.threads.BackgroundScope
import java.net.InetSocketAddress
import java.net.Socket

object NetworkManager : Manager {

    var isOffline = false; private set

    init {
        BackgroundScope.launchLooping("offline", 1500L) {
            @Suppress("BlockingMethodInNonBlockingContext")
            isOffline = try {
                Socket().use { socket ->
                    socket.connect(InetSocketAddress("1.1.1.1", 80), 100)
                    false
                }
            } catch (e: Exception) {
                true // Either timeout or unreachable or failed DNS lookup.
            }
        }
    }
}