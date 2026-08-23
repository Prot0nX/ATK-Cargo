<?php
// PHP/config/config.php فایل پیکربندی دیتابیس و تنظیمات پایه سیستم.

declare(strict_types=1);

// بارگذاری متغیرهای محیطی از فایل .env

if (!function_exists('loadEnvFile')) {
    function loadEnvFile(string $filePath): void {
        if (!file_exists($filePath)) {
            return;
        }
        $lines = file($filePath, FILE_IGNORE_NEW_LINES | FILE_SKIP_EMPTY_LINES);
        foreach ($lines as $line) {
            $line = trim($line);
            if (empty($line) || strpos($line, '#') === 0) {
                continue;
            }
            if (strpos($line, '=') !== false) {
                list($key, $value) = explode('=', $line, 2);
                $key = trim($key);
                $value = trim($value, " \t\n\r\0\x0B\"'");
                if (!array_key_exists($key, $_ENV)) {
                    $_ENV[$key] = $value;
                    putenv("{$key}={$value}");
                }
            }
        }
    }
}

// بارگذاری فایل .env
loadEnvFile(dirname(__DIR__) . '/.env');

if (!defined('DB_HOST')) {
    define('DB_HOST', $_ENV['DB_HOST'] ?? getenv('DB_HOST') ?: 'localhost');
}
if (!defined('DB_USER')) {
    define('DB_USER', $_ENV['DB_USER'] ?? getenv('DB_USER') ?: 'root');
}
if (!defined('DB_PASSWORD')) {
    define('DB_PASSWORD', $_ENV['DB_PASSWORD'] ?? getenv('DB_PASSWORD') ?: '');
}
if (!defined('DB_NAME')) {
    define('DB_NAME', $_ENV['DB_NAME'] ?? getenv('DB_NAME') ?: 'atk_cargo');
}

if (!defined('UPDATE_CHECK_API_KEY')) {
    define('UPDATE_CHECK_API_KEY', $_ENV['UPDATE_CHECK_API_KEY'] ?? getenv('UPDATE_CHECK_API_KEY') ?: '');
}