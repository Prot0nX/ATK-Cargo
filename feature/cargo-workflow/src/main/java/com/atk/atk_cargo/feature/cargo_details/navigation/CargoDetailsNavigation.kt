package com.atk.atk_cargo.feature.cargo_details.navigation

import androidx.compose.runtime.remember
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.atk.atk_cargo.data.repository.ReportsRepository
import com.atk.atk_cargo.feature.cargo_details.presentation.CargoDetailsScreen
import kotlinx.serialization.Serializable

// در ReportsNavigation.kt (feature:reports) بود، اما به feature/cargo_details
// وابسته است — یعنی به reports تعلق ندارد. در Phase4 #23 (۵/۶) به app منتقل
// شد چون cargo_details و cargo_counter (مصرف‌کننده‌ی navigateToCargoDetails)
// هنوز هرکدام جدا در app بودند؛ حالا که هر دو با هم در feature:cargo-workflow
// جمع شده‌اند (Phase4 #29)، این فایل هم به همین‌جا منتقل شد.
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
