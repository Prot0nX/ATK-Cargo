<?php
// PHP/src/Services/LoginAttemptLimiter.php

declare(strict_types=1);

namespace App\Services;

// محدودکننده‌ی تلاش‌های ناموفق ورود، با دو شمارنده‌ی مستقل برای username و IP sleep تصاعدی قبلی حذف شد چون worker را مسدود و بستر DoS می‌کرد.
final class LoginAttemptLimiter {
    private const MAX_USER_ATTEMPTS = 5;
    private const MAX_IP_ATTEMPTS = 50;
    private const LOCKOUT_WINDOW_SECONDS = 900; // ۱۵ دقیقه
    private const CACHE_PREFIX = 'login_gate_attempts_';

    /** @var callable(string, string, string): void */
    private $alerter;

    // alerter قابل تزریق است تا تست واحد بدون درخواست شبکه‌ی واقعی اجرا شود
    public function __construct(?callable $alerter = null) {
        $this->alerter = $alerter ?? static function (string $event, string $message, string $dedupeKey): void {
            SecurityAlerter::getInstance()->alert($event, $message, $dedupeKey);
        };
    }

    public function isLocked(string $username, string $ipAddress): bool {
        return $this->getCount($this->userKey($username)) >= self::MAX_USER_ATTEMPTS
            || $this->getCount($this->ipKey($ipAddress)) >= self::MAX_IP_ATTEMPTS;
    }

    public function registerFailedAttempt(string $username, string $ipAddress): void {
        $userAttempts = $this->increment($this->userKey($username));
        $ipAttempts = $this->increment($this->ipKey($ipAddress));

        // اعلان فقط دقیقاً در لحظه‌ی عبور از سقف ارسال می‌شود تا اسپم نشود
        if ($userAttempts === self::MAX_USER_ATTEMPTS) {
            ($this->alerter)(
                'ACCOUNT_LOCKED',
                "حساب کاربری «{$username}» پس از $userAttempts تلاش ناموفق ورود قفل شد (IP: $ipAddress).",
                'account_locked_' . strtolower($username)
            );
        }
        if ($ipAttempts === self::MAX_IP_ATTEMPTS) {
            ($this->alerter)(
                'IP_LOCKED',
                "آدرس IP «{$ipAddress}» پس از $ipAttempts تلاش ناموفق ورود (احتمال credential-stuffing) مسدود شد.",
                'ip_locked_' . $ipAddress
            );
        }
    }

    public function resetAttempts(string $username, string $ipAddress): void {
        // فقط شمارنده‌ی username پاک می‌شود؛ شمارنده‌ی IP برای محدودسازی همچنان باقی می‌ماند
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