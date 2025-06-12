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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Engineering
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
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.atk.atk_cargo.api.ApiService
import com.atk.atk_cargo.api.CargoViewModel
import com.atk.atk_cargo.api.CargoViewModelFactory
import com.atk.atk_cargo.api.ChangeLogInfo
import com.atk.atk_cargo.api.Constants
import com.atk.atk_cargo.api.CreateUserRequest
import com.atk.atk_cargo.api.DeleteUserRequest
import com.atk.atk_cargo.api.LoadingNotificationService
import com.atk.atk_cargo.api.LoginRequest
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
import com.atk.atk_cargo.security.SecurityBlockScreen
import com.atk.atk_cargo.security.SecurityErrorType
import com.atk.atk_cargo.security.SignatureVerifier
import com.atk.atk_cargo.ui.theme.ATKCargoTheme
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
    private lateinit var signatureVerifier: SignatureVerifier
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
                        performSecurityCheck()
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

                    HandleSecurityCheck {
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

            signatureVerifier = SignatureVerifier(this)

            userPreferencesManager = UserPreferencesManager(this)

            reportsRepository = ReportsRepository(RetrofitClient.apiService)

            cargoViewModelFactory = CargoViewModelFactory(reportsRepository, userPreferencesManager)

        } catch (e: Exception) {
            // خطا در مقداردهی وابستگی‌ها
            throw e // پرتاب مجدد خطا برای مدیریت در سطح بالاتر
        }
    }



    private fun performSecurityCheck() {
        lifecycleScope.launch {
            isSecurityCheckLoading = true
            try {
                val (isValid, error) = signatureVerifier.i()
                isSecurityCheckPassed = isValid
                securityErrorType = error
            } catch (_: Exception) {
                isSecurityCheckPassed = false
                securityErrorType = SecurityErrorType.TAMPERED
            } finally {
                isSecurityCheckLoading = false
            }
        }
    }

    @Composable
    private fun HandleSecurityCheck(content: @Composable () -> Unit) {
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
                if (username.isNotEmpty()) {
                    val apiService = RetrofitClient.apiService
                    val response = apiService.checkSession(SessionCheckRequest(username))
                    when {
                        response.isSuccessful && response.body()?.success == true -> {
                            _isSessionValid.value = true
                            
                            // راه‌اندازی سرویس اعلان‌های بارگیری بعد از تأیید اعتبار جلسه
                            startLoadingNotificationService()
                        }
                        else -> {
                            _isSessionValid.value = false
                            userPreferencesManager.clearUserCredentials()
                            showMessage("لطفاً دوباره وارد شوید!")
                        }
                    }
                } else {
                    _isSessionValid.value = false
                }
            } catch (_: Exception) {
                _isSessionValid.value = false
                showMessage("خطا در بررسی جلسه کاربر. لطفاً دوباره تلاش کنید.")
            }
        }
    }

    fun updateSessionValidity(isValid: Boolean) {
        _isSessionValid.value = isValid
    }

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun startLoadingNotificationService() {
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
        delay(3000)
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
                                                    userPreferencesManager.clearUserCredentials()
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
                            userPreferencesManager.saveUserCredentials(loggedInUsername, loggedInUserType)
                            mainActivity.updateSessionValidity(true)
                        }
                    } else {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(message)
                        }
                    }
                },
                updateSessionValidity = mainActivity::updateSessionValidity
            )
        }

        if (showUserManagement) {
            UserManagementDialog(
                onDismiss = { showUserManagement = false }
            )
        }
    }
}

