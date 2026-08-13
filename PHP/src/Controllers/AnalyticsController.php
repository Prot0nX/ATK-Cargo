<?php
// PHP/src/Controllers/AnalyticsController.php

declare(strict_types=1);

namespace App\Controllers;

use Exception;
use InvalidArgumentException;
use mysqli;
use App\Core\Database;
use App\Core\Logger;
use App\Core\MicroCache;
use App\Core\Request;
use App\Repositories\UserRepository;
use App\Services\PermissionService;
use App\Services\SessionService;

class AnalyticsController {
    private mysqli $conn;
    private Logger $logger;
    private Request $request;
    private SessionService $sessionService;
    private PermissionService $permissionService;
    private ?string $authenticatedUsername = null;

    // C-4/B-11 (گزارش تحلیل جامع عملیات): این دو مرز عمداً متفاوت‌اند، نه یک
    // ناهماهنگی تصادفی — WORKDAY_BOUNDARY_TIME مرز پنجره «تحلیل جامع عملیات»
    // (handleComprehensiveAnalysisRequest) است؛ SHIFT_DAY_START_TIME مرز شروع
    // شیفت روز در «بارگیری لحظه‌ای» (determineShiftInfo) است. حواله‌های خروج‌شده
    // بین این دو مرز (۰۷:۰۰ تا ۰۷:۳۰) به روز کاری جدید تعلق می‌گیرند اما هنوز به
    // شیفت روز نپیوسته‌اند؛ به همین دلیل دو صفحه برای این نیم‌ساعت عدد متفاوت
    // نشان می‌دهند. هر دو ثابت اینجا در یک نقطه نگه داشته می‌شوند تا این تفاوت
    // آگاهانه بماند، نه اینکه یکی جا بماند وقتی دیگری تغییر می‌کند.
    private const WORKDAY_BOUNDARY_TIME = '07:00:00';
    private const SHIFT_DAY_START_TIME = '07:30:00';
    private const SHIFT_DAY_END_TIME = '19:00:00';

    public function __construct() {
        $this->conn = Database::getInstance()->getMysqliConnection();
        $this->logger = Logger::getInstance();
        $this->request = new Request();
        $this->sessionService = new SessionService();
        $this->permissionService = new PermissionService();
    }

    /**
     * تمام دادهٔ این کنترلر (لیست کشتی‌ها، کوتاژها، تناژ) تجاری و محرمانه است؛
     * مطابق الگوی AppApiController::requireAuthenticatedSession باید فقط برای
     * نشست معتبر در دسترس باشد. هویت از هدرها خوانده می‌شود، نه از GET، تا در
     * لاگ دسترسی/پروکسی ذخیره نشود.
     */
    private function requireAuthenticatedSession(): void {
        $username = (string)($this->request->getHeader('X-Username') ?? '');
        $deviceId = (string)($this->request->getHeader('X-Device-Id') ?? '');
        $token = (string)($this->request->getHeader('X-Session-Token') ?? '');

        if (!$this->sessionService->isValidToken($username, $deviceId, $token)) {
            header('Content-Type: application/json; charset=UTF-8');
            http_response_code(401);
            echo json_encode(['error' => 'نشست معتبر نیست. لطفاً دوباره وارد شوید.'], JSON_UNESCAPED_UNICODE);
            exit;
        }

        $this->authenticatedUsername = $username;
    }

    /**
     * requireAuthenticatedSession فقط معتبر بودن نشست را تضمین می‌کند، نه اینکه
     * کاربر مجاز به دیدن آمار تحلیلی باشد؛ مطابق الگوی
     * AppApiController::requirePermission این شکاف را می‌بندد (مجوز
     * "view_reports" که پیش‌تر فقط در UI/config تعریف شده بود ولی هرگز سمت
     * سرور بررسی نمی‌شد).
     */
    private function requirePermission(string $feature): void {
        $username = $this->authenticatedUsername ?? '';
        $user = (new UserRepository())->getByUsername($username);
        $userType = (string)($user['userType'] ?? '');

        if (!$this->permissionService->hasPermission($username, $userType, $feature)) {
            header('Content-Type: application/json; charset=UTF-8');
            http_response_code(403);
            echo json_encode(['error' => 'شما مجوز مشاهده آمار تحلیلی را ندارید.'], JSON_UNESCAPED_UNICODE);
            exit;
        }
    }

