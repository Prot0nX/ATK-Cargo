package com.atk.atk_cargo.domain.model

import android.util.Log
import com.atk.atk_cargo.data.model.CargoInfo

// مدل دامنه/UI حواله با فیلد خالص تایپ‌شده، تفکیک‌شده از DTO شبکه جهت استقلال معماری.
data class Cargo(
    val id: Int? = null,
    val trackingNumber: String,
    val numberOfPeople: String,
    val username: String,
    val userType: String,
    val entryTime: String,
    val netWeight: Kilograms?,
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
    netWeight = Kilograms.parse(netWeight).also {
        // فقط رشته‌ی غیرخالی و غیرعددی لاگ می‌شود؛ netWeight خالی برای حواله‌ی هنوز باسکول‌نشده طبیعی است
        if (it == null && netWeight.isNotBlank()) {
            Log.w("Cargo_toDomain", "وزن خالص نامعتبر برای حواله #$trackingNumber: '$netWeight'")
        }
    },
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
    netWeight = netWeight?.toWireString() ?: "",
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
