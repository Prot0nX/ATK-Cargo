package com.atk.atk_cargo.core.domain

// object سراسری بدون وابستگی به Compose/Context تا همه‌ی featureهای دارای انیمیشن بدون لبه‌ی ماژولی جدید به آن دسترسی داشته باشند
object AnimationManager {
    private var performanceScore: Int = 50
    private var performanceAllowsAnimations: Boolean = true

    // پیش‌فرض true تا MainActivity مقدار واقعی تنظیمات «حذف انیمیشن‌ها»ی سیستم را در startup بخواند
    private var systemAllowsAnimations: Boolean = true

    fun setPerformanceScore(score: Int) {
        performanceScore = score
        performanceAllowsAnimations = score >= 70
    }

    fun setSystemAnimationsEnabled(enabled: Boolean) {
        systemAllowsAnimations = enabled
    }

    fun areAnimationsEnabled(): Boolean {
        return performanceAllowsAnimations && systemAllowsAnimations
    }
}
