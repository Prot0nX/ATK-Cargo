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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
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
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.onEach
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {

    private val startupViewModel: StartupViewModel by viewModel()

    private val notificationPermissionLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (BuildConfig.DEBUG) {
            Log.d("MainActivity", "مجوز POST_NOTIFICATIONS: ${if (isGranted) "اعطا شد" else "رد شد"}")
        }
        if (!isGranted && !shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
            showMessage("برای دریافت اعلان‌ها، مجوز نوتیفیکیشن را از تنظیمات برنامه فعال کنید")
        }
    }

    private var isThemeColorLoaded = false

    @SuppressLint("FlowOperatorInvokedInComposition")
    override fun onCreate(savedInstanceState: Bundle?) {

        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        splashScreen.setKeepOnScreenCondition { !isThemeColorLoaded }

        try {
            setContent {
                LaunchedEffect(Unit) {
                    startupViewModel.handleIntent(intent)
                    startupViewModel.runStartupSequenceOnce()
                }

                LaunchedEffect(Unit) {

                    lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                        startupViewModel.events.collect { event ->
                            when (event) {
                                is StartupEvent.ShowMessage -> showMessage(event.message)
                                is StartupEvent.RequestBatteryOptimization -> requestBatteryOptimization()
                            }
                        }
                    }
                }

                val themeColorLong by startupViewModel.themeColor
                    .onEach { isThemeColorLoaded = true }
                    .collectAsState(initial = UserPreferencesManager.DEFAULT_THEME_COLOR)
                val primaryColor = Color(themeColorLong)

                CompositionLocalProvider(
                    LocalStartupViewModel provides startupViewModel,
                    LocalNotificationPermissionRequester provides ::checkNotificationPermission
                ) {
                    ATKCargoTheme(primaryColor = primaryColor) {
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

                                    onExit = { finishAndRemoveTask() }
                                )
                            }
                            is StartupState.SecurityBlocked -> {
                                SecurityBlockScreen(
                                    isLoading = state.isLoading,
                                    errorType = state.errorType,
                                    onRetry = { startupViewModel.retrySecurityCheck() }
                                )
                            }
                            is StartupState.Ready -> {
                                HandleMainContent()
                            }
                        }
                    }
                }
            }

        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("MainActivity", "خطای بحرانی در راه‌اندازی برنامه: ${e.message}", e)
            isThemeColorLoaded = true
            setContent {
                ATKCargoTheme(primaryColor = Color(UserPreferencesManager.DEFAULT_THEME_COLOR)) {
                    StartupErrorScreen(
                        onRetry = { recreate() },
                        onExit = { finishAndRemoveTask() }
                    )
                }
            }
        }
    }

    @Composable
    private fun HandleMainContent() {
        MainScreen()

        val updateManager = remember { startupViewModel.getUpdateManager() }
        val isUpdateAvailable by startupViewModel.isUpdateAvailable.collectAsState()
        val updateInfo by updateManager.updateInfo.collectAsState()
        val info = updateInfo

        if (isUpdateAvailable && info != null) {
            var isDialogDismissed by remember { mutableStateOf(false) }

            if (!isDialogDismissed || info.forceUpdate) {
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
                    updateInfo = info,
                    downloadProgress = downloadProgress,
                    downloadState = downloadState,
                    onUpdateClick = { updateManager.startDownload(info.downloadUrl) },
                    onPauseClick = { updateManager.pauseDownload() },
                    onResumeClick = { updateManager.resumeDownload() },
                    onCancelClick = { updateManager.cancelDownload() },
                    onDismiss = { if (!info.forceUpdate) isDialogDismissed = true }
                )
            }
        }
    }

    @SuppressLint("BatteryLife")
    private fun requestBatteryOptimization() {
        try {
            val intent = Intent(android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = "package:$packageName".toUri()
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
            showMessage("لطفاً اجازه دهید برنامه بدون محدودیت باتری اجرا شود")
        } catch (_: android.content.ActivityNotFoundException) {
            try {
                val batteryIntent = Intent(android.provider.Settings.ACTION_BATTERY_SAVER_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                startActivity(batteryIntent)
                showMessage("لطفاً برنامه را از محدودیت‌های بهینه‌سازی باتری خارج کنید")
            } catch (_: android.content.ActivityNotFoundException) {
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

@Composable
private fun StartupErrorScreen(onRetry: () -> Unit, onExit: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "متأسفانه در راه‌اندازی برنامه خطایی رخ داد",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(24.dp))
            Button(onClick = onRetry) {
                Text("تلاش مجدد")
            }
            Spacer(Modifier.height(12.dp))
            OutlinedButton(onClick = onExit) {
                Text("خروج از برنامه")
            }
        }
    }
}
