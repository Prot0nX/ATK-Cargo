package com.atk.atk_cargo.feature.auth.data

import kotlinx.coroutines.flow.Flow

/**
 * انتزاع نازک روی UserPreferencesManager — همان الگوی TokenStore در
 * core:network (DEEP_CODE_AUDIT.md #Phase4.2/#Phase4.3). قبل از استخراج
 * feature:auth، AuthRepositoryImpl/LogoutUseCase مستقیماً UserPreferencesManager
 * (کلاس بزرگ‌تر با تنظیمات چت/تم که در app باقی مانده) را می‌گرفتند —
 * برخلاف TokenStore این ماژول نمی‌تواند به app وابسته شود (app به این
 * ماژول وابسته است، نه برعکس)، پس این اینترفیس مرز است؛
 * UserPreferencesManager همچنان همان‌جا آن را پیاده‌سازی می‌کند.
 */
interface AuthPreferencesStore {
    val username: Flow<String>
    val userType: Flow<String>
    val deviceId: Flow<String>
    val sessionToken: Flow<String>

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
