package com.atk.atk_cargo.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// تابع کمکی برای ترکیب رنگ شفاف روی پس‌زمینه
private fun Color.compositeOver(background: Color): Color {
    val a = this.alpha
    return Color(
        red = this.red * a + background.red * (1f - a),
        green = this.green * a + background.green * (1f - a),
        blue = this.blue * a + background.blue * (1f - a),
        alpha = 1f
    )
}

// ساخت ColorScheme پویا برای حالت تاریک، مطابق استاندارد MD3
fun buildAppDarkColorScheme(primary: Color): ColorScheme = darkColorScheme(
    primary = primary,
    onPrimary = Color.White,
    primaryContainer = primary.copy(alpha = 0.25f).compositeOver(BackgroundDark),
    onPrimaryContainer = primary.copy(alpha = 0.90f),
    secondary = primary,
    onSecondary = Color.White,
    secondaryContainer = primary.copy(alpha = 0.20f).compositeOver(BackgroundDark),
    onSecondaryContainer = primary.copy(alpha = 0.90f),
    tertiary = primary,
    onTertiary = Color.White,
    tertiaryContainer = primary.copy(alpha = 0.20f).compositeOver(BackgroundDark),
    onTertiaryContainer = primary.copy(alpha = 0.90f),
    error = ErrorDark,
    onError = Color.White,
    errorContainer = ErrorContainerDark,
    onErrorContainer = Color(0xFFFFCDD2),
    background = BackgroundDark,
    onBackground = TextPrimaryDark,
    surface = SurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = TextSecondaryDark,
    surfaceContainer = SurfaceVariantDark,
    surfaceContainerHigh = SurfaceVariantDark2,
    surfaceContainerHighest = SurfaceVariantDark3,
    outline = BorderDark,
    outlineVariant = BorderDark2,
    scrim = Color.Black.copy(alpha = 0.6f)
)

// ساخت ColorScheme پویا برای حالت روشن، مطابق استاندارد MD3
fun buildAppLightColorScheme(primary: Color): ColorScheme = lightColorScheme(
    primary = primary,
    onPrimary = Color.White,
    primaryContainer = primary.copy(alpha = 0.12f).compositeOver(Color.White),
    onPrimaryContainer = primary,
    secondary = primary,
    onSecondary = Color.White,
    secondaryContainer = primary.copy(alpha = 0.12f).compositeOver(Color.White),
    onSecondaryContainer = primary,
    tertiary = primary,
    onTertiary = Color.White,
    tertiaryContainer = primary.copy(alpha = 0.12f).compositeOver(Color.White),
    onTertiaryContainer = primary,
    error = ErrorLight,
    onError = Color.White,
    errorContainer = ErrorContainerLight,
    onErrorContainer = ErrorLight,
    background = BackgroundLight,
    onBackground = TextPrimaryLight,
    surface = SurfaceLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = BorderLight,
    onSurfaceVariant = TextSecondaryLight,
    surfaceContainer = Gray100,
    surfaceContainerHigh = Gray200,
    surfaceContainerHighest = Gray300,
    outline = BorderLight,
    outlineVariant = Gray300,
    scrim = Color.Black.copy(alpha = 0.4f)
)
