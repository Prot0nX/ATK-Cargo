package com.atk.atk_cargo.core.domain

/**
 * از `feature/startup/domain` به `core:domain` منتقل شد (Phase4 #34) — یک
 * `object` سراسری بدون هیچ وابستگی به Compose/Context است، پس محل طبیعی‌اش
 * ماژولی است که همه‌ی featureهای دارای انیمیشن (از جمله feature:admin که
 * به feature:startup وابسته نیست) بتوانند بدون یک لبه‌ی ماژولی جدید و
 * نامرتبط به آن دسترسی داشته باشند.
 */
object AnimationManager {
    private var performanceScore: Int = 50
    private var performanceAllowsAnimations: Boolean = true

    // پیش‌فرض true تا وقتی MainActivity مقدار واقعی سیستم را در startup
    // بخواند (Settings.Global.ANIMATOR_DURATION_SCALE) — کاربری که
    // «حذف انیمیشن‌ها» را در تنظیمات سیستم فعال کرده (الزام دسترس‌پذیری
    // برای افراد حساس به حرکت، یا صرفه‌جویی باتری) دیگر انیمیشن نمی‌بیند
    // (DEEP_CODE_AUDIT.md #Phase3.12).
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
