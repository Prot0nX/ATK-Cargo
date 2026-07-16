package com.atk.atk_cargo.feature.reports.domain

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.atk.atk_cargo.api.CargoInfo
import com.atk.atk_cargo.api.Quota
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.roundToInt

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
    return NumberFormat.getNumberInstance(Locale("en", "US")).format(number)
}

fun formatHoursToPersian(hours: Float): String {
    if (hours <= 0) return "0 دقیقه"

    val wholeHours = hours.toInt()
    val minutes = ((hours - wholeHours) * 60).roundToInt()

    return when {
        wholeHours > 0 && minutes > 0 -> "${formatNumber(wholeHours)} ساعت و ${formatNumber(minutes)} دقیقه"
        wholeHours > 0 -> "${formatNumber(wholeHours)} ساعت"
        minutes > 0 -> "${formatNumber(minutes)} دقیقه"
        else -> "0 دقیقه"
    }
}

val PERSIAN_MONTHS = listOf(
    "فروردین", "اردیبهشت", "خرداد",
    "تیر", "مرداد", "شهریور",
    "مهر", "آبان", "آذر",
    "دی", "بهمن", "اسفند"
)

fun getDaysInPersianMonth(year: Int, month: Int): Int {
    return when (month) {
        in 1..6 -> 31
        in 7..11 -> 30
        12 -> if (isPersianLeapYear(year)) 30 else 29
        else -> 30
    }
}

fun isPersianLeapYear(year: Int): Boolean {
    val remainder = year % 33
    return remainder == 1 || remainder == 5 || remainder == 9 || 
           remainder == 13 || remainder == 17 || remainder == 22 || 
           remainder == 26 || remainder == 30
}

@SuppressLint("DefaultLocale")
fun addOneDayToPersianDate(date: String): String {
    val parts = date.split("/")
    if (parts.size != 3) return date

    var year = parts[0].toIntOrNull() ?: return date
    var month = parts[1].toIntOrNull() ?: return date
    var day = parts[2].toIntOrNull() ?: return date

    day++
    val daysInMonth = getDaysInPersianMonth(year, month)

    if (day > daysInMonth) {
        day = 1
        month++
    }

    if (month > 12) {
        month = 1
        year++
    }

    return "%04d/%02d/%02d".format(year, month, day)
}

fun persianDateFormat(dateString: String): String {
    return dateString
}

fun shareCargoInfo(cargoInfo: CargoInfo, context: Context) {
    val shareText = buildString {
        appendLine("📦 اطلاعات حواله")
        appendLine("━━━━━━━━━━━━━━━━━━━━")
        appendLine("🔢 شماره حواله: ${cargoInfo.trackingNumber}")
        appendLine("🧾 قبض باسکول: ${cargoInfo.scaleReceiptNumber}")
        appendLine("⚖️ وزن خالص: ${formatNumber(cargoInfo.netWeight.toIntOrNull() ?: 0)} کیلوگرم")
        appendLine("🚢 کشتی: ${cargoInfo.shipName}")
        appendLine("🏢 شرکت: ${cargoInfo.shippingCompany}")
        appendLine("📅 زمان ورود: ${cargoInfo.entryTime}")
        if (cargoInfo.exitTime != null) {
            appendLine("🚪 زمان خروج: ${cargoInfo.exitTime}")
            appendLine("✅ وضعیت: خروج شده")
        } else {
            appendLine("⏳ وضعیت: در انتظار خروج")
        }
        appendLine("━━━━━━━━━━━━━━━━━━━━")
        appendLine("📱 ارسال شده از اپلیکیشن ATK Cargo")
    }

    val shareIntent = Intent().apply {
        action = Intent.ACTION_SEND
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, shareText)
        putExtra(Intent.EXTRA_SUBJECT, "اطلاعات حواله ${cargoInfo.trackingNumber}")
    }

    val chooserIntent = Intent.createChooser(shareIntent, "اطلاعات حواله ${cargoInfo.trackingNumber}")

    try {
        context.startActivity(chooserIntent)
    } catch (e: Exception) {
        Toast.makeText(context, "خطا در اشتراک‌گذاری: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}
