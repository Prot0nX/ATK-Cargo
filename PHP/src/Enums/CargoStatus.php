<?php
// PHP/src/Enums/CargoStatus.php

declare(strict_types=1);

namespace App\Enums;

// وضعیت ستون CargoInfo.status؛ مقادیر عیناً همان رشته‌های فارسی موجود در دیتابیس‌اند
enum CargoStatus: string {
    case ENTERED = 'ورود';
    case EXITED = 'خروج';
}
