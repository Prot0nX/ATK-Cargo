<?php
// PHP/src/Controllers/AnalyticsController.php

declare(strict_types=1);

namespace App\Controllers;

use Exception;
use InvalidArgumentException;
use mysqli;
use App\Core\Database;
use App\Core\Logger;

class AnalyticsController {
    private mysqli $conn;
    private Logger $logger;

    public function __construct() {
        $this->conn = Database::getInstance()->getMysqliConnection();
        $this->logger = Logger::getInstance();
    }

    /**
     * مدیریت درخواست‌های realTimeLoadingData.php
     */
    public function handleRealTimeLoadingData(): void {
        header('Content-Type: application/json; charset=UTF-8');
        date_default_timezone_set('Asia/Tehran');

        if ($_SERVER['REQUEST_METHOD'] !== 'GET') {
            $this->sendJsonResponse(['error' => 'فقط متد GET مجاز است.'], 400);
        }

        try {
            $action = $_GET['action'] ?? '';

            switch ($action) {
                case 'getKotazhInfo':
                    $this->handleKotazhRequest();
                    break;
                case 'getRealTimeData':
                    $this->handleRealTimeDataRequest();
                    break;
                case 'getComprehensiveAnalysis':
                    $this->handleComprehensiveAnalysisRequest();
                    break;
                default:
                    $this->sendJsonResponse(['error' => 'عملیات نامعتبر است.'], 400);
            }
        } catch (InvalidArgumentException $e) {
            $this->sendJsonResponse(['error' => $e->getMessage()], 400);
        } catch (Exception $e) {
            $this->logger->error("Error in handleRealTimeLoadingData: " . $e->getMessage());
            $this->sendJsonResponse(['error' => 'خطایی در سرور رخ داد.'], 500);
        }
    }

    /**
     * مدیریت درخواست‌های quota_remaining_api.php
     */
    public function handleQuotaRemaining(): void {
        header('Content-Type: application/json; charset=UTF-8');
        header('Cache-Control: max-age=60, public');
        date_default_timezone_set('Asia/Tehran');

        if (extension_loaded('zlib') && !ini_get('zlib.output_compression')) {
            if (ob_get_level() === 0) {
                ob_start('ob_gzhandler');
            }
        }

        if ($_SERVER['REQUEST_METHOD'] !== 'GET') {
            $this->sendJsonResponse(['success' => false, 'error' => 'روش درخواست نامعتبر است'], 500);
        }

        try {
            if (!isset($_GET['action'])) {
                throw new InvalidArgumentException('عملیات مشخص نشده است');
            }

            $action = $this->sanitizeInput($_GET['action']);

            switch ($action) {
                case 'getActiveQuotasRemaining':
                    $result = $this->getActiveQuotasRemaining();
                    $this->sendJsonResponse($result);
                    break;

                case 'getShipQuotasRemaining':
                    if (!isset($_GET['shipName'])) {
                        throw new InvalidArgumentException('نام کشتی مشخص نشده است');
                    }
                    $result = $this->getShipQuotasRemaining($_GET['shipName']);
                    $this->sendJsonResponse($result);
                    break;

                default:
                    throw new InvalidArgumentException('عملیات نامعتبر است');
            }
        } catch (Exception $e) {
            $this->logger->error("Error in handleQuotaRemaining: " . $e->getMessage());
            $this->sendJsonResponse([
                'success' => false,
                'error' => $e->getMessage()
            ], 500);
        }
    }

