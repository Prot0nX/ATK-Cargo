<?php
/**
 * Gateway endpoint for online users management panel (online_users_api.php)
 */

declare(strict_types=1);

date_default_timezone_set('Asia/Tehran');

// Load PSR-4 Autoloader
require_once __DIR__ . '/../src/Core/Autoloader.php';
\AtkCargo\Core\Autoloader::register();

// Register Exception Handler and config
\AtkCargo\Core\ExceptionHandler::register();
\AtkCargo\Core\Config::init();

$request = new \AtkCargo\Core\Request();
$controller = new \AtkCargo\Controllers\AuthController();

$action = $request->getString('action', 'get_online_users');

switch ($action) {
    case 'get_online_users':
        $controller->getOnlineUsers($request);
        break;
    case 'get_session_stats':
        $controller->getSessionStats($request);
        break;
    case 'force_logout':
        $controller->forceLogoutFromDeviceWeb($request);
        break;
    case 'logout_all_users':
        $controller->logoutAllActiveUsers($request);
        break;
    case 'get_all_sessions':
        $controller->getAllSessions($request);
        break;
    default:
        \AtkCargo\Core\Response::json([
            'success' => false,
            'message' => 'عملیات نامعتبر است'
        ], 400);
}