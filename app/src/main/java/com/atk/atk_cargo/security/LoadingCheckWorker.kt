package com.atk.atk_cargo.security

import android.content.Context
import android.content.Intent
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.atk.atk_cargo.api.LoadingCheckService
import java.util.concurrent.TimeUnit

class LoadingCheckWorker(
    private val context: Context,
    workerParameters: WorkerParameters
) : CoroutineWorker(context, workerParameters) {

    override suspend fun doWork(): Result {
        try {
            // راه‌اندازی مجدد سرویس اگر متوقف شده باشد
            val serviceIntent = Intent(context, LoadingCheckService::class.java)
            context.startForegroundService(serviceIntent)
            return Result.success()
        } catch (e: Exception) {
            return Result.retry()
        }
    }

    companion object {
        fun startPeriodicWorker(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val workRequest = PeriodicWorkRequestBuilder<LoadingCheckWorker>(
                15, TimeUnit.MINUTES,
                5, TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .addTag("loading_check_worker")
                .build()

            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    "LoadingCheckWorker",
                    ExistingPeriodicWorkPolicy.KEEP,
                    workRequest
                )
        }
    }
}