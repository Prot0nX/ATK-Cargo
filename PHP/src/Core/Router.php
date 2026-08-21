<?php
// PHP/src/Core/Router.php

declare(strict_types=1);

namespace App\Core;

// Router صریح نسخه‌ی ۲ API با مدل opt-in؛ فقط مسیرهای تعریف‌شده در routes/api_v2.php قابل‌دسترسی‌اند
final class Router {
    /** @var array<int, array{method:string, path:string, auth:bool, permission:?string, handler:callable}> */
    private array $routes;

    public function __construct(array $routes) {
        // مرتب‌سازی بر اساس تعداد پارامتر تا مسیرهای دقیق‌تر قبل از عمومی‌تر بررسی شوند
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

        // حذف پیشوند api/v2/ که توسط .htaccess اضافه شده تا الگوهای مسیر تمیز بمانند
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

            // پارامترهای مسیر در $_GET هم قرار می‌گیرند تا با Request::get() سازگار باشند
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

    /** @return array<string, string>|null آرایه‌ی پارامترهای مسیر در صورت تطبیق، یا null */
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
