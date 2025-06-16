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
    private $sessionTimeout = 3600; // 1 ساعت (بر حسب ثانیه)
    
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
     */
    public function createSession($username, $deviceId, $deviceModel = null, $androidVersion = null, $ipAddress = null) {
        try {
            // ابتدا جلسات منقضی شده را پاک می‌کنیم
            $this->cleanupExpiredSessions();
            
            // بررسی جلسه فعال موجود
            $existingSession = $this->getActiveSession($username);
            
            if ($existingSession) {
                // اگر همان دستگاه است، جلسه را به‌روزرسانی می‌کنیم
                if ($existingSession['device_id'] === $deviceId) {
                    return $this->updateSessionActivity($username, $deviceId);
                } else {
                    // اگر دستگاه متفاوت است، خطا برمی‌گردانیم
                    throw new Exception("کاربر در دستگاه دیگری فعال است");
                }
            }
            
            // ایجاد جلسه جدید
            $stmt = $this->pdo->prepare("
                INSERT INTO user_sessions 
                (username, device_id, device_model, android_version, login_time, is_active, ip_address) 
                VALUES (?, ?, ?, ?, NOW(), 1, ?)
            ");
            
            $result = $stmt->execute([$username, $deviceId, $deviceModel, $androidVersion, $ipAddress]);
            
            if ($result) {
                $this->logActivity($username, 'LOGIN', $deviceId, $ipAddress);
                return [
                    'success' => true,
                    'message' => 'جلسه با موفقیت ایجاد شد',
                    'session_id' => $this->pdo->lastInsertId()
                ];
            }
            
            throw new Exception("خطا در ایجاد جلسه");
            
        } catch (Exception $e) {
            error_log("خطا در ایجاد جلسه: " . $e->getMessage());
            throw $e;
        }
    }
    
    /**
     * بررسی وضعیت فعال بودن جلسه کاربر
     */
    public function isSessionActive($username, $deviceId = null) {
        try {
            $this->cleanupExpiredSessions();
            
            $query = "
                SELECT id, device_id, login_time, 
                       TIMESTAMPDIFF(SECOND, login_time, NOW()) as session_duration
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
                // بررسی انقضای جلسه
                if ($session['session_duration'] > $this->sessionTimeout) {
                    $this->deactivateSession($username, $session['device_id']);
                    return false;
                }
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
            $query = "UPDATE user_sessions SET is_active = 0, logout_time = NOW() WHERE username = ? AND is_active = 1";
            $params = [$username];
            
            if ($deviceId) {
                $query .= " AND device_id = ?";
                $params[] = $deviceId;
            }
            
            $stmt = $this->pdo->prepare($query);
            $result = $stmt->execute($params);
            
            if ($result && $stmt->rowCount() > 0) {
                $this->logActivity($username, 'LOGOUT', $deviceId);
                return [
                    'success' => true,
                    'message' => 'خروج با موفقیت انجام شد',
                    'affected_sessions' => $stmt->rowCount()
                ];
            }
            
            return [
                'success' => false,
                'message' => 'جلسه فعالی برای غیرفعال کردن یافت نشد'
            ];
            
        } catch (Exception $e) {
            error_log("خطا در غیرفعال کردن جلسه: " . $e->getMessage());
            throw $e;
        }
    }
    
    /**
     * دریافت اطلاعات جلسه فعال کاربر
     */
    public function getActiveSession($username) {
        try {
            $stmt = $this->pdo->prepare("
                SELECT id, device_id, device_model, android_version, login_time, ip_address,
                       TIMESTAMPDIFF(SECOND, login_time, NOW()) as session_duration
                FROM user_sessions 
                WHERE username = ? AND is_active = 1
                ORDER BY login_time DESC 
                LIMIT 1
            ");
            
            $stmt->execute([$username]);
            return $stmt->fetch();
            
        } catch (Exception $e) {
            error_log("خطا در دریافت جلسه فعال: " . $e->getMessage());
            return null;
        }
    }
    
    /**
     * به‌روزرسانی فعالیت جلسه (برای جلوگیری از انقضا)
     */
    public function updateSessionActivity($username, $deviceId) {
        try {
            $stmt = $this->pdo->prepare("
                UPDATE user_sessions 
                SET updated_at = NOW() 
                WHERE username = ? AND device_id = ? AND is_active = 1
            ");
            
            $result = $stmt->execute([$username, $deviceId]);
            
            return [
                'success' => $result && $stmt->rowCount() > 0,
                'message' => $result ? 'فعالیت جلسه به‌روزرسانی شد' : 'جلسه فعالی یافت نشد'
            ];
            
        } catch (Exception $e) {
            error_log("خطا در به‌روزرسانی فعالیت جلسه: " . $e->getMessage());
            throw $e;
        }
    }
    
    /**
     * پاکسازی جلسات منقضی شده
     */
    public function cleanupExpiredSessions() {
        try {
            $stmt = $this->pdo->prepare("
                UPDATE user_sessions 
                SET is_active = 0, logout_time = NOW() 
                WHERE is_active = 1 
                AND TIMESTAMPDIFF(SECOND, COALESCE(updated_at, login_time), NOW()) > ?
            ");
            
            $stmt->execute([$this->sessionTimeout]);
            
            if ($stmt->rowCount() > 0) {
                error_log("تعداد " . $stmt->rowCount() . " جلسه منقضی شده پاک شد");
            }
            
        } catch (Exception $e) {
            error_log("خطا در پاکسازی جلسات منقضی: " . $e->getMessage());
        }
    }
    
    /**
     * دریافت لیست کاربران آنلاین
     */
    public function getOnlineUsers() {
        try {
            $this->cleanupExpiredSessions();
            
            $stmt = $this->pdo->prepare("
                SELECT DISTINCT us.username, u.userType, us.device_model, us.login_time,
                       TIMESTAMPDIFF(SECOND, us.login_time, NOW()) as online_duration
                FROM user_sessions us
                JOIN Users u ON us.username = u.username
                WHERE us.is_active = 1
                ORDER BY us.login_time DESC
            ");
            
            $stmt->execute();
            return $stmt->fetchAll();
            
        } catch (Exception $e) {
            error_log("خطا در دریافت کاربران آنلاین: " . $e->getMessage());
            return [];
        }
    }
    
    /**
     * ثبت فعالیت در لاگ
     */
    private function logActivity($username, $action, $deviceId = null, $ipAddress = null) {
        try {
            $logDir = __DIR__ . '/logs';
            if (!is_dir($logDir)) {
                mkdir($logDir, 0755, true);
            }
            
            $logFile = $logDir . '/session_activity.log';
            $timestamp = date('Y-m-d H:i:s');
            $ip = $ipAddress ?: ($_SERVER['REMOTE_ADDR'] ?? 'نامشخص');
            
            $logEntry = "[$timestamp] $action | کاربر: $username";
            
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
}
?>