<?php
// PHP/src/Core/AuthenticatesRequests.php

declare(strict_types=1);

namespace App\Core;

use App\Services\PermissionService;
use App\Services\SessionService;

// گیت مشترک احراز هویت/مجوز به‌صورت trait؛ کلاس مصرف‌کننده باید پراپرتی Request $request داشته باشد
trait AuthenticatesRequests {
    private ?string $authenticatedUsername = null;
    private ?string $authenticatedUserType = null;

    // بررسی نشست با SessionService؛ به‌جای ۳ کوئری قدیمی حالا یک SELECT + یک UPDATE throttled انجام می‌شود
    private function requireAuthenticatedSession(): void {
        $this->enforceMinAppVersion();

        $username = (string)($this->request->getHeader('X-Username') ?? '');
        $deviceId = (string)($this->request->getHeader('X-Device-Id') ?? '');
        $token = (string)($this->request->getHeader('X-Session-Token') ?? '');

        $sessionService = new SessionService();
        $userType = $sessionService->validateAndGetUserType($username, $deviceId, $token);
        if ($userType === null) {
            // تمایز بین انقضای access token (نیاز به refresh) و نامعتبر بودن کل نشست (نیاز به login مجدد)
            $code = $sessionService->isAccessTokenExpiredButSessionActive($username, $deviceId, $token)
                ? 'access_token_expired'
                : 'session_invalid';
            $this->sendAuthErrorResponse('نشست معتبر نیست. لطفاً دوباره وارد شوید.', 401, $code);
        }

        $this->authenticatedUsername = $username;
        $this->authenticatedUserType = $userType;
    }

    // ارسال پاسخ خطا؛ شکل پیش‌فرض مطابق قرارداد CargoController/UtilityController و قابل override برای سایرین
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

    // اجرای سمت‌سرور قفل نسخه‌ی منقضی؛ در صورت نبود هدر X-App-Version عبور مجاز است
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

    // گیت سطح دسترسی برای actionهای حساس؛ باید بعد از requireAuthenticatedSession صدا زده شود
    private function requirePermission(string $feature): void {
        $username = $this->authenticatedUsername ?? '';
        $userType = $this->authenticatedUserType ?? '';

        $permissionService = new PermissionService();
        if (!$permissionService->hasPermission($username, $userType, $feature)) {
            $this->sendAuthErrorResponse('شما مجوز انجام این عملیات را ندارید.', 403);
        }
    }
}
