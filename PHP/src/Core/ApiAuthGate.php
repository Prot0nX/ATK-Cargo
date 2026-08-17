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

        $userType = (new SessionService())->validateAndGetUserType($username, $deviceId, $token);
        if ($userType === null) {
            Response::error('نشست معتبر نیست. لطفاً دوباره وارد شوید.', 401);
        }

        return [$username, $userType];
    }

    public static function requirePermission(string $username, string $userType, string $feature): void {
        if (!(new PermissionService())->hasPermission($username, $userType, $feature)) {
            Response::error('شما مجوز انجام این عملیات را ندارید.', 403);
        }
    }
}
