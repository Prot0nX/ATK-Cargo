<?php
// تنظیم منطقه زمانی تهران
date_default_timezone_set('Asia/Tehran');

require_once __DIR__ . '/config/config.php';

/**
 * کلاس مدیریت جلسات کاربری
 * این کلاس مسئول مدیریت اصولی و بهینه جلسات کاربران است
 */
class SessionManager {
    private $pdo;
    private $sessionTimeout = 86400; // 24 ساعت
    
    public function __construct() {
        try {
            $this->pdo = new PDO(
                "mysql:host=" . DB_HOST . ";dbname=" . DB_NAME . ";charset=utf8mb4", 
                DB_USER, 
                DB_PASSWORD,
                [
                    PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION,
                    PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
                    PDO::ATTR_EMULATE_PREPARES => false
                ]
            );
        } catch (PDOException $e) {
            error_log("خطا در اتصال به دیتابیس: " . $e->getMessage());
            throw new Exception("خطا در اتصال به دیتابیس");
        }
    }
    
    /**
     * ایجاد جلسه جدید برای کاربر
     * @param string $username نام کاربری
     * @param string $deviceId شناسه دستگاه
     * @param string $deviceModel مدل دستگاه
     * @param string $androidVersion نسخه اندروید
     * @param string $ipAddress آدرس آی‌پی
     * @param string $userType نوع کاربر (admin, operator, verifier)
     * @return array نتیجه عملیات
     */
    public function createSession($username, $deviceId, $deviceModel, $androidVersion, $ipAddress, $userType = null) {
        try {
            // بررسی جلسه فعال موجود برای همین دستگاه
            $stmt = $this->pdo->prepare("
                SELECT id, session_token 
                FROM user_sessions 
                WHERE username = ? AND device_id = ? AND is_active = 1
                LIMIT 1
            ");
            
            $stmt->execute([$username, $deviceId]);
            $existingSession = $stmt->fetch();
            
            if ($existingSession) {
                // اگر همان دستگاه است، جلسه را به‌روزرسانی می‌کنیم
                $updateResult = $this->updateSessionActivity($username, $deviceId);
                if ($updateResult['success']) {
                    return [
                        'success' => true,
                        'message' => 'جلسه موجود به‌روزرسانی شد',
                        'session_id' => $existingSession['id'],
                        'session_token' => $existingSession['session_token']
                    ];
                }
            }
            
            // دریافت نوع کاربری از دیتابیس اگر ارسال نشده باشد
            if ($userType === null) {
                $stmt = $this->pdo->prepare("SELECT userType FROM Users WHERE username = ? LIMIT 1");
                $stmt->execute([$username]);
                $user = $stmt->fetch();
                
                if ($user) {
                    $userType = $user['userType'];
                } else {
                    throw new Exception("کاربر در سیستم یافت نشد");
                }
            }
            
            // غیرفعال کردن تمام جلسه‌های فعال قبلی کاربر
            $stmt = $this->pdo->prepare("
                UPDATE user_sessions 
                SET is_active = 0, logout_time = NOW() 
                WHERE username = ? AND is_active = 1
            ");
            $stmt->execute([$username]);
            
            // تولید توکن جلسه منحصر به فرد
            $sessionToken = bin2hex(random_bytes(32));
            
            // ایجاد جلسه جدید با ثبت نوع کاربر و توکن جلسه
            $stmt = $this->pdo->prepare("
                INSERT INTO user_sessions 
                (username, device_id, device_model, android_version, login_time, last_activity, is_active, ip_address, userType, session_token) 
                VALUES (?, ?, ?, ?, NOW(), NOW(), 1, ?, ?, ?)
            ");
            
            $result = $stmt->execute([$username, $deviceId, $deviceModel, $androidVersion, $ipAddress, $userType, $sessionToken]);
            
            if ($result) {
                $this->logActivity($username, 'LOGIN', $deviceId, $ipAddress, $userType);
                return [
                    'success' => true,
                    'message' => 'جلسه با موفقیت ایجاد شد',
                    'session_id' => $this->pdo->lastInsertId(),
                    'session_token' => $sessionToken
                ];
            }
            
            throw new Exception("خطا در ایجاد جلسه");
            
        } catch (Exception $e) {
            error_log("خطا در ایجاد جلسه: " . $e->getMessage());
            throw $e;
        }
    }
    
    /**
     * دریافت جلسه فعال کاربر
     * @param string $username نام کاربری
     * @return array|null اطلاعات جلسه فعال یا null
     */
    public function getActiveSession($username) {
        try {
            // حذف فراخوانی cleanupExpiredSessions برای جلوگیری از انقضای خودکار جلسات
            
            $stmt = $this->pdo->prepare("
                SELECT id, username, device_id, device_model, android_version, 
                       login_time, last_activity, session_token, userType,
                       TIMESTAMPDIFF(SECOND, COALESCE(last_activity, login_time), NOW()) as session_duration
                FROM user_sessions 
                WHERE username = ? AND is_active = 1
                ORDER BY COALESCE(last_activity, login_time) DESC
                LIMIT 1
            ");
            
            $stmt->execute([$username]);
            $session = $stmt->fetch();
            
            if ($session) {
                // حذف بررسی انقضای جلسه - جلسه‌ها فقط با خروج کاربر منقضی می‌شوند
                return $session;
            }
            
            return null;
            
        } catch (Exception $e) {
            error_log("خطا در دریافت جلسه فعال: " . $e->getMessage());
            return null;
        }
    }
    
    /**
     * بررسی وضعیت فعال بودن جلسه کاربر
     */
    public function isSessionActive($username, $deviceId = null) {
        try {
            // حذف فراخوانی cleanupExpiredSessions برای جلوگیری از انقضای خودکار جلسات
            
            $query = "
                SELECT id, device_id, login_time, last_activity, session_token, userType
                FROM user_sessions 
                WHERE username = ? AND is_active = 1
            ";
            
            $params = [$username];
            
            if ($deviceId) {
                $query .= " AND device_id = ?";
                $params[] = $deviceId;
            }
            
            $stmt = $this->pdo->prepare($query);
            $stmt->execute($params);
            $session = $stmt->fetch();
            
            if ($session) {
                // جلسه برای همه کاربران همیشه فعال است مگر اینکه خودشان خارج شوند
                // به‌روزرسانی last_activity هنگام بررسی جلسه
                $this->updateLastActivity($username, $session['device_id']);
                return true;
            }
            
            return false;
            
        } catch (Exception $e) {
            error_log("خطا در بررسی وضعیت جلسه: " . $e->getMessage());
            return false;
        }
    }
    
    /**
     * غیرفعال کردن جلسه کاربر (خروج)
     */
    public function deactivateSession($username, $deviceId = null) {
        try {
            // ابتدا بررسی می‌کنیم که آیا کاربر وجود دارد یا نه
            $userCheck = $this->pdo->prepare("SELECT username FROM Users WHERE username = ? LIMIT 1");
            $userCheck->execute([$username]);
            
            if (!$userCheck->fetch()) {
                return [
                    'success' => false,
                    'message' => 'کاربر در سیستم وجود ندارد',
                    'http_code' => 404
                ];
            }
            
            // شروع تراکنش برای جلوگیری از مشکلات همزمانی
            $this->pdo->beginTransaction();
            
            try {
                // بررسی وجود جلسه فعال
                $checkQuery = "SELECT id FROM user_sessions WHERE username = ? AND is_active = 1";
                $checkParams = [$username];
                
                if ($deviceId) {
                    $checkQuery .= " AND device_id = ?";
                    $checkParams[] = $deviceId;
                }
                
                $checkStmt = $this->pdo->prepare($checkQuery);
                $checkStmt->execute($checkParams);
                
                if ($checkStmt->rowCount() === 0) {
                    $this->pdo->rollback();
                    return [
                        'success' => false,
                        'message' => 'جلسه فعالی برای غیرفعال کردن یافت نشد',
                        'http_code' => 404
                    ];
                }
                
                // به‌روزرسانی جلسه‌های فعال به غیرفعال با استفاده از ID
                $sessionIds = $checkStmt->fetchAll(PDO::FETCH_COLUMN);
                $placeholders = str_repeat('?,', count($sessionIds) - 1) . '?';
                
                $updateQuery = "UPDATE user_sessions SET is_active = 0, logout_time = NOW() WHERE id IN ($placeholders)";
                $updateStmt = $this->pdo->prepare($updateQuery);
                $updateResult = $updateStmt->execute($sessionIds);
                
                if ($updateResult && $updateStmt->rowCount() > 0) {
                    $this->pdo->commit();
                    $this->logActivity($username, 'LOGOUT', $deviceId);
                    return [
                        'success' => true,
                        'message' => 'خروج با موفقیت انجام شد',
                        'affected_sessions' => $updateStmt->rowCount(),
                        'http_code' => 200
                    ];
                }
                
                $this->pdo->rollback();
                return [
                    'success' => false,
                    'message' => 'خطا در غیرفعال کردن جلسه',
                    'http_code' => 500
                ];
                
            } catch (Exception $innerE) {
                $this->pdo->rollback();
                throw $innerE;
            }
            
        } catch (Exception $e) {
            error_log("خطا در غیرفعال کردن جلسه: " . $e->getMessage());
            return [
                'success' => false,
                'message' => 'خطای داخلی سرور در هنگام خروج',
                'error' => $e->getMessage(),
                'http_code' => 500
            ];
        }
    }
    

    
    /**
     * به‌روزرسانی فعالیت جلسه (برای جلوگیری از انقضا)
     */
    public function updateSessionActivity($username, $deviceId) {
        return $this->updateLastActivity($username, $deviceId);
    }
    
    /**
     * به‌روزرسانی آخرین فعالیت کاربر
     */
    public function updateLastActivity($username, $deviceId) {
        try {
            $stmt = $this->pdo->prepare("
                UPDATE user_sessions 
                SET last_activity = NOW() 
                WHERE username = ? AND device_id = ? AND is_active = 1
            ");
            
            $result = $stmt->execute([$username, $deviceId]);
            
            return [
                'success' => $result && $stmt->rowCount() > 0,
                'message' => $result ? 'آخرین فعالیت به‌روزرسانی شد' : 'جلسه فعالی یافت نشد'
            ];
            
        } catch (Exception $e) {
            error_log("خطا در به‌روزرسانی آخرین فعالیت: " . $e->getMessage());
            throw $e;
        }
    }
    
    /**
     * پاکسازی جلسات منقضی شده - غیرفعال شده برای جلوگیری از انقضای خودکار جلسات
     */
    public function cleanupExpiredSessions() {
        // این تابع دیگر جلسات را منقضی نمی‌کند
        // جلسات فقط با خروج کاربر منقضی می‌شوند
        return 0;
    }
    
    /**
     * دریافت لیست کاربران آنلاین
     */
    public function getOnlineUsers() {
        try {
            $this->cleanupExpiredSessions();
            
            $stmt = $this->pdo->prepare("
                SELECT us.id, us.username, u.userType, us.device_model, us.device_id, 
                       us.login_time, us.last_activity, us.ip_address,
                       TIMESTAMPDIFF(SECOND, us.login_time, NOW()) as online_duration,
                       TIMESTAMPDIFF(SECOND, COALESCE(us.last_activity, us.login_time), NOW()) as idle_time
                FROM user_sessions us
                JOIN Users u ON us.username = u.username
                WHERE us.is_active = 1
                ORDER BY us.last_activity DESC, us.login_time DESC
            ");
            
            $stmt->execute();
            return $stmt->fetchAll();
            
        } catch (Exception $e) {
            error_log("خطا در دریافت کاربران آنلاین: " . $e->getMessage());
            return [];
        }
    }
    
    /**
     * خروج اجباری کاربر از دستگاه خاص
     */
    public function forceLogoutFromDevice($username, $deviceId) {
        try {
            // شروع تراکنش برای جلوگیری از race condition
            $this->pdo->beginTransaction();
            
            // ابتدا تمام جلسه‌های فعال این کاربر و دستگاه را پیدا کنیم
            $checkStmt = $this->pdo->prepare("
                SELECT id, is_active 
                FROM user_sessions 
                WHERE username = ? AND device_id = ? AND is_active = 1
                FOR UPDATE
            ");
            $checkStmt->execute([$username, $deviceId]);
            $activeSessions = $checkStmt->fetchAll(PDO::FETCH_ASSOC);
            
            if (empty($activeSessions)) {
                $this->pdo->rollback();
                return [
                    'success' => false,
                    'message' => 'جلسه فعالی برای خروج یافت نشد'
                ];
            }
            
            // به‌روزرسانی جلسه‌های فعال به غیرفعال با استفاده از ID
            $sessionIds = array_column($activeSessions, 'id');
            $placeholders = str_repeat('?,', count($sessionIds) - 1) . '?';
            
            $updateStmt = $this->pdo->prepare("
                UPDATE user_sessions 
                SET is_active = 0, logout_time = NOW(), last_activity = NOW()
                WHERE id IN ($placeholders)
            ");
            
            $result = $updateStmt->execute($sessionIds);
            
            if ($result && $updateStmt->rowCount() > 0) {
                $this->pdo->commit();
                $this->logActivity($username, 'FORCE_LOGOUT', $deviceId);
                
                return [
                    'success' => true,
                    'message' => 'کاربر از دستگاه قبلی خارج شد'
                ];
            }
            
            $this->pdo->rollback();
            return [
                'success' => false,
                'message' => 'خطا در خروج اجباری'
            ];
            
        } catch (Exception $e) {
            if ($this->pdo->inTransaction()) {
                $this->pdo->rollback();
            }
            
            error_log("خطا در خروج اجباری: " . $e->getMessage());
            return [
                'success' => false,
                'error' => $e->getMessage(),
                'debug_info' => [
                    'file' => __FILE__,
                    'line' => __LINE__,
                    'action' => 'force_logout'
                ]
            ];
        }
    }
    
    /**
     * دریافت آمار جلسات
     */
    public function getSessionStats() {
        try {
            $this->cleanupExpiredSessions();
            
            $stmt = $this->pdo->prepare("
                SELECT 
                    COUNT(*) as total_active_sessions,
                    COUNT(DISTINCT username) as unique_users_online,
                    AVG(TIMESTAMPDIFF(SECOND, login_time, NOW())) as avg_session_duration
                FROM user_sessions 
                WHERE is_active = 1
            ");
            
            $stmt->execute();
            $stats = $stmt->fetch();
            
            // آمار امروز
            $stmt = $this->pdo->prepare("
                SELECT 
                    COUNT(*) as today_logins,
                    COUNT(DISTINCT username) as unique_users_today
                FROM user_sessions 
                WHERE DATE(login_time) = CURDATE()
            ");
            
            $stmt->execute();
            $todayStats = $stmt->fetch();
            
            return array_merge($stats, $todayStats);
            
        } catch (Exception $e) {
            error_log("خطا در دریافت آمار جلسات: " . $e->getMessage());
            return [];
        }
    }
    
    /**
     * ثبت فعالیت در لاگ
     */
    public function logActivity($username, $action, $deviceId = null, $ipAddress = null, $userType = null) {
        try {
            $logDir = __DIR__ . '/logs';
            if (!is_dir($logDir)) {
                mkdir($logDir, 0755, true);
            }
            
            $logFile = $logDir . '/session_activity.log';
            $timestamp = date('Y-m-d H:i:s');
            $ip = $ipAddress ?: ($_SERVER['REMOTE_ADDR'] ?? 'نامشخص');
            
            $logEntry = "[$timestamp] $action | کاربر: $username";
            
            if ($userType) {
                $logEntry .= " | نوع کاربر: $userType";
            }
            
            if ($deviceId) {
                $logEntry .= " | دستگاه: $deviceId";
            }
            
            $logEntry .= " | IP: $ip\n";
            
            file_put_contents($logFile, $logEntry, FILE_APPEND | LOCK_EX);
            
        } catch (Exception $e) {
            error_log("خطا در ثبت لاگ فعالیت: " . $e->getMessage());
        }
    }
    
    /**
     * تنظیم مدت زمان انقضای جلسه (بر حسب ثانیه)
     */
    public function setSessionTimeout($seconds) {
        $this->sessionTimeout = max(300, $seconds); // حداقل 5 دقیقه
    }
    
    /**
     * دریافت مدت زمان انقضای جلسه
     */
    public function getSessionTimeout() {
        return $this->sessionTimeout;
    }
    
    /**
     * اعتبارسنجی توکن جلسه
     */
    public function validateSessionToken($username, $sessionToken, $deviceId = null) {
        try {
            $query = "
                SELECT id, device_id, login_time, last_activity, userType,
                       TIMESTAMPDIFF(SECOND, COALESCE(last_activity, login_time), NOW()) as session_duration
                FROM user_sessions 
                WHERE username = ? AND session_token = ? AND is_active = 1
            ";
            
            $params = [$username, $sessionToken];
            
            if ($deviceId) {
                $query .= " AND device_id = ?";
                $params[] = $deviceId;
            }
            
            $stmt = $this->pdo->prepare($query);
            $stmt->execute($params);
            $session = $stmt->fetch();
            
            if (!$session) {
                return false;
            }
            
            // برای کاربران admin، توکن همیشه معتبر است (بدون بررسی انقضا)
            if ($session['userType'] === 'admin') {
                // به‌روزرسانی آخرین فعالیت
                $this->updateLastActivity($username, $session['device_id']);
                return true;
            }
            
            // برای سایر کاربران، بررسی انقضای جلسه (حداکثر 12 ساعت)
            $maxSessionTime = 43200; // 12 ساعت
            if ($session['session_duration'] > $maxSessionTime) {
                $this->deactivateSession($username, $session['device_id']);
                return false;
            }
            
            // به‌روزرسانی آخرین فعالیت
            $this->updateLastActivity($username, $session['device_id']);
            
            return true;
            
        } catch (Exception $e) {
            error_log("خطا در اعتبارسنجی توکن جلسه: " . $e->getMessage());
            return false;
        }
    }
    
    /**
     * دریافت توکن جلسه فعال کاربر
     */
    public function getSessionToken($username, $deviceId = null) {
        try {
            $query = "
                SELECT session_token
                FROM user_sessions 
                WHERE username = ? AND is_active = 1
            ";
            
            $params = [$username];
            
            if ($deviceId) {
                $query .= " AND device_id = ?";
                $params[] = $deviceId;
            }
            
            $query .= " ORDER BY COALESCE(last_activity, login_time) DESC LIMIT 1";
            
            $stmt = $this->pdo->prepare($query);
            $stmt->execute($params);
            $result = $stmt->fetch();
            
            return $result ? $result['session_token'] : null;
            
        } catch (Exception $e) {
            error_log("خطا در دریافت توکن جلسه: " . $e->getMessage());
            return null;
        }
    }
}
?>