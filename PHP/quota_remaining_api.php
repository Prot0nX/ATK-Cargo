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

class DatabaseManager {
    private mysqli $conn;
    private static ?self $instance = null;
    
    private function __construct() {
        $this->conn = $this->getDbConnection();
        // بهینه‌سازی تنظیمات MySQL
        $this->conn->query("SET SESSION sql_mode = 'NO_ENGINE_SUBSTITUTION'");
    }
    
    // الگوی Singleton برای جلوگیری از اتصالات متعدد
    public static function getInstance(): self {
        if (self::$instance === null) {
            self::$instance = new self();
        }
        return self::$instance;
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
 * محاسبه مانده تناژ کوتاژهای فعال - نسخه فوق‌بهینه
 * بهینه‌سازی‌های پیشرفته:
 * 1. محاسبات کامل در SQL (حذف محاسبات PHP)
 * 2. استفاده از CTE برای خوانایی و بهینه‌سازی بهتر
 * 3. محاسبه مانده و درصد بارگیری در SQL
 * 4. کاهش تعداد عملیات JOIN
 * 5. استفاده از عملگرهای ریاضی بهینه
 */
function getActiveQuotasRemaining(DatabaseManager $db): array {
    try {
        // کوئری فوق‌بهینه با CTE و محاسبات کامل در SQL + آمار کلی
        $query = "
        WITH ExitSummary AS (
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
        ),
        QuotaData AS (
            SELECT 
                i.shipName,
                i.loadingQuotaNumber as quotaNumber,
                i.shippingCompany,
                i.cargoOwner,
                i.loadingWarehouse as warehouse,
                i.cargoType,
                i.cargoWeight as totalTonnage,
                i.percentage,
                i.is_enabled as isPercentageEnabled,
                COALESCE(e.loadedTonnage, 0) as loadedTonnage,
                COALESCE(e.voucherCount, 0) as voucherCount,
                IF(i.is_enabled AND i.percentage > 0, 
                   i.cargoWeight * i.percentage * 0.01, 
                   0) as percentageAmount,
                IF(i.is_enabled AND i.percentage > 0,
                   i.cargoWeight * (1 - i.percentage * 0.01),
                   i.cargoWeight) as adjustedTonnage,
                GREATEST(0,
                    IF(i.is_enabled AND i.percentage > 0,
                       i.cargoWeight * (1 - i.percentage * 0.01),
                       i.cargoWeight) - COALESCE(e.loadedTonnage, 0)
                ) as remainingTonnage,
                IF(i.cargoWeight > 0,
                   ROUND((COALESCE(e.loadedTonnage, 0) / 
                          IF(i.is_enabled AND i.percentage > 0,
                             i.cargoWeight * (1 - i.percentage * 0.01),
                             i.cargoWeight)) * 100, 2),
                   0) as percentageLoaded
            FROM InitialInfo i
            LEFT JOIN ExitSummary e ON 
                e.loadingQuotaNumber = i.loadingQuotaNumber
                AND e.shipName = i.shipName
                AND e.loadingWarehouse = i.loadingWarehouse
                AND e.shippingCompany = i.shippingCompany
                AND e.cargoType = i.cargoType
            WHERE i.isActive = 1
        )
        SELECT 
            q.*,
            -- محاسبه آمار کلی در SQL
            (SELECT COUNT(*) FROM QuotaData) as totalQuotas,
            (SELECT SUM(totalTonnage) FROM QuotaData) as totalOriginalTonnage,
            (SELECT SUM(loadedTonnage) FROM QuotaData) as totalLoadedTonnage,
            (SELECT SUM(remainingTonnage) FROM QuotaData) as totalRemainingTonnage
        FROM QuotaData q
        ORDER BY q.shipName, q.quotaNumber
        ";
        
        $stmt = $db->prepare($query);
        $stmt->execute();
        $result = $stmt->get_result();
        
        // پردازش بدون محاسبات - همه چیز از SQL آماده است
        $quotasData = [];
        $summary = null;
        
        while ($row = $result->fetch_assoc()) {
            // ذخیره آمار کلی از اولین ردیف (یکسان برای همه)
            if ($summary === null) {
                $totalOriginal = (float)$row['totalOriginalTonnage'];
                $totalLoaded = (float)$row['totalLoadedTonnage'];
                
                $summary = [
                    'totalQuotas' => (int)$row['totalQuotas'],
                    'totalOriginalTonnage' => $totalOriginal,
                    'totalLoadedTonnage' => $totalLoaded,
                    'totalRemainingTonnage' => (float)$row['totalRemainingTonnage'],
                    'overallPercentageLoaded' => $totalOriginal > 0 
                        ? round($totalLoaded / $totalOriginal * 100, 2) 
                        : 0
                ];
            }
            
            // ساخت آرایه خروجی - فقط type casting
            $remainingTonnage = (float)$row['remainingTonnage'];
            $quotasData[] = [
                'shipName' => $row['shipName'],
                'quotaNumber' => (int)$row['quotaNumber'],
                'shippingCompany' => $row['shippingCompany'],
                'cargoOwner' => $row['cargoOwner'] ?? '',
                'warehouse' => $row['warehouse'],
                'cargoType' => $row['cargoType'],
                'totalTonnage' => (float)$row['totalTonnage'],
                'percentageAmount' => (float)$row['percentageAmount'],
                'percentage' => (float)$row['percentage'],
                'isPercentageEnabled' => (bool)$row['isPercentageEnabled'],
                'adjustedTotalTonnage' => (float)$row['adjustedTonnage'],
                'loadedTonnage' => (float)$row['loadedTonnage'],
                'remainingTonnage' => $remainingTonnage,
                'percentageLoaded' => (float)$row['percentageLoaded'],
                'voucherCount' => (int)$row['voucherCount'],
                'status' => $remainingTonnage > 0 ? 'دارای مانده' : 'تکمیل شده'
            ];
        }
        
        $stmt->close();
        
        // خروجی نهایی - بدون محاسبات PHP
        return [
            'success' => true,
            'data' => $quotasData,
            'summary' => $summary ?? [
                'totalQuotas' => 0,
                'totalOriginalTonnage' => 0,
                'totalLoadedTonnage' => 0,
                'totalRemainingTonnage' => 0,
                'overallPercentageLoaded' => 0
            ],
            'timestamp' => date('Y-m-d H:i:s')
        ];
        
    } catch (Exception $e) {
        throw new Exception('خطا در محاسبه مانده کوتاژها: ' . $e->getMessage());
    }
}

/**
 * محاسبه مانده تناژ برای یک کشتی خاص - نسخه فوق‌بهینه
 */
function getShipQuotasRemaining(DatabaseManager $db, string $shipName): array {
    try {
        $shipName = sanitizeInput($shipName);
        
        // کوئری فوق‌بهینه با CTE و محاسبات کامل در SQL + آمار کلی
        $query = "
        WITH ExitSummary AS (
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
        ),
        QuotaData AS (
            SELECT 
                i.shipName,
                i.loadingQuotaNumber as quotaNumber,
                i.shippingCompany,
                i.cargoOwner,
                i.loadingWarehouse as warehouse,
                i.cargoType,
                i.cargoWeight as totalTonnage,
                i.percentage,
                i.is_enabled as isPercentageEnabled,
                COALESCE(e.loadedTonnage, 0) as loadedTonnage,
                COALESCE(e.voucherCount, 0) as voucherCount,
                IF(i.is_enabled AND i.percentage > 0, 
                   i.cargoWeight * i.percentage * 0.01, 
                   0) as percentageAmount,
                IF(i.is_enabled AND i.percentage > 0,
                   i.cargoWeight * (1 - i.percentage * 0.01),
                   i.cargoWeight) as adjustedTonnage,
                GREATEST(0,
                    IF(i.is_enabled AND i.percentage > 0,
                       i.cargoWeight * (1 - i.percentage * 0.01),
                       i.cargoWeight) - COALESCE(e.loadedTonnage, 0)
                ) as remainingTonnage,
                IF(i.cargoWeight > 0,
                   ROUND((COALESCE(e.loadedTonnage, 0) / 
                          IF(i.is_enabled AND i.percentage > 0,
                             i.cargoWeight * (1 - i.percentage * 0.01),
                             i.cargoWeight)) * 100, 2),
                   0) as percentageLoaded
            FROM InitialInfo i
            LEFT JOIN ExitSummary e ON 
                e.loadingQuotaNumber = i.loadingQuotaNumber
                AND e.shipName = i.shipName
                AND e.loadingWarehouse = i.loadingWarehouse
                AND e.shippingCompany = i.shippingCompany
                AND e.cargoType = i.cargoType
            WHERE i.isActive = 1 AND i.shipName = ?
        )
        SELECT 
            q.*,
            (SELECT COUNT(*) FROM QuotaData) as totalQuotas,
            (SELECT SUM(totalTonnage) FROM QuotaData) as totalOriginalTonnage,
            (SELECT SUM(loadedTonnage) FROM QuotaData) as totalLoadedTonnage,
            (SELECT SUM(remainingTonnage) FROM QuotaData) as totalRemainingTonnage
        FROM QuotaData q
        ORDER BY q.quotaNumber
        ";
        
        $stmt = $db->prepare($query);
        $stmt->bind_param("ss", $shipName, $shipName);
        $stmt->execute();
        $result = $stmt->get_result();
        
        $quotasData = [];
        $summary = null;
        
        while ($row = $result->fetch_assoc()) {
            if ($summary === null) {
                $totalOriginal = (float)$row['totalOriginalTonnage'];
                $totalLoaded = (float)$row['totalLoadedTonnage'];
                
                $summary = [
                    'totalQuotas' => (int)$row['totalQuotas'],
                    'totalOriginalTonnage' => $totalOriginal,
                    'totalLoadedTonnage' => $totalLoaded,
                    'totalRemainingTonnage' => (float)$row['totalRemainingTonnage'],
                    'overallPercentageLoaded' => $totalOriginal > 0 
                        ? round($totalLoaded / $totalOriginal * 100, 2) 
                        : 0
                ];
            }
            
            $remainingTonnage = (float)$row['remainingTonnage'];
            $quotasData[] = [
                'shipName' => $row['shipName'],
                'quotaNumber' => (int)$row['quotaNumber'],
                'shippingCompany' => $row['shippingCompany'],
                'cargoOwner' => $row['cargoOwner'] ?? '',
                'warehouse' => $row['warehouse'],
                'cargoType' => $row['cargoType'],
                'totalTonnage' => (float)$row['totalTonnage'],
                'percentageAmount' => (float)$row['percentageAmount'],
                'percentage' => (float)$row['percentage'],
                'isPercentageEnabled' => (bool)$row['isPercentageEnabled'],
                'adjustedTotalTonnage' => (float)$row['adjustedTonnage'],
                'loadedTonnage' => (float)$row['loadedTonnage'],
                'remainingTonnage' => $remainingTonnage,
                'percentageLoaded' => (float)$row['percentageLoaded'],
                'voucherCount' => (int)$row['voucherCount'],
                'status' => $remainingTonnage > 0 ? 'دارای مانده' : 'تکمیل شده'
            ];
        }
        
        $stmt->close();
        
        return [
            'success' => true,
            'shipName' => $shipName,
            'data' => $quotasData,
            'summary' => $summary ?? [
                'totalQuotas' => 0,
                'totalOriginalTonnage' => 0,
                'totalLoadedTonnage' => 0,
                'totalRemainingTonnage' => 0,
                'overallPercentageLoaded' => 0
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
