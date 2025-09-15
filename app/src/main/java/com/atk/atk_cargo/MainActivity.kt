package com.atk.atk_cargo

import android.Manifest
import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.PowerManager
import android.os.StatFs
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColor
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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
import com.atk.atk_cargo.api.ApiService
import com.atk.atk_cargo.api.CargoViewModel
import com.atk.atk_cargo.api.CargoViewModelFactory
import com.atk.atk_cargo.api.ChangeLogInfo
import com.atk.atk_cargo.api.Constants
import com.atk.atk_cargo.api.CreateUserRequest
import com.atk.atk_cargo.api.DeleteUserRequest
import com.atk.atk_cargo.api.LoadingNotificationService
import com.atk.atk_cargo.api.LoginRequest
import com.atk.atk_cargo.api.LogoutRequest
import com.atk.atk_cargo.api.MenuItem
import com.atk.atk_cargo.api.ReportsRepository
import com.atk.atk_cargo.api.ReportsViewModel
import com.atk.atk_cargo.api.RetrofitClient
import com.atk.atk_cargo.api.SessionCheckRequest
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.URLDecoder
import java.security.MessageDigest
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
    val isSessionValid: StateFlow<Boolean> = _isSessionValid.asStateFlow()

    @SuppressLint("CoroutineCreationDuringComposition", "BatteryLife")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            initializeDependencies()

            setContent {
                ATKCargoTheme {
                    var showMainContent by remember { mutableStateOf(false) }
                    // متغیر جدید برای کنترل نمایش دیالوگ‌های مجوز
                    var canRequestPermissions by remember { mutableStateOf(false) }

                    LaunchedEffect(Unit) {
                        // ابتدا بررسی امنیتی را انجام می‌دهیم
                        calculateWeatherForecast()
                        delay(1500) // افزایش تاخیر

                        // سپس بررسی بروزرسانی را انجام می‌دهیم
                        checkForUpdate()
                        delay(1500) // افزایش تاخیر

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

                    RenderMusicPlaylist {
                        HandleMainContent(
                            showMainContent = showMainContent,
                            isUpdateAvailable = isUpdateAvailable,
                            updateInfo = updateInfo
                        )
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
                
                Log.d("HardwarePerformance", "امتیاز عملکرد دستگاه: $performanceScore")
                Log.d("AnimationManager", "وضعیت انیمیشن‌ها: ${if (AnimationManager.areAnimationsEnabled()) "فعال" else "غیرفعال"}")
                
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
                    .fillMaxHeight(0.75f)
                    .alpha(dialogAlpha),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    UpdateHeader(
                        version = updateInfo.latestVersion,
                        message = updateInfo.updateMessage,
                        releaseDate = updateInfo.releaseDate
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        ChangeLogSection(updateInfo.changeLog)
                    }

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
private fun ChangeLogSection(changeLog: ChangeLogInfo) {
    // ذخیره وضعیت باز/بسته بودن هر دسته
    var expandedCategory by remember { mutableStateOf<String?>(null) }

    // تعیین اولین دسته موجود به عنوان پیش‌فرض
    LaunchedEffect(Unit) {
        expandedCategory = when {
            changeLog.newFeatures.isNotEmpty() -> "new"
            changeLog.improvements.isNotEmpty() -> "improvements"
            changeLog.fixes.isNotEmpty() -> "fixes"
            else -> null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = "تغییرات این نسخه:",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        // قابلیت‌های جدید
        changeLog.newFeatures.takeIf { it.isNotEmpty() }?.let {
            ExpandableChangeLogCategory(
                title = "قابلیت‌های جدید",
                icon = Icons.Default.Add,
                color = MaterialTheme.colorScheme.primary,
                items = it,
                isExpanded = expandedCategory == "new",
                onExpandChange = { expanded ->
                    expandedCategory = if (expanded) "new" else null
                }
            )
        }

        // بهبودها
        changeLog.improvements.takeIf { it.isNotEmpty() }?.let {
            ExpandableChangeLogCategory(
                title = "بهبودها",
                icon = Icons.Default.Check,
                color = MaterialTheme.colorScheme.secondary,
                items = it,
                isExpanded = expandedCategory == "improvements",
                onExpandChange = { expanded ->
                    expandedCategory = if (expanded) "improvements" else null
                }
            )
        }

        // رفع اشکالات
        changeLog.fixes.takeIf { it.isNotEmpty() }?.let {
            ExpandableChangeLogCategory(
                title = "رفع اشکالات",
                icon = Icons.Default.Info,
                color = MaterialTheme.colorScheme.tertiary,
                items = it,
                isExpanded = expandedCategory == "fixes",
                onExpandChange = { expanded ->
                    expandedCategory = if (expanded) "fixes" else null
                }
            )
        }
    }
}

@Composable
private fun ExpandableChangeLogCategory(
    title: String,
    icon: ImageVector,
    color: Color,
    items: List<String>,
    isExpanded: Boolean,
    onExpandChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
            .clickable { onExpandChange(!isExpanded) },
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        color = color,
                        fontWeight = FontWeight.Bold
                    )
                    Badge(
                        containerColor = color.copy(alpha = 0.2f),
                    ) {
                        Text(
                            text = items.size.toString(),
                            color = color
                        )
                    }
                }

                val rotation by animateFloatAsState(
                    targetValue = if (isExpanded) 180f else 0f,
                    animationSpec = tween(
                        durationMillis = 300,
                        easing = FastOutSlowInEasing
                    ),
                    label = ""
                )

                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "بستن" else "باز کردن",
                    tint = color,
                    modifier = Modifier
                        .size(24.dp)
                        .rotate(rotation)
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
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
                        .padding(top = 12.dp)
                        .fillMaxWidth()
                ) {
                    items.forEach { item ->
                        Row(
                            modifier = Modifier
                                .padding(start = 28.dp, top = 4.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .offset(y = 8.dp)
                                    .background(color.copy(alpha = 0.5f), CircleShape)
                            )
                            Text(
                                text = item,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
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
                        Text(
                            text = "حجم فایل: $updateSize مگابایت",
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
    var showLoginDialog by remember { mutableStateOf(false) }
    var showUserManagement by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val mainActivity = LocalContext.current as MainActivity
    val isSessionValid by mainActivity.isSessionValid.collectAsState()

    LaunchedEffect(key1 = true) {
        delay(4700)
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
                    SplashScreen()
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
                                NavHost(
                                    navController = navController,
                                    startDestination = "home"
                                ) {
                                    composable("home") {
                                        HomeScreen(
                                            navController = navController,
                                            username = username,
                                            userType = userType,
                                            isSessionValid = isSessionValid,
                                            onLoginClick = { showLoginDialog = true },
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
                                            onManageUsersClick = { showUserManagement = true }
                                        )
                                    }
                                    composable(
                                        route = "initial_info",
                                        // اضافه کردن انیمیشن برای انتقال بین صفحات
                                        enterTransition = {
                                            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left)
                                        },
                                        exitTransition = {
                                            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right)
                                        }
                                    ) {
                                        Log.d("Navigation", "Composing InitialInfoScreen")
                                        InitialInfoScreen()
                                    }
                                    composable(
                                        route = "select_info",
                                        enterTransition = {
                                            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left)
                                        },
                                        exitTransition = {
                                            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right)
                                        }
                                    ) {
                                        Log.d("Navigation", "Composing SelectInfoScreen")
                                        val cargoViewModel: CargoViewModel = viewModel(factory = cargoViewModelFactory)
                                        SelectInfoScreenContent(navController = navController, viewModel = cargoViewModel)
                                    }
                                    composable(
                                        route = "cargo_counter",
                                        enterTransition = {
                                            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left)
                                        },
                                        exitTransition = {
                                            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right)
                                        }
                                    ) {
                                        Log.d("Navigation", "Composing CargoCounterScreen")
                                        CargoCounterScreen(navController = navController)
                                    }
                                    composable(
                                        route = "manage_reports",
                                        enterTransition = {
                                            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left)
                                        },
                                        exitTransition = {
                                            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right)
                                        }
                                    ) {
                                        Log.d("Navigation", "Composing ManageReportsScreen")
                                        val reportsViewModel: ReportsViewModel = viewModel(
                                            factory = ReportsViewModel.Factory
                                        )
                                        ManageReportsScreen(viewModel = reportsViewModel)
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
                                            "سایت نیاکوزرین",
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

        if (showLoginDialog) {
            LoginDialog(
                onDismiss = { showLoginDialog = false },
                onLoginChecked = { success, message, loggedInUserType, loggedInUsername ->
                    if (success) {
                        showLoginDialog = false
                        coroutineScope.launch {
                            val deviceId = Build.DISPLAY ?: UUID.randomUUID().toString()
                            // دریافت session_token از UserPreferencesManager
                            val sessionToken = userPreferencesManager.sessionToken.first()
                            userPreferencesManager.saveUserCredentials(loggedInUsername, loggedInUserType, deviceId, sessionToken)
                            mainActivity.updateSessionValidity(true)
                            // شروع بررسی دوره‌ای جلسه پس از ورود موفق
                            mainActivity.startLoadingNotificationService()
                        }
                    } else {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(message)
                        }
                    }
                },
                updateSessionValidity = mainActivity::updateSessionValidity,
                userPreferencesManager = userPreferencesManager
            )
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
fun SplashScreen() {
    val textAlpha = remember { Animatable(0f) }
    val context = LocalContext.current
    val appVersion = remember {
        try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        } catch (_: Exception) {
            "نامشخص"
        }
    }

    LaunchedEffect(Unit) {
        // انیمیشن متن با تاخیر
        delay(800)
        textAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800)
        )
    }

    Box(
        modifier = Modifier.fillMaxSize()
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
                    volume = 0f // بی‌صدا کردن ویدیو
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
                    useController = false // مخفی کردن کنترل‌های پخش
                    setShowBuffering(PlayerView.SHOW_BUFFERING_NEVER)
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM // پر کردن تمام صفحه
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

        // متن‌ها در پایین صفحه
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // عنوان برنامه
            Text(
                text = "سیستم مدیریت هوشمند بارگیری",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .alpha(textAlpha.value)
                    .padding(bottom = 8.dp)
            )

            // نام شرکت
            Text(
                text = "شرکت امین تجار خوزستان",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .alpha(textAlpha.value)
                    .padding(bottom = 16.dp)
            )

            // نسخه برنامه
            Text(
                text = "نسخه $appVersion",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.alpha(textAlpha.value)
            )
        }
    }
}

