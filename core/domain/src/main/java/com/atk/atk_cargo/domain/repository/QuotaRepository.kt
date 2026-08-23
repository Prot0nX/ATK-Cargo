package com.atk.atk_cargo.domain.repository

import com.atk.atk_cargo.data.model.CargoDeleteResponse
import com.atk.atk_cargo.data.model.CargoInfo
import com.atk.atk_cargo.data.model.CargoInfoRequest
import com.atk.atk_cargo.data.model.CargoInfoResponse
import com.atk.atk_cargo.data.model.LoadableTonnageResponse
import com.atk.atk_cargo.data.model.Quota
import com.atk.atk_cargo.data.model.QuotaExistenceMultipleResponse
import com.atk.atk_cargo.data.model.QuotaStatusResponse
import com.atk.atk_cargo.data.model.SaveOrUpdateResponse
import com.atk.atk_cargo.data.model.ScaleReceiptCheckResponse
import com.atk.atk_cargo.data.model.SuccessResponse
import com.google.gson.JsonElement
import retrofit2.Response

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

    // کش TTL (۳۰ ثانیه) اینجا نگه‌داری می‌شود، نه در ViewModel — تا بین نمونه‌های مختلف ViewModel هم مشترک بماند .
    suspend fun getLoadableTonnage(
        quotaNumber: String,
        shippingCompany: String,
        warehouse: String,
        cargoType: String,
        forceRefresh: Boolean = false
    ): LoadableTonnageResponse?

    // فراخوان‌کننده باید بعد از هر تغییری که تناژ باقی‌مانده‌ی سرور را عوض می‌کند (ثبت/حذف/تغییر وضعیت حواله) این را صدا بزند تا فراخوانی بعدی getLoadableTonnage مقدار کهنه‌ی کش‌شده را برنگرداند.
    suspend fun invalidateLoadableTonnageCache()

 // ===== شش متد زیر عمداً Response<T> خام Retrofit را برمی‌گردانند =====
    // تفسیر هر پاسخ (کد HTTP، بدنه‌ی خطا، فیلدهای status/error خاص هر endpoint) در CargoViewModel منطق UI-محور و به‌شدت خاص هر عملیات است.

    suspend fun checkQuotaExistenceCargo(quotaNumber: String, shipName: String): Response<QuotaExistenceMultipleResponse>

    suspend fun saveOrUpdateCargoInfo(cargoInfo: CargoInfo): Response<SaveOrUpdateResponse>

    suspend fun checkScaleReceiptNumber(scaleReceiptNumber: String): Response<ScaleReceiptCheckResponse>

    suspend fun confirmCargo(request: Map<String, String>): Response<Map<String, JsonElement>>

    // نام متفاوت از ReportsRepository.toggleQuotaStatus(id): Boolean عمدی است — همان endpoint را صدا می‌زند اما CargoViewModel به پیام خطای دقیق سرور نیاز دارد، پس Response خام را می‌خواهد نه Boolean.
    suspend fun toggleCargoQuotaStatus(id: Int): Response<SuccessResponse>

    suspend fun deleteCargo(cargoInfoRequest: CargoInfoRequest): Response<CargoDeleteResponse>
}