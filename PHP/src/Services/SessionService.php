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

    // Phase 3.3: پارامترهای اختیاری برای تزریق mock در تست واحد (بدون تماس
    // واقعی با دیتابیس)؛ همه‌ی فراخوان‌های production با new SessionService()
    // بدون آرگومان بدون تغییر کار می‌کنند.
    public function __construct(?SessionRepository $sessionRepository = null, ?UserRepository $userRepository = null) {
        $this->sessionRepository = $sessionRepository ?? new SessionRepository();
        $this->userRepository = $userRepository ?? new UserRepository();
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
                // همان دستگاه - طبق تصمیم I-05، هر login موفق (حتی از همان
                // دستگاه) هر دو توکن را کاملاً تازه صادر می‌کند، نه reuse
                // توکن قبلی (رفتار قبلی) — login با رمز واقعی، قوی‌ترین نوع
                // احراز هویت است، پس نباید ضعیف‌تر از یک refresh معمولی رفتار کند.
                $tokens = $this->generateTokenPair();
                $this->sessionRepository->rotateTokens(
                    (int)$existingSession['id'],
                    $tokens['accessToken'],
                    $tokens['accessTokenExpiresAt'],
                    $tokens['refreshToken'],
                    $tokens['refreshTokenExpiresAt']
                );
                $this->logActivity($username, 'LOGIN', $deviceId, $ipAddress, $userType);
                return [
                    'success' => true,
                    'message' => 'جلسه موجود به‌روزرسانی شد',
                    'session_id' => $existingSession['id'],
                    'session_token' => $tokens['accessToken'],
                    'access_token_expires_in' => SessionRepository::ACCESS_TOKEN_TTL_SECONDS,
                    'refresh_token' => $tokens['refreshToken'],
                    'refresh_token_expires_in' => SessionRepository::REFRESH_TOKEN_TTL_SECONDS,
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

        $tokens = $this->generateTokenPair();

        $sessionId = $this->sessionRepository->createSession([
            'username' => $username,
            'device_id' => $deviceId,
            'device_model' => $deviceModel,
            'android_version' => $androidVersion,
            'app_version' => $appVersion,
            'ip_address' => $ipAddress,
            'userType' => $userType,
            'session_token' => $tokens['accessToken'],
            'access_token_expires_at' => $tokens['accessTokenExpiresAt'],
            'refresh_token' => $tokens['refreshToken'],
            'refresh_token_expires_at' => $tokens['refreshTokenExpiresAt'],
        ]);

        $this->logActivity($username, 'LOGIN', $deviceId, $ipAddress, $userType);

        return [
            'success' => true,
            'message' => 'جلسه با موفقیت ایجاد شد',
            'session_id' => $sessionId,
            'session_token' => $tokens['accessToken'],
            'access_token_expires_in' => SessionRepository::ACCESS_TOKEN_TTL_SECONDS,
            'refresh_token' => $tokens['refreshToken'],
            'refresh_token_expires_in' => SessionRepository::REFRESH_TOKEN_TTL_SECONDS,
        ];
    }

    /**
     * تولید یک جفت توکن تازه (access + refresh) — برای login موفق (جدید یا
     * همان دستگاه) و POST /auth/refresh مشترک است (I-05).
     */
    private function generateTokenPair(): array {
        $now = time();
        return [
            'accessToken' => bin2hex(random_bytes(32)),
            'accessTokenExpiresAt' => date('Y-m-d H:i:s', $now + SessionRepository::ACCESS_TOKEN_TTL_SECONDS),
            'refreshToken' => bin2hex(random_bytes(32)),
            'refreshTokenExpiresAt' => date('Y-m-d H:i:s', $now + SessionRepository::REFRESH_TOKEN_TTL_SECONDS),
        ];
    }

    /**
     * تمدید access token با استفاده از refresh token (POST /auth/refresh،
     * I-05). هر دو توکن rotate می‌شوند (sliding refresh expiry). اگر توکن
     * داده‌شده دقیقاً با آخرین refresh token صادرشده برای این کاربر/دستگاه
     * مطابقت نداشته باشد (نه لزوماً نامعتبر بودن ساده، بلکه احتمال استفاده‌ی
     * دوباره از یک توکن قبلاً rotate‌شده — نشانه‌ی سرقت)، طبق تصمیم محصولی،
     * تمام نشست‌های فعال این کاربر (نه فقط همین دستگاه) باطل می‌شوند.
     */
    public function refreshTokens(string $username, string $deviceId, string $refreshToken): array {
        if ($username === '' || $deviceId === '' || $refreshToken === '') {
            return ['success' => false, 'message' => 'پارامترهای ورودی نامعتبر است', 'http_code' => 400];
        }

        $session = $this->sessionRepository->getActiveSessionByDevice($username, $deviceId);
        if ($session === null) {
            return ['success' => false, 'message' => 'نشست یافت نشد. لطفاً دوباره وارد شوید.', 'http_code' => 401];
        }

        // session['refresh_token'] از دیتابیس هش‌شده برمی‌گردد (Phase 2.1)،
        // پس طرف مقابل مقایسه هم باید هش شود.
        $storedRefreshTokenHash = (string)($session['refresh_token'] ?? '');
        if ($storedRefreshTokenHash === '' || !hash_equals($storedRefreshTokenHash, SessionRepository::hashToken($refreshToken))) {
            $this->sessionRepository->deactivateAllSessions($username);
            $this->logActivity($username, 'REFRESH_TOKEN_REUSE_DETECTED', $deviceId);
            SecurityAlerter::getInstance()->alert(
                'REFRESH_TOKEN_REUSE_DETECTED',
                "refresh token غیرمعتبر/استفاده‌شده برای «{$username}» از دستگاه «{$deviceId}» ارسال شد — احتمال سرقت توکن. تمام نشست‌های این کاربر باطل شدند.",
                'refresh_reuse_' . strtolower($username)
            );
            return ['success' => false, 'message' => 'نشست به دلایل امنیتی باطل شد. لطفاً دوباره وارد شوید.', 'http_code' => 401];
        }

        $refreshExpiresAtRaw = $session['refresh_token_expires_at'] ?? null;
        if ($refreshExpiresAtRaw === null || strtotime((string)$refreshExpiresAtRaw) < time()) {
            return ['success' => false, 'message' => 'نشست منقضی شده است. لطفاً دوباره وارد شوید.', 'http_code' => 401];
        }

        $tokens = $this->generateTokenPair();
        $this->sessionRepository->rotateTokens(
            (int)$session['id'],
            $tokens['accessToken'],
            $tokens['accessTokenExpiresAt'],
            $tokens['refreshToken'],
            $tokens['refreshTokenExpiresAt']
        );

        return [
            'success' => true,
            'session_token' => $tokens['accessToken'],
            'access_token_expires_in' => SessionRepository::ACCESS_TOKEN_TTL_SECONDS,
            'refresh_token' => $tokens['refreshToken'],
            'refresh_token_expires_in' => SessionRepository::REFRESH_TOKEN_TTL_SECONDS,
            'userType' => $session['userType'] ?? null,
            'http_code' => 200,
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
            // session_token/refresh_token از دیتابیس هش‌شده برمی‌گردند
            // (Phase 2.1) و قابل بازگرداندن به کلاینت یا reuse نیستند؛ مثل
            // createMobileSession (تصمیم I-05)، به‌جای reuse یک جفت توکن
            // کاملاً تازه صادر و rotate می‌شود.
            $tokens = $this->generateTokenPair();
            $this->sessionRepository->rotateTokens(
                (int)$existingSession['id'],
                $tokens['accessToken'],
                $tokens['accessTokenExpiresAt'],
                $tokens['refreshToken'],
                $tokens['refreshTokenExpiresAt']
            );
            return [
                'success' => true,
                'message' => 'جلسه موجود به‌روزرسانی شد',
                'session_id' => $existingSession['id'],
                'session_token' => $tokens['accessToken']
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
     * فقط روی مسیر شکست validateAndGetUserType صدا زده می‌شود، برای تمایز
     * «access token منقضی» از «نشست کاملاً نامعتبر» (I-05) — نگاه کنید به
     * SessionRepository::isAccessTokenExpiredButSessionActive.
     */
    public function isAccessTokenExpiredButSessionActive(string $username, string $deviceId, string $token): bool {
        if ($username === '' || $deviceId === '' || $token === '') {
            return false;
        }
        return $this->sessionRepository->isAccessTokenExpiredButSessionActive($username, $deviceId, $token);
    }

    /**
     * نسخه‌ی بهینه‌شده‌ی isValidToken برای گیت AuthenticatesRequests (P-01):
     * اعتبار نشست و userType را با یک کوئری واحد برمی‌گرداند (به‌جای
     * isValidToken + یک SELECT جداگانه‌ی UserRepository)، و last_activity را
     * throttled به‌روزرسانی می‌کند (نه در هر تک درخواست) چون این متد در «هر»
     * درخواست API احرازشده صدا زده می‌شود.
     *
     * @return string|null userType در صورت معتبر بودن نشست، در غیر این صورت null
     */
    public function validateAndGetUserType(string $username, string $deviceId, string $token): ?string {
        if ($username === '' || $deviceId === '' || $token === '') {
            return null;
        }

        $userType = $this->sessionRepository->validateTokenAndGetUserType($username, $deviceId, $token);
        if ($userType !== null) {
            $this->sessionRepository->touchLastActivityThrottled($username, $deviceId);
        }
        return $userType;
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
     * غیرفعال کردن نشست‌های منقضی (بی‌فعالیت طولانی)
     */
    public function cleanupExpiredSessions(): int {
        return $this->sessionRepository->cleanupExpiredSessions();
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
