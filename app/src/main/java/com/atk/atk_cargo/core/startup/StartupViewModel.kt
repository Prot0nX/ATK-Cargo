package com.atk.atk_cargo.core.startup

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.atk.atk_cargo.domain.session.StartupController
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.atk.atk_cargo.api.AppNotificationManager
import com.atk.atk_cargo.api.LoadingNotificationService
import com.atk.atk_cargo.api.RetrofitClient
import com.atk.atk_cargo.api.Secrets
import com.atk.atk_cargo.api.SessionCheckRequest
import com.atk.atk_cargo.api.TokenRefresher
import com.atk.atk_cargo.api.UpdateManager
import com.atk.atk_cargo.api.UserPreferencesManager
import com.atk.atk_cargo.feature.chat.data.ChatRepository
import com.atk.atk_cargo.security.SecurityErrorType
import com.atk.atk_cargo.security.SecurityVerifier
import com.atk.atk_cargo.workers.ChatNotificationWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.milliseconds
sealed interface StartupState {
    data object Splash : StartupState
    data object Syncing : StartupState
    data object VersionExpired : StartupState
    data class SecurityBlocked(val isLoading: Boolean, val errorType: SecurityErrorType) : StartupState
    data object Ready : StartupState
}

data class SecurityCheckState(
    val isPassed: Boolean = false,
    val isLoading: Boolean = true,
    val errorType: SecurityErrorType = SecurityErrorType.TAMPERED
)

/** رخدادهای یک‌باره‌ای که نیاز به یک Activity واقعی دارند (startActivity/دیالوگ سیستمی). */
sealed interface StartupEvent {
    data class ShowMessage(val message: String) : StartupEvent
    data object RequestBatteryOptimization : StartupEvent
}

