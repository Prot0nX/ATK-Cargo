<?php
// PHP/check_Auth.php

declare(strict_types=1);

require_once __DIR__ . '/src/bootstrap.php';

use App\Controllers\AuthController;
use App\Core\Response;

try {
    $controller = new AuthController();
    $controller->login();
} catch (Exception $e) {
    error_log("Error in check_Auth.php wrapper: " . $e->getMessage());
    Response::json([
        'success' => false,
        'message' => 'خطایی در سرور رخ داده است. لطفا بعدا تلاش کنید.',
        'userType' => null
    ], 200); // 200 جهت پایداری با کلاینت اندروید
}