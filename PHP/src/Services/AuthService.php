<?php

declare(strict_types=1);

namespace AtkCargo\Services;

use AtkCargo\Repositories\SessionRepository;
use Exception;

/**
 * Authentication and Session Management Service Layer
 */
class AuthService
{
    private SessionRepository $sessionRepository;

    public function __construct()
    {
        $this->sessionRepository = new SessionRepository();
    }

    /**
     * Handle user login logic
     * 
     * @return array Login status and session info
     * @throws Exception
     */
    public function login(array $credentials): array
    {
        $username = $credentials['username'] ?? '';
        $password = $credentials['password'] ?? '';
        $userType = $credentials['userType'] ?? '';
        $deviceId = $credentials['deviceId'] ?? '';
        $deviceModel = $credentials['deviceModel'] ?? '';
        $androidVersion = $credentials['androidVersion'] ?? '';
        $appVersion = $credentials['appVersion'] ?? null;
        $ipAddress = $credentials['ipAddress'] ?? 'Unknown';

        if (empty($username) || empty($password)) {
            throw new Exception("نام کاربری و رمز عبور الزامی است.", 400);
        }

        // 1. Fetch user credentials from DB
        $user = $this->sessionRepository->getUserByUsername($username);
        
        // Log attempt
        error_log("Login attempt - Username hash: " . md5($username) . ", Result: " . ($user ? 'Found' : 'Not found'));

        if (!$user || !hash_equals($user['password'], $password)) {
            throw new Exception("نام کاربری یا رمز عبور اشتباه است.", 401);
        }

        // 2. Load and verify permissions
        $permissions = $this->loadPermissions($username, $user['userType'], $userType);
        if (!$permissions['allowed']) {
            throw new Exception("شما دسترسی به این بخش را ندارید.", 403);
        }

        // 3. Handle Active Sessions (Single Session Rule)
        $existingSession = $this->sessionRepository->getAnyActiveSession($username);
        if ($existingSession) {
            if ($existingSession['device_id'] === $deviceId) {
                // Same device - update activity and return existing session
                $this->sessionRepository->updateSessionActivity($username, $deviceId);
                return [
                    'success' => true,
                    'message' => 'جلسه موجود به‌روزرسانی شد',
                    'session_token' => $existingSession['session_token'],
                    'userType' => $user['userType']
                ];
            } else {
                // Different device - concurrent logins not allowed
                throw new Exception("شما در حال حاضر از دستگاه دیگری وارد شده‌اید. لطفاً ابتدا از آن دستگاه خارج شوید.", 400);
            }
        }

        // 4. Create new Session
        $sessionToken = bin2hex(random_bytes(32));
        $this->sessionRepository->createSession(
            $username,
            $deviceId,
            $deviceModel,
            $androidVersion,
            $appVersion,
            $ipAddress,
            $user['userType'],
            $sessionToken
        );

        $this->sessionRepository->logActivity($username, 'LOGIN', $deviceId, $ipAddress, $user['userType']);

        return [
            'success' => true,
            'message' => 'ورود با موفقیت انجام شد',
            'session_token' => $sessionToken,
            'userType' => $user['userType']
        ];
    }

    /**
     * Check if a session is currently active
     */
    public function checkSession(string $username, string $deviceId): bool
    {
        if (empty($username)) {
            return false;
        }

        $user = $this->sessionRepository->getUserByUsername($username);
        if (!$user) {
            return false;
        }

        return $this->sessionRepository->isSessionActive($username, $deviceId);
    }

    /**
     * Sync and fetch current permissions for an active session
     * 
     * @throws Exception
     */
    public function syncPermissions(string $username, string $deviceId): array
    {
        // 1. Validate session
        if (!$this->checkSession($username, $deviceId)) {
            throw new Exception("نشست کاربر منقضی شده است.", 401);
        }

        // 2. Fetch User
        $user = $this->sessionRepository->getUserByUsername($username);
        if (!$user) {
            throw new Exception("کاربر یافت نشد.", 404);
        }

        // 3. Load Permissions
        $permsData = $this->loadPermissions($username, $user['userType'], '');
        
        return [
            'success' => true,
            'message' => 'Permissions synced successfully.',
            'userType' => $user['userType'],
            'permissions' => $permsData['permissions']
        ];
    }

