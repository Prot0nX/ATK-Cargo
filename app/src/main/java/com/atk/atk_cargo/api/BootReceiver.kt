package com.atk.atk_cargo.api

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * BroadcastReceiver برای راه‌اندازی سرویس نوتیفیکیشن بارگیری لحظه‌ای هنگام روشن شدن دستگاه
 */
class BootReceiver : BroadcastReceiver() {
    private val TAG = "BootReceiver"
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d(TAG, "Boot completed, starting service")
            
            // بررسی سطح دسترسی کاربر قبل از راه‌اندازی سرویس
            val userPreferencesManager = UserPreferencesManager(context.applicationContext)
            
            CoroutineScope(Dispatchers.IO).launch {
                                    try {
                    val userType = userPreferencesManager.userType.first()
                    
                    // فقط برای کاربران با نقش مدیر سرویس را راه‌اندازی می‌کنیم
                    if (userType == "admin") {
                        Log.d(TAG, "User is admin, starting loading notification service")
                        // سرویس شروع می‌شود و در onStartCommand سطح دسترسی مجدداً بررسی می‌شود
                        LoadingNotificationService.startLoadingNotification(context)
                    } else {
                        Log.d(TAG, "User is not admin, skipping service start")
                        // برای اطمینان اگر سرویس در حال اجراست متوقف شود
                        val stopIntent = Intent(context, LoadingNotificationService::class.java)
                        stopIntent.action = "STOP_SERVICE"
                        context.startService(stopIntent)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error starting service on boot: ${e.message}", e)
                }
            }
        }
    }
    
    companion object {
        private const val TAG = "BootReceiver"
    }
} 