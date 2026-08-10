<?php
// PHP/src/Repositories/SessionRepository.php

declare(strict_types=1);

namespace App\Repositories;

use App\Core\Database;
use PDO;

class SessionRepository {
    private PDO $db;

    public function __construct() {
        $this->db = Database::getInstance()->getPdoConnection();
    }

    /**
     * دریافت جلسه فعال بر اساس نام کاربری
     */
    public function getActiveSession(string $username): ?array {
        $stmt = $this->db->prepare("
            SELECT id, username, device_id, device_model, android_version, app_version,
                   login_time, last_activity, session_token, userType,
                   TIMESTAMPDIFF(SECOND, COALESCE(last_activity, login_time), NOW()) as session_duration
            FROM user_sessions 
            WHERE username = :username AND is_active = 1
            ORDER BY COALESCE(last_activity, login_time) DESC
            LIMIT 1
        ");
        $stmt->execute([':username' => $username]);
        $session = $stmt->fetch();
        return $session ?: null;
    }

    /**
     * بررسی معتبر بودن دقیق یک جلسه (نام کاربری + دستگاه + توکن + فعال بودن)
     * برای احراز هویت درخواست‌های API (مثل app_api.php) استفاده می‌شود.
     */
    public function isValidToken(string $username, string $deviceId, string $token): bool {
        $stmt = $this->db->prepare("
            SELECT id FROM user_sessions
            WHERE username = :username AND device_id = :device_id
              AND session_token = :token AND is_active = 1
            LIMIT 1
        ");
        $stmt->execute([
            ':username' => $username,
            ':device_id' => $deviceId,
            ':token' => $token,
        ]);
        return (bool)$stmt->fetch();
    }

    /**
     * دریافت جلسه فعال بر اساس نام کاربری و دستگاه
     */
    public function getActiveSessionByDevice(string $username, string $deviceId): ?array {
        $stmt = $this->db->prepare("
            SELECT id, session_token, device_id 
            FROM user_sessions 
            WHERE username = :username AND device_id = :device_id AND is_active = 1
            LIMIT 1
        ");
        $stmt->execute([
            ':username' => $username,
            ':device_id' => $deviceId
        ]);
        $session = $stmt->fetch();
        return $session ?: null;
    }

    /**
     * غیرفعال کردن تمامی جلسات فعال یک کاربر
     */
    public function deactivateAllSessions(string $username): int {
        $stmt = $this->db->prepare("
            UPDATE user_sessions 
            SET is_active = 0, logout_time = NOW() 
            WHERE username = :username AND is_active = 1
        ");
        $stmt->execute([':username' => $username]);
        return $stmt->rowCount();
    }

    /**
     * غیرفعال کردن جلسات فعال کاربر با شناسه‌های خاص
     */
    public function deactivateSessionsByIds(array $ids): int {
        if (empty($ids)) {
            return 0;
        }
        $placeholders = str_repeat('?,', count($ids) - 1) . '?';
        $stmt = $this->db->prepare("UPDATE user_sessions SET is_active = 0, logout_time = NOW() WHERE id IN ($placeholders)");
        $stmt->execute($ids);
        return $stmt->rowCount();
    }

    /**
     * غیرفعال کردن جلسه بر اساس نام کاربری و دستگاه خاص
     */
    public function deactivateSessionByDevice(string $username, string $deviceId): int {
        $stmt = $this->db->prepare("
            UPDATE user_sessions 
            SET is_active = 0, logout_time = NOW(), last_activity = NOW()
            WHERE username = :username AND device_id = :device_id AND is_active = 1
        ");
        $stmt->execute([
            ':username' => $username,
            ':device_id' => $deviceId
        ]);
        return $stmt->rowCount();
    }

    /**
     * ایجاد جلسه جدید
     */
    public function createSession(array $data): int {
        $stmt = $this->db->prepare("
            INSERT INTO user_sessions 
            (username, device_id, device_model, android_version, app_version, login_time, last_activity, is_active, ip_address, userType, session_token) 
            VALUES (:username, :device_id, :device_model, :android_version, :app_version, NOW(), NOW(), 1, :ip_address, :userType, :session_token)
        ");
        
        $stmt->execute([
            ':username' => $data['username'],
            ':device_id' => $data['device_id'],
            ':device_model' => $data['device_model'],
            ':android_version' => $data['android_version'],
            ':app_version' => $data['app_version'] ?? null,
            ':ip_address' => $data['ip_address'],
            ':userType' => $data['userType'],
            ':session_token' => $data['session_token']
        ]);
        
        return (int)$this->db->lastInsertId();
    }

    /**
     * به‌روزرسانی زمان فعالیت جلسه
     */
    public function updateLastActivity(string $username, string $deviceId): bool {
        $stmt = $this->db->prepare("
            UPDATE user_sessions 
            SET last_activity = NOW() 
            WHERE username = :username AND device_id = :device_id AND is_active = 1
        ");
        $stmt->execute([
            ':username' => $username,
            ':device_id' => $deviceId
        ]);
        return $stmt->rowCount() > 0;
    }

    /**
     * دریافت کاربران آنلاین به همراه نوع کاربری
     */
    public function getOnlineUsers(): array {
        $stmt = $this->db->prepare("
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
    }

    /**
     * دریافت آخرین جلسه ثبت شده برای تمامی کاربران (جهت بررسی وضعیت آنلاین/آفلاین و تاریخ آخرین بازدید)
     */
    public function getLatestSessionsForAllUsers(): array {
        $stmt = $this->db->prepare("
            SELECT us.id, us.username, us.device_model, us.device_id, us.is_active,
                   us.login_time, us.last_activity, us.ip_address,
                   TIMESTAMPDIFF(SECOND, COALESCE(us.last_activity, us.login_time), NOW()) as idle_time
            FROM user_sessions us
            INNER JOIN (
                SELECT username, MAX(id) as max_id
                FROM user_sessions
                GROUP BY username
            ) latest ON us.id = latest.max_id
        ");
        $stmt->execute();
        return $stmt->fetchAll();
    }

    /**
     * دریافت تمامی جلسات فعال کاربر
     */
    public function getActiveSessionsForUser(string $username): array {
        $stmt = $this->db->prepare("SELECT id FROM user_sessions WHERE username = :username AND is_active = 1");
        $stmt->execute([':username' => $username]);
        return $stmt->fetchAll(PDO::FETCH_COLUMN);
    }

    /**
     * دریافت آمار جلسات فعال و امروز
     */
    public function getSessionStats(): array {
        // آمار جلسات فعال فعلی
        $stmt = $this->db->prepare("
            SELECT 
                COUNT(*) as total_active_sessions,
                COUNT(DISTINCT username) as unique_users_online,
                AVG(TIMESTAMPDIFF(SECOND, login_time, NOW())) as avg_session_duration
            FROM user_sessions 
            WHERE is_active = 1
        ");
        $stmt->execute();
        $stats = $stmt->fetch();

        // لاگین‌های امروز
        $stmt = $this->db->prepare("
            SELECT 
                COUNT(*) as today_logins,
                COUNT(DISTINCT username) as unique_users_today
            FROM user_sessions 
            WHERE DATE(login_time) = CURDATE()
        ");
        $stmt->execute();
        $todayStats = $stmt->fetch();

        return array_merge($stats ?: [], $todayStats ?: []);
    }
}
