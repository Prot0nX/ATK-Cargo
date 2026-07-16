package com.atk.atk_cargo

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.atk.atk_cargo.api.AppNotificationManager
import com.atk.atk_cargo.api.LoadingNotificationService
import com.atk.atk_cargo.api.RetrofitClient
import com.atk.atk_cargo.api.SessionCheckRequest
import com.atk.atk_cargo.api.TonnageWarningService
import com.atk.atk_cargo.api.UpdateInfo
import com.atk.atk_cargo.api.UpdateManager
import com.atk.atk_cargo.api.UpdateManagerFactory
import com.atk.atk_cargo.api.UserPreferencesManager
import com.atk.atk_cargo.core.navigation.MainScreen
import com.atk.atk_cargo.feature.startup.domain.AnimationManager
import com.atk.atk_cargo.feature.startup.domain.HardwarePerformanceEvaluator
import com.atk.atk_cargo.feature.startup.presentation.ServerSyncingScreen
import com.atk.atk_cargo.feature.startup.presentation.SplashScreen
import com.atk.atk_cargo.feature.update.presentation.UpdateDialog
import com.atk.atk_cargo.security.SecurityBlockScreen
import com.atk.atk_cargo.security.SecurityErrorType
import com.atk.atk_cargo.security.SecurityVerifier
import com.atk.atk_cargo.security.VersionExpiredDialog
import com.atk.atk_cargo.ui.theme.ATKCargoTheme
import com.atk.atk_cargo.workers.ChatNotificationWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.milliseconds

class MainActivity : ComponentActivity() {
    private var updateInfo by mutableStateOf<UpdateInfo?>(null)
    private var downloadProgress by mutableStateOf(UpdateManager.DownloadProgress.Initial)
    private var downloadState by mutableStateOf<UpdateManager.DownloadState>(UpdateManager.DownloadState.Idle)
    private lateinit var updateManager: UpdateManager
    private var isUpdateAvailable by mutableStateOf(false)
    private val userPreferencesManager: UserPreferencesManager by inject()
    private val _isSessionValid = MutableStateFlow(false)
    private val securityVerifier: SecurityVerifier by inject()
    private var isSecurityCheckPassed by mutableStateOf(false)
    private var isSecurityCheckLoading by mutableStateOf(true)
    private var securityErrorType by mutableStateOf<SecurityErrorType?>(null)
    var shouldOpenWarningsDialog by mutableStateOf(false)
    var pendingNavigationDestination by mutableStateOf<String?>(null)
    private lateinit var chatRepository: com.atk.atk_cargo.data.repository.ChatRepository
    val isSessionValid: StateFlow<Boolean> = _isSessionValid.asStateFlow()

    // ===== وضعیت‌های Splash Screen =====
    private var isSplashVisible by mutableStateOf(true)
    private var isVersionAllowedState by mutableStateOf(true)
    private var isServerSyncing by mutableStateOf(false)

    @SuppressLint("CoroutineCreationDuringComposition", "BatteryLife")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            initializeDependencies()

            // بررسی intent برای باز کردن دیالوگ هشدار تناژ کوتاژ
            handleIntent(intent)
            intent.getStringExtra("navigate_to")?.let { 
                pendingNavigationDestination = it
                intent.removeExtra("navigate_to")
            }

