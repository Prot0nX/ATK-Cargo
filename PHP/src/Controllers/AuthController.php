<?php

declare(strict_types=1);

namespace AtkCargo\Controllers;

use AtkCargo\Core\Request;
use AtkCargo\Core\Response;
use AtkCargo\Services\AuthService;
use Exception;

/**
 * Controller class to route and handle auth requests
 */
class AuthController
{
    private AuthService $authService;

    public function __construct()
    {
        $this->authService = new AuthService();
    }

    /**
     * Handle login API request
     */
    public function login(Request $request): void
    {
        if ($request->getMethod() !== 'POST') {
            Response::json([
                'success' => false,
                'message' => 'روش درخواست مجاز نیست. لطفاً از روش POST استفاده کنید.'
            ], 405);
        }

        $credentials = [
            'username' => $request->getString('username'),
            'password' => $request->getString('password'),
            'userType' => $request->getString('userType'),
            'deviceId' => $request->getString('deviceId'),
            'deviceModel' => $request->getString('deviceModel'),
            'androidVersion' => $request->getString('androidVersion'),
            'appVersion' => $request->getString('appVersion'),
            'ipAddress' => $_SERVER['REMOTE_ADDR'] ?? 'Unknown'
        ];

        try {
            $result = $this->authService->login($credentials);
            Response::json($result);
        } catch (Exception $e) {
            $code = $e->getCode();
            $statusCode = ($code >= 400 && $code <= 500) ? $code : 400;
            Response::json([
                'success' => false,
                'message' => $e->getMessage()
            ], $statusCode);
        }
    }

    /**
     * Handle session check API request
     */
    public function checkSession(Request $request): void
    {
        if ($request->getMethod() !== 'POST') {
            Response::json([
                'success' => false,
                'message' => 'روش درخواست مجاز نیست. لطفاً از روش POST استفاده کنید.'
            ], 405);
        }

        $username = $request->getString('username');
        $deviceId = $request->getString('deviceId');

        if (empty($username)) {
            Response::json([
                'success' => false,
                'message' => 'نام کاربری الزامی است.'
            ], 400);
        }

        try {
            $user = (new \AtkCargo\Repositories\SessionRepository())->getUserByUsername($username);
            if (!$user) {
                Response::json([
                    'success' => false,
                    'message' => 'کاربر در سیستم وجود ندارد'
                ], 404);
            }

            $isActive = $this->authService->checkSession($username, $deviceId);

            if ($isActive) {
                Response::json([
                    'success' => true,
                    'message' => 'جلسه کاربر معتبر است',
                    'userType' => $user['userType']
                ]);
            } else {
                Response::json([
                    'success' => false,
                    'message' => 'جلسه کاربر فعال نیست. لطفاً وارد شوید.'
                ], 401);
            }
        } catch (Exception $e) {
            Response::json([
                'success' => false,
                'message' => 'خطایی رخ داده است. لطفا بعدا تلاش کنید.'
            ], 500);
        }
    }

    /**
     * Handle logout API request
     */
    public function logout(Request $request): void
    {
        if ($request->getMethod() !== 'POST') {
            Response::json([
                'success' => false,
                'message' => 'روش درخواست مجاز نیست. لطفاً از روش POST استفاده کنید.'
            ], 405);
        }

        $username = $request->getString('username');
        $deviceId = $request->getString('deviceId');

        if (empty($username)) {
            Response::json([
                'success' => false,
                'message' => 'نام کاربری الزامی است.'
            ], 400);
        }

        try {
            $result = $this->authService->deactivateSession($username, $deviceId);
            $httpCode = $result['http_code'] ?? 200;
            unset($result['http_code']);
            Response::json($result, $httpCode);
        } catch (Exception $e) {
            Response::json([
                'success' => false,
                'message' => 'خطایی در فرآیند خروج رخ داده است.'
            ], 500);
        }
    }

    /**
     * Handle live permission synchronization API request
     */
    public function syncPermissions(Request $request): void
    {
        if ($request->getMethod() !== 'POST') {
            Response::json([
                'success' => false,
                'message' => 'Only POST method is allowed.'
            ], 405);
        }

        $username = $request->getString('username');
        $deviceId = $request->getString('deviceId');

        if (empty($username)) {
            Response::json([
                'success' => false,
                'message' => 'نام کاربری الزامی است.'
            ], 400);
        }

        try {
            $result = $this->authService->syncPermissions($username, $deviceId);
            Response::json($result);
        } catch (Exception $e) {
            $code = $e->getCode();
            $statusCode = ($code >= 400 && $code <= 500) ? $code : 500;
            Response::json([
                'success' => false,
                'message' => $e->getMessage()
            ], $statusCode);
        }
    }

