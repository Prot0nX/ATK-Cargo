package com.atk.atk_cargo.api

import android.util.Log
import com.atk.atk_cargo.data.model.RefreshTokenResponse
import com.google.gson.Gson
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

// منطق مشترک POST /api/v2/auth/refresh بین TokenAuthenticator (رفرش واکنشی روی ۴۰۱) و SessionValidator (بررسی صریح در startup، چون checkSession هرگز ۴۰۱ نمی‌شود)
object TokenRefresher {
 // مشتق از HttpStack.shared (connection pool مشترک، ).
    private val httpClient: OkHttpClient by lazy {
        HttpStack.shared.newBuilder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
    }

 /** @return access token جدید در صورت موفقیت، یا null (و پاک‌شدن نشست محلی اگر سرور refresh token را قطعاً رد کرده باشد) */
    suspend fun refresh(baseUrl: String, tokenStore: TokenStore): String? {
 // AuthSession در cold start ممکن است هنوز از DataStore پر نشده باشد؛ برای حذف این race مستقیماً از tokenStore خوانده می‌شود
        val username = tokenStore.getUsername()
        val deviceId = tokenStore.getDeviceId()
        val refreshToken = tokenStore.getRefreshToken()

        if (username.isEmpty() || deviceId.isEmpty() || refreshToken.isEmpty()) {
            return null
        }

        return try {
            val formBody = FormBody.Builder()
                .add("username", username)
                .add("deviceId", deviceId)
                .add("refreshToken", refreshToken)
                .build()

 // مسیر تمیز /api/v2/auth/refresh روی این هاست کار نمی‌کند (mod_rewrite فعال نیست)، پس از الگوی query-string استفاده می‌شود
            val url = baseUrl.trimEnd('/') + "/api/v2/index.php?route=auth/refresh"
            val request = Request.Builder().url(url).post(formBody).build()

            httpClient.newCall(request).execute().use { httpResponse ->
                val bodyStr = httpResponse.body?.string()
                if (!httpResponse.isSuccessful || bodyStr.isNullOrEmpty()) {
                    if (httpResponse.code == 401) {
 // refresh token هم رد شد (منقضی یا سرقت‌شده)، پس نشست محلی هم کاملاً پاک می‌شود تا کاربر واقعاً به صفحه‌ی ورود برود
                        tokenStore.clearCredentials()
                    }
                    return null
                }

                val parsed = Gson().fromJson(bodyStr, RefreshTokenResponse::class.java)
                if (parsed?.success != true || parsed.sessionToken.isNullOrEmpty() || parsed.refreshToken.isNullOrEmpty()) {
                    return null
                }

                tokenStore.saveRefreshedTokens(parsed.sessionToken, parsed.refreshToken)
                parsed.sessionToken
            }
        } catch (e: Exception) {
            Log.w("TokenRefresher", "خطا هنگام تمدید access token", e)
            null
        }
    }
}