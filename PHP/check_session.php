<?php
// PHP/check_session.php

declare(strict_types=1);

require_once __DIR__ . '/src/bootstrap.php';

use App\Controllers\AuthController;
use App\Core\Response;

try {
    $controller = new AuthController();
    $controller->checkSession();
} catch (Exception $e) {
    error_log("Error in check_session.php wrapper: " . $e->getMessage());
    // ۵۰۳ (نه ۲۰۰) — کلاینت اندروید بین «خطای گذرای سرور» و «رد صریح نشست» تفاوت
    // می‌گذارد و فقط حالت دوم را fail-closed می‌کند؛ ۲۰۰ باعث خروج اجباری همه
    // کاربران با هر قطعی موقت دیتابیس/سرور می‌شد
    Response::json([
        'success' => false,
        'message' => 'خطایی در سرور رخ داده است.',
        'userType' => null
    ], 503);
}