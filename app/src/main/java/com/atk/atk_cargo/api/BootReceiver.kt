package com.atk.atk_cargo.api

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.atk.atk_cargo.workers.LoadingNotificationWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.concurrent.TimeUnit

class BootReceiver : BroadcastReceiver(), KoinComponent {
    private val userPreferencesManager: UserPreferencesManager by inject()

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val userType = userPreferencesManager.userType.first()

                    // فقط برای کاربران با نقش مدیر polling دوره‌ای را زمان‌بندی می‌کنیم
                    if (userType == "admin") {
                        val workRequest = PeriodicWorkRequestBuilder<LoadingNotificationWorker>(15, TimeUnit.MINUTES)
                            .build()
                        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                            LoadingNotificationWorker.UNIQUE_WORK_NAME,
                            ExistingPeriodicWorkPolicy.KEEP,
                            workRequest
                        )
                    } else {
                        WorkManager.getInstance(context).cancelUniqueWork(LoadingNotificationWorker.UNIQUE_WORK_NAME)
                    }
                } catch (_: Exception) {
                    ""
                }
            }
        }
    }
} 