@Composable
fun SplashScreen() {
    val backgroundScale = remember { Animatable(1.1f) }
    val logoScale = remember { Animatable(0.8f) }
    val logoAlpha = remember { Animatable(0f) }
    val textAlpha = remember { Animatable(0f) }
    val context = LocalContext.current
    val appVersion = remember {
        try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        } catch (_: Exception) {
            "نسخه 3.0.7"
        }
    }

    LaunchedEffect(Unit) {
        // انیمیشن پس‌زمینه
        backgroundScale.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 1000,
                easing = FastOutSlowInEasing
            )
        )

        // انیمیشن لوگو
        launch {
            logoAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 800)
            )
            logoScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }

        // انیمیشن متن با تاخیر
        delay(600)
        textAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surfaceVariant,
                        MaterialTheme.colorScheme.surface
                    ),
                    radius = 1200f
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // دایره پشت لوگو
        Box(
            modifier = Modifier
                .size(300.dp)
                .scale(backgroundScale.value)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            MaterialTheme.colorScheme.surface.copy(alpha = 0f)
                        )
                    ),
                    shape = CircleShape
                )
        )

        // ستون محتوای اصلی
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // لوگوی انیمیشنی
            Box(
                modifier = Modifier
                    .size(250.dp)
                    .scale(logoScale.value)
                    .alpha(logoAlpha.value),
                contentAlignment = Alignment.Center
            ) {
                val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.lottie_logo))
                val progress by animateLottieCompositionAsState(
                    composition = composition,
                    iterations = 1
                )
                LottieAnimation(
                    composition = composition,
                    progress = { progress },
                    modifier = Modifier.size(220.dp)
                )
            }

            // عنوان برنامه
            Text(
                text = "سیستم مدیریت هوشمند بارگیری",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .alpha(textAlpha.value)
                    .padding(16.dp)
            )

            // نام شرکت
            Text(
                text = "شرکت امین تجار خوزستان",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.alpha(textAlpha.value)
            )
        }

        // نسخه برنامه در پایین صفحه
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 32.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Text(
                text = "نسخه $appVersion",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.alpha(textAlpha.value)
            )
        }
    }
}

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
                            showGridAnimation = false
                            delay(300)
                            userPreferencesManager.clearUserCredentials()
                            mainActivity.updateSessionValidity(false)
                            onLogoutClick()
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
                                userPreferencesManager.clearUserCredentials()
                                mainActivity.updateSessionValidity(false)
                                onLogoutClick()
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
    index: Int, // پارامتر استفاده شده است، نمی‌توان به _ تغییر داد
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
    
    // انیمیشن‌های ظاهر شدن
    val dialogAppearance = remember { Animatable(0.9f) }
    val contentAlpha = remember { Animatable(0f) }
    val headerAppearance = remember { Animatable(0f) }
    val searchBarOffset = remember { Animatable(-50f) }
    val listOffset = remember { Animatable(50f) }

    LaunchedEffect(Unit) {
        // انیمیشن اصلی دیالوگ
        launch {
            dialogAppearance.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
        
        // انیمیشن محتوا
        launch {
            delay(100)
            contentAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 500)
            )
        }
        
        // انیمیشن هدر
        launch {
            delay(200)
            headerAppearance.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
        
        // انیمیشن جستجو
        launch {
            delay(300)
            searchBarOffset.animateTo(
                targetValue = 0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
        
        // انیمیشن لیست
        launch {
            delay(400)
            listOffset.animateTo(
                targetValue = 0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
    }

    // تابع کمکی برای مرتب‌سازی کاربران
    fun sortUsersByType(users: List<User>): List<User> {
        val typeOrder = mapOf(
            "admin" to 0,
            "operator" to 1,
            "verifier" to 2
        )
        return users.sortedWith(compareBy(
            { typeOrder[it.userType] ?: 3 }, // اول بر اساس نوع کاربری
            { it.username } // سپس بر اساس نام کاربری
        ))
    }

    // تابع کمکی برای به‌روزرسانی لیست کاربران
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

    // بارگیری کاربران
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

    // فیلتر کاربران بر اساس جستجو
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
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            shadowElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f)
                .scale(dialogAppearance.value)
                .alpha(contentAlpha.value)
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                // اضافه کردن افکت گرادیان پس‌زمینه
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.surface,
                                    MaterialTheme.colorScheme.surface,
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                )
                            )
                        )
                )
                
                // دایره‌های تزئینی
                Box(
                    modifier = Modifier
                        .size(220.dp)
                        .offset(x = (-100).dp, y = (-80).dp)
                        .alpha(0.03f)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    Color.Transparent
                                )
                            ),
                            shape = CircleShape
                        )
                )
                
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .align(Alignment.BottomEnd)
                        .offset(x = 80.dp, y = 60.dp)
                        .alpha(0.02f)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.tertiary,
                                    Color.Transparent
                                )
                            ),
                            shape = CircleShape
                        )
                )
            
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                ) {
                    // Header with Add Button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .scale(headerAppearance.value),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // دکمه بستن با طراحی جدید
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "بستن",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        // عنوان با انیمیشن
                        Text(
                            "مدیریت کاربران",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        
                        // دکمه افزودن کاربر جدید
                        if (currentUserType == "admin") {
                            IconButton(
                                onClick = { showAddDialog = true },
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(
                                        brush = Brush.linearGradient(
                                            colors = listOf(
                                                MaterialTheme.colorScheme.primary,
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                                            )
                                        )
                                    )
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = "افزودن کاربر",
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.size(40.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Search Box with animation
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .offset(y = searchBarOffset.value.dp),
                        placeholder = { Text("جستجوی نام کاربری یا نام...") },
                        leadingIcon = { 
                            Icon(
                                Icons.Default.Search,
                                contentDescription = null,
                                tint = if (searchQuery.isNotEmpty())
                                    MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
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
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                            focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Users List with background and animation
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                            .offset(y = listOffset.value.dp),
                        shape = RoundedCornerShape(24.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f),
                        tonalElevation = 1.dp
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp)
                        ) {
                            if (isLoading) {
                                // حالت در حال بارگذاری
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(48.dp),
                                        strokeWidth = 4.dp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        "در حال بارگذاری اطلاعات کاربران...",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            } else if (filteredUsers.isEmpty()) {
                                // حالت بدون نتیجه
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        Icons.Default.PersonSearch,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(80.dp)
                                            .alpha(0.4f),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = if (searchQuery.isEmpty()) 
                                            "لیست کاربران خالی است"
                                        else
                                            "کاربری با این مشخصات یافت نشد",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                        fontWeight = FontWeight.Medium
                                    )
                                    
                                    if (searchQuery.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        OutlinedButton(
                                            onClick = { searchQuery = "" },
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.padding(top = 8.dp)
                                        ) {
                                            Text("پاک کردن جستجو")
                                        }
                                    }
                                }
                            } else {
                                // نمایش لیست کاربران
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.spacedBy(10.dp),
                                    contentPadding = PaddingValues(vertical = 12.dp, horizontal = 4.dp)
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

// طراحی جدید آیتم لیست کاربران
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

    var isHovered by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isHovered) 1.02f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = ""
    )
    val elevation by animateFloatAsState(
        targetValue = if (isHovered) 4f else 1f,
        animationSpec = tween(durationMillis = 200),
        label = ""
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp, horizontal = 4.dp)
            .scale(scale)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = { 
                        isHovered = true
                        tryAwaitRelease()
                        isHovered = false
                    }
                )
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = elevation.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // User Info Section with icon
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // آیکون کاربر با دایره رنگی متناسب با نوع کاربری
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    userTypeColor.copy(alpha = 0.2f),
                                    userTypeColor.copy(alpha = 0.05f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getIconForUserType(user.userType),
                        contentDescription = null,
                        tint = userTypeColor,
                        modifier = Modifier.size(26.dp)
                    )
                }
                
                // اطلاعات کاربر
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = user.username,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        if (user.username == "Prot0nX") {
                            Surface(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.padding(start = 4.dp)
                            ) {
                                Text(
                                    text = "مدیر اصلی",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    // نمایش نام و نام خانوادگی
                    Text(
                        text = user.fullName ?: "—",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // نمایش نوع کاربری با بج شیک
                    Surface(
                        color = userTypeColor.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(userTypeColor)
                            )
                            Text(
                                text = getUserTypeDisplay(user.userType),
                                style = MaterialTheme.typography.bodySmall,
                                color = userTypeColor,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Action Buttons - فقط برای مدیر اصلی فعال هستند
            if (isMainAdmin) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    // دکمه ویرایش با افکت hover
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                shape = CircleShape
                            )
                            .clickable(onClick = onEditClick),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "ویرایش",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // دکمه حذف با افکت hover
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                if (user.username == "Prot0nX") 
                                    MaterialTheme.colorScheme.error.copy(alpha = 0.05f)
                                else 
                                    MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                            )
                            .border(
                                width = 1.dp,
                                color = if (user.username == "Prot0nX")
                                    MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                                else
                                    MaterialTheme.colorScheme.error.copy(alpha = 0.2f),
                                shape = CircleShape
                            )
                            .clickable(
                                enabled = user.username != "Prot0nX",
                                onClick = onDeleteClick
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "حذف",
                            tint = if (user.username == "Prot0nX")
                                MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
                            else MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            } else {
                // اگر کاربر فعلی مدیر اصلی نیست، دکمه‌ها را غیرفعال نشان می‌دهیم
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "ویرایش",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "حذف",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                            modifier = Modifier.size(18.dp)
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

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "افزودن کاربر جدید",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        },
        text = {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Username field with validation
                OutlinedTextField(
                    value = username,
                    onValueChange = { input ->
                        val newValue = input.filter { char ->
                            char.isLetterOrDigit() && char.code < 128  // Ensures only ASCII characters
                        }
                        username = newValue.lowercase()  // Convert to lowercase for consistency
                        errorMessage = ""
                    },
                    label = { Text("نام کاربری") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = LocalTextStyle.current.copy(
                        textAlign = TextAlign.Center,
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Ascii,
                        imeAction = ImeAction.Next
                    ),
                    supportingText = {
                        Text(
                            "فقط حروف انگلیسی و اعداد مجاز است",
                            style = MaterialTheme.typography.bodySmall
                        )
                    },
                    isError = errorMessage.isNotEmpty() && username.isEmpty(),
                    leadingIcon = {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = if (username.isNotEmpty())
                                MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )

                // Full name field
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it.trim() },
                    label = { Text("نام و نام خانوادگی") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Right),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )

                // Password field
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it.filter { char -> char.isDigit() } },
                    label = { Text("رمز عبور") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Right),
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
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Right
                    )
                }
            }
        },
        confirmButton = {
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
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("ایجاد کاربر")
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
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium
        )

        // Using simple Row instead of LazyVerticalGrid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            userTypes.forEach { userType ->
                UserTypeOption(
                    userType = userType,
                    isSelected = selectedUserType == userType.value,
                    onSelect = { onUserTypeSelected(userType.value) }
                )
            }
        }
    }
}

