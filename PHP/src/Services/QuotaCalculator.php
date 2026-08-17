<?php
// PHP/src/Services/QuotaCalculator.php

declare(strict_types=1);

namespace App\Services;

/**
 * منطق محاسباتی خالص (بدون دیتابیس/IO) مربوط به تناژ و وضعیت کوتاژ — قبلاً
 * این دو متد به‌صورت private داخل AppApiController بودند و به‌خاطر وابستگی
 * ضمنی به کل کلاس (که خودش به دیتابیس متصل می‌شود) هیچ تست واحدی نمی‌شد
 * برایشان نوشت؛ اینجا به‌عنوان یک کلاس مستقل و بدون‌حالت (stateless) استخراج
 * شده‌اند تا مستقیماً و بدون نیاز به دیتابیس تست شوند (I-02). AppApiController
 * اکنون فقط به این کلاس delegate می‌کند، رفتار بیرونی‌اش عوض نشده است.
 */
final class QuotaCalculator {
    /**
     * تناژ قابل‌بارگیری واقعی یک کوتاژ. اگر محدودیت درصدی فعال باشد، بخشی
     * از ظرفیت باقی‌مانده (percentage% از کل تناژ) کنار گذاشته می‌شود و
     * قابل‌بارگیری نیست.
     */
    public function calculateLoadableTonnage(
        float $remainingTonnage,
        float $totalTonnage,
        ?float $percentage,
        bool $isPercentageRestricted
    ): float {
        if ($isPercentageRestricted && $percentage !== null) {
            $percentageAmount = $totalTonnage * ($percentage / 100);
            return $remainingTonnage - $percentageAmount;
        }
        return $remainingTonnage;
    }

    /**
     * پیام وضعیت قابل‌نمایش به کاربر بر اساس فعال‌بودن کوتاژ و درصد بارگیری‌شده.
     */
    public function generateStatusMessage(bool $isActive, float $percentageLoaded, string $quotaNumber, string $cargoType): string {
        if (!$isActive) {
            return "کوتاژ $quotaNumber با نوع کالای $cargoType غیرفعال است";
        }
        if ($percentageLoaded >= 100) {
            return "ظرفیت بارگیری کوتاژ $quotaNumber با نوع کالای $cargoType تکمیل شده است";
        }
        if ($percentageLoaded >= 95) {
            return "هشدار: ظرفیت بارگیری کوتاژ $quotaNumber با نوع کالای $cargoType به " . number_format($percentageLoaded, 2, '.', '') . "% رسیده است";
        }
        return "کوتاژ $quotaNumber با نوع کالای $cargoType فعال است";
    }
}
