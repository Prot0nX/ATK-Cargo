package com.atk.atk_cargo.api

import android.app.Application
import com.atk.atk_cargo.BuildConfig
import com.atk.atk_cargo.di.appModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class AtkCargoApplication : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()

        // باید همین ابتدا نصب شود — قبل از هر initialization دیگری که خودش
        // می‌تواند کرش کند (Koin، RetrofitClient) — تا آن کرش‌ها هم گزارش شوند
        // (DEEP_CODE_AUDIT.md #Phase2.13).
        CrashReporter.install(this)

        // Initialize Koin DI
        val koinApp = startKoin {
            // لاگ verbose فقط در build های debug — در release نباید فعال باشد
            if (BuildConfig.DEBUG) {
                androidLogger()
            }
            androidContext(this@AtkCargoApplication)
            modules(appModule)
        }

        // WorkManager توسط androidx.startup.InitializationProvider به‌صورت خودکار
        // و پیش از این نقطه مقداردهی می‌شود؛ فراخوانی دستی WorkManager.initialize()
        // اینجا همیشه IllegalStateException می‌داد (بی‌صدا catch می‌شد) و Configuration
        // سفارشی هرگز اعمال نمی‌شد

        // باید قبل از اولین دسترسی به RetrofitClient.apiService (که Koin به‌صورت
        // lazy در اولین get() می‌سازد) فراخوانی شود تا کش HTTP دیسک فعال شود.
        RetrofitClient.init(this)

        // AuthSession یک نگه‌دارنده‌ی درون‌حافظه است و با هر بار کشته‌شدن پروسه خالی
        // می‌شود؛ اینجا از مقادیر ذخیره‌شده در DataStore (کاربری که قبلاً لاگین کرده)
        // پر می‌شود تا هدرهای احراز هویت از همان اولین درخواست بعد از باز شدن اپ درست
        // ارسال شوند.
        val userPreferencesManager = koinApp.koin.get<UserPreferencesManager>()
        applicationScope.launch {
            AuthSession.username = userPreferencesManager.username.first()
            AuthSession.deviceId = userPreferencesManager.deviceId.first()
            AuthSession.sessionToken = userPreferencesManager.sessionToken.first()

            // ارسال گزارش کرشِ اجرای قبلی (در صورت وجود) — best-effort، بعد
            // از این‌که AuthSession.username برای مرجع در دسترس است.
            CrashReporter.sendPendingReportIfAny(this@AtkCargoApplication, Secrets.getBaseUrl(), applicationScope)
        }
    }
}