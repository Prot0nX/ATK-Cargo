package com.atk.atk_cargo.feature.startup.domain

object AnimationManager {
    private var performanceScore: Int = 50
    private var performanceAllowsAnimations: Boolean = true

    // پیش‌فرض true تا زمانی که MainActivity مقدار واقعی تنظیم سیستمی انیمیشن را بخواند
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
