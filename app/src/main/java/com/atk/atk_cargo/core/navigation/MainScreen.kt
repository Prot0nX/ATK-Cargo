package com.atk.atk_cargo.core.navigation

// ===== FEATURE NAVIGATION IMPORTS =====
import android.annotation.SuppressLint
import android.os.Build
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.EaseInCubic
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.atk.atk_cargo.MainActivity
import com.atk.atk_cargo.api.LogoutRequest
import com.atk.atk_cargo.api.PermissionPoller
import com.atk.atk_cargo.api.RetrofitClient
import com.atk.atk_cargo.api.UserPreferencesManager
import com.atk.atk_cargo.feature.admin.presentation.UserManagementDialog
import com.atk.atk_cargo.feature.auth.navigation.loginScreen
import com.atk.atk_cargo.feature.auth.presentation.LoginScreen
import com.atk.atk_cargo.feature.cargo_counter.navigation.CargoCounterRoute
import com.atk.atk_cargo.feature.cargo_counter.presentation.CargoCounterScreen
import com.atk.atk_cargo.feature.cargo_entry.navigation.InitialInfoRoute
import com.atk.atk_cargo.feature.cargo_entry.navigation.SelectInfoRoute
import com.atk.atk_cargo.feature.cargo_entry.presentation.InitialInfoScreen
import com.atk.atk_cargo.feature.cargo_entry.presentation.SelectInfoScreenContent
import com.atk.atk_cargo.feature.cargo_registration.navigation.cargoRegistrationScreen
import com.atk.atk_cargo.feature.chat.navigation.AdminChatRoute
import com.atk.atk_cargo.feature.chat.navigation.navigateToAdminChat
import com.atk.atk_cargo.feature.chat.presentation.ChatScreen
import com.atk.atk_cargo.feature.home.navigation.HomeRoute
import com.atk.atk_cargo.feature.home.navigation.homeScreen
import com.atk.atk_cargo.feature.reports.navigation.ManageShipsRoute
import com.atk.atk_cargo.feature.reports.navigation.cargoDetailsScreen
import com.atk.atk_cargo.feature.startup.presentation.SplashScreen
import com.atk.atk_cargo.ui.screens.ManageReportsScreen
import com.atk.atk_cargo.ui.viewmodel.CargoViewModel
import com.atk.atk_cargo.ui.viewmodel.ReportsViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import java.util.UUID

