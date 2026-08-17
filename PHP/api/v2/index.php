<?php
// PHP/api/v2/index.php
//
// نقطه‌ی ورود واحد API نسخه‌ی ۲ — جایگزین تدریجی protected_proxy.php.
//
// روی هاست تولید (atk-nk.ir) با curl مستقیم روی سرور تأیید شد که mod_rewrite
// از طریق .htaccess اصلاً فعال نیست (AllowOverride این پوشه ظاهراً FileInfo
// را شامل نمی‌شود؛ حتی قانون قدیمی hotlink-protection که ربطی به v2 ندارد هم
// هیچ‌وقت واقعاً اجرا نشده). یعنی URL تمیز مثل /api/v2/auth/login هرگز به این
// فایل نمی‌رسد و صرفاً 404 خام آپاچی می‌گیرد — نه یک محدودیت قابل رفع در
// .htaccess. راه‌حل قابل‌اتکا، دقیقاً همان الگوی اثبات‌شده‌ی v1
// (protected_proxy.php?target=X.php) است: مسیر route از query string خوانده
// می‌شود، نه از REQUEST_URI. اگر یک هاست دیگر AllowOverride FileInfo داشته
// باشد، rewrite تعریف‌شده در .htaccess همچنان به همین فایل با REQUEST_URI
// تمیز می‌رسد و به‌عنوان fallback کار می‌کند.
declare(strict_types=1);

require_once __DIR__ . '/../../src/bootstrap.php';

use App\Core\Response;
use App\Core\Router;

try {
    $routeParam = $_GET['route'] ?? null;
    $path = $routeParam !== null
        ? '/' . ltrim((string)$routeParam, '/')
        : ($_SERVER['REQUEST_URI'] ?? '/');

    $routes = require __DIR__ . '/../../src/routes/api_v2.php';
    $router = new Router($routes);
    $router->dispatch($_SERVER['REQUEST_METHOD'] ?? 'GET', $path);
} catch (\Throwable $e) {
    error_log('api/v2 unexpected error: ' . $e->getMessage());
    Response::error('خطای داخلی سرور رخ داده است.', 500);
}
