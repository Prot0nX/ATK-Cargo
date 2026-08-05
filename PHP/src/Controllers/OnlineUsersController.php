<?php
// PHP/src/Controllers/OnlineUsersController.php

declare(strict_types=1);

namespace App\Controllers;

use Exception;
use SessionManager;
use App\Core\Logger;
use App\Core\Request;

class OnlineUsersController {
    private Logger $logger;
    private Request $request;

    public function __construct() {
        $this->logger = Logger::getInstance();
        $this->request = new Request();
    }

    public function handleRequest(): void {
        date_default_timezone_set('Asia/Tehran');

        header('Content-Type: application/json; charset=utf-8');
        header('Access-Control-Allow-Origin: *');
        header('Access-Control-Allow-Methods: GET, POST, OPTIONS');
        header('Access-Control-Allow-Headers: Content-Type');

        if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
            http_response_code(200);
            exit;
        }

        try {
            $sessionManager = new SessionManager();
            $action = (string)($this->request->get('action') ?? $this->request->post('action') ?? 'get_online_users');

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

                    $this->sendJsonResponse([
                        'success' => true,
                        'users' => $processedUsers,
                        'total_count' => count($processedUsers),
                        'last_update' => jdate('Y/m/d H:i:s')
                    ]);
                    break;

                case 'get_session_stats':
                    $stats = $sessionManager->getSessionStats();
                    $this->sendJsonResponse([
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

                    $this->sendJsonResponse($result);
                    break;

                case 'heartbeat':
                    $username = (string)($this->request->post('username') ?? $this->request->get('username') ?? '');
                    $deviceId = (string)($this->request->post('device_id') ?? $this->request->get('device_id') ?? '');

                    if (empty($username) || empty($deviceId)) {
                        throw new Exception('اطلاعات کاربر و دستگاه الزامی است');
                    }

                    $sessionManager->updateLastActivity($username, $deviceId);
                    $this->sendJsonResponse([
                        'success' => true,
                        'message' => 'Heartbeat updated'
                    ]);
                    break;

                case 'cleanup_inactive':
                    $cleanedCount = $sessionManager->cleanupExpiredSessions();
                    $this->sendJsonResponse([
                        'success' => true,
                        'message' => "تعداد $cleanedCount نشست منقضی شده پاکسازی شد",
                        'cleaned_count' => $cleanedCount
                    ]);
                    break;

                default:
                    throw new Exception('عملیات نامعتبر است');
            }
        } catch (Exception $e) {
            $this->sendJsonResponse([
                'success' => false,
                'message' => $e->getMessage()
            ], 400);
        }
    }

    private function sendJsonResponse(array $data, int $statusCode = 200): void {
        http_response_code($statusCode);
        echo json_encode($data, JSON_UNESCAPED_UNICODE);
        exit;
    }
}
