package com.lambda.client.util

import com.lambda.client.util.ColorUtil.ColorName
import org.lwjgl.opengl.GL11
import com.lambda.client.util.ColorUtil
import java.awt.Color
import java.util.ArrayList

class ColorUtil {
    private fun initColorList(): ArrayList<ColorName> {
        val colorList = ArrayList<ColorName>()
        colorList.add(ColorName("AliceBlue", 240, 248, 255))
        colorList.add(ColorName("AntiqueWhite", 250, 235, 215))
        colorList.add(ColorName("Aqua", 0, 255, 255))
        colorList.add(ColorName("Aquamarine", 127, 255, 212))
        colorList.add(ColorName("Azure", 240, 255, 255))
        colorList.add(ColorName("Beige", 245, 245, 220))
        colorList.add(ColorName("Bisque", 255, 228, 196))
        colorList.add(ColorName("Black", 0, 0, 0))
        colorList.add(ColorName("BlanchedAlmond", 255, 235, 205))
        colorList.add(ColorName("Blue", 0, 0, 255))
        colorList.add(ColorName("BlueViolet", 138, 43, 226))
        colorList.add(ColorName("Brown", 165, 42, 42))
        colorList.add(ColorName("BurlyWood", 222, 184, 135))
        colorList.add(ColorName("CadetBlue", 95, 158, 160))
        colorList.add(ColorName("Chartreuse", 127, 255, 0))
        colorList.add(ColorName("Chocolate", 210, 105, 30))
        colorList.add(ColorName("Coral", 255, 127, 80))
        colorList.add(ColorName("CornflowerBlue", 100, 149, 237))
        colorList.add(ColorName("Cornsilk", 255, 248, 220))
        colorList.add(ColorName("Crimson", 220, 20, 60))
        colorList.add(ColorName("Cyan", 0, 255, 255))
        colorList.add(ColorName("DarkBlue", 0, 0, 139))
        colorList.add(ColorName("DarkCyan", 0, 139, 139))
        colorList.add(ColorName("DarkGoldenRod", 184, 134, 11))
        colorList.add(ColorName("DarkGray", 169, 169, 169))
        colorList.add(ColorName("DarkGreen", 0, 100, 0))
        colorList.add(ColorName("DarkKhaki", 189, 183, 107))
        colorList.add(ColorName("DarkMagenta", 139, 0, 139))
        colorList.add(ColorName("DarkOliveGreen", 85, 107, 47))
        colorList.add(ColorName("DarkOrange", 255, 140, 0))
        colorList.add(ColorName("DarkOrchid", 153, 50, 204))
        colorList.add(ColorName("DarkRed", 139, 0, 0))
        colorList.add(ColorName("DarkSalmon", 233, 150, 122))
        colorList.add(ColorName("DarkSeaGreen", 143, 188, 143))
        colorList.add(ColorName("DarkSlateBlue", 72, 61, 139))
        colorList.add(ColorName("DarkSlateGray", 47, 79, 79))
        colorList.add(ColorName("DarkTurquoise", 0, 206, 209))
        colorList.add(ColorName("DarkViolet", 148, 0, 211))
        colorList.add(ColorName("DeepPink", 255, 20, 147))
        colorList.add(ColorName("DeepSkyBlue", 0, 191, 255))
        colorList.add(ColorName("DimGray", 105, 105, 105))
        colorList.add(ColorName("DodgerBlue", 30, 144, 255))
        colorList.add(ColorName("FireBrick", 178, 34, 34))
        colorList.add(ColorName("FloralWhite", 255, 250, 240))
        colorList.add(ColorName("ForestGreen", 34, 139, 34))
        colorList.add(ColorName("Fuchsia", 255, 0, 255))
        colorList.add(ColorName("Gainsboro", 220, 220, 220))
        colorList.add(ColorName("GhostWhite", 248, 248, 255))
        colorList.add(ColorName("Gold", 255, 215, 0))
        colorList.add(ColorName("GoldenRod", 218, 165, 32))
        colorList.add(ColorName("Gray", 128, 128, 128))
        colorList.add(ColorName("Green", 0, 128, 0))
        colorList.add(ColorName("GreenYellow", 173, 255, 47))
        colorList.add(ColorName("HoneyDew", 240, 255, 240))
        colorList.add(ColorName("HotPink", 255, 105, 180))
        colorList.add(ColorName("IndianRed", 205, 92, 92))
        colorList.add(ColorName("Indigo", 75, 0, 130))
        colorList.add(ColorName("Ivory", 255, 255, 240))
        colorList.add(ColorName("Khaki", 240, 230, 140))
        colorList.add(ColorName("Lavender", 230, 230, 250))
        colorList.add(ColorName("LavenderBlush", 255, 240, 245))
        colorList.add(ColorName("LawnGreen", 124, 252, 0))
        colorList.add(ColorName("LemonChiffon", 255, 250, 205))
        colorList.add(ColorName("LightBlue", 173, 216, 230))
        colorList.add(ColorName("LightCoral", 240, 128, 128))
        colorList.add(ColorName("LightCyan", 224, 255, 255))
        colorList.add(ColorName("LightGoldenRodYellow", 250, 250, 210))
        colorList.add(ColorName("LightGray", 211, 211, 211))
        colorList.add(ColorName("LightGreen", 144, 238, 144))
        colorList.add(ColorName("LightPink", 255, 182, 193))
        colorList.add(ColorName("LightSalmon", 255, 160, 122))
        colorList.add(ColorName("LightSeaGreen", 32, 178, 170))
        colorList.add(ColorName("LightSkyBlue", 135, 206, 250))
        colorList.add(ColorName("LightSlateGray", 119, 136, 153))
        colorList.add(ColorName("LightSteelBlue", 176, 196, 222))
        colorList.add(ColorName("LightYellow", 255, 255, 224))
        colorList.add(ColorName("Lime", 0, 255, 0))
        colorList.add(ColorName("LimeGreen", 50, 205, 50))
        colorList.add(ColorName("Linen", 250, 240, 230))
        colorList.add(ColorName("Magenta", 255, 0, 255))
        colorList.add(ColorName("Maroon", 128, 0, 0))
        colorList.add(ColorName("MediumAquaMarine", 102, 205, 170))
        colorList.add(ColorName("MediumBlue", 0, 0, 205))
        colorList.add(ColorName("MediumOrchid", 186, 85, 211))
        colorList.add(ColorName("MediumPurple", 147, 112, 219))
        colorList.add(ColorName("MediumSeaGreen", 60, 179, 113))
        colorList.add(ColorName("MediumSlateBlue", 123, 104, 238))
        colorList.add(ColorName("MediumSpringGreen", 0, 250, 154))
        colorList.add(ColorName("MediumTurquoise", 72, 209, 204))
        colorList.add(ColorName("MediumVioletRed", 199, 21, 133))
        colorList.add(ColorName("MidnightBlue", 25, 25, 112))
        colorList.add(ColorName("MintCream", 245, 255, 250))
        colorList.add(ColorName("MistyRose", 255, 228, 225))
        colorList.add(ColorName("Moccasin", 255, 228, 181))
        colorList.add(ColorName("NavajoWhite", 255, 222, 173))
        colorList.add(ColorName("Navy", 0, 0, 128))
        colorList.add(ColorName("OldLace", 253, 245, 230))
        colorList.add(ColorName("Olive", 128, 128, 0))
        colorList.add(ColorName("OliveDrab", 107, 142, 35))
        colorList.add(ColorName("Orange", 255, 165, 0))
        colorList.add(ColorName("OrangeRed", 255, 69, 0))
        colorList.add(ColorName("Orchid", 218, 112, 214))
        colorList.add(ColorName("PaleGoldenRod", 238, 232, 170))
        colorList.add(ColorName("PaleGreen", 152, 251, 152))
        colorList.add(ColorName("PaleTurquoise", 175, 238, 238))
        colorList.add(ColorName("PaleVioletRed", 219, 112, 147))
        colorList.add(ColorName("PapayaWhip", 255, 239, 213))
        colorList.add(ColorName("PeachPuff", 255, 218, 185))
        colorList.add(ColorName("Peru", 205, 133, 63))
        colorList.add(ColorName("Pink", 255, 192, 203))
        colorList.add(ColorName("Plum", 221, 160, 221))
        colorList.add(ColorName("PowderBlue", 176, 224, 230))
        colorList.add(ColorName("Purple", 128, 0, 128))
        colorList.add(ColorName("Red", 255, 0, 0))
        colorList.add(ColorName("RosyBrown", 188, 143, 143))
        colorList.add(ColorName("RoyalBlue", 65, 105, 225))
        colorList.add(ColorName("SaddleBrown", 139, 69, 19))
        colorList.add(ColorName("Salmon", 250, 128, 114))
        colorList.add(ColorName("SandyBrown", 244, 164, 96))
        colorList.add(ColorName("SeaGreen", 46, 139, 87))
        colorList.add(ColorName("SeaShell", 255, 245, 238))
        colorList.add(ColorName("Sienna", 160, 82, 45))
        colorList.add(ColorName("Silver", 192, 192, 192))
        colorList.add(ColorName("SkyBlue", 135, 206, 235))
        colorList.add(ColorName("SlateBlue", 106, 90, 205))
        colorList.add(ColorName("SlateGray", 112, 128, 144))
        colorList.add(ColorName("Snow", 255, 250, 250))
        colorList.add(ColorName("SpringGreen", 0, 255, 127))
        colorList.add(ColorName("SteelBlue", 70, 130, 180))
        colorList.add(ColorName("Tan", 210, 180, 140))
        colorList.add(ColorName("Teal", 0, 128, 128))
        colorList.add(ColorName("Thistle", 216, 191, 216))
        colorList.add(ColorName("Tomato", 255, 99, 71))
        colorList.add(ColorName("Turquoise", 64, 224, 208))
        colorList.add(ColorName("Violet", 238, 130, 238))
        colorList.add(ColorName("Wheat", 245, 222, 179))
        colorList.add(ColorName("White", 255, 255, 255))
        colorList.add(ColorName("WhiteSmoke", 245, 245, 245))
        colorList.add(ColorName("Yellow", 255, 255, 0))
        colorList.add(ColorName("YellowGreen", 154, 205, 50))
        return colorList
    }

