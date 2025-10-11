package com.atk.atk_cargo.api

import android.app.Application
import android.util.Log
import androidx.work.Configuration
import androidx.work.WorkManager

class AtkCargoApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppContainer()

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