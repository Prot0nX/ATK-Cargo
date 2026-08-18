package com.atk.atk_cargo.domain.model

import com.atk.atk_cargo.data.model.CargoInfo

/**
 * مدل دامنه/UI حواله — از CargoInfo (DTO شبکه، data/model/CargoModels.kt) جدا
 * شده (DEEP_CODE_AUDIT.md #Phase3.6) تا تغییر فرمت پاسخ سرور مستقیماً به کد
 * UI/ViewModel سرایت نکند. مپینگ در مرز شبکه (CargoViewModel.loadCargoInfoList)
 * انجام می‌شود؛ CargoInfo فقط برای درخواست/پاسخ Retrofit استفاده می‌شود.
 */
data class Cargo(
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
    val confirm: String,
    val confirmation: String? = null,
    val duplicateConfirmation: String? = null
)

fun CargoInfo.toDomain(): Cargo = Cargo(
    id = id,
    trackingNumber = trackingNumber,
    numberOfPeople = numberOfPeople,
    username = username,
    userType = userType,
    entryTime = entryTime,
    netWeight = netWeight,
    scaleReceiptNumber = scaleReceiptNumber,
    shortageWeight = shortageWeight,
    excessWeight = excessWeight,
    exitTime = exitTime,
    exitDate = exitDate,
    status = status,
    shipName = shipName,
    loadingWarehouse = loadingWarehouse,
    cargoType = cargoType,
    shippingCompany = shippingCompany,
    loadingQuotaNumber = loadingQuotaNumber,
    confirm = confirm,
    confirmation = confirmation,
    duplicateConfirmation = duplicateConfirmation
)

fun Cargo.toDto(): CargoInfo = CargoInfo(
    id = id,
    trackingNumber = trackingNumber,
    numberOfPeople = numberOfPeople,
    username = username,
    userType = userType,
    entryTime = entryTime,
    netWeight = netWeight,
    scaleReceiptNumber = scaleReceiptNumber,
    shortageWeight = shortageWeight,
    excessWeight = excessWeight,
    exitTime = exitTime,
    exitDate = exitDate,
    status = status,
    shipName = shipName,
    loadingWarehouse = loadingWarehouse,
    cargoType = cargoType,
    shippingCompany = shippingCompany,
    loadingQuotaNumber = loadingQuotaNumber,
    confirm = confirm,
    confirmation = confirmation,
    duplicateConfirmation = duplicateConfirmation
)
