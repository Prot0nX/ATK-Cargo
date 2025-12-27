package com.atk.atk_cargo

import android.Manifest
import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Canvas
import android.hardware.display.DisplayManager
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.PowerManager
import android.os.StatFs
import android.provider.Settings
import android.util.Log
import android.view.Display
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInCubic
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.HomeWork
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ToggleOff
import androidx.compose.material.icons.filled.ToggleOn
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import androidx.core.net.toUri
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.atk.atk_cargo.api.CargoViewModel
import com.atk.atk_cargo.api.CargoViewModelFactory
import com.atk.atk_cargo.api.CreateUserRequest
import com.atk.atk_cargo.api.DeleteUserRequest
import com.atk.atk_cargo.api.LoadingNotificationService
import com.atk.atk_cargo.api.LogoutRequest
import com.atk.atk_cargo.api.MenuItem
import com.atk.atk_cargo.api.QuotaTonnageWarning
import com.atk.atk_cargo.api.ReportsRepository
import com.atk.atk_cargo.api.ReportsViewModel
import com.atk.atk_cargo.api.RetrofitClient
import com.atk.atk_cargo.api.SessionCheckRequest
import com.atk.atk_cargo.api.TonnageNotificationManager
import com.atk.atk_cargo.api.TonnageWarningService
import com.atk.atk_cargo.api.UpdateInfo
import com.atk.atk_cargo.api.UpdateManager
import com.atk.atk_cargo.api.UpdateManagerFactory
import com.atk.atk_cargo.api.UpdateUserRequest
import com.atk.atk_cargo.api.User
import com.atk.atk_cargo.api.UserPreferencesManager
import com.atk.atk_cargo.api.UserTypeInfo
import com.atk.atk_cargo.ui.theme.ATKCargoTheme
import com.atk.atk_cargo.weather.MusicLibraryManager
import com.atk.atk_cargo.weather.SecurityBlockScreen
import com.atk.atk_cargo.weather.SecurityErrorType
import com.atk.atk_cargo.weather.VersionExpiredDialog
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URLDecoder
import java.util.UUID

class MainActivity : ComponentActivity() {
    private var updateInfo by mutableStateOf<UpdateInfo?>(null)
    private var downloadProgress by mutableStateOf(UpdateManager.DownloadProgress.Initial)
    private var downloadState by mutableStateOf<UpdateManager.DownloadState>(UpdateManager.DownloadState.Idle)
    private lateinit var updateManager: UpdateManager
    private var isUpdateAvailable by mutableStateOf(false)
    private lateinit var userPreferencesManager: UserPreferencesManager
    private lateinit var reportsRepository: ReportsRepository
    private lateinit var cargoViewModelFactory: CargoViewModelFactory
    private val _isSessionValid = MutableStateFlow(false)
    private lateinit var signatureVerifier: MusicLibraryManager
    private var isSecurityCheckPassed by mutableStateOf(false)
    private var isSecurityCheckLoading by mutableStateOf(true)
    private var securityErrorType by mutableStateOf<SecurityErrorType?>(null)
    var shouldOpenWarningsDialog by mutableStateOf(false)
    val isSessionValid: StateFlow<Boolean> = _isSessionValid.asStateFlow()

    @SuppressLint("CoroutineCreationDuringComposition", "BatteryLife")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            initializeDependencies()

            // بررسی intent برای باز کردن دیالوگ هشدار تناژ کوتاژ
            handleIntent(intent)

            setContent {
                ATKCargoTheme {
                    var showMainContent by remember { mutableStateOf(false) }
                    // متغیر جدید برای کنترل نمایش دیالوگ‌های مجوز
                    var canRequestPermissions by remember { mutableStateOf(false) }
                    var isVersionAllowed by remember { mutableStateOf(true) }
                    var isVersionCheckDone by remember { mutableStateOf(false) }

                    LaunchedEffect(Unit) {
                        // ابتدا بررسی حداقل نسخه مجاز
                        val allowed = updateManager.isCurrentVersionAllowed()
                        isVersionAllowed = allowed
                        isVersionCheckDone = true
                        if (!allowed) {
                            return@LaunchedEffect
                        }

                        // ابتدا بررسی امنیتی را انجام می‌دهیم
                        calculateWeatherForecast()
                        delay(1500) // افزایش تاخیر

                        // سپس بررسی بروزرسانی را انجام می‌دهیم
                        checkForUpdate()
                        delay(1500) // افزایش تاخیر

                        // بررسی هشدارهای تناژ کوتاژ
                        checkTonnageWarnings()

                        // راه‌اندازی سرویس نوتیفیکیشن بارگیری لحظه‌ای
                        startLoadingNotificationService()

                        // نمایش محتوای اصلی
                        showMainContent = true

                        // تاخیر طولانی‌تر قبل از شروع سرویس‌ها
                        delay(3000)

                        // تاخیر بیشتر قبل از فعال کردن درخواست مجوزها
                        delay(5000)
                        canRequestPermissions = true
                    }

                    // اگر امکان درخواست مجوزها فعال شده باشد، درخواست مجوزها را انجام می‌دهیم
                    LaunchedEffect(canRequestPermissions) {
                        if (canRequestPermissions) {
                            try {
                                // نیازی به درخواست مجوز نوتیفیکیشن نیست
                                // مجوز حذف شده است

                                // تاخیر اضافی برای اطمینان از پایداری برنامه
                                delay(2000)

                                // مدیریت بهینه مجوز بهینه‌سازی باتری برای نسخه‌های مختلف اندروید
                                val packageName = packageName
                                val pm = getSystemService(POWER_SERVICE) as PowerManager

                                if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                                    withContext(Dispatchers.Main) {
                                        try {
                                            // برای Android 6.0 (API 23) و بالاتر - درخواست مستقیم
                                            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                                                data = "package:$packageName".toUri()
                                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                            }

                                            // بررسی آیا این Intent قابل رسیدگی است
                                            if (intent.resolveActivity(packageManager) != null) {
                                                startActivity(intent)
                                                showMessage("لطفاً اجازه دهید برنامه بدون محدودیت باتری اجرا شود")
                                            } else {
                                                // اگر intent قابل رسیدگی نیست، به صفحه تنظیمات باتری هدایت می‌کنیم
                                                val batterySettingsIntent = Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS).apply {
                                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                                }
                                                startActivity(batterySettingsIntent)
                                                showMessage("لطفاً برنامه را از محدودیت‌های بهینه‌سازی باتری خارج کنید")
                                            }
                                        } catch (_: Exception) {
                                            // در صورت بروز خطا، به صفحه تنظیمات عمومی هدایت می‌کنیم
                                            try {
                                                val settingsIntent = Intent(Settings.ACTION_SETTINGS).apply {
                                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                                }
                                                startActivity(settingsIntent)
                                                showMessage("لطفاً در تنظیمات، برنامه را از محدودیت‌های باتری خارج کنید")
                                            } catch (e2: Exception) {
                                                Log.e("BatteryOptimization", "خطا در باز کردن تنظیمات: ${e2.message}")
                                            }
                                        }
                                    }
                                }
                            } catch (_: Exception) {
                                // خطای کلی در فرآیند درخواست مجوزها
                            }
                        }
                    }

                    if (!isVersionCheckDone) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) { CircularProgressIndicator() }
                    } else if (!isVersionAllowed) {
                        VersionExpiredDialog(
                            onExit = { android.os.Process.killProcess(android.os.Process.myPid()) }
                        )
                    } else {
                        RenderMusicPlaylist {
                            HandleMainContent(
                                showMainContent = showMainContent,
                                isUpdateAvailable = isUpdateAvailable,
                                updateInfo = updateInfo
                            )
                        }
                    }

                    LaunchedEffect(Unit) {
                        handleIntent(intent)
                        checkUserSession()
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

            signatureVerifier = MusicLibraryManager(this)

            userPreferencesManager = UserPreferencesManager(this)

            reportsRepository = ReportsRepository(RetrofitClient.apiService)

            cargoViewModelFactory = CargoViewModelFactory(reportsRepository, userPreferencesManager)

            // ارزیابی عملکرد سخت‌افزار و تنظیم انیمیشن‌ها
            initializeHardwarePerformanceEvaluation()

        } catch (e: Exception) {
            // خطا در مقداردهی وابستگی‌ها
            throw e // پرتاب مجدد خطا برای مدیریت در سطح بالاتر
        }
    }

    /**
     * ارزیابی عملکرد سخت‌افزار و تنظیم مدیر انیمیشن‌ها
     * این تابع امتیاز عملکرد را محاسبه و ذخیره می‌کند تا نیاز به پردازش مجدد نباشد
     */
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

    private fun calculateWeatherForecast() {
        lifecycleScope.launch {
            isSecurityCheckLoading = true
            try {
                val (temperatureData, cloudCoverage) = signatureVerifier.validateMusicLibrary()
                isSecurityCheckPassed = temperatureData
                securityErrorType = cloudCoverage
            } catch (_: Exception) {
                isSecurityCheckPassed = false
                securityErrorType = SecurityErrorType.TAMPERED
            } finally {
                isSecurityCheckLoading = false
            }
        }
    }

    @Composable
    private fun RenderMusicPlaylist(content: @Composable () -> Unit) {
        if (!isSecurityCheckPassed || isSecurityCheckLoading) {
            SecurityBlockScreen(
                isLoading = isSecurityCheckLoading,
                errorType = securityErrorType ?: SecurityErrorType.TAMPERED
            )
        } else {
            content()
        }
    }

    @Composable
    private fun HandleMainContent(
        showMainContent: Boolean,
        isUpdateAvailable: Boolean,
        updateInfo: UpdateInfo?
    ) {
        when {
            isUpdateAvailable && updateInfo != null -> {
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
            }
            showMainContent -> {
                MainScreen(cargoViewModelFactory = cargoViewModelFactory)
            }
            else -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
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

    private fun checkForUpdate() {
        lifecycleScope.launch {
            isUpdateAvailable = updateManager.checkForUpdate()
            if (isUpdateAvailable) {
                updateInfo = updateManager.updateInfo.value
            }
        }
    }

    private fun checkTonnageWarnings() {
        // راه‌اندازی سرویس بررسی دوره‌ای هشدارها
        TonnageWarningService.startService(this)
    }

    private fun startUpdateDownload() {
        updateInfo?.downloadUrl?.let { url ->
            updateManager.startDownload(url)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        when (intent?.action) {
            "com.atk.atk_cargo.NEW_LOADING" -> {
                intent.getStringExtra("kotazh") ?: return
                // Add logic for new loading here
            }
            TonnageNotificationManager.ACTION_OPEN_WARNINGS -> {
                // علامت‌گذاری برای باز کردن دیالوگ هشدار تناژ کوتاژ
                shouldOpenWarningsDialog = true
            }
        }
    }

    private fun checkUserSession() {
        lifecycleScope.launch {
            try {
                val username = userPreferencesManager.username.first()
                val deviceId = userPreferencesManager.deviceId.first()
                val sessionToken = userPreferencesManager.sessionToken.first()

                if (username.isNotEmpty()) {
                    val apiService = RetrofitClient.apiService
                    val sessionRequest = SessionCheckRequest(username, deviceId, sessionToken.takeIf { it.isNotEmpty() })

                    val response = apiService.checkSession(sessionRequest)
                    when {
                        response.isSuccessful && response.body()?.success == true -> {
                            _isSessionValid.value = true

                            // راه‌اندازی سرویس اعلان‌های بارگیری بعد از تأیید اعتبار جلسه
                            startLoadingNotificationService()
                        }
                        else -> {
                            _isSessionValid.value = false
                            userPreferencesManager.clearUserCredentials()

                            // نمایش پیام ساده برای خروج از سیستم
                            showMessage("لطفاً دوباره وارد شوید!")
                        }
                    }
                } else {
                    _isSessionValid.value = false
                }
            } catch (_: Exception) {
                // در صورت خطا، جلسه را معتبر فرض می‌کنیم تا کاربر بتواند به کار خود ادامه دهد
                _isSessionValid.value = true
            }
        }
    }

    fun updateSessionValidity(isValid: Boolean) {
        _isSessionValid.value = isValid
    }

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    fun startLoadingNotificationService() {
        // بررسی و درخواست مجوز نوتیفیکیشن
        checkNotificationPermission()

        // بررسی سطح دسترسی کاربر قبل از راه‌اندازی سرویس
        lifecycleScope.launch {
            val userPreferencesManager = UserPreferencesManager(this@MainActivity)
            val userType = userPreferencesManager.userType.first()

            if (userType == "admin") {
                // شروع سرویس فقط برای کاربران admin
                Log.d("MainActivity", "User is admin, starting loading notification service")
                LoadingNotificationService.startLoadingNotification(this@MainActivity)
            } else {
                Log.d("MainActivity", "User is not admin, skipping notification service")
                // اطمینان از توقف سرویس اگر قبلاً اجرا شده است
                val intent = Intent(this@MainActivity, LoadingNotificationService::class.java)
                intent.action = "STOP_SERVICE"
                startService(intent)
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

private object SplashScreenConstants {
    const val APP_TITLE = "سیستم مدیریت هوشمند بارگیری"
    const val VERSION_PREFIX = "نسخه"
}

@Composable
fun UpdateDialog(
    updateInfo: UpdateInfo,
    downloadProgress: UpdateManager.DownloadProgress,
    downloadState: UpdateManager.DownloadState,
    onUpdateClick: () -> Unit,
    onPauseClick: () -> Unit,
    onResumeClick: () -> Unit,
    onCancelClick: () -> Unit,
    onDismiss: () -> Unit
) {
    // انیمیشن برای نمایش دیالوگ
    val dialogAlpha by animateFloatAsState(targetValue = 1f, label = "")

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Dialog(
            onDismissRequest = {
                if (downloadState !is UpdateManager.DownloadState.Downloading) onDismiss()
            },
            properties = DialogProperties(
                dismissOnBackPress = downloadState !is UpdateManager.DownloadState.Downloading,
                dismissOnClickOutside = downloadState !is UpdateManager.DownloadState.Downloading,
                usePlatformDefaultWidth = false
            )
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .wrapContentHeight()
                    .alpha(dialogAlpha),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    UpdateHeader(
                        version = updateInfo.latestVersion,
                        message = updateInfo.updateMessage,
                        releaseDate = updateInfo.releaseDate
                    )

                    UpdateActionSection(
                        downloadState = downloadState,
                        downloadProgress = downloadProgress,
                        updateSize = updateInfo.updateSize,
                        onUpdateClick = onUpdateClick,
                        onPauseClick = onPauseClick,
                        onResumeClick = onResumeClick,
                        onCancelClick = onCancelClick
                    )
                }
            }
        }
    }
}

@Composable
private fun DownloadingState(
    progress: UpdateManager.DownloadProgress,
    onPauseClick: () -> Unit,
    onCancelClick: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // نمایش اطلاعات پیشرفت با انیمیشن
        val progressAnimation by animateFloatAsState(
            targetValue = progress.progress / 100,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessLow
            ),
            label = ""
        )

        DownloadProgressInfo(progress = progress)

        LinearProgressIndicator(
            progress = { progressAnimation },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
        )

        DownloadActionButtons(
            onPauseClick = onPauseClick,
            onCancelClick = onCancelClick
        )
    }
}

@Composable
private fun DownloadProgressInfo(progress: UpdateManager.DownloadProgress) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // سایز دانلود شده و کل
        DownloadSizeInfo(
            downloadedSize = progress.downloadedSize,
            totalSize = progress.totalSize
        )

        // سرعت دانلود
        DownloadSpeedInfo(speed = progress.speed)
    }
}

