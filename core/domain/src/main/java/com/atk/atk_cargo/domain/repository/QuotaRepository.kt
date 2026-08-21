package com.atk.atk_cargo.domain.repository

import com.atk.atk_cargo.data.model.CargoInfoResponse
import com.atk.atk_cargo.data.model.Quota
import com.atk.atk_cargo.data.model.QuotaStatusResponse

// انتزاع نازک روی ReportsRepository تا feature:cargo مجبور به وابستگی مستقیم به یک ماژول feature دیگر نشود
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
