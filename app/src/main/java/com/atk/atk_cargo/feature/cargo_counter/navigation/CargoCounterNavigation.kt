package com.atk.atk_cargo.feature.cargo_counter.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.atk.atk_cargo.api.CargoViewModel
import com.atk.atk_cargo.feature.cargo_counter.presentation.CargoCounterOperationScreen
import com.atk.atk_cargo.feature.cargo_counter.presentation.CargoCounterScreen
import kotlinx.serialization.Serializable

@Serializable
object CargoCounterRoute

fun NavController.navigateToCargoCounter() {
    navigate(CargoCounterRoute)
}

fun NavGraphBuilder.cargoCounterScreen(navController: NavController, viewModel: CargoViewModel) {
    composable<CargoCounterRoute> {
        CargoCounterOperationScreen(navController = navController, viewModel = viewModel)
    }
}

