<?php
// PHP/src/Services/CrashReportRateLimiter.php

declare(strict_types=1);

namespace App\Services;

// محدودکننده‌ی نرخ اختصاصی endpoint گزارش کرش؛ برخلاف LoginAttemptLimiter سقف بالاتر دارد و به‌جای ۴۲۹ بی‌صدا drop می‌کند
final class CrashReportRateLimiter {
    private const MAX_REPORTS_PER_WINDOW = 60;
    private const WINDOW_SECONDS = 3600; // ۱ ساعت
    private const CACHE_PREFIX = 'crash_report_rate_';

    // آیا این IP از سقف مجاز در بازه‌ی جاری عبور کرده است
    public function isOverLimit(string $ipAddress): bool {
        return $this->getCount($this->key($ipAddress)) >= self::MAX_REPORTS_PER_WINDOW;
    }

    // ثبت یک گزارش؛ باید فقط وقتی isOverLimit() == false صدا زده شود
    public function registerReport(string $ipAddress): void {
        $this->increment($this->key($ipAddress));
    }

    private function key(string $ipAddress): string {
        return self::CACHE_PREFIX . $ipAddress;
    }

    private function increment(string $key): void {
        $count = $this->getCount($key) + 1;

        if (function_exists('apcu_store')) {
            apcu_store($key, $count, self::WINDOW_SECONDS);
            return;
        }

        $file = $this->fallbackFile($key);
        file_put_contents($file, json_encode(['count' => $count, 'expires' => time() + self::WINDOW_SECONDS]));
    }

    private function getCount(string $key): int {
        if (function_exists('apcu_fetch')) {
            $value = apcu_fetch($key, $ok);
            return $ok ? (int)$value : 0;
        }

        $file = $this->fallbackFile($key);
        if (!file_exists($file)) {
            return 0;
        }
        $data = json_decode((string)file_get_contents($file), true);
        if (!is_array($data) || ($data['expires'] ?? 0) < time()) {
            return 0;
        }
        return (int)($data['count'] ?? 0);
    }

    private function fallbackFile(string $key): string {
        $safeKey = preg_replace('/[^a-zA-Z0-9_]/', '_', $key) ?? 'unknown';
        $dir = APP_ROOT . '/log';
        if (!is_dir($dir)) {
            mkdir($dir, 0755, true);
        }
        return $dir . "/{$safeKey}.json";
    }
}
