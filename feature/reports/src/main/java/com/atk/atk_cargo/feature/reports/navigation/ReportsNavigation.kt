package com.atk.atk_cargo.feature.reports.navigation

import androidx.navigation.NavController
import kotlinx.serialization.Serializable

@Serializable
object ManageShipsRoute

fun NavController.navigateToManageShips() {
    navigate(ManageShipsRoute)
}
