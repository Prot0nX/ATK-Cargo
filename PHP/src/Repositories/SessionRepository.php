<?php
// PHP/src/Repositories/SessionRepository.php

declare(strict_types=1);

namespace App\Repositories;

use App\Core\Database;
use PDO;

class SessionRepository {
    // پس از این مدت بی‌فعالیتی، نشست منقضی در نظر گرفته می‌شود — قبلاً هیچ
    // محدودیتی روی last_activity اعمال نمی‌شد و یک session_token سرقت‌شده
    // (از دستگاه گم‌شده/بکاپ/لاگ) تا ابد معتبر می‌ماند (S-05).
    public const SESSION_TIMEOUT_SECONDS = 86400; // ۲۴ ساعت

    // I-05: access token (ستون فعلی session_token) حالا کوتاه‌مدت است و فقط
    // برای درخواست‌های واقعی API استفاده می‌شود؛ refresh token بلندمدت‌تر و
    // فقط برای گرفتن access token جدید از endpoint اختصاصی refresh فرستاده
    // می‌شود. هر دو با هر رفرش موفق rotate می‌شوند (sliding).
    public const ACCESS_TOKEN_TTL_SECONDS = 1800;   // ۳۰ دقیقه
    public const REFRESH_TOKEN_TTL_SECONDS = 86400; // ۲۴ ساعت

    private PDO $db;

    public function __construct() {
        $this->db = Database::getInstance()->getPdoConnection();
    }

