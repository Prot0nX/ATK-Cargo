package com.atk.atk_cargo.api

// انتزاع نازک روی هویت نشست ذخیره‌شده؛ مرز بین ماژول core:network و UserPreferencesManager در app که این اینترفیس را پیاده‌سازی می‌کند
interface TokenStore {
    suspend fun getUsername(): String
    suspend fun getDeviceId(): String
    suspend fun getSessionToken(): String
    suspend fun getRefreshToken(): String
    suspend fun saveRefreshedTokens(accessToken: String, refreshToken: String)
    suspend fun clearCredentials()
}
