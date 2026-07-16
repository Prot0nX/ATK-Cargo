<?php
/**
 * Gateway endpoint for user management (users_api.php)
 */

declare(strict_types=1);

date_default_timezone_set('Asia/Tehran');

// Load PSR-4 Autoloader
require_once __DIR__ . '/src/Core/Autoloader.php';
\AtkCargo\Core\Autoloader::register();

// Register Exception Handler and config
\AtkCargo\Core\ExceptionHandler::register();
\AtkCargo\Core\Config::init();

$request = new \AtkCargo\Core\Request();
$controller = new \AtkCargo\Controllers\UserController();

$action = $request->getString('action');

if ($request->getMethod() === 'GET') {
    switch ($action) {
        case 'getAllUsers':
            $controller->getAllUsers($request);
            break;
        case 'getActiveDeviceId':
            $controller->getActiveDeviceId($request);
            break;
        default:
            \AtkCargo\Core\Response::json([
                'success' => false,
                'message' => 'عملیات نامعتبر است'
            ], 400);
    }
} elseif ($request->getMethod() === 'POST') {
    switch ($action) {
        case 'createUser':
            $controller->createUser($request);
            break;
        case 'updateUser':
            $controller->updateUser($request);
            break;
        case 'deleteUser':
            $controller->deleteUser($request);
            break;
        case 'forceLogout':
            $controller->forceLogout($request);
            break;
        default:
            \AtkCargo\Core\Response::json([
                'success' => false,
                'message' => 'عملیات نامعتبر است'
            ], 400);
    }
} else {
    \AtkCargo\Core\Response::json([
        'success' => false,
        'message' => 'روش درخواست نامعتبر است'
    ], 405);
}