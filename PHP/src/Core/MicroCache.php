<?php
// PHP/src/Core/MicroCache.php

declare(strict_types=1);

namespace App\Core;

// کش کوتاه‌مدت سمت سرور (چند ثانیه) برای endpointهای پرتکرار. اگر APCu روی
// سرور فعال نباشد، به‌سادگی هر بار مقدار را مستقیم محاسبه می‌کند (بدون خطا).
final class MicroCache {
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
}
