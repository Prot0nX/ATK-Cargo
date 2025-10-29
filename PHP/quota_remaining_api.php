<?php
// quota_remaining_api.php
// API برای محاسبه مانده تناژ کوتاژهای فعال - نسخه بهینه‌شده

declare(strict_types=1);

ini_set('display_errors', 0);
ini_set('display_startup_errors', 0);
error_reporting(E_ALL);

// فعال‌سازی فشرده‌سازی خروجی برای کاهش حجم داده
if (extension_loaded('zlib') && !ini_get('zlib.output_compression')) {
    ob_start('ob_gzhandler');
}

header('Content-Type: application/json; charset=UTF-8');
header('Cache-Control: max-age=60, public'); // کش 60 ثانیه‌ای

require_once __DIR__ . '/config/config.php';

date_default_timezone_set('Asia/Tehran');

/**
 * کلاس Logger مینیمال برای performance monitoring
 */
class PerformanceLogger {
    private bool $debugMode;
    
    public function __construct(bool $debugMode = false) {
        $this->debugMode = $debugMode;
    }
    
    public function log(string $message, string $level = 'INFO'): void {
        if ($this->debugMode) {
            $timestamp = date('[Y-m-d H:i:s]');
            error_log("$timestamp [$level] $message");
        }
    }
    
    public function logExecutionTime(string $context, float $executionTime): void {
        if ($this->debugMode) {
            $this->log("$context completed in " . number_format($executionTime, 4) . " seconds", 'PERFORMANCE');
        }
    }
}

class DatabaseManager {
    private mysqli $conn;
    private static ?self $instance = null;
    private array $cache = []; // کش حافظه داخلی
    private PerformanceLogger $logger;
    
    private function __construct() {
        $this->logger = new PerformanceLogger(false); // تغییر به true برای فعال‌سازی debug
        $this->conn = $this->getDbConnection();
        $this->setupOptimizedConnection();
    }
    
    // الگوی Singleton برای جلوگیری از اتصالات متعدد
    public static function getInstance(): self {
        if (self::$instance === null) {
            self::$instance = new self();
        }
        return self::$instance;
    }
    
    /**
     * تنظیم اتصال بهینه‌شده MySQL برای سرعت بیشتر
     */
    private function setupOptimizedConnection(): void {
        try {
            // تنظیمات بهینه‌سازی MySQL مشابه realTimeLoadingData.php
            $this->conn->query("SET SESSION sql_mode = 'NO_ENGINE_SUBSTITUTION'");
            $this->conn->query("SET time_zone = '+03:30'");
            
            // تنظیمات بهینه‌سازی برای کوئری‌های پیچیده
            $this->conn->query("SET SESSION SQL_BIG_SELECTS=1");
            $this->conn->query("SET SESSION group_concat_max_len=1000000");
            $this->conn->query("SET SESSION optimizer_search_depth=0");
            $this->conn->query("SET SESSION max_execution_time=30000");
            $this->conn->query("SET SESSION sort_buffer_size=1048576");
            
            $this->logger->log("MySQL connection optimized successfully", 'INFO');
        } catch (Exception $e) {
            $this->logger->log("Failed to optimize MySQL connection: " . $e->getMessage(), 'ERROR');
            throw new Exception("Database connection optimization failed: " . $e->getMessage());
        }
    }
    
    private function getDbConnection(): mysqli {
        $conn = new mysqli(DB_HOST, DB_USER, DB_PASSWORD, DB_NAME);
        if ($conn->connect_error) {
            throw new Exception("خطا در اتصال به پایگاه داده: " . $conn->connect_error);
        }
        $conn->set_charset("utf8mb4");
        
        // بهینه‌سازی اتصال
        $conn->options(MYSQLI_OPT_CONNECT_TIMEOUT, 5);
        $conn->options(MYSQLI_OPT_READ_TIMEOUT, 10);
        
        return $conn;
    }
    
