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
/*         $timestamp = date('Y-m-d H:i:s');
        $logMessage = "[$timestamp] [$type] $message" . PHP_EOL;
        error_log($logMessage); // این خط را اضافه کنید
        file_put_contents($this->logFile, $logMessage, FILE_APPEND); */
    }
}

/**
 * کلاس DateConverter برای تبدیل تاریخ میلادی به شمسی.
 */
class DateConverter {
    public static function gregorianToJalali(int $gy, int $gm, int $gd): array {
        $g_d_m = [0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334];
        $gy2 = ($gm > 2) ? ($gy + 1) : $gy;
        $days = 355666 + (365 * $gy) + ((int)(($gy2 + 3) / 4)) 
                - ((int)(($gy2 + 99) / 100)) + ((int)(($gy2 + 399) / 400)) 
                + $gd + $g_d_m[$gm - 1];
        
        $jy = -1595 + (33 * intdiv($days, 12053));
        $days %= 12053;
        $jy += 4 * intdiv($days, 1461);
        $days %= 1461;
        
        if ($days > 365) {
            $jy += intdiv(($days - 1), 365);
            $days = ($days - 1) % 365;
        }
        
        if ($days < 186) {
            $jm = 1 + intdiv($days, 31);
            $jd = 1 + ($days % 31);
        } else {
            $jm = 7 + intdiv(($days - 186), 30);
            $jd = 1 + (($days - 186) % 30);
        }
        
        return [$jy, $jm, $jd];
    }

