package com.atk.atk_cargo.feature.reports.domain

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import com.atk.atk_cargo.data.model.Quota
import com.atk.atk_cargo.data.model.Ship
import com.atk.atk_cargo.data.model.ShipSortingMode
import java.text.Collator
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.roundToInt

val persianCollator: Collator = Collator.getInstance(Locale("fa", "IR")).apply {
    strength = Collator.PRIMARY
}

object QuotaWarningThresholds {
 /** کوتاژ بدون محدودیت درصد (percentage == 0) وقتی مانده‌اش زیر این مقدار برود هشدار می‌گیرد. */
    const val ZERO_PERCENT_REMAINING_KG = 7000f

 /** فاصله‌ی مجاز مانده تا سقف محدودیت درصد، پیش از نمایش هشدار. */
    const val PERCENTAGE_CAP_PROXIMITY_KG = 12000f
}

fun sortShips(ships: List<Ship>, sortingMode: ShipSortingMode): List<Ship> {
    return when (sortingMode) {
        ShipSortingMode.REMAINING_TONNAGE_ASC -> ships.sortedBy { it.remainingTonnage }
        ShipSortingMode.REMAINING_TONNAGE_DESC -> ships.sortedByDescending { it.remainingTonnage }
        ShipSortingMode.LOADED_TONNAGE_ASC -> ships.sortedBy { it.loadedTonnage }
        ShipSortingMode.LOADED_TONNAGE_DESC -> ships.sortedByDescending { it.loadedTonnage }
        ShipSortingMode.NAME_ASC -> ships.sortedWith(compareBy(persianCollator) { it.name })
        ShipSortingMode.NAME_DESC -> ships.sortedWith(compareByDescending(persianCollator) { it.name })
    }
}

@SuppressLint("DefaultLocale")
fun formatWeightWithDetail(weightInKg: Float): String {
    val simplifiedWeight = when {
        weightInKg >= 1_000_000 -> {
            val thousandTons = weightInKg / 1_000_000
            if (thousandTons % 1 == 0f) {
                "${thousandTons.toInt()} هزار تن"
            } else {
                val formattedThousandTons = formatNumber((weightInKg / 1_000).toInt())
                "$formattedThousandTons هزار تن"
            }
        }
        weightInKg >= 1_000 -> {
            val tons = (weightInKg / 1_000)
            if (tons % 1 == 0f) {
                "${tons.toInt()} تن"
            } else {
                String.format("%.1f تن", tons)
            }
        }
        else -> "${weightInKg.toInt()} کیلو"
    }

    return simplifiedWeight
}

fun calculateProgress(value: Float, total: Float): Float {
    return if (total > 0f) {
        val rawProgress = (value / total).coerceIn(0f, 1f)
        (rawProgress * 100f).roundToInt() / 100f
    } else 0f
}

@SuppressLint("DefaultLocale", "SimpleDateFormat")
fun buildQuotasShareText(
    groupedQuotas: LinkedHashMap<String?, List<Quota>>,
    shipName: String = "",
    includeVoucherCount: Boolean = false
): String {
    val shareText = StringBuilder()
    shareText.append("📄 *اطلاعات کشتی*")
    if (shipName.isNotEmpty()) {
        shareText.append(": ").append(shipName)
    }
    shareText.append("\n\n")

    groupedQuotas.forEach { (groupName, quotas) ->
        if (groupName != null) {
            shareText.append("🏛️ *صاحب کالا*: ").append(groupName).append("\n\n")

            quotas.forEach { quota ->
                shareText.append("📋 *شماره کوتاژ*: ").append(quota.number).append("\n")
                shareText.append("🚚 *بارگیری*: ").append(formatNumber(quota.loadedTonnage.toInt())).append(" تن\n")
                shareText.append("⚖️ *مانده*: ").append(formatNumber(quota.remainingTonnage.toInt())).append(" تن\n")
                if (includeVoucherCount) {
                    shareText.append("🎫 *تعداد حواله*: ").append(formatNumber(quota.voucherCount)).append("\n")
                }
                shareText.append("\n")
            }

            shareText.append("=".repeat(35)).append("\n\n")
        }
    }

    return shareText.toString()
}

fun shareQuotasData(context: Context, shareText: String) {
    val intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, shareText)
        type = "text/plain"
    }
    context.startActivity(Intent.createChooser(intent, "اشتراک‌گذاری اطلاعات"))
}

fun calculatePercentage(value: Float, total: Float): Int {
    return if (total > 0f) ((value / total) * 100).toInt().coerceIn(0, 100) else 0
}

fun Double.format(digits: Int) = "%.${digits}f".format(this)

fun formatNumber(number: Int): String {
    return NumberFormat.getNumberInstance(Locale.US).format(number)
}

fun persianDateFormat(dateString: String): String {
    return dateString
}

