<?php
// PHP/src/Controllers/AnalyticsController.php

declare(strict_types=1);

namespace App\Controllers;

use Exception;
use InvalidArgumentException;
use mysqli;
use App\Core\AuthenticatesRequests;
use App\Core\Database;
use App\Core\Logger;
use App\Core\MicroCache;
use App\Core\Request;
use App\Core\Response;
use App\Validators\InputValidator;
use App\Enums\CargoStatus;

class AnalyticsController {
    use AuthenticatesRequests;

    // بدون ->value چون PHP 8.1 اجازه‌ی property-fetch در class const نمی‌دهد
    private const ENTERED = CargoStatus::ENTERED;
    private const EXITED = CargoStatus::EXITED;

    private mysqli $conn;
    private Logger $logger;
    private Request $request;

    // دو مرز زمانی عمداً متفاوت: WORKDAY_BOUNDARY_TIME برای تحلیل جامع، SHIFT_DAY_START_TIME برای شیفت روز
    private const WORKDAY_BOUNDARY_TIME = '07:00:00';
    private const SHIFT_DAY_START_TIME = '07:30:00';
    private const SHIFT_DAY_END_TIME = '19:00:00';

    public function __construct() {
        $this->conn = Database::getInstance()->getMysqliConnection();
        $this->logger = Logger::getInstance();
        $this->request = new Request();
    }

    // فرمت سفارشی پاسخ خطای احراز هویت برای سازگاری با کلاینت این کنترلر
    protected function sendAuthErrorResponse(string $message, int $httpCode, ?string $code = null): void {
        header('Content-Type: application/json; charset=UTF-8');
        http_response_code($httpCode);
        $body = ['error' => $message];
        if ($code !== null) {
            $body['code'] = $code;
        }
        echo json_encode($body, JSON_UNESCAPED_UNICODE);
        exit;
    }

    // مدیریت درخواست‌های realTimeLoadingData.php
    public function handleRealTimeLoadingData(): void {
        header('Content-Type: application/json; charset=UTF-8');
        header('Cache-Control: no-store');
        date_default_timezone_set('Asia/Tehran');

        // logAnalyticsExport یک عملیات نوشتنی است و فقط با POST مجاز است؛ بقیه‌ی actionها GET هستند
        if (!$this->request->isGet() && !$this->request->isPost()) {
            Response::error('فقط متد GET یا POST مجاز است.', 400);
        }

        $this->requireAuthenticatedSession();

        try {
            $action = (string)$this->request->get('action', '');

            switch ($action) {
                case 'getKotazhInfo':
                    $this->requirePermission('view_reports');
                    $this->handleKotazhRequest();
                    break;
                case 'getRealTimeData':
                    $this->requirePermission('view_reports');
                    $this->handleRealTimeDataRequest();
                    break;
                case 'getComprehensiveAnalysis':
                    $this->requirePermission('view_reports');
                    $this->handleComprehensiveAnalysisRequest();
                    break;
                case 'logAnalyticsExport':
                    if (!$this->request->isPost()) {
                        Response::error('این عملیات فقط با POST مجاز است.', 400);
                    }
                    $this->requirePermission('view_reports');
                    $this->handleLogAnalyticsExport();
                    break;
                default:
                    Response::error('عملیات نامعتبر است.', 400);
            }
        } catch (InvalidArgumentException $e) {
            Response::error($e->getMessage(), 400);
        } catch (Exception $e) {
            $this->logger->error("Error in handleRealTimeLoadingData: " . $e->getMessage());
            Response::error('خطایی در سرور رخ داد.', 500);
        }
    }

    // مدیریت درخواست‌های GET analytics/quota-remaining
    public function handleQuotaRemaining(): void {
        header('Content-Type: application/json; charset=UTF-8');
        header('Cache-Control: max-age=60, public');
        date_default_timezone_set('Asia/Tehran');

        // فشرده‌سازی PHP-level حذف شد چون mod_deflate در htaccess همین کار را می‌کند (P-06)
        if (!$this->request->isGet()) {
            Response::error('روش درخواست نامعتبر است', 500);
        }

        // افزودن گیت احراز هویت برای جلوگیری از دسترسی ناشناس به داده‌های تجاری، مطابق handleRealTimeLoadingData
        $this->requireAuthenticatedSession();
        $this->requirePermission('active_quotas');

        try {
            $action = $this->request->get('action');
            if ($action === null) {
                throw new InvalidArgumentException('عملیات مشخص نشده است');
            }

            $action = InputValidator::sanitize((string)$action);

            switch ($action) {
                case 'getActiveQuotasRemaining':
                    $result = $this->getActiveQuotasRemaining();
                    Response::json($result);
                    break;

                case 'getShipQuotasRemaining':
                    $shipName = $this->request->get('shipName');
                    if ($shipName === null) {
                        throw new InvalidArgumentException('نام کشتی مشخص نشده است');
                    }
                    $result = $this->getShipQuotasRemaining((string)$shipName);
                    Response::json($result);
                    break;

                default:
                    throw new InvalidArgumentException('عملیات نامعتبر است');
            }
        } catch (Exception $e) {
            $this->logger->error("Error in handleQuotaRemaining: " . $e->getMessage());
            Response::error($e->getMessage(), 500);
        }
    }

