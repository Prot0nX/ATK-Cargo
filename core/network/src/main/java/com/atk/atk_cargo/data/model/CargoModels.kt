package com.atk.atk_cargo.data.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class CargoInfo(
    val id: Int? = null,
    val trackingNumber: String,
    val numberOfPeople: String,
    val username: String,
    val userType: String,
    val entryTime: String,
    val netWeight: String,
    val scaleReceiptNumber: String,
    val shortageWeight: String,
    val excessWeight: String,
    val exitTime: String? = null,
    val exitDate: String? = null,
    val status: String,
    val shipName: String,
    val loadingWarehouse: String,
    val cargoType: String,
    val shippingCompany: String,
    val loadingQuotaNumber: String,
    var confirm: String,
    val confirmation: String? = null,
    val duplicateConfirmation: String? = null
) : Parcelable

data class CargoInfoRequest(
    val id: Int,
    val password: String
)

data class CargoDeleteResponse(
    val status: String? = null,
    val message: String? = null
)

data class CargoInfoResponse(
    val cargoInfoList: List<CargoInfo>,
    val initialInfo: InitialInfo
)

data class CargoStats(
    val totalWeight: Float,
    val totalCount: Int,
    val averageWeight: Float
)

data class CargoInfoSearch(
    val exists: Boolean,
    val message: String,
    val cargoInfo: CargoInfo? = null
)

data class CargoSearchResponse(
    val success: Boolean,
    val error: String? = null,
    val cargoInfoList: List<CargoInfoSearch>? = null
)

data class ExistingCargo(
    val id: Int,
    val trackingNumber: String,
    val status: String,
    val netWeight: String?,
    val scaleReceiptNumber: String?
)

data class CheckExistenceRequest(
    val loadingQuotaNumber: Int,
    val shipName: String,
    val loadingWarehouse: String,
    val cargoType: String,
    val shippingCompany: String
)

data class CheckExistenceResponse(
    val status: String,
    val message: String
)

@Parcelize
data class InitialInfo(
    val shipName: String,
    val loadingWarehouse: String,
    val cargoType: String,
    val shippingCompany: String,
    val cargoWeight: Float,
    val loadingQuotaNumber: Int,
    val remainingWeight: Float,
    val totalNetWeight: Float,
    val averageNetWeight: Float,
    val remainingServices: Int,
    val cargoOwner: String = "",
    val isActive: Int = 1,
    val totalVoucherCount: Int = 0,
    val tempTonnageStatus: Boolean = false,
    val tempTonnageAmount: Float? = null
) : Parcelable

data class SaveOrUpdateResponse(
    val success: Boolean,
    val error: Boolean,
    val message: String,
    val status: String? = null,
    val warning: Boolean? = null,
 // سرور این کلید را snake_case می‌فرستد؛ بدون @SerializedName این فیلد همیشه null می‌ماند و دیالوگ تأیید ثبت تکراری نمایش داده نمی‌شود
    @SerializedName("requires_confirmation")
    val requiresConfirmation: Boolean? = null,
    val requiresManagerPassword: Boolean? = null,
    val exitDate: String? = null,
    val exitTime: String? = null,
    val shipName: String? = null,
    val loadingQuotaNumber: String? = null
)

data class QuotaExistenceMultipleResponse(
    val exists: Boolean,
    val matchingQuotas: List<MatchingQuota>,
    val message: String
)

data class MatchingQuota(
    val quotaNumber: String,
    val shipName: String,
    val shippingCompany: String,
    val cargoType: String,
    val warehouse: String,
    val cargoOwner: String? = null,
    var isActive: Boolean = true
)

data class SuccessResponse(
    val success: Boolean,
    val message: String? = null
)

data class QuotaValidationResult(
    val isValid: Boolean,
    val isActive: Boolean,
    val percentageReached: Boolean,
    val message: String,
    val messageType: MessageType,
    val quotaIdToToggle: Int? = null,
    val warningMessage: String? = null
)

enum class MessageType {
    SUCCESS,
    ERROR,
    WARNING
}

data class ScaleReceiptCheckResponse(
    val exists: Boolean,
    val message: String,
    val trackingNumber: String? = null,
    val netWeight: String? = null,
    val loadingQuotaNumber: String? = null
)

data class QuotaTonnageWarning(
    val shipName: String,
    val cargoOwner: String,
    val quotaNumber: String,
    val currentRemaining: String,
    val voucherCount: String,
    val remainingAfterExit: String,
    val isNegative: Boolean,
    val isActive: Boolean = false
)

data class LoadableTonnageResponse(
    val success: Boolean,
    val loadableTonnage: Float?,
    val remainingTonnage: Float?,
    val totalTonnage: Float?,
    val loadedTonnage: Float?,
    val percentage: Float?,
    val isPercentageRestricted: Boolean?,
    val trucks18Wheeler: Int?,
    val trucks10Wheeler: Int?,
    val message: String?
)

data class MenuItem(
    val title: String,
    val iconResourceId: Int,
    val route: String,
    val category: String = "",
    val description: String = ""
)