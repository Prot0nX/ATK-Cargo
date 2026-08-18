package com.atk.atk_cargo.domain.model

/**
 * مدل UI بخش «اطلاعات کشتی» صفحه‌ی ثبت حواله — از QuotaInfo محلی ساخته
 * می‌شود (RegisterCargoScreen)، هرگز مستقیماً از سرور دریافت یا به سرور
 * ارسال نمی‌شود، پس اساساً یک مدل دامنه/UI است نه DTO شبکه. با
 * DEEP_CODE_AUDIT.md #Phase3.6 از data/model (لایه‌ی DTO) به اینجا منتقل شد.
 */
data class ShipInfo(
    val shipName: String,
    val loadingWarehouse: String,
    val cargoType: String,
    val shippingCompany: String,
    val loadingQuotaNumber: String,
    val cargoWeight: String,
    val remainingWeight: String,
    val totalNetWeight: String,
    val averageNetWeight: String,
    val totalServices: String,
    val remainingServices: String,
    val tempTonnageStatus: Boolean = false,
    val tempTonnageAmount: Float? = null,
    val cargoOwner: String = ""
)
