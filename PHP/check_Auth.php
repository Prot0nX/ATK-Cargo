<?php
/**
 * Gateway endpoint for user login (check_Auth.php)
 */

declare(strict_types=1);

date_default_timezone_set('Asia/Tehran');

// Load PSR-4 Autoloader
require_once __DIR__ . '/src/Core/Autoloader.php';
\AtkCargo\Core\Autoloader::register();

// Register Exception Handler and config
\AtkCargo\Core\ExceptionHandler::register();
\AtkCargo\Core\Config::init();

// Process request
$request = new \AtkCargo\Core\Request();
$controller = new \AtkCargo\Controllers\AuthController();
$controller->login($request);