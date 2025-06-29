<?php
// config.php

// استفاده از متغیرهای محیطی برای ذخیره اطلاعات حساس
define('DB_NAME', getenv('DB_NAME') ?: 'myapp_db');
define('DB_USER', getenv('DB_USER') ?: 'myapp_user');
define('DB_PASSWORD', getenv('DB_PASSWORD') ?: '5rvaC89xQPeqx1/p');
define('DB_HOST', getenv('DB_HOST') ?: 'localhost');

// تابع برای ایجاد اتصال به پایگاه داده
function getDbConnection() {
    static $conn;
    if ($conn === null) {
        $conn = new mysqli(DB_HOST, DB_USER, DB_PASSWORD, DB_NAME);
        if ($conn->connect_error) {
            error_log("خطا در اتصال به پایگاه داده: " . $conn->connect_error);
            die("خطا در اتصال به پایگاه داده. لطفاً بعداً دوباره تلاش کنید.");
        }
        $conn->set_charset("utf8mb4");
    }
    return $conn;
}
?>