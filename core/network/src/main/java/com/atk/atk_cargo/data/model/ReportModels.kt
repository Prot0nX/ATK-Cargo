package com.atk.atk_cargo.data.model

import androidx.compose.runtime.Immutable

@Immutable
data class ShiftInfo(
    val startDate: String?,
    val endDate: String?,
    val startTime: String?,
    val endTime: String?,
    val type: String?
)

@Immutable
data class WarningStatus(
    val show: Boolean,
    val quotaId: Int? = null,
    val quotaNumber: String,
    val percentage: Double,
    val remainingTonnage: Float,
    val percentageAmount: Double,
    val isActive: Boolean = true,
    val isPercentageRestricted: Boolean = false
)

@Immutable
data class QuotaPercentageData(
    val id: Int,
    val quotaNumber: String,
    val percentage: Double,
    val calculations: CalculationResult,
    val isEnabled: Int = if (percentage > 0.0) 1 else 0
)

@Immutable
data class CalculationResult(
    val percentageAmount: Double,
    val remainingAfterPercentage: Double,
    val totalRemainingAfterPercentage: Double
)

@Immutable
data class RealTimeLoadingData(
    val loadingQuotaNumber: String,
    val shipName: String,
    val loadingWarehouse: String,
    val shippingCompany: String,
    val cargoType: String?,
    val entryVouchers: Int,
    val exitVouchers: Int,
    val totalNetWeight: Int,
    val cargoOwner: String? = null
)

@Immutable
data class ShipsData(
    val activeShips: List<Ship>,
    val inactiveShips: List<Ship>
)

@Immutable
data class Ship(
    val name: String,
    val cargoType: String? = null,
    val warehouseCount: Int,
    val quotaCount: Int,
    val totalTonnage: Float,
    val remainingTonnage: Float,
 // این فیلد از قبل محاسبه‌شده از سرور می‌آید؛ کلاینت نباید totalTonnage - remainingTonnage را دوباره حساب کند
    val loadedTonnage: Float = 0f,
 // getShipsList این دو فیلد را ارسال نمی‌کند؛ بدون مقدار پیش‌فرض، Gson قید non-null کاتلین را دور می‌زد و warehouses را null می‌گذاشت (کرش پنهان)
    val totalVoucherCount: Int = 0,
    val isActive: Boolean,
    val warehouses: List<Warehouse> = emptyList()
)

@Immutable
data class Quota(
    val id: Int? = null,
    val number: String,
    val shipName: String?,
    val warehouse: String?,
    val cargoType: String?,
    val totalTonnage: Float,
    val remainingTonnage: Float,
    val loadedTonnage: Float,
    val voucherCount: Int,
    val exitDates: List<ExitDateInfo>?,
    val isActive: Boolean,
    val shippingCompany: String,
    val percentage: Double? = null,
    val isPercentageRestricted: Boolean? = false,
    val cargoOwner: String? = null
)

@Immutable
data class ExitDateInfo(
    val date: String,
    val time: String
)

@Immutable
data class QuotaEditData(
    val id: Int,
    val quotaNumber: String,
    val shipName: String,
    val shippingCompany: String,
    val warehouse: String,
    val cargoType: String,
    val totalTonnage: Float
)

@Immutable
data class Warehouse(
    val name: String,
    val quotaCount: Int,
    val totalTonnage: Float,
    val loadedTonnage: Float,
    val remainingTonnage: Float,
 // getShipDetails این دو فیلد را ارسال نمی‌کند؛ بدون مقدار پیش‌فرض، Gson قید non-null کاتلین را دور می‌زد و این فیلدها را null می‌گذاشت (کرش پنهان)
    val quotas: List<Quota> = emptyList(),
    val availableExitDates: List<String> = emptyList()
)

@Immutable
data class QuotaDetails(
    val number: String,
    val totalTonnage: Float,
    val loadedTonnage: Float,
    val remainingTonnage: Float,
    val voucherCount: Int,
    val startDate: String,
    val endDate: String,
    val additionalInfo: String?
)

@Immutable
data class FilteredSummaryResponse(
    val totalNetWeight: Float,
    val voucherCount: Int,
    val voucherDetails: List<VoucherDetail>?
)

@Immutable
data class VoucherDetail(
    val trackingNumber: String,
    val entryTime: String,
    val netWeight: Float,
    val exitTime: String,
    val exitDate: String,
    val scaleReceiptNumber: String,
    val username: String?,
    val confirmUsername: String?
)

