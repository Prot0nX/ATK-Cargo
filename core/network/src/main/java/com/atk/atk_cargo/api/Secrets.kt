package com.atk.atk_cargo.api

object Secrets {
    // بارگذاری ایمن؛ روی ABI پشتیبانی‌نشده (مثلاً release که فقط arm64-v8a می‌سازد) قبلاً
    // UnsatisfiedLinkError در همین initializer باعث ExceptionInInitializerError
    // غیرقابل‌بازیابی می‌شد. حالا isAvailable=false می‌شود و لایه‌ی بالاتر (StartupViewModel)
    // یک پیام خطای صریح نشان می‌دهد (DEEP_CODE_AUDIT.md #۱۶)
    val isAvailable: Boolean = try {
        System.loadLibrary("secrets")
        true
    } catch (_: UnsatisfiedLinkError) {
        false
    }

    external fun getBaseUrl(): String

    external fun getExpectedSignatureHash(): String
    external fun getLicenseStatusPrefKey(): String
    external fun getSignatureCheckUrl(): String
    external fun getLicenseCheckUrl(): String
    external fun getLicenseInfoUrl(): String
    external fun getLicenseKey(): String
    external fun getApiKey(): String
}