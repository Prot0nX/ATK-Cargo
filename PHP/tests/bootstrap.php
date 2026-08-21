<?php
// PHP/tests/bootstrap.php

declare(strict_types=1);

require dirname(__DIR__) . '/vendor/autoload.php';

// تعریف APP_ROOT به یک پوشه موقت جدا از مخزن برای جلوگیری از تداخل فایل‌های fallback/لاگ با اجرای واقعی سرور
if (!defined('APP_ROOT')) {
    define('APP_ROOT', sys_get_temp_dir() . '/atk_cargo_phpunit');
}
