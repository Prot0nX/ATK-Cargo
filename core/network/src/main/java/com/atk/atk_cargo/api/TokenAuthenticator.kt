package com.atk.atk_cargo.api

import com.atk.atk_cargo.data.model.AuthErrorBody
import com.google.gson.Gson
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

// تمدید خودکار access token با refresh token روی پاسخ ۴۰۱ با code="access_token_expired"؛ منطق واقعی HTTP در TokenRefresher مشترک است
class TokenAuthenticator(
    private val baseUrl: String,
    private val tokenStore: TokenStore
) : Authenticator {

    private val mutex = Mutex()

    override fun authenticate(route: Route?, response: Response): Request? {
 // اگر همان درخواست قبلاً یک‌بار retry شده، دیگر تلاش نکن تا حلقه‌ی بی‌نهایت پیش نیاید
        if (responseCount(response) >= 2) {
            return null
        }

        if (!isAccessTokenExpiredError(response)) {
            return null
        }

        val failedToken = response.request.header("X-Session-Token")

        val newAccessToken = runBlocking {
            mutex.withLock {
 // اگر یک درخواست موازی دیگر قبلاً همین رفرش را انجام داده، نیازی به رفرش دوباره نیست
                val current = AuthSession.sessionToken
                if (current.isNotEmpty() && current != failedToken) {
                    current
                } else {
                    TokenRefresher.refresh(baseUrl, tokenStore)
                }
            }
        } ?: return null

        return response.request.newBuilder()
            .header("X-Session-Token", newAccessToken)
            .build()
    }

    private fun isAccessTokenExpiredError(response: Response): Boolean {
        if (response.code != 401) return false
        return try {
 // peekBody بدنه را مصرف نمی‌کند — پاسخ همچنان برای caller قابل خواندن می‌ماند
            val bodyStr = response.peekBody(2048).string()
            Gson().fromJson(bodyStr, AuthErrorBody::class.java)?.code == "access_token_expired"
        } catch (_: Exception) {
            false
        }
    }

    private fun responseCount(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }
}
