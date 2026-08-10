<?php
// PHP/app_api.php

declare(strict_types=1);

require_once __DIR__ . '/src/bootstrap.php';

use App\Controllers\AppApiController;

try {
    $controller = new AppApiController();
    $controller->handle();
} catch (Exception $e) {
    // پیام‌های Exception در AppApiController متن فارسی صریح و امن هستند (مثلاً
    // «نام کشتی مشخص نشده است») و مستقیماً به کلاینت نمایش داده می‌شوند.
    header('Content-Type: application/json; charset=UTF-8');
    http_response_code(400);
    echo json_encode(['error' => $e->getMessage()], JSON_UNESCAPED_UNICODE);
} catch (Throwable $e) {
    // خطاهای غیرمنتظره (Error/TypeError و مشابه) ممکن است مسیر فایل یا جزئیات
    // داخلی را در پیام خود داشته باشند؛ این‌ها فقط در لاگ سرور ثبت می‌شوند.
    error_log('app_api.php unexpected error: ' . $e->getMessage());
    header('Content-Type: application/json; charset=UTF-8');
    http_response_code(500);
    echo json_encode(['error' => 'خطای داخلی سرور رخ داده است.'], JSON_UNESCAPED_UNICODE);
}