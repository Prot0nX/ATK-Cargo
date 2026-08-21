<?php
// PHP/src/Services/QuotaCalculator.php

declare(strict_types=1);

namespace App\Services;

// منطق محاسباتی خالص (بدون دیتابیس) مربوط به تناژ و وضعیت کوتاژ، استخراج‌شده از AppApiController برای قابلیت تست مستقل
final class QuotaCalculator {
    // تناژ قابل‌بارگیری واقعی کوتاژ؛ اگر محدودیت درصدی فعال باشد، سهم آن از کل تناژ کسر می‌شود
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

    // پیام وضعیت قابل‌نمایش به کاربر بر اساس فعال‌بودن کوتاژ و درصد بارگیری‌شده
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
