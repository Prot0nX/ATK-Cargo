package com.atk.atk_cargo.data.model

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils

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
    val cargoWeight: Int
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
    val totalVoucherCount: Int,
    val isActive: Boolean,
    val warehouses: List<Warehouse>
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
    val quotas: List<Quota>,
    val availableExitDates: List<String>
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
    val dayName: String
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

data class ThirdPartyOrderRequest(
    val companyCode: String,
    val date1: String,
    val date2: String,
    val reportName: String
)

data class ThirdPartyOrderResponse(
    val value: List<ThirdPartyOrder>,
    val formatters: List<Any>,
    val contentTypes: List<Any>,
    val declaredType: String?,
    val statusCode: Int
)

@Immutable
data class ThirdPartyOrder(
    val orderId: String,
    val companyInternalContractCode: String?,
    val orderGoodDescreption: String?,
    val orderIssueDate: String?,
    val orderIssueTime: String?,
    val ctName: String?,
    val truckLicensePlate: String?,
    val driverFullName: String?,
    val orderGoodCount: Int?,
    val orderStatus: String?,
    val ladingStatus: String?,
    val scaleEmpty: Double?,
    val scaleFull: Double?,
    val orderWeight: Int?
)

class ColorSelector(private val colors: List<Color>) {
    private val assignedColors = mutableMapOf<String, Color>()
    private val usedColors = mutableMapOf<Color, Boolean>()
    private val random = java.util.Random(System.currentTimeMillis())

    init {
        colors.forEach { usedColors[it] = false }
    }

    fun assignDistinctColors(identifiers: Set<String>): Map<String, Color> {
        if (identifiers.size > colors.size) {
            return assignColorsWithGeneration(identifiers)
        }

        reset()
        val result = mutableMapOf<String, Color>()
        val availableColors = colors.toMutableList()

        identifiers.filter { assignedColors.containsKey(it) }.forEach { id ->
            val previousColor = assignedColors[id]
            if (previousColor != null && previousColor in availableColors) {
                result[id] = previousColor
                availableColors.remove(previousColor)
                usedColors[previousColor] = true
            }
        }

        identifiers.filter { !result.containsKey(it) }.forEach { id ->
            if (availableColors.isNotEmpty()) {
                val colorIndex = random.nextInt(availableColors.size)
                val selectedColor = availableColors[colorIndex]
                result[id] = selectedColor
                availableColors.removeAt(colorIndex)
                assignedColors[id] = selectedColor
                usedColors[selectedColor] = true
            }
        }
        return result
    }

    private fun assignColorsWithGeneration(identifiers: Set<String>): Map<String, Color> {
        val result = mutableMapOf<String, Color>()
        identifiers.forEachIndexed { index, id ->
            val color = if (index < colors.size) {
                colors[index]
            } else {
                val hue = (360f * index / identifiers.size) % 360f
                val saturation = 0.85f + (random.nextFloat() * 0.15f)
                val lightness = 0.4f + (random.nextFloat() * 0.15f)
                val hsl = floatArrayOf(hue, saturation, lightness)
                Color(ColorUtils.HSLToColor(hsl))
            }
            result[id] = color
            assignedColors[id] = color
        }
        return result
    }

    fun getNextColor(): Color {
        val unusedColors = usedColors.filter { !it.value }.keys.toList()
        if (unusedColors.isNotEmpty()) {
            val selectedColor = unusedColors[random.nextInt(unusedColors.size)]
            usedColors[selectedColor] = true
            return selectedColor
        }
        return colors[random.nextInt(colors.size)]
    }

    fun reset() {
        colors.forEach { usedColors[it] = false }
    }
}

fun adjustColorForTheme(color: Color, isDarkTheme: Boolean): Color {
    val hsl = FloatArray(3)
    ColorUtils.colorToHSL(color.toArgb(), hsl)
    hsl[2] = if (isDarkTheme) 0.65f else 0.45f
    hsl[1] = 0.85f
    return Color(ColorUtils.HSLToColor(hsl))
}

val cardColors = listOf(
    Color(0xFFEF5350), Color(0xFF66BB6A), Color(0xFF42A5F5), Color(0xFFEC407A),
    Color(0xFF26C6DA), Color(0xFFAB47BC), Color(0xFF26A69A), Color(0xFF8D6E63),
    Color(0xFF7E57C2), Color(0xFF29B6F6), Color(0xFFFF7043), Color(0xFF9CCC65),
    Color(0xFF5C6BC0), Color(0xFFFFCA28), Color(0xFF78909C), Color(0xFFD32F2F),
    Color(0xFF388E3C), Color(0xFF1976D2), Color(0xFFC2185B), Color(0xFF0097A7),
    Color(0xFF7B1FA2), Color(0xFF00796B), Color(0xFF5D4037), Color(0xFF512DA8),
    Color(0xFF0288D1), Color(0xFFF57C00), Color(0xFFE64A19), Color(0xFF689F38),
    Color(0xFF303F9F), Color(0xFFFBC020)
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
    var totalNetWeight: Int = 0
)
