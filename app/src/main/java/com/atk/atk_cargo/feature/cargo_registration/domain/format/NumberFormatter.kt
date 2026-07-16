package com.atk.atk_cargo.feature.cargo_registration.domain.format

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

fun formatNumber(value: String): String {
    return try {
        // حذف کاما و تبدیل به عدد
        val number = value.replace(",", "").toDoubleOrNull() ?: return toEnglishNumbers(value)

        // فرمت‌بندی با کاما و اعداد انگلیسی
        DecimalFormat("#,###.##", DecimalFormatSymbols(Locale.ENGLISH)).format(number)
    } catch (_: Exception) {
        toEnglishNumbers(value)
    }
}

fun toEnglishNumbers(input: String): String {
    val persianNumbers = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
    val arabicNumbers = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
    val englishNumbers = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')

    var result = input

    // تبدیل اعداد فارسی به انگلیسی
    for (i in persianNumbers.indices) {
        result = result.replace(persianNumbers[i], englishNumbers[i])
    }

    // تبدیل اعداد عربی به انگلیسی
    for (i in arabicNumbers.indices) {
        result = result.replace(arabicNumbers[i], englishNumbers[i])
    }

    return result
}
