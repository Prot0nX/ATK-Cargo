<?php
// PHP/users_api.php

declare(strict_types=1);

require_once __DIR__ . '/src/bootstrap.php';

use App\Controllers\UserController;
use App\Core\Response;

try {
    $controller = new UserController();
    $controller->handle();
} catch (Exception $e) {
    error_log("Error in users_api.php wrapper: " . $e->getMessage());
    Response::json([
        'success' => false,
        'message' => 'خطایی در سرور رخ داده است.'
    ], 500);
}