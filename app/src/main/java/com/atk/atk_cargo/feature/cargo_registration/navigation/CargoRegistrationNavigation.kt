package com.atk.atk_cargo.feature.cargo_registration.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.atk.atk_cargo.ui.viewmodel.CargoViewModel
import com.atk.atk_cargo.feature.cargo_registration.presentation.RegisterCargoScreen
import com.atk.atk_cargo.data.model.InitialInfo
import com.google.gson.Gson
import kotlinx.serialization.Serializable
import android.util.Log

import org.koin.androidx.compose.koinViewModel

@Serializable
data class CargoRegistrationRoute(
    val initialInfoJson: String,
    val barcode: String? = null
)

fun NavController.navigateToCargoRegistration(initialInfoJson: String, barcode: String? = null) {
    navigate(CargoRegistrationRoute(initialInfoJson, barcode))
}

fun NavGraphBuilder.cargoRegistrationScreen(
    navController: NavController
) {
    composable<CargoRegistrationRoute> { backStackEntry ->
        val viewModel: CargoViewModel = koinViewModel()
        val route: CargoRegistrationRoute = backStackEntry.toRoute()
        
        val initialInfoExtra = remember(route.initialInfoJson) {
            try {
                Gson().fromJson(route.initialInfoJson, InitialInfo::class.java)
            } catch (e: Exception) {
                Log.e("CargoRegistrationNav", "Error decoding InitialInfo from JSON: ${e.message}")
                null
            }
        }

        val cargoInfoList by viewModel.cargoInfoList.collectAsState()
        val initialInfo by viewModel.initialInfo.collectAsState()
        val resultMessage by viewModel.resultMessage.collectAsState()
        val showAnimatedMessage by viewModel.showAnimatedMessage.collectAsState()
        val messageType by viewModel.messageType.collectAsState()

        RegisterCargoScreen(
            initialInfo = initialInfo,
            cargoInfoList = cargoInfoList,
            resultMessage = resultMessage,
            showAnimatedMessage = showAnimatedMessage,
            messageType = messageType,
            viewModel = viewModel
        )

        LaunchedEffect(initialInfoExtra) {
            initialInfoExtra?.let { info ->
                viewModel.setInitialInfo(info)
                viewModel.refreshCargoInfo()
            }
        }
    }
}