@SuppressLint("HardwareIds")
@Composable
fun HomeScreen(
    navController: NavHostController,
    username: String,
    userType: String,
    isSessionValid: Boolean,
    onLoginClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onManageUsersClick: () -> Unit
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
                ModernHeader(
                    username = username,
                    userType = userType,
                    onLoginClick = onLoginClick,
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
                    mainActivity = mainActivity
                )

                WelcomeSection(username)

                if (isLoggedIn) {
                    LaunchedEffect(Unit) {
                        delay(300)
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
                } else {
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(animationSpec = tween(600)) + expandVertically(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        )
                    ) {
                        LoginPrompt(onLoginClick = onLoginClick)
                    }
                }
            }
        }
    }

    LaunchedEffect(selectedMenuItem) {
        selectedMenuItem?.let { menuItem ->
            when (menuItem.route) {
                "initial_info", "select_info", "cargo_counter", "manage_reports", "manage_users" -> {
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
private fun ModernHeader(
    username: String,
    userType: String,
    onLoginClick: () -> Unit,
    onLogoutClick: () -> Unit,
    userPreferencesManager: UserPreferencesManager,
    coroutineScope: CoroutineScope,
    mainActivity: MainActivity
) {
    // اضافه کردن انیمیشن‌های ورود
    var isInitialRender by remember { mutableStateOf(true) }
    val headerScale = remember { Animatable(0.96f) }
    val headerOpacity = remember { Animatable(0f) }

    // انیمیشن‌های شروع
    LaunchedEffect(key1 = Unit) {
        launch {
            headerScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
        launch {
            headerOpacity.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 500)
            )
        }
        delay(600)
        isInitialRender = false
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 2.dp)
            .scale(headerScale.value)
            .alpha(headerOpacity.value),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f),
        tonalElevation = 2.dp,
        shadowElevation = 4.dp,
        shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
    ) {
        // گرادیان ظریف پس‌زمینه برای عمق بیشتر
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.03f),
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                if (username.isNotEmpty()) {
                    // نمایش منوی پروفایل برای کاربران وارد شده
                    ProfileMenu(
                        username = username,
                        userType = userType,
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
                        }
                    )
                } else {
                    // نمایش دکمه ورود برای کاربران مهمان با طراحی جدید
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f)
                                    )
                                )
                            )
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // بخش اطلاعات کاربر مهمان
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // آیکون کاربر با طراحی جدید
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(
                                            brush = Brush.radialGradient(
                                                colors = listOf(
                                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                                                )
                                            )
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                // اطلاعات متنی کاربر مهمان
                                Column {
                                    Text(
                                        text = "کاربر مهمان",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                    AnimatedVisibility(
                                        visible = !isInitialRender,
                                        enter = fadeIn() + expandVertically(),
                                        exit = fadeOut() + shrinkVertically()
                                    ) {
                                        Text(
                                            text = "برای دسترسی کامل وارد شوید",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                        )
                                    }
                                }
                            }

                            // دکمه ورود با طراحی جدید
                            Button(
                                onClick = onLoginClick,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)
                                ),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                elevation = ButtonDefaults.buttonElevation(
                                    defaultElevation = 2.dp,
                                    pressedElevation = 4.dp
                                )
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Login,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        "ورود",
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                }
                            }
                        }
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

    // کارت پروفایل با افکت‌های جدید
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 4.dp)
            .scale(contentScale.value)
            .clickable { expanded = !expanded }
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 4.dp
        )
    ) {
        // گرادیان ملایم پس‌زمینه
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f),
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // هدر پروفایل با پیام‌های جدید
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // اطلاعات کاربر با آیکون
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // آیکون کاربر با نماد نوع کاربری
                        Box {
                            // دایره پشت آیکون با گرادیان
                            val userTypeColor = when (userType) {
                                "admin" -> MaterialTheme.colorScheme.primary
                                "operator" -> MaterialTheme.colorScheme.secondary
                                "verifier" -> MaterialTheme.colorScheme.tertiary
                                else -> MaterialTheme.colorScheme.primary
                            }

                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(CircleShape)
                                    .background(
                                        brush = Brush.radialGradient(
                                            colors = listOf(
                                                userTypeColor.copy(alpha = 0.15f),
                                                userTypeColor.copy(alpha = 0.05f)
                                            )
                                        )
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = userTypeColor.copy(alpha = 0.3f),
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = getIconForUserType(userType),
                                    contentDescription = null,
                                    modifier = Modifier.size(26.dp),
                                    tint = userTypeColor
                                )
                            }
                        }

                        // اطلاعات نام کاربری
                        Column {
                            Text(
                                text = username,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                // نوع کاربر
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(
                                            when (userType) {
                                                "admin" -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                                "operator" -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                                                "verifier" -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)
                                                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f)
                                            }
                                        )
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = getUserTypeDisplay(userType),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = when (userType) {
                                            "admin" -> MaterialTheme.colorScheme.primary
                                            "operator" -> MaterialTheme.colorScheme.secondary
                                            "verifier" -> MaterialTheme.colorScheme.tertiary
                                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                                        }
                                    )
                                }
                                
                                // نمایش امتیاز سخت‌افزار
                                if (hardwareScore > 0) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(
                                                MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)
                                            )
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "امتیاز: $hardwareScore",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.tertiary
                                        )
                                    }
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
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Settings Button
        OutlinedButton(
            onClick = onSettingsClick,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Security,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("رمز عبور")
            }
        }

        // Logout Button
        Button(
            onClick = onLogoutClick,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            )
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("خروج")
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

