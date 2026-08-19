package com.atk.atk_cargo.domain.model

import android.util.Log
import com.atk.atk_cargo.data.model.CargoInfo

/**
 * مدل دامنه/UI حواله — از CargoInfo (DTO شبکه، data/model/CargoModels.kt) جدا
 * شده (DEEP_CODE_AUDIT.md #Phase3.6) تا تغییر فرمت پاسخ سرور مستقیماً به کد
 * UI/ViewModel سرایت نکند. مپینگ در مرز شبکه (CargoViewModel.loadCargoInfoList)
 * انجام می‌شود؛ CargoInfo فقط برای درخواست/پاسخ Retrofit استفاده می‌شود.
 */
// netWeight تایپ‌شده‌ی Kilograms است (DEEP_CODE_AUDIT.md #Phase5.1 ادامه):
// ستون DB متناظر (`CargoInfo.netWeight`) عددی است (`int unsigned` — schema.sql)
// پس تبدیل امن است. shortageWeight/excessWeight عمداً String ماندند: ستون DB
// آن‌ها varchar(100) آزاد است (نه اجباراً عددی)، پس تبدیل به Kilograms ریسک
// تبدیل بی‌صدای داده‌ی قدیمی نامعتبر به null داشت.
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
        // netWeight خالی برای حواله‌ی هنوز باسکول‌نشده (status=ورود) طبیعی
        // است و لاگ نمی‌شود؛ فقط رشته‌ی غیرخالی و غیرعددی لاگ می‌شود.
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
