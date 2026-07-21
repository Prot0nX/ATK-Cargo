package com.atk.atk_cargo.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * تابع رنگ‌بندی درصد تکمیل (جهت سازگاری با بخش‌های موجود).
 */
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
        else              -> if (isDarkTheme) Gray200 else Gray800
    }
}

/**
 * تم اصلی سازمانی ATK-Cargo بر پایه Material Design 3 و سیستم توکن‌های طراحی.
 */
@Composable
fun ATKCargoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    primaryColor: Color = PrimaryBlue,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        buildAppDarkColorScheme(primaryColor)
    } else {
        buildAppLightColorScheme(primaryColor)
    }

    val semanticColors = if (darkTheme) DarkSemanticColors else LightSemanticColors
    val typography = createTypography()
    val adaptiveConfig = rememberAdaptiveLayoutConfig()

    val spacing = Spacing()
    val dimensions = Dimensions()
    val appShapes = AppShapes()
    val elevation = Elevation()
    val motion = Motion()
    val componentStyles = ComponentStyles()

    CompositionLocalProvider(
        LocalSpacing provides spacing,
        LocalDimensions provides dimensions,
        LocalAppShapes provides appShapes,
        LocalElevation provides elevation,
        LocalMotion provides motion,
        LocalSemanticColors provides semanticColors,
        LocalComponentStyles provides componentStyles,
        LocalAdaptiveLayout provides adaptiveConfig
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            shapes = Material3Shapes,
            typography = typography,
            content = content
        )
    }
}

// ثوابت سازگاری انحناها
val CornerM = 8.dp
val CornerL = 12.dp
val CornerXL = 16.dp
val Corner2XL = 20.dp
val Corner3XL = 24.dp