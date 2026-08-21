<?php
// PHP/src/routes/api_v2.php
//
// جدول route صریح API نسخه‌ی ۲ (opt-in)؛ فقط منطق استخراج/اعتبارسنجی پارامتر دارد و متدهای موجود کنترلرهای v1 را صدا می‌زند.
// دو الگو: Direct passthrough (متدهای مستقل با بررسی دوگانه‌ی auth/permission) و Shim (کنترلرهای چندعملی که مجوز را خودشان داخلی چک می‌کنند).

declare(strict_types=1);

use App\Controllers\AnalyticsController;
use App\Controllers\AppApiController;
use App\Controllers\AuthController;
use App\Controllers\CargoController;
use App\Controllers\ChatController;
use App\Controllers\DiagnosticsController;
use App\Controllers\LicenseController;
use App\Controllers\UserController;
use App\Controllers\UtilityController;
use App\Core\Request;
use App\Core\Response;
use App\Exceptions\ApiException;

// یک نمونه‌ی مشترک AppApiController برای تمام routeهای این فایل، به‌جای ساخت جداگانه به‌ازای هر route
$appApi = static function (): AppApiController {
    static $instance = null;
    if ($instance === null) {
        $instance = new AppApiController();
    }
    return $instance;
};

// تبدیل استثناهای رایج کنترلرها به پاسخ JSON یکدست، مطابق catch-block v1
$safeCall = static function (callable $fn) {
    try {
        return $fn();
    } catch (ApiException $e) {
        Response::json(['error' => $e->getMessage()] + ($e->getDetails() ?? []), $e->getStatusCode());
    } catch (\Throwable $e) {
        // فقط ApiException به کلاینت می‌رود؛ بقیه فقط لاگ می‌شوند تا ساختار DB افشا نشود
        error_log('api_v2 safeCall: ' . $e->getMessage());
        Response::json(['error' => 'خطای داخلی سرور رخ داده است.'], 500);
    }
};

