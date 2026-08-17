<?php
// PHP/src/Services/AppApiCacheKeys.php

declare(strict_types=1);

namespace App\Services;

/**
 * کلیدهای کش پرکاربردترین دو endpoint گروه Ships/Quotas (جزئیات کشتی + لیست
 * کوتاژها)، هر کدام به‌ازای هر نام کشتی جداگانه. هم ShipService (که این
 * دیتا را کش می‌کند) و هم QuotaService (که با نوشتن‌هایش — editQuota/
 * deleteQuota/toggleQuotaStatus/updateQuotaPercentage* — باید همین کلیدها
 * را invalidate کند) به یک تعریف مشترک نیاز دارند تا رشته‌ی جادویی کلید در
 * دو کلاس تکرار/ناهماهنگ نشود.
 */
final class AppApiCacheKeys {
    public static function shipDetails(string $shipName): string {
        return 'app_api_ship_details_' . $shipName;
    }

    public static function quotasList(string $shipName): string {
        return 'app_api_quotas_list_' . $shipName;
    }
}
