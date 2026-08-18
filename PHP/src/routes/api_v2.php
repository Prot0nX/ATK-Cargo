<?php
// PHP/src/routes/api_v2.php
//
// جدول route صریح API نسخه‌ی ۲ — تنها منبع حقیقت برای این‌که «چه مسیری با
// چه متد HTTP‌ای، با چه نیاز احراز هویت/مجوزی» وجود دارد. برخلاف
// protected_proxy.php (v1) که هر فایل .php در پوشه به‌طور پیش‌فرض یک
// endpoint است مگر صراحتاً مستثنا شود (opt-out)، اینجا برعکس است: فقط
// دقیقاً همین ردیف‌ها قابل دسترسی‌اند (opt-in).
//
// این فایل منطق تجاری ندارد — فقط پارامترهای route/query/body را استخراج و
// اعتبارسنجی سطحی می‌کند، سپس متدهای عمومی و از قبل تایپ‌شده‌ی کنترلرهای v1
// را صدا می‌زند (مثلاً AppApiController::updateQuotaPercentage) تا منطق
// تجاری/کوئری‌های دیتابیس دوباره‌نویسی نشوند.
//
// همه‌ی گروه‌های مصرف‌شده توسط کلاینت اندروید مهاجرت داده شده‌اند: Ships/Quotas
// (app_api.php)، Auth، Cargo، Utility، Users، Chat، Analytics/Real-Time.
// دو الگوی متفاوت استفاده شده:
//   ۱. Direct passthrough — برای متدهایی که از قبل کاملاً مستقل هستند
//      (پارامتر خودشان را از Request می‌خوانند و خودشان Response::json صدا
//      می‌زنند)، مثل CargoController::saveOrUpdate یا AuthController::login.
//      اینجا router سطح auth/permission را دقیقاً هم‌راستا با چیزی که متد
//      داخلاً چک می‌کند اعلام می‌کند — یعنی یک بررسی دوگانه‌ی واقعی (نه فقط
//      تزئینی)، چون هرکدام مستقل از دیگری اجرا می‌شوند.
//   ۲. Shim برای کنترلرهای چندعملی — UserController::handle،
//      ChatController::handleChatRequest و
//      AnalyticsController::handleRealTimeLoadingData هرکدام یک ورودی واحد
//      با switch داخلی روی action هستند و منطق مجوز نامتقارن‌شان (مثل تمایز
//      خودِ‌کاربر/ادمین در updateUser) داخل خودشان زندگی می‌کند. برای این‌ها
//      router فقط پارامترهای مسیر را در $_GET قرار می‌دهد و متد اصلی را صدا
//      می‌زند؛ auth/permission در سطح router برای این دسته یا خالی (auth
//      داخلی خودش را دارد) یا فقط برای بخشی که واقعاً یکنواخت است تنظیم شده.

declare(strict_types=1);

use App\Controllers\AnalyticsController;
use App\Controllers\AppApiController;
use App\Controllers\AuthController;
use App\Controllers\CargoController;
use App\Controllers\ChatController;
use App\Controllers\DiagnosticsController;
use App\Controllers\UserController;
use App\Controllers\UtilityController;
use App\Core\Request;
use App\Core\Response;
use App\Exceptions\ApiException;

/**
 * یک نمونه‌ی مشترک AppApiController برای تمام routeهای این فایل — چون هر
 * route handler جدا فراخوانی می‌شود (نه یک متد dispatcher مثل v1)، ساخت
 * جداگانه به‌ازای هر route لازم نیست.
 */
$appApi = static function (): AppApiController {
    static $instance = null;
    if ($instance === null) {
        $instance = new AppApiController();
    }
    return $instance;
};

/**
 * تبدیل استثناهای رایج کنترلرها (Exception ساده یا ApiException با کد
 * وضعیت مشخص) به پاسخ JSON یکدست — مطابق همان catch-block که در
 * AppApiController::handle وجود دارد، تا رفتار خطا با v1 هم‌راستا بماند.
 */