private val userTypes = listOf(
    UserTypeInfo(
        label = "مدیر سیستم",
        value = "admin",
        icon = Icons.Default.AdminPanelSettings,
        color = Color(0xFF2196F3)
    ),
    UserTypeInfo(
        label = "باسکول‌چی",
        value = "operator",
        icon = Icons.Default.Engineering,
        color = Color(0xFF4CAF50)
    ),
    UserTypeInfo(
        label = "بارشمار",
        value = "verifier",
        icon = Icons.Default.PersonSearch,
        color = Color(0xFFFFA000)
    )
)

@Composable
private fun UserTypeOption(
    userType: UserTypeInfo,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val backgroundColor = if (isSelected) {
        userType.color.copy(alpha = 0.15f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    }

    val borderColor = if (isSelected) {
        userType.color.copy(alpha = 0.5f)
    } else {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
    }

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onSelect)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = userType.icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = if (isSelected) userType.color else MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = userType.label,
            style = MaterialTheme.typography.bodySmall,
            color = if (isSelected) userType.color else MaterialTheme.colorScheme.onSurface,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )

        RadioButton(
            selected = isSelected,
            onClick = onSelect,
            colors = RadioButtonDefaults.colors(
                selectedColor = userType.color,
                unselectedColor = MaterialTheme.colorScheme.outline
            ),
            modifier = Modifier.size(20.dp)
        )
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

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "ویرایش کاربر",
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it.trim() },
                    label = { Text("نام کاربری") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Right),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )

                // فیلد جدید نام و نام خانوادگی
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it.trim() },
                    label = { Text("نام و نام خانوادگی") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Right),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it.filter { char -> char.isDigit() } },
                    label = { Text("رمز عبور جدید") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Right),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.NumberPassword,
                        imeAction = ImeAction.Done
                    )
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
            }
        },
        confirmButton = {
            Button(onClick = {
                prepareUpdateRequest()?.let {
                    showConfirmation = true
                }
            }) {
                Text("ذخیره")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("انصراف")
            }
        }
    )

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
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "تأیید اطلاعات کاربر جدید",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("آیا از صحت اطلاعات وارد شده اطمینان دارید؟")
                Spacer(modifier = Modifier.height(8.dp))

                // اطلاعات کاربر
            Card(
                    modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                    shape = RoundedCornerShape(8.dp)
            ) {
                Column(
                    modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("نام کاربری:")
                            Text(
                                username,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("نام و نام خانوادگی:")
                            Text(
                                fullName,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("نوع کاربر:")
                            Text(
                                getUserTypeDisplay(selectedUserType),
                                fontWeight = FontWeight.Bold,
                                color = when (selectedUserType) {
                                    "admin" -> MaterialTheme.colorScheme.primary
                                    "operator" -> MaterialTheme.colorScheme.secondary
                                    "verifier" -> MaterialTheme.colorScheme.tertiary
                                    else -> MaterialTheme.colorScheme.onSurface
                                }
                            )
                        }
                    }
                }

                // راهنما
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            "پس از ایجاد کاربر، اطلاعات ورود به حساب را به کاربر اعلام کنید.",
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Text("تأیید و ایجاد")
                    }
                }
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                enabled = !isLoading
            ) {
                Text("بازبینی")
            }
        }
    )
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
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
                                Text(
                "تأیید تغییرات",
                                    style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("آیا از اعمال تغییرات زیر اطمینان دارید؟")
                Spacer(modifier = Modifier.height(8.dp))

                // نمایش تغییرات
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (originalUser.username != newUsername) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    "نام کاربری:",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            originalUser.username,
                                            style = TextStyle(textDecoration = TextDecoration.LineThrough),
                                            color = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                                        )
                                        Icon(
                                            Icons.AutoMirrored.Filled.ArrowForward,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Text(
                                        newUsername,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

                        // نمایش تغییرات نام و نام خانوادگی
                        if (originalUser.fullName != newFullName) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                            Text(
                                    "نام و نام خانوادگی:",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                Text(
                                            originalUser.fullName ?: "",
                                            style = TextStyle(textDecoration = TextDecoration.LineThrough),
                                            color = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                                        )
                                        Icon(
                                            Icons.AutoMirrored.Filled.ArrowForward,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Text(
                                        newFullName,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

                        if (hasPasswordChanged) {
                            Surface(
                                color = MaterialTheme.colorScheme.tertiaryContainer,
                                shape = RoundedCornerShape(4.dp)
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
                                        color = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                }
                            }
                        }

                        // نمایش نوع کاربری
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("نوع کاربر:")
                            Text(
                                getUserTypeDisplay(originalUser.userType),
                                fontWeight = FontWeight.Bold,
                                color = when (originalUser.userType) {
                                    "admin" -> MaterialTheme.colorScheme.primary
                                    "operator" -> MaterialTheme.colorScheme.secondary
                                    "verifier" -> MaterialTheme.colorScheme.tertiary
                                    else -> MaterialTheme.colorScheme.onSurface
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Save,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Text("تأیید و ذخیره")
                    }
                }
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                enabled = !isLoading
            ) {
                Text("بازبینی")
            }
        }
    )
}