@Composable
private fun WelcomeSection(username: String) {
    var textVisible by remember { mutableStateOf(false) }
    val textScale = remember { Animatable(0.9f) }

    val hintText = if (username.isNotEmpty())
        "لطفاً گزینه مورد نظر خود را انتخاب کنید"
    else
        "برای دسترسی به امکانات سیستم لطفاً وارد شوید"

    // انیمیشن رنگ برای نام کاربر
    val usernameColorAnimation = rememberInfiniteTransition()
    val usernameColor by usernameColorAnimation.animateColor(
        initialValue = MaterialTheme.colorScheme.primary,
        targetValue = MaterialTheme.colorScheme.tertiary,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    // انیمیشن مقیاس برای نام کاربر
    val usernameScaleAnimation = rememberInfiniteTransition()
    val usernameScale by usernameScaleAnimation.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    LaunchedEffect(Unit) {
        delay(500)
        textVisible = true
        textScale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                        MaterialTheme.colorScheme.surface
                    )
                )
            )
    ) {
        // دایره‌های تزئینی با اندازه کوچکتر
        Box(
            modifier = Modifier
                .size(80.dp)
                .offset(x = (-25).dp, y = (-25).dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            Color.Transparent
                        ),
                        radius = 80f
                    ),
                    shape = CircleShape
                )
        )

        Box(
            modifier = Modifier
                .size(60.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 15.dp, y = 15.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
                            Color.Transparent
                        ),
                        radius = 60f
                    ),
                    shape = CircleShape
                )
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedVisibility(
                visible = textVisible,
                enter = fadeIn(animationSpec = tween(1000)) +
                        slideInVertically(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            ),
                            initialOffsetY = { it / 2 }
                        )
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .scale(textScale.value)
                ) {
                    // متن خوش‌آمدگویی با انیمیشن فقط برای نام کاربر
                    if (username.isNotEmpty()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // بخش ثابت متن خوش‌آمدگویی
                            Text(
                                text = "خوش آمدید، ",
                                style = MaterialTheme.typography.titleLarge,
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            // فقط نام کاربر با انیمیشن (بدون حالت تایپ کردن)
                            Text(
                                text = username,
                                style = MaterialTheme.typography.titleLarge,
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.ExtraBold,
                                color = usernameColor,
                                modifier = Modifier.scale(usernameScale)
                            )
                        }
                    } else {
                        // حالت بدون نام کاربر
                        Text(
                            text = "به سیستم مدیریت هوشمند بارگیری خوش آمدید",
                            style = MaterialTheme.typography.titleLarge,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // متن راهنما بدون انیمیشن
                    Text(
                        text = hintText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
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
        "manage_reports" -> "گزارش‌های مدیریتی"
        "manage_users" -> "مدیریت کاربران سیستم"
        else -> ""
    }
}

@Composable
private fun LoginPrompt(onLoginClick: () -> Unit) {
    val scale = remember { Animatable(0.95f) }
    val shadowElevation = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        delay(300)
        launch {
            scale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
        shadowElevation.animateTo(
            targetValue = 10f,
            animationSpec = tween(durationMillis = 500)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .scale(scale.value),
            shape = RoundedCornerShape(28.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = shadowElevation.value.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.05f)
                            )
                        )
                    )
            ) {
                // دایره تزئینی پشت آیکون
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .offset(x = (-20).dp, y = (-20).dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                    Color.Transparent
                                )
                            ),
                            shape = CircleShape
                        )
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // آیکون قفل در دایره
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        "برای دسترسی به منوها وارد شوید",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        "دسترسی به تمامی امکانات سیستم تنها با ورود به حساب کاربری امکان‌پذیر است",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = onLoginClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Login,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "ورود به سیستم",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
            }
        }
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
            shape = RoundedCornerShape(20.dp),
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
                                        isMainAdmin = isMainAdmin
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
            }
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
    isMainAdmin: Boolean
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
            if (isMainAdmin) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Edit button
                    IconButton(
                        onClick = onEditClick,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "ویرایش",
                            tint = MaterialTheme.colorScheme.primary,
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
    onUserAdded: () -> Unit
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
                    onValueChange = { fullName = it.trim() },
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
                    onUserTypeSelected = { selectedUserType = it }
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
                        enabled = !isLoading,
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
    onUserTypeSelected: (String) -> Unit
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

        // Horizontal arrangement of user type options
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            userTypes.forEach { userType ->
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

    // تابع برای آماده‌سازی درخواست آپدیت
    fun prepareUpdateRequest(): UpdateUserRequest? {
        if (username.isEmpty() || fullName.isEmpty()) {
            errorMessage = "نام کاربری و مشخصات کاربر نمی‌تواند خالی باشد"
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
                    onValueChange = { 
                        fullName = it.trim()
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
                        enabled = !isLoading,
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

@SuppressLint("HardwareIds")
@Composable
fun LoginDialog(
    onDismiss: () -> Unit,
    onLoginChecked: (Boolean, String, String, String) -> Unit,
    updateSessionValidity: (Boolean) -> Unit,
    userPreferencesManager: UserPreferencesManager
) {
    // State variables - مینیمال
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var showPassword by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    // انیمیشن ساده
    val dialogScale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "dialog_scale"
    )

    val apiService = remember {
        Retrofit.Builder()
            .baseUrl(Constants.getBaseUrl())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .heightIn(max = 400.dp)
                .scale(dialogScale),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 12.dp
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // هدر مینیمال
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // آیکون ساده
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                MaterialTheme.colorScheme.primaryContainer,
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.AccountCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // عنوان
                    Text(
                        text = "ورود",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // دکمه بستن
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "بستن",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // فرم ورود کامپکت
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // فیلد نام کاربری کامپکت
                    OutlinedTextField(
                        value = username,
                        onValueChange = {
                            username = it.trim()
                            if (errorMessage != null) errorMessage = null
                        },
                        label = { Text("نام کاربری") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        ),
                        singleLine = true
                    )

                    // فیلد رمز عبور کامپکت
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it.filter { char -> char.isDigit() }
                            if (errorMessage != null) errorMessage = null
                        },
                        label = { Text("رمز عبور") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        trailingIcon = {
                            IconButton(
                                onClick = { showPassword = !showPassword },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    if (showPassword) Icons.Default.Visibility
                                        else Icons.Default.VisibilityOff,
                                    contentDescription = if (showPassword) "پنهان کردن رمز" else "نمایش رمز",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        },
                        visualTransformation = if (showPassword) VisualTransformation.None
                            else PasswordVisualTransformation(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.NumberPassword,
                            imeAction = ImeAction.Done
                        ),
                        singleLine = true
                    )
                }

                // نمایش خطا کامپکت
                AnimatedVisibility(
                    visible = errorMessage != null,
                    enter = slideInVertically() + fadeIn(),
                    exit = slideOutVertically() + fadeOut()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = errorMessage ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                // دکمه ورود کامپکت
                Button(
                    onClick = {
                        if (username.isNotBlank() && password.isNotBlank() && !isLoading) {
                            coroutineScope.launch {
                                isLoading = true
                                errorMessage = null

                                try {
                                    val hashedPassword = hashPassword(password)
                                    val deviceModel = Build.MODEL ?: "Unknown"
                                    val androidVersion = Build.VERSION.RELEASE ?: "Unknown"
                                    val deviceId = Build.DISPLAY ?: UUID.randomUUID().toString()

                                    val loginRequest = LoginRequest(
                                        username = username,
                                        password = hashedPassword,
                                        userType = "",
                                        deviceModel = deviceModel,
                                        deviceId = deviceId,
                                        androidVersion = androidVersion
                                    )

                                    val response = apiService.checkLogin(loginRequest)

                                    if (response.isSuccessful) {
                                        val responseBody = response.body()
                                        if (responseBody != null && responseBody.success) {
                                            responseBody.sessionToken?.let { sessionToken ->
                                                launch {
                                                    userPreferencesManager.saveSessionToken(sessionToken)
                                                }
                                            }

                                            onLoginChecked(
                                                true,
                                                responseBody.message,
                                                responseBody.userType ?: "",
                                                username
                                            )
                                            updateSessionValidity(true)
                                            onDismiss()
                                        } else {
                                            errorMessage = responseBody?.message ?: "خطا در ورود"
                                        }
                                    } else {
                                        errorMessage = when (response.code()) {
                                            401 -> "نام کاربری یا رمز عبور اشتباه است"
                                            403 -> "دسترسی مجاز نیست"
                                            409 -> "ورود همزمان مجاز نیست"
                                            500 -> "خطای سرور"
                                            else -> "خطا در اتصال"
                                        }
                                    }
                                } catch (e: Exception) {
                                    errorMessage = when (e) {
                                        is java.net.UnknownHostException -> "عدم دسترسی به اینترنت"
                                        is java.net.SocketTimeoutException -> "زمان اتصال به پایان رسید"
                                        else -> "خطا در اتصال"
                                    }
                                } finally {
                                    isLoading = false
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    enabled = username.isNotBlank() && password.isNotBlank() && !isLoading,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "ورود",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }
    }
}

fun hashPassword(password: String): String {
    return MessageDigest.getInstance("SHA-256")
        .digest(password.toByteArray())
        .fold("") { str, it -> str + "%02x".format(it) }
}

fun getMenuItemsForUserType(userType: String): List<MenuItem> {
    return when (userType) {
        "admin" -> listOf(
            MenuItem("ثبت حواله", R.drawable.ic_boosters, "select_info"),
            MenuItem("نظارت بارشمار", R.drawable.ic_cargo_counter, "cargo_counter"),
            MenuItem("تعریف کشتی", R.drawable.ic_journal, "initial_info"),
            MenuItem("مدیریت گزارشات", R.drawable.ic_reports, "manage_reports"),
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
            MenuItem("مدیریت گزارشات", R.drawable.ic_reports, "manage_reports")
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
     * ارزیابی عملکرد سخت‌افزار و تعیین امتیاز
     * @return امتیاز عملکرد از 0 تا 100
     */
    suspend fun evaluatePerformance(): Int {
        // بررسی آیا ارزیابی قبلی هنوز معتبر است
        val lastEvaluation = userPreferencesManager.getScoreTimestamp()
        val currentTime = System.currentTimeMillis()
        val validityDuration = EVALUATION_VALIDITY_HOURS * 60 * 60 * 1000L
        
        // بررسی تغییر مشخصات دستگاه
        val currentDeviceSpecs = generateDeviceSpecs()
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
        
        Log.d("HardwarePerformance", "محاسبه مجدد امتیاز سخت‌افزار...")
        
        var totalScore = 0
        var maxScore = 0
        
        // ارزیابی RAM
        val ramScore = evaluateRAM()
        totalScore += ramScore
        maxScore += 30
        
        // ارزیابی CPU
        val cpuScore = evaluateCPU()
        totalScore += cpuScore
        maxScore += 25
        
        // ارزیابی نسخه اندروید
        val androidScore = evaluateAndroidVersion()
        totalScore += androidScore
        maxScore += 20
        
        // ارزیابی فضای ذخیره‌سازی
        val storageScore = evaluateStorage()
        totalScore += storageScore
        maxScore += 15
        
        // ارزیابی وضعیت باتری
        val batteryScore = evaluateBattery()
        totalScore += batteryScore
        maxScore += 10
        
        // محاسبه امتیاز نهایی
        val finalScore = ((totalScore.toFloat() / maxScore) * 100).toInt().coerceIn(0, 100)
        
        // ذخیره نتیجه در UserPreferencesManager
        userPreferencesManager.saveHardwareScore(finalScore, currentDeviceSpecs)
        
        Log.d("HardwarePerformance", "امتیاز جدید محاسبه و ذخیره شد: $finalScore")
        
        return finalScore
    }
    
    /**
     * تولید رشته مشخصات دستگاه برای مقایسه تغییرات
     */
    private fun generateDeviceSpecs(): String {
        return try {
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val memoryInfo = ActivityManager.MemoryInfo()
            activityManager.getMemoryInfo(memoryInfo)
            
            val totalRAM = memoryInfo.totalMem / (1024 * 1024 * 1024)
            val coreCount = Runtime.getRuntime().availableProcessors()
            val androidVersion = Build.VERSION.SDK_INT
            
            val statFs = StatFs(Environment.getDataDirectory().path)
            val totalStorage = statFs.totalBytes / (1024 * 1024 * 1024)
            
            "RAM:${totalRAM}GB|CPU:${coreCount}cores|Android:${androidVersion}|Storage:${totalStorage}GB"
        } catch (_: Exception) {
            "UNKNOWN_SPECS"
        }
    }
    
    private fun evaluateRAM(): Int {
        return try {
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val memoryInfo = ActivityManager.MemoryInfo()
            activityManager.getMemoryInfo(memoryInfo)
            
            val totalRAM = memoryInfo.totalMem / (1024 * 1024 * 1024) // تبدیل به گیگابایت
            
            when {
                totalRAM >= 8 -> 30 // 8GB یا بیشتر
                totalRAM >= 6 -> 25 // 6-8GB
                totalRAM >= 4 -> 20 // 4-6GB
                totalRAM >= 3 -> 15 // 3-4GB
                totalRAM >= 2 -> 10 // 2-3GB
                else -> 5 // کمتر از 2GB
            }
        } catch (_: Exception) {
            15 // امتیاز متوسط در صورت خطا
        }
    }
    
    private fun evaluateCPU(): Int {
        return try {
            val coreCount = Runtime.getRuntime().availableProcessors()
            
            when {
                coreCount >= 8 -> 25 // 8 هسته یا بیشتر
                coreCount >= 6 -> 20 // 6-8 هسته
                coreCount >= 4 -> 15 // 4-6 هسته
                coreCount >= 2 -> 10 // 2-4 هسته
                else -> 5 // تک هسته
            }
        } catch (_: Exception) {
            12 // امتیاز متوسط در صورت خطا
        }
    }
    
    @SuppressLint("ObsoleteSdkInt")
    private fun evaluateAndroidVersion(): Int {
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> 20 // Android 13+
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> 18 // Android 12
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> 16 // Android 11
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> 14 // Android 10
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.P -> 12 // Android 9
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> 10 // Android 8
            else -> 5 // نسخه‌های قدیمی‌تر
        }
    }
    
    private fun evaluateStorage(): Int {
        return try {
            val statFs = StatFs(Environment.getDataDirectory().path)
            val availableBytes = statFs.availableBytes
            val availableGB = availableBytes / (1024 * 1024 * 1024)
            
            when {
                availableGB >= 32 -> 15 // 32GB یا بیشتر فضای آزاد
                availableGB >= 16 -> 12 // 16-32GB
                availableGB >= 8 -> 10 // 8-16GB
                availableGB >= 4 -> 7 // 4-8GB
                availableGB >= 2 -> 5 // 2-4GB
                else -> 2 // کمتر از 2GB
            }
        } catch (_: Exception) {
            8 // امتیاز متوسط در صورت خطا
        }
    }
    
    private fun evaluateBattery(): Int {
        return try {
            val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
            val batteryLevel = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
            
            when {
                batteryLevel >= 80 -> 10 // باتری بالای 80%
                batteryLevel >= 50 -> 8 // باتری 50-80%
                batteryLevel >= 30 -> 6 // باتری 30-50%
                batteryLevel >= 15 -> 4 // باتری 15-30%
                else -> 2 // باتری کمتر از 15%
            }
        } catch (_: Exception) {
            6 // امتیاز متوسط در صورت خطا
        }
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
        animationsEnabled = score >= 60 // انیمیشن‌ها فقط برای دستگاه‌های با امتیاز 60 یا بالاتر فعال می‌شوند
    }
    
    /**
     * بررسی فعال بودن انیمیشن‌ها
     */
    fun areAnimationsEnabled(): Boolean {
        return animationsEnabled
    }
}