    public static function getCurrentJalaliDate(): string {
        [$jY, $jM, $jD] = self::gregorianToJalali(
            (int)date('Y'), 
            (int)date('m'), 
            (int)date('d')
        );
        return sprintf('%04d/%02d/%02d', $jY, $jM, $jD);
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
    private array $cache = [];
    private string $yesterdayJalaliDate;
    private string $todayJalaliDate;

    public function __construct(mysqli $conn) {
        $this->conn = $conn;
        $this->logger = new Logger();
        $this->setupConnection();
        $this->yesterdayJalaliDate = $this->getYesterdayJalaliDate();
        $this->todayJalaliDate = $this->getTodayJalaliDate();
    }

    private function setupConnection(): void {
        try {
            $this->conn->set_charset("utf8mb4");
            $this->conn->query("SET time_zone = '+03:30'");
            // تنظیم حالت SQL_BIG_SELECTS برای کوئری‌های پیچیده
            $this->conn->query("SET SESSION SQL_BIG_SELECTS=1");
            // افزایش مقدار group_concat_max_len برای گروه‌بندی‌های بزرگ
            $this->conn->query("SET SESSION group_concat_max_len=1000000");
            // تنظیم حالت بهینه‌سازی کوئری
            $this->conn->query("SET SESSION optimizer_search_depth=0");
            // افزایش زمان اجرای کوئری برای کوئری‌های پیچیده
            $this->conn->query("SET SESSION max_execution_time=30000");
            // تنظیم حافظه موقت برای عملیات مرتب‌سازی
            $this->conn->query("SET SESSION sort_buffer_size=1048576");
        } catch (Exception $e) {
            throw new Exception("Database connection setup failed: " . $e->getMessage());
        }
    }

    private function testDatabaseConnection(): void {
        try {
            $result = $this->conn->query("SELECT 1");
            if (!$result) {
                throw new Exception("Basic query failed");
            }
            
            $result = $this->conn->query("SELECT COUNT(*) FROM CargoInfo LIMIT 1");
            if (!$result) {
                throw new Exception("Cannot access CargoInfo table");
            }
            
            $this->logger->log(self::LOG_PREFIX . " - Database connection test passed successfully", 'INFO');
            
        } catch (Exception $e) {
            $this->logger->log(self::LOG_PREFIX . " - Database connection test failed: " . $e->getMessage(), 'ERROR');
            throw $e;
        }
    }

    public function getComprehensiveAnalysis(): array {
		$jsonFilePath = __DIR__ . '/daily_stats.json';
		$currentDate = date('Y-m-d');
		
		if (file_exists($jsonFilePath)) {
			$fileDate = date('Y-m-d', filemtime($jsonFilePath));
			if ($fileDate === $currentDate) {
				$jsonContent = file_get_contents($jsonFilePath);
				$decodedContent = json_decode($jsonContent, true);
				if ($decodedContent !== null) {
					$this->logger->log(self::LOG_PREFIX . " - Returning pre-calculated analysis from JSON file", 'INFO');
					return $decodedContent;
				}
			}
		}
		
		try {
			$this->testDatabaseConnection();
			$this->logger->log(self::LOG_PREFIX . " - Starting comprehensive analysis", 'INFO');
			
			$data = [
				'success' => true,
				'data' => [
					'peakHoursAnalysis' => $this->getPeakHoursAnalysis(),
					'shiftPerformanceAnalysis' => $this->getShiftPerformanceAnalysis(),
					'weekdayAnalysis' => $this->getWeekdayWorkloadAnalysis(),
					'weightVariationAnalysis' => $this->getWeightVariationAnalysis(),
					'carrierPerformanceAnalysis' => $this->getCarrierPerformanceAnalysis(),
					'quotaCompletionAnalysis' => $this->getQuotaCompletionAnalysis(),
					'quotaProgressAnalysis' => $this->getQuotaProgressAnalysis(),
					'quotaPredictionAnalysis' => $this->getQuotaPredictionAnalysis(),
					'carrierWeightAnalysis' => $this->getCarrierWeightAnalysis(),
					'carrierActivityPatterns' => $this->getCarrierActivityPatterns(),
					'warehouseEfficiencyAnalysis' => $this->getWarehouseEfficiencyAnalysis(),
					'warehouseSpeedComparison' => $this->getWarehouseSpeedComparison(),
					'warehouseTrafficPatterns' => $this->getWarehouseTrafficPatterns(),
                    'warehousePeakTimes' => $this->getWarehousePeakTimes(),
                    'cargoOwnerAnalysis' => $this->getCargoOwnerAnalysis()
				]
			];
			
			$this->logger->log(self::LOG_PREFIX . " - Analysis complete", 'INFO');
			
			if (file_put_contents($jsonFilePath, json_encode($data)) === false) {
				$this->logger->log(self::LOG_PREFIX . " - Failed to write analysis results to JSON file", 'WARNING');
			} else {
				$this->logger->log(self::LOG_PREFIX . " - Analysis results successfully written to JSON file", 'INFO');
			}
			
			return $data;
		} catch (Exception $e) {
			$this->logger->log(self::LOG_PREFIX . " - Analysis failed: " . $e->getMessage(), 'ERROR');
			throw $e;
		}
	}

    private function getPeakHoursAnalysis(): array {
        try {
            $query = "
                SELECT 
                    HOUR(entryTime) as hour,
                    COUNT(*) as total_operations,
                    COUNT(CASE WHEN status = 'ورود' THEN 1 END) as entries,
                    COUNT(CASE WHEN status = 'خروج' THEN 1 END) as exits,
                    ROUND(COUNT(*) * 100.0 / SUM(COUNT(*)) OVER (), 2) as percentage
                FROM CargoInfo
                WHERE 
                    (exitDate = ? AND exitTime >= '07:00:00') OR
                    (exitDate = ? AND exitTime < '07:00:00')
                GROUP BY HOUR(entryTime)
                ORDER BY total_operations DESC
            ";
    
            $stmt = $this->conn->prepare($query);
            if (!$stmt) {
                throw new Exception("Error preparing statement: " . $this->conn->error);
            }
    
            $stmt->bind_param('ss', $this->yesterdayJalaliDate, $this->todayJalaliDate);
    
            if (!$stmt->execute()) {
                throw new Exception("Error executing statement: " . $stmt->error);
            }
    
            $result = $stmt->get_result();
            if (!$result) {
                throw new Exception("Error getting results: " . $stmt->error);
            }
    
            $data = [];
            while ($row = $result->fetch_assoc()) {
                $data[] = [
                    'hour' => (int)$row['hour'],
                    'total_operations' => (int)$row['total_operations'],
                    'entries' => (int)$row['entries'],
                    'exits' => (int)$row['exits'],
                    'percentage' => (float)$row['percentage']
                ];
            }
    
            return $data;
    
        } catch (Exception $e) {
            $this->logger->log("Error in getPeakHoursAnalysis: " . $e->getMessage(), "ERROR");
            throw $e;
        }
    }

    private function getShiftPerformanceAnalysis(): array {
        try {
            $query = "
                WITH RangeStats AS (
                    SELECT
                        c.*,
                        CASE 
                            WHEN (c.exitDate = ? AND c.exitTime >= '07:00:00' AND c.exitTime < '19:00:00') THEN 'روز'
                            WHEN (c.exitDate = ? AND c.exitTime >= '19:00:00') OR 
                                 (c.exitDate = ? AND c.exitTime < '07:00:00') THEN 'شب'
                        END as shift,
                        HOUR(c.exitTime) as exit_hour,
                        CASE
                            WHEN TIME_TO_SEC(c.exitTime) < TIME_TO_SEC(c.entryTime) THEN
                                (TIME_TO_SEC(c.exitTime) + (24*3600 - TIME_TO_SEC(c.entryTime)))/60
                            ELSE 
                                TIME_TO_SEC(TIMEDIFF(c.exitTime, c.entryTime))/60
                        END as operation_minutes
                    FROM CargoInfo c
                    WHERE 
                        c.status = 'خروج'
                        AND (
                            (c.exitDate = ? AND c.exitTime >= '07:00:00' AND c.exitTime < '19:00:00') OR
                            (c.exitDate = ? AND c.exitTime >= '19:00:00') OR 
                            (c.exitDate = ? AND c.exitTime < '07:00:00')
                        )
                ),
                CarrierStats AS (
                    SELECT 
                        shift,
                        shippingCompany,
                        COUNT(DISTINCT loadingQuotaNumber) as quota_count,
                        COUNT(DISTINCT trackingNumber) as voucher_count,
                        SUM(netWeight) as total_weight
                    FROM RangeStats
                    GROUP BY shift, shippingCompany
                ),
                DelayedOperations AS (
                    SELECT 
                        shift,
                        loadingQuotaNumber,
                        shippingCompany,
                        trackingNumber,
                        operation_minutes
                    FROM RangeStats
                    WHERE operation_minutes > 60
                ),
                DelayedStats AS (
                    SELECT 
                        shift,
                        loadingQuotaNumber,
                        shippingCompany,
                        GROUP_CONCAT(DISTINCT trackingNumber ORDER BY trackingNumber) as tracking_numbers,
                        COUNT(DISTINCT trackingNumber) as delay_count,
                        ROUND(MAX(operation_minutes)/60, 1) as max_delay_hours
                    FROM DelayedOperations
                    GROUP BY shift, loadingQuotaNumber, shippingCompany
                ),
                HourlyStats AS (
                    SELECT 
                        shift,
                        exit_hour,
                        shippingCompany,
                        loadingQuotaNumber,
                        COUNT(DISTINCT trackingNumber) as voucher_count,
                        SUM(netWeight) as total_weight,
                        MIN(TIME_FORMAT(entryTime, '%H:%i')) as first_entry,
                        MAX(TIME_FORMAT(exitTime, '%H:%i')) as last_exit
                    FROM RangeStats
                    GROUP BY shift, exit_hour, shippingCompany, loadingQuotaNumber
                ),
                HourlyAggregated AS (
                    SELECT 
                        shift,
                        exit_hour,
                        COUNT(DISTINCT loadingQuotaNumber) as quota_count,
                        SUM(voucher_count) as voucher_count,
                        GROUP_CONCAT(
                            DISTINCT CONCAT(
                                shippingCompany, ':', 
                                loadingQuotaNumber, ':',
                                voucher_count, ':',
                                ROUND(total_weight), ':',
                                first_entry, '-', last_exit
                            )
                            ORDER BY total_weight DESC
                            SEPARATOR '|'
                        ) as hour_details
                    FROM HourlyStats
                    GROUP BY shift, exit_hour
                ),
                ShiftStats AS (
                    SELECT 
                        r.shift,
                        COUNT(DISTINCT r.loadingQuotaNumber) as total_operations,
                        COUNT(DISTINCT r.trackingNumber) as total_vouchers,
                        GROUP_CONCAT(
                            DISTINCT CONCAT(
                                c.shippingCompany, ':', 
                                c.quota_count, ':', 
                                c.voucher_count, ':', 
                                ROUND(c.total_weight)
                            )
                            ORDER BY c.voucher_count DESC, c.shippingCompany ASC
                            SEPARATOR ','
                        ) as active_carriers_list,
                        COUNT(DISTINCT r.shippingCompany) as total_active_carriers,
                        ROUND(AVG(r.operation_minutes), 2) as avg_completion_time,
                        COUNT(DISTINCT r.loadingQuotaNumber) as completed_operations,
                        SUM(r.netWeight) as total_weight_tons,
                        ROUND(SUM(r.netWeight) / COUNT(DISTINCT r.trackingNumber)) as avg_weight_per_operation,
                        GROUP_CONCAT(
                            DISTINCT CONCAT(
                                d.loadingQuotaNumber, ':', 
                                d.shippingCompany, ':', 
                                d.tracking_numbers, ':', 
                                d.max_delay_hours, ':',
                                d.delay_count
                            )
                            ORDER BY d.max_delay_hours DESC
                            SEPARATOR '|'
                        ) as delayed_operations_detail,
                        CAST(SUM(COALESCE(d.delay_count, 0)) as UNSIGNED) as total_delayed_operations,
                        ROUND(STDDEV(r.netWeight), 2) as weight_standard_deviation
                    FROM RangeStats r
                    LEFT JOIN CarrierStats c ON r.shift = c.shift AND r.shippingCompany = c.shippingCompany
                    LEFT JOIN DelayedStats d ON r.shift = d.shift AND r.loadingQuotaNumber = d.loadingQuotaNumber AND r.shippingCompany = d.shippingCompany
                    GROUP BY r.shift
                ),
                PeakHours AS (
                    SELECT 
                        h.*,
                        ROW_NUMBER() OVER (
                            PARTITION BY h.shift 
                            ORDER BY h.voucher_count DESC, h.quota_count DESC
                        ) as rn
                    FROM HourlyAggregated h
                )
                SELECT 
                    s.shift,
                    s.total_operations,
                    s.total_vouchers,
                    s.active_carriers_list,
                    s.avg_completion_time,
                    s.completed_operations,
                    s.total_weight_tons,
                    MAX(CASE WHEN p.rn = 1 THEN p.exit_hour END) as peak_hour,
                    MAX(CASE WHEN p.rn = 1 THEN p.quota_count END) as peak_hour_operations,
                    MAX(CASE WHEN p.rn = 1 THEN p.voucher_count END) as peak_hour_vouchers,
                    MAX(CASE WHEN p.rn = 1 THEN p.hour_details END) as peak_hour_detail,
                    s.avg_weight_per_operation,
                    s.delayed_operations_detail,
                    100.0 as completion_rate,
                    ROUND(
                        (
                            100.0 * 0.4 + 
                            (100 - LEAST(COALESCE(s.avg_completion_time, 0), 90) / 90 * 100) * 0.3 + 
                            (100 - COALESCE(s.total_delayed_operations * 100.0 / NULLIF(s.total_vouchers, 0), 0)) * 0.3 
                        ),
                        2
                    ) as efficiency_score,
                    s.total_active_carriers,
                    s.total_delayed_operations,
                    s.weight_standard_deviation
                FROM ShiftStats s
                LEFT JOIN PeakHours p ON s.shift = p.shift
                GROUP BY 
                    s.shift,
                    s.total_operations,
                    s.total_vouchers,
                    s.active_carriers_list,
                    s.avg_completion_time,
                    s.completed_operations,
                    s.total_weight_tons,
                    s.avg_weight_per_operation,
                    s.delayed_operations_detail,
                    s.total_active_carriers,
                    s.total_delayed_operations,
                    s.weight_standard_deviation
                ORDER BY 
                    CASE s.shift
                        WHEN 'روز' THEN 1
                        WHEN 'شب' THEN 2
                    END";
    
            $stmt = $this->conn->prepare($query);
            if (!$stmt) {
                throw new Exception("خطا در آماده‌سازی کوئری: " . $this->conn->error);
            }
    
            $stmt->bind_param('ssssss', 
                $this->yesterdayJalaliDate,
                $this->yesterdayJalaliDate,
                $this->todayJalaliDate,
                $this->yesterdayJalaliDate,
                $this->yesterdayJalaliDate,
                $this->todayJalaliDate
            );
    
            if (!$stmt->execute()) {
                throw new Exception("خطا در اجرای کوئری: " . $stmt->error);
            }
    
            $result = $stmt->get_result();
            if (!$result) {
                throw new Exception("خطا در دریافت نتایج: " . $stmt->error);
            }
    
            $data = [];
            while ($row = $result->fetch_assoc()) {
                $data[] = [
                    'shift' => $row['shift'],
                    'total_operations' => (int)$row['total_operations'],
                    'total_vouchers' => (int)$row['total_vouchers'],
                    'active_carriers_list' => $row['active_carriers_list'],
                    'avg_completion_time' => (float)$row['avg_completion_time'],
                    'completed_operations' => (int)$row['completed_operations'],
                    'total_weight_tons' => (float)$row['total_weight_tons'],
                    'peak_hour' => $row['peak_hour'] ? (int)$row['peak_hour'] : null,
                    'peak_hour_operations' => $row['peak_hour_operations'] ? (int)$row['peak_hour_operations'] : null,
                    'peak_hour_vouchers' => $row['peak_hour_vouchers'] ? (int)$row['peak_hour_vouchers'] : null,
                    'peak_hour_detail' => $row['peak_hour_detail'],
                    'avg_weight_per_operation' => (float)$row['avg_weight_per_operation'],
                    'delayed_operations_detail' => $row['delayed_operations_detail'],
                    'completion_rate' => (float)$row['completion_rate'],
                    'efficiency_score' => (float)$row['efficiency_score'],
                    'total_active_carriers' => (int)$row['total_active_carriers'],
                    'total_delayed_operations' => (int)$row['total_delayed_operations'],
                    'weight_standard_deviation' => (float)$row['weight_standard_deviation']
                ];
            }
    
            return $data;
    
        } catch (Exception $e) {
            $this->logger->log("خطا در getShiftPerformanceAnalysis: " . $e->getMessage(), "ERROR");
            throw $e;
        }
    }
    
    private function getWeekdayWorkloadAnalysis(): array {
        // بررسی کش
        $cacheKey = 'weekdayWorkloadAnalysis';
        if (isset($this->cache[$cacheKey])) {
            return $this->cache[$cacheKey];
        }
        
        try {
            $query = "
                SELECT 
                    DAYNAME(STR_TO_DATE(exitDate, '%Y/%m/%d')) as weekday,
                    COUNT(*) as total_operations,
                    ROUND(AVG(netWeight), 2) as avg_weight,
                    SUM(CASE WHEN status = 'خروج' THEN 1 ELSE 0 END) as completed_operations,
                    0 as workload_percentage
                FROM CargoInfo
                WHERE 
                    (exitDate = ? AND exitTime >= '07:00:00') OR
                    (exitDate = ? AND exitTime < '07:00:00')
                GROUP BY weekday
                ORDER BY total_operations DESC
            ";
    
            $result = $this->executeQuery($query, 'Weekday Workload Analysis', [$this->yesterdayJalaliDate, $this->todayJalaliDate]);
            
            // محاسبه درصد در PHP به جای SQL
            $totalOperations = array_sum(array_column($result, 'total_operations'));
            foreach ($result as &$row) {
                $row['workload_percentage'] = $totalOperations > 0 ? 
                    round(($row['total_operations'] * 100.0) / $totalOperations, 2) : 0;
            }
            
            // ذخیره در کش
            $this->cache[$cacheKey] = $result;
            return $result;
    
        } catch (Exception $e) {
            $this->logger->log("Error in getWeekdayWorkloadAnalysis: " . $e->getMessage(), "ERROR");
            throw $e;
        }
    }

    private function getWeightVariationAnalysis(): array {
        // بررسی کش
        $cacheKey = 'weightVariationAnalysis';
        if (isset($this->cache[$cacheKey])) {
            return $this->cache[$cacheKey];
        }
        
        try {
            $query = "
                SELECT 
                    loadingQuotaNumber,
                    shipName,
                    COUNT(*) as total_vouchers,
                    SUM(CASE WHEN shortageWeight > 0 THEN 1 ELSE 0 END) as shortage_count,
                    SUM(CASE WHEN excessWeight > 0 THEN 1 ELSE 0 END) as excess_count,
                    ROUND(AVG(shortageWeight), 2) as avg_shortage,
                    ROUND(AVG(excessWeight), 2) as avg_excess,
                    ROUND(AVG(CASE WHEN netWeight > 0 THEN netWeight END), 2) as avg_net_weight
                FROM CargoInfo
                WHERE 
                    status = 'خروج'
                    AND (
                        (exitDate = ? AND exitTime >= '07:00:00') OR
                        (exitDate = ? AND exitTime < '07:00:00')
                    )
                GROUP BY loadingQuotaNumber, shipName
                HAVING shortage_count > 0 OR excess_count > 0
                ORDER BY (shortage_count + excess_count) DESC
            ";
    
            $data = $this->executeQuery($query, 'Weight Variation Analysis', [$this->yesterdayJalaliDate, $this->todayJalaliDate]);
            
            // ذخیره در کش
            $this->cache[$cacheKey] = $data;
            return $data;
    
        } catch (Exception $e) {
            $this->logger->log("Error in getWeightVariationAnalysis: " . $e->getMessage(), "ERROR");
            throw $e;
        }
    }

    private function getCarrierPerformanceAnalysis(): array {
        // بررسی کش
        $cacheKey = 'carrierPerformanceAnalysis';
        if (isset($this->cache[$cacheKey])) {
            return $this->cache[$cacheKey];
        }
        
        try {
            $this->logger->log(self::LOG_PREFIX . " - Starting carrier performance analysis", "INFO");
            $this->logger->log(self::LOG_PREFIX . " - Analyzing data for date range: " . $this->yesterdayJalaliDate . " to " . $this->todayJalaliDate, "DEBUG");
            
            // تقسیم کوئری پیچیده به چند کوئری ساده‌تر
            
            // کوئری 1: اطلاعات پایه حامل‌ها
            $baseQuery = "
                    SELECT 
                        shippingCompany,
                    loadingQuotaNumber,
                    trackingNumber,
                    netWeight,
                    entryTime,
                    exitTime
                    FROM CargoInfo
                    WHERE 
                    status = 'خروج'
                    AND (
                        (exitDate = ? AND exitTime >= '07:00:00') OR
                        (exitDate = ? AND exitTime < '07:00:00')
                    )
            ";
            
            $baseData = $this->executeQuery($baseQuery, 'Carrier Performance Base Data', [$this->yesterdayJalaliDate, $this->todayJalaliDate]);
            
            // پردازش داده‌ها در PHP به جای SQL
            $carriers = [];
            $carrierQuotas = [];
            $carrierVouchers = [];
            $carrierHours = [];
            
            foreach ($baseData as $row) {
                $company = $row['shippingCompany'];
                $quota = $row['loadingQuotaNumber'];
                $voucher = $row['trackingNumber'];
                $hour = (int)date('G', strtotime($row['entryTime']));
                
                // ثبت شرکت حمل و نقل
                if (!isset($carriers[$company])) {
                    $carriers[$company] = [
                        'total_deliveries' => 0,
                        'completed_deliveries' => 0,
                        'total_quotas' => 0,
                        'completed_quotas' => 0,
                        'completed_quota_numbers' => [],
                        'avg_net_weight' => 0,
                        'min_weight' => PHP_FLOAT_MAX,
                        'max_weight' => 0,
                        'total_weight' => 0,
                        'weight_count' => 0,
                        'peak_hour' => null,
                        'peak_hour_operations' => 0,
                        'hour_operations' => []
                    ];
                }
                
                // ثبت کوتاژ
                if (!isset($carrierQuotas[$company][$quota])) {
                    $carrierQuotas[$company][$quota] = true;
                    $carriers[$company]['total_quotas']++;
                    $carriers[$company]['completed_quotas']++;
                    $carriers[$company]['completed_quota_numbers'][] = $quota;
                }
                
                // ثبت بارنامه
                if (!isset($carrierVouchers[$company][$voucher])) {
                    $carrierVouchers[$company][$voucher] = true;
                    $carriers[$company]['total_deliveries']++;
                    $carriers[$company]['completed_deliveries']++;
                }
                
                // ثبت وزن
                $weight = (float)$row['netWeight'];
                $carriers[$company]['total_weight'] += $weight;
                $carriers[$company]['weight_count']++;
                $carriers[$company]['min_weight'] = min($carriers[$company]['min_weight'], $weight);
                $carriers[$company]['max_weight'] = max($carriers[$company]['max_weight'], $weight);
                
                // ثبت ساعت فعالیت
                if (!isset($carrierHours[$company][$hour])) {
                    $carrierHours[$company][$hour] = 0;
                }
                $carrierHours[$company][$hour]++;
                $carriers[$company]['hour_operations'][$hour] = $carrierHours[$company][$hour];
                
                // بررسی ساعت اوج
                if ($carrierHours[$company][$hour] > $carriers[$company]['peak_hour_operations']) {
                    $carriers[$company]['peak_hour'] = $hour;
                    $carriers[$company]['peak_hour_operations'] = $carrierHours[$company][$hour];
                }
            }
            
            // محاسبه میانگین وزن و امتیاز کیفیت
            $result = [];
            foreach ($carriers as $company => $data) {
                if ($data['weight_count'] > 0) {
                    $data['avg_net_weight'] = round($data['total_weight'] / $data['weight_count'], 2);
                }
                
                // محاسبه امتیاز کیفیت
                $qualityScore = 0;
                if ($data['peak_hour_operations'] >= 15) {
                    $qualityScore = 90.0;
                    $performanceCategory = 'بسیار عالی';
                } elseif ($data['peak_hour_operations'] >= 12) {
                    $qualityScore = 80.0;
                    $performanceCategory = 'عالی';
                } elseif ($data['peak_hour_operations'] >= 10) {
                    $qualityScore = 70.0;
                    $performanceCategory = 'خوب';
                } elseif ($data['peak_hour_operations'] >= 7) {
                    $qualityScore = 60.0;
                    $performanceCategory = 'خوب متوسط';
                } elseif ($data['peak_hour_operations'] >= 4) {
                    $qualityScore = 50.0;
                    $performanceCategory = 'متوسط';
                } elseif ($data['peak_hour_operations'] >= 2) {
                    $qualityScore = 40.0;
                    $performanceCategory = 'ضعیف';
                } else {
                    $qualityScore = 20.0;
                    $performanceCategory = 'بیش از حد ضعیف';
                }
                
                $result[] = [
                    'shippingCompany' => $company,
                    'total_deliveries' => $data['total_deliveries'],
                    'completed_deliveries' => $data['completed_deliveries'],
                    'total_quotas' => $data['total_quotas'],
                    'completed_quotas' => $data['completed_quotas'],
                    'completed_quota_numbers' => implode(',', $data['completed_quota_numbers']),
                    'avg_net_weight' => $data['avg_net_weight'],
                    'min_weight' => $data['min_weight'] == PHP_FLOAT_MAX ? 0 : $data['min_weight'],
                    'max_weight' => $data['max_weight'],
                    'peak_hour' => $data['peak_hour'],
                    'peak_hour_operations' => $data['peak_hour_operations'],
                    'quality_score' => $qualityScore,
                    'performance_category' => $performanceCategory
                ];
            }
            
            // مرتب‌سازی بر اساس امتیاز کیفیت
            usort($result, function($a, $b) {
                if ($a['quality_score'] != $b['quality_score']) {
                    return $b['quality_score'] - $a['quality_score'];
                }
                return $b['peak_hour_operations'] - $a['peak_hour_operations'];
            });
            
            $this->logger->log(self::LOG_PREFIX . " - Found " . count($result) . " active carriers", "INFO");
            
            // ذخیره در کش
            $this->cache[$cacheKey] = $result;
            return $result;
    
        } catch (Exception $e) {
            $this->logger->log(self::LOG_PREFIX . " - Error in carrier analysis: " . $e->getMessage(), "ERROR");
            throw $e;
        } finally {
            $this->logger->log(self::LOG_PREFIX . " - Carrier performance analysis completed", "INFO");
        }
    }
    
    private function getQuotaCompletionAnalysis(): array {
        try {
            $yesterdayJalaliDate = $this->getYesterdayJalaliDate();
            $todayJalaliDate = $this->getTodayJalaliDate();
    
            $query = "
                SELECT 
                    c.loadingQuotaNumber,
                    i.cargoWeight AS total_quota_weight,
                    COUNT(*) AS total_vouchers,
                    ROUND(SUM(c.netWeight) * 100.0 / LEAST(i.cargoWeight, 3000000), 2) AS completion_percentage_raw,
                    ROUND(
                        LEAST(SUM(c.netWeight) * 100.0 / LEAST(i.cargoWeight, 3000000), 100.0),
                        2
                    ) AS completion_percentage,
                    ROUND(
                        AVG(
                            CASE 
                                WHEN c.status = 'خروج' AND c.exitTime IS NOT NULL AND c.entryTime IS NOT NULL THEN 
                                    CASE
                                        WHEN TIME_TO_SEC(c.exitTime) < TIME_TO_SEC(c.entryTime) THEN
                                            (TIME_TO_SEC(c.exitTime) + (24*3600 - TIME_TO_SEC(c.entryTime))) / 3600
                                        ELSE 
                                            TIME_TO_SEC(TIMEDIFF(c.exitTime, c.entryTime)) / 3600
                                    END
                                ELSE NULL
                            END
                        ),
                    2) AS avg_completion_hours,
                    i.shipName,
                    c.shippingCompany,
                    SUM(c.netWeight) AS last_24h_weight,
                    COUNT(*) AS last_24h_vouchers
                FROM CargoInfo c
                JOIN InitialInfo i ON c.loadingQuotaNumber = i.loadingQuotaNumber
                WHERE 
                    c.status = 'خروج' 
                    AND (
                        (c.exitDate = ? AND c.exitTime >= '07:00:00') OR
                        (c.exitDate = ? AND c.exitTime < '07:00:00')
                    )
                GROUP BY c.loadingQuotaNumber, i.cargoWeight, i.shipName, c.shippingCompany
                ORDER BY completion_percentage DESC";
    
            $stmt = $this->conn->prepare($query);
            if (!$stmt) {
                throw new Exception("Error preparing statement: " . $this->conn->error);
            }
    
            $stmt->bind_param("ss", $yesterdayJalaliDate, $todayJalaliDate);
            if (!$stmt->execute()) {
                throw new Exception("Error executing statement: " . $stmt->error);
            }
    
            $result = $stmt->get_result();
            $data = [];
    
            while ($row = $result->fetch_assoc()) {
                // محدود کردن completion_percentage به 100 درصد
                $completion_percentage = min((float)$row['completion_percentage'], 100.00);
    
                $processedRow = [
                    'loadingQuotaNumber' => $row['loadingQuotaNumber'],
                    'total_quota_weight' => (float)$row['total_quota_weight'],
                    'total_vouchers' => (int)$row['total_vouchers'],
                    'completion_percentage' => $completion_percentage,
                    'avg_completion_hours' => is_null($row['avg_completion_hours']) ? 0 : (float)$row['avg_completion_hours'],
                    'shipName' => $row['shipName'],
                    'shippingCompany' => $row['shippingCompany'],
                    'last_24h_weight' => (float)$row['last_24h_weight'],
                    'last_24h_vouchers' => (int)$row['last_24h_vouchers'],
                ];
    
                $this->logger->log(
                    "Quota {$processedRow['loadingQuotaNumber']} completion: {$processedRow['completion_percentage']}%",
                    "DEBUG"
                );
    
                $data[] = $processedRow;
            }
    
            $stmt->close();
    
            $this->logger->log("Processed " . count($data) . " quota completion records", "INFO");
    
            return $data;
    
        } catch (Exception $e) {
            $this->logger->log("Error in getQuotaCompletionAnalysis: " . $e->getMessage(), "ERROR");
            throw $e;
        }
    }
    
    private function getQuotaProgressAnalysis(): array {
        try {
            $yesterdayJalaliDate = $this->getYesterdayJalaliDate();
            $todayJalaliDate = $this->getTodayJalaliDate();

            $query = "
                SELECT 
                    c.loadingQuotaNumber,
                    c.exitDate,
                    COUNT(*) as daily_vouchers,
                    ROUND(SUM(c.netWeight), 2) as daily_weight,
                    ROUND(SUM(SUM(c.netWeight)) OVER (
                        PARTITION BY c.loadingQuotaNumber 
                        ORDER BY c.exitDate
                    ) * 100.0 / MAX(i.cargoWeight), 2) as cumulative_completion
                FROM CargoInfo c
                JOIN InitialInfo i ON c.loadingQuotaNumber = i.loadingQuotaNumber
                WHERE 
                    c.status = 'خروج'
                    AND (
                        (c.exitDate = ? AND c.exitTime >= '07:00:00') OR
                        (c.exitDate = ? AND c.exitTime < '07:00:00')
                    )
                GROUP BY c.loadingQuotaNumber, c.exitDate
                ORDER BY c.loadingQuotaNumber, c.exitDate
            ";

            return $this->executeQuery($query, 'Quota Progress Analysis', [$yesterdayJalaliDate, $todayJalaliDate]);
        } catch (Exception $e) {
            $this->logger->log("Error in getQuotaProgressAnalysis: " . $e->getMessage(), "ERROR");
            throw $e;
        }
    }

    private function getQuotaPredictionAnalysis(): array {
        try {
            $yesterdayJalaliDate = $this->getYesterdayJalaliDate();
            $todayJalaliDate = $this->getTodayJalaliDate();

            $query = "
                WITH QuotaRates AS (
                    SELECT 
                        c.loadingQuotaNumber,
                        SUM(c.netWeight) as total_completed_weight,
                        COUNT(DISTINCT c.exitDate) as active_days,
                        ROUND(SUM(c.netWeight) / COUNT(DISTINCT c.exitDate), 2) as daily_rate,
                        MAX(i.cargoWeight) as total_weight
                    FROM CargoInfo c
                    JOIN InitialInfo i ON c.loadingQuotaNumber = i.loadingQuotaNumber
                    WHERE 
                        c.status = 'خروج'
                        AND (
                            (c.exitDate = ? AND c.exitTime >= '07:00:00') OR
                            (c.exitDate = ? AND c.exitTime < '07:00:00')
                        )
                    GROUP BY c.loadingQuotaNumber
                )
                SELECT 
                    loadingQuotaNumber,
                    daily_rate,
                    total_completed_weight,
                    total_weight,
                    ROUND((total_weight - total_completed_weight) / NULLIF(daily_rate, 0), 1) as estimated_days_remaining
                FROM QuotaRates
                WHERE total_completed_weight < total_weight
                ORDER BY estimated_days_remaining
            ";

            return $this->executeQuery($query, 'Quota Prediction Analysis', [$yesterdayJalaliDate, $todayJalaliDate]);
        } catch (Exception $e) {
            $this->logger->log("Error in getQuotaPredictionAnalysis: " . $e->getMessage(), "ERROR");
            throw $e;
        }
    }

    private function getCarrierWeightAnalysis(): array {
        try {
            $yesterdayJalaliDate = $this->getYesterdayJalaliDate();
            $todayJalaliDate = $this->getTodayJalaliDate();

            $query = "
                SELECT 
                    shippingCompany,
                    COUNT(*) as total_deliveries,
                    ROUND(AVG(netWeight), 2) as avg_weight,
                    ROUND(MIN(netWeight), 2) as min_weight,
                    ROUND(MAX(netWeight), 2) as max_weight,
                    ROUND(STDDEV(netWeight), 2) as weight_std_dev,
                    ROUND(SUM(netWeight), 2) as total_weight
                FROM CargoInfo
                WHERE 
                    status = 'خروج'
                    AND (
                        (exitDate = ? AND exitTime >= '07:00:00') OR
                        (exitDate = ? AND exitTime < '07:00:00')
                    )
                GROUP BY shippingCompany
                ORDER BY avg_weight DESC
            ";

            return $this->executeQuery($query, 'Carrier Weight Analysis', [$yesterdayJalaliDate, $todayJalaliDate]);
        } catch (Exception $e) {
            $this->logger->log("Error in getCarrierWeightAnalysis: " . $e->getMessage(), "ERROR");
            throw $e;
        }
    }

    private function getCarrierActivityPatterns(): array {
        try {
            $yesterdayJalaliDate = $this->getYesterdayJalaliDate();
            $todayJalaliDate = $this->getTodayJalaliDate();
     
            $query = "
                WITH ActivityStats AS (
                    SELECT 
                        shippingCompany,
                        HOUR(entryTime) as hour,
                        COUNT(*) as operation_count,
                        COUNT(CASE WHEN status = 'خروج' THEN 1 END) as completed_operations,
                        ROUND(AVG(CASE 
                            WHEN status = 'خروج' 
                            THEN TIME_TO_SEC(TIMEDIFF(exitTime, entryTime))/60 
                        END), 2) as avg_completion_minutes,
                        COUNT(*) * 100.0 / SUM(COUNT(*)) OVER (
                            PARTITION BY shippingCompany
                        ) as percentage
                    FROM CargoInfo c
                    WHERE 
                        status = 'خروج'
                        AND (
                            (exitDate = ? AND exitTime >= '07:00:00') OR
                            (exitDate = ? AND exitTime < '07:00:00')
                        )
                    GROUP BY shippingCompany, HOUR(entryTime)
                )
                SELECT 
                    shippingCompany,
                    hour,
                    operation_count,
                    ROUND(percentage, 2) as hour_percentage,
                    completed_operations,
                    avg_completion_minutes
                FROM ActivityStats
                ORDER BY shippingCompany, operation_count DESC
            ";
     
            return $this->executeQuery($query, 'Carrier Activity Patterns', [$yesterdayJalaliDate, $todayJalaliDate]);
        } catch (Exception $e) {
            $this->logger->log("Error in getCarrierActivityPatterns: " . $e->getMessage(), "ERROR");
            throw $e;
        }
    }

    private function getWarehouseEfficiencyAnalysis(): array {
        try {
            $yesterdayJalaliDate = $this->getYesterdayJalaliDate();
            $todayJalaliDate = $this->getTodayJalaliDate();

            $query = "
                WITH WarehouseStats AS (
                    SELECT 
                        i.loadingWarehouse,
                        COUNT(DISTINCT c.loadingQuotaNumber) as active_quotas,
                        COUNT(*) as total_operations,
                        SUM(CASE WHEN c.status = 'خروج' THEN c.netWeight ELSE 0 END) as total_processed_weight,
                        COUNT(DISTINCT c.exitDate) as active_days
                    FROM InitialInfo i
                    LEFT JOIN CargoInfo c ON i.loadingQuotaNumber = c.loadingQuotaNumber
                    WHERE 
                        i.isActive = 1
                        AND (
                            (c.exitDate = ? AND c.exitTime >= '07:00:00') OR
                            (c.exitDate = ? AND c.exitTime < '07:00:00')
                        )
                    GROUP BY i.loadingWarehouse
                )
                SELECT 
                    loadingWarehouse,
                    active_quotas,
                    total_operations,
                    ROUND(total_processed_weight, 2) as total_processed_weight,
                    ROUND(total_processed_weight / NULLIF(active_days, 0), 2) as daily_throughput,
                    ROUND(total_operations / NULLIF(active_days, 0), 2) as daily_operations,
                    ROUND(total_operations * 100.0 / SUM(total_operations) OVER (), 2) as operation_percentage
                FROM WarehouseStats
                ORDER BY daily_throughput DESC
            ";

            return $this->executeQuery($query, 'Warehouse Efficiency Analysis', [$yesterdayJalaliDate, $todayJalaliDate]);
        } catch (Exception $e) {
            $this->logger->log("Error in getWarehouseEfficiencyAnalysis: " . $e->getMessage(), "ERROR");
            throw $e;
        }
    }

    private function getWarehouseSpeedComparison(): array {
        try {
            $yesterdayJalaliDate = $this->getYesterdayJalaliDate();
            $todayJalaliDate = $this->getTodayJalaliDate();

            $query = "
                WITH WarehouseSpeed AS (
                    SELECT 
                        i.loadingWarehouse,
                        COUNT(*) as total_operations,
                        ROUND(AVG(CASE 
                            WHEN c.status = 'خروج' 
                            THEN TIME_TO_SEC(TIMEDIFF(c.exitTime, c.entryTime))/60 
                        END), 2) as avg_processing_minutes,
                        COUNT(CASE WHEN c.status = 'خروج' THEN 1 END) as completed_operations,
                        ROUND(AVG(CASE WHEN c.status = 'خروج' THEN c.netWeight END), 2) as avg_processed_weight
                    FROM InitialInfo i
                    JOIN CargoInfo c ON i.loadingQuotaNumber = c.loadingQuotaNumber
                    WHERE 
                        (c.exitDate = ? AND c.exitTime >= '07:00:00') OR
                        (c.exitDate = ? AND c.exitTime < '07:00:00')
                    GROUP BY i.loadingWarehouse
                )
                SELECT 
                    *,
                    ROUND(completed_operations * 100.0 / NULLIF(total_operations,0), 2) as completion_rate,
                    ROUND(avg_processed_weight / NULLIF(avg_processing_minutes, 0), 2) as weight_per_minute
                FROM WarehouseSpeed
                ORDER BY weight_per_minute DESC
            ";

            return $this->executeQuery($query, 'Warehouse Speed Comparison', [$yesterdayJalaliDate, $todayJalaliDate]);
        } catch (Exception $e) {
            $this->logger->log("Error in getWarehouseSpeedComparison: " . $e->getMessage(), "ERROR");
            throw $e;
        }
    }

    private function getWarehouseTrafficPatterns(): array {
        try {
            $yesterdayJalaliDate = $this->getYesterdayJalaliDate();
            $todayJalaliDate = $this->getTodayJalaliDate();

            $query = "
                SELECT 
                    i.loadingWarehouse,
                    HOUR(c.entryTime) as hour,
                    COUNT(CASE WHEN c.status = 'ورود' THEN 1 END) as entries,
                    COUNT(CASE WHEN c.status = 'خروج' THEN 1 END) as exits,
                    ROUND(COUNT(*) * 100.0 / SUM(COUNT(*)) OVER (
                        PARTITION BY i.loadingWarehouse
                    ), 2) as hour_percentage,
                    ROUND(AVG(CASE 
                        WHEN c.status = 'خروج' 
                        THEN c.netWeight 
                    END), 2) as avg_processed_weight
                FROM InitialInfo i
                JOIN CargoInfo c ON i.loadingQuotaNumber = c.loadingQuotaNumber
                WHERE 
                    (c.exitDate = ? AND c.exitTime >= '07:00:00') OR
                    (c.exitDate = ? AND c.exitTime < '07:00:00')
                GROUP BY i.loadingWarehouse, HOUR(c.entryTime)
                ORDER BY i.loadingWarehouse, hour
            ";

            return $this->executeQuery($query, 'Warehouse Traffic Patterns', [$yesterdayJalaliDate, $todayJalaliDate]);
        } catch (Exception $e) {
            $this->logger->log("Error in getWarehouseTrafficPatterns: " . $e->getMessage(), "ERROR");
            throw $e;
        }
    }

    private function getWarehousePeakTimes(): array {
        try {
            $yesterdayJalaliDate = $this->getYesterdayJalaliDate();
            $todayJalaliDate = $this->getTodayJalaliDate();

            $query = "
                WITH HourlyStats AS (
                    SELECT 
                        i.loadingWarehouse,
                        HOUR(c.entryTime) as hour,
                        COUNT(*) as operation_count,
                        ROUND(AVG(CASE 
                            WHEN c.status = 'خروج' 
                            THEN c.netWeight 
                        END), 2) as avg_weight,
                        ROUND(COUNT(*) * 100.0 / SUM(COUNT(*)) OVER (
                            PARTITION BY i.loadingWarehouse
                        ), 2) as period_percentage
                    FROM InitialInfo i
                    JOIN CargoInfo c ON i.loadingQuotaNumber = c.loadingQuotaNumber
                    WHERE 
                        (c.exitDate = ? AND c.exitTime >= '07:00:00') OR
                        (c.exitDate = ? AND c.exitTime < '07:00:00')
                    GROUP BY i.loadingWarehouse, HOUR(c.entryTime)
                )
                SELECT 
                    loadingWarehouse,
                    hour,
                    operation_count,
                    avg_weight,
                    period_percentage,
                    CASE 
                        WHEN period_percentage >= 75 THEN 'اوج شدید'
                        WHEN period_percentage >= 50 THEN 'اوج متوسط'
                        WHEN period_percentage >= 25 THEN 'معمولی'
                        ELSE 'کم‌فعالیت'
                    END as activity_level
                FROM HourlyStats
                ORDER BY loadingWarehouse, period_percentage DESC
            ";

            return $this->executeQuery($query, 'Warehouse Peak Times', [$yesterdayJalaliDate, $todayJalaliDate]);
        } catch (Exception $e) {
            $this->logger->log("Error in getWarehousePeakTimes: " . $e->getMessage(), "ERROR");
            throw $e;
        }
    }

    private function getCargoOwnerAnalysis(): array {
        try {
            // تاریخ شروع برای 24 ساعت گذشته
            $yesterdayDate = $this->getYesterdayJalaliDate();
            $todayDate = $this->getTodayJalaliDate();
            
            $query = "
                WITH ShipOwnerData AS (
                    SELECT 
                        i.shipName,
                        i.cargoOwner,
                        COUNT(DISTINCT c.id) AS voucher_count,
                        SUM(CASE WHEN c.status = 'خروج' THEN c.netWeight ELSE 0 END) AS net_weight,
                        COUNT(DISTINCT i.loadingQuotaNumber) AS quota_count
                    FROM InitialInfo i
                    LEFT JOIN CargoInfo c ON i.loadingQuotaNumber = c.loadingQuotaNumber
                    WHERE 
                        i.cargoOwner IS NOT NULL AND i.cargoOwner != '' 
                        AND (
                            (c.exitDate = ? AND c.exitTime >= '07:00:00') OR
                            (c.exitDate = ? AND c.exitTime < '07:00:00')
                        )
                    GROUP BY i.shipName, i.cargoOwner
                ),
                ShipSummary AS (
                    SELECT 
                        shipName,
                        COUNT(DISTINCT cargoOwner) AS owner_count,
                        SUM(voucher_count) AS total_vouchers,
                        SUM(net_weight) AS total_net_weight
                    FROM ShipOwnerData
                    GROUP BY shipName
                )
                SELECT 
                    s.shipName,
                    s.owner_count,
                    s.total_vouchers,
                    s.total_net_weight,
                    JSON_ARRAYAGG(
                        JSON_OBJECT(
                            'cargoOwner', d.cargoOwner,
                            'voucher_count', d.voucher_count,
                            'net_weight', d.net_weight,
                            'quota_count', d.quota_count
                        )
                    ) as owners_data
                FROM ShipSummary s
                JOIN ShipOwnerData d ON s.shipName = d.shipName
                GROUP BY s.shipName, s.owner_count, s.total_vouchers, s.total_net_weight
                ORDER BY s.total_net_weight DESC
            ";

            $params = [$yesterdayDate, $todayDate];
            $result = $this->executeQuery($query, 'getCargoOwnerAnalysis', $params);
            
            $data = [];
            foreach ($result as $row) {
                $ownersData = json_decode($row['owners_data'], true);
                
                foreach ($ownersData as &$owner) {
                    $owner['net_weight'] = (float)$owner['net_weight'];
                    $owner['voucher_count'] = (int)$owner['voucher_count'];
                    $owner['quota_count'] = (int)$owner['quota_count'];
                }
                
                $data[] = [
                    'shipName' => $row['shipName'],
                    'owner_count' => (int)$row['owner_count'],
                    'total_vouchers' => (int)$row['total_vouchers'],
                    'total_net_weight' => (float)$row['total_net_weight'],
                    'owners' => $ownersData
                ];
            }
            
            return $data;
        } catch (Exception $e) {
            $this->logger->log("Error in getCargoOwnerAnalysis: " . $e->getMessage(), "ERROR");
            throw $e;
        }
    }

    private function executeQuery(string $query, string $context, array $params = []): array {
        try {
            $startTime = microtime(true);
            $this->logger->log(self::LOG_PREFIX . " - Executing query for $context", 'DEBUG');
    
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
                $data[] = array_map(function($value) {
		if (is_numeric($value)) {
			if (is_string($value) && strpos($value, '.') !== false) {
				return (float)$value;
			}
			return (int)$value;
		}
		return $value;
	}, $row);
            }
    
            $this->logger->log(self::LOG_PREFIX . " - Query results for $context: " . json_encode($data), 'DEBUG');
    
            $executionTime = microtime(true) - $startTime;
            $this->logger->log(
                self::LOG_PREFIX . " - Query for $context completed in " . 
                number_format($executionTime, 4) . " seconds",
                'DEBUG'
            );
    
            return $data;
    
        } catch (Exception $e) {
            $this->logger->log(self::LOG_PREFIX . " - Error in $context: " . $e->getMessage(), 'ERROR');
            throw new Exception("Query execution failed in $context: " . $e->getMessage());
        }
    }

