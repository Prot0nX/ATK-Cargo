package com.atk.atk_cargo.feature.cargo_counter.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.atk.atk_cargo.feature.cargo_details.presentation.CargoDetailsScreen
import com.atk.atk_cargo.feature.cargo.viewmodel.CargoViewModel

@Composable
fun CargoCounterOperationScreen(
    navController: NavController,
    viewModel: CargoViewModel,
    onSessionInvalid: () -> Unit
) {
    val cargoUiState by viewModel.uiState.collectAsStateWithLifecycle()
    val initialInfo = cargoUiState.initialInfo

    AnimatedContent(
        targetState = initialInfo != null,
        transitionSpec = {
            fadeIn() togetherWith fadeOut()
        },
        label = "CargoCounterOperationTransition"
    ) { isInitialInfoSelected ->
        if (!isInitialInfoSelected) {
            CargoCounterScreen(
                navController = navController,
                onSessionInvalid = onSessionInvalid,
                sharedViewModel = viewModel
            )
        } else {
            CargoDetailsScreen(
                navController = navController,
                onSessionInvalid = onSessionInvalid,
                quotaNumber = initialInfo?.loadingQuotaNumber?.toString() ?: "",
                shippingCompany = initialInfo?.shippingCompany ?: "",
                warehouse = initialInfo?.loadingWarehouse ?: "",
                cargoType = initialInfo?.cargoType ?: "",
                passedViewModel = viewModel,
                onChangeSelectionClick = {
                    viewModel.resetCurrentSelection()
                }
            )
        }
    }
}
