package com.atk.atk_cargo.feature.auth.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.atk.atk_cargo.feature.auth.presentation.LoginScreen
import kotlinx.serialization.Serializable

@Serializable
object LoginRoute

fun NavController.navigateToLogin() {
    navigate(LoginRoute) {
        popUpTo(0) { inclusive = true }
    }
}

fun NavGraphBuilder.loginScreen(
    onLoginSuccess: () -> Unit
) {
    composable<LoginRoute> {
        LoginScreen(
            onLoginSuccess = onLoginSuccess
        )
    }
}
