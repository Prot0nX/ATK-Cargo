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

            // پنل مدیریت لایسنس (PHP/Lic) — رمزش عمداً از ADMIN_PASSWORD_HASH
            // (پنل PermissionManager) جداست تا دسترسی به این دو پنل مستقل
            // بتواند به دو نفر متفاوت داده و مستقل چرخانده شود.
            'lic_admin_password_hash' => self::env('LIC_ADMIN_PASSWORD_HASH', ''),
            'lic_session_idle_timeout' => (int)(self::env('LIC_SESSION_IDLE_TIMEOUT', '1800') ?: '1800'),
        ];

    }

    /**
     * خواندن یک متغیر محیطی (بارگذاری‌شده از .env توسط config/config.php).
     *
     * الگوی `$_ENV['X'] ?? getenv('X') ?: $default` پیش از این در چهار فایل
     * جداگانه (PermissionManager، SecurityAlerter، UserService و همین کلاس)
     * دست‌نویس تکرار شده بود؛ این متد همان معنا را در یک جا متمرکز می‌کند.
     *
     * توجه: `?:` عمدی است نه `??` — یعنی رشته‌ی خالی هم مثل مقدار تنظیم‌نشده
     * رفتار می‌کند، چون کلیدهای خالی در .env (مثل `LIC_ADMIN_PASSWORD_HASH=`)
     * به معنای «تنظیم نشده» هستند نه «عمداً خالی».
     */
    public static function env(string $key, ?string $default = null): ?string {
        // `??` خودش حالت null را به getenv واگذار می‌کند، پس اینجا فقط
        // false (کلید تعریف‌نشده) و رشته‌ی خالی باقی می‌ماند.
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