    /**
     * مدیریت درخواست‌های realTimeLoadingData.php
     */
    public function handleRealTimeLoadingData(): void {
        header('Content-Type: application/json; charset=UTF-8');
        header('Cache-Control: no-store');
        date_default_timezone_set('Asia/Tehran');

        // logAnalyticsExport یک عملیات نوشتنی (ثبت لاگ) است، هم‌راستا با قرارداد
        // پروژه (WRITE_ACTIONS در AppApiController) که چنین actionهایی فقط با
        // POST مجازند؛ بقیه actionهای این کنترلر فقط-خواندنی می‌مانند و کلاینت
        // برایشان همچنان GET می‌فرستد.
        if (!$this->request->isGet() && !$this->request->isPost()) {
            $this->sendJsonResponse(['error' => 'فقط متد GET یا POST مجاز است.'], 400);
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
                        $this->sendJsonResponse(['error' => 'این عملیات فقط با POST مجاز است.'], 400);
                    }
                    $this->requirePermission('view_reports');
                    $this->handleLogAnalyticsExport();
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

        if (extension_loaded('zlib') && !ini_get('zlib.output_compression') && !in_array('ob_gzhandler', ob_list_handlers(), true)) {
            ob_start('ob_gzhandler');
        }

        if (!$this->request->isGet()) {
            $this->sendJsonResponse(['success' => false, 'error' => 'روش درخواست نامعتبر است'], 500);
        }

        try {
            $action = $this->request->get('action');
            if ($action === null) {
                throw new InvalidArgumentException('عملیات مشخص نشده است');
            }

            $action = $this->sanitizeInput((string)$action);

            switch ($action) {
                case 'getActiveQuotasRemaining':
                    $result = $this->getActiveQuotasRemaining();
                    $this->sendJsonResponse($result);
                    break;

                case 'getShipQuotasRemaining':
                    $shipName = $this->request->get('shipName');
                    if ($shipName === null) {
                        throw new InvalidArgumentException('نام کشتی مشخص نشده است');
                    }
                    $result = $this->getShipQuotasRemaining((string)$shipName);
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
        // کلاینت فقط ۰ تا ۱۴- را می‌فرستد (ناوبری شیفت در RealTimeShiftNavigation)؛
        // کلمپ سمت سرور از مقادیر آینده (offset مثبت) و از cache-flooding با
        // مقادیر بزرگ دلخواه که هرکدام یک کلید جدید در MicroCache می‌سازند جلوگیری می‌کند.
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

    /**
     * این endpoint هر ۳۰ ثانیه توسط دیالوگ «بارگیری لحظه‌ای» poll می‌شود؛ در
     * بیشتر تیک‌ها داده تغییری نکرده. با ETag/304 (به‌جای Cache-Control:
     * no-store که handleRealTimeLoadingData پیش‌تر برای سایر actionها تنظیم
     * کرده و اینجا override می‌شود)، در حالت بی‌تغییر فقط یک پاسخ خالی ۳۰۴
     * منتقل می‌شود، نه کل payload. max-age کوتاه هم‌راستا با TTL همان کش ۵
     * ثانیه‌ای MicroCache در getRealTimeData است.
     */
    private function sendCacheableRealTimeResponse(array $data): void {
        $etag = '"' . md5(json_encode($data, JSON_UNESCAPED_UNICODE)) . '"';
        header('Cache-Control: private, max-age=5');
        header("ETag: $etag");

        $ifNoneMatch = $this->request->getHeader('If-None-Match');
        if ($ifNoneMatch !== null && trim($ifNoneMatch) === $etag) {
            http_response_code(304);
            exit;
        }

        $this->sendJsonResponse($data);
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
                // باید دقیقاً برابر با startTime شیفت روز باشد، وگرنه بازه‌ی
                // WORKDAY_BOUNDARY_TIME تا SHIFT_DAY_START_TIME در هیچ‌کدام از
                // دو شیفت شمرده نمی‌شود.
                'endTime' => self::SHIFT_DAY_START_TIME,
                'type' => 'شب'
            ];
        }
    }

    private function getRealTimeData(array $shiftInfo): array {
        // کش کوتاه (۵ ثانیه) به ازای هر شیفت مشخص؛ چون چندین کاربر هم‌زمان همین
        // شیفت را poll می‌کنند، بار دیتابیس بدون از دست دادن تازگی داده کم می‌شود.
        $cacheKey = 'analytics_realtime_' . md5(implode('|', [
            $shiftInfo['type'],
            $shiftInfo['startDate'],
            $shiftInfo['endDate'],
            $shiftInfo['startTime'],
            $shiftInfo['endTime'],
        ]));

        return MicroCache::remember($cacheKey, 5, function () use ($shiftInfo) {
            // B-11: شرط‌های WHERE روی ستون‌های c.* در عمل LEFT JOIN را به INNER JOIN
            // تبدیل می‌کردند (ردیف‌های بدون تطبیق، NULL می‌شدند و همان شرط‌ها حذفشان
            // می‌کرد)؛ INNER JOIN صریح همان رفتار واقعی را بدون گمراه‌کنندگی نشان می‌دهد.
            // B-12: فیلتر i.isActive = 1 (هم‌راستا با getActiveQuotasRemaining) اضافه شد
            // تا کوتاژهای غیرفعال‌شده در «بارگیری لحظه‌ای» ظاهر نشوند.
            // C-3: دو شاخه‌ی شیفت روز/شب فقط در شرط زمانی WHERE و تعداد پارامترها
            // تفاوت داشتند؛ SELECT/JOIN/GROUP BY مشترک یک‌بار نوشته می‌شود.
            $baseQuery = "SELECT
                i.loadingQuotaNumber, i.shipName, i.loadingWarehouse, i.shippingCompany, i.cargoType,
                COUNT(DISTINCT CASE WHEN c.status = 'ورود' THEN c.id END) AS entryVouchers,
                COUNT(DISTINCT CASE WHEN c.status = 'خروج' THEN c.id END) AS exitVouchers,
                COUNT(DISTINCT c.id) AS totalVouchers,
                SUM(CASE WHEN c.status = 'خروج' THEN c.netWeight ELSE 0 END) AS totalNetWeight
                FROM InitialInfo i
                INNER JOIN CargoInfo c ON i.loadingQuotaNumber = c.loadingQuotaNumber
                    AND i.loadingWarehouse = c.loadingWarehouse
                    AND i.shippingCompany = c.shippingCompany
                WHERE i.isActive = 1 AND (%s)
                GROUP BY i.loadingQuotaNumber, i.shipName, i.loadingWarehouse, i.shippingCompany, i.cargoType";

            if ($shiftInfo['type'] === 'روز') {
                $shiftCondition = "(c.exitDate = ? AND c.exitTime BETWEEN ? AND ?) OR (c.status = 'ورود' AND c.exitDate IS NULL)";
                $paramTypes = "sss";
                $params = [$shiftInfo['startDate'], $shiftInfo['startTime'], $shiftInfo['endTime']];
            } else {
                $shiftCondition = "(c.exitDate = ? AND c.exitTime >= ?) OR (c.exitDate = ? AND c.exitTime < ?) OR (c.status = 'ورود' AND c.exitDate IS NULL)";
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

    // کلاینت فقط ۰ تا ۷- را می‌فرستد (ناوبری تاریخ در AnalyticsDateNavigation)؛
    // بدون این کلمپ سمت سرور، offset دلخواه (از جمله مقادیر مثبت/آینده یا
    // بسیار بزرگ که در ضرب $offset * 86400 سرریز عدد صحیح PHP را تریگر
    // می‌کنند) هم پذیرفته می‌شد. هم‌راستا با کلمپ مشابه shiftOffset در
    // handleRealTimeDataRequest.
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

        // P-6/C-8 (گزارش تحلیل جامع عملیات): $targetTime از قبل یک timestamp
        // معتبر است؛ نیازی به رفت‌وبرگشت شمسی→میلادی→شمسی (split رشته تاریخ،
        // jalali_to_gregorian، mktime) برای گرفتن نام روز نیست — jdate('l', ...)
        // مستقیماً روی همان timestamp کار می‌کند. نسخه قبلی هم متغیرهایش را با
        // پیشوند گمراه‌کننده‌ی g (gregorian) روی مقادیر شمسی نام‌گذاری کرده بود،
        // هم یک fallback بی‌صدا داشت که در نبود jalali_to_gregorian، نام روز
        // «الان» را برای تاریخی که ممکن بود روزها قبل باشد برمی‌گرداند.

        // B-1/B-8 (گزارش تحلیل جامع عملیات): پنجره واقعی کوئری «روز کاری»
        // (دیروز ۰۷:۰۰ تا امروز ۰۷:۰۰) است، نه «۲۴ ساعت گذشته تا این لحظه».
        // قبلاً فقط jalaliDate/dayName (تاریخ پایان پنجره) برگردانده می‌شد و
        // کلاینت آن را زیر برچسب گمراه‌کننده‌ی «امروز / گزارشات ۲۴ ساعته»
        // نمایش می‌داد. اینجا مرزهای دقیق پنجره صریحاً اضافه می‌شود تا کلاینت
        // بازه واقعی را نشان دهد.
        $workdayBoundaryShort = substr(self::WORKDAY_BOUNDARY_TIME, 0, 5); // "07:00:00" -> "07:00"
        $dateInfo = [
            'jalaliDate' => $todayJalaliDate,
            'dayName' => jdate('l', $targetTime),
            'windowStartDate' => $yesterdayJalaliDate,
            'windowStartTime' => $workdayBoundaryShort,
            'windowEndDate' => $todayJalaliDate,
            'windowEndTime' => $workdayBoundaryShort
        ];

        // P-2 (گزارش تحلیل جامع عملیات): برخلاف getRealTimeData در همین کلاس، این
        // کوئری (که به‌مراتب سنگین‌تر است و روی idx_cargo_exit_window تازه اضافه‌شده
        // هم full scan نمی‌کند ولی همچنان JOIN+GROUP BY سنگینی دارد) نه MicroCache
        // داشت نه ETag. برای روزهای گذشته (offset < 0) داده دیگر تغییر نمی‌کند، پس
        // TTL طولانی‌تر (۱ ساعت) امن است؛ برای روز کاری جاری (offset = 0) TTL کوتاه
        // (۶۰ ثانیه، هم‌راستا با max-age کوتاه در سایر پاسخ‌های این کنترلر).
        $cacheTtl = $offset < 0 ? 3600 : 60;
        $cacheKey = 'analytics_comprehensive_' . md5($yesterdayJalaliDate . '|' . $todayJalaliDate);

        // B-6/B-7 (گزارش تحلیل جامع عملیات):
        // - i.isActive = 1 هم‌راستا با getActiveQuotasRemaining/getRealTimeData اضافه شد
        //   تا کوتاژهای غیرفعال‌شده در تحلیل جامع ظاهر نشوند و آمار دو صفحه بخواند.
        // - COUNT(DISTINCT c.trackingNumber) به‌جای COUNT(*) تا شمارش «تعداد حواله»
        //   با getActiveQuotasRemaining/getShipQuotasRemaining یکسان باشد و اگر یک
        //   trackingNumber بیش از یک ردیف داشته باشد، بیش‌برآورد نشود.
        $workdayBoundary = self::WORKDAY_BOUNDARY_TIME;
        $completionData = MicroCache::remember($cacheKey, $cacheTtl, function () use ($yesterdayJalaliDate, $todayJalaliDate, $workdayBoundary) {
            // B-6 (گزارش تحلیل جامع عملیات): کلید JOIN قبلاً فقط سه‌تایی
            // (loadingQuotaNumber, loadingWarehouse, shippingCompany) بود که در
            // InitialInfo یکتا نیست؛ اگر دو ردیف InitialInfo همین سه‌تایی را با
            // cargoType متفاوت داشته باشند، هر ردیف CargoInfo با هر دو تطبیق
            // می‌خورد و SUM(netWeight) دو برابر می‌شد. افزودن cargoType به شرط
            // JOIN، هم‌راستا با کلید تطبیق پنج‌تایی که getActiveQuotasRemaining/
            // getShipQuotasRemaining در همین فایل استفاده می‌کنند
            // (loadingQuotaNumber|shipName|loadingWarehouse|shippingCompany|cargoType).
            $query = "SELECT
                        c.loadingQuotaNumber, i.shipName, c.shippingCompany, i.cargoOwner, c.loadingWarehouse, i.cargoType,
                        SUM(c.netWeight) AS last_24h_weight, COUNT(DISTINCT c.trackingNumber) AS last_24h_vouchers
                    FROM CargoInfo c
                    JOIN InitialInfo i ON c.loadingQuotaNumber = i.loadingQuotaNumber
                        AND c.loadingWarehouse = i.loadingWarehouse
                        AND c.shippingCompany = i.shippingCompany
                        AND c.cargoType = i.cargoType
                    WHERE i.isActive = 1 AND c.status = 'خروج' AND ((c.exitDate = ? AND c.exitTime >= '$workdayBoundary') OR (c.exitDate = ? AND c.exitTime < '$workdayBoundary'))
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

    /**
     * A-5 (گزارش تحلیل جامع عملیات): اشتراک‌گذاری خلاصه تحلیل جامع (نام کشتی،
     * صاحب کالا، انبار، تناژ، تعداد حواله) از طریق Intent.ACTION_SEND کاملاً
     * سمت کلاینت اتفاق می‌افتد؛ سرور هیچ ثبتی نداشت که چه کسی چه داده‌ای را
     * در چه زمانی خارج کرده. این endpoint خودِ محتوای اشتراک‌گذاری‌شده را
     * ذخیره نمی‌کند (ممکن است حجیم/تکراری باشد)، فقط چه‌کسی/چه‌دامنه‌ای/چند
     * گروه را با Logger موجود پروژه ثبت می‌کند تا در صورت نیاز به بررسی نشت
     * داده، منبع و زمان قابل ردیابی باشد.
     */
    private function handleLogAnalyticsExport(): void {
        $rawScope = (string)$this->request->get('scope', 'نامشخص');
        // دفاعی: scope از GET/POST خوانده می‌شود و نظری به مقدار واقعی که
        // کلاینت رسمی می‌فرستد ندارد؛ جلوگیری از log injection (خط جدید) و
        // محدود کردن طول برای فایل لاگ.
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

        $this->sendJsonResponse(['success' => true]);
    }

    /**
     * مشابه sendCacheableRealTimeResponse برای handleRealTimeDataRequest؛ چون آن
     * متد Cache-Control با max-age ثابت (۵) دارد و اینجا max-age بسته به TTL کش
     * (تاریخچه در برابر روز جاری) متفاوت است، نسخه مجزا با $ttlSeconds پارامتری.
     */
    private function sendCacheableAnalyticsResponse(array $data, int $ttlSeconds): void {
        $etag = '"' . md5(json_encode($data, JSON_UNESCAPED_UNICODE)) . '"';
        header("Cache-Control: private, max-age=$ttlSeconds");
        header("ETag: $etag");

        $ifNoneMatch = $this->request->getHeader('If-None-Match');
        if ($ifNoneMatch !== null && trim($ifNoneMatch) === $etag) {
            http_response_code(304);
            exit;
        }

        $this->sendJsonResponse($data);
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
        if (extension_loaded('zlib') && !ini_get('zlib.output_compression') && !in_array('ob_gzhandler', ob_list_handlers(), true)) {
            ob_start('ob_gzhandler');
        }
        echo json_encode($data, JSON_UNESCAPED_UNICODE);
        exit;
    }
}
