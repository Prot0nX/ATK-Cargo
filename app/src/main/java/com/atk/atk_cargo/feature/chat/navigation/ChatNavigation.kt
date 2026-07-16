package com.atk.atk_cargo.feature.chat.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.atk.atk_cargo.api.UserPreferencesManager
import com.atk.atk_cargo.feature.chat.presentation.ChatScreen
import kotlinx.serialization.Serializable

@Serializable
object AdminChatRoute

fun NavController.navigateToAdminChat() {
    navigate(AdminChatRoute)
}

fun NavGraphBuilder.adminChatScreen(
    navController: NavController,
    userPreferencesManager: UserPreferencesManager
) {
    composable<AdminChatRoute> {
        ChatScreen(
            userPreferencesManager = userPreferencesManager,
            onBackClick = { navController.popBackStack() }
        )
    }
}
