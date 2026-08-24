package com.atk.atk_cargo.core.domain

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

// object سراسری بدون وابستگی به Context تا همه‌ی featureهای دارای انیمیشن بدون لبه‌ی ماژولی جدید به آن دسترسی داشته باشند snapshot state (نه var ساده) تا خواندن areAnimationsEnabled در Composable با تغییر تنظیم Reduce Motion واقعاً recompose شود
object AnimationManager {
 // پیش‌فرض true تا AtkCargoApplication.onCreate مقدار واقعی تنظیمات «حذف انیمیشن‌ها»ی سیستم را بخواند
    var systemAllowsAnimations: Boolean by mutableStateOf(true)
        private set

    fun setSystemAnimationsEnabled(enabled: Boolean) {
        systemAllowsAnimations = enabled
    }

    val areAnimationsEnabled: Boolean
        get() = systemAllowsAnimations
}