return [
    // ===== SHIPS =====
    [
        'method' => 'GET',
        'path' => 'ships',
        'auth' => true,
        'permission' => 'view_reports',
        'handler' => function () use ($appApi, $safeCall): void {
            $safeCall(function () use ($appApi) {
                $controller = $appApi();
                $ships = $controller->getShipsList();
                $etagSource = [
                    'activeShips' => $ships['data']['activeShips'] ?? [],
                    'inactiveShips' => $ships['data']['inactiveShips'] ?? [],
                ];
                $controller->sendCacheableJsonResponse($ships, $etagSource, 8);
            });
        },
    ],
    [
        'method' => 'GET',
        'path' => 'ships/{shipName}',
        'auth' => true,
        'permission' => 'view_reports',
        'handler' => function (array $params) use ($appApi, $safeCall): void {
            $safeCall(function () use ($appApi, $params) {
                $controller = $appApi();
                $details = $controller->getShipDetails($params['shipName']);
                $controller->sendCacheableJsonResponse($details, $details, 6);
            });
        },
    ],
    [
        'method' => 'GET',
        'path' => 'ships/{shipName}/warehouses/{warehouseName}',
        'auth' => true,
        'permission' => 'view_reports',
        'handler' => function (array $params) use ($appApi, $safeCall): void {
            $safeCall(function () use ($appApi, $params) {
                $details = $appApi()->getWarehouseDetails($params['shipName'], $params['warehouseName']);
                Response::json($details);
            });
        },
    ],
    [
        // permission عمداً null است چون این route هنگام ثبت حواله هم توسط کلاینت صدا زده می‌شود، نه فقط فیچر گزارش‌ها
        'method' => 'GET',
        'path' => 'ships/{shipName}/quotas',
        'auth' => true,
        'permission' => null,
        'handler' => function (array $params) use ($appApi, $safeCall): void {
            $safeCall(function () use ($appApi, $params) {
                $controller = $appApi();
                $quotas = $controller->getQuotasList($params['shipName']);
                $controller->sendCacheableJsonResponse($quotas, $quotas, 6);
            });
        },
    ],

    // ===== QUOTAS (خواندنی) =====
    [
        'method' => 'GET',
        'path' => 'quotas/filtered',
        'auth' => true,
        'permission' => 'view_reports',
        'handler' => function (array $params, Request $request) use ($appApi, $safeCall): void {
            $safeCall(function () use ($appApi, $request) {
                $shipName = (string)$request->get('shipName', '');
                $startDateTime = (string)$request->get('startDateTime', '');
                $endDateTime = (string)$request->get('endDateTime', '');
                if ($shipName === '' || $startDateTime === '' || $endDateTime === '') {
                    throw new \Exception('پارامترهای ورودی ناقص هستند - نام کشتی، تاریخ شروع و پایان الزامی است');
                }
                Response::json($appApi()->getFilteredQuotas($shipName, $startDateTime, $endDateTime));
            });
        },
    ],
    [
        'method' => 'GET',
        'path' => 'quotas/filtered-summary',
        'auth' => true,
        'permission' => 'view_reports',
        'handler' => function (array $params, Request $request) use ($appApi, $safeCall): void {
            $safeCall(function () use ($appApi, $request) {
                $shipName = (string)$request->get('shipName', '');
                $warehouseName = (string)$request->get('warehouseName', '');
                $selectedQuota = (string)$request->get('selectedQuota', '');
                $startDateTime = (string)$request->get('startDateTime', '');
                $endDateTime = (string)$request->get('endDateTime', '');
                if ($shipName === '' || $warehouseName === '' || $selectedQuota === '' || $startDateTime === '' || $endDateTime === '') {
                    throw new \Exception('پارامترهای ورودی ناقص هستند');
                }
                // getFilteredSummary یک رشته‌ی JSON آماده برمی‌گرداند، پس مستقیم echo می‌شود
                $summary = $appApi()->getFilteredSummary($shipName, $warehouseName, $selectedQuota, $startDateTime, $endDateTime);
                header('Content-Type: application/json; charset=UTF-8');
                echo $summary;
                exit;
            });
        },
    ],
    [
        'method' => 'GET',
        'path' => 'quotas/grouped',
        'auth' => true,
        'permission' => 'view_reports',
        'handler' => function (array $params, Request $request) use ($appApi, $safeCall): void {
            $safeCall(function () use ($appApi, $request) {
                $shipNameFilter = $request->get('shipName');
                $shipNameFilter = ($shipNameFilter !== null && trim((string)$shipNameFilter) !== '')
                    ? \App\Validators\InputValidator::sanitize((string)$shipNameFilter)
                    : null;
                Response::json($appApi()->getGroupedQuotas($shipNameFilter));
            });
        },
    ],
    [
        'method' => 'GET',
        'path' => 'quotas/existence',
        'auth' => true,
        'permission' => null,
        'handler' => function (array $params, Request $request) use ($appApi, $safeCall): void {
            $safeCall(function () use ($appApi, $request) {
                $quotaNumber = (string)$request->get('quotaNumber', '');
                $shipName = (string)$request->get('shipName', '');
                if ($quotaNumber === '' || $shipName === '') {
                    throw new \Exception('شماره کوتاژ یا نام کشتی مشخص نشده است');
                }
                Response::json($appApi()->checkQuotaExistenceCargo($quotaNumber, $shipName));
            });
        },
    ],
    [
        'method' => 'GET',
        'path' => 'quotas/{quotaNumber}',
        'auth' => true,
        'permission' => 'view_reports',
        'handler' => function (array $params) use ($appApi, $safeCall): void {
            $safeCall(function () use ($appApi, $params) {
                $details = $appApi()->getQuotaDetails($params['quotaNumber']);
                if ($details === null) {
                    Response::json(['error' => 'کوتاژ مورد نظر یافت نشد'], 404);
                }
                Response::json($details);
            });
        },
    ],
    [
        'method' => 'GET',
        'path' => 'quotas/{quotaNumber}/status',
        'auth' => true,
        'permission' => null,
        'handler' => function (array $params, Request $request) use ($appApi, $safeCall): void {
            $safeCall(function () use ($appApi, $params, $request) {
                $status = $appApi()->checkQuotaStatus([
                    'quotaNumber' => $params['quotaNumber'],
                    'shipName' => (string)$request->get('shipName', ''),
                    'cargoType' => (string)$request->get('cargoType', ''),
                    'shippingCompany' => (string)$request->get('shippingCompany', ''),
                    'warehouse' => (string)$request->get('warehouse', ''),
                ]);
                Response::json($status);
            });
        },
    ],
    [
        'method' => 'GET',
        'path' => 'quotas/{quotaNumber}/loadable-tonnage',
        'auth' => true,
        'permission' => null,
        'handler' => function (array $params, Request $request) use ($appApi, $safeCall): void {
            $safeCall(function () use ($appApi, $params, $request) {
                Response::json($appApi()->getLoadableTonnage(
                    $params['quotaNumber'],
                    (string)$request->get('shippingCompany', ''),
                    (string)$request->get('warehouse', ''),
                    (string)$request->get('cargoType', '')
                ));
            });
        },
    ],

    // ===== QUOTAS (نوشتنی — مطابق قرارداد WRITE_ACTIONS در v1، همه POST) =====
    [
        'method' => 'POST',
        'path' => 'quotas/edit',
        'auth' => true,
        'permission' => 'manage_quotas',
        'handler' => function (array $params, Request $request) use ($appApi, $safeCall): void {
            $safeCall(function () use ($appApi, $request) {
                $body = $request->all();
                $required = ['id', 'oldQuotaNumber', 'newQuotaNumber', 'shipName', 'shippingCompany', 'warehouse', 'cargoType', 'totalTonnage'];
                foreach ($required as $field) {
                    if (!isset($body[$field])) {
                        throw new \Exception('پارامترهای ورودی ناقص هستند');
                    }
                }
                $result = $appApi()->editQuota(
                    (int)$body['id'],
                    (string)$body['oldQuotaNumber'],
                    (string)$body['newQuotaNumber'],
                    (string)$body['shipName'],
                    (string)$body['shippingCompany'],
                    (string)$body['warehouse'],
                    (string)$body['cargoType'],
                    (float)$body['totalTonnage']
                );
                Response::json(['success' => $result]);
            });
        },
    ],
    [
        'method' => 'POST',
        'path' => 'quotas/{id}/percentage',
        'auth' => true,
        'permission' => 'manage_quotas',
        'handler' => function (array $params, Request $request) use ($appApi, $safeCall): void {
            $safeCall(function () use ($appApi, $params, $request) {
                $percentage = $request->get('percentage');
                if ($percentage === null) {
                    throw new \Exception('درصد مشخص نشده است');
                }
                $result = $appApi()->updateQuotaPercentage((int)$params['id'], (float)$percentage);
                Response::json(['success' => $result]);
            });
        },
    ],
    [
        'method' => 'POST',
        'path' => 'quotas/{id}/toggle-status',
        'auth' => true,
        'permission' => 'manage_quotas',
        'handler' => function (array $params) use ($appApi, $safeCall): void {
            $safeCall(function () use ($appApi, $params) {
                $result = $appApi()->toggleQuotaStatus((int)$params['id']);
                Response::json(['success' => $result]);
            });
        },
    ],
    [
        'method' => 'POST',
        'path' => 'quotas/{id}/percentage-restriction',
        'auth' => true,
        'permission' => 'manage_quotas',
        'handler' => function (array $params, Request $request) use ($appApi, $safeCall): void {
            $safeCall(function () use ($appApi, $params, $request) {
                $isEnabled = $request->get('isEnabled');
                if ($isEnabled === null) {
                    throw new \Exception('مقدار محدودیت مشخص نشده است');
                }
                $result = $appApi()->updateQuotaPercentageRestriction((int)$params['id'], (int)$isEnabled);
                Response::json(['success' => $result]);
            });
        },
    ],
    [
        'method' => 'POST',
        'path' => 'quotas/delete',
        'auth' => true,
        'permission' => 'manage_quotas',
        'handler' => function (array $params, Request $request) use ($appApi, $safeCall): void {
            $safeCall(function () use ($appApi, $request) {
                $body = $request->all();
                $required = ['quotaNumber', 'shipName', 'warehouse', 'shippingCompany', 'cargoType'];
                foreach ($required as $field) {
                    if (empty($body[$field])) {
                        throw new \Exception('پارامترهای ورودی ناقص هستند');
                    }
                }
                $result = $appApi()->deleteQuota(
                    (string)$body['quotaNumber'],
                    (string)$body['shipName'],
                    (string)$body['warehouse'],
                    (string)$body['shippingCompany'],
                    (string)$body['cargoType']
                );
                Response::json(['success' => $result]);
            });
        },
    ],
    [
        'method' => 'POST',
        'path' => 'quotas/{quotaNumber}/temporary-tonnage',
        'auth' => true,
        'permission' => 'manage_quotas',
        'handler' => function (array $params, Request $request) use ($appApi, $safeCall): void {
            $safeCall(function () use ($appApi, $params, $request) {
                $enabled = $request->get('enabled');
                if ($enabled === null) {
                    throw new \Exception('پارامترهای ورودی ناقص هستند');
                }
                $enabledVal = (int)$enabled;
                $tonnage = $request->get('tonnage');
                if ($enabledVal === 1 && $tonnage === null) {
                    throw new \Exception('مقدار تناژ موقت الزامی است');
                }
                $tonnageVal = $tonnage !== null ? (float)$tonnage : null;
                $result = $appApi()->updateTemporaryTonnage($params['quotaNumber'], $enabledVal, $tonnageVal);
                Response::json($result);
            });
        },
    ],

    // ===== REAL-TIME (نسخه‌ی سبک، همان چیزی که app_api.php?action=getRealTimeData برمی‌گرداند) =====
    [
        'method' => 'GET',
        'path' => 'realtime/ships',
        'auth' => true,
        'permission' => null,
        'handler' => function () use ($appApi, $safeCall): void {
            $safeCall(function () use ($appApi) {
                $shipsList = $appApi()->getShipsList();
                Response::json([
                    'ships' => $shipsList['data']['activeShips'] ?? [],
                    'timestamp' => date('Y-m-d H:i:s'),
                    'status' => 'success',
                ]);
            });
        },
    ],

    // ===== AUTH — متدهای مستقل AuthController؛ auth=false چون این‌ها خودِ ورودی به سیستم‌اند =====
    [
        'method' => 'POST', 'path' => 'auth/login', 'auth' => false, 'permission' => null,
        'handler' => function () { (new AuthController())->login(); },
    ],
    [
        // عمداً فقط روی v2؛ auth=>false چون دقیقاً زمانی صدا زده می‌شود که access token منقضی شده، اعتبارسنجی واقعی داخل AuthController::refresh است
        'method' => 'POST', 'path' => 'auth/refresh', 'auth' => false, 'permission' => null,
        'handler' => function () { (new AuthController())->refresh(); },
    ],
    [
        'method' => 'POST', 'path' => 'auth/session', 'auth' => false, 'permission' => null,
        'handler' => function () { (new AuthController())->checkSession(); },
    ],
    [
        'method' => 'POST', 'path' => 'auth/logout', 'auth' => false, 'permission' => null,
        'handler' => function () { (new AuthController())->logout(); },
    ],

    // ===== CARGO — الگوی Direct passthrough؛ auth/permission هر route دقیقاً مطابق چک داخلی خودِ متد است =====
    [
        'method' => 'POST', 'path' => 'cargo', 'auth' => true, 'permission' => null,
        'handler' => function () { (new CargoController())->saveOrUpdate(); },
    ],
    [
        // PATCH — قبلاً POST بود، با هماهنگی کلاینت اندروید به فعل معنایی درست تغییر کرد
        'method' => 'PATCH', 'path' => 'cargo/update', 'auth' => true, 'permission' => 'edit_cargo',
        'handler' => function () { (new CargoController())->updateCargoInfo(); },
    ],
    [
        'method' => 'POST', 'path' => 'cargo/confirm', 'auth' => true, 'permission' => 'cargo_counter',
        'handler' => function () { (new CargoController())->confirmCargo(); },
    ],
    [
        // DELETE — همان دلیل بالا
        'method' => 'DELETE', 'path' => 'cargo/delete', 'auth' => true, 'permission' => 'delete_cargo',
        'handler' => function () { (new CargoController())->deleteCargoInfo(); },
    ],
    [
        'method' => 'GET', 'path' => 'cargo/search/scale-receipt', 'auth' => true, 'permission' => null,
        'handler' => function () { (new CargoController())->searchByScaleReceipt(); },
    ],
    [
        'method' => 'GET', 'path' => 'cargo/search/tracking', 'auth' => true, 'permission' => null,
        'handler' => function () { (new CargoController())->searchByTracking(); },
    ],
    [
        'method' => 'GET', 'path' => 'cargo/initial-info', 'auth' => true, 'permission' => null,
        'handler' => function () { (new CargoController())->getInitialInfo(); },
    ],
    [
        'method' => 'POST', 'path' => 'cargo/initial-info', 'auth' => true, 'permission' => 'initial_info',
        'handler' => function () { (new CargoController())->saveInitialInfo(); },
    ],
    [
        'method' => 'GET', 'path' => 'cargo/scale-receipt/check', 'auth' => true, 'permission' => null,
        'handler' => function () { (new CargoController())->checkScaleReceipt(); },
    ],
    [
        // «ships/active» عمداً از «ships» بالاتر جداست، مسیر سبک‌تر برای جریان ثبت حواله نه گزارش‌گیری
        'method' => 'GET', 'path' => 'ships/active', 'auth' => true, 'permission' => null,
        'handler' => function () { (new CargoController())->getActiveShips(); },
    ],

    // ===== UTILITY =====
    [
        'method' => 'POST', 'path' => 'utility/check-password', 'auth' => true, 'permission' => null,
        'handler' => function () { (new UtilityController())->checkPassword(); },
    ],
    [
        'method' => 'POST', 'path' => 'utility/check-existence', 'auth' => true, 'permission' => null,
        'handler' => function () { (new UtilityController())->checkExistence(); },
    ],
    [
        // auth=>true با ApiAuthGate (که توکن را هم بررسی می‌کند) شکاف اعتبارسنجی v1 را برای مسیر v2 می‌بندد
        'method' => 'POST', 'path' => 'utility/sync-permissions', 'auth' => true, 'permission' => null,
        'handler' => function () { (new UtilityController())->syncPermissions(); },
    ],

    // ===== USERS — الگوی Shim؛ ADMIN_ONLY_ACTIONS باید با UserController هماهنگ بماند. updateUser عمداً permission=>null دارد چون تمایز خودِکاربر/ادمین داخلی است =====
    [
        'method' => 'GET', 'path' => 'users', 'auth' => true, 'permission' => null,
        'handler' => function () { $_GET['action'] = 'getAllUsers'; (new UserController())->handle(); },
    ],
    [
        // بعد از قفل شدن getAllUsers پشت manage_users، این دو action محدودتر برای هر کاربر احرازشده باز ماندند
        'method' => 'GET', 'path' => 'users/self', 'auth' => true, 'permission' => null,
        'handler' => function () { $_GET['action'] = 'getSelfProfile'; (new UserController())->handle(); },
    ],
    [
        'method' => 'GET', 'path' => 'users/admins', 'auth' => true, 'permission' => null,
        'handler' => function () { $_GET['action'] = 'getAdminUsers'; (new UserController())->handle(); },
    ],
    [
        'method' => 'GET', 'path' => 'users/status', 'auth' => true, 'permission' => 'manage_users',
        'handler' => function () { $_GET['action'] = 'getAllUsersWithStatus'; (new UserController())->handle(); },
    ],
    [
        'method' => 'GET', 'path' => 'users/active-device', 'auth' => true, 'permission' => 'manage_users',
        'handler' => function () { $_GET['action'] = 'getActiveDeviceId'; (new UserController())->handle(); },
    ],
    [
        'method' => 'POST', 'path' => 'users', 'auth' => true, 'permission' => 'manage_users',
        'handler' => function () { $_GET['action'] = 'createUser'; (new UserController())->handle(); },
    ],
    [
        // PATCH — با Request::isWrite() هر فعل نوشتنی معنای معادل خودش را می‌گیرد
        'method' => 'PATCH', 'path' => 'users/{id}/update', 'auth' => true, 'permission' => null,
        'handler' => function (array $params) {
            $_GET['action'] = 'updateUser';
            $_GET['id'] = $params['id'];
            (new UserController())->handle();
        },
    ],
    [
        // DELETE — همان دلیل بالا
        'method' => 'DELETE', 'path' => 'users/{id}/delete', 'auth' => true, 'permission' => 'manage_users',
        'handler' => function (array $params) {
            $_GET['action'] = 'deleteUser';
            $_GET['userId'] = $params['id']; // نام فیلد داخلی UserController متفاوت از {id} مسیر است
            (new UserController())->handle();
        },
    ],
    [
        'method' => 'POST', 'path' => 'users/force-logout', 'auth' => true, 'permission' => 'manage_users',
        'handler' => function () { $_GET['action'] = 'forceLogout'; (new UserController())->handle(); },
    ],

    // ===== CHAT — الگوی Shim؛ کنترل دسترسی واقعی داخل ChatController است، اینجا permission=>null و auth=>true فقط معتبربودن نشست را تضمین می‌کند =====
    [
        'method' => 'GET', 'path' => 'chat/messages', 'auth' => true, 'permission' => null,
        'handler' => function () { $_GET['action'] = 'getMessages'; (new ChatController())->handleChatRequest(); },
    ],
    [
        'method' => 'GET', 'path' => 'chat/unread-count', 'auth' => true, 'permission' => null,
        'handler' => function () { $_GET['action'] = 'getUnreadCount'; (new ChatController())->handleChatRequest(); },
    ],
    [
        'method' => 'POST', 'path' => 'chat/messages', 'auth' => true, 'permission' => null,
        'handler' => function () { $_GET['action'] = 'sendMessage'; (new ChatController())->handleChatRequest(); },
    ],
    [
        // PATCH — با Request::isWrite() هر فعل نوشتنی معنای معادل خودش را می‌گیرد
        'method' => 'PATCH', 'path' => 'chat/messages/{id}/edit', 'auth' => true, 'permission' => null,
        'handler' => function (array $params) {
            $_GET['action'] = 'editMessage';
            $_GET['messageId'] = $params['id'];
            (new ChatController())->handleChatRequest();
        },
    ],
    [
        // DELETE — همان دلیل بالا
        'method' => 'DELETE', 'path' => 'chat/messages/{id}/delete', 'auth' => true, 'permission' => null,
        'handler' => function (array $params) {
            $_GET['action'] = 'deleteMessage';
            $_GET['messageId'] = $params['id'];
            (new ChatController())->handleChatRequest();
        },
    ],
    [
        'method' => 'POST', 'path' => 'chat/messages/{id}/read', 'auth' => true, 'permission' => null,
        'handler' => function (array $params) {
            $_GET['action'] = 'markAsRead';
            $_GET['messageId'] = $params['id'];
            (new ChatController())->handleChatRequest();
        },
    ],

    // ===== ANALYTICS / REAL-TIME — الگوی Shim؛ هر ۴ اکشن دقیقاً همین یک permission ('view_reports') را چک می‌کنند =====
    [
        'method' => 'GET', 'path' => 'analytics/kotazh', 'auth' => true, 'permission' => 'view_reports',
        'handler' => function () { $_GET['action'] = 'getKotazhInfo'; (new AnalyticsController())->handleRealTimeLoadingData(); },
    ],
    [
        'method' => 'GET', 'path' => 'analytics/realtime', 'auth' => true, 'permission' => 'view_reports',
        'handler' => function () { $_GET['action'] = 'getRealTimeData'; (new AnalyticsController())->handleRealTimeLoadingData(); },
    ],
    [
        'method' => 'GET', 'path' => 'analytics/comprehensive', 'auth' => true, 'permission' => 'view_reports',
        'handler' => function () { $_GET['action'] = 'getComprehensiveAnalysis'; (new AnalyticsController())->handleRealTimeLoadingData(); },
    ],
    [
        'method' => 'POST', 'path' => 'analytics/export-log', 'auth' => true, 'permission' => 'view_reports',
        'handler' => function () { $_GET['action'] = 'logAnalyticsExport'; (new AnalyticsController())->handleRealTimeLoadingData(); },
    ],

    // ===== DIAGNOSTICS — عمداً بدون auth: health باید برای مانیتورینگ خارجی و گزارش کرش حتی بدون نشست معتبر در دسترس باشد =====
    [
        'method' => 'GET', 'path' => 'health', 'auth' => false, 'permission' => null,
        'handler' => function () { (new DiagnosticsController())->health(); },
    ],
    [
        'method' => 'POST', 'path' => 'diagnostics/crash', 'auth' => false, 'permission' => null,
        'handler' => function () { (new DiagnosticsController())->reportCrash(); },
    ],

    // ===== این ۶ مسیر جایگزین shimهای مستقل حذف‌شده‌ی ریشه‌ی PHP/ شدند و اکنون تنها راه دسترسی‌اند؛ هشدار: URLهای قدیمی هنوز در کلاینت hardcode هستند و اکنون ۴۰۴ می‌دهند =====
    [
        'method' => 'POST', 'path' => 'utility/check-signature', 'auth' => false, 'permission' => null,
        'handler' => function () { (new UtilityController())->checkSignature(); },
    ],
    [
        'method' => 'GET', 'path' => 'utility/check-update', 'auth' => false, 'permission' => null,
        'handler' => function () { (new UtilityController())->checkUpdate(); },
    ],
    [
        'method' => 'POST', 'path' => 'license/validate', 'auth' => false, 'permission' => null,
        'handler' => function () { (new LicenseController())->validateLicense(); },
    ],
    [
        'method' => 'GET', 'path' => 'license/info', 'auth' => false, 'permission' => null,
        'handler' => function () { (new LicenseController())->getLicenseInfo(); },
    ],
    [
        // handleQuotaRemaining داخلاً requireAuthenticatedSession() و requirePermission('active_quotas') را صدا می‌زند
        'method' => 'GET', 'path' => 'analytics/quota-remaining', 'auth' => true, 'permission' => 'active_quotas',
        'handler' => function () { (new AnalyticsController())->handleQuotaRemaining(); },
    ],
    [
        // updateFcmToken داخلاً requireAuthenticatedSession() را صدا می‌زند و فقط برای کاربر همان نشست عمل می‌کند
        'method' => 'POST', 'path' => 'users/fcm-token', 'auth' => true, 'permission' => null,
        'handler' => function () { (new UserController())->updateFcmToken(); },
    ],
];
