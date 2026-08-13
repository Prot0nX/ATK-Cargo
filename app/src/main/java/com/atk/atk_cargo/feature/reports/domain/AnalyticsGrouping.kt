package com.atk.atk_cargo.feature.reports.domain

import com.atk.atk_cargo.data.model.QuotaCompletionData
import com.atk.atk_cargo.data.model.QuotaGroupingMode

/**
 * C-2/C-3 (گزارش تحلیل جامع عملیات): منطق گروه‌بندی/مرتب‌سازی و ساخت کلید
 * ترکیبی "${shipName}|${cargoType}|${warehouse}" قبلاً در سه جای مستقل
 * QuotaAnalysisSection.kt (اشتراک‌گذاری کل، نمایش لیست، اشتراک‌گذاری یک گروه)
 * تکرار شده بود و دوباره در ReportsViewModel با ترتیب متفاوتی از فیلدهای همان
 * کلید (که چون خروجی‌اش هیچ‌جا مصرف نمی‌شد، این ناسازگاری بی‌اثر مانده بود).
 * فیلدهای گروه اینجا مستقیماً به‌صورت تایپ‌شده نگه داشته می‌شوند، نه یک رشته
 * "|"-جدا که هر مصرف‌کننده مجبور بود split کند.
 */
data class QuotaGroup(
    val mode: QuotaGroupingMode,
    val ship: String? = null,
    val cargoType: String? = null,
    val warehouse: String? = null,
    val carrier: String? = null,
    val quotas: List<QuotaCompletionData>
) {
    val totalWeight: Float get() = quotas.sumOf { it.last_24h_weight.toDouble() }.toFloat()
    val totalVouchers: Int get() = quotas.sumOf { it.last_24h_vouchers }

    /** کلید یکتای گروه؛ برای key پارامتر LazyColumn و مقایسه isExpanded استفاده می‌شود. */
    val key: String
        get() = when (mode) {
            QuotaGroupingMode.BY_CARGO_OWNER -> "${ship.orEmpty()}|${cargoType.orEmpty()}|${warehouse.orEmpty()}"
            QuotaGroupingMode.BY_SHIP -> "${ship.orEmpty()}|${cargoType.orEmpty()}"
            QuotaGroupingMode.BY_CARRIER -> carrier.orEmpty()
        }

    /** عنوان قابل‌خواندن برای متن اشتراک‌گذاری (share). */
    val shareTitle: String
        get() = when (mode) {
            QuotaGroupingMode.BY_CARGO_OWNER -> "کشتی: ${ship.orEmpty()} | کالا: ${cargoType.orEmpty()} | انبار: ${warehouse.orEmpty()}"
            QuotaGroupingMode.BY_SHIP -> "کشتی: ${ship.orEmpty()} | کالا: ${cargoType.orEmpty()}"
            QuotaGroupingMode.BY_CARRIER -> carrier.orEmpty()
        }
}

private const val UNKNOWN_LABEL = "نامشخص"

/**
 * گروه‌بندی و مرتب‌سازی کوتاژهای فعال بر اساس حالت انتخاب‌شده. برای
 * BY_CARGO_OWNER، گروه‌ها ابتدا بر اساس تعداد کوتاژ فعال همان انبار (نه گروه)
 * مرتب می‌شوند تا انبارهای پرکارتر بالاتر بیایند — دقیقاً همان قاعده‌ای که
 * قبلاً در QuotaAnalysisSection.kt پیاده بود.
 */
fun buildQuotaGroups(
    quotas: List<QuotaCompletionData>,
    mode: QuotaGroupingMode
): List<QuotaGroup> {
    val groups = when (mode) {
        QuotaGroupingMode.BY_CARGO_OWNER -> quotas
            .groupBy { Triple(it.shipName, it.cargoType ?: UNKNOWN_LABEL, it.warehouse ?: UNKNOWN_LABEL) }
            .map { (k, qs) -> QuotaGroup(mode, ship = k.first, cargoType = k.second, warehouse = k.third, quotas = qs) }

        QuotaGroupingMode.BY_SHIP -> quotas
            .groupBy { Pair(it.shipName, it.cargoType ?: UNKNOWN_LABEL) }
            .map { (k, qs) -> QuotaGroup(mode, ship = k.first, cargoType = k.second, quotas = qs) }

        QuotaGroupingMode.BY_CARRIER -> quotas
            .groupBy { it.shippingCompany }
            .map { (carrier, qs) -> QuotaGroup(mode, carrier = carrier, quotas = qs) }
    }

    return groups.sortedWith(
        if (mode == QuotaGroupingMode.BY_CARGO_OWNER) {
            val warehouseQuotaCounts = quotas.groupBy { it.warehouse ?: UNKNOWN_LABEL }.mapValues { it.value.size }
            compareByDescending<QuotaGroup> { warehouseQuotaCounts[it.warehouse ?: UNKNOWN_LABEL] ?: 0 }
                .thenBy { it.warehouse ?: UNKNOWN_LABEL }
                .thenByDescending { it.totalWeight }
        } else {
            compareByDescending<QuotaGroup> { it.quotas.size }
                .thenByDescending { it.totalWeight }
        }
    )
}
