package com.atk.atk_cargo.feature.cargo_entry.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.atk.atk_cargo.feature.cargo_entry.presentation.CargoOperationScreen
import com.atk.atk_cargo.feature.cargo_entry.presentation.InitialInfoScreen
import com.atk.atk_cargo.ui.viewmodel.CargoViewModel
import kotlinx.serialization.Serializable

@Serializable
object InitialInfoRoute

@Serializable
object SelectInfoRoute

fun NavController.navigateToInitialInfo() {
    navigate(InitialInfoRoute)
}

fun NavController.navigateToSelectInfo() {
    navigate(SelectInfoRoute)
}

fun NavGraphBuilder.initialInfoScreen(navController: NavController) {
    composable<InitialInfoRoute> {
        InitialInfoScreen(navController = navController)
    }
}

fun NavGraphBuilder.selectInfoScreen(navController: NavController, viewModel: CargoViewModel) {
    composable<SelectInfoRoute> {
        CargoOperationScreen(navController = navController, viewModel = viewModel)
    }
}


