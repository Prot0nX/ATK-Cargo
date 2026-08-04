<?php
// PHP/src/Controllers/AppApiController.php

declare(strict_types=1);

namespace App\Controllers;

use Exception;
use mysqli;
use mysqli_stmt;
use App\Core\Database;
use App\Core\DatabaseManager;
use App\Core\Request;
use App\Core\Response;

class AppApiController {
    private DatabaseManager $db;
    private Request $request;

    public function __construct() {
        $this->db = new DatabaseManager();
        $this->request = new Request();
    }

    public function handle(): void {
        try {
            if (!$this->request->isGet()) {
                throw new Exception('روش درخواست نامعتبر است');
            }

            $action = $this->request->get('action');
            if (!$action) {
                throw new Exception('عملیات مشخص نشده است');
            }

            $action = $this->sanitizeInput((string)$action);

            switch ($action) {
                case 'getShipsList':
                    $ships = $this->getShipsList();
                    $this->sendJsonResponse($ships);
                    break;

                case 'getShipDetails':
                    $shipName = $this->request->get('shipName');
                    if (!$shipName) {
                        throw new Exception('نام کشتی مشخص نشده است');
                    }
                    $shipDetails = $this->getShipDetails((string)$shipName);
                    $this->sendJsonResponse($shipDetails);
                    break;

                case 'getWarehouseDetails':
                    $shipName = $this->request->get('shipName');
                    $warehouseName = $this->request->get('warehouseName');
                    if (!$shipName || !$warehouseName) {
                        throw new Exception('نام کشتی یا انبار مشخص نشده است');
                    }
                    $warehouseDetails = $this->getWarehouseDetails((string)$shipName, (string)$warehouseName);
                    $this->sendJsonResponse($warehouseDetails);
                    break;

                case 'getQuotaDetails':
                    $quotaNumber = $this->request->get('quotaNumber');
                    if (!$quotaNumber) {
                        throw new Exception('شماره کوتاژ مشخص نشده است');
                    }
                    $quotaDetails = $this->getQuotaDetails((string)$quotaNumber);
                    if ($quotaDetails === null) {
                        $this->sendJsonResponse(['error' => 'کوتاژ مورد نظر یافت نشد'], 404);
                    } else {
                        $this->sendJsonResponse($quotaDetails);
                    }
                    break;

                case 'getQuotasList':
                    $shipName = $this->request->get('shipName');
                    if (!$shipName) {
                        throw new Exception('نام کشتی مشخص نشده است');
                    }
                    $quotasList = $this->getQuotasList((string)$shipName);
                    $this->sendJsonResponse($quotasList);
                    break;

                case 'getFilteredQuotas':
                    $shipName = $this->request->get('shipName');
                    $startDateTime = $this->request->get('startDateTime');
                    $endDateTime = $this->request->get('endDateTime');
                    if (!$shipName || !$startDateTime || !$endDateTime) {
                        throw new Exception('پارامترهای ورودی ناقص هستند - نام کشتی، تاریخ شروع و پایان الزامی است');
                    }
                    $filteredQuotas = $this->getFilteredQuotas((string)$shipName, (string)$startDateTime, (string)$endDateTime);
                    $this->sendJsonResponse($filteredQuotas);
                    break;

                case 'getFilteredSummary':
                    $shipName = $this->request->get('shipName');
                    $warehouseName = $this->request->get('warehouseName');
                    $selectedQuota = $this->request->get('selectedQuota');
                    $startDateTime = $this->request->get('startDateTime');
                    $endDateTime = $this->request->get('endDateTime');
                    if (!$shipName || !$warehouseName || !$selectedQuota || !$startDateTime || !$endDateTime) {
                        throw new Exception('پارامترهای ورودی ناقص هستند');
                    }
                    $filteredSummary = $this->getFilteredSummary(
                        (string)$shipName,
                        (string)$warehouseName,
                        (string)$selectedQuota,
                        (string)$startDateTime,
                        (string)$endDateTime
                    );
                    header('Content-Type: application/json; charset=UTF-8');
                    echo $filteredSummary;
                    exit;

                case 'checkQuotaExistence':
                case 'checkQuotaExistenceCargo':
                    $quotaNumber = $this->request->get('quotaNumber');
                    $shipName = $this->request->get('shipName');
                    if (!$quotaNumber || !$shipName) {
                        throw new Exception('شماره کوتاژ یا نام کشتی مشخص نشده است');
                    }
                    $result = $this->checkQuotaExistenceCargo((string)$quotaNumber, (string)$shipName);
                    $this->sendJsonResponse($result);
                    break;

                case 'getGroupedQuotas':
                    $groupedQuotas = $this->getGroupedQuotas();
                    $this->sendJsonResponse($groupedQuotas);
                    break;

                case 'updateTemporaryTonnage':
                    $quotaNumber = $this->request->get('quotaNumber');
                    $enabled = $this->request->get('enabled');
                    if ($quotaNumber === null || $enabled === null) {
                        throw new Exception('پارامترهای ورودی ناقص هستند');
                    }
                    $enabledVal = intval($enabled);
                    $tonnage = $this->request->get('tonnage');
                    if ($enabledVal === 1 && $tonnage === null) {
                        throw new Exception('مقدار تناژ موقت الزامی است');
                    }
                    $tonnageVal = $tonnage !== null ? floatval($tonnage) : null;
                    $result = $this->updateTemporaryTonnage((string)$quotaNumber, $enabledVal, $tonnageVal);
                    $this->sendJsonResponse($result);
                    break;

                case 'editQuota':
                    $params = $this->request->all();
                    $required = ['id', 'oldQuotaNumber', 'newQuotaNumber', 'shipName', 'shippingCompany', 'warehouse', 'cargoType', 'totalTonnage'];
                    foreach ($required as $field) {
                        if (!isset($params[$field])) {
                            throw new Exception('پارامترهای ورودی ناقص هستند');
                        }
                    }
                    $result = $this->editQuota(
                        intval($params['id']),
                        (string)$params['oldQuotaNumber'],
                        (string)$params['newQuotaNumber'],
                        (string)$params['shipName'],
                        (string)$params['shippingCompany'],
                        (string)$params['warehouse'],
                        (string)$params['cargoType'],
                        floatval($params['totalTonnage'])
                    );
                    $this->sendJsonResponse(['success' => $result]);
                    break;

                case 'updateQuotaPercentage':
                    $quotaNumber = $this->request->get('quotaNumber');
                    $percentage = $this->request->get('percentage');
                    if ($quotaNumber === null || $percentage === null) {
                        throw new Exception('پارامترهای ورودی ناقص هستند');
                    }
                    $result = $this->updateQuotaPercentage((string)$quotaNumber, floatval($percentage));
                    $this->sendJsonResponse(['success' => $result]);
                    break;

                case 'toggleQuotaStatus':
                    $quotaNumber = $this->request->get('quotaNumber');
                    if ($quotaNumber === null) {
                        throw new Exception('شماره کوتاژ مشخص نشده است');
                    }
                    $id = $this->request->get('id') ? intval($this->request->get('id')) : 0;
                    $result = $this->toggleQuotaStatus((string)$quotaNumber, $id);
                    $this->sendJsonResponse(['success' => $result]);
                    break;

                case 'updateQuotaPercentageRestriction':
                    $quotaNumber = $this->request->get('quotaNumber');
                    $isEnabled = $this->request->get('isEnabled');
                    if ($quotaNumber === null || $isEnabled === null) {
                        throw new Exception('پارامترهای ورودی ناقص هستند');
                    }
                    $result = $this->updateQuotaPercentageRestriction((string)$quotaNumber, intval($isEnabled));
                    $this->sendJsonResponse(['success' => $result]);
                    break;

                case 'deleteQuota':
                    $quotaNumber = $this->request->get('quotaNumber');
                    $shipName = $this->request->get('shipName');
                    $warehouse = $this->request->get('warehouse');
                    $shippingCompany = $this->request->get('shippingCompany');
                    $cargoType = $this->request->get('cargoType');
                    if (!$quotaNumber || !$shipName || !$warehouse || !$shippingCompany || !$cargoType) {
                        throw new Exception('پارامترهای ورودی ناقص هستند');
                    }
                    $result = $this->deleteQuota(
                        (string)$quotaNumber,
                        (string)$shipName,
                        (string)$warehouse,
                        (string)$shippingCompany,
                        (string)$cargoType
                    );
                    $this->sendJsonResponse(['success' => $result]);
                    break;

                case 'getLoadableTonnage':
                    $quotaNumber = $this->request->get('quotaNumber');
                    if (!$quotaNumber) {
                        throw new Exception('شماره کوتاژ مشخص نشده است');
                    }
                    $shippingCompany = $this->request->get('shippingCompany', '');
                    $warehouse = $this->request->get('warehouse', '');
                    $cargoType = $this->request->get('cargoType', '');
                    $result = $this->getLoadableTonnage(
                        (string)$quotaNumber,
                        (string)$shippingCompany,
                        (string)$warehouse,
                        (string)$cargoType
                    );
                    $this->sendJsonResponse($result);
                    break;

                case 'checkQuotaStatus':
                    $quotaNumber = $this->request->get('quotaNumber');
                    if (!$quotaNumber) {
                        throw new Exception('شماره کوتاژ مشخص نشده است');
                    }
                    $params = [
                        'quotaNumber' => (string)$quotaNumber,
                        'shipName' => (string)$this->request->get('shipName', ''),
                        'cargoType' => (string)$this->request->get('cargoType', ''),
                        'shippingCompany' => (string)$this->request->get('shippingCompany', ''),
                        'warehouse' => (string)$this->request->get('warehouse', '')
                    ];
                    $status = $this->checkQuotaStatus($params);
                    $this->sendJsonResponse($status);
                    break;

                case 'getRealTimeData':
                    $shipsList = $this->getShipsList();
                    $realTimeData = [
                        'ships' => $shipsList['data']['activeShips'] ?? [],
                        'timestamp' => date('Y-m-d H:i:s'),
                        'status' => 'success'
                    ];
                    $this->sendJsonResponse($realTimeData);
                    break;

                default:
                    throw new Exception('عملیات نامعتبر است');
            }
        } catch (Exception $e) {
            $this->sendJsonResponse(['error' => $e->getMessage()], 500);
        }
    }

