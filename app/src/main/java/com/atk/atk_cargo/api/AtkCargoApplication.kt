package com.atk.atk_cargo.api

import android.app.Application
import android.provider.Settings
import com.atk.atk_cargo.BuildConfig
import com.atk.atk_cargo.core.domain.AnimationManager
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

        // باید همین ابتدا نصب شود، قبل از هر initialization دیگری که ممکن است کرش کند (Koin، RetrofitClient) تا آن کرش‌ها هم گزارش شوند (DEEP_CODE_AUDIT.md #Phase2.13)
        CrashReporter.install(this)

        // تنظیم Reduce Motion سیستم قبل از اولین composition خوانده می‌شود تا هیچ Composableای مقدار پیش‌فرض را نبیند (DEEP_CODE_AUDIT.md #۱۴)
        val animatorDurationScale = Settings.Global.getFloat(contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f)
        AnimationManager.setSystemAnimationsEnabled(animatorDurationScale != 0f)

        // Initialize Koin DI
        val koinApp = startKoin {
            // لاگ verbose فقط در build های debug — در release نباید فعال باشد
            if (BuildConfig.DEBUG) {
                androidLogger()
            }
            androidContext(this@AtkCargoApplication)
            modules(appModule)
        }

        // WorkManager از قبل توسط InitializationProvider مقداردهی می‌شود؛ فراخوانی دستی initialize() اینجا همیشه IllegalStateException می‌داد

        // AuthSession درون‌حافظه‌ای است و با کشته‌شدن پروسه خالی می‌شود؛ اینجا از DataStore پر می‌شود تا هدرهای احراز هویت از اولین درخواست درست ارسال شوند
        val userPreferencesManager = koinApp.koin.get<UserPreferencesManager>()

        // Secrets.isAvailable روی ABI پشتیبانی‌نشده false است؛ لمس RetrofitClient (حتی فقط init())
        // کل initializer شیء را اجرا می‌کند و BASE_URL = Secrets.getBaseUrl() آنجا بی‌قید‌وشرط
        // فراخوانی می‌شد — یعنی یک UnsatisfiedLinkError غیرقابل‌بازیابی. حالا StartupViewModel
        // این پرچم را می‌بیند و صفحه‌ی خطای صریح نشان می‌دهد (DEEP_CODE_AUDIT.md #۱۶)
        if (Secrets.isAvailable) {
            // باید قبل از اولین دسترسی lazy به RetrofitClient.apiService فراخوانی شود تا کش HTTP دیسک فعال شود؛ debugLogging از اینجا تزریق می‌شود چون core:network به BuildConfig ماژول app دسترسی ندارد (DEEP_CODE_AUDIT.md #Phase4.2)
            RetrofitClient.init(this, userPreferencesManager, debugLogging = BuildConfig.DEBUG)
        }

        applicationScope.launch {
            try {
                AuthSession.username = userPreferencesManager.username.first()
                AuthSession.deviceId = userPreferencesManager.deviceId.first()
                AuthSession.sessionToken = userPreferencesManager.sessionToken.first()
            } finally {
                // finally تضمین می‌کند حتی با خطای غیرمنتظره در خواندن DataStore، headersInterceptor برای همیشه مسدود نماند (DEEP_CODE_AUDIT.md #۱۵)
                AuthSession.markReady()
            }

            if (Secrets.isAvailable) {
                // ارسال best-effort گزارش کرشِ اجرای قبلی، پس از اینکه AuthSession.username در دسترس است
                CrashReporter.sendPendingReportIfAny(this@AtkCargoApplication, Secrets.getBaseUrl(), applicationScope)
            }
        }
    }
}