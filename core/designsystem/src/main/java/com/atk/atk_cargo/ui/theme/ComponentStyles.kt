package com.atk.atk_cargo.ui.theme

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.dp

// مشخصات و پدینگ‌های پیش‌فرض برای انواع استایل‌های کامپوننت
@Immutable
data class ComponentStyles(
    val buttonPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    val smallButtonPadding: PaddingValues = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
    val iconButtonPadding: PaddingValues = PaddingValues(8.dp),
    val cardPadding: PaddingValues = PaddingValues(16.dp),
    val inputPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    val dialogPadding: PaddingValues = PaddingValues(24.dp),
    val chipPadding: PaddingValues = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
)

val LocalComponentStyles = staticCompositionLocalOf { ComponentStyles() }
