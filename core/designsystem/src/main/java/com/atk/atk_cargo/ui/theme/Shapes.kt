package com.atk.atk_cargo.ui.theme

import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.dp

/**
 * توکن‌های اشکال و انحناهای گوشه‌ها (Shapes Tokens) مطابق با Material Design 3.
 */
@Immutable
data class AppShapes(
    val none: CornerBasedShape = RoundedCornerShape(0.dp),
    val extraSmall: CornerBasedShape = RoundedCornerShape(4.dp),
    val small: CornerBasedShape = RoundedCornerShape(8.dp),
    val medium: CornerBasedShape = RoundedCornerShape(12.dp),
    val large: CornerBasedShape = RoundedCornerShape(16.dp),
    val extraLarge: CornerBasedShape = RoundedCornerShape(24.dp),
    val extraExtraLarge: CornerBasedShape = RoundedCornerShape(28.dp),
    val full: CornerBasedShape = RoundedCornerShape(50),
    
    // اشکال تخصصی کامپوننت‌ها
    val button: CornerBasedShape = RoundedCornerShape(12.dp),
    val card: CornerBasedShape = RoundedCornerShape(16.dp),
    val dialog: CornerBasedShape = RoundedCornerShape(24.dp),
    val bottomSheet: CornerBasedShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    val textField: CornerBasedShape = RoundedCornerShape(12.dp),
    val chip: CornerBasedShape = RoundedCornerShape(8.dp),
    val badge: CornerBasedShape = RoundedCornerShape(50)
)

val Material3Shapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

val LocalAppShapes = staticCompositionLocalOf { AppShapes() }