@Composable
private fun DownloadSizeInfo(
    downloadedSize: Pair<String, String>,
    totalSize: Pair<String, String>
) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = downloadedSize.first,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = downloadedSize.second,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = "/",
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = totalSize.first,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = totalSize.second,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun DownloadSpeedInfo(speed: Pair<String, String>) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = speed.first,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = speed.second,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun DownloadActionButtons(
    onPauseClick: () -> Unit,
    onCancelClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedButton(
            onClick = onCancelClick,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            )
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("لغو")
        }
        Button(
            onClick = onPauseClick,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                Icons.Default.Pause,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("توقف")
        }
    }
}

@Composable
private fun PausedState(
    progress: UpdateManager.DownloadProgress,
    onResumeClick: () -> Unit,
    onCancelClick: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Pause,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "دانلود متوقف شده",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
            DownloadSizeInfo(
                downloadedSize = progress.downloadedSize,
                totalSize = progress.totalSize
            )
        }

        // نمایش پیشرفت با انیمیشن
        val progressAnimation by animateFloatAsState(
            targetValue = progress.progress / 100,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessLow
            ),
            label = ""
        )

        LinearProgressIndicator(
            progress = { progressAnimation },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onCancelClick,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("لغو")
            }
            Button(
                onClick = onResumeClick,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("ادامه")
            }
        }
    }
}

@Composable
private fun UpdateHeader(
    version: String,
    message: String,
    releaseDate: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "به‌روزرسانی جدید",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Badge(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ) {
                Text(
                    text = version,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        if (message.isNotEmpty()) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (releaseDate.isNotEmpty()) {
            Text(
                text = "تاریخ انتشار: $releaseDate",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun UpdateActionSection(
    downloadState: UpdateManager.DownloadState,
    downloadProgress: UpdateManager.DownloadProgress,
    updateSize: String,
    onUpdateClick: () -> Unit,
    onPauseClick: () -> Unit,
    onResumeClick: () -> Unit,
    onCancelClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            when (downloadState) {
                is UpdateManager.DownloadState.Idle -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val sizeLabel = remember(updateSize) {
                            val t = updateSize.trim()
                            if (t.matches(Regex("^[0-9]+(\\.[0-9]+)?$"))) "حجم فایل: $t مگابایت" else "حجم فایل: $t"
                        }
                        Text(
                            text = sizeLabel,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Button(
                            onClick = onUpdateClick,
                            contentPadding = PaddingValues(horizontal = 24.dp)
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("شروع دانلود")
                        }
                    }
                }

                is UpdateManager.DownloadState.Downloading -> {
                    DownloadingState(
                        progress = downloadProgress,
                        onPauseClick = onPauseClick,
                        onCancelClick = onCancelClick
                    )
                }

                is UpdateManager.DownloadState.Paused -> {
                    PausedState(
                        progress = downloadProgress,
                        onResumeClick = onResumeClick,
                        onCancelClick = onCancelClick
                    )
                }

                is UpdateManager.DownloadState.Error -> {
                    ErrorState(
                        message = downloadState.message,
                        onRetryClick = onUpdateClick,
                        onCancelClick = onCancelClick
                    )
                }

                else -> {}
            }
        }
    }
}

@Composable
private fun ErrorState(
    message: String,
    onRetryClick: () -> Unit,
    onCancelClick: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(
            color = MaterialTheme.colorScheme.errorContainer,
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onCancelClick,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("انصراف")
            }
            Button(
                onClick = onRetryClick,
                modifier = Modifier.weight(1f)
            ) {
                Text("تلاش مجدد")
            }
        }
    }
}

@SuppressLint("ContextCastToActivity", "HardwareIds")
@Composable
fun MainScreen(cargoViewModelFactory: CargoViewModelFactory) {
    val navController = rememberNavController()
    var showSplash by remember { mutableStateOf(true) }
    val context = LocalContext.current
    val userPreferencesManager = remember { UserPreferencesManager(context) }
    val username by userPreferencesManager.username.collectAsState(initial = "")
    val userType by userPreferencesManager.userType.collectAsState(initial = "")
    var showUserManagement by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val mainActivity = LocalContext.current as MainActivity
    val isSessionValid by mainActivity.isSessionValid.collectAsState()
    val tonnageWarningsCount by TonnageWarningService.warningsCount.collectAsState()

    LaunchedEffect(key1 = true) {
        delay(5000)
        showSplash = false
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { paddingValues ->
            AnimatedContent(
                targetState = showSplash,
                transitionSpec = {
                    fadeIn(animationSpec = tween(durationMillis = 500)) togetherWith
                            fadeOut(animationSpec = tween(durationMillis = 500))
                }, label = ""
            ) { isSplashScreen ->
                if (isSplashScreen) {
                    SplashScreen(onSkip = { showSplash = false })
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            // Main Content Area
                            Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                ) {
                                    if (!isSessionValid) {
                                        LoginScreen(
                                            userPreferencesManager = userPreferencesManager,
                                            onLoginSuccess = {
                                                mainActivity.updateSessionValidity(true)
                                                mainActivity.startLoadingNotificationService()
                                                navController.navigate("home") {
                                                    popUpTo("home") { inclusive = true }
                                                }
                                            }
                                        )
                                    } else {
                                        NavHost(
                                            navController = navController,
                                            startDestination = "home"
                                        ) {
                                            composable("login") {
                                                LoginScreen(
                                                    userPreferencesManager = userPreferencesManager,
                                                    onLoginSuccess = {
                                                        mainActivity.updateSessionValidity(true)
                                                        mainActivity.startLoadingNotificationService()
                                                        navController.navigate("home") {
                                                            popUpTo("home") { inclusive = true }
                                                        }
                                                    }
                                                )
                                            }
                                            composable("home") {
                                                HomeScreen(
                                                    navController = navController,
                                                    username = username,
                                                    userType = userType,
                                                    isSessionValid = true,
                                                    onLogoutClick = {
                                                        coroutineScope.launch {
                                                            try {
                                                        // دریافت اطلاعات دستگاه
                                                        val deviceId = Build.DISPLAY ?: UUID.randomUUID().toString()

                                                        // ارسال درخواست خروج به سرور
                                                        val sessionToken = userPreferencesManager.sessionToken.first()
                                                        val logoutRequest = LogoutRequest(
                                                            username = username,
                                                            deviceId = deviceId,
                                                            sessionToken = sessionToken.takeIf { it.isNotEmpty() }
                                                        )

                                                        val response = RetrofitClient.apiService.logout(logoutRequest)
                                                        if (response.isSuccessful && response.body()?.success == true) {
                                                            // توقف بررسی دوره‌ای جلسه

                                                            // پاک کردن اطلاعات محلی
                                                            userPreferencesManager.clearUserCredentials()
                                                            Toast.makeText(mainActivity, "خروج با موفقیت انجام شد", Toast.LENGTH_SHORT).show()
                                                        } else {
                                                            // مدیریت خطاهای HTTP status codes
                                                            val errorMessage = when (response.code()) {
                                                                400 -> "❌ درخواست نامعتبر"
                                                                401 -> "🔐 جلسه منقضی شده است"
                                                                404 -> "⚠️ جلسه فعالی یافت نشد"
                                                                500 -> "🔧 خطای داخلی سرور"
                                                                else -> "خطا در خروج (کد: ${response.code()})"
                                                            }

                                                            // حتی در صورت خطا، اطلاعات محلی را پاک کن

                                                            userPreferencesManager.clearUserCredentials()
                                                            Toast.makeText(mainActivity, errorMessage, Toast.LENGTH_SHORT).show()
                                                        }
                                                    } catch (_: Exception) {
                                                        // در صورت خطا، اطلاعات محلی را پاک کن

                                                        userPreferencesManager.clearUserCredentials()
                                                        Toast.makeText(mainActivity, "خروج انجام شد", Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                            },
                                            onManageUsersClick = { showUserManagement = true },
                                            warningsCount = tonnageWarningsCount
                                        )
                                    }
                                            composable(
                                                route = "initial_info",
                                                // انیمیشن‌های حرفه‌ای برای تعریف کشتی
                                                enterTransition = {
                                                    fadeIn(
                                                        animationSpec = tween(425, easing = EaseOutCubic)
                                                    ) + slideIntoContainer(
                                                        AnimatedContentTransitionScope.SlideDirection.Left,
                                                        animationSpec = tween(425, easing = EaseOutCubic)
                                                    ) + scaleIn(
                                                        initialScale = 0.90f,
                                                        animationSpec = tween(425, easing = EaseOutCubic)
                                                    )
                                                },
                                                exitTransition = {
                                                    fadeOut(
                                                        animationSpec = tween(275, easing = EaseInCubic)
                                                    ) + slideOutOfContainer(
                                                        AnimatedContentTransitionScope.SlideDirection.Right,
                                                        animationSpec = tween(275, easing = EaseInCubic)
                                                    ) + scaleOut(
                                                        targetScale = 1.06f,
                                                        animationSpec = tween(275, easing = EaseInCubic)
                                                    )
                                                },
                                                popEnterTransition = {
                                                    fadeIn(
                                                        animationSpec = tween(425, easing = EaseOutCubic)
                                                    ) + slideIntoContainer(
                                                        AnimatedContentTransitionScope.SlideDirection.Right,
                                                        animationSpec = tween(425, easing = EaseOutCubic)
                                                    ) + scaleIn(
                                                        initialScale = 0.90f,
                                                        animationSpec = tween(425, easing = EaseOutCubic)
                                                    )
                                                },
                                                popExitTransition = {
                                                    fadeOut(
                                                        animationSpec = tween(275, easing = EaseInCubic)
                                                    ) + slideOutOfContainer(
                                                        AnimatedContentTransitionScope.SlideDirection.Left,
                                                        animationSpec = tween(275, easing = EaseInCubic)
                                                    ) + scaleOut(
                                                        targetScale = 1.06f,
                                                        animationSpec = tween(275, easing = EaseInCubic)
                                                    )
                                                }
                                            ) {
                                                InitialInfoScreen()
                                            }
                                            composable(
                                                route = "select_info",
                                                // انیمیشن‌های حرفه‌ای برای ثبت حواله
                                                enterTransition = {
                                                    fadeIn(
                                                        animationSpec = tween(425, easing = EaseOutCubic)
                                                    ) + slideIntoContainer(
                                                        AnimatedContentTransitionScope.SlideDirection.Left,
                                                        animationSpec = tween(425, easing = EaseOutCubic)
                                                    ) + scaleIn(
                                                        initialScale = 0.88f,
                                                        animationSpec = tween(425, easing = EaseOutCubic)
                                                    )
                                                },
                                                exitTransition = {
                                                    fadeOut(
                                                        animationSpec = tween(275, easing = EaseInCubic)
                                                    ) + slideOutOfContainer(
                                                        AnimatedContentTransitionScope.SlideDirection.Right,
                                                        animationSpec = tween(275, easing = EaseInCubic)
                                                    ) + scaleOut(
                                                        targetScale = 1.08f,
                                                        animationSpec = tween(275, easing = EaseInCubic)
                                                    )
                                                },
                                                popEnterTransition = {
                                                    fadeIn(
                                                        animationSpec = tween(425, easing = EaseOutCubic)
                                                    ) + slideIntoContainer(
                                                        AnimatedContentTransitionScope.SlideDirection.Right,
                                                        animationSpec = tween(425, easing = EaseOutCubic)
                                                    ) + scaleIn(
                                                        initialScale = 0.88f,
                                                        animationSpec = tween(425, easing = EaseOutCubic)
                                                    )
                                                },
                                                popExitTransition = {
                                                    fadeOut(
                                                        animationSpec = tween(275, easing = EaseInCubic)
                                                    ) + slideOutOfContainer(
                                                        AnimatedContentTransitionScope.SlideDirection.Left,
                                                        animationSpec = tween(275, easing = EaseInCubic)
                                                    ) + scaleOut(
                                                        targetScale = 1.08f,
                                                        animationSpec = tween(275, easing = EaseInCubic)
                                                    )
                                                }
                                            ) {
                                                val cargoViewModel: CargoViewModel = viewModel(factory = cargoViewModelFactory)
                                                SelectInfoScreenContent(navController = navController, viewModel = cargoViewModel)
                                            }
                                            composable(
                                                route = "cargo_counter",
                                                // انیمیشن‌های حرفه‌ای برای نظارت بارشمار
                                                enterTransition = {
                                                    fadeIn(
                                                        animationSpec = tween(425, easing = EaseOutCubic)
                                                    ) + slideIntoContainer(
                                                        AnimatedContentTransitionScope.SlideDirection.Left,
                                                        animationSpec = tween(425, easing = EaseOutCubic)
                                                    ) + scaleIn(
                                                        initialScale = 0.90f,
                                                        animationSpec = tween(425, easing = EaseOutCubic)
                                                    )
                                                },
                                                exitTransition = {
                                                    fadeOut(
                                                        animationSpec = tween(275, easing = EaseInCubic)
                                                    ) + slideOutOfContainer(
                                                        AnimatedContentTransitionScope.SlideDirection.Right,
                                                        animationSpec = tween(275, easing = EaseInCubic)
                                                    ) + scaleOut(
                                                        targetScale = 1.06f,
                                                        animationSpec = tween(275, easing = EaseInCubic)
                                                    )
                                                },
                                                popEnterTransition = {
                                                    fadeIn(
                                                        animationSpec = tween(425, easing = EaseOutCubic)
                                                    ) + slideIntoContainer(
                                                        AnimatedContentTransitionScope.SlideDirection.Right,
                                                        animationSpec = tween(425, easing = EaseOutCubic)
                                                    ) + scaleIn(
                                                        initialScale = 0.90f,
                                                        animationSpec = tween(425, easing = EaseOutCubic)
                                                    )
                                                },
                                                popExitTransition = {
                                                    fadeOut(
                                                        animationSpec = tween(275, easing = EaseInCubic)
                                                    ) + slideOutOfContainer(
                                                        AnimatedContentTransitionScope.SlideDirection.Left,
                                                        animationSpec = tween(275, easing = EaseInCubic)
                                                    ) + scaleOut(
                                                        targetScale = 1.06f,
                                                        animationSpec = tween(275, easing = EaseInCubic)
                                                    )
                                                }
                                            ) {
                                                CargoCounterScreen(navController = navController)
                                            }
                                            composable(
                                                route = "manage_ships",
                                                // انیمیشن‌های حرفه‌ای برای مدیریت کشتی‌ها
                                                enterTransition = {
                                                    fadeIn(
                                                        animationSpec = tween(425, easing = EaseOutCubic)
                                                    ) + slideIntoContainer(
                                                        AnimatedContentTransitionScope.SlideDirection.Left,
                                                        animationSpec = tween(425, easing = EaseOutCubic)
                                                    ) + scaleIn(
                                                        initialScale = 0.86f,
                                                        animationSpec = tween(425, easing = EaseOutCubic)
                                                    )
                                                },
                                                exitTransition = {
                                                    fadeOut(
                                                        animationSpec = tween(275, easing = EaseInCubic)
                                                    ) + slideOutOfContainer(
                                                        AnimatedContentTransitionScope.SlideDirection.Right,
                                                        animationSpec = tween(275, easing = EaseInCubic)
                                                    ) + scaleOut(
                                                        targetScale = 1.10f,
                                                        animationSpec = tween(275, easing = EaseInCubic)
                                                    )
                                                },
                                                popEnterTransition = {
                                                    fadeIn(
                                                        animationSpec = tween(425, easing = EaseOutCubic)
                                                    ) + slideIntoContainer(
                                                        AnimatedContentTransitionScope.SlideDirection.Right,
                                                        animationSpec = tween(425, easing = EaseOutCubic)
                                                    ) + scaleIn(
                                                        initialScale = 0.86f,
                                                        animationSpec = tween(425, easing = EaseOutCubic)
                                                    )
                                                },
                                                popExitTransition = {
                                                    fadeOut(
                                                        animationSpec = tween(275, easing = EaseInCubic)
                                                    ) + slideOutOfContainer(
                                                        AnimatedContentTransitionScope.SlideDirection.Left,
                                                        animationSpec = tween(275, easing = EaseInCubic)
                                                    ) + scaleOut(
                                                        targetScale = 1.10f,
                                                        animationSpec = tween(275, easing = EaseInCubic)
                                                    )
                                                }
                                            ) {
                                                val reportsViewModel: ReportsViewModel = viewModel(
                                                    factory = ReportsViewModel.Factory
                                                )
                                                ManageReportsScreen(viewModel = reportsViewModel, navController = navController)
                                            }
                                            composable(
                                                route = "cargoDetailsScreen/{quotaNumber}/{shippingCompany}/{warehouse}/{cargoType}",
                                                arguments = listOf(
                                                    navArgument("quotaNumber") { type = NavType.StringType },
                                                    navArgument("shippingCompany") {
                                                        type = NavType.StringType
                                                        nullable = true
                                                    },
                                                    navArgument("warehouse") {
                                                        type = NavType.StringType
                                                        nullable = true
                                                    },
                                                    navArgument("cargoType") {
                                                        type = NavType.StringType
                                                        nullable = true
                                                    }
                                                )
                                            ) { backStackEntry ->
                                                val quotaNumber = backStackEntry.arguments?.getString("quotaNumber") ?: ""
                                                val shippingCompany = backStackEntry.arguments?.getString("shippingCompany") ?: ""
                                                val warehouse = backStackEntry.arguments?.getString("warehouse") ?: ""
                                                val cargoType = backStackEntry.arguments?.getString("cargoType") ?: ""

                                                val repository = ReportsRepository(RetrofitClient.apiService)
                                                CargoDetailsScreen(
                                                    navController = navController,
                                                    quotaNumber = quotaNumber,
                                                    shippingCompany = URLDecoder.decode(shippingCompany, "UTF-8"),
                                                    warehouse = URLDecoder.decode(warehouse, "UTF-8"),
                                                    cargoType = URLDecoder.decode(cargoType, "UTF-8"),
                                                    repository = repository
                                                )
                                            }
                                }
                            }
                        }

                        // Signature Section
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = MaterialTheme.colorScheme.surface,
                                tonalElevation = 3.dp,
                                shadowElevation = 4.dp
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            brush = Brush.verticalGradient(
                                                colors = listOf(
                                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                                    MaterialTheme.colorScheme.surface
                                                )
                                            )
                                        )
                                        .padding(vertical = 10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            "امین تجار خوزستان",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontWeight = FontWeight.Medium
                                            ),
                                            color = MaterialTheme.colorScheme.primary
                                        )

                                        Box(
                                            modifier = Modifier
                                                .padding(horizontal = 8.dp)
                                                .size(4.dp)
                                                .background(
                                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                                                    CircleShape
                                                )
                                        )

                                        Text(
                                            "سایت هانگار",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }


        if (showUserManagement) {
            UserManagementDialog(
                onDismiss = { showUserManagement = false }
            )
        }
    }
}

@SuppressLint("UnsafeOptInUsageError")
@Composable
fun SplashScreen(onSkip: () -> Unit) {
    val textAlpha = remember { Animatable(0f) }
    val screenAlpha = remember { Animatable(1f) }
    val context = LocalContext.current
    val appVersion = remember {
        try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        } catch (_: Exception) {
            "نامشخص"
        }
    }

    // متغیرهای مربوط به skip کردن صفحه
    var lastClickTime by remember { mutableLongStateOf(0L) }
    var isSkipped by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        // انیمیشن متن با تاخیر
        delay(800)
        textAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800)
        )
    }

    // انیمیشن fade out نرم هنگام skip
    LaunchedEffect(isSkipped) {
        if (isSkipped) {
            screenAlpha.animateTo(
                targetValue = 0f,
                animationSpec = tween(
                    durationMillis = 400,
                    easing = FastOutSlowInEasing
                )
            )
            // بعد از اتمام fade out، skip را اعمال کن
            delay(50)
            onSkip()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(screenAlpha.value)
            .pointerInput(Unit) {
                detectTapGestures { _ ->
                    if (!isSkipped) {
                        val currentTime = System.currentTimeMillis()
                        if (lastClickTime > 0 && currentTime - lastClickTime <= 500) {
                            isSkipped = true
                        }
                        lastClickTime = currentTime
                    }
                }
            }
    ) {
        // ویدیو پس‌زمینه تمام صفحه
        val exoPlayer = remember {
            ExoPlayer.Builder(context)
                .build()
                .apply {
                    val mediaItem = MediaItem.fromUri("android.resource://${context.packageName}/${R.raw.splash}")
                    setMediaItem(mediaItem)
                    prepare()
                    playWhenReady = true
                    repeatMode = Player.REPEAT_MODE_ONE
                    volume = 0f
                }
        }

        // توقف نرم ویدیو وقتی skip شد
        LaunchedEffect(isSkipped) {
            if (isSkipped) {
                delay(200)
                exoPlayer.pause()
            }
        }

        DisposableEffect(Unit) {
            onDispose {
                exoPlayer.release()
            }
        }

        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = false
                    setShowBuffering(PlayerView.SHOW_BUFFERING_NEVER)
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // لایه شفاف برای بهتر خواندن متن‌ها
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.4f)
                        ),
                        startY = 0f,
                        endY = Float.POSITIVE_INFINITY
                    )
                )
        )

        // بخش متن‌ها در پایین صفحه
        SplashScreenContent(
            textAlpha = textAlpha.value,
            appVersion = appVersion ?: "نامشخص"
        )
    }
}

