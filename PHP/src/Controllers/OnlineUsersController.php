<?php
// PHP/src/Controllers/OnlineUsersController.php

declare(strict_types=1);

namespace App\Controllers;

use Exception;
use SessionManager;
use App\Core\Request;
use App\Core\Response;

class OnlineUsersController {
    private Request $request;

    public function __construct() {
        $this->request = new Request();
    }

    public function handleRequest(): void {
        date_default_timezone_set('Asia/Tehran');

        header('Content-Type: application/json; charset=utf-8');
        header('Access-Control-Allow-Methods: GET, POST, OPTIONS');
        header('Access-Control-Allow-Headers: Content-Type');

        if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
            http_response_code(200);
            exit;
        }

 // احراز هویت از طریق نشست سراسری PermissionManager.php برای جلوگیری از افشای اطلاعات کاربران
        if (session_status() === PHP_SESSION_NONE) {
            session_start();
        }
        if (!isset($_SESSION['perm_manager_auth']) || $_SESSION['perm_manager_auth'] !== true) {
            http_response_code(401);
            echo json_encode([
                'success' => false,
                'message' => 'دسترسی غیرمجاز: برای مشاهده‌ی این پنل ابتدا از طریق PermissionManager.php وارد شوید.'
            ], JSON_UNESCAPED_UNICODE);
            exit;
        }

        try {
            $sessionManager = new SessionManager();
 // استفاده از Request::get برای خواندن اکشن، چون Request::post وجود ندارد
            $action = (string)($this->request->get('action', 'get_online_users'));

            switch ($action) {
                case 'get_online_users':
                    $onlineUsers = $sessionManager->getOnlineUsers();
                    $processedUsers = [];

                    foreach ($onlineUsers as $user) {
                        $loginTimeJalali = '';
                        $lastActivityJalali = '';

                        if (!empty($user['login_time'])) {
                            $loginTimestamp = strtotime($user['login_time']);
                            $loginTimeJalali = jdate('Y/m/d H:i:s', $loginTimestamp);
                        }

                        if (!empty($user['last_activity'])) {
                            $lastActivityTimestamp = strtotime($user['last_activity']);
                            $lastActivityJalali = jdate('Y/m/d H:i:s', $lastActivityTimestamp);
                        }

                        $idleMinutes = (int)floor($user['idle_time'] / 60);
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
                            'idle_time' => $user['idle_time'],
                            'idle_minutes' => $idleMinutes,
                            'status' => $status,
                            'status_text' => $statusText
                        ];
                    }

                    Response::json([
                        'success' => true,
                        'users' => $processedUsers,
                        'total_count' => count($processedUsers),
                        'last_update' => jdate('Y/m/d H:i:s')
                    ]);
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

                    $input = file_get_contents('php://input');
                    $data = json_decode((string)$input, true);

                    if ($data === null) {
                        $data = $_POST;
                    }

                    $username = $data['username'] ?? '';
                    $deviceId = $data['device_id'] ?? '';

                    if (empty($username)) {
                        throw new Exception('نام کاربری الزامی است');
                    }

                    if (!empty($deviceId)) {
                        $result = $sessionManager->forceLogoutFromDevice($username, $deviceId);
                    } else {
                        $result = $sessionManager->deactivateSession($username);
                    }

                    Response::json($result);
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
            Response::json([
                'success' => false,
                'message' => $e->getMessage()
            ], 400);
        }
    }

}