    private function handleKotazhRequest(): void {
        $kotazh = trim((string)($this->request->get('kotazh', '')));
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
            Response::error('کوتاژ مورد نظر یافت نشد.', 404);
        }

        $stmt2 = $this->conn->prepare("SELECT trackingNumber, entryTime, netWeight, scaleReceiptNumber, shortageWeight, excessWeight, exitTime, exitDate, status FROM CargoInfo WHERE loadingQuotaNumber = ?");
        $stmt2->bind_param("s", $kotazh);
        $stmt2->execute();
        $cargoInfo = $stmt2->get_result()->fetch_all(MYSQLI_ASSOC);
        $stmt2->close();

        Response::json([
            'kotazhInfo' => $kotazhInfo,
            'cargoInfo' => $cargoInfo
        ]);
    }

    private function handleRealTimeDataRequest(): void {
        // کلمپ سمت سرور روی shiftOffset برای جلوگیری از مقادیر آینده و cache-flooding در MicroCache
        $shiftOffset = max(-14, min(0, (int)$this->request->get('shiftOffset', 0)));
        $targetTimestamp = time() + ($shiftOffset * 12 * 3600);
        $currentTimeString = date('H:i:s', $targetTimestamp);

        $shiftInfo = $this->determineShiftInfo($currentTimeString, $targetTimestamp);
        $realTimeData = $this->getRealTimeData($shiftInfo);

        $this->sendCacheableRealTimeResponse([
            'shiftInfo' => $shiftInfo,
            'data' => $realTimeData
        ]);
    }

    // پاسخ‌دهی با ETag/304 برای این endpoint پرتکرار تا در حالت بی‌تغییر فقط پاسخ خالی ارسال شود
    private function sendCacheableRealTimeResponse(array $data): void {
        $etag = '"' . md5(json_encode($data, JSON_UNESCAPED_UNICODE)) . '"';
        header('Cache-Control: private, max-age=5');
        header("ETag: $etag");

        $ifNoneMatch = $this->request->getHeader('If-None-Match');
        if ($ifNoneMatch !== null && trim($ifNoneMatch) === $etag) {
            http_response_code(304);
            exit;
        }

        Response::json($data);
    }

    private function determineShiftInfo(string $currentTime, int $targetTimestamp): array {
        $currentJalaliDate = jdate('Y/m/d', $targetTimestamp);

        if ($currentTime >= self::SHIFT_DAY_START_TIME && $currentTime < self::SHIFT_DAY_END_TIME) {
            return [
                'startDate' => $currentJalaliDate,
                'endDate' => $currentJalaliDate,
                'startTime' => self::SHIFT_DAY_START_TIME,
                'endTime' => self::SHIFT_DAY_END_TIME,
                'type' => 'روز'
            ];
        } else {
            if ($currentTime >= '00:00:00' && $currentTime < self::SHIFT_DAY_START_TIME) {
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
                'startTime' => self::SHIFT_DAY_END_TIME,
                // باید دقیقاً برابر startTime شیفت روز باشد وگرنه بازه‌ی بین دو مرز شمرده نمی‌شود
                'endTime' => self::SHIFT_DAY_START_TIME,
                'type' => 'شب'
            ];
        }
    }

    private function getRealTimeData(array $shiftInfo): array {
        // کش کوتاه ۵ ثانیه‌ای به ازای هر شیفت برای کاهش بار دیتابیس در poll همزمان کاربران
        $cacheKey = 'analytics_realtime_' . md5(implode('|', [
            $shiftInfo['type'],
            $shiftInfo['startDate'],
            $shiftInfo['endDate'],
            $shiftInfo['startTime'],
            $shiftInfo['endTime'],
        ]));

        return MicroCache::remember($cacheKey, 5, function () use ($shiftInfo) {
            // INNER JOIN صریح، فیلتر isActive و اشتراک SELECT/JOIN بین دو شیفت (B-11/B-12/C-3)
            $baseQuery = "SELECT
                i.loadingQuotaNumber, i.shipName, i.loadingWarehouse, i.shippingCompany, i.cargoType,
                COUNT(DISTINCT CASE WHEN c.status = '" . self::ENTERED->value . "' THEN c.id END) AS entryVouchers,
                COUNT(DISTINCT CASE WHEN c.status = '" . self::EXITED->value . "' THEN c.id END) AS exitVouchers,
                COUNT(DISTINCT c.id) AS totalVouchers,
                SUM(CASE WHEN c.status = '" . self::EXITED->value . "' THEN c.netWeight ELSE 0 END) AS totalNetWeight
                FROM InitialInfo i
                INNER JOIN CargoInfo c ON i.loadingQuotaNumber = c.loadingQuotaNumber
                    AND i.loadingWarehouse = c.loadingWarehouse
                    AND i.shippingCompany = c.shippingCompany
                WHERE i.isActive = 1 AND (%s)
                GROUP BY i.loadingQuotaNumber, i.shipName, i.loadingWarehouse, i.shippingCompany, i.cargoType";

            if ($shiftInfo['type'] === 'روز') {
                $shiftCondition = "(c.exitDate = ? AND c.exitTime BETWEEN ? AND ?) OR (c.status = '" . self::ENTERED->value . "' AND c.exitDate IS NULL)";
                $paramTypes = "sss";
                $params = [$shiftInfo['startDate'], $shiftInfo['startTime'], $shiftInfo['endTime']];
            } else {
                $shiftCondition = "(c.exitDate = ? AND c.exitTime >= ?) OR (c.exitDate = ? AND c.exitTime < ?) OR (c.status = '" . self::ENTERED->value . "' AND c.exitDate IS NULL)";
                $paramTypes = "ssss";
                $params = [$shiftInfo['startDate'], $shiftInfo['startTime'], $shiftInfo['endDate'], $shiftInfo['endTime']];
            }

            $stmt = $this->conn->prepare(sprintf($baseQuery, $shiftCondition));
            $stmt->bind_param($paramTypes, ...$params);
            $stmt->execute();
            $result = $stmt->get_result()->fetch_all(MYSQLI_ASSOC);
            $stmt->close();
            return $result;
        });
    }

    // کلمپ سمت سرور روی offset برای جلوگیری از مقادیر بزرگ/آینده، مشابه shiftOffset
    private const MAX_ANALYTICS_DAYS_BACK = 7;

    private function handleComprehensiveAnalysisRequest(): void {
        $offset = max(-self::MAX_ANALYTICS_DAYS_BACK, min(0, (int)$this->request->get('offset', 0)));

        $currentTime = time();
        if (date('H') < 7) {
            $currentTime = strtotime('-1 day');
        }
        $targetTime = $currentTime + ($offset * 86400);
        $todayJalaliDate = jdate('Y/m/d', $targetTime);
        $yesterdayJalaliDate = jdate('Y/m/d', $targetTime - 86400);

        // محاسبه‌ی مستقیم نام روز از timestamp (بدون رفت‌وبرگشت شمسی) و افزودن مرزهای دقیق پنجره‌ی «روز کاری» (P-6/C-8/B-1/B-8)
        $workdayBoundaryShort = substr(self::WORKDAY_BOUNDARY_TIME, 0, 5); // "07:00:00" -> "07:00"
        $dateInfo = [
            'jalaliDate' => $todayJalaliDate,
            'dayName' => jdate('l', $targetTime),
            'windowStartDate' => $yesterdayJalaliDate,
            'windowStartTime' => $workdayBoundaryShort,
            'windowEndDate' => $todayJalaliDate,
            'windowEndTime' => $workdayBoundaryShort
        ];

        // افزودن کش با TTL متغیر: طولانی برای روزهای گذشته، کوتاه برای روز جاری (P-2)
        $cacheTtl = $offset < 0 ? 3600 : 60;
        $cacheKey = 'analytics_comprehensive_' . md5($yesterdayJalaliDate . '|' . $todayJalaliDate);

        // فیلتر isActive و شمارش با COUNT(DISTINCT trackingNumber) برای هم‌راستایی آمار با سایر توابع (B-6/B-7)
        $workdayBoundary = self::WORKDAY_BOUNDARY_TIME;
        $completionData = MicroCache::remember($cacheKey, $cacheTtl, function () use ($yesterdayJalaliDate, $todayJalaliDate, $workdayBoundary) {
            // افزودن cargoType به شرط JOIN برای جلوگیری از دوبرابر شدن SUM(netWeight) در تطبیق نادرست (B-6)
            $query = "SELECT
                        c.loadingQuotaNumber, i.shipName, c.shippingCompany, i.cargoOwner, c.loadingWarehouse, i.cargoType,
                        SUM(c.netWeight) AS last_24h_weight, COUNT(DISTINCT c.trackingNumber) AS last_24h_vouchers
                    FROM CargoInfo c
                    JOIN InitialInfo i ON c.loadingQuotaNumber = i.loadingQuotaNumber
                        AND c.loadingWarehouse = i.loadingWarehouse
                        AND c.shippingCompany = i.shippingCompany
                        AND c.cargoType = i.cargoType
                    WHERE i.isActive = 1 AND c.status = '" . self::EXITED->value . "' AND ((c.exitDate = ? AND c.exitTime >= '$workdayBoundary') OR (c.exitDate = ? AND c.exitTime < '$workdayBoundary'))
                    GROUP BY c.loadingQuotaNumber, i.shipName, c.shippingCompany, i.cargoOwner, c.loadingWarehouse, i.cargoType
                    ORDER BY last_24h_vouchers DESC";

            $stmt = $this->conn->prepare($query);
            $stmt->bind_param("ss", $yesterdayJalaliDate, $todayJalaliDate);
            $stmt->execute();
            $result = $stmt->get_result();

            $rows = [];
            while ($row = $result->fetch_assoc()) {
                $rows[] = [
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
            return $rows;
        });

        $this->sendCacheableAnalyticsResponse([
            'success' => true,
            'data' => [
                'dateInfo' => $dateInfo,
                'quotaCompletionAnalysis' => $completionData
            ]
        ], $cacheTtl);
    }

    // ثبت لاگ اشتراک‌گذاری تحلیل جامع (کاربر/دامنه/تعداد گروه) برای ردیابی احتمالی نشت داده (A-5)
    private function handleLogAnalyticsExport(): void {
        $rawScope = (string)$this->request->get('scope', 'نامشخص');
        // پاک‌سازی دفاعی scope برای جلوگیری از log injection و محدود کردن طول
        $scope = mb_substr(str_replace(["\r", "\n"], ' ', $rawScope), 0, 200);
        $groupCount = max(0, (int)$this->request->get('groupCount', 0));

        $this->logger->info(
            sprintf(
                'خروجی تحلیل جامع عملیات توسط کاربر «%s» | دامنه: %s | تعداد گروه: %d',
                $this->authenticatedUsername ?? 'نامشخص',
                $scope,
                $groupCount
            ),
            'analytics_export'
        );

        Response::json(['success' => true]);
    }

    // مشابه sendCacheableRealTimeResponse ولی با max-age پارامتری بسته به TTL کش
    private function sendCacheableAnalyticsResponse(array $data, int $ttlSeconds): void {
        $etag = '"' . md5(json_encode($data, JSON_UNESCAPED_UNICODE)) . '"';
        header("Cache-Control: private, max-age=$ttlSeconds");
        header("ETag: $etag");

        $ifNoneMatch = $this->request->getHeader('If-None-Match');
        if ($ifNoneMatch !== null && trim($ifNoneMatch) === $etag) {
            http_response_code(304);
            exit;
        }

        Response::json($data);
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

        $exitQuery = "SELECT loadingQuotaNumber, shipName, loadingWarehouse, shippingCompany, cargoType, SUM(netWeight) as loadedTonnage, COUNT(DISTINCT trackingNumber) as voucherCount FROM CargoInfo WHERE status = '" . self::EXITED->value . "' GROUP BY loadingQuotaNumber, shipName, loadingWarehouse, shippingCompany, cargoType";
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
        $shipName = InputValidator::sanitize($shipName);

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

        $exitQuery = "SELECT loadingQuotaNumber, shipName, loadingWarehouse, shippingCompany, cargoType, SUM(netWeight) as loadedTonnage, COUNT(DISTINCT trackingNumber) as voucherCount FROM CargoInfo WHERE status = '" . self::EXITED->value . "' AND shipName = ? GROUP BY loadingQuotaNumber, shipName, loadingWarehouse, shippingCompany, cargoType";
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

}
