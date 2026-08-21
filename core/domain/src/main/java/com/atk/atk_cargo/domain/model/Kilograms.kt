package com.atk.atk_cargo.domain.model

import java.text.DecimalFormat
import kotlin.math.roundToInt

// وزن تایپ‌شده (کیلوگرم) به‌جای محاسبه‌ی مستقیم روی String خام، تا رشته‌ی نامعتبر به‌جای ۰ صریحاً null شود
@JvmInline
value class Kilograms(val value: Double) {
    operator fun plus(other: Kilograms): Kilograms = Kilograms(value + other.value)
    operator fun minus(other: Kilograms): Kilograms = Kilograms(value - other.value)

    fun coerceAtLeastZero(): Kilograms = Kilograms(value.coerceAtLeast(0.0))

    /** فرمت خام برای DTO سرور — بدون کاما (ستون DB معادل `int unsigned` است). */
    fun toWireString(): String = value.roundToInt().toString()

    /** فرمت نمایشی با جداکننده‌ی هزارگان، برای UI. */
    fun formatted(): String = DecimalFormat("#,###").format(value.roundToInt())

    companion object {
        val ZERO = Kilograms(0.0)

        /** رشته‌ی فرمت‌شده با کاما (`"1,234"`) یا خام (`"1234"`) را می‌پذیرد. ورودی نامعتبر/خالی → null (نه صفر). */
        fun parse(raw: String?): Kilograms? {
            val cleaned = raw?.trim()?.replace(",", "")
            if (cleaned.isNullOrEmpty()) return null
            return cleaned.toDoubleOrNull()?.let { Kilograms(it) }
        }
    }
}
