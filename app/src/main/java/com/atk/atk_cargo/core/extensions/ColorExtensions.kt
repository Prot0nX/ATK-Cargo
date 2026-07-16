package com.atk.atk_cargo.core.extensions

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils

fun adjustColorForTheme(color: Color, isDarkTheme: Boolean): Color {
    val hsl = FloatArray(3)
    ColorUtils.colorToHSL(color.toArgb(), hsl)
    hsl[2] = if (isDarkTheme) 0.65f else 0.45f
    hsl[1] = 0.85f
    return Color(ColorUtils.HSLToColor(hsl))
}
