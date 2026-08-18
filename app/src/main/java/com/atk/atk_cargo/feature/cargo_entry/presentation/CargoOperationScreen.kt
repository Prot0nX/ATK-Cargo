package com.atk.atk_cargo.feature.cargo_entry.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import com.atk.atk_cargo.feature.cargo_registration.presentation.RegisterCargoScreen
import com.atk.atk_cargo.ui.viewmodel.CargoViewModel

@Composable
fun CargoOperationScreen(
    navController: NavController,
    viewModel: CargoViewModel
) {
    val initialInfo by viewModel.initialInfo.collectAsStateWithLifecycle()
    val cargoInfoList by viewModel.cargoInfoList.collectAsStateWithLifecycle()
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
                viewModel = viewModel
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
