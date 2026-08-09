package com.atk.atk_cargo.api

import android.app.Application
import com.atk.atk_cargo.BuildConfig
import com.atk.atk_cargo.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class AtkCargoApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize Koin DI
        startKoin {
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
    }
}