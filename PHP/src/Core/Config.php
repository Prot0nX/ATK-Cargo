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
            // قبلاً همان هش واقعی رمز پیش‌فرض ادمین که در .env.example هم بود
            // (S-14) اینجا هم به‌عنوان fallback کد شده بود — یعنی حتی بعد از
            // خالی‌کردن .env.example، اگر ADMIN_PASSWORD_HASH در .env واقعی
            // تنظیم نمی‌شد، این مقدار ثابت و در گیت قابل‌مشاهده هنوز فعال بود.
            // فعلاً هیچ‌جای کد از این تنظیم استفاده نمی‌کند (PermissionManager.php
            // مستقیماً از env می‌خواند)، اما نگه‌داشتنش یک راز زنده در سورس بود.
            'admin_password_hash' => $_ENV['ADMIN_PASSWORD_HASH'] ?? getenv('ADMIN_PASSWORD_HASH') ?: '',
            'session_timeout' => 86400, // 24 ساعت به ثانیه
        ];

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
