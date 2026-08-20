<?php
// PHP/src/Controllers/UserController.php

declare(strict_types=1);

namespace App\Controllers;

use App\Core\AuthenticatesRequests;
use App\Core\Request;
use App\Core\Response;
use App\Services\LoginAttemptLimiter;
use App\Services\UserService;
use App\Validators\InputValidator;
use App\Exceptions\ApiException;

class UserController {
    use AuthenticatesRequests;

    private UserService $userService;
    private Request $request;
    private LoginAttemptLimiter $loginAttemptLimiter;

    public function __construct() {
        $this->userService = new UserService();
        $this->request = new Request();
        $this->loginAttemptLimiter = new LoginAttemptLimiter();
    }

    // actionهایی که فقط مدیر (دسترسی manage_users) مجاز به اجرای آن‌هاست.
    // getAllUsers قبلاً اینجا نبود و فهرست کامل username/fullName/userType
    // همه‌ی کاربران را به هر کاربر احرازشده می‌داد (Excessive Data Exposure —
    // DEEP_CODE_AUDIT.md #Phase1.3). صفحه‌ی «تنظیمات پروفایل» و «چت با مدیر»
    // که قبلاً از getAllUsers استفاده می‌کردند اکنون به‌ترتیب از
    // getSelfProfile و getAdminUsers استفاده می‌کنند که فقط دامنه‌ی
    // موردنیاز خودشان را برمی‌گردانند و به هر کاربر احرازشده اجازه داده
    // می‌شوند (بدون نیاز به manage_users).
    private const ADMIN_ONLY_ACTIONS = [
        'getAllUsers',
        'getAllUsersWithStatus',
        'getActiveDeviceId',
        'createUser',
        'deleteUser',
        'forceLogout',
    ];

    /**
     * مدیریت و مسیریابی درخواست‌های کاربران
     *
     * تمام actionهای این کنترلر (خواندن/ساخت/ویرایش/حذف کاربران، خروج
     * اجباری) داده‌ی حساس هستند؛ بدون این گیت هر کلاینت ناشناس می‌توانست
     * کاربر admin بسازد یا رمز/نوع کاربری هر کاربر موجود را تغییر دهد (S-01).
     */
    public function handle(): void {
        try {
            $this->requireAuthenticatedSession();

            $action = $this->request->get('action');
            if (!$action) {
                throw new ApiException('پارامتر action مورد نیاز است', 400);
            }
            $action = (string)$action;

            if (in_array($action, self::ADMIN_ONLY_ACTIONS, true)) {
                $this->requirePermission('manage_users');
            }

            if ($this->request->isGet()) {
                $this->handleGet($action);
            } elseif ($this->request->isPost()) {
                $this->handlePost($action);
            } else {
                throw new ApiException('روش درخواست نامعتبر است', 405);
            }
        } catch (ApiException $e) {
            Response::json([
                'success' => false,
                'message' => $e->getMessage()
            ], $e->getStatusCode());
        } catch (\Exception $e) {
            error_log("Error in UserController: " . $e->getMessage());
            Response::json([
                'success' => false,
                'message' => 'خطایی در پردازش درخواست رخ داده است.'
            ], 400);
        }
    }

