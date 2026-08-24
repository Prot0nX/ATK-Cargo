package com.atk.atk_cargo.api

import androidx.compose.ui.graphics.Color

// صدور مجدد ویومدل‌ها و ریپازیتوری‌ها.
typealias CargoViewModel = com.atk.atk_cargo.feature.cargo.viewmodel.CargoViewModel
typealias CargoViewModelFactory = com.atk.atk_cargo.feature.cargo.viewmodel.CargoViewModelFactory
typealias ReportsViewModel = com.atk.atk_cargo.feature.reports.viewmodel.ReportsViewModel
typealias ReportsRepository = com.atk.atk_cargo.data.repository.ReportsRepository

// صدور مجدد مدل‌های احراز هویت.
typealias LoginRequest = com.atk.atk_cargo.data.model.LoginRequest
typealias LoginResponse = com.atk.atk_cargo.data.model.LoginResponse
typealias LogoutRequest = com.atk.atk_cargo.data.model.LogoutRequest
typealias LogoutResponse = com.atk.atk_cargo.data.model.LogoutResponse
typealias ForceLogoutRequest = com.atk.atk_cargo.data.model.ForceLogoutRequest
typealias ForceLogoutResponse = com.atk.atk_cargo.data.model.ForceLogoutResponse
typealias SessionCheckRequest = com.atk.atk_cargo.data.model.SessionCheckRequest
typealias SessionResponse = com.atk.atk_cargo.data.model.SessionResponse
typealias PermissionSyncRequest = com.atk.atk_cargo.data.model.PermissionSyncRequest
typealias PermissionSyncResponse = com.atk.atk_cargo.data.model.PermissionSyncResponse
typealias PasswordCheckResponse = com.atk.atk_cargo.data.model.PasswordCheckResponse
typealias User = com.atk.atk_cargo.data.model.User
typealias ActiveSessionResponse = com.atk.atk_cargo.data.model.ActiveSessionResponse
typealias UpdateUserRequest = com.atk.atk_cargo.data.model.UpdateUserRequest
typealias DeleteUserRequest = com.atk.atk_cargo.data.model.DeleteUserRequest
typealias CreateUserRequest = com.atk.atk_cargo.data.model.CreateUserRequest

// صدور مجدد مدل‌های حواله بار.
typealias CargoInfo = com.atk.atk_cargo.data.model.CargoInfo
typealias CargoInfoRequest = com.atk.atk_cargo.data.model.CargoInfoRequest
typealias CargoDeleteResponse = com.atk.atk_cargo.data.model.CargoDeleteResponse
typealias CargoInfoResponse = com.atk.atk_cargo.data.model.CargoInfoResponse
typealias CargoInfoSearch = com.atk.atk_cargo.data.model.CargoInfoSearch
typealias CargoSearchResponse = com.atk.atk_cargo.data.model.CargoSearchResponse
typealias CheckExistenceRequest = com.atk.atk_cargo.data.model.CheckExistenceRequest
typealias CheckExistenceResponse = com.atk.atk_cargo.data.model.CheckExistenceResponse
typealias InitialInfo = com.atk.atk_cargo.data.model.InitialInfo
typealias SaveOrUpdateResponse = com.atk.atk_cargo.data.model.SaveOrUpdateResponse
typealias QuotaExistenceMultipleResponse = com.atk.atk_cargo.data.model.QuotaExistenceMultipleResponse
typealias MatchingQuota = com.atk.atk_cargo.data.model.MatchingQuota
typealias SuccessResponse = com.atk.atk_cargo.data.model.SuccessResponse
typealias MessageType = com.atk.atk_cargo.data.model.MessageType
typealias ScaleReceiptCheckResponse = com.atk.atk_cargo.data.model.ScaleReceiptCheckResponse
typealias LoadableTonnageResponse = com.atk.atk_cargo.data.model.LoadableTonnageResponse

// صدور مجدد مدل‌های گزارش‌ها.
typealias ActiveShipInfo = com.atk.atk_cargo.data.model.ActiveShipInfo
typealias ShiftInfo = com.atk.atk_cargo.data.model.ShiftInfo
typealias WarningStatus = com.atk.atk_cargo.data.model.WarningStatus
typealias QuotaPercentageData = com.atk.atk_cargo.data.model.QuotaPercentageData
typealias CalculationResult = com.atk.atk_cargo.data.model.CalculationResult
typealias RealTimeLoadingData = com.atk.atk_cargo.data.model.RealTimeLoadingData
typealias ShipsData = com.atk.atk_cargo.data.model.ShipsData
typealias Ship = com.atk.atk_cargo.data.model.Ship
typealias Quota = com.atk.atk_cargo.data.model.Quota
typealias QuotaEditData = com.atk.atk_cargo.data.model.QuotaEditData
typealias Warehouse = com.atk.atk_cargo.data.model.Warehouse
typealias QuotaDetails = com.atk.atk_cargo.data.model.QuotaDetails
typealias FilteredSummaryResponse = com.atk.atk_cargo.data.model.FilteredSummaryResponse
typealias VoucherDetail = com.atk.atk_cargo.data.model.VoucherDetail
typealias FilteredSummary = com.atk.atk_cargo.data.model.FilteredSummary
typealias QuotaStatusResponse = com.atk.atk_cargo.data.model.QuotaStatusResponse
typealias ComprehensiveAnalysisResponse = com.atk.atk_cargo.data.model.ComprehensiveAnalysisResponse
typealias QuotaCompletionData = com.atk.atk_cargo.data.model.QuotaCompletionData
typealias QuotaGroupingMode = com.atk.atk_cargo.data.model.QuotaGroupingMode
typealias WarehouseQuotaGroupingMode = com.atk.atk_cargo.data.model.WarehouseQuotaGroupingMode
typealias QuotaSortingMode = com.atk.atk_cargo.data.model.QuotaSortingMode
typealias GroupSortingMode = com.atk.atk_cargo.data.model.GroupSortingMode
typealias ShipSortingMode = com.atk.atk_cargo.data.model.ShipSortingMode
typealias QuotaItem = com.atk.atk_cargo.data.model.QuotaItem
typealias ColorSelector = com.atk.atk_cargo.core.ui.components.ColorSelector

// توابع کمکی صادرشده از مدل‌های گزارش.
val cardColors = com.atk.atk_cargo.core.ui.components.cardColors
fun adjustColorForTheme(color: Color, isDarkTheme: Boolean) = com.atk.atk_cargo.core.ui.components.adjustColorForTheme(color, isDarkTheme)
fun Float.toTon(): Int = (this / 1000).toInt()

fun formatNumber(number: Number): String {
    return java.text.NumberFormat.getNumberInstance(java.util.Locale.US).format(number)
}

typealias RealTimeUiState = com.atk.atk_cargo.feature.reports.viewmodel.ReportsViewModel.RealTimeUiState

