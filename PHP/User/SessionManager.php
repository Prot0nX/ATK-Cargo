<?php
// PHP/User/SessionManager.php
// Wrapper کلاس قدیمی پنل کاربران جهت حفظ سازگاری کامل و کاهش افزونگی

declare(strict_types=1);

require_once __DIR__ . '/../src/bootstrap.php';

class SessionManager {
    private \App\Services\SessionService $service;
    private \App\Repositories\SessionRepository $repo;
    private int $sessionTimeout = 86400;

    public function __construct() {
        $this->service = new \App\Services\SessionService();
        $this->repo = new \App\Repositories\SessionRepository();
    }

    public function createSession($username, $deviceId, $deviceModel, $androidVersion, $ipAddress, $userType = null) {
        return $this->service->createWebSession(
            (string)$username,
            (string)$deviceId,
            (string)$deviceModel,
            (string)$androidVersion,
            (string)$ipAddress,
            $userType ? (string)$userType : null
        );
    }

    public function getActiveSession($username) {
        return $this->repo->getActiveSession((string)$username);
    }

    public function isSessionActive($username, $deviceId = null) {
        return $this->service->isSessionActive((string)$username, $deviceId ? (string)$deviceId : null);
    }

    public function deactivateSession($username, $deviceId = null) {
        return $this->service->deactivateSession((string)$username, $deviceId ? (string)$deviceId : null);
    }

    public function updateSessionActivity($username, $deviceId) {
        return $this->updateLastActivity($username, $deviceId);
    }

    public function updateLastActivity($username, $deviceId) {
        $result = $this->repo->updateLastActivity((string)$username, (string)$deviceId);
        return [
            'success' => $result,
            'message' => $result ? 'آخرین فعالیت به‌روزرسانی شد' : 'جلسه فعالی یافت نشد'
        ];
    }

    public function getOnlineUsers() {
        return $this->repo->getOnlineUsers();
    }

    public function forceLogoutFromDevice($username, $deviceId) {
        return $this->service->forceLogoutFromDevice((string)$username, (string)$deviceId);
    }

    public function getSessionStats() {
        return $this->repo->getSessionStats();
    }

    public function logActivity($username, $action, $deviceId = null, $ipAddress = null, $userType = null) {
        $this->service->logActivity(
            (string)$username,
            (string)$action,
            $deviceId ? (string)$deviceId : null,
            $ipAddress ? (string)$ipAddress : null,
            $userType ? (string)$userType : null
        );
    }

    public function getSessionToken($username) {
        $session = $this->getActiveSession($username);
        return $session ? $session['session_token'] : null;
    }

    public function setSessionTimeout($seconds) {
        $this->sessionTimeout = (int)$seconds;
    }

    public function getSessionTimeout() {
        return $this->sessionTimeout;
    }
}