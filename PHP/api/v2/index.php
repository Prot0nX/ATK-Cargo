<?php
// PHP/api/v2/index.php
//
// نقطه‌ی ورود واحد API نسخه‌ی ۲ — جایگزین تدریجی protected_proxy.php.
// تمام درخواست‌های api/v2/* توسط .htaccess به همین فایل rewrite می‌شوند.
// این فایل هیچ include پویا و هیچ منطق تجاری ندارد؛ فقط bootstrap + جدول
// route را بار می‌کند و به Router می‌سپارد.

declare(strict_types=1);

require_once __DIR__ . '/../../src/bootstrap.php';

use App\Core\Response;
use App\Core\Router;

try {
    $routes = require __DIR__ . '/../../src/routes/api_v2.php';
    $router = new Router($routes);
    $router->dispatch($_SERVER['REQUEST_METHOD'] ?? 'GET', $_SERVER['REQUEST_URI'] ?? '/');
} catch (\Throwable $e) {
    error_log('api/v2 unexpected error: ' . $e->getMessage());
    Response::error('خطای داخلی سرور رخ داده است.', 500);
}
