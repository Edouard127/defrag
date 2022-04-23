package com.lambda.client.util.graphics.font

import java.awt.Font

enum class Style(val code: String, val codeChar: Char, val fontPath: String, val styleConst: Int) {
    REGULAR("§r", 'r', "/assets/fonts/Glass_TTY_VT220.ttf", Font.PLAIN),
    BOLD("§l", 'l', "/assets/fonts/FiraCode-Bold.ttf", Font.BOLD),
    ITALIC("§o", 'o', "/assets/fonts/FiraCode-Regular.ttf", Font.ITALIC)
}