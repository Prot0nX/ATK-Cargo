<?php
// PHP/check_logout.php

declare(strict_types=1);

require_once __DIR__ . '/src/bootstrap.php';

use App\Controllers\AuthController;
use App\Core\Response;

try {
    $controller = new AuthController();
    $controller->logout();
} catch (Exception $e) {
    error_log("Error in check_logout.php wrapper: " . $e->getMessage());
    Response::json([
        'success' => false,
        'message' => 'خطایی در سرور رخ داده است.'
    ], 500);
}