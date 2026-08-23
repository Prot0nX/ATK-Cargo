<?php
// PHP/src/Controllers/UserController.php

declare(strict_types=1);

namespace App\Controllers;

use App\Core\ApiAuthGate;
use App\Core\Request;
use App\Core\Response;
use App\Services\LoginAttemptLimiter;
use App\Services\UserService;
use App\Validators\InputValidator;
use App\Exceptions\ApiException;

class UserController {
    private UserService $userService;
    private Request $request;
    private LoginAttemptLimiter $loginAttemptLimiter;
 // از Router::dispatch پر می‌شود، نه اعتبارسنجی داخلی
    private ?string $authenticatedUsername = null;
    private ?string $authenticatedUserType = null;

    public function __construct() {
        $this->userService = new UserService();
        $this->request = new Request();
        $this->loginAttemptLimiter = new LoginAttemptLimiter();
    }

    // actionهای مخصوص مدیر؛ getAllUsers قفل شد تا افشای اطلاعات همه‌ی کاربران رخ ندهد (Phase1.3)
    private const ADMIN_ONLY_ACTIONS = [
        'getAllUsers',
        'getAllUsersWithStatus',
        'getActiveDeviceId',
        'createUser',
        'deleteUser',
        'forceLogout',
    ];

    // مدیریت و مسیریابی درخواست‌های کاربران؛ $username/$userType از Router::dispatch می‌آیند — همه‌ی routeهای این
    // کنترلر auth=>true دارند. getAllUsers با وجود permission=>null سطح Router، تنها از طریق عضویت در
 // ADMIN_ONLY_ACTIONS محدود می‌شود، پس این بررسی داخلی حذف نشد، فقط به ApiAuthGate منتقل شد
    public function handle(?string $username, ?string $userType): void {
        $this->authenticatedUsername = $username;
        $this->authenticatedUserType = $userType;
        try {
            $action = $this->request->get('action');
            if (!$action) {
                throw new ApiException('پارامتر action مورد نیاز است', 400);
            }
            $action = (string)$action;

            if (in_array($action, self::ADMIN_ONLY_ACTIONS, true)) {
                ApiAuthGate::requirePermission((string)$username, (string)$userType, 'manage_users');
            }

            if ($this->request->isGet()) {
                $this->handleGet($action);
            } elseif ($this->request->isWrite()) {
                // isWrite() هر سه فعل POST/PATCH/DELETE را پوشش می‌دهد؛ تفکیک واقعی با action انجام می‌شود (Phase4 #33)
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

                // کاربر بدون manage_users فقط مجاز به ویرایش رکورد خودش با فیلدهای غیرحساس است، وگرنه IDOR ممکن بود
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

                    // تأیید رمز فعلی برای تغییر رمز خود کاربر، تا session token دزدیده‌شده به تصاحب دائمی حساب ارتقا نیابد (Phase2.8)
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

}