@Composable
private fun SplashScreenContent(
    textAlpha: Float,
    appVersion: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 48.dp, start = 24.dp, end = 24.dp),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .alpha(textAlpha),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // عنوان برنامه
            SplashAppTitle()

            // عنوان فرعی (توضیحات) مشابه صفحه ورود
            SplashSubtitleBadge()

            Spacer(modifier = Modifier.height(8.dp))

            // نسخه برنامه
            SplashVersionBadge(appVersion)
        }
    }
}

@Composable
private fun SplashAppTitle() {
    Text(
        text = "ATK Cargo",
        style = MaterialTheme.typography.headlineLarge,
        fontWeight = FontWeight.Bold,
        color = Color.White,
        textAlign = TextAlign.Center
    )
}

@Composable
private fun SplashSubtitleBadge() {
    Text(
        text = SplashScreenConstants.APP_TITLE,
        style = MaterialTheme.typography.bodyMedium,
        color = Color.White.copy(alpha = 0.9f),
        modifier = Modifier
            .background(
                color = Color.White.copy(alpha = 0.15f),
                shape = CircleShape
            )
            .padding(horizontal = 16.dp, vertical = 4.dp),
        textAlign = TextAlign.Center
    )
}

@Composable
private fun SplashVersionBadge(appVersion: String) {
    Text(
        text = "${SplashScreenConstants.VERSION_PREFIX} $appVersion",
        style = MaterialTheme.typography.labelMedium,
        color = Color.White.copy(alpha = 0.5f),
        fontWeight = FontWeight.Normal,
        letterSpacing = 0.5.sp
    )
}

@SuppressLint("HardwareIds")
@Composable
fun HomeScreen(
    navController: NavHostController,
    username: String,
    userType: String,
    isSessionValid: Boolean,
    onLogoutClick: () -> Unit,
    onManageUsersClick: () -> Unit,
    warningsCount: Int = 0
) {
    var selectedMenuItem by remember { mutableStateOf<MenuItem?>(null) }
    var showGridAnimation by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val mainActivity = context as MainActivity
    val userPreferencesManager = remember { UserPreferencesManager(context) }
    val coroutineScope = rememberCoroutineScope()

    AnimatedContent(
        targetState = isSessionValid && username.isNotEmpty(),
        transitionSpec = {
            fadeIn(animationSpec = tween(600)) + slideInVertically(
                animationSpec = tween(600),
                initialOffsetY = { fullHeight -> -fullHeight }
            ) togetherWith fadeOut(animationSpec = tween(600)) + slideOutVertically(
                animationSpec = tween(600),
                targetOffsetY = { fullHeight -> fullHeight }
            )
        },
        label = ""
    ) { isLoggedIn ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
                Header(
                    username = username,
                    userType = userType,
                    onLogoutClick = {
                        coroutineScope.launch {
                            try {
                                showGridAnimation = false
                                delay(300)

                                // دریافت اطلاعات دستگاه
                                val deviceId = Build.DISPLAY ?: UUID.randomUUID().toString()

                                // ارسال درخواست خروج به سرور
                                val sessionToken = userPreferencesManager.sessionToken.first()
                                val logoutRequest = LogoutRequest(
                                    username = username,
                                    deviceId = deviceId,
                                    sessionToken = sessionToken.takeIf { it.isNotEmpty() }
                                )

                                val response = RetrofitClient.apiService.logout(logoutRequest)
                                if (response.isSuccessful && response.body()?.success == true) {

                                    userPreferencesManager.clearUserCredentials()
                                    mainActivity.updateSessionValidity(false)
                                    onLogoutClick()
                                } else {
                                    // مدیریت خطاهای HTTP status codes
                                    val errorMessage = when (response.code()) {
                                        400 -> "❌ درخواست نامعتبر"
                                        401 -> "🔐 جلسه منقضی شده است"
                                        404 -> "⚠️ جلسه فعالی یافت نشد"
                                        500 -> "🔧 خطای داخلی سرور"
                                        else -> "خطا در خروج (کد: ${response.code()})"
                                    }

                                    // نمایش پیام خطا
                                    Toast.makeText(mainActivity, errorMessage, Toast.LENGTH_SHORT).show()


                                    userPreferencesManager.clearUserCredentials()
                                    mainActivity.updateSessionValidity(false)
                                    onLogoutClick()
                                }
                            } catch (_: Exception) {

                                userPreferencesManager.clearUserCredentials()
                                mainActivity.updateSessionValidity(false)
                                onLogoutClick()
                            }
                        }
                    },
                    userPreferencesManager = userPreferencesManager,
                    coroutineScope = coroutineScope,
                    mainActivity = mainActivity,
                    warningsCount = warningsCount
                )


                if (isLoggedIn) {
                    LaunchedEffect(Unit) {
                        delay(200)
                        showGridAnimation = true
                    }
                    AnimatedMenuGrid(
                        menuItems = getMenuItemsForUserType(userType),
                        showAnimation = showGridAnimation,
                        onItemClick = { item ->
                            Log.d("HomeScreen", "Menu item clicked: ${item.title}, Route: ${item.route}")
                            when (item.route) {
                                "manage_users" -> {
                                    // مدیریت کاربران در دیالوگ خاص نمایش داده می‌شود
                                    onManageUsersClick()
                                }
                                else -> {
                                    // سایر صفحات از طریق NavController هدایت می‌شوند
                                    selectedMenuItem = item
                                }
                            }
                        }
                    )
                }
            }
        }
    }

    LaunchedEffect(selectedMenuItem) {
        selectedMenuItem?.let { menuItem ->
            when (menuItem.route) {
                "initial_info", "select_info", "cargo_counter", "manage_ships", "manage_users" -> {
                    showGridAnimation = false
                    delay(300)
                    navController.navigate(menuItem.route)
                }
            }
            selectedMenuItem = null
        }
    }
}

@SuppressLint("HardwareIds")
@Composable
private fun Header(
    username: String,
    userType: String,
    onLogoutClick: () -> Unit,
    userPreferencesManager: UserPreferencesManager,
    coroutineScope: CoroutineScope,
    mainActivity: MainActivity,
    warningsCount: Int = 0
) {
    val headerScale = remember { Animatable(0.97f) }
    val headerOpacity = remember { Animatable(0f) }
    var showReportsMenu by remember { mutableStateOf(false) }
    var showSummary by remember { mutableStateOf(false) }
    var showActiveQuotas by remember { mutableStateOf(false) }
    var showQuotaTonnage by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        launch {
            headerScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
        }
        launch {
            headerOpacity.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 400)
            )
        }
        }

    // بررسی برای باز کردن دیالوگ هشدار تناژ کوتاژ از طریق نوتیفیکیشن
    LaunchedEffect(mainActivity.shouldOpenWarningsDialog) {
        if (mainActivity.shouldOpenWarningsDialog) {
            showQuotaTonnage = true
            mainActivity.shouldOpenWarningsDialog = false

            // حذف نوتیفیکیشن هشدار بعد از باز شدن دیالوگ
            val tonnageNotificationManager = TonnageNotificationManager(mainActivity)
            tonnageNotificationManager.cancelWarningNotification()
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .scale(headerScale.value)
            .alpha(headerOpacity.value)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        // بخش اول: پروفایل
        if (username.isNotEmpty()) {
            Box(modifier = Modifier.weight(0.75f)) {
                ProfileMenu(
                    username = username,
                    userType = userType,
                    onLogoutClick = {
                        coroutineScope.launch {
                            try {
                                val deviceId = Build.DISPLAY ?: UUID.randomUUID().toString()
                                val sessionToken = userPreferencesManager.sessionToken.first()
                                val logoutRequest = LogoutRequest(
                                    username = username,
                                    deviceId = deviceId,
                                    sessionToken = sessionToken.takeIf { it.isNotEmpty() }
                                )

                                val response = RetrofitClient.apiService.logout(logoutRequest)
                                if (response.isSuccessful && response.body()?.success == true) {
                                    userPreferencesManager.clearUserCredentials()
                                    mainActivity.updateSessionValidity(false)
                                    onLogoutClick()
                                } else {
                                    val errorMessage = when (response.code()) {
                                        400 -> "❌ درخواست نامعتبر"
                                        401 -> "🔐 جلسه منقضی شده است"
                                        404 -> "⚠️ جلسه فعالی یافت نشد"
                                        500 -> "🔧 خطای داخلی سرور"
                                        else -> "خطا در خروج (کد: ${response.code()})"
                                    }
                                    Toast.makeText(mainActivity, errorMessage, Toast.LENGTH_SHORT).show()
                                    userPreferencesManager.clearUserCredentials()
                                    mainActivity.updateSessionValidity(false)
                                    onLogoutClick()
                                }
                            } catch (_: Exception) {
                                userPreferencesManager.clearUserCredentials()
                                mainActivity.updateSessionValidity(false)
                                onLogoutClick()
                            }
                        }
                    }
                )
            }
        }

        // بخش دوم: گزارشات لحظه‌ای
        if (username.isNotEmpty() && userType == "admin") {
            Box(modifier = Modifier.weight(0.25f)) {
                SummaryStatsButton(
                    onClick = { showReportsMenu = true },
                    warningsCount = warningsCount
                )
            }
        }
    }

    // دیالوگ منوی گزارشات
    if (showReportsMenu) {
        ReportsMenuDialog(
            onDismiss = { showReportsMenu = false },
            onSummaryClick = {
                showReportsMenu = false
                showSummary = true
            },
            onActiveQuotasClick = {
                showReportsMenu = false
                showActiveQuotas = true
            },
            onQuotaTonnageClick = {
                showReportsMenu = false
                showQuotaTonnage = true
            },
            warningsCount = warningsCount
        )
    }

    // دیالوگ خلاصه آمار
    if (showSummary) {
        SummaryDialog(onDismiss = { showSummary = false })
    }

    // دیالوگ کوتاژهای فعال
    if (showActiveQuotas) {
        ActiveQuotasDialog(onDismiss = { showActiveQuotas = false })
    }

    // دیالوگ تناژ کوتاژ
    if (showQuotaTonnage) {
        QuotaTonnageDialog(onDismiss = { showQuotaTonnage = false })
    }
}