    private function sanitizeInput(string $input): string {
        return htmlspecialchars(strip_tags(trim($input)), ENT_QUOTES, 'UTF-8');
    }

    private function sendJsonResponse($data, int $statusCode = 200): void {
        header('Content-Type: application/json; charset=UTF-8');
        http_response_code($statusCode);
        echo json_encode($data, JSON_UNESCAPED_UNICODE | JSON_THROW_ON_ERROR);
        exit;
    }

    private function calculateLoadableTonnage(float $remainingTonnage, float $totalTonnage, ?float $percentage, bool $isPercentageRestricted): float {
        if ($isPercentageRestricted && $percentage !== null) {
            $percentageAmount = $totalTonnage * ($percentage / 100);
            return $remainingTonnage - $percentageAmount;
        }
        return $remainingTonnage;
    }

    public function checkQuotaStatus(array $params): array {
        // پیاده‌سازی منطق checkQuotaStatus
        $requiredParams = ['quotaNumber', 'shipName', 'cargoType', 'shippingCompany', 'warehouse'];
        foreach ($requiredParams as $param) {
            if (empty(trim($params[$param] ?? ''))) {
                throw new Exception("پارامتر $param نمی‌تواند خالی باشد");
            }
        }

        $quotaNumber = $this->sanitizeInput($params['quotaNumber']);
        $shipName = $this->sanitizeInput($params['shipName']);
        $cargoType = $this->sanitizeInput($params['cargoType']);
        $shippingCompany = $this->sanitizeInput($params['shippingCompany']);
        $warehouse = $this->sanitizeInput($params['warehouse']);

        $query = "SELECT 
            i.loadingQuotaNumber, i.shipName, i.cargoType, i.shippingCompany, i.loadingWarehouse,
            i.cargoWeight as totalWeight, i.isActive, COALESCE(SUM(c.netWeight), 0) as loadedWeight
        FROM InitialInfo i
        LEFT JOIN CargoInfo c ON 
            c.loadingQuotaNumber = i.loadingQuotaNumber AND c.shipName = i.shipName
            AND c.cargoType = i.cargoType AND c.shippingCompany = i.shippingCompany
            AND c.loadingWarehouse = i.loadingWarehouse AND c.status = 'خروج'
        WHERE i.loadingQuotaNumber = ? AND i.shipName = ? AND i.cargoType = ? 
            AND i.shippingCompany = ? AND i.loadingWarehouse = ?
        GROUP BY i.loadingQuotaNumber, i.shipName, i.cargoType, i.shippingCompany, i.loadingWarehouse, i.cargoWeight, i.isActive
        LIMIT 1";

        $stmt = $this->db->prepare($query);
        $stmt->bind_param("sssss", $quotaNumber, $shipName, $cargoType, $shippingCompany, $warehouse);
        $stmt->execute();
        $result = $stmt->get_result();

        if ($row = $result->fetch_assoc()) {
            $totalWeight = (float)$row['totalWeight'];
            $loadedWeight = (float)$row['loadedWeight'];
            $isActiveDb = (bool)$row['isActive'];

            $percentageLoaded = $totalWeight > 0 ? ($loadedWeight / $totalWeight) * 100 : 0.0;
            $remainingCapacity = $totalWeight - $loadedWeight;
            $isActiveStatus = $isActiveDb && ($loadedWeight < $totalWeight);

            $statusMessage = $this->generateStatusMessage($isActiveDb, $percentageLoaded, $quotaNumber, $cargoType);

            return [
                'isActive' => $isActiveStatus,
                'status' => true,
                'message' => $statusMessage,
                'details' => [
                    'quotaNumber' => $row['loadingQuotaNumber'],
                    'shipName' => $row['shipName'],
                    'cargoType' => $row['cargoType'],
                    'shippingCompany' => $row['shippingCompany'],
                    'totalWeight' => $totalWeight,
                    'loadedWeight' => $loadedWeight,
                    'remainingCapacity' => $remainingCapacity,
                    'percentageLoaded' => number_format($percentageLoaded, 2, '.', '')
                ]
            ];
        }

        return [
            'isActive' => false,
            'status' => false,
            'message' => "کوتاژ $quotaNumber با مشخصات درخواستی یافت نشد",
            'details' => null
        ];
    }

    private function generateStatusMessage(bool $isActive, float $percentageLoaded, string $quotaNumber, string $cargoType): string {
        if (!$isActive) {
            return "کوتاژ $quotaNumber با نوع کالای $cargoType غیرفعال است";
        }
        if ($percentageLoaded >= 100) {
            return "ظرفیت بارگیری کوتاژ $quotaNumber با نوع کالای $cargoType تکمیل شده است";
        }
        if ($percentageLoaded >= 95) {
            return "هشدار: ظرفیت بارگیری کوتاژ $quotaNumber با نوع کالای $cargoType به " . number_format($percentageLoaded, 2, '.', '') . "% رسیده است";
        }
        return "کوتاژ $quotaNumber با نوع کالای $cargoType فعال است";
    }

    public function checkQuotaExistenceCargo(string $quotaNumber, string $shipName): array {
        $quotaNumber = $this->sanitizeInput($quotaNumber);
        $shipName = $this->sanitizeInput($shipName);

        $query = "SELECT loadingQuotaNumber, shipName, shippingCompany, cargoType, loadingWarehouse 
        FROM InitialInfo WHERE loadingQuotaNumber LIKE ? AND shipName = ? ORDER BY loadingQuotaNumber";

        $stmt = $this->db->prepare($query);
        $likeQuotaNumber = '%' . $quotaNumber;
        $stmt->bind_param("ss", $likeQuotaNumber, $shipName);
        $stmt->execute();
        $result = $stmt->get_result();

        $matchingQuotas = [];
        while ($row = $result->fetch_assoc()) {
            $matchingQuotas[] = [
                'quotaNumber' => $row['loadingQuotaNumber'],
                'shipName' => $row['shipName'],
                'shippingCompany' => $row['shippingCompany'],
                'cargoType' => $row['cargoType'],
                'warehouse' => $row['loadingWarehouse']
            ];
        }

        $exists = !empty($matchingQuotas);
        $message = $exists ? "کوتاژ(های) مطابق یافت شد." : "کوتاژ مورد نظر در سیستم وجود ندارد.";

        return [
            'exists' => $exists,
            'matchingQuotas' => $matchingQuotas,
            'message' => $message
        ];
    }

