<?php

declare(strict_types=1);

// تنظیم هدرها و پیکربندی‌های اولیه
header('Content-Type: application/json; charset=UTF-8');
error_reporting(E_ALL);
ini_set('display_errors', '0');
ini_set('log_errors', '0');
ini_set('error_log', __DIR__ . '/error.log');
date_default_timezone_set('Asia/Tehran');

// بارگذاری فایل‌های پیکربندی و کلاس‌ها
require_once __DIR__ . '/config/config.php';
require_once __DIR__ . '/jdf.php';

/**
 * کلاس استثنا برای خطاهای مرتبط با API.
 */
class APIException extends Exception {}

/**
 * کلاس Logger برای ثبت لاگ‌ها در فایل.
 */
class Logger {
    private string $logFile;

    public function __construct(string $logFile = '/realTimeLoadingData_log.log') {
        $this->logFile = __DIR__ . $logFile;
    }

    public function log(string $message, string $type = 'INFO'): void {
        // Log logic disabled for production performance
    }
}

/**
 * کلاس DateConverter برای تبدیل تاریخ میلادی به شمسی.
 */
class DateConverter {
    public static function gregorianToJalali(int $gy, int $gm, int $gd): array {
        return gregorian_to_jalali($gy, $gm, $gd);
    }

    public static function jalaliToGregorian(int $jy, int $jm, int $jd): array {
        return jalali_to_gregorian($jy, $jm, $jd);
    }

    public static function getCurrentJalaliDate(): string {
        return jdate('Y/m/d');
    }
}

/**
 * کلاس APIResponse برای ارسال پاسخ‌های JSON به کلاینت.
 */
class APIResponse {
    public static function send(array $data, int $statusCode = 200): void {
        http_response_code($statusCode);
        echo json_encode($data);
        exit;
    }
}

/**
 * کلاس تحلیل پیشرفته اطلاعات محموله‌ها
 */
class AdvancedCargoAnalytics {
    private mysqli $conn;
    private Logger $logger;
    private const LOG_PREFIX = 'AdvancedCargoAnalytics';
    private string $yesterdayJalaliDate;
    private string $todayJalaliDate;

    public function __construct(mysqli $conn, int $offset = 0) {
        $this->conn = $conn;
        $this->logger = new Logger();
        $this->setupConnection();
        
        $currentTime = time();
        if (date('H') < 7) {
            $currentTime = strtotime('-1 day');
        }
        
        $targetTime = $currentTime + ($offset * 86400);
        $this->todayJalaliDate = jdate('Y/m/d', $targetTime);
        $this->yesterdayJalaliDate = jdate('Y/m/d', $targetTime - 86400);
    }

    private function setupConnection(): void {
        try {
            $this->conn->set_charset("utf8mb4");
            $this->conn->query("SET time_zone = '+03:30'");
            $this->conn->query("SET SESSION SQL_BIG_SELECTS=1");
            $this->conn->query("SET SESSION group_concat_max_len=1000000");
            $this->conn->query("SET SESSION optimizer_search_depth=0");
            $this->conn->query("SET SESSION max_execution_time=30000");
            $this->conn->query("SET SESSION sort_buffer_size=1048576");
        } catch (Exception $e) {
            throw new Exception("Database connection setup failed: " . $e->getMessage());
        }
    }

    private function testDatabaseConnection(): void {
        try {
            $result = $this->conn->query("SELECT 1 FROM CargoInfo LIMIT 1");
            if (!$result) {
                throw new Exception("Cannot access CargoInfo table");
            }
        } catch (Exception $e) {
            $this->logger->log(self::LOG_PREFIX . " - Database connection test failed: " . $e->getMessage(), 'ERROR');
            throw $e;
        }
    }

