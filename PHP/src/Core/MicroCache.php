<?php
// PHP/src/Core/MicroCache.php

declare(strict_types=1);

namespace App\Core;

// کش کوتاه‌مدت سمت سرور (چند ثانیه) برای endpointهای پرتکرار. اگر APCu روی
// سرور فعال نباشد، به‌سادگی هر بار مقدار را مستقیم محاسبه می‌کند (بدون خطا).
final class MicroCache {
    // کلید کش لیست کشتی‌ها (AppApiController::getShipsList) — چون هم از
    // AppApiController و هم از CargoController (نوشتن حواله‌ها) استفاده
    // می‌شود، اینجا یک‌بار تعریف شده تا رشته‌ی جادویی در دو کلاس تکرار نشود.
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

    /**
     * برای invalidate کردن دستی یک کلید پس از نوشتنی که داده‌ی کش‌شده را
     * منسوخ می‌کند (مثل تغییر وضعیت/حذف کوتاژ که روی خروجی getShipsList اثر
     * می‌گذارد)، به‌جای منتظر ماندن تا انقضای TTL.
     */
    public static function forget(string $key): void {
        if (function_exists('apcu_delete')) {
            apcu_delete($key);
        }
    }
}
