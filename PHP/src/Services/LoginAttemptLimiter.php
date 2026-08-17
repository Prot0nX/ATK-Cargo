<?php
// PHP/src/Services/LoginAttemptLimiter.php

declare(strict_types=1);

namespace App\Services;

/**
 * محدودکننده‌ی تلاش‌های ناموفق ورود (username + IP). قبلاً check_Auth.php
 * هیچ قفلی نداشت و رِیت‌لیمیت عمومی پروکسی (۶۰ درخواست/دقیقه به‌ازای IP) تنها
 * سدّ موجود بود — یعنی با چرخش IP عملاً بدون محدودیت (S-06). الگو دقیقاً
 * مطابق PasswordGateService (APCu با fallback فایلی) است تا با معماری موجود
 * یکدست بماند.
 */
final class LoginAttemptLimiter {
    private const MAX_ATTEMPTS = 5;
    private const LOCKOUT_WINDOW_SECONDS = 900; // ۱۵ دقیقه
    private const CACHE_PREFIX = 'login_gate_attempts_';

    public function isLocked(string $username, string $ipAddress): bool {
        return $this->getAttemptCount($username, $ipAddress) >= self::MAX_ATTEMPTS;
    }

    public function registerFailedAttempt(string $username, string $ipAddress): void {
        $key = $this->attemptsKey($username, $ipAddress);
        $count = $this->getAttemptCount($username, $ipAddress) + 1;

        if (function_exists('apcu_store')) {
            apcu_store($key, $count, self::LOCKOUT_WINDOW_SECONDS);
            return;
        }

        $file = $this->fallbackFile($key);
        file_put_contents($file, json_encode(['count' => $count, 'expires' => time() + self::LOCKOUT_WINDOW_SECONDS]));
    }

    public function resetAttempts(string $username, string $ipAddress): void {
        $key = $this->attemptsKey($username, $ipAddress);
        if (function_exists('apcu_delete')) {
            apcu_delete($key);
            return;
        }
        $file = $this->fallbackFile($key);
        if (file_exists($file)) {
            unlink($file);
        }
    }

    private function attemptsKey(string $username, string $ipAddress): string {
        return self::CACHE_PREFIX . strtolower($username) . '_' . $ipAddress;
    }

    private function getAttemptCount(string $username, string $ipAddress): int {
        $key = $this->attemptsKey($username, $ipAddress);

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
        return $dir . "/loginlimit_$safeKey.json";
    }
}
