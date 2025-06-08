package com.atk.atk_cargo.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Green200,
    onPrimary = Gray900,
    primaryContainer = Green700,
    onPrimaryContainer = Green50,
    secondary = Teal200,
    onSecondary = Gray900,
    secondaryContainer = Teal700,
    onSecondaryContainer = Teal50,
    tertiary = Amber200,
    onTertiary = Gray900,
    tertiaryContainer = Amber700,
    onTertiaryContainer = Amber50,
    error = DeepOrange200,
    onError = Gray900,
    errorContainer = DeepOrange700,
    onErrorContainer = DeepOrange50,
    background = DarkBackground,
    onBackground = Gray50,
    surface = DarkSurface,
    onSurface = Gray50,
    surfaceVariant = Gray800,
    onSurfaceVariant = Gray300,
    outline = Gray600
)

private val LightColorScheme = lightColorScheme(
    primary = Green500,
    onPrimary = Color.White,
    primaryContainer = Green100,
    onPrimaryContainer = Green900,
    secondary = Teal200,
    onSecondary = Color.White,
    secondaryContainer = Teal100,
    onSecondaryContainer = Teal900,
    tertiary = Amber200,
    onTertiary = Color.White,
    tertiaryContainer = Amber100,
    onTertiaryContainer = Amber900,
    error = DeepOrange700,
    onError = Color.White,
    errorContainer = DeepOrange100,
    onErrorContainer = DeepOrange900,
    background = Gray50,
    onBackground = Gray900,
    surface = Color.White,
    onSurface = Gray900,
    surfaceVariant = Gray100,
    onSurfaceVariant = Gray700,
    outline = Gray400
)

fun getCompletionColor(percentage: Float, isDarkTheme: Boolean): Color {
    return when {
        percentage >= 95f -> if (isDarkTheme) Green300 else Green700
        percentage >= 85f -> if (isDarkTheme) Green200 else Green600
        percentage >= 75f -> if (isDarkTheme) Green100 else Green500
        percentage >= 65f -> if (isDarkTheme) Teal100 else Teal700
        percentage >= 55f -> if (isDarkTheme) Amber100 else Amber700
        percentage >= 45f -> if (isDarkTheme) Amber50 else Amber900
        percentage >= 35f -> if (isDarkTheme) DeepOrange100 else DeepOrange700
        percentage >= 25f -> if (isDarkTheme) DeepOrange50 else DeepOrange900
        percentage >= 15f -> if (isDarkTheme) Gray300 else Gray700
        else -> if (isDarkTheme) Gray200 else Gray800
    }
}

@Composable
fun ATKCargoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}