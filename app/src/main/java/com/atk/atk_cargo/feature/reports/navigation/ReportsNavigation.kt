package com.atk.atk_cargo.feature.reports.navigation

import androidx.compose.runtime.remember
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.atk.atk_cargo.ui.screens.ManageReportsScreen
import com.atk.atk_cargo.feature.cargo_details.presentation.CargoDetailsScreen
import com.atk.atk_cargo.ui.viewmodel.ReportsViewModel
import com.atk.atk_cargo.data.repository.ReportsRepository
import com.atk.atk_cargo.api.RetrofitClient
import kotlinx.serialization.Serializable
import java.net.URLDecoder

@Serializable
object ManageShipsRoute

@Serializable
data class CargoDetailsRoute(
    val quotaNumber: String,
    val shippingCompany: String? = null,
    val warehouse: String? = null,
    val cargoType: String? = null
)

fun NavController.navigateToManageShips() {
    navigate(ManageShipsRoute)
}

fun NavController.navigateToCargoDetails(
    quotaNumber: String,
    shippingCompany: String? = null,
    warehouse: String? = null,
    cargoType: String? = null
) {
    navigate(CargoDetailsRoute(quotaNumber, shippingCompany, warehouse, cargoType))
}

fun NavGraphBuilder.manageShipsScreen(
    navController: NavController,
    viewModel: ReportsViewModel
) {
    composable<ManageShipsRoute> {
        ManageReportsScreen(viewModel = viewModel, navController = navController)
    }
}

fun NavGraphBuilder.cargoDetailsScreen(navController: NavController) {
    composable<CargoDetailsRoute> { backStackEntry ->
        val route: CargoDetailsRoute = backStackEntry.toRoute()
        val repository = remember { ReportsRepository(RetrofitClient.apiService) }
        
        CargoDetailsScreen(
            navController = navController,
            quotaNumber = route.quotaNumber,
            shippingCompany = route.shippingCompany?.let { URLDecoder.decode(it, "UTF-8") } ?: "",
            warehouse = route.warehouse?.let { URLDecoder.decode(it, "UTF-8") } ?: "",
            cargoType = route.cargoType?.let { URLDecoder.decode(it, "UTF-8") } ?: "",
            repository = repository
        )
    }
}
