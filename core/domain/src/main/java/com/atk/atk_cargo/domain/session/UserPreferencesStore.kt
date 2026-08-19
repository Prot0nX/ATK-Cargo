package com.atk.atk_cargo.domain.session

import kotlinx.coroutines.flow.Flow

/**
 * انتزاع نازک روی UserPreferencesManager — همان الگوی TokenStore در
 * core:network (DEEP_CODE_AUDIT.md #Phase4.2/#Phase4.3). به‌جای اینکه هر
 * feature (auth، admin، ...) مستقیماً UserPreferencesManager (کلاس بزرگ‌تر
 * با تنظیمات چت/تم که در app باقی مانده) را بگیرد که چرخه‌ی وابستگی
 * می‌ساخت، این اینترفیس در core:domain (زیر همه‌ی featureها) تعریف شده؛
 * UserPreferencesManager همچنان در app آن را پیاده‌سازی می‌کند. ابتدا با نام
 * AuthPreferencesStore فقط برای feature:auth ساخته شده بود (Phase 5.10)؛
 * هنگام استخراج feature:admin که به همین سطح دسترسی (+ permissions) نیاز
 * داشت، به اینجا منتقل و کلی‌تر شد (Phase 5.12) تا مصرف‌کننده‌ی دوم feature
 * دیگری را وابسته به feature:auth نکند.
 */
interface UserPreferencesStore {
    val username: Flow<String>
    val userType: Flow<String>
    val deviceId: Flow<String>
    val sessionToken: Flow<String>
    val permissions: Flow<Map<String, Boolean>>

    suspend fun saveUserCredentials(
        username: String,
        userType: String,
        deviceId: String = "",
        sessionToken: String = "",
        permissions: Map<String, Boolean>? = null
    )

    suspend fun saveSessionToken(sessionToken: String)
    suspend fun saveRefreshToken(refreshToken: String)
    suspend fun setLoginState(isLoggedIn: Boolean)
    suspend fun clearUserCredentials()
}
