<?php
// PHP/src/Core/Router.php

declare(strict_types=1);

namespace App\Core;

/**
 * Router صریح نسخه‌ی ۲ API — جایگزین مدل «هر فایل .php در پوشه = یک endpoint»
 * که protected_proxy.php (v1) با whitelist opt-out پیاده‌سازی می‌کند. اینجا
 * برعکس: فقط دقیقاً همان مسیرهایی که در routes/api_v2.php صراحتاً تعریف
 * شده‌اند قابل دسترسی‌اند (opt-in) — هیچ include پویا و هیچ glob روی
 * فایل‌سیستم وجود ندارد.
 *
 * هر ورودی route یک آرایه با کلیدهای زیر است:
 *   'method'     => 'GET'|'POST'|...
 *   'path'       => الگوی مسیر، مثل 'ships/{shipName}'
 *   'auth'       => bool — آیا این route نیاز به نشست معتبر دارد
 *   'permission' => string|null — در صورت نیاز به مجوز خاص (بعد از auth بررسی می‌شود)
 *   'handler'    => callable(array $pathParams, Request $request, ?string $username, ?string $userType): void
 *                   handler مسئول فراخوانی Response::json/error/success (یا هم‌ارز) است.
 */
final class Router {
    /** @var array<int, array{method:string, path:string, auth:bool, permission:?string, handler:callable}> */
    private array $routes;

    public function __construct(array $routes) {
        // مسیرهای دقیق‌تر (با تعداد سگمنت {پارامتر} کمتر) باید قبل از
        // مسیرهای عمومی‌تر بررسی شوند — وگرنه ترتیب فیزیکی ردیف‌ها در
        // routes/api_v2.php (که به‌سادگی می‌تواند بعداً جابه‌جا شود) نتیجه را
        // تعیین می‌کند. مثال واقعی: 'quotas/filtered' باید قبل از
        // 'quotas/{quotaNumber}' تطبیق داده شود وگرنه دومی همیشه برنده است.
        // usort با معیار پایدار (تعداد پارامترها) این وابستگی به ترتیب دستی
        // را حذف می‌کند.
        usort($routes, static function (array $a, array $b): int {
            return self::countParams($a['path']) <=> self::countParams($b['path']);
        });
        $this->routes = $routes;
    }

    private static function countParams(string $path): int {
        return substr_count($path, '{');
    }

    public function dispatch(string $method, string $rawPath): void {
        $method = strtoupper($method);
        $path = trim((string)parse_url($rawPath, PHP_URL_PATH), '/');

        // پیشوند api/v2/ در تمام مسیرهای rewrite‌شده وجود دارد (.htaccess را
        // ببینید)؛ اینجا حذف می‌شود تا الگوهای routes/api_v2.php تمیز بمانند.
        $path = preg_replace('#^api/v2/?#', '', $path) ?? $path;

        $request = new Request();

        $pathAllowedMethods = [];
        foreach ($this->routes as $route) {
            $params = $this->matchPath($route['path'], $path);
            if ($params === null) {
                continue;
            }
            if ($route['method'] !== $method) {
                $pathAllowedMethods[] = $route['method'];
                continue;
            }

            // پارامترهای مسیر (مثل {id}) در $_GET هم قرار می‌گیرند تا اگر
            // handler داخلاً از Request::get() استفاده کند (سازگار با الگوی
            // فعلی کنترلرها) هم کار کند.
            foreach ($params as $key => $value) {
                $_GET[$key] = $value;
            }

            $username = null;
            $userType = null;
            if ($route['auth']) {
                [$username, $userType] = ApiAuthGate::requireAuthenticated($request);
                if ($route['permission'] !== null) {
                    ApiAuthGate::requirePermission($username, $userType, $route['permission']);
                }
            }

            ($route['handler'])($params, $request, $username, $userType);
            return;
        }

        if (!empty($pathAllowedMethods)) {
            // مسیر وجود دارد ولی متد HTTP اشتباه است — 405 دقیق‌تر از 404 عمومی است.
            Response::error('روش درخواست برای این مسیر مجاز نیست.', 405);
        }

        Response::error('مسیر یافت نشد.', 404);
    }

    /**
     * @return array<string, string>|null آرایه‌ی پارامترهای مسیر در صورت تطبیق، یا null
     */
    private function matchPath(string $pattern, string $path): ?array {
        $patternParts = $pattern === '' ? [] : explode('/', trim($pattern, '/'));
        $pathParts = $path === '' ? [] : explode('/', $path);

        if (count($patternParts) !== count($pathParts)) {
            return null;
        }

        $params = [];
        foreach ($patternParts as $index => $part) {
            if (preg_match('/^\{(\w+)\}$/', $part, $matches) === 1) {
                if ($pathParts[$index] === '') {
                    return null;
                }
                $params[$matches[1]] = rawurldecode($pathParts[$index]);
                continue;
            }
            if ($part !== $pathParts[$index]) {
                return null;
            }
        }

        return $params;
    }
}
