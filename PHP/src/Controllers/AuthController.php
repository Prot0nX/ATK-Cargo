<?php
// PHP/src/Controllers/AuthController.php

declare(strict_types=1);

namespace App\Controllers;

use App\Core\Request;
use App\Core\Response;
use App\Core\Logger;
use App\Services\UserService;
use App\Services\SessionService;
use App\Validators\InputValidator;
use App\Exceptions\ApiException;

class AuthController {
    private UserService $userService;
    private SessionService $sessionService;
    private Request $request;
    private Logger $logger;

    public function __construct() {
        $this->userService = new UserService();
        $this->sessionService = new SessionService();
        $this->request = new Request();
        $this->logger = Logger::getInstance();
    }

    /**
     * ورود کاربر و احراز هویت (check_Auth.php)
     */
    public function login(): void {
        if (!$this->request->isPost()) {
            Response::error('روش درخواست مجاز نیست. لطفاً از روش POST استفاده کنید.', 405);
        }

        $username = $this->request->get('username');
        $password = $this->request->get('password');
        $requestedSection = $this->request->get('userType'); // نام متغیر در ریکوئست اندروید
        $deviceModel = $this->request->get('deviceModel', '');
        $deviceId = $this->request->get('deviceId', '');
        $androidVersion = $this->request->get('androidVersion', '');
        $appVersion = $this->request->get('appVersion', '');

        if (!$username || !$password) {
            Response::error('نام کاربری و رمز عبور الزامی است.', 400);
        }

        // پاک‌سازی ورودی‌ها
        $username = InputValidator::sanitize((string)$username);
        $requestedSection = InputValidator::sanitize((string)$requestedSection);
        $deviceModel = InputValidator::sanitize((string)$deviceModel);
        $deviceId = InputValidator::sanitize((string)$deviceId);
        $androidVersion = InputValidator::sanitize((string)$androidVersion);
        $appVersion = InputValidator::sanitize((string)$appVersion);

        // تلاش برای احراز هویت
        $user = $this->userService->verifyCredentials($username, (string)$password);

        $this->logger->info(
            sprintf("Login attempt - Username: %s, Result: %s", $username, $user ? 'Success' : 'Failed'),
            'security'
        );

        if (!$user) {
            Response::json([
                'success' => false,
                'message' => 'نام کاربری یا رمز عبور اشتباه است!',
                'userType' => null
            ], 200); // بازگرداندن 200 برای پایداری با کلاینت اندروید
        }

        // بارگذاری تنظیمات سطح دسترسی
        $userPermissions = $this->getUserPermissions($username, $user['userType']);

        // بررسی دسترسی به بخش درخواستی (اگر ارسال شده باشد)
        $allowedAccess = true;
        if (!empty($requestedSection)) {
            $allowedAccess = $userPermissions[$requestedSection] ?? ($user['userType'] === 'admin');
        }

        if (!$allowedAccess) {
            Response::json([
                'success' => false,
                'message' => 'شما دسترسی ورود به این بخش را ندارید.',
                'userType' => null
            ], 200);
        }

        // مدیریت جلسه
        $ipAddress = $this->request->getClientIp();
        
        // فراخوانی سرویس برای ایجاد یا به‌روزرسانی جلسه موبایل
        $sessionResult = $this->sessionService->createMobileSession(
            $username,
            $deviceId,
            $deviceModel,
            $androidVersion,
            $ipAddress,
            $user['userType'],
            $appVersion
        );

        if (!$sessionResult['success']) {
            // خطا به دلیل ورود همزمان از دستگاه دیگر
            Response::json([
                'success' => false,
                'message' => $sessionResult['message'],
                'userType' => null
            ], 200); // برای پایداری کلاینت اندروید، ترجیحاً پاسخ 200 یا وضعیت‌های سازگار ارسال می‌شود
        }

        // ورود موفق
        Response::json([
            'success' => true,
            'message' => 'ورود موفقیت‌آمیز بود',
            'userType' => $user['userType'],
            'session_token' => $sessionResult['session_token'],
            'permissions' => $userPermissions
        ]);
    }

    /**
     * بررسی وضعیت نشست کاربر (check_session.php)
     */
    public function checkSession(): void {
        if (!$this->request->isPost()) {
            Response::error('روش درخواست مجاز نیست. لطفاً از روش POST استفاده کنید.', 405);
        }

        $username = $this->request->get('username');
        $deviceId = $this->request->get('deviceId');
        
        if (!$username) {
            Response::error('نام کاربری الزامی است.', 400);
        }

        $username = InputValidator::sanitize((string)$username);
        $deviceId = $deviceId ? InputValidator::sanitize((string)$deviceId) : null;

        // دریافت اطلاعات کاربر برای استخراج userType
        $userRepo = new \App\Repositories\UserRepository();
        $user = $userRepo->getByUsername($username);

        if (!$user) {
            Response::json([
                'success' => false,
                'message' => 'کاربر در سیستم وجود ندارد',
                'userType' => null
            ], 200); // 200 برای پایداری اندروید
        }

        $isActive = $this->sessionService->isSessionActive($username, $deviceId);

        if ($isActive) {
            Response::json([
                'success' => true,
                'message' => 'جلسه کاربر معتبر است',
                'userType' => $user['userType']
            ]);
        } else {
            Response::json([
                'success' => false,
                'message' => 'جلسه کاربر فعال نیست. لطفاً وارد شوید.',
                'userType' => $user['userType']
            ]);
        }
    }

    /**
     * خروج کاربر از سیستم (check_logout.php)
     */
    public function logout(): void {
        if (!$this->request->isPost()) {
            Response::error('روش درخواست مجاز نیست. لطفاً از روش POST استفاده کنید.', 405);
        }

        $username = $this->request->get('username');
        $deviceId = $this->request->get('deviceId');

        if (!$username) {
            Response::error('نام کاربری الزامی است.', 400);
        }

        $username = InputValidator::sanitize((string)$username);
        $deviceId = $deviceId ? InputValidator::sanitize((string)$deviceId) : null;

        $result = $this->sessionService->deactivateSession($username, $deviceId);

        Response::json([
            'success' => $result['success'],
            'message' => $result['message']
        ], $result['http_code'] ?? 200);
    }

    /**
     * خواندن سطوح دسترسی کاربر از فایل permissions.json
     */
    private function getUserPermissions(string $username, string $userType): array {
        $permissionsFile = APP_ROOT . '/config/permissions.json';
        if (!file_exists($permissionsFile)) {
            return [];
        }

        $allData = json_decode(file_get_contents($permissionsFile), true);
        if (!$allData) {
            return [];
        }

        if (isset($allData['roles'])) {
            return $allData['users'][$username] ?? $allData['roles'][$userType] ?? [];
        }

        return $allData[$userType] ?? [];
    }
}
