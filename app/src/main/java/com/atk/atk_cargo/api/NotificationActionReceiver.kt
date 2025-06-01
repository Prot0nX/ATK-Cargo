package com.atk.atk_cargo.api

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.Toast

/**
 * BroadcastReceiver برای مدیریت اکشن‌های نوتیفیکیشن‌های بارگیری لحظه‌ای
 * این کلاس دستورات مربوط به بی‌صدا کردن نوتیفیکیشن‌های مربوط به کشتی‌های خاص را مدیریت می‌کند
 */
class NotificationActionReceiver : BroadcastReceiver() {
    
    companion object {
        private const val TAG = "NotificationAction"
        
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
        val TAG = "NotificationActionReceiver"
        Log.d(TAG, "Received action: ${intent.action}")
        
        when (intent.action) {
            // غیرفعال کردن نوتیفیکیشن یک کشتی
            ACTION_MUTE_SHIP -> {
                val shipName = intent.getStringExtra(EXTRA_SHIP_NAME)
                if (shipName != null) {
                    val notificationManager = LoadingNotificationManager(context)
                    notificationManager.muteShip(shipName)
                }
            }
            
            // فعال کردن مجدد نوتیفیکیشن یک کشتی
            ACTION_UNMUTE_SHIP -> {
                val shipName = intent.getStringExtra(EXTRA_SHIP_NAME)
                if (shipName != null) {
                    val notificationManager = LoadingNotificationManager(context)
                    notificationManager.unmuteShip(shipName)
                    
                    // بروزرسانی نوتیفیکیشن‌ها بعد از فعال کردن مجدد
                    val serviceIntent = Intent(context, LoadingNotificationService::class.java).apply {
                        action = LoadingNotificationService.ACTION_REFRESH
                    }
                    
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(serviceIntent)
                    } else {
                        context.startService(serviceIntent)
                    }
                }
            }
            
            // پاک کردن لیست کشتی‌های غیرفعال شده
            ACTION_CLEAR_MUTED_SHIPS -> {
                val notificationManager = LoadingNotificationManager(context)
                notificationManager.clearMutedShips()
                
                // بروزرسانی نوتیفیکیشن‌ها بعد از پاک کردن لیست
                val serviceIntent = Intent(context, LoadingNotificationService::class.java).apply {
                    action = LoadingNotificationService.ACTION_REFRESH
                }
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent)
                } else {
                    context.startService(serviceIntent)
                }
            }
            
            // غیرفعال کردن نوتیفیکیشن‌ها برای شیفت فعلی
            ACTION_DISABLE_SHIFT_NOTIFICATIONS -> {
                val prefs = context.getSharedPreferences("ShiftNotificationsPrefs", Context.MODE_PRIVATE)
                val shiftInfo = prefs.getString("current_shift_id", "") ?: ""
                
                if (shiftInfo.isNotEmpty()) {
                    prefs.edit().putBoolean("disabled_$shiftInfo", true).apply()
                    Log.d(TAG, "Disabled notifications for shift: $shiftInfo")
                    
                    // نمایش پیام برای اطلاع‌رسانی به کاربر
                    val shiftType = if (shiftInfo.contains("day")) "روز" else if (shiftInfo.contains("night")) "شب" else "فعلی"
                    Toast.makeText(
                        context, 
                        "اعلان‌های بارگیری برای شیفت $shiftType غیرفعال شدند. در شیفت بعدی دوباره فعال خواهند شد.", 
                        Toast.LENGTH_LONG
                    ).show()
                }
                
                // پاک کردن نوتیفیکیشن‌های فعلی
                val notificationManager = LoadingNotificationManager(context)
                notificationManager.clearNotifications()
            }
            
            // بروزرسانی نوتیفیکیشن‌ها
            ACTION_REFRESH_NOTIFICATIONS -> {
                val serviceIntent = Intent(context, LoadingNotificationService::class.java).apply {
                    action = LoadingNotificationService.ACTION_REFRESH
                }
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent)
                } else {
                    context.startService(serviceIntent)
                }
            }
        }
    }
} 