    private function handleKotazhRequest(): void {
        $kotazh = trim((string)($_GET['kotazh'] ?? ''));
        if (empty($kotazh)) {
            throw new InvalidArgumentException('کوتاژ نمی‌تواند خالی باشد.');
        }
        if (!preg_match('/^\d{8}$/', $kotazh)) {
            throw new InvalidArgumentException('فرمت کوتاژ نامعتبر است. باید شامل 8 رقم باشد.');
        }

        $stmt = $this->conn->prepare("SELECT shipName, loadingWarehouse, cargoType, shippingCompany, cargoWeight, loadingQuotaNumber FROM InitialInfo WHERE loadingQuotaNumber = ?");
        $stmt->bind_param("s", $kotazh);
        $stmt->execute();
        $kotazhInfo = $stmt->get_result()->fetch_assoc();
        $stmt->close();

        if (!$kotazhInfo) {
            $this->sendJsonResponse(['error' => 'کوتاژ مورد نظر یافت نشد.'], 404);
        }

        $stmt2 = $this->conn->prepare("SELECT trackingNumber, entryTime, netWeight, scaleReceiptNumber, shortageWeight, excessWeight, exitTime, exitDate, status FROM CargoInfo WHERE loadingQuotaNumber = ?");
        $stmt2->bind_param("s", $kotazh);
        $stmt2->execute();
        $cargoInfo = $stmt2->get_result()->fetch_all(MYSQLI_ASSOC);
        $stmt2->close();

        $this->sendJsonResponse([
            'kotazhInfo' => $kotazhInfo,
            'cargoInfo' => $cargoInfo
        ]);
    }

    private function handleRealTimeDataRequest(): void {
        $shiftOffset = isset($_GET['shiftOffset']) ? (int)$_GET['shiftOffset'] : 0;
        $targetTimestamp = time() + ($shiftOffset * 12 * 3600);
        $currentTimeString = date('H:i:s', $targetTimestamp);

        $shiftInfo = $this->determineShiftInfo($currentTimeString, $targetTimestamp);
        $realTimeData = $this->getRealTimeData($shiftInfo);

        $this->sendJsonResponse([
            'shiftInfo' => $shiftInfo,
            'data' => $realTimeData
        ]);
    }

    private function determineShiftInfo(string $currentTime, int $targetTimestamp): array {
        $currentJalaliDate = jdate('Y/m/d', $targetTimestamp);

        if ($currentTime >= '07:30:00' && $currentTime < '19:00:00') {
            return [
                'startDate' => $currentJalaliDate,
                'endDate' => $currentJalaliDate,
                'startTime' => '07:30:00',
                'endTime' => '19:00:00',
                'type' => 'روز'
            ];
        } else {
            if ($currentTime >= '00:00:00' && $currentTime < '07:30:00') {
                $prevTimestamp = $targetTimestamp - 86400;
                $shiftStartDate = jdate('Y/m/d', $prevTimestamp);
                $shiftEndDate = $currentJalaliDate;
            } else {
                $shiftStartDate = $currentJalaliDate;
                $nextTimestamp = $targetTimestamp + 86400;
                $shiftEndDate = jdate('Y/m/d', $nextTimestamp);
            }

            return [
                'startDate' => $shiftStartDate,
                'endDate' => $shiftEndDate,
                'startTime' => '19:00:00',
                'endTime' => '07:00:00',
                'type' => 'شب'
            ];
        }
    }

