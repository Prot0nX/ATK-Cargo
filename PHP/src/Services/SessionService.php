<?php
// PHP/src/Services/SessionService.php

declare(strict_types=1);

namespace App\Services;

use App\Repositories\SessionRepository;
use App\Repositories\UserRepository;
use App\Exceptions\ApiException;

class SessionService {
    private SessionRepository $sessionRepository;
    private UserRepository $userRepository;

    public function __construct() {
        $this->sessionRepository = new SessionRepository();
        $this->userRepository = new UserRepository();
    }

    /**
     * ایجاد جلسه جدید برای نسخه اندروید (Mobile App) با محدودیت ورود همزمان تک دستگاهی
     */
    public function createMobileSession(
        string $username, 
        string $deviceId, 
        string $deviceModel, 
        string $androidVersion, 
        string $ipAddress, 
        ?string $userType = null, 
        ?string $appVersion = null
    ): array {
        // بررسی وجود جلسه فعال در هر دستگاهی
        $existingSession = $this->sessionRepository->getActiveSession($username);
        
        if ($existingSession) {
            if ($existingSession['device_id'] === $deviceId) {
                // همان دستگاه - به‌روزرسانی زمان فعالیت
                $this->sessionRepository->updateLastActivity($username, $deviceId);
                return [
                    'success' => true,
                    'message' => 'جلسه موجود به‌روزرسانی شد',
                    'session_id' => $existingSession['id'],
                    'session_token' => $existingSession['session_token']
                ];
            } else {
                // دستگاه دیگر - اجازه ورود همزمان داده نمی‌شود
                return [
                    'success' => false,
                    'message' => 'شما در حال حاضر از دستگاه دیگری وارد شده‌اید. لطفاً ابتدا از آن دستگاه خارج شوید.'
                ];
            }
        }

        if ($userType === null) {
            $user = $this->userRepository->getByUsername($username);
            if (!$user) {
                throw new ApiException('کاربر در سیستم یافت نشد', 404);
            }
            $userType = $user['userType'];
        }

        // تولید توکن تصادفی امن
        $sessionToken = bin2hex(random_bytes(32));

        $sessionId = $this->sessionRepository->createSession([
            'username' => $username,
            'device_id' => $deviceId,
            'device_model' => $deviceModel,
            'android_version' => $androidVersion,
            'app_version' => $appVersion,
            'ip_address' => $ipAddress,
            'userType' => $userType,
            'session_token' => $sessionToken
        ]);

        $this->logActivity($username, 'LOGIN', $deviceId, $ipAddress, $userType);

        return [
            'success' => true,
            'message' => 'جلسه با موفقیت ایجاد شد',
            'session_id' => $sessionId,
            'session_token' => $sessionToken
        ];
    }

    /**
     * ایجاد جلسه جدید برای پنل تحت وب (User Panel) با امکان خروج خودکار سایر دستگاه‌ها
     */
    public function createWebSession(
        string $username, 
        string $deviceId, 
        string $deviceModel, 
        string $androidVersion, 
        string $ipAddress, 
        ?string $userType = null
    ): array {
        // بررسی وجود جلسه فعال برای همین دستگاه
        $existingSession = $this->sessionRepository->getActiveSessionByDevice($username, $deviceId);
        
        if ($existingSession) {
            $this->sessionRepository->updateLastActivity($username, $deviceId);
            return [
                'success' => true,
                'message' => 'جلسه موجود به‌روزرسانی شد',
                'session_id' => $existingSession['id'],
                'session_token' => $existingSession['session_token']
            ];
        }

        if ($userType === null) {
            $user = $this->userRepository->getByUsername($username);
            if (!$user) {
                throw new ApiException('کاربر در سیستم یافت نشد', 404);
            }
            $userType = $user['userType'];
        }

        // غیرفعال کردن تمامی جلسات قبلی کاربر بر روی سایر دستگاه‌ها
        $this->sessionRepository->deactivateAllSessions($username);

        // ایجاد توکن جلسه جدید
        $sessionToken = bin2hex(random_bytes(32));

        $sessionId = $this->sessionRepository->createSession([
            'username' => $username,
            'device_id' => $deviceId,
            'device_model' => $deviceModel,
            'android_version' => $androidVersion,
            'ip_address' => $ipAddress,
            'userType' => $userType,
            'session_token' => $sessionToken
        ]);

        $this->logActivity($username, 'LOGIN', $deviceId, $ipAddress, $userType);

        return [
            'success' => true,
            'message' => 'جلسه با موفقیت ایجاد شد',
            'session_id' => $sessionId,
            'session_token' => $sessionToken
        ];
    }

