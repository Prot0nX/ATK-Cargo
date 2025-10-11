package com.atk.atk_cargo.api

import kotlinx.coroutines.flow.first

suspend fun validateServerSession(
    userPreferencesManager: UserPreferencesManager
): Result<Boolean> {
    return try {
        val username = userPreferencesManager.username.first()
        val deviceId = userPreferencesManager.deviceId.first()
        val sessionToken = userPreferencesManager.sessionToken.first()
        
        if (username.isEmpty()) {
            return Result.failure(Exception("No user logged in"))
        }
        
        val request = SessionCheckRequest(username, deviceId, sessionToken)
        val response = RetrofitClient.apiService.checkSession(request)
        
        if (response.isSuccessful && response.body()?.success == true) {
            Result.success(true)
        } else {
            userPreferencesManager.clearUserCredentials()
            Result.failure(Exception("Session invalid"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}
