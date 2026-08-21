package com.atk.atk_cargo.feature.cargo_details.navigation

import androidx.compose.runtime.remember
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.atk.atk_cargo.data.repository.ReportsRepository
import com.atk.atk_cargo.feature.cargo_details.presentation.CargoDetailsScreen
import kotlinx.serialization.Serializable

// این فایل از feature:reports به feature:cargo-workflow منتقل شد چون به cargo_details وابسته است، نه reports
@Serializable
data class CargoDetailsRoute(
    val quotaNumber: String,
    val shippingCompany: String? = null,
    val warehouse: String? = null,
    val cargoType: String? = null
)

fun NavController.navigateToCargoDetails(
    quotaNumber: String,
    shippingCompany: String? = null,
    warehouse: String? = null,
    cargoType: String? = null
) {
    navigate(CargoDetailsRoute(quotaNumber, shippingCompany, warehouse, cargoType))
}

fun NavGraphBuilder.cargoDetailsScreen(navController: NavController, onSessionInvalid: () -> Unit) {
    composable<CargoDetailsRoute> { backStackEntry ->
        val route: CargoDetailsRoute = backStackEntry.toRoute()
        val repository = remember { ReportsRepository() }

        CargoDetailsScreen(
            navController = navController,
            onSessionInvalid = onSessionInvalid,
            quotaNumber = route.quotaNumber,
            // toRoute() مقادیر را از قبل decode می‌کند؛ decode دستی دوباره باعث کرش روی '%' و ناسازگاری می‌شد
            shippingCompany = route.shippingCompany ?: "",
            warehouse = route.warehouse ?: "",
            cargoType = route.cargoType ?: "",
            repository = repository
        )
    }
}