    public function getShipsList(): array {
        $query = "SELECT
            i.shipName, i.cargoType, COUNT(DISTINCT i.loadingWarehouse) as warehouseCount,
            COUNT(DISTINCT CONCAT(i.loadingQuotaNumber, '-', i.loadingWarehouse, '-', i.shippingCompany, '-', i.cargoType)) as quotaCount,
            COUNT(DISTINCT i.shippingCompany) as shippingCompanyCount, COUNT(DISTINCT i.cargoType) as cargoTypeCount,
            SUM(i.cargoWeight) as totalTonnage, COALESCE(SUM(loaded.loadedWeight), 0) as loadedTonnage,
            (SUM(i.cargoWeight) - COALESCE(SUM(loaded.loadedWeight), 0)) as remainingTonnage,
            CASE WHEN SUM(i.cargoWeight) > 0 THEN ROUND((COALESCE(SUM(loaded.loadedWeight), 0) / SUM(i.cargoWeight)) * 100, 2) ELSE 0.00 END as percentageLoaded,
            MAX(i.isActive) as isActive
        FROM InitialInfo i
        LEFT JOIN (
            SELECT c.loadingQuotaNumber, c.shipName, c.loadingWarehouse, c.shippingCompany, c.cargoType, SUM(c.netWeight) as loadedWeight
            FROM CargoInfo c WHERE c.status = 'خروج'
            GROUP BY c.loadingQuotaNumber, c.shipName, c.loadingWarehouse, c.shippingCompany, c.cargoType
        ) loaded ON 
            loaded.loadingQuotaNumber = i.loadingQuotaNumber AND loaded.shipName = i.shipName
            AND loaded.loadingWarehouse = i.loadingWarehouse AND loaded.shippingCompany = i.shippingCompany
            AND loaded.cargoType = i.cargoType
        GROUP BY i.shipName, i.cargoType
        ORDER BY isActive DESC, shipName ASC";

        $stmt = $this->db->prepare($query);
        $stmt->execute();
        $result = $stmt->get_result();

        $activeShips = [];
        $inactiveShips = [];
        $totalActiveTonnage = 0;
        $totalRemainingTonnage = 0;
        $totalLoadedTonnage = 0;

        while ($row = $result->fetch_assoc()) {
            $ship = [
                'name' => $row['shipName'],
                'cargoType' => $row['cargoType'],
                'warehouseCount' => (int)$row['warehouseCount'],
                'quotaCount' => (int)$row['quotaCount'],
                'shippingCompanyCount' => (int)$row['shippingCompanyCount'],
                'cargoTypeCount' => (int)$row['cargoTypeCount'],
                'totalTonnage' => (float)$row['totalTonnage'],
                'remainingTonnage' => (float)$row['remainingTonnage'],
                'loadedTonnage' => (float)$row['loadedTonnage'],
                'percentageLoaded' => (float)$row['percentageLoaded'],
                'isActive' => (bool)$row['isActive']
            ];

            if ($ship['isActive']) {
                $activeShips[] = $ship;
                $totalActiveTonnage += $ship['totalTonnage'];
                $totalRemainingTonnage += $ship['remainingTonnage'];
                $totalLoadedTonnage += $ship['loadedTonnage'];
            } else {
                $inactiveShips[] = $ship;
            }
        }

        return [
            'data' => [
                'activeShips' => $activeShips,
                'inactiveShips' => $inactiveShips,
                'statistics' => [
                    'totalShips' => count($activeShips) + count($inactiveShips),
                    'activeShipsCount' => count($activeShips),
                    'inactiveShipsCount' => count($inactiveShips),
                    'totalActiveTonnage' => $totalActiveTonnage,
                    'totalRemainingTonnage' => $totalRemainingTonnage,
                    'totalLoadedTonnage' => $totalLoadedTonnage,
                    'timestamp' => date('Y-m-d H:i:s')
                ]
            ]
        ];
    }

    public function getShipDetails(string $shipName): array {
        $shipName = $this->sanitizeInput($shipName);

        $query = "SELECT 
            i.shipName, i.loadingWarehouse, COUNT(DISTINCT i.loadingQuotaNumber) as quotaCount,
            COUNT(DISTINCT i.shippingCompany) as shippingCompanyCount, COUNT(DISTINCT i.cargoType) as cargoTypeCount,
            COUNT(DISTINCT CONCAT(i.loadingQuotaNumber, i.shipName, i.loadingWarehouse, i.shippingCompany, i.cargoType)) as uniqueQuotaCombinations,
            SUM(i.cargoWeight) as totalTonnage, COALESCE(SUM(loaded.loadedWeight), 0) as loadedTonnage,
            (SELECT COUNT(DISTINCT c.trackingNumber) FROM CargoInfo c WHERE c.shipName = i.shipName AND c.status = 'خروج') as totalVoucherCount,
            MAX(i.isActive) as isActive
        FROM InitialInfo i
        LEFT JOIN (
            SELECT c.loadingQuotaNumber, c.shipName, c.loadingWarehouse, c.shippingCompany, c.cargoType, SUM(c.netWeight) as loadedWeight
            FROM CargoInfo c WHERE c.status = 'خروج'
            GROUP BY c.loadingQuotaNumber, c.shipName, c.loadingWarehouse, c.shippingCompany, c.cargoType
        ) loaded ON 
            loaded.loadingQuotaNumber = i.loadingQuotaNumber AND loaded.shipName = i.shipName
            AND loaded.loadingWarehouse = i.loadingWarehouse AND loaded.shippingCompany = i.shippingCompany
            AND loaded.cargoType = i.cargoType
        WHERE i.shipName = ?
        GROUP BY i.shipName, i.loadingWarehouse";

        $stmt = $this->db->prepare($query);
        $stmt->bind_param("s", $shipName);
        $stmt->execute();
        $result = $stmt->get_result();

        $warehouses = [];
        $totalQuotaCount = 0;
        $totalTonnage = 0;
        $totalRemainingTonnage = 0;
        $totalVoucherCount = 0;
        $isActive = false;

        while ($row = $result->fetch_assoc()) {
            $warehouseTotalTonnage = floatval($row['totalTonnage']);
            $warehouseLoadedTonnage = floatval($row['loadedTonnage']);
            $warehouseRemainingTonnage = $warehouseTotalTonnage - $warehouseLoadedTonnage;
            $warehousePercentageLoaded = ($warehouseTotalTonnage > 0) ? ($warehouseLoadedTonnage / $warehouseTotalTonnage) * 100 : 0;

            $warehouses[] = [
                'name' => $row['loadingWarehouse'],
                'quotaCount' => intval($row['quotaCount']),
                'uniqueQuotaCombinations' => intval($row['uniqueQuotaCombinations']),
                'shippingCompanyCount' => intval($row['shippingCompanyCount']),
                'cargoTypeCount' => intval($row['cargoTypeCount']),
                'totalTonnage' => $warehouseTotalTonnage,
                'remainingTonnage' => $warehouseRemainingTonnage,
                'loadedTonnage' => $warehouseLoadedTonnage,
                'percentageLoaded' => number_format($warehousePercentageLoaded, 2, '.', '')
            ];

            $totalQuotaCount += intval($row['quotaCount']);
            $totalTonnage += $warehouseTotalTonnage;
            $totalRemainingTonnage += $warehouseRemainingTonnage;
            $totalVoucherCount = intval($row['totalVoucherCount']);
            $isActive = (bool)$row['isActive'];
        }

        if (empty($warehouses)) {
            throw new Exception("کشتی با نام '$shipName' یافت نشد.");
        }

        $totalLoadedTonnage = $totalTonnage - $totalRemainingTonnage;
        $totalPercentageLoaded = ($totalTonnage > 0) ? ($totalLoadedTonnage / $totalTonnage) * 100 : 0;

        return [
            'name' => $shipName,
            'warehouseCount' => count($warehouses),
            'quotaCount' => $totalQuotaCount,
            'totalTonnage' => $totalTonnage,
            'remainingTonnage' => $totalRemainingTonnage,
            'loadedTonnage' => $totalLoadedTonnage,
            'percentageLoaded' => number_format($totalPercentageLoaded, 2, '.', ''),
            'totalVoucherCount' => $totalVoucherCount,
            'isActive' => $isActive,
            'warehouses' => $warehouses,
            'lastUpdated' => date('Y-m-d H:i:s')
        ];
    }

