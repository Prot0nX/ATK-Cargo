<?php
// PHP/app_api.php

declare(strict_types=1);

require_once __DIR__ . '/src/bootstrap.php';

use App\Controllers\AppApiController;

try {
    $controller = new AppApiController();
    $controller->handle();
} catch (Exception $e) {
    header('Content-Type: application/json; charset=UTF-8');
    http_response_code(500);
    echo json_encode(['error' => $e->getMessage()], JSON_UNESCAPED_UNICODE);
}