<?php
// PHP/src/Controllers/AppApiController.php

declare(strict_types=1);

namespace App\Controllers;

use App\Core\AuthenticatesRequests;
use App\Core\Request;
use App\Core\Response;
use App\Exceptions\ApiException;
use App\Services\QuotaService;
use App\Services\ShipService;
use App\Validators\InputValidator;

/**
 * فقط مسئول احراز هویت/routing/پارس درخواست است (C-05) — منطق تجاری واقعی
 * (کوئری‌های دیتابیس، محاسبات) به ShipService/QuotaService/QuotaCalculator
 * منتقل شده. متدهای عمومی این کلاس (getShipsList، updateQuotaPercentage و
 * غیره) عمداً حفظ شده‌اند — فقط delegate می‌کنند — چون routes/api_v2.php
 * مستقیماً همین امضاها را صدا می‌زند و نباید بشکند.
 */
class AppApiController {
    use AuthenticatesRequests;

    private Request $request;
    private ShipService $shipService;
    private QuotaService $quotaService;

    public function __construct() {
        $this->request = new Request();
        $this->shipService = new ShipService();
        $this->quotaService = new QuotaService();
    }

    /**
     * کلاینت این کنترلر (ReportsRepository سمت اندروید) پاسخ خطا را با شکل
     * {"error": "متن پیام"} می‌خواند (نه {"error": true, "message": "..."}
     * که کلاینت CargoController/UtilityController می‌خواند) — override می‌کند
     * تا AuthenticatesRequests بتواند منطق مشترک را بدون شکستن این قرارداد
     * سرویس دهد.
     */
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

    // actionهای خواندنی که فقط توسط فیچر گزارش‌ها (ReportsRepository سمت
    // اندروید) مصرف می‌شوند و باید پشت مجوز view_reports قفل شوند
    // (DEEP_CODE_AUDIT.md #Phase1.4 — Broken Function Level Authorization).
    // عمداً «getQuotasList» در این لیست نیست: برخلاف پیشنهاد اولیه‌ی گزارش،
    // همین action در QuotaValidationUseCase.kt هنگام ثبت حواله (نه فقط
    // گزارش‌گیری) هم صدا زده می‌شود؛ قفل کردن آن پشت view_reports باعث
    // می‌شد نقش‌های operator/verifier (که view_reports ندارند) نتوانند
    // حواله ثبت کنند. «getRealTimeData» اینجا هم در لیست هست چون نسخه‌ی
    // معادل و مصرف‌شده‌ی آن (AnalyticsController::handleRealTimeLoadingData)
    // از قبل پشت view_reports است؛ این نسخه اصلاً از کلاینت صدا زده نمی‌شود.
    private const READ_ACTIONS_REQUIRING_REPORTS = [
        'getShipsList',
        'getShipDetails',
        'getWarehouseDetails',
        'getQuotaDetails',
        'getFilteredQuotas',
        'getFilteredSummary',
        'getGroupedQuotas',
        'getRealTimeData',
    ];