@Composable
private fun DeleteConfirmationDialog(
    user: User,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    var isLoading by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "تأیید حذف کاربر",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("آیا از حذف کاربر زیر اطمینان دارید؟")
                Spacer(modifier = Modifier.height(8.dp))

                // اطلاعات کاربر
                    Card(
                    modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                    shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("نام کاربری:")
                            Text(
                                user.username,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("نوع کاربر:")
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

                // هشدار
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(4.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                            .padding(8.dp),
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
                                    fontWeight = FontWeight.Bold
                                )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    isLoading = true
                    onConfirm()
                },
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onError
                    )
                } else {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                                Icon(
                            Icons.Default.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Text("تأیید و حذف")
                    }
                }
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                enabled = !isLoading
            ) {
                Text("انصراف")
            }
        }
    )
}

fun getUserTypeDisplay(userType: String): String {
    return when (userType) {
        "admin" -> "مدیر"
        "operator" -> "باسکول‌چی"
        "verifier" -> "بارشمار"
        else -> userType
    }
}

@Composable
fun LoginDialog(
    onDismiss: () -> Unit,
    onLoginChecked: (Boolean, String, String, String) -> Unit,
    updateSessionValidity: (Boolean) -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var showPassword by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val errorShakeController = remember { Animatable(0f) }
    var loginAttempted by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }
    
    // انیمیشن‌های ورود
    val dialogScale = remember { Animatable(0.9f) }
    val contentAlpha = remember { Animatable(0f) }
    val logoScale = remember { Animatable(0.8f) }
    val logoRotation = remember { Animatable(0f) }
    val loginButtonWidth = remember { Animatable(0f) }

    // انیمیشن‌های لرزش خطا
    LaunchedEffect(errorMessage) {
        if (errorMessage != null && loginAttempted) {
            errorShakeController.animateTo(
                targetValue = 0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
            errorShakeController.animateTo(
                targetValue = 15f,
                animationSpec = tween(durationMillis = 80)
            )
            errorShakeController.animateTo(
                targetValue = -12f,
                animationSpec = tween(durationMillis = 80)
            )
            errorShakeController.animateTo(
                targetValue = 8f,
                animationSpec = tween(durationMillis = 80)
            )
            errorShakeController.animateTo(
                targetValue = -4f,
                animationSpec = tween(durationMillis = 80)
            )
            errorShakeController.animateTo(
                targetValue = 0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
    }

    // انیمیشن‌های شروع
    LaunchedEffect(Unit) {
        showDialog = true
        launch {
            dialogScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
        
        launch {
            delay(150)
            contentAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 600)
            )
        }
        
        launch {
            delay(200)
            logoScale.animateTo(
                targetValue = 1.1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
            logoScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
        
        launch {
            logoRotation.animateTo(
                targetValue = 360f,
                animationSpec = tween(
                    durationMillis = 1200,
                    easing = FastOutSlowInEasing
                )
            )
        }
        
        launch {
            delay(400)
            loginButtonWidth.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
    }

    val apiService = remember {
        Retrofit.Builder()
            .baseUrl(Constants.getBaseUrl())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    if (showDialog) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true,
                usePlatformDefaultWidth = false
            )
        ) {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                shadowElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .fillMaxHeight(0.65f)
                    .scale(dialogScale.value)
                    .offset(x = errorShakeController.value.dp)
                    .alpha(contentAlpha.value)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // دایره‌های تزئینی با گرادیان ملایم
                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .offset(x = (-80).dp, y = (-60).dp)
                            .alpha(0.04f)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary,
                                        Color.Transparent
                                    )
                                ),
                                shape = CircleShape
                            )
                    )
                    
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .align(Alignment.BottomEnd)
                            .offset(x = 60.dp, y = 40.dp)
                            .alpha(0.03f)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.tertiary,
                                        Color.Transparent
                                    )
                                ),
                                shape = CircleShape
                            )
                    )
                
                    // دکمه بستن در گوشه بالا راست با افکت محو
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp)
                            .size(36.dp)
                            .alpha(0.7f)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "بستن",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // محتوای اصلی دیالوگ
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 32.dp, vertical = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // بخش هدر با لوگو و عنوان
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // لوگو با افکت چرخش و مقیاس
                            Box(
                                modifier = Modifier
                                    .size(90.dp)
                                    .scale(logoScale.value)
                                    .background(
                                        brush = Brush.radialGradient(
                                            colors = listOf(
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.03f)
                                            )
                                        ),
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .size(45.dp)
                                        .rotate(logoRotation.value)
                                )
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // عنوان با سبک تایپوگرافی مدرن
                            Text(
                                text = "ورود به سیستم",
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold
                            )

                            // توضیحات با انیمیشن نمایش/مخفی شدن
                            AnimatedVisibility(
                                visible = !isLoading,
                                enter = fadeIn() + expandVertically(),
                                exit = fadeOut() + shrinkVertically()
                            ) {
                                Text(
                                    text = "لطفاً اطلاعات کاربری خود را وارد کنید",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                        }

                        // بخش فرم ورود
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // فیلد نام کاربری
                            OutlinedTextField(
                                value = username,
                                onValueChange = {
                                    username = it.trim()
                                    errorMessage = null
                                },
                                label = { Text("نام کاربری") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Right),
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Person,
                                        contentDescription = null,
                                        tint = if (username.isNotEmpty())
                                            MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                },
                                keyboardOptions = KeyboardOptions(
                                    imeAction = ImeAction.Next
                                ),
                                isError = errorMessage != null && username.isEmpty(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                )
                            )

                            // فیلد رمز عبور
                            OutlinedTextField(
                                value = password,
                                onValueChange = {
                                    password = it.filter { char -> char.isDigit() }
                                    errorMessage = null
                                },
                                label = { Text("رمز عبور") },
                                singleLine = true,
                                visualTransformation = if (showPassword)
                                    VisualTransformation.None
                                else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.NumberPassword,
                                    imeAction = ImeAction.Done
                                ),
                                modifier = Modifier.fillMaxWidth(),
                                textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Right),
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = if (password.isNotEmpty())
                                            MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                },
                                trailingIcon = {
                                    IconButton(onClick = { showPassword = !showPassword }) {
                                        Icon(
                                            if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                            contentDescription = if (showPassword) "پنهان کردن رمز" else "نمایش رمز",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                        )
                                    }
                                },
                                isError = errorMessage != null && password.isEmpty(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                )
                            )

                            // پیام خطا با انیمیشن نمایش/مخفی شدن
                            AnimatedVisibility(
                                visible = errorMessage != null,
                                enter = fadeIn() + expandVertically(),
                                exit = fadeOut() + shrinkVertically()
                            ) {
                                Surface(
                                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.Warning,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Text(
                                            text = errorMessage ?: "",
                                            color = MaterialTheme.colorScheme.onErrorContainer,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }

                        // بخش دکمه‌ها
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // دکمه ورود با انیمیشن عرض
                            Button(
                                onClick = {
                                    loginAttempted = true
                                    if (username.isNotEmpty() && password.isNotEmpty()) {
                                        isLoading = true
                                        errorMessage = null
                                        coroutineScope.launch {
                                            try {
                                                val hashedPassword = hashPassword(password)
                                                val loginRequest = LoginRequest(username, hashedPassword, "")
                                                val response = apiService.checkLogin(loginRequest)

                                                if (response.isSuccessful) {
                                                    val responseBody = response.body()
                                                    if (responseBody != null) {
                                                        if (responseBody.success) {
                                                            // موفقیت در ورود
                                                            onLoginChecked(
                                                                true,
                                                                responseBody.message,
                                                                responseBody.userType ?: "",
                                                                username
                                                            )
                                                            updateSessionValidity(true)
                                                            
                                                        } else {
                                                            errorMessage = responseBody.message
                                                        }
                                                    } else {
                                                        errorMessage = "پاسخ سرور خالی است"
                                                    }
                                                } else {
                                                    errorMessage = "خطا در ورود: لطفاً اطلاعات را بررسی کنید"
                                                }
                                            } catch (_: Exception) {
                                                errorMessage = "خطا در ارتباط با سرور"
                                            } finally {
                                                isLoading = false
                                            }
                                        }
                                    } else {
                                        errorMessage = "لطفاً نام کاربری و رمز عبور را وارد کنید"
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth(loginButtonWidth.value)
                                    .height(54.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                elevation = ButtonDefaults.buttonElevation(
                                    defaultElevation = 4.dp,
                                    pressedElevation = 8.dp
                                )
                            ) {
                                if (isLoading) {
                                    // نمایش لودینگ هنگام پردازش
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(22.dp),
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    // نمایش متن دکمه در حالت عادی
                                    Row(
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.AutoMirrored.Filled.Login,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            "ورود به سیستم",
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                    }
                                }
                            }

                            // دکمه انصراف
                            TextButton(
                                onClick = onDismiss,
                                modifier = Modifier.padding(top = 4.dp),
                                enabled = !isLoading
                            ) {
                                Text(
                                    "انصراف",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                )
                            }
                        }
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