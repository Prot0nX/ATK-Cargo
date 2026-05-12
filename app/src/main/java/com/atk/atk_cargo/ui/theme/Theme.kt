package com.atk.atk_cargo.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ===== تابع کمکی: ترکیب رنگ شفاف روی پس‌زمینه جهانی =====
private fun Color.compositeOver(background: Color): Color {
    val a = this.alpha
    return Color(
        red   = this.red   * a + background.red   * (1f - a),
        green = this.green * a + background.green * (1f - a),
        blue  = this.blue  * a + background.blue  * (1f - a),
        alpha = 1f
    )
}

// ===== ساخت ColorScheme پویا (Dark) بر اساس رنگ primary انتخابی =====
private fun buildDynamicDarkColorScheme(primary: Color) = darkColorScheme(
    primary              = primary,
    onPrimary            = Color.White,
    primaryContainer     = primary.copy(alpha = 0.25f).compositeOver(Color(0xFF0f172a)),
    onPrimaryContainer   = primary.copy(alpha = 0.90f),
    secondary            = primary,
    onSecondary          = Color.White,
    secondaryContainer   = primary.copy(alpha = 0.20f).compositeOver(Color(0xFF0f172a)),
    onSecondaryContainer = primary.copy(alpha = 0.90f),
    tertiary             = primary,
    onTertiary           = Color.White,
    tertiaryContainer    = primary.copy(alpha = 0.20f).compositeOver(Color(0xFF0f172a)),
    onTertiaryContainer  = primary.copy(alpha = 0.90f),
    error                = ErrorDark,
    onError              = Color.White,
    errorContainer       = ErrorContainerDark,
    onErrorContainer     = Color(0xFFFFCDD2),
    background           = BackgroundDark,
    onBackground         = TextPrimaryDark,
    surface              = SurfaceDark,
    onSurface            = TextPrimaryDark,
    surfaceVariant       = BorderDark,
    onSurfaceVariant     = TextSecondaryDark,
    outline              = BorderDark
)

// ===== ساخت ColorScheme پویا (Light) بر اساس رنگ primary انتخابی =====
private fun buildDynamicLightColorScheme(primary: Color) = lightColorScheme(
    primary              = primary,
    onPrimary            = Color.White,
    primaryContainer     = primary.copy(alpha = 0.12f).compositeOver(Color.White),
    onPrimaryContainer   = primary,
    secondary            = primary,
    onSecondary          = Color.White,
    secondaryContainer   = primary.copy(alpha = 0.12f).compositeOver(Color.White),
    onSecondaryContainer = primary,
    tertiary             = primary,
    onTertiary           = Color.White,
    tertiaryContainer    = primary.copy(alpha = 0.12f).compositeOver(Color.White),
    onTertiaryContainer  = primary,
    error                = ErrorLight,
    onError              = Color.White,
    errorContainer       = ErrorContainerLight,
    onErrorContainer     = ErrorLight,
    background           = BackgroundLight,
    onBackground         = TextPrimaryLight,
    surface              = SurfaceLight,
    onSurface            = TextPrimaryLight,
    surfaceVariant       = BorderLight,
    onSurfaceVariant     = TextSecondaryLight,
    outline              = BorderLight
)

// ===== تابع رنگ‌بندی درصد تکمیل (بدون تغییر) =====
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

// ===== تم اصلی برنامه با پشتیبانی از رنگ primary پویا =====
@Composable
fun ATKCargoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    primaryColor: Color = PrimaryBlue,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        buildDynamicDarkColorScheme(primaryColor)
    } else {
        buildDynamicLightColorScheme(primaryColor)
    }

    val vazirmatnFontFamily = VazirmatnFontFamily.create()

    val typography = Typography(
        displayLarge = TextStyle(
            fontFamily = vazirmatnFontFamily,
            fontWeight = FontWeight(400),
            fontSize = 57.sp
        ),
        displayMedium = TextStyle(
            fontFamily = vazirmatnFontFamily,
            fontWeight = FontWeight(400),
            fontSize = 45.sp
        ),
        displaySmall = TextStyle(
            fontFamily = vazirmatnFontFamily,
            fontWeight = FontWeight(400),
            fontSize = 36.sp
        ),
        headlineLarge = TextStyle(
            fontFamily = vazirmatnFontFamily,
            fontWeight = FontWeight(500),
            fontSize = 32.sp
        ),
        headlineMedium = TextStyle(
            fontFamily = vazirmatnFontFamily,
            fontWeight = FontWeight(500),
            fontSize = 28.sp
        ),
        headlineSmall = TextStyle(
            fontFamily = vazirmatnFontFamily,
            fontWeight = FontWeight(500),
            fontSize = 20.sp
        ),
        titleLarge = TextStyle(
            fontFamily = vazirmatnFontFamily,
            fontWeight = FontWeight(500),
            fontSize = 22.sp
        ),
        titleMedium = TextStyle(
            fontFamily = vazirmatnFontFamily,
            fontWeight = FontWeight(500),
            fontSize = 15.sp
        ),
        titleSmall = TextStyle(
            fontFamily = vazirmatnFontFamily,
            fontWeight = FontWeight(500),
            fontSize = 13.sp
        ),
        bodyLarge = TextStyle(
            fontFamily = vazirmatnFontFamily,
            fontWeight = FontWeight(400),
            fontSize = 16.sp
        ),
        bodyMedium = TextStyle(
            fontFamily = vazirmatnFontFamily,
            fontWeight = FontWeight(400),
            fontSize = 13.sp
        ),
        bodySmall = TextStyle(
            fontFamily = vazirmatnFontFamily,
            fontWeight = FontWeight(400),
            fontSize = 11.sp
        ),
        labelLarge = TextStyle(
            fontFamily = vazirmatnFontFamily,
            fontWeight = FontWeight(500),
            fontSize = 14.sp
        ),
        labelMedium = TextStyle(
            fontFamily = vazirmatnFontFamily,
            fontWeight = FontWeight(500),
            fontSize = 11.sp
        ),
        labelSmall = TextStyle(
            fontFamily = vazirmatnFontFamily,
            fontWeight = FontWeight(500),
            fontSize = 10.sp
        )
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        content = content
    )
}

val CornerM  = 8.dp
val CornerL  = 12.dp
val CornerXL = 16.dp
val Corner2XL = 20.dp
val Corner3XL = 24.dp