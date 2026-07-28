package com.atk.atk_cargo.api

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

// ===== RE-EXPORT VIEWMODELS & REPOSITORIES =====
typealias CargoViewModel = com.atk.atk_cargo.ui.viewmodel.CargoViewModel
typealias CargoViewModelFactory = com.atk.atk_cargo.ui.viewmodel.CargoViewModelFactory
typealias ReportsViewModel = com.atk.atk_cargo.ui.viewmodel.ReportsViewModel
typealias ReportsRepository = com.atk.atk_cargo.data.repository.ReportsRepository

// ===== RE-EXPORT AUTH MODELS =====
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
typealias UserTypeInfo = com.atk.atk_cargo.data.model.UserTypeInfo

// ===== RE-EXPORT CARGO MODELS =====
typealias CargoInfo = com.atk.atk_cargo.data.model.CargoInfo
typealias CargoInfoRequest = com.atk.atk_cargo.data.model.CargoInfoRequest
typealias CargoInfoResponse = com.atk.atk_cargo.data.model.CargoInfoResponse
typealias CargoStats = com.atk.atk_cargo.data.model.CargoStats
typealias CargoInfoSearch = com.atk.atk_cargo.data.model.CargoInfoSearch
typealias CargoSearchResponse = com.atk.atk_cargo.data.model.CargoSearchResponse
typealias ExistingCargo = com.atk.atk_cargo.data.model.ExistingCargo
typealias ShipInfo = com.atk.atk_cargo.data.model.ShipInfo
typealias MenuItem = com.atk.atk_cargo.data.model.MenuItem
typealias CheckExistenceRequest = com.atk.atk_cargo.data.model.CheckExistenceRequest
typealias CheckExistenceResponse = com.atk.atk_cargo.data.model.CheckExistenceResponse
typealias InitialInfo = com.atk.atk_cargo.data.model.InitialInfo
typealias SaveOrUpdateResponse = com.atk.atk_cargo.data.model.SaveOrUpdateResponse
typealias QuotaExistenceMultipleResponse = com.atk.atk_cargo.data.model.QuotaExistenceMultipleResponse
typealias MatchingQuota = com.atk.atk_cargo.data.model.MatchingQuota
typealias SuccessResponse = com.atk.atk_cargo.data.model.SuccessResponse
typealias QuotaValidationResult = com.atk.atk_cargo.data.model.QuotaValidationResult
typealias MessageType = com.atk.atk_cargo.data.model.MessageType
typealias ScaleReceiptCheckResponse = com.atk.atk_cargo.data.model.ScaleReceiptCheckResponse
typealias QuotaTonnageWarning = com.atk.atk_cargo.data.model.QuotaTonnageWarning
typealias LoadableTonnageResponse = com.atk.atk_cargo.data.model.LoadableTonnageResponse

// ===== RE-EXPORT REPORT MODELS =====
typealias ActiveShipInfo = com.atk.atk_cargo.data.model.ActiveShipInfo
typealias ShiftInfo = com.atk.atk_cargo.data.model.ShiftInfo
typealias WarningStatus = com.atk.atk_cargo.data.model.WarningStatus
typealias QuotaPercentageData = com.atk.atk_cargo.data.model.QuotaPercentageData
typealias CalculationResult = com.atk.atk_cargo.data.model.CalculationResult
typealias RealTimeLoadingData = com.atk.atk_cargo.data.model.RealTimeLoadingData
typealias ShipsData = com.atk.atk_cargo.data.model.ShipsData
typealias Ship = com.atk.atk_cargo.data.model.Ship
typealias Quota = com.atk.atk_cargo.data.model.Quota
typealias ExitDateInfo = com.atk.atk_cargo.data.model.ExitDateInfo
typealias QuotaEditData = com.atk.atk_cargo.data.model.QuotaEditData
typealias Warehouse = com.atk.atk_cargo.data.model.Warehouse
typealias QuotaDetails = com.atk.atk_cargo.data.model.QuotaDetails
typealias FilteredSummaryResponse = com.atk.atk_cargo.data.model.FilteredSummaryResponse
typealias VoucherDetail = com.atk.atk_cargo.data.model.VoucherDetail
typealias FilteredSummary = com.atk.atk_cargo.data.model.FilteredSummary
typealias QuotaStatusResponse = com.atk.atk_cargo.data.model.QuotaStatusResponse
typealias QuotaStatusDetails = com.atk.atk_cargo.data.model.QuotaStatusDetails
typealias ExistingQuota = com.atk.atk_cargo.data.model.ExistingQuota
typealias DateInfo = com.atk.atk_cargo.data.model.DateInfo
typealias ComprehensiveAnalysisResponse = com.atk.atk_cargo.data.model.ComprehensiveAnalysisResponse
typealias AnalyticsData = com.atk.atk_cargo.data.model.AnalyticsData
typealias ComprehensiveAnalytics = com.atk.atk_cargo.data.model.ComprehensiveAnalytics
typealias QuotaCompletionAnalysis = com.atk.atk_cargo.data.model.QuotaCompletionAnalysis
typealias QuotaCompletionData = com.atk.atk_cargo.data.model.QuotaCompletionData
typealias QuotaGroupingMode = com.atk.atk_cargo.data.model.QuotaGroupingMode
typealias WarehouseQuotaGroupingMode = com.atk.atk_cargo.data.model.WarehouseQuotaGroupingMode
typealias QuotaSortingMode = com.atk.atk_cargo.data.model.QuotaSortingMode
typealias GroupSortingMode = com.atk.atk_cargo.data.model.GroupSortingMode
typealias ShipSortingMode = com.atk.atk_cargo.data.model.ShipSortingMode
typealias QuotaItem = com.atk.atk_cargo.data.model.QuotaItem
typealias ThirdPartyOrderRequest = com.atk.atk_cargo.data.model.ThirdPartyOrderRequest
typealias ThirdPartyOrderResponse = com.atk.atk_cargo.data.model.ThirdPartyOrderResponse
typealias ThirdPartyOrder = com.atk.atk_cargo.data.model.ThirdPartyOrder
typealias ColorSelector = com.atk.atk_cargo.data.model.ColorSelector

// ===== UTILS EXPOSED FROM REPORT MODELS =====
val cardColors = com.atk.atk_cargo.data.model.cardColors
fun adjustColorForTheme(color: Color, isDarkTheme: Boolean) = com.atk.atk_cargo.data.model.adjustColorForTheme(color, isDarkTheme)
fun Float.toTon(): Int = (this / 1000).toInt()

fun formatNumber(number: Number): String {
    return java.text.NumberFormat.getNumberInstance(java.util.Locale.US).format(number)
}

typealias UiState = com.atk.atk_cargo.ui.viewmodel.ReportsViewModel.UiState
typealias LoadingState = com.atk.atk_cargo.ui.viewmodel.ReportsViewModel.LoadingState

// ===== COMMON TYPES IN ORIGINAL DATA MODEL =====
data class FabItem(
    val icon: ImageVector,
    val label: String,
    val onClick: () -> Unit
)

data class UpdateInfo(
    val latestVersion: String,
    val downloadUrl: String,
    val minRequiredVersion: String = "1.0",
    val updatePriority: String = "normal",
    val updateMessage: String = "",
    val forceUpdate: Boolean = false,
    val updateSize: String = "0",
    val releaseDate: String = "",
    val minAndroidVersion: Int = 21,
    val minAppVersion: String = "1.0",
    val excludedVersions: List<String> = emptyList()
)