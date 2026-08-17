<?php
// PHP/src/Core/AuthenticatesRequests.php

declare(strict_types=1);

namespace App\Core;

use App\Services\PermissionService;
use App\Services\SessionService;

/**
 * گیت مشترک احراز هویت/مجوز برای کنترلرهایی که داده‌ی تجاری را می‌خوانند یا
 * می‌نویسند. الگو از AppApiController::requireAuthenticatedSession گرفته شده
 * (هویت از هدرهای X-Username/X-Device-Id/X-Session-Token خوانده می‌شود، نه
 * از GET/بدنه، تا در لاگ دسترسی/پروکسی ذخیره نشود) و اینجا به‌صورت trait
 * قابل استفاده‌ی مشترک درآمده تا در چند کنترلر (مثل CargoController) تکرار
 * نشود.
 *
 * کلاس مصرف‌کننده باید یک پراپرتی «Request $request» در دسترس داشته باشد.
 */
trait AuthenticatesRequests {
    private ?string $authenticatedUsername = null;
    private ?string $authenticatedUserType = null;

    /**
     * پیش‌تر این متد ۳ کوئری در هر درخواست احرازشده می‌زد: SELECT برای
     * isValidToken، UPDATE برای last_activity (در هر تک درخواست، بدون
     * throttle)، و یک SELECT * FROM Users جداگانه فقط برای userType (P-01).
     * حالا با SessionService::validateAndGetUserType یک SELECT (userType از
     * روی همان جدول user_sessions) + یک UPDATE throttled‌شده (فقط اگر بیش از
     * ۶۰ ثانیه از آخرین به‌روزرسانی گذشته باشد) انجام می‌شود.
     */
    private function requireAuthenticatedSession(): void {
        $this->enforceMinAppVersion();

        $username = (string)($this->request->getHeader('X-Username') ?? '');
        $deviceId = (string)($this->request->getHeader('X-Device-Id') ?? '');
        $token = (string)($this->request->getHeader('X-Session-Token') ?? '');

        $sessionService = new SessionService();
        $userType = $sessionService->validateAndGetUserType($username, $deviceId, $token);
        if ($userType === null) {
            // I-05: تمایز بین «فقط access token منقضی شده» (کلاینت باید بی‌صدا
            // POST /auth/refresh بزند) و «کل نشست نامعتبر است» (کلاینت باید
            // کاربر را به صفحه‌ی login بفرستد) — بدون این تمایز، کلاینت هیچ
            // راهی برای فهمیدن اینکه آیا ارزش تلاش برای refresh را دارد یا نه، ندارد.
            $code = $sessionService->isAccessTokenExpiredButSessionActive($username, $deviceId, $token)
                ? 'access_token_expired'
                : 'session_invalid';
            $this->sendAuthErrorResponse('نشست معتبر نیست. لطفاً دوباره وارد شوید.', 401, $code);
        }

        $this->authenticatedUsername = $username;
        $this->authenticatedUserType = $userType;
    }

    /**
     * شکل پاسخ خطا بین دو گروه از کنترلرها متفاوت است — CargoController/
     * UtilityController با کلاینتی صحبت می‌کنند که «error» را boolean
     * می‌خواند ({error:true,message:"..."} → SaveOrUpdateResponse)، در حالی
     * که AppApiController/AnalyticsController کلاینتی دارند که «error» را
     * مستقیماً رشته‌ی پیام می‌خواند ({error:"..."} → ErrorResponse). یکی‌کردن
     * این دو شکل بدون تغییر هم‌زمان کلاینت، یکی از این دو مصرف‌کننده را خراب
     * می‌کرد؛ به همین دلیل قابل override است — پیش‌فرض همان شکلی‌ست که
     * CargoController/UtilityController از قبل داشتند.
     */
    protected function sendAuthErrorResponse(string $message, int $httpCode, ?string $code = null): void {
        header('Content-Type: application/json; charset=UTF-8');
        http_response_code($httpCode);
        $body = ['error' => true, 'message' => $message];
        if ($code !== null) {
            $body['code'] = $code;
        }
        echo json_encode($body, JSON_UNESCAPED_UNICODE);
        exit;
    }

    /**
     * قفل نسخه‌ی منقضی (min_allowed_version) قبلاً فقط سمت کلاینت اعمال می‌شد؛
     * یک کلاینت قدیمی یا دستکاری‌شده که دیالوگ VersionExpired را دور بزند
     * همچنان به همه‌ی APIهای تجاری دسترسی کامل داشت (S-4). این گیت همان
     * تصمیم را روی سرور هم اجرا می‌کند.
     *
     * وقتی هدر X-App-Version ارسال نشود (نسخه‌های نصب‌شده‌ی کلاینت که قبل از
     * این تغییر ساخته شده‌اند) عبور مجاز است — در غیر این صورت کل نصب‌های
     * فعلی با همین یک deploy فوراً قفل می‌شدند. این گیت فقط برای کلاینت‌هایی
     * که واقعاً نسخه‌شان را گزارش می‌کنند مؤثر است.
     */
    private function enforceMinAppVersion(): void {
        $appVersion = $this->request->getHeader('X-App-Version');
        if (!$appVersion) {
            return;
        }

        $configFile = APP_ROOT . '/update_config.php';
        if (!file_exists($configFile)) {
            return;
        }

        $config = include $configFile;
        $minAllowed = $config['min_allowed_version'] ?? $config['min_required_version'] ?? null;
        if (!$minAllowed) {
            return;
        }

        if (version_compare((string)$appVersion, (string)$minAllowed, '<')) {
            $this->sendAuthErrorResponse('نسخه‌ی برنامه‌ی شما منسوخ شده است. لطفاً به‌روزرسانی کنید.', 426);
        }
    }

    /**
     * گیت سطح دسترسی برای actionهای حساس (حذف/ویرایش کامل و مشابه). باید
     * بعد از requireAuthenticatedSession صدا زده شود.
     */
    private function requirePermission(string $feature): void {
        $username = $this->authenticatedUsername ?? '';
        $userType = $this->authenticatedUserType ?? '';

        $permissionService = new PermissionService();
        if (!$permissionService->hasPermission($username, $userType, $feature)) {
            $this->sendAuthErrorResponse('شما مجوز انجام این عملیات را ندارید.', 403);
        }
    }
}
