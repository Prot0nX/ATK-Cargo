package com.atk.atk_cargo.feature.cargo_entry.navigation

import androidx.navigation.NavController
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

