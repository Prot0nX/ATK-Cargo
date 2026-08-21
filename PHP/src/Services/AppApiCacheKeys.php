<?php
// PHP/src/Services/AppApiCacheKeys.php

declare(strict_types=1);

namespace App\Services;

// کلیدهای کش مشترک جزئیات کشتی و لیست کوتاژها، برای هم‌آهنگی بین ShipService و QuotaService
final class AppApiCacheKeys {
    public static function shipDetails(string $shipName): string {
        return 'app_api_ship_details_' . $shipName;
    }

    public static function quotasList(string $shipName): string {
        return 'app_api_quotas_list_' . $shipName;
    }
}
