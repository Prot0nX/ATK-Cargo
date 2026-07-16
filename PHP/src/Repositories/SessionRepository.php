<?php

declare(strict_types=1);

namespace AtkCargo\Repositories;

use AtkCargo\Core\Database;
use PDO;
use Exception;

/**
 * Repository layer for User Sessions and Authentication Database Queries
 */
class SessionRepository
{
    private PDO $db;

    public function __construct()
    {
        $this->db = Database::getInstance()->getPdo();
    }

    /**
     * Fetch user credentials and type by username
     */
    public function getUserByUsername(string $username): ?array
    {
        $stmt = $this->db->prepare("SELECT password, userType FROM Users WHERE username = :username LIMIT 1");
        $stmt->bindParam(':username', $username, PDO::PARAM_STR);
        $stmt->execute();
        $user = $stmt->fetch();
        return $user ?: null;
    }

    /**
     * Get active session for a specific user and device
     */
    public function getActiveSession(string $username, string $deviceId): ?array
    {
        $stmt = $this->db->prepare("
            SELECT id, session_token, device_id, login_time, last_activity, userType
            FROM user_sessions 
            WHERE username = ? AND device_id = ? AND is_active = 1
            LIMIT 1
        ");
        $stmt->execute([$username, $deviceId]);
        $session = $stmt->fetch();
        return $session ?: null;
    }

    /**
     * Get any active session for user regardless of device
     */
    public function getAnyActiveSession(string $username): ?array
    {
        $stmt = $this->db->prepare("
            SELECT id, session_token, device_id, login_time, last_activity, userType
            FROM user_sessions 
            WHERE username = ? AND is_active = 1
            ORDER BY COALESCE(last_activity, login_time) DESC
            LIMIT 1
        ");
        $stmt->execute([$username]);
        $session = $stmt->fetch();
        return $session ?: null;
    }

    /**
     * Update session last activity time
     */
    public function updateSessionActivity(string $username, string $deviceId): bool
    {
        $stmt = $this->db->prepare("
            UPDATE user_sessions 
            SET last_activity = NOW() 
            WHERE username = ? AND device_id = ? AND is_active = 1
        ");
        return $stmt->execute([$username, $deviceId]);
    }

    /**
     * Create a new session entry
     */
    public function createSession(
        string $username,
        string $deviceId,
        string $deviceModel,
        string $androidVersion,
        ?string $appVersion,
        string $ipAddress,
        string $userType,
        string $sessionToken
    ): int {
        $stmt = $this->db->prepare("
            INSERT INTO user_sessions 
            (username, device_id, device_model, android_version, app_version, login_time, last_activity, is_active, ip_address, userType, session_token) 
            VALUES (?, ?, ?, ?, ?, NOW(), NOW(), 1, ?, ?, ?)
        ");
        
        $stmt->execute([
            $username,
            $deviceId,
            $deviceModel,
            $androidVersion,
            $appVersion,
            $ipAddress,
            $userType,
            $sessionToken
        ]);

        return (int)$this->db->lastInsertId();
    }

    /**
     * Deactivate all active sessions of a user
     */
    public function deactivateAllSessions(string $username): bool
    {
        $stmt = $this->db->prepare("
            UPDATE user_sessions 
            SET is_active = 0, logout_time = NOW() 
            WHERE username = ? AND is_active = 1
        ");
        return $stmt->execute([$username]);
    }

    /**
     * Deactivate a specific active session by device
     */
    public function deactivateSessionByDevice(string $username, string $deviceId): bool
    {
        $stmt = $this->db->prepare("
            UPDATE user_sessions 
            SET is_active = 0, logout_time = NOW() 
            WHERE username = ? AND device_id = ? AND is_active = 1
        ");
        return $stmt->execute([$username, $deviceId]);
    }

    /**
     * Check if a specific session is active
     */
    public function isSessionActive(string $username, string $deviceId): bool
    {
        $stmt = $this->db->prepare("
            SELECT COUNT(*) 
            FROM user_sessions 
            WHERE username = ? AND device_id = ? AND is_active = 1
        ");
        $stmt->execute([$username, $deviceId]);
        $count = (int)$stmt->fetchColumn();
        
        if ($count > 0) {
            $this->updateSessionActivity($username, $deviceId);
            return true;
        }
        
        return false;
    }

    /**
     * Log user activity (login, logout, action, etc.)
     */
    public function logActivity(string $username, string $action, string $deviceId, string $ipAddress, ?string $userType): void
    {
        try {
            $stmt = $this->db->prepare("
                INSERT INTO user_activity_logs 
                (username, action, device_id, ip_address, user_type, created_at) 
                VALUES (?, ?, ?, ?, ?, NOW())
            ");
            $stmt->execute([$username, $action, $deviceId, $ipAddress, $userType]);
        } catch (Exception $e) {
            // Silence log tables errors to prevent crashing the app flow
            error_log("Failed to write to user_activity_logs: " . $e->getMessage());
        }
    }

    /**
     * Get list of active online users with durations
     */
    public function getOnlineUsers(): array
    {
        $stmt = $this->db->prepare("
            SELECT us.id, us.username, u.userType, us.device_model, us.device_id, 
                   us.login_time, us.last_activity, us.ip_address,
                   TIMESTAMPDIFF(SECOND, us.login_time, NOW()) as online_duration,
                   TIMESTAMPDIFF(SECOND, us.last_activity, NOW()) as idle_time
            FROM user_sessions us
            JOIN Users u ON us.username = u.username
            WHERE us.is_active = 1
            ORDER BY us.last_activity DESC, us.login_time DESC
        ");
        $stmt->execute();
        return $stmt->fetchAll(PDO::FETCH_ASSOC);
    }

    /**
     * Fetch all sessions filtered by time and status
     */
    public function getAllSessions(string $timeFilter = 'all', string $statusFilter = 'all'): array
    {
        $whereConditions = [];
        $params = [];
        
        if ($statusFilter === 'active') {
            $whereConditions[] = "us.is_active = 1";
        } elseif ($statusFilter === 'inactive') {
            $whereConditions[] = "us.is_active = 0";
        }
        
        switch ($timeFilter) {
            case 'today':
                $whereConditions[] = "DATE(us.login_time) = CURDATE()";
                break;
            case '24h':
                $whereConditions[] = "us.login_time >= DATE_SUB(NOW(), INTERVAL 24 HOUR)";
                break;
        }
        
        $whereClause = !empty($whereConditions) ? 'WHERE ' . implode(' AND ', $whereConditions) : '';
        
        $stmt = $this->db->prepare("
            SELECT us.id, us.username, u.userType, us.device_model, us.device_id, 
                   us.android_version, us.app_version,
                   us.login_time, us.last_activity, us.logout_time, us.ip_address, us.is_active,
                   COALESCE(us.updated_at, us.last_activity, us.login_time) as updated_at,
                   TIMESTAMPDIFF(SECOND, us.login_time, COALESCE(us.logout_time, NOW())) as session_duration,
                   TIMESTAMPDIFF(SECOND, us.last_activity, NOW()) as idle_time
            FROM user_sessions us
            JOIN Users u ON us.username = u.username
            $whereClause
            ORDER BY us.is_active DESC, us.login_time DESC, us.last_activity DESC
        ");
        
        $stmt->execute($params);
        return $stmt->fetchAll(PDO::FETCH_ASSOC);
    }

    /**
     * Get aggregate statistics on sessions
     */
    public function getSessionStats(): array
    {
        // 1. Current Active Stats
        $stmt = $this->db->prepare("
            SELECT 
                COUNT(*) as total_active_sessions,
                COUNT(DISTINCT username) as unique_users_online,
                COALESCE(AVG(TIMESTAMPDIFF(SECOND, login_time, NOW())), 0) as avg_session_duration
            FROM user_sessions 
            WHERE is_active = 1
        ");
        $stmt->execute();
        $stats = $stmt->fetch(PDO::FETCH_ASSOC) ?: [];

        // 2. Today's cumulative logins
        $stmt = $this->db->prepare("
            SELECT 
                COUNT(*) as today_logins,
                COUNT(DISTINCT username) as unique_users_today
            FROM user_sessions 
            WHERE DATE(login_time) = CURDATE()
        ");
        $stmt->execute();
        $todayStats = $stmt->fetch(PDO::FETCH_ASSOC) ?: [];

        return array_merge($stats, $todayStats);
    }

    /**
     * Deactivate all active sessions across the system
     */
    public function deactivateAllActiveUsers(): array
    {
        $stmt = $this->db->prepare("
            UPDATE user_sessions 
            SET is_active = 0, logout_time = NOW(), last_activity = NOW()
            WHERE is_active = 1
        ");
        $stmt->execute();
        $count = $stmt->rowCount();
        
        return [
            'success' => true,
            'message' => 'خروج همگانی با موفقیت انجام شد',
            'logged_out_count' => $count
        ];
    }

    /**
     * Deactivate user session for device and clean up older inactive records
     */
    public function forceLogoutFromDevice(string $username, string $deviceId): array
    {
        // 1. Delete old inactive sessions for this user/device to clean up database size
        $deleteOld = $this->db->prepare("
            DELETE FROM user_sessions 
            WHERE username = ? AND device_id = ? AND is_active = 0
        ");
        $deleteOld->execute([$username, $deviceId]);

        // 2. Deactivate active session
        $update = $this->db->prepare("
            UPDATE user_sessions 
            SET is_active = 0, logout_time = NOW(), last_activity = NOW()
            WHERE username = ? AND device_id = ? AND is_active = 1
        ");
        $update->execute([$username, $deviceId]);
        $affected = $update->rowCount();

        if ($affected > 0) {
            $this->logActivity($username, 'FORCE_LOGOUT', $deviceId, $_SERVER['REMOTE_ADDR'] ?? 'Unknown', null);
            return [
                'success' => true,
                'message' => 'کاربر از دستگاه قبلی خارج شد'
            ];
        }

        return [
            'success' => false,
            'message' => 'جلسه فعالی برای خروج یافت نشد'
        ];
    }
}

