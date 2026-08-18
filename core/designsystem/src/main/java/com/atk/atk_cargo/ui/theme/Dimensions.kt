package com.atk.atk_cargo.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * توکن‌های ابعادی (Dimension Tokens) برای اجزای رابط کاربر مانند آیکون‌ها، دکمه‌ها، ورودی‌ها و کارت‌ها.
 */
@Immutable
data class Dimensions(
    val iconXs: Dp = 12.dp,
    val iconSmall: Dp = 16.dp,
    val iconMedium: Dp = 20.dp,
    val iconDefault: Dp = 24.dp,
    val iconLarge: Dp = 32.dp,
    val iconXLarge: Dp = 48.dp,
    val iconHuge: Dp = 64.dp,
    val buttonMinHeight: Dp = 48.dp,
    val buttonSmallHeight: Dp = 36.dp,
    val buttonLargeHeight: Dp = 56.dp,
    val inputMinHeight: Dp = 52.dp,
    val touchTargetMin: Dp = 48.dp,
    val dividerThickness: Dp = 1.dp,
    val borderWidthThin: Dp = 1.dp,
    val borderWidthMedium: Dp = 2.dp,
    val borderWidthThick: Dp = 3.dp,
    val avatarSmall: Dp = 32.dp,
    val avatarMedium: Dp = 40.dp,
    val avatarLarge: Dp = 56.dp,
    val cardCornerRadius: Dp = 16.dp,
    val cardMinHeight: Dp = 100.dp,
    val dialogCornerRadius: Dp = 24.dp,
    val sheetCornerRadius: Dp = 28.dp,
    val buttonCornerRadius: Dp = 12.dp
)

val LocalDimensions = staticCompositionLocalOf { Dimensions() }
