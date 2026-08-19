package com.atk.atk_cargo.domain.model

/**
 * وضعیت ورود/خروج حواله. رشته‌های فارسی «ورود»/«خروج» قراردادی بین کلاینت و
 * سرور (ستون status در جدول CargoInfo) هستند که قبلاً به‌صورت magic string
 * در ده‌ها فایل تکرار می‌شدند (DEEP_CODE_AUDIT.md #Phase3.8). فرمت روی
 * سیم/دیتابیس اینجا بدون تغییر می‌ماند؛ این enum فقط یک منبع واحد برای آن
 * دو رشته فراهم می‌کند تا اشتباه تایپی (مثلاً فاصله یا نویسه‌ی متفاوت) در
 * محل تعریف گیر بیفتد، نه در نقطه‌ی مقایسه.
 */
enum class CargoStatus(val wireValue: String) {
    ENTERED("ورود"),
    EXITED("خروج");

    companion object {
        fun fromWire(value: String?): CargoStatus? =
            entries.firstOrNull { it.wireValue == value?.trim() }
    }
}

/** وضعیت تأیید حواله (ستون confirm) — همان الگو، سه مقدار قراردادی. */
enum class CargoConfirmStatus(val wireValue: String) {
    PENDING(""),
    AWAITING_CONFIRMATION("در انتظار تائید"),
    CONFIRMED("تائید شده");

    companion object {
        fun fromWire(value: String?): CargoConfirmStatus =
            entries.firstOrNull { it.wireValue == value?.trim() } ?: PENDING
    }
}