    fun getColorNameFromRgb(r: Int, g: Int, b: Int): String {
        val colorList = initColorList()
        var closestMatch: ColorName? = null
        var minMSE = Int.MAX_VALUE
        for (c in colorList) {
            val mse = c.computeMSE(r, g, b)
            if (mse >= minMSE) continue
            minMSE = mse
            closestMatch = c
        }
        return closestMatch?.name ?: "No matched color name."
    }

    fun getColorNameFromHex(hexColor: Int): String {
        val r = hexColor and 0xFF0000 shr 16
        val g = hexColor and 0xFF00 shr 8
        val b = hexColor and 0xFF
        return getColorNameFromRgb(r, g, b)
    }

    fun colorToHex(c: Color): Int {
        return Integer.decode("0x" + Integer.toHexString(c.rgb).substring(2))
    }

    fun getColorNameFromColor(color: Color): String {
        return getColorNameFromRgb(color.red, color.green, color.blue)
    }

    class HueCycler(cycles: Int) {
        var index = 0
        var cycles: IntArray
        fun reset() {
            index = 0
        }

        fun reset(index: Int) {
            this.index = index
        }

        operator fun next(): Int {
            val a = cycles[index]
            ++index
            if (index >= cycles.size) {
                index = 0
            }
            return a
        }