            setContent {
                val themeColorLong by userPreferencesManager.themeColor.collectAsState(initial = 0xFF137fecL)
                val primaryColor = Color(themeColorLong)
                ATKCargoTheme(primaryColor = primaryColor) {

                    // ===== راه‌اندازی موازی تمام فرآیندهای پس‌زمینه =====
                    LaunchedEffect(Unit) {
                        kotlinx.coroutines.coroutineScope {
                            // راه‌اندازی کانال‌های نوتیفیکیشن
                            launch(Dispatchers.IO) {
                                AppNotificationManager(this@MainActivity).setupChannels()
                            }

                            // بلاک پردازش‌های شبکه
                            val networkJob = async {
                                val versionJob = async { updateManager.isCurrentVersionAllowed() }
                                val securityJob = async { performAppSecurityCheck() }
                                val updateJob = async { checkForUpdate() }
                                val sessionJob = async { checkUserSessionAsync() }

                                val isVersionAllowed = versionJob.await()
                                if (!isVersionAllowed) return@async Pair(false, false)

                                securityJob.await()
                                updateJob.await()
                                val sessionValid = sessionJob.await()

                                return@async Pair(true, sessionValid)
                            }

                            // تایمر دقیق پخش Splash
                            val splashTimer = launch {
                                delay(4500.milliseconds)
                                isSplashVisible = false
                            }

                            // منتظر می‌مانیم تا اسپلش به خاطر تایمر یا کلیک کاربر (Skip) بسته شود
                            androidx.compose.runtime.snapshotFlow { isSplashVisible }.first { !it }
                            splashTimer.cancel()

                            // اگر عملیات سرور همچنان در حال انجام است، وضعیت همگام‌سازی را روشن کن
                            if (networkJob.isActive) {
                                isServerSyncing = true
                            }

                            val (isVersionAllowed, sessionValid) = networkJob.await()
                            
                            isVersionAllowedState = isVersionAllowed
                            isServerSyncing = false

                            if (!isVersionAllowed) {
                                return@coroutineScope
                            }

                            // مدیریت وضعیت نشست کاربر
                            if (!sessionValid && userPreferencesManager.username.first().isNotEmpty()) {
                                userPreferencesManager.clearUserCredentials()
                                withContext(Dispatchers.Main) {
                                    showMessage("لطفاً دوباره وارد شوید!")
                                }
                            }
                            
                            _isSessionValid.value = sessionValid
                            
                            if (sessionValid) {
                                startLoadingNotificationService()
                                startChatNotificationWorker()
                                checkInitialChatMessages()
                            }

                            checkTonnageWarnings()
                            requestBatteryOptimizationIfNeeded()
                        }
                    }

                    LaunchedEffect(Unit) {
                        handleIntent(intent)
                    }

                    // ===== منطق نمایش صفحات =====
                    when {
                        // ۱. Splash Screen: تا زمانی که ویدیو تمام شود یا کاربر رد کند
                        isSplashVisible -> {
                            SplashScreen(onSkip = { isSplashVisible = false })
                        }
                        // ۲. صفحه همگام‌سازی سرور: اگر پس از اتمام ویدیو، شبکه هنوز پاسخ نداده باشد
                        isServerSyncing -> {
                            ServerSyncingScreen()
                        }
                        // ۳. نسخه منقضی شده
                        !isVersionAllowedState -> {
                            VersionExpiredDialog(
                                onExit = { android.os.Process.killProcess(android.os.Process.myPid()) }
                            )
                        }
                        // ۳. بررسی امنیتی ناموفق یا در حال بارگیری
                        !isSecurityCheckPassed || isSecurityCheckLoading -> {
                            SecurityBlockScreen(
                                isLoading = isSecurityCheckLoading,
                                errorType = securityErrorType ?: SecurityErrorType.TAMPERED
                            )
                        }
                        // ۴. محتوای اصلی برنامه
                        else -> {
                            HandleMainContent(
                                isUpdateAvailable = isUpdateAvailable,
                                updateInfo = updateInfo
                            )
                        }
                    }
                }
            }

