package com.atk.atk_cargo.feature.home.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.atk.atk_cargo.feature.home.presentation.HomeScreen
import kotlinx.serialization.Serializable

@Serializable
object HomeRoute

fun NavController.navigateToHome() {
    navigate(HomeRoute) {
        popUpTo(HomeRoute) { inclusive = true }
    }
}

fun NavGraphBuilder.homeScreen(
    navController: NavController,
    username: String,
    userType: String,
    userPermissions: Map<String, Boolean>,
    onLogoutClick: () -> Unit,
    onManageUsersClick: () -> Unit,
    warningsCount: Int
) {
    composable<HomeRoute> {
        HomeScreen(
            navController = navController,
            username = username,
            userType = userType,
            userPermissions = userPermissions,
            isSessionValid = true,
            onLogoutClick = onLogoutClick,
            onManageUsersClick = onManageUsersClick
        )
    }
}
