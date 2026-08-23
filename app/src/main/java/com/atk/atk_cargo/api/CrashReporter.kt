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

// گزارش کرش خودمیزبان بدون سرویس ثالث: [install] هندلر uncaught-exception سراسری نصب می‌کند که فایل ساده (نه DataStore، چون coroutine ممکن است قبل از kill شدن process کامل نشود) می‌نویسد و [sendPendingReportIfAny] در اجرای بعدی آن را best-effort به سرور می‌فرستد و پاک می‌کند
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
 // زنجیره به handler قبلی سیستم — این کلاس فقط ثبت می‌کند و رفتار طبیعی کرش را دست‌نخورده می‌گذارد
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
 // best-effort — بدون retry، تا کرش تکرارشونده باعث ارسال بی‌نهایت نشود
                file.delete()
            }
        }
    }
}
