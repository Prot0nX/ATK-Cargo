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
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.atk.atk_cargo.api.AppNotificationManager
import com.atk.atk_cargo.api.LoadingNotificationService
import com.atk.atk_cargo.api.RetrofitClient
import com.atk.atk_cargo.api.SessionCheckRequest
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
import kotlinx.coroutines.withTimeoutOrNull
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

    private var isSplashVisible by mutableStateOf(true)
    private var isVersionAllowedState by mutableStateOf(true)
    private var isServerSyncing by mutableStateOf(false)

    // باید در زمان ساخت Activity (نه داخل onCreate) ثبت شود؛ برخلاف
    // ActivityCompat.requestPermissions قدیمی، این API نتیجه را واقعاً برمی‌گرداند
    private val notificationPermissionLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        Log.d("MainActivity", "مجوز POST_NOTIFICATIONS: ${if (isGranted) "اعطا شد" else "رد شد"}")
    }

    @SuppressLint("CoroutineCreationDuringComposition", "BatteryLife")
    override fun onCreate(savedInstanceState: Bundle?) {
        // باید قبل از super.onCreate فراخوانی شود؛ پنجره‌ی سفید پیش‌فرض سیستم را با
        // پس‌زمینه/آیکون برند جایگزین می‌کند تا Compose برای اولین فریم آماده شود
        installSplashScreen()
        super.onCreate(savedInstanceState)

        try {
            initializeDependencies()

            // بررسی intent برای باز کردن دیالوگ هشدار تناژ کوتاژ
            handleIntent(intent)
            intent.getStringExtra("navigate_to")?.let {
                pendingNavigationDestination = it
                intent.removeExtra("navigate_to")
            }

            // خواندن blocking و یک‌باره‌ی رنگ تم قبل از setContent — بدون این کار،
            // فریم اول با رنگ پیش‌فرض رندر می‌شد و به‌محض emit شدن مقدار واقعی از
            // DataStore، کل درخت UI recompose می‌شد (پرش رنگ قابل مشاهده)
            val initialThemeColor = kotlinx.coroutines.runBlocking {
                userPreferencesManager.themeColor.first()
            }

            setContent {
                val themeColorLong by userPreferencesManager.themeColor.collectAsState(initial = initialThemeColor)
                val primaryColor = Color(themeColorLong)
                ATKCargoTheme(primaryColor = primaryColor) {
                    val retryScope = androidx.compose.runtime.rememberCoroutineScope()

                    // ===== راه‌اندازی موازی تمام فرآیندهای پس‌زمینه =====
                    LaunchedEffect(Unit) {
                        kotlinx.coroutines.coroutineScope {
                            // راه‌اندازی کانال‌های نوتیفیکیشن
                            launch(Dispatchers.IO) {
                                AppNotificationManager(this@MainActivity).setupChannels()
                            }

                            // بلاک پردازش‌های شبکه — روی Dispatchers.IO تا ساخت اولیه‌ی
                            // OkHttpClient/Retrofit (که با اولین دسترسی به apiService رخ
                            // می‌دهد) روی main thread قبل از اولین فریم اجرا نشود
                            val networkJob = async(Dispatchers.IO) {
                                // بررسی نسخه‌ی مجاز و وجود آپدیت با یک تک درخواست مشترک به check_update.php
                                val versionAndUpdateJob = async { updateManager.checkVersionAndUpdate() }
                                val securityJob = async { performAppSecurityCheck() }
                                val sessionJob = async { checkUserSessionAsync() }

                                val versionResult = versionAndUpdateJob.await()
                                if (!versionResult.isVersionAllowed) {
                                    // چون securityJob/sessionJob فرزند همین coroutine هستند، تا
                                    // پایان کارشان return@async کامل نمی‌شود مگر صریحاً cancel شوند —
                                    // در غیر این صورت این return هیچ زمانی صرفه‌جویی نمی‌کرد
                                    securityJob.cancel()
                                    sessionJob.cancel()
                                    return@async Pair(false, false)
                                }

                                applyUpdateResult(versionResult.hasUpdate)
                                securityJob.await()
                                val sessionValid = sessionJob.await()

                                return@async Pair(true, sessionValid)
                            }

                            // تایمر تطبیقی Splash: حداقل نمایش برای جلوگیری از پرش بصری،
                            // سپس به‌محض آماده شدن شبکه بسته می‌شود (حداکثر تا سقف SPLASH_MAX_DURATION)
                            val splashTimer = launch {
                                delay(SPLASH_MIN_DURATION.milliseconds)
                                withTimeoutOrNull((SPLASH_MAX_DURATION - SPLASH_MIN_DURATION).milliseconds) {
                                    networkJob.join()
                                }
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

                            requestBatteryOptimizationIfNeeded()
                        }
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
                                errorType = securityErrorType ?: SecurityErrorType.TAMPERED,
                                onRetry = {
                                    retryScope.launch { performAppSecurityCheck() }
                                }
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

        } catch (e: Exception) {
            // خطای کلی در راه‌اندازی برنامه — باید لاگ شود، وگرنه کاربر فقط یک صفحه‌ی
            // سفید بدون هیچ نشانه‌ای می‌بیند و عیب‌یابی در میدان غیرممکن می‌شود
            Log.e("MainActivity", "خطای بحرانی در راه‌اندازی برنامه: ${e.message}", e)
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
                { RetrofitClient.apiService },
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
        // heuristic ارزان و همزمان است (بدون I/O، بدون بنچمارک CPU/GPU/حافظه، بدون
        // System.gc())؛ نیازی به coroutine یا کش DataStore برای تعیین امتیاز ندارد —
        // فقط نتیجه برای نمایش در پروفایل کاربر ذخیره می‌شود
        val performanceScore = HardwarePerformanceEvaluator(this).evaluatePerformance()
        AnimationManager.setPerformanceScore(performanceScore)

        lifecycleScope.launch(Dispatchers.IO) {
            userPreferencesManager.saveHardwareScore(performanceScore)
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

            // فقط یک بار در طول عمر نصب برنامه از کاربر بپرس — نه در هر راه‌اندازی؛
            // اگر یک بار «نه» بگوید، تکرار مکرر این دیالوگ هم آزاردهنده است و هم
            // ریسک policy در Google Play دارد (REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
            if (userPreferencesManager.hasBatteryOptimizationBeenRequested()) return
            userPreferencesManager.markBatteryOptimizationRequested()

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

    private fun applyUpdateResult(hasUpdate: Boolean) {
        isUpdateAvailable = hasUpdate
        if (hasUpdate) {
            updateInfo = updateManager.updateInfo.value
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
        val username = userPreferencesManager.username.first()
        if (username.isEmpty()) return false

        return try {
            val deviceId = userPreferencesManager.deviceId.first()
            val sessionToken = userPreferencesManager.sessionToken.first()
            val apiService = RetrofitClient.apiService
            val sessionRequest = SessionCheckRequest(username, deviceId, sessionToken.takeIf { it.isNotEmpty() })

            val response = apiService.checkSession(sessionRequest)
            val isValid = response.isSuccessful && response.body()?.success == true
            if (isValid) {
                userPreferencesManager.saveLastSessionVerifiedTimestamp(System.currentTimeMillis())
            }
            // پاسخ صریح سرور (از جمله ۴۰۹ نشست تکراری) همیشه fail-closed است — هرگز
            // grace period نمی‌گیرد، وگرنه منطق «یک دستگاه در هر زمان» دور زده می‌شود
            isValid
        } catch (e: java.io.IOException) {
            // فقط خطای شبکه‌ی واقعی (نه رد صریح سرور) واجد شرایط grace period محدود است —
            // همان سیاست fail-closed-با-مهلت که برای بخش امنیتی هم استفاده می‌شود (C-2)
            val lastVerified = userPreferencesManager.getLastSessionVerifiedTimestamp()
            val withinGracePeriod = lastVerified > 0L &&
                    (System.currentTimeMillis() - lastVerified) < SESSION_OFFLINE_GRACE_PERIOD_MS
            withinGracePeriod
        } catch (_: Exception) {
            false
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
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    companion object {
        // حداقل زمان نمایش Splash (جلوگیری از پرش بصری) و سقف حداکثر انتظار برای شبکه
        private const val SPLASH_MIN_DURATION = 1200L
        private const val SPLASH_MAX_DURATION = 4500L

        // مهلت آفلاین برای نشست کاربر — همسان با OFFLINE_GRACE_PERIOD_MS در SecurityVerifier (C-2)
        private const val SESSION_OFFLINE_GRACE_PERIOD_MS = 3 * 24 * 60 * 60 * 1000L // ۳ روز
    }
}