<?php
// PHP/config/config.php
// فایل پیکربندی دیتابیس و تنظیمات پایه سیستم

declare(strict_types=1);

if (!defined('DB_HOST')) {
    define('DB_HOST', 'localhost');
}
if (!defined('DB_USER')) {
    define('DB_USER', 'root');
}
if (!defined('DB_PASSWORD')) {
    define('DB_PASSWORD', '');
}
if (!defined('DB_NAME')) {
    define('DB_NAME', 'atk_cargo');
}

/**
     * دریافت کانکشن خام mysqli برای کدهای قدیمی
 */
if (!function_exists('getDbConnection')) {
    function getDbConnection(): mysqli {
        $conn = new mysqli(DB_HOST, DB_USER, DB_PASSWORD, DB_NAME);
        if ($conn->connect_error) {
            throw new Exception("خطا در اتصال به پایگاه داده: " . $conn->connect_error);
        }
        $conn->set_charset("utf8mb4");
        return $conn;
    }
}