            observeApplicationStates()

        } catch (_: Exception) {
            // خطای کلی در راه‌اندازی برنامه
        }
    }

    private fun initializeDependencies() {
        try {
            updateManager = ViewModelProvider(
                this,
                UpdateManagerFactory(this)
            )[UpdateManager::class.java]

            val database = com.atk.atk_cargo.data.db.AppDatabase.getDatabase(this)
            chatRepository = com.atk.atk_cargo.data.repository.ChatRepository(
                database.chatDao(),
                RetrofitClient.apiService,
                userPreferencesManager
            )

            // ارزیابی عملکرد سخت‌افزار و تنظیم انیمیشن‌ها
            initializeHardwarePerformanceEvaluation()

        } catch (e: Exception) {
            // خطا در مقداردهی وابستگی‌ها
            throw e // پرتاب مجدد خطا برای مدیریت در سطح بالاتر
        }
    }

    private fun initializeHardwarePerformanceEvaluation() {
        lifecycleScope.launch {
            try {
                val userPreferencesManager = UserPreferencesManager(this@MainActivity)
                val hardwareEvaluator = HardwarePerformanceEvaluator(this@MainActivity, userPreferencesManager)

                // استفاده از تابع suspend برای ارزیابی عملکرد با قابلیت کش
                val performanceScore = hardwareEvaluator.evaluatePerformance()

                // تنظیم امتیاز عملکرد در مدیر انیمیشن‌ها
                AnimationManager.setPerformanceScore(performanceScore)
            } catch (e: Exception) {
                Log.e("HardwarePerformance", "خطا در ارزیابی عملکرد سخت‌افزار: ${e.message}")
                // در صورت خطا، امتیاز متوسط تنظیم می‌شود
                AnimationManager.setPerformanceScore(50)
            }
        }
    }

    private suspend fun performAppSecurityCheck() {
        isSecurityCheckLoading = true
        try {
            val (securityPassed, errorType) = securityVerifier.verifySecurityStatus()
            isSecurityCheckPassed = securityPassed
            securityErrorType = errorType
        } catch (_: Exception) {
            isSecurityCheckPassed = false
            securityErrorType = SecurityErrorType.TAMPERED
        } finally {
            isSecurityCheckLoading = false
        }
    }

    @Composable
    private fun HandleMainContent(
        isUpdateAvailable: Boolean,
        updateInfo: UpdateInfo?
    ) {
        if (isUpdateAvailable && updateInfo != null) {
            UpdateDialog(
                updateInfo = updateInfo,
                downloadProgress = downloadProgress,
                downloadState = downloadState,
                onUpdateClick = { startUpdateDownload() },
                onPauseClick = { updateManager.pauseDownload() },
                onResumeClick = { updateManager.resumeDownload() },
                onCancelClick = { updateManager.cancelDownload() },
                onDismiss = { /* Handle dismiss */ }
            )
        } else {
            MainScreen()
        }
    }

    @SuppressLint("BatteryLife")
    private suspend fun requestBatteryOptimizationIfNeeded() {
        try {
            val pm = getSystemService(POWER_SERVICE) as PowerManager
            if (pm.isIgnoringBatteryOptimizations(packageName)) return

            withContext(Dispatchers.Main) {
                try {
                    val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                        data = "package:$packageName".toUri()
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    if (intent.resolveActivity(packageManager) != null) {
                        startActivity(intent)
                        showMessage("لطفاً اجازه دهید برنامه بدون محدودیت باتری اجرا شود")
                    } else {
                        val batteryIntent = Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        startActivity(batteryIntent)
                        showMessage("لطفاً برنامه را از محدودیت‌های بهینه‌سازی باتری خارج کنید")
                    }
                } catch (_: Exception) {
                    try {
                        startActivity(Intent(Settings.ACTION_SETTINGS).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        })
                        showMessage("لطفاً در تنظیمات، برنامه را از محدودیت‌های باتری خارج کنید")
                    } catch (e: Exception) {
                        Log.e("BatteryOptimization", "خطا در باز کردن تنظیمات: ${e.message}")
                    }
                }
            }
        } catch (_: Exception) {
            // نادیده گرفتن خطای مجوز باتری - غیرحیاتی است
        }
    }

    private fun observeApplicationStates() {
        lifecycleScope.launch {
            updateManager.downloadProgress.collect { progress ->
                downloadProgress = progress
            }
        }

        lifecycleScope.launch {
            updateManager.downloadState.collect { state ->
                downloadState = state
                handleDownloadState(state)
            }
        }
    }

    private fun handleDownloadState(state: UpdateManager.DownloadState) {
        when (state) {
            is UpdateManager.DownloadState.Completed -> {
                updateInfo?.let {
                    updateManager.installUpdate(updateManager.getDownloadedFile())
                }
            }
            is UpdateManager.DownloadState.Error -> {
                showMessage(state.message)
            }
            else -> { /* Other states don't require specific handling */ }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::updateManager.isInitialized) {
            updateManager.onCleared()
        }
    }

    private suspend fun checkForUpdate() {
        isUpdateAvailable = updateManager.checkForUpdate()
        if (isUpdateAvailable) {
            updateInfo = updateManager.updateInfo.value
        }
    }

    fun checkTonnageWarnings() {
        lifecycleScope.launch {
            if (userPreferencesManager.loadingNotificationsEnabled.first()) {
                // راه‌اندازی سرویس بررسی دوره‌ای هشدارها
                TonnageWarningService.startService(this@MainActivity)
            }
        }
    }

    private fun startUpdateDownload() {
        updateInfo?.downloadUrl?.let { url ->
            updateManager.startDownload(url)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
        
        // Handle navigation intent
        intent.getStringExtra("navigate_to")?.let { destination ->
            pendingNavigationDestination = destination
            intent.removeExtra("navigate_to")
        }
    }

    private fun handleIntent(intent: Intent?) {
        when (intent?.action) {
            "com.atk.atk_cargo.NEW_LOADING" -> {
                intent.getStringExtra("kotazh") ?: return
                // Add logic for new loading here
            }
            "com.atk.atk_cargo.OPEN_WARNINGS" -> {
                // علامت‌گذاری برای باز کردن دیالوگ هشدار تناژ کوتاژ
                shouldOpenWarningsDialog = true
            }
        }
    }

    private suspend fun checkUserSessionAsync(): Boolean {
        return try {
            val username = userPreferencesManager.username.first()
            val deviceId = userPreferencesManager.deviceId.first()
            val sessionToken = userPreferencesManager.sessionToken.first()

            if (username.isNotEmpty()) {
                val apiService = RetrofitClient.apiService
                val sessionRequest = SessionCheckRequest(username, deviceId, sessionToken.takeIf { it.isNotEmpty() })

                val response = apiService.checkSession(sessionRequest)
                response.isSuccessful && response.body()?.success == true
            } else {
                false
            }
        } catch (_: Exception) {
            // در صورت خطا، جلسه را معتبر فرض می‌کنیم تا کاربر بتواند به کار خود ادامه دهد
            true
        }
    }

    fun updateSessionValidity(isValid: Boolean) {
        _isSessionValid.value = isValid
    }

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    fun getChatRepository() = chatRepository

    fun startLoadingNotificationService() {
        // بررسی و درخواست مجوز نوتیفیکیشن
        checkNotificationPermission()

        // بررسی سطح دسترسی کاربر قبل از راه‌اندازی سرویس
        lifecycleScope.launch {
            val userPreferencesManager = UserPreferencesManager(this@MainActivity)
            
            // اگر نوتیفیکیشن‌های بارگیری غیرفعال باشند، سرویس را متوقف می‌کنیم
            if (!userPreferencesManager.loadingNotificationsEnabled.first()) {
                stopLoadingNotificationService()
                return@launch
            }

            val userType = userPreferencesManager.userType.first()

            if (userType == "admin") {
                // شروع سرویس فقط برای کاربران admin
                LoadingNotificationService.startLoadingNotification(this@MainActivity)
            } else {
                // اطمینان از توقف سرویس اگر قبلاً اجرا شده است
                val intent = Intent(this@MainActivity, LoadingNotificationService::class.java)
                intent.action = "STOP_SERVICE"
                startService(intent)
            }
        }
    }

    fun stopLoadingNotificationService() {
        // توقف سرویس بارگیری
        val loadingIntent = Intent(this, LoadingNotificationService::class.java)
        loadingIntent.action = LoadingNotificationService.ACTION_STOP_SERVICE
        startService(loadingIntent)
        
        // توقف سرویس هشدار تناژ
        val tonnageIntent = Intent(this, TonnageWarningService::class.java)
        stopService(tonnageIntent)
    }

    fun stopChatNotificationService() {
        // لغو ورکر چت
        WorkManager.getInstance(applicationContext).cancelUniqueWork("ChatNotificationWorker")
    }

    fun startChatNotificationWorker() {
        lifecycleScope.launch {
            if (!userPreferencesManager.chatNotificationsEnabled.first()) {
                stopChatNotificationService()
                return@launch
            }

            val constraints = androidx.work.Constraints.Builder()
                .setRequiredNetworkType(androidx.work.NetworkType.CONNECTED)
                .build()

            val workRequest = PeriodicWorkRequestBuilder<ChatNotificationWorker>(15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
                "ChatNotificationWorker",
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        }
    }

    private fun checkInitialChatMessages() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val enabled = userPreferencesManager.chatNotificationsEnabled.first()
                if (!enabled) return@launch

                val username = userPreferencesManager.username.first()
                if (username.isEmpty()) return@launch

                Log.d("ATK_CHAT_DEBUG", "Startup: Checking initial chat messages...")

                // همگام‌سازی پیام‌ها با دیتابیس محلی
                chatRepository.refreshMessages()

                // دریافت آخرین آی‌دی اعلان شده از تنظیمات
                val lastNotifiedId = userPreferencesManager.lastNotifiedMessageId.first()
                val userType = userPreferencesManager.userType.first()

                // دریافت پیام‌های جدید نخوانده از دیتابیس محلی
                val allMessages = chatRepository.messages.first()
                val newUnreadMessages = allMessages.filter {
                    it.id > lastNotifiedId && !it.isReadByMe && it.username != username
                }

                if (newUnreadMessages.isNotEmpty()) {
                    Log.d("ATK_CHAT_DEBUG", "Startup: Found ${newUnreadMessages.size} new unread messages")
                    val appNotificationManager = AppNotificationManager(this@MainActivity)
                    var maxId = lastNotifiedId

                    // مرتب‌سازی بر اساس آی‌دی برای نمایش به ترتیب
                    newUnreadMessages.sortedBy { it.id }.forEach { msg ->
                        if (msg.id > maxId) maxId = msg.id

                        // منطق مشابه ChatNotificationWorker برای نمایش اعلان
                        if (userType == "admin" || msg.message.contains("@$username")) {
                            // بررسی مجوز POST_NOTIFICATIONS برای Android 13+
                            val hasNotificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                ContextCompat.checkSelfPermission(
                                    this@MainActivity,
                                    Manifest.permission.POST_NOTIFICATIONS
                                ) == PackageManager.PERMISSION_GRANTED
                            } else {
                                true
                            }
                            if (hasNotificationPermission) {
                                withContext(Dispatchers.Main) {
                                    try {
                                        appNotificationManager.showChatNotification(
                                            msg.fullName,
                                            msg.message
                                        )
                                    } catch (se: SecurityException) {
                                        Log.w("ATK_CHAT_DEBUG", "مجوز نوتیفیکیشن رد شد: ${se.message}")
                                    }
                                }
                            }
                        }
                    }

                    // به‌روزرسانی آخرین آی‌دی اعلان شده
                    userPreferencesManager.saveLastNotifiedMessageId(maxId)
                } else {
                    Log.d("ATK_CHAT_DEBUG", "Startup: No new messages to notify")
                }
            } catch (e: Exception) {
                Log.e("ATK_CHAT_DEBUG", "Error checking initial chat messages", e)
            }
        }
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!hasPermission) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    companion object {
        private const val NOTIFICATION_PERMISSION_REQUEST_CODE = 100
    }
}