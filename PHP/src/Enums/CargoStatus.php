<?php
// PHP/src/Enums/CargoStatus.php

declare(strict_types=1);

namespace App\Enums;

/**
 * وضعیت ستون CargoInfo.status. معادل سمت Kotlin: domain/model/CargoStatus.kt
 * (Phase 3.8 آنجا انجام شده بود؛ این فایل همان کار را سمت PHP تکمیل می‌کند —
 * جایگزینی magic string 'ورود'/'خروج' با enum تایپ‌شده در سراسر کوئری‌های SQL).
 *
 * مقادیر enum عمداً دقیقاً همان رشته‌های فارسی موجود در دیتابیس‌اند تا هیچ
 * migration/تغییر داده‌ای لازم نباشد — این فقط جایگزینی literal با ثابت است.
 */
enum CargoStatus: string {
    case ENTERED = 'ورود';
    case EXITED = 'خروج';
}
