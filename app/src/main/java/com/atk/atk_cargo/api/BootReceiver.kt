package com.atk.atk_cargo.api

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {

            // بررسی سطح دسترسی کاربر قبل از راه‌اندازی سرویس
            val userPreferencesManager = UserPreferencesManager(context.applicationContext)
            
            CoroutineScope(Dispatchers.IO).launch {
                                    try {
                    val userType = userPreferencesManager.userType.first()
                    
                    // فقط برای کاربران با نقش مدیر سرویس را راه‌اندازی می‌کنیم
                    if (userType == "admin") {
                        // سرویس شروع می‌شود و در onStartCommand سطح دسترسی مجدداً بررسی می‌شود
                        LoadingNotificationService.startLoadingNotification(context)
                    } else {
                        // برای اطمینان اگر سرویس در حال اجراست متوقف شود
                        val stopIntent = Intent(context, LoadingNotificationService::class.java)
                        stopIntent.action = "STOP_SERVICE"
                        context.startService(stopIntent)
                    }
                } catch (_: Exception) {
                    ""
                }
            }
        }
    }
} 