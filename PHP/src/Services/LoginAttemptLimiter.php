<?php
// PHP/src/Services/LoginAttemptLimiter.php

declare(strict_types=1);

namespace App\Services;

/**
 * محدودکننده‌ی تلاش‌های ناموفق ورود. دو شمارنده‌ی مستقل دارد:
 * - به‌ازای username (مستقل از IP) → سدّ اصلی brute-force روی یک حساب.
 * - به‌ازای IP (مستقل از username) → سدّ credential-stuffing گسترده.
 * نسخه‌ی قبلی فقط کلید ترکیبی username+IP داشت که با چرخش IP کاملاً دور
 * زده می‌شد (S-06 / DEEP_CODE_AUDIT.md #1.1). الگوی ذخیره‌سازی (APCu با
 * fallback فایلی) مطابق PasswordGateService است.
 */
final class LoginAttemptLimiter {
    private const MAX_USER_ATTEMPTS = 5;
    private const MAX_IP_ATTEMPTS = 50;
    private const LOCKOUT_WINDOW_SECONDS = 900; // ۱۵ دقیقه
    private const CACHE_PREFIX = 'login_gate_attempts_';
    private const MAX_BACKOFF_SECONDS = 8;

    /** @var callable(int): void */
    private $sleeper;

    // Phase 3.3: sleep() تصاعدی داخل registerFailedAttempt تست واحد این کلاس
    // را غیرممکن می‌کرد (تا ۸ ثانیه واقعی به‌ازای هر تست). با تزریق یک تابع
    // sleep قابل جایگزینی (پیش‌فرض همان sleep() واقعی برای کد production)،
    // تست می‌تواند این تابع را با یک no-op جایگزین کند و فقط منطق شمارش/قفل
    // را بسنجد، نه گذر زمان واقعی را.
    public function __construct(?callable $sleeper = null) {
        $this->sleeper = $sleeper ?? static function (int $seconds): void {
            sleep($seconds);
        };
    }

    public function isLocked(string $username, string $ipAddress): bool {
        return $this->getCount($this->userKey($username)) >= self::MAX_USER_ATTEMPTS
            || $this->getCount($this->ipKey($ipAddress)) >= self::MAX_IP_ATTEMPTS;
    }

    public function registerFailedAttempt(string $username, string $ipAddress): void {
        $userAttempts = $this->increment($this->userKey($username));
        $this->increment($this->ipKey($ipAddress));

        // تأخیر تصاعدی: 2, 4, 8, 8, 8... ثانیه به‌ازای تلاش ناموفق روی همین username
        $delay = min(2 ** $userAttempts, self::MAX_BACKOFF_SECONDS);
        ($this->sleeper)($delay);
    }

    public function resetAttempts(string $username, string $ipAddress): void {
        // فقط شمارنده‌ی username پاک می‌شود؛ شمارنده‌ی IP باقی می‌ماند تا
        // credential-stuffing از همان IP روی حساب‌های دیگر همچنان محدود بماند.
        $this->clear($this->userKey($username));
    }

    private function userKey(string $username): string {
        return self::CACHE_PREFIX . 'u_' . strtolower($username);
    }

    private function ipKey(string $ipAddress): string {
        return self::CACHE_PREFIX . 'ip_' . $ipAddress;
    }

    private function increment(string $key): int {
        $count = $this->getCount($key) + 1;

        if (function_exists('apcu_store')) {
            apcu_store($key, $count, self::LOCKOUT_WINDOW_SECONDS);
            return $count;
        }

        $file = $this->fallbackFile($key);
        file_put_contents($file, json_encode(['count' => $count, 'expires' => time() + self::LOCKOUT_WINDOW_SECONDS]));
        return $count;
    }

    private function clear(string $key): void {
        if (function_exists('apcu_delete')) {
            apcu_delete($key);
            return;
        }
        $file = $this->fallbackFile($key);
        if (file_exists($file)) {
            unlink($file);
        }
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
        return $dir . "/loginlimit_$safeKey.json";
    }
}
