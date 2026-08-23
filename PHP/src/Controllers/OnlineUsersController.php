<?php
// PHP/src/Controllers/OnlineUsersController.php

declare(strict_types=1);

namespace App\Controllers;

use Exception;
use SessionManager;
use App\Core\Request;
use App\Core\Response;
use App\Repositories\SessionRepository;

class OnlineUsersController {
    private Request $request;

    public function __construct() {
        $this->request = new Request();
    }

    // احراز هویت پیش از این متد در online_users_api.php (ou_require_auth_json، نشست اختصاصی ATKOU) انجام می‌شود.
    public function handleRequest(): void {
        date_default_timezone_set('Asia/Tehran');

        if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
            http_response_code(200);
            exit;
        }

        try {
            $sessionManager = new SessionManager();
 // استفاده از Request::get برای خواندن اکشن، چون Request::post وجود ندارد
            $action = (string)($this->request->get('action', 'get_all_sessions'));

            switch ($action) {
                case 'get_online_users':
                    Response::json($this->buildSessionsResponse($sessionManager->getOnlineUsers()));
                    break;

 // بارگذاری اصلی صفحه: همه‌ی جلسات ۹۰ روز اخیر (آنلاین + آفلاین)، برای گروه‌بندی روزانه در گرید.
                case 'get_all_sessions':
                    $repo = new SessionRepository();
                    $allSessions = $repo->getAllSessions(90);
                    $activeCount = count(array_filter($allSessions, static fn(array $s): bool => (int)($s['is_active'] ?? 0) === 1));
                    $response = $this->buildSessionsResponse($allSessions);
                    $response['active_count'] = $activeCount;
                    Response::json($response);
                    break;

                case 'get_session_stats':
                    $stats = $sessionManager->getSessionStats();
                    Response::json([
                        'success' => true,
                        'stats' => $stats
                    ]);
                    break;

                case 'force_logout':
                    if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
                        throw new Exception('روش درخواست نامعتبر است');
                    }

                    $data = $this->readJsonBody();
                    $username = (string)($data['username'] ?? '');
                    $deviceId = (string)($data['device_id'] ?? '');

                    if ($username === '') {
                        throw new Exception('نام کاربری الزامی است');
                    }

                    $result = $deviceId !== ''
                        ? $sessionManager->forceLogoutFromDevice($username, $deviceId)
                        : $sessionManager->deactivateSession($username);

                    Response::json($result);
                    break;

 // خروج دسته‌جمعی از منوی مدیریت خروج: all=همه، except-admin=همه به‌جز مدیران، operators=فقط اپراتورها.
                case 'logout_all_users':
                    if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
                        throw new Exception('روش درخواست نامعتبر است');
                    }

                    $data = $this->readJsonBody();
                    $type = (string)($data['type'] ?? 'all');
                    $repo = new SessionRepository();

                    if ($type === 'all') {
                        $count = $repo->deactivateAllActiveSessions();
                    } elseif ($type === 'except-admin') {
                        $ids = $repo->getActiveSessionIdsByUserType(['admin'], true);
                        $count = $repo->deactivateSessionsByIds($ids);
                    } elseif ($type === 'operators') {
                        $ids = $repo->getActiveSessionIdsByUserType(['operator']);
                        $count = $repo->deactivateSessionsByIds($ids);
                    } else {
                        throw new Exception('نوع خروج دسته‌جمعی نامعتبر است');
                    }

                    Response::json([
                        'success' => true,
                        'message' => "تعداد $count نشست خارج شد",
                        'count' => $count
                    ]);
                    break;

                case 'heartbeat':
                    $username = (string)$this->request->get('username', '');
                    $deviceId = (string)$this->request->get('device_id', '');

                    if (empty($username) || empty($deviceId)) {
                        throw new Exception('اطلاعات کاربر و دستگاه الزامی است');
                    }

                    $sessionManager->updateLastActivity($username, $deviceId);
                    Response::json([
                        'success' => true,
                        'message' => 'Heartbeat updated'
                    ]);
                    break;

                case 'cleanup_inactive':
                    if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
                        throw new Exception('روش درخواست نامعتبر است');
                    }

                    $cleanedCount = $sessionManager->cleanupExpiredSessions();
                    Response::json([
                        'success' => true,
                        'message' => "تعداد $cleanedCount نشست منقضی شده پاکسازی شد",
                        'cleaned_count' => $cleanedCount
                    ]);
                    break;

                default:
                    throw new Exception('عملیات نامعتبر است');
            }
        } catch (Exception $e) {
            Response::error($e->getMessage());
        }
    }

 // پردازش مشترک خروجی خام دیتابیس (چه get_online_users چه get_all_sessions) به شکل قابل‌استفاده‌ی فرانت‌اند: تبدیل تاریخ‌ها به جلالی + وضعیت بیکار.
    private function buildSessionsResponse(array $rawSessions): array {
        $processed = [];

        foreach ($rawSessions as $user) {
            $loginTimeJalali = '';
            $lastActivityJalali = '';
            $logoutTimeJalali = '';

            if (!empty($user['login_time'])) {
                $loginTimeJalali = jdate('Y/m/d H:i:s', strtotime($user['login_time']));
            }
            if (!empty($user['last_activity'])) {
                $lastActivityJalali = jdate('Y/m/d H:i:s', strtotime($user['last_activity']));
            }
            if (!empty($user['logout_time'])) {
                $logoutTimeJalali = jdate('Y/m/d H:i:s', strtotime($user['logout_time']));
            }

            $idleMinutes = (int)floor(((int)($user['idle_time'] ?? 0)) / 60);
            $status = 'online';
            $statusText = 'آنلاین';
            if (!empty($user['logout_time'])) {
                $status = 'offline';
                $statusText = 'آفلاین';
            } elseif ($idleMinutes > 5) {
                $status = 'idle';
                $statusText = "بیکار ($idleMinutes دقیقه)";
            }

            $processed[] = [
                'id' => $user['id'],
                'username' => $user['username'],
                'userType' => $user['userType'],
                'device_model' => $user['device_model'],
                'device_id' => $user['device_id'],
                'app_version' => $user['app_version'] ?? null,
                'ip_address' => $user['ip_address'],
                'login_time' => $user['login_time'],
                'login_time_jalali' => $loginTimeJalali,
                'last_activity' => $user['last_activity'],
                'last_activity_jalali' => $lastActivityJalali,
                'logout_time' => $user['logout_time'] ?? null,
                'logout_time_jalali' => $logoutTimeJalali !== '' ? $logoutTimeJalali : null,
                'online_duration' => $user['online_duration'],
                'idle_time' => $user['idle_time'],
                'idle_minutes' => $idleMinutes,
                'status' => $status,
                'status_text' => $statusText
            ];
        }

        return [
            'success' => true,
            'sessions' => $processed,
            'users' => $processed,
            'total_count' => count($processed),
            'last_update' => jdate('Y/m/d H:i:s')
        ];
    }

 // خواندن بدنه‌ی JSON درخواست با fallback به $_POST برای سازگاری با فرم‌های معمولی
    private function readJsonBody(): array {
        $input = file_get_contents('php://input');
        $data = json_decode((string)$input, true);
        return is_array($data) ? $data : $_POST;
    }
}