    /**
     * Get active online users with Jalali date formats
     */
    public function getOnlineUsers(Request $request): void
    {
        try {
            $onlineUsers = $this->authService->getOnlineUsers();
            $processedUsers = [];

            foreach ($onlineUsers as $user) {
                $loginTimeJalali = '';
                $lastActivityJalali = '';

                if ($user['login_time']) {
                    $loginTimeJalali = \AtkCargo\Helpers\DateHelper::formatJalali('Y/m/d H:i:s', strtotime($user['login_time']));
                }

                if ($user['last_activity']) {
                    $lastActivityJalali = \AtkCargo\Helpers\DateHelper::formatJalali('Y/m/d H:i:s', strtotime($user['last_activity']));
                }

                $idleSeconds = $user['idle_time'] ?? 0;
                $idleMinutes = (int)floor($idleSeconds / 60);
                $status = 'online';
                $statusText = 'آنلاین';

                if ($idleMinutes > 5) {
                    $status = 'idle';
                    $statusText = "بیکار ($idleMinutes دقیقه)";
                }

                $processedUsers[] = [
                    'id' => $user['id'],
                    'username' => $user['username'],
                    'userType' => $user['userType'],
                    'device_model' => $user['device_model'],
                    'device_id' => $user['device_id'],
                    'ip_address' => $user['ip_address'],
                    'login_time' => $user['login_time'],
                    'login_time_jalali' => $loginTimeJalali,
                    'last_activity' => $user['last_activity'],
                    'last_activity_jalali' => $lastActivityJalali,
                    'online_duration' => $user['online_duration'],
                    'idle_time' => $idleSeconds,
                    'idle_minutes' => $idleMinutes,
                    'status' => $status,
                    'status_text' => $statusText
                ];
            }

            Response::json([
                'success' => true,
                'users' => $processedUsers,
                'total_count' => count($processedUsers),
                'last_update' => \AtkCargo\Helpers\DateHelper::formatJalali('Y/m/d H:i:s')
            ]);
        } catch (Exception $e) {
            Response::json([
                'success' => false,
                'message' => 'خطا در دریافت کاربران آنلاین: ' . $e->getMessage()
            ], 500);
        }
    }

    /**
     * Get aggregate session statistics
     */
    public function getSessionStats(Request $request): void
    {
        try {
            $stats = $this->authService->getSessionStats();
            Response::json([
                'success' => true,
                'stats' => $stats
            ]);
        } catch (Exception $e) {
            Response::json([
                'success' => false,
                'message' => 'خطا در دریافت آمار: ' . $e->getMessage()
            ], 500);
        }
    }

    /**
     * Force logout a specific user device from the web panel
     */
    public function forceLogoutFromDeviceWeb(Request $request): void
    {
        $username = $request->getString('username');
        $deviceId = $request->getString('device_id'); // From raw JSON body

        if (empty($username) || empty($deviceId)) {
            Response::json([
                'success' => false,
                'message' => 'نام کاربری و شناسه دستگاه الزامی هستند'
            ], 400);
        }

        try {
            $result = $this->authService->forceLogoutFromDevice($username, $deviceId);
            Response::json($result);
        } catch (Exception $e) {
            Response::json([
                'success' => false,
                'message' => 'خطا در خروج اجباری: ' . $e->getMessage()
            ], 500);
        }
    }

    /**
     * Log out all active users
     */
    public function logoutAllActiveUsers(Request $request): void
    {
        try {
            $result = $this->authService->logoutAllActiveUsers();
            Response::json($result);
        } catch (Exception $e) {
            Response::json([
                'success' => false,
                'message' => 'خطا در خروج همگانی کاربران: ' . $e->getMessage()
            ], 500);
        }
    }

    /**
     * Get history of all sessions
     */
    public function getAllSessions(Request $request): void
    {
        $timeFilter = $request->getString('time_filter', 'all');
        $statusFilter = $request->getString('status_filter', 'all');

        try {
            $sessions = $this->authService->getAllSessions($timeFilter, $statusFilter);
            $processedSessions = [];

            foreach ($sessions as $session) {
                $loginTimeJalali = $session['login_time'] ? \AtkCargo\Helpers\DateHelper::formatJalali('Y/m/d H:i:s', strtotime($session['login_time'])) : '';
                $lastActivityJalali = $session['last_activity'] ? \AtkCargo\Helpers\DateHelper::formatJalali('Y/m/d H:i:s', strtotime($session['last_activity'])) : '';
                $logoutTimeJalali = $session['logout_time'] ? \AtkCargo\Helpers\DateHelper::formatJalali('Y/m/d H:i:s', strtotime($session['logout_time'])) : '';
                $updatedAtJalali = $session['updated_at'] ? \AtkCargo\Helpers\DateHelper::formatJalali('Y/m/d H:i:s', strtotime($session['updated_at'])) : '';

                $idleSeconds = $session['idle_time'] ?? 0;
                $idleMinutes = (int)floor($idleSeconds / 60);

                $processedSessions[] = [
                    'id' => $session['id'],
                    'username' => $session['username'],
                    'userType' => $session['userType'],
                    'device_model' => $session['device_model'],
                    'device_id' => $session['device_id'],
                    'android_version' => $session['android_version'],
                    'app_version' => $session['app_version'],
                    'login_time' => $session['login_time'],
                    'login_time_jalali' => $loginTimeJalali,
                    'last_activity' => $session['last_activity'],
                    'last_activity_jalali' => $lastActivityJalali,
                    'logout_time' => $session['logout_time'],
                    'logout_time_jalali' => $logoutTimeJalali,
                    'ip_address' => $session['ip_address'],
                    'is_active' => (bool)$session['is_active'],
                    'updated_at' => $session['updated_at'],
                    'updated_at_jalali' => $updatedAtJalali,
                    'session_duration' => $session['session_duration'],
                    'idle_time' => $idleSeconds,
                    'idle_minutes' => $idleMinutes
                ];
            }

            Response::json([
                'success' => true,
                'sessions' => $processedSessions
            ]);
        } catch (Exception $e) {
            Response::json([
                'success' => false,
                'message' => 'خطا در دریافت تمام جلسات: ' . $e->getMessage()
            ], 500);
        }
    }
}