$safeCall = static function (callable $fn) {
    try {
        return $fn();
    } catch (ApiException $e) {
        Response::json(['error' => $e->getMessage()] + ($e->getDetails() ?? []), $e->getStatusCode());
    } catch (\Throwable $e) {
        // فقط ApiException (پیام‌های عمدی) به کلاینت می‌رود؛ بقیه فقط لاگ
        // می‌شوند تا ساختار جدول/کوئری افشا نشود (DEEP_CODE_AUDIT.md
        // #Phase2.4).
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
        // permission عمداً null است — این route (=getQuotasList در
        // AppApiController) هنگام ثبت حواله توسط QuotaValidationUseCase.kt
        // هم صدا زده می‌شود، نه فقط فیچر گزارش‌ها. رجوع کنید به کامنت
        // READ_ACTIONS_REQUIRING_REPORTS در AppApiController.php.
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
                // getFilteredSummary یک رشته‌ی JSON از پیش‌ساخته برمی‌گرداند
                // (نه آرایه)، پس مستقیم echo می‌شود نه Response::json.
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

    // ===== AUTH — متدهای AuthController مستقل و کامل‌اند (پارامترهای خود
    // را از Request می‌خوانند، خودشان Response::json صدا می‌زنند)؛ auth=false
    // چون این‌ها خودِ ورودی به سیستم هستند (نمی‌توان نشستی که هنوز ایجاد
    // نشده را الزامی کرد). =====
    [
        'method' => 'POST', 'path' => 'auth/login', 'auth' => false, 'permission' => null,
        'handler' => function () { (new AuthController())->login(); },
    ],
    [
        // I-05: عمداً فقط روی v2 (نه v1) — این endpoint کاملاً جدید است، هیچ
        // نصب فعلی اپ آن را صدا نمی‌زند، پس نیازی به اضافه‌شدن به whitelist
        // پروکسی v1 نیست. auth=>false چون دقیقاً زمانی صدا زده می‌شود که
        // access token منقضی شده — نمی‌توان همان توکن منقضی را برای عبور از
        // گیت auth الزامی کرد؛ اعتبارسنجی واقعی (تطبیق دقیق refresh token)
        // داخل AuthController::refresh/SessionService::refreshTokens انجام می‌شود.
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

    // ===== CARGO — الگوی Direct passthrough؛ auth/permission هر route دقیقاً
    // مطابق چیزی است که خودِ متد هم داخلاً چک می‌کند (بررسی دوگانه‌ی واقعی). =====
    [
        'method' => 'POST', 'path' => 'cargo', 'auth' => true, 'permission' => null,
        'handler' => function () { (new CargoController())->saveOrUpdate(); },
    ],
    [
        // POST نه PATCH — CargoController::updateCargoInfo داخلاً
        // $_SERVER['REQUEST_METHOD'] !== 'POST' را رد می‌کند (کنترلرهای
        // legacy فقط GET/POST را می‌شناسند)؛ استفاده از PATCH اینجا باعث
        // می‌شد router مسیر را تطبیق دهد ولی خودِ متد قدیمی با ۴۰۵ رد کند.
        'method' => 'POST', 'path' => 'cargo/update', 'auth' => true, 'permission' => 'edit_cargo',
        'handler' => function () { (new CargoController())->updateCargoInfo(); },
    ],
    [
        'method' => 'POST', 'path' => 'cargo/confirm', 'auth' => true, 'permission' => 'cargo_counter',
        'handler' => function () { (new CargoController())->confirmCargo(); },
    ],
    [
        'method' => 'POST', 'path' => 'cargo/delete', 'auth' => true, 'permission' => 'delete_cargo',
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
        // نام مسیر «ships/active» عمداً از app_api.php's «ships» (بالاتر) جدا
        // نگه داشته شده چون این یکی CargoController::getActiveShips است — یک
        // مسیر داده‌ی سبک‌تر و متفاوت از AppApiController::getShipsList که
        // در جریان ثبت حواله (نه گزارش‌گیری) مصرف می‌شود.
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
        // syncPermissions در v1 اعتبار نشست را فقط با username+deviceId چک
        // می‌کند (بدون توکن — S-20)؛ گیت auth=>true در سطح router اینجا با
        // ApiAuthGate (که توکن را هم بررسی می‌کند) این شکاف را برای مسیر v2
        // می‌بندد، مستقل از منطق داخلی قدیمی‌تر متد.
        'method' => 'POST', 'path' => 'utility/sync-permissions', 'auth' => true, 'permission' => null,
        'handler' => function () { (new UtilityController())->syncPermissions(); },
    ],

    // ===== USERS — الگوی Shim؛ ADMIN_ONLY_ACTIONS اینجا دقیقاً مطابق همان
    // ثابت در UserController است (اگر یکی تغییر کرد، دیگری هم باید عمداً
    // بازبینی شود). updateUser عمداً permission=>null دارد چون تمایز
    // خودِکاربر/ادمین داخل خودِ UserController::handle انجام می‌شود. =====
    [
        'method' => 'GET', 'path' => 'users', 'auth' => true, 'permission' => null,
        'handler' => function () { $_GET['action'] = 'getAllUsers'; (new UserController())->handle(); },
    ],
    [
        // Phase1.4 (DEEP_CODE_AUDIT.md): بعد از این‌که getAllUsers پشت
        // manage_users قفل شد، دو action محدودتر اضافه شدند که هر کاربر
        // احرازشده (نه فقط ادمین) می‌تواند صدا بزند — permission=>null دقیقاً
        // مطابق نبودن این دو در ADMIN_ONLY_ACTIONS داخل UserController.
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
        // POST نه PATCH — UserController::handle فقط isGet()/isPost() را
        // می‌شناسد (نه PATCH/DELETE)؛ همان محدودیت بالا در مورد cargo/update.
        'method' => 'POST', 'path' => 'users/{id}/update', 'auth' => true, 'permission' => null,
        'handler' => function (array $params) {
            $_GET['action'] = 'updateUser';
            $_GET['id'] = $params['id'];
            (new UserController())->handle();
        },
    ],
    [
        'method' => 'POST', 'path' => 'users/{id}/delete', 'auth' => true, 'permission' => 'manage_users',
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

    // ===== CHAT — الگوی Shim؛ کنترل دسترسی واقعی (isAdmin بر اساس
    // userType) داخل خودِ ChatController انجام می‌شود، نه با permission
    // استاندارد permissions.json؛ پس اینجا permission=>null است و auth=>true
    // فقط معتبربودن نشست را تضمین می‌کند (همان‌قدر که router می‌تواند). =====
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
        // POST نه PATCH — ChatController::handleChatRequest فقط GET یا
        // دقیقاً REQUEST_METHOD==='POST' را می‌شناسد؛ با PATCH نه شاخه‌ی GET
        // اجرا می‌شد نه شاخه‌ی POST، یعنی پاسخ کاملاً خالی (بدون هیچ echo)
        // برمی‌گشت — حتی بی‌سروصداتر از یک ۴۰۵.
        'method' => 'POST', 'path' => 'chat/messages/{id}/edit', 'auth' => true, 'permission' => null,
        'handler' => function (array $params) {
            $_GET['action'] = 'editMessage';
            $_GET['messageId'] = $params['id'];
            (new ChatController())->handleChatRequest();
        },
    ],
    [
        'method' => 'POST', 'path' => 'chat/messages/{id}/delete', 'auth' => true, 'permission' => null,
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

    // ===== ANALYTICS / REAL-TIME (پشت realTimeLoadingData.php در v1) —
    // الگوی Shim؛ permission در سطح router اینجا واقعاً دقیق است چون
    // AnalyticsController::handleRealTimeLoadingData برای هر ۴ اکشن دقیقاً
    // همین یک permission ('view_reports') را چک می‌کند، نه ترکیب نامتقارن
    // مثل UserController. =====
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

    // ===== DIAGNOSTICS (Phase 2.13) — عمداً فقط روی v2، بدون auth: health
    // باید برای ابزار مانیتورینگ خارجی در دسترس باشد، و گزارش کرش باید حتی
    // بدون نشست معتبر (یا قبل از لاگین) هم برسد. =====
    [
        'method' => 'GET', 'path' => 'health', 'auth' => false, 'permission' => null,
        'handler' => function () { (new DiagnosticsController())->health(); },
    ],
    [
        'method' => 'POST', 'path' => 'diagnostics/crash', 'auth' => false, 'permission' => null,
        'handler' => function () { (new DiagnosticsController())->reportCrash(); },
    ],
];