        fun setNext() {
            val rgb = next()
        }

        fun set() {
            val rgb = cycles[index]
            val red = (rgb shr 16 and 0xFF).toFloat() / 255.0f
            val green = (rgb shr 8 and 0xFF).toFloat() / 255.0f
            val blue = (rgb and 0xFF).toFloat() / 255.0f
            GL11.glColor3f(red, green, blue)
        }

        fun setNext(alpha: Float) {
            val rgb = next()
            val red = (rgb shr 16 and 0xFF).toFloat() / 255.0f
            val green = (rgb shr 8 and 0xFF).toFloat() / 255.0f
            val blue = (rgb and 0xFF).toFloat() / 255.0f
            GL11.glColor4f(red, green, blue, alpha)
        }

        fun current(): Int {
            return cycles[index]
        }

        init {
            require(cycles > 0) { "cycles <= 0" }
            this.cycles = IntArray(cycles)
            var hue = 0.0
            val add = 1.0 / cycles.toDouble()
            for (i in 0 until cycles) {
                this.cycles[i] = Color.HSBtoRGB(hue.toFloat(), 1.0f, 1.0f)
                hue += add
            }
        }
    }

    object Colors {
        val WHITE = toRGBA(255, 255, 255, 255)
        val BLACK = toRGBA(0, 0, 0, 255)
        val RED = toRGBA(255, 0, 0, 255)
        val GREEN = toRGBA(0, 255, 0, 255)
        val BLUE = toRGBA(0, 0, 255, 255)
        val ORANGE = toRGBA(255, 128, 0, 255)
        val PURPLE = toRGBA(163, 73, 163, 255)
        val GRAY = toRGBA(127, 127, 127, 255)
        val DARK_RED = toRGBA(64, 0, 0, 255)
        val YELLOW = toRGBA(255, 255, 0, 255)
        const val RAINBOW = Int.MIN_VALUE
    }

