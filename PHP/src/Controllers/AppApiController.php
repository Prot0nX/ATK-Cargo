<?php
// PHP/src/Controllers/AppApiController.php

declare(strict_types=1);

namespace App\Controllers;

use Exception;
use mysqli;
use mysqli_stmt;
use App\Core\Database;
use App\Core\DatabaseManager;
use App\Core\MicroCache;
use App\Core\Request;
use App\Core\Response;
use App\Exceptions\ApiException;
use App\Repositories\UserRepository;
use App\Services\PermissionService;
use App\Services\SessionService;

class AppApiController {
    private DatabaseManager $db;
    private Request $request;
    private SessionService $sessionService;
    private PermissionService $permissionService;
    private ?string $authenticatedUsername = null;

    public function __construct() {
        $this->db = new DatabaseManager();
        $this->request = new Request();
        $this->sessionService = new SessionService();
        $this->permissionService = new PermissionService();
    }

    /**
     * تمام عملیات این کنترلر شامل داده‌های تجاری (نام کشتی‌ها، تناژ، کوتاژها) است
     * و باید فقط برای کاربران دارای نشست فعال در دسترس باشد. هویت از هدرهای
     * درخواست خوانده می‌شود (نه از پارامترهای GET) تا در لاگ‌های دسترسی/پروکسی
     * ذخیره نشود.
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
     * گیت سطح دسترسی برای actionهای نوشتنی (ویرایش/حذف/تغییر وضعیت کوتاژ).
     * requireAuthenticatedSession فقط معتبر بودن نشست را تضمین می‌کند، نه
     * اینکه کاربر مجاز به این عملیات خاص باشد؛ این متد آن شکاف را می‌بندد.
     */
    private function requirePermission(string $feature): void {
        $username = $this->authenticatedUsername ?? '';
        $userRepository = new UserRepository();
        $user = $userRepository->getByUsername($username);
        $userType = (string)($user['userType'] ?? '');

        if (!$this->permissionService->hasPermission($username, $userType, $feature)) {
            header('Content-Type: application/json; charset=UTF-8');
            http_response_code(403);
            echo json_encode(['error' => 'شما مجوز انجام این عملیات را ندارید.'], JSON_UNESCAPED_UNICODE);
            exit;
        }
    }

    // این actionها داده را تغییر می‌دهند یا حذف می‌کنند و باید فقط با POST
    // فراخوانی شوند: قابل بازپخش نبودن (URL به‌تنهایی کافی برای اجرای دوباره
    // نیست)، عدم امکان کش شدن توسط پروکسی/OkHttp (که فقط GET را کش می‌کنند)،
    // و نبود پارامترهای حساس در Query String لاگ‌های وب‌سرور.
    private const WRITE_ACTIONS = [
        'editQuota',
        'updateQuotaPercentage',
        'toggleQuotaStatus',
        'updateQuotaPercentageRestriction',
        'deleteQuota',
        'updateTemporaryTonnage',
    ];