    private function getRealTimeData(array $shiftInfo): array {
        if ($shiftInfo['type'] === 'روز') {
            $query = "SELECT 
                i.loadingQuotaNumber, i.shipName, i.loadingWarehouse, i.shippingCompany, i.cargoType,
                COUNT(DISTINCT CASE WHEN c.status = 'ورود' THEN c.id END) AS entryVouchers,
                COUNT(DISTINCT CASE WHEN c.status = 'خروج' THEN c.id END) AS exitVouchers,
                COUNT(DISTINCT c.id) AS totalVouchers,
                SUM(CASE WHEN c.status = 'خروج' THEN c.netWeight ELSE 0 END) AS totalNetWeight
                FROM InitialInfo i
                LEFT JOIN CargoInfo c ON i.loadingQuotaNumber = c.loadingQuotaNumber 
                    AND i.loadingWarehouse = c.loadingWarehouse
                    AND i.shippingCompany = c.shippingCompany
                WHERE ((c.exitDate = ? AND c.exitTime BETWEEN ? AND ?) OR (c.status = 'ورود' AND c.exitDate IS NULL))
                GROUP BY i.loadingQuotaNumber, i.shipName, i.loadingWarehouse, i.shippingCompany, i.cargoType";

            $stmt = $this->conn->prepare($query);
            $stmt->bind_param("sss", $shiftInfo['startDate'], $shiftInfo['startTime'], $shiftInfo['endTime']);
        } else {
            $query = "SELECT 
                i.loadingQuotaNumber, i.shipName, i.loadingWarehouse, i.shippingCompany, i.cargoType,
                COUNT(DISTINCT CASE WHEN c.status = 'ورود' THEN c.id END) AS entryVouchers,
                COUNT(DISTINCT CASE WHEN c.status = 'خروج' THEN c.id END) AS exitVouchers,
                COUNT(DISTINCT c.id) AS totalVouchers,
                SUM(CASE WHEN c.status = 'خروج' THEN c.netWeight ELSE 0 END) AS totalNetWeight
                FROM InitialInfo i
                LEFT JOIN CargoInfo c ON i.loadingQuotaNumber = c.loadingQuotaNumber 
                    AND i.loadingWarehouse = c.loadingWarehouse
                    AND i.shippingCompany = c.shippingCompany
                WHERE ((c.exitDate = ? AND c.exitTime >= ?) OR (c.exitDate = ? AND c.exitTime < ?) OR (c.status = 'ورود' AND c.exitDate IS NULL))
                GROUP BY i.loadingQuotaNumber, i.shipName, i.loadingWarehouse, i.shippingCompany, i.cargoType";

            $stmt = $this->conn->prepare($query);
            $stmt->bind_param("ssss", $shiftInfo['startDate'], $shiftInfo['startTime'], $shiftInfo['endDate'], $shiftInfo['endTime']);
        }

        $stmt->execute();
        $result = $stmt->get_result()->fetch_all(MYSQLI_ASSOC);
        $stmt->close();
        return $result;
    }

    private function handleComprehensiveAnalysisRequest(): void {
        $offset = isset($_GET['offset']) ? (int)$_GET['offset'] : 0;
        
        $currentTime = time();
        if (date('H') < 7) {
            $currentTime = strtotime('-1 day');
        }
        $targetTime = $currentTime + ($offset * 86400);
        $todayJalaliDate = jdate('Y/m/d', $targetTime);
        $yesterdayJalaliDate = jdate('Y/m/d', $targetTime - 86400);

        $dateParts = explode('/', $todayJalaliDate);
        $gy = (int)$dateParts[0]; $gm = (int)$dateParts[1]; $gd = (int)$dateParts[2];
        if (function_exists('jalali_to_gregorian')) {
            $gDate = jalali_to_gregorian($gy, $gm, $gd);
            $timestamp = mktime(12, 0, 0, $gDate[1], $gDate[2], $gDate[0]);
        } else {
            $timestamp = time();
        }

        $dateInfo = [
            'jalaliDate' => $todayJalaliDate,
            'dayName' => jdate('l', $timestamp)
        ];

        $query = "SELECT 
                    c.loadingQuotaNumber, i.shipName, c.shippingCompany, i.cargoOwner, c.loadingWarehouse, i.cargoType,
                    SUM(c.netWeight) AS last_24h_weight, COUNT(*) AS last_24h_vouchers
                FROM CargoInfo c
                JOIN InitialInfo i ON c.loadingQuotaNumber = i.loadingQuotaNumber 
                    AND c.loadingWarehouse = i.loadingWarehouse
                    AND c.shippingCompany = i.shippingCompany
                WHERE c.status = 'خروج' AND ((c.exitDate = ? AND c.exitTime >= '07:00:00') OR (c.exitDate = ? AND c.exitTime < '07:00:00'))
                GROUP BY c.loadingQuotaNumber, i.shipName, c.shippingCompany, i.cargoOwner, c.loadingWarehouse, i.cargoType
                ORDER BY last_24h_vouchers DESC";

        $stmt = $this->conn->prepare($query);
        $stmt->bind_param("ss", $yesterdayJalaliDate, $todayJalaliDate);
        $stmt->execute();
        $result = $stmt->get_result();

        $completionData = [];
        while ($row = $result->fetch_assoc()) {
            $completionData[] = [
                'loadingQuotaNumber' => $row['loadingQuotaNumber'],
                'shipName' => $row['shipName'],
                'shippingCompany' => $row['shippingCompany'],
                'cargoOwner' => $row['cargoOwner'],
                'warehouse' => $row['loadingWarehouse'],
                'cargoType' => !empty($row['cargoType']) ? $row['cargoType'] : 'نامشخص',
                'last_24h_weight' => (float)$row['last_24h_weight'],
                'last_24h_vouchers' => (int)$row['last_24h_vouchers'],
            ];
        }
        $stmt->close();

        $this->sendJsonResponse([
            'success' => true,
            'data' => [
                'dateInfo' => $dateInfo,
                'quotaCompletionAnalysis' => $completionData
            ]
        ]);
    }

