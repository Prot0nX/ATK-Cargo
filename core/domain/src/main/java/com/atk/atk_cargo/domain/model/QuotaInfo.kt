package com.atk.atk_cargo.domain.model

import com.atk.atk_cargo.data.model.InitialInfo

// مدل دامنه/UI اطلاعات کوتاژ فعال، جدا از DTO شبکه‌ی InitialInfo و مخصوص CargoUiState و اعتبارسنجی
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
