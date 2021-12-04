package com.defrag.client.util

import java.util.*
import java.util.regex.Pattern

object TextUtil {
    const val SECTIONSIGN = "\u00a7"
    private val STRIP_COLOR_PATTERN = Pattern.compile("(?i)" + "\u00a7" + "[0-9A-FK-OR]")
    const val BLACK = "\u00a70"
    const val DARK_BLUE = "\u00a71"
    const val DARK_GREEN = "\u00a72"
    const val DARK_AQUA = "\u00a73"
    const val DARK_RED = "\u00a74"
    const val DARK_PURPLE = "\u00a75"
    const val GOLD = "\u00a76"
    const val GRAY = "\u00a77"
    const val DARK_GRAY = "\u00a78"
    const val BLUE = "\u00a79"
    const val GREEN = "\u00a7a"
    const val AQUA = "\u00a7b"
    const val RED = "\u00a7c"
    const val LIGHT_PURPLE = "\u00a7d"
    const val YELLOW = "\u00a7e"
    const val WHITE = "\u00a7f"
    const val OBFUSCATED = "\u00a7k"
    const val BOLD = "\u00a7l"
    const val STRIKE = "\u00a7m"
    const val UNDERLINE = "\u00a7n"
    const val ITALIC = "\u00a7o"
    const val RESET = "\u00a7r"
    const val RAINBOW = "\u00a7+"
    const val blank =
        " \u2592\u2592\u2592\u2592\u2592\u2592\u2592\u2592\u2592\u2592\u2592\u2592\u2592\u2592\u2592\u2592\u2592\u2592\u2592\u2592\u2592\u2592\u2592"
    const val line1 =
        " \u2588\u2588\u2588\u2592\u2588\u2592\u2588\u2592\u2588\u2588\u2588\u2592\u2588\u2588\u2588\u2592\u2588\u2588\u2588\u2592\u2588\u2588\u2588"
    const val line2 =
        " \u2588\u2592\u2588\u2592\u2588\u2592\u2588\u2592\u2588\u2592\u2588\u2592\u2588\u2592\u2588\u2592\u2588\u2592\u2588\u2592\u2588\u2592\u2592"
    const val line3 =
        " \u2588\u2588\u2588\u2592\u2588\u2588\u2588\u2592\u2588\u2592\u2588\u2592\u2588\u2588\u2588\u2592\u2588\u2592\u2588\u2592\u2588\u2588\u2588"
    const val line4 =
        " \u2588\u2592\u2592\u2592\u2588\u2592\u2588\u2592\u2588\u2592\u2588\u2592\u2588\u2592\u2588\u2592\u2588\u2592\u2588\u2592\u2592\u2592\u2588"
    const val line5 =
        " \u2588\u2592\u2592\u2592\u2588\u2592\u2588\u2592\u2588\u2588\u2588\u2592\u2588\u2588\u2588\u2592\u2588\u2588\u2588\u2592\u2588\u2588\u2588"
    const val pword =
        "  \u2592\u2592\u2592\u2592\u2592\u2592\u2592\u2592\u2592\u2592\u2592\u2592\u2592\u2592\u2592\u2592\u2592\u2592\u2592\u2592\u2592\u2592\u2592\n \u2588\u2588\u2588\u2592\u2588\u2592\u2588\u2592\u2588\u2588\u2588\u2592\u2588\u2588\u2588\u2592\u2588\u2588\u2588\u2592\u2588\u2588\u2588\n \u2588\u2592\u2588\u2592\u2588\u2592\u2588\u2592\u2588\u2592\u2588\u2592\u2588\u2592\u2588\u2592\u2588\u2592\u2588\u2592\u2588\u2592\u2592\n \u2588\u2588\u2588\u2592\u2588\u2588\u2588\u2592\u2588\u2592\u2588\u2592\u2588\u2588\u2588\u2592\u2588\u2592\u2588\u2592\u2588\u2588\u2588\n \u2588\u2592\u2592\u2592\u2588\u2592\u2588\u2592\u2588\u2592\u2588\u2592\u2588\u2592\u2588\u2592\u2588\u2592\u2588\u2592\u2592\u2592\u2588\n \u2588\u2592\u2592\u2592\u2588\u2592\u2588\u2592\u2588\u2588\u2588\u2592\u2588\u2588\u2588\u2592\u2588\u2588\u2588\u2592\u2588\u2588\u2588\n \u2592\u2592\u2592\u2592\u2592\u2592\u2592\u2592\u2592\u2592\u2592\u2592\u2592\u2592\u2592\u2592\u2592\u2592\u2592\u2592\u2592\u2592\u2592"
    var shrug = "\u00af\\_(\u30c4)_/\u00af"
    private val rand = Random()
    fun stripColor(input: String?): String? {
        return if (input == null) {
            null
        } else STRIP_COLOR_PATTERN.matcher(input).replaceAll("")
    }

    fun coloredString(string: String, color: Color?): String {
        var coloredString = string
        when (color) {
            Color.AQUA -> {
                coloredString = AQUA + coloredString + RESET
            }
            Color.WHITE -> {
                coloredString = WHITE + coloredString + RESET
            }
            Color.BLACK -> {
                coloredString = BLACK + coloredString + RESET
            }
            Color.DARK_BLUE -> {
                coloredString = DARK_BLUE + coloredString + RESET
            }
            Color.DARK_GREEN -> {
                coloredString = DARK_GREEN + coloredString + RESET
            }
            Color.DARK_AQUA -> {
                coloredString = DARK_AQUA + coloredString + RESET
            }
            Color.DARK_RED -> {
                coloredString = DARK_RED + coloredString + RESET
            }
            Color.DARK_PURPLE -> {
                coloredString = DARK_PURPLE + coloredString + RESET
            }
            Color.GOLD -> {
                coloredString = GOLD + coloredString + RESET
            }
            Color.DARK_GRAY -> {
                coloredString = DARK_GRAY + coloredString + RESET
            }
            Color.GRAY -> {
                coloredString = GRAY + coloredString + RESET
            }
            Color.BLUE -> {
                coloredString = BLUE + coloredString + RESET
            }
            Color.RED -> {
                coloredString = RED + coloredString + RESET
            }
            Color.GREEN -> {
                coloredString = GREEN + coloredString + RESET
            }
            Color.LIGHT_PURPLE -> {
                coloredString = LIGHT_PURPLE + coloredString + RESET
            }
            Color.YELLOW -> {
                coloredString = YELLOW + coloredString + RESET
            }
        }
        return coloredString
    }

    fun cropMaxLengthMessage(s: String, i: Int): String {
        var output = ""
        if (s.length >= 256 - i) {
            output = s.substring(0, 256 - i)
        }
        return output
    }

    enum class Color {
        NONE, WHITE, BLACK, DARK_BLUE, DARK_GREEN, DARK_AQUA, DARK_RED, DARK_PURPLE, GOLD, GRAY, DARK_GRAY, BLUE, GREEN, AQUA, RED, LIGHT_PURPLE, YELLOW
    }
}