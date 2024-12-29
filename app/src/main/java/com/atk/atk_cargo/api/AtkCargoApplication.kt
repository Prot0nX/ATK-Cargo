package com.atk.atk_cargo.api

import android.app.Application
import android.util.Log
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class AtkCargoApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)

        try {
            // Initialize WorkManager
            val config = Configuration.Builder()
                .setMinimumLoggingLevel(Log.INFO)
                .build()
            WorkManager.initialize(this, config)

            // Schedule the RealTimeLoadingWorker
            scheduleRealTimeLoadingWorker()
        } catch (e: Exception) {
            Log.e("AtkCargoApplication", "Error initializing WorkManager: ${e.message}")
        }
    }

    private fun scheduleRealTimeLoadingWorker() {
        try {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val workRequest = PeriodicWorkRequestBuilder<RealTimeLoadingWorker>(30, TimeUnit.SECONDS)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "RealTimeLoadingWorker",
                ExistingPeriodicWorkPolicy.UPDATE,
                workRequest
            )
            Log.d("AtkCargoApplication", "RealTimeLoadingWorker scheduled successfully")
        } catch (e: Exception) {
            Log.e("AtkCargoApplication", "Error scheduling RealTimeLoadingWorker: ${e.message}")
        }
    }
}