package com.atk.atk_cargo.feature.monitoring.navigation

import androidx.navigation.NavController
import kotlinx.serialization.Serializable

@Serializable
object MonitoringRoute

fun NavController.navigateToMonitoring() {
    navigate(MonitoringRoute)
}
