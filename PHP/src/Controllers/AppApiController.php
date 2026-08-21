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

// فقط مسئول احراز هویت/routing/پارس درخواست؛ منطق تجاری به ShipService/QuotaService منتقل شده (C-05)
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

    // فرمت سفارشی پاسخ خطا برای سازگاری با ساختار مورد انتظار کلاینت اندروید این کنترلر
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

    // actionهای نوشتنی که فقط با POST مجازند تا قابل کش/بازپخش نباشند و پارامتر حساس در لاگ URL نیفتد
    private const WRITE_ACTIONS = [
        'editQuota',
        'updateQuotaPercentage',
        'toggleQuotaStatus',
        'updateQuotaPercentageRestriction',
        'deleteQuota',
        'updateTemporaryTonnage',
    ];

    // actionهای خواندنی مخصوص فیچر گزارش‌ها که باید پشت مجوز view_reports قفل شوند؛ getQuotasList عمداً حذف شده تا ثبت حواله نشکند (Phase1.4)
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

    // این dispatcher قدیمی دیگر از هیچ route صدا زده نمی‌شود؛ حذف کامل آن خارج از دامنه‌ی این تغییر است
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
                    // ETag فقط از activeShips/inactiveShips محاسبه می‌شود، نه از statistics.timestamp همیشه‌متغیر
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
                    // فشرده‌سازی PHP-level حذف شد چون mod_deflate در htaccess همین کار را می‌کند (P-06)
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
            // فقط ApiException به کلاینت می‌رود؛ بقیه‌ی خطاها فقط لاگ می‌شوند تا ساختار دیتابیس افشا نشود (Phase2.4)
            error_log('AppApiController: ' . $e->getMessage());
            Response::error('خطای داخلی سرور رخ داده است.', 500);
        }
    }

    // فقط برای پاسخ‌های GET غیرقابل تغییر؛ public چون route handlerهای api/v2 هم به همین کش/ETag نیاز دارند
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

    // ===== زیر این خط: delegate خالص به ShipService/QuotaService؛ امضاها دست‌نخورده چون routes/api_v2.php مستقیماً صدا می‌زند (C-05) =====

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

    // فراخوانی requireAuthenticatedSession در متدهای نوشتنی زیر نه برای احراز هویت، بلکه برای پر کردن authenticatedUsername جهت AuditLogger لازم است
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