    private function getYesterdayJalaliDate(): string {
        $yesterdayTimestamp = strtotime('-1 day');
        [$yesterdayJY, $yesterdayJM, $yesterdayJD] = DateConverter::gregorianToJalali(
            (int)date('Y', $yesterdayTimestamp),
            (int)date('m', $yesterdayTimestamp),
            (int)date('d', $yesterdayTimestamp)
        );
        return sprintf('%04d/%02d/%02d', $yesterdayJY, $yesterdayJM, $yesterdayJD);
    }

    private function getTodayJalaliDate(): string {
        $todayTimestamp = time();
        [$todayJY, $todayJM, $todayJD] = DateConverter::gregorianToJalali(
            (int)date('Y', $todayTimestamp),
            (int)date('m', $todayTimestamp),
            (int)date('d', $todayTimestamp)
        );
        return sprintf('%04d/%02d/%02d', $todayJY, $todayJM, $todayJD);
    }
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
        if (defined('IS_CLI') || isset($_GET['action'])) {
            switch ($_GET['action']) {
                case 'getRealTimeData':
                    $this->handleRealTimeDataRequest();
                    break;
                case 'getComprehensiveAnalysis':
                    $this->handleComprehensiveAnalysisRequest();
                    break;
                case 'generateDailyStats':
                    $this->generateDailyStats();
                    break;
                default:
                    throw new APIException('عملیات درخواستی نامعتبر است');
            }
        } else {
            throw new APIException('عملیات نامعتبر یا عدم تعیین عملیات');
        }
    } catch (APIException $e) {
        APIResponse::send(['error' => $e->getMessage()], 400);
    } catch (Exception $e) {
        $this->logger->log($e->getMessage(), 'ERROR');
        APIResponse::send(['error' => 'خطای داخلی سرور'], 500);
    }
}

