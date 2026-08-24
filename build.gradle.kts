// فایل ساخت سطح ریشه برای تنظیمات مشترک زیرپروژه‌ها و ماژول‌ها.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.android.test) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.androidx.baselineprofile) apply false
    alias(libs.plugins.ksp) apply false
}