    /**
     * توکن‌ها به‌صورت hash در ستون‌های session_token/refresh_token ذخیره
     * می‌شوند، نه plaintext — یک SQL Injection/بکاپ لورفته/دسترسی خواندنی DB
     * دیگر به معنای جعل هویت فوری همه‌ی کاربران نیست (DEEP_CODE_AUDIT.md
     * #Phase2.1). چون خودِ توکن ۲۵۶ بیتی و تصادفی است (نه رمز کاربر)، SHA-256
     * ساده کافی است — نیازی به bcrypt/salt/iteration نیست، و lookup با
     * ایندکس equality (نه bcrypt) کارایی قبلی را حفظ می‌کند.
     */
    public static function hashToken(string $token): string {
        return hash('sha256', $token);
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
     * بررسی معتبر بودن دقیق یک جلسه (نام کاربری + دستگاه + توکن + فعال بودن +
     * عدم انقضا بر اساس آخرین فعالیت) برای احراز هویت درخواست‌های API (مثل
     * app_api.php) استفاده می‌شود.
     */
    public function isValidToken(string $username, string $deviceId, string $token): bool {
        $stmt = $this->db->prepare("
            SELECT id FROM user_sessions
            WHERE username = :username AND device_id = :device_id
              AND session_token = :token AND is_active = 1
              AND last_activity > (NOW() - INTERVAL " . self::SESSION_TIMEOUT_SECONDS . " SECOND)
              AND access_token_expires_at > NOW()
            LIMIT 1
        ");
        $stmt->execute([
            ':username' => $username,
            ':device_id' => $deviceId,
            ':token' => self::hashToken($token),
        ]);
        return (bool)$stmt->fetch();
    }

    /**
     * برای تشخیص «فقط access token منقضی شده» (باید silent refresh شود) در
     * مقابل «کل نشست نامعتبر است» (باید login کامل انجام شود) — دقیقاً همان
     * شرط‌های isValidToken ولی بدون بررسی access_token_expires_at. فقط روی
     * مسیر شکست isValidToken/validateTokenAndGetUserType صدا زده می‌شود، نه
     * در هر درخواست، پس هزینه‌ی یک کوئری اضافه در حالت موفق ندارد (I-05).
     */
    public function isAccessTokenExpiredButSessionActive(string $username, string $deviceId, string $token): bool {
        $stmt = $this->db->prepare("
            SELECT id FROM user_sessions
            WHERE username = :username AND device_id = :device_id
              AND session_token = :token AND is_active = 1
              AND last_activity > (NOW() - INTERVAL " . self::SESSION_TIMEOUT_SECONDS . " SECOND)
              AND access_token_expires_at <= NOW()
            LIMIT 1
        ");
        $stmt->execute([
            ':username' => $username,
            ':device_id' => $deviceId,
            ':token' => self::hashToken($token),
        ]);
        return (bool)$stmt->fetch();
    }

    /**
     * نسخه‌ی بهینه‌ی isValidToken برای گیت AuthenticatesRequests که علاوه بر
     * اعتبار نشست، userType را هم برمی‌گرداند (P-01). userType در همین جدول
     * (نه Users) ذخیره است — در زمان createSession کپی می‌شود و با هر تغییر
     * نوع کاربری، UserService::updateUser تمام نشست‌های کاربر را
     * deactivateAllSessions می‌کند؛ پس همیشه تازه است و نیازی به JOIN/کوئری
     * دوم روی Users نیست. قبلاً هر درخواست احرازشده ۳ کوئری می‌زد (این
     * SELECT + UPDATE last_activity + یک SELECT * FROM Users جداگانه در
     * AuthenticatesRequests)؛ اکنون این متد همان یک کوئری قبلی را با یک
     * ستون اضافه جایگزین آن SELECT سوم می‌کند.
     *
     * @return string|null userType در صورت معتبر بودن نشست، در غیر این صورت null
     */
    public function validateTokenAndGetUserType(string $username, string $deviceId, string $token): ?string {
        $stmt = $this->db->prepare("
            SELECT userType FROM user_sessions
            WHERE username = :username AND device_id = :device_id
              AND session_token = :token AND is_active = 1
              AND last_activity > (NOW() - INTERVAL " . self::SESSION_TIMEOUT_SECONDS . " SECOND)
              AND access_token_expires_at > NOW()
            LIMIT 1
        ");
        $stmt->execute([
            ':username' => $username,
            ':device_id' => $deviceId,
            ':token' => self::hashToken($token),
        ]);
        $row = $stmt->fetch();
        return $row ? (string)($row['userType'] ?? '') : null;
    }

    /**
     * به‌روزرسانی last_activity با throttle — فقط اگر بیش از ۶۰ ثانیه از
     * آخرین به‌روزرسانی گذشته باشد واقعاً UPDATE می‌زند. این متد مخصوص گیت
     * احراز هویت است (که در «هر» درخواست API صدا زده می‌شود)؛ updateLastActivity
     * معمولی (بدون throttle) برای heartbeat صریح دست نخورده باقی می‌ماند تا
     * معنای «تازه‌بودن» آن تغییر نکند.
     */
    public function touchLastActivityThrottled(string $username, string $deviceId): void {
        $stmt = $this->db->prepare("
            UPDATE user_sessions
            SET last_activity = NOW()
            WHERE username = :username AND device_id = :device_id AND is_active = 1
              AND last_activity < (NOW() - INTERVAL 60 SECOND)
        ");
        $stmt->execute([
            ':username' => $username,
            ':device_id' => $deviceId,
        ]);
    }

    /**
     * غیرفعال کردن نشست‌های منقضی (بی‌فعالیت بیش از SESSION_TIMEOUT_SECONDS)
     * — قبلاً این متد صدا زده می‌شد (OnlineUsersController::cleanup_inactive)
     * بدون اینکه هیچ‌جا تعریف شده باشد (B-02)، به همین دلیل هیچ نشستی هرگز
     * منقضی نمی‌شد.
     */
    public function cleanupExpiredSessions(): int {
        $stmt = $this->db->prepare("
            UPDATE user_sessions
            SET is_active = 0, logout_time = NOW()
            WHERE is_active = 1
              AND last_activity <= (NOW() - INTERVAL " . self::SESSION_TIMEOUT_SECONDS . " SECOND)
        ");
        $stmt->execute();
        return $stmt->rowCount();
    }

    /**
     * دریافت جلسه فعال بر اساس نام کاربری و دستگاه
     */
    public function getActiveSessionByDevice(string $username, string $deviceId): ?array {
        $stmt = $this->db->prepare("
            SELECT id, session_token, device_id, userType, refresh_token, refresh_token_expires_at
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
        // ستون‌های access_token_expires_at/refresh_token/refresh_token_expires_at
        // اختیاری‌اند (NULL اگر ارسال نشوند) — createWebSession (پنل وب، خارج
        // از محدوده‌ی I-05) این کلیدها را نمی‌فرستد و رفتار قبلی‌اش را حفظ
        // می‌کند؛ فقط createMobileSession این مقادیر را پر می‌کند.
        $stmt = $this->db->prepare("
            INSERT INTO user_sessions
            (username, device_id, device_model, android_version, app_version, login_time, last_activity, is_active, ip_address, userType, session_token, access_token_expires_at, refresh_token, refresh_token_expires_at)
            VALUES (:username, :device_id, :device_model, :android_version, :app_version, NOW(), NOW(), 1, :ip_address, :userType, :session_token, :access_token_expires_at, :refresh_token, :refresh_token_expires_at)
        ");

        $stmt->execute([
            ':username' => $data['username'],
            ':device_id' => $data['device_id'],
            ':device_model' => $data['device_model'],
            ':android_version' => $data['android_version'],
            ':app_version' => $data['app_version'] ?? null,
            ':ip_address' => $data['ip_address'],
            ':userType' => $data['userType'],
            ':session_token' => self::hashToken($data['session_token']),
            ':access_token_expires_at' => $data['access_token_expires_at'] ?? null,
            ':refresh_token' => isset($data['refresh_token']) ? self::hashToken($data['refresh_token']) : null,
            ':refresh_token_expires_at' => $data['refresh_token_expires_at'] ?? null,
        ]);

        return (int)$this->db->lastInsertId();
    }

    /**
     * چرخش (rotation) هر دو توکن روی یک نشست موجود — برای login موفق مجدد از
     * همان دستگاه (تصمیم: هر login هر دو توکن را کاملاً تازه صادر می‌کند) و
     * برای POST /auth/refresh (I-05).
     */
    public function rotateTokens(
        int $sessionId,
        string $accessToken,
        string $accessTokenExpiresAt,
        string $refreshToken,
        string $refreshTokenExpiresAt
    ): bool {
        $stmt = $this->db->prepare("
            UPDATE user_sessions
            SET session_token = :access_token,
                access_token_expires_at = :access_token_expires_at,
                refresh_token = :refresh_token,
                refresh_token_expires_at = :refresh_token_expires_at,
                last_activity = NOW(),
                is_active = 1
            WHERE id = :id
        ");
        return $stmt->execute([
            ':access_token' => self::hashToken($accessToken),
            ':access_token_expires_at' => $accessTokenExpiresAt,
            ':refresh_token' => self::hashToken($refreshToken),
            ':refresh_token_expires_at' => $refreshTokenExpiresAt,
            ':id' => $sessionId,
        ]);
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
