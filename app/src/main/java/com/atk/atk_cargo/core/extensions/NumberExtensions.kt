package com.atk.atk_cargo.core.extensions

import java.text.NumberFormat
import java.util.Locale

fun formatNumber(number: Number): String {
    return NumberFormat.getNumberInstance(Locale("en", "US")).format(number)
}
