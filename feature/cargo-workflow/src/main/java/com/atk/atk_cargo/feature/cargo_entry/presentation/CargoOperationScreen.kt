package com.atk.atk_cargo.feature.cargo_entry.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.atk.atk_cargo.feature.cargo_registration.presentation.RegisterCargoScreen
import com.atk.atk_cargo.feature.cargo.viewmodel.CargoViewModel

@Composable
fun CargoOperationScreen(
    navController: NavController,
    viewModel: CargoViewModel,
    onSessionInvalid: () -> Unit
) {
    val cargoUiState by viewModel.uiState.collectAsStateWithLifecycle()
    val initialInfo = cargoUiState.initialInfo
    val cargoInfoList = cargoUiState.cargoInfoList
    val resultMessage by viewModel.resultMessage.collectAsStateWithLifecycle()
    val showAnimatedMessage by viewModel.showAnimatedMessage.collectAsStateWithLifecycle()
    val messageType by viewModel.messageType.collectAsStateWithLifecycle()

    AnimatedContent(
        targetState = initialInfo != null,
        transitionSpec = {
            fadeIn() togetherWith fadeOut()
        },
        label = "CargoOperationTransition"
    ) { isInitialInfoSelected ->
        if (!isInitialInfoSelected) {
            SelectInfoScreenContent(
                navController = navController,
                viewModel = viewModel,
                onSessionInvalid = onSessionInvalid
            )
        } else {
            RegisterCargoScreen(
                initialInfo = initialInfo,
                cargoInfoList = cargoInfoList,
                resultMessage = resultMessage,
                showAnimatedMessage = showAnimatedMessage,
                messageType = messageType,
                viewModel = viewModel,
                onChangeSelectionClick = {
                    viewModel.resetCurrentSelection()
                }
            )
        }
    }
}
