package com.atk.atk_cargo.feature.reports.navigation

import androidx.compose.runtime.remember
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.atk.atk_cargo.api.RetrofitClient
import com.atk.atk_cargo.data.repository.ReportsRepository
import com.atk.atk_cargo.feature.cargo_details.presentation.CargoDetailsScreen
import com.atk.atk_cargo.ui.screens.ManageReportsScreen
import com.atk.atk_cargo.ui.viewmodel.ReportsViewModel
import kotlinx.serialization.Serializable

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
            // toRoute() از قبل مقادیر را decode می‌کند؛ decode دستی دوباره
            // اینجا (به‌علاوه‌ی یک لایه‌ی سوم در خود CargoDetailsScreen) روی
            // مقادیر حاوی '%' کرش می‌کرد و بین بارگذاری اول و بروزرسانی‌های
            // بعدی (که مقدار خام می‌فرستند) ناسازگاری ایجاد می‌کرد.
            shippingCompany = route.shippingCompany ?: "",
            warehouse = route.warehouse ?: "",
            cargoType = route.cargoType ?: "",
            repository = repository
        )
    }
}
