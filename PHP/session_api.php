<?php
// تنظیم منطقه زمانی تهران
date_default_timezone_set('Asia/Tehran');

header('Content-Type: application/json; charset=UTF-8');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET, POST, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type');

// تنظیم هدرهای امنیتی
header('X-Content-Type-Options: nosniff');
header('X-Frame-Options: DENY');
header('X-XSS-Protection: 1; mode=block');
header('Content-Security-Policy: default-src \'self\'');
header('Strict-Transport-Security: max-age=31536000; includeSubDomains; preload');

require_once __DIR__ . '/SessionManager.php';

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    exit(0);
}

function send_json_response($status, $message, $data = null, $http_code = 200) {
    http_response_code($http_code);
    $response = [
        'success' => $status,
        'message' => $message
    ];
    
    if ($data !== null) {
        $response['data'] = $data;
    }
    
    echo json_encode($response, JSON_UNESCAPED_UNICODE);
    exit();
}

try {
    $sessionManager = new SessionManager();
    
    // دریافت پارامتر action از URL یا POST data
    $action = $_GET['action'] ?? ($_POST['action'] ?? null);
    
    if (!$action) {
        $input = json_decode(file_get_contents('php://input'), true);
        $action = $input['action'] ?? null;
    }
    
    switch ($action) {
        case 'get_online_users':
            // دریافت لیست کاربران آنلاین
            $onlineUsers = $sessionManager->getOnlineUsers();
            
            $formattedUsers = array_map(function($user) {
                return [
                    'username' => $user['username'],
                    'userType' => $user['userType'],
                    'device_model' => $user['device_model'],
                    'login_time' => $user['login_time'],
                    'online_duration' => gmdate('H:i:s', $user['online_duration'])
                ];
            }, $onlineUsers);
            
            send_json_response(true, 'لیست کاربران آنلاین', [
                'count' => count($formattedUsers),
                'users' => $formattedUsers
            ]);
            break;
            
        case 'check_session_status':
            // بررسی وضعیت جلسه کاربر مشخص
            $input = json_decode(file_get_contents('php://input'), true);
            
            if (!isset($input['username'])) {
                send_json_response(false, 'نام کاربری الزامی است', null, 400);
            }
            
            $username = trim($input['username']);
            $deviceId = isset($input['deviceId']) ? trim($input['deviceId']) : null;
            
            $isActive = $sessionManager->isSessionActive($username, $deviceId);
            $sessionInfo = $sessionManager->getActiveSession($username);
            
            if ($isActive && $sessionInfo) {
                send_json_response(true, 'جلسه فعال است', [
                    'is_active' => true,
                    'login_time' => $sessionInfo['login_time'],
                    'device_model' => $sessionInfo['device_model'],
                    'session_duration' => gmdate('H:i:s', $sessionInfo['session_duration'])
                ]);
            } else {
                send_json_response(false, 'جلسه فعال نیست', [
                    'is_active' => false
                ]);
            }
            break;
            
        case 'force_logout':
            // خروج اجباری کاربر (فقط برای ادمین)
            $input = json_decode(file_get_contents('php://input'), true);
            
            if (!isset($input['username']) || !isset($input['admin_username'])) {
                send_json_response(false, 'نام کاربری و نام ادمین الزامی است', null, 400);
            }
            
            // TODO: بررسی دسترسی ادمین
            // در اینجا باید بررسی شود که admin_username واقعاً ادمین است
            
            $username = trim($input['username']);
            $deviceId = isset($input['deviceId']) ? trim($input['deviceId']) : null;
            
            $result = $sessionManager->deactivateSession($username, $deviceId);
            
            if ($result['success']) {
                // ثبت لاگ خروج اجباری
                $logMessage = sprintf(
                    "[%s] FORCE LOGOUT - Target: %s, Admin: %s, IP: %s\n",
                    date('Y-m-d H:i:s'),
                    $username,
                    $input['admin_username'],
                    $_SERVER['REMOTE_ADDR'] ?? 'Unknown'
                );
                
                $logDir = __DIR__ . '/logs';
                if (!is_dir($logDir)) {
                    mkdir($logDir, 0755, true);
                }
                
                file_put_contents($logDir . '/admin_actions.log', $logMessage, FILE_APPEND | LOCK_EX);
            }
            
            send_json_response($result['success'], $result['message']);
            break;
            
        case 'cleanup_sessions':
            // پاکسازی جلسات منقضی شده
            $sessionManager->cleanupExpiredSessions();
            send_json_response(true, 'جلسات منقضی شده پاک شدند');
            break;
            
        case 'get_session_timeout':
            // دریافت مدت زمان انقضای جلسه
            $timeout = $sessionManager->getSessionTimeout();
            send_json_response(true, 'مدت زمان انقضای جلسه', [
                'timeout_seconds' => $timeout,
                'timeout_minutes' => round($timeout / 60, 2),
                'timeout_hours' => round($timeout / 3600, 2)
            ]);
            break;
            
        case 'set_session_timeout':
            // تنظیم مدت زمان انقضای جلسه (فقط برای ادمین)
            $input = json_decode(file_get_contents('php://input'), true);
            
            if (!isset($input['timeout_seconds']) || !isset($input['admin_username'])) {
                send_json_response(false, 'مدت زمان انقضا و نام ادمین الزامی است', null, 400);
            }
            
            // TODO: بررسی دسترسی ادمین
            
            $timeoutSeconds = intval($input['timeout_seconds']);
            
            if ($timeoutSeconds < 300) { // حداقل 5 دقیقه
                send_json_response(false, 'حداقل مدت زمان انقضا 5 دقیقه است', null, 400);
            }
            
            $sessionManager->setSessionTimeout($timeoutSeconds);
            
            // ثبت لاگ تغییر تنظیمات
            $logMessage = sprintf(
                "[%s] SESSION TIMEOUT CHANGED - New timeout: %d seconds, Admin: %s, IP: %s\n",
                date('Y-m-d H:i:s'),
                $timeoutSeconds,
                $input['admin_username'],
                $_SERVER['REMOTE_ADDR'] ?? 'Unknown'
            );
            
            $logDir = __DIR__ . '/logs';
            if (!is_dir($logDir)) {
                mkdir($logDir, 0755, true);
            }
            
            file_put_contents($logDir . '/admin_actions.log', $logMessage, FILE_APPEND | LOCK_EX);
            
            send_json_response(true, 'مدت زمان انقضای جلسه تغییر کرد', [
                'new_timeout_seconds' => $timeoutSeconds
            ]);
            break;
            
        default:
            send_json_response(false, 'عملیات نامعتبر', null, 400);
    }
    
} catch (Exception $e) {
    error_log("خطا در session_api: " . $e->getMessage());
    send_json_response(false, 'خطای سرور', null, 500);
}
?>