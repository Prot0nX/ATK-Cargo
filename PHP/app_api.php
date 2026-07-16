<?php
/**
 * Gateway endpoint for general Cargo APIs (app_api.php)
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
$controller = new \AtkCargo\Controllers\CargoController();

$action = $request->getString('action');

switch ($action) {
    case 'getShipsList':
        $controller->getShipsList($request);
        break;
    case 'getShipDetails':
        $controller->getShipDetails($request);
        break;
    case 'getWarehouseDetails':
        $controller->getWarehouseDetails($request);
        break;
    case 'getQuotaDetails':
        $controller->getQuotaDetails($request);
        break;
    case 'getQuotasList':
        $controller->getQuotasList($request);
        break;
    case 'getFilteredQuotas':
        $controller->getFilteredQuotas($request);
        break;
    case 'getFilteredSummary':
        $controller->getFilteredSummary($request);
        break;
    case 'checkQuotaExistence':
    case 'checkQuotaExistenceCargo':
        $controller->checkQuotaExistence($request);
        break;
    case 'getGroupedQuotas':
        $controller->getGroupedQuotas($request);
        break;
    case 'updateTemporaryTonnage':
        $controller->updateTemporaryTonnage($request);
        break;
    case 'editQuota':
        $controller->editQuota($request);
        break;
    case 'updateQuotaPercentage':
        $controller->updateQuotaPercentage($request);
        break;
    case 'toggleQuotaStatus':
        $controller->toggleQuotaStatus($request);
        break;
    case 'updateQuotaPercentageRestriction':
        $controller->updateQuotaPercentageRestriction($request);
        break;
    case 'deleteQuota':
        $controller->deleteQuota($request);
        break;
    case 'getLoadableTonnage':
        $controller->getLoadableTonnage($request);
        break;
    case 'checkQuotaStatus':
        $controller->checkQuotaStatus($request);
        break;
    case 'getRealTimeData':
        $controller->getRealTimeData($request);
        break;
    default:
        \AtkCargo\Core\Response::json([
            'success' => false,
            'message' => 'عملیات نامعتبر است'
        ], 400);
}