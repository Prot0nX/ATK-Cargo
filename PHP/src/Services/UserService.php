<?php
// PHP/src/Services/UserService.php

declare(strict_types=1);

namespace App\Services;

use App\Repositories\UserRepository;
use App\Exceptions\ApiException;

class UserService {
    private UserRepository $userRepository;

    public function __construct() {
        $this->userRepository = new UserRepository();
    }

    /**
     * بررسی اعتبار کاربر و احراز هویت
     *
     * استراتژی هشینگ سه‌مسیره (DEEP_CODE_AUDIT.md #Phase3.11):
     * کلاینت از این مرحله به بعد رمز خام (نه SHA-256 آن) می‌فرستد، اما تا
     * وقتی همه‌ی دستگاه‌های یک کاربر به نسخه‌ی جدید آپدیت نشده‌اند، دستگاه‌های
     * قدیمی همچنان SHA-256(رمز) می‌فرستند — پس هر دو باید امتحان شوند:
     * ۱. bcrypt(رمز خام) — طرح جدید.
     * ۲. bcrypt(SHA-256(رمز)) — طرح قبلی (کلاینت هنوز آپدیت نشده)؛ در موفقیت،
     *    هش دیتابیس بی‌صدا به حالت ۱ ارتقا می‌یابد (Silent Migration) — از آن
     *    پس فقط دستگاه‌های آپدیت‌شده‌ی همان کاربر می‌توانند وارد شوند تا
     *    دوباره یک لاگین موفق با کلاینت جدید آن را عبور دهد.
     * ۳. SHA-256 یا متن خام مستقیم در دیتابیس (سیستم بسیار قدیمی، پیش از
     *    bcrypt) — با رمز خام و SHA-256(رمز) هر دو مقایسه و روی موفقیت به
     *    bcrypt(رمز خام) ارتقا می‌یابد.
     */
    public function verifyCredentials(string $username, string $password): ?array {
        $user = $this->userRepository->getByUsername($username);
        if (!$user) {
            return null;
        }

        $storedPassword = $user['password'];

        // ===== تشخیص نوع هش: bcrypt پیشوند $2y$ یا $2a$ دارد =====
        // از بررسی password_get_info['algo'] اجتناب می‌شود چون در PHP 8
        // برای هش‌های ناشناخته null برمی‌گرداند (نه 0 مثل PHP 7) و
        // باعث می‌شود شرط algo !== 0 به اشتباه true شود.
        $isBcrypt = (
            strlen($storedPassword) >= 60
            && (str_starts_with($storedPassword, '$2y$') || str_starts_with($storedPassword, '$2a$'))
        );

        if ($isBcrypt) {
            // ===== حالت ۱: bcrypt(رمز خام) — طرح جدید =====
            if (password_verify($password, $storedPassword)) {
                if (password_needs_rehash($storedPassword, PASSWORD_BCRYPT, ['cost' => 12])) {
                    $this->userRepository->updatePassword($user['id'], password_hash($password, PASSWORD_BCRYPT, ['cost' => 12]));
                }
                return $user;
            }

            // ===== حالت ۲: bcrypt(SHA-256(رمز)) — طرح قبلی، کلاینت هنوز آپدیت نشده =====
            if (password_verify(hash('sha256', $password), $storedPassword)) {
                $this->userRepository->updatePassword($user['id'], password_hash($password, PASSWORD_BCRYPT, ['cost' => 12]));
                return $user;
            }

            return null;
        }

        // ===== حالت ۳: SHA-256 یا متن خام مستقیم در دیتابیس (سیستم بسیار قدیمی) =====
        if (hash_equals($storedPassword, $password) || hash_equals($storedPassword, hash('sha256', $password))) {
            $upgradedHash = password_hash($password, PASSWORD_BCRYPT, ['cost' => 12]);
            $this->userRepository->updatePassword($user['id'], $upgradedHash);
            return $user;
        }

        return null;
    }

