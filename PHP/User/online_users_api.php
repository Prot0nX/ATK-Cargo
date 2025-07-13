<?php
// تنظیم منطقه زمانی تهران
date_default_timezone_set('Asia/Tehran');

require_once __DIR__ . '/SessionManager.php';
require_once __DIR__ . '/../jdf.php';

// تنظیم هدرهای CORS و JSON
header('Content-Type: application/json; charset=utf-8');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET, POST, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type');

// پاسخ به درخواست OPTIONS
if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit();
}

try {
    $sessionManager = new SessionManager();
    $action = $_GET['action'] ?? $_POST['action'] ?? 'get_online_users';
    
    switch ($action) {
        case 'get_online_users':
            $onlineUsers = $sessionManager->getOnlineUsers();
            $processedUsers = [];
            
            foreach ($onlineUsers as $user) {
                // تبدیل تاریخ‌ها به شمسی
                $loginTimeJalali = '';
                $lastActivityJalali = '';
                
                if ($user['login_time']) {
                    $loginTimestamp = strtotime($user['login_time']);
                    $loginTimeJalali = jdate('Y/m/d H:i:s', $loginTimestamp);
                }
                
                if ($user['last_activity']) {
                    $lastActivityTimestamp = strtotime($user['last_activity']);
                    $lastActivityJalali = jdate('Y/m/d H:i:s', $lastActivityTimestamp);
                }
                
                // محاسبه وضعیت کاربر
                $idleMinutes = floor($user['idle_time'] / 60);
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
            
            echo json_encode([
                'success' => true,
                'users' => $processedUsers,
                'total_count' => count($processedUsers),
                'last_update' => jdate('Y/m/d H:i:s')
            ], JSON_UNESCAPED_UNICODE);
            break;
            
        case 'get_session_stats':
            $stats = $sessionManager->getSessionStats();
            
            echo json_encode([
                'success' => true,
                'stats' => $stats
            ], JSON_UNESCAPED_UNICODE);
            break;
            
        case 'force_logout':
            if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
                throw new Exception('روش درخواست نامعتبر است');
            }
            
            // دریافت داده‌های JSON
            $input = file_get_contents('php://input');
            $data = json_decode($input, true);
            
            // اگر JSON decode نشد، سعی کن از POST استفاده کنی
            if ($data === null) {
                $data = $_POST;
            }
            
            // لاگ کردن تمام داده‌های دریافتی برای دیباگ
            error_log("Raw input: " . $input);
            error_log("Decoded data: " . print_r($data, true));
            error_log("Content-Type: " . ($_SERVER['CONTENT_TYPE'] ?? 'not set'));
            error_log("JSON decode error: " . json_last_error_msg());
            
            $username = $data['username'] ?? '';
            $deviceId = $data['device_id'] ?? '';
            
            // اضافه کردن لاگ برای دیباگ
            error_log("Force logout request - Username: $username, DeviceId: $deviceId");
            
            if (empty($username) || empty($deviceId)) {
                error_log("Missing parameters - Username empty: " . (empty($username) ? 'yes' : 'no') . ", DeviceId empty: " . (empty($deviceId) ? 'yes' : 'no'));
                throw new Exception('نام کاربری و شناسه دستگاه الزامی است');
            }
            
            $result = $sessionManager->forceLogoutFromDevice($username, $deviceId);
            
            echo json_encode($result, JSON_UNESCAPED_UNICODE);
            break;
            
        case 'filter_by_time':
            $filter = $_GET['filter'] ?? 'all';
            $onlineUsers = $sessionManager->getOnlineUsers();
            $filteredUsers = [];
            
            $now = time();
            
            foreach ($onlineUsers as $user) {
                $loginTime = strtotime($user['login_time']);
                $include = false;
                
                switch ($filter) {
                    case 'today':
                        $include = date('Y-m-d', $loginTime) === date('Y-m-d', $now);
                        break;
                    case '24h':
                        $include = ($now - $loginTime) <= 86400; // 24 ساعت
                        break;
                    case 'all':
                    default:
                        $include = true;
                        break;
                }
                
                if ($include) {
                    // تبدیل تاریخ‌ها به شمسی
                    $loginTimeJalali = jdate('Y/m/d H:i:s', $loginTime);
                    $lastActivityJalali = '';
                    
                    if ($user['last_activity']) {
                        $lastActivityTimestamp = strtotime($user['last_activity']);
                        $lastActivityJalali = jdate('Y/m/d H:i:s', $lastActivityTimestamp);
                    }
                    
                    // محاسبه وضعیت کاربر
                    $status = 'online';
                    $statusText = 'آنلاین';
                    
                    $filteredUsers[] = [
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
                        'status' => $status,
                        'status_text' => $statusText
                    ];
                }
            }
            
            echo json_encode([
                'success' => true,
                'users' => $filteredUsers,
                'total_count' => count($filteredUsers),
                'filter' => $filter,
                'last_update' => jdate('Y/m/d H:i:s')
            ], JSON_UNESCAPED_UNICODE);
            break;
            
        case 'get_all_sessions':
            $timeFilter = $_GET['time_filter'] ?? 'all';
            $statusFilter = $_GET['status_filter'] ?? 'all';
            
            $allSessions = $sessionManager->getAllSessions($timeFilter, $statusFilter);
            $processedSessions = [];
            
            foreach ($allSessions as $session) {
                // تبدیل تاریخ‌ها به شمسی
                $loginTimeJalali = '';
                $lastActivityJalali = '';
                $logoutTimeJalali = '';
                
                if ($session['login_time']) {
                    $loginTimestamp = strtotime($session['login_time']);
                    $loginTimeJalali = jdate('Y/m/d H:i:s', $loginTimestamp);
                }
                
                if ($session['last_activity']) {
                    $lastActivityTimestamp = strtotime($session['last_activity']);
                    $lastActivityJalali = jdate('Y/m/d H:i:s', $lastActivityTimestamp);
                }
                
                if ($session['logout_time']) {
                    $logoutTimestamp = strtotime($session['logout_time']);
                    $logoutTimeJalali = jdate('Y/m/d H:i:s', $logoutTimestamp);
                }
                
                // تعیین وضعیت جلسه
                $status = $session['is_active'] ? 'active' : 'inactive';
                $statusText = $session['is_active'] ? 'فعال' : 'خارج شده';
                
                // تعیین وضعیت ساده: آنلاین یا آفلاین
                 if ($session['is_active']) {
                     $statusText = 'آنلاین';
                 }
                
                $processedSessions[] = [
                    'id' => $session['id'],
                    'username' => $session['username'],
                    'userType' => $session['userType'],
                    'device_model' => $session['device_model'],
                    'device_id' => $session['device_id'],
                    'ip_address' => $session['ip_address'],
                    'is_active' => $session['is_active'],
                    'login_time' => $session['login_time'],
                    'login_time_jalali' => $loginTimeJalali,
                    'last_activity' => $session['last_activity'],
                    'last_activity_jalali' => $lastActivityJalali,
                    'logout_time' => $session['logout_time'],
                    'logout_time_jalali' => $logoutTimeJalali,
                    'session_duration' => $session['session_duration'],
                    'status' => $status,
                    'status_text' => $statusText
                ];
            }
            
            // محاسبه آمار
            $activeCount = count(array_filter($processedSessions, function($s) { return $s['is_active']; }));
            $inactiveCount = count(array_filter($processedSessions, function($s) { return !$s['is_active']; }));
            
            echo json_encode([
                'success' => true,
                'sessions' => $processedSessions,
                'total_count' => count($processedSessions),
                'active_count' => $activeCount,
                'inactive_count' => $inactiveCount,
                'time_filter' => $timeFilter,
                'status_filter' => $statusFilter,
                'last_update' => jdate('Y/m/d H:i:s')
            ], JSON_UNESCAPED_UNICODE);
            break;
            
        default:
            throw new Exception('عملیات نامعتبر است');
    }
    
} catch (Exception $e) {
    // لاگ کردن جزئیات خطا برای دیباگ
    error_log("API Error in online_users_api.php: " . $e->getMessage());
    error_log("Stack trace: " . $e->getTraceAsString());
    
    http_response_code(400);
    echo json_encode([
        'success' => false,
        'error' => $e->getMessage(),
        'debug_info' => [
            'file' => $e->getFile(),
            'line' => $e->getLine(),
            'action' => $_GET['action'] ?? $_POST['action'] ?? 'unknown'
        ]
    ], JSON_UNESCAPED_UNICODE);
}
?>