    public function getWarehouseDetails(string $shipName, string $warehouseName): array {
        $shipName = $this->sanitizeInput($shipName);
        $warehouseName = $this->sanitizeInput($warehouseName);

        $query = "SELECT 
            i.loadingQuotaNumber, i.cargoType, i.shippingCompany, i.cargoOwner,
            i.cargoWeight as totalTonnage, i.isActive, COALESCE(loaded.loadedWeight, 0) as loadedTonnage,
            COALESCE(vouchers.voucherCount, 0) as voucherCount
        FROM InitialInfo i
        LEFT JOIN (
            SELECT c.loadingQuotaNumber, c.shipName, c.loadingWarehouse, c.shippingCompany, c.cargoType, SUM(c.netWeight) as loadedWeight
            FROM CargoInfo c WHERE c.status = 'خروج' AND c.shipName = ? AND c.loadingWarehouse = ?
            GROUP BY c.loadingQuotaNumber, c.shipName, c.loadingWarehouse, c.shippingCompany, c.cargoType
        ) loaded ON 
            loaded.loadingQuotaNumber = i.loadingQuotaNumber AND loaded.shipName = i.shipName
            AND loaded.loadingWarehouse = i.loadingWarehouse AND loaded.shippingCompany = i.shippingCompany
            AND loaded.cargoType = i.cargoType
        LEFT JOIN (
            SELECT c.loadingQuotaNumber, c.shipName, c.loadingWarehouse, c.shippingCompany, c.cargoType, COUNT(DISTINCT c.trackingNumber) as voucherCount
            FROM CargoInfo c WHERE c.status = 'خروج' AND c.shipName = ? AND c.loadingWarehouse = ?
            GROUP BY c.loadingQuotaNumber, c.shipName, c.loadingWarehouse, c.shippingCompany, c.cargoType
        ) vouchers ON 
            vouchers.loadingQuotaNumber = i.loadingQuotaNumber AND vouchers.shipName = i.shipName
            AND vouchers.loadingWarehouse = i.loadingWarehouse AND vouchers.shippingCompany = i.shippingCompany
            AND vouchers.cargoType = i.cargoType
        WHERE i.shipName = ? AND i.loadingWarehouse = ?";

        $stmt = $this->db->prepare($query);
        $stmt->bind_param("ssssss", $shipName, $warehouseName, $shipName, $warehouseName, $shipName, $warehouseName);
        $stmt->execute();
        $result = $stmt->get_result();

        $quotas = [];
        $totalTonnage = 0;
        $totalRemainingTonnage = 0;
        $totalLoadedTonnage = 0;
        $totalVoucherCount = 0;
        $allExitDates = [];
        $uniqueCargoTypes = [];
        $uniqueShippingCompanies = [];
        $uniqueCargoOwners = [];
        $activeQuotasCount = 0;

        while ($row = $result->fetch_assoc()) {
            $quotaNumber = $row['loadingQuotaNumber'];
            $cargoType = $row['cargoType'];
            $shippingCompany = $row['shippingCompany'];
            $cargoOwner = $row['cargoOwner'] ?? 'نامشخص';
            $isActive = (bool)$row['isActive'];

            if (!in_array($cargoType, $uniqueCargoTypes)) {
                $uniqueCargoTypes[] = $cargoType;
            }
            if (!in_array($shippingCompany, $uniqueShippingCompanies)) {
                $uniqueShippingCompanies[] = $shippingCompany;
            }
            if (!in_array($cargoOwner, $uniqueCargoOwners) && $cargoOwner !== 'نامشخص') {
                $uniqueCargoOwners[] = $cargoOwner;
            }

            if ($isActive) {
                $activeQuotasCount++;
            }

            $quotaTotalTonnage = floatval($row['totalTonnage']);
            $quotaLoadedTonnage = floatval($row['loadedTonnage']);
            $quotaRemainingTonnage = $quotaTotalTonnage - $quotaLoadedTonnage;
            $percentageLoaded = ($quotaTotalTonnage > 0) ? ($quotaLoadedTonnage / $quotaTotalTonnage) * 100 : 0;

            $exitQuery = "SELECT DISTINCT exitDate, exitTime FROM CargoInfo c WHERE c.loadingQuotaNumber = ? 
            AND c.shipName = ? AND c.loadingWarehouse = ? AND c.status = 'خروج' ORDER BY exitDate, exitTime";
            $exitStmt = $this->db->prepare($exitQuery);
            $exitStmt->bind_param("sss", $quotaNumber, $shipName, $warehouseName);
            $exitStmt->execute();
            $exitResult = $exitStmt->get_result();

            $exitDates = [];
            while ($exitRow = $exitResult->fetch_assoc()) {
                $exitDates[] = [
                    'date' => $exitRow['exitDate'],
                    'time' => $exitRow['exitTime']
                ];
                $allExitDates[] = $exitRow['exitDate'];
            }

            $quotas[] = [
                'number' => $quotaNumber,
                'cargoType' => $cargoType,
                'shippingCompany' => $shippingCompany,
                'cargoOwner' => $cargoOwner,
                'isActive' => $isActive,
                'totalTonnage' => $quotaTotalTonnage,
                'remainingTonnage' => $quotaRemainingTonnage,
                'loadedTonnage' => $quotaLoadedTonnage,
                'percentageLoaded' => round($percentageLoaded, 2),
                'voucherCount' => intval($row['voucherCount']),
                'exitDates' => $exitDates
            ];

            $totalTonnage += $quotaTotalTonnage;
            $totalRemainingTonnage += $quotaRemainingTonnage;
            $totalLoadedTonnage += $quotaLoadedTonnage;
            $totalVoucherCount += intval($row['voucherCount']);
        }

        usort($quotas, function($a, $b) {
            if ($a['isActive'] !== $b['isActive']) {
                return $b['isActive'] <=> $a['isActive'];
            }
            return $b['remainingTonnage'] <=> $a['remainingTonnage'];
        });

        $totalPercentageLoaded = ($totalTonnage > 0) ? ($totalLoadedTonnage / $totalTonnage) * 100 : 0;
        $allExitDates = array_unique($allExitDates);
        sort($allExitDates);

        return [
            'name' => $warehouseName,
            'shipName' => $shipName,
            'quotaCount' => count($quotas),
            'activeQuotasCount' => $activeQuotasCount,
            'cargoTypesCount' => count($uniqueCargoTypes),
            'shippingCompaniesCount' => count($uniqueShippingCompanies),
            'cargoOwnersCount' => count($uniqueCargoOwners),
            'totalTonnage' => $totalTonnage,
            'remainingTonnage' => $totalRemainingTonnage,
            'loadedTonnage' => $totalLoadedTonnage,
            'percentageLoaded' => number_format($totalPercentageLoaded, 2, '.', ''),
            'totalVoucherCount' => $totalVoucherCount,
            'quotas' => $quotas,
            'availableExitDates' => $allExitDates,
            'cargoTypes' => $uniqueCargoTypes,
            'shippingCompanies' => $uniqueShippingCompanies,
            'lastUpdated' => date('Y-m-d H:i:s')
        ];
    }

