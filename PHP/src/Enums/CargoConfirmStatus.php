<?php
// PHP/src/Enums/CargoConfirmStatus.php

declare(strict_types=1);

namespace App\Enums;

/**
 * وضعیت ستون CargoInfo.confirm. معادل سمت Kotlin: domain/model/CargoStatus.kt
 * (CargoConfirmStatus، Phase 3.8). رشته‌ی خالی = «هنوز نیازی به تأیید نیست»
 * (وضعیت پیش‌فرض قبل از رسیدن به آستانه‌ی کوتاژ)، نه یک مقدار نامشخص.
 */
enum CargoConfirmStatus: string {
    case PENDING = '';
    case AWAITING_CONFIRMATION = 'در انتظار تائید';
    case CONFIRMED = 'تائید شده';
}
