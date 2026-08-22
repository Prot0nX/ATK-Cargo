package com.atk.atk_cargo.core.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils

// کمک‌کننده‌های رنگ مخصوص UI که به‌دلیل وابستگی به Compose در designsystem نگه داشته شده‌اند، نه core:network.
class ColorSelector(private val colors: List<Color>) {
    private val assignedColors = mutableMapOf<String, Color>()
    private val usedColors = mutableMapOf<Color, Boolean>()
    private val random = java.util.Random(System.currentTimeMillis())

    init {
        colors.forEach { usedColors[it] = false }
    }

    fun assignDistinctColors(identifiers: Set<String>): Map<String, Color> {
        if (identifiers.size > colors.size) {
            return assignColorsWithGeneration(identifiers)
        }

        reset()
        val result = mutableMapOf<String, Color>()
        val availableColors = colors.toMutableList()

        identifiers.filter { assignedColors.containsKey(it) }.forEach { id ->
            val previousColor = assignedColors[id]
            if (previousColor != null && previousColor in availableColors) {
                result[id] = previousColor
                availableColors.remove(previousColor)
                usedColors[previousColor] = true
            }
        }

        identifiers.filter { !result.containsKey(it) }.forEach { id ->
            if (availableColors.isNotEmpty()) {
                val colorIndex = random.nextInt(availableColors.size)
                val selectedColor = availableColors[colorIndex]
                result[id] = selectedColor
                availableColors.removeAt(colorIndex)
                assignedColors[id] = selectedColor
                usedColors[selectedColor] = true
            }
        }
        return result
    }

    private fun assignColorsWithGeneration(identifiers: Set<String>): Map<String, Color> {
        val result = mutableMapOf<String, Color>()
        identifiers.forEachIndexed { index, id ->
            val color = if (index < colors.size) {
                colors[index]
            } else {
                val hue = (360f * index / identifiers.size) % 360f
                val saturation = 0.85f + (random.nextFloat() * 0.15f)
                val lightness = 0.4f + (random.nextFloat() * 0.15f)
                val hsl = floatArrayOf(hue, saturation, lightness)
                Color(ColorUtils.HSLToColor(hsl))
            }
            result[id] = color
            assignedColors[id] = color
        }
        return result
    }

    fun getNextColor(): Color {
        val unusedColors = usedColors.filter { !it.value }.keys.toList()
        if (unusedColors.isNotEmpty()) {
            val selectedColor = unusedColors[random.nextInt(unusedColors.size)]
            usedColors[selectedColor] = true
            return selectedColor
        }
        return colors[random.nextInt(colors.size)]
    }

    fun reset() {
        colors.forEach { usedColors[it] = false }
    }
}

fun adjustColorForTheme(color: Color, isDarkTheme: Boolean): Color {
    val hsl = FloatArray(3)
    ColorUtils.colorToHSL(color.toArgb(), hsl)
    hsl[2] = if (isDarkTheme) 0.65f else 0.45f
    hsl[1] = 0.85f
    return Color(ColorUtils.HSLToColor(hsl))
}

val cardColors = listOf(
    Color(0xFFEF5350), Color(0xFF66BB6A), Color(0xFF42A5F5), Color(0xFFEC407A),
    Color(0xFF26C6DA), Color(0xFFAB47BC), Color(0xFF26A69A), Color(0xFF8D6E63),
    Color(0xFF7E57C2), Color(0xFF29B6F6), Color(0xFFFF7043), Color(0xFF9CCC65),
    Color(0xFF5C6BC0), Color(0xFFFFCA28), Color(0xFF78909C), Color(0xFFD32F2F),
    Color(0xFF388E3C), Color(0xFF1976D2), Color(0xFFC2185B), Color(0xFF0097A7),
    Color(0xFF7B1FA2), Color(0xFF00796B), Color(0xFF5D4037), Color(0xFF512DA8),
    Color(0xFF0288D1), Color(0xFFF57C00), Color(0xFFE64A19), Color(0xFF689F38),
    Color(0xFF303F9F), Color(0xFFFBC020)
)