private function generateDailyStats(): void {
    $analytics = new AdvancedCargoAnalytics($this->conn);
    $dailyStats = $analytics->getComprehensiveAnalysis();
    
    $jsonFilePath = __DIR__ . '/daily_stats.json';
    
    // حذف محتوای فایل قبلی (در صورت وجود) و نوشتن اطلاعات جدید
    file_put_contents($jsonFilePath, json_encode($dailyStats, JSON_PRETTY_PRINT));
    
    echo "آمار روزانه با موفقیت محاسبه و ذخیره شد.\n";
    
    // ثبت لاگ برای پیگیری‌های آینده
    $this->logger->log("آمار روزانه در تاریخ " . date('Y-m-d H:i:s') . " به‌روزرسانی شد.", 'INFO');
}

	private function validateRequestMethod(): void {
		if (!defined('IS_CLI') && $_SERVER['REQUEST_METHOD'] !== 'GET') {
			throw new APIException('روش درخواست نامعتبر است.');
		}
	}

    private function handleKotazhRequest(): void {
        $kotazh = $this->validateKotazh($_GET['kotazh']);
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
        $currentTime = date('H:i:s');
        $shiftInfo = $this->determineShiftInfo($currentTime);
        $realTimeData = $this->getRealTimeData($shiftInfo);

        // ثبت لاگ برای اطلاع از تعداد رکوردهای نهایی پاسخ
        $this->logger->log("تعداد رکوردهای نهایی ارسالی در پاسخ: " . count($realTimeData), "INFO");

        APIResponse::send([
مشکل             'success' => true,
            'shiftInfo' => $shiftInfo,
            'data' => $realTimeData
        ]);
    }

    private function determineShiftInfo(string $currentTime): array {
        $currentJalaliDate = DateConverter::getCurrentJalaliDate();
        $shiftType = '';
        $shiftInfo = [];

        if ($currentTime >= '07:30:00' && $currentTime < '19:00:00') {
            // شیفت روز
            $shiftType = 'روز';
            $shiftInfo = [
                'startDate' => $currentJalaliDate,
                'endDate' => $currentJalaliDate,
                'startTime' => '07:30:00',
                'endTime' => '18:30:00',
                'type' => $shiftType
            ];
        } else {
            // شیفت شب
            $shiftType = 'شب';
            if ($currentTime >= '00:00:00' && $currentTime < '07:30:00') {
                // شیفت شب از روز قبل تا کنونی
                $prevDateTimestamp = strtotime('-1 day');
                [$prevJY, $prevJM, $prevJD] = DateConverter::gregorianToJalali(
                    (int)date('Y', $prevDateTimestamp),
                    (int)date('m', $prevDateTimestamp),
                    (int)date('d', $prevDateTimestamp)
                );
                $shiftStartDate = sprintf('%04d/%02d/%02d', $prevJY, $prevJM, $prevJD);
                $shiftEndDate = $currentJalaliDate;
            } else {
                // شیفت شب از روز جاری تا روز بعدی
                [$nextJY, $nextJM, $nextJD] = DateConverter::gregorianToJalali(
                    (int)date('Y', strtotime('+1 day')),
                    (int)date('m', strtotime('+1 day')),
                    (int)date('d', strtotime('+1 day'))
                );
                $shiftStartDate = $currentJalaliDate;
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
        
        // ثبت لاگ برای اطلاع از تعداد رکوردهای پردازش شده
        $this->logger->log("تعداد رکوردهای پردازش شده در پاسخ به درخواست داده‌های لحظه‌ای: " . $result->num_rows, "INFO");
        
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
                SUM(CASE WHEN c.status = 'خروج' THEN c.netWeight ELSE 0 END) AS totalNetWeight,
                MIN(TIME_FORMAT(c.entryTime, '%H:%i')) as firstEntryTime,
                MAX(TIME_FORMAT(c.exitTime, '%H:%i')) as lastExitTime
            FROM 
                InitialInfo i
            LEFT JOIN 
                CargoInfo c ON i.loadingQuotaNumber = c.loadingQuotaNumber 
                    AND i.loadingWarehouse = c.loadingWarehouse
                    AND i.shippingCompany = c.shippingCompany
                    AND i.cargoType = c.cargoType
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

    // CargoAPI class
    private function handleComprehensiveAnalysisRequest(): void {
		try {
			$analytics = new AdvancedCargoAnalytics($this->conn);
			$results = $analytics->getComprehensiveAnalysis();
			
			header('Content-Type: application/json; charset=UTF-8');
			echo json_encode($results);
			exit;
		} catch (Exception $e) {
			error_log("Error in comprehensive analysis: " . $e->getMessage());
			error_log("Stack trace: " . $e->getTraceAsString());
			
			header('Content-Type: application/json; charset=UTF-8');
			http_response_code(500);
			echo json_encode([
				'success' => false,
				'error' => $e->getMessage(),
				'trace' => $e->getTraceAsString()
			]);
			exit;
		}
	}
}

// مقداردهی اولیه و مدیریت درخواست API
try {
    $dbConnection = getDbConnection(); // اطمینان حاصل کنید که getDbConnection() در config.php به درستی تعریف شده است
    $cargoAPI = new CargoAPI($dbConnection);
    $cargoAPI->handleRequest();
} catch (Exception $e) {
    // در صورت بروز خطاهای غیرمنتظره در هنگام راه‌اندازی API
    $logger = new Logger();
    $logger->log($e->getMessage(), 'CRITICAL');
    APIResponse::send(['error' => 'خطای داخلی سرور'], 500);
}
?>