<?php
// PHP/src/Services/PasswordGateService.php

declare(strict_types=1);

namespace App\Services;

use App\Core\Database;
use PDO;

// دروازه‌ی مشترک بررسی رمزهای عملیاتی؛ شمارنده‌ی تلاش‌ها روی APCu با کلید هویت نشست نگه‌داری می‌شود، نه $_SESSION
final class PasswordGateService {
    private const MAX_ATTEMPTS = 5;
    private const LOCKOUT_WINDOW_SECONDS = 900; // 15 دقیقه
    private const CACHE_PREFIX = 'pwd_gate_attempts_';

    private PDO $conn;

    public function __construct() {
        $this->conn = Database::getInstance()->getPdoConnection();
    }

 /** @return array{success: bool, locked: bool, message: string} */
    public function verify(string $passwordType, string $password, string $identityKey): array {
        if ($this->isLocked($identityKey, $passwordType)) {
            return [
                'success' => false,
                'locked' => true,
                'message' => 'تعداد تلاش‌های ناموفق بیش از حد مجاز است. لطفاً ۱۵ دقیقه دیگر تلاش کنید.'
            ];
        }

        $stmt = $this->conn->prepare("SELECT password FROM Passwords WHERE passwordType = ? LIMIT 1");
        $stmt->execute([$passwordType]);
        $row = $stmt->fetch(PDO::FETCH_ASSOC);

        $isValid = $row !== false && password_verify($password, (string)$row['password']);

        if ($isValid) {
            $this->resetAttempts($identityKey, $passwordType);
            return ['success' => true, 'locked' => false, 'message' => 'رمز عبور صحیح است.'];
        }

        $this->registerFailedAttempt($identityKey, $passwordType);
        return ['success' => false, 'locked' => false, 'message' => 'رمز عبور اشتباه است.'];
    }

    private function attemptsKey(string $identityKey, string $passwordType): string {
        return self::CACHE_PREFIX . $passwordType . '_' . $identityKey;
    }

    private function isLocked(string $identityKey, string $passwordType): bool {
        return $this->getAttemptCount($identityKey, $passwordType) >= self::MAX_ATTEMPTS;
    }

    private function getAttemptCount(string $identityKey, string $passwordType): int {
        $key = $this->attemptsKey($identityKey, $passwordType);

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

    private function registerFailedAttempt(string $identityKey, string $passwordType): void {
        $key = $this->attemptsKey($identityKey, $passwordType);
        $count = $this->getAttemptCount($identityKey, $passwordType) + 1;

        if (function_exists('apcu_store')) {
            apcu_store($key, $count, self::LOCKOUT_WINDOW_SECONDS);
            return;
        }

        $file = $this->fallbackFile($key);
        file_put_contents($file, json_encode(['count' => $count, 'expires' => time() + self::LOCKOUT_WINDOW_SECONDS]));
    }

    private function resetAttempts(string $identityKey, string $passwordType): void {
        $key = $this->attemptsKey($identityKey, $passwordType);
        if (function_exists('apcu_delete')) {
            apcu_delete($key);
            return;
        }
        $file = $this->fallbackFile($key);
        if (file_exists($file)) {
            unlink($file);
        }
    }

    private function fallbackFile(string $key): string {
        $safeKey = preg_replace('/[^a-zA-Z0-9_]/', '_', $key) ?? 'unknown';
        $dir = APP_ROOT . '/log';
        if (!is_dir($dir)) {
            mkdir($dir, 0755, true);
        }
        return $dir . "/pwdgate_$safeKey.json";
    }
}