    // هیچ‌جای دیگری (route/shim) دیگر این متد را صدا نمی‌زند — تأیید شد حین
    // Phase3 #21؛ Router مستقیماً متدهای عمومی زیر را از طریق $safeCall در
    // routes/api_v2.php صدا می‌زند، نه این dispatcher قدیمی. پاسخ‌های خطای
    // داخل همین متد به Response::error() یکسان شدند چون دسترس‌ناپذیر بودن
    // آن‌ها تأیید شد؛ خودِ حذف متد خارج از دامنه‌ی این تغییر ماند.
    public function handle(): void {
        try {
            if (!$this->request->isGet() && !$this->request->isPost()) {
                throw new ApiException('روش درخواست نامعتبر است', 400);
            }

            $this->requireAuthenticatedSession();

            $action = $this->request->get('action');
            if (!$action) {
                throw new ApiException('عملیات مشخص نشده است', 400);
            }

            $action = InputValidator::sanitize((string)$action);

            $isWriteAction = in_array($action, self::WRITE_ACTIONS, true);
            if ($isWriteAction && !$this->request->isPost()) {
                throw new ApiException('این عملیات باید با متد POST ارسال شود', 400);
            }
            if (!$isWriteAction && !$this->request->isGet()) {
                throw new ApiException('این عملیات باید با متد GET ارسال شود', 400);
            }

            if (in_array($action, self::READ_ACTIONS_REQUIRING_REPORTS, true)) {
                $this->requirePermission('view_reports');
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
                        throw new ApiException('نام کشتی مشخص نشده است', 400);
                    }
                    $shipDetails = $this->getShipDetails((string)$shipName);
                    $this->sendCacheableJsonResponse($shipDetails, $shipDetails, 6);
                    break;

                case 'getWarehouseDetails':
                    $shipName = $this->request->get('shipName');
                    $warehouseName = $this->request->get('warehouseName');
                    if (!$shipName || !$warehouseName) {
                        throw new ApiException('نام کشتی یا انبار مشخص نشده است', 400);
                    }
                    $warehouseDetails = $this->getWarehouseDetails((string)$shipName, (string)$warehouseName);
                    Response::json($warehouseDetails);
                    break;

                case 'getQuotaDetails':
                    $quotaNumber = $this->request->get('quotaNumber');
                    if (!$quotaNumber) {
                        throw new ApiException('شماره کوتاژ مشخص نشده است', 400);
                    }
                    $quotaDetails = $this->getQuotaDetails((string)$quotaNumber);
                    if ($quotaDetails === null) {
                        Response::error('کوتاژ مورد نظر یافت نشد', 404);
                    } else {
                        Response::json($quotaDetails);
                    }
                    break;

                case 'getQuotasList':
                    $shipName = $this->request->get('shipName');
                    if (!$shipName) {
                        throw new ApiException('نام کشتی مشخص نشده است', 400);
                    }
                    $quotasList = $this->getQuotasList((string)$shipName);
                    $this->sendCacheableJsonResponse($quotasList, $quotasList, 6);
                    break;

                case 'getFilteredQuotas':
                    $shipName = $this->request->get('shipName');
                    $startDateTime = $this->request->get('startDateTime');
                    $endDateTime = $this->request->get('endDateTime');
                    if (!$shipName || !$startDateTime || !$endDateTime) {
                        throw new ApiException('پارامترهای ورودی ناقص هستند - نام کشتی، تاریخ شروع و پایان الزامی است', 400);
                    }
                    $filteredQuotas = $this->getFilteredQuotas((string)$shipName, (string)$startDateTime, (string)$endDateTime);
                    Response::json($filteredQuotas);
                    break;

                case 'getFilteredSummary':
                    $shipName = $this->request->get('shipName');
                    $warehouseName = $this->request->get('warehouseName');
                    $selectedQuota = $this->request->get('selectedQuota');
                    $startDateTime = $this->request->get('startDateTime');
                    $endDateTime = $this->request->get('endDateTime');
                    if (!$shipName || !$warehouseName || !$selectedQuota || !$startDateTime || !$endDateTime) {
                        throw new ApiException('پارامترهای ورودی ناقص هستند', 400);
                    }
                    $filteredSummary = $this->getFilteredSummary(
                        (string)$shipName,
                        (string)$warehouseName,
                        (string)$selectedQuota,
                        (string)$startDateTime,
                        (string)$endDateTime
                    );
                    // فشرده‌سازی PHP-level حذف شد (P-06): .htaccess از قبل
                    // mod_deflate را برای application/json فعال کرده؛ فشرده‌سازی
                    // دوباره اینجا فقط CPU اضافه بدون فایده بود.
                    header('Content-Type: application/json; charset=UTF-8');
                    echo $filteredSummary;
                    exit;

                case 'checkQuotaExistence':
                case 'checkQuotaExistenceCargo':
                    $quotaNumber = $this->request->get('quotaNumber');
                    $shipName = $this->request->get('shipName');
                    if (!$quotaNumber || !$shipName) {
                        throw new ApiException('شماره کوتاژ یا نام کشتی مشخص نشده است', 400);
                    }
                    $result = $this->checkQuotaExistenceCargo((string)$quotaNumber, (string)$shipName);
                    Response::json($result);
                    break;

                case 'getGroupedQuotas':
                    $shipNameFilter = $this->request->get('shipName');
                    $shipNameFilter = ($shipNameFilter !== null && trim((string)$shipNameFilter) !== '')
                        ? InputValidator::sanitize((string)$shipNameFilter)
                        : null;
                    $groupedQuotas = $this->getGroupedQuotas($shipNameFilter);
                    Response::json($groupedQuotas);
                    break;

                case 'updateTemporaryTonnage':
                    $this->requirePermission('manage_quotas');
                    $quotaNumber = $this->request->get('quotaNumber');
                    $enabled = $this->request->get('enabled');
                    if ($quotaNumber === null || $enabled === null) {
                        throw new ApiException('پارامترهای ورودی ناقص هستند', 400);
                    }
                    $enabledVal = intval($enabled);
                    $tonnage = $this->request->get('tonnage');
                    if ($enabledVal === 1 && $tonnage === null) {
                        throw new ApiException('مقدار تناژ موقت الزامی است', 400);
                    }
                    $tonnageVal = $tonnage !== null ? floatval($tonnage) : null;
                    $result = $this->updateTemporaryTonnage((string)$quotaNumber, $enabledVal, $tonnageVal);
                    Response::json($result);
                    break;

                case 'editQuota':
                    $this->requirePermission('manage_quotas');
                    $params = $this->request->all();
                    $required = ['id', 'oldQuotaNumber', 'newQuotaNumber', 'shipName', 'shippingCompany', 'warehouse', 'cargoType', 'totalTonnage'];
                    foreach ($required as $field) {
                        if (!isset($params[$field])) {
                            throw new ApiException('پارامترهای ورودی ناقص هستند', 400);
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
                    Response::json(['success' => $result]);
                    break;

                case 'updateQuotaPercentage':
                    $this->requirePermission('manage_quotas');
                    $id = $this->request->get('id');
                    $percentage = $this->request->get('percentage');
                    if (!$id || intval($id) <= 0 || $percentage === null) {
                        throw new ApiException('شناسه کوتاژ یا درصد مشخص نشده است', 400);
                    }
                    $result = $this->updateQuotaPercentage(intval($id), floatval($percentage));
                    Response::json(['success' => $result]);
                    break;

                case 'toggleQuotaStatus':
                    $this->requirePermission('manage_quotas');
                    $id = $this->request->get('id');
                    if (!$id || intval($id) <= 0) {
                        throw new ApiException('شناسه کوتاژ مشخص نشده است', 400);
                    }
                    $result = $this->toggleQuotaStatus(intval($id));
                    Response::json(['success' => $result]);
                    break;

                case 'updateQuotaPercentageRestriction':
                    $this->requirePermission('manage_quotas');
                    $id = $this->request->get('id');
                    $isEnabled = $this->request->get('isEnabled');
                    if (!$id || intval($id) <= 0 || $isEnabled === null) {
                        throw new ApiException('شناسه کوتاژ یا مقدار محدودیت مشخص نشده است', 400);
                    }
                    $result = $this->updateQuotaPercentageRestriction(intval($id), intval($isEnabled));
                    Response::json(['success' => $result]);
                    break;

                case 'deleteQuota':
                    $this->requirePermission('manage_quotas');
                    $quotaNumber = $this->request->get('quotaNumber');
                    $shipName = $this->request->get('shipName');
                    $warehouse = $this->request->get('warehouse');
                    $shippingCompany = $this->request->get('shippingCompany');
                    $cargoType = $this->request->get('cargoType');
                    if (!$quotaNumber || !$shipName || !$warehouse || !$shippingCompany || !$cargoType) {
                        throw new ApiException('پارامترهای ورودی ناقص هستند', 400);
                    }
                    $result = $this->deleteQuota(
                        (string)$quotaNumber,
                        (string)$shipName,
                        (string)$warehouse,
                        (string)$shippingCompany,
                        (string)$cargoType
                    );
                    Response::json(['success' => $result]);
                    break;

                case 'getLoadableTonnage':
                    $quotaNumber = $this->request->get('quotaNumber');
                    if (!$quotaNumber) {
                        throw new ApiException('شماره کوتاژ مشخص نشده است', 400);
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
                    Response::json($result);
                    break;

                case 'checkQuotaStatus':
                    $quotaNumber = $this->request->get('quotaNumber');
                    if (!$quotaNumber) {
                        throw new ApiException('شماره کوتاژ مشخص نشده است', 400);
                    }
                    $params = [
                        'quotaNumber' => (string)$quotaNumber,
                        'shipName' => (string)$this->request->get('shipName', ''),
                        'cargoType' => (string)$this->request->get('cargoType', ''),
                        'shippingCompany' => (string)$this->request->get('shippingCompany', ''),
                        'warehouse' => (string)$this->request->get('warehouse', '')
                    ];
                    $status = $this->checkQuotaStatus($params);
                    Response::json($status);
                    break;

                case 'getRealTimeData':
                    $shipsList = $this->getShipsList();
                    $realTimeData = [
                        'ships' => $shipsList['data']['activeShips'] ?? [],
                        'timestamp' => date('Y-m-d H:i:s'),
                        'status' => 'success'
                    ];
                    Response::json($realTimeData);
                    break;

                default:
                    throw new ApiException('عملیات نامعتبر است', 400);
            }
        } catch (ApiException $e) {
            Response::error($e->getMessage(), $e->getStatusCode(), $e->getDetails());
        } catch (\Throwable $e) {
            // فقط ApiException (پیام‌های فارسی عمدی) به کلاینت می‌رود؛ بقیه
            // (مثل خطای خام دیتابیس از ShipService/QuotaService) فقط لاگ
            // می‌شود تا ساختار جدول/کوئری افشا نشود (DEEP_CODE_AUDIT.md
            // #Phase2.4).
            error_log('AppApiController: ' . $e->getMessage());
            Response::error('خطای داخلی سرور رخ داده است.', 500);
        }
    }

    /**
     * فقط برای پاسخ‌های GET غیرقابل تغییر (idempotent) استفاده شود، هرگز برای
     * عملیات نوشتن/حذف. ETag از $etagSource (نه کل $data) محاسبه می‌شود تا
     * فیلدهای همیشه‌متغیر (مثل timestamp) باعث نادیده گرفتن کش نشوند.
     */
    // public (نه private) چون route handlerهای api/v2 (خارج از این کلاس) هم
    // برای همان endpointهای پرکاربرد (getShipsList/getShipDetails/getQuotasList)
    // به همین کش/ETag نیاز دارند و نباید آن را دوباره‌نویسی کنند.
    public function sendCacheableJsonResponse($data, array $etagSource, int $maxAgeSeconds): void {
        $etag = '"' . md5(json_encode($etagSource, JSON_UNESCAPED_UNICODE)) . '"';
        header('Cache-Control: private, max-age=' . $maxAgeSeconds);
        header("ETag: $etag");

        $ifNoneMatch = $this->request->getHeader('If-None-Match');
        if ($ifNoneMatch !== null && trim($ifNoneMatch) === $etag) {
            http_response_code(304);
            exit;
        }

        Response::json($data);
    }

    // ===== زیر این خط: delegate خالص به ShipService/QuotaService (C-05).
    // امضاها عمداً دست‌نخورده مانده‌اند چون routes/api_v2.php مستقیماً همین
    // متدها را صدا می‌زند. =====

    public function checkQuotaStatus(array $params): array {
        return $this->quotaService->checkQuotaStatus($params);
    }

    public function checkQuotaExistenceCargo(string $quotaNumber, string $shipName): array {
        return $this->quotaService->checkQuotaExistenceCargo($quotaNumber, $shipName);
    }

    public function getShipsList(): array {
        return $this->shipService->getShipsList();
    }

    public function getShipDetails(string $shipName): array {
        return $this->shipService->getShipDetails($shipName);
    }

    public function getWarehouseDetails(string $shipName, string $warehouseName): array {
        return $this->shipService->getWarehouseDetails($shipName, $warehouseName);
    }

    public function getFilteredSummary(string $shipName, string $warehouseName, string $selectedQuota, string $startDateTime, string $endDateTime): string {
        return $this->shipService->getFilteredSummary($shipName, $warehouseName, $selectedQuota, $startDateTime, $endDateTime);
    }

    public function getQuotaDetails(string $quotaNumber): ?array {
        return $this->quotaService->getQuotaDetails($quotaNumber);
    }

    public function getFilteredQuotas(string $shipName, string $startDateTime, string $endDateTime): array {
        return $this->quotaService->getFilteredQuotas($shipName, $startDateTime, $endDateTime);
    }

    public function getAllQuotasList(?string $shipName = null): array {
        return $this->quotaService->getAllQuotasList($shipName);
    }

    public function getQuotasList(string $shipName): array {
        return $this->quotaService->getQuotasList($shipName);
    }

    public function getLoadableTonnage(string $quotaNumber, string $shippingCompany = '', string $warehouse = '', string $cargoType = ''): array {
        return $this->quotaService->getLoadableTonnage($quotaNumber, $shippingCompany, $warehouse, $cargoType);
    }

    /**
     * ۶ متد نوشتنی زیر همگی با requireAuthenticatedSession() شروع می‌شوند —
     * نه برای احراز هویت (Router::dispatch از قبل با ApiAuthGate این کار را
     * کرده)، بلکه چون این تنها راه پر شدن $this->authenticatedUsername روی
     * این نمونه‌ی AppApiController است. handle() (تنها جای دیگری که این
     * مقدار را پر می‌کرد) از حذف app_api.php در فاز ۳.۱ دیگر هرگز صدا زده
     * نمی‌شود، پس بدون این فراخوانی $this->authenticatedUsername همیشه null
     * می‌ماند و AuditLogger::log در QuotaService (که با actorUsername!==null
     * گیت شده) بی‌صدا هیچ‌وقت اجرا نمی‌شود — دقیقاً همان چیزی که رخ می‌داد
     * (کشف‌شده هنگام بررسی خالی‌ماندن audit_log برای عملیات کوتاژ،
     * DEEP_CODE_AUDIT.md). این همان الگوی «بررسی دوگانه‌ی واقعی» است که
     * برای CargoController::saveOrUpdate در routes/api_v2.php مستند شده.
     */
    public function editQuota(int $id, string $oldQuotaNumber, string $newQuotaNumber, string $shipName, string $shippingCompany, string $warehouse, string $cargoType, float $totalTonnage): bool {
        $this->requireAuthenticatedSession();
        return $this->quotaService->editQuota($id, $oldQuotaNumber, $newQuotaNumber, $shipName, $shippingCompany, $warehouse, $cargoType, $totalTonnage, $this->authenticatedUsername);
    }

    public function updateQuotaPercentage(int $id, float $percentage): bool {
        $this->requireAuthenticatedSession();
        return $this->quotaService->updateQuotaPercentage($id, $percentage, $this->authenticatedUsername);
    }

    public function toggleQuotaStatus(int $id): bool {
        $this->requireAuthenticatedSession();
        return $this->quotaService->toggleQuotaStatus($id, $this->authenticatedUsername);
    }

    public function updateQuotaPercentageRestriction(int $id, int $isEnabled): bool {
        $this->requireAuthenticatedSession();
        return $this->quotaService->updateQuotaPercentageRestriction($id, $isEnabled, $this->authenticatedUsername);
    }

    public function deleteQuota(string $quotaNumber, string $shipName, string $warehouse, string $shippingCompany, string $cargoType): bool {
        $this->requireAuthenticatedSession();
        return $this->quotaService->deleteQuota($quotaNumber, $shipName, $warehouse, $shippingCompany, $cargoType, $this->authenticatedUsername);
    }

    public function getGroupedQuotas(?string $shipName = null): array {
        return $this->quotaService->getGroupedQuotas($shipName);
    }

    public function updateTemporaryTonnage(string $quotaNumber, int $enabledVal, ?float $tonnageVal): array {
        $this->requireAuthenticatedSession();
        return $this->quotaService->updateTemporaryTonnage($quotaNumber, $enabledVal, $tonnageVal, $this->authenticatedUsername);
    }
}