    public function getFilteredSummary(string $shipName, string $warehouseName, string $selectedQuota, string $startDateTime, string $endDateTime): string {
        if (empty($selectedQuota) || $selectedQuota === "null") {
            throw new Exception("هیچ کوتاژی انتخاب نشده است");
        }

        if (strlen($startDateTime) === 16) {
            $startDateTime .= ':00';
        }
        if (strlen($endDateTime) === 16) {
            $endDateTime .= ':00';
        }

        $summaryQuery = "SELECT COALESCE(SUM(c.netWeight), 0) as totalNetWeight, COUNT(DISTINCT c.trackingNumber) as voucherCount,
            MIN(c.exitTime) as firstExitTime, MAX(c.exitTime) as lastExitTime, MIN(c.exitDate) as firstExitDate, MAX(c.exitDate) as lastExitDate
        FROM CargoInfo c
        WHERE c.loadingQuotaNumber = ? AND c.shipName = ? AND c.loadingWarehouse = ? AND c.status = 'خروج'
            AND CONCAT(c.exitDate, ' ', c.exitTime) >= ? AND CONCAT(c.exitDate, ' ', c.exitTime) < ?";

        $stmt = $this->db->prepare($summaryQuery);
        $stmt->bind_param("sssss", $selectedQuota, $shipName, $warehouseName, $startDateTime, $endDateTime);
        $stmt->execute();
        $summaryResult = $stmt->get_result();
        $summary = $summaryResult->fetch_assoc();

        $detailsQuery = "SELECT c.trackingNumber, c.entryTime, c.netWeight, c.exitTime, c.exitDate, c.scaleReceiptNumber,
            c.username, c.confirm_username, c.cargoType, c.shippingCompany, i.cargoOwner
        FROM CargoInfo c
        JOIN InitialInfo i ON c.loadingQuotaNumber = i.loadingQuotaNumber AND c.shipName = i.shipName
        WHERE c.loadingQuotaNumber = ? AND c.shipName = ? AND c.loadingWarehouse = ? AND c.status = 'خروج'
            AND CONCAT(c.exitDate, ' ', c.exitTime) >= ? AND CONCAT(c.exitDate, ' ', c.exitTime) < ?
        ORDER BY c.exitDate, c.exitTime";

        $stmtDetails = $this->db->prepare($detailsQuery);
        $stmtDetails->bind_param("sssss", $selectedQuota, $shipName, $warehouseName, $startDateTime, $endDateTime);
        $stmtDetails->execute();
        $detailsResult = $stmtDetails->get_result();

        $voucherDetails = [];
        $totalWeights = [];
        $uniqueUsers = [];

        while ($row = $detailsResult->fetch_assoc()) {
            if (!isset($totalWeights[$row['cargoType']])) {
                $totalWeights[$row['cargoType']] = 0;
            }
            $totalWeights[$row['cargoType']] += floatval($row['netWeight']);

            if (!empty($row['username']) && !in_array($row['username'], $uniqueUsers)) {
                $uniqueUsers[] = $row['username'];
            }
            if (!empty($row['confirm_username']) && !in_array($row['confirm_username'], $uniqueUsers)) {
                $uniqueUsers[] = $row['confirm_username'];
            }

            $voucherDetails[] = [
                'trackingNumber' => $row['trackingNumber'],
                'entryTime' => $row['entryTime'],
                'netWeight' => floatval($row['netWeight']),
                'exitTime' => $row['exitTime'],
                'exitDate' => $row['exitDate'],
                'scaleReceiptNumber' => $row['scaleReceiptNumber'],
                'username' => $row['username'],
                'confirmUsername' => $row['confirm_username'],
                'cargoType' => $row['cargoType'],
                'shippingCompany' => $row['shippingCompany'],
                'cargoOwner' => $row['cargoOwner'] ?? 'نامشخص'
            ];
        }

        $response = [
            'totalNetWeight' => floatval($summary['totalNetWeight']),
            'voucherCount' => intval($summary['voucherCount']),
            'voucherDetails' => $voucherDetails,
            'statistics' => [
                'cargoTypeWeights' => $totalWeights,
                'operatorCount' => count($uniqueUsers),
                'firstOperation' => [
                    'date' => $summary['firstExitDate'] ?? '',
                    'time' => $summary['firstExitTime'] ?? ''
                ],
                'lastOperation' => [
                    'date' => $summary['lastExitDate'] ?? '',
                    'time' => $summary['lastExitTime'] ?? ''
                ]
            ],
            'quotaNumber' => $selectedQuota,
            'shipName' => $shipName,
            'warehouseName' => $warehouseName,
            'startDateTime' => $startDateTime,
            'endDateTime' => $endDateTime,
            'generatedAt' => date('Y-m-d H:i:s')
        ];

        return json_encode($response, JSON_UNESCAPED_UNICODE);
    }

    public function getQuotaDetails(string $quotaNumber): ?array {
        $quotaNumber = $this->sanitizeInput($quotaNumber);

        $query = "SELECT 
            i.loadingQuotaNumber, i.shipName, i.loadingWarehouse, i.shippingCompany, i.cargoType, i.cargoOwner,
            i.cargoWeight as totalTonnage, i.isActive, i.percentage, i.is_enabled,
            COALESCE(exit_data.loadedTonnage, 0) as loadedTonnage, COALESCE(exit_data.exitVoucherCount, 0) as exitVoucherCount,
            COALESCE(exit_data.startDate, '') as startDate, COALESCE(exit_data.endDate, '') as endDate
        FROM InitialInfo i
        LEFT JOIN (
            SELECT c.loadingQuotaNumber, c.shipName, c.loadingWarehouse, c.shippingCompany, c.cargoType,
                SUM(c.netWeight) as loadedTonnage, COUNT(DISTINCT c.trackingNumber) as exitVoucherCount,
                MIN(c.exitDate) as startDate, MAX(c.exitDate) as endDate
            FROM CargoInfo c WHERE c.status = 'خروج'
            GROUP BY c.loadingQuotaNumber, c.shipName, c.loadingWarehouse, c.shippingCompany, c.cargoType
        ) exit_data ON 
            exit_data.loadingQuotaNumber = i.loadingQuotaNumber AND exit_data.shipName = i.shipName
            AND exit_data.loadingWarehouse = i.loadingWarehouse AND exit_data.shippingCompany = i.shippingCompany
            AND exit_data.cargoType = i.cargoType
        WHERE i.loadingQuotaNumber = ?";

        $stmt = $this->db->prepare($query);
        $stmt->bind_param("s", $quotaNumber);
        $stmt->execute();
        $result = $stmt->get_result();

        if ($row = $result->fetch_assoc()) {
            $totalTonnage = floatval($row['totalTonnage']);
            $loadedTonnage = floatval($row['loadedTonnage']);
            $remainingTonnage = $totalTonnage - $loadedTonnage;
            $percentageLoaded = ($totalTonnage > 0) ? ($loadedTonnage / $totalTonnage) * 100 : 0;
            $isActive = (bool)$row['isActive'];
            $isPercentageRestricted = (bool)$row['is_enabled'];
            $percentage = $row['percentage'] !== null ? floatval($row['percentage']) : null;

            $loadableTonnage = $this->calculateLoadableTonnage($remainingTonnage, $totalTonnage, $percentage, $isPercentageRestricted);
            $exitVoucherCount = intval($row['exitVoucherCount']);
            $avgVoucherWeight = ($exitVoucherCount > 0) ? ($loadedTonnage / $exitVoucherCount) : 0;

            return [
                'number' => $row['loadingQuotaNumber'],
                'shipName' => $row['shipName'],
                'warehouseName' => $row['loadingWarehouse'],
                'shippingCompany' => $row['shippingCompany'],
                'cargoType' => $row['cargoType'],
                'cargoOwner' => $row['cargoOwner'] ?? 'نامشخص',
                'totalTonnage' => $totalTonnage,
                'remainingTonnage' => $remainingTonnage,
                'loadedTonnage' => $loadedTonnage,
                'percentageLoaded' => round($percentageLoaded, 2),
                'loadableTonnage' => $loadableTonnage,
                'isActive' => $isActive,
                'voucherCount' => $exitVoucherCount,
                'exitVoucherCount' => $exitVoucherCount,
                'startDate' => $row['startDate'],
                'endDate' => $row['endDate'],
                'avgVoucherWeight' => round($avgVoucherWeight, 2),
                'isPercentageRestricted' => $isPercentageRestricted,
                'percentage' => $percentage,
                'lastUpdated' => date('Y-m-d H:i:s')
            ];
        }
        return null;
    }