@SuppressLint("ContextCastToActivity", "HardwareIds")
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    var showSplash by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val userPreferencesManager = remember { UserPreferencesManager(context) }
    val username by userPreferencesManager.username.collectAsState(initial = "")
    val userType by userPreferencesManager.userType.collectAsState(initial = "")

    val permissionPoller = remember { PermissionPoller(userPreferencesManager) }
    val livePermissions by permissionPoller.livePermissions.collectAsState()
    val storedPermissions by userPreferencesManager.permissions.collectAsState(initial = emptyMap())
    val userPermissions = livePermissions.ifEmpty { storedPermissions }

    var showUserManagement by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val mainActivity = LocalContext.current as MainActivity
    val isSessionValid by mainActivity.isSessionValid.collectAsState()

    LaunchedEffect(mainActivity.pendingNavigationDestination, isSessionValid) {
        if (isSessionValid && mainActivity.pendingNavigationDestination == "admin_chat") {
            try {
                navController.navigateToAdminChat()
                mainActivity.pendingNavigationDestination = null
            } catch (_: Exception) {
            }
        }
    }

    LaunchedEffect(isSessionValid) {
        if (isSessionValid) {
            permissionPoller.start()
        } else {
            permissionPoller.stop()
        }
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
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                            ) {
                                if (!isSessionValid) {
                                    LoginScreen(
                                        onLoginSuccess = {
                                            mainActivity.updateSessionValidity(true)
                                            mainActivity.startLoadingNotificationService()
                                        }
                                    )
                                } else {
                                    NavHost(
                                        navController = navController,
                                        startDestination = HomeRoute
                                    ) {
                                        loginScreen(
                                            onLoginSuccess = {
                                                mainActivity.updateSessionValidity(true)
                                                mainActivity.startLoadingNotificationService()
                                            }
                                        )
                                        homeScreen(
                                            navController = navController,
                                            username = username,
                                            userType = userType,
                                            userPermissions = userPermissions,
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
                                                            Toast.makeText(mainActivity, "خروج با موفقیت انجام شد", Toast.LENGTH_SHORT).show()
                                                        } else {
                                                            val errorMessage = when (response.code()) {
                                                                400 -> "❌ درخواست نامعتبر"
                                                                401 -> "🔐 جلسه منقضی شده است"
                                                                404 -> "⚠️ جلسه فعالی یافت نشد"
                                                                500 -> "🔧 خطای داخلی سرور"
                                                                else -> "خطا در خروج (کد: ${response.code()})"
                                                            }
                                                            userPreferencesManager.clearUserCredentials()
                                                            Toast.makeText(mainActivity, errorMessage, Toast.LENGTH_SHORT).show()
                                                        }
                                                    } catch (_: Exception) {
                                                        userPreferencesManager.clearUserCredentials()
                                                        Toast.makeText(mainActivity, "خروج انجام شد", Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                            },
                                            onManageUsersClick = { showUserManagement = true },
                                            warningsCount = 0
                                        )
                                        composable<InitialInfoRoute>(
                                            enterTransition = {
                                                fadeIn(animationSpec = tween(425, easing = EaseOutCubic)) +
                                                        slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(425, easing = EaseOutCubic)) +
                                                        scaleIn(initialScale = 0.90f, animationSpec = tween(425, easing = EaseOutCubic))
                                            },
                                            exitTransition = {
                                                fadeOut(animationSpec = tween(275, easing = EaseInCubic)) +
                                                        slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(275, easing = EaseInCubic)) +
                                                        scaleOut(targetScale = 1.06f, animationSpec = tween(275, easing = EaseInCubic))
                                            },
                                            popEnterTransition = {
                                                fadeIn(animationSpec = tween(425, easing = EaseOutCubic)) +
                                                        slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(425, easing = EaseOutCubic)) +
                                                        scaleIn(initialScale = 0.90f, animationSpec = tween(425, easing = EaseOutCubic))
                                            },
                                            popExitTransition = {
                                                fadeOut(animationSpec = tween(275, easing = EaseInCubic)) +
                                                        slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(275, easing = EaseInCubic)) +
                                                        scaleOut(targetScale = 1.06f, animationSpec = tween(275, easing = EaseInCubic))
                                            }
                                        ) {
                                            InitialInfoScreen(navController = navController)
                                        }
                                        composable<SelectInfoRoute>(
                                            enterTransition = {
                                                fadeIn(animationSpec = tween(425, easing = EaseOutCubic)) +
                                                        slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(425, easing = EaseOutCubic)) +
                                                        scaleIn(initialScale = 0.88f, animationSpec = tween(425, easing = EaseOutCubic))
                                            },
                                            exitTransition = {
                                                fadeOut(animationSpec = tween(275, easing = EaseInCubic)) +
                                                        slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(275, easing = EaseInCubic)) +
                                                        scaleOut(targetScale = 1.08f, animationSpec = tween(275, easing = EaseInCubic))
                                            },
                                            popEnterTransition = {
                                                fadeIn(animationSpec = tween(425, easing = EaseOutCubic)) +
                                                        slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(425, easing = EaseOutCubic)) +
                                                        scaleIn(initialScale = 0.88f, animationSpec = tween(425, easing = EaseOutCubic))
                                            },
                                            popExitTransition = {
                                                fadeOut(animationSpec = tween(275, easing = EaseInCubic)) +
                                                        slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(275, easing = EaseInCubic)) +
                                                        scaleOut(targetScale = 1.08f, animationSpec = tween(275, easing = EaseInCubic))
                                            }
                                        ) {
                                            val cargoViewModel: CargoViewModel = koinViewModel()
                                            com.atk.atk_cargo.feature.cargo_entry.presentation.CargoOperationScreen(navController = navController, viewModel = cargoViewModel)
                                        }
                                        cargoRegistrationScreen(
                                            navController = navController
                                        )
                                        composable<CargoCounterRoute>(
                                            enterTransition = {
                                                fadeIn(animationSpec = tween(425, easing = EaseOutCubic)) +
                                                        slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(425, easing = EaseOutCubic)) +
                                                        scaleIn(initialScale = 0.90f, animationSpec = tween(425, easing = EaseOutCubic))
                                            },
                                            exitTransition = {
                                                fadeOut(animationSpec = tween(275, easing = EaseInCubic)) +
                                                        slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(275, easing = EaseInCubic)) +
                                                        scaleOut(targetScale = 1.06f, animationSpec = tween(275, easing = EaseInCubic))
                                            },
                                            popEnterTransition = {
                                                fadeIn(animationSpec = tween(425, easing = EaseOutCubic)) +
                                                        slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(425, easing = EaseOutCubic)) +
                                                        scaleIn(initialScale = 0.90f, animationSpec = tween(425, easing = EaseOutCubic))
                                            },
                                            popExitTransition = {
                                                fadeOut(animationSpec = tween(275, easing = EaseInCubic)) +
                                                        slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(275, easing = EaseInCubic)) +
                                                        scaleOut(targetScale = 1.06f, animationSpec = tween(275, easing = EaseInCubic))
                                            }
                                        ) {
                                            CargoCounterScreen(navController = navController)
                                        }
                                        composable<ManageShipsRoute>(
                                            enterTransition = {
                                                fadeIn(animationSpec = tween(425, easing = EaseOutCubic)) +
                                                        slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(425, easing = EaseOutCubic)) +
                                                        scaleIn(initialScale = 0.86f, animationSpec = tween(425, easing = EaseOutCubic))
                                            },
                                            exitTransition = {
                                                fadeOut(animationSpec = tween(275, easing = EaseInCubic)) +
                                                        slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(275, easing = EaseInCubic)) +
                                                        scaleOut(targetScale = 1.10f, animationSpec = tween(275, easing = EaseInCubic))
                                            },
                                            popEnterTransition = {
                                                fadeIn(animationSpec = tween(425, easing = EaseOutCubic)) +
                                                        slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(425, easing = EaseOutCubic)) +
                                                        scaleIn(initialScale = 0.86f, animationSpec = tween(425, easing = EaseOutCubic))
                                            },
                                            popExitTransition = {
                                                fadeOut(animationSpec = tween(275, easing = EaseInCubic)) +
                                                        slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(275, easing = EaseInCubic)) +
                                                        scaleOut(targetScale = 1.10f, animationSpec = tween(275, easing = EaseInCubic))
                                            }
                                        ) {
                                            val reportsViewModel: ReportsViewModel = koinViewModel()
                                            ManageReportsScreen(viewModel = reportsViewModel, navController = navController)
                                        }
                                        composable<AdminChatRoute>(
                                            enterTransition = {
                                                fadeIn(animationSpec = tween(425, easing = EaseOutCubic)) +
                                                        slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(425, easing = EaseOutCubic)) +
                                                        scaleIn(initialScale = 0.90f, animationSpec = tween(425, easing = EaseOutCubic))
                                            },
                                            exitTransition = {
                                                fadeOut(animationSpec = tween(275, easing = EaseInCubic)) +
                                                        slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(275, easing = EaseInCubic)) +
                                                        scaleOut(targetScale = 1.10f, animationSpec = tween(275, easing = EaseInCubic))
                                            },
                                            popEnterTransition = {
                                                fadeIn(animationSpec = tween(425, easing = EaseOutCubic)) +
                                                        slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(425, easing = EaseOutCubic)) +
                                                        scaleIn(initialScale = 0.90f, animationSpec = tween(425, easing = EaseOutCubic))
                                            },
                                            popExitTransition = {
                                                fadeOut(animationSpec = tween(275, easing = EaseInCubic)) +
                                                        slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(275, easing = EaseInCubic)) +
                                                        scaleOut(targetScale = 1.10f, animationSpec = tween(275, easing = EaseInCubic))
                                            }
                                        ) {
                                            ChatScreen(
                                                userPreferencesManager = userPreferencesManager,
                                                onBackClick = { navController.popBackStack() }
                                            )
                                        }
                                        cargoDetailsScreen(navController = navController)
                                    }
                                }
                            }

                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = Color.Transparent
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    HorizontalDivider(
                                        modifier = Modifier.fillMaxWidth(0.9f),
                                        thickness = 0.5.dp,
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                                    )

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 14.dp),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            "امین تجار خوزستان",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontWeight = FontWeight.SemiBold,
                                                letterSpacing = 0.5.sp
                                            ),
                                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                                        )

                                        Box(
                                            modifier = Modifier
                                                .padding(horizontal = 10.dp)
                                                .size(3.dp)
                                                .background(
                                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                                    CircleShape
                                                )
                                        )

                                        Text(
                                            "سامانه هوشمند هانگار",
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
        }

        if (showUserManagement) {
            UserManagementDialog(
                onDismiss = { showUserManagement = false }
            )
        }
    }
}
