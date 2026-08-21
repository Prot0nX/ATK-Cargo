package com.atk.atk_cargo.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalConfiguration

// رده‌بندی ابعاد صفحه نمایش برای لایه‌بندی انطباقی (Window Size Classes)
enum class WindowSizeClass {
    COMPACT, // گوشی هوشمند عمودی (< 600dp)
    MEDIUM,  // تبلت کوچک یا حالت افقی گوشی (600dp - 840dp)
    EXPANDED // تبلت بزرگ یا حالت دسکتاپ (> 840dp)
}

@Immutable
data class AdaptiveLayoutConfig(
    val windowSizeClass: WindowSizeClass = WindowSizeClass.COMPACT,
    val isTablet: Boolean = false,
    val columnsCount: Int = 1
)

val LocalAdaptiveLayout = staticCompositionLocalOf { AdaptiveLayoutConfig() }

@Composable
fun rememberAdaptiveLayoutConfig(): AdaptiveLayoutConfig {
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp

    val windowSizeClass = when {
        screenWidthDp < 600 -> WindowSizeClass.COMPACT
        screenWidthDp < 840 -> WindowSizeClass.MEDIUM
        else -> WindowSizeClass.EXPANDED
    }

    val isTablet = windowSizeClass != WindowSizeClass.COMPACT
    val columnsCount = when (windowSizeClass) {
        WindowSizeClass.COMPACT -> 1
        WindowSizeClass.MEDIUM -> 2
        WindowSizeClass.EXPANDED -> 3
    }

    return AdaptiveLayoutConfig(
        windowSizeClass = windowSizeClass,
        isTablet = isTablet,
        columnsCount = columnsCount
    )
}