    public function getFilteredQuotas(string $shipName, string $startDateTime, string $endDateTime): array {
        $shipName = $this->sanitizeInput($shipName);
        $startDateTime = $this->sanitizeInput($startDateTime);
        $endDateTime = $this->sanitizeInput($endDateTime);

        if (strlen($startDateTime) === 16) {
            $startDateTime .= ':00';
        }
        if (strlen($endDateTime) === 16) {
            $endDateTime .= ':00';
        }

        $query = "SELECT i.id, i.loadingQuotaNumber as number, i.shipName, i.loadingWarehouse, i.cargoType,
            i.cargoWeight as totalTonnage, i.isActive, i.shippingCompany, i.cargoOwner, i.percentage, i.is_enabled,
            COALESCE(exit_data.loadedTonnage, 0) as loadedTonnage, COALESCE(all_vouchers.voucherCount, 0) as voucherCount,
            COALESCE(exit_data.exitVoucherCount, 0) as exitVoucherCount, COALESCE(exit_data.lastExitDate, '') as lastExitDate
        FROM InitialInfo i
        LEFT JOIN (
            SELECT c.loadingQuotaNumber, c.shipName, c.loadingWarehouse, c.shippingCompany, c.cargoType,
                SUM(c.netWeight) as loadedTonnage, COUNT(DISTINCT c.trackingNumber) as exitVoucherCount, MAX(c.exitDate) as lastExitDate
            FROM CargoInfo c WHERE c.status = 'خروج' AND c.shipName = ?
                AND ((c.exitDate > ? OR (c.exitDate = ? AND c.exitTime >= ?)) AND (c.exitDate < ? OR (c.exitDate = ? AND c.exitTime <= ?)))
            GROUP BY c.loadingQuotaNumber, c.shipName, c.loadingWarehouse, c.shippingCompany, c.cargoType
        ) exit_data ON 
            exit_data.loadingQuotaNumber = i.loadingQuotaNumber AND exit_data.shipName = i.shipName
            AND exit_data.loadingWarehouse = i.loadingWarehouse AND exit_data.shippingCompany = i.shippingCompany
            AND exit_data.cargoType = i.cargoType
        LEFT JOIN (
            SELECT c.loadingQuotaNumber, c.shipName, c.loadingWarehouse, c.shippingCompany, c.cargoType, COUNT(DISTINCT c.trackingNumber) as voucherCount
            FROM CargoInfo c WHERE c.status = 'خروج' AND c.shipName = ?
                AND ((c.exitDate > ? OR (c.exitDate = ? AND c.exitTime >= ?)) AND (c.exitDate < ? OR (c.exitDate = ? AND c.exitTime <= ?)))
            GROUP BY c.loadingQuotaNumber, c.shipName, c.loadingWarehouse, c.shippingCompany, c.cargoType
        ) all_vouchers ON 
            all_vouchers.loadingQuotaNumber = i.loadingQuotaNumber AND all_vouchers.shipName = i.shipName
            AND all_vouchers.loadingWarehouse = i.loadingWarehouse AND all_vouchers.shippingCompany = i.shippingCompany
            AND all_vouchers.cargoType = i.cargoType
        WHERE i.shipName = ?
        ORDER BY i.isActive DESC, i.loadingQuotaNumber ASC";

        $startDate = substr($startDateTime, 0, 10);
        $startTime = substr($startDateTime, 11, 8);
        $endDate = substr($endDateTime, 0, 10);
        $endTime = substr($endDateTime, 11, 8);

        $stmt = $this->db->prepare($query);
        $stmt->bind_param("sssssssssssssss", 
            $shipName, $startDate, $startDate, $startTime, $endDate, $endDate, $endTime,
            $shipName, $startDate, $startDate, $startTime, $endDate, $endDate, $endTime,
            $shipName
        );
        $stmt->execute();
        $result = $stmt->get_result();
        $quotas = [];

        while ($row = $result->fetch_assoc()) {
            $loadedTonnage = floatval($row['loadedTonnage']);
            $totalTonnage = floatval($row['totalTonnage']);
            $remainingTonnage = $totalTonnage - $loadedTonnage;
            $percentage = $row['percentage'] !== null ? floatval($row['percentage']) : null;
            $isPercentageRestricted = (bool)$row['is_enabled'];
            $percentageLoaded = ($totalTonnage > 0) ? ($loadedTonnage / $totalTonnage) * 100 : 0;

            $loadableTonnage = $this->calculateLoadableTonnage($remainingTonnage, $totalTonnage, $percentage, $isPercentageRestricted);
            $exitVoucherCount = intval($row['exitVoucherCount']);
            $avgVoucherWeight = ($exitVoucherCount > 0) ? ($loadedTonnage / $exitVoucherCount) : 0;

            $quotas[] = [
                'id' => (int)$row['id'],
                'number' => $row['number'],
                'shipName' => $row['shipName'] ?? '',
                'warehouse' => $row['loadingWarehouse'] ?? '',
                'cargoType' => $row['cargoType'] ?? '',
                'totalTonnage' => $totalTonnage,
                'remainingTonnage' => $remainingTonnage,
                'loadedTonnage' => $loadedTonnage,
                'percentageLoaded' => round($percentageLoaded, 2),
                'voucherCount' => intval($row['voucherCount']),
                'entryVoucherCount' => 0,
                'exitVoucherCount' => $exitVoucherCount,
                'pendingVoucherCount' => 0,
                'isActive' => (bool)$row['isActive'],
                'shippingCompany' => $row['shippingCompany'] ?? '',
                'cargoOwner' => $row['cargoOwner'] ?? '',
                'percentage' => $percentage,
                'isPercentageRestricted' => $isPercentageRestricted,
                'loadableTonnage' => $loadableTonnage,
                'avgVoucherWeight' => round($avgVoucherWeight, 2),
                'lastExitDate' => $row['lastExitDate'],
                'quotaKey' => $row['number'] . '|' . $row['shipName'] . '|' . $row['loadingWarehouse'] . '|' . $row['shippingCompany'] . '|' . $row['cargoType']
            ];
        }
        return $quotas;
    }

    public function getAllQuotasList(): array {
        $query = "SELECT id, loadingQuotaNumber as number, shipName, loadingWarehouse, cargoType, cargoWeight as totalTonnage,
            isActive, shippingCompany, cargoOwner, percentage, is_enabled, temp_tonnage_status, temp_tonnage_amount
        FROM InitialInfo ORDER BY shipName ASC, isActive DESC, loadingQuotaNumber ASC";

        $stmt = $this->db->prepare($query);
        $stmt->execute();
        $result = $stmt->get_result();
        $quotas = [];

        while ($row = $result->fetch_assoc()) {
            $totalTonnage = floatval($row['totalTonnage']);
            $percentage = $row['percentage'] !== null ? floatval($row['percentage']) : null;
            $isPercentageRestricted = (bool)$row['is_enabled'];

            $quotas[] = [
                'id' => (int)$row['id'],
                'number' => $row['number'],
                'shipName' => $row['shipName'] ?? '',
                'warehouse' => $row['loadingWarehouse'] ?? '',
                'cargoType' => $row['cargoType'] ?? '',
                'totalTonnage' => $totalTonnage,
                'remainingTonnage' => $totalTonnage,
                'loadedTonnage' => 0,
                'percentageLoaded' => 0,
                'voucherCount' => 0,
                'entryVoucherCount' => 0,
                'exitVoucherCount' => 0,
                'pendingVoucherCount' => 0,
                'isActive' => (bool)$row['isActive'],
                'shippingCompany' => $row['shippingCompany'] ?? '',
                'cargoOwner' => $row['cargoOwner'] ?? '',
                'percentage' => $percentage,
                'isPercentageRestricted' => $isPercentageRestricted,
                'loadableTonnage' => $totalTonnage,
                'avgVoucherWeight' => 0,
                'lastExitDate' => '',
                'temporaryTonnageEnabled' => (bool)($row['temp_tonnage_status'] ?? false),
                'temporaryTonnageValue' => $row['temp_tonnage_amount'] ? floatval($row['temp_tonnage_amount']) : null,
                'quotaKey' => $row['number'] . '|' . $row['shipName'] . '|' . $row['loadingWarehouse'] . '|' . $row['shippingCompany'] . '|' . $row['cargoType']
            ];
        }
        return $quotas;
    }

