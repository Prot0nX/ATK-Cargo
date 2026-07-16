package com.atk.atk_cargo.api

import android.app.Application
import android.util.Log
import androidx.work.Configuration
import androidx.work.WorkManager
import com.atk.atk_cargo.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class AtkCargoApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize Koin DI
        startKoin {
            androidLogger()
            androidContext(this@AtkCargoApplication)
            modules(appModule)
        }

        try {
            val config = Configuration.Builder()
                .setMinimumLoggingLevel(Log.INFO)
                .build()
            WorkManager.initialize(this, config)
        } catch (e: Exception) {
            Log.e("AtkCargoApplication", "Error initializing WorkManager: ${e.message}")
        }
    }
}