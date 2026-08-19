package com.atk.atk_cargo.api

import android.util.Log
import com.atk.atk_cargo.data.model.RefreshTokenResponse
import com.google.gson.Gson
import kotlinx.coroutines.flow.first
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

/**
 * منطق مشترک POST /api/v2/auth/refresh (I-05) — بین دو مصرف‌کننده به اشتراک
 * گذاشته شده تا پیاده‌سازی HTTP یک‌بار نوشته شود:
 *   ۱. TokenAuthenticator: silent refresh واکنشی روی ۴۰۱ با
 *      code=access_token_expired.
 *   ۲. SessionValidator (startup): checkSession همیشه HTTP ۲۰۰ برمی‌گرداند
 *      (حتی روی شکست، برای سازگاری با کلاینت قدیمی)، پس هیچ‌وقت ۴۰۱ نمی‌شود
 *      و Authenticator اصلاً برایش صدا زده نمی‌شود؛ بدون این فراخوانی صریح،
 *      کاربری که اپ را بعد از >۳۰ دقیقه دوباره باز می‌کند همیشه به صفحه‌ی
 *      ورود می‌رفت، حتی با یک refresh token کاملاً معتبر.
 */
object TokenRefresher {
    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    /**
     * @return access token جدید در صورت موفقیت، یا null (و در صورت رد قطعی
     *         شدن refresh token توسط سرور، پاک‌شدن کامل نشست محلی).
     */
    suspend fun refresh(baseUrl: String, userPreferencesManager: UserPreferencesManager): String? {
        // AuthSession یک singleton درون‌حافظه‌ای است که در AtkCargoApplication
        // با یک coroutine جدا (fire-and-forget) از DataStore پر می‌شود. در
        // cold start (دقیقاً همان لحظه‌ای که این تابع بعد از >۳۰ دقیقه
        // بی‌فعالیتی از StartupViewModel صدا زده می‌شود)، ممکن است این
        // coroutine هنوز کامل نشده باشد و AuthSession.username/deviceId هنوز
        // "" باشند — حتی با یک refreshToken کاملاً معتبر در DataStore. با
        // خواندن مستقیم از userPreferencesManager (همان منبع پایدار که
        // refreshToken هم از آن خوانده می‌شود)، این race حذف می‌شود.
        val username = userPreferencesManager.username.first()
        val deviceId = userPreferencesManager.deviceId.first()
        val refreshToken = userPreferencesManager.refreshToken.first()

        if (username.isEmpty() || deviceId.isEmpty() || refreshToken.isEmpty()) {
            return null
        }

        return try {
            val formBody = FormBody.Builder()
                .add("username", username)
                .add("deviceId", deviceId)
                .add("refreshToken", refreshToken)
                .build()

            // مسیر تمیز /api/v2/auth/refresh روی این هاست کار نمی‌کند
            // (mod_rewrite از طریق .htaccess فعال نیست — نگاه کنید به
            // کامنت‌های api/v2/index.php)؛ همان الگوی query-string که آن فایل
            // به‌عنوان راه‌حل اثبات‌شده استفاده می‌کند.
            val url = baseUrl.trimEnd('/') + "/api/v2/index.php?route=auth/refresh"
            val request = Request.Builder().url(url).post(formBody).build()

            httpClient.newCall(request).execute().use { httpResponse ->
                val bodyStr = httpResponse.body?.string()
                if (!httpResponse.isSuccessful || bodyStr.isNullOrEmpty()) {
                    if (httpResponse.code == 401) {
                        // refresh token هم رد شد (منقضی یا نشانه‌ی سرقت که سمت
                        // سرور تمام نشست‌ها را باطل کرده) — نشست محلی هم باید
                        // کاملاً پاک شود تا کاربر واقعاً به صفحه‌ی ورود برود.
                        userPreferencesManager.clearUserCredentials()
                    }
                    return null
                }

                val parsed = Gson().fromJson(bodyStr, RefreshTokenResponse::class.java)
                if (parsed?.success != true || parsed.sessionToken.isNullOrEmpty() || parsed.refreshToken.isNullOrEmpty()) {
                    return null
                }

                userPreferencesManager.saveRefreshedTokens(parsed.sessionToken, parsed.refreshToken)
                parsed.sessionToken
            }
        } catch (e: Exception) {
            Log.w("TokenRefresher", "خطا هنگام تمدید access token", e)
            null
        }
    }
}
