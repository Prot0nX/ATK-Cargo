package com.atk.atk_cargo.api

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.edit
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.atk.atk_cargo.workers.LoadingNotificationWorker

// BroadcastReceiver برای مدیریت اکشن‌های نوتیفیکیشن‌های بارگیری لحظه‌ای، از جمله بی‌صدا کردن کشتی‌های خاص
class NotificationActionReceiver : BroadcastReceiver() {
    
    companion object {

        // اکشن‌ها
        const val ACTION_MUTE_SHIP = "com.atk.atk_cargo.MUTE_SHIP"
        const val ACTION_UNMUTE_SHIP = "com.atk.atk_cargo.UNMUTE_SHIP"
        const val ACTION_CLEAR_MUTED_SHIPS = "com.atk.atk_cargo.CLEAR_MUTED_SHIPS"
        const val ACTION_DISABLE_SHIFT_NOTIFICATIONS = "com.atk.atk_cargo.DISABLE_SHIFT_NOTIFICATIONS"
        const val ACTION_REFRESH_NOTIFICATIONS = "com.atk.atk_cargo.REFRESH_NOTIFICATIONS"
        
        // پارامترها
        const val EXTRA_SHIP_NAME = "ship_name"
    }
    
    override fun onReceive(context: Context, intent: Intent) {

        when (intent.action) {
            // غیرفعال کردن نوتیفیکیشن یک کشتی
            ACTION_MUTE_SHIP -> {
                val shipName = intent.getStringExtra(EXTRA_SHIP_NAME)
                if (shipName != null) {
                    val notificationManager = AppNotificationManager(context)
                    notificationManager.muteShip(shipName)
                }
            }
            
            // فعال کردن مجدد نوتیفیکیشن یک کشتی
            ACTION_UNMUTE_SHIP -> {
                val shipName = intent.getStringExtra(EXTRA_SHIP_NAME)
                if (shipName != null) {
                    val notificationManager = AppNotificationManager(context)
                    notificationManager.unmuteShip(shipName)
                    enqueueImmediateLoadingRefresh(context)
                }
            }

            // پاک کردن لیست کشتی‌های غیرفعال شده
            ACTION_CLEAR_MUTED_SHIPS -> {
                val notificationManager = AppNotificationManager(context)
                notificationManager.clearMutedShips()
                enqueueImmediateLoadingRefresh(context)
            }
            
            // غیرفعال کردن نوتیفیکیشن‌ها برای شیفت فعلی
            ACTION_DISABLE_SHIFT_NOTIFICATIONS -> {
                val prefs = context.getSharedPreferences("ShiftNotificationsPrefs", Context.MODE_PRIVATE)
                val shiftInfo = prefs.getString("current_shift_id", "") ?: ""
                
                if (shiftInfo.isNotEmpty()) {
                    prefs.edit { putBoolean("disabled_$shiftInfo", true) }

                    // نمایش پیام برای اطلاع‌رسانی به کاربر
                    val shiftType = if (shiftInfo.contains("day")) "روز" else if (shiftInfo.contains("night")) "شب" else "فعلی"
                    Toast.makeText(
                        context, 
                        "اعلان‌های بارگیری برای شیفت $shiftType غیرفعال شدند. در شیفت بعدی دوباره فعال خواهند شد.", 
                        Toast.LENGTH_LONG
                    ).show()
                }
                
                // پاک کردن نوتیفیکیشن‌های فعلی
                val notificationManager = AppNotificationManager(context)
                notificationManager.clearAll()
            }
            
            // بروزرسانی نوتیفیکیشن‌ها
            ACTION_REFRESH_NOTIFICATIONS -> {
                enqueueImmediateLoadingRefresh(context)
            }
        }
    }

    // اجرای یک‌بار فوری LoadingNotificationWorker به‌جای منتظر ماندن برای چرخه‌ی دوره‌ای بعدی
    private fun enqueueImmediateLoadingRefresh(context: Context) {
        val workRequest = OneTimeWorkRequestBuilder<LoadingNotificationWorker>()
            .setInputData(workDataOf(LoadingNotificationWorker.KEY_IS_REFRESH to true))
            .build()
        WorkManager.getInstance(context).enqueue(workRequest)
    }
} 