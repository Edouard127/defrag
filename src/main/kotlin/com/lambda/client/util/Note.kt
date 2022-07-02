package com.lambda.client.util

import com.lambda.client.util.color.ColorHolder

enum class Note(val default: ColorHolder, val rainbow: ColorHolder) {
    F_SHARP_LOW(ColorHolder(85, 221, 192), ColorHolder(119, 215, 0)),
    G_LOW(ColorHolder(126, 196, 177), ColorHolder(149, 192, 0)),
    G_SHARP_LOW(ColorHolder(164, 164, 171), ColorHolder(178, 165, 0)),
    A_LOW(ColorHolder(196, 126, 177), ColorHolder(204, 134, 0)),
    A_SHARP_LOW(ColorHolder(221, 85, 192), ColorHolder(226, 101, 0)),
    B_LOW(ColorHolder(237, 44, 218), ColorHolder(243, 65, 0)),
    C_LOW(ColorHolder(243, 6, 6), ColorHolder(252, 30, 0)),
    C_SHARP_LOW(ColorHolder(237, 218, 44), ColorHolder(254, 0, 15)),
    D_LOW(ColorHolder(221, 192, 85), ColorHolder(247, 0, 51)),
    D_SHARP_LOW(ColorHolder(196, 177, 126), ColorHolder(232, 0, 90)),
    E_LOW(ColorHolder(164, 171, 164), ColorHolder(207, 0, 131)),
    F_LOW(ColorHolder(126, 177, 196), ColorHolder(174, 0, 169)),
    F_SHARP_HIGH(ColorHolder(85, 192, 221), ColorHolder(134, 0, 204)),
    G_HIGH(ColorHolder(44, 218, 237), ColorHolder(91, 0, 231)),
    G_SHARP_HIGH(ColorHolder(6, 6, 243), ColorHolder(45, 0, 249)),
    A_HIGH(ColorHolder(218, 44, 237), ColorHolder(2, 10, 254)),
    A_SHARP_HIGH(ColorHolder(192, 85, 221), ColorHolder(0, 55, 246)),
    B_HIGH(ColorHolder(177, 126, 196), ColorHolder(0, 104, 224)),
    C_HIGH(ColorHolder(171, 164, 164), ColorHolder(0, 154, 188)),
    C_SHARP_HIGH(ColorHolder(177, 196, 126), ColorHolder(0, 198, 141)),
    D_HIGH(ColorHolder(192, 221, 85), ColorHolder(0, 233, 88)),
    D_SHARP_HIGH(ColorHolder(218, 237, 44), ColorHolder(0, 252, 33)),
    E_HIGH(ColorHolder(6, 243, 6), ColorHolder(31, 252, 0)),
    F_HIGH(ColorHolder(44, 237, 218), ColorHolder(89, 232, 0)),
    F_SHARP_SUPER_HIGH(ColorHolder(85, 221, 192), ColorHolder(148, 193, 0))
}