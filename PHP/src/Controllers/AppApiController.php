<?php
// PHP/src/Controllers/AppApiController.php

declare(strict_types=1);

namespace App\Controllers;

use Exception;
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
    protected function sendAuthErrorResponse(string $message, int $httpCode): void {
        header('Content-Type: application/json; charset=UTF-8');
        http_response_code($httpCode);
        echo json_encode(['error' => $message], JSON_UNESCAPED_UNICODE);
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

            $action = InputValidator::sanitize((string)$action);

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
                    Response::json($warehouseDetails);
                    break;

                case 'getQuotaDetails':
                    $quotaNumber = $this->request->get('quotaNumber');
                    if (!$quotaNumber) {
                        throw new Exception('شماره کوتاژ مشخص نشده است');
                    }
                    $quotaDetails = $this->getQuotaDetails((string)$quotaNumber);
                    if ($quotaDetails === null) {
                        Response::json(['error' => 'کوتاژ مورد نظر یافت نشد'], 404);
                    } else {
                        Response::json($quotaDetails);
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
                    Response::json($filteredQuotas);
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
                        throw new Exception('شماره کوتاژ یا نام کشتی مشخص نشده است');
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
                        throw new Exception('پارامترهای ورودی ناقص هستند');
                    }
                    $enabledVal = intval($enabled);
                    $tonnage = $this->request->get('tonnage');
                    if ($enabledVal === 1 && $tonnage === null) {
                        throw new Exception('مقدار تناژ موقت الزامی است');
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
                    Response::json(['success' => $result]);
                    break;

                case 'updateQuotaPercentage':
                    $this->requirePermission('manage_quotas');
                    $id = $this->request->get('id');
                    $percentage = $this->request->get('percentage');
                    if (!$id || intval($id) <= 0 || $percentage === null) {
                        throw new Exception('شناسه کوتاژ یا درصد مشخص نشده است');
                    }
                    $result = $this->updateQuotaPercentage(intval($id), floatval($percentage));
                    Response::json(['success' => $result]);
                    break;

                case 'toggleQuotaStatus':
                    $this->requirePermission('manage_quotas');
                    $id = $this->request->get('id');
                    if (!$id || intval($id) <= 0) {
                        throw new Exception('شناسه کوتاژ مشخص نشده است');
                    }
                    $result = $this->toggleQuotaStatus(intval($id));
                    Response::json(['success' => $result]);
                    break;

                case 'updateQuotaPercentageRestriction':
                    $this->requirePermission('manage_quotas');
                    $id = $this->request->get('id');
                    $isEnabled = $this->request->get('isEnabled');
                    if (!$id || intval($id) <= 0 || $isEnabled === null) {
                        throw new Exception('شناسه کوتاژ یا مقدار محدودیت مشخص نشده است');
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
                        throw new Exception('پارامترهای ورودی ناقص هستند');
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
                    Response::json($result);
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
                    throw new Exception('عملیات نامعتبر است');
            }
        } catch (ApiException $e) {
            // ApiException برای خطاهایی که کد وضعیت HTTP معنادار دارند (مثلاً
            // «یافت نشد» → 404) استفاده می‌شود؛ کلاینت به‌جای تطبیق رشته‌ی
            // فارسی پیام خطا، بر اساس details['code'] یا کد وضعیت تصمیم می‌گیرد.
            Response::json(
                ['error' => $e->getMessage()] + ($e->getDetails() ?? []),
                $e->getStatusCode()
            );
        } catch (Exception $e) {
            Response::json(['error' => $e->getMessage()], 500);
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

    public function editQuota(int $id, string $oldQuotaNumber, string $newQuotaNumber, string $shipName, string $shippingCompany, string $warehouse, string $cargoType, float $totalTonnage): bool {
        return $this->quotaService->editQuota($id, $oldQuotaNumber, $newQuotaNumber, $shipName, $shippingCompany, $warehouse, $cargoType, $totalTonnage, $this->authenticatedUsername);
    }

    public function updateQuotaPercentage(int $id, float $percentage): bool {
        return $this->quotaService->updateQuotaPercentage($id, $percentage, $this->authenticatedUsername);
    }

    public function toggleQuotaStatus(int $id): bool {
        return $this->quotaService->toggleQuotaStatus($id, $this->authenticatedUsername);
    }

    public function updateQuotaPercentageRestriction(int $id, int $isEnabled): bool {
        return $this->quotaService->updateQuotaPercentageRestriction($id, $isEnabled, $this->authenticatedUsername);
    }

    public function deleteQuota(string $quotaNumber, string $shipName, string $warehouse, string $shippingCompany, string $cargoType): bool {
        return $this->quotaService->deleteQuota($quotaNumber, $shipName, $warehouse, $shippingCompany, $cargoType, $this->authenticatedUsername);
    }

    public function getGroupedQuotas(?string $shipName = null): array {
        return $this->quotaService->getGroupedQuotas($shipName);
    }

    public function updateTemporaryTonnage(string $quotaNumber, int $enabledVal, ?float $tonnageVal): array {
        return $this->quotaService->updateTemporaryTonnage($quotaNumber, $enabledVal, $tonnageVal, $this->authenticatedUsername);
    }
}