    public function getQuotasList(string $shipName): array {
        $shipName = $this->sanitizeInput($shipName);

        $query = "SELECT i.id, i.loadingQuotaNumber as number, i.shipName, i.loadingWarehouse, i.cargoType, i.cargoWeight as totalTonnage, i.isActive, i.shippingCompany, i.cargoOwner, i.percentage, i.is_enabled,
            COALESCE(exit_data.loadedTonnage, 0) as loadedTonnage, COALESCE(all_vouchers.voucherCount, 0) as voucherCount, COALESCE(exit_data.exitVoucherCount, 0) as exitVoucherCount, COALESCE(exit_data.lastExitDate, '') as lastExitDate
        FROM InitialInfo i
        LEFT JOIN (
            SELECT c.loadingQuotaNumber, c.shipName, c.loadingWarehouse, c.shippingCompany, c.cargoType, SUM(c.netWeight) as loadedTonnage, COUNT(DISTINCT c.trackingNumber) as exitVoucherCount, MAX(c.exitDate) as lastExitDate
            FROM CargoInfo c WHERE c.status = 'خروج' AND c.shipName = ?
            GROUP BY c.loadingQuotaNumber, c.shipName, c.loadingWarehouse, c.shippingCompany, c.cargoType
        ) exit_data ON 
            exit_data.loadingQuotaNumber = i.loadingQuotaNumber AND exit_data.shipName = i.shipName
            AND exit_data.loadingWarehouse = i.loadingWarehouse AND exit_data.shippingCompany = i.shippingCompany
            AND exit_data.cargoType = i.cargoType
        LEFT JOIN (
            SELECT c.loadingQuotaNumber, c.shipName, c.loadingWarehouse, c.shippingCompany, c.cargoType, COUNT(DISTINCT c.trackingNumber) as voucherCount
            FROM CargoInfo c WHERE c.status = 'خروج' AND c.shipName = ?
            GROUP BY c.loadingQuotaNumber, c.shipName, c.loadingWarehouse, c.shippingCompany, c.cargoType
        ) all_vouchers ON 
            all_vouchers.loadingQuotaNumber = i.loadingQuotaNumber AND all_vouchers.shipName = i.shipName
            AND all_vouchers.loadingWarehouse = i.loadingWarehouse AND all_vouchers.shippingCompany = i.shippingCompany
            AND all_vouchers.cargoType = i.cargoType
        WHERE i.shipName = ?
        ORDER BY i.isActive DESC, i.loadingQuotaNumber ASC";

        $stmt = $this->db->prepare($query);
        $stmt->bind_param("sss", $shipName, $shipName, $shipName);
        $stmt->execute();
        $result = $stmt->get_result();
        $quotas = [];

        while ($row = $result->fetch_assoc()) {
            $loadedTonnage = floatval($row['loadedTonnage']);
            $totalTonnage = floatval($row['totalTonnage']);
            $remainingTonnage = $totalTonnage - $loadedTonnage;
            $percentage = $row['percentage'] !== null ? floatval($row['percentage']) : null;
            $isPercentageRestricted = (bool)$row['is_enabled'];
            $percentageLoaded = ($totalTonnage > 0) ? ($loadedTonnage / $totalTonnage) * 100 : 0;

            $loadableTonnage = $this->calculateLoadableTonnage($remainingTonnage, $totalTonnage, $percentage, $isPercentageRestricted);
            $exitVoucherCount = intval($row['exitVoucherCount']);
            $avgVoucherWeight = ($exitVoucherCount > 0) ? ($loadedTonnage / $exitVoucherCount) : 0;

            $quotas[] = [
                'id' => (int)$row['id'],
                'number' => $row['number'],
                'shipName' => $row['shipName'] ?? '',
                'warehouse' => $row['loadingWarehouse'] ?? '',
                'cargoType' => $row['cargoType'] ?? '',
                'totalTonnage' => $totalTonnage,
                'remainingTonnage' => $remainingTonnage,
                'loadedTonnage' => $loadedTonnage,
                'percentageLoaded' => round($percentageLoaded, 2),
                'voucherCount' => intval($row['voucherCount']),
                'entryVoucherCount' => 0,
                'exitVoucherCount' => $exitVoucherCount,
                'pendingVoucherCount' => 0,
                'isActive' => (bool)$row['isActive'],
                'shippingCompany' => $row['shippingCompany'] ?? '',
                'cargoOwner' => $row['cargoOwner'] ?? '',
                'percentage' => $percentage,
                'isPercentageRestricted' => $isPercentageRestricted,
                'loadableTonnage' => $loadableTonnage,
                'avgVoucherWeight' => round($avgVoucherWeight, 2),
                'lastExitDate' => $row['lastExitDate'],
                'quotaKey' => $row['number'] . '|' . $row['shipName'] . '|' . $row['loadingWarehouse'] . '|' . $row['shippingCompany'] . '|' . $row['cargoType']
            ];
        }
        return $quotas;
    }

    public function getLoadableTonnage(string $quotaNumber, string $shippingCompany = '', string $warehouse = '', string $cargoType = ''): array {
        $quotaNumber = $this->sanitizeInput($quotaNumber);
        $shippingCompany = $this->sanitizeInput($shippingCompany);
        $warehouse = $this->sanitizeInput($warehouse);
        $cargoType = $this->sanitizeInput($cargoType);

        $whereConditions = ["i.loadingQuotaNumber = ?"];
        $params = [$quotaNumber];
        $types = "s";

        if (!empty($shippingCompany)) {
            $whereConditions[] = "i.shippingCompany = ?";
            $params[] = $shippingCompany;
            $types .= "s";
        }
        if (!empty($warehouse)) {
            $whereConditions[] = "i.loadingWarehouse = ?";
            $params[] = $warehouse;
            $types .= "s";
        }
        if (!empty($cargoType)) {
            $whereConditions[] = "i.cargoType = ?";
            $params[] = $cargoType;
            $types .= "s";
        }

        $query = "SELECT i.loadingQuotaNumber as number, i.cargoWeight as totalTonnage, i.percentage, i.is_enabled, COALESCE(exit_data.loadedTonnage, 0) as loadedTonnage
        FROM InitialInfo i
        LEFT JOIN (
            SELECT c.loadingQuotaNumber, c.shipName, c.loadingWarehouse, c.shippingCompany, c.cargoType, SUM(c.netWeight) as loadedTonnage
            FROM CargoInfo c WHERE c.status = 'خروج'
            GROUP BY c.loadingQuotaNumber, c.shipName, c.loadingWarehouse, c.shippingCompany, c.cargoType
        ) exit_data ON 
            exit_data.loadingQuotaNumber = i.loadingQuotaNumber AND exit_data.shipName = i.shipName
            AND exit_data.loadingWarehouse = i.loadingWarehouse AND exit_data.shippingCompany = i.shippingCompany
            AND exit_data.cargoType = i.cargoType
        WHERE " . implode(" AND ", $whereConditions) . " LIMIT 1";

        $stmt = $this->db->prepare($query);
        if (count($params) > 0) {
            $stmt->bind_param($types, ...$params);
        }
        $stmt->execute();
        $result = $stmt->get_result();

        if ($row = $result->fetch_assoc()) {
            $loadedTonnage = floatval($row['loadedTonnage']);
            $totalTonnage = floatval($row['totalTonnage']);
            $remainingTonnage = $totalTonnage - $loadedTonnage;
            $percentage = $row['percentage'] !== null ? floatval($row['percentage']) : null;
            $isPercentageRestricted = (bool)$row['is_enabled'];

            $loadableTonnage = $this->calculateLoadableTonnage($remainingTonnage, $totalTonnage, $percentage, $isPercentageRestricted);
            $trucks18Wheeler = $loadableTonnage > 0 ? floor($loadableTonnage / 25000) : 0;
            $trucks10Wheeler = $loadableTonnage > 0 ? floor($loadableTonnage / 15000) : 0;

            return [
                'success' => true,
                'loadableTonnage' => $loadableTonnage,
                'remainingTonnage' => $remainingTonnage,
                'totalTonnage' => $totalTonnage,
                'loadedTonnage' => $loadedTonnage,
                'percentage' => $percentage,
                'isPercentageRestricted' => $isPercentageRestricted,
                'trucks18Wheeler' => $trucks18Wheeler,
                'trucks10Wheeler' => $trucks10Wheeler
            ];
        }

        return [
            'success' => false,
            'message' => 'کوتاژ مورد نظر با مشخصات وارد شده یافت نشد'
        ];
    }

