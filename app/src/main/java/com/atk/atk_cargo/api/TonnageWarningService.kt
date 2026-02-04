package com.atk.atk_cargo.api

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * سرویس پس‌زمینه برای بررسی دوره‌ای هشدارهای تناژ کوتاژ
 * این سرویس هر 120 ثانیه یکبار هشدارها را بررسی می‌کند
 */
class TonnageWarningService : Service() {
    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var userPreferencesManager: UserPreferencesManager
    private lateinit var notificationManager: AppNotificationManager
    private var lastWarningCount = 0
    
    companion object {
        private const val CHECK_INTERVAL_SECONDS = 120L
        private const val TAG = "TonnageWarningService"
        
        // StateFlow برای اشتراک‌گذاری تعداد هشدارها
        private val _warningsCount = MutableStateFlow(0)
        val warningsCount: StateFlow<Int> = _warningsCount.asStateFlow()
        
        /**
         * راه‌اندازی سرویس بررسی هشدارهای تناژ
         */
        fun startService(context: Context) {
            val intent = Intent(context, TonnageWarningService::class.java)
            context.startService(intent)
        }
    }
    
    override fun onCreate() {
        super.onCreate()
        userPreferencesManager = UserPreferencesManager(this)
        notificationManager = AppNotificationManager(this)
        notificationManager.setupChannels()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        coroutineScope.launch {
            // بررسی سطح دسترسی کاربر
            val userType = userPreferencesManager.userType.first()
            if (userType == "admin") {
                startPeriodicCheck()
            } else {
                stopSelf()
            }
        }
        
        return START_STICKY
    }
    
    /**
     * شروع بررسی دوره‌ای هشدارها
     */
    private suspend fun startPeriodicCheck() {

        while (coroutineScope.isActive) {
            try {
                checkTonnageWarnings()
                delay(CHECK_INTERVAL_SECONDS * 1000)
            } catch (_: Exception) {
                delay(CHECK_INTERVAL_SECONDS * 1000)
            }
        }
    }
    
    /**
     * بررسی هشدارهای تناژ
     */
    private suspend fun checkTonnageWarnings() {
        try {
            val response = RetrofitClient.apiService.getActiveQuotaReport()
            if (response.isSuccessful) {
                response.body()?.use { responseBody ->
                    val body = responseBody.string()
                    if (body.isNotEmpty()) {
                        val parsedWarnings = parseQuotaTonnageData(body)
                        val count = parsedWarnings.size
                        
                        // به‌روزرسانی StateFlow
                        _warningsCount.value = count
                        
                        // ارسال نوتیفیکیشن در صورت وجود هشدار جدید
                        if (count > 0 && count != lastWarningCount) {
                            val firstWarning = parsedWarnings.firstOrNull()
                            val title = "⚠️ هشدار تناژ کوتاژ"
                            val text = if (count == 1 && firstWarning != null) {
                                "کشتی ${firstWarning.shipName} - کوتاژ ${firstWarning.quotaNumber}"
                            } else {
                                "$count مورد هشدار جدید در تناژ کوتاژها"
                            }
                            notificationManager.showSystemAlert(title, text)
                            lastWarningCount = count
                        }
                    } else {
                        _warningsCount.value = 0
                        lastWarningCount = 0
                    }
                }
            } else {
                Log.e(TAG, "خطا در دریافت داده (کد: ${response.code()})")
            }
        } catch (e: Exception) {
            Log.e(TAG, "خطا در بررسی هشدارها: ${e.message}")
        }
    }
    
    /**
     * پارس کردن داده‌های تناژ کوتاژ
     */
    private fun parseQuotaTonnageData(rawData: String): List<QuotaTonnageWarning> {
        val warnings = mutableListOf<QuotaTonnageWarning>()
        
        try {
            // تقسیم به بلوک‌های جداگانه
            val blocks = rawData.split("━━━━━━━━━━━━━━━━")
            
            for (block in blocks) {
                if (block.contains("کشتی") && block.contains("کوتاژ")) {
                    try {
                        val shipName = block.substringAfter("کشتی *").substringBefore("*").trim()
                        val cargoOwner = block.substringAfter("👤 ").substringBefore("\n").trim()
                        val quotaNumber = block.substringAfter("کوتاژ: ").substringBefore("\n").trim()
                        val currentRemaining = block.substringAfter("مانده فعلی: ").substringBefore(" کیلوگرم").trim()
                        val voucherCount = block.substringAfter("حواله‌های ورود شده: ").substringBefore(" عدد").trim()
                        val remainingAfterExit = block.substringAfter("مانده بعداز خروج: ").substringBefore(" کیلوگرم").trim()
                        
                        val isNegative = remainingAfterExit.contains("−") || remainingAfterExit.contains("-")
                        
                        warnings.add(
                            QuotaTonnageWarning(
                                shipName = shipName,
                                cargoOwner = cargoOwner,
                                quotaNumber = quotaNumber,
                                currentRemaining = currentRemaining,
                                voucherCount = voucherCount,
                                remainingAfterExit = remainingAfterExit,
                                isNegative = isNegative
                            )
                        )
                    } catch (_: Exception) {
                        // اگر پارس ناموفق بود، این بلوک را رد می‌کنیم
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "خطا در پارس کردن داده: ${e.message}")
        }
        
        return warnings
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel()
        
        // حذف تمام نوتیفیکیشن‌های هشدار هنگام توقف سرویس
        notificationManager.clearAll()
        Log.d(TAG, "سرویس بررسی هشدارهای تناژ کوتاژ متوقف شد")
    }
}
