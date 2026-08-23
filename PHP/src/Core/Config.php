<?php
// PHP/src/Core/Config.php

declare(strict_types=1);

namespace App\Core;

class Config {
    private static ?self $instance = null;
    private array $settings = [];

    private function __construct() {
        $this->settings = [
            'db_host' => defined('DB_HOST') ? DB_HOST : ($_ENV['DB_HOST'] ?? 'localhost'),
            'db_user' => defined('DB_USER') ? DB_USER : ($_ENV['DB_USER'] ?? 'root'),
            'db_pass' => defined('DB_PASSWORD') ? DB_PASSWORD : ($_ENV['DB_PASSWORD'] ?? ''),
            'db_name' => defined('DB_NAME') ? DB_NAME : ($_ENV['DB_NAME'] ?? 'atk_cargo'),
 // دیگر مقدار fallback ثابت ندارد؛ فقط از ADMIN_PASSWORD_HASH واقعی خوانده می‌شود
            'admin_password_hash' => $_ENV['ADMIN_PASSWORD_HASH'] ?? getenv('ADMIN_PASSWORD_HASH') ?: '',
            'session_timeout' => 86400, // 24 ساعت به ثانیه

 // رمز پنل لایسنس عمداً از رمز پنل PermissionManager جداست
            'lic_admin_password_hash' => self::env('LIC_ADMIN_PASSWORD_HASH', ''),
            'lic_session_idle_timeout' => (int)(self::env('LIC_SESSION_IDLE_TIMEOUT', '1800') ?: '1800'),

 // رمز داشبورد مانیتورینگ — مستقل از دو رمز بالا
            'monitoring_admin_password_hash' => self::env('MONITORING_ADMIN_PASSWORD_HASH', ''),
            'monitoring_session_idle_timeout' => (int)(self::env('MONITORING_SESSION_IDLE_TIMEOUT', '1800') ?: '1800'),

 // رمز پنل گزارش آماری کوتاژ — مستقل از سه رمز بالا
            'quota_reports_admin_password_hash' => self::env('QUOTA_REPORTS_ADMIN_PASSWORD_HASH', ''),
            'quota_reports_session_idle_timeout' => (int)(self::env('QUOTA_REPORTS_SESSION_IDLE_TIMEOUT', '1800') ?: '1800'),
        ];

    }

 // خواندن متمرکز یک متغیر محیطی؛ رشته‌ی خالی هم مثل مقدار تنظیم‌نشده در نظر گرفته می‌شود
    public static function env(string $key, ?string $default = null): ?string {
 // فقط کلید تعریف‌نشده (false) یا رشته‌ی خالی باقی می‌ماند
        $value = $_ENV[$key] ?? getenv($key);
        if ($value === false || $value === '') {
            return $default;
        }
        return (string)$value;
    }

    public static function getInstance(): self {
        if (self::$instance === null) {
            self::$instance = new self();
        }
        return self::$instance;
    }

    public function get(string $key, $default = null) {
        return $this->settings[$key] ?? $default;
    }

    public function set(string $key, $value): void {
        $this->settings[$key] = $value;
    }
}
