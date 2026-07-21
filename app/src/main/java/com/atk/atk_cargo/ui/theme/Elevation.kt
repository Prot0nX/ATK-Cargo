package com.atk.atk_cargo.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * توکن‌های ارتفاع و سایه (Elevation Tokens) مطابق با مراتب عمق‌دهی Material Design 3.
 */
@Immutable
data class Elevation(
    val level0: Dp = 0.dp,
    val level1: Dp = 1.dp,
    val level2: Dp = 3.dp,
    val level3: Dp = 6.dp,
    val level4: Dp = 8.dp,
    val level5: Dp = 12.dp,
    val cardDefault: Dp = 2.dp,
    val cardHovered: Dp = 4.dp,
    val cardPressed: Dp = 1.dp,
    val dialog: Dp = 6.dp,
    val bottomSheet: Dp = 8.dp,
    val topAppBar: Dp = 2.dp,
    val fab: Dp = 6.dp
)

val LocalElevation = staticCompositionLocalOf { Elevation() }