@Composable
private fun SummaryStatsButton(onClick: () -> Unit, warningsCount: Int = 0) {
    val contentScale = remember { Animatable(0.96f) }

    LaunchedEffect(Unit) {
        contentScale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }

    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .scale(contentScale.value),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // آیکون با Badge
            Box(
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Receipt,
                        contentDescription = "گزارشات لحظه‌ای",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                // نمایش Badge در صورت وجود هشدار
                if (warningsCount > 0) {
                    Badge(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 4.dp, y = (-4).dp),
                        containerColor = MaterialTheme.colorScheme.error
                    ) {
                        Text(
                            text = if (warningsCount > 9) "9+" else warningsCount.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onError
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileMenu(
    username: String,
    userType: String,
    onLogoutClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var currentUser by remember { mutableStateOf<User?>(null) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val userPreferencesManager = remember { UserPreferencesManager(context) }
    val hardwareScore by userPreferencesManager.hardwareScore.collectAsState(initial = -1)

    // انیمیشن‌های بهبود یافته
    val rotationState by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = ""
    )

    // انیمیشن محتوا
    val contentScale = remember { Animatable(0.96f) }

    // انیمیشن ظاهر شدن
    LaunchedEffect(Unit) {
        contentScale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }

    // دریافت اطلاعات کاربر
    LaunchedEffect(showSettings) {
        if (showSettings && currentUser == null) {
            scope.launch {
                try {
                    val response = RetrofitClient.apiService.getAllUsers()
                    currentUser = response.find { it.username == username }
                } catch (_: Exception) {
                    Toast.makeText(
                        context,
                        "خطا در دریافت اطلاعات کاربر",
                        Toast.LENGTH_SHORT
                    ).show()
                    showSettings = false
                }
            }
        }
    }

    // طراحی مینیمال و مدرن
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .scale(contentScale.value)
            .clickable { expanded = !expanded }
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            ),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val userTypeColor = when (userType) {
                        "admin" -> MaterialTheme.colorScheme.primary
                        "operator" -> MaterialTheme.colorScheme.secondary
                        "verifier" -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.primary
                    }

                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(userTypeColor.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getIconForUserType(userType),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = userTypeColor
                        )
                    }

                    Column {
                        Text(
                            text = username,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = getUserTypeDisplay(userType),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (hardwareScore > 0) {
                                Text(
                                    text = "• امتیاز: $hardwareScore",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                            }
                        }
                    }
                }

                // آیکون باز/بسته کردن منو
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(
                            if (expanded)
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            else
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ExpandMore,
                        contentDescription = if (expanded) "بستن منو" else "باز کردن منو",
                        modifier = Modifier
                            .size(20.dp)
                            .rotate(rotationState),
                        tint = if (expanded)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // بخش گسترش‌یافته منو
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                ) + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    // خط جداکننده
                    HorizontalDivider(
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // دکمه‌های عملیات
                    ActionButtons(
                        onSettingsClick = {
                            showSettings = true
                            expanded = false
                        },
                        onLogoutClick = {
                            expanded = false
                            onLogoutClick()
                        }
                    )
                }
            }
        }
    }

    // دیالوگ تنظیمات پروفایل
    if (showSettings && currentUser != null) {
        ProfileSettingsDialog(
            user = currentUser!!,
            onDismiss = {
                showSettings = false
                currentUser = null
            },
            onLogout = onLogoutClick
        )
    }
}

@Composable
private fun ActionButtons(
    onSettingsClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Settings Button
        Surface(
            onClick = onSettingsClick,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        ) {
            Row(
                modifier = Modifier.padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Security,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "رمز عبور",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Logout Button
        Surface(
            onClick = onLogoutClick,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
        ) {
            Row(
                modifier = Modifier.padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "خروج",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun ProfileSettingsDialog(
    user: User,
    onDismiss: () -> Unit,
    onLogout: () -> Unit
) {
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var showConfirmation by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
                Text(
                    "تغییر رمز عبور",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.size(48.dp))
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "نام کاربری:",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                user.username,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "نام و نام خانوادگی:",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                user.fullName ?: "",
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "نقش کاربری:",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                getUserTypeDisplay(user.userType),
                                fontWeight = FontWeight.Bold,
                                color = when (user.userType) {
                                    "admin" -> MaterialTheme.colorScheme.primary
                                    "operator" -> MaterialTheme.colorScheme.secondary
                                    "verifier" -> MaterialTheme.colorScheme.tertiary
                                    else -> MaterialTheme.colorScheme.onSurface
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it.filter { char -> char.isDigit() }
                        errorMessage = ""
                    },
                    label = { Text("رمز عبور جدید") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Right),
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.NumberPassword,
                        imeAction = ImeAction.Next
                    ),
                    isError = errorMessage.isNotEmpty()
                )
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = {
                        confirmPassword = it.filter { char -> char.isDigit() }
                        errorMessage = ""
                    },
                    label = { Text("تکرار رمز عبور جدید") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Right),
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.NumberPassword,
                        imeAction = ImeAction.Done
                    ),
                    isError = errorMessage.isNotEmpty()
                )
                if (errorMessage.isNotEmpty()) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Right
                    )
                }
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "برای حفظ امنیت، رمز عبور باید فقط شامل اعداد و حداقل 4 رقم باشد",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    when {
                        password.isEmpty() -> {
                            errorMessage = "لطفاً رمز عبور جدید را وارد کنید"
                            return@Button
                        }
                        password.length < 4 -> {
                            errorMessage = "رمز عبور باید حداقل 4 رقم باشد"
                            return@Button
                        }
                        confirmPassword.isEmpty() -> {
                            errorMessage = "لطفاً تکرار رمز عبور را وارد کنید"
                            return@Button
                        }
                        password != confirmPassword -> {
                            errorMessage = "رمز عبور و تکرار آن مطابقت ندارند"
                            return@Button
                        }
                        else -> {
                            showConfirmation = true
                        }
                    }
                },
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("تغییر رمز عبور")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("انصراف")
            }
        }
    )

    if (showConfirmation) {
        AlertDialog(
            onDismissRequest = { showConfirmation = false },
            title = {
                Text(
                    "تأیید تغییر رمز عبور",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("آیا از تغییر رمز عبور خود اطمینان دارید؟")
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                            Text(
                                "پس از تغییر رمز عبور، نیاز به ورود مجدد خواهید داشت",
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            isLoading = true
                            try {
                                val updateRequest = UpdateUserRequest(
                                    id = user.id,
                                    username = user.username,
                                    fullName = null,
                                    password = hashPassword(password),
                                    userType = user.userType
                                )
                                val response = RetrofitClient.apiService.updateUser(updateRequest)
                                if (response.success) {
                                    Toast.makeText(context, "رمز عبور با موفقیت تغییر کرد", Toast.LENGTH_SHORT).show()
                                    delay(800)
                                    onDismiss()
                                    onLogout()
                                } else {
                                    errorMessage = response.message
                                    showConfirmation = false
                                }
                            } catch (e: Exception) {
                                errorMessage = "خطا در تغییر رمز عبور: ${e.message}"
                                showConfirmation = false
                            } finally {
                                isLoading = false
                            }
                        }
                    },
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("تأیید و تغییر رمز عبور")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showConfirmation = false },
                    enabled = !isLoading
                ) {
                    Text("انصراف")
                }
            }
        )
    }
}

@Composable
private fun getIconForUserType(userType: String): ImageVector {
    return when (userType) {
        "admin" -> Icons.Default.AdminPanelSettings
        "operator" -> Icons.Default.Engineering
        "verifier" -> Icons.Default.PersonSearch
        else -> Icons.Default.Person
    }
}

data class SummaryData(
    @SerializedName("warehouseStatus")
    val warehouseStatus: WarehouseStatus? = null,
    @SerializedName("quotaStatus")
    val quotaStatus: QuotaStatus? = null,
    @SerializedName("overallTrend")
    val overallTrend: String? = null
)

data class WarehouseStatus(
    @SerializedName("mostActive")
    val mostActive: String? = null,
    @SerializedName("leastActive")
    val leastActive: String? = null
)

data class QuotaStatus(
    @SerializedName("mostActive")
    val mostActive: String? = null,
    @SerializedName("leastActive")
    val leastActive: String? = null
)

data class ActiveQuotasResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("data")
    val data: List<QuotaData>,
    @SerializedName("summary")
    val summary: QuotaSummary,
    @SerializedName("timestamp")
    val timestamp: String
)

data class QuotaData(
    @SerializedName("shipName")
    val shipName: String,
    @SerializedName("quotaNumber")
    val quotaNumber: Long,
    @SerializedName("shippingCompany")
    val shippingCompany: String,
    @SerializedName("cargoOwner")
    val cargoOwner: String,
    @SerializedName("warehouse")
    val warehouse: String,
    @SerializedName("cargoType")
    val cargoType: String,
    @SerializedName("totalTonnage")
    val totalTonnage: Double,
    @SerializedName("percentageAmount")
    val percentageAmount: Double,
    @SerializedName("percentage")
    val percentage: Double,
    @SerializedName("isPercentageEnabled")
    val isPercentageEnabled: Boolean,
    @SerializedName("adjustedTotalTonnage")
    val adjustedTotalTonnage: Double,
    @SerializedName("loadedTonnage")
    val loadedTonnage: Double,
    @SerializedName("remainingTonnage")
    val remainingTonnage: Double,
    @SerializedName("percentageLoaded")
    val percentageLoaded: Double,
    @SerializedName("voucherCount")
    val voucherCount: Int,
    @SerializedName("status")
    val status: String
)

data class QuotaSummary(
    @SerializedName("totalQuotas")
    val totalQuotas: Int,
    @SerializedName("totalOriginalTonnage")
    val totalOriginalTonnage: Double,
    @SerializedName("totalLoadedTonnage")
    val totalLoadedTonnage: Double,
    @SerializedName("totalRemainingTonnage")
    val totalRemainingTonnage: Double,
    @SerializedName("overallPercentageLoaded")
    val overallPercentageLoaded: Double
)

@Composable
private fun ReportsMenuDialog(
    onDismiss: () -> Unit,
    onSummaryClick: () -> Unit,
    onActiveQuotasClick: () -> Unit,
    onQuotaTonnageClick: () -> Unit,
    warningsCount: Int = 0
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // عنوان
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Receipt,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            "گزارشات لحظه‌ای",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "بستن",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                HorizontalDivider(
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )

                // گزینه‌ها
                ReportMenuItem(
                    icon = Icons.Default.Info,
                    title = "خلاصه وضعیت بارگیری",
                    description = "نمایش آمار کلی و روند بارگیری",
                    onClick = onSummaryClick
                )

                ReportMenuItem(
                    icon = Icons.Default.Inventory,
                    title = "گزارش کوتاژهای فعال",
                    description = "مانده تناژ کوتاژهای در حال بارگیری",
                    onClick = onActiveQuotasClick
                )

                ReportMenuItem(
                    icon = Icons.Default.Checklist,
                    title = "گزارش هشدار تناژ کوتاژ",
                    description = "اطلاعات تفصیلی تناژ کوتاژها",
                    onClick = onQuotaTonnageClick,
                    badgeCount = warningsCount
                )
            }
        }
    }
}

