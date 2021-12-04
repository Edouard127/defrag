package com.defrag.client.module

import com.defrag.commons.interfaces.DisplayEnum

enum class Category(override val displayName: String) : DisplayEnum {
    CHAT("Chat"),
    CLIENT("Client"),
    COMBAT("Combat"),
    MISC("Misc"),
    MOVEMENT("Movement"),
    PLAYER("Player"),
    RENDER("Render"),
    EXTRA("Extra");

    override fun toString() = displayName
}