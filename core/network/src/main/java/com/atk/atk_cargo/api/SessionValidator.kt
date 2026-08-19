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
        val response = RetrofitClient.apiServiceV2.checkSession(request)

        if (response.isSuccessful && response.body()?.success == true) {
            return Result.success(true)
        }

        // I-05: checkSession همیشه HTTP ۲۰۰ برمی‌گرداند (حتی روی شکست، برای
        // سازگاری با کلاینت قدیمی)، پس هیچ‌وقت واقعاً ۴۰۱ نمی‌شود و
        // TokenAuthenticator اصلاً برای این درخواست صدا زده نمی‌شود. بدون این
        // تلاش صریح، کاربری که اپ را بعد از >۳۰ دقیقه (عمر access token) دوباره
        // باز می‌کند همیشه به صفحه‌ی ورود می‌رفت، حتی با refresh token کاملاً معتبر.
        val newAccessToken = TokenRefresher.refresh(Secrets.getBaseUrl(), userPreferencesManager)
        if (newAccessToken != null) {
            return Result.success(true)
        }

        userPreferencesManager.clearUserCredentials()
        Result.failure(Exception("Session invalid"))
    } catch (e: Exception) {
        Result.failure(e)
    }
}
