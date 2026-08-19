package com.atk.atk_cargo.api

/**
 * انتزاع نازک روی هویت نشست ذخیره‌شده — قبل از استخراج core:network،
 * TokenAuthenticator/TokenRefresher/SessionValidator مستقیماً UserPreferencesManager
 * (کلاس بزرگ‌تر با تنظیمات چت/نوتیفیکیشن/تم که هیچ ربطی به شبکه ندارند و
 * برای رمزنگاری به CryptoManager نیاز دارد) را می‌گرفتند. آن کلاس عمداً در
 * app باقی ماند (DEEP_CODE_AUDIT.md #Phase4.2)؛ این اینترفیس مرز بین دو
 * ماژول است — UserPreferencesManager همچنان همان‌جا آن را پیاده‌سازی می‌کند.
 */
interface TokenStore {
    suspend fun getUsername(): String
    suspend fun getDeviceId(): String
    suspend fun getSessionToken(): String
    suspend fun getRefreshToken(): String
    suspend fun saveRefreshedTokens(accessToken: String, refreshToken: String)
    suspend fun clearCredentials()
}