    /**
     * اجرای کوئری با performance monitoring و کش
     */
    public function executeQuery(string $query, string $context, array $params = []): array {
        $cacheKey = md5($query . serialize($params));
        
        // بررسی کش حافظه
        if (isset($this->cache[$cacheKey])) {
            $this->logger->log("Cache hit for $context", 'DEBUG');
            return $this->cache[$cacheKey];
        }
        
        $startTime = microtime(true);
        $this->logger->log("Executing query for $context", 'DEBUG');
        
        try {
            $stmt = $this->conn->prepare($query);
            if (!$stmt) {
                throw new Exception("Error preparing statement: " . $this->conn->error);
            }
            
            if (!empty($params)) {
                $types = str_repeat('s', count($params));
                $stmt->bind_param($types, ...$params);
            }
            
            if (!$stmt->execute()) {
                throw new Exception("Error executing statement: " . $stmt->error);
            }
            
            $result = $stmt->get_result();
            if (!$result) {
                throw new Exception("Error getting results: " . $stmt->error);
            }
            
            $data = [];
            while ($row = $result->fetch_assoc()) {
                $data[] = $row;
            }
            
            $stmt->close();
            
            // ذخیره در کش
            $this->cache[$cacheKey] = $data;
            
            $executionTime = microtime(true) - $startTime;
            $this->logger->logExecutionTime($context, $executionTime);
            
            return $data;
            
        } catch (Exception $e) {
            $this->logger->log("Error in $context: " . $e->getMessage(), 'ERROR');
            throw new Exception("Query execution failed in $context: " . $e->getMessage());
        }
    }
    
    public function prepare(string $query): mysqli_stmt {
        $stmt = $this->conn->prepare($query);
        if (!$stmt) {
            throw new Exception("خطا در آماده‌سازی دستور SQL: " . $this->conn->error);
        }
        // استفاده از buffered result برای عملکرد بهتر
        $stmt->attr_set(MYSQLI_STMT_ATTR_CURSOR_TYPE, MYSQLI_CURSOR_TYPE_READ_ONLY);
        return $stmt;
    }
    
    public function close(): void {
        if ($this->conn) {
            $this->conn->close();
        }
    }
}

function customLog(string $message): void {
    // می‌توانید در صورت نیاز لاگ را فعال کنید
    // $logFile = __DIR__ . '/logs/quota_remaining.log';
    // $logMessage = date('[Y-m-d H:i:s] ') . $message . PHP_EOL;
    // file_put_contents($logFile, $logMessage, FILE_APPEND);
}

function sendJsonResponse($data, int $statusCode = 200): void {
    http_response_code($statusCode);
    echo json_encode($data, JSON_UNESCAPED_UNICODE | JSON_THROW_ON_ERROR);
    exit;
}

function sanitizeInput(string $input): string {
    return htmlspecialchars(strip_tags(trim($input)), ENT_QUOTES, 'UTF-8');
}

/**
 * محاسبه مانده تناژ کوتاژهای فعال - نسخه فوق‌بهینه (تقسیم کوئری)
 * بهینه‌سازی‌های پیشرفته:
 * 1. تقسیم کوئری پیچیده به کوئری‌های ساده‌تر (کاهش CPU بیشتر)
 * 2. محاسبات در PHP به جای SQL
 * 3. کاهش JOIN های زیادی
 * 4. استفاده از indexed lookups در PHP
 * 5. Streaming نتایج برای کاهش حافظه
 */
