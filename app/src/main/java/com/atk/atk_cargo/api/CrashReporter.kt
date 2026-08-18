package com.atk.atk_cargo.api

import android.content.Context
import android.os.Build
import android.util.Log
import com.atk.atk_cargo.BuildConfig
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.util.concurrent.TimeUnit

private data class PendingCrashReport(
    val stackTrace: String,
    val appVersion: String,
    val deviceModel: String,
    val androidVersion: String,
    val username: String = ""
)

/**
 * گزارش کرش خودمیزبان و سبک — بدون سرویس شخص ثالث (Crashlytics/Sentry)،
 * چون `mapping.txt` هر release از قبل آرشیو می‌شود و برای deobfuscate کافی
 * است (DEEP_CODE_AUDIT.md #Phase2.13).
 *
 * الگو: [install] یک uncaught-exception-handler سراسری نصب می‌کند که فقط
 * یک فایل ساده (نه DataStore) روی همان thread کرش‌کننده می‌نویسد — یک
 * suspend/coroutine write ممکن است هرگز کامل نشود چون process بلافاصله
 * بعد از کرش kill می‌شود. در اجرای بعدی، [sendPendingReportIfAny] فایل را
 * (در صورت وجود) به سرور می‌فرستد و صرف‌نظر از نتیجه پاک می‌کند
 * (best-effort، بدون retry loop).
 */
object CrashReporter {
    private const val TAG = "CrashReporter"
    private const val CRASH_FILE_NAME = "pending_crash_report.json"
    private val gson = Gson()

    fun install(context: Context) {
        val appContext = context.applicationContext
        val previousHandler = Thread.getDefaultUncaughtExceptionHandler()

        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                writeCrashFile(appContext, throwable)
            } catch (_: Throwable) {
                // هیچ استثنایی نباید از خودِ handler خارج شود
            }
            // زنجیره به handler قبلی (سیستم/Android) — این کلاس فقط ثبت
            // می‌کند، هرگز کرش را نمی‌بلعد؛ رفتار طبیعی کرش (دیالوگ سیستم،
            // بستن اپ) باید دست‌نخورده بماند.
            previousHandler?.uncaughtException(thread, throwable)
        }
    }

    private fun writeCrashFile(context: Context, throwable: Throwable) {
        val sw = StringWriter()
        throwable.printStackTrace(PrintWriter(sw))
        val report = PendingCrashReport(
            stackTrace = sw.toString(),
            appVersion = BuildConfig.VERSION_NAME,
            deviceModel = "${Build.MANUFACTURER} ${Build.MODEL}",
            androidVersion = Build.VERSION.RELEASE ?: "",
            username = AuthSession.username
        )
        File(context.filesDir, CRASH_FILE_NAME).writeText(gson.toJson(report))
    }

    fun sendPendingReportIfAny(context: Context, baseUrl: String, scope: CoroutineScope) {
        val file = File(context.applicationContext.filesDir, CRASH_FILE_NAME)
        if (!file.exists()) return

        scope.launch {
            try {
                val json = file.readText()
                val client = OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(10, TimeUnit.SECONDS)
                    .build()
                val url = baseUrl.trimEnd('/') + "/api/v2/index.php?route=diagnostics/crash"
                val body = json.toRequestBody("application/json; charset=utf-8".toMediaType())
                val request = Request.Builder().url(url).post(body).build()
                client.newCall(request).execute().close()
            } catch (e: Exception) {
                Log.w(TAG, "ارسال گزارش کرش معلق شکست خورد", e)
            } finally {
                // best-effort — چه موفق چه ناموفق، دوباره retry نمی‌کنیم تا
                // یک کرش تکرارشونده باعث ارسال بی‌نهایت نشود.
                file.delete()
            }
        }
    }
}
