<?php
// PHP/Quota Reports/api.php — تنها endpoint JSON پنل گزارش کوتاژ؛ فقط خواندنی، منطق از QuotaService/CargoRepository موجود پروژه می‌آید.

declare(strict_types=1);

require_once __DIR__ . '/_guard.php';

use App\Core\Logger;
use App\Core\Request;
use App\Core\Response;
use App\Exceptions\ApiException;
use App\Repositories\CargoRepository;
use App\Services\QuotaService;

qr_require_auth_json();

$request = new Request();

if (!$request->isGet()) {
    Response::error('این پنل فقط عملیات خواندنی دارد؛ روش درخواست معتبر نیست.', 405);
}

// اعتبارسنجی شماره‌ی کوتاژ — همان قاعده‌ی ۸ رقمی نسخه‌ی قبلی، حالا سمت سرور هم اعمال می‌شود.
function qr_validate_kotazh(string $kotazh): string {
    $kotazh = trim($kotazh);
    if ($kotazh === '') {
        throw new ApiException('کوتاژ نمی‌تواند خالی باشد.', 422);
    }
    if (!preg_match('/^\d{8}$/', $kotazh)) {
        throw new ApiException('فرمت کوتاژ نامعتبر است. باید شامل ۸ رقم باشد.', 422);
    }
    return $kotazh;
}

$action = (string)$request->get('action', '');

try {
    $quotaService = new QuotaService();
    $cargoRepository = new CargoRepository();

    switch ($action) {
        // بارگذاری اولیه‌ی داشبورد: فهرست سبک همه‌ی کوتاژها (برای پر کردن دراپ‌داون کشتی/کالا)، بدون محاسبات تناژ/درصد/حواله
        case 'groups':
            Response::success('', ['quotas' => $quotaService->getQuotaGroupsSummary()]);
            break;

        // محاسبات فقط برای ترکیب یک کشتی + یک نوع کالا — زمانی صدا زده می‌شود که کاربر آن آیتم را از دراپ‌داون انتخاب کند
        case 'shipStats':
            $shipName = trim((string)$request->get('shipName', ''));
            if ($shipName === '') {
                throw new ApiException('نام کشتی نمی‌تواند خالی باشد.', 422);
            }
            $cargoType = trim((string)$request->get('cargoType', ''));
            Response::success('', ['quotas' => $quotaService->getQuotaStatsForShip($shipName, $cargoType !== '' ? $cargoType : null)]);
            break;

        // نمای جزئیات یک کوتاژ: کارت‌های خلاصه (از دیتابیس، نه محاسبه‌ی کلاینت) + جدول حواله‌ها
        case 'quota':
            $kotazh = qr_validate_kotazh((string)$request->get('kotazh', ''));
            $kotazhInfo = $quotaService->getQuotaDetails($kotazh);
            if ($kotazhInfo === null) {
                Response::error('کوتاژ مورد نظر یافت نشد.', 404);
            }
            $cargoInfo = $cargoRepository->findByQuotaNumber($kotazh);
            Response::success('', ['kotazhInfo' => $kotazhInfo, 'cargoInfo' => $cargoInfo]);
            break;

        // جستجوی پیشرفته با شماره‌ی قبض باسکول — کوتاژ متناظر را برمی‌گرداند تا کلاینت به نمای جزئیات هدایت کند
        case 'searchReceipt':
            $receipt = trim((string)$request->get('receipt', ''));
            if ($receipt === '') {
                throw new ApiException('شماره قبض باسکول نمی‌تواند خالی باشد.', 422);
            }
            $found = $cargoRepository->findByScaleReceiptNumber($receipt);
            if ($found === null) {
                Response::error('حواله‌ای با این شماره قبض باسکول یافت نشد.', 404);
            }
            Response::success('', ['result' => $found]);
            break;

        default:
            Response::error('عملیات نامعتبر است.', 404);
    }
} catch (ApiException $e) {
    Response::error($e->getMessage(), $e->getStatusCode(), $e->getDetails());
} catch (\Throwable $e) {
    // فقط ApiException (پیام‌های عمدی) به کلاینت می‌رسد؛ بقیه لاگ می‌شوند تا ساختار دیتابیس افشا نشود. کلاس + فایل:خط هم ثبت می‌شود چون فقط پیام خطا برای عیب‌یابی (مثلاً خطاهای PDO) کافی نیست.
    Logger::getInstance()->error(sprintf(
        'Quota Reports api.php (action=%s): [%s] %s in %s:%d',
        $action,
        get_class($e),
        $e->getMessage(),
        $e->getFile(),
        $e->getLine()
    ));
    Response::error('خطای داخلی سرور رخ داده است.', 500);
}