function getActiveQuotasRemaining(DatabaseManager $db): array {
    try {
        $startTime = microtime(true);
        
        // کوئری 1: دریافت اطلاعات فعال کوتاژها - ساده و سریع
        $baseQuery = "
        SELECT 
            i.shipName,
            i.loadingQuotaNumber as quotaNumber,
            i.shippingCompany,
            i.cargoOwner,
            i.loadingWarehouse as warehouse,
            i.cargoType,
            CAST(i.cargoWeight AS DECIMAL(15,2)) as totalTonnage,
            CAST(i.percentage AS DECIMAL(5,2)) as percentage,
            CAST(i.is_enabled AS UNSIGNED) as isPercentageEnabled
        FROM InitialInfo i
        WHERE i.isActive = 1
        ORDER BY i.shipName, i.loadingQuotaNumber
        ";
        
        $baseData = $db->executeQuery($baseQuery, 'Active Quotas Base Data', []);
        
        if (empty($baseData)) {
            return [
                'success' => true,
                'data' => [],
                'summary' => [
                    'totalQuotas' => 0,
                    'totalOriginalTonnage' => 0,
                    'totalLoadedTonnage' => 0,
                    'totalRemainingTonnage' => 0,
                    'overallPercentageLoaded' => 0
                ],
                'timestamp' => date('Y-m-d H:i:s')
            ];
        }
        
        // کوئری 2: دریافت خروج‌ها - ساده و سریع
        $exitQuery = "
        SELECT 
            loadingQuotaNumber,
            shipName,
            loadingWarehouse,
            shippingCompany,
            cargoType,
            SUM(netWeight) as loadedTonnage,
            COUNT(DISTINCT trackingNumber) as voucherCount
        FROM CargoInfo
        WHERE status = 'خروج'
        GROUP BY loadingQuotaNumber, shipName, loadingWarehouse, shippingCompany, cargoType
        ";
        
        $exitData = $db->executeQuery($exitQuery, 'Active Quotas Exit Summary', []);
        
        // ساخت indexed array برای جستجوی سریع
        $exitMap = [];
        foreach ($exitData as $row) {
            $key = $row['loadingQuotaNumber'] . '|' . $row['shipName'] . '|' . $row['loadingWarehouse'] . '|' . $row['shippingCompany'] . '|' . $row['cargoType'];
            $exitMap[$key] = [
                'loadedTonnage' => (float)$row['loadedTonnage'],
                'voucherCount' => (int)$row['voucherCount']
            ];
        }
        
        // محاسبات در PHP - خیلی سریعتر از SQL برای این نوع محاسبات
        $quotasData = [];
        $totalOriginal = 0;
        $totalLoaded = 0;
        $totalRemaining = 0;
        
        foreach ($baseData as $row) {
            $totalTonnage = (float)$row['totalTonnage'];
            $percentage = (float)$row['percentage'];
            $isEnabled = (bool)$row['isPercentageEnabled'];
            
            // محاسبه adjusted tonnage
            if ($isEnabled && $percentage > 0) {
                $percentageAmount = $totalTonnage * $percentage * 0.01;
                $adjustedTonnage = $totalTonnage * (1 - $percentage * 0.01);
            } else {
                $percentageAmount = 0;
                $adjustedTonnage = $totalTonnage;
            }
            
            // جستجو در exit map
            $key = $row['quotaNumber'] . '|' . $row['shipName'] . '|' . $row['warehouse'] . '|' . $row['shippingCompany'] . '|' . $row['cargoType'];
            $exitInfo = $exitMap[$key] ?? ['loadedTonnage' => 0, 'voucherCount' => 0];
            
            $loadedTonnage = $exitInfo['loadedTonnage'];
            $remainingTonnage = max(0, $adjustedTonnage - $loadedTonnage);
            $percentageLoaded = $adjustedTonnage > 0 ? round($loadedTonnage / $adjustedTonnage * 100, 2) : 0;
            $status = $remainingTonnage > 0 ? 'دارای مانده' : 'تکمیل شده';
            
            // تجمیع برای خلاصه
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
        
    } catch (Exception $e) {
        throw new Exception('خطا در محاسبه مانده کوتاژها: ' . $e->getMessage());
    }
}

/**
 * محاسبه مانده تناژ برای یک کشتی خاص - نسخه فوق‌بهینه (تقسیم کوئری)
 */
function getShipQuotasRemaining(DatabaseManager $db, string $shipName): array {
    try {
        $shipName = sanitizeInput($shipName);
        
        // کوئری 1: اطلاعات کوتاژهای کشتی - ساده و سریع
        $baseQuery = "
        SELECT 
            i.shipName,
            i.loadingQuotaNumber as quotaNumber,
            i.shippingCompany,
            i.cargoOwner,
            i.loadingWarehouse as warehouse,
            i.cargoType,
            CAST(i.cargoWeight AS DECIMAL(15,2)) as totalTonnage,
            CAST(i.percentage AS DECIMAL(5,2)) as percentage,
            CAST(i.is_enabled AS UNSIGNED) as isPercentageEnabled
        FROM InitialInfo i
        WHERE i.isActive = 1 AND i.shipName = ?
        ORDER BY i.loadingQuotaNumber
        ";
        
        $baseData = $db->executeQuery($baseQuery, 'Ship Quotas Base Data', [$shipName]);
        
        if (empty($baseData)) {
            return [
                'success' => true,
                'shipName' => $shipName,
                'data' => [],
                'summary' => [
                    'totalQuotas' => 0,
                    'totalOriginalTonnage' => 0,
                    'totalLoadedTonnage' => 0,
                    'totalRemainingTonnage' => 0,
                    'overallPercentageLoaded' => 0
                ],
                'timestamp' => date('Y-m-d H:i:s')
            ];
        }
        
        // کوئری 2: خروج‌های کشتی - ساده و سریع
        $exitQuery = "
        SELECT 
            loadingQuotaNumber,
            shipName,
            loadingWarehouse,
            shippingCompany,
            cargoType,
            SUM(netWeight) as loadedTonnage,
            COUNT(DISTINCT trackingNumber) as voucherCount
        FROM CargoInfo
        WHERE status = 'خروج' AND shipName = ?
        GROUP BY loadingQuotaNumber, shipName, loadingWarehouse, shippingCompany, cargoType
        ";
        
        $exitData = $db->executeQuery($exitQuery, 'Ship Quotas Exit Summary', [$shipName]);
        
        // ساخت indexed array برای جستجوی سریع
        $exitMap = [];
        foreach ($exitData as $row) {
            $key = $row['loadingQuotaNumber'] . '|' . $row['shipName'] . '|' . $row['loadingWarehouse'] . '|' . $row['shippingCompany'] . '|' . $row['cargoType'];
            $exitMap[$key] = [
                'loadedTonnage' => (float)$row['loadedTonnage'],
                'voucherCount' => (int)$row['voucherCount']
            ];
        }
        
        // محاسبات در PHP
        $quotasData = [];
        $totalOriginal = 0;
        $totalLoaded = 0;
        $totalRemaining = 0;
        
        foreach ($baseData as $row) {
            $totalTonnage = (float)$row['totalTonnage'];
            $percentage = (float)$row['percentage'];
            $isEnabled = (bool)$row['isPercentageEnabled'];
            
            // محاسبه adjusted tonnage
            if ($isEnabled && $percentage > 0) {
                $percentageAmount = $totalTonnage * $percentage * 0.01;
                $adjustedTonnage = $totalTonnage * (1 - $percentage * 0.01);
            } else {
                $percentageAmount = 0;
                $adjustedTonnage = $totalTonnage;
            }
            
            // جستجو در exit map
            $key = $row['quotaNumber'] . '|' . $row['shipName'] . '|' . $row['warehouse'] . '|' . $row['shippingCompany'] . '|' . $row['cargoType'];
            $exitInfo = $exitMap[$key] ?? ['loadedTonnage' => 0, 'voucherCount' => 0];
            
            $loadedTonnage = $exitInfo['loadedTonnage'];
            $remainingTonnage = max(0, $adjustedTonnage - $loadedTonnage);
            $percentageLoaded = $adjustedTonnage > 0 ? round($loadedTonnage / $adjustedTonnage * 100, 2) : 0;
            $status = $remainingTonnage > 0 ? 'دارای مانده' : 'تکمیل شده';
            
            // تجمیع برای خلاصه
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
        
    } catch (Exception $e) {
        throw new Exception('خطا در محاسبه مانده کوتاژهای کشتی: ' . $e->getMessage());
    }
}

// پردازش درخواست‌های API - نسخه بهینه‌شده
try {
    // بررسی سریع متد درخواست
    if ($_SERVER['REQUEST_METHOD'] !== 'GET') {
        throw new Exception('روش درخواست نامعتبر است');
    }
    
    // استفاده از Singleton برای اتصال واحد
    $db = DatabaseManager::getInstance();
    
    // بررسی وجود action
    if (!isset($_GET['action'])) {
        throw new Exception('عملیات مشخص نشده است');
    }
    
    $action = sanitizeInput($_GET['action']);
    
    // پردازش درخواست بر اساس action
    switch ($action) {
        case 'getActiveQuotasRemaining':
            $result = getActiveQuotasRemaining($db);
            sendJsonResponse($result);
            break;
            
        case 'getShipQuotasRemaining':
            if (!isset($_GET['shipName'])) {
                throw new Exception('نام کشتی مشخص نشده است');
            }
            $result = getShipQuotasRemaining($db, $_GET['shipName']);
            sendJsonResponse($result);
            break;
            
        default:
            throw new Exception('عملیات نامعتبر است');
    }
    
} catch (Exception $e) {
    sendJsonResponse([
        'success' => false,
        'error' => $e->getMessage()
    ], 500);
} finally {
    if (isset($db)) {
        $db->close();
    }
}

?>