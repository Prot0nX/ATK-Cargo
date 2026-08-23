package com.atk.atk_cargo.api

import com.atk.atk_cargo.data.model.SessionCheckRequest

// نتیجه‌ی بررسی سه‌حالته‌ی نشست جهت تمایز بین نشست نامعتبر و خطای گذرای شبکه.
sealed interface SessionValidationOutcome {
    data object Valid : SessionValidationOutcome
    // نشست واقعاً نامعتبر است (سرور رد کرده و رفرش توکن هم شکست خورده)؛ clearCredentials() قبلاً فراخوانی شده و کاربر باید دوباره وارد شود
    data object Invalid : SessionValidationOutcome
    // خطای گذرای شبکه/سرور؛ اعتبارنامه‌ها دست‌نخورده مانده‌اند، کاربر می‌تواند دوباره تلاش کند
    data object NetworkError : SessionValidationOutcome
}

suspend fun validateServerSession(
    tokenStore: TokenStore
): SessionValidationOutcome {
    return try {
        val username = tokenStore.getUsername()
        val deviceId = tokenStore.getDeviceId()
        val sessionToken = tokenStore.getSessionToken()

        if (username.isEmpty()) {
            return SessionValidationOutcome.Invalid
        }

        val request = SessionCheckRequest(username, deviceId, sessionToken)
        val response = RetrofitClient.apiServiceV2.checkSession(request)

        if (response.isSuccessful && response.body()?.success == true) {
            return SessionValidationOutcome.Valid
        }

        // از نسخه‌ی ۴.۱.۰ به بعد checkSession می‌تواند ۴۰۱ هم بدهد، اما code آن 'session_invalid' است نه 'access_token_expired'، پس TokenAuthenticator (که فقط دومی را رفرش می‌کند) اینجا صدا زده نمی‌شود.
        val newAccessToken = TokenRefresher.refresh(Secrets.getBaseUrl(), tokenStore)
        if (newAccessToken != null) {
            return SessionValidationOutcome.Valid
        }

        tokenStore.clearCredentials()
        SessionValidationOutcome.Invalid
    } catch (e: Exception) {
        SessionValidationOutcome.NetworkError
    }
}