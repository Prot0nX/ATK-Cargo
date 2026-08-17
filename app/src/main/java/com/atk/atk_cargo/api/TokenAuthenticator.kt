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

/**
 * تمدید خودکار access token با refresh token (I-05) — طبق طراحی
 * PHP/docs/refresh_token_design.md، بخش ۴.۲. منطق واقعی HTTP در
 * TokenRefresher مشترک است (همان‌جا هم توسط SessionValidator در startup استفاده می‌شود).
 *
 * فقط وقتی وارد عمل می‌شود که پاسخ ۴۰۱ صریحاً code="access_token_expired"
 * داشته باشد (نه هر ۴۰۱ دلخواه — که ممکن است «کل نشست نامعتبر است» باشد و
 * تلاش برای refresh حتماً دوباره شکست می‌خورد).
 */
class TokenAuthenticator(
    private val baseUrl: String,
    private val userPreferencesManager: UserPreferencesManager
) : Authenticator {

    private val mutex = Mutex()

    override fun authenticate(route: Route?, response: Response): Request? {
        // اگر همان درخواست قبلاً یک‌بار retry شده (یعنی access token جدید هم
        // ۴۰۱ گرفته)، دیگر تلاش نکن — یا refresh endpoint خودش مشکل دارد یا
        // نشست واقعاً باطل شده (جلوگیری از حلقه‌ی بی‌نهایت).
        if (responseCount(response) >= 2) {
            return null
        }

        if (!isAccessTokenExpiredError(response)) {
            return null
        }

        val failedToken = response.request.header("X-Session-Token")

        val newAccessToken = runBlocking {
            mutex.withLock {
                // اگر یک درخواست موازی دیگر در فاصله‌ی صف‌ماندن روی این قفل
                // قبلاً همین رفرش را انجام داده باشد (AuthSession.sessionToken
                // دیگر با توکنی که این درخواست با آن شکست خورد یکی نیست)،
                // نیازی به رفرش دوباره (و rotate دوباره‌ی refresh token) نیست.
                val current = AuthSession.sessionToken
                if (current.isNotEmpty() && current != failedToken) {
                    current
                } else {
                    TokenRefresher.refresh(baseUrl, userPreferencesManager)
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
