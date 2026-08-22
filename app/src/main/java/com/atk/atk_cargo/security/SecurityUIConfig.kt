package com.atk.atk_cargo.security

import androidx.compose.runtime.Composable
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// ثابت‌های مشترک شعاع گوشه و افکت شیشه‌ای برای صفحات امنیتی — از SecurityScreen.kt جدا شد (فاز۴ #۴۰)
internal object UIConfig {
    val CornerRadiusLarge = 28.dp
    val CornerRadiusMedium = 16.dp
    val CornerRadiusSmall = 12.dp
    
    // انیمیشن‌های پس‌زمینه
    const val BG_ANIMATION_DURATION_1 = 18000
    const val BG_ANIMATION_DURATION_2 = 24000
    
    // رنگ‌ها به صورت داینامیک از تم سیستم مشتق می‌شوند
    @Composable
    fun getGlassBorderBrush(isDark: Boolean): Brush {
        return Brush.verticalGradient(
            listOf(
                Color.White.copy(alpha = if (isDark) 0.15f else 0.40f),
                Color.White.copy(alpha = if (isDark) 0.03f else 0.12f)
            )
        )
    }
}