    public function handle(): void {
        try {
            if (!$this->request->isGet() && !$this->request->isPost()) {
                throw new Exception('روش درخواست نامعتبر است');
            }

            $this->requireAuthenticatedSession();

            $action = $this->request->get('action');
            if (!$action) {
                throw new Exception('عملیات مشخص نشده است');
            }

            $action = $this->sanitizeInput((string)$action);

            $isWriteAction = in_array($action, self::WRITE_ACTIONS, true);
            if ($isWriteAction && !$this->request->isPost()) {
                throw new Exception('این عملیات باید با متد POST ارسال شود');
            }
            if (!$isWriteAction && !$this->request->isGet()) {
                throw new Exception('این عملیات باید با متد GET ارسال شود');
            }

            switch ($action) {
                case 'getShipsList':
                    $ships = $this->getShipsList();
                    // ETag فقط از activeShips/inactiveShips (بخشی که کلاینت واقعاً
                    // مصرف می‌کند) محاسبه می‌شود، نه از statistics.timestamp که
                    // همیشه لحظه‌ای است و هر بار ETag را بی‌دلیل عوض می‌کرد.
                    $etagSource = [
                        'activeShips' => $ships['data']['activeShips'] ?? [],
                        'inactiveShips' => $ships['data']['inactiveShips'] ?? [],
                    ];
                    // TTL هم‌راستا با MicroCache (۸ ثانیه) در getShipsList
                    $this->sendCacheableJsonResponse($ships, $etagSource, 8);
                    break;

                case 'getShipDetails':
                    $shipName = $this->request->get('shipName');
                    if (!$shipName) {
                        throw new Exception('نام کشتی مشخص نشده است');
                    }
                    $shipDetails = $this->getShipDetails((string)$shipName);
                    $this->sendCacheableJsonResponse($shipDetails, $shipDetails, 6);
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
                    $this->sendCacheableJsonResponse($quotasList, $quotasList, 6);
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
                    if (extension_loaded('zlib') && !ini_get('zlib.output_compression') && !in_array('ob_gzhandler', ob_list_handlers(), true)) {
                        ob_start('ob_gzhandler');
                    }
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
                    $shipNameFilter = $this->request->get('shipName');
                    $shipNameFilter = ($shipNameFilter !== null && trim((string)$shipNameFilter) !== '')
                        ? $this->sanitizeInput((string)$shipNameFilter)
                        : null;
                    $groupedQuotas = $this->getGroupedQuotas($shipNameFilter);
                    $this->sendJsonResponse($groupedQuotas);
                    break;

                case 'updateTemporaryTonnage':
                    $this->requirePermission('manage_quotas');
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
                    $this->requirePermission('manage_quotas');
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
                    $this->requirePermission('manage_quotas');
                    $id = $this->request->get('id');
                    $percentage = $this->request->get('percentage');
                    if (!$id || intval($id) <= 0 || $percentage === null) {
                        throw new Exception('شناسه کوتاژ یا درصد مشخص نشده است');
                    }
                    $result = $this->updateQuotaPercentage(intval($id), floatval($percentage));
                    $this->sendJsonResponse(['success' => $result]);
                    break;

                case 'toggleQuotaStatus':
                    $this->requirePermission('manage_quotas');
                    $id = $this->request->get('id');
                    if (!$id || intval($id) <= 0) {
                        throw new Exception('شناسه کوتاژ مشخص نشده است');
                    }
                    $result = $this->toggleQuotaStatus(intval($id));
                    $this->sendJsonResponse(['success' => $result]);
                    break;

                case 'updateQuotaPercentageRestriction':
                    $this->requirePermission('manage_quotas');
                    $id = $this->request->get('id');
                    $isEnabled = $this->request->get('isEnabled');
                    if (!$id || intval($id) <= 0 || $isEnabled === null) {
                        throw new Exception('شناسه کوتاژ یا مقدار محدودیت مشخص نشده است');
                    }
                    $result = $this->updateQuotaPercentageRestriction(intval($id), intval($isEnabled));
                    $this->sendJsonResponse(['success' => $result]);
                    break;

                case 'deleteQuota':
                    $this->requirePermission('manage_quotas');
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
        } catch (ApiException $e) {
            // ApiException برای خطاهایی که کد وضعیت HTTP معنادار دارند (مثلاً
            // «یافت نشد» → 404) استفاده می‌شود؛ کلاینت به‌جای تطبیق رشته‌ی
            // فارسی پیام خطا، بر اساس details['code'] یا کد وضعیت تصمیم می‌گیرد.
            $this->sendJsonResponse(
                ['error' => $e->getMessage()] + ($e->getDetails() ?? []),
                $e->getStatusCode()
            );
        } catch (Exception $e) {
            $this->sendJsonResponse(['error' => $e->getMessage()], 500);
        }
    }

    private function sanitizeInput(string $input): string {
        return htmlspecialchars(strip_tags(trim($input)), ENT_QUOTES, 'UTF-8');
    }

    /**
     * برای مقادیری که در یک شرط تساوی (WHERE = ?) با prepared statement مقایسه
     * می‌شوند (مثل نام کشتی) نباید htmlspecialchars اعمال شود، وگرنه نامی مثل
     * "M&V" به "M&amp;V" تبدیل و مقایسه با دیتابیس شکسته می‌شود. SQL Injection
     * توسط prepared statement مهار می‌شود، نه توسط escape کردن ورودی.
     */
    private function validateIdentifier(string $input, int $maxLength = 150): string {
        $value = trim($input);
        if ($value === '' || mb_strlen($value) > $maxLength) {
            throw new Exception('مقدار ورودی نامعتبر است');
        }
        return $value;
    }

    private function sendJsonResponse($data, int $statusCode = 200): void {
        header('Content-Type: application/json; charset=UTF-8');
        header('Cache-Control: no-store');
        http_response_code($statusCode);
        if (extension_loaded('zlib') && !ini_get('zlib.output_compression') && !in_array('ob_gzhandler', ob_list_handlers(), true)) {
            ob_start('ob_gzhandler');
        }
        echo json_encode($data, JSON_UNESCAPED_UNICODE | JSON_THROW_ON_ERROR);
        exit;
    }

    /**
     * فقط برای پاسخ‌های GET غیرقابل تغییر (idempotent) استفاده شود، هرگز برای
     * عملیات نوشتن/حذف. ETag از $etagSource (نه کل $data) محاسبه می‌شود تا
     * فیلدهای همیشه‌متغیر (مثل timestamp) باعث نادیده گرفتن کش نشوند.
     */
    private function sendCacheableJsonResponse($data, array $etagSource, int $maxAgeSeconds): void {
        $etag = '"' . md5(json_encode($etagSource, JSON_UNESCAPED_UNICODE)) . '"';
        header('Cache-Control: private, max-age=' . $maxAgeSeconds);
        header("ETag: $etag");

        $ifNoneMatch = $this->request->getHeader('If-None-Match');
        if ($ifNoneMatch !== null && trim($ifNoneMatch) === $etag) {
            http_response_code(304);
            exit;
        }

        $this->sendJsonResponse($data);
    }

    // کلیدهای کش پرکاربردترین دو endpoint این صفحه (جزئیات کشتی + لیست کوتاژها)،
    // هر کدام به‌ازای هر نام کشتی جداگانه؛ در نوشتن‌هایی که خروجی این دو را
    // تغییر می‌دهند (editQuota/deleteQuota/toggleQuotaStatus/updateQuotaPercentage*)
    // صریحاً forget می‌شوند تا کاربر داده‌ی بیات نبیند.
    private function shipDetailsCacheKey(string $shipName): string {
        return 'app_api_ship_details_' . $shipName;
    }

    private function quotasListCacheKey(string $shipName): string {
        return 'app_api_quotas_list_' . $shipName;
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

        // ستونی به نام loadingQuotaNumberReversed هرگز در جدول InitialInfo وجود
        // نداشته (این endpoint از ابتدا با خطای "Unknown column" شکست می‌خورد)؛
        // REVERSE() همان منطق تطبیق معکوس رقم‌ها را بدون نیاز به ستون واقعی انجام می‌دهد.
        // InitialInfo حجم کمی دارد (چند صد ردیف)، پس نبود ایندکس روی این شرط مشکلی ایجاد نمی‌کند.
        //
        // isActive اینجا مستقیماً محاسبه می‌شود (همان فرمول checkQuotaStatus:
        // ستون isActive AND هنوز ظرفیت باقی مانده) تا کلاینت لازم نباشد به
        // ازای هر ردیف نتیجه یک درخواست جداگانه به checkQuotaStatus بزند
        // (رِیس N+1 قبلی در CargoViewModel.checkQuotaExistenceCargo).
        $query = "SELECT i.loadingQuotaNumber, i.shipName, i.shippingCompany, i.cargoType, i.loadingWarehouse,
                i.isActive, i.cargoWeight as totalWeight, COALESCE(SUM(c.netWeight), 0) as loadedWeight
            FROM InitialInfo i
            LEFT JOIN CargoInfo c ON
                c.loadingQuotaNumber = i.loadingQuotaNumber AND c.shipName = i.shipName
                AND c.cargoType = i.cargoType AND c.shippingCompany = i.shippingCompany
                AND c.loadingWarehouse = i.loadingWarehouse AND c.status = 'خروج'
            WHERE REVERSE(i.loadingQuotaNumber) LIKE ? AND i.shipName = ?
            GROUP BY i.loadingQuotaNumber, i.shipName, i.shippingCompany, i.cargoType, i.loadingWarehouse, i.isActive, i.cargoWeight
            ORDER BY i.loadingQuotaNumber";

        $stmt = $this->db->prepare($query);
        $reversedLikeQuotaNumber = strrev($quotaNumber) . '%';
        $stmt->bind_param("ss", $reversedLikeQuotaNumber, $shipName);
        $stmt->execute();
        $result = $stmt->get_result();

        $matchingQuotas = [];
        while ($row = $result->fetch_assoc()) {
            $totalWeight = (float)$row['totalWeight'];
            $loadedWeight = (float)$row['loadedWeight'];
            $matchingQuotas[] = [
                'quotaNumber' => $row['loadingQuotaNumber'],
                'shipName' => $row['shipName'],
                'shippingCompany' => $row['shippingCompany'],
                'cargoType' => $row['cargoType'],
                'warehouse' => $row['loadingWarehouse'],
                'isActive' => (bool)$row['isActive'] && ($loadedWeight < $totalWeight)
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
        // نتیجه‌ی کوئری (بدون timestamp) به مدت کوتاهی کش می‌شود تا این کوئری سنگین
        // که هم توسط action=getShipsList و هم action=getRealTimeData صدا زده می‌شود
        // روی هر poll دوباره روی دیتابیس اجرا نشود؛ timestamp همیشه لحظه‌ای محاسبه می‌شود.
        // TTL از ۸ به ۲۰ ثانیه افزایش یافت: با ایندکس‌های جدید روی CargoInfo این
        // کوئری دیگر سنگین نیست، و نوشتن‌هایی که خروجی این کوئری را عوض می‌کنند
        // (editQuota/toggleQuotaStatus/deleteQuota) صریحاً کش را invalidate می‌کنند.
        $shipsData = MicroCache::remember(MicroCache::SHIPS_LIST_KEY, 20, function () {
            // shippingCompanyCount، cargoTypeCount, percentageLoaded و بلاک statistics
            // قبلاً هم در پاسخ محاسبه می‌شدند هم به کلاینت ارسال، اما مدل Ship/ShipsData
            // اندروید هیچ‌کدام را map نمی‌کرد (هدر و پردازش Gson بی‌فایده). حذف شدند.
            $query = "SELECT
                i.shipName, i.cargoType, COUNT(DISTINCT i.loadingWarehouse) as warehouseCount,
                COUNT(DISTINCT CONCAT(i.loadingQuotaNumber, '-', i.loadingWarehouse, '-', i.shippingCompany, '-', i.cargoType)) as quotaCount,
                SUM(i.cargoWeight) as totalTonnage, COALESCE(SUM(loaded.loadedWeight), 0) as loadedTonnage,
                (SUM(i.cargoWeight) - COALESCE(SUM(loaded.loadedWeight), 0)) as remainingTonnage,
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

            while ($row = $result->fetch_assoc()) {
                $ship = [
                    'name' => $row['shipName'],
                    'cargoType' => $row['cargoType'],
                    'warehouseCount' => (int)$row['warehouseCount'],
                    'quotaCount' => (int)$row['quotaCount'],
                    'totalTonnage' => (float)$row['totalTonnage'],
                    'remainingTonnage' => (float)$row['remainingTonnage'],
                    'loadedTonnage' => (float)$row['loadedTonnage'],
                    'isActive' => (bool)$row['isActive']
                ];

                if ($ship['isActive']) {
                    $activeShips[] = $ship;
                } else {
                    $inactiveShips[] = $ship;
                }
            }

            return [
                'activeShips' => $activeShips,
                'inactiveShips' => $inactiveShips,
            ];
        });

        return [
            'data' => [
                'activeShips' => $shipsData['activeShips'],
                'inactiveShips' => $shipsData['inactiveShips'],
            ]
        ];
    }

    public function getShipDetails(string $shipName): array {
        $shipName = $this->validateIdentifier($shipName);

        return MicroCache::remember($this->shipDetailsCacheKey($shipName), 6, function () use ($shipName) {
            return $this->computeShipDetails($shipName);
        });
    }

    private function computeShipDetails(string $shipName): array {
        $query = "SELECT
            i.shipName, i.loadingWarehouse, COUNT(DISTINCT i.loadingQuotaNumber) as quotaCount,
            SUM(i.cargoWeight) as totalTonnage, COALESCE(SUM(loaded.loadedWeight), 0) as loadedTonnage,
            MAX(i.isActive) as isActive
        FROM InitialInfo i
        LEFT JOIN (
            SELECT c.loadingQuotaNumber, c.shipName, c.loadingWarehouse, c.shippingCompany, c.cargoType, SUM(c.netWeight) as loadedWeight
            FROM CargoInfo c WHERE c.status = 'خروج' AND c.shipName = ?
            GROUP BY c.loadingQuotaNumber, c.shipName, c.loadingWarehouse, c.shippingCompany, c.cargoType
        ) loaded ON
            loaded.loadingQuotaNumber = i.loadingQuotaNumber AND loaded.shipName = i.shipName
            AND loaded.loadingWarehouse = i.loadingWarehouse AND loaded.shippingCompany = i.shippingCompany
            AND loaded.cargoType = i.cargoType
        WHERE i.shipName = ?
        GROUP BY i.shipName, i.loadingWarehouse";

        $stmt = $this->db->prepare($query);
        $stmt->bind_param("ss", $shipName, $shipName);
        $stmt->execute();
        $result = $stmt->get_result();

        // totalVoucherCount مستقل از هر انبار است (فقط به shipName وابسته)؛
        // قبلاً به‌صورت زیرکوئری همبسته داخل SELECT اصلی بود و به ازای هر
        // ردیف گروه (هر انبار) دوباره اجرا می‌شد، با اینکه نتیجه‌اش همیشه
        // یکسان است. یک بار جدا محاسبه می‌شود.
        $voucherStmt = $this->db->prepare(
            "SELECT COUNT(DISTINCT trackingNumber) as totalVoucherCount FROM CargoInfo WHERE shipName = ? AND status = 'خروج'"
        );
        $voucherStmt->bind_param("s", $shipName);
        $voucherStmt->execute();
        $totalVoucherCount = (int)($voucherStmt->get_result()->fetch_assoc()['totalVoucherCount'] ?? 0);

        $warehouses = [];
        $totalQuotaCount = 0;
        $totalTonnage = 0;
        $totalRemainingTonnage = 0;
        $isActive = false;

        while ($row = $result->fetch_assoc()) {
            $warehouseTotalTonnage = floatval($row['totalTonnage']);
            $warehouseLoadedTonnage = floatval($row['loadedTonnage']);
            $warehouseRemainingTonnage = $warehouseTotalTonnage - $warehouseLoadedTonnage;

            $warehouses[] = [
                'name' => $row['loadingWarehouse'],
                'quotaCount' => intval($row['quotaCount']),
                'totalTonnage' => $warehouseTotalTonnage,
                'remainingTonnage' => $warehouseRemainingTonnage,
                'loadedTonnage' => $warehouseLoadedTonnage
            ];

            $totalQuotaCount += intval($row['quotaCount']);
            $totalTonnage += $warehouseTotalTonnage;
            $totalRemainingTonnage += $warehouseRemainingTonnage;
            // MAX(i.isActive) فقط داخل هر گروه (هر انبار) اعمال می‌شود، نه بین
            // انبارها؛ برای اینکه یک انبار کاملاً غیرفعال، کشتی‌ای با انبارهای
            // دیگر فعال را به‌اشتباه isActive=false نشان ندهد، نتیجه با OR
            // منطقی بین انبارها ترکیب می‌شود، نه بازنویسی ساده.
            $isActive = $isActive || (bool)$row['isActive'];
        }

        if (empty($warehouses)) {
            throw new ApiException("کشتی با نام '$shipName' یافت نشد.", 404, ['code' => 'SHIP_NOT_FOUND']);
        }

        $totalLoadedTonnage = $totalTonnage - $totalRemainingTonnage;

        return [
            'name' => $shipName,
            'warehouseCount' => count($warehouses),
            'quotaCount' => $totalQuotaCount,
            'totalTonnage' => $totalTonnage,
            'remainingTonnage' => $totalRemainingTonnage,
            'loadedTonnage' => $totalLoadedTonnage,
            'totalVoucherCount' => $totalVoucherCount,
            'isActive' => $isActive,
            'warehouses' => $warehouses
        ];
    }

    public function getWarehouseDetails(string $shipName, string $warehouseName): array {
        $shipName = $this->validateIdentifier($shipName);
        $warehouseName = $this->validateIdentifier($warehouseName);

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

        $exitDatesByQuota = [];
        $exitQuery = "SELECT DISTINCT loadingQuotaNumber, exitDate, exitTime FROM CargoInfo c WHERE c.shipName = ?
            AND c.loadingWarehouse = ? AND c.status = 'خروج' ORDER BY loadingQuotaNumber, exitDate, exitTime";
        $exitStmt = $this->db->prepare($exitQuery);
        $exitStmt->bind_param("ss", $shipName, $warehouseName);
        $exitStmt->execute();
        $exitResultAll = $exitStmt->get_result();
        while ($exitRow = $exitResultAll->fetch_assoc()) {
            $exitDatesByQuota[$exitRow['loadingQuotaNumber']][] = [
                'date' => $exitRow['exitDate'],
                'time' => $exitRow['exitTime']
            ];
        }

        $quotas = [];
        $totalTonnage = 0;
        $totalRemainingTonnage = 0;
        $totalLoadedTonnage = 0;
        $totalVoucherCount = 0;
        $allExitDates = [];
        $uniqueCargoTypes = [];
        $uniqueShippingCompanies = [];
        $uniqueCargoOwners = [];
        $seenCargoTypes = [];
        $seenShippingCompanies = [];
        $seenCargoOwners = [];
        $activeQuotasCount = 0;

        while ($row = $result->fetch_assoc()) {
            $quotaNumber = $row['loadingQuotaNumber'];
            $cargoType = $row['cargoType'];
            $shippingCompany = $row['shippingCompany'];
            $cargoOwner = $row['cargoOwner'] ?? 'نامشخص';
            $isActive = (bool)$row['isActive'];

            if (!isset($seenCargoTypes[$cargoType])) {
                $seenCargoTypes[$cargoType] = true;
                $uniqueCargoTypes[] = $cargoType;
            }
            if (!isset($seenShippingCompanies[$shippingCompany])) {
                $seenShippingCompanies[$shippingCompany] = true;
                $uniqueShippingCompanies[] = $shippingCompany;
            }
            if ($cargoOwner !== 'نامشخص' && !isset($seenCargoOwners[$cargoOwner])) {
                $seenCargoOwners[$cargoOwner] = true;
                $uniqueCargoOwners[] = $cargoOwner;
            }

            if ($isActive) {
                $activeQuotasCount++;
            }

            $quotaTotalTonnage = floatval($row['totalTonnage']);
            $quotaLoadedTonnage = floatval($row['loadedTonnage']);
            $quotaRemainingTonnage = $quotaTotalTonnage - $quotaLoadedTonnage;
            $percentageLoaded = ($quotaTotalTonnage > 0) ? ($quotaLoadedTonnage / $quotaTotalTonnage) * 100 : 0;

            $exitDates = $exitDatesByQuota[$quotaNumber] ?? [];
            foreach ($exitDates as $exitDateEntry) {
                $allExitDates[] = $exitDateEntry['date'];
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

        $startDate = substr($startDateTime, 0, 10);
        $startTime = substr($startDateTime, 11, 8);
        $endDate = substr($endDateTime, 0, 10);
        $endTime = substr($endDateTime, 11, 8);

        $dateRangeCondition = "(c.exitDate > ? OR (c.exitDate = ? AND c.exitTime >= ?))
            AND (c.exitDate < ? OR (c.exitDate = ? AND c.exitTime < ?))";

        $summaryQuery = "SELECT COALESCE(SUM(c.netWeight), 0) as totalNetWeight, COUNT(DISTINCT c.trackingNumber) as voucherCount,
            MIN(c.exitTime) as firstExitTime, MAX(c.exitTime) as lastExitTime, MIN(c.exitDate) as firstExitDate, MAX(c.exitDate) as lastExitDate
        FROM CargoInfo c
        WHERE c.loadingQuotaNumber = ? AND c.shipName = ? AND c.loadingWarehouse = ? AND c.status = 'خروج'
            AND $dateRangeCondition";

        $stmt = $this->db->prepare($summaryQuery);
        $stmt->bind_param("sssssssss", $selectedQuota, $shipName, $warehouseName, $startDate, $startDate, $startTime, $endDate, $endDate, $endTime);
        $stmt->execute();
        $summaryResult = $stmt->get_result();
        $summary = $summaryResult->fetch_assoc();

        $detailsQuery = "SELECT c.trackingNumber, c.entryTime, c.netWeight, c.exitTime, c.exitDate, c.scaleReceiptNumber,
            c.username, c.confirm_username, c.cargoType, c.shippingCompany, i.cargoOwner
        FROM CargoInfo c
        JOIN InitialInfo i ON c.loadingQuotaNumber = i.loadingQuotaNumber AND c.shipName = i.shipName
        WHERE c.loadingQuotaNumber = ? AND c.shipName = ? AND c.loadingWarehouse = ? AND c.status = 'خروج'
            AND $dateRangeCondition
        ORDER BY c.exitDate, c.exitTime";

        $stmtDetails = $this->db->prepare($detailsQuery);
        $stmtDetails->bind_param("sssssssss", $selectedQuota, $shipName, $warehouseName, $startDate, $startDate, $startTime, $endDate, $endDate, $endTime);
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

            if (!empty($row['username'])) {
                $uniqueUsers[$row['username']] = true;
            }
            if (!empty($row['confirm_username'])) {
                $uniqueUsers[$row['confirm_username']] = true;
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
            FROM CargoInfo c WHERE c.status = 'خروج' AND c.loadingQuotaNumber = ?
            GROUP BY c.loadingQuotaNumber, c.shipName, c.loadingWarehouse, c.shippingCompany, c.cargoType
        ) exit_data ON
            exit_data.loadingQuotaNumber = i.loadingQuotaNumber AND exit_data.shipName = i.shipName
            AND exit_data.loadingWarehouse = i.loadingWarehouse AND exit_data.shippingCompany = i.shippingCompany
            AND exit_data.cargoType = i.cargoType
        WHERE i.loadingQuotaNumber = ?";

        $stmt = $this->db->prepare($query);
        $stmt->bind_param("ss", $quotaNumber, $quotaNumber);
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
        $shipName = $this->validateIdentifier($shipName);
        $startDateTime = $this->validateIdentifier($startDateTime);
        $endDateTime = $this->validateIdentifier($endDateTime);

        if (strlen($startDateTime) === 16) {
            $startDateTime .= ':00';
        }
        if (strlen($endDateTime) === 16) {
            $endDateTime .= ':00';
        }

        // all_vouchers قبلاً یک LEFT JOIN مستقل با همان شرط/GROUP BY/ON exit_data
        // بود و دقیقاً همان عدد را دوباره محاسبه می‌کرد؛ حذف شد (نگاه کنید به
        // computeQuotasList برای همین اصلاح).
        $query = "SELECT i.id, i.loadingQuotaNumber as number, i.shipName, i.loadingWarehouse, i.cargoType,
            i.cargoWeight as totalTonnage, i.isActive, i.shippingCompany, i.cargoOwner, i.percentage, i.is_enabled,
            COALESCE(exit_data.loadedTonnage, 0) as loadedTonnage,
            COALESCE(exit_data.exitVoucherCount, 0) as exitVoucherCount
        FROM InitialInfo i
        LEFT JOIN (
            SELECT c.loadingQuotaNumber, c.shipName, c.loadingWarehouse, c.shippingCompany, c.cargoType,
                SUM(c.netWeight) as loadedTonnage, COUNT(DISTINCT c.trackingNumber) as exitVoucherCount
            FROM CargoInfo c WHERE c.status = 'خروج' AND c.shipName = ?
                AND ((c.exitDate > ? OR (c.exitDate = ? AND c.exitTime >= ?)) AND (c.exitDate < ? OR (c.exitDate = ? AND c.exitTime <= ?)))
            GROUP BY c.loadingQuotaNumber, c.shipName, c.loadingWarehouse, c.shippingCompany, c.cargoType
        ) exit_data ON
            exit_data.loadingQuotaNumber = i.loadingQuotaNumber AND exit_data.shipName = i.shipName
            AND exit_data.loadingWarehouse = i.loadingWarehouse AND exit_data.shippingCompany = i.shippingCompany
            AND exit_data.cargoType = i.cargoType
        WHERE i.shipName = ?
        ORDER BY i.isActive DESC, i.loadingQuotaNumber ASC";

        $startDate = substr($startDateTime, 0, 10);
        $startTime = substr($startDateTime, 11, 8);
        $endDate = substr($endDateTime, 0, 10);
        $endTime = substr($endDateTime, 11, 8);

        $stmt = $this->db->prepare($query);
        $stmt->bind_param("ssssssss",
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
            $exitVoucherCount = intval($row['exitVoucherCount']);

            $quotas[] = [
                'id' => (int)$row['id'],
                'number' => $row['number'],
                'shipName' => $row['shipName'] ?? '',
                'warehouse' => $row['loadingWarehouse'] ?? '',
                'cargoType' => $row['cargoType'] ?? '',
                'totalTonnage' => $totalTonnage,
                'remainingTonnage' => $remainingTonnage,
                'loadedTonnage' => $loadedTonnage,
                'voucherCount' => $exitVoucherCount,
                'isActive' => (bool)$row['isActive'],
                'shippingCompany' => $row['shippingCompany'] ?? '',
                'cargoOwner' => $row['cargoOwner'] ?? '',
                'percentage' => $percentage,
                'isPercentageRestricted' => $isPercentageRestricted
            ];
        }
        return $quotas;
    }

    public function getAllQuotasList(?string $shipName = null): array {
        $query = "SELECT id, loadingQuotaNumber as number, shipName, loadingWarehouse, cargoType, cargoWeight as totalTonnage,
            isActive, shippingCompany, cargoOwner, percentage, is_enabled, temp_tonnage_status, temp_tonnage_amount
        FROM InitialInfo";
        if ($shipName !== null && $shipName !== '') {
            $query .= " WHERE shipName = ?";
        }
        $query .= " ORDER BY shipName ASC, isActive DESC, loadingQuotaNumber ASC";

        $stmt = $this->db->prepare($query);
        if ($shipName !== null && $shipName !== '') {
            $stmt->bind_param("s", $shipName);
        }
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
        $shipName = $this->validateIdentifier($shipName);

        return MicroCache::remember($this->quotasListCacheKey($shipName), 6, function () use ($shipName) {
            return $this->computeQuotasList($shipName);
        });
    }

    private function computeQuotasList(string $shipName): array {
        // all_vouchers قبلاً یک LEFT JOIN مستقل با همان شرط/GROUP BY/ON
        // exit_data بود و دقیقاً همان عدد را دوباره محاسبه می‌کرد (COUNT
        // DISTINCT trackingNumber با status='خروج')؛ حذف شد و exitVoucherCount
        // برای هر دو فیلد voucherCount/exitVoucherCount استفاده می‌شود.
        $query = "SELECT i.id, i.loadingQuotaNumber as number, i.shipName, i.loadingWarehouse, i.cargoType, i.cargoWeight as totalTonnage, i.isActive, i.shippingCompany, i.cargoOwner, i.percentage, i.is_enabled,
            COALESCE(exit_data.loadedTonnage, 0) as loadedTonnage, COALESCE(exit_data.exitVoucherCount, 0) as exitVoucherCount
        FROM InitialInfo i
        LEFT JOIN (
            SELECT c.loadingQuotaNumber, c.shipName, c.loadingWarehouse, c.shippingCompany, c.cargoType, SUM(c.netWeight) as loadedTonnage, COUNT(DISTINCT c.trackingNumber) as exitVoucherCount
            FROM CargoInfo c WHERE c.status = 'خروج' AND c.shipName = ?
            GROUP BY c.loadingQuotaNumber, c.shipName, c.loadingWarehouse, c.shippingCompany, c.cargoType
        ) exit_data ON
            exit_data.loadingQuotaNumber = i.loadingQuotaNumber AND exit_data.shipName = i.shipName
            AND exit_data.loadingWarehouse = i.loadingWarehouse AND exit_data.shippingCompany = i.shippingCompany
            AND exit_data.cargoType = i.cargoType
        WHERE i.shipName = ?
        ORDER BY i.isActive DESC, i.loadingQuotaNumber ASC";

        $stmt = $this->db->prepare($query);
        $stmt->bind_param("ss", $shipName, $shipName);
        $stmt->execute();
        $result = $stmt->get_result();
        $quotas = [];

        while ($row = $result->fetch_assoc()) {
            $loadedTonnage = floatval($row['loadedTonnage']);
            $totalTonnage = floatval($row['totalTonnage']);
            $remainingTonnage = $totalTonnage - $loadedTonnage;
            $percentage = $row['percentage'] !== null ? floatval($row['percentage']) : null;
            $isPercentageRestricted = (bool)$row['is_enabled'];
            $exitVoucherCount = intval($row['exitVoucherCount']);

            $quotas[] = [
                'id' => (int)$row['id'],
                'number' => $row['number'],
                'shipName' => $row['shipName'] ?? '',
                'warehouse' => $row['loadingWarehouse'] ?? '',
                'cargoType' => $row['cargoType'] ?? '',
                'totalTonnage' => $totalTonnage,
                'remainingTonnage' => $remainingTonnage,
                'loadedTonnage' => $loadedTonnage,
                'voucherCount' => $exitVoucherCount,
                'isActive' => (bool)$row['isActive'],
                'shippingCompany' => $row['shippingCompany'] ?? '',
                'cargoOwner' => $row['cargoOwner'] ?? '',
                'percentage' => $percentage,
                'isPercentageRestricted' => $isPercentageRestricted
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
            FROM CargoInfo c WHERE c.status = 'خروج' AND c.loadingQuotaNumber = ?
            GROUP BY c.loadingQuotaNumber, c.shipName, c.loadingWarehouse, c.shippingCompany, c.cargoType
        ) exit_data ON
            exit_data.loadingQuotaNumber = i.loadingQuotaNumber AND exit_data.shipName = i.shipName
            AND exit_data.loadingWarehouse = i.loadingWarehouse AND exit_data.shippingCompany = i.shippingCompany
            AND exit_data.cargoType = i.cargoType
        WHERE " . implode(" AND ", $whereConditions) . " LIMIT 1";

        $derivedParams = array_merge([$quotaNumber], $params);
        $derivedTypes = "s" . $types;

        $stmt = $this->db->prepare($query);
        $stmt->bind_param($derivedTypes, ...$derivedParams);
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
            MicroCache::forget(MicroCache::SHIPS_LIST_KEY);
            MicroCache::forget($this->shipDetailsCacheKey($oldData['shipName']));
            MicroCache::forget($this->quotasListCacheKey($oldData['shipName']));
            if ($shipName !== $oldData['shipName']) {
                MicroCache::forget($this->shipDetailsCacheKey($shipName));
                MicroCache::forget($this->quotasListCacheKey($shipName));
            }
            return true;
        } catch (Exception $e) {
            $this->db->rollback();
            throw new Exception("خطا در ویرایش کوتاژ: " . $e->getMessage());
        }
    }

    // هر سه تابع زیر عمداً فقط با id (کلید یکتای InitialInfo) کار می‌کنند، نه
    // loadingQuotaNumber که یکتا نیست. اگر بر اساس شماره کوتاژ فیلتر شود، عملیات
    // روی تمام ردیف‌های هم‌شماره (حتی متعلق به کشتی/انبار/شرکت دیگر) اعمال می‌شود.
    /**
     * نام کشتی مرتبط با یک ردیف InitialInfo، فقط برای invalidate کردن کش
     * per-ship بعد از یک نوشتن که تنها id را دارد (نه shipName).
     */
    private function getShipNameById(int $id): ?string {
        $stmt = $this->db->prepare("SELECT shipName FROM InitialInfo WHERE id = ?");
        $stmt->bind_param("i", $id);
        $stmt->execute();
        $row = $stmt->get_result()->fetch_assoc();
        return $row['shipName'] ?? null;
    }

    private function forgetShipCaches(?string $shipName): void {
        if ($shipName === null) {
            return;
        }
        MicroCache::forget($this->shipDetailsCacheKey($shipName));
        MicroCache::forget($this->quotasListCacheKey($shipName));
    }

    public function updateQuotaPercentage(int $id, float $percentage): bool {
        $shipName = $this->getShipNameById($id);
        $isEnabled = ($percentage > 0.00) ? 1 : 0;
        $query = "UPDATE InitialInfo SET percentage = ?, is_enabled = ? WHERE id = ?";
        $stmt = $this->db->prepare($query);
        $stmt->bind_param("dii", $percentage, $isEnabled, $id);
        $success = $stmt->execute();
        if ($success) {
            $this->forgetShipCaches($shipName);
        }
        return $success;
    }

    public function toggleQuotaStatus(int $id): bool {
        $shipName = $this->getShipNameById($id);
        $query = "UPDATE InitialInfo SET isActive = NOT isActive WHERE id = ?";
        $stmt = $this->db->prepare($query);
        $stmt->bind_param("i", $id);
        $success = $stmt->execute();
        if ($success) {
            MicroCache::forget(MicroCache::SHIPS_LIST_KEY);
            $this->forgetShipCaches($shipName);
        }
        return $success;
    }

    public function updateQuotaPercentageRestriction(int $id, int $isEnabled): bool {
        $shipName = $this->getShipNameById($id);
        $query = "UPDATE InitialInfo SET is_enabled = ? WHERE id = ?";
        $stmt = $this->db->prepare($query);
        $stmt->bind_param("ii", $isEnabled, $id);
        $success = $stmt->execute();
        if ($success) {
            $this->forgetShipCaches($shipName);
        }
        return $success;
    }

    public function deleteQuota(string $quotaNumber, string $shipName, string $warehouse, string $shippingCompany, string $cargoType): bool {
        try {
            $quotaNumber = $this->validateIdentifier($quotaNumber);
            $shipName = $this->validateIdentifier($shipName);
            $warehouse = $this->validateIdentifier($warehouse);
            $shippingCompany = $this->validateIdentifier($shippingCompany);
            $cargoType = $this->validateIdentifier($cargoType);

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

            $cargoQuery = "DELETE FROM CargoInfo WHERE loadingQuotaNumber = ? AND shipName = ? AND loadingWarehouse = ? AND shippingCompany = ? AND cargoType = ?";
            $cargoStmt = $this->db->prepare($cargoQuery);
            $cargoStmt->bind_param("sssss", $quotaNumber, $shipName, $warehouse, $shippingCompany, $cargoType);
            $cargoStmt->execute();

            $initialQuery = "DELETE FROM InitialInfo WHERE loadingQuotaNumber = ? AND shipName = ? AND loadingWarehouse = ? AND shippingCompany = ? AND cargoType = ?";
            $initialStmt = $this->db->prepare($initialQuery);
            $initialStmt->bind_param("sssss", $quotaNumber, $shipName, $warehouse, $shippingCompany, $cargoType);
            $initialStmt->execute();

            $this->db->commit();
            MicroCache::forget(MicroCache::SHIPS_LIST_KEY);
            $this->forgetShipCaches($shipName);
            return true;
        } catch (Exception $e) {
            $this->db->rollback();
            throw new Exception("خطا در حذف کوتاژ: " . $e->getMessage());
        }
    }

    public function getGroupedQuotas(?string $shipName = null): array {
        $quotas = $this->getAllQuotasList($shipName);
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
        $quotaNumber = $this->validateIdentifier($quotaNumber);

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