@Immutable
data class FilteredSummary(
    val totalNetWeight: Float,
    val voucherCount: Int,
    val voucherDetails: List<VoucherDetail>,
    val quotaNumber: String,
    val shipName: String,
    val startDate: String,
    val startTime: String,
    val endDate: String,
    val endTime: String
)

@Immutable
data class QuotaStatusResponse(
    val isActive: Boolean,
    val status: Boolean,
    val message: String,
    val details: QuotaStatusDetails? = null
)

@Immutable
data class QuotaStatusDetails(
    val quotaNumber: String,
    val shipName: String,
    val cargoType: String,
    val shippingCompany: String,
    val totalWeight: Float,
    val loadedWeight: Float,
    val remainingCapacity: Float,
    val percentageLoaded: Float,
    val existingQuotas: List<ExistingQuota>? = null
)

@Immutable
data class ExistingQuota(
    val quotaNumber: String,
    val shipName: String,
    val cargoType: String,
    val shippingCompany: String
)

@Immutable
data class DateInfo(
    val jalaliDate: String,
    val dayName: String,
 // مرزهای دقیق «روز کاری» (دیروز ۰۷:۰۰ تا امروز ۰۷:۰۰)؛ nullable چون سرورهای قدیمی‌تر ممکن است این فیلدها را نفرستند
    val windowStartDate: String? = null,
    val windowStartTime: String? = null,
    val windowEndDate: String? = null,
    val windowEndTime: String? = null
)

@Immutable
data class ComprehensiveAnalysisResponse(
    val success: Boolean,
    val data: AnalyticsData
)

@Immutable
data class AnalyticsData(
    val dateInfo: DateInfo?,
    val quotaCompletionAnalysis: List<QuotaCompletionAnalysis>?
)

@Immutable
data class ComprehensiveAnalytics(
    val dateInfo: DateInfo? = null,
    val quotaCompletionAnalysis: List<QuotaCompletionData> = emptyList()
)

@Immutable
data class QuotaCompletionAnalysis(
    val loadingQuotaNumber: String,
    val shipName: String,
    val shippingCompany: String,
    val last_24h_weight: Float,
    val last_24h_vouchers: Int,
    val cargoOwner: String? = null,
    val warehouse: String? = null,
    val cargoType: String? = null
)

@Immutable
data class QuotaCompletionData(
    val loadingQuotaNumber: String,
    val shipName: String,
    val shippingCompany: String,
    val last_24h_weight: Float,
    val last_24h_vouchers: Int,
    val cargoOwner: String? = null,
    val warehouse: String? = null,
    val cargoType: String? = null
)

enum class QuotaGroupingMode {
    BY_SHIP,
    BY_CARRIER,
    BY_CARGO_OWNER
}

enum class WarehouseQuotaGroupingMode {
    BY_SHIPPING_COMPANY,
    BY_CARGO_OWNER,
    BY_WAREHOUSE
}

enum class QuotaSortingMode {
    REMAINING_TONNAGE_ASC,
    REMAINING_TONNAGE_DESC
}

enum class GroupSortingMode {
    ALPHABETICAL,
    REMAINING_TONNAGE_ASC,
    REMAINING_TONNAGE_DESC
}

enum class ShipSortingMode {
    REMAINING_TONNAGE_ASC,
    REMAINING_TONNAGE_DESC,
    LOADED_TONNAGE_ASC,
    LOADED_TONNAGE_DESC,
    NAME_ASC,
    NAME_DESC
}

@Immutable
data class QuotaItem(
    val id: Int? = null,
    val number: String,
    val shipName: String,
    val warehouse: String,
    val cargoType: String,
    val shippingCompany: String,
    val cargoOwner: String,
    val isActive: Boolean,
    val temporaryTonnageEnabled: Boolean,
    val temporaryTonnageValue: Float?,
    val quotaKey: String
)


fun Float.toTon(): Int = (this / 1000).toInt()

data class RealTimeDataResponse(
    val shiftInfo: ShiftInfo,
    val data: List<RealTimeLoadingData>
)

data class ActiveShipInfo(
    val shipName: String,
    val loadingWarehouse: String,
    val cargoType: String,
    val shippingCompany: String,
    val loadingQuotaNumber: String,
    var entryVouchers: Int = 0,
    var exitVouchers: Int = 0,
    var totalNetWeight: Int = 0,
    val cargoOwner: String? = null
)
