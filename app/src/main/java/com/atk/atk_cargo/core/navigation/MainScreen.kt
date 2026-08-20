package com.atk.atk_cargo.core.navigation

// ===== FEATURE NAVIGATION IMPORTS =====
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.EaseInCubic
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.atk.atk_cargo.api.PermissionPoller
import com.atk.atk_cargo.api.UserPreferencesManager
import com.atk.atk_cargo.core.startup.LocalNotificationPermissionRequester
import com.atk.atk_cargo.core.startup.LocalStartupViewModel
import com.atk.atk_cargo.feature.admin.presentation.UserManagementDialog
import com.atk.atk_cargo.feature.auth.presentation.LoginScreen
import com.atk.atk_cargo.feature.auth.viewmodel.AuthViewModel
import com.atk.atk_cargo.feature.cargo_counter.navigation.CargoCounterRoute
import com.atk.atk_cargo.feature.cargo_details.navigation.cargoDetailsScreen
import com.atk.atk_cargo.feature.cargo_entry.navigation.InitialInfoRoute
import com.atk.atk_cargo.feature.cargo_entry.navigation.SelectInfoRoute
import com.atk.atk_cargo.feature.cargo_entry.presentation.InitialInfoScreen
import com.atk.atk_cargo.feature.cargo_entry.presentation.InitialInfoViewModel
import com.atk.atk_cargo.feature.cargo_registration.navigation.cargoRegistrationScreen
import com.atk.atk_cargo.feature.chat.navigation.AdminChatRoute
import com.atk.atk_cargo.feature.chat.navigation.navigateToAdminChat
import com.atk.atk_cargo.feature.chat.presentation.ChatScreen
import com.atk.atk_cargo.feature.home.navigation.HomeRoute
import com.atk.atk_cargo.feature.home.navigation.homeScreen
import com.atk.atk_cargo.feature.home.navigation.navigateToHome
import com.atk.atk_cargo.feature.reports.navigation.ManageShipsRoute
import com.atk.atk_cargo.ui.screens.ManageReportsScreen
import com.atk.atk_cargo.ui.viewmodel.CargoViewModel
import com.atk.atk_cargo.ui.viewmodel.ReportsViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val userPreferencesManager = koinInject<UserPreferencesManager>()
    val username by userPreferencesManager.username.collectAsStateWithLifecycle(initialValue = "")
    val userType by userPreferencesManager.userType.collectAsStateWithLifecycle(initialValue = "")

    val permissionPoller = remember { PermissionPoller(userPreferencesManager) }
    val livePermissions by permissionPoller.livePermissions.collectAsStateWithLifecycle()
    val storedPermissions by userPreferencesManager.permissions.collectAsStateWithLifecycle(initialValue = emptyMap())
    val userPermissions = livePermissions.ifEmpty { storedPermissions }

    var showUserManagement by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val startupViewModel = LocalStartupViewModel.current
    val isSessionValid by startupViewModel.isSessionValid.collectAsStateWithLifecycle()
    val pendingNavigationDestination by startupViewModel.pendingNavigationDestination.collectAsStateWithLifecycle()

    LaunchedEffect(pendingNavigationDestination, isSessionValid) {
        if (isSessionValid && pendingNavigationDestination == "admin_chat") {
            try {
                navController.navigateToAdminChat()
                startupViewModel.consumePendingNavigation()
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

    DisposableEffect(permissionPoller) {
        onDispose { permissionPoller.destroy() }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { paddingValues ->
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
                                    val requestNotificationPermission = LocalNotificationPermissionRequester.current
                                    val authViewModel: AuthViewModel = koinViewModel()
                                    // پس از خروج کاربر، AuthViewModel همچنان در حالت Success
                                    // کش شده است. بدون ریست، LoginScreen بلافاصله onLoginSuccess
                                    // را صدا زده و حلقه بی‌نهایت ایجاد می‌شود (صفحه سفید).
                                    LaunchedEffect(Unit) {
                                        authViewModel.resetState()
                                    }
                                    LoginScreen(
                                        viewModel = authViewModel,
                                        onLoginSuccess = {
                                            startupViewModel.updateSessionValidity(true)
                                            requestNotificationPermission()
                                            startupViewModel.startLoadingNotificationService()
                                        }
                                    )
                                } else {
                                    NavHost(
                                        navController = navController,
                                        startDestination = HomeRoute
                                    ) {
                                        homeScreen(
                                            navController = navController,
                                            username = username,
                                            userType = userType,
                                            userPermissions = userPermissions,
                                            onManageUsersClick = { showUserManagement = true },
                                            warningsCount = 0
                                        )
                                        val initialInfoTransitions = standardTransitions(initialScale = 0.90f, targetScale = 1.06f)
                                        composable<InitialInfoRoute>(
                                            enterTransition = initialInfoTransitions.enter,
                                            exitTransition = initialInfoTransitions.exit,
                                            popEnterTransition = initialInfoTransitions.popEnter,
                                            popExitTransition = initialInfoTransitions.popExit
                                        ) {
                                            val initialInfoViewModel: InitialInfoViewModel = koinViewModel()
                                            InitialInfoScreen(navController = navController, viewModel = initialInfoViewModel)
                                        }
                                        val selectInfoTransitions = standardTransitions(initialScale = 0.88f, targetScale = 1.08f)
                                        composable<SelectInfoRoute>(
                                            enterTransition = selectInfoTransitions.enter,
                                            exitTransition = selectInfoTransitions.exit,
                                            popEnterTransition = selectInfoTransitions.popEnter,
                                            popExitTransition = selectInfoTransitions.popExit
                                        ) {
                                            val cargoViewModel: CargoViewModel = koinViewModel()
                                            com.atk.atk_cargo.feature.cargo_entry.presentation.CargoOperationScreen(navController = navController, viewModel = cargoViewModel, onSessionInvalid = { navController.navigateToHome() })
                                        }
                                        cargoRegistrationScreen(
                                            navController = navController
                                        )
                                        val cargoCounterTransitions = standardTransitions(initialScale = 0.90f, targetScale = 1.06f)
                                        composable<CargoCounterRoute>(
                                            enterTransition = cargoCounterTransitions.enter,
                                            exitTransition = cargoCounterTransitions.exit,
                                            popEnterTransition = cargoCounterTransitions.popEnter,
                                            popExitTransition = cargoCounterTransitions.popExit
                                        ) {
                                            val cargoViewModel: CargoViewModel = koinViewModel()
                                            com.atk.atk_cargo.feature.cargo_counter.presentation.CargoCounterOperationScreen(navController = navController, viewModel = cargoViewModel, onSessionInvalid = { navController.navigateToHome() })
                                        }
                                        val manageShipsTransitions = standardTransitions(initialScale = 0.86f, targetScale = 1.10f)
                                        composable<ManageShipsRoute>(
                                            enterTransition = manageShipsTransitions.enter,
                                            exitTransition = manageShipsTransitions.exit,
                                            popEnterTransition = manageShipsTransitions.popEnter,
                                            popExitTransition = manageShipsTransitions.popExit
                                        ) {
                                            val reportsViewModel: ReportsViewModel = koinViewModel()
                                            ManageReportsScreen(viewModel = reportsViewModel, navController = navController)
                                        }
                                        val adminChatTransitions = standardTransitions(initialScale = 0.90f, targetScale = 1.10f)
                                        composable<AdminChatRoute>(
                                            enterTransition = adminChatTransitions.enter,
                                            exitTransition = adminChatTransitions.exit,
                                            popEnterTransition = adminChatTransitions.popEnter,
                                            popExitTransition = adminChatTransitions.popExit
                                        ) {
                                            ChatScreen(
                                                userPreferencesManager = userPreferencesManager,
                                                onBackClick = { navController.popBackStack() }
                                            )
                                        }
                                        cargoDetailsScreen(navController = navController, onSessionInvalid = { navController.navigateToHome() })
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

        if (showUserManagement) {
            UserManagementDialog(
                onDismiss = { showUserManagement = false }
            )
        }
    }
}

/** انیمیشن‌های ورود/خروج مسیرهای NavHost که قبلاً به‌صورت ۶ بلاک ۶۰ خطی تکراری نوشته می‌شدند. */
private data class RouteTransitions(
    val enter: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition,
    val exit: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition,
    val popEnter: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition,
    val popExit: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition
)

private fun standardTransitions(initialScale: Float, targetScale: Float): RouteTransitions = RouteTransitions(
    enter = {
        fadeIn(animationSpec = tween(425, easing = EaseOutCubic)) +
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(425, easing = EaseOutCubic)) +
                scaleIn(initialScale = initialScale, animationSpec = tween(425, easing = EaseOutCubic))
    },
    exit = {
        fadeOut(animationSpec = tween(275, easing = EaseInCubic)) +
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(275, easing = EaseInCubic)) +
                scaleOut(targetScale = targetScale, animationSpec = tween(275, easing = EaseInCubic))
    },
    popEnter = {
        fadeIn(animationSpec = tween(425, easing = EaseOutCubic)) +
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(425, easing = EaseOutCubic)) +
                scaleIn(initialScale = initialScale, animationSpec = tween(425, easing = EaseOutCubic))
    },
    popExit = {
        fadeOut(animationSpec = tween(275, easing = EaseInCubic)) +
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(275, easing = EaseInCubic)) +
                scaleOut(targetScale = targetScale, animationSpec = tween(275, easing = EaseInCubic))
    }
)
