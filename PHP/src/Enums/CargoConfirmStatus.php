<?php
// PHP/src/Enums/CargoConfirmStatus.php

declare(strict_types=1);

namespace App\Enums;

// وضعیت ستون CargoInfo.confirm؛ رشته‌ی خالی یعنی «هنوز نیازی به تأیید نیست»
enum CargoConfirmStatus: string {
    case PENDING = '';
    case AWAITING_CONFIRMATION = 'در انتظار تائید';
    case CONFIRMED = 'تائید شده';
}
