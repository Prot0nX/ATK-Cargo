package com.atk.atk_cargo.api

import android.Manifest
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.content.edit
import com.atk.atk_cargo.R
import com.atk.atk_cargo.api.RetrofitClient.apiService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class LoadingNotificationService : Service() {
    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var notificationManager: AppNotificationManager
    private lateinit var userPreferencesManager: UserPreferencesManager
    
    // تنظیمات فاصله زمانی بین درخواست‌ها (به دقیقه)
    companion object {
        private const val UPDATE_INTERVAL_MINUTES = 5L
        private const val INITIAL_DELAY_SECONDS = 10L
        
        // کلیدهای تنظیمات
        private const val PREFS_NAME = "LoadingNotificationPrefs"
        private const val KEY_LAST_UPDATE_TIME = "last_update_time"
        private const val KEY_ENABLED = "notifications_enabled"
        
        // شناسه سرویس فورگراند
        const val FOREGROUND_ID = 1001
        
        // اکشن‌ها
        const val ACTION_REFRESH = "com.atk.atk_cargo.REFRESH_NOTIFICATIONS"
        const val ACTION_STOP_SERVICE = "com.atk.atk_cargo.STOP_SERVICE"
        
        /**
         * راه‌اندازی سرویس نوتیفیکیشن بارگیری لحظه‌ای
         * فقط برای کاربران با سطح دسترسی admin سرویس را راه‌اندازی می‌کند
         */
        fun startLoadingNotification(context: Context) {
            // سرویس برای همه کاربران شروع می‌شود
            // اما در onStartCommand بررسی می‌شود که فقط برای کاربران admin ادامه پیدا کند
            val intent = Intent(context, LoadingNotificationService::class.java)
            context.startForegroundService(intent)
            Log.d("LoadingNotificationService", "Service requested to start - will check user permissions")
        }
    }
    
    override fun onCreate() {
        super.onCreate()
        notificationManager = AppNotificationManager(this)
        userPreferencesManager = UserPreferencesManager(this)
        
        // ایجاد کانال‌های جدید اعلان
        notificationManager.setupChannels()
    }
    
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // بررسی آیا سرویس باید متوقف شود
        if (intent?.action == ACTION_STOP_SERVICE) {
            stopSelf()
            return START_NOT_STICKY
        }
        
        coroutineScope.launch {
            // بررسی سطح دسترسی کاربر قبل از راه‌اندازی سرویس فورگراند
            val userType = userPreferencesManager.userType.first()
            if (userType == "admin") {
                // راه‌اندازی سرویس به عنوان فورگراند فقط برای کاربران admin
                startForegroundService()
                
                // بررسی اکشن بروزرسانی
                if (intent?.action == ACTION_REFRESH) {
                    // بروزرسانی فوری نوتیفیکیشن‌ها
                    try {
                        fetchAndNotify(true)
                    } catch (_: Exception) {
                        ""
                    }
                } else {
                    // شروع دریافت دوره‌ای اطلاعات
                    startPeriodicFetching()
                }
            } else {
                // کاربر admin نیست، سرویس را متوقف می‌کنیم
                stopSelf()
                return@launch
            }
        }

        return START_STICKY
    }

    /**
     * راه‌اندازی سرویس به عنوان فورگراند در اندروید 8.0 و بالاتر
     */
    private fun startForegroundService() {
        val notification = NotificationCompat.Builder(this, AppNotificationManager.CHANNEL_SERVICE)
            .setContentTitle("سرویس عملیات بارگیری")
            .setContentText("در حال مانیتورینگ هوشمند بارگیری...")
            .setSmallIcon(R.drawable.ic_notification_icon)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
            
        startForeground(FOREGROUND_ID, notification)
    }
    
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
    
    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel()
        notificationManager.clearAll()
    }
    
    /**
     * شروع دریافت دوره‌ای اطلاعات بارگیری
     */
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun startPeriodicFetching() {
        coroutineScope.launch {
            // تاخیر اولیه برای اطمینان از راه‌اندازی کامل برنامه
            delay(TimeUnit.SECONDS.toMillis(INITIAL_DELAY_SECONDS))
            
            while (isActive) {
                try {
                    fetchAndNotify(false)
                } catch (_: Exception) {
                    ""
                }
                
                // انتظار تا زمان دریافت بعدی
                delay(TimeUnit.MINUTES.toMillis(UPDATE_INTERVAL_MINUTES))
            }
        }
    }
    
    /**
     * دریافت اطلاعات بارگیری از سرور و نمایش نوتیفیکیشن
     * @param isRefresh آیا این درخواست برای بروزرسانی دستی است
     */
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private suspend fun fetchAndNotify(isRefresh: Boolean = false) {
        // بررسی سطح دسترسی کاربر
        val userType = userPreferencesManager.userType.first()
        if (userType != "admin") {
            return
        }
        
        // بررسی فعال بودن نوتیفیکیشن‌ها
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        if (!isRefresh && !prefs.getBoolean(KEY_ENABLED, true)) {
            return
        }
        
        // بررسی غیرفعال بودن نوتیفیکیشن‌ها برای شیفت فعلی
        if (!isRefresh && isCurrentShiftDisabled()) {
            return
        }
        
        // دریافت اطلاعات بارگیری
        try {
            val response = apiService.getRealTimeLoadingData()
            
            if (response.isSuccessful) {
                val loadingData = response.body()
                
                if (loadingData != null) {
                    // ذخیره زمان آخرین به‌روزرسانی
                    prefs.edit { putLong(KEY_LAST_UPDATE_TIME, System.currentTimeMillis()) }
                    
                    // ذخیره اطلاعات شیفت فعلی
                    saveCurrentShiftInfo(loadingData.shiftInfo)
                    
                    // ذخیره داده‌ها در کش برای استفاده در نمایش آمار کلی
                    cacheLoadingData(loadingData.data)
                    
                    // نمایش همه کشتی‌ها
                    if (loadingData.data.isNotEmpty()) {
                        // دریافت لیست کشتی‌های مسدود شده
                        val mutedShips = getSharedPreferences("ship_notifications_prefs", MODE_PRIVATE)
                            .getStringSet("muted_ships", emptySet()) ?: emptySet()

                        // نمایش نوتیفیکیشن از طریق مدیریت مرکزی
                        notificationManager.notifyLoadingData(loadingData.data, mutedShips)
                    }
                }
            } else {
                ""
            }
        } catch (e: Exception) {
            throw e
        }
    }
    
    /**
     * ذخیره اطلاعات شیفت فعلی
     */
    private fun saveCurrentShiftInfo(shiftInfo: ShiftInfo) {
        // ایجاد یک شناسه یکتا برای هر شیفت
        val shiftId = "${shiftInfo.type}_${shiftInfo.startDate}"
        
        val prefs = getSharedPreferences("ShiftNotificationsPrefs", MODE_PRIVATE)
        val previousShiftId = prefs.getString("current_shift_id", "") ?: ""
        
        // اگر شیفت تغییر کرده باشد، پرچم غیرفعال‌سازی شیفت قبلی را پاک می‌کنیم
        if (previousShiftId.isNotEmpty() && previousShiftId != shiftId) {

            // پرچم غیرفعال‌سازی برای شیفت قبلی را پاک می‌کنیم تا شیفت جدید فعال باشد
            prefs.edit {
                remove("disabled_$previousShiftId")
                putString("current_shift_id", shiftId)
            }
        } else {
            // شیفت تغییر نکرده، فقط ذخیره می‌کنیم
            prefs.edit { putString("current_shift_id", shiftId) }
        }
    }
    
    /**
     * بررسی غیرفعال بودن نوتیفیکیشن‌ها برای شیفت فعلی
     */
    private fun isCurrentShiftDisabled(): Boolean {
        val prefs = getSharedPreferences("ShiftNotificationsPrefs", MODE_PRIVATE)
        val currentShiftId = prefs.getString("current_shift_id", "") ?: ""
        
        if (currentShiftId.isEmpty()) {
            return false
        }
        
        val isDisabled = prefs.getBoolean("disabled_$currentShiftId", false)
        
        return isDisabled
    }

    /**
     * ذخیره داده‌های بارگیری در کش
     */
    private fun cacheLoadingData(data: List<RealTimeLoadingData>) {
        try {
            val gson = com.google.gson.Gson()
            val jsonData = gson.toJson(data)
            
            // ذخیره داده‌ها در SharedPreferences
            getSharedPreferences("LoadingDataCache", MODE_PRIVATE)
                .edit {
                    putString("cached_data", jsonData)
                }
        } catch (_: Exception) {
            ""
        }
    }
} 