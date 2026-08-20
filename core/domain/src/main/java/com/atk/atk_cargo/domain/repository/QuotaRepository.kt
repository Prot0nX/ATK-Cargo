package com.atk.atk_cargo.domain.repository

import com.atk.atk_cargo.data.model.CargoInfoResponse
import com.atk.atk_cargo.data.model.Quota
import com.atk.atk_cargo.data.model.QuotaStatusResponse

/**
 * انتزاع نازک روی ReportsRepository — همان الگوی UserPreferencesStore
 * (DEEP_CODE_AUDIT.md #Phase5.10/5.12). feature:cargo (QuotaValidationUseCase،
 * و از Phase4 #29 CargoViewModel) فقط به این متدها نیاز دارد، نه کل
 * ReportsRepository (که هنوز در feature:reports است و این اینترفیس را
 * پیاده‌سازی می‌کند) — بدون این مرز، feature:cargo مجبور می‌شد مستقیماً به
 * یک ماژول feature دیگر وابسته شود.
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

    suspend fun getCargoInfo(
        quotaNumber: String,
        shippingCompany: String,
        warehouse: String,
        cargoType: String
    ): CargoInfoResponse
}