    private function handleGet(string $action): void {
        switch ($action) {
            case 'getAllUsers':
                $users = $this->userService->getAllUsers();
                Response::json($users);
                break;

            case 'getAllUsersWithStatus':
                $users = $this->userService->getAllUsersWithStatus();
                Response::json($users);
                break;

            case 'getSelfProfile':
                $profile = $this->userService->getSelfProfile((string)$this->authenticatedUsername);
                if (!$profile) {
                    throw new ApiException('کاربر یافت نشد', 404);
                }
                Response::json($profile);
                break;

            case 'getAdminUsers':
                $admins = $this->userService->getAdminUsers();
                Response::json($admins);
                break;

            case 'getActiveDeviceId':
                $username = $this->request->get('username');
                if (!$username) {
                    throw new ApiException('نام کاربری الزامی است', 400);
                }
                
                $sessionService = new \App\Services\SessionService();
                $activeSession = $sessionService->isSessionActive((string)$username);
                
                if ($activeSession) {
                    $sessionRepo = new \App\Repositories\SessionRepository();
                    $sessionData = $sessionRepo->getActiveSession((string)$username);
                    Response::json([
                        'success' => true,
                        'message' => 'جلسه فعال یافت شد',
                        'device_id' => $sessionData['device_id'] ?? null,
                        'last_activity' => $sessionData['last_activity'] ?? null,
                        'login_time' => $sessionData['login_time'] ?? null
                    ]);
                } else {
                    Response::json([
                        'success' => false,
                        'message' => 'هیچ جلسه فعالی برای این کاربر یافت نشد',
                        'device_id' => null
                    ]);
                }
                break;

            default:
                throw new ApiException('عملیات نامعتبر است', 400);
        }
    }

    private function handlePost(string $action): void {
        $params = $this->request->all();

        switch ($action) {
            case 'createUser':
                InputValidator::validateRequired($params, ['username', 'fullName', 'password', 'userType']);
                
                $username = InputValidator::validateUsername((string)$params['username']);
                $fullName = InputValidator::validateFullName((string)$params['fullName']);
                $password = InputValidator::validatePassword((string)$params['password']);
                $userType = InputValidator::sanitize((string)$params['userType']);

                $result = $this->userService->createUser([
                    'username' => $username,
                    'fullName' => $fullName,
                    'password' => $password,
                    'userType' => $userType
                ], $this->authenticatedUsername);
                Response::json($result);
                break;

            case 'updateUser':
                InputValidator::validateRequired($params, ['id']);
                $id = (int)$params['id'];

                // کاربر بدون دسترسی manage_users (مثلاً از دیالوگ «تغییر رمز
                // عبور» در تنظیمات پروفایل خودش) فقط مجاز به ویرایش رکورد
                // خودش است و فقط فیلدهای غیرحساس (fullName/password)؛ بدون
                // این بررسی، هر کاربر احرازشده می‌توانست با فرستادن id دلخواه
                // رمز/نوع کاربری هر کاربر دیگری (از جمله ادمین) را عوض کند.
                $isAdmin = (new \App\Services\PermissionService())
                    ->hasPermission($this->authenticatedUsername ?? '', $this->authenticatedUserType ?? '', 'manage_users');

                if (!$isAdmin) {
                    $selfRecord = $this->userService->getSelfProfile((string)$this->authenticatedUsername);
                    if (!$selfRecord || (int)$selfRecord['id'] !== $id) {
                        throw new ApiException('شما فقط مجاز به ویرایش حساب خودتان هستید.', 403);
                    }
                    if (isset($params['username']) || isset($params['userType'])) {
                        throw new ApiException('شما مجاز به تغییر نام کاربری یا نوع کاربری خودتان نیستید.', 403);
                    }

                    // بدون این، یک session token دزدیده‌شده (که فقط تا انقضای
                    // نشست کار می‌کرد) می‌توانست رمز را عوض کند و به تصاحب
                    // دائمی حساب ارتقا پیدا کند (DEEP_CODE_AUDIT.md #Phase2.8).
                    // فقط برای خودِ کاربر اعمال می‌شود؛ ادمین هنگام تغییر رمز
                    // کاربر دیگر از این مسیر عبور نمی‌کند.
                    if (isset($params['password'])) {
                        $username = (string)$this->authenticatedUsername;
                        $ip = (string)($_SERVER['REMOTE_ADDR'] ?? 'unknown');
                        if ($this->loginAttemptLimiter->isLocked($username, $ip)) {
                            throw new ApiException('تعداد تلاش‌های ناموفق بیش از حد مجاز است. لطفاً ۱۵ دقیقه دیگر تلاش کنید.', 429);
                        }
                        $currentPassword = (string)($params['currentPassword'] ?? '');
                        if ($currentPassword === '' || $this->userService->verifyCredentials($username, $currentPassword) === null) {
                            $this->loginAttemptLimiter->registerFailedAttempt($username, $ip);
                            throw new ApiException('رمز عبور فعلی نادرست است.', 403);
                        }
                        $this->loginAttemptLimiter->resetAttempts($username, $ip);
                    }
                }

                $updates = [];
                if (isset($params['username'])) {
                    $updates['username'] = InputValidator::validateUsername((string)$params['username']);
                }
                if (isset($params['fullName'])) {
                    $updates['fullName'] = InputValidator::validateFullName((string)$params['fullName']);
                }
                if (isset($params['password'])) {
                    $updates['password'] = InputValidator::validatePassword((string)$params['password']);
                }
                if (isset($params['userType'])) {
                    $updates['userType'] = InputValidator::sanitize((string)$params['userType']);
                }

                $result = $this->userService->updateUser($id, $updates, $this->authenticatedUsername);
                Response::json($result);
                break;

            case 'deleteUser':
                InputValidator::validateRequired($params, ['userId']);
                $userId = (int)$params['userId'];
                $result = $this->userService->deleteUser($userId, $this->authenticatedUsername);
                Response::json($result);
                break;

            case 'forceLogout':
                InputValidator::validateRequired($params, ['username']);
                $username = InputValidator::sanitize((string)$params['username']);
                $deviceId = isset($params['device_id']) ? InputValidator::sanitize((string)$params['device_id']) : '';

                $sessionService = new \App\Services\SessionService();
                if (!empty($deviceId)) {
                    $result = $sessionService->forceLogoutFromDevice($username, $deviceId);
                } else {
                    $result = $sessionService->deactivateSession($username);
                }
                
                // فرمت پاسخ برای هماهنگی با کلاینت اندروید
                if (isset($result['http_code'])) {
                    unset($result['http_code']);
                }
                Response::json($result);
                break;

            default:
                throw new ApiException('عملیات نامعتبر است', 400);
        }
    }