    class ColorName(var name: String, var r: Int, var g: Int, var b: Int) {
        fun computeMSE(pixR: Int, pixG: Int, pixB: Int): Int {
            return ((pixR - r) * (pixR - r) + (pixG - g) * (pixG - g) + (pixB - b) * (pixB - b)) / 3
        }
    }

    companion object {
        fun toRGBA(r: Double, g: Double, b: Double, a: Double): Int {
            return toRGBA(r.toFloat(), g.toFloat(), b.toFloat(), a.toFloat())
        }

        fun toRGBA(r: Int, g: Int, b: Int): Int {
            return toRGBA(r, g, b, 255)
        }

        fun toRGBA(r: Int, g: Int, b: Int, a: Int): Int {
            return (r shl 16) + (g shl 8) + b + (a shl 24)
        }

        fun toARGB(r: Int, g: Int, b: Int, a: Int): Int {
            return Color(r, g, b, a).rgb
        }

        fun toRGBA(r: Float, g: Float, b: Float, a: Float): Int {
            return toRGBA((r * 255.0f).toInt(), (g * 255.0f).toInt(), (b * 255.0f).toInt(), (a * 255.0f).toInt())
        }

        fun toRGBA(colors: FloatArray): Int {
            require(colors.size == 4) { "colors[] must have a length of 4!" }
            return toRGBA(colors[0], colors[1], colors[2], colors[3])
        }

        fun toRGBA(colors: DoubleArray): Int {
            require(colors.size == 4) { "colors[] must have a length of 4!" }
            return toRGBA(
                colors[0].toFloat(), colors[1].toFloat(), colors[2].toFloat(), colors[3].toFloat()
            )
        }

        @JvmStatic
        fun toRGBA(color: Color): Int {
            return toRGBA(color.red, color.green, color.blue, color.alpha)
        }

        fun toRGBAArray(colorBuffer: Int): IntArray {
            return intArrayOf(
                colorBuffer shr 16 and 0xFF,
                colorBuffer shr 8 and 0xFF,
                colorBuffer and 0xFF,
                colorBuffer shr 24 and 0xFF
            )
        }

        fun changeAlpha(origColor: Int, userInputedAlpha: Int): Int {
            var origColor = origColor
            return userInputedAlpha shl 24 or 0xFFFFFF.let { origColor = origColor and it; origColor }
        }
    }
}