class StartupViewModel(
    application: Application,
    private val userPreferencesManager: UserPreferencesManager,
    private val securityVerifier: SecurityVerifier,
    private val updateManager: UpdateManager,
    private val chatRepository: ChatRepository
) : AndroidViewModel(application), StartupController {

    private val appContext get() = getApplication<Application>()
    val themeColor: Flow<Long> = userPreferencesManager.themeColor

    private val _isSplashVisible = MutableStateFlow(true)
    private val _isServerSyncing = MutableStateFlow(false)
    private val _isVersionAllowed = MutableStateFlow(true)
    private val _securityCheck = MutableStateFlow(SecurityCheckState())

    val startupState: StateFlow<StartupState> = combine(
        _isSplashVisible, _isServerSyncing, _isVersionAllowed, _securityCheck
    ) { splashVisible, serverSyncing, versionAllowed, security ->
        when {
            splashVisible -> StartupState.Splash
            serverSyncing -> StartupState.Syncing
            !versionAllowed -> StartupState.VersionExpired
            !security.isPassed || security.isLoading -> StartupState.SecurityBlocked(security.isLoading, security.errorType)
            else -> StartupState.Ready
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, StartupState.Splash)

    private val _isSessionValid = MutableStateFlow(false)
    override val isSessionValid: StateFlow<Boolean> = _isSessionValid.asStateFlow()

    private val _isUpdateAvailable = MutableStateFlow(false)
    val isUpdateAvailable: StateFlow<Boolean> = _isUpdateAvailable.asStateFlow()

    private val _pendingNavigationDestination = MutableStateFlow<String?>(null)
    override val pendingNavigationDestination: StateFlow<String?> = _pendingNavigationDestination.asStateFlow()

    private val _shouldOpenWarningsDialog = MutableStateFlow(false)

    private val _events = Channel<StartupEvent>(Channel.BUFFERED)
    val events: Flow<StartupEvent> = _events.receiveAsFlow()

    private var startupSequenceStarted = false

    fun getUpdateManager(): UpdateManager = updateManager

    /** فقط یک‌بار در طول عمر ViewModel اجرا می‌شود؛ در چرخش صفحه دوباره اجرا نمی‌شود. */
    fun runStartupSequenceOnce() {
        if (startupSequenceStarted) return
        startupSequenceStarted = true

        viewModelScope.launch {
            coroutineScope {
                launch(Dispatchers.IO) {
                    AppNotificationManager(appContext).setupChannels()
                }

                val networkJob = async(Dispatchers.IO) {
                    val versionAndUpdateJob = async { updateManager.checkVersionAndUpdate() }
                    val securityJob = async { performAppSecurityCheck() }
                    val sessionJob = async { checkUserSessionAsync() }

                    val versionResult = versionAndUpdateJob.await()
                    if (!versionResult.isVersionAllowed) {
                        // چون securityJob/sessionJob فرزند همین coroutine هستند، تا پایان
                        // کارشان return@async کامل نمی‌شود مگر صریحاً cancel شوند
                        securityJob.cancel()
                        sessionJob.cancel()
                        return@async Pair(false, false)
                    }

                    _isUpdateAvailable.value = versionResult.hasUpdate
                    securityJob.await()
                    val sessionValid = sessionJob.await()

                    Pair(true, sessionValid)
                }

                // تایمر تطبیقی Splash: حداقل نمایش برای جلوگیری از پرش بصری، سپس به‌محض
                // آماده شدن شبکه بسته می‌شود (حداکثر تا سقف SPLASH_MAX_DURATION)
                val splashTimer = launch {
                    delay(SPLASH_MIN_DURATION.milliseconds)
                    withTimeoutOrNull((SPLASH_MAX_DURATION - SPLASH_MIN_DURATION).milliseconds) {
                        networkJob.join()
                    }
                    _isSplashVisible.value = false
                }

                // منتظر می‌مانیم تا اسپلش به خاطر تایمر یا کلیک کاربر (Skip) بسته شود
                _isSplashVisible.first { !it }
                splashTimer.cancel()

                if (networkJob.isActive) {
                    _isServerSyncing.value = true
                }

                val (isVersionAllowed, sessionValid) = networkJob.await()

                _isVersionAllowed.value = isVersionAllowed
                _isServerSyncing.value = false

                if (!isVersionAllowed) {
                    return@coroutineScope
                }

                if (!sessionValid && userPreferencesManager.username.first().isNotEmpty()) {
                    userPreferencesManager.clearUserCredentials()
                    _events.send(StartupEvent.ShowMessage("لطفاً دوباره وارد شوید!"))
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
    }

    fun skipSplash() {
        _isSplashVisible.value = false
    }

    fun retrySecurityCheck() {
        viewModelScope.launch {
            performAppSecurityCheck()
        }
    }

    override fun updateSessionValidity(isValid: Boolean) {
        _isSessionValid.value = isValid
    }

    override fun consumePendingNavigation() {
        _pendingNavigationDestination.value = null
    }

    fun handleIntent(intent: Intent?) {
        when (intent?.action) {
            "com.atk.atk_cargo.OPEN_WARNINGS" -> {
                _shouldOpenWarningsDialog.value = true
                intent.action = null
            }
        }
        intent?.getStringExtra("navigate_to")?.let {
            _pendingNavigationDestination.value = it
            intent.removeExtra("navigate_to")
        }
    }

    private suspend fun performAppSecurityCheck() {
        _securityCheck.value = _securityCheck.value.copy(isLoading = true)
        try {
            val (securityPassed, errorType) = securityVerifier.verifySecurityStatus()
            _securityCheck.value = SecurityCheckState(
                isPassed = securityPassed,
                isLoading = false,
                errorType = errorType ?: SecurityErrorType.TAMPERED
            )
        } catch (_: Exception) {
            _securityCheck.value = SecurityCheckState(
                isPassed = false,
                isLoading = false,
                errorType = SecurityErrorType.TAMPERED
            )
        }
    }

    private suspend fun checkUserSessionAsync(): Boolean {
        val username = userPreferencesManager.username.first()
        if (username.isEmpty()) return false

        return try {
            val deviceId = userPreferencesManager.deviceId.first()
            val sessionToken = userPreferencesManager.sessionToken.first()
            val sessionRequest = SessionCheckRequest(username, deviceId, sessionToken.takeIf { it.isNotEmpty() })

            val response = RetrofitClient.apiServiceV2.checkSession(sessionRequest)

            if (!response.isSuccessful && response.code() >= 500) {
                isWithinSessionOfflineGracePeriod()
            } else {
                val isValid = response.isSuccessful && response.body()?.success == true
                if (isValid) {
                    userPreferencesManager.saveLastSessionVerifiedTimestamp(System.currentTimeMillis())
                    true
                } else {
                    // I-05: checkSession همیشه HTTP ۲۰۰ برمی‌گرداند (حتی روی شکست، برای
                    // سازگاری با کلاینت قدیمی)، پس وقتی فقط access token منقضی شده
                    // (عمر ۳۰ دقیقه‌ای) اینجا هم isValid=false می‌شود، حتی با یک
                    // refresh token کاملاً معتبر. بدون این تلاش صریح، کاربری که اپ را
                    // بعد از >۳۰ دقیقه دوباره باز می‌کند همیشه به صفحه‌ی ورود می‌رفت —
                    // همان الگوی SessionValidator.kt که این مسیر (چون startup مسیر
                    // جداگانه‌ای است) شاملش نمی‌شد.
                    val refreshed = TokenRefresher.refresh(Secrets.getBaseUrl(), userPreferencesManager) != null
                    if (refreshed) {
                        userPreferencesManager.saveLastSessionVerifiedTimestamp(System.currentTimeMillis())
                    }
                    refreshed
                }
            }
        } catch (e: java.io.IOException) {
            isWithinSessionOfflineGracePeriod()
        } catch (_: Exception) {
            false
        }
    }

    private suspend fun isWithinSessionOfflineGracePeriod(): Boolean {
        val lastVerified = userPreferencesManager.getLastSessionVerifiedTimestamp()
        return lastVerified > 0L &&
                (System.currentTimeMillis() - lastVerified) < SESSION_OFFLINE_GRACE_PERIOD_MS
    }

    private suspend fun requestBatteryOptimizationIfNeeded() {
        try {
            val pm = appContext.getSystemService(Context.POWER_SERVICE) as PowerManager
            if (pm.isIgnoringBatteryOptimizations(appContext.packageName)) return

            // فقط یک بار در طول عمر نصب برنامه از کاربر بپرس — نه در هر راه‌اندازی
            if (userPreferencesManager.hasBatteryOptimizationBeenRequested()) return
            userPreferencesManager.markBatteryOptimizationRequested()

            _events.send(StartupEvent.RequestBatteryOptimization)
        } catch (_: Exception) {
            // نادیده گرفتن خطای مجوز باتری - غیرحیاتی است
        }
    }

    override fun startLoadingNotificationService() {
        viewModelScope.launch {
            if (!userPreferencesManager.loadingNotificationsEnabled.first()) {
                stopLoadingNotificationService()
                return@launch
            }

            val userType = userPreferencesManager.userType.first()
            if (userType == "admin") {
                LoadingNotificationService.startLoadingNotification(appContext)
            } else {
                val intent = Intent(appContext, LoadingNotificationService::class.java)
                intent.action = "STOP_SERVICE"
                appContext.startService(intent)
            }
        }
    }

    override fun stopLoadingNotificationService() {
        val loadingIntent = Intent(appContext, LoadingNotificationService::class.java)
        loadingIntent.action = LoadingNotificationService.ACTION_STOP_SERVICE
        appContext.startService(loadingIntent)
    }

    override fun stopChatNotificationService() {
        WorkManager.getInstance(appContext).cancelUniqueWork("ChatNotificationWorker")
    }

    override fun startChatNotificationWorker() {
        viewModelScope.launch {
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

            WorkManager.getInstance(appContext).enqueueUniquePeriodicWork(
                "ChatNotificationWorker",
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        }
    }

    private fun checkInitialChatMessages() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val enabled = userPreferencesManager.chatNotificationsEnabled.first()
                if (!enabled) return@launch

                val username = userPreferencesManager.username.first()
                if (username.isEmpty()) return@launch

                Log.d("ATK_CHAT_DEBUG", "Startup: Checking initial chat messages...")

                chatRepository.refreshMessages()

                val lastNotifiedId = userPreferencesManager.lastNotifiedMessageId.first()
                val userType = userPreferencesManager.userType.first()

                val allMessages = chatRepository.messages.first()
                val newUnreadMessages = allMessages.filter {
                    it.id > lastNotifiedId && !it.isReadByMe && it.username != username
                }

                if (newUnreadMessages.isNotEmpty()) {
                    Log.d("ATK_CHAT_DEBUG", "Startup: Found ${newUnreadMessages.size} new unread messages")
                    val appNotificationManager = AppNotificationManager(appContext)
                    var maxId = lastNotifiedId

                    newUnreadMessages.sortedBy { it.id }.forEach { msg ->
                        if (msg.id > maxId) maxId = msg.id

                        if (userType == "admin" || msg.message.contains("@$username")) {
                            val hasNotificationPermission = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                                androidx.core.content.ContextCompat.checkSelfPermission(
                                    appContext,
                                    android.Manifest.permission.POST_NOTIFICATIONS
                                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                            } else {
                                true
                            }
                            if (hasNotificationPermission) {
                                withContext(Dispatchers.Main) {
                                    try {
                                        appNotificationManager.showChatNotification(msg.fullName, msg.message)
                                    } catch (se: SecurityException) {
                                        Log.w("ATK_CHAT_DEBUG", "مجوز نوتیفیکیشن رد شد: ${se.message}")
                                    }
                                }
                            }
                        }
                    }

                    userPreferencesManager.saveLastNotifiedMessageId(maxId)
                } else {
                    Log.d("ATK_CHAT_DEBUG", "Startup: No new messages to notify")
                }
            } catch (e: Exception) {
                Log.e("ATK_CHAT_DEBUG", "Error checking initial chat messages", e)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        updateManager.onCleared()
        _events.close()
    }

    companion object {
        private const val SPLASH_MIN_DURATION = 4500L
        private const val SPLASH_MAX_DURATION = 5500L
        private const val SESSION_OFFLINE_GRACE_PERIOD_MS = 3 * 24 * 60 * 60 * 1000L // ۳ روز
    }
}
