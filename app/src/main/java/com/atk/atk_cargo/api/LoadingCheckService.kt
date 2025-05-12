package com.atk.atk_cargo.api

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.os.bundleOf
import com.atk.atk_cargo.MainActivity
import com.atk.atk_cargo.R
import com.atk.atk_cargo.security.LoadingCheckWorker
import com.atk.atk_cargo.security.ServiceRestartReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

class LoadingCheckService : Service() {
    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var userPreferencesManager: UserPreferencesManager
    private lateinit var notificationManager: NotificationManager
    private val apiService = RetrofitClient.apiService
    private var isAdminUser = false
    private lateinit var mainServiceJob: Job

    private val powerManager by lazy {
        getSystemService(Context.POWER_SERVICE) as PowerManager
    }

    private val wakeLock by lazy {
        powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK or
                    PowerManager.ON_AFTER_RELEASE,
            "LoadingCheck::StrongWakeLock"
        ).apply {
            setReferenceCounted(false)
        }
    }

    companion object {
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "LoadingCheckChannel"
        private const val FULL_SCREEN_CHANNEL_ID = "FullScreenAlertChannel"
        private const val CHECK_INTERVAL_SECONDS = 30L
        private const val PREFS_NAME = "LoadingCheckPrefs"
        private const val LAST_SHIFT_KEY = "LastShiftKey"
        private const val MESSAGE_CHANNEL_ID = "MessageChannel"
        private const val DISPLAYED_MESSAGES_KEY = "DisplayedMessages"
        private const val LAST_MESSAGE_CHECK_TIME_KEY = "LastMessageCheckTime"
        private const val REAL_TIME_CHANNEL_ID = "RealTimeLoadingChannel"
        private const val REAL_TIME_NOTIFICATION_ID = 2
        private const val ACTION_DO_NOT_SHOW = "com.atk.atk_cargo.ACTION_DO_NOT_SHOW"
        private const val DO_NOT_SHOW_PREFIX = "do_not_show_"
    }

    private val doNotShowReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == ACTION_DO_NOT_SHOW) {
                val loadingQuotaNumber = intent.getStringExtra("loadingQuotaNumber")
                loadingQuotaNumber?.let {
                    markNotificationAsDoNotShow(it)
                    notificationManager.cancel(REAL_TIME_NOTIFICATION_ID + it.hashCode())
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate() {
        try {
            Log.d("ATKLog", "LoadingCheckService: onCreate شروع شد")
            super.onCreate()
            
            try {
                Log.d("ATKLog", "LoadingCheckService: درخواست wakeLock")
                acquireWakeLock()
                Log.d("ATKLog", "LoadingCheckService: wakeLock با موفقیت دریافت شد")
            } catch (e: Exception) {
                Log.e("ATKLog", "LoadingCheckService: خطا در دریافت wakeLock", e)
            }
            
            try {
                Log.d("ATKLog", "LoadingCheckService: مقداردهی اولیه متغیرها")
                userPreferencesManager = UserPreferencesManager(applicationContext)
                notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                Log.d("ATKLog", "LoadingCheckService: مقداردهی اولیه متغیرها با موفقیت انجام شد")
            } catch (e: Exception) {
                Log.e("ATKLog", "LoadingCheckService: خطا در مقداردهی اولیه متغیرها", e)
            }
            
            try {
                Log.d("ATKLog", "LoadingCheckService: ایجاد کانال‌های نوتیفیکیشن")
                createNotificationChannels()
                createMessageNotificationChannel()
                Log.d("ATKLog", "LoadingCheckService: کانال‌های نوتیفیکیشن با موفقیت ایجاد شدند")
            } catch (e: Exception) {
                Log.e("ATKLog", "LoadingCheckService: خطا در ایجاد کانال‌های نوتیفیکیشن", e)
            }
            
            try {
                Log.d("ATKLog", "LoadingCheckService: ایجاد نوتیفیکیشن فورگراند")
                val notification = createNotification()
                Log.d("ATKLog", "LoadingCheckService: نوتیفیکیشن فورگراند ایجاد شد")
                startForeground(NOTIFICATION_ID, notification)
                Log.d("ATKLog", "LoadingCheckService: سرویس با موفقیت به حالت فورگراند رفت")
            } catch (e: Exception) {
                Log.e("ATKLog", "LoadingCheckService: خطا در شروع فورگراند سرویس", e)
            }

            // تنظیم نوع کاربر و بررسی admin بودن
            try {
                Log.d("ATKLog", "LoadingCheckService: شروع جمع‌آوری اطلاعات نوع کاربر")
                serviceScope.launch {
                    try {
                        userPreferencesManager.userType.collect { userType ->
                            isAdminUser = (userType == "admin")
                            Log.d("ATKLog", "LoadingCheckService: نوع کاربر: $userType، آیا ادمین است: $isAdminUser")
                        }
                    } catch (e: Exception) {
                        Log.e("ATKLog", "LoadingCheckService: خطا در جمع‌آوری اطلاعات نوع کاربر", e)
                    }
                }
                Log.d("ATKLog", "LoadingCheckService: کوروتین جمع‌آوری اطلاعات نوع کاربر با موفقیت شروع شد")
            } catch (e: Exception) {
                Log.e("ATKLog", "LoadingCheckService: خطا در شروع کوروتین جمع‌آوری اطلاعات نوع کاربر", e)
            }
            
            try {
                Log.d("ATKLog", "LoadingCheckService: شروع کار دوره‌ای")
                LoadingCheckWorker.startPeriodicWorker(this)
                Log.d("ATKLog", "LoadingCheckService: کار دوره‌ای با موفقیت شروع شد")
            } catch (e: Exception) {
                Log.e("ATKLog", "LoadingCheckService: خطا در شروع کار دوره‌ای", e)
            }
            
            try {
                Log.d("ATKLog", "LoadingCheckService: ثبت گیرنده برودکست")
                registerReceiver(doNotShowReceiver, IntentFilter(ACTION_DO_NOT_SHOW), RECEIVER_NOT_EXPORTED)
                Log.d("ATKLog", "LoadingCheckService: گیرنده برودکست با موفقیت ثبت شد")
            } catch (e: Exception) {
                Log.e("ATKLog", "LoadingCheckService: خطا در ثبت گیرنده برودکست", e)
            }
            
            Log.d("ATKLog", "LoadingCheckService: onCreate با موفقیت به پایان رسید")
        } catch (e: Exception) {
            Log.e("ATKLog", "LoadingCheckService: خطای کلی در onCreate", e)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try {
            Log.d("ATKLog", "LoadingCheckService: onStartCommand شروع شد")
            
            try {
                Log.d("ATKLog", "LoadingCheckService: شروع مجدد فورگراند سرویس")
                startForeground(NOTIFICATION_ID, createNotification())
                Log.d("ATKLog", "LoadingCheckService: فورگراند سرویس با موفقیت مجدداً شروع شد")
            } catch (e: Exception) {
                Log.e("ATKLog", "LoadingCheckService: خطا در شروع مجدد فورگراند سرویس", e)
            }

            // اگر کوروتین قبلاً شروع شده، آن را لغو نمی‌کنیم
            if (!::mainServiceJob.isInitialized || mainServiceJob.isCancelled) {
                mainServiceJob = serviceScope.launch {
                    Log.d("ATKLog", "LoadingCheckService: شروع حلقه اصلی سرویس")
                    while (true) {
                        try {
                            val userType = userPreferencesManager.userType.first()
                            Log.d("ATKLog", "LoadingCheckService: نوع کاربر دریافت شد: $userType")

                            // فقط برای کاربران admin اطلاعات لحظه‌ای را نمایش می‌دهد
                            if (isAdminUser) {
                                Log.d("ATKLog", "LoadingCheckService: کاربر ادمین است، دریافت اطلاعات لحظه‌ای")
                                try {
                                    fetchRealTimeData()
                                    Log.d("ATKLog", "LoadingCheckService: اطلاعات لحظه‌ای با موفقیت دریافت شد")
                                } catch (e: Exception) {
                                    Log.e("ATKLog", "LoadingCheckService: خطا در دریافت اطلاعات لحظه‌ای", e)
                                }
                            }

                            try {
                                Log.d("ATKLog", "LoadingCheckService: بررسی وضعیت بارگیری")
                                checkLoadingStatus()
                                Log.d("ATKLog", "LoadingCheckService: بررسی وضعیت بارگیری با موفقیت انجام شد")
                            } catch (e: Exception) {
                                Log.e("ATKLog", "LoadingCheckService: خطا در بررسی وضعیت بارگیری", e)
                            }
                            
                            try {
                                Log.d("ATKLog", "LoadingCheckService: بررسی پیام‌های جدید")
                                checkNewMessages(userType)
                                Log.d("ATKLog", "LoadingCheckService: بررسی پیام‌های جدید با موفقیت انجام شد")
                            } catch (e: Exception) {
                                Log.e("ATKLog", "LoadingCheckService: خطا در بررسی پیام‌های جدید", e)
                            }
                            
                            Log.d("ATKLog", "LoadingCheckService: تاخیر ${CHECK_INTERVAL_SECONDS} ثانیه‌ای قبل از چرخه بعدی")
                            delay(TimeUnit.SECONDS.toMillis(CHECK_INTERVAL_SECONDS))
                        } catch (e: Exception) {
                            Log.e("ATKLog", "LoadingCheckService: خطا در حلقه اصلی سرویس", e)
                            delay(TimeUnit.SECONDS.toMillis(CHECK_INTERVAL_SECONDS))
                        }
                    }
                }
            }
            
            Log.d("ATKLog", "LoadingCheckService: onStartCommand با موفقیت به پایان رسید")
            return START_STICKY
        } catch (e: Exception) {
            Log.e("ATKLog", "LoadingCheckService: خطای کلی در onStartCommand", e)
            return START_STICKY
        }
    }

    private fun acquireWakeLock() {
        try {
            Log.d("ATKLog", "acquireWakeLock: شروع دریافت wakeLock")
            if (!wakeLock.isHeld) {
                try {
                    Log.d("ATKLog", "acquireWakeLock: wakeLock در حال حاضر نگه داشته نشده است، تلاش برای دریافت")
                    wakeLock.acquire(10*60*1000L)
                    Log.d("ATKLog", "acquireWakeLock: wakeLock با موفقیت دریافت شد")
                } catch (e: Exception) {
                    Log.e("ATKLog", "acquireWakeLock: خطا در دریافت wakeLock", e)
                }
            } else {
                Log.d("ATKLog", "acquireWakeLock: wakeLock قبلاً دریافت شده است")
            }
        } catch (e: Exception) {
            Log.e("ATKLog", "acquireWakeLock: خطای کلی در دریافت wakeLock", e)
        }
    }

    private suspend fun fetchRealTimeData() {
        try {
            val response = apiService.getRealTimeLoadingData()
            if (response.isSuccessful) {
                val realTimeDataResponse = response.body()
                if (realTimeDataResponse != null) {
                    processRealTimeData(realTimeDataResponse)
                } else {
                    Log.e("LoadingCheckService", "Received null real-time data")
                }
            } else {
                Log.e("LoadingCheckService", "Unsuccessful response: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e("LoadingCheckService", "Error fetching real-time data: ${e.message}")
        }
    }

    private fun processRealTimeData(realTimeDataResponse: RealTimeDataResponse) {
        if (!isAdminUser) {
            return
        }

        try {
            val activeNotifications = notificationManager.activeNotifications
                .filter { it.id >= REAL_TIME_NOTIFICATION_ID }
                .associateBy { it.notification.extras.getString("loadingQuotaNumber") }

            realTimeDataResponse.data.forEach { loadingData ->
                val existingNotification = activeNotifications[loadingData.loadingQuotaNumber]
                val isDoNotShow = isNotificationMarkedAsDoNotShow(loadingData.loadingQuotaNumber)

                if (!isDoNotShow) {
                    val needsUpdate = existingNotification?.let { notification ->
                        val currentContent = notification.notification.extras.getString(NotificationCompat.EXTRA_TEXT)
                        val newContent = "📥 ${loadingData.entryVouchers} | 📤 ${loadingData.exitVouchers} | 📊 ${loadingData.entryVouchers + loadingData.exitVouchers}"
                        currentContent != newContent
                    } ?: true

                    if (needsUpdate) {
                        showRealTimeLoadingNotification(loadingData)
                    }
                }
            }

        } catch (e: Exception) {
            Log.e("LoadingCheckService", "Error processing real-time data: ${e.message}")
        }
    }

    private fun showRealTimeLoadingNotification(loadingData: RealTimeLoadingData) {
        val notificationId = REAL_TIME_NOTIFICATION_ID + loadingData.loadingQuotaNumber.hashCode()
        val totalVouchers = loadingData.entryVouchers + loadingData.exitVouchers

        val intent = Intent(this, MainActivity::class.java).apply {
            action = "com.atk.atk_cargo.VIEW_LOADING"
            putExtra("loadingQuotaNumber", loadingData.loadingQuotaNumber)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val doNotShowIntent = Intent(ACTION_DO_NOT_SHOW).apply {
            putExtra("loadingQuotaNumber", loadingData.loadingQuotaNumber)
        }
        val doNotShowPendingIntent = PendingIntent.getBroadcast(
            this,
            notificationId,
            doNotShowIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, REAL_TIME_CHANNEL_ID)
            .setContentTitle("${loadingData.shipName} - ${getLastFourDigits(loadingData.loadingQuotaNumber)}")
            .setContentText("ورود: ${loadingData.entryVouchers} | خروج: ${loadingData.exitVouchers} | کل: $totalVouchers")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("""
                🏭 ${loadingData.loadingWarehouse} - 🚛 ${loadingData.shippingCompany}
                📥 ${loadingData.entryVouchers} | 📤 ${loadingData.exitVouchers} | 📊 $totalVouchers | ⚖️ ${formatWeight(loadingData.totalNetWeight.toFloat())}
            """.trimIndent())
            )
            .setSmallIcon(R.drawable.ic_notification)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setAutoCancel(false)
            .setContentIntent(pendingIntent)
            .addAction(R.drawable.ic_cargo_counter, "عدم نمایش", doNotShowPendingIntent)
            .addExtras(bundleOf("loadingQuotaNumber" to loadingData.loadingQuotaNumber))
            .build()

        notificationManager.notify(notificationId, notification)
    }

    private fun markNotificationAsDoNotShow(loadingQuotaNumber: String) {
        sharedPreferences.edit().putBoolean(DO_NOT_SHOW_PREFIX + loadingQuotaNumber, true).apply()
    }

    private fun isNotificationMarkedAsDoNotShow(loadingQuotaNumber: String): Boolean {
        return sharedPreferences.getBoolean(DO_NOT_SHOW_PREFIX + loadingQuotaNumber, false)
    }

    private fun clearDoNotShowPreferences() {
        val editor = sharedPreferences.edit()
        sharedPreferences.all.keys
            .filter { it.startsWith(DO_NOT_SHOW_PREFIX) }
            .forEach { editor.remove(it) }
        editor.apply()
    }

    private fun processLoadingData(realTimeDataResponse: RealTimeDataResponse) {
        try {
            val currentShift = realTimeDataResponse.shiftInfo.type
            val lastShift = sharedPreferences.getString(LAST_SHIFT_KEY, "")

            if (currentShift != lastShift) {
                sharedPreferences.edit()
                    .clear()
                    .putString(LAST_SHIFT_KEY, currentShift)
                    .apply()
                clearDoNotShowPreferences()
            }

            // Rest of the function remains the same
        } catch (e: Exception) {
            Log.e("LoadingCheckService", "Error in processLoadingData", e)
        }
    }

    private suspend fun checkNewMessages(userType: String) {
        try {
            val lastCheckTime = getLastMessageCheckTime()
            val response = RetrofitClient.apiService.getNewMessages(
                userType = userType,
                lastCheckTime = lastCheckTime
            )

            if (response.isSuccessful) {
                val messages = response.body()
                if (!messages.isNullOrEmpty()) {
                    processNewMessages(messages)
                    val newLastCheckTime = messages.maxByOrNull { it.dateTime }?.dateTime ?: lastCheckTime
                    updateLastMessageCheckTime(newLastCheckTime)
                }
            }
        } catch (e: Exception) {
            Log.e("LoadingCheckService", "Error in checkNewMessages", e)
        }
    }

    private suspend fun processNewMessages(messages: List<Message>) {
        val displayedMessages = getDisplayedMessageIds()
        val username = userPreferencesManager.username.first()

        messages.forEach { message ->
            if (message.id !in displayedMessages) {
                try {
                    val markAsReadResponse = RetrofitClient.apiService.markMessageAsRead(
                        MessageReadRequest(
                            messageId = message.id,
                            username = username
                        )
                    )

                    if (markAsReadResponse.isSuccessful) {
                        showMessageNotification(message)
                        addDisplayedMessageId(message.id)
                    }
                } catch (e: Exception) {
                    Log.e("LoadingCheckService", "Error processing message", e)
                }
            }
        }
    }

    private fun createNotificationChannels() {
        // کانال اصلی سرویس
        val channel = NotificationChannel(
            CHANNEL_ID,
            "بررسی بارگیری",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "کانال نوتیفیکیشن برای سرویس بررسی بارگیری"
        }
        notificationManager.createNotificationChannel(channel)

        // کانال هشدارهای تمام صفحه
        val fullScreenChannel = NotificationChannel(
            FULL_SCREEN_CHANNEL_ID,
            "هشدارهای مهم",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "اعلان‌های تمام صفحه برای هشدارهای مهم"
            enableVibration(true)
            enableLights(true)
        }
        notificationManager.createNotificationChannel(fullScreenChannel)

        // کانال اطلاعات لحظه‌ای
        val realTimeChannel = NotificationChannel(
            REAL_TIME_CHANNEL_ID,
            "بارگیری لحظه‌ای",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "نمایش اطلاعات لحظه‌ای بارگیری"
            setShowBadge(true)
            enableVibration(true)
            enableLights(true)
        }
        notificationManager.createNotificationChannel(realTimeChannel)
    }

    private fun createMessageNotificationChannel() {
        val channel = NotificationChannel(
            MESSAGE_CHANNEL_ID,
            "پیام‌های جدید",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "کانال نوتیفیکیشن برای پیام‌های جدید"
            enableLights(true)
            enableVibration(true)
            setShowBadge(true)
        }
        notificationManager.createNotificationChannel(channel)
    }

    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("سرویس بررسی بارگیری")
            .setContentText("در حال بررسی اطلاعات بارگیری")
            .setSmallIcon(R.drawable.ic_notification)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent)
            .build()
    }

    private fun getLastMessageCheckTime(): String {
        val lastCheckTime = sharedPreferences.getString(LAST_MESSAGE_CHECK_TIME_KEY, null)
        return if (lastCheckTime.isNullOrEmpty()) {
            val defaultTime = ZonedDateTime.now(ZoneId.of("Asia/Tehran"))
                .minusHours(1)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            updateLastMessageCheckTime(defaultTime)
            defaultTime
        } else {
            lastCheckTime
        }
    }

    private fun updateLastMessageCheckTime(newLastCheckTime: String) {
        sharedPreferences.edit().putString(LAST_MESSAGE_CHECK_TIME_KEY, newLastCheckTime).apply()
    }

    private fun getDisplayedMessageIds(): Set<Int> {
        return sharedPreferences.getStringSet(DISPLAYED_MESSAGES_KEY, emptySet())
            ?.mapNotNull { it.toIntOrNull() }
            ?.toSet() ?: emptySet()
    }

    private fun addDisplayedMessageId(messageId: Int) {
        val displayedMessages = getDisplayedMessageIds().toMutableSet()
        displayedMessages.add(messageId)
        sharedPreferences.edit()
            .putStringSet(DISPLAYED_MESSAGES_KEY, displayedMessages.map { it.toString() }.toSet())
            .apply()
    }

    private fun showMessageNotification(message: Message) {
        try {
            val fullScreenIntent = Intent(this, MainActivity::class.java).apply {
                action = "com.atk.atk_cargo.NEW_MESSAGE"
                putExtra("messageId", message.id)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }

            val fullScreenPendingIntent = PendingIntent.getActivity(
                this,
                message.id,
                fullScreenIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(this, MESSAGE_CHANNEL_ID)
                .setContentTitle(message.title)
                .setContentText(message.body)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message.body))
                .setSmallIcon(R.drawable.ic_notification)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setAutoCancel(true)
                .setFullScreenIntent(fullScreenPendingIntent, true)
                .build()

            notificationManager.notify(message.id, notification)
        } catch (e: Exception) {
            Log.e("LoadingCheckService", "Error showing message notification", e)
        }
    }

    private fun checkLoadingStatus() {
        if (!isAdminUser) return

        serviceScope.launch {
            try {
                val response = apiService.getRealTimeLoadingData()
                if (response.isSuccessful) {
                    val realTimeDataResponse = response.body()
                    realTimeDataResponse?.let {
                        processLoadingData(it)
                    }
                }
            } catch (e: Exception) {
                Log.e("LoadingCheckService", "Error in checkLoadingStatus", e)
            }
        }
    }

    private fun getLastFourDigits(number: String): String {
        return if (number.length > 4) {
            number.takeLast(4)
        } else {
            number
        }
    }

    private fun formatWeight(weightKg: Float): String {
        return when {
            weightKg >= 1_000_000 -> "%.1f هزار تن".format(weightKg / 1_000_000)
            weightKg >= 1_000 -> "%.1f تن".format(weightKg / 1_000)
            else -> "$weightKg کیلوگرم"
        }
    }

    override fun onDestroy() {
        try {
            Log.d("ATKLog", "LoadingCheckService: onDestroy شروع شد")
            super.onDestroy()
            
            try {
                Log.d("ATKLog", "LoadingCheckService: آزادسازی wakeLock")
                if (wakeLock.isHeld) {
                    wakeLock.release()
                    Log.d("ATKLog", "LoadingCheckService: wakeLock با موفقیت آزاد شد")
                } else {
                    Log.d("ATKLog", "LoadingCheckService: wakeLock قبلاً آزاد شده است")
                }
            } catch (e: Exception) {
                Log.e("ATKLog", "LoadingCheckService: خطا در آزادسازی wakeLock", e)
            }
            
            try {
                Log.d("ATKLog", "LoadingCheckService: ارسال برودکست برای راه‌اندازی مجدد سرویس")
                val broadcastIntent = Intent(this, ServiceRestartReceiver::class.java)
                sendBroadcast(broadcastIntent)
                Log.d("ATKLog", "LoadingCheckService: برودکست با موفقیت ارسال شد")
            } catch (e: Exception) {
                Log.e("ATKLog", "LoadingCheckService: خطا در ارسال برودکست", e)
            }
            
            try {
                Log.d("ATKLog", "LoadingCheckService: لغو ثبت گیرنده برودکست")
                unregisterReceiver(doNotShowReceiver)
                Log.d("ATKLog", "LoadingCheckService: گیرنده برودکست با موفقیت لغو ثبت شد")
            } catch (e: Exception) {
                Log.e("ATKLog", "LoadingCheckService: خطا در لغو ثبت گیرنده برودکست", e)
            }
            
            try {
                Log.d("ATKLog", "LoadingCheckService: لغو کوروتین‌ها")
                // فقط کوروتین‌های غیر از mainServiceJob را لغو می‌کنیم
                if (::mainServiceJob.isInitialized && !mainServiceJob.isCancelled) {
                    // mainServiceJob را لغو نمی‌کنیم تا سرویس بتواند مجدداً راه‌اندازی شود
                    Log.d("ATKLog", "LoadingCheckService: mainServiceJob لغو نشد تا سرویس بتواند مجدداً راه‌اندازی شود")
                } else {
                    serviceScope.cancel()
                    Log.d("ATKLog", "LoadingCheckService: کوروتین‌ها با موفقیت لغو شدند")
                }
            } catch (e: Exception) {
                Log.e("ATKLog", "LoadingCheckService: خطا در لغو کوروتین‌ها", e)
            }
            
            Log.d("ATKLog", "LoadingCheckService: onDestroy با موفقیت به پایان رسید")
        } catch (e: Exception) {
            Log.e("ATKLog", "LoadingCheckService: خطای کلی در onDestroy", e)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}