    private function getActiveQuotasRemaining(): array {
        $startTime = microtime(true);

        $baseQuery = "SELECT 
            i.shipName, i.loadingQuotaNumber as quotaNumber, i.shippingCompany, i.cargoOwner, i.loadingWarehouse as warehouse, i.cargoType,
            CAST(i.cargoWeight AS DECIMAL(15,2)) as totalTonnage, CAST(i.percentage AS DECIMAL(5,2)) as percentage, CAST(i.is_enabled AS UNSIGNED) as isPercentageEnabled
            FROM InitialInfo i WHERE i.isActive = 1 ORDER BY i.shipName, i.loadingQuotaNumber";

        $stmt = $this->conn->prepare($baseQuery);
        $stmt->execute();
        $baseData = $stmt->get_result()->fetch_all(MYSQLI_ASSOC);
        $stmt->close();

        if (empty($baseData)) {
            return [
                'success' => true,
                'data' => [],
                'summary' => [
                    'totalQuotas' => 0, 'totalOriginalTonnage' => 0, 'totalLoadedTonnage' => 0, 'totalRemainingTonnage' => 0, 'overallPercentageLoaded' => 0
                ],
                'timestamp' => date('Y-m-d H:i:s')
            ];
        }

        $exitQuery = "SELECT loadingQuotaNumber, shipName, loadingWarehouse, shippingCompany, cargoType, SUM(netWeight) as loadedTonnage, COUNT(DISTINCT trackingNumber) as voucherCount FROM CargoInfo WHERE status = 'خروج' GROUP BY loadingQuotaNumber, shipName, loadingWarehouse, shippingCompany, cargoType";
        $stmtExit = $this->conn->prepare($exitQuery);
        $stmtExit->execute();
        $exitData = $stmtExit->get_result()->fetch_all(MYSQLI_ASSOC);
        $stmtExit->close();

        $exitMap = [];
        foreach ($exitData as $row) {
            $key = $row['loadingQuotaNumber'] . '|' . $row['shipName'] . '|' . $row['loadingWarehouse'] . '|' . $row['shippingCompany'] . '|' . $row['cargoType'];
            $exitMap[$key] = [
                'loadedTonnage' => (float)$row['loadedTonnage'],
                'voucherCount' => (int)$row['voucherCount']
            ];
        }

        $quotasData = [];
        $totalOriginal = 0; $totalLoaded = 0; $totalRemaining = 0;

        foreach ($baseData as $row) {
            $totalTonnage = (float)$row['totalTonnage'];
            $percentage = (float)$row['percentage'];
            $isEnabled = (bool)$row['isPercentageEnabled'];

            if ($isEnabled && $percentage > 0) {
                $percentageAmount = $totalTonnage * $percentage * 0.01;
                $adjustedTonnage = $totalTonnage * (1 - $percentage * 0.01);
            } else {
                $percentageAmount = 0;
                $adjustedTonnage = $totalTonnage;
            }

            $key = $row['quotaNumber'] . '|' . $row['shipName'] . '|' . $row['warehouse'] . '|' . $row['shippingCompany'] . '|' . $row['cargoType'];
            $exitInfo = $exitMap[$key] ?? ['loadedTonnage' => 0, 'voucherCount' => 0];

            $loadedTonnage = $exitInfo['loadedTonnage'];
            $remainingTonnage = max(0, $adjustedTonnage - $loadedTonnage);
            $percentageLoaded = $adjustedTonnage > 0 ? round($loadedTonnage / $adjustedTonnage * 100, 2) : 0;
            $status = $remainingTonnage > 0 ? 'دارای مانده' : 'تکمیل شده';

            $totalOriginal += $totalTonnage;
            $totalLoaded += $loadedTonnage;
            $totalRemaining += $remainingTonnage;

            $quotasData[] = [
                'shipName' => $row['shipName'],
                'quotaNumber' => (int)$row['quotaNumber'],
                'shippingCompany' => $row['shippingCompany'],
                'cargoOwner' => $row['cargoOwner'] ?? '',
                'warehouse' => $row['warehouse'],
                'cargoType' => $row['cargoType'],
                'totalTonnage' => $totalTonnage,
                'percentageAmount' => $percentageAmount,
                'percentage' => $percentage,
                'isPercentageEnabled' => $isEnabled,
                'adjustedTotalTonnage' => $adjustedTonnage,
                'loadedTonnage' => $loadedTonnage,
                'remainingTonnage' => $remainingTonnage,
                'percentageLoaded' => $percentageLoaded,
                'voucherCount' => $exitInfo['voucherCount'],
                'status' => $status
            ];
        }

        $executionTime = microtime(true) - $startTime;

        return [
            'success' => true,
            'data' => $quotasData,
            'summary' => [
                'totalQuotas' => count($quotasData),
                'totalOriginalTonnage' => round($totalOriginal, 2),
                'totalLoadedTonnage' => round($totalLoaded, 2),
                'totalRemainingTonnage' => round($totalRemaining, 2),
                'overallPercentageLoaded' => $totalOriginal > 0 ? round($totalLoaded / $totalOriginal * 100, 2) : 0
            ],
            'timestamp' => date('Y-m-d H:i:s'),
            'executionTime' => round($executionTime, 4)
        ];
    }

