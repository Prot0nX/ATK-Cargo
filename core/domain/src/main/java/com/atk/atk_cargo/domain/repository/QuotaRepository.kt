package com.atk.atk_cargo.domain.repository

import com.atk.atk_cargo.data.model.CargoInfoResponse
import com.atk.atk_cargo.data.model.LoadableTonnageResponse
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

    // کش TTL (۳۰ ثانیه) اینجا نگه‌داری می‌شود، نه در ViewModel — تا بین نمونه‌های مختلف ViewModel هم مشترک بماند
    // (DEEP_CODE_AUDIT.md فاز۳ #۲۲). forceRefresh=false یعنی پاسخ کش‌شده‌ی تازه (در صورت وجود) به‌جای درخواست شبکه برگردد.
    suspend fun getLoadableTonnage(
        quotaNumber: String,
        shippingCompany: String,
        warehouse: String,
        cargoType: String,
        forceRefresh: Boolean = false
    ): LoadableTonnageResponse?

    // فراخوان‌کننده باید بعد از هر تغییری که تناژ باقی‌مانده‌ی سرور را عوض می‌کند (ثبت/حذف/تغییر وضعیت حواله) این را
    // صدا بزند تا فراخوانی بعدی getLoadableTonnage مقدار کهنه‌ی کش‌شده را برنگرداند. کل کش را پاک می‌کند (نه فقط
    // یک کوتاژ خاص) چون در عمل هر لحظه فقط یک کوتاژ روی صفحه فعال است؛ ساده‌تر و بی‌ریسک‌تر از کلیدسازی دقیق در هر نقطه‌ی فراخوانی.
    suspend fun invalidateLoadableTonnageCache()
}
