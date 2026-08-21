package com.atk.atk_cargo.domain.session

import kotlinx.coroutines.flow.Flow

// انتزاع نازک روی UserPreferencesManager در core:domain تا featureها بدون چرخه‌ی وابستگی به آن دسترسی داشته باشند
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
