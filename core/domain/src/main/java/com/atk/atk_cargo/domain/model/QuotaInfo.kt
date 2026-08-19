package com.atk.atk_cargo.domain.model

import com.atk.atk_cargo.data.model.InitialInfo

/**
 * مدل دامنه/UI اطلاعات کوتاژ فعال — از InitialInfo (DTO شبکه، data/model/CargoModels.kt)
 * جدا شده (DEEP_CODE_AUDIT.md #Phase3.6). InitialInfo همچنان هم برای پاسخ سرور
 * (CargoInfoResponse) و هم برای انتقال JSON بین صفحات (Gson روی nav-arg) استفاده
 * می‌شود؛ QuotaInfo فقط در CargoUiState و کد UI/اعتبارسنجی مصرف می‌شود.
 */
data class QuotaInfo(
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
)

fun InitialInfo.toDomain(): QuotaInfo = QuotaInfo(
    shipName = shipName,
    loadingWarehouse = loadingWarehouse,
    cargoType = cargoType,
    shippingCompany = shippingCompany,
    cargoWeight = cargoWeight,
    loadingQuotaNumber = loadingQuotaNumber,
    remainingWeight = remainingWeight,
    totalNetWeight = totalNetWeight,
    averageNetWeight = averageNetWeight,
    remainingServices = remainingServices,
    cargoOwner = cargoOwner,
    isActive = isActive,
    totalVoucherCount = totalVoucherCount,
    tempTonnageStatus = tempTonnageStatus,
    tempTonnageAmount = tempTonnageAmount
)

fun QuotaInfo.toDto(): InitialInfo = InitialInfo(
    shipName = shipName,
    loadingWarehouse = loadingWarehouse,
    cargoType = cargoType,
    shippingCompany = shippingCompany,
    cargoWeight = cargoWeight,
    loadingQuotaNumber = loadingQuotaNumber,
    remainingWeight = remainingWeight,
    totalNetWeight = totalNetWeight,
    averageNetWeight = averageNetWeight,
    remainingServices = remainingServices,
    cargoOwner = cargoOwner,
    isActive = isActive,
    totalVoucherCount = totalVoucherCount,
    tempTonnageStatus = tempTonnageStatus,
    tempTonnageAmount = tempTonnageAmount
)
