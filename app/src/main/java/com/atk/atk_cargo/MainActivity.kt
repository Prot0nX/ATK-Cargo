package com.atk.atk_cargo

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.AccountCircle
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
import com.atk.atk_cargo.api.LoadingCheckService
import com.atk.atk_cargo.api.LoginRequest
import com.atk.atk_cargo.api.MenuItem
import com.atk.atk_cargo.api.Message
import com.atk.atk_cargo.api.MessageReadRequest
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
import com.atk.atk_cargo.security.LoadingCheckWorker
import com.atk.atk_cargo.security.SecurityBlockScreen
import com.atk.atk_cargo.security.SignatureVerifier
import com.atk.atk_cargo.ui.theme.ATKCargoTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.URLDecoder
import java.security.MessageDigest
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

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
    val isSessionValid: StateFlow<Boolean> = _isSessionValid.asStateFlow()

    @RequiresApi(Build.VERSION_CODES.P)
    @SuppressLint("CoroutineCreationDuringComposition")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initializeDependencies()
        requestRequiredPermissions()
        requestBackgroundPermissions()
        startBackgroundServices()
        startLoadingCheckService()

        setContent {
            ATKCargoTheme {
                var showMainContent by remember { mutableStateOf(false) }

                LaunchedEffect(Unit) {
                    performSecurityCheck()
                    checkForUpdate()
                    showMainContent = true
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
    }

    private fun initializeDependencies() {
        updateManager = ViewModelProvider(
            this,
            UpdateManagerFactory(this)
        )[UpdateManager::class.java]

        signatureVerifier = SignatureVerifier(this)
        userPreferencesManager = UserPreferencesManager(this)
        reportsRepository = ReportsRepository(RetrofitClient.apiService)
        cargoViewModelFactory = CargoViewModelFactory(reportsRepository, userPreferencesManager)
    }

    private fun requestRequiredPermissions() {
        requestNotificationPermission()
    }

    @SuppressLint("BatteryLife")
    private fun requestBackgroundPermissions() {
        val packageName = packageName
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        if (!pm.isIgnoringBatteryOptimizations(packageName)) {
            try {
                val intent = Intent().apply {
                    action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                    data = Uri.parse("package:$packageName")
                }
                startActivity(intent)
            } catch (e: Exception) {
                Log.e("MainActivity", "Error requesting battery optimization permission", e)
            }
        }
    }

    private fun startBackgroundServices() {
        Intent(this, LoadingCheckService::class.java).also { intent ->
            startForegroundService(intent)
        }
    }

    private fun performSecurityCheck() {
        lifecycleScope.launch {
            isSecurityCheckLoading = true
            isSecurityCheckPassed = signatureVerifier.verifyAppSignature()
            isSecurityCheckLoading = false
            if (!isSecurityCheckPassed) {
                showMessage("خطای امنیتی: امضای برنامه نامعتبر است.")
            }
        }
    }

    @Composable
    private fun HandleSecurityCheck(content: @Composable () -> Unit) {
        if (!isSecurityCheckPassed || isSecurityCheckLoading) {
            SecurityBlockScreen(isLoading = isSecurityCheckLoading)
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

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    0
                )
            }
        }
    }

    fun startLoadingCheckService() {
        lifecycleScope.launch {
            try {
                val userType = userPreferencesManager.userType.first()
                if (userType.isNotEmpty()) {
                    val serviceIntent = Intent(this@MainActivity, LoadingCheckService::class.java)
                    startForegroundService(serviceIntent)
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Error starting LoadingCheckService", e)
            }
        }
        Intent(this, LoadingCheckService::class.java).also { intent ->
            startForegroundService(intent)
        }
        LoadingCheckWorker.startPeriodicWorker(this)
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
                            startLoadingCheckService()
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
            } catch (e: Exception) {
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
                    .fillMaxHeight(0.8f)
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
                                    composable("initial_info") {
                                        InitialInfoScreen()
                                    }
                                    composable("select_info") {
                                        val cargoViewModel: CargoViewModel = viewModel(factory = cargoViewModelFactory)
                                        SelectInfoScreenContent(navController = navController, viewModel = cargoViewModel)
                                    }
                                    composable("cargo_counter") {
                                        CargoCounterScreen(navController = navController)
                                    }
                                    composable("manage_reports") {
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
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                tonalElevation = 2.dp
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        "امین تجار خوزستان (سایت نیاکوزرین)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        "نسخه ${BuildConfig.VERSION_NAME}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
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
                context = LocalContext.current,
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
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.lottie_logo))
        val progress by animateLottieCompositionAsState(composition)
        LottieAnimation(
            composition = composition,
            progress = { progress },
            modifier = Modifier.size(200.dp)
        )
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
                            when (item.route) {
                                "manage_users" -> onManageUsersClick()
                                else -> selectedMenuItem = item
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
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 2.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
        ) {
            if (username.isNotEmpty()) {
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
                // نمایش دکمه ورود برای کاربران مهمان
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "کاربر مهمان",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Button(
                        onClick = onLoginClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("ورود")
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
    val rotationState by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = ""
    )
    var messages by remember { mutableStateOf<List<Message>>(emptyList()) }
    var unreadMessages by remember { mutableStateOf<List<Message>>(emptyList()) }
    val badgeScale = remember { Animatable(initialValue = 0f) }
    var hasShownMessages by remember { mutableStateOf(false) }
    val shouldShowMessages = remember(expanded, unreadMessages) {
        expanded && unreadMessages.isNotEmpty() && !hasShownMessages
    }

    LaunchedEffect(showSettings) {
        if (showSettings && currentUser == null) {
            scope.launch {
                try {
                    val response = RetrofitClient.apiService.getAllUsers()
                    currentUser = response.find { it.username == username }
                } catch (e: Exception) {
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

    LaunchedEffect(Unit) {
        try {
            val response = RetrofitClient.apiService.getNewMessages(
                userType = userType,
                lastCheckTime = getLastCheckTime(),
                includeReadStatus = true
            )
            if (response.isSuccessful) {
                messages = response.body() ?: emptyList()
                unreadMessages = messages.filter { it.readBy?.contains(username) != true }

                if (unreadMessages.isNotEmpty()) {
                    badgeScale.animateTo(
                        targetValue = 1f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Log.e("ProfileMenu", "Error fetching messages", e)
        }
    }

    LaunchedEffect(shouldShowMessages) {
        if (shouldShowMessages && unreadMessages.isNotEmpty()) {
            unreadMessages.forEach { message ->
                try {
                    RetrofitClient.apiService.markMessageAsRead(
                        MessageReadRequest(
                            messageId = message.id,
                            username = username
                        )
                    )
                } catch (e: Exception) {
                    Log.e("ProfileMenu", "Error marking message as read", e)
                }
            }
            hasShownMessages = true
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable { expanded = !expanded }
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            ProfileHeader(
                username = username,
                userType = userType,
                unreadCount = if (!hasShownMessages) unreadMessages.size else 0,
                badgeScale = badgeScale.value,
                rotationState = rotationState,
                expanded = expanded
            )

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(16.dp))

                    if (shouldShowMessages) {
                        MessagesSection(
                            messages = unreadMessages.take(3),
                            username = username,
                            onMessageClick = { /* No need for click handler since messages are auto-marked */ }
                        )

                        if (unreadMessages.size > 3) {
                            TextButton(
                                onClick = { /* Show all messages */ },
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text("پیام‌ها")
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(16.dp))
                    }

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
private fun ProfileHeader(
    username: String,
    userType: String,
    unreadCount: Int,
    badgeScale: Float,
    rotationState: Float,
    expanded: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getIconForUserType(userType),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                if (unreadCount > 0) {
                    Badge(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .scale(badgeScale)
                            .offset(x = 4.dp, y = (-4).dp),
                        containerColor = MaterialTheme.colorScheme.error
                    ) {
                        Text(
                            text = unreadCount.toString(),
                            color = MaterialTheme.colorScheme.onError,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }
                }
            }

            Column {
                Text(
                    text = username,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = getUserTypeDisplay(userType),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (unreadCount > 0) {
                        Text(
                            text = "• $unreadCount پیام جدید",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
        Icon(
            imageVector = Icons.Default.ExpandMore,
            contentDescription = if (expanded) "بستن منو" else "باز کردن منو",
            modifier = Modifier.rotate(rotationState)
        )
    }
}

@Composable
private fun MessagesSection(
    messages: List<Message>,
    username: String,
    onMessageClick: () -> Unit
) {
    Column {
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
                    imageVector = Icons.AutoMirrored.Filled.Message,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "پیام‌های جدید",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        messages.forEach { message ->
            MessageItem(
                message = message,
                username = username,
                onMessageClick = onMessageClick
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
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
fun MessageItem(
    message: Message,
    username: String,
    onMessageClick: () -> Unit
) {
    val isUnread = message.readBy?.contains(username) != true
    val backgroundColor = if (isUnread) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onMessageClick),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = message.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = if (isUnread) FontWeight.Bold else FontWeight.Normal
                )
                if (isUnread) {
                    Badge(
                        containerColor = MaterialTheme.colorScheme.primary
                    ) {
                        Text("جدید")
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = message.body,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = formatMessageDate(message.dateTime),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}

private fun getLastCheckTime(): String {
    val dateTime = ZonedDateTime.now(ZoneId.of("Asia/Tehran"))
        .minusHours(24)
        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
    return dateTime
}

private fun formatMessageDate(dateTime: String): String {
    return try {
        val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val outputFormatter = DateTimeFormatter.ofPattern("HH:mm yyyy/MM/dd")

        val parsedDateTime = LocalDateTime.parse(dateTime, inputFormatter)
        parsedDateTime.format(outputFormatter)
    } catch (e: Exception) {
        Log.e("MessageFormat", "Error formatting date: ${e.message}")
        dateTime
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

    LaunchedEffect(Unit) {
        delay(500)
        textVisible = true
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AnimatedVisibility(
            visible = textVisible,
            enter = fadeIn(animationSpec = tween(1000)) +
                    slideInVertically(animationSpec = spring(dampingRatio = 0.8f)),
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (username.isNotEmpty())
                        "خوش آمدید، $username"
                    else "به سیستم مدیریت هوشمند بارگیری خوش آمدید",
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (username.isNotEmpty())
                        "لطفاً گزینه مورد نظر خود را انتخاب کنید"
                    else "برای دسترسی به امکانات سیستم لطفاً وارد شوید",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
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

    LazyVerticalGrid(
        columns = GridCells.Fixed(columnCount),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(
            count = menuItems.size,
            span = { index ->
                when {
                    // گزینه مدیریت کاربران همیشه عرض کامل
                    menuItems[index].route == "manage_users" -> GridItemSpan(columnCount)
                    // برای تک آیتم، عرض کامل
                    menuItems.size == 1 -> GridItemSpan(columnCount)
                    // برای حالت پیش‌فرض، یک ستون
                    else -> GridItemSpan(1)
                }
            }
        ) { index ->
            val item = menuItems[index]
            AnimatedVisibility(
                visible = showAnimation,
                enter = fadeIn(
                    animationSpec = tween(
                        durationMillis = 300,
                        delayMillis = index * 100
                    )
                ) + expandVertically(
                    animationSpec = spring(
                        dampingRatio = 0.8f,
                        stiffness = Spring.StiffnessLow
                    )
                )
            ) {
                Card(
                    modifier = Modifier
                        .let {
                            when {
                                // مدیریت کاربران یا تک آیتم
                                menuItems.size == 1 || item.route == "manage_users" -> {
                                    it.fillMaxWidth()
                                        .height(120.dp)
                                }
                                // سایر آیتم‌ها
                                else -> {
                                    it.aspectRatio(1f)
                                }
                            }
                        }
                        .animateContentSize()
                        .clickable { onItemClick(item) },
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                    ),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(24.dp)
                            )
                    ) {
                        if (menuItems.size == 1 || item.route == "manage_users") {
                            // لایه افقی برای آیتم‌های تکی و مدیریت کاربران
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 24.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Image(
                                    painter = painterResource(id = item.iconResourceId),
                                    contentDescription = item.title,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .padding(8.dp)
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            // لایه عمودی برای سایر آیتم‌ها
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Image(
                                    painter = painterResource(id = item.iconResourceId),
                                    contentDescription = item.title,
                                    modifier = Modifier
                                        .size(64.dp)
                                        .padding(8.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LoginPrompt(onLoginClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "برای دسترسی به منوها وارد شوید",
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onLoginClick,
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    Text("ورود به سیستم")
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
                users.filter { it.username.contains(searchQuery, ignoreCase = true) }
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

    // Filter users based on search query
    LaunchedEffect(searchQuery, users) {
        filteredUsers = if (searchQuery.isEmpty()) {
            users
        } else {
            sortUsersByType(
                users.filter { it.username.contains(searchQuery, ignoreCase = true) }
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
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .fillMaxHeight(0.8f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                // Header with Add Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                    Text(
                        "مدیریت کاربران",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    if (currentUserType == "admin") {
                        IconButton(
                            onClick = { showAddDialog = true },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Add User",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.size(40.dp))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Search Box
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("جستجوی نام کاربری...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Users List
                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
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

    showDeleteConfirmation?.let { user ->
        DeleteConfirmationDialog(
            user = user,
            onConfirm = {
                scope.launch {
                    try {
                        val request = DeleteUserRequest(userId = user.id)
                        val response = RetrofitClient.apiService.deleteUser(request)
                        if (response.success) {
                            // به‌روزرسانی لیست با حفظ ترتیب
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
            .padding(vertical = 4.dp)
            .animateContentSize(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // User Info Section
            Column(
                modifier = Modifier.weight(1f),
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
                }

                // نمایش نام و نام خانوادگی
                Text(
                    text = user.fullName ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(userTypeColor)
                    )
                    Text(
                        text = getUserTypeDisplay(user.userType),
                        style = MaterialTheme.typography.bodySmall,
                        color = userTypeColor
                    )
                }
            }

            // Action Buttons - فقط برای مدیر اصلی فعال هستند
            if (isMainAdmin) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    IconButton(
                        onClick = onEditClick,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "ویرایش",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(2.dp))

                    IconButton(
                        onClick = onDeleteClick,
                        enabled = user.username != "Prot0nX",
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.error.copy(alpha = 0.1f))
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.error.copy(alpha = 0.2f),
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "حذف",
                            tint = if (user.username == "Prot0nX")
                                MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
                            else MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            } else {
                // اگر کاربر فعلی مدیر اصلی نیست، دکمه‌ها را غیرفعال نشان می‌دهیم
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "ویرایش",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                            modifier = Modifier
                                .size(20.dp)
                                .align(Alignment.Center)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "حذف",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                            modifier = Modifier
                                .size(20.dp)
                                .align(Alignment.Center)
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
    context: Context,
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
    val scale = remember { Animatable(0.8f) }

    LaunchedEffect(Unit) {
        showDialog = true
        scale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }

    val apiService = remember {
        Retrofit.Builder()
            .baseUrl(Constants.getBaseUrl())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

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
                targetValue = 20f,
                animationSpec = tween(durationMillis = 100)
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

    if (showDialog) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = false,
                usePlatformDefaultWidth = false
            )
        ) {
            Surface(
                shape = RoundedCornerShape(32.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .scale(scale.value)
                    .offset(x = errorShakeController.value.dp)
                    .padding(16.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .background(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                            MaterialTheme.colorScheme.surface
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
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "ورود به سیستم",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        AnimatedVisibility(
                            visible = !isLoading,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Text(
                                text = "لطفا اطلاعات خود را وارد کنید",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(32.dp))

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
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Next
                            ),
                            isError = errorMessage != null && username.isEmpty(),
                            shape = RoundedCornerShape(16.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

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
                                    if (showPassword) Icons.Default.Lock else Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = if (password.isNotEmpty())
                                        MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = { showPassword = !showPassword }) {
                                    Icon(
                                        if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = if (showPassword) "Hide password" else "Show password",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            isError = errorMessage != null && password.isEmpty(),
                            shape = RoundedCornerShape(16.dp)
                        )

                        AnimatedVisibility(
                            visible = errorMessage != null,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(vertical = 8.dp)
                                    .fillMaxWidth()
                                    .background(
                                        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = errorMessage ?: "",
                                        color = MaterialTheme.colorScheme.error,
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

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
                                                        // Success animation
                                                        onLoginChecked(
                                                            true,
                                                            responseBody.message,
                                                            responseBody.userType ?: "",
                                                            username
                                                        )
                                                        updateSessionValidity(true)
                                                        (context as? MainActivity)?.startLoadingCheckService()
                                                    } else {
                                                        errorMessage = responseBody.message
                                                    }
                                                } else {
                                                    errorMessage = "پاسخ سرور خالی است"
                                                }
                                            } else {
                                                errorMessage = "خطا در ورود: لطفاً اطلاعات را بررسی کنید"
                                            }
                                        } catch (e: Exception) {
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
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.Login,
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

                        Spacer(modifier = Modifier.height(16.dp))

                        TextButton(
                            onClick = onDismiss,
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isLoading
                        ) {
                            Text(
                                "انصراف",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
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