    /**
     * Deactivate user session simulating legacy deactivateSession behavior
     */
    public function deactivateSession(string $username, ?string $deviceId = null): array
    {
        $user = $this->sessionRepository->getUserByUsername($username);
        if (!$user) {
            return [
                'success' => false,
                'message' => 'کاربر در سیستم وجود ندارد',
                'http_code' => 404
            ];
        }

        $success = $this->sessionRepository->deactivateAllSessions($username);
        if ($success) {
            $this->sessionRepository->logActivity($username, 'LOGOUT_ALL_SESSIONS', $deviceId ?? 'Unknown', $_SERVER['REMOTE_ADDR'] ?? 'Unknown', $user['userType']);
            return [
                'success' => true,
                'message' => 'خروج با موفقیت انجام شد - تمام جلسات فعال غیرفعال شدند',
                'http_code' => 200
            ];
        }

        return [
            'success' => false,
            'message' => 'جلسه فعالی برای غیرفعال کردن یافت نشد',
            'http_code' => 404
        ];
    }

    /**
     * Force logout from device or all devices
     */
    public function forceLogout(string $username, ?string $deviceId = null): array
    {
        if ($deviceId) {
            $success = $this->sessionRepository->deactivateSessionByDevice($username, $deviceId);
            $message = $success ? 'خروج از دستگاه با موفقیت انجام شد' : 'جلسه فعالی برای این دستگاه یافت نشد';
        } else {
            $success = $this->sessionRepository->deactivateAllSessions($username);
            $message = $success ? 'خروج از تمام دستگاه‌ها با موفقیت انجام شد' : 'هیچ جلسه فعالی یافت نشد';
        }

        return [
            'success' => $success,
            'message' => $message
        ];
    }

    /**
     * Get list of active online users
     */
    public function getOnlineUsers(): array
    {
        return $this->sessionRepository->getOnlineUsers();
    }

    /**
     * Get all sessions history with filters
     */
    public function getAllSessions(string $timeFilter = 'all', string $statusFilter = 'all'): array
    {
        return $this->sessionRepository->getAllSessions($timeFilter, $statusFilter);
    }

    /**
     * Get session statistics
     */
    public function getSessionStats(): array
    {
        return $this->sessionRepository->getSessionStats();
    }

    /**
     * Force logout a user from a specific device
     */
    public function forceLogoutFromDevice(string $username, string $deviceId): array
    {
        return $this->sessionRepository->forceLogoutFromDevice($username, $deviceId);
    }

    /**
     * Terminate all active sessions on the system
     */
    public function logoutAllActiveUsers(): array
    {
        return $this->sessionRepository->deactivateAllActiveUsers();
    }

    /**
     * Load permissions from config/permissions.json and check access
     */
    private function loadPermissions(string $username, string $dbUserType, string $requestedUserType): array
    {
        $permissionsFile = dirname(dirname(__DIR__)) . '/config/permissions.json';
        $allPermissions = [];
        
        if (file_exists($permissionsFile)) {
            $allPermissions = json_decode(file_get_contents($permissionsFile), true) ?: [];
        }

        // Determine user permissions based on user priority
        if (isset($allPermissions['roles'])) {
            $userPermissions = $allPermissions['users'][$username] ?? $allPermissions['roles'][$dbUserType] ?? [];
        } else {
            $userPermissions = $allPermissions[$dbUserType] ?? [];
        }

        $allowedAccess = true;
        if (!empty($requestedUserType)) {
            $allowedAccess = $userPermissions[$requestedUserType] ?? ($dbUserType === 'admin');
        }

        return [
            'allowed' => $allowedAccess,
            'permissions' => $userPermissions
        ];
    }
}
