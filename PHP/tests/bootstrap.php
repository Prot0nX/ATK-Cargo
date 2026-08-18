<?php
// PHP/tests/bootstrap.php

declare(strict_types=1);

require dirname(__DIR__) . '/vendor/autoload.php';

// چند سرویس (LoginAttemptLimiter، SessionService::logActivity) به APP_ROOT
// برای نوشتن فایل‌های fallback/لاگ نیاز دارند؛ در اجرای عادی این ثابت در
// src/bootstrap.php تعریف می‌شود که تست‌ها آن را include نمی‌کنند. اینجا به یک
// پوشه‌ی موقت جدا از مخزن تعریف می‌شود تا این فایل‌ها با اجرای واقعی سرور
// قاطی نشوند.
if (!defined('APP_ROOT')) {
    define('APP_ROOT', sys_get_temp_dir() . '/atk_cargo_phpunit');
}