    public function getComprehensiveAnalysis(): array {
		try {
			$this->testDatabaseConnection();
			
			[$jy, $jm, $jd] = array_map('intval', explode('/', $this->todayJalaliDate));
			[$gy, $gm, $gd] = DateConverter::jalaliToGregorian($jy, $jm, $jd);
			$timestamp = mktime(12, 0, 0, $gm, $gd, $gy);
			
			$dateInfo = [
				'jalaliDate' => $this->todayJalaliDate,
				'dayName' => jdate('l', $timestamp)
			];

			$data = [
				'success' => true,
				'data' => [
					'dateInfo' => $dateInfo,
					'quotaCompletionAnalysis' => $this->getQuotaCompletionAnalysis()
				]
			];
			
			return $data;
		} catch (Exception $e) {
			$this->logger->log(self::LOG_PREFIX . " - Analysis failed: " . $e->getMessage(), 'ERROR');
			throw $e;
		}
	}

    private function getQuotaCompletionAnalysis(): array {
        try {
            $query = "
                SELECT 
                    c.loadingQuotaNumber,
                    i.shipName,
                    c.shippingCompany,
                    i.cargoOwner,
                    c.loadingWarehouse,
                    i.cargoType,
                    SUM(c.netWeight) AS last_24h_weight,
                    COUNT(*) AS last_24h_vouchers
                FROM CargoInfo c
                JOIN InitialInfo i ON c.loadingQuotaNumber = i.loadingQuotaNumber 
                    AND c.loadingWarehouse = i.loadingWarehouse
                    AND c.shippingCompany = i.shippingCompany
                WHERE 
                    c.status = 'خروج' 
                    AND (
                        (c.exitDate = ? AND c.exitTime >= '07:00:00') OR
                        (c.exitDate = ? AND c.exitTime < '07:00:00')
                    )
                GROUP BY c.loadingQuotaNumber, i.shipName, c.shippingCompany, i.cargoOwner, c.loadingWarehouse, i.cargoType
                ORDER BY last_24h_vouchers DESC";
    
            $stmt = $this->conn->prepare($query);
            $stmt->bind_param("ss", $this->yesterdayJalaliDate, $this->todayJalaliDate);
            $stmt->execute();
            $result = $stmt->get_result();
            $data = [];
    
            while ($row = $result->fetch_assoc()) {
                $data[] = [
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
            return $data;
        } catch (Exception $e) {
            $this->logger->log("Error in getQuotaCompletionAnalysis: " . $e->getMessage(), "ERROR");
            throw $e;
        }
    }

    // getQuotaProgressAnalysis and getQuotaPredictionAnalysis removed
}

/**
 * کلاس اصلی برای مدیریت درخواست‌های API مربوط به Cargo.
 */
class CargoAPI {
    private mysqli $conn;
    private Logger $logger;

    public function __construct(mysqli $conn) {
        $this->conn = $conn;
        $this->logger = new Logger();
    }

    public function handleRequest(): void {
        try {
            $this->validateRequestMethod();
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
                    APIResponse::send(['error' => 'عملیات نامعتبر است.'], 400);
            }
        } catch (APIException $e) {
            APIResponse::send(['error' => $e->getMessage()], 400);
        } catch (Exception $e) {
            $this->logger->log($e->getMessage(), 'ERROR');
            APIResponse::send(['error' => 'خطایی در سرور رخ داد.'], 500);
        }
    }

    private function validateRequestMethod(): void {
        if ($_SERVER['REQUEST_METHOD'] !== 'GET') {
            throw new APIException('فقط متد GET مجاز است.');
        }
    }

    private function handleKotazhRequest(): void {
        $kotazh = $this->validateKotazh($_GET['kotazh'] ?? '');
        $kotazhInfo = $this->getKotazhInfo($kotazh);

        if (!$kotazhInfo) {
            APIResponse::send(['error' => 'کوتاژ مورد نظر یافت نشد.'], 404);
        }

        $cargoInfo = $this->getCargoInfo($kotazh);
        APIResponse::send([
            'kotazhInfo' => $kotazhInfo,
            'cargoInfo' => $cargoInfo
        ]);
    }

    private function validateKotazh(string $kotazh): string {
        $kotazh = trim($kotazh);
        if (empty($kotazh)) {
            throw new APIException('کوتاژ نمی‌تواند خالی باشد.');
        }
        if (!preg_match('/^\d{8}$/', $kotazh)) {
            throw new APIException('فرمت کوتاژ نامعتبر است. باید شامل 8 رقم باشد.');
        }
        return $kotazh;
    }

    private function getKotazhInfo(string $kotazh): ?array {
        $query = "
            SELECT 
                shipName, loadingWarehouse, cargoType, 
                shippingCompany, cargoWeight, loadingQuotaNumber 
            FROM InitialInfo 
            WHERE loadingQuotaNumber = ?
        ";
        $stmt = $this->conn->prepare($query);
        if (!$stmt) {
            throw new Exception('آماده‌سازی پرس‌وجوی پایگاه داده ناموفق بود.');
        }

        $stmt->bind_param("s", $kotazh);
        $stmt->execute();
        $result = $stmt->get_result();
        return $result->fetch_assoc();
    }

    private function getCargoInfo(string $kotazh): array {
        $query = "
            SELECT 
                trackingNumber, entryTime, netWeight, scaleReceiptNumber,
                shortageWeight, excessWeight, exitTime, exitDate, status 
            FROM CargoInfo 
            WHERE loadingQuotaNumber = ?
        ";
        $stmt = $this->conn->prepare($query);
        if (!$stmt) {
            throw new Exception('آماده‌سازی پرس‌وجوی پایگاه داده ناموفق بود.');
        }

        $stmt->bind_param("s", $kotazh);
        $stmt->execute();
        $result = $stmt->get_result();
        return $result->fetch_all(MYSQLI_ASSOC);
    }

    private function handleRealTimeDataRequest(): void {
        $shiftOffset = isset($_GET['shiftOffset']) ? (int)$_GET['shiftOffset'] : 0;
        $targetTimestamp = time() + ($shiftOffset * 12 * 3600);
        $currentTimeString = date('H:i:s', $targetTimestamp);
        
        $shiftInfo = $this->determineShiftInfo($currentTimeString, $targetTimestamp);
        $realTimeData = $this->getRealTimeData($shiftInfo);

        APIResponse::send([
            'shiftInfo' => $shiftInfo,
            'data' => $realTimeData
        ]);
    }

    private function determineShiftInfo(string $currentTime, int $targetTimestamp = null): array {
        if ($targetTimestamp === null) {
            $targetTimestamp = time();
        }
        
        $currentJalaliDate = jdate('Y/m/d', $targetTimestamp);
        $shiftType = '';
        $shiftInfo = [];

        if ($currentTime >= '07:30:00' && $currentTime < '19:00:00') {
            $shiftType = 'روز';
            $shiftInfo = [
                'startDate' => $currentJalaliDate,
                'endDate' => $currentJalaliDate,
                'startTime' => '07:30:00',
                'endTime' => '19:00:00',
                'type' => $shiftType
            ];
        } else {
            $shiftType = 'شب';
            if ($currentTime >= '00:00:00' && $currentTime < '07:30:00') {
                $prevDateTimestamp = $targetTimestamp - 86400;
                [$prevJY, $prevJM, $prevJD] = DateConverter::gregorianToJalali(
                    (int)date('Y', $prevDateTimestamp),
                    (int)date('m', $prevDateTimestamp),
                    (int)date('d', $prevDateTimestamp)
                );
                $shiftStartDate = sprintf('%04d/%02d/%02d', $prevJY, $prevJM, $prevJD);
                $shiftEndDate = $currentJalaliDate;
            } else {
                $shiftStartDate = $currentJalaliDate;
                $nextDateTimestamp = $targetTimestamp + 86400;
                [$nextJY, $nextJM, $nextJD] = DateConverter::gregorianToJalali(
                    (int)date('Y', $nextDateTimestamp),
                    (int)date('m', $nextDateTimestamp),
                    (int)date('d', $nextDateTimestamp)
                );
                $shiftEndDate = sprintf('%04d/%02d/%02d', $nextJY, $nextJM, $nextJD);
            }

            $shiftInfo = [
                'startDate' => $shiftStartDate,
                'endDate' => $shiftEndDate,
                'startTime' => '19:00:00',
                'endTime' => '07:00:00',
                'type' => $shiftType
            ];
        }

        return $shiftInfo;
    }

    private function getRealTimeData(array $shiftInfo): array {
        $query = $this->buildRealTimeDataQuery($shiftInfo);
        $stmt = $this->conn->prepare($query);
        if (!$stmt) {
            throw new Exception('آماده‌سازی پرس‌وجوی پایگاه داده ناموفق بود.');
        }

        if ($shiftInfo['type'] === 'روز') {
            $stmt->bind_param(
                "sss", 
                $shiftInfo['startDate'], 
                $shiftInfo['startTime'], 
                $shiftInfo['endTime']
            );
        } else {
            $stmt->bind_param(
                "ssss", 
                $shiftInfo['startDate'], 
                $shiftInfo['startTime'], 
                $shiftInfo['endDate'], 
                $shiftInfo['endTime']
            );
        }

        $stmt->execute();
        $result = $stmt->get_result();
        return $result->fetch_all(MYSQLI_ASSOC);
    }

    private function buildRealTimeDataQuery(array $shiftInfo): string {
        $baseQuery = "
            SELECT 
                i.loadingQuotaNumber,
                i.shipName,
                i.loadingWarehouse,
                i.shippingCompany,
                i.cargoType,
                COUNT(DISTINCT CASE WHEN c.status = 'ورود' THEN c.id END) AS entryVouchers,
                COUNT(DISTINCT CASE WHEN c.status = 'خروج' THEN c.id END) AS exitVouchers,
                COUNT(DISTINCT c.id) AS totalVouchers,
                SUM(CASE WHEN c.status = 'خروج' THEN c.netWeight ELSE 0 END) AS totalNetWeight
            FROM 
                InitialInfo i
            LEFT JOIN 
                CargoInfo c ON i.loadingQuotaNumber = c.loadingQuotaNumber 
                    AND i.loadingWarehouse = c.loadingWarehouse
                    AND i.shippingCompany = c.shippingCompany
            WHERE 
        ";

        if ($shiftInfo['type'] === 'روز') {
            $conditions = "
                (c.exitDate = ? AND c.exitTime BETWEEN ? AND ?)
                OR (c.status = 'ورود' AND c.exitDate IS NULL)
            ";
        } else {
            $conditions = "
                (c.exitDate = ? AND c.exitTime >= ?)
                OR (c.exitDate = ? AND c.exitTime < ?)
                OR (c.status = 'ورود' AND c.exitDate IS NULL)
            ";
        }

        $groupBy = "
            GROUP BY 
                i.loadingQuotaNumber, i.shipName, i.loadingWarehouse, i.shippingCompany, i.cargoType
        ";

        return $baseQuery . $conditions . $groupBy;
    }

    private function handleComprehensiveAnalysisRequest(): void {
        try {
            $offset = isset($_GET['offset']) ? (int)$_GET['offset'] : 0;
            $analytics = new AdvancedCargoAnalytics($this->conn, $offset);
            $results = $analytics->getComprehensiveAnalysis();
            
            header('Content-Type: application/json; charset=UTF-8');
            echo json_encode($results);
            exit;
        } catch (Exception $e) {
            header('Content-Type: application/json; charset=UTF-8');
            http_response_code(500);
            echo json_encode([
                'success' => false,
                'error' => $e->getMessage()
            ]);
            exit;
        }
    }
}

// مقداردهی اولیه و مدیریت درخواست API
try {
    $dbConnection = getDbConnection();
    $cargoAPI = new CargoAPI($dbConnection);
    $cargoAPI->handleRequest();
} catch (Exception $e) {
    $logger = new Logger();
    $logger->log($e->getMessage(), 'CRITICAL');
    APIResponse::send(['error' => 'خطای داخلی سرور'], 500);
}
?>