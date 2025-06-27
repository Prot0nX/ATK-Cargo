<?php
// تنظیم منطقه زمانی تهران
date_default_timezone_set('Asia/Tehran');

header('Content-Type: application/json; charset=UTF-8');
error_reporting(E_ALL);
ini_set('display_errors', 0);
require_once __DIR__ . '/config/config.php';
require_once __DIR__ . '/SessionManager.php';

// تنظیم هدرهای امنیتی
header('X-Content-Type-Options: nosniff');
header('X-Frame-Options: DENY');
header('X-XSS-Protection: 1; mode=block');
header('Content-Security-Policy: default-src \'self\'');
header('Strict-Transport-Security: max-age=31536000; includeSubDomains; preload');

function send_json_response($status, $message, $data = null, $http_code = 200) {
    http_response_code($http_code);
    $response = ["success" => $status, "message" => $message];
    if ($data !== null) {
        $response['data'] = $data;
    }
    echo json_encode($response, JSON_UNESCAPED_UNICODE);
    exit();
}

if ($_SERVER["REQUEST_METHOD"] !== "GET") {
    send_json_response(false, "روش درخواست مجاز نیست. لطفاً از روش GET استفاده کنید.", null, 405);
}

$action = isset($_GET['action']) ? trim($_GET['action']) : '';

if (empty($action)) {
    send_json_response(false, "پارامتر action الزامی است.", null, 400);
}

try {
    $sessionManager = new SessionManager();
    
    switch ($action) {
        case 'getOnlineUsers':
            $onlineUsers = $sessionManager->getOnlineUsers();
            
            // فرمت کردن داده‌ها برای نمایش بهتر
            $formattedUsers = array_map(function($user) {
                return [
                    'id' => $user['id'],
                    'username' => $user['username'],
                    'userType' => $user['userType'],
                    'device_model' => $user['device_model'],
                    'device_id' => $user['device_id'],
                    'ip_address' => $user['ip_address'],
                    'login_time' => $user['login_time'],
                    'last_activity' => $user['last_activity'],
                    'online_duration' => $user['online_duration'],
                    'idle_time' => $user['idle_time'],
                    'online_duration_formatted' => formatDuration($user['online_duration']),
                    'idle_time_formatted' => formatDuration($user['idle_time'])
                ];
            }, $onlineUsers);
            
            send_json_response(true, "لیست کاربران آنلاین با موفقیت دریافت شد", $formattedUsers);
            break;
            
        case 'getSessionStats':
            $stats = $sessionManager->getSessionStats();
            
            // فرمت کردن آمار
            $formattedStats = [
                'total_active_sessions' => (int)$stats['total_active_sessions'],
                'unique_users_online' => (int)$stats['unique_users_online'],
                'avg_session_duration' => (int)$stats['avg_session_duration'],
                'avg_session_duration_formatted' => formatDuration((int)$stats['avg_session_duration']),
                'today_logins' => (int)$stats['today_logins'],
                'unique_users_today' => (int)$stats['unique_users_today']
            ];
            
            send_json_response(true, "آمار جلسات با موفقیت دریافت شد", $formattedStats);
            break;
            
        case 'forceLogout':
            $username = isset($_GET['username']) ? trim($_GET['username']) : '';
            $deviceId = isset($_GET['deviceId']) ? trim($_GET['deviceId']) : '';
            
            if (empty($username) || empty($deviceId)) {
                send_json_response(false, "پارامترهای username و deviceId الزامی هستند.", null, 400);
            }
            
            $result = $sessionManager->forceLogoutFromDevice($username, $deviceId);
            
            if ($result['success']) {
                send_json_response(true, $result['message']);
            } else {
                send_json_response(false, $result['message'], null, 404);
            }
            break;
            
        default:
            send_json_response(false, "عملیات نامعتبر است.", null, 400);
    }
    
} catch (Exception $e) {
    error_log("Session Management API Error: " . $e->getMessage());
    send_json_response(false, "خطایی رخ داده است. لطفا بعدا تلاش کنید.", null, 500);
}

/**
 * فرمت کردن مدت زمان به صورت خوانا
 */
function formatDuration($seconds) {
    if ($seconds < 60) {
        return $seconds . " ثانیه";
    } elseif ($seconds < 3600) {
        $minutes = floor($seconds / 60);
        $remainingSeconds = $seconds % 60;
        return $minutes . " دقیقه" . ($remainingSeconds > 0 ? " و " . $remainingSeconds . " ثانیه" : "");
    } else {
        $hours = floor($seconds / 3600);
        $remainingMinutes = floor(($seconds % 3600) / 60);
        return $hours . " ساعت" . ($remainingMinutes > 0 ? " و " . $remainingMinutes . " دقیقه" : "");
    }
}
?>