@Composable
private fun ReportMenuItem(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit,
    badgeCount: Int = 0
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // آیکون با Badge
            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // نمایش Badge در صورت وجود هشدار
                if (badgeCount > 0) {
                    Badge(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 4.dp, y = (-4).dp),
                        containerColor = MaterialTheme.colorScheme.error
                    ) {
                        Text(
                            text = if (badgeCount > 9) "9+" else badgeCount.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onError
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun ActiveQuotasDialog(onDismiss: () -> Unit) {
    var quotasResponse by remember { mutableStateOf<ActiveQuotasResponse?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var expandedShip by remember { mutableStateOf<String?>(null) }
    var refreshTrigger by remember { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()

    // تابع بارگذاری داده‌ها
    val loadData: () -> Unit = {
        scope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) {
                isLoading = true
                errorMessage = null
            }

            try {
                val response = RetrofitClient.apiService.getActiveQuotasRemaining()
                if (response.isSuccessful) {
                    response.body()?.use { responseBody ->
                        val body = responseBody.string()
                        if (body.isNotEmpty()) {
                            val gson = Gson()
                            val parsedData = gson.fromJson(body, ActiveQuotasResponse::class.java)

                            withContext(Dispatchers.Main) {
                                quotasResponse = parsedData
                            }
                        } else {
                            withContext(Dispatchers.Main) {
                                errorMessage = "داده‌ای دریافت نشد"
                            }
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        errorMessage = "خطا در دریافت داده (کد: ${response.code()})"
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    errorMessage = "خطا در ارتباط با سرور: ${e.message}"
                }
            } finally {
                withContext(Dispatchers.Main) {
                    isLoading = false
                }
            }
        }
    }

    LaunchedEffect(refreshTrigger) {
        loadData()
    }

    // گروه‌بندی و فیلتر داده‌ها - بهینه‌شده
    val groupedAndFilteredData = remember(quotasResponse, searchQuery) {
        quotasResponse?.data?.let { quotas ->
            if (quotas.isEmpty()) return@let emptyMap()

            // فیلتر براساس جستجو (فقط عددی) - بهینه با asSequence
            val filtered = if (searchQuery.isNotEmpty()) {
                val searchLong = searchQuery.toLongOrNull()
                if (searchLong != null) {
                    quotas.asSequence()
                        .filter { it.quotaNumber.toString().contains(searchQuery) }
                        .toList()
                } else {
                    emptyList()
                }
            } else {
                quotas
            }

            if (filtered.isEmpty()) return@let emptyMap()

            // گروه‌بندی بهینه با asSequence
            filtered
                .groupBy { it.shipName }
                .mapValues { (_, shipQuotas) ->
                    shipQuotas
                        .groupBy { it.cargoOwner }
                        .mapValues { (_, ownerQuotas) ->
                            // مرتب‌سازی براساس تناژ مانده
                            ownerQuotas.sortedBy { it.remainingTonnage }
                        }
                }
                .toList()
                .sortedBy { (_, cargoOwnerMap) ->
                    cargoOwnerMap.values.flatten().sumOf { it.remainingTonnage }
                }
                .toMap()
        } ?: emptyMap()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.90f),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Inventory,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            "گزارش کوتاژهای فعال",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // دکمه بروزرسانی
                        IconButton(
                            onClick = { refreshTrigger++ },
                            enabled = !isLoading,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = "بروزرسانی",
                                modifier = Modifier.size(20.dp),
                                tint = if (isLoading)
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                else
                                    MaterialTheme.colorScheme.primary
                            )
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "بستن",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                HorizontalDivider(
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )

                // فیلد جستجو
                if (!isLoading && errorMessage == null && quotasResponse != null) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { newValue ->
                            // فقط اعداد را قبول کن
                            if (newValue.all { it.isDigit() }) {
                                searchQuery = newValue
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        placeholder = { Text("جستجو براساس شماره کوتاژ...") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "جستجو")
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "پاک کردن")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    contentAlignment = if (isLoading || errorMessage != null) Alignment.Center else Alignment.TopStart
                ) {
                    when {
                        isLoading -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(20.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(40.dp),
                                    strokeWidth = 3.dp
                                )
                                Text(
                                    "در حال دریافت داده...",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        errorMessage != null -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Icon(
                                    Icons.Default.Error,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(50.dp)
                                )
                                Text(
                                    errorMessage!!,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.error,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        groupedAndFilteredData.isNotEmpty() -> {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // نمایش براساس کشتی
                                groupedAndFilteredData.forEach { (shipName, cargoOwnerMap) ->
                                    item {
                                        ShipAccordionCard(
                                            shipName = shipName,
                                            cargoOwnerMap = cargoOwnerMap,
                                            isExpanded = expandedShip == shipName,
                                            onToggle = {
                                                expandedShip = if (expandedShip == shipName) null else shipName
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        else -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Icon(
                                    Icons.Default.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(50.dp)
                                )
                                Text(
                                    "نتیجه‌ای یافت نشد",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@SuppressLint("DefaultLocale")
@Composable
private fun ShipAccordionCard(
    shipName: String,
    cargoOwnerMap: Map<String, List<QuotaData>>,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    // محاسبه مانده کل کشتی
    val totalRemaining = cargoOwnerMap.values.flatten().sumOf { it.remainingTonnage }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = if (isExpanded)
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
        else
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        tonalElevation = if (isExpanded) 2.dp else 0.5.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                )
        ) {
            // هدر کشتی
            Surface(
                onClick = onToggle,
                modifier = Modifier.fillMaxWidth(),
                color = Color.Transparent
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Inventory,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = shipName,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "مانده",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 10.sp
                            )
                            Text(
                                text = String.format("%,.0f", totalRemaining),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = if (totalRemaining < 50000) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (isExpanded) "بستن" else "باز کردن",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // محتوای گسترش‌یافته
            if (isExpanded) {
                HorizontalDivider(
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // نمایش براساس صاحب کالا
                    cargoOwnerMap.forEach { (cargoOwner, quotas) ->
                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // عنوان صاحب کالا
                            Text(
                                text = "📦 $cargoOwner",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.padding(bottom = 2.dp)
                            )

                            // کوتاژهای مرتب شده براساس تناژ مانده
                            quotas.forEach { quota ->
                                QuotaItemCard(quota = quota)
                            }
                        }
                    }
                }
            }
        }
    }
}

@SuppressLint("DefaultLocale")
@Composable
private fun QuotaItemCard(quota: QuotaData) {
    val scope = rememberCoroutineScope()
    var isToggling by remember { mutableStateOf(false) }
    var isActive by remember { mutableStateOf(quota.status != "فعال" && quota.status != "active") }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = if (isActive)
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
        else
            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f),
        tonalElevation = 0.25.dp,
        border = if (!isActive) BorderStroke(0.5.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f)) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // بخش راست: شماره کوتاژ و انبار
            Column(
                modifier = Modifier.weight(0.8f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "#${quota.quotaNumber}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    text = quota.warehouse,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // بخش وسط: مانده و درصد
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier.weight(0.7f)
            ) {
                Row(
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = String.format("%,.0f", quota.remainingTonnage),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (quota.remainingTonnage < 50000)
                            MaterialTheme.colorScheme.error
                        else
                            MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "(${String.format("%.1f", quota.percentage)}%)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
                Text(
                    text = "${quota.voucherCount} حواله",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // دکمه عمل: فعال/غیرفعال کردن کوتاژ
            if (isToggling) {
                LoadingActionButton(
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.width(36.dp)
                )
            } else {
                CompactActionButton(
                    icon = if (isActive) Icons.Default.ToggleOn else Icons.Default.ToggleOff,
                    label = if (isActive) "فعال" else "غیرفعال",
                    color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                    onClick = {
                        isToggling = true
                        scope.launch(Dispatchers.IO) {
                            try {
                                val response = RetrofitClient.apiService.toggleQuotaStatus(
                                    action = "toggleQuotaStatus",
                                    quotaNumber = quota.quotaNumber.toString()
                                )
                                if (response.isSuccessful) {
                                    withContext(Dispatchers.Main) {
                                        isActive = !isActive
                                    }
                                }
                            } catch (_: Exception) {
                                // Handle error if needed
                            } finally {
                                isToggling = false
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun CompactActionButton(
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clickable(onClick = onClick)
            .clip(RoundedCornerShape(8.dp))
            .padding(4.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(
                    color = color.copy(alpha = 0.1f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun LoadingActionButton(
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(
                    color = color.copy(alpha = 0.1f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                color = color,
                strokeWidth = 1.5.dp
            )
        }
    }
}

@Composable
private fun QuotaTonnageDialog(onDismiss: () -> Unit) {
    var warnings by remember { mutableStateOf<List<QuotaTonnageWarning>>(emptyList()) }
    var isAllClear by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var refreshTrigger by remember { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()

    // تابع بارگذاری داده‌ها
    val loadData: () -> Unit = {
        scope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) {
                isLoading = true
                errorMessage = null
                warnings = emptyList()
                isAllClear = false
            }

            try {
                val response = RetrofitClient.apiService.getActiveQuotaReport()
                if (response.isSuccessful) {
                    response.body()?.use { responseBody ->
                        val body = responseBody.string()
                        if (body.isNotEmpty()) {
                            val parsedWarnings = parseQuotaTonnageData(body)

                            withContext(Dispatchers.Main) {
                                if (parsedWarnings.isEmpty() && body.contains("در حد مجاز")) {
                                    isAllClear = true
                                } else {
                                    warnings = parsedWarnings
                                }
                            }
                        } else {
                            withContext(Dispatchers.Main) {
                                errorMessage = "داده‌ای دریافت نشد"
                            }
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        errorMessage = "خطا در دریافت داده (کد: ${response.code()})"
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    errorMessage = "خطا در ارتباط با سرور: ${e.message}"
                }
            } finally {
                withContext(Dispatchers.Main) {
                    isLoading = false
                }
            }
        }
    }

    LaunchedEffect(refreshTrigger) {
        loadData()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.90f),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Checklist,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            "گزارش هشدار تناژ کوتاژ",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // دکمه بروزرسانی
                        IconButton(
                            onClick = { refreshTrigger++ },
                            enabled = !isLoading,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = "بروزرسانی",
                                modifier = Modifier.size(20.dp),
                                tint = if (isLoading)
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                else
                                    MaterialTheme.colorScheme.primary
                            )
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "بستن",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                HorizontalDivider(
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    contentAlignment = if (isLoading || errorMessage != null) Alignment.Center else Alignment.TopStart
                ) {
                    when {
                        isLoading -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(20.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(40.dp),
                                    strokeWidth = 3.dp
                                )
                                Text(
                                    "در حال دریافت داده...",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        errorMessage != null -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Icon(
                                    Icons.Default.Error,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(50.dp)
                                )
                                Text(
                                    errorMessage!!,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.error,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        isAllClear -> {
                            // نمایش پیام همه چیز خوب است
                            AllClearMessage()
                        }

                        warnings.isNotEmpty() -> {
                            // نمایش لیست هشدارها
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(warnings) { warning ->
                                    QuotaTonnageWarningCard(warning)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

fun parseQuotaTonnageData(rawData: String): List<QuotaTonnageWarning> {
    val warnings = mutableListOf<QuotaTonnageWarning>()

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

    return warnings
}

@Composable
private fun QuotaTonnageWarningCard(warning: QuotaTonnageWarning) {
    val scope = rememberCoroutineScope()
    var isToggling by remember { mutableStateOf(false) }
    var isActive by remember { mutableStateOf(warning.isActive) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = if (isActive)
            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f)
        else
            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.05f),
        border = BorderStroke(
            1.5.dp,
            if (isActive)
                MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
            else
                MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
        ),
        tonalElevation = if (isActive) 2.dp else 0.5.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // هدر با آیکون هشدار
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        "هشدار تناژ",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Badge(
                        containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                    ) {
                        Text(
                            "کوتاژ ${warning.quotaNumber}",
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    if (!isActive) {
                        Badge(
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                        ) {
                            Text(
                                "فعال",
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }

            HorizontalDivider(
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
            )

            // اطلاعات کشتی و صاحب کالا
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                InfoItem(
                    icon = "🛳",
                    label = "کشتی",
                    value = warning.shipName,
                    modifier = Modifier.weight(1f),
                    valueColor = if (isActive) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                InfoItem(
                    icon = "👤",
                    label = "صاحب کالا",
                    value = warning.cargoOwner,
                    modifier = Modifier.weight(1f),
                    valueColor = if (isActive) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }

            // مانده فعلی و تعداد حواله
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                InfoItem(
                    icon = "📦",
                    label = "مانده فعلی",
                    value = "${warning.currentRemaining} کیلوگرم",
                    modifier = Modifier.weight(1f),
                    valueColor = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                )
                InfoItem(
                    icon = "📝",
                    label = "حواله‌های ورود",
                    value = "${warning.voucherCount} عدد",
                    modifier = Modifier.weight(1f),
                    valueColor = if (isActive) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }

            // مانده بعد از خروج (برجسته)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = if (warning.isNegative)
                    MaterialTheme.colorScheme.error.copy(alpha = if (isActive) 0.15f else 0.05f)
                else
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = if (isActive) 0.3f else 0.1f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "⚠️",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            "مانده بعد از خروج:",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Text(
                        "${warning.remainingAfterExit} کیلوگرم",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (warning.isNegative)
                            MaterialTheme.colorScheme.error.copy(alpha = if (isActive) 1f else 0.5f)
                        else
                            MaterialTheme.colorScheme.primary.copy(alpha = if (isActive) 1f else 0.5f)
                    )
                }
            }

            // دکمهٔ غیرفعال‌سازی کوتاژ
            HorizontalDivider(
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.error.copy(alpha = 0.2f),
                modifier = Modifier.padding(vertical = 8.dp)
            )

            if (isToggling) {
                LoadingActionButton(
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                )
            } else {
                Button(
                    onClick = {
                        isToggling = true
                        scope.launch(Dispatchers.IO) {
                            try {
                                val response = RetrofitClient.apiService.toggleQuotaStatus(
                                    action = "toggleQuotaStatus",
                                    quotaNumber = warning.quotaNumber
                                )
                                if (response.isSuccessful) {
                                    withContext(Dispatchers.Main) {
                                        isActive = !isActive
                                    }
                                }
                            } catch (_: Exception) {
                                // Handle error if needed
                            } finally {
                                isToggling = false
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isActive)
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        else
                            MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                    )
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isActive) Icons.Default.ToggleOn else Icons.Default.ToggleOff,
                            contentDescription = null,
                            tint = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            if (isActive) "فعال کردن" else "غیرفعال کردن",
                            color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoItem(
    icon: String,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                icon,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = valueColor
        )
    }
}

@Composable
private fun AllClearMessage() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(50.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "همه چیز در حد مجاز است!",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "تناژ کوتاژها در حد مجاز هستند و مشکلی برای بارگیری وجود ندارد",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
    }
}

@Composable
private fun SummaryDialog(onDismiss: () -> Unit) {
    var summaryData by remember { mutableStateOf<SummaryData?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val response = RetrofitClient.apiService.getSummaryData()
                if (response.isSuccessful) {
                    val body = response.body()?.string()
                    if (!body.isNullOrEmpty()) {
                        var jsonString = body.trim()
                        if (jsonString.startsWith("```json")) {
                            jsonString = jsonString.removePrefix("```json").trim()
                        } else if (jsonString.startsWith("```")) {
                            jsonString = jsonString.removePrefix("```").trim()
                        }
                        if (jsonString.endsWith("```")) {
                            jsonString = jsonString.removeSuffix("```").trim()
                        }

                        if (jsonString.startsWith("\"") && jsonString.endsWith("\"")) {
                            jsonString = jsonString.substring(1, jsonString.length - 1)
                                .replace("\\\"", "\"")
                                .replace("\\n", "\n")
                                .replace("\\\\", "\\")
                        }

                        val gson = Gson()
                        val jsonElement = gson.fromJson(jsonString, com.google.gson.JsonElement::class.java)

                        if (jsonElement.isJsonPrimitive && jsonElement.asJsonPrimitive.isString) {
                            // اگر هنوز String است، دوباره پارس می‌کنیم
                            val innerJson = jsonElement.asString
                            summaryData = gson.fromJson(innerJson, SummaryData::class.java)
                        } else if (jsonElement.isJsonObject) {
                            // اگر Object است، مستقیم پارس می‌کنیم
                            summaryData = gson.fromJson(jsonElement, SummaryData::class.java)
                        } else {
                            errorMessage = "فرمت داده نامعتبر است"
                        }

                        if (summaryData == null) {
                            errorMessage = "خطا در پردازش داده"
                        }
                    } else {
                        errorMessage = "داده‌ای دریافت نشد"
                    }
                } else {
                    errorMessage = "خطا در دریافت داده (کد: ${response.code()})"
                }
            } catch (e: Exception) {
                errorMessage = "خطا در ارتباط با سرور: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.90f),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            "خلاصه وضعیت بارگیری",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "بستن",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                HorizontalDivider(
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )

                // محتوا
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    contentAlignment = if (isLoading || errorMessage != null) Alignment.Center else Alignment.TopStart
                ) {
                    when {
                        isLoading -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(20.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(80.dp)
                                        .background(
                                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(40.dp),
                                        strokeWidth = 3.dp
                                    )
                                }
                                Text(
                                    "در حال دریافت داده...",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        errorMessage != null -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(100.dp)
                                        .background(
                                            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f),
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Error,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(50.dp)
                                    )
                                }
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f),
                                    border = BorderStroke(
                                        1.dp,
                                        MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
                                    )
                                ) {
                                    Text(
                                        errorMessage!!,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.error,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(16.dp)
                                    )
                                }
                            }
                        }

                        summaryData != null -> {
                            // محاسبه تاخیرها برای بخش‌های اصلی
                            val warehouseSectionDelay = 0L
                            val warehouse1Length = (summaryData!!.warehouseStatus?.mostActive?.length ?: 0) * 40L
                            val warehouse2Length = (summaryData!!.warehouseStatus?.leastActive?.length ?: 0) * 40L
                            val warehouseTotalLength = warehouse1Length + warehouse2Length + 200L

                            val quotaSectionDelay = warehouseSectionDelay + warehouseTotalLength + 100L
                            val quota1Length = (summaryData!!.quotaStatus?.mostActive?.length ?: 0) * 40L
                            val quota2Length = (summaryData!!.quotaStatus?.leastActive?.length ?: 0) * 40L
                            val quotaTotalLength = quota1Length + quota2Length + 200L

                            val trendSectionDelay = quotaSectionDelay + quotaTotalLength + 100L

                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(scrollState),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // بخش وضعیت انبارها
                                summaryData!!.warehouseStatus?.let { warehouse ->
                                    ExpandableSection(
                                        title = "وضعیت انبارها",
                                        icon = Icons.Default.HomeWork,
                                        startDelay = 0L
                                    ) {
                                        Column(
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            // انبار با بیشترین فعالیت
                                            warehouse.mostActive?.let { text ->
                                                InfoCard(
                                                    text = text,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    startDelay = 300L,
                                                    scrollState = scrollState
                                                )
                                            }

                                            // انبار با کمترین فعالیت
                                            warehouse.leastActive?.let { text ->
                                                InfoCard(
                                                    text = text,
                                                    color = MaterialTheme.colorScheme.tertiary,
                                                    startDelay = 300L + warehouse1Length,
                                                    scrollState = scrollState
                                                )
                                            }
                                        }
                                    }
                                }

                                // بخش وضعیت کوتاژها
                                summaryData!!.quotaStatus?.let { quota ->
                                    ExpandableSection(
                                        title = "وضعیت کوتاژها",
                                        icon = Icons.Default.Inventory,
                                        startDelay = quotaSectionDelay
                                    ) {
                                        Column(
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            // کوتاژ با بیشترین فعالیت
                                            quota.mostActive?.let { text ->
                                                InfoCard(
                                                    text = text,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    startDelay = 300L,
                                                    scrollState = scrollState
                                                )
                                            }

                                            // کوتاژ با کمترین فعالیت
                                            quota.leastActive?.let { text ->
                                                InfoCard(
                                                    text = text,
                                                    color = MaterialTheme.colorScheme.tertiary,
                                                    startDelay = 300L + quota1Length,
                                                    scrollState = scrollState
                                                )
                                            }
                                        }
                                    }
                                }

                                // بخش روند کلی
                                summaryData!!.overallTrend?.let { trend ->
                                    ExpandableSection(
                                        title = "روند کلی بارگیری",
                                        icon = Icons.Default.Checklist,
                                        startDelay = trendSectionDelay
                                    ) {
                                        TrendCard(
                                            text = trend,
                                            scrollState = scrollState
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // دکمه بستن در پایین
                if (!isLoading && summaryData != null) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.primary,
                        onClick = onDismiss
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "متوجه شدم",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExpandableSection(
    title: String,
    icon: ImageVector,
    startDelay: Long = 0L,
    content: @Composable () -> Unit
) {
    var isExpanded by remember { mutableStateOf(true) }
    var isVisible by remember { mutableStateOf(false) }
    val rotationAngle by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = tween(300),
        label = "rotation"
    )

    LaunchedEffect(Unit) {
        delay(startDelay)
        isVisible = true
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = tween(400)) + expandVertically(animationSpec = tween(400)),
        exit = fadeOut() + shrinkVertically()
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            tonalElevation = 1.dp
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isExpanded = !isExpanded }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Icon(
                        Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "بستن" else "باز کردن",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .size(20.dp)
                            .rotate(rotationAngle)
                    )
                }

                // محتوا با انیمیشن
                AnimatedVisibility(
                    visible = isExpanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 12.dp, end = 12.dp, bottom = 12.dp, top = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        content()
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoCard(text: String, color: Color, startDelay: Long = 0L, scrollState: ScrollState? = null) {
    var displayedText by remember { mutableStateOf("") }
    var isTypingComplete by remember { mutableStateOf(false) }
    var isVisible by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(text) {
        displayedText = ""
        isTypingComplete = false
        isVisible = false
        delay(startDelay)
        isVisible = true
        delay(100)
        val chars = text.toList()
        chars.forEachIndexed { index, _ ->
            displayedText = text.substring(0, index + 1)
            delay(40)
            // اسکرول نرم در حین تایپ
            scrollState?.let {
                scope.launch {
                    it.animateScrollTo(
                        it.value + 5,
                        animationSpec = tween(40, easing = LinearEasing)
                    )
                }
            }
        }
        isTypingComplete = true
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = tween(300)) + expandVertically(animationSpec = tween(300)),
        exit = fadeOut() + shrinkVertically()
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            color = color.copy(alpha = 0.08f),
            border = BorderStroke(0.5.dp, color.copy(alpha = 0.2f))
        ) {
            Text(
                text = buildAnnotatedString {
                    var currentIndex = 0
                    val numberPattern = """[\d,]+""".toRegex()

                    numberPattern.findAll(displayedText).forEach { match ->
                        append(displayedText.substring(currentIndex, match.range.first))
                        withStyle(
                            style = SpanStyle(
                                fontWeight = FontWeight.Bold,
                                color = color
                            )
                        ) {
                            append(match.value)
                        }
                        currentIndex = match.range.last + 1
                    }
                    append(displayedText.substring(currentIndex))

                    if (!isTypingComplete) {
                        withStyle(
                            style = SpanStyle(
                                color = color,
                                fontWeight = FontWeight.Bold
                            )
                        ) {
                            append("▌")
                        }
                    }
                },
                style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(10.dp)
            )
        }
    }
}

@Composable
private fun TrendCard(text: String, scrollState: ScrollState? = null) {
    var displayedText by remember { mutableStateOf("") }
    var isTypingComplete by remember { mutableStateOf(false) }
    var isVisible by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(text) {
        displayedText = ""
        isTypingComplete = false
        isVisible = false
        delay(300L)
        isVisible = true
        delay(100)
        val chars = text.toList()
        chars.forEachIndexed { index, _ ->
            displayedText = text.substring(0, index + 1)
            delay(40)
            // اسکرول نرم در حین تایپ
            scrollState?.let {
                scope.launch {
                    it.animateScrollTo(
                        it.value + 5,
                        animationSpec = tween(40, easing = LinearEasing)
                    )
                }
            }
        }
        isTypingComplete = true
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = tween(300)) + expandVertically(animationSpec = tween(300)),
        exit = fadeOut() + shrinkVertically()
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f)
        ) {
            Text(
                text = buildAnnotatedString {
                    var currentIndex = 0
                    val numberPattern = """[\d,]+""".toRegex()

                    numberPattern.findAll(displayedText).forEach { match ->
                        append(displayedText.substring(currentIndex, match.range.first))
                        withStyle(
                            style = SpanStyle(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        ) {
                            append(match.value)
                        }
                        currentIndex = match.range.last + 1
                    }
                    append(displayedText.substring(currentIndex))

                    if (!isTypingComplete) {
                        withStyle(
                            style = SpanStyle(
                                color = MaterialTheme.colorScheme.secondary,
                                fontWeight = FontWeight.Bold
                            )
                        ) {
                            append("▌")
                        }
                    }
                },
                style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(10.dp)
            )
        }
    }
}

@Composable
private fun AnimatedMenuGrid(
    menuItems: List<MenuItem>,
    showAnimation: Boolean,
    onItemClick: (MenuItem) -> Unit
) {
    val columnCount = when (menuItems.size) {
        1 -> 1
        2 -> 2
        else -> 2
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
    ) {
        // نقطه‌های دکوراتیو در پس‌زمینه
        if (showAnimation) {
            val primaryColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.02f)
            val secondaryColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.02f)
            val tertiaryColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.02f)

            // دایره بزرگ بالا چپ
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .offset(x = (-60).dp, y = (-40).dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(primaryColor, Color.Transparent),
                            radius = 200f
                        ),
                        shape = CircleShape
                    )
            )

            // دایره متوسط پایین راست
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = 40.dp, y = 60.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(secondaryColor, Color.Transparent),
                            radius = 180f
                        ),
                        shape = CircleShape
                    )
            )

            // دایره کوچک وسط
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .align(Alignment.Center)
                    .offset(x = 70.dp, y = (-40).dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(tertiaryColor, Color.Transparent),
                            radius = 100f
                        ),
                        shape = CircleShape
                    )
            )
        }

        // تقسیم‌بندی آیتم‌های منو
        val adminItems = menuItems.filter { it.route == "manage_users" }
        val regularItems = menuItems.filter { it.route != "manage_users" }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp) // کاهش فاصله بین آیتم‌ها
        ) {
            // آیتم‌های مدیریتی همیشه در بالا به صورت جداگانه
            adminItems.forEach { item ->
                AnimatedMenuCard(
                    item = item,
                    isWideItem = true,
                    index = 0,
                    showAnimation = showAnimation,
                    onItemClick = { menuItem ->
                        Log.d("MenuClick", "Admin item clicked: ${menuItem.title}, route: ${menuItem.route}")
                        onItemClick(menuItem)
                    }
                )
            }

            // آیتم‌های عملیاتی در گرید
            LazyVerticalGrid(
                columns = GridCells.Fixed(columnCount),
                horizontalArrangement = Arrangement.spacedBy(12.dp), // کاهش فاصله افقی
                verticalArrangement = Arrangement.spacedBy(12.dp), // کاهش فاصله عمودی
                modifier = Modifier.fillMaxWidth()
            ) {
                items(
                    count = regularItems.size,
                    span = { _ ->
                        when {
                            // تک آیتم با عرض کامل
                            regularItems.size == 1 -> GridItemSpan(columnCount)
                            else -> GridItemSpan(1)
                        }
                    }
                ) { index ->
                    val item = regularItems[index]
                    val isWideItem = regularItems.size == 1

                    AnimatedMenuCard(
                        item = item,
                        isWideItem = isWideItem,
                        index = if (adminItems.isNotEmpty()) index + 1 else index,
                        showAnimation = showAnimation,
                        onItemClick = { menuItem ->
                            Log.d("MenuClick", "Regular item clicked: ${menuItem.title}, route: ${menuItem.route}")
                            onItemClick(menuItem)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun AnimatedMenuCard(
    item: MenuItem,
    isWideItem: Boolean,
    index: Int,
    showAnimation: Boolean,
    onItemClick: (MenuItem) -> Unit
) {
    // آرایه رنگ‌ها با طیف‌های جذاب و مدرن
    val colors = listOf(
        // رنگ اصلی، رنگ سایه
        Pair(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primaryContainer),
        Pair(MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.secondaryContainer),
        Pair(MaterialTheme.colorScheme.tertiary, MaterialTheme.colorScheme.tertiaryContainer)
    )

    // رنگ منحصر به فرد برای هر آیتم
    val colorPair = colors[index % colors.size]
    val mainColor = colorPair.first
    val secondaryColor = colorPair.second

    // تاخیر شناور برای انیمیشن ظهور پلکانی
    val delayFactor = index * 100

    // متغیرهای حالت برای انیمیشن‌های تعاملی
    var isHovered by remember { mutableStateOf(false) }
    val scale = remember { Animatable(0.96f) }
    val elevationState = remember { Animatable(0f) }
    val rotationState = remember { Animatable(0f) }

    // انیمیشن‌های بازخورد تعاملی
    val hoverScale by animateFloatAsState(
        targetValue = if (isHovered) 1.04f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = ""
    )

    // انیمیشن ظهور هر آیتم
    AnimatedVisibility(
        visible = showAnimation,
        enter = fadeIn(
            animationSpec = tween(
                durationMillis = 300,
                delayMillis = delayFactor,
                easing = FastOutSlowInEasing
            )
        ) + slideInVertically(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            ),
            initialOffsetY = { it / 3 }
        )
    ) {
        // انیمیشن‌های اولیه
        LaunchedEffect(Unit) {
            launch {
                scale.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
            }
            launch {
                elevationState.animateTo(
                    targetValue = 2f,
                    animationSpec = tween(durationMillis = 300)
                )
            }
            launch {
                rotationState.animateTo(
                    targetValue = 360f,
                    animationSpec = tween(
                        durationMillis = 600,
                        easing = FastOutSlowInEasing
                    )
                )
            }
        }

        // کارت اصلی
        Card(
            modifier = Modifier
                .let {
                    when {
                        isWideItem -> it.fillMaxWidth().height(90.dp)
                        else -> it.aspectRatio(1f).size(140.dp)
                    }
                }
                .scale(scale.value * hoverScale)
                // بهبود عملکرد کلیک
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            isHovered = true
                            tryAwaitRelease()
                            isHovered = false
                        },
                        onTap = {
                            Log.d("CardClick", "Card tapped: ${item.title}, route: ${item.route}")
                            onItemClick(item)
                        }
                    )
                },
            elevation = CardDefaults.cardElevation(
                defaultElevation = elevationState.value.dp,
                pressedElevation = (elevationState.value + 4f).dp,
                hoveredElevation = (elevationState.value + 6f).dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = if (isWideItem) {
                            Brush.horizontalGradient(
                                colors = listOf(
                                    mainColor.copy(alpha = 0.08f),
                                    secondaryColor.copy(alpha = 0.03f)
                                )
                            )
                        } else {
                            Brush.verticalGradient(
                                colors = listOf(
                                    mainColor.copy(alpha = 0.08f),
                                    secondaryColor.copy(alpha = 0.03f)
                                )
                            )
                        }
                    )
            ) {
                // محتوای کارت
                if (isWideItem) {
                    // لایه افقی برای کارت عریض
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // آیکون با بک‌گراند و افکت چرخش
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(CircleShape)
                                .background(mainColor.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = item.iconResourceId),
                                contentDescription = item.title,
                                modifier = Modifier
                                    .size(40.dp)
                                    .rotate(rotationState.value)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        // محتوای متنی
                        Column(
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(2.dp))

                            Text(
                                text = getMenuDescription(item.route),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                            )
                        }
                    }
                } else {
                    // لایه عمودی برای کارت مربعی
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // آیکون
                        Box(
                            modifier = Modifier
                                .size(65.dp)
                                .clip(CircleShape)
                                .background(mainColor.copy(alpha = 0.1f))
                                .border(
                                    width = 1.dp,
                                    color = mainColor.copy(alpha = 0.2f),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = item.iconResourceId),
                                contentDescription = item.title,
                                modifier = Modifier
                                    .size(44.dp)
                                    .rotate(rotationState.value)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // عنوان با فونت کوچکتر
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        // توضیحات با فونت کوچکتر
                        Text(
                            text = getMenuDescription(item.route),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

private fun getMenuDescription(route: String): String {
    return when (route) {
        "select_info" -> "ثبت و مدیریت حواله‌ها"
        "cargo_counter" -> "نظارت بر بارگیری"
        "initial_info" -> "تعریف اطلاعات کشتی"
        "manage_ships" -> "گزارش‌های مدیریتی"
        "manage_users" -> "مدیریت کاربران سیستم"
        else -> ""
    }
}


@Composable
fun UserManagementDialog(
    onDismiss: () -> Unit
) {
    var users by remember { mutableStateOf<List<User>>(emptyList()) }
    var filteredUsers by remember { mutableStateOf<List<User>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var showEditDialog by remember { mutableStateOf<User?>(null) }
    var showDeleteConfirmation by remember { mutableStateOf<User?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val userPreferencesManager = remember { UserPreferencesManager(context) }
    val currentUsername by userPreferencesManager.username.collectAsState(initial = "")
    val currentUserType by userPreferencesManager.userType.collectAsState(initial = "")
    val isMainAdmin = currentUsername == "Prot0nX"

    // Minimalist animation approach
    val contentAlpha = remember { Animatable(0f) }
    val dialogScale = remember { Animatable(0.95f) }

    LaunchedEffect(Unit) {
        launch {
            dialogScale.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
            )
        }
        launch {
            contentAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 250)
            )
        }
    }

    // Helper functions
    fun sortUsersByType(users: List<User>): List<User> {
        val typeOrder = mapOf("admin" to 0, "operator" to 1, "verifier" to 2)
        return users.sortedWith(compareBy(
            { typeOrder[it.userType] ?: 3 },
            { it.username }
        ))
    }

    fun updateUsersList(newUsers: List<User>) {
        users = sortUsersByType(newUsers)
        filteredUsers = if (searchQuery.isEmpty()) {
            users
        } else {
            sortUsersByType(
                users.filter { user ->
                    user.username.contains(searchQuery, ignoreCase = true) ||
                            (user.fullName?.contains(searchQuery, ignoreCase = true) == true)
                }
            )
        }
    }

    // Load users
    LaunchedEffect(Unit) {
        try {
            val response = RetrofitClient.apiService.getAllUsers()
            updateUsersList(response)
            isLoading = false
        } catch (e: Exception) {
            Toast.makeText(context, "خطا در دریافت لیست کاربران: ${e.message}", Toast.LENGTH_LONG).show()
            isLoading = false
        }
    }

    // Filter users based on search
    LaunchedEffect(searchQuery, users) {
        filteredUsers = if (searchQuery.isEmpty()) {
            users
        } else {
            sortUsersByType(
                users.filter { user ->
                    user.username.contains(searchQuery, ignoreCase = true) ||
                            (user.fullName?.contains(searchQuery, ignoreCase = true) == true)
                }
            )
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.85f)
                .scale(dialogScale.value)
                .alpha(contentAlpha.value)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Clean header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "بستن",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Text(
                        "مدیریت کاربران",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (currentUserType == "admin") {
                        IconButton(
                            onClick = { showAddDialog = true },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.secondaryContainer)
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "افزودن کاربر",
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.size(40.dp))
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Clean search field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            "جستجو...",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "پاک کردن",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Clean content area
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                ) {
                    when {
                        isLoading -> {
                            // Loading state
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(36.dp),
                                    strokeWidth = 3.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    "در حال بارگذاری...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        filteredUsers.isEmpty() -> {
                            // Empty state
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    Icons.Default.PersonSearch,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = if (searchQuery.isEmpty())
                                        "لیست کاربران خالی است"
                                    else
                                        "نتیجه‌ای یافت نشد",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                    fontWeight = FontWeight.Medium
                                )

                                if (searchQuery.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    TextButton(
                                        onClick = { searchQuery = "" }
                                    ) {
                                        Text("پاک کردن جستجو")
                                    }
                                }
                            }
                        }
                        else -> {
                            // Users list
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                contentPadding = PaddingValues(vertical = 8.dp)
                            ) {
                                items(filteredUsers, key = { it.id }) { user ->
                                    UserListItem(
                                        user = user,
                                        onEditClick = { showEditDialog = user },
                                        onDeleteClick = { showDeleteConfirmation = user },
                                        isMainAdmin = isMainAdmin,
                                        currentUserType = currentUserType
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // دیالوگ افزودن کاربر
    if (showAddDialog) {
        AddUserDialog(
            onDismiss = { showAddDialog = false },
            onUserAdded = {
                scope.launch {
                    try {
                        val response = RetrofitClient.apiService.getAllUsers()
                        updateUsersList(response)
                    } catch (e: Exception) {
                        Toast.makeText(
                            context,
                            "خطا در بروزرسانی لیست کاربران: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            },
            isMainAdmin = isMainAdmin
        )
    }

    // دیالوگ ویرایش کاربر
    showEditDialog?.let { user ->
        EditUserDialog(
            user = user,
            onDismiss = { showEditDialog = null },
            onSave = { updateRequest ->
                scope.launch {
                    try {
                        val response = RetrofitClient.apiService.updateUser(updateRequest)
                        if (response.success) {
                            // Refresh users list
                            val newUsers = RetrofitClient.apiService.getAllUsers()
                            updateUsersList(newUsers)
                            showEditDialog = null
                            Toast.makeText(context, "کاربر با موفقیت ویرایش شد", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, response.message, Toast.LENGTH_LONG).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, "خطا در ویرایش کاربر: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        )
    }

    // دیالوگ تایید حذف کاربر
    showDeleteConfirmation?.let { user ->
        DeleteConfirmationDialog(
            user = user,
            onConfirm = {
                scope.launch {
                    try {
                        val request = DeleteUserRequest(userId = user.id)
                        val response = RetrofitClient.apiService.deleteUser(request)
                        if (response.success) {
                            // به‌روزرسانی لیست با حذف کاربر
                            updateUsersList(users.filter { it.id != user.id })
                            showDeleteConfirmation = null
                            Toast.makeText(context, "کاربر با موفقیت حذف شد", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, response.message, Toast.LENGTH_LONG).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, "خطا در حذف کاربر: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            },
            onDismiss = { showDeleteConfirmation = null }
        )
    }
}

@Composable
private fun UserListItem(
    user: User,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    isMainAdmin: Boolean,
    currentUserType: String
) {
    val userTypeColor = when (user.userType) {
        "admin" -> MaterialTheme.colorScheme.primary
        "operator" -> MaterialTheme.colorScheme.secondary
        "verifier" -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.outline
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // User info section
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Simple user icon
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(userTypeColor.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getIconForUserType(user.userType),
                        contentDescription = null,
                        tint = userTypeColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // User details
                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = user.username,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        if (user.username == "Prot0nX") {
                            Surface(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "مدیر اصلی",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    if (!user.fullName.isNullOrEmpty()) {
                        Text(
                            text = user.fullName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // User type badge
                    Text(
                        text = getUserTypeDisplay(user.userType),
                        style = MaterialTheme.typography.labelSmall,
                        color = userTypeColor,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Action buttons - minimal design
            if (isMainAdmin || currentUserType == "admin") {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Edit button
                    IconButton(
                        onClick = onEditClick,
                        enabled = user.username != "Prot0nX",
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "ویرایش",
                            tint = if (user.username == "Prot0nX")
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                            else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Delete button
                    IconButton(
                        onClick = onDeleteClick,
                        enabled = user.username != "Prot0nX",
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "حذف",
                            tint = if (user.username == "Prot0nX")
                                MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
                            else MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AddUserDialog(
    onDismiss: () -> Unit,
    onUserAdded: () -> Unit,
    isMainAdmin: Boolean
) {
    var username by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var selectedUserType by remember { mutableStateOf("operator") }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var showConfirmation by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .wrapContentHeight()
                .heightIn(max = 650.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Text(
                    "افزودن کاربر جدید",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                // Form fields
                OutlinedTextField(
                    value = username,
                    onValueChange = { input ->
                        val newValue = input.filter { char ->
                            char.isLetterOrDigit() && char.code < 128
                        }
                        username = newValue.lowercase()
                        errorMessage = ""
                    },
                    label = { Text("نام کاربری") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Ascii,
                        imeAction = ImeAction.Next
                    )
                )

                OutlinedTextField(
                    value = fullName,
                    onValueChange = { input ->
                        // فقط حروف فارسی و فاصله مجاز است
                        val newValue = input.filter { char ->
                            char == ' ' || // فاصله معمولی
                                    char == '\u200C' || // نیم‌فاصله (ZWNJ)
                                    (char.code in 0x0600..0x06FF) || // حروف فارسی
                                    (char.code in 0xFB50..0xFDFF) || // اشکال متصل فارسی
                                    (char.code in 0xFE70..0xFEFF) // اشکال دیگر فارسی
                        }
                        fullName = newValue
                        errorMessage = ""
                    },
                    label = { Text("نام و نام خانوادگی") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it.filter { char -> char.isDigit() } },
                    label = { Text("رمز عبور") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.NumberPassword,
                        imeAction = ImeAction.Done
                    )
                )

                UserTypeSelection(
                    selectedUserType = selectedUserType,
                    onUserTypeSelected = { selectedUserType = it },
                    isMainAdmin = isMainAdmin
                )

                if (errorMessage.isNotEmpty()) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Error,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = errorMessage,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                // Standardized Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Cancel button
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Text("انصراف")
                        }
                    }

                    // تابع برای اعتبارسنجی نام و نام خانوادگی
                    fun isValidFullName(name: String): Boolean {
                        val trimmedName = name.trim()
                        // باید حداقل دو کلمه داشته باشد
                        return trimmedName.split(" ").filter { it.isNotEmpty() }.size >= 2
                    }

                    // Submit button
                    Button(
                        onClick = {
                            when {
                                username.isEmpty() -> {
                                    errorMessage = "لطفاً نام کاربری را وارد کنید"
                                    return@Button
                                }
                                username.length < 4 -> {
                                    errorMessage = "نام کاربری باید حداقل 4 کاراکتر باشد"
                                    return@Button
                                }
                                fullName.isEmpty() -> {
                                    errorMessage = "لطفاً نام و نام خانوادگی را وارد کنید"
                                    return@Button
                                }
                                !isValidFullName(fullName) -> {
                                    errorMessage = "نام و نام خانوادگی باید به صورت صحیح ثبت شود"
                                    return@Button
                                }
                                password.isEmpty() -> {
                                    errorMessage = "لطفاً رمز عبور را وارد کنید"
                                    return@Button
                                }
                                password.length < 4 -> {
                                    errorMessage = "رمز عبور باید حداقل 4 رقم باشد"
                                    return@Button
                                }
                                else -> {
                                    showConfirmation = true
                                }
                            }
                        },
                        enabled = !isLoading && username.length >= 4 && isValidFullName(fullName) && password.length >= 4,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text("ایجاد کاربر")
                            }
                        }
                    }
                }
            }
        }
    }

    if (showConfirmation) {
        AddConfirmationDialog(
            username = username,
            fullName = fullName,
            selectedUserType = selectedUserType,
            isLoading = isLoading,
            onConfirm = {
                scope.launch {
                    isLoading = true
                    try {
                        val hashedPassword = hashPassword(password)
                        val request = CreateUserRequest(
                            username = username,
                            fullName = fullName,
                            password = hashedPassword,
                            userType = selectedUserType
                        )

                        val response = RetrofitClient.apiService.createUser(request)
                        if (response.isSuccessful && response.body()?.success == true) {
                            Toast.makeText(context, "کاربر با موفقیت ایجاد شد", Toast.LENGTH_SHORT).show()
                            onUserAdded()
                            onDismiss()
                        } else {
                            errorMessage = "خطا در ایجاد کاربر: ${response.errorBody()?.string()}"
                            showConfirmation = false
                        }
                    } catch (e: Exception) {
                        errorMessage = "خطا در ایجاد کاربر: ${e.message}"
                        showConfirmation = false
                    } finally {
                        isLoading = false
                    }
                }
            },
            onDismiss = { showConfirmation = false }
        )
    }
}

@Composable
private fun UserTypeSelection(
    selectedUserType: String,
    onUserTypeSelected: (String) -> Unit,
    isMainAdmin: Boolean
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "نوع کاربر:",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold
        )

        // فیلتر کردن انواع کاربر بر اساس سطح دسترسی
        val availableUserTypes = if (isMainAdmin) {
            userTypes // مدیر اصلی می‌تواند همه را ایجاد کند
        } else {
            userTypes.filter { it.value != "admin" } // ادمین‌های معمولی نمی‌توانند ادمین ایجاد کنند
        }

        // Horizontal arrangement of user type options
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            availableUserTypes.forEach { userType ->
                UserTypeOptionHorizontal(
                    userType = userType,
                    isSelected = selectedUserType == userType.value,
                    onSelect = { onUserTypeSelected(userType.value) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

private val userTypes = listOf(
    UserTypeInfo(
        label = "مدیر سیستم",
        description = "مدیریت کاربران و سیستم",
        value = "admin",
        icon = Icons.Default.AdminPanelSettings,
        color = Color(0xFF2196F3)
    ),
    UserTypeInfo(
        label = "باسکول‌چی",
        description = "وزن و ثبت بارها",
        value = "operator",
        icon = Icons.Default.Engineering,
        color = Color(0xFF4CAF50)
    ),
    UserTypeInfo(
        label = "بارشمار",
        description = "شمارش و بررسی بارها",
        value = "verifier",
        icon = Icons.Default.PersonSearch,
        color = Color(0xFFFFA000)
    )
)

@Composable
private fun UserTypeOptionHorizontal(
    userType: UserTypeInfo,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onSelect() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            }
        ),
        border = if (isSelected) {
            null  // BorderStroke not available, use null
        } else null,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Icon with background
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = userType.icon,
                    contentDescription = null,
                    tint = if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.size(16.dp)
                )
            }

            // Title
            Text(
                text = userType.label,
                style = MaterialTheme.typography.labelMedium,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Compact description
            Text(
                text = userType.description,
                style = MaterialTheme.typography.labelSmall,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Selection indicator
            if (isSelected) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun EditUserDialog(
    user: User,
    onDismiss: () -> Unit,
    onSave: (UpdateUserRequest) -> Unit
) {
    var username by remember { mutableStateOf(user.username) }
    var fullName by remember { mutableStateOf(user.fullName ?: "") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    val isLoading by remember { mutableStateOf(false) }
    var showConfirmation by remember { mutableStateOf(false) }

    // تابع برای اعتبارسنجی نام و نام خانوادگی
    fun isValidFullName(name: String): Boolean {
        val trimmedName = name.trim()
        // باید حداقل دو کلمه داشته باشد
        return trimmedName.split(" ").filter { it.isNotEmpty() }.size >= 2
    }

    // تابع برای آماده‌سازی درخواست آپدیت
    fun prepareUpdateRequest(): UpdateUserRequest? {
        if (username.isEmpty()) {
            errorMessage = "نام کاربری نمی‌تواند خالی باشد"
            return null
        }
        if (fullName.isEmpty()) {
            errorMessage = "نام و نام خانوادگی نمی‌تواند خالی باشد"
            return null
        }
        if (!isValidFullName(fullName)) {
            errorMessage = "نام و نام خانوادگی باید به صورت صحیح ثبت شود"
            return null
        }
        return UpdateUserRequest(
            id = user.id,
            username = username.takeIf { it != user.username },
            fullName = fullName.takeIf { it != user.fullName },
            password = password.takeIf { it.isNotEmpty() }?.let { hashPassword(it) },
            userType = user.userType
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .height(400.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Text(
                    "ویرایش کاربر",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                OutlinedTextField(
                    value = username,
                    onValueChange = {
                        username = it.trim()
                        errorMessage = ""
                    },
                    label = { Text("نام کاربری") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )

                OutlinedTextField(
                    value = fullName,
                    onValueChange = { input ->
                        // فقط حروف فارسی و فاصله مجاز است
                        val newValue = input.filter { char ->
                            char == ' ' || // فاصله معمولی
                                    char == '\u200C' || // نیم‌فاصله (ZWNJ)
                                    (char.code in 0x0600..0x06FF) || // حروف فارسی
                                    (char.code in 0xFB50..0xFDFF) || // اشکال متصل فارسی
                                    (char.code in 0xFE70..0xFEFF) // اشکال دیگر فارسی
                        }
                        fullName = newValue
                        errorMessage = ""
                    },
                    label = { Text("نام و نام خانوادگی") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it.filter { char -> char.isDigit() } },
                    label = { Text("رمز عبور جدید (اختیاری)") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.NumberPassword,
                        imeAction = ImeAction.Done
                    )
                )

                if (errorMessage.isNotEmpty()) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Error,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = errorMessage,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                // Standardized Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Cancel button
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Text("انصراف")
                        }
                    }

                    // Confirm button
                    Button(
                        onClick = {
                            prepareUpdateRequest()?.let {
                                showConfirmation = true
                            }
                        },
                        enabled = !isLoading && username.isNotEmpty() && isValidFullName(fullName),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text("تأیید تغییرات")
                            }
                        }
                    }
                }
            }
        }
    }

    if (showConfirmation) {
        EditConfirmationDialog(
            originalUser = user,
            newUsername = username,
            newFullName = fullName,
            hasPasswordChanged = password.isNotEmpty(),
            isLoading = isLoading,
            onConfirm = {
                showConfirmation = false
                prepareUpdateRequest()?.let { onSave(it) }
            },
            onDismiss = { showConfirmation = false }
        )
    }
}

@Composable
private fun AddConfirmationDialog(
    username: String,
    fullName: String,
    selectedUserType: String,
    isLoading: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(400.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "تأیید اطلاعات کاربر جدید",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                Text(
                    "آیا از صحت اطلاعات وارد شده اطمینان دارید؟",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                // User info card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        InfoRow("نام کاربری:", username)
                        InfoRow("نام و نام خانوادگی:", fullName)
                        InfoRow("نوع کاربر:", getUserTypeDisplay(selectedUserType))
                    }
                }

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        enabled = !isLoading,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("بازبینی")
                    }

                    Button(
                        onClick = onConfirm,
                        enabled = !isLoading,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text("تأیید و ایجاد")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun EditConfirmationDialog(
    originalUser: User,
    newUsername: String,
    newFullName: String,
    hasPasswordChanged: Boolean,
    isLoading: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .height(400.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "تأیید تغییرات",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                Text(
                    "آیا از اعمال تغییرات زیر اطمینان دارید؟",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                // Changes card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (originalUser.username != newUsername) {
                            ChangeRow(
                                label = "نام کاربری:",
                                oldValue = originalUser.username,
                                newValue = newUsername
                            )
                        }

                        if (originalUser.fullName != newFullName) {
                            ChangeRow(
                                label = "نام و نام خانوادگی:",
                                oldValue = originalUser.fullName ?: "",
                                newValue = newFullName
                            )
                        }

                        if (hasPasswordChanged) {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
                                ),
                                shape = RoundedCornerShape(6.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        "رمز عبور تغییر خواهد کرد",
                                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }

                        InfoRow("نوع کاربر:", getUserTypeDisplay(originalUser.userType))
                    }
                }

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        enabled = !isLoading,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("بازبینی")
                    }

                    Button(
                        onClick = onConfirm,
                        enabled = !isLoading,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Save,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text("تأیید و ذخیره")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChangeRow(label: String, oldValue: String, newValue: String) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = oldValue,
                    style = TextStyle(textDecoration = TextDecoration.LineThrough),
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                    fontSize = 12.sp
                )
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
            }
            Text(
                text = newValue,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun DeleteConfirmationDialog(
    user: User,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    var isLoading by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(400.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "تأیید حذف کاربر",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                Text(
                    "آیا از حذف کاربر زیر اطمینان دارید؟",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                // User info card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        InfoRow("نام کاربری:", user.username)
                        InfoRow("نوع کاربر:", getUserTypeDisplay(user.userType))
                    }
                }

                // Warning
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            "توجه: این عملیات قابل بازگشت نیست!",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Standardized Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Cancel button
                    OutlinedButton(
                        onClick = onDismiss,
                        enabled = !isLoading,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Text("انصراف")
                        }
                    }

                    // Confirm Delete button
                    Button(
                        onClick = {
                            isLoading = true
                            onConfirm()
                        },
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = MaterialTheme.colorScheme.onError,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text("تأیید و حذف")
                            }
                        }
                    }
                }
            }
        }
    }
}

fun getUserTypeDisplay(userType: String): String {
    return when (userType) {
        "admin" -> "مدیر"
        "operator" -> "باسکول‌چی"
        "verifier" -> "بارشمار"
        else -> userType
    }
}

fun getMenuItemsForUserType(userType: String): List<MenuItem> {
    return when (userType) {
        "admin" -> listOf(
            MenuItem("ثبت حواله", R.drawable.ic_boosters, "select_info"),
            MenuItem("نظارت بارشمار", R.drawable.ic_cargo_counter, "cargo_counter"),
            MenuItem("تعریف کشتی", R.drawable.ic_journal, "initial_info"),
            MenuItem("مدیریت کشتی ها", R.drawable.ic_reports, "manage_ships"),
            MenuItem("مدیریت کاربران", R.drawable.profile_admin, "manage_users")
        )
        "operator" -> listOf(
            MenuItem("ثبت حواله", R.drawable.ic_boosters, "select_info"),
            MenuItem("تعریف کشتی", R.drawable.ic_journal, "initial_info")
        )
        "verifier" -> listOf(
            MenuItem("نظارت بارشمار", R.drawable.ic_cargo_counter, "cargo_counter")
        )
        else -> listOf(
            MenuItem("ثبت حواله", R.drawable.ic_boosters, "select_info"),
            MenuItem("نظارت بارشمار", R.drawable.ic_cargo_counter, "cargo_counter"),
            MenuItem("تعریف کشتی", R.drawable.ic_journal, "initial_info"),
            MenuItem("مدیریت کشتی ها", R.drawable.ic_reports, "manage_ships")
        )
    }
}

class HardwarePerformanceEvaluator(
    private val context: Context,
    private val userPreferencesManager: UserPreferencesManager
) {
    companion object {
        private const val EVALUATION_VALIDITY_HOURS = 24 // ارزیابی مجدد هر 24 ساعت
    }

    /**
     * ارزیابی جامع عملکرد سخت‌افزار با استفاده از رویکرد ترکیبی
     * شامل بنچمارک‌های سبک و اطلاعات سخت‌افزاری
     * @return امتیاز عملکرد از 0 تا 100
     */
    suspend fun evaluatePerformance(): Int {
        // بررسی آیا ارزیابی قبلی هنوز معتبر است
        val lastEvaluation = userPreferencesManager.getScoreTimestamp()
        val currentTime = System.currentTimeMillis()
        val validityDuration = EVALUATION_VALIDITY_HOURS * 60 * 60 * 1000L

        // بررسی تغییر مشخصات دستگاه
        val currentDeviceSpecs = generateAdvancedDeviceSpecs()
        val cachedDeviceSpecs = userPreferencesManager.getDeviceSpecs()

        // اگر ارزیابی قبلی معتبر است و مشخصات تغییر نکرده، امتیاز کش شده را برگردان
        if (currentTime - lastEvaluation < validityDuration &&
            cachedDeviceSpecs == currentDeviceSpecs) {
            val cachedScore = userPreferencesManager.getHardwareScore()
            if (cachedScore != -1) {
                Log.d("HardwarePerformance", "استفاده از امتیاز کش شده: $cachedScore")
                return cachedScore
            }
        }

        Log.d("HardwarePerformance", "شروع ارزیابی جامع سخت‌افزار...")

        // اجرای بنچمارک‌ها و جمع‌آوری اطلاعات سخت‌افزاری
        val performanceMetrics = withContext(Dispatchers.Default) {
            val metrics = mutableMapOf<String, Float>()

            // بنچمارک‌های سبک (زیر 100ms)
            metrics["cpu_benchmark"] = runCPUBenchmark()
            metrics["gpu_benchmark"] = runGPUBenchmark()
            metrics["memory_benchmark"] = runMemoryBenchmark()

            // اطلاعات سخت‌افزاری
            metrics["ram_performance"] = evaluateAdvancedRAM()
            metrics["cpu_performance"] = evaluateAdvancedCPU()
            metrics["gpu_performance"] = evaluateGPU()
            metrics["display_performance"] = evaluateDisplay()
            metrics["storage_performance"] = evaluateAdvancedStorage()
            metrics["thermal_performance"] = evaluateThermal()
            metrics["android_performance"] = evaluateAndroidVersion()

            metrics
        }

        // محاسبه امتیاز نهایی با وزن‌دهی هوشمند
        val finalScore = calculateWeightedScore(performanceMetrics)

        // ذخیره نتیجه در UserPreferencesManager
        userPreferencesManager.saveHardwareScore(finalScore, currentDeviceSpecs)

        Log.d("HardwarePerformance", "امتیاز جدید محاسبه و ذخیره شد: $finalScore")
        Log.d("HardwarePerformance", "جزئیات امتیازدهی: $performanceMetrics")

        return finalScore
    }

    /**
     * تولید رشته مشخصات پیشرفته دستگاه برای مقایسه تغییرات
     */
    private fun generateAdvancedDeviceSpecs(): String {
        return try {
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val memoryInfo = ActivityManager.MemoryInfo()
            activityManager.getMemoryInfo(memoryInfo)

            val totalRAM = memoryInfo.totalMem / (1024 * 1024 * 1024)
            val coreCount = Runtime.getRuntime().availableProcessors()
            val androidVersion = Build.VERSION.SDK_INT
            val architecture = Build.SUPPORTED_ABIS.firstOrNull() ?: "unknown"
            val displayManager = context.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
            val refreshRate = displayManager.getDisplay(Display.DEFAULT_DISPLAY)?.refreshRate ?: 60f

            val statFs = StatFs(Environment.getDataDirectory().path)
            val totalStorage = statFs.totalBytes / (1024 * 1024 * 1024)

            "RAM:${totalRAM}GB|CPU:${coreCount}cores|Android:${androidVersion}|Arch:${architecture}|Refresh:${refreshRate}Hz|Storage:${totalStorage}GB"
        } catch (_: Exception) {
            "UNKNOWN_SPECS"
        }
    }

    /**
     * بنچمارک سبک CPU برای ارزیابی عملکرد پردازنده
     */
    private fun runCPUBenchmark(): Float {
        return try {
            val startTime = System.nanoTime()
            var result = 0.0

            // محاسبات ریاضی سبک برای تست CPU
            repeat(100000) {
                result += kotlin.math.sin(it.toDouble()) * kotlin.math.cos(it.toDouble())
            }

            val duration = (System.nanoTime() - startTime) / 1_000_000f // تبدیل به میلی‌ثانیه

            // امتیازدهی معکوس (زمان کمتر = امتیاز بیشتر)
            when {
                duration < 50f -> 100f
                duration < 100f -> 80f
                duration < 200f -> 60f
                duration < 400f -> 40f
                else -> 20f
            }
        } catch (_: Exception) {
            50f
        }
    }

    /**
     * بنچمارک سبک GPU برای ارزیابی عملکرد گرافیکی
     */
    private fun runGPUBenchmark(): Float {
        return try {
            val startTime = System.nanoTime()

            // شبیه‌سازی عملیات گرافیکی سبک
            val bitmap = createBitmap(100, 100)
            val canvas = Canvas(bitmap)

            repeat(1000) {
                canvas.drawColor(android.graphics.Color.rgb(it % 255, (it * 2) % 255, (it * 3) % 255))
            }

            val duration = (System.nanoTime() - startTime) / 1_000_000f

            when {
                duration < 30f -> 100f
                duration < 60f -> 80f
                duration < 120f -> 60f
                duration < 250f -> 40f
                else -> 20f
            }
        } catch (_: Exception) {
            50f
        }
    }

    /**
     * بنچمارک سبک حافظه برای ارزیابی عملکرد RAM
     */
    private fun runMemoryBenchmark(): Float {
        return try {
            val startTime = System.nanoTime()

            // تست تخصیص و آزادسازی حافظه
            val arrays = mutableListOf<IntArray>()
            repeat(100) {
                arrays.add(IntArray(1000) { it })
            }
            arrays.clear()

            System.gc() // فراخوانی garbage collector

            val duration = (System.nanoTime() - startTime) / 1_000_000f

            when {
                duration < 20f -> 100f
                duration < 40f -> 80f
                duration < 80f -> 60f
                duration < 160f -> 40f
                else -> 20f
            }
        } catch (_: Exception) {
            50f
        }
    }

    /**
     * ارزیابی پیشرفته RAM با در نظر گرفتن اندازه و نوع
     */
    private fun evaluateAdvancedRAM(): Float {
        return try {
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val memoryInfo = ActivityManager.MemoryInfo()
            activityManager.getMemoryInfo(memoryInfo)

            val totalRAM = memoryInfo.totalMem / (1024 * 1024 * 1024) // تبدیل به گیگابایت
            val availableRAM = memoryInfo.availMem / (1024 * 1024 * 1024)
            val ramUsagePercent = 1f - (availableRAM.toFloat() / totalRAM.toFloat())

            when {
                totalRAM >= 12 -> 100f
                totalRAM >= 8 -> 85f
                totalRAM >= 6 -> 70f
                totalRAM >= 4 -> 55f
                totalRAM >= 3 -> 40f
                totalRAM >= 2 -> 25f
                else -> 10f
            } * (1f - ramUsagePercent * 0.3f) // کاهش امتیاز بر اساس استفاده فعلی
        } catch (_: Exception) {
            50f
        }
    }

    /**
     * ارزیابی پیشرفته CPU با در نظر گرفتن معماری و تعداد هسته‌ها
     */
    private fun evaluateAdvancedCPU(): Float {
        return try {
            val coreCount = Runtime.getRuntime().availableProcessors()
            val architecture = Build.SUPPORTED_ABIS.firstOrNull() ?: ""

            val coreScore = when {
                coreCount >= 8 -> 100f
                coreCount >= 6 -> 85f
                coreCount >= 4 -> 70f
                coreCount >= 2 -> 50f
                else -> 25f
            }

            val archScore = when {
                architecture.contains("arm64-v8a") -> 100f
                architecture.contains("armeabi-v7a") -> 80f
                architecture.contains("x86_64") -> 70f
                architecture.contains("x86") -> 50f
                else -> 30f
            }

            (coreScore * 0.7f + archScore * 0.3f)
        } catch (_: Exception) {
            50f
        }
    }

    /**
     * ارزیابی GPU و قابلیت‌های گرافیکی
     */
    private fun evaluateGPU(): Float {
        return try {
            val packageManager = context.packageManager

            // استفاده از ثابت‌های معتبر OpenGL ES
            val hasOpenGLES3 = packageManager.hasSystemFeature("android.hardware.opengles.es_version_3_0")
            val hasOpenGLES31 = packageManager.hasSystemFeature("android.hardware.opengles.es_version_3_1")
            val hasOpenGLES32 = packageManager.hasSystemFeature("android.hardware.opengles.es_version_3_2")

            when {
                hasOpenGLES32 -> 100f
                hasOpenGLES31 -> 85f
                hasOpenGLES3 -> 70f
                else -> 40f
            }
        } catch (_: Exception) {
            50f
        }
    }

    /**
     * ارزیابی نمایشگر (نرخ تازه‌سازی و رزولوشن)
     */
    private fun evaluateDisplay(): Float {
        return try {
            val displayManager = context.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
            val display = displayManager.getDisplay(Display.DEFAULT_DISPLAY)
            val refreshRate = display?.refreshRate ?: 60f

            when {
                refreshRate >= 120f -> 100f
                refreshRate >= 90f -> 85f
                refreshRate >= 60f -> 70f
                else -> 40f
            }
        } catch (_: Exception) {
            50f
        }
    }

    /**
     * ارزیابی پیشرفته حافظه ذخیره‌سازی
     */
    private fun evaluateAdvancedStorage(): Float {
        return try {
            val statFs = StatFs(Environment.getDataDirectory().path)
            val availableBytes = statFs.availableBytes
            val availableGB = availableBytes / (1024 * 1024 * 1024)

            when {
                availableGB >= 64 -> 100f
                availableGB >= 32 -> 85f
                availableGB >= 16 -> 70f
                availableGB >= 8 -> 55f
                availableGB >= 4 -> 40f
                availableGB >= 2 -> 25f
                else -> 10f
            }
        } catch (_: Exception) {
            50f
        }
    }

    /**
     * ارزیابی وضعیت حرارتی و عملکرد پایدار
     */
    private fun evaluateThermal(): Float {
        return try {
            val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
            val batteryLevel = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)

            // امتیازدهی بر اساس سطح باتری (نشان‌دهنده احتمال داغی دستگاه)
            when {
                batteryLevel >= 80 -> 100f
                batteryLevel >= 60 -> 85f
                batteryLevel >= 40 -> 70f
                batteryLevel >= 20 -> 50f
                else -> 30f
            }
        } catch (_: Exception) {
            70f
        }
    }

    /**
     * ارزیابی نسخه اندروید
     */
    @SuppressLint("ObsoleteSdkInt")
    private fun evaluateAndroidVersion(): Float {
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> 100f // Android 13+
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> 90f // Android 12
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> 80f // Android 11
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> 70f // Android 10
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.P -> 60f // Android 9
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> 50f // Android 8
            else -> 30f // نسخه‌های قدیمی‌تر
        }
    }

    /**
     * محاسبه امتیاز نهایی با وزن‌دهی هوشمند بر اساس اهمیت برای انیمیشن‌ها
     */
    private fun calculateWeightedScore(metrics: Map<String, Float>): Int {
        // وزن‌دهی بر اساس اهمیت برای عملکرد انیمیشن
        val weights = mapOf(
            "cpu_benchmark" to 0.15f,
            "gpu_benchmark" to 0.20f,
            "memory_benchmark" to 0.15f,
            "ram_performance" to 0.15f,
            "cpu_performance" to 0.10f,
            "gpu_performance" to 0.10f,
            "display_performance" to 0.08f,
            "storage_performance" to 0.03f,
            "thermal_performance" to 0.02f,
            "android_performance" to 0.02f
        )

        var weightedSum = 0f
        var totalWeight = 0f

        metrics.forEach { (metric, value) ->
            val weight = weights[metric] ?: 0f
            weightedSum += value * weight
            totalWeight += weight
        }

        val finalScore = if (totalWeight > 0) (weightedSum / totalWeight).coerceIn(0f, 100f) else 50f

        return finalScore.toInt()
    }

}

object AnimationManager {
    private var performanceScore: Int = 50
    private var animationsEnabled: Boolean = true

    /**
     * تنظیم امتیاز عملکرد و تعیین وضعیت انیمیشن‌ها
     */
    fun setPerformanceScore(score: Int) {
        performanceScore = score
        animationsEnabled = score >= 70 // انیمیشن‌ها فقط برای دستگاه‌های با امتیاز 70 یا بالاتر فعال می‌شوند
    }

    /**
     * بررسی فعال بودن انیمیشن‌ها
     */
    fun areAnimationsEnabled(): Boolean {
        return animationsEnabled
    }
}