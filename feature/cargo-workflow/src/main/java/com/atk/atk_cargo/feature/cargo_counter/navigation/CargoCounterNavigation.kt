package com.atk.atk_cargo.feature.cargo_counter.navigation

import androidx.navigation.NavController
import kotlinx.serialization.Serializable

@Serializable
object CargoCounterRoute

fun NavController.navigateToCargoCounter() {
    navigate(CargoCounterRoute)
}