    public function getAllUsers(): array {
        return $this->userRepository->getAll();
    }

    /**
     * پروفایل خودِ کاربر احرازشده — بدون افشای کل فهرست کاربران/نقش‌ها به
     * کاربرانی که مجوز manage_users ندارند (Phase 1.3 / DEEP_CODE_AUDIT.md).
     */
    public function getSelfProfile(string $username): ?array {
        $user = $this->userRepository->getByUsername($username);
        if (!$user) {
            return null;
        }
        unset($user['password']);
        return $user;
    }

    /**
     * فهرست کاربران admin — فقط id/username/fullName/userType، برای پرکردن
     * مقصدهای «چت با مدیر» که باید برای هر کاربر احرازشده در دسترس باشد.
     */
    public function getAdminUsers(): array {
        return $this->userRepository->getByUserType('admin');
    }

    /**
     * دریافت تمامی کاربران به همراه وضعیت آنلاین و آخرین فعالیت.
     *
     * منطق آنلاین بودن: اگر `last_activity` در ۵ دقیقه گذشته ثبت شده باشد،
     * کاربر آنلاین در نظر گرفته می‌شود (هماهنگ با OnlineUsersController).
     */
    public function getAllUsersWithStatus(): array {
        $users = $this->userRepository->getAll();
        $sessionRepo = new \App\Repositories\SessionRepository();
        $latestSessions = $sessionRepo->getLatestSessionsForAllUsers();

        // ساخت Map از strtolower(username) → session برای تطبیق دقیق و Case-Insensitive
        $sessionMap = [];
        foreach ($latestSessions as $session) {
            if (!empty($session['username'])) {
                $sessionMap[strtolower((string)$session['username'])] = $session;
            }
        }

        $result = [];
        foreach ($users as $user) {
            $usernameKey = strtolower((string)$user['username']);
            $session     = $sessionMap[$usernameKey] ?? null;
            $isActive    = $session ? ((int)($session['is_active'] ?? 0) === 1) : false;
            $idleSeconds = $session ? (int)($session['idle_time'] ?? PHP_INT_MAX) : PHP_INT_MAX;

            // آنلاین بودن: داشتن نشست فعال + فعالیت در ۵ دقیقه اخیر (۳۰۰ ثانیه)
            $isOnline    = $isActive && ($idleSeconds <= 300);
            $idleMinutes = ($session && $idleSeconds < PHP_INT_MAX) ? (int)floor($idleSeconds / 60) : null;

            // استخراج تاریخ آخرین بازدید با فرمت شمسی در صورت امکان
            $rawLastActivity = $session['last_activity'] ?? $session['login_time'] ?? $user['updatedAt'] ?? $user['createdAt'] ?? null;
            $lastActivityFormatted = null;
            if (!empty($rawLastActivity)) {
                $timestamp = strtotime((string)$rawLastActivity);
                if ($timestamp > 0) {
                    if (function_exists('jdate')) {
                        $lastActivityFormatted = jdate('Y/m/d H:i', $timestamp);
                    } else {
                        $lastActivityFormatted = date('Y/m/d H:i', $timestamp);
                    }
                } else {
                    $lastActivityFormatted = (string)$rawLastActivity;
                }
            }

            $result[] = array_merge($user, [
                'is_online'     => $isOnline,
                'last_activity' => $lastActivityFormatted,
                'idle_minutes'  => $idleMinutes,
                'login_time'    => $session['login_time'] ?? null,
                'device_model'  => $session['device_model'] ?? null,
            ]);
        }

        return $result;
    }

