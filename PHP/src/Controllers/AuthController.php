<?php
// PHP/src/Controllers/AuthController.php

declare(strict_types=1);

namespace App\Controllers;

use App\Core\Request;
use App\Core\Response;
use App\Core\Logger;
use App\Services\UserService;
use App\Services\SessionService;
use App\Services\PermissionService;
use App\Services\LoginAttemptLimiter;
use App\Validators\InputValidator;
use App\Exceptions\ApiException;

class AuthController {
    private UserService $userService;
    private SessionService $sessionService;
    private PermissionService $permissionService;
    private LoginAttemptLimiter $loginAttemptLimiter;
    private Request $request;
    private Logger $logger;

    public function __construct() {
        $this->userService = new UserService();
        $this->sessionService = new SessionService();
        $this->permissionService = new PermissionService();
        $this->loginAttemptLimiter = new LoginAttemptLimiter();
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

        $ipAddress = $this->request->getClientIp();

        // قفل تلاش‌های ناموفق (username+IP) — پیش از این فقط rate-limit عمومی
        // پروکسی (۶۰ درخواست/دقیقه به‌ازای IP) وجود داشت که با چرخش IP دور زده
        // می‌شد و عملاً حمله‌ی brute-force را محدود نمی‌کرد (S-06).
        if ($this->loginAttemptLimiter->isLocked($username, $ipAddress)) {
            Response::json([
                'success' => false,
                'message' => 'تعداد تلاش‌های ناموفق بیش از حد مجاز است. لطفاً ۱۵ دقیقه دیگر تلاش کنید.',
                'userType' => null
            ], 200); // ۲۰۰ برای پایداری با کلاینت اندروید، هم‌راستا با بقیه‌ی خطاهای این تابع
        }

        // تلاش برای احراز هویت
        $user = $this->userService->verifyCredentials($username, (string)$password);

        $this->logger->info(
            sprintf("Login attempt - Username: %s, Result: %s", $username, $user ? 'Success' : 'Failed'),
            'security'
        );

        if (!$user) {
            $this->loginAttemptLimiter->registerFailedAttempt($username, $ipAddress);
            Response::json([
                'success' => false,
                'message' => 'نام کاربری یا رمز عبور اشتباه است!',
                'userType' => null
            ], 200); // بازگرداندن 200 برای پایداری با کلاینت اندروید
        }

        $this->loginAttemptLimiter->resetAttempts($username, $ipAddress);

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

        // مدیریت جلسه ($ipAddress پیش‌تر برای گیت rate-limit محاسبه شده)
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
            // کد 409 برگردانده می‌شود تا کلاینت اندروید بتواند از مسیر اختصاصی
            // LoginResult.ConflictSession استفاده کند (مطابق قرارداد قبلی API)
            Response::json([
                'success' => false,
                'message' => $sessionResult['message'],
                'userType' => null
            ], 409);
        }

        // ورود موفق. session_token همچنان برای سازگاری با نصب‌های فعلی اپ
        // فرستاده می‌شود (همان access token کوتاه‌مدت جدید است)؛ فیلدهای جدید
        // (I-05) برای نسخه‌ای از اپ که هنوز منتشر نشده اضافه شده‌اند.
        Response::json([
            'success' => true,
            'message' => 'ورود موفقیت‌آمیز بود',
            'userType' => $user['userType'],
            'session_token' => $sessionResult['session_token'],
            'access_token_expires_in' => $sessionResult['access_token_expires_in'],
            'refresh_token' => $sessionResult['refresh_token'],
            'refresh_token_expires_in' => $sessionResult['refresh_token_expires_in'],
            'permissions' => $userPermissions
        ]);
    }

    /**
     * تمدید access token با استفاده از refresh token — فقط روی Router v2
     * (`/api/v2/auth/refresh`) در دسترس است، نه protected_proxy.php (I-05).
     */
    public function refresh(): void {
        if (!$this->request->isPost()) {
            Response::error('روش درخواست مجاز نیست. لطفاً از روش POST استفاده کنید.', 405);
        }

        $username = $this->request->get('username');
        $deviceId = $this->request->get('deviceId');
        $refreshToken = $this->request->get('refreshToken');

        if (!$username || !$deviceId || !$refreshToken) {
            Response::error('username، deviceId و refreshToken الزامی هستند.', 400);
        }

        $username = InputValidator::sanitize((string)$username);
        $deviceId = InputValidator::sanitize((string)$deviceId);
        $refreshToken = (string)$refreshToken; // مقایسه‌ی دقیق با hash_equals؛ نباید توسط sanitize تغییر کند
        $ipAddress = $this->request->getClientIp();

        // Router v2 برخلاف protected_proxy.php (v1) هیچ rate-limit عمومی‌ای
        // ندارد؛ چون این endpoint بدون گیت auth است (نمی‌تواند بدون auth باشد
        // چون دقیقاً برای توکن منقضی صدا زده می‌شود)، طبق تصمیم طراحی همان
        // الگوی قفل ۵ تلاش/۱۵ دقیقه‌ی LoginAttemptLimiter (username+IP) اینجا
        // هم استفاده می‌شود — نه برای برute-force عملی (حدس یک توکن ۲۵۶ بیتی
        // غیرممکن است)، بلکه به‌عنوان لایه‌ی دوم در برابر اسپم/سوءاستفاده.
        if ($this->loginAttemptLimiter->isLocked($username, $ipAddress)) {
            Response::error('تعداد تلاش‌های ناموفق بیش از حد مجاز است. لطفاً ۱۵ دقیقه دیگر تلاش کنید.', 429);
        }

        $result = $this->sessionService->refreshTokens($username, $deviceId, $refreshToken);

        if (!$result['success']) {
            $this->loginAttemptLimiter->registerFailedAttempt($username, $ipAddress);
            Response::error($result['message'], $result['http_code']);
        }

        $this->loginAttemptLimiter->resetAttempts($username, $ipAddress);

        Response::json([
            'success' => true,
            'session_token' => $result['session_token'],
            'access_token_expires_in' => $result['access_token_expires_in'],
            'refresh_token' => $result['refresh_token'],
            'refresh_token_expires_in' => $result['refresh_token_expires_in'],
            'userType' => $result['userType'],
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
        $sessionToken = $this->request->get('sessionToken');

        if (!$username) {
            Response::error('نام کاربری الزامی است.', 400);
        }

        $username = InputValidator::sanitize((string)$username);
        $deviceId = $deviceId ? InputValidator::sanitize((string)$deviceId) : null;
        $sessionToken = $sessionToken ? InputValidator::sanitize((string)$sessionToken) : null;

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

        // اعتبارسنجی با توکن نشست (نه فقط username+deviceId که هیچ‌کدام سرّی
        // نیستند) — همان گیت isValidToken که AuthenticatesRequests برای سایر
        // APIهای تجاری استفاده می‌کند؛ بدون توکن معتبر، نشست نامعتبر است (C-5)
        $isActive = ($deviceId && $sessionToken)
            ? $this->sessionService->isValidToken($username, $deviceId, $sessionToken)
            : false;

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
        return $this->permissionService->getUserPermissions($username, $userType);
    }
}
