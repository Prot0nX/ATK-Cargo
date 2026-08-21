package com.atk.atk_cargo.api

import com.atk.atk_cargo.data.model.SessionCheckRequest

suspend fun validateServerSession(
    tokenStore: TokenStore
): Result<Boolean> {
    return try {
        val username = tokenStore.getUsername()
        val deviceId = tokenStore.getDeviceId()
        val sessionToken = tokenStore.getSessionToken()

        if (username.isEmpty()) {
            return Result.failure(Exception("No user logged in"))
        }

        val request = SessionCheckRequest(username, deviceId, sessionToken)
        val response = RetrofitClient.apiServiceV2.checkSession(request)

        if (response.isSuccessful && response.body()?.success == true) {
            return Result.success(true)
        }

        // checkSession همیشه HTTP ۲۰۰ برمی‌گرداند پس هرگز ۴۰۱ نمی‌شود و TokenAuthenticator صدا زده نمی‌شود؛ بدون این تلاش صریح، کاربر با اپ سرد بعد از انقضای access token همیشه به صفحه‌ی ورود می‌رفت
        val newAccessToken = TokenRefresher.refresh(Secrets.getBaseUrl(), tokenStore)
        if (newAccessToken != null) {
            return Result.success(true)
        }

        tokenStore.clearCredentials()
        Result.failure(Exception("Session invalid"))
    } catch (e: Exception) {
        Result.failure(e)
    }
}