    /**
     * به‌روزرسانی توکن FCM (POST users/fcm-token)
     *
     * قبلاً بدون احراز هویت بود و user_id مستقیم از ورودی خوانده می‌شد؛ هر
     * کلاینت ناشناس می‌توانست توکن push هر کاربر دلخواه را با توکن خودش
     * جایگزین کند (S-08). حالا user_id از نشست احرازشده گرفته می‌شود، نه از
     * ورودی — پارامتر user_id ورودی نادیده گرفته می‌شود.
     */
    public function updateFcmToken(): void {
        $this->requireAuthenticatedSession();

        $userRepo = new \App\Repositories\UserRepository();
        $currentUser = $userRepo->getByUsername((string)$this->authenticatedUsername);
        if (!$currentUser) {
            Response::json(['error' => 'کاربر یافت نشد'], 404);
        }
        $userId = (int)$currentUser['id'];
        $token = (string)$this->request->get('token', '');

        if (!$userId || empty($token)) {
            Response::json(['error' => 'Missing user_id or token'], 400);
        }

        try {
            $conn = \App\Core\Database::getInstance()->getMysqliConnection();
            $stmt = $conn->prepare("UPDATE Users SET fcm_token = ? WHERE id = ?");
            if (!$stmt) {
                // تست جدول users در صورت حروف کوچک
                $stmt = $conn->prepare("UPDATE users SET fcm_token = ? WHERE id = ?");
            }
            $stmt->bind_param("si", $token, $userId);
            if ($stmt->execute()) {
                $stmt->close();
                Response::json(['message' => 'FCM token updated successfully']);
            } else {
                $stmt->close();
                Response::json(['error' => 'Failed to update FCM token'], 500);
            }
        } catch (\Exception $e) {
            Response::json(['error' => 'Failed to update FCM token'], 500);
        }
    }
}