    private function getShipQuotasRemaining(string $shipName): array {
        $shipName = $this->sanitizeInput($shipName);

        $baseQuery = "SELECT 
            i.shipName, i.loadingQuotaNumber as quotaNumber, i.shippingCompany, i.cargoOwner, i.loadingWarehouse as warehouse, i.cargoType,
            CAST(i.cargoWeight AS DECIMAL(15,2)) as totalTonnage, CAST(i.percentage AS DECIMAL(5,2)) as percentage, CAST(i.is_enabled AS UNSIGNED) as isPercentageEnabled
            FROM InitialInfo i WHERE i.isActive = 1 AND i.shipName = ? ORDER BY i.loadingQuotaNumber";

        $stmt = $this->conn->prepare($baseQuery);
        $stmt->bind_param("s", $shipName);
        $stmt->execute();
        $baseData = $stmt->get_result()->fetch_all(MYSQLI_ASSOC);
        $stmt->close();

        if (empty($baseData)) {
            return [
                'success' => true,
                'shipName' => $shipName,
                'data' => [],
                'summary' => [
                    'totalQuotas' => 0, 'totalOriginalTonnage' => 0, 'totalLoadedTonnage' => 0, 'totalRemainingTonnage' => 0, 'overallPercentageLoaded' => 0
                ],
                'timestamp' => date('Y-m-d H:i:s')
            ];
        }

        $exitQuery = "SELECT loadingQuotaNumber, shipName, loadingWarehouse, shippingCompany, cargoType, SUM(netWeight) as loadedTonnage, COUNT(DISTINCT trackingNumber) as voucherCount FROM CargoInfo WHERE status = 'خروج' AND shipName = ? GROUP BY loadingQuotaNumber, shipName, loadingWarehouse, shippingCompany, cargoType";
        $stmtExit = $this->conn->prepare($exitQuery);
        $stmtExit->bind_param("s", $shipName);
        $stmtExit->execute();
        $exitData = $stmtExit->get_result()->fetch_all(MYSQLI_ASSOC);
        $stmtExit->close();

        $exitMap = [];
        foreach ($exitData as $row) {
            $key = $row['loadingQuotaNumber'] . '|' . $row['shipName'] . '|' . $row['loadingWarehouse'] . '|' . $row['shippingCompany'] . '|' . $row['cargoType'];
            $exitMap[$key] = [
                'loadedTonnage' => (float)$row['loadedTonnage'],
                'voucherCount' => (int)$row['voucherCount']
            ];
        }

        $quotasData = [];
        $totalOriginal = 0; $totalLoaded = 0; $totalRemaining = 0;

        foreach ($baseData as $row) {
            $totalTonnage = (float)$row['totalTonnage'];
            $percentage = (float)$row['percentage'];
            $isEnabled = (bool)$row['isPercentageEnabled'];

            if ($isEnabled && $percentage > 0) {
                $percentageAmount = $totalTonnage * $percentage * 0.01;
                $adjustedTonnage = $totalTonnage * (1 - $percentage * 0.01);
            } else {
                $percentageAmount = 0;
                $adjustedTonnage = $totalTonnage;
            }

            $key = $row['quotaNumber'] . '|' . $row['shipName'] . '|' . $row['warehouse'] . '|' . $row['shippingCompany'] . '|' . $row['cargoType'];
            $exitInfo = $exitMap[$key] ?? ['loadedTonnage' => 0, 'voucherCount' => 0];

            $loadedTonnage = $exitInfo['loadedTonnage'];
            $remainingTonnage = max(0, $adjustedTonnage - $loadedTonnage);
            $percentageLoaded = $adjustedTonnage > 0 ? round($loadedTonnage / $adjustedTonnage * 100, 2) : 0;
            $status = $remainingTonnage > 0 ? 'دارای مانده' : 'تکمیل شده';

            $totalOriginal += $totalTonnage;
            $totalLoaded += $loadedTonnage;
            $totalRemaining += $remainingTonnage;

            $quotasData[] = [
                'shipName' => $row['shipName'],
                'quotaNumber' => (int)$row['quotaNumber'],
                'shippingCompany' => $row['shippingCompany'],
                'cargoOwner' => $row['cargoOwner'] ?? '',
                'warehouse' => $row['warehouse'],
                'cargoType' => $row['cargoType'],
                'totalTonnage' => $totalTonnage,
                'percentageAmount' => $percentageAmount,
                'percentage' => $percentage,
                'isPercentageEnabled' => $isEnabled,
                'adjustedTotalTonnage' => $adjustedTonnage,
                'loadedTonnage' => $loadedTonnage,
                'remainingTonnage' => $remainingTonnage,
                'percentageLoaded' => $percentageLoaded,
                'voucherCount' => $exitInfo['voucherCount'],
                'status' => $status
            ];
        }

        return [
            'success' => true,
            'shipName' => $shipName,
            'data' => $quotasData,
            'summary' => [
                'totalQuotas' => count($quotasData),
                'totalOriginalTonnage' => round($totalOriginal, 2),
                'totalLoadedTonnage' => round($totalLoaded, 2),
                'totalRemainingTonnage' => round($totalRemaining, 2),
                'overallPercentageLoaded' => $totalOriginal > 0 ? round($totalLoaded / $totalOriginal * 100, 2) : 0
            ],
            'timestamp' => date('Y-m-d H:i:s')
        ];
    }

    private function sanitizeInput(string $input): string {
        return htmlspecialchars(strip_tags(trim($input)), ENT_QUOTES, 'UTF-8');
    }

    private function sendJsonResponse(array $data, int $statusCode = 200): void {
        http_response_code($statusCode);
        echo json_encode($data, JSON_UNESCAPED_UNICODE);
        exit;
    }
}
