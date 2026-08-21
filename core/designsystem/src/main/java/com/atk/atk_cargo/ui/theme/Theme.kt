package com.atk.atk_cargo.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// توکن‌های ثابت طراحی، مستقل از تم و دستگاه‌اند، پس نیازی به ساخت دوباره در هر recomposition ندارند
private val AppSpacing = Spacing()
private val AppDimensions = Dimensions()
private val AppShapesTokens = AppShapes()
private val AppElevation = Elevation()
private val AppMotion = Motion()
private val AppComponentStyles = ComponentStyles()

// تابع رنگ‌بندی بر اساس درصد تکمیل (برای سازگاری با بخش‌های موجود)
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

// تم اصلی سازمانی ATK-Cargo بر پایه Material Design 3 و سیستم توکن‌های طراحی
@Composable
fun ATKCargoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    primaryColor: Color = PrimaryBlue,
    content: @Composable () -> Unit
) {
    val colorScheme = remember(darkTheme, primaryColor) {
        if (darkTheme) {
            buildAppDarkColorScheme(primaryColor)
        } else {
            buildAppLightColorScheme(primaryColor)
        }
    }

    val semanticColors = if (darkTheme) DarkSemanticColors else LightSemanticColors
    val typography = createTypography()
    val adaptiveConfig = rememberAdaptiveLayoutConfig()

    CompositionLocalProvider(
        LocalSpacing provides AppSpacing,
        LocalDimensions provides AppDimensions,
        LocalAppShapes provides AppShapesTokens,
        LocalElevation provides AppElevation,
        LocalMotion provides AppMotion,
        LocalSemanticColors provides semanticColors,
        LocalComponentStyles provides AppComponentStyles,
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