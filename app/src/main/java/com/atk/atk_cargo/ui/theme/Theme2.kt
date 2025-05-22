package com.atk.atk_cargo.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorPalette = darkColors(
    primary = Green300,
    primaryVariant = Green700,
    secondary = Teal200,
    secondaryVariant = Teal700,
    background = DarkBackground,
    surface = DarkSurface,
    error = DeepOrange300,
    onPrimary = Gray900,
    onSecondary = Gray900,
    onBackground = Gray100,
    onSurface = Gray100,
    onError = Gray900
)

private val LightColorPalette = lightColors(
    primary = Green500,
    primaryVariant = Green700,
    secondary = Teal200,
    secondaryVariant = Teal700,
    background = Gray50,
    surface = Color.White,
    error = DeepOrange700,
    onPrimary = Color.White,
    onSecondary = Gray900,
    onBackground = Gray900,
    onSurface = Gray900,
    onError = Color.White
)

@Composable
fun Theme2(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColorPalette else LightColorPalette

    MaterialTheme(
        colors = colors,
        content = content
    )
}