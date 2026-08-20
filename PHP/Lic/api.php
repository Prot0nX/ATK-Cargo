<?php
// PHP/Lic/api.php
//
// تنها endpoint JSON پنل مدیریت لایسنس.
//
// الگو: whitelist صریح action (opt-in)، دقیقاً مثل src/routes/api_v2.php —
// هر عملیاتی که در جدول زیر نباشد اصلاً وجود ندارد.
//
// تفاوت‌های امنیتی نسبت به manage_licenses.php که این فایل جایگزینش می‌شود:
//   ۱. احراز هویت سمت سرور روی *همه‌ی* actionها (قبلاً هیچ‌کدام گیت نداشتند؛
//      یک GET بی‌نام‌ونشان تمام کلیدهای لایسنس مشتریان را برمی‌گرداند).
//   ۲. بررسی واقعی توکن CSRF روی افعال نوشتنی (قبلاً توکن تولید می‌شد اما
//      هیچ‌جا بررسی نمی‌شد).
//   ۳. حذف `Access-Control-Allow-Origin: *` — این پنل هرگز نباید
//      cross-origin پاسخ بدهد.
//   ۴. تفکیک متد: خواندن با GET، نوشتن با POST (قبلاً action نوشتنی از
//      طریق GET هم قابل فراخوانی بود).

declare(strict_types=1);

require_once __DIR__ . '/_guard.php';

use App\Core\Csrf;
use App\Core\Logger;
use App\Core\Request;
use App\Core\Response;
use App\Exceptions\ApiException;
use App\Services\LicenseAdminService;

lic_require_auth_json();

$request = new Request();
$action = (string)$request->get('action', '');

/** actionهایی که وضعیت را تغییر می‌دهند: POST + توکن CSRF الزامی. */
const WRITE_ACTIONS = ['create', 'update', 'toggle', 'delete'];

if (in_array($action, WRITE_ACTIONS, true)) {
    if (!$request->isPost()) {
        Response::error('این عملیات فقط با متد POST قابل انجام است.', 405);
    }
    // توکن از هدر خوانده می‌شود (نه بدنه) تا بدنه‌ی JSON خالص بماند.
    Csrf::requireValid($request->getHeader('X-CSRF-Token'));
} elseif (!$request->isGet()) {
    Response::error('روش درخواست معتبر نیست.', 405);
}

/**
 * شناسه‌ی لایسنس از ورودی — همه‌ی actionهای هدفمند بر پایه‌ی id عددی کار
 * می‌کنند، نه خودِ license_key. دلیل: کلید یک راز است و نباید در تاریخچه‌ی
 * درخواست‌ها و لاگ‌ها بیش از حد لازم بچرخد.
 */
function lic_required_id(Request $request): int {
    $id = (int)$request->get('id', 0);
    if ($id <= 0) {
        throw new ApiException('شناسه‌ی لایسنس مشخص نشده است.', 422);
    }
    return $id;
}

try {
    $service = new LicenseAdminService();

    switch ($action) {
        case 'list':
            $search = $request->get('search');
            $status = $request->get('status');
            Response::success('', [
                'licenses' => $service->list(
                    is_string($search) ? trim($search) : null,
                    is_string($status) ? $status : null
                ),
                'stats' => $service->stats(),
            ]);
            break;

        case 'stats':
            Response::success('', ['stats' => $service->stats()]);
            break;

        case 'create':
            $license = $service->create($request->all(), LIC_ACTOR);
            Response::success('لایسنس با موفقیت ایجاد شد.', ['license' => $license], 201);
            break;

        case 'update':
            $license = $service->update(lic_required_id($request), $request->all(), LIC_ACTOR);
            Response::success('تغییرات ذخیره شد.', ['license' => $license]);
            break;

        case 'toggle':
            $license = $service->toggle(lic_required_id($request), LIC_ACTOR);
            Response::success(
                $license['is_active'] ? 'لایسنس فعال شد.' : 'لایسنس غیرفعال شد.',
                ['license' => $license]
            );
            break;

        case 'delete':
            $service->delete(lic_required_id($request), LIC_ACTOR);
            Response::success('لایسنس حذف شد.');
            break;

        default:
            Response::error('عملیات نامعتبر است.', 404);
    }
} catch (ApiException $e) {
    Response::error($e->getMessage(), $e->getStatusCode(), $e->getDetails());
} catch (\Throwable $e) {
    // فقط ApiException (پیام‌های عمدی) به کلاینت می‌رسد؛ بقیه صرفاً لاگ
    // می‌شوند تا ساختار جدول/کوئری افشا نشود — همان قرارداد $safeCall در
    // src/routes/api_v2.php.
    Logger::getInstance()->error('Lic api.php: ' . $e->getMessage());
    Response::error('خطای داخلی سرور رخ داده است.', 500);
}
