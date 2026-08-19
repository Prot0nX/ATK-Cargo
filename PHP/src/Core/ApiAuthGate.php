<?php
// PHP/src/Core/ApiAuthGate.php

declare(strict_types=1);

namespace App\Core;

use App\Services\PermissionService;
use App\Services\SessionService;

/**
 * گیت احراز هویت/مجوز router نسخه‌ی ۲ — نسخه‌ی static (نه trait) چون Router
 * از داخل کلاس کنترلر صدا نمی‌زند، از داخل route handler صدا می‌زند که یک
 * closure ساده است، نه یک شیء با پراپرتی $request.
 *
 * طراحی عمداً به‌گونه‌ای است که حتی اگر یک route handler فراموش کند خودش این
 * گیت را صدا بزند، Router::dispatch (routes/api_v2.php را ببینید) این متد را
 * برای هر route‌ای که auth=>true دارد، *قبل از* فراخوانی handler صدا می‌زند —
 * یعنی برخلاف مدل v1 (که گیت کاملاً به یاد کنترلر بستگی داشت و فراموش‌شدنش
 * دقیقاً همان چیزی بود که S-01 را ایجاد کرد)، اینجا گیت در سطح router است.
 */
final class ApiAuthGate {
    /**
     * @return array{0: string, 1: string} [username, userType]
     */
    public static function requireAuthenticated(Request $request): array {
        MinVersionGate::enforce($request);

        $username = (string)($request->getHeader('X-Username') ?? '');
        $deviceId = (string)($request->getHeader('X-Device-Id') ?? '');
        $token = (string)($request->getHeader('X-Session-Token') ?? '');

        $sessionService = new SessionService();
        $userType = $sessionService->validateAndGetUserType($username, $deviceId, $token);
        if ($userType === null) {
            // I-05: تمایز بین «فقط access token منقضی شده» (کلاینت باید بی‌صدا
            // POST /auth/refresh بزند) و «کل نشست نامعتبر است» (کلاینت باید
            // کاربر را به صفحه‌ی login بفرستد). این تمایز قبلاً فقط در گیت
            // v1 (AuthenticatesRequests) وجود داشت؛ با حذف کامل v1 در فاز ۳،
            // تنها تولیدکننده‌ی این نشانه از بین رفت و TokenAuthenticator
            // سمت کلاینت — که دقیقاً منتظر code=access_token_expired است —
            // دیگر هرگز رفرش نمی‌کرد. نتیجه: کاربر بعد از ۳۰ دقیقه (عمر
            // access token) از هر منویی بیرون انداخته می‌شد.
            $code = $sessionService->isAccessTokenExpiredButSessionActive($username, $deviceId, $token)
                ? 'access_token_expired'
                : 'session_invalid';

            // Response::error فیلد code را پشتیبانی نمی‌کند (فقط message/details)،
            // پس اینجا مستقیماً json فرستاده می‌شود — با همان کلیدهای
            // success/message که بقیه‌ی پاسخ‌های خطای v2 دارند.
            Response::json([
                'success' => false,
                'message' => 'نشست معتبر نیست. لطفاً دوباره وارد شوید.',
                'code' => $code,
            ], 401);
        }

        return [$username, $userType];
    }

    public static function requirePermission(string $username, string $userType, string $feature): void {
        if (!(new PermissionService())->hasPermission($username, $userType, $feature)) {
            Response::error('شما مجوز انجام این عملیات را ندارید.', 403);
        }
    }
}
