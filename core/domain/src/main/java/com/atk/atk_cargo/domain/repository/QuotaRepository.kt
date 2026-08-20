package com.atk.atk_cargo.domain.repository

import com.atk.atk_cargo.data.model.Quota
import com.atk.atk_cargo.data.model.QuotaStatusResponse

/**
 * انتزاع نازک روی ReportsRepository — همان الگوی UserPreferencesStore
 * (DEEP_CODE_AUDIT.md #Phase5.10/5.12). feature:cargo (QuotaValidationUseCase)
 * برای استخراج به ماژول مستقل (DEEP_CODE_REVIEW.md Phase4 #29) فقط به این
 * دو متد نیاز دارد، نه کل ReportsRepository (که هنوز در app باقی می‌ماند و
 * این اینترفیس را پیاده‌سازی می‌کند) — بدون این مرز، feature:cargo مجبور
 * می‌شد مستقیماً به یک کلاس در app وابسته شود که جهت وابستگی را برعکس
 * می‌کرد.
 */
interface QuotaRepository {
    suspend fun checkQuotaStatus(
        quotaNumber: String,
        shipName: String,
        cargoType: String,
        shippingCompany: String,
        warehouse: String
    ): QuotaStatusResponse

    suspend fun getShipQuotas(shipName: String, forceRefresh: Boolean = false): List<Quota>
}
