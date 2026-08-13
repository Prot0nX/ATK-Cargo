<?php
// PHP/src/Core/AuthenticatesRequests.php

declare(strict_types=1);

namespace App\Core;

use App\Repositories\UserRepository;
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

    private function requireAuthenticatedSession(): void {
        $username = (string)($this->request->getHeader('X-Username') ?? '');
        $deviceId = (string)($this->request->getHeader('X-Device-Id') ?? '');
        $token = (string)($this->request->getHeader('X-Session-Token') ?? '');

        $sessionService = new SessionService();
        if (!$sessionService->isValidToken($username, $deviceId, $token)) {
            header('Content-Type: application/json; charset=UTF-8');
            http_response_code(401);
            echo json_encode(['error' => true, 'message' => 'نشست معتبر نیست. لطفاً دوباره وارد شوید.'], JSON_UNESCAPED_UNICODE);
            exit;
        }

        $this->authenticatedUsername = $username;

        $user = (new UserRepository())->getByUsername($username);
        $this->authenticatedUserType = (string)($user['userType'] ?? '');
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
            header('Content-Type: application/json; charset=UTF-8');
            http_response_code(403);
            echo json_encode(['error' => true, 'message' => 'شما مجوز انجام این عملیات را ندارید.'], JSON_UNESCAPED_UNICODE);
            exit;
        }
    }
}