    public function editQuota(int $id, string $oldQuotaNumber, string $newQuotaNumber, string $shipName, string $shippingCompany, string $warehouse, string $cargoType, float $totalTonnage): bool {
        try {
            $this->db->beginTransaction();

            $selectQuery = "SELECT loadingQuotaNumber, shipName, loadingWarehouse, shippingCompany, cargoType FROM InitialInfo WHERE id = ?";
            $selectStmt = $this->db->prepare($selectQuery);
            $selectStmt->bind_param("i", $id);
            $selectStmt->execute();
            $result = $selectStmt->get_result();
            $oldData = $result->fetch_assoc();

            if (!$oldData) {
                throw new Exception("کوتاژ با شناسه مشخص شده یافت نشد");
            }

            $queryInitialInfo = "UPDATE InitialInfo SET loadingQuotaNumber = ?, shipName = ?, shippingCompany = ?, loadingWarehouse = ?, cargoType = ?, cargoWeight = ? WHERE id = ?";
            $stmtInitialInfo = $this->db->prepare($queryInitialInfo);
            $stmtInitialInfo->bind_param("sssssdi", $newQuotaNumber, $shipName, $shippingCompany, $warehouse, $cargoType, $totalTonnage, $id);
            $stmtInitialInfo->execute();

            $queryCargoInfo = "UPDATE CargoInfo SET loadingQuotaNumber = ?, shipName = ?, loadingWarehouse = ?, shippingCompany = ?, cargoType = ?
                WHERE loadingQuotaNumber = ? AND shipName = ? AND loadingWarehouse = ? AND shippingCompany = ? AND cargoType = ?";
            $stmtCargoInfo = $this->db->prepare($queryCargoInfo);
            $stmtCargoInfo->bind_param("ssssssssss", 
                $newQuotaNumber, $shipName, $warehouse, $shippingCompany, $cargoType,
                $oldData['loadingQuotaNumber'], $oldData['shipName'], $oldData['loadingWarehouse'], $oldData['shippingCompany'], $oldData['cargoType']
            );
            $stmtCargoInfo->execute();

            $this->db->commit();
            return true;
        } catch (Exception $e) {
            $this->db->rollback();
            throw new Exception("خطا در ویرایش کوتاژ: " . $e->getMessage());
        }
    }

    public function updateQuotaPercentage(string $quotaNumber, float $percentage): bool {
        $isEnabled = ($percentage > 0.00) ? 1 : 0;
        $query = "UPDATE InitialInfo SET percentage = ?, is_enabled = ? WHERE loadingQuotaNumber = ?";
        $stmt = $this->db->prepare($query);
        $stmt->bind_param("dis", $percentage, $isEnabled, $quotaNumber);
        return $stmt->execute();
    }

    public function toggleQuotaStatus(string $quotaNumber, int $id = 0): bool {
        if ($id > 0) {
            $query = "UPDATE InitialInfo SET isActive = NOT isActive WHERE id = ?";
            $stmt = $this->db->prepare($query);
            $stmt->bind_param("i", $id);
        } else {
            $query = "UPDATE InitialInfo SET isActive = NOT isActive WHERE loadingQuotaNumber = ?";
            $stmt = $this->db->prepare($query);
            $stmt->bind_param("s", $quotaNumber);
        }
        return $stmt->execute();
    }

    public function updateQuotaPercentageRestriction(string $quotaNumber, int $isEnabled): bool {
        $query = "UPDATE InitialInfo SET is_enabled = ? WHERE loadingQuotaNumber = ?";
        $stmt = $this->db->prepare($query);
        $stmt->bind_param("is", $isEnabled, $quotaNumber);
        return $stmt->execute();
    }

    public function deleteQuota(string $quotaNumber, string $shipName, string $warehouse, string $shippingCompany, string $cargoType): bool {
        try {
            $quotaNumber = $this->sanitizeInput($quotaNumber);
            $shipName = $this->sanitizeInput($shipName);
            $warehouse = $this->sanitizeInput($warehouse);
            $shippingCompany = $this->sanitizeInput($shippingCompany);
            $cargoType = $this->sanitizeInput($cargoType);

            $this->db->beginTransaction();

            $checkQuery = "SELECT COUNT(*) as count FROM InitialInfo WHERE loadingQuotaNumber = ? AND shipName = ? AND loadingWarehouse = ? AND shippingCompany = ? AND cargoType = ?";
            $checkStmt = $this->db->prepare($checkQuery);
            $checkStmt->bind_param("sssss", $quotaNumber, $shipName, $warehouse, $shippingCompany, $cargoType);
            $checkStmt->execute();
            $checkResult = $checkStmt->get_result();
            $row = $checkResult->fetch_assoc();

            if ($row['count'] == 0) {
                $this->db->rollback();
                return false;
            }

            $cargoQuery = "DELETE FROM CargoInfo WHERE loadingQuotaNumber = ? AND shipName = ? AND loadingWarehouse = ? AND shippingCompany = ?";
            $cargoStmt = $this->db->prepare($cargoQuery);
            $cargoStmt->bind_param("ssss", $quotaNumber, $shipName, $warehouse, $shippingCompany);
            $cargoStmt->execute();

            $initialQuery = "DELETE FROM InitialInfo WHERE loadingQuotaNumber = ? AND shipName = ? AND loadingWarehouse = ? AND shippingCompany = ? AND cargoType = ?";
            $initialStmt = $this->db->prepare($initialQuery);
            $initialStmt->bind_param("sssss", $quotaNumber, $shipName, $warehouse, $shippingCompany, $cargoType);
            $initialStmt->execute();

            $this->db->commit();
            return true;
        } catch (Exception $e) {
            $this->db->rollback();
            throw new Exception("خطا در حذف کوتاژ: " . $e->getMessage());
        }
    }

    public function getGroupedQuotas(): array {
        $quotas = $this->getAllQuotasList();
        $grouped = [];
        foreach ($quotas as $quota) {
            $shipName = $quota['shipName'] ?: 'نامشخص';
            $cargoOwner = $quota['cargoOwner'] ?: 'نامشخص';

            if (!isset($grouped[$shipName])) {
                $grouped[$shipName] = [];
            }
            if (!isset($grouped[$shipName][$cargoOwner])) {
                $grouped[$shipName][$cargoOwner] = [];
            }
            $grouped[$shipName][$cargoOwner][] = $quota;
        }
        return $grouped;
    }

    public function updateTemporaryTonnage(string $quotaNumber, int $enabledVal, ?float $tonnageVal): array {
        $quotaNumber = $this->sanitizeInput($quotaNumber);

        $checkQuery = "SELECT loadingQuotaNumber FROM InitialInfo WHERE loadingQuotaNumber = ?";
        $checkStmt = $this->db->prepare($checkQuery);
        $checkStmt->bind_param("s", $quotaNumber);
        $checkStmt->execute();
        if ($checkStmt->get_result()->num_rows === 0) {
            throw new Exception('کوتاژ مورد نظر یافت نشد');
        }

        if ($enabledVal && $tonnageVal !== null) {
            $query = "UPDATE InitialInfo SET temp_tonnage_status = 1, temp_tonnage_amount = ? WHERE loadingQuotaNumber = ?";
            $stmt = $this->db->prepare($query);
            $stmt->bind_param("ds", $tonnageVal, $quotaNumber);
        } else {
            $query = "UPDATE InitialInfo SET temp_tonnage_status = 0, temp_tonnage_amount = NULL WHERE loadingQuotaNumber = ?";
            $stmt = $this->db->prepare($query);
            $stmt->bind_param("s", $quotaNumber);
        }

        if ($stmt->execute()) {
            return ['success' => true, 'message' => 'تناژ موقت با موفقیت به‌روزرسانی شد'];
        }
        throw new Exception('خطا در اجرای کوئری');
    }
}
