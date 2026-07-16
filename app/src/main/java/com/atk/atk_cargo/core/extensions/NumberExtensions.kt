package com.atk.atk_cargo.core.extensions

import java.text.NumberFormat
import java.util.Locale

fun Float.toTon(): Int = (this / 1000).toInt()

fun formatNumber(number: Number): String {
    return NumberFormat.getNumberInstance(Locale("en", "US")).format(number)
}
