package com.atk.atk_cargo.core.navigation

object NavRoutes {
    const val LOGIN = "login"
    const val HOME = "home"
    const val INITIAL_INFO = "initial_info"
    const val SELECT_INFO = "select_info"
    const val CARGO_COUNTER = "cargo_counter"
    const val MANAGE_SHIPS = "manage_ships"
    const val ADMIN_CHAT = "admin_chat"
    
    // Cargo Details Route Builder
    const val CARGO_DETAILS_ROUTE = "cargoDetailsScreen/{quotaNumber}/{shippingCompany}/{warehouse}/{cargoType}"
    fun cargoDetails(quotaNumber: String, shippingCompany: String, warehouse: String, cargoType: String): String {
        return "cargoDetailsScreen/$quotaNumber/$shippingCompany/$warehouse/$cargoType"
    }

    // Reports sub-navigation (internal to reports feature)
    const val SHIPS_LIST = "shipsList"
    const val SHIP_DETAILS_ROUTE = "shipDetails/{shipName}"
    fun shipDetails(shipName: String): String = "shipDetails/$shipName"
    
    const val WAREHOUSE_DETAILS_ROUTE = "warehouseDetails/{shipName}/{warehouseName}"
    fun warehouseDetails(shipName: String, warehouseName: String): String = "warehouseDetails/$shipName/$warehouseName"
    
    const val QUOTA_DETAILS_ROUTE = "quotaDetails/{quotaNumber}"
    fun quotaDetails(quotaNumber: String): String = "quotaDetails/$quotaNumber"
}
