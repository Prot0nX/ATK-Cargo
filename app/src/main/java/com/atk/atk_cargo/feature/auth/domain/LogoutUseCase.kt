package com.atk.atk_cargo.feature.auth.domain

import com.atk.atk_cargo.api.ApiService
import com.atk.atk_cargo.api.LogoutRequest
import com.atk.atk_cargo.api.UserPreferencesManager
import kotlinx.coroutines.flow.first

class LogoutUseCase(
    private val apiService: ApiService,
    private val userPreferencesManager: UserPreferencesManager
) {
    suspend operator fun invoke(username: String): Result<String?> {
        return try {
            val deviceId = userPreferencesManager.deviceId.first()
            val sessionToken = userPreferencesManager.sessionToken.first()
            val logoutRequest = LogoutRequest(
                username = username,
                deviceId = deviceId,
                sessionToken = sessionToken.takeIf { it.isNotEmpty() }
            )

            val response = apiService.logout(logoutRequest)
            userPreferencesManager.clearUserCredentials()
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success("خروج با موفقیت انجام شد")
            } else {
                val errorMessage = when (response.code()) {
                    400 -> "❌ درخواست نامعتبر"
                    401 -> "🔐 جلسه منقضی شده است"
                    404 -> "⚠️ جلسه فعالی یافت نشد"
                    500 -> "🔧 خطای داخلی سرور"
                    else -> "خطا در خروج (کد: ${response.code()})"
                }
                Result.success(errorMessage)
            }
        } catch (e: Exception) {
            userPreferencesManager.clearUserCredentials()
            Result.failure(e)
        }
    }
}
