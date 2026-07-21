package com.atk.atk_cargo.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable

/**
 * دسترسی سیستماتیک و ایزوله به تمامی توکن‌های طراحی دیزاین سیستم ATK-Cargo.
 */
object ATKCargoTheme {
    val spacing: Spacing
        @Composable
        @ReadOnlyComposable
        get() = LocalSpacing.current

    val dimensions: Dimensions
        @Composable
        @ReadOnlyComposable
        get() = LocalDimensions.current

    val appShapes: AppShapes
        @Composable
        @ReadOnlyComposable
        get() = LocalAppShapes.current

    val elevation: Elevation
        @Composable
        @ReadOnlyComposable
        get() = LocalElevation.current

    val motion: Motion
        @Composable
        @ReadOnlyComposable
        get() = LocalMotion.current

    val semanticColors: SemanticColors
        @Composable
        @ReadOnlyComposable
        get() = LocalSemanticColors.current

    val styles: ComponentStyles
        @Composable
        @ReadOnlyComposable
        get() = LocalComponentStyles.current

    val adaptive: AdaptiveLayoutConfig
        @Composable
        @ReadOnlyComposable
        get() = LocalAdaptiveLayout.current
}
