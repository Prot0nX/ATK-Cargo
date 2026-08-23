<?php
// PHP/src/Core/MicroCache.php

declare(strict_types=1);

namespace App\Core;

// کش کوتاه‌مدت سمت سرور برای endpointهای پرتکرار؛ بدون APCu مستقیم محاسبه می‌کند
final class MicroCache {
 // کلید کش لیست کشتی‌ها، مشترک بین ShipService و CargoController
    public const SHIPS_LIST_KEY = 'app_api_ships_list';

    public static function remember(string $key, int $ttlSeconds, callable $compute) {
        if (function_exists('apcu_fetch')) {
            $value = apcu_fetch($key, $success);
            if ($success) {
                return $value;
            }
        }

        $value = $compute();

        if (function_exists('apcu_store')) {
            apcu_store($key, $value, $ttlSeconds);
        }

        return $value;
    }

 // ابطال دستی یک کلید کش پس از نوشتنی که داده را منسوخ می‌کند، بدون انتظار برای TTL
    public static function forget(string $key): void {
        if (function_exists('apcu_delete')) {
            apcu_delete($key);
        }
    }
}
