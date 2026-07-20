<?php
// PHP/saveOrUpdateCargoInfo.php

declare(strict_types=1);

require_once __DIR__ . '/src/bootstrap.php';

use App\Controllers\CargoController;

try {
    $controller = new CargoController();
    $controller->saveOrUpdate();
} catch (Exception $e) {
    error_log("Error in saveOrUpdateCargoInfo.php wrapper: " . $e->getMessage());
    http_response_code(500);
    echo json_encode([
        'error' => true,
        'message' => 'خطای داخلی سرور: ' . $e->getMessage()
    ], JSON_UNESCAPED_UNICODE | JSON_THROW_ON_ERROR);
}