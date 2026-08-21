<?php
// نقطه ورود واحد API v2؛ چون mod_rewrite روی هاست تولید فعال نیست، مسیر route از query string (?route=) خوانده می‌شود نه REQUEST_URI
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
