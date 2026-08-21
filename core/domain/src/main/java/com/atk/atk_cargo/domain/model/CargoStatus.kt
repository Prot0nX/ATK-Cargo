package com.atk.atk_cargo.domain.model

// enum یک‌دست برای رشته‌های قراردادی «ورود»/«خروج» تا خطای تایپی در همین‌جا گیر بیفتد نه در مقایسه‌ها
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
