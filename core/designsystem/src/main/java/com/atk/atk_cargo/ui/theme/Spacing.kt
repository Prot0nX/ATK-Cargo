package com.atk.atk_cargo.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * توکن‌های استاندارد فاصله‌گذاری (Spacing Tokens) مطابق با مقیاس Material Design 3.
 * تمامی فاصله‌ها، پدینگ‌ها و مارجین‌های برنامه‌ باید از این مقادیر استفاده کنند.
 */
@Immutable
data class Spacing(
    val none: Dp = 0.dp,
    val xxs: Dp = 2.dp,
    val xs: Dp = 4.dp,
    val s: Dp = 8.dp,
    val m: Dp = 12.dp,
    val l: Dp = 16.dp,
    val xl: Dp = 20.dp,
    val xxl: Dp = 24.dp,
    val xxxl: Dp = 28.dp,
    val huge: Dp = 32.dp,
    val xHuge: Dp = 40.dp,
    val xxHuge: Dp = 48.dp,
    val xxxHuge: Dp = 56.dp,
    val max: Dp = 64.dp,
    val screenPaddingHorizontal: Dp = 16.dp,
    val screenPaddingVertical: Dp = 16.dp,
    val cardContentPadding: Dp = 16.dp,
    val dialogContentPadding: Dp = 20.dp,
    val listItemPadding: Dp = 12.dp
)

val LocalSpacing = staticCompositionLocalOf { Spacing() }
