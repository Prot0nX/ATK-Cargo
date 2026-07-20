<?php
// PHP/src/Core/Config.php

declare(strict_types=1);

namespace App\Core;

class Config {
    private static ?self $instance = null;
    private array $settings = [];

    private function __construct() {
        // لود کردن مقادیر پیش‌فرض
        $this->settings = [
            'db_host' => defined('DB_HOST') ? DB_HOST : ($_ENV['DB_HOST'] ?? 'localhost'),
            'db_user' => defined('DB_USER') ? DB_USER : ($_ENV['DB_USER'] ?? 'root'),
            'db_pass' => defined('DB_PASSWORD') ? DB_PASSWORD : ($_ENV['DB_PASSWORD'] ?? ''),
            'db_name' => defined('DB_NAME') ? DB_NAME : ($_ENV['DB_NAME'] ?? 'atk_cargo'),
            'admin_password_hash' => '$2y$10$bUbu4IBE6ZJKpLL5lxc9puyUEUJg3o9F/zzI896I2U6vUPA5OjI.S',
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
