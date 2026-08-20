package com.atk.atk_cargo.feature.cargo_entry.domain

import java.util.Locale

fun isValidShipName(name: String): Boolean {
    return name.matches(Regex("^[a-zA-Z0-9 ]{3,50}$"))
}

fun isValidWarehouseName(text: String): Boolean {
    return text.length in 3..50
}

fun isValidPersianText(text: String): Boolean {
    return text.matches(Regex("^[\\u0600-\\u06FF\\s0-9]{3,50}$"))
}

fun isValidWeight(weight: String): Boolean {
    val weightValue = weight.toLongOrNull()
    return weightValue != null && weightValue in 1..9999999999
}

fun isValidQuotaNumber(number: String): Boolean {
    return number.all { it.isDigit() } && number.length >= 5
}

fun formatNumber(number: String): String {
    return try {
        val value = number.toLong()
        "%,d".format(Locale.US, value)
    } catch (_: Exception) {
        number
    }
}
