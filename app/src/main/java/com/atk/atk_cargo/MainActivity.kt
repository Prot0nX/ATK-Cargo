package com.atk.atk_cargo

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.atk.atk_cargo.api.UpdateInfo
import com.atk.atk_cargo.api.UpdateManager
import com.atk.atk_cargo.api.UserPreferencesManager
import com.atk.atk_cargo.core.navigation.MainScreen
import com.atk.atk_cargo.core.startup.LocalNotificationPermissionRequester
import com.atk.atk_cargo.core.startup.LocalStartupViewModel
import com.atk.atk_cargo.core.startup.StartupEvent
import com.atk.atk_cargo.core.startup.StartupState
import com.atk.atk_cargo.core.startup.StartupViewModel
import com.atk.atk_cargo.feature.startup.presentation.ServerSyncingScreen
import com.atk.atk_cargo.feature.startup.presentation.SplashScreen
import com.atk.atk_cargo.feature.update.presentation.UpdateDialog
import com.atk.atk_cargo.security.SecurityBlockScreen
import com.atk.atk_cargo.security.VersionExpiredDialog
import com.atk.atk_cargo.ui.theme.ATKCargoTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {

    private val userPreferencesManager: UserPreferencesManager by inject()
    private val startupViewModel: StartupViewModel by viewModel()

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
            // خواندن blocking و یک‌باره‌ی رنگ تم قبل از setContent — بدون این کار،
            // فریم اول با رنگ پیش‌فرض رندر می‌شد و به‌محض emit شدن مقدار واقعی از
            // DataStore، کل درخت UI recompose می‌شد (پرش رنگ قابل مشاهده)
            val initialThemeColor = kotlinx.coroutines.runBlocking {
                userPreferencesManager.themeColor.first()
            }

            setContent {
                LaunchedEffect(Unit) {
                    startupViewModel.handleIntent(intent)
                    startupViewModel.runStartupSequenceOnce()
                }

                LaunchedEffect(Unit) {
                    startupViewModel.events.collect { event ->
                        when (event) {
                            is StartupEvent.ShowMessage -> showMessage(event.message)
                            is StartupEvent.RequestBatteryOptimization -> requestBatteryOptimization()
                        }
                    }
                }

                val themeColorLong by userPreferencesManager.themeColor.collectAsState(initial = initialThemeColor)
                val primaryColor = Color(themeColorLong)

                CompositionLocalProvider(
                    LocalStartupViewModel provides startupViewModel,
                    LocalNotificationPermissionRequester provides ::checkNotificationPermission
                ) {
                    ATKCargoTheme(primaryColor = primaryColor) {
                        val retryScope = rememberCoroutineScope()
                        val startupState by startupViewModel.startupState.collectAsState()

                        when (val state = startupState) {
                            is StartupState.Splash -> {
                                SplashScreen(onSkip = { startupViewModel.skipSplash() })
                            }
                            is StartupState.Syncing -> {
                                ServerSyncingScreen()
                            }
                            is StartupState.VersionExpired -> {
                                VersionExpiredDialog(
                                    onExit = { android.os.Process.killProcess(android.os.Process.myPid()) }
                                )
                            }
                            is StartupState.SecurityBlocked -> {
                                SecurityBlockScreen(
                                    isLoading = state.isLoading,
                                    errorType = state.errorType,
                                    onRetry = {
                                        retryScope.launch { startupViewModel.retrySecurityCheck() }
                                    }
                                )
                            }
                            is StartupState.Ready -> {
                                HandleMainContent()
                            }
                        }
                    }
                }
            }

        } catch (e: Exception) {
            // خطای کلی در راه‌اندازی برنامه — باید لاگ شود، وگرنه کاربر فقط یک صفحه‌ی
            // سفید بدون هیچ نشانه‌ای می‌بیند و عیب‌یابی در میدان غیرممکن می‌شود
            Log.e("MainActivity", "خطای بحرانی در راه‌اندازی برنامه: ${e.message}", e)
        }
    }

    @androidx.compose.runtime.Composable
    private fun HandleMainContent() {
        val updateManager = remember { startupViewModel.getUpdateManager() }
        val isUpdateAvailable by startupViewModel.isUpdateAvailable.collectAsState()
        val updateInfo by updateManager.updateInfo.collectAsState()

        if (isUpdateAvailable && updateInfo != null) {
            val downloadProgress by updateManager.downloadProgress.collectAsState()
            val downloadState by updateManager.downloadState.collectAsState()

            LaunchedEffect(downloadState) {
                when (val state = downloadState) {
                    is UpdateManager.DownloadState.Completed -> {
                        updateManager.installUpdate(updateManager.getDownloadedFile())
                    }
                    is UpdateManager.DownloadState.Error -> {
                        showMessage(state.message)
                    }
                    else -> { /* Other states don't require specific handling */ }
                }
            }

            UpdateDialog(
                updateInfo = updateInfo as UpdateInfo,
                downloadProgress = downloadProgress,
                downloadState = downloadState,
                onUpdateClick = { updateInfo?.downloadUrl?.let { updateManager.startDownload(it) } },
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
    private fun requestBatteryOptimization() {
        try {
            val intent = Intent(android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = "package:$packageName".toUri()
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
                showMessage("لطفاً اجازه دهید برنامه بدون محدودیت باتری اجرا شود")
            } else {
                val batteryIntent = Intent(android.provider.Settings.ACTION_BATTERY_SAVER_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                startActivity(batteryIntent)
                showMessage("لطفاً برنامه را از محدودیت‌های بهینه‌سازی باتری خارج کنید")
            }
        } catch (_: Exception) {
            try {
                startActivity(Intent(android.provider.Settings.ACTION_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                })
                showMessage("لطفاً در تنظیمات، برنامه را از محدودیت‌های باتری خارج کنید")
            } catch (e: Exception) {
                Log.e("BatteryOptimization", "خطا در باز کردن تنظیمات: ${e.message}")
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        startupViewModel.handleIntent(intent)
    }

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
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
}