    public function createUser(array $data, ?string $actorUsername = null): array {
        // فیلتر کردن و اعتبارسنجی مقادیر
        $username = trim($data['username']);
        $fullName = trim($data['fullName']);
        $password = $data['password'];
        $userType = trim($data['userType']);

        // بررسی تکراری نبودن نام کاربری
        if ($this->userRepository->getByUsername($username) !== null) {
            throw new ApiException('این نام کاربری قبلاً ثبت شده است', 400);
        }

        // ===== ذخیره پسورد با bcrypt برای کاربران جدید =====
        // پسورد ورودی در زمان ایجاد از پنل ادمین به‌عنوان متن خام دریافت می‌شود
        $hashedPassword = password_hash($password, PASSWORD_BCRYPT, ['cost' => 12]);

        $userId = $this->userRepository->create([
            'username' => $username,
            'fullName' => $fullName,
            'password' => $hashedPassword,
            'userType' => $userType
        ]);

        if ($actorUsername !== null) {
            AuditLogger::log($actorUsername, 'createUser', 'user', (string)$userId, [
                'username' => $username,
                'fullName' => $fullName,
                'userType' => $userType,
            ]);
        }

        return [
            'success' => true,
            'message' => 'کاربر جدید با موفقیت ایجاد شد',
            'userId' => $userId
        ];
    }

    public function updateUser(int $id, array $data, ?string $actorUsername = null): array {
        $user = $this->userRepository->getById($id);
        if (!$user) {
            throw new ApiException('کاربری با این شناسه یافت نشد', 404);
        }

        $updates = [];

        if (isset($data['username'])) {
            $newUsername = trim($data['username']);
            $existingUser = $this->userRepository->getByUsername($newUsername);
            if ($existingUser && $existingUser['id'] !== $id) {
                throw new ApiException('این نام کاربری قبلاً ثبت شده است', 400);
            }
            $updates['username'] = $newUsername;
        }

        if (isset($data['fullName'])) {
            $updates['fullName'] = trim($data['fullName']);
        }

        if (isset($data['password'])) {
            // ذخیره پسورد جدید با bcrypt
            $updates['password'] = password_hash($data['password'], PASSWORD_BCRYPT, ['cost' => 12]);
        }

        if (isset($data['userType'])) {
            $updates['userType'] = trim($data['userType']);
        }

        if (empty($updates)) {
            throw new ApiException('هیچ داده‌ای برای به‌روزرسانی ارائه نشده است', 400);
        }

        $this->userRepository->update($id, $updates);

        // پس از ویرایش موفق، جلسات قبلی کاربر را غیرفعال می‌کنیم
        $sessionRepo = new \App\Repositories\SessionRepository();
        $sessionRepo->deactivateAllSessions($user['username']);

        if ($actorUsername !== null) {
            // فقط نام فیلدهای تغییریافته ثبت می‌شود، نه مقدار رمز عبور
            AuditLogger::log($actorUsername, 'updateUser', 'user', (string)$id, [
                'changedFields' => array_keys($updates),
            ]);
        }

        return [
            'success' => true,
            'message' => 'اطلاعات کاربر با موفقیت به‌روزرسانی شد'
        ];
    }

    public function deleteUser(int $id, ?string $actorUsername = null): array {
        $user = $this->userRepository->getById($id);
        if (!$user) {
            throw new ApiException('کاربری با این شناسه یافت نشد', 404);
        }

        // بررسی تعداد مدیران
        if ($user['userType'] === 'admin') {
            $adminCount = $this->userRepository->getAdminCount();
            if ($adminCount <= 1) {
                throw new ApiException('حذف آخرین مدیر سیستم امکان‌پذیر نیست', 400);
            }
        }

        $this->userRepository->delete($id);

        // غیرفعال کردن تمامی جلسات کاربر حذف شده
        $sessionRepo = new \App\Repositories\SessionRepository();
        $sessionRepo->deactivateAllSessions($user['username']);

        if ($actorUsername !== null) {
            AuditLogger::log($actorUsername, 'deleteUser', 'user', (string)$id, [
                'username' => $user['username'],
                'userType' => $user['userType'],
            ]);
        }

        return [
            'success' => true,
            'message' => 'کاربر با موفقیت حذف شد'
        ];
    }
}
