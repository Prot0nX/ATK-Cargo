package com.atk.atk_cargo.domain.model

/**
 * وزن تایپ‌شده (کیلوگرم) — جایگزین محاسبات مستقیم روی String خام
 * (DEEP_CODE_AUDIT.md #Primitive Obsession). DTOها همچنان String می‌مانند
 * (سازگاری با سرور)؛ این نوع فقط در نقاط محاسبه (CargoViewModel) استفاده
 * می‌شود تا «رشته‌ی نامعتبر» به‌جای تبدیل بی‌صدا به ۰، صریحاً null شود و
 * قابل لاگ باشد.
 */
@JvmInline
value class Kilograms(val value: Double) {
    operator fun plus(other: Kilograms): Kilograms = Kilograms(value + other.value)
    operator fun minus(other: Kilograms): Kilograms = Kilograms(value - other.value)

    fun coerceAtLeastZero(): Kilograms = Kilograms(value.coerceAtLeast(0.0))

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
