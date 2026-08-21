package com.atk.atk_cargo.ui.theme

import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf

// توکن‌های حرکت و انیمیشن بر اساس Material Design 3 Motion System
@Immutable
data class Motion(
    val durationShort1: Int = 50,
    val durationShort2: Int = 100,
    val durationMedium1: Int = 150,
    val durationMedium2: Int = 200,
    val durationLong1: Int = 300,
    val durationLong2: Int = 400,
    val durationExtraLong: Int = 500,
    
    val easingStandard: Easing = FastOutSlowInEasing,
    val easingDecelerate: Easing = LinearOutSlowInEasing,
    val easingAccelerate: Easing = FastOutLinearInEasing
)

val LocalMotion = staticCompositionLocalOf { Motion() }
