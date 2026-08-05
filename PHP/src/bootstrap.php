<?php
// PHP/src/bootstrap.php

declare(strict_types=1);

// تعیین مسیر اصلی پوشه PHP
define('APP_ROOT', dirname(__DIR__));

// بارگذاری Autoloader رسمی Composer در صورت وجود
if (file_exists(APP_ROOT . '/vendor/autoload.php')) {
    require_once APP_ROOT . '/vendor/autoload.php';
}

// ثبت Autoloader سفارشی برای کلاس‌های App (جهت پشتیبانی دائم)
spl_autoload_register(function (string $class) {
    $prefix = 'App\\';
    $base_dir = APP_ROOT . '/src/';
    
    // بررسی اینکه کلاس از فضای نام App استفاده می‌کند
    $len = strlen($prefix);
    if (strncmp($prefix, $class, $len) !== 0) {
        return;
    }
    
    // دریافت نام نسبی کلاس
    $relative_class = substr($class, $len);
    
    // تبدیل جداکننده‌های فضای نام به جداکننده مسیر دایرکتوری و اضافه کردن پسوند php
    $file = $base_dir . str_replace('\\', '/', $relative_class) . '.php';
    
    // بارگذاری فایل در صورت وجود
    if (file_exists($file)) {
        require_once $file;
    }
});

// تنظیم منطقه زمانی تهران
date_default_timezone_set('Asia/Tehran');

// بررسی و بارگذاری فایل jdf.php در صورت وجود
if (file_exists(APP_ROOT . '/jdf.php')) {
    require_once APP_ROOT . '/jdf.php';
}

// بارگذاری فایل پیکربندی قدیمی در صورت وجود برای حفظ سازگاری
if (file_exists(APP_ROOT . '/config/config.php')) {
    require_once APP_ROOT . '/config/config.php';
}
