<?php
// PHP/src/Core/ApiAuthGate.php

declare(strict_types=1);

namespace App\Core;

use App\Services\PermissionService;
use App\Services\SessionService;

// گیت static احراز هویت/مجوز router v2؛ Router::dispatch آن را برای هر route با auth=>true صدا می‌زند
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
            // تمایز بین انقضای access token (نیاز به refresh بی‌صدا) و نامعتبر بودن کل نشست (نیاز به login مجدد)
            $code = $sessionService->isAccessTokenExpiredButSessionActive($username, $deviceId, $token)
                ? 'access_token_expired'
                : 'session_invalid';

            // چون Response::error فیلد code را پشتیبانی نمی‌کند، اینجا مستقیماً json فرستاده می‌شود
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