    /**
     * بررسی دقیق اعتبار یک نشست (نام کاربری + دستگاه + توکن) برای گیت احراز
     * هویت درخواست‌های API. برخلاف isSessionActive، اینجا مطابقت توکن هم بررسی
     * می‌شود تا صرف دانستن نام کاربری/شناسه دستگاه برای عبور از گیت کافی نباشد.
     */
    public function isValidToken(string $username, string $deviceId, string $token): bool {
        if ($username === '' || $deviceId === '' || $token === '') {
            return false;
        }

        $valid = $this->sessionRepository->isValidToken($username, $deviceId, $token);
        if ($valid) {
            $this->sessionRepository->updateLastActivity($username, $deviceId);
        }
        return $valid;
    }

    /**
     * بررسی معتبر بودن جلسه کاربر
     */
    public function isSessionActive(string $username, ?string $deviceId = null): bool {
        $session = null;
        if ($deviceId) {
            $session = $this->sessionRepository->getActiveSessionByDevice($username, $deviceId);
        } else {
            $session = $this->sessionRepository->getActiveSession($username);
        }

        if ($session) {
            // به‌روزرسانی خودکار آخرین فعالیت
            $this->sessionRepository->updateLastActivity($username, $session['device_id'] ?? $deviceId);
            return true;
        }

        return false;
    }

    /**
     * خروج کاربر (غیرفعال کردن تمام جلسات فعال)
     */
    public function deactivateSession(string $username, ?string $deviceId = null): array {
        $user = $this->userRepository->getByUsername($username);
        if (!$user) {
            return [
                'success' => false,
                'message' => 'کاربر در سیستم وجود ندارد',
                'http_code' => 404
            ];
        }

        $activeIds = $this->sessionRepository->getActiveSessionsForUser($username);
        if (empty($activeIds)) {
            return [
                'success' => false,
                'message' => 'جلسه فعالی برای غیرفعال کردن یافت نشد',
                'http_code' => 404
            ];
        }

        $this->sessionRepository->deactivateSessionsByIds($activeIds);
        $this->logActivity($username, 'LOGOUT_ALL_SESSIONS', $deviceId);

        return [
            'success' => true,
            'message' => 'خروج با موفقیت انجام شد - تمام جلسات فعال غیرفعال شدند',
            'affected_sessions' => count($activeIds),
            'http_code' => 200
        ];
    }

    /**
     * خروج اجباری از دستگاه خاص
     */
    public function forceLogoutFromDevice(string $username, string $deviceId): array {
        $activeSession = $this->sessionRepository->getActiveSessionByDevice($username, $deviceId);
        
        if (!$activeSession) {
            return [
                'success' => false,
                'message' => 'جلسه فعالی برای خروج یافت نشد'
            ];
        }

        $this->sessionRepository->deactivateSessionByDevice($username, $deviceId);
        $this->logActivity($username, 'FORCE_LOGOUT', $deviceId);

        return [
            'success' => true,
            'message' => 'کاربر از دستگاه قبلی خارج شد'
        ];
    }

    /**
     * دریافت لیست کاربران آنلاین
     */
    public function getOnlineUsers(): array {
        return $this->sessionRepository->getOnlineUsers();
    }

    /**
     * دریافت آمار جلسات
     */
    public function getSessionStats(): array {
        return $this->sessionRepository->getSessionStats();
    }

    /**
     * ثبت لاگ فعالیت نشست‌ها
     */
    public function logActivity(string $username, string $action, ?string $deviceId = null, ?string $ipAddress = null, ?string $userType = null): void {
        try {
            $logDir = APP_ROOT . '/logs';
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
        } catch (\Exception $e) {
            error_log("Error writing session log: " . $e->getMessage());
        }
    }
}
